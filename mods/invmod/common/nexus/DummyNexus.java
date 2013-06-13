package mods.invmod.common.nexus;


import java.util.List;

import mods.invmod.common.entity.AttackerAI;
import mods.invmod.common.entity.EntityIMLiving;
import net.minecraft.world.World;

public class DummyNexus implements INexusAccess
{
	private World world;
	
	public void setWorld(World world)
	{
		this.world = world;
	}
	
	@Override
	public void attackNexus(int damage)	{ }

	@Override
	public void registerMobDied() {	}

	@Override
	public boolean isActivating()
	{
		return false;
	}

	@Override
	public int getMode()
	{
		return 0;
	}

	@Override
	public int getActivationTimer()
	{
		return 0;
	}

	@Override
	public int getSpawnRadius()
	{
		return 45;
	}

	@Override
	public int getNexusKills()
	{
		return 0;
	}

	@Override
	public int getGeneration()
	{
		return 0;
	}

	@Override
	public int getNexusLevel()
	{
		return 1;
	}

	@Override
	public int getCurrentWave()
	{
		return 1;
	}

	@Override
	public int getXCoord()
	{
		return 0;
	}

	@Override
	public int getYCoord()
	{
		return 0;
	}

	@Override
	public int getZCoord()
	{
		return 0;
	}

	@Override
	public World getWorld()
	{
		return world;
	}
	
	@Override
	public List<EntityIMLiving> getMobList()
	{
		return null;
	}

	@Override
	public void askForRespawn(EntityIMLiving entity) { }

	@Override
	public AttackerAI getAttackerAI()
	{
		return null;
	}
}
