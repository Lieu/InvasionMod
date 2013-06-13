package mods.invmod.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.MathHelper;

public class EntityAIThrowerKillEntity<T extends EntityLiving> extends EntityAIKillEntity<T>
{
	private boolean melee;
	private int ammo;
	private float attackRangeSq;
	private float launchSpeed;
	private final EntityIMThrower theEntity;
	
	public EntityAIThrowerKillEntity(EntityIMThrower entity, Class<? extends T> targetClass, int attackDelay, float throwRange, float launchSpeed, int ammo)
	{
		super(entity, targetClass, attackDelay);
		this.attackRangeSq = throwRange * throwRange;
		this.launchSpeed = launchSpeed;
		this.ammo = ammo;
		theEntity = entity;
	}

	@Override
	protected void attackEntity(Entity target)
	{
		if(melee)
		{
			setAttackTime(getAttackDelay());
			super.attackEntity(target);
		}
		else
		{
			ammo--;
			setAttackTime(getAttackDelay() * 2);
			theEntity.throwBoulder(target.posX, target.posY, target.posZ);
		}
	}
	
	@Override
	protected boolean canAttackEntity(Entity target)
	{
		melee = super.canAttackEntity(target);
		if(melee)
			return true;
		
		if(!theEntity.canThrow() || ammo == 0)
			return false;
		
		// Calculate if target is in ballistics range
		double dX = theEntity.posX - target.posX;
        double dZ = theEntity.posZ - target.posZ;
        double dXY = MathHelper.sqrt_double(dX * dX + dZ * dZ);
		return getAttackTime() <= 0 && theEntity.getEntitySenses().canSee(target) && (0.025D * dXY) / (launchSpeed * launchSpeed) <= 1.0F;
	}
}
