package mods.invmod.common.item;

import mods.invmod.common.mod_Invasion;
import mods.invmod.common.entity.EntityIMWolf;
import mods.invmod.common.nexus.TileEntityNexus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

public class ItemStrangeBone extends ItemIM
{
	public ItemStrangeBone(int i)
    {
        super(i);
        maxStackSize = 1;
    }
	
	@Override
	public int getDamageVsEntity(Entity entity)
	{
		 return 0;
	}
	
	@Override
	public boolean itemInteractionForEntity(ItemStack itemstack, EntityLiving targetEntity)
    {
		if(!targetEntity.worldObj.isRemote && targetEntity instanceof EntityWolf && !(targetEntity instanceof EntityIMWolf))
		{
			EntityWolf wolf = (EntityWolf)targetEntity;
			if(wolf.isTamed())
			{
				TileEntityNexus nexus = null;
				int x = MathHelper.floor_double(wolf.posX);
				int y = MathHelper.floor_double(wolf.posY);
				int z = MathHelper.floor_double(wolf.posZ);
				for(int i = -7; i < 8; i++)
				{
					for(int j = -4; j < 5; j++)
					{
						for(int k = -7; k < 8; k++)
						{
							if(wolf.worldObj.getBlockId(x + i, y + j, z + k) == mod_Invasion.blockNexus.blockID)
							{
								nexus = (TileEntityNexus)wolf.worldObj.getBlockTileEntity(x + i, y + j, z + k);
								break;
							}
						}
					}
				}
				
				if(nexus != null)
				{
					// Create a deep copy of the wolf for replacement
					EntityIMWolf newWolf = new EntityIMWolf(wolf, nexus);
					wolf.worldObj.spawnEntityInWorld(newWolf);
					wolf.setDead();
					itemstack.stackSize--;
				}
			}
			return true;
		}
		return false;
    }
	
	@Override
	public boolean hitEntity(ItemStack itemstack, EntityLiving entityLiving, EntityLiving entityLiving1)
    {
		/*if(!entityLiving.worldObj.isRemote)
		{
			// Can't use right-click (saddleEntity() - use item on entity) because the callback is to EntityWolf first
			if(entityLiving instanceof EntityCreature)
			{
				((EntityCreature)entityLiving).setTarget(null);
				if(entityLiving instanceof EntityWolf && entityLiving1 instanceof EntityPlayer)
				{
					EntityWolf wolf = (EntityWolf)entityLiving;
					EntityPlayer player = (EntityPlayer)entityLiving1;
					wolf.setAngry(false);
					wolf.heal(2);
					if(wolf.isTamed() && player.username.equalsIgnoreCase(wolf.getOwnerName())) // isTamed and owner
					{
						TileEntityNexus nexus = null;
						int x = MathHelper.floor_double(wolf.posX);
						int y = MathHelper.floor_double(wolf.posY);
						int z = MathHelper.floor_double(wolf.posZ);
						for(int i = -7; i < 8; i++)
						{
							for(int j = -4; j < 5; j++)
							{
								for(int k = -7; k < 8; k++)
								{
									if(wolf.worldObj.getBlockId(x + i, y + j, z + k) == mod_Invasion.blockNexus.blockID)
									{
										nexus = (TileEntityNexus)wolf.worldObj.getBlockTileEntity(x + i, y + j, z + k);
										break;
									}
								}
							}
						}
						
						if(nexus != null)
						{
							// Create a deep copy of the wolf for replacement
							EntityIMWolf newWolf = new EntityIMWolf(wolf, nexus);
							wolf.worldObj.spawnEntityInWorld(newWolf);
							wolf.setEntityDead();
							itemstack.stackSize--;
							return true;
						}
					}
				}
			}
		}*/
        return false;
    }
}
