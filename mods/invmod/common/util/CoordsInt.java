package mods.invmod.common.util;

public class CoordsInt implements IPosition
{
	public CoordsInt(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public int getXCoord()
	{
		return x;
	}

	@Override
	public int getYCoord()
	{
		return y;
	}

	@Override
	public int getZCoord()
	{
		return z;
	}
	
	/**
	 * The offsets, by index, to refer to adjacent coordinates.
	 * Order: +X, -X, +Z, -Z
	 */
	public final static int[] offsetAdjX = { 1, -1,  0,  0 };
	public final static int[] offsetAdjZ = { 0,  0,  1, -1 };
	
	/**
	 * The offsets, by index, to refer to adjacent coordinates of a 2x2 square.
	 * Uses positive offset from origin scheme for 2x2 centre.
	 */
	public final static int[] offsetAdj2X = { 2,  2, -1, -1,  1,  0,  0,  1};
	public final static int[] offsetAdj2Z = { 0,  1,  1,  0,  2,  2, -1, -1};
	
	/**
	 * The offsets, by index, refering to coordinates in a square around a point.
	 */
	public final static int[] offsetRing1X = { 1,  0, -1, -1, -1,  0,  1,  1 };
	public final static int[] offsetRing1Z = { 1,  1,  1,  0, -1, -1, -1,  0 };
	
	private int x;
	private int y;
	private int z;
}
