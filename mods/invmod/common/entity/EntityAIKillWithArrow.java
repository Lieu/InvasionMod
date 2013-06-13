package mods.invmod.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.projectile.EntityArrow;

public class EntityAIKillWithArrow<T extends EntityLiving> extends EntityAIKillEntity<T>
{
	private float attackRangeSq;
	
	public EntityAIKillWithArrow(EntityIMLiving entity, Class<? extends T> targetClass, int attackDelay, float attackRange)
	{
		super(entity, targetClass, attackDelay);
		this.attackRangeSq = attackRange * attackRange;
	}
	
	@Override
	public void updateTask()
	{
		super.updateTask();
		T target = getTarget();
		if(getEntity().getDistanceSq(target.posX, target.boundingBox.minY, target.posZ) < 36 && getEntity().getEntitySenses().canSee(target))
			getEntity().getNavigatorNew().haltForTick();
	}

	@Override
	protected void attackEntity(Entity target)
	{
		setAttackTime(getAttackDelay());
		EntityLiving entity = getEntity();
		EntityArrow entityarrow = new EntityArrow(entity.worldObj, entity, getTarget(), 1.1F, 12F);
		entity.worldObj.playSoundAtEntity(entity, "random.bow", 1.0F, 1.0F / (entity.getRNG().nextFloat() * 0.4F + 0.8F));
		entity.worldObj.spawnEntityInWorld(entityarrow);
	}
	
	@Override
	protected boolean canAttackEntity(Entity target)
	{
		return getAttackTime() <= 0 && getEntity().getDistanceSq(target.posX, target.boundingBox.minY, target.posZ) < attackRangeSq && getEntity().getEntitySenses().canSee(target);
	}
}
