
package mods.invmod.common.nexus;


import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Random;

import mods.invmod.common.util.PolarAngle;
import net.minecraft.world.World;

/**
 * Container for SpawnPoint objects, representing a single pool of
 * spawn points.
 * 
 * @author Lieu
 */
public class SpawnPointContainer
{	
	/**
	 * Creates a new, empty container
	 */
	public SpawnPointContainer()
	{
		sorted = false;
		random = new Random();
		angleDesired = new PolarAngle(0);
		spawnPoints = new EnumMap<SpawnType, ArrayList<SpawnPoint>>(SpawnType.class);
		for(SpawnType type : SpawnType.values())
		{  
			spawnPoints.put(type, new ArrayList<SpawnPoint>());
		}
	}
	
	/**
	 * Adds a spawn point on the XZ plane, unique to the Y axis.
	 * The nearest Y value to the nexus is always preferred.
	 */
	public void addSpawnPointXZ(SpawnPoint spawnPoint)
	{
		boolean flag = false;
		ArrayList<SpawnPoint> spawnList = spawnPoints.get(spawnPoint.getType());
		for(int i = 0; i < spawnList.size(); i++)
		{
			SpawnPoint oldPoint = spawnList.get(i);
			if(oldPoint.getXCoord() == spawnPoint.getXCoord() && oldPoint.getZCoord() == spawnPoint.getZCoord())
			{
				if(oldPoint.getYCoord() > spawnPoint.getYCoord())
				{
					spawnList.set(i, spawnPoint);
				}
				flag = true;
				break;
			}
		}
		
		if(!flag)
		{
			spawnList.add(spawnPoint);
		}
		sorted = false;
	}
	
	/**
	 * Returns a random spawn point of the type specified.
	 */
	public SpawnPoint getRandomSpawnPoint(SpawnType spawnType)
	{
		ArrayList<SpawnPoint> spawnList = spawnPoints.get(spawnType);
		if(spawnList.size() == 0)
		{
			return null;
		}		
		return spawnList.get(random.nextInt(spawnList.size()));
	}
	
	/**
	 * Returns a random spawn point of the type specified, within the range of the angles
	 * given. Bounds are from -180 to 180 degrees.
	 */
	public SpawnPoint getRandomSpawnPoint(SpawnType spawnType, int minAngle, int maxAngle)
	{
		ArrayList<SpawnPoint> spawnList = spawnPoints.get(spawnType);
		if(spawnList.size() == 0)
		{
			return null;
		}		
		
		if(!sorted)
		{
			Collections.sort(spawnList);
			sorted = true;
		}
		
		angleDesired.setAngle(minAngle);
		int start = Collections.binarySearch(spawnList, angleDesired);
		if(start < 0)
			start = -start - 1;
			
		angleDesired.setAngle(maxAngle);
		int end = Collections.binarySearch(spawnList, angleDesired);
		if(end < 0)
			end = -end - 1;
				
		if(end > start)
			return spawnList.get(start + random.nextInt(end - start));
		
		if(start > end && end > 0)
		{
			int r = start + random.nextInt(spawnList.size() + end - start);
			if(r >= spawnList.size())
				r -= spawnList.size();
			
			return spawnList.get(r);
		}
		return null;
	}
	
	/**
	 * Returns number of spawn points in this container of the specified type
	 */
	public int getNumberOfSpawnPoints(SpawnType type)
	{
		return spawnPoints.get(SpawnType.HUMANOID).size();
	}
	
	/**
	 * Returns number of spawn points in this container of the specified type
	 * within the specified range
	 */
	public int getNumberOfSpawnPoints(SpawnType spawnType, int minAngle, int maxAngle)
	{
		ArrayList<SpawnPoint> spawnList = spawnPoints.get(spawnType);
		if(spawnList.size() == 0 || maxAngle - minAngle >= 360)
		{
			return spawnList.size();
		}		
		
		if(!sorted)
		{
			Collections.sort(spawnList);
			sorted = true;
		}
		
		angleDesired.setAngle(minAngle);
		int start = Collections.binarySearch(spawnList, angleDesired);
		if(start < 0)
			start = -start - 1;
			
		angleDesired.setAngle(maxAngle);
		int end = Collections.binarySearch(spawnList, angleDesired);
		if(end < 0)
			end = -end - 1;
		
		if(end > start)
			return end - start;
		
		if(start > end && end > 0)
			return end + spawnList.size() - start;
		
		return 0;
	}
	
	/**
	 * Creates visual indicators in the game world for where each spawn
	 * point is in this container.
	 */
	public void pointDisplayTest(int blockID, World world)
	{
		ArrayList<SpawnPoint> points = spawnPoints.get(SpawnType.HUMANOID);
		SpawnPoint point = null;
		for(int i = 0; i < points.size(); i++)
		{
			point = points.get(i);
			world.setBlock(point.getXCoord(), point.getYCoord(), point.getZCoord(), blockID);
		}
	}
	
	private EnumMap<SpawnType, ArrayList<SpawnPoint>> spawnPoints;
	private boolean sorted;
	private Random random;
	private PolarAngle angleDesired;
}