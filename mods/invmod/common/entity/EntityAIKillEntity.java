package mods.invmod.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

public class EntityAIKillEntity<T extends EntityLiving> extends EntityAIMoveToEntity<T>
{
	private static final float ATTACK_RANGE = 1.0F;
	
	private int attackDelay;
	private int nextAttack;
	
	public EntityAIKillEntity(EntityIMLiving entity, Class<? extends T> targetClass, int attackDelay)
	{
		super(entity, targetClass);
		this.attackDelay = attackDelay;
		nextAttack = 0;
	}
	
	@Override
	public void updateTask()
    {
		super.updateTask();
		setAttackTime(getAttackTime() - 1);
		Entity target = getTarget();
    	if(canAttackEntity(target))
    	{
    		attackEntity(target);
    	}
    }
	
	protected void attackEntity(Entity target)
	{
		getEntity().attackEntityAsMob(getTarget());
		setAttackTime(getAttackDelay());
	}
	
	protected boolean canAttackEntity(Entity target)
	{
		if(getAttackTime() <= 0)
		{
			Entity entity = getEntity();
			double d = (entity.width + ATTACK_RANGE) * (entity.width + ATTACK_RANGE);
			//if(entity.isSprinting()) { d *= 1.3; }
			return entity.getDistanceSq(target.posX, target.boundingBox.minY, target.posZ) < d;
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
