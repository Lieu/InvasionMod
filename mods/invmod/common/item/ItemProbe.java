package mods.invmod.common.item;


import java.util.List;

import mods.invmod.common.mod_Invasion;
import mods.invmod.common.entity.EntityIMLiving;
import mods.invmod.common.nexus.TileEntityNexus;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemProbe extends ItemIM
{
	@SideOnly(Side.CLIENT)
	private Icon iconAdjuster;
	@SideOnly(Side.CLIENT)
	private Icon iconProbe;
	
	public ItemProbe(int itemId)
	{
		super(itemId);
		maxStackSize = 1;
		setHasSubtypes(true);
        setMaxDamage(0);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
    public void registerIcons(IconRegister par1IconRegister)
    {
        iconAdjuster = par1IconRegister.registerIcon("invmod:" + "adjuster");
        iconProbe = par1IconRegister.registerIcon("invmod:" + "probe");
    }
	
	@Override
	public int getDamageVsEntity(Entity entity)
    {
        return 0;
    }
	
	@Override
	public boolean isFull3D()
    {
        return true;
    }
	
	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
    {
        return itemstack;
    }
	
	@Override
	public boolean onItemUseFirst(ItemStack itemstack, EntityPlayer entityplayer, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
		if(world.isRemote)
			return false;
		
		int id = world.getBlockId(x, y, z);
        if(id == mod_Invasion.blockNexus.blockID)
        {
        	TileEntityNexus nexus = (TileEntityNexus)world.getBlockTileEntity(x, y, z);
        	int newRange = nexus.getSpawnRadius() + 8;
        	if(newRange > 128)
        		newRange = 32;
        	nexus.setSpawnRadius(newRange);
        	// TODO
        	mod_Invasion.sendMessageToPlayer(entityplayer.username, "Nexus range changed to: " + nexus.getSpawnRadius());
	        return true;
        }
        else if(itemstack.getItemDamage() == 1)
        {        	
        	float blockStrength = EntityIMLiving.getBlockStrength(x, y, z, id, world);
        	// TODO
        	mod_Invasion.sendMessageToPlayer(entityplayer.username, "Block strength: " + (double)((int)((blockStrength + 0.005) * 100F))/100F);
        	return true;
        }        
        return false;
    }
	
	@Override
	public String getUnlocalizedName(ItemStack itemstack)
    {
    	if(itemstack.getItemDamage() < probeNames.length)
    		return probeNames[itemstack.getItemDamage()];
    	else
    		return "";
    }
	    
    @Override
	public Icon getIconFromDamage(int i)
    {
        if(i == 1)
        	return iconProbe;
        else
        	return iconAdjuster;
    }
    
    @Override
	public int getItemEnchantability()
    {
        return 14;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(int id, CreativeTabs tab, List dest)
    {
        dest.add(new ItemStack(id, 1, 0));
        dest.add(new ItemStack(id, 1, 1));
    }
	
	public static final String probeNames[] = {
        "Nexus Adjuster", "Material Probe"
    };
}
