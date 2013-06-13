package mods.invmod.common.nexus;


import java.util.List;

import mods.invmod.common.entity.AttackerAI;
import mods.invmod.common.entity.EntityIMLiving;
import mods.invmod.common.util.IPosition;
import net.minecraft.world.World;

public interface INexusAccess extends IPosition
{

	void attackNexus(int damage);

	void registerMobDied();

	boolean isActivating();

	int getMode();

	int getActivationTimer();

	int getSpawnRadius();

	int getNexusKills();

	int getGeneration();

	int getNexusLevel();

	int getCurrentWave();
	
	World getWorld();
	
	List<EntityIMLiving> getMobList();
	
	AttackerAI getAttackerAI();

	void askForRespawn(EntityIMLiving entity);
}