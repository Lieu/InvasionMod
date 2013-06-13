
package mods.invmod.common.entity;

import mods.invmod.common.mod_Invasion;
import mods.invmod.common.nexus.EntityConstruct;
import mods.invmod.common.nexus.IMEntityType;
import mods.invmod.common.nexus.INexusAccess;
import net.minecraft.block.Block;
import net.minecraft.block.StepSound;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;


public class EntityIMSpider extends EntityIMMob implements ISpawnsOffspring
{
	private EntityMoveHelper moveHelper;
	private byte metaChanged;
	private int tier;
	private int flavour;
	private int pounceTime;
	private int pounceAbility;
	private int airborneTime;

	private static final int META_CHANGED = 29;
	private static final int META_TIER = 30;
	private static final int META_TEXTURE = 31;
	private static final int META_FLAVOUR = 28;
	
	public EntityIMSpider(World world)
	{
		this(world, null);
	}

	public EntityIMSpider(World world, INexusAccess nexus)
	{
		super(world, nexus);
		texture = "/mob/spider.png";
		setSize(1.4F, 0.9F);
		setCanClimb(true);
		airborneTime = 0;
		if(world.isRemote)
			metaChanged = 1;
		else
			metaChanged = 0;
		tier = 1;
		flavour = 0;
		setAttributes(tier, flavour);
		setAI();
		moveHelper = new IMMoveHelperSpider(this);

		DataWatcher dataWatcher = getDataWatcher();
		dataWatcher.addObject(META_CHANGED, metaChanged);
		dataWatcher.addObject(META_TIER, tier);
		dataWatcher.addObject(META_TEXTURE, 0);
		dataWatcher.addObject(META_FLAVOUR, flavour);        
	}

	protected void setAI()
	{
		tasks = new EntityAITasks(worldObj.theProfiler);
		tasks.addTask(0, new EntityAIKillEntity<EntityPlayer>(this, EntityPlayer.class, 40));
		tasks.addTask(1, new EntityAIAttackNexus(this));
		tasks.addTask(2, new EntityAIWaitForEngy(this, 5.0F, false));
		tasks.addTask(3, new EntityAIKillEntity<EntityLiving>(this, EntityLiving.class, 40));
		tasks.addTask(4, new EntityAIGoToNexus(this));
		tasks.addTask(6, new EntityAIWanderIM(this, moveSpeed));
		tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8F));

		tasks.addTask(8, new EntityAILookIdle(this));

		targetTasks = new EntityAITasks(worldObj.theProfiler);
		targetTasks.addTask(0, new EntityAITargetRetaliate(this, EntityLiving.class, 12F));
		targetTasks.addTask(2, new EntityAISimpleTarget(this, EntityPlayer.class, 14F, true));
		targetTasks.addTask(3, new EntityAITargetOnNoNexusPath(this, EntityIMPigEngy.class, 3.5F));
		targetTasks.addTask(4, new EntityAIHurtByTarget(this, false));

		// Set to follow creepers
		tasks.addTask(0, new EntityAIRallyBehindEntity<EntityIMCreeper>(this, EntityIMCreeper.class, 4));
		tasks.addTask(8, new EntityAIWatchClosest(this, EntityIMCreeper.class, 12F));
		//targetTasks.addTask(0, new EntityAISimpleTarget(this, EntityPlayer.class, 6F, true));
		//targetTasks.addTask(1, new EntityAILeaderTarget(this, EntityIMCreeper.class, 10F, true));


		if(tier == 2)
		{
			if(flavour == 0)
			{
				tasks.addTask(2, new EntityAIPounce(this, 0.2F, 1.55F, 180));
			}
			else if(flavour == 1)
			{
				tasks.addTask(0, new EntityAILayEgg(this, 1));
			}
		}
	}

	@Override
	public void onUpdate()
	{
		if(worldObj.isRemote)
			worldObj.isRemote = worldObj.isRemote;
		
		super.onUpdate();
		if(worldObj.isRemote && metaChanged != getDataWatcher().getWatchableObjectByte(META_CHANGED))
		{
			DataWatcher data = getDataWatcher();
			metaChanged = data.getWatchableObjectByte(META_CHANGED);
			setTexture(data.getWatchableObjectInt(META_TEXTURE));

			if(tier != data.getWatchableObjectInt(META_TIER))
				setTier(data.getWatchableObjectInt(META_TIER));
			if(flavour != data.getWatchableObjectInt(META_FLAVOUR))
				setFlavour(data.getWatchableObjectInt(META_FLAVOUR));
		}
	}

	/**
	 * Moves entity towards XZ heading according to current accelerations.
	 * This is an overridden and deobfuscated/cleaned up method with minor changes.
	 */
	@Override
	public void moveEntityWithHeading(float x, float z)
	{
		if(isInWater())
		{
			double y = posY;
			moveFlying(x, z, 0.02F);
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
			moveFlying(x, z, 0.02F);
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
			if(airborneTime == 0)
			{
				if(onGround)
				{
					groundFriction = 0.546F; 
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
			}
			
			if(worldObj.isRemote)
				worldObj.isRemote = worldObj.isRemote;

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

				if(isSneaking() && motionY < 0)
					motionY = 0;
			}

			moveEntity(motionX, motionY, motionZ);    
			if((isCollidedHorizontally || isJumping) && isOnLadder())
				motionY = 0.2;

			float airResistance = 1.0F;
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

	@Override
	public EntityMoveHelper getMoveHelper()
	{
		return moveHelper;
	}

	@Override
	protected void jump()
	{
		motionY = 0.41D;
		isAirBorne = true;
	}

	/**
	 * Sets the spider's tier level, taking on the properties of it.
	 * If texture or flavour (respective individually) have not been
	 * specified already, they will take on values according 
	 * to the default distribution.
	 */
	public void setTier(int tier)
	{
		this.tier = tier;
		getDataWatcher().updateObject(META_TIER, tier);
		setAttributes(tier, flavour);
		setAI();

		if(getDataWatcher().getWatchableObjectInt(META_TEXTURE) == 0)
		{
			if(tier == 1)
			{
				setTexture(0);
			}
			else if(tier == 2)
			{
				if(flavour == 0)
					setTexture(1);
				else
					setTexture(2);
			}
		}
	}

	public void setTexture(int textureId)
	{
		getDataWatcher().updateObject(META_TEXTURE, textureId);
		if(textureId == 0)
		{
			texture = "/mob/spider.png";
		}
		else if(textureId == 1)
		{
			texture = "/mods/invmod/textures/spiderT2.png";
		}
		else if(textureId == 2)
		{
			texture = "/mods/invmod/textures/spiderT2b.png";
		}
	}

	public void setFlavour(int flavour)
	{
		this.flavour = flavour;
		getDataWatcher().updateObject(META_FLAVOUR, flavour);
		setAttributes(tier, flavour);
	}

	@Override
	public String toString()
	{
		return "EntityIMSpider#" + tier + "-" + getDataWatcher().getWatchableObjectInt(META_TEXTURE) + "-" + flavour;
	}

	@Override
	public double getMountedYOffset()
	{
		return height * 0.75D - 0.5D;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
	{
		nbttagcompound.setInteger("tier", tier);
		nbttagcompound.setInteger("flavour", flavour);
		nbttagcompound.setInteger("textureId", dataWatcher.getWatchableObjectInt(META_TEXTURE));
		super.writeEntityToNBT(nbttagcompound);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
	{
		setTexture(nbttagcompound.getInteger("textureId"));
		flavour = nbttagcompound.getInteger("flavour");
		tier = nbttagcompound.getInteger("tier");
		if(tier == 0)
			tier = 1;
		setTier(tier);
		super.readEntityFromNBT(nbttagcompound);
	}

	@Override
	public boolean avoidsBlock(int id)
	{
		if(id == 51 || id == 7)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public float spiderScaleAmount()
	{
		if(tier == 1 && flavour == 1)
			return 0.35F;
		else if(tier == 2 && flavour == 1)
			return 1.3F;
		else
			return 1.0F;
	}

	@Override
	public Entity[] getOffspring(Entity partner)
	{
		if(tier == 2 && flavour == 1)
		{
			EntityConstruct template = new EntityConstruct(IMEntityType.SPIDER, 1, 0, 1, 1.0F, 0, 0);
			Entity[] offSpring = new Entity[6];
			for(int i = 0; i < offSpring.length; i++)
			{
				offSpring[i] = mod_Invasion.getMobBuilder().createMobFromConstruct(template, worldObj, getNexus());
			}
			return offSpring;
		}
		else
		{
			return null;
		}
	}

	public int getAirborneTime()
	{
		return airborneTime;
	}

	@Override
	public boolean canBePushed()
	{
		return !isOnLadder();
	}
	
	@Override
	public EnumCreatureAttribute getCreatureAttribute()
    {
        return EnumCreatureAttribute.ARTHROPOD;
    }
	
	@Override
	public boolean checkForAdjacentClimbBlock()
	{
		return isCollidedHorizontally;
	}

	// --------- Sparrow API --------- //

	/**What should this entity be referred to as? (Dog, Cat, Human, Enderman, etc.)*/
	@Override
	public String getSpecies()
	{
		return "Spider";
	}
	
	@Override
	public int getTier()
 	{
		return 2;
	}

	// ------------------------------- //

	protected void setAirborneTime(int time)
	{
		airborneTime = time;
	}

	@Override
	protected boolean canTriggerWalking()
	{
		return false;
	}

	@Override
	protected String getLivingSound()
	{
		return "mob.spider.say";
	}

	@Override
	protected String getHurtSound()
	{
		return "mob.spider.say";
	}

	@Override
	protected String getDeathSound()
	{
		return "mob.spider.death";
	}

	@Override
	protected void fall(float f)
	{
		int i = (int)Math.ceil(f - 3F);
		if(i > 0)
		{
			int j = worldObj.getBlockId(MathHelper.floor_double(posX), MathHelper.floor_double(posY - 0.2D - yOffset), MathHelper.floor_double(posZ));
			if(j > 0)
			{
				StepSound stepsound = Block.blocksList[j].stepSound;
				worldObj.playSoundAtEntity(this, stepsound.getStepSound(), stepsound.getVolume() * 0.5F, stepsound.getPitch() * 0.75F);
			}
		}
	}

	@Override
	protected int getDropItemId()
	{
		return Item.silk.itemID;
	}

	@Override
	protected void dropFewItems(boolean flag, int bonus)
	{
		if(tier == 1 && flavour == 1)
			return;
		
		super.dropFewItems(flag, bonus);
		if(rand.nextFloat() < 0.35F)
		{
			dropItem(Item.silk.itemID, 1);
		}
	}

	private void setAttributes(int tier, int flavour)
	{
		setGravity(0.08F);
		setSize(1.4F, 0.9F);
		setGender(rand.nextInt(2) + 1); // 50/50 male/female
		if(tier == 1)
		{
			if(flavour == 0)
			{
				setName("Spider");
				moveSpeed = 0.8F;
				attackStrength = 3;
				health = 18;
				maxHealth = 18;
				pounceTime = 0;
				pounceAbility = 0;
				maxDestructiveness = 0;
				setDestructiveness(0);
				setAggroRange(10);
			}
			else if(flavour == 1)
			{
				setName("Baby Spider");
				setSize(0.42F, 0.3F);
				moveSpeed = 0.45F;
				attackStrength = 1;
				health = 3;
				maxHealth = 3;
				pounceTime = 0;
				pounceAbility = 0;
				maxDestructiveness = 0;
				setDestructiveness(0);
				setAggroRange(10);
			}
		}
		else if(tier == 2)
		{
			if(flavour == 0)
			{
				setName("Jumping Spider");
				moveSpeed = 0.9F;
				attackStrength = 5;
				health = 18;
				maxHealth = 18;
				pounceTime = 0;
				pounceAbility = 1;
				maxDestructiveness = 0;
				setDestructiveness(0);
				setAggroRange(18);
				setGravity(0.05F);
			}
			else if(flavour == 1)
			{
				setName("Mother Spider");
				setGender(2);
				moveSpeed = 0.8F;
				attackStrength = 4;
				health = 23;
				maxHealth = 23;
				pounceTime = 0;
				pounceAbility = 0;
				maxDestructiveness = 0;
				setDestructiveness(0);
				setAggroRange(18);
			}
		}
	}
}
