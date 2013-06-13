package mods.invmod.common;

import mods.invmod.common.entity.PathNode;
import mods.invmod.common.entity.PathfinderIM;
import net.minecraft.world.IBlockAccess;

/**
 * IPathfindable represents pathfinding being applicable to this object
 * 
 * @author Lieu
 */
public interface IPathfindable
{
	/**
	 * Returns the cost of moving from one node to the next.
	 */
	float getBlockPathCost(PathNode prevNode, PathNode node, IBlockAccess worldMap);
	
	void getPathOptionsFromNode(IBlockAccess terrainMap, PathNode currentNode, PathfinderIM pathFinder);
}