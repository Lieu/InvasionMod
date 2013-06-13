package mods.invmod.common.entity;

import net.minecraft.entity.EntityLiving;

public class EntityAITargetRetaliate extends EntityAISimpleTarget
{
	public EntityAITargetRetaliate(EntityIMLiving entity, Class<? extends EntityLiving> targetType, float distance)
	{
		super(entity, targetType, distance);
	}

	@Override
	public boolean shouldExecute()
    {
		EntityLiving attacker = getEntity().getAITarget();
		if(attacker != null) // Was attacked
		{
			if(getEntity().getDistanceToEntity(attacker) <= getAggroRange() && getTargetType().isAssignableFrom(attacker.getClass()))
			{
				setTarget(attacker);
				return true;
			}
		}
		return false;
    }
}
