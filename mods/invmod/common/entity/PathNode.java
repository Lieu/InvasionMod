package mods.invmod.common.entity;

import mods.invmod.common.util.IPosition;
import net.minecraft.util.MathHelper;

public class PathNode implements IPosition
{
    public final int xCoord;
    public final int yCoord;
    public final int zCoord;
    public final PathAction action;
    private final int hash;
    int index;
    float totalPathDistance;
    float distanceToNext;
    float distanceToTarget;
    PathNode previous;
    public boolean isFirst;
    
    public PathNode(int i, int j, int k)
    {
        this(i, j, k, PathAction.NONE);
    }

    public PathNode(int i, int j, int k, PathAction pathAction)
    {
        index = -1;
        isFirst = false;
        xCoord = i;
        yCoord = j;
        zCoord = k;
        action = pathAction;
        hash = makeHash(i, j, k, action);
    }

    /**
     * Returns a hash of a coordinate and action, unique per 256 blocks per axis (lower order bits) and action.
     */
    public static int makeHash(int x, int y, int z, PathAction action)
    {
    	return  y & 0xFF | (x & 0xFF) << 8 | (z & 0xFF) << 16 | (action.ordinal() & 0xFF) << 24;
    }

    public float distanceTo(PathNode pathpoint)
    {
        float f = pathpoint.xCoord - xCoord;
        float f1 = pathpoint.yCoord - yCoord;
        float f2 = pathpoint.zCoord - zCoord;
        return MathHelper.sqrt_float(f * f + f1 * f1 + f2 * f2);
    }
    
    public float distanceTo(float x, float y, float z)
    {
        float f = x - xCoord;
        float f1 = y - yCoord;
        float f2 = z - zCoord;
        return MathHelper.sqrt_float(f * f + f1 * f1 + f2 * f2);
    }

    @Override
	public boolean equals(Object obj)
    {
        if (obj instanceof PathNode)
        {
        	PathNode node = (PathNode)obj;
            return hash == node.hash && xCoord == node.xCoord && yCoord == node.yCoord && zCoord == node.zCoord && node.action == action;
        }
        else
        {
            return false;
        }
    }
    
    public boolean equals(int x, int y, int z)
    {
    	return xCoord == x && yCoord == y && zCoord == z;
    }

    @Override
	public int hashCode()
    {
        return hash;
    }

    public boolean isAssigned()
    {
        return index >= 0;
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
	public String toString()
    {
        return xCoord + ", " + yCoord + ", " + zCoord + ", " + action;
    }
}
