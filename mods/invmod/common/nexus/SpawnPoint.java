
package mods.invmod.common.nexus;

import mods.invmod.common.util.IPolarAngle;
import mods.invmod.common.util.IPosition;


/**
 * Represents a single location of a certain type in a world, along
 * with its polar coordinate about an unspecified origin. SpawnPoint
 * is sortable by its polar coordinate.
 * 
 * @author Lieu
 */
public class SpawnPoint implements IPosition, IPolarAngle, Comparable<IPolarAngle>
{	
	public SpawnPoint(int x, int y, int z, int angle, SpawnType type)
	{
		xCoord = x;
		yCoord = y;
		zCoord = z;
		spawnAngle = angle;
		spawnType = type;
	}
	
	@Override
	public int getXCoord()
	{
		return xCoord;
	}
	
	@Override
	public int getYCoord()
	{
		return yCoord;
	}
	
	@Override
	public int getZCoord()
	{
		return zCoord;
	}
	
	@Override
	public int getAngle()
	{
		return spawnAngle;
	}
	
	public SpawnType getType()
	{
		return spawnType;
	}
	
	@Override
	public int compareTo(IPolarAngle polarAngle)
	{
		if(spawnAngle < polarAngle.getAngle())
		{
			return -1;
		}
		else if(spawnAngle > polarAngle.getAngle())
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
	
	@Override
	public String toString()
	{
		return "Spawn#" + spawnType + "#" + xCoord + "," + yCoord + "," + zCoord + "#" + spawnAngle;
	}
	
	private int xCoord;
	private int yCoord;
	private int zCoord;
	private int spawnAngle;
	private SpawnType spawnType;
	
}