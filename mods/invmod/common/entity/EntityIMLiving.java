
package mods.invmod.common.entity;


import java.util.HashMap;
import java.util.Map;

import mods.invmod.common.IBlockAccessExtended;
import mods.invmod.common.INotifyTask;
import mods.invmod.common.IPathfindable;
import mods.invmod.common.SparrowAPI;
import mods.invmod.common.mod_Invasion;
import mods.invmod.common.nexus.INexusAccess;
import mods.invmod.common.util.CoordsInt;
import mods.invmod.common.util.Distance;
import mods.invmod.common.util.IPosition;
import mods.invmod.common.util.MathUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * EntityIMWaveAttacker is an entity that is attached to a nexus and
 * is definable by IMMobConstruct.
 * 
 * @author Lieu
 */
public abstract class EntityIMLiving extends EntityCreature
    implements IMob, IPathfindable, IPosition, IHasNexus, SparrowAPI
{
	
	/**
     * Default navigator component for IM mobs. Seeks to replace {@link #getNavigator()},
     * also uses an interface to try and be friendlier to the next round of implementation.
     * This component should have the same lifetime as the object or otherwise be set in
     * a very controlled way (remove 'final' with caution. Setters easily break this kind
     * of composition).
     */
	private final NavigatorIM navigator;
	
	/**
	 * Compatibility wrapper for the navigator and minecraft classes. This should have
	 * the actual navigator behind it.
	 */
	private final PathNavigateAdapter oldNavAdapter;
    
	private PathCreator pathSource;
    protected Goal currentGoal;
    protected Goal prevGoal;
    protected EntityAITasks tasks;
    protected EntityAITasks targetTasks;
	private IMMoveHelper moveHelper;
	private MoveState moveState;
	private float rotationRoll;
	private float rotationYawHeadIM;
	private float rotationPitchHead;
	private float prevRotationRoll;
	private float prevRotationYawHeadIM;
	private float prevRotationPitchHead;
	private int debugMode;
	
	private float airResistance;
	private float groundFriction;
	private float gravityAcel;
	//private boolean isHoldingOntoLadder;
	protected float moveSpeedBase;
	private float turnRate;
	private float pitchRate;
	private int rallyCooldown;
	private IPosition currentTargetPos;
	private IPosition lastBreathExtendPos;
	private String simplyID;
	private String name;
	private String renderLabel;
	private boolean shouldRenderLabel;
	private int gender; // 0 for neither, 1 for male, 2 for female
	private boolean isHostile;
	private boolean creatureRetaliates;
    protected INexusAccess targetNexus;
    protected int attackStrength;
    protected float attackRange;
    protected int maxHealth;
    protected int selfDamage;
    protected int maxSelfDamage;
    protected int maxDestructiveness;
    protected float blockRemoveSpeed;
    protected boolean floatsInWater;
    private CoordsInt collideSize;
    private boolean canClimb;
    private boolean canDig;
    private boolean nexusBound;
    private boolean alwaysIndependent;
    private boolean burnsInDay;
    private int jumpHeight;
    private int aggroRange;
    private int senseRange;
    private int stunTimer;
    protected int throttled;
    protected int throttled2;
    protected int pathThrottle;
    protected int destructionTimer;
    protected int flammability;
    protected int destructiveness;
    protected Entity entityToAttack;
    
    protected static final int META_CLIMB_STATE = 20;
    protected static final byte META_CLIMBABLE_BLOCK = 21;
    protected static final byte META_JUMPING = 22;
    protected static final byte META_MOVESTATE = 23;
    protected static final byte META_ROTATION = 24;
    protected static final byte META_RENDERLABEL = 25;
	protected static final float DEFAULT_SOFT_STRENGTH = 2.5F;
    protected static final float DEFAULT_HARD_STRENGTH = 5.5F;
    protected static final float DEFAULT_SOFT_COST = 2.0F;
    protected static final float DEFAULT_HARD_COST = 3.2F;
    protected static final float AIR_BASE_COST = 1.0F;
    protected static final Map<Integer, Float> blockCosts = new HashMap<Integer, Float>();
    private static final Map<Integer, Float> blockStrength = new HashMap<Integer, Float>();
    private static final Map<Integer, BlockSpecial> blockSpecials = new HashMap<Integer, BlockSpecial>();
    private static final Map<Integer, Integer> blockType = new HashMap<Integer, Integer>();
    
	public EntityIMLiving(World world)
	{
		this(world, null);
	}
	
	public EntityIMLiving(World world, INexusAccess nexus)
    {
        super(world);
        targetNexus = nexus;
        currentGoal = Goal.NONE;
        prevGoal = Goal.NONE;
        moveState = MoveState.STANDING;
        tasks = new EntityAITasks(world.theProfiler);
        targetTasks = new EntityAITasks(world.theProfiler);
        pathSource = new PathCreator();
        navigator = new NavigatorIM(this, pathSource);
        oldNavAdapter = new PathNavigateAdapter(navigator);
        moveHelper = new IMMoveHelper(this);
        collideSize = new CoordsInt(MathHelper.floor_double(width + 1.0F), MathHelper.floor_double(height + 1.0F), MathHelper.floor_double(width + 1.0F));
        moveSpeed = moveSpeedBase = 0.26F;
        turnRate = 30F;
        pitchRate = 2F;
        CoordsInt initCoords = new CoordsInt(0, 0, 0);
        currentTargetPos = initCoords;
        lastBreathExtendPos = initCoords;
        simplyID = "needID";
        renderLabel = "";
        shouldRenderLabel = false;
        gender = 0;
        isHostile = true;
        creatureRetaliates = true;
        debugMode = 0;
        
        setAIMoveSpeed(0.1F); // Really acceleration
        
        airResistance = 0.9995F;
        groundFriction = 0.546F;
        gravityAcel = 0.08F;
        //isHoldingOntoLadder = false;
        
        // Mob stats and attributes
        attackStrength = 2;
        attackRange = 0;
        maxHealth = 20;
        health = 20;
        selfDamage = 2;
        maxSelfDamage = 6;
        flammability = 2;
        isImmuneToFire = false;
        canClimb = false;
        canDig = true;
        floatsInWater = true;
        alwaysIndependent = false;
        jumpHeight = 1;
        experienceValue = 5;
        maxDestructiveness = 0;
        blockRemoveSpeed = 1.0f;
        setBurnsInDay(false);
        setAggroRange(10);
        setSenseRange(3);
        if(nexus != null)
        	nexusBound = true;
        else
        	nexusBound = false;
        
        // Status
        hasAttacked = false;
        destructionTimer = 0;
        destructiveness = 0;
        throttled = 0;
        throttled2 = 0;
        pathThrottle = 0;
        
        dataWatcher.addObject(META_CLIMB_STATE, (byte)0);
        dataWatcher.addObject(META_CLIMBABLE_BLOCK, (byte)0);
        dataWatcher.addObject(META_JUMPING, (byte)0);
        dataWatcher.addObject(META_MOVESTATE, (int)moveState.ordinal());
        dataWatcher.addObject(META_ROTATION, (int)MathUtil.packAnglesDeg(rotationRoll, rotationYawHeadIM, rotationPitchHead, 0));
        dataWatcher.addObject(META_RENDERLABEL, (String)"");
    }
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		prevRotationRoll = rotationRoll;
		prevRotationYawHeadIM = rotationYawHeadIM;
		prevRotationPitchHead = rotationPitchHead;
		if(worldObj.isRemote)
		{
			moveState = MoveState.values()[dataWatcher.getWatchableObjectInt(META_MOVESTATE)];
			int packedAngles = dataWatcher.getWatchableObjectInt(META_ROTATION);
			rotationRoll = MathUtil.unpackAnglesDeg_1(packedAngles);
			rotationYawHeadIM = MathUtil.unpackAnglesDeg_2(packedAngles);
			rotationPitchHead = MathUtil.unpackAnglesDeg_3(packedAngles);
			renderLabel = dataWatcher.getWatchableObjectString(META_RENDERLABEL);
			
			//System.out.println(",> " + getRotationPitchHead() + ", " + rotationPitch);
		}
		else
		{
			int packedAngles = MathUtil.packAnglesDeg(rotationRoll, rotationYawHeadIM, rotationPitchHead, 0);
			if(packedAngles != dataWatcher.getWatchableObjectInt(META_ROTATION))
				dataWatcher.updateObject(META_ROTATION, packedAngles);
			
			if(!renderLabel.equals(dataWatcher.getWatchableObjectString(META_RENDERLABEL)))
				dataWatcher.updateObject(META_RENDERLABEL, renderLabel);
		}
	}
	
	@Override
	public void onEntityUpdate()
	{
		super.onEntityUpdate();
		
		if(worldObj.isRemote)
		{
			/*if(dataWatcher.getWatchableObjectByte(META_CLIMB_STATE) == 1)
				isHoldingOntoLadder = true;
			else
				isHoldingOntoLadder = false;*/
			
			if(dataWatcher.getWatchableObjectByte(META_JUMPING) == 1)
				isJumping = true;
			else
				isJumping = false;
		}
		else
		{
			setAdjacentClimbBlock(checkForAdjacentClimbBlock());
		}
		
		// Extend breath if good progress is made. Prevents premature drowning.
		if(getAir() == 190)
		{
			lastBreathExtendPos = new CoordsInt(getXCoord(), getYCoord(), getZCoord());
		}
		else if(getAir() == 0)
		{
			IPosition pos = new CoordsInt(getXCoord(), getYCoord(), getZCoord());
			if(Distance.distanceBetween(lastBreathExtendPos, pos) > 4.0)
			{
				lastBreathExtendPos = pos;
				setAir(180);
			}
		}
		
		if(simplyID == "needID")
	    {
			//simplyID = SimplyID.getNextSimplyID(this);
	    }
	}

	@Override
	public void onLivingUpdate()
    {
    	if(!nexusBound)
    	{
	        float brightness = getBrightness(1.0F);
	        if(brightness > 0.5F || posY < 55)
	            entityAge += 2;
	        
	        if(getBurnsInDay() && worldObj.isDaytime() && !worldObj.isRemote)
	        {
	            if(brightness > 0.5F && worldObj.canBlockSeeTheSky(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ)) && rand.nextFloat() * 30F < (brightness - 0.4F) * 2.0F)
	            {
	            	sunlightDamageTick();
	            }
	        }
    	}
        super.onLivingUpdate();
    }
    
    @Override
	public boolean attackEntityFrom(DamageSource damagesource, int damage)
    {
        if(super.attackEntityFrom(damagesource, damage))
        {
            Entity entity = damagesource.getEntity();
            if(riddenByEntity == entity || ridingEntity == entity)
                return true;
            
            if(entity != this)
                entityToAttack = entity;
            
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public boolean stunEntity(int ticks)
    {
    	if(stunTimer < ticks)
    		stunTimer = ticks;
    	
    	motionX = 0;
    	motionZ = 0;
    	return true;
    }
    
	@Override
	public boolean attackEntityAsMob(Entity entity)
    {
        return entity.attackEntityFrom(DamageSource.causeMobDamage(this), attackStrength);
    }
	
	public boolean attackEntityAsMob(Entity entity, int damageOverride)
    {
		return entity.attackEntityFrom(DamageSource.causeMobDamage(this), damageOverride);
    }

    @Override
	public void moveEntityWithHeading(float x, float z)
    {
    	if(isInWater())
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
        else if(handleLavaMovement())
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
            float groundFriction = 0.91F;
            float landMoveSpeed;
            if(onGround)
            {
                groundFriction = getGroundFriction(); 
                int i = worldObj.getBlockId(MathHelper.floor_double(posX), MathHelper.floor_double(boundingBox.minY) - 1, MathHelper.floor_double(posZ));
                if(i > 0)
                    groundFriction = Block.blocksList[i].slipperiness * 0.91F;
                
                if (isAIEnabled())
                    landMoveSpeed = getAIMoveSpeed();
                else
                    landMoveSpeed = landMovementFactor;
                
                landMoveSpeed *= 0.1627714F / (groundFriction * groundFriction * groundFriction);
            }
            else
            {
                landMoveSpeed = jumpMovementFactor;
            }

            moveFlying(x, z, landMoveSpeed);

            
            if(isOnLadder())
            {
                float maxLadderXZSpeed = 0.15F;
                if(motionX < (-maxLadderXZSpeed))
                    motionX = -maxLadderXZSpeed;               
                if(motionX > maxLadderXZSpeed)
                    motionX = maxLadderXZSpeed;
                if(motionZ < (-maxLadderXZSpeed))
                    motionZ = -maxLadderXZSpeed;  
                if(motionZ > maxLadderXZSpeed)
                    motionZ = maxLadderXZSpeed;
                
                fallDistance = 0.0F;
                if(motionY < -0.15)
                    motionY = -0.15;
                
                if(isHoldingOntoLadder() || (isSneaking() && motionY < 0))
                    motionY = 0;
                else if(worldObj.isRemote && isJumping)
                	motionY += 0.04D;
            }

            moveEntity(motionX, motionY, motionZ);

            if ((isCollidedHorizontally/* || isJumping*/) && isOnLadder())
                motionY = 0.2;

            motionY -= getGravity();
            motionY *= airResistance;
            motionX *= groundFriction * airResistance;
            motionZ *= groundFriction * airResistance;
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
     * Adds X and Z velocity to the entity according to facing, limited to
     * a certain speed, multiplied by a movement factor.
     */
    @Override
	public void moveFlying(float strafeAmount, float forwardAmount, float movementFactor)
    {
        float unit = MathHelper.sqrt_float(strafeAmount * strafeAmount + forwardAmount * forwardAmount);

        if (unit < 0.01F)
        {
            return;
        }

        if (unit < 20.0F) // Set the speed limit higher because IM mobs need it
        {
            unit = 1.0F;
        }

        unit = movementFactor / unit;
        strafeAmount *= unit;
        forwardAmount *= unit;
        
        // Split movement into x and z axis (sin(X) + cos(x) = 1)
        float com1 = MathHelper.sin((rotationYaw * (float)Math.PI) / 180F);
        float com2 = MathHelper.cos((rotationYaw * (float)Math.PI) / 180F);
        motionX += strafeAmount * com2 - forwardAmount * com1;
        motionZ += forwardAmount * com2 + strafeAmount * com1;
    }
    
    @Override
	public boolean handleWaterMovement()
	{
		if(floatsInWater)
		{
			return worldObj.handleMaterialAcceleration(boundingBox.expand(0.0, -0.4, 0.0).contract(0.001, 0.001, 0.001), Material.water, this);
		}
		else
		{
			// Dirty way of bypassing water pushing without changing base classes
	    	double vX = motionX;
	    	double vY = motionY;
	    	double vZ = motionZ;
	    	boolean isInWater = worldObj.handleMaterialAcceleration(boundingBox.expand(0.0, -0.4, 0.0).contract(0.001, 0.001, 0.001), Material.water, this);
	    	motionX = vX;
	    	motionY = vY;
	    	motionZ = vZ;
	    	return isInWater;
		}
	}

	public void rally(Entity leader)
	{
		rallyCooldown = 300;
	}

	/**
	 * Called when the entity acquires a new target to follow. Null for target lost.
	 */
	public void onFollowingEntity(Entity entity)
	{
	}

	public void onPathSet()
	{
	}

	public void onBlockRemoved(int x, int y, int z, int id)
	{
		// Damage self if appropriate
		if(health > maxHealth - maxSelfDamage)
		{
			attackEntityFrom(DamageSource.generic, selfDamage);
		}
		
		// Play a sound
		if(throttled == 0 && (id == 3 || id == 2 || id == 12 || id == 13))
		{
			worldObj.playSoundAtEntity(this, "step.gravel", 1.4F, 1.0F / (rand.nextFloat() * 0.6F + 1.0F));
			//worldObj.setEntityState(this, (byte)6);
			throttled = 5;
		}
		else
		{
			worldObj.playSoundAtEntity(this, "step.stone", 1.4F, 1.0F / (rand.nextFloat() * 0.6F + 1.0F));
			//worldObj.setEntityState(this, (byte)5);
			throttled = 5;
		}
	}

	/**
	 * Returns true if this mob considers the block a hazard and avoids it.
	 */
	public boolean avoidsBlock(int id)
	{
		if(id == Block.fire.blockID || id == Block.bedrock.blockID || id == Block.lavaStill.blockID || id == Block.lavaMoving.blockID)
		{
			return true;
		}
		return false;
	}
	
	public boolean ignoresBlock(int id)
	{
		if(id == Block.tallGrass.blockID || id == Block.deadBush.blockID || id == Block.plantRed.blockID
		   || id == Block.plantYellow.blockID || id == Block.mushroomBrown.blockID || id == Block.mushroomRed.blockID
		   || id == Block.pressurePlatePlanks.blockID || id == Block.pressurePlateIron.blockID || id == Block.pressurePlateStone.blockID)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if this mob can destroy the block with given id.
	 */
	public boolean isBlockDestructible(IBlockAccess terrainMap, int x, int y, int z, int id)
	{
		return isBlockTypeDestructible(id);
	}

	public boolean isBlockTypeDestructible(int id)
	{
		return false;
	}

	public boolean canEntityBeDetected(Entity entity)
	{
		float distance = getDistanceToEntity(entity);
	    return distance  <= getSenseRange() || (canEntityBeSeen(entity) && distance <= getAggroRange());
	}

	public double findDistanceToNexus()
	{
		if(targetNexus == null)
			return Double.MAX_VALUE;
			
		double x = targetNexus.getXCoord() + 0.5 - posX;
		double y = targetNexus.getYCoord() - posY + (height * 0.5);
		double z = targetNexus.getZCoord() + 0.5 - posZ;
		return Math.sqrt(x * x + y * y + z * z);
	}

	@Override
	public Entity findPlayerToAttack()
	{
		EntityPlayer entityPlayer = worldObj.getClosestPlayerToEntity(this, getSenseRange());
		if(entityPlayer != null)
			return entityPlayer;
		
	    entityPlayer = worldObj.getClosestPlayerToEntity(this, getAggroRange());
	    if(entityPlayer != null && canEntityBeSeen(entityPlayer))
	        return entityPlayer;
	
	    return null;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
	{
		nbttagcompound.setBoolean("alwaysIndependent", alwaysIndependent);
	    super.writeEntityToNBT(nbttagcompound);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
	{
		alwaysIndependent = nbttagcompound.getBoolean("alwaysIndependent");
		if(alwaysIndependent)
		{
			setAggroRange(mod_Invasion.getNightMobSightRange());
			setSenseRange(mod_Invasion.getNightMobSenseRange());
			setBurnsInDay(mod_Invasion.getNightMobsBurnInDay());
		}
	    super.readEntityFromNBT(nbttagcompound);
	}
	
	public float getPrevRotationRoll()
	{
		return prevRotationRoll;
	}
	
	public float getRotationRoll()
	{
		return rotationRoll;
	}
	
	public float getPrevRotationYawHeadIM()
	{
		return prevRotationYawHeadIM;
	}
	
	public float getRotationYawHeadIM()
	{
		return rotationYawHeadIM;
	}
	
	public float getPrevRotationPitchHead()
	{
		return prevRotationPitchHead;
	}
	
	public float getRotationPitchHead()
	{
		return rotationPitchHead;
	}

	@Override
	public int getXCoord()
	{
		return MathHelper.floor_double(posX);
	}

	@Override
	public int getYCoord()
	{
		return MathHelper.floor_double(posY);
	}

	@Override
	public int getZCoord()
	{
		return MathHelper.floor_double(posZ);
	}
	
	public float getAttackRange()
	{
		return attackRange;
	}
	
	@Override
	public int getMaxHealth()
	{
		return maxHealth;
	}

	@Override
	public boolean getCanSpawnHere()
	{
		boolean lightFlag = false;
		if(nexusBound || getLightLevelBelow8())
			lightFlag = true;
		
		return super.getCanSpawnHere() && lightFlag && worldObj.isBlockNormalCube(MathHelper.floor_double(posX), MathHelper.floor_double(boundingBox.minY + 0.5D) - 1, MathHelper.floor_double(posZ));
	}
	
	public MoveState getMoveState()
	{
		return moveState;
	}

	public int getJumpHeight()
	{
		return jumpHeight;
	}

	// Get a block's strength vs the mob's digging
	public float getBlockStrength(int x, int y, int z)
	{
		return getBlockStrength(x, y, z, worldObj.getBlockId(x, y, z));
	}

	public float getBlockStrength(int x, int y, int z, int id)
	{
		return getBlockStrength(x, y, z, id, worldObj);
	}

	public boolean getCanClimb()
	{
		return canClimb;
	}

	public boolean getCanDigDown()
	{
		return canDig;
	}

	public int getAggroRange()
	{
		return aggroRange;
	}
	
	public int getSenseRange()
	{
		return senseRange;
	}

	@Override
	public float getBlockPathWeight(int i, int j, int k)
	{
		if(nexusBound)
			return 0.0F;
		else
			return 0.5F - worldObj.getLightBrightness(i, j, k);
	}

	public boolean getBurnsInDay()
	{
		return burnsInDay;
	}

	public int getDestructiveness()
	{
		return destructiveness;
	}

	public float getTurnRate()
	{
		return turnRate;
	}
	
	public float getPitchRate()
	{
		return pitchRate;
	}

	public float getGravity()
	{
		return gravityAcel;
	}
	
	public float getAirResistance()
	{
		return airResistance;
	}
	
	public float getGroundFriction()
	{
		return groundFriction;
	}

	public CoordsInt getCollideSize()
	{
		return collideSize;
	}

	public static BlockSpecial getBlockSpecial(int id)
    {
    	if(blockSpecials.containsKey(id))
    		return blockSpecials.get(id);
    	else
    		return BlockSpecial.NONE;
    }
    
    public Goal getAIGoal()
    {
    	return currentGoal;
    }
    
    public Goal getPrevAIGoal()
    {
    	return prevGoal;
    }
    
    public float getMoveSpeed()
    {
    	return moveSpeed;
    }
    
    /**
     * Compatibility method for base minecraft classes. Semi-compatible adapter.
     * Setting paths and updating is preserved, but setting or getting other
     * internal state is not, such as getting PathEntity objects.
     * 
     * Use {@link #getNavigatorNew()} instead.
     * 
     * Underlying object should be the same as {@link #getNavigatorNew()}. No
     * guarantees are made about compatibility.
     */
    @Override
	public PathNavigateAdapter getNavigator()
    {
    	return oldNavAdapter;
    }
    
    /**
     * Returns the navigation component this entity uses.
     */
    public INavigation getNavigatorNew()
    {
    	return navigator;
    }
    
    public IPathSource getPathSource()
    {
    	return pathSource;
    }
    
    /**
     * Returns the pathfinding cost of moving from one node to another.
     */
    @Override
	public float getBlockPathCost(PathNode prevNode, PathNode node, IBlockAccess terrainMap)
    {
    	return calcBlockPathCost(prevNode, node, terrainMap);
    }
    
    @Override
	public void getPathOptionsFromNode(IBlockAccess terrainMap, PathNode currentNode, PathfinderIM pathFinder)
    {
    	calcPathOptions(terrainMap, currentNode, pathFinder);
    }
    
    public IPosition getCurrentTargetPos()
    {
    	return currentTargetPos;
    }
    
    public IPosition[] getBlockRemovalOrder(int x, int y, int z)
	{
		if(MathHelper.floor_double(posY) >= y)
		{
			IPosition[] blocks = new IPosition[2];
			blocks[1] = new CoordsInt(x, y + 1, z);
			blocks[0] = new CoordsInt(x, y, z);
			return blocks;
		}
		else
		{
			IPosition[] blocks = new IPosition[3];
			blocks[2] = new CoordsInt(x, y, z);
			blocks[1] = new CoordsInt(MathHelper.floor_double(posX), MathHelper.floor_double(posY) + collideSize.getYCoord(), MathHelper.floor_double(posZ));
			blocks[0] = new CoordsInt(x, y + 1, z);
			return blocks;
		}
	}
	
	@Override
	public EntityMoveHelper getMoveHelper()
	{
	    return moveHelper;
	}

	@Override
	public INexusAccess getNexus()
	{
		return targetNexus;
	}
	
	public String getRenderLabel()
	{
		return renderLabel;
	}
	
	public int getDebugMode()
	{
		return debugMode;
	}

	// --------  Sparrow API  --------- //
	
	/**Does this entity attack the player on sight?*/
    @Override
	public boolean isHostile()
    {
    	return isHostile;
    }
	
    /**Will this entity attack, but only when provoked?*/
    @Override
	public boolean isNeutral()
    {
    	return creatureRetaliates;
    }
    
    /**Should this entity be considered a threat to par1entity?*/
    @Override
	public boolean isThreatTo(Entity entity)
    {
    	if(isHostile && entity instanceof EntityPlayer)
    		return true;
    	else
    		return false;
    }
    
    /**What is this entity currently targeting with intent to kill? Used to differentiate between the attack method monsters use and the attack method used for breeding and following*/
    @Override
	public Entity getAttackingTarget()
    {
    	return getAttackTarget();
    }
	
	/**Is this thing like a creeper, in that engaging it in combat has unexpected consequences? Would this entity and its allies be better off not fighting it at all? Set this to true if the attacker's combat abilities are basically a non-factor in what will happen to it if it fights this.*/
	@Override
	public boolean isStupidToAttack()
	{
		return false;
	}

	/**When a mod triggers an event that would set an entity to be dead with no reference to damage, should this entity be spared?*/
	@Override
	public boolean doNotVaporize()
	{
		return false;
	}

	/**Does this entity attack non-player entities on sight?*/
	@Override
	public boolean isPredator()
	{
		return false;
	}

	/**Is this entity incapable of combat?*/
	@Override
	public boolean isPeaceful()
	{
		return false;
	}

	/**Is this entity viable prey for a predator?*/
	@Override
	public boolean isPrey()
	{
		return false;
	}

	/**Is this entity incapable of taking damage, and thus pointless to attack?*/
	@Override
	public boolean isUnkillable()
	{
		return false;
	}

	/**Should this entity be considered a friend of par1entity?*/
	@Override
	public boolean isFriendOf(Entity par1entity)
	{
		return false;
	}

	/**Is this entity what people would generally consider to be an NPC?*/
	@Override
	public boolean isNPC()
	{
		return false;
	}

	/**Is this a pet? 0 if not, 1 if it can be but isn't currently, 2 if it is.*/
	@Override
	public int isPet()
	{
		return 0;
	}

	/**What is the name of this individual entity?*/
	@Override
	public String getName()
	{
		return name;
	}

	/**What is this entity's gender? 1 for male, 2 for female, 0 for neither*/
	@Override
	public int getGender()
	{
		return gender;
	}

	/**Who is this pet's owner?*/
	@Override
	public Entity getPetOwner()
	{
		return null;
	}

	/**What is the size of this entity? Multiply its two dimensions (X and Z are considered the same) in terms of blocks and put in the result ( a chicken would be .3 * .7, which is roughly .2)*/
	@Override
	public float getSize()
	{
		return height * width;
	}

	/**This is for mod-specific features. A mod can search for a response to a custom string, and you can add in whether or not they'll respond to it here, and what the response will be.*/
	@Override
	public String customStringAndResponse(String s)
	{
		return null;
	}
	
	@Override
	public int getTier()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/**Have this return the string you store your simplyID in. Use the function SimplyID.getNextSimplyID(this) to assign a simplyID to your entities that implement Sparrow.*/
	@Override
	public String getSimplyID()
	{
		return simplyID;
	}
	
	// ------------------------------ //

	public boolean isNexusBound()
	{
		return nexusBound;
	}

	public boolean isHoldingOntoLadder()
	{
		return dataWatcher.getWatchableObjectByte(META_CLIMB_STATE) == 1;
		//return isHoldingOntoLadder;
	}
	
	@Override
	public boolean isOnLadder()
	{
		return isAdjacentClimbBlock();
	}
	
	public boolean isAdjacentClimbBlock()
	{
		return dataWatcher.getWatchableObjectByte(META_CLIMBABLE_BLOCK) == 1;
	}
	
	public boolean checkForAdjacentClimbBlock()
	{
		int var1 = MathHelper.floor_double(this.posX);
        int var2 = MathHelper.floor_double(this.boundingBox.minY);
        int var3 = MathHelper.floor_double(this.posZ);
        int var4 = this.worldObj.getBlockId(var1, var2, var3);
        return Block.blocksList[var4] != null && Block.blocksList[var4].isLadder(worldObj, var1, var2, var3);
	}

	public boolean readyToRally()
	{
		return rallyCooldown == 0;
	}

	public boolean canSwimHorizontal()
	{
		return true;
	}
	
	public boolean canSwimVertical()
	{
		return true;
	}
	
	public boolean shouldRenderLabel()
	{
		return shouldRenderLabel;
	}

	@Override
	public void acquiredByNexus(INexusAccess nexus)
	{
		if(targetNexus == null && !alwaysIndependent)
		{
			targetNexus = nexus;
			nexusBound = true;
		}
	}

	@Override
	public void setDead()
	{
		super.setDead();
		if(health <= 0 && targetNexus != null)
			targetNexus.registerMobDied();
	}
	
	public void setEntityIndependent()
	{
		targetNexus = null;
		nexusBound = false;
		alwaysIndependent = true;
	}

	@Override
	public void setSize(float width, float height)
	{
		super.setSize(width, height);
		collideSize = new CoordsInt(MathHelper.floor_double(width + 1.0F), MathHelper.floor_double(height + 1.0F), MathHelper.floor_double(width + 1.0F));
	}

	public void setBurnsInDay(boolean flag)
	{
		burnsInDay = flag;
	}

	public void setAggroRange(int range)
	{
		aggroRange = range;
	}

	public void setSenseRange(int range)
	{
		senseRange = range;
	}

	public void setIsHoldingIntoLadder(boolean flag)
	{
		//isHoldingOntoLadder = flag;
		if(!worldObj.isRemote)
			dataWatcher.updateObject(META_CLIMB_STATE, flag ? (byte)1 : (byte)0);
	}
	
	@Override
	public void setJumping(boolean flag)
    {
        super.setJumping(flag);
        if(!worldObj.isRemote)
        	dataWatcher.updateObject(META_JUMPING, flag ? (byte)1 : (byte)0);
    }
	
	public void setAdjacentClimbBlock(boolean flag)
	{
		if(!worldObj.isRemote)
			dataWatcher.updateObject(META_CLIMBABLE_BLOCK, flag ? (byte)1 : (byte)0);
	}
	
	public void setRenderLabel(String label)
	{
		renderLabel = label;
	}
	
	public void setShouldRenderLabel(boolean flag)
	{
		shouldRenderLabel = flag;
	}
	
	public void setDebugMode(int mode)
	{
		debugMode = mode;
		onDebugChange();
	}
	
	@Override
	protected void updateAITasks()
	{
		worldObj.theProfiler.startSection("Entity IM");
		entityAge++;
		despawnEntity();
		getEntitySenses().clearSensingCache();
		targetTasks.onUpdateTasks();
		updateAITick();
	    tasks.onUpdateTasks();
	    getNavigatorNew().onUpdateNavigation();
	    getLookHelper().onUpdateLook();
	    getMoveHelper().onUpdateMoveHelper();
	    getJumpHelper().doJump();
	    worldObj.theProfiler.endSection();
	}

	@Override
	protected void updateAITick()
	{
		if(rallyCooldown > 0)
			rallyCooldown--;
		
		if(getAttackTarget() != null)
			currentGoal = Goal.TARGET_ENTITY;
		else if(targetNexus != null)
			currentGoal = Goal.BREAK_NEXUS;
		else
			currentGoal = Goal.CHILL;
	}

	@Override
	protected boolean isAIEnabled()
	{
	    return true;
	}

	@Override
	protected boolean canDespawn()
	{
		return !nexusBound;
	}
	
	protected void setRotationRoll(float roll)
	{
		rotationRoll = roll;
	}
	
	public void setRotationYawHeadIM(float yaw)
	{
		rotationYawHeadIM = yaw;
	}
	
	protected void setRotationPitchHead(float pitch)
	{
		rotationPitchHead = pitch;
	}
	
	protected void setAttackRange(float range)
	{
		attackRange = range;
	}

	protected void setCurrentTargetPos(IPosition pos)
	{
		currentTargetPos = pos;
	}

	@Override
	protected void attackEntity(Entity entity, float f)
	{
	    if(attackTime <= 0 && f < 2.0F && entity.boundingBox.maxY > boundingBox.minY && entity.boundingBox.minY < boundingBox.maxY)
	    {
	        attackTime = 38;
	        attackEntityAsMob(entity);
	    }
	}
	
	protected void sunlightDamageTick()
	{
		setFire(8);
	}

	/**
	 * Callback for how to handle a path being obstructed. Deals with, or
	 * delegates the issue and returns true if caller should wait for notification.
	 * Returns false if caller should continue immediately(probably dropping the path).
	 */
	protected boolean onPathBlocked(Path path, INotifyTask asker)
	{
		/*else if(targetNexus != null && findDistanceToNexus() < 1.5 && x == targetNexus.getXCoord() && y == targetNexus.getYCoord() && z == targetNexus.getZCoord())
		{
			tryAttackNexus();
		}*/
		
		return false;
	}

	@Override
	protected void dealFireDamage(int i)
	{
	    super.dealFireDamage(i * flammability);
	}

	@Override
	protected void dropFewItems(boolean flag, int amount)
	{
	    if(rand.nextInt(4) == 0)
	    {
	    	entityDropItem(new ItemStack(mod_Invasion.itemRemnants, 1, 1), 0.0F);
	    }
	}

	protected float calcBlockPathCost(PathNode prevNode, PathNode node, IBlockAccess terrainMap)
    {
    	float multiplier = 1.0F;
		if(terrainMap instanceof IBlockAccessExtended)
		{
			int mobDensity = ((IBlockAccessExtended)terrainMap).getLayeredData(node.xCoord, node.yCoord, node.zCoord) & 7;
			multiplier += mobDensity * 3;
			/*if(mobDensity > 0)
			{
				worldObj.spawnParticle("heart", getXCoord() + 0.2, getYCoord() + 0.2, getZCoord() + 0.2, getXCoord() + 0.5, getYCoord() + 0.5, getZCoord() + 0.5);
			}*/
		}
		
		// Increase cost if digging upwards
		if(node.yCoord > prevNode.yCoord && getCollide(terrainMap, node.xCoord, node.yCoord, node.zCoord) == 2)
		{
			multiplier += 2;
		}
		
		// Discourage destroying blocks with ladders attached
		if(blockHasLadder(terrainMap, node.xCoord, node.yCoord, node.zCoord))
		{
			multiplier += 5;
		}
		
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
    
    protected void calcPathOptions(IBlockAccess terrainMap, PathNode currentNode, PathfinderIM pathFinder)
    {
    	if(currentNode.yCoord <= 0 || currentNode.yCoord > 255)
    		return;
    	
    	// Get vertical path options
    	calcPathOptionsVertical(terrainMap, currentNode, pathFinder);
    	
    	if(currentNode.action == PathAction.DIG && !canStandAt(terrainMap, currentNode.xCoord, currentNode.yCoord, currentNode.zCoord))
    		return;
    	
    	// Calculate jumping or moving on to adjacent XZ blocks
    	// Check jump clearance; very important
    	int height = getJumpHeight();
    	for(int i = 1; i <= height; i++)
    	{
    		if(getCollide(terrainMap, currentNode.xCoord, currentNode.yCoord + i, currentNode.zCoord) == 0)
    			height = i - 1;
    	}
    	
    	// Check adjacent columns (x,z) from jump height downwards for valid points, down to a maximum fall distance.
    	int maxFall = 7;
    	for(int i = 0; i < 4; i++)
    	{
	    	int yOffset = 0;
	    	int currentY = currentNode.yCoord + height;
	    	boolean passedLevel = false;
	    	do
	    	{
	    		yOffset = getNextLowestSafeYOffset(terrainMap, currentNode.xCoord + CoordsInt.offsetAdjX[i], currentY, currentNode.zCoord + CoordsInt.offsetAdjZ[i], maxFall + currentY - currentNode.yCoord);
	    		if(yOffset < 1)
	    		{
	    			if(yOffset > -maxFall)
	    			{
	    				pathFinder.addNode(currentNode.xCoord + CoordsInt.offsetAdjX[i], currentY + yOffset, currentNode.zCoord + CoordsInt.offsetAdjZ[i], PathAction.NONE);
	    			}
	    			
	    			currentY += yOffset - 1;
	    			
	    			if(!passedLevel && currentY <= currentNode.yCoord)
	    			{
	    				passedLevel = true;
	    				if(currentY != currentNode.yCoord)
	    				{
	    					addAdjacent(terrainMap, currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord, currentNode.zCoord + CoordsInt.offsetAdjZ[i], pathFinder);
	    				}
	    			}
	    		}
	    		else
	    		{
	    			break;
	    		}
	    	}
	    	while(currentY >= currentNode.yCoord);
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
    
    protected void calcPathOptionsVertical(IBlockAccess terrainMap, PathNode currentNode, PathfinderIM pathFinder)
	{
		int collideUp = getCollide(terrainMap, currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord);
		if(collideUp > 0)
		{
	    	// Add node y+1 if there is a ladder
	    	if(terrainMap.getBlockId(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord) == Block.ladder.blockID)
					//|| terrainMap.getBlockId(currentNode.xCoord, currentNode.yCoord, currentNode.zCoord) == 65)
					//|| terrainMap.getBlockId(currentNode.xCoord, currentNode.yCoord - 1, currentNode.zCoord) == 65)
			{
	    		int meta = terrainMap.getBlockMetadata(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord);
	    		PathAction action = PathAction.NONE;
	    		if(meta == 4)
	    			action = PathAction.LADDER_UP_PX;
	    		else if(meta == 5)
	    			action = PathAction.LADDER_UP_NX;
	    		else if(meta == 2)
	    			action = PathAction.LADDER_UP_PZ;
	    		else if(meta == 3)
	    			action = PathAction.LADDER_UP_NZ;
	    			
	    		// Previous action being NONE covers 99% of cases
	    		if(currentNode.action == PathAction.NONE)
	    		{
	    			pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, action);
	    		}
	    		else if(currentNode.action == PathAction.LADDER_UP_PX || currentNode.action == PathAction.LADDER_UP_NX
	    				|| currentNode.action == PathAction.LADDER_UP_PZ || currentNode.action == PathAction.LADDER_UP_NZ)
	    		{
	    			if(action == currentNode.action)
	    				pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, action);
	    		}
	    		else
	    		{
	    			pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, action);
	    		}
			}
	    	else if(getCanClimb())
	    	{
	    		if(isAdjacentSolidBlock(terrainMap, currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord))
	    			pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.NONE);
	    	}
		}
		
		int below = getCollide(terrainMap, currentNode.xCoord, currentNode.yCoord - 1, currentNode.zCoord);
		int above = getCollide(terrainMap, currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord);
		if(getCanDigDown())
		{
			if(below == 2)
			{
				pathFinder.addNode(currentNode.xCoord, currentNode.yCoord - 1, currentNode.zCoord, PathAction.DIG);
			}
			else if(below == 1)
			{
				int maxFall = 5;
				int yOffset = getNextLowestSafeYOffset(terrainMap, currentNode.xCoord, currentNode.yCoord - 1, currentNode.zCoord, maxFall);
				if(yOffset <= 0)
				{
					pathFinder.addNode(currentNode.xCoord, currentNode.yCoord - 1 + yOffset, currentNode.zCoord, PathAction.NONE);
				}
			}
		}
		
		if(canSwimVertical())
		{
			if(below == -1)
				pathFinder.addNode(currentNode.xCoord, currentNode.yCoord - 1, currentNode.zCoord, PathAction.SWIM);
			
			if(above == -1)
				pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.SWIM);
		}
	}

	protected void addAdjacent(IBlockAccess terrainMap, int x, int y, int z, PathfinderIM pathFinder)
	{
		if(getCollide(terrainMap, x, y, z) <= 0)
			return;
		
		if(getCanClimb())
		{
			if(isAdjacentSolidBlock(terrainMap, x, y, z))
				pathFinder.addNode(x, y, z, PathAction.NONE);
		}
		else if(terrainMap.getBlockId(x, y, z) == Block.ladder.blockID)
		{
			pathFinder.addNode(x, y, z, PathAction.NONE);
		}
	}

	protected boolean isAdjacentSolidBlock(IBlockAccess terrainMap, int x, int y, int z)
	{
		if(collideSize.getXCoord() == 1 && collideSize.getZCoord() == 1)
		{
	    	for(int i = 0; i < 4; i++)
	    	{
				int id = terrainMap.getBlockId(x + CoordsInt.offsetAdjX[i], y, z + CoordsInt.offsetAdjZ[i]);
				if(id > 0 && Block.blocksList[id].blockMaterial.isSolid())
					return true;
	    	}
		}
		else if(collideSize.getXCoord() == 2 && collideSize.getZCoord() == 2)
		{
			for(int i = 0; i < 8; i++)
	    	{
				int id = terrainMap.getBlockId(x + CoordsInt.offsetAdj2X[i], y, z + CoordsInt.offsetAdj2Z[i]);
				if(id > 0 && Block.blocksList[id].blockMaterial.isSolid())
					return true;
	    	}
		}
		return false;
	}

	/**
	 * Returns the next height(as an offset) below the given position that the entity
	 * can safely stand at, including the position given. Returns +1 if the maximum
	 * offset or the bottom of the world was reached.
	 */
	protected int getNextLowestSafeYOffset(IBlockAccess terrainMap, int x, int y, int z, int maxOffsetMagnitude)
	{
		for(int i = 0; i + y > 0 && i < maxOffsetMagnitude; i--)
		{
			if(canStandAtAndIsValid(terrainMap, x, y + i, z) || (canSwimHorizontal() && getCollide(terrainMap, x, y + i, z) == -1))
				return i;
		}
		
		return 1;
	}

	/**
	 * Returns true if at this position the entity would be physically supported from
	 * underneath. Does not check for collisions.
	 */
	protected boolean canStandAt(IBlockAccess terrainMap, int x, int y, int z)
	{
		boolean isSolidBlock = false;
		for(int xOffset = x; xOffset < x + collideSize.getXCoord(); xOffset++)
	    {
	        for(int zOffset = z; zOffset < z + collideSize.getZCoord(); zOffset++)
	        {
	        	int id = terrainMap.getBlockId(xOffset, y - 1, zOffset);
	        	if(id == 0)
	        		continue;
	        	
	        	if(!Block.blocksList[id].getBlocksMovement(terrainMap, xOffset, y - 1, zOffset))
	        		isSolidBlock = true;
	        	
	        	else if(avoidsBlock(id))
	        		return false;
	        }
	    }
		return isSolidBlock;
	}

	/**
	 * Returns true if a move to this position would be valid for this entity,
	 * clear or not.
	 */
	protected boolean canStandAtAndIsValid(IBlockAccess terrainMap, int x, int y, int z)
	{
		if(getCollide(terrainMap, x, y, z) > 0 && canStandAt(terrainMap, x, y, z))
			return true;
		
		return false;
	}

	/**
	 * Returns true if the entity can stand on this block, false if not or if
	 * it considers the block a hazard to be avoided.
	 */
	protected boolean canStandOnBlock(IBlockAccess terrainMap, int x, int y, int z)
	{
		int id = terrainMap.getBlockId(x, y, z);
		if(id != 0 && !Block.blocksList[id].getBlocksMovement(terrainMap, x, y, z) && !avoidsBlock(id))
			return true;
		
		return false;
	}

	protected boolean blockHasLadder(IBlockAccess terrainMap, int x, int y, int z)
	{
		for(int i = 0; i < 4; i++)
		{
			if(terrainMap.getBlockId(x + CoordsInt.offsetAdjX[i], y, z + CoordsInt.offsetAdjZ[i]) == Block.ladder.blockID)
				return true;
		}
		return false;
	}

	/**
	 * Returns code based on which blocks the entity collides with at specified point.
	 * 0 = impassable; >0 = passable with mining; <0 = hazard, avoidance or liquid
	 */
	protected int getCollide(IBlockAccess terrainMap, int x, int y, int z)
	{
		boolean destructibleFlag = false;
		boolean liquidFlag = false;
		for(int xOffset = x; xOffset < x + collideSize.getXCoord(); xOffset++)
	    {
	        for(int yOffset = y; yOffset < y + collideSize.getYCoord(); yOffset++)
	        {
	            for(int zOffset = z; zOffset < z + collideSize.getZCoord(); zOffset++)
	            {
	                int id = terrainMap.getBlockId(xOffset, yOffset, zOffset);
	                if(id <= 0)
	                    continue;
	                
	                if(id == Block.waterStill.blockID || id == Block.waterMoving.blockID
	                		|| id == Block.lavaStill.blockID || id == Block.lavaMoving.blockID)
	                {
	                	liquidFlag = true;
	                }
	                else if(!Block.blocksList[id].getBlocksMovement(terrainMap, xOffset, yOffset, zOffset))
	                {
	                	if(isBlockDestructible(terrainMap, x, y, z, id))
	                		destructibleFlag = true;
	                	else
	                		return 0;
	                }
	                else if(terrainMap.getBlockId(xOffset, yOffset - 1, zOffset) == Block.fence.blockID)
	                {
	                	if(isBlockDestructible(terrainMap, x, y, z, Block.fence.blockID))
	                		return 3;
	                	else
	                		return 0;
	                }
	                
	                if(avoidsBlock(id))
	                	return -2;
	            }
	        }
	    }
	
	    if(destructibleFlag)
	    	return 2;
	    else if(liquidFlag)
	    	return -1;
	    else
	    	return 1;
	}

	protected boolean getLightLevelBelow8()
	{
	    int i = MathHelper.floor_double(posX);
	    int j = MathHelper.floor_double(boundingBox.minY);
	    int k = MathHelper.floor_double(posZ);
	    if(worldObj.getSavedLightValue(EnumSkyBlock.Sky, i, j, k) > rand.nextInt(32))
	    {
	        return false;
	    }
	    int l = worldObj.getBlockLightValue(i, j, k);
	    if(worldObj.isThundering())
	    {
	        int i1 = worldObj.skylightSubtracted;
	        worldObj.skylightSubtracted = 10;
	        l = worldObj.getBlockLightValue(i, j, k);
	        worldObj.skylightSubtracted = i1;
	    }
	    return l <= rand.nextInt(8);
	}
	
	protected void setAIGoal(Goal goal)
	{
		currentGoal = goal;
	}
	
	protected void setPrevAIGoal(Goal goal)
	{
		prevGoal = goal;
	}
	
	protected void transitionAIGoal(Goal newGoal)
	{
		prevGoal = currentGoal;
		currentGoal = newGoal;
	}
	
	protected void setMoveState(MoveState moveState)
	{
		this.moveState = moveState;
		if(!worldObj.isRemote)
			dataWatcher.updateObject(META_MOVESTATE, moveState.ordinal());
	}

	protected void setDestructiveness(int x)
	{
		destructiveness = x;
	}

	protected void setGravity(float acceleration)
	{
		gravityAcel = acceleration;
	}
	
	protected void setGroundFriction(float frictionCoefficient)
	{
		groundFriction = frictionCoefficient;
	}

	protected void setCanClimb(boolean flag)
	{
		canClimb = flag;
	}

	protected void setJumpHeight(int height)
	{
		jumpHeight = height;
	}

	protected void setMoveSpeed(float moveSpeed)
	{
		this.moveSpeed = moveSpeed;
		getNavigatorNew().setSpeed(moveSpeed);
	}

	protected void resetMoveSpeed()
	{
		moveSpeed = moveSpeedBase;
		getNavigatorNew().setSpeed(moveSpeed);
	}

	protected void setTurnRate(float rate)
	{
		turnRate = rate;
	}

	protected void setName(String name)
    {
    	this.name = name;
    }
    
    protected void setGender(int gender)
    {
    	this.gender = gender;
    }
    
    protected void onDebugChange()
    {
    	
    }
    
    public static int getBlockType(int id)
    {
    	if(blockType.containsKey(id))
    		return blockType.get(id);
    	else
    		return 0;
    }
    
    public static float getBlockStrength(int x, int y, int z, int id, World world)
	{
		if(blockSpecials.containsKey(id))
		{
			BlockSpecial special = blockSpecials.get(id);
			if(special == BlockSpecial.CONSTRUCTION_1)
			{
				//Increment bonus for every matching adjacent block
				int bonus = 0;
				if(world.getBlockId(x, y - 1, z) == id) { bonus++; }
				if(world.getBlockId(x, y + 1, z) == id) { bonus++; }
				if(world.getBlockId(x + 1, y, z) == id) { bonus++; }
				if(world.getBlockId(x - 1, y, z) == id) { bonus++; }
				if(world.getBlockId(x, y, z + 1) == id) { bonus++; }
				if(world.getBlockId(x, y, z - 1) == id) { bonus++; }
				
				return blockStrength.get(id) * (1 + bonus * 0.1F);
			}
			else if(special == BlockSpecial.CONSTRUCTION_STONE)
			{
				//Increment bonus for every adjacent stone related block - stone, cobble, mossy cobble, stone brick
				int bonus = 0;
				int adjId = world.getBlockId(x, y - 1, z);
				if(adjId == 1 || adjId == 4 || adjId == 48 || adjId == 98) { bonus++; }
				adjId = world.getBlockId(x, y + 1, z);
				if(adjId == 1 || adjId == 4 || adjId == 48 || adjId == 98) { bonus++; }
				adjId = world.getBlockId(x - 1, y, z);
				if(adjId == 1 || adjId == 4 || adjId == 48 || adjId == 98) { bonus++; }
				adjId = world.getBlockId(x + 1, y, z);
				if(adjId == 1 || adjId == 4 || adjId == 48 || adjId == 98) { bonus++; }
				adjId = world.getBlockId(x, y, z - 1);
				if(adjId == 1 || adjId == 4 || adjId == 48 || adjId == 98) { bonus++; }
				adjId = world.getBlockId(x, y, z + 1);
				if(adjId == 1 || adjId == 4 || adjId == 48 || adjId == 98) { bonus++; }
				
				return blockStrength.get(id) * (1 + bonus * 0.1F);
			}
		}
		
		if(blockStrength.containsKey(id))
		{
			return blockStrength.get(id);
		}
		return DEFAULT_SOFT_STRENGTH;
	}

	public static void putBlockStrength(int id, float strength)
	{
		blockStrength.put(id, strength);
	}

	public static void putBlockCost(int id, float cost)
	{
		blockCosts.put(id, cost);
	}
    
    static
    {
    	//Default block costs for entity, for pathfinder
    	blockCosts.put(0, AIR_BASE_COST); //Air
    	blockCosts.put(Block.ladder.blockID, AIR_BASE_COST);
    	blockCosts.put(Block.stone.blockID, DEFAULT_HARD_COST);
    	blockCosts.put(Block.stoneBrick.blockID, DEFAULT_HARD_COST);
    	blockCosts.put(Block.cobblestone.blockID, DEFAULT_HARD_COST);
    	blockCosts.put(Block.cobblestoneMossy.blockID, DEFAULT_HARD_COST);
    	blockCosts.put(Block.brick.blockID, DEFAULT_HARD_COST);
    	blockCosts.put(Block.obsidian.blockID, DEFAULT_HARD_COST);
    	blockCosts.put(Block.blockIron.blockID, DEFAULT_HARD_COST);
    	blockCosts.put(Block.dirt.blockID, DEFAULT_SOFT_COST);
    	blockCosts.put(Block.sand.blockID, DEFAULT_SOFT_COST);
    	blockCosts.put(Block.gravel.blockID, DEFAULT_SOFT_COST);
    	blockCosts.put(Block.glass.blockID, DEFAULT_SOFT_COST);
    	blockCosts.put(Block.leaves.blockID, DEFAULT_SOFT_COST);
    	blockCosts.put(Block.doorIron.blockID, DEFAULT_HARD_COST * 0.7F);
    	blockCosts.put(Block.doorWood.blockID, DEFAULT_SOFT_COST * 0.7F);
    	blockCosts.put(Block.trapdoor.blockID, DEFAULT_SOFT_COST * 0.7F);
    	blockCosts.put(Block.sandStone.blockID, DEFAULT_HARD_COST);
    	blockCosts.put(Block.wood.blockID, DEFAULT_HARD_COST);
    	blockCosts.put(Block.planks.blockID, DEFAULT_HARD_COST);
    	blockCosts.put(Block.blockGold.blockID, DEFAULT_HARD_COST);
    	blockCosts.put(Block.blockDiamond.blockID, DEFAULT_HARD_COST);
    	blockCosts.put(Block.fence.blockID, DEFAULT_HARD_COST);
    	blockCosts.put(Block.netherrack.blockID, DEFAULT_HARD_COST);
    	blockCosts.put(Block.netherBrick.blockID, DEFAULT_HARD_COST);
    	blockCosts.put(Block.slowSand.blockID, DEFAULT_SOFT_COST);
    	blockCosts.put(Block.glowStone.blockID, DEFAULT_SOFT_COST);
    	blockCosts.put(Block.tallGrass.blockID, AIR_BASE_COST);
    	
    	
    	//Default block hardness for entity block destruction
    	blockStrength.put(0, 0.01F); //Air
    	blockStrength.put(Block.stone.blockID, DEFAULT_HARD_STRENGTH);
    	blockStrength.put(Block.stoneBrick.blockID, DEFAULT_HARD_STRENGTH);
    	blockStrength.put(Block.cobblestone.blockID, DEFAULT_HARD_STRENGTH);
    	blockStrength.put(Block.cobblestoneMossy.blockID, DEFAULT_HARD_STRENGTH);
    	blockStrength.put(Block.brick.blockID, DEFAULT_HARD_STRENGTH);
    	blockStrength.put(Block.obsidian.blockID, DEFAULT_HARD_STRENGTH * 1.4F);
    	blockStrength.put(Block.blockIron.blockID, DEFAULT_HARD_STRENGTH * 1.4F);
    	blockStrength.put(Block.dirt.blockID, DEFAULT_SOFT_STRENGTH * 1.25F);
    	blockStrength.put(Block.grass.blockID, DEFAULT_SOFT_STRENGTH * 1.25F);
    	blockStrength.put(Block.sand.blockID, DEFAULT_SOFT_STRENGTH);
    	blockStrength.put(Block.gravel.blockID, DEFAULT_SOFT_STRENGTH);
    	blockStrength.put(Block.glass.blockID, DEFAULT_SOFT_STRENGTH);
    	blockStrength.put(Block.leaves.blockID, DEFAULT_SOFT_STRENGTH * 0.5F);
    	blockStrength.put(Block.vine.blockID, DEFAULT_SOFT_STRENGTH * 0.5F);
    	blockStrength.put(Block.doorIron.blockID, DEFAULT_HARD_STRENGTH * 2.8F);
    	blockStrength.put(Block.doorWood.blockID, DEFAULT_HARD_STRENGTH * 1.8F);
    	blockStrength.put(Block.sandStone.blockID, DEFAULT_HARD_STRENGTH);
    	blockStrength.put(Block.wood.blockID, DEFAULT_HARD_STRENGTH);
    	blockStrength.put(Block.planks.blockID, DEFAULT_HARD_STRENGTH);
    	blockStrength.put(Block.blockGold.blockID, DEFAULT_HARD_STRENGTH);
    	blockStrength.put(Block.blockDiamond.blockID, DEFAULT_HARD_STRENGTH);
    	blockStrength.put(Block.fence.blockID, DEFAULT_HARD_STRENGTH);
    	blockStrength.put(Block.netherrack.blockID, DEFAULT_HARD_STRENGTH * 0.7F);
    	blockStrength.put(Block.netherBrick.blockID, DEFAULT_HARD_STRENGTH);
    	blockStrength.put(Block.slowSand.blockID, DEFAULT_SOFT_STRENGTH);
    	blockStrength.put(Block.glowStone.blockID, DEFAULT_SOFT_STRENGTH);
    	blockStrength.put(Block.tallGrass.blockID, 0.3F);
    	blockStrength.put(Block.dragonEgg.blockID, 15F);
    	
    	//Special block atributes
    	blockSpecials.put(Block.stone.blockID, BlockSpecial.CONSTRUCTION_STONE);
    	blockSpecials.put(Block.stoneBrick.blockID, BlockSpecial.CONSTRUCTION_STONE);
    	blockSpecials.put(Block.cobblestone.blockID, BlockSpecial.CONSTRUCTION_STONE);
    	blockSpecials.put(Block.cobblestoneMossy.blockID, BlockSpecial.CONSTRUCTION_STONE);
    	blockSpecials.put(Block.brick.blockID, BlockSpecial.CONSTRUCTION_1);
    	blockSpecials.put(Block.sandStone.blockID, BlockSpecial.CONSTRUCTION_1);
    	blockSpecials.put(112, BlockSpecial.CONSTRUCTION_1);
    	blockSpecials.put(Block.obsidian.blockID, BlockSpecial.DEFLECTION_1);
    	
    	blockType.put(0, 1);
    	blockType.put(Block.tallGrass.blockID, 1);
    	blockType.put(Block.deadBush.blockID, 1);
    	blockType.put(Block.plantRed.blockID, 1);
    	blockType.put(Block.plantYellow.blockID, 1);
    	blockType.put(Block.pressurePlateStone.blockID, 1);
    	blockType.put(Block.pressurePlateIron.blockID, 1);
    	blockType.put(Block.pressurePlatePlanks.blockID, 1);
    	blockType.put(Block.pressurePlateGold.blockID, 1);
    	blockType.put(Block.stoneButton.blockID, 1);
    	blockType.put(Block.woodenButton.blockID, 1);
    	blockType.put(Block.torchRedstoneIdle.blockID, 1);
    	blockType.put(Block.torchRedstoneActive.blockID, 1);
    	blockType.put(Block.torchWood.blockID, 1);
    	blockType.put(Block.lever.blockID, 1);
    	blockType.put(Block.reed.blockID, 1);
    	blockType.put(Block.crops.blockID, 1); // wheat
    	blockType.put(Block.carrot.blockID, 1);
    	blockType.put(Block.potato.blockID, 1);
    	blockType.put(Block.fire.blockID, 2);
    	blockType.put(Block.bedrock.blockID, 2);
    	blockType.put(Block.lavaStill.blockID, 2);
    	blockType.put(Block.lavaMoving.blockID, 2);
    }
}