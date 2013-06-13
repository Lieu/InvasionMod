package mods.invmod.common.entity;

import net.minecraft.entity.EntityLiving;

public class EntityAIWingAttack extends EntityAIMeleeAttack
{
	private EntityIMBird theEntity;
	
	public EntityAIWingAttack(EntityIMBird entity, Class<? extends EntityLiving> targetClass, int attackDelay)
	{
		super(entity, targetClass, attackDelay);
		theEntity = entity;
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
	}
	
	protected boolean isInStartMeleeRange()
	{
		EntityLiving target = theEntity.getAttackTarget();
		if(target == null)
			return false;
		
		double d = (theEntity.width + theEntity.getAttackRange() + 3.0);
		return theEntity.getDistanceSq(target.posX, target.boundingBox.minY, target.posZ) < (d * d);
	}
}
