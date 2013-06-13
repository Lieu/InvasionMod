package mods.invmod.common.entity;

import mods.invmod.common.INotifyTask;

public interface ITerrainDig
{
	public boolean askRemoveBlock(int x, int y, int z, INotifyTask asker, float costMultiplier);
	
	public boolean askClearPosition(int x, int y, int z, INotifyTask asker, float costMultiplier);
}
