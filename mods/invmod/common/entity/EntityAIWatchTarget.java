package mods.invmod.common.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIWatchTarget extends EntityAIBase
{
	private EntityLiving theEntity;
	
	public EntityAIWatchTarget(EntityLiving entity)
	{
		theEntity = entity;
	}

	@Override
	public boolean shouldExecute()
	{
		return theEntity.getAttackTarget() != null;
	}
	
	@Override
	public void updateTask()
	{
		theEntity.getLookHelper().setLookPositionWithEntity(theEntity.getAttackTarget(), 2, 2);
	}
}
