package mods.invmod.common.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class ItemRiftFlux extends ItemIM
{

    public ItemRiftFlux(int i)
    {
        super(i);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    /*public int getIconFromDamage(int i)
    {
        int j = i;
        return iconIndex + (j % 8) * 16 + j / 8;
    }*/

    @Override
	public String getUnlocalizedName(ItemStack itemstack)
    {
    	return fluxNames[itemstack.getItemDamage()].toString();
    }

    @Override
    public boolean onItemUseFirst(ItemStack itemstack, EntityPlayer entityplayer, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
        /*if(!entityplayer.canPlayerEdit(i, j, k))
        {
            return false;
        }
        if(itemstack.getItemDamage() == 15)
        {
        }*/
        return false;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void getSubItems(int id, CreativeTabs tab, List dest)
    {
        dest.add(new ItemStack(id, 1, 1));
    }

    public static final String fluxNames[] = {
        "Inert Flux", "Rift Flux", "ff Flux", "yy Flux", "invalid", "invalid", "invalid", "invalid", "invalid", "invalid", 
        "invalid", "invalid", "invalid", "invalid", "invalid", "invalid"
    };
}
