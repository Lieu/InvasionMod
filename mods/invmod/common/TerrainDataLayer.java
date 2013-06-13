package mods.invmod.common;

import mods.invmod.common.entity.PathAction;
import mods.invmod.common.entity.PathNode;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.Vec3Pool;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeGenBase;

/**
 * Wrapper for IBlockAccess, adding a data layer to the terrain.
 * 
 * @author Lieu
 */
public class TerrainDataLayer implements IBlockAccessExtended
{
	public static final int EXT_DATA_SCAFFOLD_METAPOSITION = 1 << 14; // Bit 14 currently indicates scaffold
	// Bits 1-3 used for entity density map
	
    private IBlockAccess world;
    private IntHashMap dataLayer;
    
	public TerrainDataLayer(IBlockAccess world)
	{
		this.world = world;
		dataLayer = new IntHashMap();
	}
	
	@Override
	public void setData(int x, int y, int z, Integer data)
	{
		dataLayer.addKey(PathNode.makeHash(x, y, z, PathAction.NONE), data);
	}
	
	@Override
	public int getLayeredData(int x, int y, int z)
	{
		int key = PathNode.makeHash(x, y, z, PathAction.NONE);
    	if(dataLayer.containsItem(key))
    		return (Integer)dataLayer.lookup(key);
    	else
    		return 0;
	}
	
	public void setAllData(IntHashMap data)
	{
		dataLayer = data;
	}
	
    @Override
	public int getBlockId(int x, int y, int z)
    {
    	return world.getBlockId(x, y, z);
    }

    @Override
	public TileEntity getBlockTileEntity(int x, int y, int z)
    {
    	return world.getBlockTileEntity(x, y, z);
    }

    @Override
	public int getLightBrightnessForSkyBlocks(int x, int y, int z, int meta)
    {
    	return world.getLightBrightnessForSkyBlocks(x, y, z, meta);
    }

    @Override
	public float getBrightness(int x, int y, int z, int meta)
    {
    	return world.getBrightness(x, y, z, meta);
    }

    @Override
	public float getLightBrightness(int x, int y, int z)
    {
    	return world.getLightBrightness(x, y, z);
    }

    @Override
	public int getBlockMetadata(int x, int y, int z)
    {
    	return world.getBlockMetadata(x, y, z);
    }

    @Override
	public Material getBlockMaterial(int x, int y, int z)
    {
    	return world.getBlockMaterial(x, y, z);
    }

    @Override
	public boolean isBlockOpaqueCube(int x, int y, int z)
    {
    	return world.isBlockOpaqueCube(x, y, z);
    }

    @Override
	public  boolean isBlockNormalCube(int x, int y, int z)
    {
    	return world.isBlockNormalCube(x, y, z);
    }

    @Override
	public  boolean isAirBlock(int x, int y, int z)
    {
    	return world.isAirBlock(x, y, z);
    }

	@Override
	public BiomeGenBase getBiomeGenForCoords(int i, int j)
	{
		return world.getBiomeGenForCoords(i, j);
	}

	@Override
	public int getHeight()
	{
		return world.getHeight();
	}
	
    @Override
	public boolean extendedLevelsInChunkCache()
    {
    	return world.extendedLevelsInChunkCache();
    }
    
    @Override
	public boolean doesBlockHaveSolidTopSurface(int x, int y, int z)
    {
    	return world.doesBlockHaveSolidTopSurface(x, y, z);
    }

	@Override
	public Vec3Pool getWorldVec3Pool()
	{
		return world.getWorldVec3Pool();
	}

	@Override
	public int isBlockProvidingPowerTo(int var1, int var2, int var3, int var4)
	{
		return world.isBlockProvidingPowerTo(var1, var2, var3, var4);
	}
}
