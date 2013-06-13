package mods.invmod.common.entity;

import mods.invmod.common.entity.INavigationFlying.MoveType;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIStabiliseFlying extends EntityAIBase
{
	private static int INITIAL_STABILISE_TIME = 50;
	private EntityIMFlying theEntity;
	private int time;
	private int stabiliseTime;

    public EntityAIStabiliseFlying(EntityIMFlying entity, int stabiliseTime)
    {
        theEntity = entity;
        time = 0;
        this.stabiliseTime = stabiliseTime;
    }
	
	@Override
	public boolean shouldExecute()
	{
		return theEntity.getAIGoal() == Goal.STABILISE;
	}
	
	@Override
	public boolean continueExecuting()
	{
		if(time >= stabiliseTime)
		{
			theEntity.transitionAIGoal(Goal.NONE);
			theEntity.getNavigatorNew().setPitchBias(0, 0);
			return false;
		}
		return true;
	}
	
	@Override
	public void startExecuting()
    {
		time = 0;
		INavigationFlying nav = theEntity.getNavigatorNew();
		nav.clearPath();
		nav.setMovementType(MoveType.PREFER_FLYING);
		nav.setPitchBias(20F, 0.5F);
    }
	
	@Override
	public void updateTask()
	{
		time++;
		if(time == INITIAL_STABILISE_TIME)
		{
			theEntity.getNavigatorNew().setPitchBias(0, 0);
		}
	}
}