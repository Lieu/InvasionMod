package mods.invmod.common.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAICreeperIMSwell extends EntityAIBase
{
	EntityIMCreeper theEntity;
    EntityLiving targetEntity;

    public EntityAICreeperIMSwell(EntityIMCreeper par1EntityCreeper)
    {
        theEntity = par1EntityCreeper;
        setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
	public boolean shouldExecute()
    {
        EntityLiving entityliving = theEntity.getAttackTarget();
        return theEntity.getCreeperState() > 0 || entityliving != null && theEntity.getDistanceSqToEntity(entityliving) < 9D;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
	public void startExecuting()
    {
        theEntity.getNavigatorNew().clearPath();
        targetEntity = theEntity.getAttackTarget();
    }

    /**
     * Resets the task
     */
    @Override
	public void resetTask()
    {
        targetEntity = null;
    }

    /**
     * Updates the task
     */
    @Override
	public void updateTask()
    {
        if (targetEntity == null)
        {
            theEntity.setCreeperState(-1);
            return;
        }

        if (theEntity.getDistanceSqToEntity(targetEntity) > 49D)
        {
            theEntity.setCreeperState(-1);
            return;
        }

        if (!theEntity.getEntitySenses().canSee(targetEntity))
        {
            theEntity.setCreeperState(-1);
            return;
        }
        else
        {
            theEntity.setCreeperState(1);
            return;
        }
    }
}
