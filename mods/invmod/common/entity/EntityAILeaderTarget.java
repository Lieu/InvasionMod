package mods.invmod.common.entity;

import net.minecraft.entity.EntityLiving;

public class EntityAILeaderTarget extends EntityAISimpleTarget
{
	private final EntityIMLiving theEntity;
	
	public EntityAILeaderTarget(EntityIMLiving entity, Class<? extends EntityLiving> targetType, float distance)
	{
		this(entity, targetType, distance, true);
	}
	
	public EntityAILeaderTarget(EntityIMLiving entity, Class<? extends EntityLiving> targetType, float distance, boolean needsLos)
    {
		super(entity, targetType, distance, needsLos);
		theEntity = entity;
    }
	
	@Override
	public boolean shouldExecute()
    {
		if(!theEntity.readyToRally())
			return false;
		
		return super.shouldExecute();
    }
}
