package mods.invmod.common.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.invmod.common.nexus.INexusAccess;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class EntityIMGiantBird extends EntityIMBird
{
	private static final float PICKUP_OFFSET_X = 0;
	private static final float PICKUP_OFFSET_Y = -0.2F;
	private static final float PICKUP_OFFSET_Z = -0.92F;
	private static final float MODEL_ROTATION_OFFSET_Y = 1.9F;
	private static final byte TRIGGER_SQUAWK = 10;
	private static final byte TRIGGER_SCREECH = 10;
	private static final byte TRIGGER_DEATHSOUND = 10;
	
	public EntityIMGiantBird(World world)
	{
		this(world, null);
	}

	public EntityIMGiantBird(World world, INexusAccess nexus)
	{
		super(world, nexus);
		setName("Bird");
		setGender(2);
		attackStrength = 5;
		health = 58;
		maxHealth = 58;
		texture = "/mods/invmod/textures/burd.png";
		setSize(1.9F, 2.8F);
		setGravity(0.03F);
		setThrust(0.028F);
		setMaxPoweredFlightSpeed(0.9F);
		setLiftFactor(0.35F);
		setThrustComponentRatioMin(0);
		setThrustComponentRatioMax(0.5F);
		setMaxTurnForce(getGravity() * 8);
		setAIMoveSpeed(0.4F);
		setAI();
		setDebugMode(1);
	}
	
	public void onUpdate()
	{
		super.onUpdate();
		if(getDebugMode() == 1 && !worldObj.isRemote)
		{
			setRenderLabel(getAIGoal() + "\n" + getNavString());
		}
	}
	
	public boolean canDespawn()
	{
		return false;
	}
	
	public void updateRiderPosition()
    {
        if (riddenByEntity != null)
        {
        	// Rotate offsets about entity
        	double x = PICKUP_OFFSET_X;
        	double y = getMountedYOffset() - MODEL_ROTATION_OFFSET_Y;
        	double z = PICKUP_OFFSET_Z;
        	double tmp;
        	
        	// Pitch rotation
			double dAngle = rotationPitch / 180F * Math.PI;
			double sinF = Math.sin(dAngle);
			double cosF = Math.cos(dAngle);
			tmp = z * cosF - y * sinF;
			y = y * cosF + z * sinF;
			z = tmp;
			
			// Yaw rotation second - order is important
			dAngle = rotationYaw / 180F * Math.PI; // +90F ?
			sinF = Math.sin(dAngle);
			cosF = Math.cos(dAngle);
			tmp = x * cosF - z * sinF;
			z = z * cosF + x * sinF;
			x = tmp;
			
			y += MODEL_ROTATION_OFFSET_Y + riddenByEntity.getYOffset();
			
			// 
        	
            riddenByEntity.lastTickPosX = lastTickPosX + x;
            riddenByEntity.lastTickPosY = lastTickPosY + y;
            riddenByEntity.lastTickPosZ = lastTickPosZ + z;
            riddenByEntity.setPosition(posX + x, posY + y, posZ + z);
            riddenByEntity.rotationYaw = getCarriedEntityYawOffset() + rotationYaw;
        }
    }
	
	public boolean shouldRiderSit()
    {
        return false;
    }
	
	/**
     * Returns the Y offset from the entity's position for any entity riding this one.
     */
    public double getMountedYOffset()
    {
        return -PICKUP_OFFSET_Y;
    }
    
    @Override
	protected void doScreech()
	{
    	if(!worldObj.isRemote)
    	{
    		worldObj.playSoundAtEntity(this, "invsound.v_screech", 6.0F, 1.0F + (rand.nextFloat() * 0.2F - 0.1F));
    		worldObj.setEntityState(this, TRIGGER_SCREECH);
    	}
    	else
    	{
			setBeakState(35);
    	}
	}
	
	@Override
	protected void doMeleeSound()
	{
		doSquawk();
	}
	
	@Override
	protected void doHurtSound()
	{
		doSquawk();
	}
	
	@Override
	protected void doDeathSound()
	{
		if(!worldObj.isRemote)
    	{
			worldObj.playSoundAtEntity(this, "invsound.v_death", 1.9F, 1.0F + (rand.nextFloat() * 0.2F - 0.1F));
    		worldObj.setEntityState(this, TRIGGER_DEATHSOUND);
    	}
    	else
    	{
			setBeakState(25);
    	}
	}

    protected void onDebugChange()
    {
    	if(getDebugMode() == 1)
    	{
    		setShouldRenderLabel(true);
    	}
    	else
    	{
    		setShouldRenderLabel(false);
    	}
    }
    
    @Override
	@SideOnly(Side.CLIENT)
	public void handleHealthUpdate(byte b)
	{
		super.handleHealthUpdate(b);
		if(b == TRIGGER_SQUAWK)
		{
			doSquawk();
		}
		else if(b == TRIGGER_SCREECH)
		{
			doScreech();
		}
		else if(b == TRIGGER_DEATHSOUND)
		{
			doDeathSound();
		}
	}
    
    private void doSquawk()
    {
    	if(!worldObj.isRemote)
    	{
    		worldObj.playSoundAtEntity(this, "invsound.v_squawk", 1.9F, 1.0F + (rand.nextFloat() * 0.2F - 0.1F));
    		worldObj.setEntityState(this, TRIGGER_SQUAWK);
    	}
    	else
    	{
			setBeakState(10);
    	}
    }
    
    private String getNavString()
    {
    	return getNavigatorNew().getStatus();
    }
	
	private void setAI()
	{
		tasks = new EntityAITasks(worldObj.theProfiler);
		//tasks.addTask(0, new EntityAIKillEntity<EntityPlayer>(this, EntityPlayer.class, 40));
		//tasks.addTask(1, new EntityAIAttackNexus(this));
		tasks.addTask(0, new EntityAISwoop(this));
		//tasks.addTask(1, new EntityAIFlyingMoveToEntity(this));
		tasks.addTask(3, new EntityAIBoP(this));
		tasks.addTask(4, new EntityAIFlyingStrike(this));
		tasks.addTask(4, new EntityAIFlyingTackle(this));
		tasks.addTask(4, new EntityAIPickUpEntity(this, PICKUP_OFFSET_X, PICKUP_OFFSET_Y, 0, 1.5F, 1.5F, 20, 45F, 45F));
		tasks.addTask(4, new EntityAIStabiliseFlying(this, 35));
		tasks.addTask(4, new EntityAICircleTarget(this, 500, 16, 45));
		tasks.addTask(4, new EntityAIBirdFight<EntityZombie>(this, EntityZombie.class, 25, 0.4F));
		tasks.addTask(4, new EntityAIWatchTarget(this));

		targetTasks = new EntityAITasks(worldObj.theProfiler);
		//targetTasks.addTask(0, new EntityAITargetRetaliate(this, EntityLiving.class, 12F));
		targetTasks.addTask(2, new EntityAISimpleTarget(this, EntityZombie.class, 58F, true));
		//targetTasks.addTask(5, new EntityAIHurtByTarget(this, false));

	}
}
