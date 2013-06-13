package mods.invmod.common.entity;

import mods.invmod.common.util.IPosition;

public class ModifyBlockEntry implements IPosition
{
	public ModifyBlockEntry(int x, int y, int z, int newBlockId)
	{
		this(x, y, z, newBlockId, 0, 0, -1);
	}
	
	public ModifyBlockEntry(int x, int y, int z, int newBlockId, int cost)
	{
		this(x, y, z, newBlockId, cost, 0, -1);
	}

	public ModifyBlockEntry(int x, int y, int z, int newBlockId, int cost, int newBlockMeta, int oldBlockId)
	{
		xCoord = x;
		yCoord = y;
		zCoord = z;
		this.newBlockId = newBlockId;
		this.cost = cost;
		this.newBlockMeta = newBlockMeta;
		this.oldBlockId = oldBlockId;
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
    
    public int getNewBlockId()
    {
    	return newBlockId;
    }
    
    public int getNewBlockMeta()
    {
    	return newBlockMeta;
    }
    
    public int getCost()
    {
    	return cost;
    }
    
    /**
     * Returns the block ID this entry seeks to replace, -1 if the ID is unknown.
     */
    public int getOldBlockId()
    {
    	return oldBlockId;
    }
    
    public void setOldBlockId(int id)
    {
    	oldBlockId = id;
    }
    
    private int xCoord;
    private int yCoord;
    private int zCoord;
    private int oldBlockId;
    private int newBlockId;
    private int newBlockMeta;
    private int cost;
}
