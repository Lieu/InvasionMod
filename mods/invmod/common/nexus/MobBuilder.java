package mods.invmod.common.nexus;

import mods.invmod.common.mod_Invasion;
import mods.invmod.common.entity.EntityIMBurrower;
import mods.invmod.common.entity.EntityIMCreeper;
import mods.invmod.common.entity.EntityIMLiving;
import mods.invmod.common.entity.EntityIMPigEngy;
import mods.invmod.common.entity.EntityIMSkeleton;
import mods.invmod.common.entity.EntityIMSpider;
import mods.invmod.common.entity.EntityIMThrower;
import mods.invmod.common.entity.EntityIMZombie;
import net.minecraft.world.World;

public class MobBuilder
{
	public MobBuilder() { }
	
	/**
	 * Attempts to create an entity from the mob construct provided, attached to the specified nexus.
	 * Returns null if mob construction was not possible from the construct
	 */
	public EntityIMLiving createMobFromConstruct(EntityConstruct mobConstruct, World world, INexusAccess nexus)
	{
		// Choose concrete type for mob and any mobType-specific behaviour.
		// It is designed for each case to possibly be arbitrarily different
		EntityIMLiving mob = null;
		switch(mobConstruct.getMobType())
		{
		case ZOMBIE:
			EntityIMZombie zombie = new EntityIMZombie(world, nexus);
			zombie.setTexture(mobConstruct.getTexture());
			zombie.setFlavour(mobConstruct.getFlavour());
			zombie.setTier(mobConstruct.getTier());
			mob = zombie;
			break;
		case SPIDER:
			EntityIMSpider spider = new EntityIMSpider(world, nexus);
			spider.setTexture(mobConstruct.getTexture());
			spider.setFlavour(mobConstruct.getFlavour());
			spider.setTier(mobConstruct.getTier());
			mob = spider;
			break;
		case SKELETON:
			EntityIMSkeleton skeleton = new EntityIMSkeleton(world, nexus);
			mob = skeleton;
			break;
		case PIG_ENGINEER:
			EntityIMPigEngy pigEngy = new EntityIMPigEngy(world, nexus);
			mob = pigEngy;
			break;
		case THROWER:
			EntityIMThrower thrower = new EntityIMThrower(world, nexus);
			mob = thrower;
			break;
		case BURROWER:
			EntityIMBurrower burrower = new EntityIMBurrower(world, nexus);
			mob = burrower;
			break;
		case CREEPER:
			EntityIMCreeper creeper = new EntityIMCreeper(world, nexus);
			mob = creeper;
			break;
		default:
			mod_Invasion.log("Missing mob type in MobBuilder: " + mobConstruct.getMobType());
			mob = null;
		}
		
		// Do any configuration that can be done abstractly
		//if(mob != null) { }
			
		return mob;
	}
}
