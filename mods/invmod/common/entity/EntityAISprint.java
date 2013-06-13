package mods.invmod.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.DamageSource;

public class EntityAISprint extends EntityAIBase
{
	private EntityIMLiving theEntity;
	private int updateTimer;
	private int timer;
	private boolean isExecuting;
	private boolean isSprinting;
	private boolean isInWindup;
	private int missingTarget;
	private double lastX;
	private double lastY;
	private double lastZ;

    public EntityAISprint(EntityIMLiving entity)
    {
        theEntity = entity;
        updateTimer = 0;
        timer = 0;
        isExecuting = true;
        isSprinting = false;
        isInWindup = false;
        missingTarget = 0;
    }

    @Override
	public boolean shouldExecute()
    {
    	if(--updateTimer <= 0)
    	{
    		updateTimer = 20;
    		if((theEntity.getAttackTarget() != null && theEntity.canEntityBeSeen(theEntity.getAttackTarget())) || isSprinting)
    		{
    			return true;
    		}
    		else
    		{
    			isExecuting = false;
    			return false;
    		}
    	}
    	
    	return isExecuting;
    }

    @Override
	public void startExecuting()
    {
    	isExecuting = true;
    	timer = 60;
    }

    @Override
	public void updateTask()
    {
    	if(isSprinting)
    	{
    		Entity target = theEntity.getAttackTarget();
    		if(!theEntity.isSprinting() || target == null || (missingTarget > 0 && ++missingTarget > 20))
    		{
    			endSprint();
    			return;
    		}
    		
    		double dX = target.posX - theEntity.posX;
        	double dZ = target.posZ - theEntity.posZ;
        	double dAngle = ((Math.atan2(dZ, dX) * 180 / Math.PI) - 90 - theEntity.rotationYaw) % 360;
        	if(dAngle > 60)
        	{
        		theEntity.setTurnRate(2.0F);
        		missingTarget = 1;
        	}
        	
        	if(theEntity.getDistanceSq(lastX, lastY, lastZ) < 0.0009)
        	{
        		crash();
        		return;
        	}
        	
        	lastX = theEntity.posX;
    		lastY = theEntity.posY;
    		lastZ = theEntity.posZ;
    	}
    		
    		
    	if(--timer <= 0)
    	{
    		if(!isInWindup)
    		{
    			if(!isSprinting)
        		{
        			startSprint();
        		}
    			else
    			{
    				endSprint();
    			}
    		}
    		else // End of windup; start sprinting
			{
				sprint();
			}
        }
    }
    
    protected void startSprint()
    {
    	Entity target = theEntity.getAttackTarget();
    	if(target == null || target.boundingBox.minY - theEntity.posY >= 1.0)
    		return;
    	
    	double dX = target.posX - theEntity.posX;
    	double dZ = target.posZ - theEntity.posZ;
    	double dAngle = ((Math.atan2(dZ, dX) * 180 / Math.PI) - 90 - theEntity.rotationYaw) % 360;
    	if(dAngle < 10)
    	{
	    	isInWindup = true;
	    	timer = 20;
	    	theEntity.setMoveSpeed(0F);
    	}
    	else
    	{
    		timer = 10;
    	}
    }
    
    protected void sprint()
    {
    	isInWindup = false;
    	isSprinting = true;
    	missingTarget = 0;
		timer = 35;
		theEntity.resetMoveSpeed();
		theEntity.setMoveSpeed(theEntity.getMoveSpeed() * 5.5F);
		theEntity.setSprinting(true);
		theEntity.setTurnRate(4.9F);
		theEntity.attackTime = 0;
    }
    
    protected void endSprint()
    {
    	isSprinting = false;
    	timer = 180;
    	theEntity.resetMoveSpeed();
    	theEntity.setTurnRate(30F);
    	theEntity.setSprinting(false);
    }
    
    protected void crash()
    {
    	theEntity.stunEntity(40);
    	theEntity.attackEntityFrom(DamageSource.generic, 5);
    	theEntity.worldObj.playSoundAtEntity(theEntity, "random.explode", 1.0F, 0.6F);
    	endSprint();
    }
}
