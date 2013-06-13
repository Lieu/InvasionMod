package mods.invmod.common.entity;

import mods.invmod.common.mod_Invasion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

/**
 * Entity to spawn other entities and to handle complex building of
 * such entities. Useful for avoiding modification of base classes.
 * 
 * @author Lieu *
 */
public class EntityIMSpawnProxy extends EntityLiving
{
	public EntityIMSpawnProxy(World world)
	{
		super(world);
	}
	
	@Override
	public void onEntityUpdate()
	{
		if(worldObj != null)
		{
			// Assume entity is otherwise fully initialised before this method is called
			Entity[] entities = mod_Invasion.getNightMobSpawns1(worldObj);
			for(Entity entity : entities)
			{
				entity.setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
				worldObj.spawnEntityInWorld(entity);
			}			
		}		
		setDead();
	}
	
	@Override
	public int getMaxHealth()
	{
		return 100;
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
	{		
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
	{		
	}
	
	public float getBlockPathWeight(int i, int j, int k)
    {
        return 0.5F - worldObj.getLightBrightness(i, j, k);
    }
	
	protected boolean darkEnoughToSpawn()
    {
        int i = MathHelper.floor_double(posX);
        int j = MathHelper.floor_double(boundingBox.minY);
        int k = MathHelper.floor_double(posZ);
        if (worldObj.getSavedLightValue(EnumSkyBlock.Sky, i, j, k) > rand.nextInt(32))
        {
            return false;
        }
        int l = worldObj.getBlockLightValue(i, j, k);
        if (worldObj.isThundering())
        {
            int i1 = worldObj.skylightSubtracted;
            worldObj.skylightSubtracted = 10;
            l = worldObj.getBlockLightValue(i, j, k);
            worldObj.skylightSubtracted = i1;
        }
        return l <= rand.nextInt(8);
    }

    @Override
	public boolean getCanSpawnHere()
    {
    	int i = MathHelper.floor_double(posX);
        int j = MathHelper.floor_double(boundingBox.minY);
        int k = MathHelper.floor_double(posZ);
        return darkEnoughToSpawn() && super.getCanSpawnHere() && getBlockPathWeight(i, j, k) >= 0.0F;
    }
}
