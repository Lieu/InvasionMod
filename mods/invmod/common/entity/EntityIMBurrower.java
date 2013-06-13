package mods.invmod.common.entity;

import mods.invmod.common.INotifyTask;
import mods.invmod.common.nexus.INexusAccess;
import mods.invmod.common.util.PosRotate3D;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class EntityIMBurrower extends EntityIMMob implements ICanDig
{
	public static final int NUMBER_OF_SEGMENTS = 16;
	
	/**
     * Navigator specialised for the pig engineer. Overrides via {@link #getNavigatorNew()}.
     * This component should have the same lifetime as the object or otherwise be set in
     * a very controlled way.
     */
	private final NavigatorBurrower navigator;
	
	/**
     * Compatibility side to the actual navigator component. Overrides via {@link #getNavigator()}. 
     */
	private final PathNavigateAdapter oldNavAdapter;

	private TerrainModifier terrainModifier;
    private TerrainDigger terrainDigger;
	private EntityAITasks goals;
	private PosRotate3D[] segments3D;
	private PosRotate3D[] segments3DLastTick;
	private float rotX;
	private float rotY;
	private float rotZ;
	protected float prevRotX;
	protected float prevRotY;
	protected float prevRotZ;
	
	public EntityIMBurrower(World world)
    {
        this(world, null);
    }
	
	public EntityIMBurrower(World world, INexusAccess nexus)
    {
        super(world, nexus);
        
        // Set lifetime navigation component
        IPathSource pathSource = getPathSource();
        pathSource.setSearchDepth(800);
        pathSource.setQuickFailDepth(400);
        navigator = new NavigatorBurrower(this, pathSource, NUMBER_OF_SEGMENTS, -4);
        oldNavAdapter = new PathNavigateAdapter(navigator);
        
        // Set terrain modifying components
        terrainModifier = new TerrainModifier(this, 2.0F);
        terrainDigger = new TerrainDigger(this, terrainModifier, 1.0F);
        
        texture = "/mods/invmod/textures/boulder.png";
        setName("Burrower");
        setGender(0);
        setSize(0.5f, 0.5f);
        setJumpHeight(0);
        setCanClimb(true);
        setDestructiveness(2);
        maxDestructiveness = 2;
        blockRemoveSpeed = 0.5f;
        

        // TODO
        // 1. Task list of executable, parallelisable stuff
        // 2. High-level goal as entity state
        // 3. Advanced navigator
        // 4. Path clearing component
        
    	segments3D = new PosRotate3D[NUMBER_OF_SEGMENTS];
    	segments3DLastTick = new PosRotate3D[NUMBER_OF_SEGMENTS];
    	
    	PosRotate3D zero = new PosRotate3D();
    	for(int i = 0; i < NUMBER_OF_SEGMENTS; i++)
    	{
    		segments3D[i] = zero;
    		segments3DLastTick[i] = zero;
    	}
    }
	
	/*public Entity findEntityToAttack()
	{
		return null;
	}*/

	@Override
	public String toString()
    {
    	return "EntityIMBurrower#" + "u" + "-" + "u" + "-" + "u";
    }
	
	@Override
	public IBlockAccess getTerrain()
	{
		return worldObj;
	}
	
	public float getBlockPathCost(PathPoint prevNode, PathPoint node, IBlockAccess worldMap)
    {
    	int id = worldMap.getBlockId(node.xCoord, node.yCoord, node.zCoord);
    	
    	// Burrower prefers solid blocks surrounding it
    	float penalty = 0;
    	int enclosedLevelSide = 0;    	
		if(!worldObj.isBlockOpaqueCube(node.xCoord, node.yCoord - 1, node.zCoord)) { penalty += 0.3F; }
		if(!worldObj.isBlockOpaqueCube(node.xCoord, node.yCoord + 1, node.zCoord)) { penalty += 2; }
		if(!worldObj.isBlockOpaqueCube(node.xCoord + 1, node.yCoord, node.zCoord)) { enclosedLevelSide++; }
		if(!worldObj.isBlockOpaqueCube(node.xCoord - 1, node.yCoord, node.zCoord)) { enclosedLevelSide++; }
		if(!worldObj.isBlockOpaqueCube(node.xCoord, node.yCoord, node.zCoord + 1)) { enclosedLevelSide++; }
		if(!worldObj.isBlockOpaqueCube(node.xCoord, node.yCoord, node.zCoord - 1)) { enclosedLevelSide++; }
		
		if(enclosedLevelSide > 2)
			enclosedLevelSide = 2;
		
		penalty += enclosedLevelSide * 0.5F;
    	
		// Tweak costs to ensure all solid underground blocks are preferable to above ground air, but not underground air
		if(id == 0)
		{
			return prevNode.distanceTo(node) * AIR_BASE_COST * penalty;
		}
		else if(blockCosts.containsKey(id))
    	{
			return prevNode.distanceTo(node) * AIR_BASE_COST * 1.3f * penalty;
    	}
    	else if(Block.blocksList[id].isCollidable())
    	{
    		return prevNode.distanceTo(node) * AIR_BASE_COST * 1.3f * penalty;
    	}
    	else
    	{
    		return prevNode.distanceTo(node) * AIR_BASE_COST * penalty;
    	}
    }
	
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
    
    // --------- Sparrow API --------- //

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
	
	// ------------------------------- //
    
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
    
    protected boolean onPathBlocked(int x, int y, int z, INotifyTask notifee)
    {
		if(terrainDigger.askClearPosition(x, y, z, notifee, 1.0F))
			return true;
		
		return false;
    }
	
	public float getRotX()
	{
		return rotX;
	}
	
	public float getRotY()
	{
		return rotY;
	}
	
	public float getRotZ()
	{
		return rotZ;
	}
	
	public float getPrevRotX()
	{
		return prevRotX;
	}
	
	public float getPrevRotY()
	{
		return prevRotY;
	}
	
	public float getPrevRotZ()
	{
		return prevRotZ;
	}
	
	public PosRotate3D[] getSegments3D()
	{
		return segments3D;
	}
	
	public PosRotate3D[] getSegments3DLastTick()
	{
		return segments3DLastTick;
	}
	
	public void setSegment(int index, PosRotate3D pos)
	{
		if(index < segments3D.length)
		{
			segments3DLastTick[index] = segments3D[index];
			segments3D[index] = pos;
		}
	}
	
	public void setHeadRotation(PosRotate3D pos)
	{
		prevRotX = rotX;
		prevRotY = rotY;
		prevRotZ = rotZ;
		rotX = pos.getRotX();
		rotY = pos.getRotY();
		rotZ = pos.getRotZ();
	}
	
	@Override
	public void moveEntityWithHeading(float x, float z)
    {
        if(isInWater())
        {
            double y = posY;
            moveFlying(x, z, 0.02F);
            moveEntity(motionX, motionY, motionZ);
            motionX *= 0.8;
            motionY *= 0.8;
            motionZ *= 0.8;
            motionY -= 0.02;
            if(isCollidedHorizontally && isOffsetPositionInLiquid(motionX, ((motionY + 0.6) - posY) + y, motionZ))
                motionY = 0.3;
        }
        else if(handleLavaMovement())
        {
            double y = posY;
            moveFlying(x, z, 0.02F);
            moveEntity(motionX, motionY, motionZ);
            motionX *= 0.5;
            motionY *= 0.5;
            motionZ *= 0.5;
            motionY -= 0.02;
            if(isCollidedHorizontally && isOffsetPositionInLiquid(motionX, ((motionY + 0.6) - posY) + y, motionZ))
                motionY = 0.3;
        }
        else
        {
        	float groundFriction = 1.0F; // Assume starting in air
            if(onGround)
            {
                groundFriction = 0.546F; 
                int i = worldObj.getBlockId(MathHelper.floor_double(posX), MathHelper.floor_double(boundingBox.minY) - 1, MathHelper.floor_double(posZ));
                if(i > 0)
                    groundFriction = Block.blocksList[i].slipperiness * 0.91F;
            }
            
            if(isOnLadder())
            {
                float maxLadderXZSpeed = 0.15F;
                if(motionX < (-maxLadderXZSpeed))
                    motionX = -maxLadderXZSpeed;               
                if(motionX > maxLadderXZSpeed)
                    motionX = maxLadderXZSpeed;
                if(motionZ < (-maxLadderXZSpeed))
                    motionZ = -maxLadderXZSpeed;  
                if(motionZ > maxLadderXZSpeed)
                    motionZ = maxLadderXZSpeed;
                
                fallDistance = 0.0F;
                if(motionY < -0.15)
                    motionY = -0.15;
                
                if(isSneaking() && motionY < 0)
                    motionY = 0;
            }
            
            moveEntity(motionX, motionY, motionZ);    
            if(isCollidedHorizontally && isOnLadder())
                motionY = 0.2;
            
            float airResistance = 0.98F;
            float gravityAcel = 0.00F;
            motionY -= gravityAcel;
            motionY *= airResistance;
            motionX *= airResistance;
            motionZ *= airResistance;
        }
        
        prevLimbYaw = limbYaw;
        double dX = posX - prevPosX;
        double dZ = posZ - prevPosZ;
        float f4 = MathHelper.sqrt_double(dX * dX + dZ * dZ) * 4F;

        if (f4 > 1.0F)
        {
            f4 = 1.0F;
        }

        limbYaw += (f4 - limbYaw) * 0.4F;
        limbSwing += limbYaw;
    }
	
	@Override
	protected void updateAITasks()
	{
        super.updateAITasks();
        terrainModifier.onUpdate();
	}
	
	@Override
	public void updateAITick()
	{
		super.updateAITick();
		//System.out.println(posX + ", " + posZ);
		/*Path path = getIMNavigator().getPath();
		if(path == null || getIMNavigator().noPath())
		{
			PathNode[] points2 = new PathNode[] { new PathNode(246, 65, 316), new PathNode(246, 65, 317), new PathNode(246, 65, 318), new PathNode(247, 65, 318), new PathNode(247, 66, 318), new PathNode(248, 66, 318), new PathNode(248, 66, 317), new PathNode(248, 65, 317), new PathNode(248, 65, 316), new PathNode(247, 65, 316),
					new PathNode(246, 65, 316), new PathNode(246, 65, 317)
			};
			
			PathNode[] points3 = new PathNode[] { new PathNode(246, 68, 316), new PathNode(246, 68, 317), new PathNode(246, 68, 318), new PathNode(247, 68, 318), new PathNode(247, 69, 318), new PathNode(248, 69, 318), new PathNode(248, 69, 317), new PathNode(248, 68, 317), new PathNode(248, 68, 316), new PathNode(247, 68, 316),
					new PathNode(246, 68, 316), new PathNode(246, 68, 317)
			};
			
			getIMNavigator().setPath(new Path(points2), 0.1F);
		}*/
	}
	
	
}
