package mods.invmod.common.entity;

import mods.invmod.common.nexus.INexusAccess;
import mods.invmod.common.util.CoordsInt;
import mods.invmod.common.util.Distance;
import mods.invmod.common.util.IPosition;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIGoToNexus extends EntityAIBase
{
	private EntityIMMob theEntity;
	private IPosition lastPathRequestPos;
	private int pathRequestTimer;
	private int pathFailedCount;
	
	public EntityAIGoToNexus(EntityIMMob entity)
	{
		theEntity = entity;
		lastPathRequestPos = new CoordsInt(0, -128, 0);
		pathRequestTimer = 0;
    	pathFailedCount = 0;
    	setMutexBits(1);
	}
	
	@Override
	public boolean shouldExecute()
	{
		if(theEntity.getAIGoal() == Goal.BREAK_NEXUS)
			return true;

		return false;
	}
	
	@Override
	public void startExecuting()
    {
		setPathToNexus();
    }

    @Override
	public void updateTask()
    {
    	if(pathFailedCount > 1)
    		wanderToNexus();
    		
    	if(theEntity.getNavigatorNew().noPath() || theEntity.getNavigatorNew().getStuckTime() > 40)
    		setPathToNexus();
    }
    
    private void setPathToNexus()
    {
		INexusAccess nexus = theEntity.getNexus();
		if(nexus != null && pathRequestTimer-- <= 0)
		{
			boolean pathSet = false;
			double distance = theEntity.findDistanceToNexus();
			if(distance > 2000)
			{
				pathSet = theEntity.getNavigatorNew().tryMoveTowardsXZ(nexus.getXCoord(), nexus.getZCoord(), 1, 6, 4, theEntity.getMoveSpeed());
			}
			else if(distance > 1.5)
			{
				pathSet = theEntity.getNavigatorNew().tryMoveToXYZ(nexus.getXCoord(), nexus.getYCoord(), nexus.getZCoord(), theEntity.getMoveSpeed());
			}
			
			if(!pathSet || (theEntity.getNavigatorNew().getLastPathDistanceToTarget() > 3.0F && Distance.distanceBetween(lastPathRequestPos, theEntity) < 3.5))
			{
				pathFailedCount++;
				pathRequestTimer = 40 * pathFailedCount + theEntity.worldObj.rand.nextInt(10);
			}
			else
			{
				pathFailedCount = 0;
				pathRequestTimer = 20;
			}
			
			lastPathRequestPos = new CoordsInt(theEntity.getXCoord(), theEntity.getYCoord(), theEntity.getZCoord());
		}
    }
    
    private boolean pathTooShort()
    {
    	Path path = theEntity.getNavigatorNew().getPath();
		if(path != null)
		{
			IPosition pos = path.getFinalPathPoint();
			return theEntity.getDistanceSq(pos.getXCoord(), pos.getYCoord(), pos.getZCoord()) < 2 * 2;
		}
		return true;
    }
    
    protected void wanderToNexus()
    {
    	INexusAccess nexus = theEntity.getNexus();
    	theEntity.getMoveHelper().setMoveTo(nexus.getXCoord() + 0.5, nexus.getYCoord(), nexus.getZCoord() + 0.5, theEntity.getMoveSpeed());
    }
}
