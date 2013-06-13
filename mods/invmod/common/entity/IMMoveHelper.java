package mods.invmod.common.entity;

import mods.invmod.common.util.IPosition;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.MathHelper;

/**
 * IMMoveHelper performs some basic movement tasks that involve manipulating
 * the state of an entity. It is purely mechanical and doesn't carry out tasks.
 * Inherits from EntityMoveHelper because this class is usable in EntityLiving
 * in place of it, but there is no separate interface.
 * 
 * @author Lieu
 */
public class IMMoveHelper extends EntityMoveHelper
{
	protected EntityIMLiving entity;
    protected double posX;
    protected double posY;
    protected double posZ;
    protected float setSpeed;
    protected float targetSpeed;
    protected boolean needsUpdate;
    protected boolean isRunning;
    
    public IMMoveHelper(EntityIMLiving par1EntityLiving)
    {
    	super(par1EntityLiving);
        needsUpdate = false;
        entity = par1EntityLiving;
        posX = par1EntityLiving.posX;
        posY = par1EntityLiving.posY;
        posZ = par1EntityLiving.posZ;
        setSpeed = targetSpeed = 0;
    }

    /**
     * needsUpdate()
     * 
     * Returns true if the move helper has been set to move this tick
     * but hasn't yet had an update tick.
     */
    @Override
    public boolean isUpdating()
    {
        return needsUpdate;
    }

    @Override
    /**
     * Returns the movement speed this object is set to move the entity at.
     */
	public float getSpeed()
    {
        return setSpeed;
    }
    
    public void setMoveTo(IPosition pos, float speed)
    {
    	setMoveTo(pos.getXCoord(), pos.getYCoord(), pos.getZCoord(), speed);
    }

    @Override
	public void setMoveTo(double x, double y, double z, float speed)
    {
        posX = x;
        posY = y;
        posZ = z;
        setSpeed = speed;
        targetSpeed = speed;
        needsUpdate = true;
    }

    @Override
	public void onUpdateMoveHelper()
    {
    	if (!needsUpdate)
        {
        	entity.setMoveForward(0.0F);
        	entity.setMoveState(MoveState.STANDING);
            return;
        }

    	MoveState result = doGroundMovement();
    	entity.setMoveState(result);
    }
    
    protected MoveState doGroundMovement()
    {
    	needsUpdate = false;
        boolean isInLiquid = entity.isInWater() || entity.handleLavaMovement();
        double dX = posX - entity.posX;
        double dZ = posZ - entity.posZ;
        double dY = posY - (!isInLiquid ? MathHelper.floor_double(entity.boundingBox.minY + 0.5D) : entity.posY);
          
        // Walk towards x, y, z
        float newYaw = (float)((Math.atan2(dZ, dX) * 180D) / Math.PI) - 90F;
        int ladderPos = -1;
        if(Math.abs(dX) < 0.8 && Math.abs(dZ) < 0.8  && (dY > 0.0D || entity.isHoldingOntoLadder()))
        {
        	// Walk towards correct edge of ladder if there is one
        	ladderPos = getClimbFace(entity.posX, entity.posY, entity.posZ); // Lower block
        	if(ladderPos == -1)
        		ladderPos = getClimbFace(entity.posX, entity.posY + 1, entity.posZ); // Otherwise check y + 1
        	
        	// Set target rotation towards center of block behind ladder. -1 means no ladder and no rotation.
        	switch (ladderPos)
        	{
	        	case 0: newYaw = (float)((Math.atan2(dZ, dX + 1) * 180D) / Math.PI) - 90F; break;
	        	case 1: newYaw = (float)((Math.atan2(dZ, dX - 1) * 180D) / Math.PI) - 90F; break;
	        	case 2: newYaw = (float)((Math.atan2(dZ + 1, dX) * 180D) / Math.PI) - 90F; break;
	        	case 3: newYaw = (float)((Math.atan2(dZ - 1, dX) * 180D) / Math.PI) - 90F; break;
        	}
        }
        
        double dXZSq = dX * dX + dZ * dZ;
        double distanceSquared = dXZSq + dY * dY;
        if(distanceSquared < 0.01 && ladderPos == -1) // Within sphere of 0.1 on the target position
        	return MoveState.STANDING;
        
        // Don't rotate if not climbing and difference in XZ is small (avoids
        // spinning in place). Otherwise, execute movement.
        if(dXZSq > 0.04 || ladderPos != -1)
        {
	        entity.rotationYaw = correctRotation(entity.rotationYaw, newYaw, entity.getTurnRate());
	        float moveSpeed;
	        if(distanceSquared >= 1 || entity.isSprinting())
	        	moveSpeed = targetSpeed;
	        else
	        	moveSpeed = targetSpeed * 0.5F; // Move slower if entity is very close to target
	        
	        if(entity.isInWater() && moveSpeed < 0.6)
	        	moveSpeed = 0.6F;
	        
	        entity.setMoveForward(moveSpeed);
        }
        
        // Set jump/climb if target is higher than current y. Also need
        // to account for entity width to jump at the right time (ie, spiders).
        double w = Math.max((entity.width * 0.5F) + 1, 1.0);
        w = entity.width * 0.5F + 1;
        if (dY > 0.0D && (dX * dX + dZ * dZ <= w * w || isInLiquid))
        {
            entity.getJumpHelper().setJumping();
            if(ladderPos != -1)
            	return MoveState.CLIMBING;
        }
        return MoveState.RUNNING;
    }

    /**
     * Calculates a rotation. Rotates from currentYaw by the amount given by
     * turnSpeed. Rotation is bounded to newYaw. Returns final rotation as a
     * value from -180 inclusive to 180 exclusive.
     */
    protected float correctRotation(float currentYaw, float newYaw, float turnSpeed)
    {
        float dYaw = newYaw - currentYaw;
        for (; dYaw < -180F; dYaw += 360F) { }
        for (; dYaw >= 180F; dYaw -= 360F) { }
        if (dYaw > turnSpeed)
            dYaw = turnSpeed;        
        if (dYaw < -turnSpeed)
            dYaw = -turnSpeed;
        
        return currentYaw + dYaw;
    }
    
    /**
     * Returns 0-3 corresponding to the orientation of a climbable block, -1 if not climbable.
     * 0-3 maps to: +X,-X,+Z,-Z
     */
    protected int getClimbFace(double x, double y, double z)
    {
        int mobX = MathHelper.floor_double(x);
        int mobY = MathHelper.floor_double(y);
        int mobZ = MathHelper.floor_double(z);
        
    	int id = entity.worldObj.getBlockId(mobX, mobY, mobZ);
    	if(id == Block.ladder.blockID)
    	{
    		int meta = entity.worldObj.getBlockMetadata(mobX, mobY, mobZ);
    		if(meta == 2)
    			return 2;
    		else if(meta == 3)
    			return 3;
    		else if(meta == 4)
    			return 0;
    		else if(meta == 5)
    			return 1;
    	}
    	else if(id == Block.vine.blockID)
    	{
    		int meta = entity.worldObj.getBlockMetadata(mobX, mobY, mobZ);
    		if(meta == 1)
    			return 2;
    		else if(meta == 4)
    			return 3;
    		else if(meta == 2)
    			return 1;
    		else if(meta == 8)
    			return 0;
    	}
    	return -1;
    }
}
