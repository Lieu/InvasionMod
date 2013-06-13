package mods.invmod.common.entity;


import java.util.List;

import mods.invmod.common.mod_Invasion;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityIMTrap extends Entity
{
    public static final int TRAP_DEFAULT = 0;
    public static final int TRAP_RIFT = 1;
    public static final int TRAP_FIRE = 2;
    private static final int ARM_TIME = 60;
    private static final int META_CHANGED = 29;
    private static final int META_TYPE = 30;
    private static final int META_EMPTY = 31;
    
    private int trapType;
    private int ticks;
    private boolean isEmpty;
    private byte metaChanged;
    
	public EntityIMTrap(World world)
	{
		super(world);
		setSize(0.5F, 0.28F);
		yOffset = 0;
		ticks = 0;
		isEmpty = false;
		trapType = TRAP_DEFAULT;
		if(world.isRemote)
			metaChanged = 1;
		else
			metaChanged = 0;
		
		dataWatcher.addObject(META_CHANGED, metaChanged);
		dataWatcher.addObject(META_TYPE, trapType);
		dataWatcher.addObject(META_EMPTY, isEmpty ? (byte)0 : (byte)1);
	}
	
	public EntityIMTrap(World world, double x, double y, double z)
	{
		this(world, x, y, z, TRAP_DEFAULT);
	}
	
	public EntityIMTrap(World world, double x, double y, double z, int trapType)
	{
		this(world);		
		this.trapType = trapType;
		dataWatcher.updateObject(META_TYPE, trapType);
		setLocationAndAngles(x, y, z, 0, 0);
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();	
		ticks++;
		if(worldObj.isRemote)
		{
			if(metaChanged != dataWatcher.getWatchableObjectByte(META_CHANGED) || ticks % 20 == 0)
			{
				metaChanged = dataWatcher.getWatchableObjectByte(META_CHANGED);			
				trapType = dataWatcher.getWatchableObjectInt(META_TYPE);
				boolean wasEmpty = isEmpty;
				isEmpty = dataWatcher.getWatchableObjectByte(META_EMPTY) == 0;
				if(isEmpty && !wasEmpty && trapType == TRAP_RIFT)
					doRiftParticles();
			}
			return;
		}
		
		if(!isValidPlacement())
		{
			EntityItem entityitem = new EntityItem(worldObj, posX, posY, posZ, new ItemStack(mod_Invasion.itemIMTrap.itemID, 1, 0));
	        entityitem.delayBeforeCanPickup = 10;
	        worldObj.spawnEntityInWorld(entityitem);
            setDead();
		}			
		
		if(worldObj.isRemote || (!isEmpty && ticks < ARM_TIME))
			return;

		//List<EntityLiving> entities = worldObj.getEntitiesWithinAABB(EntityLiving.class, AxisAlignedBB.getBoundingBox(posX - 0.25, posY, posZ - 0.25, posX + 0.25, posY + 0.5, posZ + 0.25));
		@SuppressWarnings("unchecked")
		List<EntityLiving> entities = worldObj.getEntitiesWithinAABB(EntityLiving.class, boundingBox);
		if(entities.size() > 0 && !isEmpty)
		{
			for(EntityLiving entity : entities)
			{
				if(trapEffect(entity))
				{
					setEmpty(); // Also sets ticks to 0 again
					return;
				}
			}
		}
	}
	
	public boolean trapEffect(EntityLiving triggerEntity)
	{
		if(trapType == TRAP_DEFAULT)
		{
			triggerEntity.attackEntityFrom(DamageSource.generic, 4);
		}
		else if(trapType == TRAP_RIFT)
		{
			triggerEntity.attackEntityFrom(DamageSource.magic, triggerEntity instanceof EntityPlayer ? 12 : 38);
			@SuppressWarnings("unchecked")
			List<Entity> entities = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(1.9F, 1.0F, 1.9F));
	    	for(Entity entity : entities)
	    	{
	    		entity.attackEntityFrom(DamageSource.magic, 8);
	    		if(entity instanceof EntityIMLiving)
	    			((EntityIMLiving)entity).stunEntity(60);
	    	}
	    	
	    	worldObj.playSoundAtEntity(this, "random.break", 1.5F, 1.0F * (rand.nextFloat() * 0.25F + 0.55F)); // -20% to -45%
	    	/*if(worldObj.isRemote)
	    	{
	    		doRiftParticles();
	    	}*/
		}
		else if(trapType == TRAP_FIRE)
		{
			worldObj.playSoundAtEntity(this, "invsound.fireball", 1.5F, 1.15F / (rand.nextFloat() * 0.3F + 1.0F));
			doFireball(1.1F, 8);
		}
			
		return true;
	}
	
	@Override
	public void onCollideWithPlayer(EntityPlayer entityPlayer)
	{
		if(!worldObj.isRemote && ticks > 30 && isEmpty)
		{
	        if(entityPlayer.inventory.addItemStackToInventory(new ItemStack(mod_Invasion.itemIMTrap, 1, 0)))
	        {
	            worldObj.playSoundAtEntity(this, "random.pop", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
	            entityPlayer.onItemPickup(this, 1);
	            setDead();
	        }
		}
	}
	
    @Override
	public boolean interact(EntityPlayer entityPlayer)
    {
    	if(worldObj.isRemote || isEmpty)
    		return false;
    	
        ItemStack itemStack = entityPlayer.inventory.getCurrentItem();
        if(itemStack != null && itemStack.itemID == mod_Invasion.itemProbe.itemID && itemStack.getItemDamage() >= 1)
        {
        	EntityItem entityitem = new EntityItem(worldObj, posX, posY, posZ, new ItemStack(mod_Invasion.itemIMTrap.itemID, 1, trapType));
	        entityitem.delayBeforeCanPickup = 5;
	        worldObj.spawnEntityInWorld(entityitem);
            setDead();
            return true;
        }
        return false;
    }
	
	public boolean isEmpty()
	{
		return isEmpty;
	}
	
	public int getTrapType()
	{
		return trapType;
	}
	
	public boolean isValidPlacement()
	{
		return worldObj.isBlockNormalCube(MathHelper.floor_double(posX), MathHelper.floor_double(posY) - 1, MathHelper.floor_double(posZ)) && worldObj.getEntitiesWithinAABB(EntityIMTrap.class, boundingBox).size() < 2;
	}
	
	@Override
	public boolean canBeCollidedWith()
    {
        return true;
    }
	
	@Override
	public void entityInit()
	{
    }
	
    @Override
	public float getShadowSize()
    {
        return 0.0F;
    }
    
    @Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
    	isEmpty = nbttagcompound.getBoolean("isEmpty");
    	trapType = nbttagcompound.getInteger("type");
    }

    @Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {    	
    	nbttagcompound.setBoolean("isEmpty", isEmpty);
    	nbttagcompound.setInteger("type", trapType);    	
    }
    
	private void setEmpty()
	{
		isEmpty = true;
		ticks = 0;
		dataWatcher.updateObject(META_EMPTY, isEmpty ? (byte)0 : (byte)1);
		dataWatcher.updateObject(META_CHANGED, dataWatcher.getWatchableObjectByte(META_CHANGED) == 0 ? (byte)1 : (byte)0);
	}
    
    private void doFireball(float size, int initialDamage)
    {
    	int x = MathHelper.floor_double(posX);
        int y = MathHelper.floor_double(posY);
        int z = MathHelper.floor_double(posZ);
        int min = 0 - (int)size;
        int max = 0 + (int)size;
    	for(int i = min; i <= max; i++)
    	{
    		for(int j = min; j <= max; j++)
    		{
    			for(int k = min; k <= max; k++)
        		{
	    			if(worldObj.getBlockId(x + i, y + j, z + k) == 0 || worldObj.getBlockMaterial(x + i, y + j, z + k).getCanBurn())
	    				worldObj.setBlock(x + i, y + j, z + k, Block.fire.blockID);
        		}
    		}
    	}
    	
    	@SuppressWarnings("unchecked")
		List<Entity> entities = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.expand(size, size, size));
    	for(Entity entity : entities)
    	{
    		entity.setFire(8);
    		entity.attackEntityFrom(DamageSource.onFire, initialDamage);
    	}
    }
    
    private void doRiftParticles()
    {
    	for(int i = 0; i < 300; i++)
    	{
    		float x = rand.nextFloat() * 6F - 3F;
    		float z = rand.nextFloat() * 6F - 3F;
    		worldObj.spawnParticle("portal", posX + x, posY + 2, posZ + z, -x / 3, -2, -z / 3);
    	}
    }
}
