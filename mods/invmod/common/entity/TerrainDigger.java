package mods.invmod.common.entity;

import mods.invmod.common.INotifyTask;
import mods.invmod.common.util.IPosition;
import net.minecraft.block.Block;

public class TerrainDigger implements ITerrainDig, INotifyTask
{
	public TerrainDigger(ICanDig digger, ITerrainModify modifier, float digRate)
	{
		this.digger = digger;
		this.modifier = modifier;
		this.digRate = digRate;
	}
	
	public void setDigRate(float digRate)
	{
		this.digRate = digRate;
	}
	
	public float getDigRate()
	{
		return digRate;
	}
	
	@Override
	public boolean askClearPosition(int x, int y, int z, INotifyTask onFinished, float costMultiplier)
	{
		IPosition[] removals = digger.getBlockRemovalOrder(x, y, z);
		ModifyBlockEntry[] removalEntries = new ModifyBlockEntry[removals.length];
		int entries = 0;
		for(int i = 0; i < removals.length; i++)
		{
			int id = digger.getTerrain().getBlockId(removals[i].getXCoord(), removals[i].getYCoord(), removals[i].getZCoord());
			if(id == 0 || Block.blocksList[id].getBlocksMovement(digger.getTerrain(), removals[i].getXCoord(), removals[i].getYCoord(), removals[i].getZCoord()))
			{
				continue;
			}
			else if(!digger.canClearBlock(removals[i].getXCoord(), removals[i].getYCoord(), removals[i].getZCoord()))
			{
				return false;
			}
			
			removalEntries[entries++] = new ModifyBlockEntry(removals[i].getXCoord(), removals[i].getYCoord(), removals[i].getZCoord(), 0, (int)(costMultiplier * digger.getBlockRemovalCost(removals[i].getXCoord(), removals[i].getYCoord(), removals[i].getZCoord()) / digRate));
		}
		ModifyBlockEntry[] finalEntries = new ModifyBlockEntry[entries];
		System.arraycopy(removalEntries, 0, finalEntries, 0, entries);
		return modifier.requestTask(finalEntries, onFinished, this);
	}
	
	@Override
	public boolean askRemoveBlock(int x, int y, int z, INotifyTask onFinished, float costMultiplier)
	{
		if(!digger.canClearBlock(x, y, z))
			return false;
		
		ModifyBlockEntry[] removalEntries = new ModifyBlockEntry[] { new ModifyBlockEntry(x, y, z, 0, (int)(costMultiplier * digger.getBlockRemovalCost(x, y, z) / digRate)) };
		return modifier.requestTask(removalEntries, onFinished, this);
	}
	
	@Override
	public void notifyTask(int result)
	{
		if(result == 0)
		{
			ModifyBlockEntry entry = modifier.getLastBlockModified();
			digger.onBlockRemoved(entry.getXCoord(), entry.getYCoord(), entry.getZCoord(), entry.getOldBlockId());
		}
	}
	
	private ICanDig digger;
	private ITerrainModify modifier;
	private float digRate;
}
