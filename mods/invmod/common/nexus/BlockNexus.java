
package mods.invmod.common.nexus;


import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import mods.invmod.common.mod_Invasion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class BlockNexus extends BlockContainer
{
	@SideOnly(Side.CLIENT)
    private Icon sideOn;
    @SideOnly(Side.CLIENT)
    private Icon sideOff;
    @SideOnly(Side.CLIENT)
    private Icon topOn;
    @SideOnly(Side.CLIENT)
    private Icon topOff;
    @SideOnly(Side.CLIENT)
    private Icon botTexture;
    
    public BlockNexus(int id)
    {
        super(id, Material.rock);
    }
    
    public void registerIcons(IconRegister iconRegister)
    {
    	sideOn = iconRegister.registerIcon("invmod:nexusSideOn");
    	sideOff = iconRegister.registerIcon("invmod:nexusSideOff");
    	topOn = iconRegister.registerIcon("invmod:nexusTopOn");
    	topOff = iconRegister.registerIcon("invmod:nexusTopOff");
    	botTexture = iconRegister.registerIcon("obsidian");
    }
    
    @Override
    public Icon getIcon(int side, int meta)
    {
    	if((meta & 4) == 0)
    	{
    		if(side == 1)
            {
                return topOff;
            }
            return side != 0 ? sideOff : botTexture;
    	}
    	else
    	{
    		if(side == 1)
            {
                return topOn;
            }
            return side != 0 ? sideOn : botTexture;
    	}
    }
    
    /*public float getBlockBrightness(IBlockAccess iblockaccess, int i, int j, int k)
    {
    	if((iblockaccess.getBlockMetadata(i, j, k) & 4) == 0)
    	{
    		//return iblockaccess.getBrightness(i, j, k, lightValue[blockID]);
    		return iblockaccess.getBrightness(i, j, k, 15);
    	}
    	else
    	{
    		//return iblockaccess.getBrightness(i, j, k, lightValue[blockID]);
    		return iblockaccess.getBrightness(i, j, k, 15);
    	}
    }*/
    
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
    {
    	ItemStack item = entityPlayer.inventory.getCurrentItem();
    	int itemId = item != null ? item.itemID : 0;
        if(world.isRemote)
        {
            return true;
        }
        else if(itemId != mod_Invasion.itemProbe.itemID && (!mod_Invasion.isDebug() || itemId != mod_Invasion.itemDebugWand.itemID))
        {        	
        	TileEntityNexus tileEntityNexus = (TileEntityNexus)world.getBlockTileEntity(x, y, z);
            if(tileEntityNexus != null)
            {
            	mod_Invasion.setNexusClicked(tileEntityNexus);
                entityPlayer.openGui(mod_Invasion.getLoadedInstance(), mod_Invasion.getGuiIdNexus(), world, x, y, z);
            }        	
            return true;
        }
        else
        {
        	return false; // Let item do its thing
        }
    }
    
    @Override
    public TileEntity createNewTileEntity(World world)
    {
        return new TileEntityNexus(world);
    }
    
    /**
     * Creates particles based on nexus state
     */
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random random)
    {
    	int meta = world.getBlockMetadata(x, y, z);
    	int numberOfParticles;
    	if((meta & 4) == 0)
    		numberOfParticles = 0;
    	else
    		numberOfParticles = 6;
    	
    	// Do particles.
    	// Note that particles *end* at the first set of coords
        for(int i = 0; i < numberOfParticles; i++)
        {
        	// y offset uniformly random between 0 and 1
        	// y vector -0.25 to 0.25 - total particle up/down movement
        	double y1 = y + random.nextFloat();
        	double y2 = (random.nextFloat() - 0.5D) * 0.5D;        	
                     
        	// Choose -1 or 1 for later direction of either x or z movement
            int direction = random.nextInt(2) * 2 - 1;
            
            // Randomly choose the axis particles will primarily move along.
            // This creates the effect of particles moving towards a plane
            double x1, z1, x2, z2;
            if(random.nextInt(2) == 0)
            {
            	// z is the long axis. Middle of block(0.5) + -0.25 to 0.25 and
            	// a -2.0 to 2.0 vector of the same sign
                z1 = z + 0.5D + 0.25D * direction;
                z2 = random.nextFloat() * 2.0F * direction;
                
                // x destination offset between 0 and 1
                // x movement between -0.25 and 0.25
                x1 = x + random.nextFloat();
                x2 = (random.nextFloat() - 0.5D) * 0.5D;
            }
            else
            {
            	// The same but x is the long axis
                x1 = x + 0.5D + 0.25D * direction;
                x2 = random.nextFloat() * 2.0F * direction;
                z1 = z + random.nextFloat();
                z2 = (random.nextFloat() - 0.5D) * 0.5D;
            }
            
            // Finally, spawn the particle
            world.spawnParticle("portal", x1, y1, z1, x2, y2, z2);
        }
    }
}
