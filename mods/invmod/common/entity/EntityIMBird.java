package mods.invmod.common.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.invmod.client.render.AnimationRegistry;
import mods.invmod.client.render.animation.AnimationAction;
import mods.invmod.client.render.animation.AnimationState;
import mods.invmod.common.nexus.INexusAccess;
import mods.invmod.common.util.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class EntityIMBird extends EntityIMFlying
{
	private static final int META_ANIMATION_FLAGS = 26;
	private AnimationState animationRun;
	private AnimationState animationFlap;
	private AnimationState animationBeak;
	private WingController wingController;
	private LegController legController;
	private MouthController beakController;
	private int animationFlags;
	private float carriedEntityYawOffset;
	
	public EntityIMBird(World world)
	{
		this(world, null);
	}

	public EntityIMBird(World world, INexusAccess nexus)
	{
		super(world, nexus);
		animationRun = new AnimationState(AnimationRegistry.instance().getAnimation("bird_run"));
		animationFlap = new AnimationState(AnimationRegistry.instance().getAnimation("wing_flap_2_piece"));
		animationBeak = new AnimationState(AnimationRegistry.instance().getAnimation("bird_beak"));
		animationRun.setNewAction(AnimationAction.STAND);
		animationFlap.setNewAction(AnimationAction.WINGTUCK);
		animationBeak.setNewAction(AnimationAction.MOUTH_CLOSE);
		wingController = new WingController(this, animationFlap);
		legController = new LegController(this, animationRun);
		beakController = new MouthController(this, animationBeak);
		setName("Bird");
		setGender(2);
		moveSpeed = 2F;
		attackStrength = 1;
		health = 18;
		maxHealth = 18;
		texture = "/mods/invmod/textures/bird_tx1.png";
		animationFlags = 0;
		carriedEntityYawOffset = 0;
		setGravity(0.025F);
		setThrust(0.1F);
		setMaxPoweredFlightSpeed(0.5F);
		setLiftFactor(0.35F);
		setThrustComponentRatioMin(0);
		setThrustComponentRatioMax(0.5F);
		setMaxTurnForce(getGravity() * 8);
		setMoveState(MoveState.STANDING);
		setFlyState(FlyState.GROUNDED);
		
		dataWatcher.addObject(META_ANIMATION_FLAGS, (int)0);
	}
	
	protected void doScreech()
	{
	}
	
	protected void doMeleeSound()
	{
	}
	
	protected void doHurtSound()
	{
	}
	
	protected void doDeathSound()
	{
	}
	
	public AnimationState getWingAnimationState()
	{
		return animationFlap;
	}
	
	public float getLegSweepProgress()
	{
		// Legs tucked away for flight
		return 1.0F;
	}
	
	public AnimationState getLegAnimationState()
	{
		return animationRun;
	}
	
	public AnimationState getBeakAnimationState()
	{
		return animationBeak;
	}
	
	@Override
	public void onUpdate()
	{
		//setDead();
		super.onUpdate();
		if(worldObj.isRemote)
    	{
			updateFlapAnimation();
			updateLegAnimation();
			updateBeakAnimation();
			animationFlags = dataWatcher.getWatchableObjectInt(META_ANIMATION_FLAGS);
    	}
		else
		{
			dataWatcher.updateObject(META_ANIMATION_FLAGS, animationFlags);
		}
		//System.out.println(rotationYawHead);
	}
	
	@Override
	public String getSpecies()
	{
		return "Bird";
	}
	
	public boolean getClawsForward()
	{
		return (animationFlags & 1) > 0;
	}
	
	public boolean isAttackingWithWings()
	{
		return (animationFlags & (1 << 1)) > 0;
	}
	
	public boolean isBeakOpen()
	{
		return (animationFlags & (1 << 2)) > 0;
	}
	
	public float getCarriedEntityYawOffset()
	{
		return carriedEntityYawOffset;
	}
	
	// Need to override for animation triggers. Might be unsightly but it's correct
	@Override
	public boolean attackEntityFrom(DamageSource par1DamageSource, int par2)
    {
        if (ForgeHooks.onLivingAttack(this, par1DamageSource, par2))
            return false;
        
        if (isEntityInvulnerable())
            return false;
        
        else if (worldObj.isRemote)
        {
            return false;
        }
        else
        {
            entityAge = 0;
            if (health <= 0)
            {
                return false;
            }
            else if (par1DamageSource.isFireDamage() && isPotionActive(Potion.fireResistance))
            {
                return false;
            }
            else
            {
                if ((par1DamageSource == DamageSource.anvil || par1DamageSource == DamageSource.fallingBlock) && getCurrentItemOrArmor(4) != null)
                {
                    getCurrentItemOrArmor(4).damageItem(par2 * 4 + rand.nextInt(par2 * 2), this);
                    par2 = (int)((float)par2 * 0.75F);
                }

                limbYaw = 1.5F;
                boolean flag = true;
                if ((float)hurtResistantTime > (float)maxHurtResistantTime / 2.0F)
                {
                    if (par2 <= lastDamage)
                    {
                        return false;
                    }

                    damageEntity(par1DamageSource, par2 - lastDamage);
                    lastDamage = par2;
                    flag = false;
                }
                else
                {
                    lastDamage = par2;
                    prevHealth = health;
                    hurtResistantTime = maxHurtResistantTime;
                    damageEntity(par1DamageSource, par2);
                    hurtTime = maxHurtTime = 10;
                }

                attackedAtYaw = 0.0F;
                Entity entity = par1DamageSource.getEntity();
                if (entity != null)
                {
                    if (entity instanceof EntityLiving)
                    {
                        setRevengeTarget((EntityLiving)entity);
                    }

                    if (entity instanceof EntityPlayer)
                    {
                        recentlyHit = 100;
                        attackingPlayer = (EntityPlayer)entity;
                    }
                    else if (entity instanceof EntityWolf)
                    {
                        EntityWolf entitywolf = (EntityWolf)entity;

                        if (entitywolf.isTamed())
                        {
                            recentlyHit = 100;
                            attackingPlayer = null;
                        }
                    }
                }

                if (flag)
                {
                    worldObj.setEntityState(this, (byte)2);
                    if (par1DamageSource != DamageSource.drown)
                    {
                        setBeenAttacked();
                    }

                    if (entity != null)
                    {
                        double d0 = entity.posX - posX;
                        double d1;
                        for (d1 = entity.posZ - posZ; d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D)
                        {
                            d0 = (Math.random() - Math.random()) * 0.01D;
                        }

                        attackedAtYaw = (float)(Math.atan2(d1, d0) * 180.0D / Math.PI) - rotationYaw;
                        knockBack(entity, par2, d0, d1);
                    }
                    else
                    {
                        attackedAtYaw = (float)((int)(Math.random() * 2.0D) * 180);
                    }
                }

                if (health <= 0)
                {
                    if (flag)
                    {
                        doDeathSound();
                    }
                    onDeath(par1DamageSource);
                }
                else if (flag)
                {
                    doHurtSound();
                }
                return true;
            }
        }
    }
	
	/**
	 * Sets the entity's beak to open or close. The beak will open for the
	 * time given, from present. If that time is 0 or less it indicates the
	 * beak is set to be closed.
	 */
	protected void setBeakState(int timeOpen)
	{
		beakController.setMouthState(timeOpen);
	}
	
	protected void onPickedUpEntity(Entity entity)
	{
		carriedEntityYawOffset = entity.rotationYaw - entity.rotationYaw;
	}
	
	protected void setClawsForward(boolean flag)
	{
		if((flag ? 1 : 0) != (animationFlags & 1))
			animationFlags ^= 1;
	}
	
	protected void setAttackingWithWings(boolean flag)
	{
		if((flag ? 1 : 0) != (animationFlags & (1 << 1)))
			animationFlags ^= (1 << 1);
	}
	
	protected void setBeakOpen(boolean flag)
	{
		if((flag ? 1 : 0) != (animationFlags & (1 << 2)))
			animationFlags ^= (1 << 2);
	}
	
	@Override
	protected void updateAITick()
	{
		//if(rallyCooldown > 0)
		//	rallyCooldown--;
	}
	
	protected void updateFlapAnimation()
	{
		wingController.update();
	}
	
	protected void updateLegAnimation()
	{
		legController.update();
	}
	
	protected void updateBeakAnimation()
	{
		beakController.update();
	}
}
