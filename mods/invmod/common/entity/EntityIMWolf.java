package mods.invmod.common.entity;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mods.invmod.common.mod_Invasion;
import mods.invmod.common.nexus.INexusAccess;
import mods.invmod.common.nexus.SpawnPoint;
import mods.invmod.common.nexus.SpawnType;
import mods.invmod.common.nexus.TileEntityNexus;
import mods.invmod.common.util.ComparatorDistanceFrom;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityIMWolf extends EntityWolf
{
	private static final int META_BOUND = 30;
	
	private INexusAccess nexus;
	private int nexusX;
	private int nexusY;
	private int nexusZ;
	private int updateTimer;
	private boolean loadedFromNBT;
	
	public EntityIMWolf(World world)
    {
        this(world, null);
    }
	
	public EntityIMWolf(EntityWolf wolf, INexusAccess nexus)
    {
        this(wolf.worldObj, nexus);
        loadedFromNBT = false;
        setPositionAndRotation(wolf.posX, wolf.posY, wolf.posZ, wolf.rotationYaw, wolf.rotationPitch);
        dataWatcher.updateObject(16, wolf.getDataWatcher().getWatchableObjectByte(16));
        dataWatcher.updateObject(17, wolf.getDataWatcher().getWatchableObjectString(17));
        dataWatcher.updateObject(18, wolf.getDataWatcher().getWatchableObjectInt(18));
        aiSit.setSitting(isSitting());
    }
	
	public EntityIMWolf(World world, INexusAccess nexus)
	{
		super(world);
		targetTasks.addTask(5, new EntityAINearestAttackableTarget(this, IMob.class, 12F, 0, true));
		health = getMaxHealth();
		dataWatcher.addObject(META_BOUND, (byte)0);
		this.nexus = nexus;
		if(nexus != null)
		{
			nexusX = nexus.getXCoord();
			nexusY = nexus.getYCoord();
			nexusZ = nexus.getZCoord();
			dataWatcher.updateObject(META_BOUND, (byte)1);
		}
	}
	
	@Override
	public void onEntityUpdate()
	{
		super.onEntityUpdate();		
		if(loadedFromNBT)
		{
			loadedFromNBT = false;
			checkNexus();
		}
		
		if(!worldObj.isRemote && updateTimer++ > 40)
			checkNexus();
	}
	
	@Override
	public boolean attackEntityAsMob(Entity par1Entity)
    {
		int damage = isTamed() ? 4 : 2;
        if(par1Entity instanceof IMob)
        	damage *= 2;
        boolean success = par1Entity.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
        if(success)
        	heal(4);   
        
        return success;
    }
	
	@Override
	public int getMaxHealth()
    {
        return !isTamed() ? 8 : 25;
    }
	
	@Override
	public String getTexture()
    {
        if(isTamed())
        {
        	if(dataWatcher.getWatchableObjectByte(META_BOUND) == 1)
        		return "/mods/invmod/textures/wolf_tame_nexus.png";
        	else
        		return "/mob/wolf_tame.png";
        }
        if(isAngry())
        {
            return "/mob/wolf_angry.png";
        } else
        {
            return super.getTexture();
        }
    }
	
	@Override
	public int getCollarColor()
    {
        return dataWatcher.getWatchableObjectByte(META_BOUND) == 1 ? 10 : 1;
    }
	
	@Override
	protected String getHurtSound()
    {
    	if(getAttackTarget() instanceof IMob)
    		return "mob.wolf.growl";
    	else
    		return "mob.wolf.hurt";
    }
	
	@Override
	protected void onDeathUpdate()
    {
        deathTime++;
        if(deathTime == 120)
        {
        	if(!worldObj.isRemote && (recentlyHit > 0 || isPlayer()) && !isChild())
            {
                for(int i = getExperiencePoints(attackingPlayer); i > 0;)
                {
                    int k = EntityXPOrb.getXPSplit(i);
                    i -= k;
                    worldObj.spawnEntityInWorld(new EntityXPOrb(worldObj, posX, posY, posZ, k));
                }
            }
            //onEntityDeath();
            setDead();
            for(int j = 0; j < 20; j++)
            {
                double d = rand.nextGaussian() * 0.02D;
                double d1 = rand.nextGaussian() * 0.02D;
                double d2 = rand.nextGaussian() * 0.02D;
                worldObj.spawnParticle("explode", (posX + (rand.nextFloat() * width * 2.0F)) - width, posY + (rand.nextFloat() * height), (posZ + (rand.nextFloat() * width * 2.0F)) - width, d, d1, d2);
            }

        }
    }
	
	@Override
	public void setDead()
	{
		respawnAtNexus();
		isDead = true;
	}
	
	public boolean respawnAtNexus()
	{
		if(!worldObj.isRemote && dataWatcher.getWatchableObjectByte(META_BOUND) == 1 && nexus != null)
		{
			EntityIMWolf wolfRecreation = new EntityIMWolf(this, nexus);
			// For now, find spawn points per death
			int x = nexus.getXCoord();
			int y = nexus.getYCoord();
			int z = nexus.getZCoord();
			List<SpawnPoint> spawnPoints = new ArrayList<SpawnPoint>();
			setRotation(0, 0);
			for(int vertical = 0; vertical < 3; vertical = (vertical > 0) ? (vertical * -1) : (vertical * -1 + 1))
			{
				for(int i = -4; i < 5; i++)
				{
					for(int j = -4; j < 5; j++)
					{
						wolfRecreation.setPosition(x + i + 0.5F, y + vertical, z + j + 0.5F);
						if(wolfRecreation.getCanSpawnHere())
							spawnPoints.add(new SpawnPoint(x + i, y + vertical, z + i, 0, SpawnType.WOLF));
					}
				}
			}
			Collections.sort(spawnPoints, new ComparatorDistanceFrom(x, y, z));
			
			//Also for now, spawn at closest point
			if(spawnPoints.size() > 0)
			{
				SpawnPoint point = spawnPoints.get(spawnPoints.size() / 2);
				wolfRecreation.setPosition(point.getXCoord() + 0.5, point.getYCoord(), point.getZCoord() + 0.5);
				wolfRecreation.heal(60);
				worldObj.spawnEntityInWorld(wolfRecreation);
				return true;
			}
		}
		mod_Invasion.log("No respawn spot for wolf");
		return false;
	}
	
	@Override
	public boolean getCanSpawnHere()
    {
        return worldObj.checkNoEntityCollision(boundingBox) && worldObj.getCollidingBoundingBoxes(this, boundingBox).size() == 0 && !worldObj.isAnyLiquid(boundingBox);
    }
	
	@Override
	public boolean interact(EntityPlayer player)
	{
		 ItemStack itemstack = player.inventory.getCurrentItem();
		 if(itemstack != null)
		 {
			 if(itemstack.itemID == Item.bone.itemID && player.username.equalsIgnoreCase(getOwnerName()) && dataWatcher.getWatchableObjectByte(META_BOUND) == 1)
			 {
				 dataWatcher.updateObject(META_BOUND, (byte)0);
				 nexus = null;
				 
				 itemstack.stackSize--;
	             if (itemstack.stackSize <= 0)
	                 player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
	             return false;
			 }
			 else if(itemstack.itemID == mod_Invasion.itemStrangeBone.itemID && player.username.equalsIgnoreCase(getOwnerName()))
			 {
				 INexusAccess newNexus = findNexus();
				 if(newNexus != null && newNexus != nexus)
				 {
					 nexus = newNexus;
					 dataWatcher.updateObject(META_BOUND, (byte)1);
					 nexusX = nexus.getXCoord();
					 nexusY = nexus.getYCoord();
					 nexusZ = nexus.getZCoord();
					 
					 itemstack.stackSize--;
		             if (itemstack.stackSize <= 0)
		                 player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
				 }
				 return true;
			 }
		 }
		 return super.interact(player);
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeEntityToNBT(nbttagcompound);
		if(nexus != null)
		{
			nbttagcompound.setInteger("nexusX", nexus.getXCoord());
			nbttagcompound.setInteger("nexusY", nexus.getYCoord());
			nbttagcompound.setInteger("nexusZ", nexus.getZCoord());
		}
		nbttagcompound.setByte("nexusBound", dataWatcher.getWatchableObjectByte(META_BOUND));
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
	{
	    super.readEntityFromNBT(nbttagcompound);
		nexusX = nbttagcompound.getInteger("nexusX");
		nexusY = nbttagcompound.getInteger("nexusY");
		nexusZ = nbttagcompound.getInteger("nexusZ");
		dataWatcher.updateObject(META_BOUND,  nbttagcompound.getByte("nexusBound"));
		loadedFromNBT = true;
	}
	
	@Override
	public void setAngry(boolean par1)
    {
    }
	
	private void checkNexus()
	{
		if(worldObj != null && dataWatcher.getWatchableObjectByte(META_BOUND) == 1)
		{
			if(worldObj.getBlockId(nexusX, nexusY, nexusZ) == mod_Invasion.blockNexus.blockID)
				nexus = (TileEntityNexus)worldObj.getBlockTileEntity(nexusX, nexusY, nexusZ);	
			
			if(nexus == null)
				dataWatcher.updateObject(META_BOUND, (byte)0);
		}
	}
	
	private INexusAccess findNexus()
	{
		TileEntityNexus nexus = null;
		int x = MathHelper.floor_double(posX);
		int y = MathHelper.floor_double(posY);
		int z = MathHelper.floor_double(posZ);
		for(int i = -7; i < 8; i++)
		{
			for(int j = -4; j < 5; j++)
			{
				for(int k = -7; k < 8; k++)
				{
					if(worldObj.getBlockId(x + i, y + j, z + k) == mod_Invasion.blockNexus.blockID)
					{
						nexus = (TileEntityNexus)worldObj.getBlockTileEntity(x + i, y + j, z + k);
						break;
					}
				}
			}
		}
		
		return nexus;
	}
}
