package mods.invmod.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

public class EntityAIMeleeFight<T extends EntityLiving> extends EntityAIMeleeAttack<T>
{
	private EntityIMLiving theEntity;
	private int time;
	private int startingHealth;
	private int damageDealt;
	private int invulnCount;
	private float retreatHealthLossPercent;
	
	public EntityAIMeleeFight(EntityIMLiving entity, Class<? extends T> targetClass, int attackDelay, float retreatHealthLossPercent)
	{
		super(entity, targetClass, attackDelay);
		theEntity = entity;
		time = 0;
		startingHealth = 0;
		damageDealt = 0;
		invulnCount = 0;
		this.retreatHealthLossPercent = retreatHealthLossPercent;
	}

	@Override
	public boolean shouldExecute()
	{
		Entity target = theEntity.getAttackTarget();
		return theEntity.getAIGoal() == Goal.MELEE_TARGET && target != null && target.getClass().isAssignableFrom(getTargetClass());
	}

	@Override
	public boolean continueExecuting()
	{
		return (theEntity.getAIGoal() == Goal.MELEE_TARGET || isWaitingForTransition()) && theEntity.getAttackTarget() != null;
	}
	
	@Override
	public void startExecuting()
	{
		time = 0;
		startingHealth = theEntity.getHealth();
		damageDealt = 0;
		invulnCount = 0;
	}
	
	@Override
	public void updateTask()
	{
		updateDisengage();
		updatePath();
		super.updateTask();
		if(damageDealt > 0 || startingHealth - theEntity.getHealth() > 0)
			time++;
	}
	
	public void updatePath()
	{
		INavigation nav = theEntity.getNavigatorNew();
		if(theEntity.getAttackTarget() != nav.getTargetEntity())
		{
			nav.clearPath();
			nav.autoPathToEntity(theEntity.getAttackTarget());
		}
	}
	
	protected void updateDisengage()
	{
		if(theEntity.getAIGoal() == Goal.MELEE_TARGET && shouldLeaveMelee())
		{
			theEntity.transitionAIGoal(Goal.LEAVE_MELEE);
		}
	}
	
	protected boolean isWaitingForTransition()
	{
		return theEntity.getAIGoal() == Goal.LEAVE_MELEE && theEntity.getPrevAIGoal() == Goal.MELEE_TARGET;
	}
	
	@Override
	protected void attackEntity(EntityLiving target)
	{
		int h = target.getHealth();
		super.attackEntity(target);
		h = h - target.getHealth();
		if(h <= 0)
		{
			invulnCount++;
		}
		damageDealt += h;
	}
	
	protected boolean shouldLeaveMelee()
	{
		int damageReceived = startingHealth - theEntity.getHealth();
		if(time > 40 && damageReceived > theEntity.getMaxHealth() * retreatHealthLossPercent)
			return true;
		
		if(time > 100 && damageReceived - damageDealt > theEntity.getMaxHealth() * 0.66F * retreatHealthLossPercent)
			return true;
		
		if(invulnCount >= 2)
			return true;
		
		return false;
	}
}
