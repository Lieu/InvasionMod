package mods.invmod.common;

import net.minecraft.world.IBlockAccess;

/**
 * Provides for a terrain data layer on top of regular block IDs and metadata
 * 
 * @author Lieu
 */
public interface IBlockAccessExtended extends IBlockAccess
{
	int getLayeredData(int x, int y, int z);
	
	void setData(int x, int y, int z, Integer data);
}
