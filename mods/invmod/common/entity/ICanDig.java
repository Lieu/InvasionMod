package mods.invmod.common.entity;

import mods.invmod.common.util.IPosition;
import net.minecraft.world.IBlockAccess;

public interface ICanDig
{
	IPosition[] getBlockRemovalOrder(int x, int y, int z);
	
	float getBlockRemovalCost(int x, int y, int z);
	
	boolean canClearBlock(int x, int y, int z);
	
	void onBlockRemoved(int x, int y, int z, int id);
	
	IBlockAccess getTerrain();
}
