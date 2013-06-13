
package mods.invmod.common.item;


import java.util.List;

import mods.invmod.common.entity.EntityIMTrap;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemIMTrap extends ItemIM
{
	@SideOnly(Side.CLIENT)
    private Icon emptyIcon;
	@SideOnly(Side.CLIENT)
    private Icon riftIcon;
	@SideOnly(Side.CLIENT)
    private Icon flameIcon;   
    
    public ItemIMTrap(int itemId)
    {
        super(itemId);
        maxStackSize = 64;
        setHasSubtypes(true);
        setMaxDamage(0);
    }
    
    @SideOnly(Side.CLIENT)
	@Override
    public void registerIcons(IconRegister par1IconRegister)
    {
        emptyIcon = par1IconRegister.registerIcon("invmod:" + "trapEmpty");
        riftIcon = par1IconRegister.registerIcon("invmod:" + "trapPurple");
        flameIcon = par1IconRegister.registerIcon("invmod:" + "trapRed");
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
    	
        if(side == 1)
        {
	        EntityIMTrap trap;        
        	if(itemstack.getItemDamage() == 1)
        		trap = new EntityIMTrap(world, x + 0.5, y + 1.0, z + 0.5, EntityIMTrap.TRAP_RIFT);
        	else if(itemstack.getItemDamage() == 2)
        		trap = new EntityIMTrap(world, x + 0.5, y + 1.0, z + 0.5, EntityIMTrap.TRAP_FIRE);
        	else
        		return false;
        	
	        if(trap.isValidPlacement() && world.getEntitiesWithinAABB(EntityIMTrap.class, trap.boundingBox).size() == 0)
	        {
                world.spawnEntityInWorld(trap);
                itemstack.stackSize--;          
	        }
	        return true;
        }
        else
        {
        	return false;
        }
    }
    
    @Override
	public String getUnlocalizedName(ItemStack itemstack)
    {
    	if(itemstack.getItemDamage() < trapNames.length)
    		return trapNames[itemstack.getItemDamage()];
    	else
    		return "";
    }
    
    @Override
	public Icon getIconFromDamage(int i)
    {
        if(i == 1)
        	return riftIcon;
        else if(i == 2)
        	return flameIcon;
        else
        	return emptyIcon;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(int id, CreativeTabs tab, List dest)
    {
        dest.add(new ItemStack(id, 1, EntityIMTrap.TRAP_DEFAULT));
        dest.add(new ItemStack(id, 1, EntityIMTrap.TRAP_RIFT));
        dest.add(new ItemStack(id, 1, EntityIMTrap.TRAP_FIRE));
    }
    
    public static final String trapNames[] = {
        "Empty Trap", "Rift Trap (folded)", "Flame Trap (folded)", "XYZ Trap"
    };
}
