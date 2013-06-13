package mods.invmod.common.entity;

import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIStoop extends EntityAIBase
{
	private EntityIMLiving theEntity;
	private int updateTimer;
	private boolean stopStoop;

    public EntityAIStoop(EntityIMLiving entity)
    {
        theEntity = entity;
        stopStoop = true;
    }

    @Override
	public boolean shouldExecute()
    {
    	if(--updateTimer <= 0)
    	{
    		updateTimer = 10;
    		if(theEntity.worldObj.getBlockMaterial(theEntity.getXCoord(), theEntity.getYCoord() + 2, theEntity.getZCoord()).blocksMovement())
    			return true;
    	}
    	
    	return false;
    }
    
    @Override
	public boolean continueExecuting()
    {
    	return !stopStoop;
    }

    @Override
	public void startExecuting()
    {
    	theEntity.setSneaking(true);
    	stopStoop = false;
    }

    @Override
	public void updateTask()
    {
    	if(--updateTimer <= 0)
    	{
    		updateTimer = 10;
    		if(!theEntity.worldObj.getBlockMaterial(theEntity.getXCoord(), theEntity.getYCoord() + 2, theEntity.getZCoord()).blocksMovement())
    		{
    			theEntity.setSneaking(false);
    			stopStoop = true;
    		}
        }
    }
}
