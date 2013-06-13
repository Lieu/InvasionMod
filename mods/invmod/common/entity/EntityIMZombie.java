
package mods.invmod.common.entity;


import java.util.List;

import mods.invmod.common.IBlockAccessExtended;
import mods.invmod.common.INotifyTask;
import mods.invmod.common.nexus.INexusAccess;
import mods.invmod.common.util.IPosition;
import net.minecraft.block.Block;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class EntityIMZombie extends EntityIMMob implements ICanDig
{
	private static final int META_CHANGED = 29;
	private static final int META_TIER = 30;
	private static final int META_TEXTURE = 31;
	private static final int META_FLAVOUR = 28;
	private static final int META_SWINGING = 27;

	private TerrainModifier terrainModifier;
	private TerrainDigger terrainDigger;
	private byte metaChanged;
	private int tier;
	private int flavour;
	private ItemStack defaultHeldItem;
	private Item itemDrop;
	private float dropChance;
	private int swingTimer;

	public EntityIMZombie(World world)
	{
		this(world, null);
	}

	public EntityIMZombie(World world, INexusAccess nexus)
	{
		super(world, nexus);
		terrainModifier = new TerrainModifier(this, 2.0F);
		terrainDigger = new TerrainDigger(this, terrainModifier, 1.0F);
		texture = "/mob/zombie.png";
		dropChance = 0;
		if(world.isRemote)
			metaChanged = 1;
		else
			metaChanged = 0;
		tier = 1;
		flavour = 0;

		DataWatcher dataWatcher = getDataWatcher();
		dataWatcher.addObject(META_CHANGED, metaChanged);
		dataWatcher.addObject(META_TIER, tier);
		dataWatcher.addObject(META_TEXTURE, 0);
		dataWatcher.addObject(META_FLAVOUR, flavour);
		dataWatcher.addObject(META_SWINGING, (byte)0);

		setAttributes(tier, flavour);
		setAI();
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();
		if(metaChanged != getDataWatcher().getWatchableObjectByte(META_CHANGED))
		{
			DataWatcher data = getDataWatcher();
			metaChanged = data.getWatchableObjectByte(META_CHANGED);
			setTexture(data.getWatchableObjectInt(META_TEXTURE));

			if(tier != data.getWatchableObjectInt(META_TIER))
				setTier(data.getWatchableObjectInt(META_TIER));
			if(flavour != data.getWatchableObjectInt(META_FLAVOUR))
				setFlavour(data.getWatchableObjectInt(META_FLAVOUR));
		}

		if(!worldObj.isRemote && flammability >= 20 && isBurning())
			doFireball();
	}

	@Override
	public void onLivingUpdate()
	{
		super.onLivingUpdate();
		updateAnimation();
		updateSound();
	}

	@Override
	public void onPathSet()
	{
		terrainModifier.cancelTask();
	}

	protected void setAI()
	{
		tasks = new EntityAITasks(worldObj.theProfiler);
		tasks.addTask(0, new EntityAIKillEntity<EntityPlayer>(this, EntityPlayer.class, 40));
		tasks.addTask(1, new EntityAIAttackNexus(this));
		tasks.addTask(2, new EntityAIWaitForEngy(this, 4.0F, true));
		tasks.addTask(3, new EntityAIKillEntity<EntityLiving>(this, EntityLiving.class, 40));
		tasks.addTask(4, new EntityAIGoToNexus(this));
		tasks.addTask(6, new EntityAIWanderIM(this, moveSpeed));
		tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8F));
		tasks.addTask(8, new EntityAIWatchClosest(this, EntityIMCreeper.class, 12F));
		tasks.addTask(8, new EntityAILookIdle(this));

		targetTasks = new EntityAITasks(worldObj.theProfiler);
		targetTasks.addTask(0, new EntityAITargetRetaliate(this, EntityLiving.class, 12F));
		targetTasks.addTask(2, new EntityAISimpleTarget(this, EntityPlayer.class, 12F, true));
		targetTasks.addTask(5, new EntityAIHurtByTarget(this, false));

		if(tier == 3)
		{
			tasks.addTask(3, new EntityAIStoop(this));
			tasks.addTask(2, new EntityAISprint(this));
		}
		else
		{
			tasks.addTask(0, new EntityAIRallyBehindEntity<EntityIMCreeper>(this, EntityIMCreeper.class, 4));
			targetTasks.addTask(1, new EntityAISimpleTarget(this, EntityPlayer.class, 6F, true));
			targetTasks.addTask(3, new EntityAILeaderTarget(this, EntityIMCreeper.class, 10F, true));
			targetTasks.addTask(4, new EntityAITargetOnNoNexusPath(this, EntityIMPigEngy.class, 3.5F));
		}
	}

	/**
	 * Sets the zombie's tier level, taking on the properties of it.
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

		// Set default textures as a fail-safe
		if(getDataWatcher().getWatchableObjectInt(META_TEXTURE) == 0)
		{
			if(tier == 1)
			{
				int r = rand.nextInt(2);
				if(r == 0)
					setTexture(0);
				else if(r == 1)
					setTexture(1);
			}
			else if(tier == 2)
			{
				if(flavour == 2)
				{
					setTexture(5);
				}
				else if(flavour == 3)
				{
					setTexture(3);
				}
				else
				{
					int r = rand.nextInt(2);
					if(r == 0)
						setTexture(2);
					else if(r == 1)
						setTexture(4);
				}
			}
			else if(tier == 3)
			{
				setTexture(6);
			}
		}
	}

	public void setTexture(int textureId)
	{
		getDataWatcher().updateObject(META_TEXTURE, textureId);
		if(textureId == 0)
		{
			texture = "/mods/invmod/textures/zombie_old.png";
		}
		else if(textureId == 1)
		{
			texture = "/mods/invmod/textures/zombieT1a.png";
		}
		else if(textureId == 2)
		{
			texture = "/mods/invmod/textures/zombieT2.png";
		}
		else if(textureId == 3)
		{
			texture = "/mods/invmod/textures/pigzombie64x32.png";
		}
		else if(textureId == 4)
		{
			texture = "/mods/invmod/textures/zombieT2a.png";
		}
		else if(textureId == 5)
		{
			texture = "/mods/invmod/textures/zombietar.png";
		}
		else if(textureId == 6)
		{
			texture = "/mods/invmod/textures/zombieT3.png";
		}
	}

	public void setFlavour(int flavour)
	{
		getDataWatcher().updateObject(META_FLAVOUR, flavour);
		this.flavour = flavour;
		setAttributes(tier, flavour);
	}

	@Override
	public String toString()
	{
		return "EntityIMZombie#" + tier + "-" + getDataWatcher().getWatchableObjectInt(META_TEXTURE) + "-" + flavour;
	}

	@Override
	public IBlockAccess getTerrain()
	{
		return worldObj;
	}

	@Override
	public ItemStack getHeldItem()
	{
		return defaultHeldItem;
	}

	@Override
	public boolean avoidsBlock(int id)
	{
		if(isImmuneToFire && (id == 51 || id == 10 || id == 11))
			return false;
		else
			return super.avoidsBlock(id);			
	}

	@Override
	public float getBlockRemovalCost(int x, int y, int z)
	{
		return getBlockStrength(x, y, z) * 20F;
	}

	@Override
	public boolean canClearBlock(int x, int y, int z)
	{
		int id = worldObj.getBlockId(x, y, z);
		return id == 0 || isBlockDestructible(worldObj, x, y, z, id);
	}

	@Override
	protected boolean onPathBlocked(Path path, INotifyTask notifee)
	{
		if(!path.isFinished() && (isNexusBound() || getAttackTarget() != null))
		{
			// Deny block destruction on last section of incomplete paths to prevent shooting in foot
			if(path.getFinalPathPoint().distanceTo(path.getIntendedTarget()) > 2.2 && path.getCurrentPathIndex() + 2 >= path.getCurrentPathLength() / 2)
				return false;

			PathNode node = path.getPathPointFromIndex(path.getCurrentPathIndex());
			if(terrainDigger.askClearPosition(node.xCoord, node.yCoord, node.zCoord, notifee, 1.0F))
				return true;
		}
		return false;
	}

	public boolean isBigRenderTempHack()
	{
		return tier == 3;
	}

	@Override
	public boolean attackEntityAsMob(Entity entity)
	{
		return tier == 3 && isSprinting() ? chargeAttack(entity) : super.attackEntityAsMob(entity);
	}

	@Override
	public boolean canBePushed()
	{
		return tier == 3 ? false : worldObj.getBlockId(getXCoord(), getYCoord(), getZCoord()) != Block.ladder.blockID;
	}

	@Override
	public void knockBack(Entity par1Entity, int par2, double par3, double par5)
	{
		if(tier == 3)
			return;

		isAirBorne = true;
		float f = MathHelper.sqrt_double(par3 * par3 + par5 * par5);
		float f1 = 0.4F;
		motionX /= 2D;
		motionY /= 2D;
		motionZ /= 2D;
		motionX -= (par3 / f) * f1;
		motionY += f1;
		motionZ -= (par5 / f) * f1;

		if (motionY > 0.40000000596046448D)
		{
			motionY = 0.40000000596046448D;
		}
	}

	/**
	 * Returns the pathfinding cost of moving from one node to another.
	 */
	@Override
	public float getBlockPathCost(PathNode prevNode, PathNode node, IBlockAccess terrainMap)
	{
		// Change swimming costs for tar zombies (they can't drown)
		if(tier == 2 && flavour == 2 && node.action == PathAction.SWIM)
		{
			float multiplier = 1.0F;
			if(terrainMap instanceof IBlockAccessExtended)
			{
				int mobDensity = ((IBlockAccessExtended)terrainMap).getLayeredData(node.xCoord, node.yCoord, node.zCoord) & 7;
				multiplier += mobDensity * 3;
			}

			if(node.yCoord > prevNode.yCoord && getCollide(terrainMap, node.xCoord, node.yCoord, node.zCoord) == 2)
			{
				multiplier += 2;
			}

			return prevNode.distanceTo(node) * 1.2F * multiplier;
		}
		else
		{
			return super.getBlockPathCost(prevNode, node, terrainMap);
		}
	}

	@Override
	public boolean canBreatheUnderwater()
	{
		return tier == 2 && flavour == 2;
	}

	@Override
	public boolean isBlockDestructible(IBlockAccess terrainMap, int x, int y, int z, int id)
	{
		if(getDestructiveness() == 0)
			return false;

		// Stop block digging when node is in a 25 degree cone offset beneath the target (gradient of 2.144... or greater)
		IPosition pos = getCurrentTargetPos();
		int dY = (pos.getYCoord() - y);
		boolean isTooSteep = false;
		if(dY > 0)
		{
			dY += 8;
			int dX = (pos.getXCoord() - x);
			int dZ = (pos.getZCoord() - z);
			double dXZ = Math.sqrt(dX*dX + dZ*dZ) + 0.00001;
			isTooSteep = (dY / dXZ > 2.144);
		}

		return !isTooSteep && isBlockTypeDestructible(id);
	}

	@Override
	public boolean isBlockTypeDestructible(int id)
	{
		if(id == 0 || id == Block.bedrock.blockID || id == Block.ladder.blockID/* || id == mod_Invasion.blockNexus.blockID*/)
		{
			return false;
		}
		else if(id == Block.doorIron.blockID || id == Block.doorWood.blockID || id == Block.trapdoor.blockID)
		{
			return true;
		}
		else if(Block.blocksList[id].blockMaterial.isSolid())
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public void onFollowingEntity(Entity entity)
	{
		if(entity == null)
		{
			setDestructiveness(1);
		}
		else if(entity instanceof EntityIMPigEngy || entity instanceof EntityIMCreeper)
		{
			setDestructiveness(0);
		}
		else
		{
			setDestructiveness(1);
		}
	}
	
	public float scaleAmount()
	{
		if(tier == 2)
			return 1.12F;
		else if(tier == 3)
			return 1.21F; // Big biped model too
		else
			return 1.0F;
	}

	// --------- Sparrow API --------- //

	/**What should this entity be referred to as? (Dog, Cat, Human, Enderman, etc.)*/
	@Override
	public String getSpecies()
	{
		return "Zombie";
	}
	
	@Override
	public int getTier()
 	{
		return tier < 3 ? 2 : 3;
	}

	// ------------------------------- //

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

		setFlavour(flavour);
		setTier(tier);
		super.readEntityFromNBT(nbttagcompound);
	}
	
	@Override
	protected void sunlightDamageTick()
	{
		if(tier == 2 && flavour == 2)
			damageEntity(DamageSource.generic, 3);
		else
			setFire(8);
	}

	protected void updateAnimation()
	{
		if(!worldObj.isRemote && terrainModifier.isBusy())
			setSwinging(true);

		int swingSpeed = getSwingSpeed();
		if(isSwinging())
		{
			swingTimer++;
			if (swingTimer >= swingSpeed)
			{
				swingTimer = 0;
				setSwinging(false);
			}
		}
		else
		{
			swingTimer = 0;
		}

		swingProgress = (float)swingTimer / (float)swingSpeed;
	}

	protected boolean isSwinging()
	{
		return getDataWatcher().getWatchableObjectByte(META_SWINGING) != (byte)0;
	}

	protected void setSwinging(boolean flag)
	{
		getDataWatcher().updateObject(META_SWINGING, flag == true ? (byte)1 : (byte)0);
	}

	protected void updateSound()
	{
		if(terrainModifier.isBusy())
		{
			if(--throttled2 <= 0)
			{
				//Make some block-destroying noise
				worldObj.playSoundAtEntity(this, "invsound.scrape", 0.85F, 1.0F / (rand.nextFloat() * 0.5F + 1.0F));
				throttled2 = 45 + rand.nextInt(20);
			}
		}
	}

	protected int getSwingSpeed()
	{
		return 10;
	}

	protected boolean chargeAttack(Entity entity)
	{
		int knockback = 4;
		entity.attackEntityFrom(DamageSource.causeMobDamage(this), attackStrength + 3);
		entity.addVelocity(-MathHelper.sin((rotationYaw * (float)Math.PI) / 180F) * knockback * 0.5F, 0.4D, MathHelper.cos((rotationYaw * (float)Math.PI) / 180F) * knockback * 0.5F);
		setSprinting(false);
		worldObj.playSoundAtEntity(entity, "damage.fallbig", 1.0F, 1.0F);
		return true;
	}

	@Override
	protected void updateAITasks()
	{
		super.updateAITasks();
		terrainModifier.onUpdate();
	}

	protected ITerrainDig getTerrainDig()
	{
		return terrainDigger;
	}

	@Override
	protected String getLivingSound()
	{
		if(tier == 3)
		{
			return rand.nextInt(3) == 0 ? "invsound.bigzombie" : null;
		}
		else
		{
			return "mob.zombie.say";
		}
	}

	@Override
	protected String getHurtSound()
	{
		return "mob.zombie.hurt";
	}

	@Override
	protected String getDeathSound()
	{
		return "mob.zombie.death";
	}

	@Override
	protected int getDropItemId()
	{
		return Item.rottenFlesh.itemID;
	}

	@Override
	protected void dropFewItems(boolean flag, int bonus)
	{
		super.dropFewItems(flag, bonus);
		if(rand.nextFloat() < 0.35F)
		{
			dropItem(Item.rottenFlesh.itemID, 1);
		}

		if(itemDrop != null && rand.nextFloat() < dropChance)
		{
			entityDropItem(new ItemStack(itemDrop, 1, 0), 0.0F);
		}
	}

	private void setAttributes(int tier, int flavour)
	{
		if(tier == 1)
		{
			if(flavour == 0)
			{
				setName("Zombie");
				setGender(1);
				moveSpeed = 0.38F;
				attackStrength = 4;
				health = 18;
				maxHealth = 18;
				selfDamage = 3;
				maxSelfDamage = 6;
				maxDestructiveness = 2;
				flammability = 3;
				//setBurnsInDay(true);
				setDestructiveness(2);
			}
			else if(flavour == 1) // Has weapon
			{
				setName("Zombie");
				setGender(1);
				moveSpeed = 0.38F;
				attackStrength = 6;
				health = 18;
				maxHealth = 18;
				selfDamage = 3;
				maxSelfDamage = 6;
				maxDestructiveness = 0;
				flammability = 3;
				defaultHeldItem = new ItemStack(Item.swordWood, 1);
				itemDrop = Item.swordWood;
				dropChance = 0.2F;
				//setBurnsInDay(true);
				setDestructiveness(0);
			}
		}
		else if(tier == 2)
		{
			if(flavour == 0)
			{
				setName("Zombie");
				setGender(1);
				moveSpeed = 0.38F;
				attackStrength = 7;
				health = 35;
				maxHealth = 35;
				selfDamage = 4;
				maxSelfDamage = 12;
				maxDestructiveness = 2;
				flammability = 4;
				itemDrop = Item.plateIron;
				dropChance = 0.25F;
				//setBurnsInDay(true);
				setDestructiveness(2);
			}
			else if(flavour == 1) // Has weapon
			{
				setName("Zombie");
				setGender(1);
				moveSpeed = 0.38F;
				attackStrength = 10;
				health = 40;
				maxHealth = 40;
				selfDamage = 3;
				maxSelfDamage = 9;
				maxDestructiveness = 0;
				itemDrop = Item.swordIron;
				dropChance = 0.25F;
				defaultHeldItem = new ItemStack(Item.swordIron, 1);
				//setBurnsInDay(true);
				setDestructiveness(0);
			}
			else if(flavour == 2) // Tar zombie
			{
				setName("Tar Zombie");
				setGender(1);
				moveSpeed = 0.38F;
				attackStrength = 5;
				health = 30;
				maxHealth = 30;
				selfDamage = 3;
				maxSelfDamage = 9;
				maxDestructiveness = 2;
				flammability = 30;
				floatsInWater = false;
				//setBurnsInDay(true);
				setDestructiveness(2);
			}
			else if(flavour == 3) // Zombie Pigman
			{
				setName("Zombie Pigman");
				setGender(1);
				moveSpeed = 0.5F;
				attackStrength = 8;
				health = 30;
				maxHealth = 30;
				maxDestructiveness = 2;
				isImmuneToFire = true;
				defaultHeldItem = new ItemStack(Item.swordGold, 1);
				setDestructiveness(2);
			}
		}
		else if(tier == 3)
		{
			if(flavour == 0)
			{
				setName("Zombie Brute");
				setGender(1);
				moveSpeed = moveSpeedBase = 0.34F;
				attackStrength = 18;
				health = 65;
				maxHealth = 65;
				selfDamage = 4;
				maxSelfDamage = 20;
				maxDestructiveness = 2;
				flammability = 4;
				//itemDrop = Item.plateSteel;
				dropChance = 0.0F;
				//setBurnsInDay(true);
				setDestructiveness(2);
			}
		}
	}

	private void doFireball()
	{
		int x = MathHelper.floor_double(posX);
		int y = MathHelper.floor_double(posY);
		int z = MathHelper.floor_double(posZ);
		for(int i = -1; i < 2; i++)
		{
			for(int j = -1; j < 2; j++)
			{
				for(int k = -1; k < 2; k++)
				{
					if(worldObj.getBlockId(x + i, y + j, z + k) == 0 || worldObj.getBlockMaterial(x + i, y + j, z + k).getCanBurn())
						worldObj.setBlock(x + i, y + j, z + k, Block.fire.blockID);
				}
			}
		}

		@SuppressWarnings("unchecked")
		List<Entity> entities = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(1.5, 1.5, 1.5));
		for(Entity entity : entities)
			entity.setFire(8);

		attackEntityFrom(DamageSource.inFire, 500);
	}
}
