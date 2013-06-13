package mods.invmod.client;


import java.util.EnumSet;

import mods.invmod.common.mod_Invasion;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandlerClient implements ITickHandler
{
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		if(type.contains(TickType.CLIENT))
		{
			clientTick();
		}
	}
	
	@Override
	public EnumSet<TickType> ticks()
	{
		return EnumSet.of(TickType.CLIENT);
	}
	
	@Override
	public String getLabel()
	{
		return "IM Client Tick";
	}
	
	protected void clientTick()
	{
		mod_Invasion.onClientTick();
		mod_Invasion.getBowHackHandler().onUpdate();
	}
}
