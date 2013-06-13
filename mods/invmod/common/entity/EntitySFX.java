package mods.invmod.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntitySFX extends Entity
{
	private int lifespan;
	
	public EntitySFX(World world)
	{
		super(world);
		lifespan = 200;
	}
	
	public EntitySFX(World world, double x, double y, double z)
	{
		super(world);
		lifespan = 200;
		posX = x;
		posY = y;
		posZ = z;
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		if(lifespan-- <= 0)
		{
			setDead();
		}
	}
	
	@Override
	public void handleHealthUpdate(byte byte0)
    {
		if(byte0 == 0)
        {
        	//mod_Invasion.playGlobalSFX(1);
        }
        else if(byte0 == 1)
        {
        }
        else if(byte0 == 2)
        {
        }
    }
	
	@Override
	public void entityInit()
	{	
	}
	
	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound)
	{	
	}

    @Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
	}
}