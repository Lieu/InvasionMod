package mods.invmod.common.entity;

import mods.invmod.common.util.PosRotate3D;
import net.minecraft.world.World;

/**
 * Navigator designed for the burrower or other segmented worm-like entities.
 * 
 * @author Lieu
 */
public class NavigatorBurrower extends NavigatorParametric
{
	public NavigatorBurrower(EntityIMBurrower entity, IPathSource pathSource, int segments, int offset)
	{
		super(entity, pathSource);
		timePerTick = 0.05F;
		prevSegmentNodes = new PathNode[segments];
		activeSegmentNodes = new PathNode[segments];
		nextSegmentNodes = new PathNode[segments];
		segmentPathIndices = new int[segments];
		segmentTime = new int[segments];
		segmentOffsets = new int[segments];
		nodeChanged = false;
		
		for(int i = 0; i < segmentOffsets.length; i++)
			segmentOffsets[i] = (i + 1) * offset;
			
	}
	
	/**
	 * Returns the entity's absolute position and head angles, in terms of param.
	 */
	@Override
	protected PosRotate3D entityPositionAtParam(int param)
	{
		return calcAbsolutePositionAndRotation(param * timePerTick, prevNode, activeNode, nextNode);
	}
	

	protected PosRotate3D positionAtTime(int tick, PathNode start, PathNode middle, PathNode end)
	{
		PosRotate3D pos = calcPositionAndRotation(tick * timePerTick, start, middle, end);
		pos.setPosX(pos.getPosX() + middle.xCoord);
		pos.setPosY(pos.getPosY() + middle.yCoord);
		pos.setPosZ(pos.getPosZ() + middle.zCoord);
		return pos;
	}
	
	@Override
	protected boolean isReadyForNextNode(int ticks)
	{
		return ticks * timePerTick >= 1.0 ? true : false;
	}
	
	@Override
	protected void pathFollow(int time)
    {
		// Check index + 2 because there must be a known node in front
		int nextFrontIndex = path.getCurrentPathIndex() + 2;
		if(isReadyForNextNode(time))
		{
			if(nextFrontIndex < path.getCurrentPathLength())
			{
				timeParam = 0;
				path.setCurrentPathIndex(nextFrontIndex - 1);
				prevNode = activeNode;
				activeNode = nextNode;
				nextNode = path.getPathPointFromIndex(nextFrontIndex);
				nodeChanged = true;
			}
		}
		else
		{
			timeParam = time;
		}
    }
	
	protected void doSegmentFollowTo(int ticks, int segmentIndex)
	{
		// Correct parameter T
		ticks += segmentOffsets[segmentIndex];
		while(ticks <= 0) { ticks += 20; }
		
		// Check for node traversal
		// Check index + 2 because there must be a known node in front
		int nextFrontIndex = segmentPathIndices[segmentIndex] + 2;
		if(isReadyForNextNode(ticks))
		{
			if(nextFrontIndex < path.getCurrentPathLength())
			{
				segmentPathIndices[segmentIndex] = nextFrontIndex - 1;
				prevSegmentNodes[segmentIndex] = activeSegmentNodes[segmentIndex];
				activeSegmentNodes[segmentIndex] = nextSegmentNodes[segmentIndex];
				if(segmentPathIndices[segmentIndex] >= 0)
					nextSegmentNodes[segmentIndex] = path.getPathPointFromIndex(nextFrontIndex);
				else
					nextSegmentNodes[segmentIndex] = path.getPathPointFromIndex(0);
				
				segmentTime[segmentIndex] = 0;
			}
		}
		else
		{
			segmentTime[segmentIndex] = ticks;
		}
		
		//Calculate segment position and rotation and apply to entity
		if(segmentPathIndices[segmentIndex] >= 0)
		{
			PosRotate3D pos = positionAtTime(segmentTime[segmentIndex], prevSegmentNodes[segmentIndex], activeSegmentNodes[segmentIndex], nextSegmentNodes[segmentIndex]);
			((EntityIMBurrower)theEntity).setSegment(segmentIndex, pos);
			if(segmentTime[segmentIndex] == 0)
				((EntityIMBurrower)theEntity).setSegment(segmentIndex, pos);
		}
	}
	
	/**
	 * Moves the entity towards the defined position at param and updates the segments to follow.
	 */
	@Override
	protected void doMovementTo(int time)
	{
		PosRotate3D movePos = entityPositionAtParam(time);
    	theEntity.moveEntity(movePos.getPosX() - theEntity.posX, movePos.getPosY() - theEntity.posY, movePos.getPosZ() - theEntity.posZ);
    	((EntityIMBurrower)theEntity).setHeadRotation(movePos);
    	
    	if(nodeChanged)
    	{
    		((EntityIMBurrower)theEntity).setHeadRotation(movePos);
    		nodeChanged = false;
    	}
    	
    	// Check if the move was valid
    	if(Math.abs(theEntity.getDistanceSq(movePos.getPosX(), movePos.getPosY(), movePos.getPosZ())) < minMoveToleranceSq)
    	{
    		// Move segments too because move was valid
    		for(int segmentIndex = 0; segmentIndex < segmentPathIndices.length; segmentIndex++)
    			doSegmentFollowTo(time, segmentIndex);
    		
    		timeParam = time;
    		ticksStuck--;
    	}
    	else
    	{
    		ticksStuck++;
    	}
	}
	
	@Override
	public boolean noPath()
    {
        return path == null || path.getCurrentPathIndex() >= path.getCurrentPathLength() - 2; // -2 because of nextNode
    }
	
	@Override
	public boolean setPath(Path newPath, float speed)
    {
        if (newPath == null || newPath.getCurrentPathLength() < 2)
        {
        	path = null;
	        return false;
        }
        
        if(path == null)
        {
        	path = newPath;
        	activeNode = path.getPathPointFromIndex(0);
        	prevNode = activeNode;
        	nextNode = path.getPathPointFromIndex(1);
        	if(activeNode.action != PathAction.NONE)
        		nodeActionFinished = false;
        	
        	for(int i = 0; i < segmentPathIndices.length; i++)
        	{
        		if(activeSegmentNodes[i] == null)
        		{
        			activeSegmentNodes[i] = activeNode;
        			nextSegmentNodes[i] = activeNode;
        			segmentPathIndices[i] = 0;
        			segmentTime[i] = segmentOffsets[i];
        			while(segmentTime[i] < 0)
        			{
        				segmentTime[i] += 20;
        				segmentPathIndices[i]--;
        			}
        		}
        	}
        }

        int mainIndex = path.getCurrentPathIndex();
        if(newPath.getPathPointFromIndex(0).equals(activeNode))
        {
        	if(segmentPathIndices.length > 0)
        	{
        		int lowestIndex = segmentPathIndices[segmentPathIndices.length - 1];
        		if(lowestIndex < 0)
        			lowestIndex = 0;
        		path = extendPath(path, newPath, lowestIndex, mainIndex);
        		mainIndex -= lowestIndex;
        		path.setCurrentPathIndex(mainIndex);
            	nextNode = path.getPathPointFromIndex(mainIndex + 1);
            	for(int i = 0; i < segmentPathIndices.length; i++)
            	{
            		segmentPathIndices[i] -= lowestIndex;
            		if(segmentPathIndices[i] == mainIndex)
            			nextSegmentNodes[i] = nextNode;
            	}
        	}
        	else
        	{
        		path = newPath;
        		path.setCurrentPathIndex(0);
            	nextNode = path.getPathPointFromIndex(1);
        	}
        }
        else
        {
        	path = newPath;
        	activeNode = path.getPathPointFromIndex(0);
        	prevNode = activeNode;
        	nextNode = path.getPathPointFromIndex(1);
        	if(activeNode.action != PathAction.NONE)
        		nodeActionFinished = false;
        	
        	for(int i = 0; i < segmentPathIndices.length; i++)
        	{
        		if(activeSegmentNodes[i] == null)
        		{
        			activeSegmentNodes[i] = activeNode;
        			nextSegmentNodes[i] = activeNode;
        			segmentPathIndices[i] = 0;
        			segmentTime[i] = segmentOffsets[i];
        			while(segmentTime[i] < 0)
        			{
        				segmentTime[i] += 20;
        				segmentPathIndices[i]--;
        			}
        		}
        	}
        }
        
        ticksStuck = 0;

        if (noSunPathfind)
        {
            removeSunnyPath();
        }

        return true;
    }
	
	/**
	 * Returns the entity's absolute position and head angles, in terms of param 0 to 1.0.
	 */
	private PosRotate3D calcAbsolutePositionAndRotation(float time, PathNode start, PathNode middle, PathNode end)
	{
		PosRotate3D pos = calcPositionAndRotation(time, start, middle, end);
		pos.setPosX(pos.getPosX() + middle.xCoord);
		pos.setPosY(pos.getPosY() + middle.yCoord);
		pos.setPosZ(pos.getPosZ() + middle.zCoord);
		return pos;
	}
	
	/**
	 * Calculates the position and rotation of a worm-like "body segment" relative to the middle node specified.
	 * Returns the position offset from node x + 0.5, y, z + 0.5, and relevant angles in radians in terms of
	 * an X, Y, Z rotation in that order. This function of param defines exact circular arcs for turns.
	 */
	private PosRotate3D calcPositionAndRotation(float time, PathNode start, PathNode middle, PathNode end)
	{		
		int vX = end.xCoord - start.xCoord;
		int vY = end.yCoord - start.yCoord;
		int vZ = end.zCoord - start.zCoord;
		int hX = middle.xCoord != start.xCoord ? 1 : -1;
		int hY = middle.yCoord != start.yCoord ? 1 : -1;
		int hZ = middle.zCoord != start.zCoord ? 1 : -1;
		int gX = middle.xCoord != end.xCoord ? 1 : -1;
		int gY = middle.yCoord != end.yCoord ? 1 : -1;
		int gZ = middle.zCoord != end.zCoord ? 1 : -1;
		double xOffset = vX * -0.5 * hX;
		double yOffset = vY * -0.5 * hY;
		double zOffset = vZ * -0.5 * hZ;
		
		double posX = 0, posY = 0, posZ = 0;
		float rotX = 0, rotY = 0, rotZ = 0;
		
		if(hX == 1 && gX == 1)
		{
			posX = (time * vX * 0.5) + (vX > 0 ? 0 : 1);
			posY = 0.5;
			posZ = 0.5;
			rotY = vX >= 1 ? 0 : (float)Math.PI;
			return new PosRotate3D(posX, posY, posZ, rotX, rotY, rotZ);
		}
		else if(hY == 1 && gY == 1)
		{
			posY = (time * vY * 0.5) + (vY > 0 ? 0 : 1);
			posX = 0.5;
			posZ = 0.5;
			return new PosRotate3D(posX, posY, posZ, rotX, rotY, rotZ);
		}
		else if(hZ == 1 && gZ == 1)
		{
			posZ = (time * vZ * 0.5) + (vZ > 0 ? 0 : 1);
			posY = 0.5;
			posX = 0.5;
			rotY = vZ * (float)Math.PI / 4;
			return new PosRotate3D(posX, posY, posZ, rotX, rotY, rotZ);
		}
		
		

		// (sign)[offset; top vs bot) * (func cos or sin)[handedness]
		
		if(hX == 1)
			posX = vX * hX * Math.sin(time * 0.5 * Math.PI) * 0.5 + xOffset;
		else
			posX = vX * hX * Math.cos(time * 0.5 * Math.PI) * 0.5 + xOffset;
		
		if(hY == 1)
			posY = vY * hY * Math.sin(time * 0.5 * Math.PI) * 0.5 + yOffset;
		else
			posY = vY * hY * Math.cos(time * 0.5 * Math.PI) * 0.5 + yOffset;
		
		if(hZ == 1)
			posZ = vZ * hZ * Math.sin(time * 0.5 * Math.PI) * 0.5 + zOffset;
		else
			posZ = vZ * hZ * Math.cos(time * 0.5 * Math.PI) * 0.5 + zOffset;
		
		if(hX == 1)
		{
			rotY = vX == 1 ? 0 : 180;
			if(gZ == 1)
				rotY += time * vZ * vX * 90F;
			else if (gY == 1)
				rotZ = time * vY * 90F;
		}
		else if(hY == 1)
		{
			if(gX == 1)
			{
				rotX = vX == 1 ? 0 : 180;
				rotZ = (90 * vY) + time * vX * -90F;
			}
			else if (gZ == 1)
			{
				rotX = 90;
				rotY = vZ * (time * vY * -90F);
				rotZ = -90;
			}
		}
		else if(hZ == 1)
		{
			if(gX == 1)
			{
				rotY = vZ * (90 + time * vX * -90F);
			}
			else if (gY == 1)
			{
				rotX = 90;
				rotY = -vZ * (-90 + time * vY * -90F);
				rotZ = -90;
			}
		}
		
		posX += 0.5;
		posY += 0.5;
		posZ += 0.5;
		
		rotX /= 57.2958F;
		rotY /= 57.2958F;
		rotZ /= 57.2958F;
		return new PosRotate3D(posX, posY, posZ, rotX, rotY, rotZ);
	}
	
	private PosRotate3D calcStraight(float time, PathNode start, PathNode end)
	{
		PosRotate3D segment = new PosRotate3D();
		segment.setPosX(start.xCoord + 0.5 + time * (end.xCoord - start.xCoord) * 0.5);
		segment.setPosY(start.yCoord + time * (end.yCoord - start.yCoord) * 0.5);
		segment.setPosZ(start.zCoord + 0.5 + time * (end.zCoord - start.zCoord * 0.5));
		return segment;
	}
	
	/**
	 * Adds path2 to path1, removing a lower and upper part of path1 as specified.
	 */
	private Path extendPath(Path path1, Path path2, int lowerBoundP1, int upperBoundP1)
	{
		int k = upperBoundP1 - lowerBoundP1;
		PathNode[] newPoints = new PathNode[k + path2.getCurrentPathLength()];
		System.arraycopy(path1.points, lowerBoundP1, newPoints, 0, k);
		System.arraycopy(path2.points, 0, newPoints, k, path2.getCurrentPathLength());
		return new Path(newPoints);
	}
	
	protected PathNode nextNode;
	protected PathNode prevNode;
	protected PathNode[] prevSegmentNodes;
	protected PathNode[] activeSegmentNodes;
	protected PathNode[] nextSegmentNodes;
	protected int[] segmentPathIndices;
	protected int[] segmentTime;
	protected int[] segmentOffsets;
	protected float timePerTick;
	protected Path lastPath;
	protected boolean nodeChanged;
}
