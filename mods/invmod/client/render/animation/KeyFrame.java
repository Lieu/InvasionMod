package mods.invmod.client.render.animation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import mods.invmod.common.util.MathUtil;

/**
 * Represents a single animation key frame, storing timestamp, angles
 * and position. Is immutable.
 * 
 * @author Lieu
 */
public class KeyFrame
{
	private float time;
	private float rotX;
	private float rotY;
	private float rotZ;
	private float posX;
	private float posY;
	private float posZ;
	private InterpType interpType;
	private float[][] mods;
	private boolean hasPos;
	
	/**
	 * @param time The absolute timestamp of this frame
	 * @param rotX Absolute x-axis rotation
	 * @param rotY Absolute y-axis rotation
	 * @param rotZ Absolute z-axis rotation
	 * @param interpType The kind of interpolation to use between this frame and the next frame
	 */
	public KeyFrame(float time, float rotX, float rotY, float rotZ, InterpType interpType)
	{
		this(time, rotX, rotY, rotZ, 0, 0, 0, interpType);
		hasPos = false;
	}
	
	public KeyFrame(float time, float rotX, float rotY, float rotZ, float posX, float posY, float posZ, InterpType interpType)
	{
		this.time = time;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
		this.interpType = interpType;
		hasPos = true;
	}
	
	public float getTime()
	{
		return time;
	}
	
	public float getRotX()
	{
		return rotX;
	}
	
	public float getRotY()
	{
		return rotY;
	}
	
	public float getRotZ()
	{
		return rotZ;
	}
	
	public float getPosX()
	{
		return posX;
	}
	
	public float getPosY()
	{
		return posY;
	}
	
	public float getPosZ()
	{
		return posZ;
	}
	
	public InterpType getInterpType()
	{
		return interpType;
	}
	
	public boolean hasPos()
	{
		return hasPos;
	}
	
	public String toString()
	{
		return "(" + time + ", " + rotX + ", " + rotY + ", " + rotZ + ")";
	}
	
	public static List<KeyFrame> cloneFrames(List<KeyFrame> keyFrames)
	{
		return new ArrayList<KeyFrame>(keyFrames);
	}
	
	public static void toRadians(List<KeyFrame> keyFrames)
	{
		ListIterator<KeyFrame> iter = keyFrames.listIterator();
		while(iter.hasNext())
		{
			float radDeg = (float)Math.PI / 180;
			KeyFrame keyFrame = iter.next();
			KeyFrame newFrame = new KeyFrame(keyFrame.getTime(), keyFrame.getRotX() * radDeg, keyFrame.getRotY() * radDeg, keyFrame.getRotZ() * radDeg,
											 keyFrame.getPosX(), keyFrame.getPosY(), keyFrame.getPosZ(), keyFrame.getInterpType());
			newFrame.hasPos = keyFrame.hasPos;
			iter.set(newFrame);
		}
	}
	
	public static void mirrorFramesX(List<KeyFrame> keyFrames)
	{
		ListIterator<KeyFrame> iter = keyFrames.listIterator();
		while(iter.hasNext())
		{
			KeyFrame keyFrame = iter.next();
			KeyFrame newFrame = new KeyFrame(keyFrame.getTime(), keyFrame.getRotX(), -keyFrame.getRotY(), -keyFrame.getRotZ(),
											 -keyFrame.getPosX(), keyFrame.getPosY(), keyFrame.getPosZ(), keyFrame.getInterpType());
			newFrame.hasPos = keyFrame.hasPos;
			iter.set(newFrame);
		}
	}
	
	public static void mirrorFramesY(List<KeyFrame> keyFrames)
	{
		ListIterator<KeyFrame> iter = keyFrames.listIterator();
		while(iter.hasNext())
		{
			KeyFrame keyFrame = iter.next();
			KeyFrame newFrame = new KeyFrame(keyFrame.getTime(), -keyFrame.getRotX(), keyFrame.getRotY(), -keyFrame.getRotZ(),
					 						 keyFrame.getPosX(), -keyFrame.getPosY(), keyFrame.getPosZ(), keyFrame.getInterpType());
			newFrame.hasPos = keyFrame.hasPos;
			iter.set(newFrame);
		}
	}
	
	public static void mirrorFramesZ(List<KeyFrame> keyFrames)
	{
		ListIterator<KeyFrame> iter = keyFrames.listIterator();
		while(iter.hasNext())
		{
			KeyFrame keyFrame = iter.next();
			KeyFrame newFrame = new KeyFrame(keyFrame.getTime(), -keyFrame.getRotX(), -keyFrame.getRotY(), keyFrame.getRotZ(),
											 keyFrame.getPosX(), keyFrame.getPosY(), -keyFrame.getPosZ(), keyFrame.getInterpType());
			newFrame.hasPos = keyFrame.hasPos;
			iter.set(newFrame);
		}
	}
	
	public static void offsetFramesCircular(List<KeyFrame> keyFrames, float start, float end, float offset)
	{
		if(keyFrames.size() < 1)
			return;
		
		float diff = end - start;
		offset = offset % diff;
		float k1 = end - offset; // k1 = the time in the old list that is the start/end boundary in the new list
		
		List<KeyFrame> copy = cloneFrames(keyFrames);
		keyFrames.clear();
		KeyFrame currFrame = null;
		ListIterator<KeyFrame> iter = copy.listIterator();
		
		// Add elements prior to t=begin
		while(iter.hasNext())
		{
			currFrame = iter.next();
			if(currFrame.getTime() < start)
				keyFrames.add(currFrame);
			else
				break;
		}
		
		// Skip over and store elements between begin and k1
		List<KeyFrame> buffer = new ArrayList<KeyFrame>();
		buffer.add(currFrame);
		while(iter.hasNext())
		{
			currFrame = iter.next();
			if(currFrame.getTime() >= k1)
				break;
			else
				buffer.add(currFrame);
		}
		
		// Create frames at t=begin and t=end if no frame will end at those boundaries
		KeyFrame fencepostStart;
		if(!MathUtil.floatEquals(currFrame.getTime(), k1, 0.001F))
		{
			iter.previous(); // Go back; fencepost is the new current frame
			KeyFrame prev = iter.previous(); 
			// Interpolate values
			float dt = k1 - prev.getTime();
			float dtFrame = currFrame.getTime() - prev.getTime();
			float r = dt / dtFrame;
			float x = prev.getRotX() + r * (currFrame.getRotX() - prev.getRotX());
			float y = prev.getRotY() + r * (currFrame.getRotY() - prev.getRotY());
			float z = prev.getRotZ() + r * (currFrame.getRotZ() - prev.getRotZ());
			fencepostStart = new KeyFrame(start, x, y, z, InterpType.LINEAR);
		}
		else
		{
			fencepostStart = currFrame;
		}
		
		// Set a frame at t=begin
		keyFrames.add(fencepostStart);
		
		// Add frames from k1 to end (k1 + offset = start = end)
		while(iter.hasNext())
		{
			currFrame = iter.next();
			if(currFrame.getTime() <= end)
			{
				float t = currFrame.getTime() + offset - diff;
				KeyFrame newFrame = new KeyFrame(t, currFrame.getRotX(), currFrame.getRotY(), currFrame.getRotZ(), currFrame.getPosX(),
												 currFrame.getPosY(), currFrame.getPosZ(), InterpType.LINEAR);
				newFrame.hasPos = currFrame.hasPos;
				keyFrames.add(newFrame);
			}
			else
			{
				iter.previous();
				break;
			}
		}
		
		// Add stored frames from begin to k1
		Iterator<KeyFrame> iter2 = buffer.iterator();
		while(iter2.hasNext())
		{
			currFrame = iter2.next();
			float t = currFrame.getTime() + offset;
			KeyFrame newFrame = new KeyFrame(t, currFrame.getRotX(), currFrame.getRotY(), currFrame.getRotZ(), currFrame.getPosX(),
					 						 currFrame.getPosY(), currFrame.getPosZ(), InterpType.LINEAR);
			newFrame.hasPos = currFrame.hasPos;
			keyFrames.add(newFrame);
		}
		
		keyFrames.add(new KeyFrame(end, fencepostStart.getRotX(), fencepostStart.getRotY(), fencepostStart.getRotZ(), InterpType.LINEAR));
		
		// Add the rest of the frames after end
		while(iter.hasNext())
		{
			keyFrames.add(iter.next());
		}
	}
}
