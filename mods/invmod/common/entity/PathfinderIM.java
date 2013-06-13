
package mods.invmod.common.entity;

import java.util.List;

import mods.invmod.common.IPathfindable;
import mods.invmod.common.util.CoordsInt;
import net.minecraft.entity.Entity;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;

public class PathfinderIM
{
	private static PathfinderIM pathfinder;
	
    private IBlockAccess worldMap;
    private NodeContainer path;
    private IntHashMap pointMap;
    private PathNode pathOptions[];
    private PathNode finalTarget;
    private int pathsIndex;
    private float searchRange;
    private int nodeLimit;
    
    static
    {
    	pathfinder = new PathfinderIM();
    }
    
    public synchronized static Path createPath(IPathfindable entity, int x, int y, int z, int x2, int y2, int z2, float maxSearchRange,
    							  IBlockAccess iblockaccess, int searchDepth, int quickFailDepth)
    {
    	return pathfinder.createEntityPathTo(entity, x, y, z, x2, y2, z2, maxSearchRange, iblockaccess, searchDepth, quickFailDepth);
    }
    
    public PathfinderIM()
    {
        path = new NodeContainer();
        pointMap = new IntHashMap();
        pathOptions = new PathNode[32];
    }
    
    public Path createEntityPathTo(IPathfindable entity, int x, int y, int z, int x2, int y2, int z2, float maxSearchRange,
    							   IBlockAccess iblockaccess, int searchDepth, int quickFailDepth)
    {
    	//Profiler.startSection("Pathfinding");  
    	worldMap = iblockaccess;
    	nodeLimit = searchDepth;
    	searchRange = maxSearchRange;
        path.clearPath();
        pointMap.clearMap();
        PathNode start = openPoint(x, y, z);
        PathNode target = openPoint(x2, y2, z2);
        finalTarget = target;
        Path pathentity = addToPath(entity, start, target);
        //Profiler.endSection();
        return pathentity;
    }

    private Path addToPath(IPathfindable entity, PathNode start, PathNode target)
    {
        start.totalPathDistance = 0.0F;
        start.distanceToNext = start.distanceTo(target);
        start.distanceToTarget = start.distanceToNext;
        path.clearPath();
        path.addPoint(start);
        PathNode previousPoint = start;
        //float mostOptimisticEstimate = Float.MAX_VALUE;
        //int noImprovement = 0;
        //int worstNoImprovement = 0;
        //int opened = 0;
        int loops = 0;
        //int poll = 0;
        //long time = System.currentTimeMillis();
        //long elapsed = 0;
        while(!path.isPathEmpty()) 
        {
        	/*if(++poll == 500)
        	{
        		elapsed = System.currentTimeMillis() - time;
        		poll = 0;
        	}*/
        	
        	if(++loops > nodeLimit) // Cut-off for breaking path into smaller chunks
        	{
        		//System.out.println(loops + " n: " + opened + " i: " + worstNoImprovement + " t: " + elapsed + " " + entity);
        		return createEntityPath(start, previousPoint);
        	}
            PathNode examiningPoint = path.dequeue();
            if(examiningPoint.equals(target))
            {
            	//System.out.println(loops + " success, n: " + opened + " i: " + worstNoImprovement + " t: " + elapsed + " " + entity);
                return createEntityPath(start, target);
            }
            if(examiningPoint.distanceTo(target) < previousPoint.distanceTo(target))
            {
            	previousPoint = examiningPoint;
            }
            examiningPoint.isFirst = true;
            //Profiler.startSection("Find adjacent nodes");
            int i = findPathOptions(entity, examiningPoint, target);
            //opened += i;
            //Profiler.endSection();
            //Profiler.startSection("Evaluate nodes");
            int j = 0;
            while(j < i) 
            {
                PathNode newPoint = pathOptions[j];
                //ModLoader.getMinecraftInstance().theWorld.spawnParticle("heart", newPoint.xCoord + 0.2, newPoint.yCoord + 0.2, newPoint.zCoord + 0.2, newPoint.xCoord + 0.5, newPoint.yCoord + 0.5, newPoint.zCoord + 0.5);
                //Profiler.startSection("Find cost to node");
                float actualCost = examiningPoint.totalPathDistance + entity.getBlockPathCost(examiningPoint, newPoint, worldMap);
                //Profiler.endSection();
                if(!newPoint.isAssigned() || actualCost < newPoint.totalPathDistance)
                {
                    newPoint.previous = examiningPoint;
                    newPoint.totalPathDistance = actualCost;
                    //newPoint.distanceToNext = newPoint.distanceTo(target);
                    newPoint.distanceToNext = estimateDistance(newPoint, target);
                    /*if(newPoint.distanceToNext < mostOptimisticEstimate)
                    {
                    	mostOptimisticEstimate = newPoint.distanceToNext;
                    	if(noImprovement > worstNoImprovement)
                    		worstNoImprovement = noImprovement;
                    	
                    	noImprovement = 0;
                    }
                    else
                    {
                    	noImprovement++;
                    }*/
                    
                    if(newPoint.isAssigned())
                    {
                        path.changeDistance(newPoint, newPoint.totalPathDistance + newPoint.distanceToNext);
                    } else
                    {
                        newPoint.distanceToTarget = newPoint.totalPathDistance + newPoint.distanceToNext;
                        path.addPoint(newPoint);
                    }
                }
                j++;
            }
            //Profiler.endSection();
        }
        if(previousPoint == start)
            return null;
        else
            return createEntityPath(start, previousPoint);
    }
    
    public void addNode(int x, int y, int z, PathAction action)
    {
    	PathNode node = openPoint(x, y, z, action);
    	if(node != null && !node.isFirst && node.distanceTo(finalTarget) < searchRange)
    		pathOptions[pathsIndex++] = node;
    }
    
    private float estimateDistance(PathNode start, PathNode target)
    {
    	return Math.abs(target.xCoord - start.xCoord) + Math.abs(target.yCoord - start.yCoord) + Math.abs(target.zCoord - start.zCoord) * 1.01F;
    }
    
    protected PathNode openPoint(int x, int y, int z)
    {
    	return openPoint(x, y, z, PathAction.NONE);
    }
    
    protected PathNode openPoint(int x, int y, int z, PathAction action)
    {
        int hash = PathNode.makeHash(x, y, z, action);
        PathNode pathpoint = (PathNode)pointMap.lookup(hash);
        if(pathpoint == null)
        {
            pathpoint = new PathNode(x, y, z, action);
            pointMap.addKey(hash, pathpoint);
        }
        
        if(pathpoint.xCoord != x || pathpoint.yCoord != y || pathpoint.zCoord != z || pathpoint.action != action)
        	System.out.println("PathNode collision");
        
        return pathpoint;
    }
    
    private int findPathOptions(IPathfindable entity, PathNode pathpoint, PathNode target)
    {
    	pathsIndex = 0;
    	entity.getPathOptionsFromNode(worldMap, pathpoint, this);
    	return pathsIndex;
    	
    	
    	
    	
    	/*    	
    	if(worldMap.getBlockMaterial(pathpoint.xCoord, pathpoint.yCoord, pathpoint.zCoord).isLiquid())
    	{
    		PathNode point = getSwimPoint(entity, pathpoint.xCoord, pathpoint.yCoord + 1, pathpoint.zCoord, entitySize);
	    	if(point != null && !point.isFirst && point.distanceTo(target) < f)
	        {
	           pathOptions[pathsIndex++] = point;
	        }
    	}
    	
        return pathsIndex;*/
    }

    
    /*private PathNode getSwimPoint(EntityIMWaveAttacker entity, int i, int j, int k, PathNode entitySize)
    {
    	if(worldMap.getBlockMaterial(i, j, k).isSolid() && entity.getDestructiveness() < 2)
    	{
    		return null;
    	}
    	else if(getCollide(entity, i, j, k, entitySize) != -2)
        {
    		return openPoint(i, j, k);
        }
    	return null;
    }
    

    //Return 1 if clear, 0 if solid
    /*private int getCollide(EntityIMWaveAttacker entity, int i, int j, int k, PathNode entitySize)
    {
        for(int l = i; l < i + entitySize.xCoord; l++)
        {
            for(int i1 = j; i1 < j + entitySize.yCoord; i1++)
            {
                for(int j1 = k; j1 < k + entitySize.zCoord; j1++)
                {
                    int k1 = worldMap.getBlockId(l, i1, j1);
                    if(k1 <= 0)
                    {
                        continue;
                    }
                    if(entity.avoidsBlock(k1))
                    {
                    	return -2;
                    }
                    if(entity.isBlockDestructible(k1))
                    {
                    	return 0;
                    }
                    if(k1 == Block.doorSteel.blockID || k1 == Block.doorWood.blockID)
                    {
                        int l1 = worldMap.getBlockMetadata(l, i1, j1);
                        if(!((BlockDoor)Block.doorWood).func_48213_h(worldMap, l, i1, j1))
                        {
                            return 0;
                        }
                        continue;
                    }
                }
            }
        }

        return 1;
    }*/

    private Path createEntityPath(PathNode pathpoint, PathNode pathpoint1)
    {
        int i = 1;
        for(PathNode pathpoint2 = pathpoint1; pathpoint2.previous != null; pathpoint2 = pathpoint2.previous)
        {
            i++;
        }

        PathNode apathpoint[] = new PathNode[i];
        PathNode pathpoint3 = pathpoint1;
        for(apathpoint[--i] = pathpoint3; pathpoint3.previous != null; apathpoint[--i] = pathpoint3)
        {
            pathpoint3 = pathpoint3.previous;
            //ModLoader.getMinecraftInstance().theWorld.spawnParticle("heart", pathpoint3.xCoord + 0.2, pathpoint3.yCoord + 0.2, pathpoint3.zCoord + 0.2, pathpoint3.xCoord + 0.5, pathpoint3.yCoord + 0.5, pathpoint3.zCoord + 0.5);
        }
        
        

        return new Path(apathpoint, finalTarget);
    }
}
