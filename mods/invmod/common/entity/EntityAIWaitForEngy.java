package mods.invmod.common.entity;

public class EntityAIWaitForEngy extends EntityAIFollowEntity<EntityIMPigEngy>
{
	private final float PATH_DISTANCE_TRIGGER = 4.0F;
	
	private boolean canHelp;
	
	public EntityAIWaitForEngy(EntityIMLiving entity, float followDistance, boolean canHelp)
	{
		super(entity, EntityIMPigEngy.class, followDistance);
		this.canHelp = canHelp;
	}
	
	@Override
	public void updateTask()
	{
		super.updateTask();
		if(canHelp)
		{
			getTarget().supportForTick(getEntity(), 1);
		}
	}
}
