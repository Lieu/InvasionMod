package mods.invmod.common.entity;

import mods.invmod.common.entity.INavigationFlying.MoveType;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIFlyingTackle extends EntityAIBase
{
	private EntityIMFlying theEntity;
	private int time;

	public EntityAIFlyingTackle(EntityIMFlying entity)
	{
		theEntity = entity;
		time = 0;
	}
	
	@Override
	public boolean shouldExecute()
	{
		return theEntity.getAIGoal() == Goal.TACKLE_TARGET;
	}
	@Override
	public boolean continueExecuting()
	{
		EntityLiving target = theEntity.getAttackTarget();
		if(target == null || target.isDead)
		{
			theEntity.transitionAIGoal(Goal.NONE);
			return false;
		}
		
		if(theEntity.getAIGoal() != Goal.TACKLE_TARGET)
			return false;
		
		return true;
	}
	
	@Override
	public void startExecuting()
	{
		time = 0;
		EntityLiving target = theEntity.getAttackTarget();
		if(target != null)
		{
			theEntity.getNavigatorNew().setMovementType(MoveType.PREFER_WALKING);
		}
	}

	@Override
	public void updateTask()
	{
		if(theEntity.getMoveState() != MoveState.FLYING)
		{
			theEntity.transitionAIGoal(Goal.MELEE_TARGET);
		}
	}
}
