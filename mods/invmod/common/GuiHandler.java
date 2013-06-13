package mods.invmod.common;

import mods.invmod.common.nexus.ContainerNexus;
import mods.invmod.common.nexus.GuiNexus;
import mods.invmod.common.nexus.TileEntityNexus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
	{
		// Needs to return a GuiScreen client-side
		if(id == mod_Invasion.getGuiIdNexus())
		{
			TileEntityNexus nexus = (TileEntityNexus)world.getBlockTileEntity(x, y, z);
			if(nexus != null)
				return new GuiNexus(player.inventory, nexus);
		}
		
		return null;
	}

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
	{
		// Needs to return a Container server-side
		if(id == mod_Invasion.getGuiIdNexus())
		{
			TileEntityNexus nexus = (TileEntityNexus)world.getBlockTileEntity(x, y, z);
			if(nexus != null)
				return new ContainerNexus(player.inventory, nexus);
		}
		
		return null;
	}
}
