package mods.invmod.common.entity;

import mods.invmod.common.nexus.INexusAccess;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIRandomBoulder extends EntityAIBase
{
	private final EntityIMThrower theEntity;
	private int randomAmmo;
	private int timer;
	
	public EntityAIRandomBoulder(EntityIMThrower entity, int ammo)
	{
		theEntity = entity;
		randomAmmo = ammo;
		timer = 180;
	}
	
	@Override
	public boolean shouldExecute()
	{
		if(theEntity.getNexus() != null && randomAmmo > 0 && theEntity.canThrow())
		{
			if(--timer <= 0)
				return true;
		}
		return false;
	}
	
	@Override
	public boolean isInterruptible()
    {
        return false;
    }
	
	@Override
	public void startExecuting()
	{
		randomAmmo--;
		timer = 240;
		INexusAccess nexus = theEntity.getNexus();
		int d = (int)(theEntity.findDistanceToNexus() * 0.37);
		theEntity.throwBoulder(nexus.getXCoord() - d + theEntity.getRNG().nextInt(2 * d), nexus.getYCoord() - 5 + theEntity.getRNG().nextInt(10), nexus.getZCoord() - d + theEntity.getRNG().nextInt(2 * d));
	}

}
