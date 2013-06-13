package mods.invmod.common.entity;

import mods.invmod.common.util.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIPickUpEntity extends EntityAIBase
{
	private EntityIMBird theEntity;
	private int time;
	private int holdTime;
	private int abortTime;
	private float pickupPointY;
	private float pickupRangeY;
	private float pickupPointX;
	private float pickupPointZ;
	private float pickupRangeXZ;
	private float abortAngleYaw;
	private float abortAnglePitch;
	private boolean isHoldingEntity;
	
	public EntityAIPickUpEntity(EntityIMBird entity, float pickupPointX, float pickupPointY, float pickupPointZ, float pickupRangeY,
								float pickupRangeXZ, int abortTime, float abortAngleYaw, float abortAnglePitch)
	{
		theEntity = entity;
		time = 0;
		holdTime = 70;
		this.pickupPointX = pickupPointX;
		this.pickupPointY = pickupPointY;
		this.pickupPointZ = pickupPointZ;
		this.pickupRangeY = pickupRangeY;
		this.pickupRangeXZ = pickupRangeXZ;
		this.abortTime = abortTime;
		this.abortAngleYaw = abortAngleYaw;
		this.abortAnglePitch = abortAnglePitch;
		isHoldingEntity = false;
	}
	
	@Override
	public boolean shouldExecute()
	{
		return theEntity.getAIGoal() == Goal.PICK_UP_TARGET || theEntity.riddenByEntity != null;
	}
	
	@Override
	public void startExecuting()
	{
		isHoldingEntity = theEntity.riddenByEntity != null;
		time = 0;
	}
	
	@Override
	public boolean continueExecuting()
	{
		EntityLiving target = theEntity.getAttackTarget();
		if(target != null && !target.isDead)
		{
			if(!isHoldingEntity)
			{
				// Abort if pickup attempt has taken too long or if
				// we are not lined up with the entity appropriately
				if(time > abortTime && isLinedUp(target))
					return true;
			}
			else if(theEntity.riddenByEntity == target)
			{
				return true;
			}
		}
		theEntity.transitionAIGoal(Goal.NONE);
		theEntity.setClawsForward(false);
		return false;
	}

	@Override
	public void updateTask()
	{
		time++;
		if(!isHoldingEntity)
		{
			// Need to check if target is in the hitbox
			EntityLiving target = theEntity.getAttackTarget();
			double dY = target.prevPosY - theEntity.prevPosY;
			System.out.println(dY);
			if(Math.abs(dY - pickupPointY) < pickupRangeY)
			{
				// Rotate offsets about entity
				double dAngle = theEntity.prevRotationYaw / 180F * Math.PI; // +90F ?
				double sinF = Math.sin(dAngle);
				double cosF = Math.cos(dAngle);
				double x = pickupPointX * cosF - pickupPointZ * sinF;
				double z = pickupPointZ * cosF + pickupPointX * sinF;
				
				// Check for hit
				double dX = target.prevPosX - (x + theEntity.prevPosX);
		    	double dZ = target.prevPosZ - (z + theEntity.prevPosZ);
		    	double dXZ = Math.sqrt(dX*dX + dZ*dZ);
		    	System.out.println(dXZ);
		    	if(dXZ < pickupRangeXZ)
		    	{
		    		target.mountEntity(theEntity);
		    		isHoldingEntity = true;
		    		time = 0;
		    		theEntity.getNavigatorNew().clearPath();
		    		theEntity.getNavigatorNew().setPitchBias(20F, 1.5F);
		    	}
			}
		}
		else if(time == 45)
		{
			theEntity.getNavigatorNew().setPitchBias(0, 0);
		}
		else if(time > holdTime)
		{
			theEntity.getAttackTarget().mountEntity(null);
		}
	}
	
	private boolean isLinedUp(Entity target)
	{
		double dX = target.posX - theEntity.posX;
    	double dY = target.posY - theEntity.posY;
    	double dZ = target.posZ - theEntity.posZ;
    	double dXZ = Math.sqrt(dX*dX + dZ*dZ);
    	double yawToTarget = Math.atan2(dZ, dX) * 180 / Math.PI - 90F;
    	double dYaw = MathUtil.boundAngle180Deg(yawToTarget - theEntity.rotationYaw);
    	if(dYaw < -abortAngleYaw || dYaw > abortAngleYaw)
    		return false;
    	
    	double dPitch = Math.atan(dY / dXZ) * 180 / Math.PI - theEntity.rotationPitch;
    	if(dPitch < -abortAnglePitch || dPitch > abortAnglePitch)
    		return false;
    	
    	return true;
	}
}
