package mods.invmod.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIMeleeAttack<T extends EntityLiving> extends EntityAIBase
{
	private EntityIMLiving theEntity;
	private Class<? extends T> targetClass;
	private float attackRange;
	private int attackDelay;
	private int nextAttack;
	
	public EntityAIMeleeAttack(EntityIMLiving entity, Class<? extends T> targetClass, int attackDelay)
	{
		theEntity = entity;
		this.targetClass = targetClass;
		this.attackDelay = attackDelay;
		attackRange = 0.6F;
		nextAttack = 0;
	}
	
	@Override
	public boolean shouldExecute()
	{
		EntityLiving target = theEntity.getAttackTarget();
		return target != null && theEntity.getAIGoal() == Goal.MELEE_TARGET && theEntity.getDistanceToEntity(target) < (attackRange + theEntity.width + target.width) * 4 && target.getClass().isAssignableFrom(targetClass);
	}
	
	@Override
	public void updateTask()
    {
		EntityLiving target = theEntity.getAttackTarget();
    	if(canAttackEntity(target))
    	{
    		attackEntity(target);
    	}
    	setAttackTime(getAttackTime() - 1);
    }
	
	public Class<? extends T> getTargetClass()
	{
		return targetClass;
	}

	protected void attackEntity(EntityLiving target)
	{
		theEntity.attackEntityAsMob(target);
		setAttackTime(getAttackDelay());
	}
	
	protected boolean canAttackEntity(EntityLiving target)
	{
		if(getAttackTime() <= 0)
		{
			double d = (theEntity.width + attackRange);
			return theEntity.getDistanceSq(target.posX, target.boundingBox.minY, target.posZ) < (d * d);
		}
		return false;
	}
	
	protected int getAttackTime()
	{
		return nextAttack;
	}
	
	protected void setAttackTime(int time)
	{
		nextAttack = time;
	}
	
	protected int getAttackDelay()
	{
		return attackDelay;
	}
	
	protected void setAttackDelay(int time)
	{
		attackDelay = time;
	}
}
