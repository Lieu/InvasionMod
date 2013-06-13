
package mods.invmod.common.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumToolMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;

public class ItemInfusedSword extends ItemSword
{
    private int weaponDamage;
    
    public ItemInfusedSword(int i)
    {
        super(i, EnumToolMaterial.EMERALD);
        maxStackSize = 1;
        setMaxDamage(21);
        weaponDamage = 7;
    }
    
    @SideOnly(Side.CLIENT)
	@Override
    public void registerIcons(IconRegister par1IconRegister)
    {
    	itemIcon = par1IconRegister.registerIcon("invmod:" + getUnlocalizedName().substring(5)); // Remove "item." prefix
    }

    /*public float getStrVsBlock(ItemStack itemstack, Block block)
    {
        return block.blockID != Block.web.blockID ? 1.5F : 15F;
    }*/
    
    @Override
    public boolean isDamageable()
    {
    	return false;
    }

    @Override
	public boolean hitEntity(ItemStack itemstack, EntityLiving entityliving, EntityLiving entityliving1)
    {
    	if(itemstack.getItemDamage() > 0)
    	{
    		itemstack.setItemDamage(itemstack.getItemDamage() - 1);
    	}
        return true;
    }

    @Override
	public boolean onBlockStartBreak(ItemStack itemstack, int i, int j, int k, EntityPlayer entityPlayer)
    {
        return true;
    }

    @Override
	public int getDamageVsEntity(Entity entity)
    {
        return weaponDamage;
    }
    
    /*public boolean isFull3D()
    {
        return true;
    }
    
    public int getItemEnchantability()
    {
        return 10;
    }*/
    
    @Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.none;
    }
    
    @Override
	public int getMaxItemUseDuration(ItemStack par1ItemStack)
    {
        return 0;
    }

    @Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
    {
    	if(itemstack.getItemDamage() == 0)
    	{
    		entityplayer.heal(6);
    		itemstack.setItemDamage(20);
    		world.spawnParticle("heart", entityplayer.posX + 1.5, entityplayer.posY, entityplayer.posZ, 0, 0, 0);
            world.spawnParticle("heart", entityplayer.posX - 1.5, entityplayer.posY, entityplayer.posZ, 0, 0, 0);
            world.spawnParticle("heart", entityplayer.posX, entityplayer.posY, entityplayer.posZ + 1.5, 0, 0, 0);
            world.spawnParticle("heart", entityplayer.posX, entityplayer.posY, entityplayer.posZ - 1.5, 0, 0, 0);
    	}
        //entityplayer.updateItemUse(itemstack, func_35404_c(itemstack));
        return itemstack;
    }

    @Override
	public boolean canHarvestBlock(Block block)
    {
        return block.blockID == Block.web.blockID;
    }
    
    @Override
    public boolean onBlockDestroyed(ItemStack par1ItemStack, World par2World, int par3, int par4, int par5, int par6, EntityLiving par7EntityLiving)
    {
    	return true;
    }
}
