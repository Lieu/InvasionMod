package mods.invmod.common.entity;

import mods.invmod.common.entity.INavigationFlying.MoveType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;

public class EntityAIBirdFight<T extends EntityLiving> extends EntityAIMeleeFight<T>
{
	private EntityIMBird theEntity;
	private boolean wantsToRetreat;
	private boolean buffetedTarget;
	
	public EntityAIBirdFight(EntityIMBird entity, Class<? extends T> targetClass, int attackDelay, float retreatHealthLossPercent)
	{
		super(entity, targetClass, attackDelay, retreatHealthLossPercent);
		theEntity = entity;
		wantsToRetreat = false;
		buffetedTarget = false;
	}
	
	@Override
	public void updateTask()
	{
		if(getAttackTime() == 0)
		{
			theEntity.setAttackingWithWings(isInStartMeleeRange());
		}
		super.updateTask();
	}
	
	@Override
	public void resetTask()
	{
		theEntity.setAttackingWithWings(false);
		super.resetTask();
	}
	
	@Override
	public void updatePath()
	{
		INavigationFlying nav = theEntity.getNavigatorNew();
		Entity target = theEntity.getAttackTarget();
		if(target != nav.getTargetEntity())
		{
			nav.clearPath();
			nav.setMovementType(MoveType.PREFER_WALKING);
			Path path = nav.getPathToEntity(target);
			if(path.getCurrentPathLength() > 1.6 * theEntity.getDistanceToEntity(target))
			{
				nav.setMovementType(MoveType.MIXED);
			}
			nav.autoPathToEntity(target);
		}
	}
	
	@Override
	protected void updateDisengage()
	{
		if(!wantsToRetreat)
		{
			if(shouldLeaveMelee())
				wantsToRetreat = true;
		}
		else if(buffetedTarget && theEntity.getAIGoal() == Goal.MELEE_TARGET)
		{
			theEntity.transitionAIGoal(Goal.LEAVE_MELEE);
		}
	}
	
	@Override
	protected void attackEntity(EntityLiving target)
	{
		theEntity.doMeleeSound();
		super.attackEntity(target);
		if(wantsToRetreat)
		{
			doWingBuffetAttack(target);
			buffetedTarget = true;
		}
	}
	
	protected boolean isInStartMeleeRange()
	{
		EntityLiving target = theEntity.getAttackTarget();
		if(target == null)
			return false;
		
		double d = (theEntity.width + theEntity.getAttackRange() + 3.0);
		return theEntity.getDistanceSq(target.posX, target.boundingBox.minY, target.posZ) < (d * d);
	}
	
	protected void doWingBuffetAttack(EntityLiving target)
	{
		int knockback = 2;
		target.addVelocity(-MathHelper.sin((theEntity.rotationYaw * (float)Math.PI) / 180F) * knockback * 0.5F, 0.4D, MathHelper.cos((theEntity.rotationYaw * (float)Math.PI) / 180F) * knockback * 0.5F);
		target.worldObj.playSoundAtEntity(target, "damage.fallbig", 1.0F, 1.0F);
	}
}
