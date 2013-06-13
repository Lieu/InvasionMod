package mods.invmod.common.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIMoveToEntity<T extends EntityLiving> extends EntityAIBase
{
    private EntityIMLiving theEntity;
	private T targetEntity;
	private Class<? extends T> targetClass;
	private boolean targetMoves;
	private double lastX;
	private double lastY;
	private double lastZ;
	private int pathRequestTimer;
	private int pathFailedCount;
	
	public EntityAIMoveToEntity(EntityIMLiving entity)
	{
		this(entity, (Class<T>)EntityLiving.class);
	}
	
	public EntityAIMoveToEntity(EntityIMLiving entity, Class<? extends T> target)
	{
		targetClass = target;
		theEntity = entity;
		targetMoves = false;
		pathRequestTimer = 0;
    	pathFailedCount = 0;
		setMutexBits(1);
	}
	
	@Override
	public boolean shouldExecute()
	{
		if(--pathRequestTimer <= 0)
		{
			EntityLiving target = theEntity.getAttackTarget();
			if(target != null && targetClass.isAssignableFrom(theEntity.getAttackTarget().getClass()))
			{
				targetEntity = targetClass.cast(target);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean continueExecuting()
	{
		EntityLiving target = theEntity.getAttackTarget();
		if(target != null && target == targetEntity)
			return true;
		
		return false;
	}
	
	@Override
	public void startExecuting()
    {
		targetMoves = true;
		setPath();
    }

    @Override
	public void resetTask()
    {
    	targetMoves = false;
    }

    @Override
	public void updateTask()
    {
    	if(--pathRequestTimer <= 0 && !theEntity.getNavigatorNew().isWaitingForTask() && targetMoves && targetEntity.getDistanceSq(lastX, lastY, lastZ) > 1.8)
    	{
    		setPath();
    	}
    }
    
    protected void setTargetMoves(boolean flag)
    {
    	targetMoves = flag;
    }
    
    protected EntityIMLiving getEntity()
    {
    	return theEntity;
    }
    
    protected T getTarget()
    {
    	return targetEntity;
    }
    
    protected void setPath()
    {
    	if(theEntity.getNavigatorNew().tryMoveToEntity(targetEntity, theEntity.getMoveSpeed()))
		{
			if(theEntity.getNavigatorNew().getLastPathDistanceToTarget() > 3.0F)
				pathRequestTimer = 30 + theEntity.worldObj.rand.nextInt(10);
			else
				pathRequestTimer = 10 + theEntity.worldObj.rand.nextInt(10);
			
			pathFailedCount = 0;
		}
		else
		{
			pathFailedCount++;
			pathRequestTimer = 40 * pathFailedCount + theEntity.worldObj.rand.nextInt(10);
		}
    	
    	lastX = targetEntity.posX;
		lastY = targetEntity.posY;
		lastZ = targetEntity.posZ;
    }
}
