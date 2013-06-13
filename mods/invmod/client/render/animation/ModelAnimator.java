package mods.invmod.client.render.animation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mods.invmod.common.util.Triplet;
import net.minecraft.client.model.ModelRenderer;

/**
 * This class applies animations to model parts based on keyframe animation.
 * Takes inputs of series of keyframes and parts to apply them to.
 * 
 * @author Lieu
 */
public class ModelAnimator<T extends Enum<T>>
{
	private List<Triplet<ModelRenderer, Integer, List<KeyFrame>>> parts; // (Model part, last keyframe index in list, list of keyframes)
	private float animationPeriod;
	
	public ModelAnimator()
	{
		this(1.0F);
	}
	
	public ModelAnimator(float animationPeriod)
	{
		this.animationPeriod = animationPeriod;
		parts = new ArrayList<Triplet<ModelRenderer, Integer, List<KeyFrame>>>(1);
	}
	
	public ModelAnimator(Map<T, ModelRenderer> modelParts, Animation<T> animation)
	{
		animationPeriod = animation.getAnimationPeriod();
		parts = new ArrayList<Triplet<ModelRenderer, Integer, List<KeyFrame>>>(animation.getSkeletonType().getEnumConstants().length);
		for(Entry<T, ModelRenderer> entry : modelParts.entrySet())
		{
			List<KeyFrame> keyFrames = animation.getKeyFramesFor(entry.getKey());
			if(keyFrames == null)
				continue;
				//keyFrames = new ArrayList<KeyFrame>(0);
			
			parts.add(new Triplet<ModelRenderer, Integer, List<KeyFrame>>(entry.getValue(), 0, keyFrames));
		}
	}
	
	public void addPart(ModelRenderer part, List<KeyFrame> keyFrames)
	{
		if(validate(keyFrames))
		{
			parts.add(new Triplet<ModelRenderer, Integer, List<KeyFrame>>(part, 0, keyFrames));
		}
	}
	
	public void clearParts()
	{
		parts.clear();
	}
	
	public void updateAnimation(float newTime)
	{
		for(Triplet<ModelRenderer, Integer, List<KeyFrame>> entry : parts)
		{
			// Establish which frames are immediately previous and after the specified
			// newTime, using the previous update's index as a shortcut (entry.getVal3())
			int prevIndex = entry.getVal2();
			List<KeyFrame> keyFrames = entry.getVal3();
			KeyFrame prevFrame = keyFrames.get(prevIndex++);
			KeyFrame nextFrame = null;
			
			if(prevFrame.getTime() <= newTime) // newTime was after the very last frame
			{
				for(; prevIndex < keyFrames.size(); prevIndex++)
				{
					KeyFrame keyFrame = keyFrames.get(prevIndex);
					if(newTime < keyFrame.getTime())
					{
						nextFrame = keyFrame;
						prevIndex--;
						break;
					}
					else
					{
						prevFrame = keyFrame;
					}
				}
				if(prevIndex >= keyFrames.size())
				{
					prevIndex = keyFrames.size() - 1;
					nextFrame = keyFrames.get(0);
				}
			}
			else
			{
				prevIndex = 0;
				for(; prevIndex < keyFrames.size(); prevIndex++)
				{
					KeyFrame keyFrame = keyFrames.get(prevIndex);
					if(newTime < keyFrame.getTime()) // Requires first frame to be at time=0
					{
						nextFrame = keyFrame;
						prevIndex--;
						prevFrame = keyFrames.get(prevIndex);
						break;
					}
				}
			}
			entry.setVal2(prevIndex);
			interpolate(prevFrame, nextFrame, newTime, entry.getVal1());
		}
	}
	
	private void interpolate(KeyFrame prevFrame, KeyFrame nextFrame, float time, ModelRenderer part)
	{
		if(prevFrame.getInterpType() == InterpType.LINEAR)
		{
			// Do a simple linear interpolation
			float dtPrev = time - prevFrame.getTime(); // Gauranteed to be positive t if a keyframe on t=0 is enforced
			float dtFrame = nextFrame.getTime() - prevFrame.getTime();
			if(dtFrame < 0) // In the case when rolling past animation end
			{
				dtFrame += animationPeriod;
			}
			
			float r = dtPrev / dtFrame;
			part.rotateAngleX = prevFrame.getRotX() + r * (nextFrame.getRotX() - prevFrame.getRotX());
			part.rotateAngleY = prevFrame.getRotY() + r * (nextFrame.getRotY() - prevFrame.getRotY());
			part.rotateAngleZ = prevFrame.getRotZ() + r * (nextFrame.getRotZ() - prevFrame.getRotZ());
			
			if(prevFrame.hasPos())
			{
				if(nextFrame.hasPos())
				{
					part.rotationPointX = prevFrame.getPosX() + r * (nextFrame.getPosX() - prevFrame.getPosX());
					part.rotationPointY = prevFrame.getPosY() + r * (nextFrame.getPosY() - prevFrame.getPosY());
					part.rotationPointZ = prevFrame.getPosZ() + r * (nextFrame.getPosZ() - prevFrame.getPosZ());
				}
				else
				{
					part.rotationPointX = prevFrame.getPosX();
					part.rotationPointY = prevFrame.getPosY();
					part.rotationPointZ = prevFrame.getPosZ();
				}
			}
			
		}
	}
	
	/**
	 * Checks if a list of keyframes will work with the rest of this class. Returns
	 * true if format is correct. Requirements:
	 * 
	 * - Must have more than 1 keyframe (making it an actual animation)
	 * - First frame must be at time=0
	 * - Proceeding frames must be in order of ascending time (time > prev. time)
	 */
	private boolean validate(List<KeyFrame> keyFrames)
	{
		if(keyFrames.size() < 2)
			return false;
		
		if(keyFrames.get(0).getTime() != 0)
			return false;
		
		int prevTime = 0;
		for(int i = 1; i < keyFrames.size(); i++)
		{
			if(keyFrames.get(i).getTime() <= prevTime)
				return false;
		}
		
		return true;
	}
}
