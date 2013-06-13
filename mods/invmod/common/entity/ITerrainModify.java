package mods.invmod.common.entity;

import mods.invmod.common.INotifyTask;

public interface ITerrainModify
{
	public boolean isReadyForTask(INotifyTask onFinished);
	
	public boolean requestTask(ModifyBlockEntry[] entries, INotifyTask onFinished, INotifyTask onBlockChange);
	
	public ModifyBlockEntry getLastBlockModified();
}
