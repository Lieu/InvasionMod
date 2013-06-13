package mods.invmod.common.entity;

import mods.invmod.common.INotifyTask;
import mods.invmod.common.util.IPosition;

public interface ITerrainBuild
{
	public boolean askBuildScaffoldLayer(IPosition pos, INotifyTask asker);
	
	public boolean askBuildLadderTower(IPosition pos, int orientation, int layersToBuild, INotifyTask asker);
	
	public boolean askBuildLadder(IPosition pos, INotifyTask asker);
	
	public boolean askBuildBridge(IPosition pos, INotifyTask asker);
}
