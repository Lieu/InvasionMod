package mods.invmod.common;

import java.util.EnumSet;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandlerServer implements ITickHandler
{
	private int elapsed;
	private long timer;
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
	{
		if(type.contains(TickType.SERVER))
		{
			serverTick();
		}
	}

	@Override
	public EnumSet<TickType> ticks()
	{
		return EnumSet.of(TickType.SERVER);
	}

	@Override
	public String getLabel()
	{
		return "IM Server Tick";
	}

	protected void serverTick()
	{
		mod_Invasion.onServerTick();
	}
}
