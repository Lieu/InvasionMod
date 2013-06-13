
package mods.invmod.common.entity;

import mods.invmod.common.INotifyTask;
import mods.invmod.common.mod_Invasion;
import mods.invmod.common.nexus.INexusAccess;
import mods.invmod.common.util.CoordsInt;
import mods.invmod.common.util.IPosition;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityIMThrower extends EntityIMMob
{
	private int throwTime;
	private int ammo;
	private int punchTimer;
	private boolean clearingPoint;
	private IPosition pointToClear;
	private INotifyTask clearPointNotifee;

	public EntityIMThrower(World world)
	{
		this(world, null);
	}

	public EntityIMThrower(World world, INexusAccess nexus)
	{
		super(world, nexus);
		texture = "/mods/invmod/textures/throwerT1.png";
		moveSpeed = 0.26F;
		attackStrength = 10;
		health = 50;
		maxHealth = 50;
		selfDamage = 0;
		maxSelfDamage = 0;
		ammo = 5;
		experienceValue = 20;
		clearingPoint = false;
		setBurnsInDay(true);
		setName("");
		setDestructiveness(2);
		setSize(1.8F, 1.95F);
		setAI();
	}

	protected void setAI()
	{
		tasks = new EntityAITasks(worldObj.theProfiler);
		tasks.addTask(0, new EntityAIThrowerKillEntity<EntityPlayer>(this, EntityPlayer.class, 55, 40F, 1.0F, 5));
		tasks.addTask(1, new EntityAIAttackNexus(this));
		tasks.addTask(2, new EntityAIRandomBoulder(this, 3));
		tasks.addTask(3, new EntityAIGoToNexus(this));
		tasks.addTask(6, new EntityAIWanderIM(this, moveSpeed));
		tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 8F));
		tasks.addTask(8, new EntityAIWatchClosest(this, EntityIMCreeper.class, 12F));
		tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 16F));
		tasks.addTask(9, new EntityAILookIdle(this));

		targetTasks = new EntityAITasks(worldObj.theProfiler);
		targetTasks.addTask(2, new EntityAISimpleTarget(this, EntityPlayer.class, 16F, true));
		targetTasks.addTask(3, new EntityAIHurtByTarget(this, false));
	}

	@Override
	public void updateAITick()
	{
		super.updateAITick();
		throwTime--;
		if(clearingPoint)
		{
			if(clearPoint())
			{
				clearingPoint = false;
				if(clearPointNotifee != null)
					clearPointNotifee.notifyTask(0);
			}
		}
	}

	@Override
	public boolean isAIEnabled()
	{
		return true;
	}

	public boolean canThrow()
	{
		return throwTime <= 0;
	}

	@Override
	public boolean onPathBlocked(Path path, INotifyTask notifee)
	{
		if(!path.isFinished())
		{
			PathNode node = path.getPathPointFromIndex(path.getCurrentPathIndex());
			clearingPoint = true;
			clearPointNotifee = notifee;
			pointToClear = new CoordsInt(node.xCoord, node.yCoord, node.zCoord);
			return true;
		}
		return false;
	}

	public boolean isBlockDestructible(int id)
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

	// --------- Sparrow API --------- //

	/**What is the name of this individual entity?*/
	@Override
	public String getName()
	{
		return getName();
	}


	/**What should this entity be referred to as? (Dog, Cat, Human, Enderman, etc.)*/
	@Override
	public String getSpecies()
	{
		return "";
	}
	
	@Override
	public int getTier()
 	{
		return 3;
	}


	/**What is this entity's gender? 1 for male, 2 for female, 0 for neither*/
	@Override
	public int getGender()
	{
		return 1;
	}

	// ------------------------------- //

	@Override
	protected String getLivingSound()
	{
		return "mob.zombie.say";
	}

	@Override
	protected String getHurtSound()
	{
		return "mob.zombie.hurt";
	}

	@Override
	protected String getDeathSound()
	{
		return "mob.zombie.death";
	}

	protected boolean clearPoint()
	{
		//Destruction timer = how often the mob can destroy blocks in logic ticks (20 ticks per second)
		if(--punchTimer <= 0)
		{
			int x = pointToClear.getXCoord();
			int y = pointToClear.getYCoord();
			int z = pointToClear.getZCoord();
			int mobX = MathHelper.floor_double(posX);
			int mobZ = MathHelper.floor_double(posZ);
			int xOffsetR = 0;
			int zOffsetR = 0;
			int axisX = 0;
			int axisZ = 0;

			float facing = rotationYaw % 360;
			if(facing < 0)
			{
				facing += 360;
			}
			if(facing >= 45 && facing  < 135) //-X North
			{
				zOffsetR = -1;
				axisX = -1;
			}
			else if(facing  >= 135 && facing  < 225) //-Z East
			{
				xOffsetR = -1;
				axisZ = -1;
			}
			else if(facing  >= 225 && facing  < 315) //+X
			{
				zOffsetR = -1;
				axisX = 1;
			}
			else //+Z
			{
				xOffsetR = -1;
				axisZ = 1;
			}

			//Check/destroy the 4 blocks in front of the mob in a square to the right
			if((Block.blocksList[worldObj.getBlockId(x, y, z)] != null && Block.blocksList[worldObj.getBlockId(x, y, z)].blockMaterial.isSolid())
					|| (Block.blocksList[worldObj.getBlockId(x, y + 1, z)] != null && Block.blocksList[worldObj.getBlockId(x, y + 1, z)].blockMaterial.isSolid())
					|| (Block.blocksList[worldObj.getBlockId(x + xOffsetR, y, z + zOffsetR)] != null && Block.blocksList[worldObj.getBlockId(x + xOffsetR, y, z + zOffsetR)].blockMaterial.isSolid())
					|| (Block.blocksList[worldObj.getBlockId(x + xOffsetR, y + 1, z + zOffsetR)] != null && Block.blocksList[worldObj.getBlockId(x + xOffsetR, y + 1, z + zOffsetR)].blockMaterial.isSolid()))
			{
				tryDestroyBlock(x, y, z);
				tryDestroyBlock(x, y + 1, z);
				tryDestroyBlock(x + xOffsetR, y, z + zOffsetR);
				tryDestroyBlock(x + xOffsetR, y + 1, z + zOffsetR);
				punchTimer = 160;
			}
			//Also, above
			else if((Block.blocksList[worldObj.getBlockId(x - axisX, y + 1, z - axisZ)] != null && Block.blocksList[worldObj.getBlockId(x - axisX, y + 1, z - axisZ)].blockMaterial.isSolid())
					|| (Block.blocksList[worldObj.getBlockId(x - axisX + xOffsetR, y + 1, z - axisZ + zOffsetR)] != null && Block.blocksList[worldObj.getBlockId(x - axisX + xOffsetR, y + 1, z - axisZ + zOffsetR)].blockMaterial.isSolid()))
			{
				tryDestroyBlock(x - axisX, y + 1, z - axisZ);
				tryDestroyBlock(x - axisX + xOffsetR, y + 1, z - axisZ + zOffsetR);
				punchTimer = 160;
			}
			else if((Block.blocksList[worldObj.getBlockId(x - 2*axisX, y + 1, z - 2*axisZ)] != null && Block.blocksList[worldObj.getBlockId(x - 2*axisX, y + 1, z - 2*axisZ)].blockMaterial.isSolid())
					|| (Block.blocksList[worldObj.getBlockId(x - 2*axisX + xOffsetR, y + 1, z - 2*axisZ + zOffsetR)] != null && Block.blocksList[worldObj.getBlockId(x - 2*axisX + xOffsetR, y + 1, z - 2*axisZ + zOffsetR)].blockMaterial.isSolid()))
			{
				tryDestroyBlock(x - 2*axisX, y + 1, z - 2*axisZ);
				tryDestroyBlock(x - 2*axisX + xOffsetR, y + 1, z - 2*axisZ + zOffsetR);
				punchTimer = 160;
			}
			else
			{
				//Otherwise, the path appears clear
				return true;
			}
		}
		return false;
	}

	//Deal with the case where the mob cannot move to the next coordinate
	/*protected void handlePointBlocked(Vec3D vec3d)
    {
    	if(pathBlocked)
        {
    		//Destructiveness determines block killing propensity
    		if(destructiveness < 2)
    		{
    			pathBlocked = false;
    			pathToEntity = null;
    			destructiveness = 2;
    			return;
    		}
    		//Destruction timer = how often the mob can destroy blocks in logic ticks (20 ticks per second)
        	if(++destructionTimer > 160)
        	{
        		destructionTimer = 0;
	        	if(vec3d != null)
	            {
	        		int x = MathHelper.floor_double(vec3d.xCoord);
		        	int y = MathHelper.floor_double(vec3d.yCoord);
		        	int z = MathHelper.floor_double(vec3d.zCoord);
		        	int mobX = MathHelper.floor_double(posX);
        	    	int mobZ = MathHelper.floor_double(posZ);
        	    	int xOffsetR = 0;
        	    	int zOffsetR = 0;
        	    	int axisX = 0;
        	    	int axisZ = 0;

        	    	float facing = rotationYaw % 360;
        	    	if(facing < 0)
        	    	{
        	    		facing += 360;
        	    	}
        	    	if(facing >= 45 && facing  < 135) //-X North
        	    	{
        	    		zOffsetR = -1;
        	    		axisX = -1;
        	    	}
        	    	else if(facing  >= 135 && facing  < 225) //-Z East
        	    	{
        	    		xOffsetR = -1;
        	    		axisZ = -1;
        	    	}
        	    	else if(facing  >= 225 && facing  < 315) //+X
        	    	{
        	    		zOffsetR = -1;
        	    		axisX = 1;
        	    	}
        	    	else //+Z
        	    	{
        	    		xOffsetR = -1;
        	    		axisZ = 1;
        	    	}

        	    	//Check/destroy the 4 blocks in front of the mob in a square to the right
	        	    if((Block.blocksList[worldObj.getBlockId(x, y, z)] != null && Block.blocksList[worldObj.getBlockId(x, y, z)].blockMaterial.isSolid())
	        	    		|| (Block.blocksList[worldObj.getBlockId(x, y + 1, z)] != null && Block.blocksList[worldObj.getBlockId(x, y + 1, z)].blockMaterial.isSolid())
	        	    		|| (Block.blocksList[worldObj.getBlockId(x + xOffsetR, y, z + zOffsetR)] != null && Block.blocksList[worldObj.getBlockId(x + xOffsetR, y, z + zOffsetR)].blockMaterial.isSolid())
	        	    		|| (Block.blocksList[worldObj.getBlockId(x + xOffsetR, y + 1, z + zOffsetR)] != null && Block.blocksList[worldObj.getBlockId(x + xOffsetR, y + 1, z + zOffsetR)].blockMaterial.isSolid()))
		            {
	        	    	tryDestroyBlock(x, y, z);
	        	    	tryDestroyBlock(x, y + 1, z);
	        	    	tryDestroyBlock(x + xOffsetR, y, z + zOffsetR);
	        	    	tryDestroyBlock(x + xOffsetR, y + 1, z + zOffsetR);	
		        	}
	        	    //Also, above
	        	    else if((Block.blocksList[worldObj.getBlockId(x - axisX, y + 1, z - axisZ)] != null && Block.blocksList[worldObj.getBlockId(x - axisX, y + 1, z - axisZ)].blockMaterial.isSolid())
	        	    			|| (Block.blocksList[worldObj.getBlockId(x - axisX + xOffsetR, y + 1, z - axisZ + zOffsetR)] != null && Block.blocksList[worldObj.getBlockId(x - axisX + xOffsetR, y + 1, z - axisZ + zOffsetR)].blockMaterial.isSolid()))
	        	    {
	        	    	tryDestroyBlock(x - axisX, y + 1, z - axisZ);
	        	    	tryDestroyBlock(x - axisX + xOffsetR, y + 1, z - axisZ + zOffsetR);
	        	    }
	        	    else if((Block.blocksList[worldObj.getBlockId(x - 2*axisX, y + 1, z - 2*axisZ)] != null && Block.blocksList[worldObj.getBlockId(x - 2*axisX, y + 1, z - 2*axisZ)].blockMaterial.isSolid())
        	    				|| (Block.blocksList[worldObj.getBlockId(x - 2*axisX + xOffsetR, y + 1, z - 2*axisZ + zOffsetR)] != null && Block.blocksList[worldObj.getBlockId(x - 2*axisX + xOffsetR, y + 1, z - 2*axisZ + zOffsetR)].blockMaterial.isSolid()))
        	    	{
	        	    	tryDestroyBlock(x - 2*axisX, y + 1, z - 2*axisZ);
	        	    	tryDestroyBlock(x - 2*axisX + xOffsetR, y + 1, z - 2*axisZ + zOffsetR);
        	    	}
		        	else
		        	{
		        		//Otherwise, the path appears clear for now
		        		pathBlocked = false;
		        		pathToEntity = null;
		        	}
	            }
	        	else
	        	{
	        		pathBlocked = false;
	        	}
        	}
        }
    }*/

	//Handle an attempt to destroy a block
	protected void tryDestroyBlock(int x, int y, int z)
	{
		int id = worldObj.getBlockId(x, y, z);
		Block block = Block.blocksList[id];
		if(block != null && (isNexusBound() || entityToAttack != null)) // Can destroy blocks if attacking a nexus or a random mob is specifically after a player
		{
			if(id == mod_Invasion.blockNexus.blockID && attackTime == 0 && x == targetNexus.getXCoord() && y == targetNexus.getYCoord() && z == targetNexus.getZCoord())
			{
				targetNexus.attackNexus(5);
				attackTime = 60;
			}
			else if(id != mod_Invasion.blockNexus.blockID)
			{
				//Remove block and notify
				int meta = worldObj.getBlockMetadata(x, y, z);
				worldObj.setBlock(x, y, z, 0);
				block.onBlockDestroyedByPlayer(worldObj, x, y, z, meta);
				block.dropBlockAsItem(worldObj, x, y, z, meta, 0);

				//Finally, play a sound
				if(throttled == 0)
				{
					worldObj.playSoundAtEntity(this, "invsound.explode", 1.0F, 0.4F);
					//worldObj.setEntityState(this, (byte)7);
					throttled = 5;
				}
			}
		}
	}

	@Override
	protected void attackEntity(Entity entity, float f)
	{
		if(throwTime <= 0 && ammo > 0  && f > 4)
		{
			throwTime = 120;
			if(f < 50F)
			{
				throwBoulder(entity.posX, entity.posY + entity.getEyeHeight() - 0.7D, entity.posZ, false); 
			}
		}
		else
		{
			super.attackEntity(entity, f);
		}
	}

	protected void throwBoulder(double entityX, double entityY, double entityZ, boolean forced)
	{
		float launchSpeed = 1.0F;	
		double dX = entityX - posX;
		double dZ = entityZ - posZ;
		double dXY = MathHelper.sqrt_double(dX * dX + dZ * dZ);

		//If within throw range and can attack
		if((0.025D * dXY) / (launchSpeed * launchSpeed) <= 1.0F && attackTime == 0)
		{
			EntityIMBoulder entityBoulder = new EntityIMBoulder(worldObj, this, launchSpeed);                
			double dY = entityY - entityBoulder.posY;     
			double angle = 0.5D * Math.asin((0.025D * dXY) / (launchSpeed * launchSpeed));
			dY += dXY * Math.tan(angle);
			entityBoulder.setBoulderHeading(dX, dY, dZ, launchSpeed, 0.05F);
			worldObj.spawnEntityInWorld(entityBoulder);
		}
		else if(forced)
		{
			EntityIMBoulder entityBoulder = new EntityIMBoulder(worldObj, this, launchSpeed);                
			double dY = entityY - entityBoulder.posY;
			dY += dXY * Math.tan(0.25D * Math.PI);
			entityBoulder.setBoulderHeading(dX, dY, dZ, launchSpeed, 0.05F);
			worldObj.spawnEntityInWorld(entityBoulder);
		}
	}

	protected void throwBoulder(double entityX, double entityY, double entityZ)
	{
		throwTime = 40;
		float launchSpeed = 1.0F;	
		double dX = entityX - posX;
		double dZ = entityZ - posZ;
		double dXY = MathHelper.sqrt_double(dX * dX + dZ * dZ);
		double p = (0.025D * dXY) / (launchSpeed * launchSpeed);
		double angle;
		// If within throw range
		if(p <= 1.0F)
			angle = 0.5 * p;
		else
			angle = 0.25 * Math.PI;

		EntityIMBoulder entityBoulder = new EntityIMBoulder(worldObj, this, launchSpeed);                
		double dY = entityY - entityBoulder.posY;
		dY += dXY * Math.tan(angle);
		entityBoulder.setBoulderHeading(dX, dY, dZ, launchSpeed, 0.05F);
		worldObj.spawnEntityInWorld(entityBoulder);
	}

	@Override
	protected void dropFewItems(boolean flag, int bonus)
	{
		super.dropFewItems(flag, bonus);
		entityDropItem(new ItemStack(mod_Invasion.itemRemnants, 1, 1), 0.0F);
	}
}
