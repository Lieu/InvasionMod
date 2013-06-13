package mods.invmod.common.entity;

import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class IMMoveHelperFlying extends IMMoveHelper
{
	private EntityIMFlying entity; // Shadow
	private float targetFlySpeed;
	private boolean wantsToBeFlying;
	
	public IMMoveHelperFlying(EntityIMFlying entity)
	{
		super(entity);
		this.entity = entity;
		wantsToBeFlying = false;
	}
	
	public void setHeading(float yaw, float pitch, float idealSpeed, int time)
	{
		double x = entity.posX + Math.sin(yaw / 180 * Math.PI) * idealSpeed * time;
        double y = entity.posY + Math.sin(pitch / 180 * Math.PI) * idealSpeed * time;
        double z = entity.posZ + Math.cos(yaw / 180 * Math.PI) * idealSpeed * time;
		setMoveTo(x, y, z, idealSpeed);
	}
	
	public void setWantsToBeFlying(boolean flag)
	{
		wantsToBeFlying = flag;
	}
	
	@Override
	public void onUpdateMoveHelper()
	{
		//entity.setDead();
		entity.setMoveForward(0.0F);
		entity.setFlightAccelerationVector(0, 0, 0);
		if(!needsUpdate && entity.getMoveState() != MoveState.FLYING)
        {
        	entity.setMoveState(MoveState.STANDING);
        	entity.setFlyState(FlyState.GROUNDED);
        	entity.rotationPitch = correctRotation(entity.rotationPitch, 50F, 4F);
            return;
        }
		needsUpdate = false;
		
		if(wantsToBeFlying)
		{
			if(entity.getFlyState() == FlyState.GROUNDED)
			{
				entity.setMoveState(MoveState.RUNNING);
				entity.setFlyState(FlyState.TAKEOFF);
			}
			else if(entity.getFlyState() == FlyState.FLYING)
			{
				entity.setMoveState(MoveState.FLYING);
			}
		}
		else
		{
			if(entity.getFlyState() == FlyState.FLYING)
			{
				entity.setFlyState(FlyState.LANDING);
			}
		}
		
		if(entity.getFlyState() == FlyState.FLYING)
		{
			FlyState result = doFlying();
			if(result == FlyState.GROUNDED)
				entity.setMoveState(MoveState.STANDING);
			else if(result == FlyState.FLYING)
				entity.setMoveState(MoveState.FLYING);
			
			entity.setFlyState(result);
		}
		else if(entity.getFlyState() == FlyState.TAKEOFF)
		{
			FlyState result = doTakeOff();
			if(result == FlyState.GROUNDED)
				entity.setMoveState(MoveState.STANDING);
			else if(result == FlyState.TAKEOFF)
				entity.setMoveState(MoveState.RUNNING);
			else if(result == FlyState.FLYING)
				entity.setMoveState(MoveState.FLYING);
			
			entity.setFlyState(result);
		}
		else if(entity.getFlyState() == FlyState.LANDING || entity.getFlyState() == FlyState.TOUCHDOWN)
		{
			FlyState result = doLanding();
			if(result == FlyState.GROUNDED || result == FlyState.TOUCHDOWN)
				entity.setMoveState(MoveState.RUNNING);
			
			entity.setFlyState(result);
		}
		else
		{
			MoveState result = doGroundMovement();
			entity.setMoveState(result);
		}
	}
	
	protected MoveState doGroundMovement()
    {
		entity.setGroundFriction(0.6F);
		entity.setRotationRoll(correctRotation(entity.getRotationRoll(), 0, 6.0F));
		targetSpeed = entity.getAIMoveSpeed();
		entity.rotationPitch = correctRotation(entity.rotationPitch, 50F, 4F/*entity.getPitchRate()*/);
		return super.doGroundMovement();
    }
	
	protected FlyState doFlying()
	{
		targetFlySpeed = setSpeed;
		return fly();
	}

	protected FlyState fly()
	{
		entity.setGroundFriction(1.0F);
        boolean isInLiquid = entity.isInWater() || entity.handleLavaMovement();
        double dX = posX - (entity.posX/* - entity.width / 2*/);
        double dZ = posZ - (entity.posZ/* + entity.width / 2*/);
        double dY = posY - (entity.posY/* + entity.height / 2*/);//(!isInLiquid ? MathHelper.floor_double(entity.boundingBox.minY + 0.5D) : entity.posY);

		// Fly towards x, y, z
        double dXZSq = dX * dX + dZ * dZ;
        double dXZ = Math.sqrt(dXZSq);
        double distanceSquared = dXZSq + dY * dY;
		
		// Don't rotate if not climbing and difference in XZ is small (avoids
		// spinning in place). Otherwise, execute movement.
		if(distanceSquared > 0.04)
		{
			int timeToTurn = 10;
			float gravity = entity.getGravity();
			float liftConstant = gravity;
			double xAccel = 0;
			double yAccel = 0;
			double zAccel = 0;
			double velX = entity.motionX;
			double velY = entity.motionY;
			double velZ = entity.motionZ;
			double hSpeedSq = velX*velX + velZ*velZ;
			if(hSpeedSq == 0)
				hSpeedSq = 0.00000001;
			double horizontalSpeed = Math.sqrt(hSpeedSq);
			double flySpeed = Math.sqrt(hSpeedSq + velY*velY);
			//double horizontalUnit = dXZ / horizontalSpeed;
			double desiredYVelocity = dY / timeToTurn; //horizontalUnit;
			double dVelY = desiredYVelocity - (velY - gravity);
			
			// Calculate controls
			float minFlightSpeed = 0.05F;
			if(flySpeed < minFlightSpeed)
			{
				float newYaw = (float)(Math.atan2(dZ, dX) * 180 / Math.PI - 90);
				newYaw = correctRotation(entity.rotationYaw, newYaw, entity.getTurnRate());
				entity.rotationYaw = newYaw;
				if(entity.onGround)
					return FlyState.GROUNDED;
			}
			else
			{
				// Calculate climb forces
				double liftForce = flySpeed / (entity.getMaxPoweredFlightSpeed() * entity.getLiftFactor()) * liftConstant;
				double climbForce = liftForce * horizontalSpeed / (Math.abs(velY) + horizontalSpeed);
				double forwardForce = liftForce * Math.abs(velY) / (Math.abs(velY) + horizontalSpeed);
				double turnForce = liftForce;
				double climbAccel;
				if(dVelY < 0)
				{
					double maxDiveForce = entity.getMaxTurnForce() - gravity;
					climbAccel = -Math.min(Math.min(climbForce, maxDiveForce), -dVelY);
				}
				else// if(dVel >= 0)
				{
					double maxClimbForce = entity.getMaxTurnForce() + gravity;
					climbAccel = Math.min(Math.min(climbForce, maxClimbForce), dVelY);
				}
				
				//System.out.println(climbAccel);
				float minBankForce = 0.01F;
				if(turnForce < minBankForce)
					turnForce = minBankForce;
				
				// Calculate bank forces in radians
				double desiredXZHeading = Math.atan2(dZ, dX) - Math.PI / 2;
				double currXZHeading = Math.atan2(velZ, velX) - Math.PI / 2;
				double dXZHeading = desiredXZHeading - currXZHeading;
				for(; dXZHeading >= Math.PI; ) { dXZHeading -= 2*Math.PI; }
				for(; dXZHeading < -Math.PI; ) { dXZHeading += 2*Math.PI; }
				double bankForce = horizontalSpeed * dXZHeading / timeToTurn;
				double maxBankForce = Math.min(turnForce, entity.getMaxTurnForce());
				if(bankForce > maxBankForce)
					bankForce = maxBankForce;
				else if(bankForce < -maxBankForce)
					bankForce = -maxBankForce;

				// Orthogonal vector
				double bankXAccel = bankForce * -velZ / horizontalSpeed;
				double bankZAccel = bankForce * velX / horizontalSpeed; 
				
				// Normalise turning force between bank/climb
				double totalForce = xAccel + yAccel + zAccel;
				//if(totalForce > turnForce)
				{
					double r = liftForce / totalForce;
					xAccel += bankXAccel;// * r;
					yAccel += climbAccel;// * r;
					zAccel += bankZAccel;// * r;
					velX += bankXAccel;
					velY += climbAccel;
					velZ += bankZAccel;
				}
				
				// Calculate semi-realistic pitch and yaw
				double dYAccelGravity = yAccel - gravity;
				double middlePitch = 15;
				double newPitch;
				if(velY - gravity < 0)
				{
					double climbForceRatio = yAccel / climbForce;
					if(climbForceRatio > 1.0)
						climbForceRatio = 1.0;
					else if(climbForceRatio < -1.0)
						climbForceRatio = -1.0;
					
					double xzSpeed = Math.sqrt(velX*velX + velZ*velZ);
					double velPitch;
					if(xzSpeed > 0)
						velPitch = Math.atan(velY / xzSpeed) / Math.PI * 180;
					else
						velPitch = -180;
					
					double pitchInfluence = (entity.getMaxPoweredFlightSpeed() - Math.abs(velY)) / entity.getMaxPoweredFlightSpeed();
					if(pitchInfluence < 0)
						pitchInfluence = 0;
					
					newPitch = velPitch + 15 * climbForceRatio * pitchInfluence;
				}
				else
				{
					double pitchLimit = entity.getMaxPitch();
					double climbForceRatio = Math.min(yAccel / climbForce, 1.0);
					newPitch = middlePitch + (pitchLimit - middlePitch) * climbForceRatio;
				}
				newPitch = correctRotation(entity.rotationPitch, (float)newPitch, 1.5F/*entity.getPitchRate()*/);
				double newYaw = Math.atan2(velZ, velX) * 180 / Math.PI - 90F;
				newYaw = correctRotation(entity.rotationYaw, (float)newYaw, entity.getTurnRate());
				entity.setPositionAndRotation(entity.posX, entity.posY, entity.posZ, (float)newYaw, (float)newPitch);
				double newRoll = 60 * bankForce / turnForce;
				entity.setRotationRoll(correctRotation(entity.getRotationRoll(), (float)newRoll, 6.0F));
				
				
				// Calculate lift forces for previous maneuvers
				// Where Y changes, lose or gain airspeed
				double horizontalForce;
				if(velY > 0)
				{
					horizontalForce = -climbAccel;
				}
				else
				{
					horizontalForce = forwardForce;
				}
				int xDirection = velX > 0 ? 1 : -1;
				int zDirection = velZ > 0 ? 1 : -1;
				double hComponentX = xDirection * velX / (xDirection * velX + zDirection * velZ);
				
				double xLiftAccel = xDirection * horizontalForce * hComponentX;
				double zLiftAccel = zDirection * horizontalForce * (1 - hComponentX);
				
				// Also do banking forces
				double loss = 0.4;
				xLiftAccel += xDirection * -Math.abs(bankForce * loss) * hComponentX;
				zLiftAccel += zDirection * -Math.abs(bankForce * loss) * (1 - hComponentX);
				
				xAccel += xLiftAccel;
				zAccel += zLiftAccel;
				
			}
			
			// Calculate thrust
			if(flySpeed < targetFlySpeed)
			{
				entity.setThrustEffort(0.6F);
				if(!entity.isThrustOn())
				{
					entity.setThrustOn(true);
				}
				double desiredVThrustRatio = (dVelY - yAccel) / entity.getThrust();
				Vec3 thrust = calcThrust(desiredVThrustRatio);
				xAccel += thrust.xCoord;
				yAccel += thrust.yCoord;
				zAccel += thrust.zCoord;
			}
			else if(flySpeed > targetFlySpeed * 1.8)
			{
				entity.setThrustEffort(1.0F);
				if(!entity.isThrustOn())
				{
					entity.setThrustOn(true);
				}
				double desiredVThrustRatio = (dVelY - yAccel) / entity.getThrust();
				Vec3 thrust = calcThrust(desiredVThrustRatio);
				//xAccel += -thrust.xCoord;
				//yAccel += thrust.yCoord;
				//zAccel += -thrust.zCoord;
			}
			else if(entity.isThrustOn())
			{	
				entity.setThrustOn(false);
				//System.out.println("Reached speed");
			}
			
			// Set the acceleration in entity
			entity.setFlightAccelerationVector((float)xAccel, (float)yAccel, (float)zAccel);
		}
		return FlyState.FLYING;
	}
	
	protected FlyState doTakeOff()
	{
		entity.setGroundFriction(0.98F);
		entity.setThrustOn(true);
		entity.setThrustEffort(1.0F);
		targetSpeed = entity.getAIMoveSpeed();
		
		MoveState result = doGroundMovement();
		if(result == MoveState.STANDING)
			return FlyState.GROUNDED;
		
		if(entity.isCollidedHorizontally)
			entity.getJumpHelper().setJumping();
		
		Vec3 thrust = calcThrust(0);
		entity.setFlightAccelerationVector((float)thrust.xCoord, (float)thrust.yCoord, (float)thrust.zCoord);
		double speed = Math.sqrt(entity.motionX*entity.motionX + entity.motionY*entity.motionY + entity.motionZ*entity.motionZ);
		
		entity.rotationPitch = correctRotation(entity.rotationPitch, 40F, 4F/*entity.getPitchRate()*/);

		float gravity = entity.getGravity();
		float liftConstant = gravity;
		double liftForce = speed / (entity.getMaxPoweredFlightSpeed() * entity.getLiftFactor()) * liftConstant;
		//System.out.println(liftForce / gravity);
		if(liftForce > gravity)
			return FlyState.FLYING;
		else
			return FlyState.TAKEOFF;
	}
	
	protected FlyState doLanding()
	{
		entity.setGroundFriction(0.3F);
		int x = MathHelper.floor_double(entity.posX);
		int y = MathHelper.floor_double(entity.posY);
		int z = MathHelper.floor_double(entity.posZ);
		int i;
		for(i = 1; i < 5; i++)
		{
			if(entity.worldObj.getBlockId(x, y - i, z) != 0)
				break;
		}
		targetFlySpeed = setSpeed * (0.66F - (0.4F - (i - 1) * 0.133F));
		FlyState result = fly();
		entity.setThrustOn(true);
		if(result == FlyState.FLYING)
		{
			double speed = Math.sqrt(entity.motionX*entity.motionX + entity.motionY*entity.motionY + entity.motionZ*entity.motionZ);
			if(entity.onGround)
			{
				if(speed < entity.getLandingSpeedThreshold())
				{
					return FlyState.GROUNDED;
				}
				else
				{
					entity.setRotationRoll(correctRotation(entity.getRotationRoll(), 40F, 6.0F));
					return FlyState.TOUCHDOWN;
				}
			}
		}
		return FlyState.LANDING;
	}
	
	protected Vec3 calcThrust(double desiredVThrustRatio)
	{
		float thrust = entity.getThrust();
		float rMin = entity.getThrustComponentRatioMin();
		float rMax = entity.getThrustComponentRatioMax();
		double vThrustRatio = desiredVThrustRatio;
		if(vThrustRatio > rMax)
			vThrustRatio = rMax;
		else if(vThrustRatio < rMin)
			vThrustRatio = rMin;
		
		double hThrust = (1 - vThrustRatio) * thrust;
		double vThrust = vThrustRatio * thrust;
		double xAccel = hThrust * -Math.sin(entity.rotationYaw / 180 * Math.PI);
		double yAccel = vThrust;
		double zAccel = hThrust * Math.cos(entity.rotationYaw / 180 * Math.PI);
		return entity.worldObj.getWorldVec3Pool().getVecFromPool(xAccel, yAccel, zAccel);
	}
}
