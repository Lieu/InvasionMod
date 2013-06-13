package mods.invmod.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class EntityIMEgg extends EntityIMLiving
{
	private static int META_HATCHED = 30;
	
	private int hatchTime;
	private int maxHp;
	private int ticks;
	private boolean hatched;
	private Entity parent;
	private Entity[] contents;
	
	public EntityIMEgg(World world)
	{
		super(world);
		getDataWatcher().addObject(META_HATCHED, (byte)0);
	}

	public EntityIMEgg(Entity parent, Entity[] contents, int hatchTime, int hp)
	{
		super(parent.worldObj);
		this.parent = parent;
		this.contents = contents;
		this.hatchTime = hatchTime;
		health = maxHp = hp;
		hatched = false;
		ticks = 0;
		moveSpeed = 0.01F;
		
		getDataWatcher().addObject(META_HATCHED, (byte)0);
		
		setName("Spider Egg");
		setGender(0);
		setPosition(parent.posX, parent.posY, parent.posZ);
		setSize(0.5F, 0.8F);
	}

	// --------- Sparrow API --------- //

	/**What should this entity be referred to as? (Dog, Cat, Human, Enderman, etc.)*/
	@Override
	public String getSpecies()
	{
		return null;
	}
	
	@Override
	public int getTier()
 	{
		return 0;
	}

	/**Does this entity attack the player on sight?*/
    @Override
	public boolean isHostile()
    {
    	return false;
    }
	
    /**Will this entity attack, but only when provoked?*/
    @Override
	public boolean isNeutral()
    {
    	return false;
    }
    
    /**Should this entity be considered a threat to par1entity?*/
    @Override
	public boolean isThreatTo(Entity entity)
    {
    	if(entity instanceof EntityPlayer)
    		return true;
    	else
    		return false;
    }
    
    /**What is this entity currently targeting with intent to kill? Used to differentiate between the attack method monsters use and the attack method used for breeding and following*/
    @Override
	public Entity getAttackingTarget()
    {
    	return null;
    }
	
	// ------------------------------- //

	/*public boolean canBePushed()
	{
		return false;
	}

	public boolean canBeCollidedWith()
    {
		return false;
    }

	public boolean isMovementBlocked()
	{
		return true;
	}*/

	@Override
	public void onEntityUpdate()
	{
		super.onEntityUpdate();
		if(!worldObj.isRemote)
		{
			ticks++;
			if(hatched)
			{
				if(ticks > hatchTime + 40)
					setDead();
			}
			else if(ticks > hatchTime)
			{
				hatch();
			}
		}
		else if(!hatched && getDataWatcher().getWatchableObjectByte(META_HATCHED) == 1)
		{
			worldObj.playSoundAtEntity(this, "invsound.egghatch", 1.0F, 1.0F);
			hatched = true;
		}
	}

	@Override
	public int getMaxHealth()
	{
		return maxHp;
	}

	private void hatch()
	{
		worldObj.playSoundAtEntity(this, "invsound.egghatch", 1.0F, 1.0F);
		hatched = true;
		if(!worldObj.isRemote)
		{
			getDataWatcher().updateObject(META_HATCHED, (byte)1);
			if(contents != null)
			{
				for(Entity entity : contents)
				{
					entity.setPosition(posX, posY, posZ);
					worldObj.spawnEntityInWorld(entity);
				}
			}
		}
	}
}
