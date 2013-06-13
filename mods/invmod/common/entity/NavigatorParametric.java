package mods.invmod.common.entity;

import mods.invmod.common.util.PosRotate3D;
import net.minecraft.world.World;

/**
 * Navigator component that works in terms of a parameter - probably time - instead
 * of distances and positions per tick. The interface semantics are now onUpdateNavigation(T),
 * where T is how much the parameter has elapsed. This class is abstract and requires the
 * definition of the parametric function, entityPositionAtParam(T), and isReadyForNextNode(T).
 * The first calculates the position at T on a per-node basis. The second function returns true
 * when the path should be incremented to the next node and T is set back to 0.
 * 
 * @author Lieu
 */
public abstract class NavigatorParametric extends NavigatorIM
{
	public NavigatorParametric (EntityIMLiving entity, IPathSource pathSource)
	{
		super(entity, pathSource);
		minMoveToleranceSq = 00000025;
		timeParam = 0;
	}
	
	/**
	 * Moves the entity forward along its path proportional to paramElapsed, where
	 * the position itself is defined by entityPositionAtParam(param). paramElapsed
	 * is the amount elapsed since the previous call to this method and will increment
	 * param if movement is successful.
	 */
	public void onUpdateNavigation(int paramElapsed)
	{
		totalTicks++;
        if (noPath() || waitingForNotify)
            return;
        
        if (canNavigate() && nodeActionFinished)
        {
        	int pathIndex = path.getCurrentPathIndex();
            pathFollow(timeParam + paramElapsed);
            doMovementTo(timeParam);
            
            if(path.getCurrentPathIndex() != pathIndex)
            {
                ticksStuck = 0;
	            if(activeNode.action != PathAction.NONE)
	            	nodeActionFinished = false;
            }
        }
        
        if(nodeActionFinished)
        {
        	if(!isPositionClear(activeNode.xCoord, activeNode.yCoord, activeNode.zCoord, theEntity))
        	{
        		if(theEntity.onPathBlocked(path, this))
        		{
        			setDoingTaskAndHold();
        		}
        		else
        		{
        			clearPath();
        		}
        	}
        }
        else
        {
        	handlePathAction();
        }
	}
	
	/**
	 * Calls onUpdateNavigation(int paramElapsed) with a default of paramElapsed = 1.
	 */
	@Override
	public void onUpdateNavigation()
	{
		onUpdateNavigation(1);
	}
	
	/**
	 * Moves the entity towards the defined position at param.
	 */
	protected void doMovementTo(int param)
	{
		PosRotate3D movePos = entityPositionAtParam(param);
    	theEntity.moveEntity(movePos.getPosX(), movePos.getPosY(), movePos.getPosZ());
    	
    	// Check if the move was valid
    	if(Math.abs(theEntity.getDistanceSq(movePos.getPosX(), movePos.getPosY(), movePos.getPosZ())) < minMoveToleranceSq)
    	{
    		timeParam = param;
    		ticksStuck--;
    	}
    	else
    	{
    		ticksStuck++;
    	}
	}
	
	/**
	 * Defines the entity's position relative to the current path node, in terms of param.
	 * Returns the position offset from node x + 0.5, y, z + 0.5 (and relevant angles).
	 */
	protected abstract PosRotate3D entityPositionAtParam(int param);
	
	/**
	 * 
	 */
	protected abstract boolean isReadyForNextNode(int param);
	
	/**
	 * 
	 */
	protected void pathFollow(int param)
    {
		int nextIndex = path.getCurrentPathIndex() + 1;
		if(isReadyForNextNode(param))
		{
			if(nextIndex < path.getCurrentPathLength())
			{
				timeParam = 0;
				path.setCurrentPathIndex(nextIndex);
				activeNode = path.getPathPointFromIndex(path.getCurrentPathIndex());
			}
		}
		else
		{
			timeParam = param;
		}
    }
	
	/**
	 * 
	 */
	@Override
	protected void pathFollow()
    {
        pathFollow(0);
    }
	
	protected double minMoveToleranceSq;
	protected int timeParam;
}
