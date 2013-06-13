package mods.invmod.common.entity;

import mods.invmod.common.nexus.INexusAccess;

public interface IHasNexus
{
	INexusAccess getNexus();
	
	void acquiredByNexus(INexusAccess nexus);
}
