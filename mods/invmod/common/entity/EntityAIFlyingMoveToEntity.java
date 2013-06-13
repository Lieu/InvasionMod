package mods.invmod.common.entity;

import mods.invmod.common.entity.INavigationFlying.MoveType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIFlyingMoveToEntity extends EntityAIBase
{
	private EntityIMFlying theEntity;

	public EntityAIFlyingMoveToEntity(EntityIMFlying entity)
	{
		theEntity = entity;
	}
	
	@Override
	public boolean shouldExecute()
	{
		return theEntity.getAIGoal() == Goal.GOTO_ENTITY && theEntity.getAttackTarget() != null;
	}
	
	@Override
	public void startExecuting()
    {
		INavigationFlying nav = theEntity.getNavigatorNew();
		Entity target = theEntity.getAttackTarget();
		if(target != nav.getTargetEntity())
		{
			nav.clearPath();
			nav.setMovementType(MoveType.PREFER_WALKING);
			Path path = nav.getPathToEntity(target);
			if(path.getCurrentPathLength() > 2.0 * theEntity.getDistanceToEntity(target))
			{
				nav.setMovementType(MoveType.MIXED);
			}
			nav.autoPathToEntity(target);
		}
	}
	
	@Override
	public void updateTask()
    {

    }
}
