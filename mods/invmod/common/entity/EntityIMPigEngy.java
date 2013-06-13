
package mods.invmod.common.entity;


import java.util.HashMap;
import java.util.Map;

import mods.invmod.common.IBlockAccessExtended;
import mods.invmod.common.INotifyTask;
import mods.invmod.common.TerrainDataLayer;
import mods.invmod.common.mod_Invasion;
import mods.invmod.common.nexus.INexusAccess;
import mods.invmod.common.util.CoordsInt;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;


public class EntityIMPigEngy extends EntityIMMob implements ICanDig
{	
	private static final ItemStack itemLadder;
    private static final ItemStack itemPick;
    private static final ItemStack itemHammer;
    private static final int MAX_LADDER_TOWER_HEIGHT = 4;
    private static final int META_ITEM_ID_HELD = 29;
    private static final int META_SWINGING = 30;
    private static final Map<Integer, ItemStack> itemMap = new HashMap<Integer, ItemStack>();

    
    /**
     * Navigator specialised for the pig engineer. Overrides via {@link #getNavigatorNew()}.
     * This component should have the same lifetime as the object or otherwise be set in
     * a very controlled way.
     */
	private final NavigatorEngy navigator;
	
	/**
     * Compatibility side to the actual navigator component. Overrides via {@link #getNavigator()}. 
     */
	private final PathNavigateAdapter oldNavAdapter;
	
    private int swingTimer;
    private int planks;
    private int askForScaffoldTimer;
    private float supportThisTick;
    private TerrainModifier terrainModifier;
    private TerrainDigger terrainDigger;
    private TerrainBuilder terrainBuilder;
    private ItemStack currentItem;
	
	public EntityIMPigEngy(World world, INexusAccess nexus)
    {
        super(world, nexus);
        
        // Set lifetime navigation component
        IPathSource pathSource = getPathSource();
        pathSource.setSearchDepth(1500);
        pathSource.setQuickFailDepth(1500);
        navigator = new NavigatorEngy(this, pathSource);
        oldNavAdapter = new PathNavigateAdapter(navigator);
        
        // Set terrain modifying components
        terrainModifier = new TerrainModifier(this, 2.8F);
        terrainDigger = new TerrainDigger(this, terrainModifier, 1.0F);
        terrainBuilder = new TerrainBuilder(this, terrainModifier, 1.0F);
        
        texture = "/mods/invmod/textures/pigengT1.png";
        moveSpeed = 0.35F;
        attackStrength = 2;
        health = 11;
        maxHealth = 11;
        selfDamage = 0;
        maxSelfDamage = 0;
        planks = 15;
        maxDestructiveness = 2;
        askForScaffoldTimer = 0;
        
        dataWatcher.addObject(META_ITEM_ID_HELD, (short)0);
        dataWatcher.addObject(META_SWINGING, (byte)0);
        
        setName("Pig Engineer");
        setGender(1);
        setDestructiveness(2);
        setJumpHeight(1);
        setCanClimb(false);
        setAI();
        
        int r = rand.nextInt(3);
        if(r == 0)
        	setCurrentItem(itemHammer);
        else if(r == 1)
        	setCurrentItem(itemPick);
        else // r == 2
        	setCurrentItem(itemLadder);
    }
	
	public EntityIMPigEngy(World world)
    {
    	this(world, null);
    }
	
	protected void setAI()
	{
		tasks = new EntityAITasks(worldObj.theProfiler);
		tasks.addTask(0, new EntityAIKillEntity<EntityPlayer>(this, EntityPlayer.class, 60));
		tasks.addTask(1, new EntityAIAttackNexus(this));
        tasks.addTask(2, new EntityAIGoToNexus(this));
        tasks.addTask(6, new EntityAIWanderIM(this, moveSpeed));
        tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 7F));
        tasks.addTask(8, new EntityAIWatchClosest(this, EntityIMCreeper.class, 12F));
        tasks.addTask(8, new EntityAILookIdle(this));
        
        targetTasks = new EntityAITasks(worldObj.theProfiler);
		targetTasks.addTask(2, new EntityAISimpleTarget(this, EntityPlayer.class, 3F, true));
		targetTasks.addTask(3, new EntityAIHurtByTarget(this, false));
	}
	
	@Override
	public void updateAITasks()
	{
		super.updateAITasks();
		terrainModifier.onUpdate();
	}
	
	@Override
	public void updateAITick()
	{
		super.updateAITick();
		terrainBuilder.setBuildRate(1.0F + supportThisTick * 0.33F);
		//System.out.println(supportThisTick);
		supportThisTick = 0;
		
		askForScaffoldTimer--;
		if(targetNexus != null)
		{
			int weight = Math.max(6000 / targetNexus.getYCoord() - getYCoord(), 1);
			if(currentGoal == Goal.BREAK_NEXUS && ((getNavigatorNew().getLastPathDistanceToTarget() > 2 && askForScaffoldTimer <= 0) || rand.nextInt(weight) == 0))
			{
				if(targetNexus.getAttackerAI().askGenerateScaffolds(this))
				{
					getNavigatorNew().clearPath();
					askForScaffoldTimer = 60;
				}
				else
				{
					askForScaffoldTimer = 140;
				}
			}
		}
	}
	
	@Override
	public void onLivingUpdate()
	{
		super.onLivingUpdate();
		updateAnimation();
	}
	
	@Override
	public void onPathSet()
    {
		terrainModifier.cancelTask();
    }
	
	/**
     * Compatibility method for base minecraft classes. Semi-compatible adapter.
     * Setting paths and updating is preserved, but setting or getting other
     * internal state is not, such as getting PathEntity objects.
     * 
     * Use {@link #getNavigatorNew()} instead.
     * 
     * Underlying object should be the same as {@link #getNavigatorNew()}. No
     * guarantees are made about compatibility.
     */
    @Override
	public PathNavigateAdapter getNavigator()
    {
    	return oldNavAdapter;
    }
    
    /**
     * Returns the navigation component this entity uses.
     */
    @Override
	public INavigation getNavigatorNew()
    {
    	return navigator;
    }
    
    @Override
	public IBlockAccess getTerrain()
	{
		return worldObj;
	}
    
    @Override
	protected boolean onPathBlocked(Path path, INotifyTask notifee)
    {
    	if(!path.isFinished())
    	{
	    	PathNode node = path.getPathPointFromIndex(path.getCurrentPathIndex());
	    	return terrainDigger.askClearPosition(node.xCoord, node.yCoord, node.zCoord, notifee, 1.0F);
    	}
    	return false;
    }
	
	protected ITerrainBuild getTerrainBuildEngy()
	{
		return terrainBuilder;
	}
	
	protected ITerrainDig getTerrainDig()
	{
		return terrainDigger;
	}

    @Override
	protected String getLivingSound()
    {
        return "mob.zombiepig.zpig";
    }

    @Override
	protected String getHurtSound()
    {
        return "mob.zombiepig.zpighurt";
    }

    @Override
	protected String getDeathSound()
    {
        return "mob.pig.death";
    }
    
    // --------- Sparrow API --------- //

 	/**What should this entity be referred to as? (Dog, Cat, Human, Enderman, etc.)*/
 	@Override
	public String getSpecies()
 	{
 		return "Pigman";
 	}
 	
 	@Override
	public int getTier()
 	{
		return 2;
	}

 	// ------------------------------- //
    
    @Override
	public float getBlockRemovalCost(int x, int y, int z)
    {
    	return getBlockStrength(x, y, z) * 20F;
    }
    
    @Override
	public boolean canClearBlock(int x, int y, int z)
    {
    	int id = worldObj.getBlockId(x, y, z);
    	return id == 0 || isBlockDestructible(worldObj, x, y, z, id);
    }
    
    @Override
	public boolean avoidsBlock(int id)
    {
    	if(id == 51 || id == 7 || id == 64 || id == 8 || id == 9 || id == 10 || id == 11)
    	{
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }
    
    @Override
	public boolean isBlockDestructible(IBlockAccess terrainMap, int x, int y, int z, int id)
    {
    	return isBlockTypeDestructible(id);
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
    
    public void supportForTick(EntityIMLiving entity, float amount)
    {
    	supportThisTick += amount;
    }
    
    @Override
	public boolean canBePushed()
    {
    	return false;
    	//return worldObj.getBlockId(getXCoord(), getYCoord(), getZCoord()) != Block.ladder.blockID;
    }
    
    // Handle costs for enginner
    @Override
	public float getBlockPathCost(PathNode prevNode, PathNode node, IBlockAccess terrainMap)
    {
    	if(node.xCoord == -21 && node.zCoord == 180)
    		planks = 10;
    	
    	int id = terrainMap.getBlockId(node.xCoord, node.yCoord, node.zCoord);
    	float materialMultiplier = id != 0 && isBlockDestructible(terrainMap, node.xCoord, node.yCoord, node.zCoord, id) ? 3.2F : 1.0F;
    	
    	if(node.action == PathAction.BRIDGE)
    		return prevNode.distanceTo(node) * 1.7F * materialMultiplier;
    	else if(node.action == PathAction.SCAFFOLD_UP)
    		return prevNode.distanceTo(node) * 0.5F;
    	else if(node.action == PathAction.LADDER_UP_NX || node.action == PathAction.LADDER_UP_NZ || node.action == PathAction.LADDER_UP_PX || node.action == PathAction.LADDER_UP_PZ)
    		return prevNode.distanceTo(node) * 1.3F * materialMultiplier;
    	else if(node.action == PathAction.LADDER_TOWER_UP_PX || node.action == PathAction.LADDER_TOWER_UP_NX || node.action == PathAction.LADDER_TOWER_UP_PZ || node.action == PathAction.LADDER_TOWER_UP_NZ)
    		return prevNode.distanceTo(node) * 1.4F;
    	
    		
    	
    	float multiplier = 1.0F;
		if(terrainMap instanceof IBlockAccessExtended)
		{
			int mobDensity = ((IBlockAccessExtended)terrainMap).getLayeredData(node.xCoord, node.yCoord, node.zCoord) & 7;
			multiplier += mobDensity;
		}
    	if(id == 0)
    	{	
    		return prevNode.distanceTo(node) * AIR_BASE_COST * multiplier;
        }
    	else if(id == Block.snow.blockID)
    	{
    		return prevNode.distanceTo(node) * AIR_BASE_COST  * multiplier;
    	}
    	else if(id == Block.ladder.blockID)
    	{
    		return prevNode.distanceTo(node) * AIR_BASE_COST * 0.7F * multiplier;
    	}
    	else if(!Block.blocksList[id].getBlocksMovement(terrainMap, node.xCoord, node.yCoord, node.zCoord) && id != mod_Invasion.blockNexus.blockID)
    	{
    		return prevNode.distanceTo(node) * 3.2F;
    	}
    	else
    	{
    		return super.getBlockPathCost(prevNode, node, terrainMap);
    	}
    }
    
    @Override
	public void getPathOptionsFromNode(IBlockAccess terrainMap, PathNode currentNode, PathfinderIM pathFinder)
    {
    	super.getPathOptionsFromNode(terrainMap, currentNode, pathFinder);
    	if(planks <= 0)
    		return;
    	
    	// Find adjacent points that have air under them for bridging
		for(int i = 0; i < 4; i++)
    	{
			// Check if adjacent point is clear at y == 0
			if(getCollide(terrainMap, currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord, currentNode.zCoord + CoordsInt.offsetAdjZ[i]) <= 0)
				continue;
			
			// Iterate downwards, adding points. Break if solid block found
			for(int yOffset = 0; yOffset > -4; yOffset--)
			{
				int id = terrainMap.getBlockId(currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord - 1 + yOffset, currentNode.zCoord + CoordsInt.offsetAdjZ[i]);
				if(id == 0)
					pathFinder.addNode(currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord + yOffset, currentNode.zCoord + CoordsInt.offsetAdjZ[i], PathAction.BRIDGE);
				else
					break;
			}
    	}
    }
    
    @Override
	protected void calcPathOptionsVertical(IBlockAccess terrainMap, PathNode currentNode, PathfinderIM pathFinder)
    {
    	if(currentNode.xCoord == -11 && currentNode.zCoord == 177)
    		planks = 10;
    	
    	super.calcPathOptionsVertical(terrainMap, currentNode, pathFinder);
    	if(planks <= 0)
    		return;
    	
    	// Find if y+1 is a valid point to ladder up to
		if( /*currentNode.previous.action != PathAction.SCAFFOLD_UP &&*/ getCollide(terrainMap, currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord) > 0)
		{
			if(terrainMap.getBlockId(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord) == 0)
			{
				if(currentNode.action == PathAction.NONE)
				{
					addAnyLadderPoint(terrainMap, currentNode, pathFinder);
				}
				else
				{
					if(!continueLadder(terrainMap, currentNode, pathFinder))
					{
						addAnyLadderPoint(terrainMap, currentNode, pathFinder);
					}
				}
			}
			
			if(currentNode.action == PathAction.NONE || currentNode.action == PathAction.BRIDGE)
			{
				// Find if entity can build a ladder tower
				// First find maximum clear height
				int maxHeight = MAX_LADDER_TOWER_HEIGHT;
				for(int i = getCollideSize().getYCoord(); i < MAX_LADDER_TOWER_HEIGHT; i++)
				{
					int id = terrainMap.getBlockId(currentNode.xCoord, currentNode.yCoord + i, currentNode.zCoord);
					if(id > 0 && !Block.blocksList[id].getBlocksMovement(terrainMap, currentNode.xCoord, currentNode.yCoord + i, currentNode.zCoord))
					{
						maxHeight = i - getCollideSize().getYCoord();
						break;
					}
				}
				
				// Check each adjacent column of blocks from the bottom up. Stop when at max height or found
				// a non-air block. If block can have a ladder placed on it, this column is valid for a
				// ladder tower. Add point y+1 with correctly oriented ladder tower action.
				for(int i = 0; i < 4; i++)
				{
					// Needs to be based on an existing solid block
					int id = terrainMap.getBlockId(currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord - 1, currentNode.zCoord + CoordsInt.offsetAdjZ[i]);
					if(!Block.isNormalCube(id))
						continue;
					
					for(int height = 0; height < maxHeight; height++)
					{
						id = terrainMap.getBlockId(currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord + height, currentNode.zCoord + CoordsInt.offsetAdjZ[i]);
						if(id == 0)
						{
							continue;
						}
						else
						{
							if(Block.isNormalCube(id))
								pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.ladderTowerIndexOrient[i]);
							
							break;
						}
					}
				}
			}
		}
		
		if(terrainMap instanceof IBlockAccessExtended)
		{
			int data = ((IBlockAccessExtended)terrainMap).getLayeredData(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord);
			if(data == TerrainDataLayer.EXT_DATA_SCAFFOLD_METAPOSITION)
			{
				pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.SCAFFOLD_UP);
			}
		}
    }
    
    protected void addAnyLadderPoint(IBlockAccess terrainMap, PathNode currentNode, PathfinderIM pathFinder)
    {
    	for(int i = 0; i < 4; i++)
		{
			if(terrainMap.isBlockNormalCube(currentNode.xCoord + CoordsInt.offsetAdjX[i], currentNode.yCoord + 1, currentNode.zCoord + CoordsInt.offsetAdjZ[i]))
				pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.ladderIndexOrient[i]);
		}
    }
    
    /**
     * Returns true if previous node was a ladder and adds the next ladder node if valid
     */
    protected boolean continueLadder(IBlockAccess terrainMap, PathNode currentNode, PathfinderIM pathFinder)
    {
    	switch(currentNode.action)
    	{
    	case LADDER_UP_PX:
    		if(terrainMap.isBlockNormalCube(currentNode.xCoord + 1, currentNode.yCoord + 1, currentNode.zCoord))
    		{
				pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.LADDER_UP_PX);
    		}
    		return true;
    	case LADDER_UP_NX:
    		if(terrainMap.isBlockNormalCube(currentNode.xCoord - 1, currentNode.yCoord + 1, currentNode.zCoord))
    		{
				pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.LADDER_UP_NX);
    		}
    		return true;
    	case LADDER_UP_PZ:
    		if(terrainMap.isBlockNormalCube(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord + 1))
    		{
				pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.LADDER_UP_PZ);
    		}
    		return true;
    	case LADDER_UP_NZ:
    		if(terrainMap.isBlockNormalCube(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord - 1))
    		{
				pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.LADDER_UP_NZ);
    		}
    		return true;
    	}
    	
    	return false;
    }
   
    @Override
	public ItemStack getHeldItem()
    {
        return getCurrentItem();
    }
    
    @Override
	protected void dropFewItems(boolean flag, int bonus)
    {
    	super.dropFewItems(flag, bonus);
    	if(rand.nextInt(2) == 0)
    	{
    		entityDropItem(new ItemStack(Item.leather, 1, 0), 0.0F);
    	}
    	else
    	{
	        if(isBurning())
	        	entityDropItem(new ItemStack(Item.porkCooked, 1, 0), 0.0F);
	        else
	        	entityDropItem(new ItemStack(Item.porkRaw, 1, 0), 0.0F);
    	}
    }
    
    protected void updateAnimation()
    {
    	if(!worldObj.isRemote && terrainModifier.isBusy())
    	{
    		setSwinging(true);
    		PathAction currentAction = getNavigatorNew().getCurrentWorkingAction();
    		if(currentAction == PathAction.NONE) // Includes digging
        		setCurrentItem(itemPick);
        	else
        		setCurrentItem(itemHammer);
    	}
    	
    	int swingSpeed = getSwingSpeed();
    	if(isSwinging())
    	{
    		swingTimer++;
            if (swingTimer >= swingSpeed)
            {
                swingTimer = 0;
                setSwinging(false);
            }
    	}
    	else
    	{
    		swingTimer = 0;
    	}
    	
    	swingProgress = (float)swingTimer / (float)swingSpeed;
    }
    
    protected boolean isSwinging()
    {
    	return getDataWatcher().getWatchableObjectByte(META_SWINGING) != (byte)0;
    }
    
    protected void setSwinging(boolean flag)
    {
    	getDataWatcher().updateObject(META_SWINGING, flag == true ? (byte)1 : (byte)0);
    }
    
    protected int getSwingSpeed()
    {
    	return 10;
    }
    
    protected ItemStack getCurrentItem()
    {
    	if(worldObj.isRemote)
    	{
    		int id = getDataWatcher().getWatchableObjectShort(META_ITEM_ID_HELD);
    		if(id != currentItem.itemID)
    			currentItem = itemMap.get(id);
    	}
    	return currentItem;
    }
    
    protected void setCurrentItem(ItemStack item)
    {
    	currentItem = item;
    	getDataWatcher().updateObject(META_ITEM_ID_HELD, (short)item.itemID);
    }
    
    public static boolean canPlaceLadderAt(IBlockAccess map, int x, int y, int z)
    {
    	if(map.isBlockNormalCube(x + 1, y, z) || map.isBlockNormalCube(x - 1, y, z)
    		|| map.isBlockNormalCube(x, y, z + 1) || map.isBlockNormalCube(x, y, z - 1))
    		return true;
    	else
    		return false;
    }
    
    static 
    {
        itemLadder = new ItemStack(Block.ladder, 1);
        itemPick = new ItemStack(Item.pickaxeIron, 1);
        itemHammer = mod_Invasion.getRenderHammerItem();
        itemMap.put(itemLadder.itemID, itemLadder);
        itemMap.put(itemPick.itemID, itemPick);
        itemMap.put(itemHammer.itemID, itemHammer);
    }
}
