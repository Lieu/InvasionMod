package mods.invmod.common.entity;

import mods.invmod.common.IPathfindable;
import mods.invmod.common.nexus.INexusAccess;
import mods.invmod.common.util.CoordsInt;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class PathCreator implements IPathSource
{
	private int searchDepth;
	private int quickFailDepth;
	private int[] nanosUsed;
	private int index;
	
	public PathCreator()
	{
		searchDepth = 200;
		quickFailDepth = 50;
		nanosUsed = new int[6];
		index = 0;
	}
	
	@Override
	public int getSearchDepth()
	{
		return searchDepth;
	}

	@Override
	public int getQuickFailDepth()
	{
		return quickFailDepth;
	}

	@Override
	public void setSearchDepth(int depth)
	{
		searchDepth = depth;
	}

	@Override
	public void setQuickFailDepth(int depth)
	{
		quickFailDepth = depth;
	}

	@Override
	public Path createPath(IPathfindable entity, int x, int y, int z, int x2, int y2, int z2, float maxSearchRange, IBlockAccess terrainMap)
	{
		long time = System.nanoTime();
		Path path = PathfinderIM.createPath(entity, x, y, z, x2, y2, z2, maxSearchRange, terrainMap, searchDepth, quickFailDepth);
		int elapsed = (int)(System.nanoTime() - time); // Overflows if call takes longer than 2.14 seconds
		nanosUsed[index] = elapsed;
		if(++index >= nanosUsed.length)
			index = 0;
		
		return path;
	}

	@Override
	public Path createPath(EntityIMLiving entity, Entity target, float maxSearchRange, IBlockAccess terrainMap)
	{
		return createPath(entity, MathHelper.floor_double(target.posX + 0.5F - (entity.width / 2.0F)), MathHelper.floor_double(target.posY),
						  MathHelper.floor_double(target.posZ + 0.5F - (entity.width / 2.0F)), maxSearchRange, terrainMap);
	}

	@Override
	public Path createPath(EntityIMLiving entity, int x, int y, int z, float maxSearchRange, IBlockAccess terrainMap)
	{
		CoordsInt size = entity.getCollideSize();
    	int startX, startY, startZ;
    	if(size.getXCoord() <= 1 && size.getZCoord() <= 1)
    	{
    		startX = entity.getXCoord();
    		startY = MathHelper.floor_double(entity.boundingBox.minY);
    		startZ = entity.getZCoord();
    	}
    	else
    	{
    		startX = MathHelper.floor_double(entity.boundingBox.minX);
    		startY = MathHelper.floor_double(entity.boundingBox.minY);
    		startZ = MathHelper.floor_double(entity.boundingBox.minZ);
    	}
        return createPath(entity, startX, startY, startZ, MathHelper.floor_double(x + 0.5F - (double)(entity.width / 2.0F)), y, MathHelper.floor_double(z + 0.5F - (double)(entity.width / 2.0F)), maxSearchRange, terrainMap);
	}

	@Override
	public void createPath(IPathResult observer, IPathfindable entity, int x, int y, int z, int x2, int y2, int z2, float maxSearchRange, IBlockAccess terrainMap)
	{
		
	}

	@Override
	public void createPath(IPathResult observer, EntityIMLiving entity, Entity target, float maxSearchRange, IBlockAccess terrainMap)
	{
		
	}

	@Override
	public void createPath(IPathResult observer, EntityIMLiving entity, int x, int y, int z, float maxSearchRange, IBlockAccess terrainMap)
	{
		
	}

	@Override
	public boolean canPathfindNice(PathPriority priority, float maxSearchRange, int searchDepth, int quickFailDepth)
	{
		return true;
	}
}
