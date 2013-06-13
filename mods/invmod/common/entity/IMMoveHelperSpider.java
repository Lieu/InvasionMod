package mods.invmod.common.entity;

import mods.invmod.common.util.CoordsInt;
import net.minecraft.util.MathHelper;

public class IMMoveHelperSpider extends IMMoveHelper
{
	public IMMoveHelperSpider(EntityIMLiving par1EntityLiving)
    {
    	super(par1EntityLiving);
    }
	
	@Override
	protected int getClimbFace(double x, double y, double z)
    {
		// Determine which integer coordinates this 2x2 mob collides with
		int mobX = MathHelper.floor_double(x - entity.width / 2);
        int mobY = MathHelper.floor_double(y);
        int mobZ = MathHelper.floor_double(z - entity.width / 2);
		
		// Find a side that has a solid block on it to climb, but prioritise the
		// side under the exit ledge. 0-1, 2-3, 4-5, 6-7 map onto +X,-X,+Z,-Z.
		int index = 0;
		Path path = entity.getNavigatorNew().getPath();
		if(path != null && !path.isFinished())
		{
			PathNode currentPoint = path.getPathPointFromIndex(path.getCurrentPathIndex());
			int pathLength = path.getCurrentPathLength();
			for(int i = path.getCurrentPathIndex(); i < pathLength; i++)
			{
				PathNode point = path.getPathPointFromIndex(i);
				if(point.xCoord > currentPoint.xCoord)
				{
					break;
				}
				else if(point.xCoord < currentPoint.xCoord)
				{
					index = 2;
					break;
				}
				else if(point.zCoord > currentPoint.zCoord)
				{
					index = 4;
					break;
				}
				else if(point.zCoord < currentPoint.zCoord)
				{
					index = 6;
					break;
				}
			}
		}
		
		// Now check for the blocks
		for(int count = 0; count < 8; count++)
		{
			if(entity.worldObj.isBlockNormalCube(mobX + CoordsInt.offsetAdj2X[index], mobY, mobZ + CoordsInt.offsetAdj2Z[index]))
				return index / 2; // Map 0-7 onto 0-3, +X,-X,+Z,-Z
			
			if(++index > 7)
				index = 0;
		}
		
		return -1;
    }
}
