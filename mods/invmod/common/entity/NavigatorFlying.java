package mods.invmod.common.entity;

import mods.invmod.common.entity.INavigationFlying.MoveType;
import mods.invmod.common.util.Distance;
import mods.invmod.common.util.MathUtil;
import mods.invmod.common.util.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class NavigatorFlying extends NavigatorIM implements INavigationFlying
{
	private static final int VISION_RESOLUTION_H = 30;
	private static final int VISION_RESOLUTION_V = 20;
	private static final float FOV_H = 300;
	private static final float FOV_V = 220;
	
	private final EntityIMFlying theEntity;
	private MoveType moveType;
	private boolean wantsToBeFlying;
	private float targetYaw;
	private float targetPitch;
	private float targetSpeed;
	private float visionDistance;
	private int visionUpdateRate;
	private int timeSinceVision;
	private float[][] retina;
	private float[][] headingAppeal;
	private Vec3 intermediateTarget;
	private Vec3 finalTarget;
	private boolean isCircling;
	private float circlingHeight;
	private float circlingRadius;
	private float pitchBias;
	private float pitchBiasAmount;
	private int timeLookingForEntity;
	private boolean precisionTarget;
	private float closestDistToTarget;
	private int timeSinceGotCloser;
	
	public NavigatorFlying (EntityIMFlying entityFlying, IPathSource pathSource)
	{
		super(entityFlying, pathSource);
		theEntity = entityFlying;
		moveType = MoveType.MIXED;
		visionDistance = 14;
		visionUpdateRate = timeSinceVision = 3;
		targetYaw = entityFlying.rotationYaw;
		targetPitch = 0;
		targetSpeed = entityFlying.getMaxPoweredFlightSpeed();
		retina = new float[VISION_RESOLUTION_H][VISION_RESOLUTION_V];
		headingAppeal = new float[VISION_RESOLUTION_H - 2][VISION_RESOLUTION_V - 2];
		intermediateTarget = Vec3.createVectorHelper(0, 0, 0);
		isCircling = false;
		pitchBias = 0;
		pitchBiasAmount = 0;
		timeLookingForEntity = 0;
		precisionTarget = false;
		closestDistToTarget = 0;
		timeSinceGotCloser = 0;
	}
	
	@Override
	public void setMovementType(MoveType moveType)
	{
		this.moveType = moveType;
	}
	
	/**
	 * Sets the navigator to fly precisely towards the target, at expense
	 * of obstacle avoidance.
	 */
	@Override
	public void enableDirectTarget(boolean enabled)
	{
		precisionTarget = enabled;
	}
	
	@Override
	public void setLandingPath()
	{
		clearPath();
		moveType = MoveType.PREFER_WALKING;
		setWantsToBeFlying(false);
	}
	
	@Override
	public void setCirclingPath(Entity target, float preferredHeight, float preferredRadius)
	{
		setCirclingPath(target.posX, target.posY, target.posZ, preferredHeight, preferredRadius);
	}
	
	@Override
	public void setCirclingPath(double x, double y, double z, float preferredHeight, float preferredRadius)
	{
		clearPath();
		finalTarget = Vec3.createVectorHelper(x, y, z);
		this.circlingHeight = preferredHeight;
		this.circlingRadius = preferredRadius;
		isCircling = true;
	}
	
	@Override
	public float getDistanceToCirclingRadius()
	{
		double dX = finalTarget.xCoord - theEntity.posX;
		double dY = finalTarget.yCoord - theEntity.posY;
		double dZ = finalTarget.zCoord - theEntity.posZ;
		return (float)(Math.sqrt(dX*dX + dZ*dZ) - circlingRadius);
	}
	
	@Override
	public void setFlySpeed(float speed)
	{
		targetSpeed = speed;
	}
	
	@Override
	public void setPitchBias(float pitch, float biasAmount)
	{
		pitchBias = pitch;
		pitchBiasAmount = biasAmount;
	}
	
	@Override
	protected void updateAutoPathToEntity()
    {
		// Update progress info
		double dist = theEntity.getDistanceToEntity(pathEndEntity);
		if(dist < closestDistToTarget - 1)
		{
			closestDistToTarget = (float)dist;
			timeSinceGotCloser = 0;
		}
		else
		{
			timeSinceGotCloser++;
		}
		
		// Need to decide between pathing methods: Flying heuristic or A*. Do this with a
		// preference to the real-time flyer and resort to A* when a route is too hard to find.
		boolean pathUpdate = false;
		boolean needsPathfinder = false;
		if(path != null)
		{
			double dSq = theEntity.getDistanceSqToEntity(pathEndEntity);
			if((moveType == MoveType.PREFER_FLYING || (moveType == MoveType.MIXED && dSq > 10*10))
			   && theEntity.canEntityBeSeen(pathEndEntity))
			{
				// Go back to using the flyer
				timeLookingForEntity = 0;
				pathUpdate = true;
			}
			else
			{
				// Compare the distance the target has moved to the how far away we are.
	    		// Bigger distance to target -> less need to update the path frequently
				double d1 = Distance.distanceBetween(pathEndEntity, pathEndEntityLastPos);
	    		double d2 = Distance.distanceBetween((Entity)theEntity, pathEndEntityLastPos);
	    		if(d1 / d2 > ENTITY_TRACKING_TOLERANCE)
	    			pathUpdate = true;
			}
		}
		else
		{
			if(moveType == MoveType.PREFER_WALKING || timeSinceGotCloser > 160 || timeLookingForEntity > 600)
			{
				// Decide to get a path
				pathUpdate = true;
				needsPathfinder = true;
				timeSinceGotCloser = 0;
				timeLookingForEntity = 500; // Delay
			}
			else if(moveType == MoveType.MIXED)
			{
				double dSq = theEntity.getDistanceSqToEntity(pathEndEntity);
				if(dSq < 10 * 10)
				{
					pathUpdate = true;
				}
			}
		}
		
		if(pathUpdate)
		{
			if(moveType == MoveType.PREFER_FLYING)
			{
				if(needsPathfinder)
				{
					theEntity.setPathfindFlying(true);
					path = createPath(theEntity, pathEndEntity);
					if(path != null)
					{
						setWantsToBeFlying(true);
						setPath(path, moveSpeed);
					}
				}
				else
				{
					// Use flyer
					setWantsToBeFlying(true);
					resetStatus();
				}
			}
			else if(moveType == MoveType.MIXED)
			{
				// For mixed mode, prefer a ground path if it's relatively direct
				theEntity.setPathfindFlying(false);
				Path path = createPath(theEntity, pathEndEntity);
				if(path != null && path.getCurrentPathLength() < dist * 1.8)
				{
					setWantsToBeFlying(false);
					setPath(path, moveSpeed);
				}
				else if(needsPathfinder)
				{
					theEntity.setPathfindFlying(true);
					path = createPath(theEntity, pathEndEntity);
					setWantsToBeFlying(true);
					if(path != null)
						setPath(path, moveSpeed);
					else
						resetStatus(); // Flyer
				}
				else
				{
					// Use flyer
					setWantsToBeFlying(true);
					resetStatus();
				}
			}
			else
			{
				setWantsToBeFlying(false);
				theEntity.setPathfindFlying(false);
				Path path = createPath(theEntity, pathEndEntity);
				if(path != null)
				{
					setPath(path, moveSpeed);
				}
			}
			pathEndEntityLastPos = Vec3.createVectorHelper(pathEndEntity.posX, pathEndEntity.posY, pathEndEntity.posZ);
		}
    }
	
	@Override
	public void autoPathToEntity(Entity target)
	{
		super.autoPathToEntity(target);
		isCircling = false;
	}
	
	@Override
	public boolean tryMoveToEntity(Entity targetEntity, float speed)
    {
		if(moveType != MoveType.PREFER_WALKING)
		{
			clearPath();
			pathEndEntity = targetEntity;
			finalTarget = Vec3.createVectorHelper(pathEndEntity.posX, pathEndEntity.posY, pathEndEntity.posZ);
			isCircling = false;
			return true;
		}
		else
		{
			theEntity.setPathfindFlying(false);
			return super.tryMoveToEntity(targetEntity, speed);
		}
    }
	
	@Override
	public boolean tryMoveToXYZ(double x, double y, double z, float speed)
	{
		Vec3 target = theEntity.worldObj.getWorldVec3Pool().getVecFromPool(x, y, z);
		if(moveType != MoveType.PREFER_WALKING)
		{
			clearPath();
			finalTarget = Vec3.createVectorHelper(x, y, z);
			isCircling = false;
			return true;
		}
		else
		{
			theEntity.setPathfindFlying(false);
			return super.tryMoveToXYZ(x, y, z, speed);
		}
	}
	
	@Override
	public boolean tryMoveTowardsXZ(double x, double z, int min, int max, int verticalRange, float speed)
    {
    	Vec3 target = findValidPointNear(x, z, min, max, verticalRange);
    	if(target != null)
    	{
    		return tryMoveToXYZ(target.xCoord, target.yCoord, target.zCoord, speed);
    	}
    	return false;
    }
	
	@Override
	public void clearPath()
	{
		super.clearPath();
		pathEndEntity = null;
		isCircling = false;
	}
	
	@Override
	public boolean isCircling()
	{
		return isCircling;
	}
	
	@Override
	public String getStatus()
	{
		if(!noPath())
		{
			return super.getStatus();
		}
		String s = "";
		if(isAutoPathingToEntity())
    	{
    		s += "Auto:";
    	}

    	s += "Flyer:";
    	if(isCircling)
    	{
    		s += "Circling:";
    	}
    	else if(wantsToBeFlying)
    	{
    		if(theEntity.getFlyState() == FlyState.TAKEOFF)
    			s += "TakeOff:";
    		else
    			s += "Flying:";
    	}
    	else
    	{
    		if(theEntity.getFlyState() == FlyState.LANDING || theEntity.getFlyState() == FlyState.TOUCHDOWN)
    			s += "Landing:";
    		else
    			s += "Ground";
    	}
    	return s;
	}
	
	@Override
	protected void pathFollow()
    {
        Vec3 vec3d = getEntityPosition();
        int maxNextLeg = path.getCurrentPathLength();

        // Move current index forward for points the entity has reached
        float fa = theEntity.width * 0.5F;
        for (int j = path.getCurrentPathIndex(); j < maxNextLeg; j++)
        {
            if (vec3d.squareDistanceTo(path.getPositionAtIndex(theEntity, j)) < fa * fa)
                path.setCurrentPathIndex(j + 1);
        }
    }
	
	protected void noPathFollow()
	{
		if(theEntity.getMoveState() != MoveState.FLYING && theEntity.getAIGoal() == Goal.CHILL)
		{
			setWantsToBeFlying(false);
			return;
		}
		
		if(moveType == MoveType.PREFER_FLYING)
			setWantsToBeFlying(true);
		else if(moveType == MoveType.PREFER_WALKING)
			setWantsToBeFlying(false);
			
		if(++timeSinceVision >= visionUpdateRate)
		{
			timeSinceVision = 0;
			if(!precisionTarget || pathEndEntity == null)
				updateHeading();
			else
				updateHeadingDirectTarget(pathEndEntity);
			
			intermediateTarget = convertToVector(targetYaw, targetPitch, targetSpeed);
		}
		theEntity.getMoveHelper().setMoveTo(intermediateTarget.xCoord, intermediateTarget.yCoord, intermediateTarget.zCoord, targetSpeed);
	}
	
	
	
	protected Vec3 convertToVector(float yaw, float pitch, float idealSpeed)
	{
		int time = visionUpdateRate + 20;
		double x = theEntity.posX + -Math.sin(yaw / 180 * Math.PI) * idealSpeed * time;
        double y = theEntity.posY + Math.sin(pitch / 180 * Math.PI) * idealSpeed * time;
        double z = theEntity.posZ + Math.cos(yaw / 180 * Math.PI) * idealSpeed * time;
		return Vec3.createVectorHelper(x, y, z);
	}
	
	protected void updateHeading()
	{
		// Decide what the best heading is
		// Cast rays to see what's in front of the entity
		float pixelDegreeH = FOV_H / VISION_RESOLUTION_H;
		float pixelDegreeV = FOV_V / VISION_RESOLUTION_V;
		for(int i = 0; i < VISION_RESOLUTION_H; i++)
		{
			double nextAngleH = i * pixelDegreeH + 0.5 * pixelDegreeH - 0.5 * FOV_H + theEntity.rotationYaw;
			for(int j = 0; j < VISION_RESOLUTION_V; j++)
			{
				double nextAngleV = j * pixelDegreeV + 0.5 * pixelDegreeV - 0.5 * FOV_V;
				double y = theEntity.posY + Math.sin(nextAngleV / 180 * Math.PI) * visionDistance;
				double distanceXZ = Math.cos(nextAngleV / 180 * Math.PI) * visionDistance;
				double x = theEntity.posX + -Math.sin(nextAngleH / 180 * Math.PI) * distanceXZ;
	            double z = theEntity.posZ + Math.cos(nextAngleH / 180 * Math.PI) * distanceXZ;
	            Vec3 target = theEntity.worldObj.getWorldVec3Pool().getVecFromPool(x, y, z);
	            Vec3 origin = theEntity.getPosition(1.0F);
	            origin.yCoord += 1;
	    		//origin.xCoord += theEntity.width / 2;
	    		//origin.yCoord += theEntity.height / 2;
	    		//origin.zCoord += theEntity.width / 2;
				MovingObjectPosition object = theEntity.worldObj.rayTraceBlocks(origin, target);
				if(object != null && object.typeOfHit == EnumMovingObjectType.TILE)
				{
					double dX = theEntity.posX - object.blockX;
					double dZ = theEntity.posY - object.blockY;
					double dY = theEntity.posZ - object.blockZ;
					retina[i][j] = (float)Math.sqrt(dX*dX + dY*dY + dZ*dZ);
					//retina[i][j] = (float)Math.sqrt(object.hitVec.xCoord + dY*dY + dZ*dZ);
				}
				else
				{
					retina[i][j] = visionDistance + 1;
				}
			}
		}
		
		// Interpolate values
		// ...
		
		for(int i = 1; i < VISION_RESOLUTION_H - 1; i++)
		{
			for(int j = 1; j < VISION_RESOLUTION_V - 1; j++)
			{
				float appeal = retina[i][j];
				appeal += retina[i - 1][j - 1];
				appeal += retina[i - 1][j];
				appeal += retina[i - 1][j + 1];
				appeal += retina[i][j - 1];
				appeal += retina[i][j + 1];
				appeal += retina[i + 1][j - 1];
				appeal += retina[i + 1][j];
				appeal += retina[i + 1][j + 1];
				appeal /= 9;
				headingAppeal[i - 1][j - 1] = appeal;
			}
		}
		
		// Do a pass to encourage circling
		if(isCircling)
		{
			double dX = finalTarget.xCoord - theEntity.posX;
			double dY = finalTarget.yCoord - theEntity.posY;
			double dZ = finalTarget.zCoord - theEntity.posZ;
			double dXZ = Math.sqrt(dX*dX + dZ*dZ);
			//System.out.println("H:" + dY + ", R:" + dXZ);
			// Calculate the angle(s) to the points where two circles intersect
			// angle = +/- cos^-1(((d^2 + r2^2 + r1^2) / 2d) / r1)
			// Must make sure circles actually do intersect or else the argument
			// to inverse cosine is outside its domain ( -1.0 < x < 1.0 )
			if(dXZ > 0 && dXZ > circlingRadius * 0.6)
			{
				double intersectRadius = Math.abs((circlingRadius - dXZ) * 2) + 8;
				if(intersectRadius > circlingRadius*1.8)
					intersectRadius = dXZ + 5;
				
				// Calculate the angles from the entity where circle(entity, intersectionRadius) intersects cicle(target, circlingRadius)
				float preferredYaw1 = (float)(Math.acos(((dXZ*dXZ - circlingRadius*circlingRadius + intersectRadius*intersectRadius) / (2*dXZ)) / intersectRadius) * 180 / Math.PI);
				float preferredYaw2 = -preferredYaw1;
				
				// Convert from angles relative to target to absolute world angles
				double dYaw = Math.atan2(dZ, dX) * 180 / Math.PI - 90F;
				preferredYaw1 += dYaw;
				preferredYaw2 += dYaw;
				
				// Calculate pitch too, but much simpler because there is only one axis
				float preferredPitch = (float)(Math.atan((dY + circlingHeight) / intersectRadius) * 180 / Math.PI);
				
				// Further away implies more bias. Multiply how far away the entity is from its circling 
				// path with units of bias per unit distance. Result is units of bias, not a factor.
				float yawBias = (float)(1.5 * Math.abs(dXZ - circlingRadius) / circlingRadius);
				float pitchBias = (float)(1.9 * Math.abs((dY + circlingHeight) / circlingHeight));
				
				// Perform the bias pass. Determines bias factors and multiplies elements against it.
				doHeadingBiasPass(headingAppeal, preferredYaw1, preferredYaw2, preferredPitch, yawBias, pitchBias);
			}
			else
			{
				float yawToTarget = (float)(Math.atan2(dZ, dX) * 180 / Math.PI - 90F);
				yawToTarget += 180F;
				float preferredPitch = (float)(Math.atan((dY + circlingHeight) / (Math.abs(circlingRadius - dXZ))) * 180 / Math.PI);
				float yawBias = (float)(0.5 * Math.abs(dXZ - circlingRadius) / circlingRadius);
				float pitchBias = (float)(0.9 * Math.abs((dY + circlingHeight) / circlingHeight));
				doHeadingBiasPass(headingAppeal, yawToTarget, yawToTarget, preferredPitch, yawBias, pitchBias);
			}
		}
		else if(pathEndEntity != null)
		{
			double dX = pathEndEntity.posX - theEntity.posX;
			double dY = pathEndEntity.posY - theEntity.posY;
			double dZ = pathEndEntity.posZ - theEntity.posZ;
			double dXZ = Math.sqrt(dX*dX + dZ*dZ);
			float yawToTarget = (float)(Math.atan2(dZ, dX) * 180 / Math.PI - 90F);
			float pitchToTarget = (float)(Math.atan(dY / dXZ) * 180 / Math.PI);
			doHeadingBiasPass(headingAppeal, yawToTarget, yawToTarget, pitchToTarget, 20.6F, 20.6F);
		}


		if(pathEndEntity == null)
		{
			// Do a pass to add bias towards the previous decision
			float dOldYaw = targetYaw - theEntity.rotationYaw;
			MathUtil.boundAngle180Deg(dOldYaw);
			float dOldPitch = targetPitch; // - theEntity.rotationPitch;
			float approxLastTargetX = dOldYaw / pixelDegreeH + ((VISION_RESOLUTION_H - 2) / 2);
			float approxLastTargetY = dOldPitch / pixelDegreeV + ((VISION_RESOLUTION_V - 2) / 2);
			if(approxLastTargetX > VISION_RESOLUTION_H - 2)
				approxLastTargetX = VISION_RESOLUTION_H - 2;
			else if(approxLastTargetX < 0)
				approxLastTargetX = 0;
			
			if(approxLastTargetY > VISION_RESOLUTION_V - 2)
				approxLastTargetY = VISION_RESOLUTION_V - 2;
			else if(approxLastTargetY < 0)
				approxLastTargetY = 0;
			
			float statusQuoBias = 0.4F;
			float falloffDist = VISION_RESOLUTION_H;
			for(int i = 0; i < VISION_RESOLUTION_H - 2; i++)
			{
				float dXSq = (approxLastTargetX - i)*(approxLastTargetX - i);
				for(int j = 0; j < VISION_RESOLUTION_V - 2; j++)
				{
					float dY = approxLastTargetY - j;
					headingAppeal[i][j] *= 1 + statusQuoBias - (statusQuoBias * Math.sqrt(dXSq + dY*dY) / falloffDist);
				}
			}
		}
		
		if(pitchBias != 0)
		{
			doHeadingBiasPass(headingAppeal, 0, 0, pitchBias, 0, pitchBiasAmount);
		}
		
		// Encourage landing if needed
		if(!wantsToBeFlying)
		{
			Pair<Float, Float> landingInfo = appraiseLanding();
			if(landingInfo.getVal2() < 4F)
			{
				if(landingInfo.getVal1() >= 0.9F)
					doHeadingBiasPass(headingAppeal, 0, 0, -45F, 0, 3.5F);
				else if(landingInfo.getVal1() >= 0.65F)
					doHeadingBiasPass(headingAppeal, 0, 0, -15F, 0, 0.4F);
			}
			else
			{
				if(landingInfo.getVal1() >= 0.52F)
					doHeadingBiasPass(headingAppeal, 0, 0, -15F, 0, 0.8F);
			}
		}
		
		// Select best pixel and set target towards it
		Pair<Integer, Integer> bestPixel = chooseCoordinate();
		targetYaw = theEntity.rotationYaw - 0.5F * FOV_H + (bestPixel.getVal1() + 1) * pixelDegreeH + 0.5F * pixelDegreeH;
		targetPitch = /*theEntity.rotationPitch*/ - 0.5F * FOV_V + (bestPixel.getVal2() + 1) * pixelDegreeV + 0.5F * pixelDegreeV;
	}
	
	protected void updateHeadingDirectTarget(Entity target)
	{
		double dX = target.posX - theEntity.posX;
    	double dY = target.posY - theEntity.posY;
    	double dZ = target.posZ - theEntity.posZ;
    	double dXZ = Math.sqrt(dX*dX + dZ*dZ);
    	targetYaw = (float)(Math.atan2(dZ, dX) * 180 / Math.PI - 90F);
    	targetPitch = (float)(Math.atan(dY / dXZ) * 180 / Math.PI);
	}
	
	protected Pair<Integer, Integer> chooseCoordinate()
	{
		int bestPixelX = 0;
		int bestPixelY = 0;
		for(int i = 0; i < VISION_RESOLUTION_H - 2; i++)
		{
			for(int j = 0; j < VISION_RESOLUTION_V - 2; j++)
			{
				if(headingAppeal[bestPixelX][bestPixelY] < headingAppeal[i][j])
				{
					bestPixelX = i;
					bestPixelY = j;
				}
			}
		}
		return new Pair<Integer, Integer>(bestPixelX, bestPixelY);
	}
	
	protected void setTarget(double x, double y, double z)
	{
		intermediateTarget = Vec3.createVectorHelper(x, y, z);
	}
	
	protected Vec3 getTarget()
	{
		return intermediateTarget;
	}
	
	protected void doHeadingBiasPass(float[][] array, float preferredYaw1, float preferredYaw2, float preferredPitch, float yawBias, float pitchBias)
	{
		float pixelDegreeH = FOV_H / VISION_RESOLUTION_H;
		float pixelDegreeV = FOV_V / VISION_RESOLUTION_V;
		for(int i = 0; i < array.length; i++)
		{
			double nextAngleH = (i + 1) * pixelDegreeH + 0.5 * pixelDegreeH - 0.5 * FOV_H + theEntity.rotationYaw;
			double dYaw1 = MathUtil.boundAngle180Deg(preferredYaw1 - nextAngleH);
			double dYaw2 = MathUtil.boundAngle180Deg(preferredYaw2 - nextAngleH);
			double yawBiasAmount = 1 + Math.min(Math.abs(dYaw1), Math.abs(dYaw2)) * yawBias / 180; // 1 unit of yawBias = pi radians
			for(int j = 0; j < array[0].length; j++)
			{
				double nextAngleV = (j + 1) * pixelDegreeV + 0.5 * pixelDegreeV - 0.5 * FOV_V;
				double pitchBiasAmount = 1 + Math.abs(MathUtil.boundAngle180Deg(preferredPitch - nextAngleV)) * pitchBias / 180;
				array[i][j] /= yawBiasAmount * pitchBiasAmount;
			}
		}
	}

	private void setWantsToBeFlying(boolean flag)
	{
		wantsToBeFlying = flag;
		theEntity.getMoveHelper().setWantsToBeFlying(flag);
	}
	
	private Pair<Float, Float> appraiseLanding()
	{
		float safety = 0;
		float distance = 0;
		int landingResolution = 3;
		double nextAngleH = theEntity.rotationYaw;
		for(int i = 0; i < landingResolution; i++)
		{
			double nextAngleV = -90 + i * 30 / landingResolution;
			double y = theEntity.posY + Math.sin(nextAngleV / 180 * Math.PI) * 64;
			double distanceXZ = Math.cos(nextAngleV / 180 * Math.PI) * 64;
			double x = theEntity.posX + -Math.sin(nextAngleH / 180 * Math.PI) * distanceXZ;
            double z = theEntity.posZ + Math.cos(nextAngleH / 180 * Math.PI) * distanceXZ;
            Vec3 target = theEntity.worldObj.getWorldVec3Pool().getVecFromPool(x, y, z);
            Vec3 origin = theEntity.getPosition(1.0F);
			MovingObjectPosition object = theEntity.worldObj.rayTraceBlocks(origin, target);
			if(object != null)
			{
				int id = theEntity.worldObj.getBlockId(object.blockX, object.blockY, object.blockZ);
				if(!theEntity.avoidsBlock(id))
					safety += 0.7F;
				
				if(object.sideHit == 1)
					safety += 0.3F;
				
				double dX = object.blockX - theEntity.posX;
				double dY = object.blockY - theEntity.posY;
				double dZ = object.blockZ - theEntity.posZ;
				distance += Math.sqrt(dX*dX + dY*dY + dZ*dZ);
			}
			else
			{
				distance += 64;
			}
		}
		distance /= landingResolution;
		safety /= landingResolution;
		return new Pair<Float, Float>(safety, distance);
	}
}
