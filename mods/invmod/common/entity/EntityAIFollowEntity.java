package mods.invmod.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

public class EntityAIFollowEntity<T extends EntityLiving> extends EntityAIMoveToEntity<T>
{
	private float followDistanceSq;
	
	public EntityAIFollowEntity(EntityIMLiving entity, float followDistance)
	{
		this(entity, (Class<T>)EntityLiving.class, followDistance);
	}
	
	public EntityAIFollowEntity(EntityIMLiving entity, Class<? extends T> target, float followDistance)
	{
		super(entity, target);
		followDistanceSq = followDistance * followDistance;
	}
	
	@Override
	public void startExecuting()
	{
		getEntity().onFollowingEntity(getTarget());
		super.startExecuting();
	}
	
	@Override
	public void resetTask()
	{
		getEntity().onFollowingEntity(null);
		super.resetTask();
	}
	
	@Override
	public void updateTask()
	{
		super.updateTask();
		Entity entity = getTarget();
		if(getEntity().getDistanceSq(entity.posX, entity.boundingBox.minY, entity.posZ) < followDistanceSq)
			getEntity().getNavigatorNew().haltForTick();
	}
}
