package mods.invmod.common.util;

import java.util.Comparator;

/**
 * Compares entities to a single point, in order of farthest to closest to that point
 */
public class ComparatorDistanceFrom implements Comparator<IPosition>
{
	public ComparatorDistanceFrom(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public int compare(IPosition pos1, IPosition pos2)
	{
		double d1 = (x - pos1.getXCoord())*(x - pos1.getXCoord()) + (y - pos1.getYCoord())*(y - pos1.getYCoord()) + (z - pos1.getZCoord())*(z - pos1.getZCoord());
		double d2 = (x - pos2.getXCoord())*(x - pos2.getXCoord()) + (y - pos2.getYCoord())*(y - pos2.getYCoord()) + (z - pos2.getZCoord())*(z - pos2.getZCoord());
		if(d1 > d2)
			return -1;
		else if(d1 < d2)
			return 1;
		else
			return 0;
	}
	
	private double x;
	private double y;
	private double z;
}
