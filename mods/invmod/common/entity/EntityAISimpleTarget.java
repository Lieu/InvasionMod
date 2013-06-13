package mods.invmod.common.entity;


import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mods.invmod.common.util.ComparatorEntityDistanceFrom;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;

public class EntityAISimpleTarget extends EntityAIBase
{
	private final EntityIMLiving theEntity;
	private EntityLiving targetEntity;
	private Class<? extends EntityLiving> targetClass;
	private int outOfLosTimer;
	private float distance;
	private boolean needsLos;
	
	public EntityAISimpleTarget(EntityIMLiving entity, Class<? extends EntityLiving> targetType, float distance)
	{
		this(entity, targetType, distance, true);
	}
	
	public EntityAISimpleTarget(EntityIMLiving entity, Class<? extends EntityLiving> targetType, float distance, boolean needsLoS)
    {
        theEntity = entity;
        targetClass = targetType;
        outOfLosTimer = 0;
        this.distance = distance;
        this.needsLos = needsLoS;
        setMutexBits(1);
    }
	
	public EntityIMLiving getEntity()
	{
		return theEntity;
	}
	
	/**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
	public boolean shouldExecute()
    {
        if (targetClass == EntityPlayer.class)
        {
            EntityPlayer entityplayer = theEntity.worldObj.getClosestVulnerablePlayerToEntity(theEntity, distance);
            if(isValidTarget(entityplayer))
            {
            	targetEntity = entityplayer;
                return true;
            }
        }

        List<EntityLiving> list = theEntity.worldObj.getEntitiesWithinAABB(targetClass, theEntity.boundingBox.expand(distance, distance / 2, distance));
        Comparator<Entity> comp = new ComparatorEntityDistanceFrom(theEntity.posX, theEntity.posY, theEntity.posZ);
        Collections.sort(list, comp);
        
        EntityLiving entity;
        boolean foundEntity = false;
        while(list.size() > 0)
        {
        	entity = list.remove(list.size() - 1);
        	if(isValidTarget(entity))
        	{
        		targetEntity = entity;
        		return true;
        	}
        }
        return false;
    }
    
    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
	public boolean continueExecuting()
    {
        EntityLiving entityliving = theEntity.getAttackTarget();
        if (entityliving == null)
            return false;

        if (!entityliving.isEntityAlive())
            return false;

        if (theEntity.getDistanceSqToEntity(entityliving) > (distance * distance))
            return false;

        if (needsLos)
        {
            if (!theEntity.getEntitySenses().canSee(entityliving))
            {
                if (++outOfLosTimer > 60)
                    return false;
            }
            else
            {
            	outOfLosTimer = 0;
            }
        }

        return true;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
	public void startExecuting()
    {
    	theEntity.setAttackTarget(targetEntity);
    	outOfLosTimer = 0;
    }
    
    /**
     * Resets the task
     */
    @Override
	public void resetTask()
    {
        theEntity.setAttackTarget(null);
    }
    
    public Class<? extends EntityLiving> getTargetType()
    {
    	return targetClass;
    }
    
    public float getAggroRange()
    {
    	return distance;
    }
    
    protected void setTarget(EntityLiving entity)
    {
    	targetEntity = entity;
    }
    
    protected boolean isValidTarget(EntityLiving entity)
    {
    	if (entity == null)
            return false;

        if (entity == theEntity)
            return false;

        if (!entity.isEntityAlive())
            return false;

        //if (!theEntity.canAttackClass(entity.getClass()))
        //    return false;

        /*if ((theEntity instanceof EntityTameable) && ((EntityTameable)theEntity).isTamed())
        {
            if ((entity instanceof EntityTameable) && ((EntityTameable)entity).isTamed())
                return false;

            if (entity == ((EntityTameable)theEntity).getOwner())
                return false;
        }*/

        if (needsLos && !theEntity.getEntitySenses().canSee(entity))
            return false;

        return true;
    }
}
