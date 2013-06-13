package mods.invmod.common.entity;

import mods.invmod.common.INotifyTask;
import mods.invmod.common.mod_Invasion;
import mods.invmod.common.nexus.INexusAccess;
import mods.invmod.common.util.CoordsInt;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class EntityIMCreeper extends EntityIMMob implements ILeader
{
	private int timeSinceIgnited;
	private int lastActiveTime;
	private boolean explosionDeath;
	private boolean commitToExplode;
	private int explodeDirection;
	
	public EntityIMCreeper(World world)
	{
		this(world, null);
	}

	public EntityIMCreeper(World world, INexusAccess nexus)
	{
		super(world, nexus);
		texture = "/mob/creeper.png";
		setName("Creeper");
		setGender(0);
		moveSpeed = 0.42F;
		tasks.addTask(0, new EntityAISwimming(this));
		tasks.addTask(1, new EntityAICreeperIMSwell(this));
		tasks.addTask(2, new EntityAIAvoidEntity(this, EntityOcelot.class, 6F, 0.25F, 0.3F));
		tasks.addTask(3, new EntityAIKillEntity<EntityLiving>(this, EntityLiving.class, 40));
		tasks.addTask(4, new EntityAIGoToNexus(this));
		tasks.addTask(5, new EntityAIWanderIM(this, 0.2F));
		tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 4.8F));
		tasks.addTask(6, new EntityAILookIdle(this));
		targetTasks.addTask(0, new EntityAITargetRetaliate(this, EntityLiving.class, 12F));
		targetTasks.addTask(1, new EntityAISimpleTarget(this, EntityPlayer.class, 4.8F, true));
		targetTasks.addTask(2, new EntityAIHurtByTarget(this, false));
	}
	
	@Override
	public void updateAITick()
	{
		super.updateAITick();
	}

	@Override
	public boolean isAIEnabled()
	{
		return true;
	}
	
	@Override
	public boolean onPathBlocked(Path path, INotifyTask notifee)
	{
		if(!path.isFinished())
    	{
	    	PathNode node = path.getPathPointFromIndex(path.getCurrentPathIndex());
	    	double dX = node.xCoord + 0.5 - posX;
	    	double dZ = node.zCoord + 0.5 - posZ;
	    	float facing = ((float)((Math.atan2(dZ, dX) * 180D) / Math.PI) - 90F);
	     	if(facing < 0)
	     	{
	     		facing += 360;
	     	}
	     	facing %= 360;
	     	
	    	if(facing >= 45 && facing  < 135) //-X North
	    		explodeDirection = 1;
	    	else if(facing  >= 135 && facing  < 225) //-Z East
	    		explodeDirection = 3;
	    	else if(facing  >= 225 && facing  < 315) //+X
	    		explodeDirection = 0;
	    	else //+Z
	    		explodeDirection = 2;
	    	
	    	setCreeperState(1);
	    	commitToExplode = true;
    	}
		return false;
	}

	@Override
	public int getMaxHealth()
	{
		return 20;
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		dataWatcher.addObject(16, Byte.valueOf((byte) - 1));
		dataWatcher.addObject(17, Byte.valueOf((byte)0));
	}

	@Override
	public void onUpdate()
	{
		if(explosionDeath)
		{
			doExplosion();
			setDead();
		}
		else if(isEntityAlive())
		{
			lastActiveTime = timeSinceIgnited;
			int state = getCreeperState();

			if(state > 0)
			{
				if(commitToExplode)
					getMoveHelper().setMoveTo(posX + CoordsInt.offsetAdjX[explodeDirection], posY, posZ + CoordsInt.offsetAdjZ[explodeDirection], 0);
				
				if(timeSinceIgnited == 0)
					worldObj.playSoundAtEntity(this, "random.fuse", 1.0F, 0.5F);
			}

			timeSinceIgnited += state;
			if(timeSinceIgnited < 0)
				timeSinceIgnited = 0;

			if(timeSinceIgnited >= 30)
			{
				timeSinceIgnited = 30;
				if(!worldObj.isRemote)
				{
					explosionDeath = true;
				}
			}
		}

		super.onUpdate();
	}
	
	@Override
	public boolean isMartyr()
	{
		return explosionDeath;
	}

	@Override
	protected String getHurtSound()
	{
		return "mob.creeper.say";
	}

	@Override
	protected String getDeathSound()
	{
		return "mob.creeper.death";
	}
	
	 // --------- Sparrow API --------- //

	/**What should this entity be referred to as? (Dog, Cat, Human, Enderman, etc.)*/
	@Override
	public String getSpecies()
	{
		return "Creeper";
	}
	
	@Override
	public int getTier()
 	{
		return 2;
	}
	
	// ------------------------------- //

	@Override
	public void onDeath(DamageSource par1DamageSource)
	{
		super.onDeath(par1DamageSource);

		if (par1DamageSource.getEntity() instanceof EntitySkeleton)
		{
			dropItem(Item.record13.itemID + rand.nextInt(10), 1);
		}
	}

	@Override
	public boolean attackEntityAsMob(Entity par1Entity)
	{
		return true;
	}

	/**
	 * Connects the the creeper flashes to the creeper's color multiplier
	 */
	public float setCreeperFlashTime(float par1)
	{
		return (lastActiveTime + (timeSinceIgnited - lastActiveTime) * par1) / 28F;
	}
	
	@Override
	public float getBlockPathCost(PathNode prevNode, PathNode node, IBlockAccess terrainMap)
    {
		int id = terrainMap.getBlockId(node.xCoord, node.yCoord, node.zCoord);
		if(id > 0 && !Block.blocksList[id].getBlocksMovement(terrainMap, node.xCoord, node.yCoord, node.zCoord) && id != mod_Invasion.blockNexus.blockID)
		{
    		return prevNode.distanceTo(node) * 12F; // Very high cost; exploding is suicide
		}
		
		return super.getBlockPathCost(prevNode, node, terrainMap);
    }
	
	@Override
	public boolean isBlockTypeDestructible(int id)
    {
    	if(id == 0 || id == Block.bedrock.blockID || id == Block.ladder.blockID/* || id == mod_Invasion.blockNexus.blockID*/)
    	{
    		return false;
    	}
    	else if(id == Block.doorIron.blockID || id == Block.doorWood.blockID || id == Block.trapdoor.blockID)
    	{
    		return true;
    	}
    	else if(Block.blocksList[id].blockMaterial.isSolid())
    	{
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }
	
	@Override
	public String toString()
    {
    	return "EntityIMCreeper#";
    }

	@Override
	protected int getDropItemId()
	{
		return Item.gunpowder.itemID;
	}
	
	protected void doExplosion()
	{
		Explosion explosion = worldObj.createExplosion(this, posX, posY + 1, posZ, 1.3F, true);
		int x = getXCoord();
		int y = getYCoord() + 1;
		int z = getZCoord();
		int direction = 0;
		float facing = rotationYaw % 360;
    	if(facing < 0)
    		facing += 360;
    	
    	if(facing >= 45 && facing  < 135) //-X North
    		direction = 1;
    	else if(facing  >= 135 && facing  < 225) //-Z East
    		direction = 3;
    	else if(facing  >= 225 && facing  < 315) //+X
    		direction = 0;
    	else //+Z
    		direction = 2;
    	
    	for(int i = -1; i < 2; i++)
    	{
    		for(int j = -1; j < 2; j++)
    		{
    				float explosionStrength = 50.0F;
    				for(int depth = 0; depth <= 3; depth++)
        			{
    					// Break for purpose of 1x2 hole at final depth
    					if(depth == 3 && (!(j == -1 || j == 0) || i != 0))
    						break;
    					
    					int xOff = i;
    					int zOff = i;
    					if(direction == 0)
    	    				xOff = depth;
    					else if(direction == 1)
    						xOff = -depth;
    					else if(direction == 2)
    						zOff = depth;
    					else
    						zOff = -depth;
    					
    					int id = worldObj.getBlockId(x + xOff, y + j, z + zOff);
        				if (id > 0 && id != mod_Invasion.blockNexus.blockID)
        	            {
        					explosionStrength -= Block.blocksList[id].getExplosionResistance(this);
        					if(explosionStrength < 0)
        						break;
        					
        	                Block.blocksList[id].dropBlockAsItemWithChance(worldObj, x + xOff, y + j, z + zOff, worldObj.getBlockMetadata(x + xOff, y + j, z + zOff), 0.5F, 0);
        	                worldObj.setBlock(x + xOff, y + j, z + zOff, 0);
        	                Block.blocksList[id].onBlockDestroyedByExplosion(worldObj, x + xOff, y + j, z + zOff, explosion);
        	            }
				}
    		}
    	}
	}

	/**
	 * Returns the current state of creeper, -1 is idle, 1 is 'in fuse'
	 */
	public int getCreeperState()
	{
		return dataWatcher.getWatchableObjectByte(16);
	}

	/**
	 * Sets the state of creeper, -1 to idle and 1 to be 'in fuse'
	 */
	public void setCreeperState(int state)
	{
		if(commitToExplode && state != 1)
			return;
		
		dataWatcher.updateObject(16, Byte.valueOf((byte)state));
	}
}
