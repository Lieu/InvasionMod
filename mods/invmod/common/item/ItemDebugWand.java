package mods.invmod.common.item;


import java.util.ArrayList;

import mods.invmod.common.mod_Invasion;
import mods.invmod.common.entity.EntityIMBird;
import mods.invmod.common.entity.EntityIMCreeper;
import mods.invmod.common.entity.EntityIMGiantBird;
import mods.invmod.common.entity.EntityIMPigEngy;
import mods.invmod.common.entity.EntityIMSkeleton;
import mods.invmod.common.entity.EntityIMSpider;
import mods.invmod.common.entity.EntityIMThrower;
import mods.invmod.common.entity.EntityIMZombie;
import mods.invmod.common.nexus.TileEntityNexus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemDebugWand extends ItemIM
{
	private TileEntityNexus nexus;
	
	public ItemDebugWand(int itemId)
	{
		super(itemId);
		maxStackSize = 1;
        setMaxDamage(0);
	}
	
	@Override
	public boolean onItemUseFirst(ItemStack itemstack, EntityPlayer entityplayer, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
		if(world.isRemote)
			return false;
		
		int id = world.getBlockId(x, y, z);
        if(id == mod_Invasion.blockNexus.blockID)
        {
        	nexus = (TileEntityNexus)world.getBlockTileEntity(x, y, z);
	        return true;
        }
        
        //EntityIMBird bird = new EntityIMBird(world);
        EntityIMBird bird = new EntityIMGiantBird(world);
        bird.setPosition(x, y + 1, z);
        //world.spawnEntityInWorld(bird);
        
        EntityZombie zombie2 = new EntityZombie(world);
        zombie2.setPosition(x, y + 1, z);
        world.spawnEntityInWorld(zombie2);
        
        //zombie2.mountEntity(bird);
        
		//if((int)entityplayer.posX % 2 == 0)
		{
			//Tester tester = new Tester();
			//tester.doWaveBuilderTest(1, 4, 180);
			
			EntityWolf wolf = new EntityWolf(world);
			wolf.setPosition(x, y + 1, z);
			//world.spawnEntityInWorld(wolf);
			
			
			Entity entity1 = new EntityIMPigEngy(world);
			entity1.setPosition(x, y + 1, z);
			//world.spawnEntityInWorld(entity1);
			
			EntityIMZombie zombie = new EntityIMZombie(world, nexus);
			zombie.setTexture(0);
			zombie.setFlavour(0);
			zombie.setTier(2);
			//zombie.setTexture(0);
			zombie.setPosition(x, y + 1, z);
			//world.spawnEntityInWorld(zombie);
			
			if(nexus != null)
			{
				Entity entity = new EntityIMPigEngy(world, nexus);
				entity.setPosition(x, y + 1, z);
				world.spawnEntityInWorld(entity);
				
				zombie = new EntityIMZombie(world, nexus);
				zombie.setTexture(0);
				zombie.setFlavour(0);
				zombie.setTier(3);
				zombie.setPosition(x, y + 1, z);
				//world.spawnEntityInWorld(zombie);
				
				Entity thrower = new EntityIMThrower(world, nexus);
				thrower.setPosition(x, y + 1, z);
				//world.spawnEntityInWorld(thrower);
				
				EntityIMCreeper creep = new EntityIMCreeper(world, nexus);
				creep.setPosition(x, y + 1, z);
				//world.spawnEntityInWorld(creep);
				
				EntityIMSpider spider = new EntityIMSpider(world, nexus);
				//EntityIMBurrower entity = new EntityIMBurrower(world);
				//entity = new EntityIMSpider(world);
				spider.setTexture(0);
				spider.setFlavour(0);
				spider.setTier(2);
				//EntityWolf entity = new EntityWolf(world);
				spider.setPosition(x, y + 1, z);
				//world.spawnEntityInWorld(spider);
			}
			EntityIMSpider entity = new EntityIMSpider(world, nexus);
			//EntityIMBurrower entity = new EntityIMBurrower(world);
			//entity = new EntityIMSpider(world);
			entity.setTexture(0);
			entity.setFlavour(1);
			entity.setTier(2);
			//EntityWolf entity = new EntityWolf(world);
			entity.setPosition(x, y + 1, z);
			//world.spawnEntityInWorld(entity);
			
			EntityIMCreeper creep = new EntityIMCreeper(world);
			creep.setPosition(150.5, 64, 271.5);
			//world.spawnEntityInWorld(creep);
			
			EntityIMSkeleton skeleton = new EntityIMSkeleton(world);
			skeleton.setPosition(x, y + 1, z);
			//world.spawnEntityInWorld(skeleton);
			
			//Entity yy = new EntityTurret(world, x, y + 1, z, 1);
			//yy.setPosition(x, y + 1, z);
			//world.spawnEntityInWorld(yy);
			
			
			
		}
		//else
		{
			//long time = world.getWorldTime();
			//world.setWorldTime(time + 24000 - time % 24000);
		}
		
		
        return true;
    }
	
	@Override
	public boolean itemInteractionForEntity(ItemStack itemstack, EntityLiving targetEntity)
    {
		if(targetEntity instanceof EntityWolf)
		{
			EntityWolf wolf = (EntityWolf)targetEntity;
			EntityPlayer player = targetEntity.worldObj.getClosestPlayer(targetEntity.posX, targetEntity.posY, targetEntity.posZ, 6F);
			if(player != null)
				wolf.setOwner(player.username);
			
			return true;
		}
		return false;
    }
	
	public void addCreativeItems(ArrayList itemList)
	{
		
	}
}
