package mods.invmod.common.entity;

import mods.invmod.common.IPathfindable;
import mods.invmod.common.util.CoordsInt;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;

public interface IPathSource
{
	public enum PathPriority
	{
		LOW, MEDIUM, HIGH
	}
	
	public Path createPath(IPathfindable entity, int x, int y, int z, int x2, int y2, int z2, float maxSearchRange, IBlockAccess terrainMap);
	
	public Path createPath(EntityIMLiving entity, Entity target, float maxSearchRange, IBlockAccess terrainMap);

    public Path createPath(EntityIMLiving entity, int x, int y, int z, float maxSearchRange, IBlockAccess terrainMap);
  
    
    /**
     * Creates a path and notifies the specified observer on completion. The path may be
     * finished at some point in the future and/or on another thread.
     * 
     * @param observer
     * @param entity
     * @param x
     * @param y
     * @param z
     * @param x2
     * @param y2
     * @param z2
     * @param maxSearchRange
     */
    public void createPath(IPathResult observer, IPathfindable entity, int x, int y, int z, int x2, int y2, int z2, float maxSearchRange, IBlockAccess terrainMap);
    
    public void createPath(IPathResult observer, EntityIMLiving entity, Entity target, float maxSearchRange, IBlockAccess terrainMap);

    public void createPath(IPathResult observer, EntityIMLiving entity, int x, int y, int z, float maxSearchRange, IBlockAccess terrainMap);
    
    public int getSearchDepth();
    
    public int getQuickFailDepth();
    
    public void setSearchDepth(int depth);
    
    public void setQuickFailDepth(int depth);
    
    /**
     * Returns true if it's this entity's 'turn' to pathfind, given the amount of CPU time
     * available to pathfind, the parameters of the search specified and the recent search
     * performance from calls to this object.
     * 
     * @param priority Pathfinding priority.
     * @param maxSearchRange How wide the search space is, such as distance from start to
     * 						 target. This has little effect on performance but might indicate
     * 						 a deeper search is more likely.
     * @param searchDepth The maximum number of nodes that may be opened. Nodes opened is
     * 					  proportional to time taken.
     * @param quickFailDepth The number of nodes opened in a row with no better result, after
     * 						 which the search is ended. This is a simple heuristic to end
     * 						 fruitless searches.
     */
    public boolean canPathfindNice(PathPriority priority, float maxSearchRange, int searchDepth, int quickFailDepth);
}
