package mods.invmod.common.entity;

import net.minecraft.entity.EntityLiving;

public class EntityAITargetOnNoNexusPath extends EntityAISimpleTarget
{
	private final float PATH_DISTANCE_TRIGGER = 4.0F;
	
	public EntityAITargetOnNoNexusPath(EntityIMLiving entity,	Class<? extends EntityLiving> targetType, float distance)
	{
		super(entity, targetType, distance);
	}

	@Override
	public boolean shouldExecute()
	{
		if(getEntity().getAIGoal() == Goal.BREAK_NEXUS && getEntity().getNavigatorNew().getLastPathDistanceToTarget() > PATH_DISTANCE_TRIGGER)
			return super.shouldExecute();
		else
			return false;
	}
	
	@Override
	public boolean continueExecuting()
	{
		if(getEntity().getAIGoal() == Goal.BREAK_NEXUS && getEntity().getNavigatorNew().getLastPathDistanceToTarget() > PATH_DISTANCE_TRIGGER)
			return super.continueExecuting();
		else
			return false;
	}
}
