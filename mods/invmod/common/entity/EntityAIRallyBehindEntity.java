package mods.invmod.common.entity;

import net.minecraft.entity.EntityLiving;

public class EntityAIRallyBehindEntity<T extends EntityLiving & ILeader> extends EntityAIFollowEntity<T>
{
	private static final float DEFAULT_FOLLOW_DISTANCE = 5.0F;
	
	public EntityAIRallyBehindEntity(EntityIMLiving entity, Class<T> leader)
	{
		this(entity, leader, DEFAULT_FOLLOW_DISTANCE);
	}

	public EntityAIRallyBehindEntity(EntityIMLiving entity, Class<T> leader, float followDistance)
	{
		super(entity, leader, followDistance);
	}
	
	@Override
	public boolean shouldExecute()
	{
		return getEntity().readyToRally() && super.shouldExecute();
	}
	
	@Override
	public boolean continueExecuting()
	{
		return getEntity().readyToRally() && super.continueExecuting();
	}
	
	@Override
	public void updateTask()
	{
		super.updateTask();
		if(getEntity().readyToRally())
		{
			T leader = getTarget();
			if(leader.isMartyr())
				rally(leader);
		}
	}
	
	protected void rally(T leader)
	{
		getEntity().rally(leader);
	}
}
