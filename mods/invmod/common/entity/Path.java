package mods.invmod.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

public class Path
{
    protected final PathNode points[];
    private PathNode intendedTarget;
    private int pathLength;
    private int pathIndex;
	private float totalCost;

    public Path(PathNode apathpoint[])
    {
    	points = apathpoint;
        pathLength = apathpoint.length;
    	if(apathpoint.length > 0)
    	{
    		intendedTarget = apathpoint[apathpoint.length - 1];
    	}
    }
    
    public Path(PathNode apathpoint[], PathNode intendedTarget)
    {
        points = apathpoint;
        pathLength = apathpoint.length;
        this.intendedTarget = intendedTarget;
    }
    
    public float getTotalPathCost()
    {
    	return points[pathLength - 1].totalPathDistance;
    }

    public void incrementPathIndex()
    {
        pathIndex++;
    }

    public boolean isFinished()
    {
        return pathIndex >= points.length;
    }
    
    public PathNode getFinalPathPoint()
    {
        if (pathLength > 0)
        {
            return points[pathLength - 1];
        }
        else
        {
            return null;
        }
    }
    
    public PathNode getPathPointFromIndex(int par1)
    {
        return points[par1];
    }

    public int getCurrentPathLength()
    {
        return pathLength;
    }
    
    public void setCurrentPathLength(int par1)
    {
        pathLength = par1;
    }

    public int getCurrentPathIndex()
    {
        return pathIndex;
    }

    public void setCurrentPathIndex(int par1)
    {
        pathIndex = par1;
    }
    
    public PathNode getIntendedTarget()
    {
    	return intendedTarget;
    }
    
    public Vec3 getPositionAtIndex(Entity entity, int index)
    {
        double d = points[index].xCoord + (int)(entity.width + 1.0F) * 0.5D;
        double d1 = points[index].yCoord;
        double d2 = points[index].zCoord + (int)(entity.width + 1.0F) * 0.5D;
        return Vec3.createVectorHelper(d, d1, d2);
    }
    
    public Vec3 getCurrentNodeVec3d(Entity entity)
    {
        return getPositionAtIndex(entity, pathIndex);
    }
    
    public Vec3 destination()
    {
    	return Vec3.createVectorHelper(points[points.length - 1].xCoord, points[points.length - 1].yCoord, points[points.length - 1].zCoord);
    }
    
    public boolean equalsPath(Path par1PathEntity)
    {
        if (par1PathEntity == null)
        {
            return false;
        }

        if (par1PathEntity.points.length != points.length)
        {
            return false;
        }

        for (int i = 0; i < points.length; i++)
        {
            if (points[i].xCoord != par1PathEntity.points[i].xCoord || points[i].yCoord != par1PathEntity.points[i].yCoord || points[i].zCoord != par1PathEntity.points[i].zCoord)
            {
                return false;
            }
        }

        return true;
    }

    public boolean isDestinationSame(Vec3 par1Vec3D)
    {
        PathNode pathpoint = getFinalPathPoint();

        if (pathpoint == null)
        {
            return false;
        }
        else
        {
            return pathpoint.xCoord == (int)par1Vec3D.xCoord && pathpoint.zCoord == (int)par1Vec3D.zCoord;
        }
    }
}
