package mods.invmod.common.entity;


import java.util.ArrayList;
import java.util.List;

import mods.invmod.common.IBlockAccessExtended;
import mods.invmod.common.IPathfindable;
import mods.invmod.common.TerrainDataLayer;
import mods.invmod.common.nexus.INexusAccess;
import mods.invmod.common.util.CoordsInt;
import mods.invmod.common.util.Distance;
import mods.invmod.common.util.IPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;

/**
 * Represents a collective AI of attackers, coordinating their choices and fulfilling
 * more demanding analysis for open use, such as terrain analysis.
 * 
 * @author Lieu
 */
public class AttackerAI
{
	private INexusAccess nexus;
	private IPathSource pathSource;
	private IntHashMap entityDensityData;
	private List<Scaffold> scaffolds;
	private int scaffoldLimit;
	private int minDistanceBetweenScaffolds;
	private int nextScaffoldCalcTimer;
	private int updateScaffoldTimer;
	private int nextEntityDensityUpdate;
	
	public AttackerAI(INexusAccess nexus)
	{
		this.nexus = nexus;
		pathSource = new PathCreator();
		pathSource.setSearchDepth(8500);
		pathSource.setQuickFailDepth(8500);
		entityDensityData = new IntHashMap();
		scaffolds = new ArrayList<Scaffold>();
	}
	
	public void update()
	{
		nextScaffoldCalcTimer--;
		if(--updateScaffoldTimer <= 0)
		{
			updateScaffoldTimer = 40;
			updateScaffolds();
			
			scaffoldLimit = 2 + nexus.getCurrentWave() / 2;
			minDistanceBetweenScaffolds = 90 / (nexus.getCurrentWave() + 10);
		}
		
		if(--nextEntityDensityUpdate <= 0)
		{
			nextEntityDensityUpdate = 20;
			updateDensityData();
		}
	}
	
	public IBlockAccessExtended wrapEntityData(IBlockAccess terrainMap)
	{
		TerrainDataLayer newTerrain = new TerrainDataLayer(terrainMap);
		newTerrain.setAllData(entityDensityData);
		return newTerrain;
	}
	
	/**
	 * Returns the current recommended scaffold closeness limit. Note that scaffolds can still be
	 * closer when generated in groups as part of a single path, or otherwise chosen by pathfinding.
	 */
	public int getMinDistanceBetweenScaffolds()
	{
		return minDistanceBetweenScaffolds;
	}
	
	/**
	 * Returns the current active list of scaffolds associated with this nexus
	 */
	public List<Scaffold> getScaffolds()
	{
		return scaffolds;
	}
	
	/*public boolean askGenerateScaffolds()
	{
		return askGenerateScaffolds(new EntityIMPigEngy(nexus.getWorld(), nexus));
	}*/
	
	/**
	 * Asks this AI to create scaffold positions between the entity specified and
	 * where the AI is associated with (or chooses). The entity's pathfinding ability
	 * is used as a base for the AI's routes.
	 */
	public boolean askGenerateScaffolds(EntityIMLiving entity)
	{
		if(nextScaffoldCalcTimer > 0 || scaffolds.size() > scaffoldLimit)
			return false;
		
		nextScaffoldCalcTimer = 200;
		List<Scaffold> newScaffolds = findMinScaffolds(entity, MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY), MathHelper.floor_double(entity.posZ));
		if(newScaffolds != null && newScaffolds.size() > 0)
		{
			addNewScaffolds(newScaffolds);
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Tries to find 1 scaffold or a minimum number of scaffolds needed to reach the associated
	 * nexus from the coordinates given, with the specified pathfindable object. This method is
	 * not guaranteed to find optimal solutions but usually will for typical terrain. Scaffolds
	 * found are returned.
	 */
	public List<Scaffold> findMinScaffolds(IPathfindable pather, int x, int y, int z)
	{
		Scaffold scaffold = new Scaffold(nexus); // Existing scaffolds are visible to this object, but the terrainMap is explicitly specified
		scaffold.setPathfindBase(pather);
		Path basePath = createPath(scaffold, x, y, z, nexus.getXCoord(), nexus.getYCoord(), nexus.getZCoord(), 12);
		if(basePath == null)
			return new ArrayList<Scaffold>();
		
		List<Scaffold> scaffoldPositions = extractScaffolds(basePath);
		if(scaffoldPositions.size() > 1)
		{
			// First assume there is one or less required scaffolds, and find which one is best in path cost terms.
			// As soon as there are multiple required scaffolds, a combinatorial layer gets added to this problem,
			// so change the approach to another heuristic if that is the case.
			float lowestCost = Float.POSITIVE_INFINITY;
			int lowestCostIndex = -1;
			for(int i = 0; i < scaffoldPositions.size(); i++)
			{
				TerrainDataLayer terrainMap = new TerrainDataLayer(getChunkCache(x, y, z, nexus.getXCoord(), nexus.getYCoord(), nexus.getZCoord(), 12));
				Scaffold s = scaffoldPositions.get(i);
				terrainMap.setData(s.getXCoord(), s.getYCoord(), s.getZCoord(), 200000);
				Path path = createPath(pather, x, y, z, nexus.getXCoord(), nexus.getYCoord(), nexus.getZCoord(), terrainMap);
				if(path.getTotalPathCost() < lowestCost && path.getFinalPathPoint().equals(nexus.getXCoord(), nexus.getYCoord(), nexus.getZCoord()))
					lowestCostIndex = i;
			}
			
			// If there is a valid index, it is possible to use a single scaffold as a minimum or "best" location. Return it.
			if(lowestCostIndex >= 0)
			{
				List<Scaffold> s = new ArrayList<Scaffold>();
				s.add(scaffoldPositions.get(lowestCostIndex));
				return s;
			}
			else // Means with only 1 scaffold, entity could not reach nexus
			{
				// Requires at least 2 scaffolds, so now take a reverse-approach and remove scaffolds individually as a
				// test to see if they are necessary. This is a heuristic and not a solution to that question.
				List<Scaffold> costDif = new ArrayList<Scaffold>(scaffoldPositions.size());
				for(int i = 0; i < scaffoldPositions.size(); i++)
				{
					TerrainDataLayer terrainMap = new TerrainDataLayer(getChunkCache(x, y, z, nexus.getXCoord(), nexus.getYCoord(), nexus.getZCoord(), 12));
					Scaffold s = scaffoldPositions.get(i);
					for(int j = 0; j < scaffoldPositions.size(); j++)
					{
						if(j == i)
							continue;
						
						terrainMap.setData(s.getXCoord(), s.getYCoord(), s.getZCoord(), 200000);
					}
					Path path = createPath(pather, x, y, z, nexus.getXCoord(), nexus.getYCoord(), nexus.getZCoord(), terrainMap);
					
					if(!path.getFinalPathPoint().equals(nexus.getXCoord(), nexus.getYCoord(), nexus.getZCoord()))
					{
						costDif.add(s);
					}
				}
				
				// Return the scaffolds that were deemed "required", ie along this path only
				return costDif;
			}
		}
		else if(scaffoldPositions.size() == 1)
		{
			return scaffoldPositions;
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Adds scaffold data to the given extended IBlockAccess. Data is for templated
	 * scaffold main sections that do or don't exist physically.
	 * 
	 * @see TerrainDataLayer.EXT_DATA_SCAFFOLD_METAPOSITION
	 */
	public void addScaffoldDataTo(IBlockAccessExtended terrainMap)
	{
		for(Scaffold scaffold : scaffolds)
		{
			for(int i = 0; i < scaffold.getTargetHeight(); i++)
			{
				int data = terrainMap.getLayeredData(scaffold.getXCoord(), scaffold.getYCoord() + i, scaffold.getZCoord());
				terrainMap.setData(scaffold.getXCoord(), scaffold.getYCoord() + i, scaffold.getZCoord(), data | TerrainDataLayer.EXT_DATA_SCAFFOLD_METAPOSITION);
			}
		}
	}
	
	/**
	 * Returns scaffold intersecting with position if it exists.
	 */
	public Scaffold getScaffoldAt(IPosition pos)
	{
		return getScaffoldAt(pos.getXCoord(), pos.getYCoord(), pos.getZCoord());
	}
	
	/**
	 * Returns scaffold intersecting with coordinates if it exists.
	 */
	public Scaffold getScaffoldAt(int x, int y, int z)
	{
		for(Scaffold scaffold : scaffolds)
		{
			if(scaffold.getXCoord() == x && scaffold.getZCoord() == z)
			{
				if(scaffold.getYCoord() <= y && scaffold.getYCoord() + scaffold.getTargetHeight() >= y)
					return scaffold;
			}
		}
		return null;
	}
	
	public void onResume()
	{
		for(Scaffold scaffold : scaffolds)
		{
			scaffold.forceStatusUpdate();
		}
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound)
    {
		NBTTagList nbtScaffoldList = nbttagcompound.getTagList("scaffolds");
		for(int i = 0; i < nbtScaffoldList.tagCount(); i++)
		{
			Scaffold scaffold = new Scaffold(nexus);
			scaffold.readFromNBT((NBTTagCompound)nbtScaffoldList.tagAt(i)); // Raw type; have to cast
			scaffolds.add(scaffold);
		}
    }
	
	public void writeToNBT(NBTTagCompound nbttagcompound)
    {
		NBTTagList nbttaglist = new NBTTagList();
        for(Scaffold scaffold : scaffolds)
        {
        	NBTTagCompound nbtscaffold = new NBTTagCompound();
        	scaffold.writeToNBT(nbtscaffold);
        	nbttaglist.appendTag(nbtscaffold);
        }
        nbttagcompound.setTag("scaffolds", nbttaglist);
    }
	
	private Path createPath(IPathfindable pather, int x1, int y1, int z1, int x2, int y2, int z2, IBlockAccess terrainMap)
    {
		return pathSource.createPath(pather, x1, y1, z1, x2, y2, z2, 12 + (float)Distance.distanceBetween(x1, y1, z1, x2, y2, z2), terrainMap);
        //return (new PathfinderIM(terrainMap, 8500)).createEntityPathTo(pather, x1, y1, z1, x2, y2, z2, 12 + (float)Distance.distanceBetween(x1, y1, z1, x2, y2, z2));
    }
	
	/**
	 * Tries to create a path with the given pathable object. Includes existing scaffold terrain data.
	 */
	private Path createPath(IPathfindable pather, int x, int y, int z, int x2, int y2, int z2, float axisExpand)
    {
		TerrainDataLayer terrainMap = new TerrainDataLayer(getChunkCache(x, y, z, x2, y2, z2, axisExpand));
		addScaffoldDataTo(terrainMap);
		return createPath(pather, x, y, z, x2, y2, z2, terrainMap);
    }
	
	/**
	 * Creates a ChunkCache as a box between the two coordinates specified, expanding the box outwards by an amount.
	 */
	private ChunkCache getChunkCache(int x1, int y1, int z1, int x2, int y2, int z2, float axisExpand)
    {
    	int d = (int)axisExpand;
        
        // Calculate box coordinates for the terrain cache to pathfind with - c1 to c2.
        // This will take the box between the start and end points and expand it
        // by f on each axis. This ensures, in all likelyhood, the relevant terrain
        // is included with little excess.
        int cX1, cY1, cZ1, cX2, cY2, cZ2;
        if(x1 < x2)
        {
        	cX1 = x1 - d;
        	cX2 = x2 + d;
        }
        else
        {
        	cX2 = x1 + d;
        	cX1 = x2 - d;
        }
        if(y1 < y2)
        {
        	cY1 = y1 - d;
        	cY2 = y2 + d;
        }
        else
        {
        	cY2 = y1 + d;
        	cY1 = y2 - d;
        }
        if(z1 < z2)
        {
        	cZ1 = z1 - d;
        	cZ2 = z2 + d;
        }
        else
        {
        	cZ2 = z1 + d;
        	cZ1 = z2 - d;
        }
        return new ChunkCache(nexus.getWorld(), cX1, cY1, cZ1, cX2, cY2, cZ2, 0);
    }
	
	private List<Scaffold> extractScaffolds(Path path)
	{
		List<Scaffold> scaffoldPositions = new ArrayList<Scaffold>();
		boolean flag = false;
		int startHeight = 0;
		for(int i = 0; i < path.getCurrentPathLength(); i++)
		{
			PathNode node = path.getPathPointFromIndex(i);
			if(!flag)
			{
				if(node.action == PathAction.SCAFFOLD_UP)
				{
					flag = true;
					startHeight = node.getYCoord() - 1;
				}
			}
			else
			{
				if(node.action != PathAction.SCAFFOLD_UP)
				{
					Scaffold scaffold = new Scaffold(node.previous.getXCoord(), startHeight, node.previous.getZCoord(), node.getYCoord() - startHeight, nexus);
					orientScaffold(scaffold, nexus.getWorld());
					scaffold.setInitialIntegrity();
					scaffoldPositions.add(scaffold);
					flag = false;
				}
			}
		}
		return scaffoldPositions;
	}
	
	/**
	 * Checks on which side of a scaffold appears to have more blocks to attach to and orients the scaffold to it.
	 */
	private void orientScaffold(Scaffold scaffold, IBlockAccess terrainMap)
	{
		int mostBlocks = 0;
		int highestDirectionIndex = 0;
		for(int i = 0; i < 4; i++)
		{
			int blockCount = 0;
			for(int height = 0; height < scaffold.getYCoord(); height++)
			{
				if(terrainMap.isBlockNormalCube(scaffold.getXCoord() + CoordsInt.offsetAdjX[i], scaffold.getYCoord() + height, scaffold.getZCoord() + CoordsInt.offsetAdjZ[i]))
					blockCount++;
				
				if(terrainMap.isBlockNormalCube(scaffold.getXCoord() + CoordsInt.offsetAdjX[i]*2, scaffold.getYCoord() + height, scaffold.getZCoord() + CoordsInt.offsetAdjZ[i]*2))
					blockCount++;
			}
			
			if(blockCount > mostBlocks)
				highestDirectionIndex = i;
		}
		
		scaffold.setOrientation(highestDirectionIndex);
	}
	
	private void addNewScaffolds(List<Scaffold> newScaffolds)
	{
		// Find if any new scaffolds intersect with existing ones
		for(Scaffold newScaffold : newScaffolds)
		{
			for(Scaffold existingScaffold : scaffolds)
			{
				// Match scaffolds on XZ plane
				if(existingScaffold.getXCoord() == newScaffold.getXCoord() && existingScaffold.getZCoord() == newScaffold.getZCoord())
				{
					// Determine if they intersect vertically
					if(newScaffold.getYCoord() > existingScaffold.getYCoord())
					{
						if(newScaffold.getYCoord() < existingScaffold.getYCoord() + existingScaffold.getTargetHeight())
						{
							existingScaffold.setHeight(newScaffold.getYCoord() + newScaffold.getTargetHeight() - existingScaffold.getYCoord());
							break;
						}
					}
					else
					{
						if(newScaffold.getYCoord() + newScaffold.getTargetHeight() > existingScaffold.getYCoord())
						{
							existingScaffold.setPosition(newScaffold.getXCoord(), newScaffold.getYCoord(), newScaffold.getZCoord());
							existingScaffold.setHeight(existingScaffold.getYCoord() + existingScaffold.getTargetHeight() - newScaffold.getYCoord());
							break;
						}
					}
						
				}
			}
			scaffolds.add(newScaffold);
		}
	}
	
	private void updateScaffolds()
	{
		for(int i = 0; i < scaffolds.size(); i++)
		{
			Scaffold lol = scaffolds.get(i);
			nexus.getWorld().spawnParticle("heart", lol.getXCoord() + 0.2, lol.getYCoord() + 0.2, lol.getZCoord() + 0.2, lol.getXCoord() + 0.5, lol.getYCoord() + 0.5, lol.getZCoord() + 0.5);
			
			scaffolds.get(i).forceStatusUpdate();
			if(scaffolds.get(i).getPercentIntactCached() + 0.05F < 0.4F * scaffolds.get(i).getPercentCompletedCached())
				scaffolds.remove(i);
		}
	}
	
	private void updateDensityData()
	{
		entityDensityData.clearMap();
		List<EntityIMLiving> mobs = nexus.getMobList();
		for(EntityIMLiving mob : mobs)
		{
			int coordHash = PathNode.makeHash(mob.getXCoord(), mob.getYCoord(), mob.getZCoord(), PathAction.NONE);
			if(entityDensityData.containsItem(coordHash))
			{
				Integer value = (Integer)entityDensityData.lookup(coordHash); // Would include bit mask '& 7' for lowest 3 bits if operating on an unknown data layer
				if(value < 7)
				{
					entityDensityData.addKey(coordHash, value + 1);
				}
			}
			else
			{
				entityDensityData.addKey(coordHash, 1);
			}
		}
	}
}
