
package mods.invmod.common.entity;

import mods.invmod.common.nexus.INexusAccess;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;


public class EntityIMSkeleton extends EntityIMMob
{
	private static final ItemStack defaultHeldItem;
	
	public EntityIMSkeleton(World world)
	{
		this(world, null);
	}

    public EntityIMSkeleton(World world, INexusAccess nexus)
    {
        super(world, nexus);
        setBurnsInDay(true);
        texture = "/mob/skeleton.png";
        health = 12;
        setBurnsInDay(true);
        setName("Skeleton");
        setGender(0);
        moveSpeed = 0.42F;
        
        tasks.addTask(0, new EntityAIKillWithArrow<EntityPlayer>(this, EntityPlayer.class, 65, 16F));
        tasks.addTask(1, new EntityAIKillWithArrow<EntityLiving>(this, EntityLiving.class, 65, 16F));
        tasks.addTask(2, new EntityAIAttackNexus(this));
        tasks.addTask(3, new EntityAIGoToNexus(this));
        tasks.addTask(4, new EntityAIWanderIM(this, moveSpeed));
        tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8F));
        tasks.addTask(5, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(2, new EntityAISimpleTarget(this, EntityPlayer.class, 16F, true));
        
        // Set to follow creepers
 		tasks.addTask(0, new EntityAIRallyBehindEntity<EntityIMCreeper>(this, EntityIMCreeper.class, 4));
 		tasks.addTask(8, new EntityAIWatchClosest(this, EntityIMCreeper.class, 12F));
     	targetTasks.addTask(0, new EntityAISimpleTarget(this, EntityPlayer.class, 6F, true));
        targetTasks.addTask(1, new EntityAILeaderTarget(this, EntityIMCreeper.class, 10F, true));
    }

    @Override
	protected String getLivingSound()
    {
        return "mob.skeleton";
    }

    @Override
	protected String getHurtSound()
    {
        return "mob.skeletonhurt";
    }

    @Override
	protected String getDeathSound()
    {
        return "mob.skeletonhurt";
    }

    @Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        super.writeEntityToNBT(nbttagcompound);
    }

    @Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        super.readEntityFromNBT(nbttagcompound);
    }
    
    // --------- Sparrow API --------- //

 	/**What should this entity be referred to as? (Dog, Cat, Human, Enderman, etc.)*/
 	@Override
	public String getSpecies()
 	{
 		return "Skeleton";
 	}
 	
 	@Override
	public int getTier()
 	{
		return 2;
	}

 	// ------------------------------- //
    
    @Override
	public String toString()
    {
    	return "EntityIMSkeleton#";
    }

    @Override
	protected int getDropItemId()
    {
        return Item.arrow.itemID;
    }

    @Override
	protected void dropFewItems(boolean flag, int bonus)
    {
    	super.dropFewItems(flag, bonus);
        int i = rand.nextInt(3);
        for(int j = 0; j < i; j++)
        {
            dropItem(Item.arrow.itemID, 1);
        }

        i = rand.nextInt(3);
        for(int k = 0; k < i; k++)
        {
            dropItem(Item.bone.itemID, 1);
        }
    }
    
    @Override
	public ItemStack getHeldItem()
    {
        return defaultHeldItem;
    }

    static 
    {
        defaultHeldItem = new ItemStack(Item.bow, 1);
    }
}
