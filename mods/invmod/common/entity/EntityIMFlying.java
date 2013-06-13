package mods.invmod.common.entity;

import mods.invmod.common.IBlockAccessExtended;
import mods.invmod.common.nexus.INexusAccess;
import mods.invmod.common.util.CoordsInt;
import mods.invmod.common.util.MathUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class EntityIMFlying extends EntityIMLiving
{
	private static final int META_TARGET_X = 29;
	private static final int META_TARGET_Y = 30;
	private static final int META_TARGET_Z = 31;
	private static final int META_THRUST_DATA = 28;
	private static final int META_FLYSTATE = 27;
	
	private final NavigatorFlying navigatorFlying;
	private final IMMoveHelperFlying moveHelper;
	private final IMLookHelper lookHelper;
	private final IMBodyHelper bodyHelper;
	private FlyState flyState;
	private float liftFactor;
	private float maxPoweredFlightSpeed;
	private float thrust;
	private float thrustComponentRatioMin;
	private float thrustComponentRatioMax;
	private float maxTurnForce;
	private float optimalPitch;
	private float landingSpeedThreshold;
	private float maxRunSpeed;
	private float flightAccelX;
	private float flightAccelY;
	private float flightAccelZ;
	private boolean thrustOn;
	private float thrustEffort;
	private boolean flyPathfind;
	private boolean debugFlying;
	
	public EntityIMFlying(World world)
	{
		this(world, null);
	}

	public EntityIMFlying(World world, INexusAccess nexus)
	{
		super(world, nexus);
		debugFlying = true;
		flyState = FlyState.GROUNDED;
		maxPoweredFlightSpeed = 0.28F;
		liftFactor = 0.4F;
		thrust = 0.08F;
		thrustComponentRatioMin = 0;
		thrustComponentRatioMax = 0.1F;
		maxTurnForce = getGravity() * 3;
		optimalPitch = 52;
		landingSpeedThreshold = moveSpeed * 1.2F;
		maxRunSpeed = 0.45F; // Units per tick
		thrustOn = false;
		thrustEffort = 1.0F;
		flyPathfind = true;
		
		moveHelper = new IMMoveHelperFlying(this);
		lookHelper = new IMLookHelper(this);
		bodyHelper = new IMBodyHelper(this);
		IPathSource pathSource = getPathSource();
        pathSource.setSearchDepth(800);
        pathSource.setQuickFailDepth(200);
		navigatorFlying = new NavigatorFlying(this, pathSource);
		
		dataWatcher.addObject(META_TARGET_X, (int)0);
		dataWatcher.addObject(META_TARGET_Y, (int)0);
		dataWatcher.addObject(META_TARGET_Z, (int)0);
		dataWatcher.addObject(META_THRUST_DATA, (byte)0);
		dataWatcher.addObject(META_FLYSTATE, (int)flyState.ordinal());
	}
	
	@Override
	public void onUpdate()
	{
		//setDead();
		super.onUpdate();
		if(!worldObj.isRemote)
		{
			if(debugFlying)
			{
				Vec3 target = navigatorFlying.getTarget();
				float oldTargetX = MathUtil.unpackFloat(dataWatcher.getWatchableObjectInt(META_TARGET_X));
				float oldTargetY = MathUtil.unpackFloat(dataWatcher.getWatchableObjectInt(META_TARGET_Y));
				float oldTargetZ = MathUtil.unpackFloat(dataWatcher.getWatchableObjectInt(META_TARGET_Z));
				
				// If any value has changed, update the coordinates
				if(!MathUtil.floatEquals(oldTargetX, (float)target.xCoord, 0.1F)
						|| !MathUtil.floatEquals(oldTargetY, (float)target.yCoord, 0.1F)
						|| !MathUtil.floatEquals(oldTargetZ, (float)target.zCoord, 0.1F))
				{
					dataWatcher.updateObject(META_TARGET_X, MathUtil.packFloat((float)(target.xCoord)));
					dataWatcher.updateObject(META_TARGET_Y, MathUtil.packFloat((float)(target.yCoord)));
					dataWatcher.updateObject(META_TARGET_Z, MathUtil.packFloat((float)(target.zCoord)));
				}
			}
			
			byte thrustData = dataWatcher.getWatchableObjectByte(META_THRUST_DATA);
			int oldThrustOn = (thrustData & 1);
			int oldThrustEffortEncoded = (thrustData >> 1) & 0xF;
			int thrustEffortEncoded = (int)(thrustEffort * 15F); // Assumes thrust effort is between 0 and 1.0
			if(thrustOn != oldThrustOn > 0 || thrustEffortEncoded != oldThrustEffortEncoded)
			{
				dataWatcher.updateObject(META_THRUST_DATA, (byte)((thrustEffortEncoded << 1) | oldThrustOn));
			}
		}
		else
		{
			//if(dataWatcher.hasChanges())
			{
				if(debugFlying)
				{
					float x = MathUtil.unpackFloat(dataWatcher.getWatchableObjectInt(META_TARGET_X));
					float y= MathUtil.unpackFloat(dataWatcher.getWatchableObjectInt(META_TARGET_Y));
					float z = MathUtil.unpackFloat(dataWatcher.getWatchableObjectInt(META_TARGET_Z));
					navigatorFlying.setTarget(x, y, z);
				}
				
				flyState = FlyState.values()[dataWatcher.getWatchableObjectInt(META_FLYSTATE)];
				
				byte thrustData = dataWatcher.getWatchableObjectByte(META_THRUST_DATA);
				thrustOn = (thrustData & 1) > 0;
				thrustEffort = (float)((thrustData >> 1) & 0xF) / 15F;
			}
		}
	}
	
	public FlyState getFlyState()
	{
		return flyState;
	}
	
	public boolean isThrustOn()
	{
		return dataWatcher.getWatchableObjectByte(META_THRUST_DATA) == 0 ? false : true;
	}
	
	public float getThrustEffort()
	{
		return thrustEffort;
	}
	
	public Vec3 getFlyTarget()
	{
		return navigatorFlying.getTarget();
	}
	
	/**
     * Returns the navigation component this entity uses.
     */
    public INavigationFlying getNavigatorNew()
    {
    	return navigatorFlying;
    }
    
    public IMMoveHelperFlying getMoveHelper()
    {
    	return moveHelper;
    }
    
    public IMLookHelper getLookHelper()
    {
    	return lookHelper;
    }
    
    public IMBodyHelper getBodyHelper()
    {
    	return bodyHelper;
    }
	
    /**
     * Moves the entity based on the specified heading.  Args: strafe, forward
     */
    public void moveEntityWithHeading(float x, float z)
    {
        if (this.isInWater())
        {
        	double y = posY;
            moveFlying(x, z, isAIEnabled() ? 0.04F : 0.02F);
            moveEntity(motionX, motionY, motionZ);
            motionX *= 0.8;
            motionY *= 0.8;
            motionZ *= 0.8;
            motionY -= 0.02;
            if(isCollidedHorizontally && isOffsetPositionInLiquid(motionX, ((motionY + 0.6) - posY) + y, motionZ))
                motionY = 0.3;
        }
        else if (this.handleLavaMovement())
        {
        	 double y = posY;
             moveFlying(x, z, isAIEnabled() ? 0.04F : 0.02F);
             moveEntity(motionX, motionY, motionZ);
             motionX *= 0.5;
             motionY *= 0.5;
             motionZ *= 0.5;
             motionY -= 0.02;
             if(isCollidedHorizontally && isOffsetPositionInLiquid(motionX, ((motionY + 0.6) - posY) + y, motionZ))
                 motionY = 0.3;
        }
        else
        {
        	float groundFriction = 0.9995F;
            float landMoveSpeed;
            if(onGround)
            {
                groundFriction = getGroundFriction(); 
                //int i = worldObj.getBlockId(MathHelper.floor_double(posX), MathHelper.floor_double(boundingBox.minY) - 1, MathHelper.floor_double(posZ));
                //if(i > 0)
                //    groundFriction *= Block.blocksList[i].slipperiness;
                
                float maxRunSpeed = getMaxRunSpeed();
                if(motionX*motionX + motionZ*motionZ < maxRunSpeed*maxRunSpeed)
                {
	                if (isAIEnabled())
	                    landMoveSpeed = getAIMoveSpeed();
	                else
	                    landMoveSpeed = landMovementFactor;
	                
	                landMoveSpeed *= 0.1627714F / (groundFriction * groundFriction * groundFriction);
	                moveFlying(x, z, landMoveSpeed); // Not flight; moving forwards
                }
            }
            else
            {
            	moveFlying(x, z, 0.01F);
            }

            motionX += flightAccelX;
            motionY += flightAccelY;
            motionZ += flightAccelZ;

            moveEntity(motionX, motionY, motionZ);
            motionY -= getGravity();
            motionY *= getAirResistance();
            motionX *= groundFriction * getAirResistance();
            motionZ *= groundFriction * getAirResistance();
        }

        prevLimbYaw = limbYaw;
        double dX = posX - prevPosX;
        double dZ = posZ - prevPosZ;
        float limbEnergy = MathHelper.sqrt_double(dX * dX + dZ * dZ) * 4F;

        if (limbEnergy > 1.0F)
        {
            limbEnergy = 1.0F;
        }

        limbYaw += (limbEnergy - limbYaw) * 0.4F;
        limbSwing += limbYaw;
    }

    /**
     * returns true if this entity is by a ladder, false otherwise
     */
    public boolean isOnLadder()
    {
        return false;
    }
    
    public boolean hasFlyingDebug()
    {
    	return debugFlying;
    }
    
    protected void setPathfindFlying(boolean flag)
    {
    	flyPathfind = flag;
    }
    
    protected void setFlyState(FlyState flyState)
    {
    	this.flyState = flyState;
    	if(!worldObj.isRemote)
    		dataWatcher.updateObject(META_FLYSTATE, flyState.ordinal());
    }
    
    protected float getMaxPoweredFlightSpeed()
    {
    	return maxPoweredFlightSpeed;
    }
    
    protected float getLiftFactor()
	{
		return liftFactor;
	}
	
	protected float getThrust()
	{
		return thrust;
	}
	
	protected float getThrustComponentRatioMin()
	{
		return thrustComponentRatioMin;
	}
	
	protected float getThrustComponentRatioMax()
	{
		return thrustComponentRatioMax;
	}
	
	protected float getMaxTurnForce()
	{
		return maxTurnForce;
	}
	
	protected float getMaxPitch()
	{
		return optimalPitch;
	}
	
	protected float getLandingSpeedThreshold()
	{
		return landingSpeedThreshold;
	}
	
	protected float getMaxRunSpeed()
	{
		return maxRunSpeed;
	}
	
	protected void setFlightAccelerationVector(float xAccel, float yAccel, float zAccel)
    {
    	flightAccelX = xAccel;
    	flightAccelY = yAccel;
    	flightAccelZ = zAccel;
    }
	
	protected void setThrustOn(boolean flag)
	{
		thrustOn = flag;
	}
	
	protected void setThrustEffort(float effortFactor)
	{
		thrustEffort = effortFactor;
	}
	
	protected void setMaxPoweredFlightSpeed(float speed)
	{
		maxPoweredFlightSpeed = speed;
		getNavigatorNew().setFlySpeed(speed);
	}
	
	protected void setThrust(float thrust)
	{
		this.thrust = thrust;
	}
	
	protected void setLiftFactor(float liftFactor)
	{
		this.liftFactor = liftFactor;
	}
	
	protected void setThrustComponentRatioMin(float ratio)
	{
		thrustComponentRatioMin = ratio;
	}
	
	protected void setThrustComponentRatioMax(float ratio)
	{
		thrustComponentRatioMax = ratio;
	}
	
	protected void setMaxTurnForce(float maxTurnForce)
	{
		this.maxTurnForce = maxTurnForce;
	}
	
	protected void setOptimalPitch(float pitch)
	{
		optimalPitch = pitch;
	}
	
	protected void setLandingSpeedThreshold(float speed)
	{
		landingSpeedThreshold = speed;
	}
	
	protected void setMaxRunSpeed(float speed)
	{
		maxRunSpeed = speed;
	}

	/**
     * Called when the mob is falling. Calculates and applies fall damage.
     */
    protected void fall(float par1) {}

    /**
     * Takes in the distance the entity has fallen this tick and whether its on the ground to update the fall distance
     * and deal fall damage if landing on the ground.  Args: distanceFallenThisTick, onGround
     */
    protected void updateFallState(double par1, boolean par3) {}
    
    @Override
    protected void calcPathOptions(IBlockAccess terrainMap, PathNode currentNode, PathfinderIM pathFinder)
    {
    	if(!flyPathfind)
    		super.calcPathOptions(terrainMap, currentNode, pathFinder);
    	else
    		calcPathOptionsFlying(terrainMap, currentNode, pathFinder);
    }
    
    protected void calcPathOptionsFlying(IBlockAccess terrainMap, PathNode currentNode, PathfinderIM pathFinder)
    {
    	if(currentNode.yCoord <= 0 || currentNode.yCoord > 255)
    		return;

    	// Check up
		if(getCollide(terrainMap, currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord) > 0)
			pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.NONE);
		
		// Check down
		if(getCollide(terrainMap, currentNode.xCoord, currentNode.yCoord - 1, currentNode.zCoord) > 0)
			pathFinder.addNode(currentNode.xCoord, currentNode.yCoord - 1, currentNode.zCoord, PathAction.NONE);
    	
		// Check adjacent
    	for(int i = 0; i < 4; i++)
    	{
    		if(getCollide(terrainMap, currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord, currentNode.zCoord + CoordsInt.offsetAdjZ[i]) > 0)
				pathFinder.addNode(currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord, currentNode.zCoord + CoordsInt.offsetAdjZ[i], PathAction.NONE);
    	}
    	
    	if(canSwimHorizontal())
    	{
    		for(int i = 0; i < 4; i++)
    		{
    			if(getCollide(terrainMap, currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord, currentNode.zCoord + CoordsInt.offsetAdjZ[i]) == -1)
    				pathFinder.addNode(currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord, currentNode.zCoord + CoordsInt.offsetAdjZ[i], PathAction.SWIM);
    		}
    	}
    }
    
    protected float calcBlockPathCost(PathNode prevNode, PathNode node, IBlockAccess terrainMap)
    {
    	float multiplier = 1.0F;
		if(terrainMap instanceof IBlockAccessExtended)
		{
			int mobDensity = ((IBlockAccessExtended)terrainMap).getLayeredData(node.xCoord, node.yCoord, node.zCoord) & 7;
			multiplier += mobDensity * 3;
		}
		
		// Check for blocks underneath flying path and modify cost. Encourage flying in open spaces
		for(int i = -1; i > -6; i--)
		{
			int id = terrainMap.getBlockId(node.xCoord, node.yCoord + i, node.zCoord);
			if(id != 0)
			{
				int blockType = getBlockType(id);
				if(blockType == 1)
				{
					continue;
				}
				multiplier += 1.0F - (-i * 0.2F); // Penalty of 1.0, 0.8, ..., 0.0
				if(blockType == 2 && i >= -2)
				{
					multiplier += 6.0F - (-i * 2.0); // Penalty of 4, 2
				}
				break;
			}
		}
		
		// Do the same on the horizontal
		for(int i = 0; i < 4; i++)
		{
			for(int j = 1; j <= 2; j++)
			{
				int id = terrainMap.getBlockId(node.xCoord + CoordsInt.offsetAdjX[i] * j, node.yCoord, node.zCoord + CoordsInt.offsetAdjZ[i] * j);
				int blockType = getBlockType(id);
				if(blockType == 1)
				{
					continue;
				}
				multiplier += 1.5F - (j * 0.5F); // Penalty of 1.0, 0.5
				if(blockType == 2 && i >= -2)
				{
					multiplier += 6.0F - (j * 2F); // Penalty of 4, 2
				}
				break;
			}
		}
			
		// Increase cost if digging upwards
		//if(node.yCoord > prevNode.yCoord && getCollide(terrainMap, node.xCoord, node.yCoord, node.zCoord) == 2)
		//{
		//	multiplier += 2;
		//}
		
		// Discourage destroying blocks with ladders attached
		//if(blockHasLadder(terrainMap, node.xCoord, node.yCoord, node.zCoord))
		//{
		//	multiplier += 5;
		//}
		
		if(node.action == PathAction.SWIM)
		{
			// Increase cost for swimming without air, but not surfacing
			multiplier *= node.yCoord <= prevNode.yCoord && terrainMap.getBlockId(node.xCoord, node.yCoord + 1, node.zCoord) != 0 ? 3.0F : 1.0F;
			return prevNode.distanceTo(node) * 1.3F * multiplier;
		}
		
    	int id = terrainMap.getBlockId(node.xCoord, node.yCoord, node.zCoord);
    	if(blockCosts.containsKey(id))
    	{
    		return prevNode.distanceTo(node) * blockCosts.get(id) * multiplier;
    	}
    	else if(Block.blocksList[id].isCollidable())
    	{
    		return prevNode.distanceTo(node) * DEFAULT_HARD_COST * multiplier;
    	}
    	else
    	{
    		return prevNode.distanceTo(node) * AIR_BASE_COST * multiplier;
    	}
    }
}
