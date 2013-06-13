package mods.invmod.common.entity;

import mods.invmod.common.entity.INavigationFlying.MoveType;
import mods.invmod.common.util.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

/**
 * Performs a swoop for a flying entity when the entity's AI goal is set
 * to FIND_ATTACK_OPPORTUNITY. This task will check for a clear enough space
 * to perform the movement, otherwise it will not trigger.
 * 
 * This swoop movement may abort if the entity becomes miss-aligned with
 * the target.
 * 
 * This task also ends when the entity hits or misses the target. In the case
 * it considers it a valid hit the AI goal is proceeded to FLYING_STRIKE.
 * 
 * @author Lieu
 */
public class EntityAISwoop extends EntityAIBase
{
	private static final int INITIAL_LINEUP_TIME = 25;
	
	private EntityIMBird theEntity;
    private float minDiveClearanceY;
    private EntityLiving swoopTarget;
    private float diveAngle;
    private float diveHeight;
    private float strikeDistance;
    private float minHeight;
    private float minXZDistance;
    private float maxSteepness;
    private float finalRunLength;
    private float finalRunArcLimit;
    private int time;
    private boolean isCommittedToFinalRun;
    private boolean endSwoop;
    private boolean usingClaws;

    public EntityAISwoop(EntityIMBird entity)
    {
        theEntity = entity;
        minDiveClearanceY = 0;
        swoopTarget = null;
        diveAngle = 0;
        diveHeight = 0;
        maxSteepness = 40F;
        strikeDistance = entity.width + 1.5F;
        minHeight = 6;
        minXZDistance = 10;
        finalRunLength = 4;
        finalRunArcLimit = 15;
        time = 0;
        isCommittedToFinalRun = false;
        endSwoop = false;
        usingClaws = false;
        setMutexBits(1);
    }
	
	@Override
	public boolean shouldExecute()
	{
		if(theEntity.getAIGoal() == Goal.FIND_ATTACK_OPPORTUNITY && theEntity.getAttackTarget() != null)
		{
			swoopTarget = theEntity.getAttackTarget();
			double dX = swoopTarget.posX - theEntity.posX;
	    	double dY = swoopTarget.posY - theEntity.posY;
	    	double dZ = swoopTarget.posZ - theEntity.posZ;
	    	double dXZ = Math.sqrt(dX*dX + dZ*dZ);
	    	if(-dY < minHeight || dXZ < minXZDistance)
	    		return false;
	    	
	    	double pitchToTarget = Math.atan(dY / dXZ) * 180 / Math.PI;
	    	if(pitchToTarget > maxSteepness)
	    		return false;
	    	
	    	finalRunLength = (float)(dXZ * 0.42);
	    	if(finalRunLength > 18)
	    		finalRunLength = 18;
	    	else if(finalRunLength < 4)
	    		finalRunLength = 4;
	    	
	    	diveAngle = (float)(Math.atan((dXZ - finalRunLength) / dY) * 180 / Math.PI);
			if(swoopTarget != null && isSwoopPathClear(swoopTarget, diveAngle))
			{
				diveHeight = (float)-dY;
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean continueExecuting()
	{
		return theEntity.getAttackTarget() == swoopTarget && !endSwoop && theEntity.getMoveState() == MoveState.FLYING;
	}
	
	@Override
	public void startExecuting()
    {
		time = 0;
		theEntity.transitionAIGoal(Goal.SWOOP);
		theEntity.getNavigatorNew().setMovementType(MoveType.PREFER_FLYING);
		theEntity.getNavigatorNew().tryMoveToEntity(swoopTarget, theEntity.getMaxPoweredFlightSpeed());
		//theEntity.getNavigatorNew().autoPathToEntity(swoopTarget);
		theEntity.doScreech();
    }
	
	@Override
	public void resetTask()
	{
		endSwoop = false;
		isCommittedToFinalRun = false;
		theEntity.getNavigatorNew().enableDirectTarget(false);
		if(theEntity.getAIGoal() == Goal.SWOOP)
		{
			theEntity.transitionAIGoal(Goal.NONE);
			theEntity.setClawsForward(false);
		}
	}

    @Override
	public void updateTask()
    {
    	time++;
    	if(!isCommittedToFinalRun)
    	{
    		// When the entity reaches a certain point, decide whether to commit
    		// to the final arc of the swoop or to abort.
	    	if(theEntity.getDistanceToEntity(swoopTarget) < finalRunLength)
	    	{
	    		theEntity.getNavigatorNew().setPitchBias(0, 1.0F);
	    		if(isFinalRunLinedUp())
	    		{
	    			// Decide which type of attack to use
	    			usingClaws = theEntity.worldObj.rand.nextFloat() > 0.6F;
	    			if(usingClaws)
	    			{
	    				theEntity.setClawsForward(true);
	    			}
	    			
	    			// Set the navigator to stop using flying heuristics
	    			// and instead fly directly towards the target. Careful,
	    			// because the entity will have no obstacle avoidance.
	    			theEntity.getNavigatorNew().enableDirectTarget(true);
	    			isCommittedToFinalRun = true;
	    		}
	    		else
	    		{
	    			// Abort
	    			theEntity.transitionAIGoal(Goal.NONE);
	    			endSwoop = true;
	    		}
	    	}
	    	else if(time > INITIAL_LINEUP_TIME)
	    	{
	    		// Keep updating the trajectory for the first part of the swoop. Reduce
	    		// the pitch bias the closer the entity gets to the ground.
	    		double dYp = -(swoopTarget.posY - theEntity.posY);
	    		if(dYp < 2.9)
	    		{
	    			dYp = 0;
	    		}
	    		theEntity.getNavigatorNew().setPitchBias(diveAngle * (float)(dYp / diveHeight), (float)(0.6 * (dYp / diveHeight)));
	    	}
    	}
    	else
    	{
    		// If the entity gets close enough to the target, this AI task ends and control is
    		// implicitly handed over to other tasks. If a miss is detected, also end this task.
    		if(theEntity.getDistanceToEntity(swoopTarget) < strikeDistance)
    		{
    			theEntity.transitionAIGoal(Goal.FLYING_STRIKE);
    			
    			// Re-enable proper flight path heuristics.
    			theEntity.getNavigatorNew().enableDirectTarget(false);
    			endSwoop = true;
    		}
    		else
    		{
    			// Not close enough to strike so check for a "miss". Check if the target
    			// is within a certain angle in front of the entity to determine this.
    			double dX = swoopTarget.posX - theEntity.posX;
    	    	double dZ = swoopTarget.posZ - theEntity.posZ;
    	    	double yawToTarget = Math.atan2(dZ, dX) * 180 / Math.PI - 90F;
    	    	if(Math.abs(MathUtil.boundAngle180Deg(yawToTarget - theEntity.rotationYaw)) > 90)
    	    	{
        			theEntity.transitionAIGoal(Goal.NONE);
    	    		theEntity.getNavigatorNew().enableDirectTarget(false);
    	    		theEntity.setClawsForward(false);
        			endSwoop = true;
    	    	}
    		}
    	}
    }
    
    /**
     * Calculates whether the expected swoop path from the entity to the
     * target is generally clear of solid obstacles. Returns true if clear.
     * 
     * @param target The target entity
     * @param diveAngle The steepest dive angle at beginning of the swoop to check for obstacles at
     */
    private boolean isSwoopPathClear(EntityLiving target, float diveAngle)
    {
    	// To evaluate the space between the entity and the target, send
    	// out some rays that will cover some of the expected arc. Then pass
    	// some of the information to a function to decide whether it's
    	// clear "enough". 
    	//
    	// The rays are cast according to the diagram, from the entity's height
    	// down to the target's height. The smaller the value dRayY is the
    	// more rays are cast between these two points, ie higher resolution.
    	//
    	//     ^                            . entity
    	//     |                       -  1/|
    	//     |                   -     2/_|
    	//     |              -         3/ a|
    	//     |target   -             4/   |
    	//     |  o -  -  -  -  -  -  5/    |
    	// xz__|______________________/_____|_________>
    	//     |
    	//     y
    	//
    	// points 1-5 - raytrace origins
    	// o          - target, raytrace end
    	// .          - this entity
    	// a          - steepest dive angle
    	// 
    	double dX = target.posX - theEntity.posX;
    	double dY = target.posY - theEntity.posY;
    	double dZ = target.posZ - theEntity.posZ;
    	double dXZ = Math.sqrt(dX*dX + dZ*dZ);
    	double dRayY = 2; // Y distance between rays cast along the dive path
    	int hitCount = 0;
    	double lowestCollide = theEntity.posY;
    	for(double y = theEntity.posY - dRayY; y > target.posY; y -= dRayY)
    	{
    		double dist = Math.tan(90 + diveAngle) * (theEntity.posY - y);
    		double x = -Math.sin(theEntity.rotationYaw / 180 * Math.PI) * dist;
    		double z = Math.cos(theEntity.rotationYaw / 180 * Math.PI) * dist;
    		Vec3 source = theEntity.worldObj.getWorldVec3Pool().getVecFromPool(x, y, z);
    		MovingObjectPosition collide = theEntity.worldObj.rayTraceBlocks(source, target.getPosition(1.0F));
    		if(collide != null)
    		{
    			if(hitCount == 0)
    				lowestCollide = y;
    			
    			hitCount++;
    		}
    	}
    	
    	if(isAcceptableDiveSpace(theEntity.posY, lowestCollide, hitCount))
    	{
    		return true;
    	}
    	
    	return false;
    }
    
    private boolean isFinalRunLinedUp()
    {
    	double dX = swoopTarget.posX - theEntity.posX;
    	double dY = swoopTarget.posY - theEntity.posY;
    	double dZ = swoopTarget.posZ - theEntity.posZ;
    	double dXZ = Math.sqrt(dX*dX + dZ*dZ);
    	double yawToTarget = Math.atan2(dZ, dX) * 180 / Math.PI - 90F;
    	double dYaw = MathUtil.boundAngle180Deg(yawToTarget - theEntity.rotationYaw);
    	if(dYaw < -finalRunArcLimit || dYaw > finalRunArcLimit)
    		return false;
    	
    	double dPitch = Math.atan(dY / dXZ) * 180 / Math.PI - theEntity.rotationPitch;
    	if(dPitch < -finalRunArcLimit || dPitch > finalRunArcLimit)
    		return false;
    	
    	return true;
    }
    
    protected boolean isAcceptableDiveSpace(double entityPosY, double lowestCollideY, int hitCount)
    {
    	double clearanceY = entityPosY - lowestCollideY;
    	if(clearanceY < minDiveClearanceY)
    		return false;
    	
    	return true;
    }
}