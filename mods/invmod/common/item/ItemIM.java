package mods.invmod.common.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;

public class ItemIM extends Item
{
	public ItemIM(int id)
	{
		super(id);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
    public void registerIcons(IconRegister par1IconRegister)
    {
        itemIcon = par1IconRegister.registerIcon("invmod:" + getUnlocalizedName().substring(5)); // Remove "item." prefix
    }
}
