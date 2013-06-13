package mods.invmod.common.util;

import java.util.Comparator;

import net.minecraft.entity.Entity;

/**
 * Compares positions to a single point, in order of farthest to closest to that point
 */
public class ComparatorEntityDistanceFrom implements Comparator<Entity>
{
	public ComparatorEntityDistanceFrom(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public int compare(Entity entity1, Entity entity2)
	{
		double d1 = (x - entity1.posX)*(x - entity1.posX) + (y - entity1.posY)*(y - entity1.posY) + (z - entity1.posZ)*(z - entity1.posZ);
		double d2 = (x - entity2.posX)*(x - entity2.posX) + (y - entity2.posY)*(y - entity2.posY) + (z - entity2.posZ)*(z - entity2.posZ);
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