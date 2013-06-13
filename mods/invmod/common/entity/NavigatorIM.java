package mods.invmod.common.entity;

import mods.invmod.common.INotifyTask;
import mods.invmod.common.entity.IPathSource.PathPriority;
import mods.invmod.common.nexus.INexusAccess;
import mods.invmod.common.util.CoordsInt;
import mods.invmod.common.util.Distance;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class NavigatorIM implements INotifyTask, INavigation
{
	protected static final int XZPATH_HORIZONTAL_SEARCH = 1;
	protected static final double ENTITY_TRACKING_TOLERANCE = 0.1;
    protected static final double MINIMUM_PROGRESS = 0.01;
    
	protected final EntityIMLiving theEntity;
	protected IPathSource pathSource;
    protected Path path;
    protected PathNode activeNode;
    protected Vec3 entityCentre;
    protected Entity pathEndEntity;
    protected Vec3 pathEndEntityLastPos;
    protected float moveSpeed;
    protected float pathSearchLimit;
    protected boolean noSunPathfind;
    protected int totalTicks;
    protected Vec3 lastPos;
    private Vec3 holdingPos;
    protected boolean nodeActionFinished;
    private boolean canSwim;
    protected boolean waitingForNotify;
    protected boolean actionCleared;
    protected double lastDistance;
    protected int ticksStuck;
    private boolean maintainPosOnWait;
    private int lastActionResult;
    private boolean haltMovement;
    private boolean autoPathToEntity;
    
    public NavigatorIM(EntityIMLiving entity, IPathSource pathSource)
    {
    	theEntity = entity;
    	this.pathSource = pathSource;
        noSunPathfind = false;
        lastPos = Vec3.createVectorHelper(0.0D, 0.0D, 0.0D);
        pathEndEntityLastPos = Vec3.createVectorHelper(0.0D, 0.0D, 0.0D);
        lastDistance = 0;
        ticksStuck = 0;
        canSwim = false;
        waitingForNotify = false;
        actionCleared = true;
        nodeActionFinished = true;
        maintainPosOnWait = false;
        haltMovement = false;
        lastActionResult = 0;
        autoPathToEntity = false;
    }
    
    @Override
	public PathAction getCurrentWorkingAction()
    {
    	if(!nodeActionFinished && !noPath())
    		return activeNode.action;
    	else
    		return PathAction.NONE;
    }
    
    protected boolean isMaintainingPos()
    {
    	return maintainPosOnWait;
    }
    
    protected void setNoMaintainPos()
    {
    	maintainPosOnWait = false;
    }
    
    protected void setMaintainPosOnWait(Vec3 pos)
    {
    	holdingPos = pos;
    	maintainPosOnWait = true;
    }

    @Override
	public void setSpeed(float par1)
    {
        moveSpeed = par1;
    }
    
    public boolean isAutoPathingToEntity()
    {
    	return autoPathToEntity;
    }
    
    @Override
    public Entity getTargetEntity()
    {
    	return pathEndEntity;
    }

    @Override
	public Path getPathToXYZ(double x, double y, double z)
    {
        if (!canNavigate())
            return null;
        else
        	return createPath(theEntity, MathHelper.floor_double(x), (int)y, MathHelper.floor_double(z));
    }

    @Override
	public boolean tryMoveToXYZ(double x, double y, double z, float speed)
    {
    	ticksStuck = 0;
        Path newPath = getPathToXYZ(MathHelper.floor_double(x), (int)y, MathHelper.floor_double(z));
        if (newPath != null)
            return setPath(newPath, speed);
        else
            return false;
    }
    
    /**
     * Try and find a path to walk a random distance between min and max in the
     * direction of [x, z] as absolute coordinates.
     */
    @Override
	public Path getPathTowardsXZ(double x, double z, int min, int max, int verticalRange)
    {
    	if(canNavigate())
    	{
    		Vec3 target = findValidPointNear(x, z, min, max, verticalRange);
    		if(target != null)
    		{
    			Path entityPath = getPathToXYZ(target.xCoord, target.yCoord, target.zCoord);
				if(entityPath != null)
        			return entityPath;
    		}
    	}
		return null;
    }
    
    @Override
	public boolean tryMoveTowardsXZ(double x, double z, int min, int max, int verticalRange, float speed)
    {
    	ticksStuck = 0;
    	Path newPath = getPathTowardsXZ(MathHelper.floor_double(x), MathHelper.floor_double(z), min, max, verticalRange);
    	if (newPath != null)
            return setPath(newPath, speed);
        else
            return false;
    }
    
    @Override
	public Path getPathToEntity(Entity targetEntity)
    {
        if (!canNavigate())
            return null;
        else
            return createPath(theEntity, MathHelper.floor_double(targetEntity.posX), MathHelper.floor_double(targetEntity.boundingBox.minY), MathHelper.floor_double(targetEntity.posZ));
    }

    @Override
	public boolean tryMoveToEntity(Entity targetEntity, float speed)
    {
        Path newPath = getPathToEntity(targetEntity);
        if (newPath != null)
        {
        	if(setPath(newPath, speed))
        	{
        		pathEndEntity = targetEntity;
        		return true;
        	}
        	else
        	{
        		pathEndEntity = null;
        		return false;
        	}
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Moves to the specified entity, updating as the entity changes position or
     * if other factors change. The pathing stops after clearPath() is called.
     * This method may choose paths when and where it deems appropriate.
     * 
     * Some factors include:
     * - If the target entity moves
     * - If the target dies or changes state
     * - The pathfinding load on the CPU, as mediated by a PathSource object
     * - The movement preferences this object has been set to
     * 
     * @See clearPath()
     * 
     * @param target The target entity
     */
    @Override
    public void autoPathToEntity(Entity target)
    {
    	autoPathToEntity = true;
    	pathEndEntity = target;
    }
    
    /**
     * Sets the active path if valid
     */
    @Override
	public boolean setPath(Path newPath, float speed)
    {
        if(newPath == null)
        {
            path = null;
            theEntity.onPathSet();
            return false;
        }
        
        moveSpeed = speed;
        lastDistance = getDistanceToActiveNode();
        ticksStuck = 0;
        resetStatus();
        
        CoordsInt size = theEntity.getCollideSize();
        entityCentre = Vec3.createVectorHelper(size.getXCoord() * 0.5D, 0, size.getZCoord() * 0.5D);
        
        path = newPath;
        activeNode = path.getPathPointFromIndex(path.getCurrentPathIndex());
        
        if(activeNode.action != PathAction.NONE)
        {
        	nodeActionFinished = false;
        }
        else
        {
        	if(size.getXCoord() <= 1 && size.getZCoord() <= 1)
        	{
        		path.incrementPathIndex();
        		if(!path.isFinished())
        		{
	        		activeNode = path.getPathPointFromIndex(path.getCurrentPathIndex());
		        	if(activeNode.action != PathAction.NONE)
		        	{
		        		nodeActionFinished = false;
		        	}
        		}
        	}
        	else
        	{
		        while(theEntity.getDistanceSq(activeNode.xCoord + entityCentre.xCoord, activeNode.yCoord + entityCentre.yCoord, activeNode.zCoord + entityCentre.zCoord) < theEntity.width)
		        {
		        	path.incrementPathIndex();
		        	if(path.isFinished())
		        		break;
		        	
		        	activeNode = path.getPathPointFromIndex(path.getCurrentPathIndex());
		        	if(activeNode.action != PathAction.NONE)
		        	{
		        		nodeActionFinished = false;
		        		break;
		        	}
		        }
        	}
        }
        
        if (noSunPathfind)
        {
            removeSunnyPath();
        }
        
        theEntity.onPathSet();
        return true;
    }

    /**
     * gets the actively used PathEntity
     */
    @Override
	public Path getPath()
    {
        return path;
    }
    
    @Override
	public boolean isWaitingForTask()
    {
    	return waitingForNotify;
    }

    @Override
	public void onUpdateNavigation()
    {
    	totalTicks++;
    	if(autoPathToEntity)
    	{
    		updateAutoPathToEntity();
    	}
    	
        if(noPath())
        {
        	noPathFollow();
            return;
        }
        
        // Continue to hold/move to a special position while waiting for task, if applicable
        if(waitingForNotify)
        {
        	if(isMaintainingPos())
        		theEntity.getMoveHelper().setMoveTo(holdingPos.xCoord, holdingPos.yCoord, holdingPos.zCoord, moveSpeed);
        	
        	return;
        }
        
        // Update path nodes based on position
        if (canNavigate() && nodeActionFinished)
        {
        	double distance = getDistanceToActiveNode();
        	if(lastDistance - distance > MINIMUM_PROGRESS)
        	{
        		lastDistance = distance;
        		ticksStuck--;
        	}
        	else
        	{
        		ticksStuck++;
        	}
        	
        	int pathIndex = path.getCurrentPathIndex();
            pathFollow();
            if (noPath())
                return;
            
            if(path.getCurrentPathIndex() != pathIndex)
            {
            	lastDistance = getDistanceToActiveNode();
                ticksStuck = 0;
	            activeNode = path.getPathPointFromIndex(path.getCurrentPathIndex());
	            if(activeNode.action != PathAction.NONE)
	            	nodeActionFinished = false;
            }
        }
        
        // Check how to proceed. Either continue movement or delegate to action processing
        if(nodeActionFinished)
        {
        	if(!isPositionClearFrom(theEntity.getXCoord(), theEntity.getYCoord(), theEntity.getZCoord(), activeNode.xCoord, activeNode.yCoord, activeNode.zCoord, theEntity))
        	{
        		if(theEntity.onPathBlocked(path, this))
        		{
        			setDoingTaskAndHoldOnPoint();
        		}
        		else
        		{
        			//clearPathEntity();
        		}
        	}
        	
        	if(!haltMovement)
        	{
        		// If within certain distance of entity (and not below), go straight to entity
	        	if(pathEndEntity != null && pathEndEntity.posY - theEntity.posY <= 0 && theEntity.getDistanceSq(pathEndEntity.posX, pathEndEntity.boundingBox.minY, pathEndEntity.posZ) < 4.5)
	        		theEntity.getMoveHelper().setMoveTo(pathEndEntity.posX, pathEndEntity.boundingBox.minY, pathEndEntity.posZ, moveSpeed);
	        	else
	        		theEntity.getMoveHelper().setMoveTo(activeNode.xCoord + entityCentre.xCoord, activeNode.yCoord + entityCentre.yCoord, activeNode.zCoord + entityCentre.zCoord, moveSpeed);
        	}
        	else
        	{
        		haltMovement = false;
        	}
        }
        else
        {
        	if(!handlePathAction())
        		clearPath();
        }
    }
    
    @Override
	public void notifyTask(int result)
    {
    	waitingForNotify = false;
    	lastActionResult = result;
    }
    
    @Override
	public int getLastActionResult()
    {
    	return lastActionResult;
    }

    /**
     * If null path or reached the end
     */
    @Override
	public boolean noPath()
    {
        return path == null || path.isFinished();
    }
    
    /**
     * Returns the number of ticks entity has been considered by
     * the navigator to have made no progress
     */
    @Override
	public int getStuckTime()
    {
    	return ticksStuck;
    }
    
    /**
     * Returns the distance between the end of the most recent path and that path's original target location
     */
    @Override
	public float getLastPathDistanceToTarget()
    {
    	if(noPath())
    	{
    		return 0;
    	}
    	else
    	{
    		return path.getFinalPathPoint().distanceTo(path.getIntendedTarget());
    	}
    }

    /**
     * sets active PathEntity to null
     */
    @Override
	public void clearPath()
    {
        path = null;
        autoPathToEntity = false;
        resetStatus();
    }
    
    @Override
	public void haltForTick()
    {
    	haltMovement = true;
    }
    
    @Override
    public String getStatus()
    {
    	String s = "";
    	if(autoPathToEntity)
    	{
    		s += "Auto:";
    	}
    	if(noPath())
    	{
    		s += "NoPath:";
    		return s;
    	}
    	s += "Pathing:";
    	s += "Node[" + path.getCurrentPathIndex() + "/" + path.getCurrentPathLength() + "]:";
    	if(!nodeActionFinished && activeNode != null)
    	{
    		s += "Action[" + activeNode.action + "]:";
    	}
    	return s;
    }
    
    protected Path createPath(EntityIMLiving entity, Entity target)
	{
		return createPath(entity, MathHelper.floor_double(target.posX), (int)target.posY, MathHelper.floor_double(target.posZ));
	}

	protected Path createPath(EntityIMLiving entity, int x, int y, int z)
	{
		theEntity.setCurrentTargetPos(new CoordsInt(x, y, z));
		IBlockAccess terrainCache = getChunkCache(entity.getXCoord(), entity.getYCoord(), entity.getZCoord(), x, y, z, 16);
	    INexusAccess nexus = entity.getNexus();
	    if(nexus != null)
	    {
	    	terrainCache = nexus.getAttackerAI().wrapEntityData(terrainCache);
	    }
		float maxSearchRange = 12 + (float)Distance.distanceBetween(entity, x, y, z);
		if(pathSource.canPathfindNice(PathPriority.HIGH, maxSearchRange, pathSource.getSearchDepth(), pathSource.getQuickFailDepth()))
			return pathSource.createPath(entity, x, y, z, maxSearchRange, terrainCache);
		else
			return null;
		
		
	    /*IBlockAccess terrainCache = getChunkCache(entity.getXCoord(), entity.getYCoord(), entity.getZCoord(), x, y, z, axisExpand);
	    INexusAccess nexus = entity.getNexus();
	    if(nexus != null)
	    {
	    	terrainCache = nexus.getAttackerAI().wrapEntityData(terrainCache);
	    }
	    return (new PathfinderIM(terrainCache, 800)).createEntityPathTo(entity, x, y, z, 12 + (float)Distance.distanceBetween(entity, x, y, z));*/
	}

	/**
	 * Calculates the next node to travel to on the current path. Skips forward on level
	 * surfaces if it is safe to walk diagonally. Never skips nodes with a PathAction other
	 * than NONE, but will always proceed to the next node when called if the entity has
	 * reached the current node.
	 */
	protected void pathFollow()
	{
	    Vec3 vec3d = getEntityPosition();
	    int maxNextLeg = path.getCurrentPathLength();
	    int f = path.getCurrentPathIndex();
	
	    // Calculate next 'leg' of the route: points sharing the same Y value
	    do
	    {
	        if (f >= path.getCurrentPathLength())
	            break;
	
	        if (path.getPathPointFromIndex(f).yCoord != (int)vec3d.yCoord)
	        {
	            maxNextLeg = f;
	            break;
	        }
	        f++;
	    }
	    while (true);
	
	    // Move current index forward for points the entity has reached
	    float fa = theEntity.width * theEntity.width;
	    for (int j = path.getCurrentPathIndex(); j < maxNextLeg; j++)
	    {
	        if (vec3d.squareDistanceTo(path.getPositionAtIndex(theEntity, j)) < fa)
	            path.setCurrentPathIndex(j + 1);
	    }
	
	    int xSize = (int)Math.ceil(theEntity.width);
	    int ySize = (int)theEntity.height + 1;
	    int zSize = xSize;
	    int index = maxNextLeg - 1;
	
	    // Find longest direct route forward on current leg, skipping points for smooth walking
	    do
	    {
	        if (index <= path.getCurrentPathIndex())
	            break;
	
	        if (isDirectPathBetweenPoints(vec3d, path.getPositionAtIndex(theEntity, index), xSize, ySize, zSize))
	            break;
	        
	        index--;
	    }
	    while (true);
	    
	    // Find potential next path action in order to avoid skipping it
	    for(int i = path.getCurrentPathIndex() + 1; i < index; i++)
	    {
	    	if(path.getPathPointFromIndex(i).action != PathAction.NONE)
	    	{
	    		index = i;
	    		break;
	    	}
	    }
	    
	    // Finally, set next node to path to
	    if(path.getCurrentPathIndex() < index)
	    	path.setCurrentPathIndex(index);
	}

	protected void noPathFollow()
	{
	}

	protected void updateAutoPathToEntity()
	{
		if(pathEndEntity == null)
			return;
		
		boolean wantsUpdate;
		if(noPath())
		{
			wantsUpdate = true;
		}
		else
		{
			// Compare the distance the target has moved to the how far away we are.
			// Bigger distance to target -> less need to update the path frequently
			double d1 = Distance.distanceBetween(pathEndEntity, pathEndEntityLastPos);
			double d2 = 6 + Distance.distanceBetween((Entity)theEntity, pathEndEntityLastPos);
			if(d1 / d2 > ENTITY_TRACKING_TOLERANCE)
				wantsUpdate = true;
			else
				wantsUpdate = false;
		}
		
		if(wantsUpdate)
		{
			Path newPath = getPathToEntity(pathEndEntity);
			if(newPath != null)
			{
				if(setPath(newPath, moveSpeed))
				{
					pathEndEntityLastPos = Vec3.createVectorHelper(pathEndEntity.posX, pathEndEntity.posY, pathEndEntity.posZ);
				}
			}
		}
	}

	protected double getDistanceToActiveNode()
	{
		if(activeNode != null)
		{
			double dX = activeNode.xCoord + 0.5 - theEntity.posX;
			double dY = activeNode.yCoord - theEntity.posY;
			double dZ = activeNode.zCoord + 0.5 - theEntity.posZ;
			return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
		}
		return 0;
	}

	/**
	 * Handles the path while an action is unresolved. Returns true when the action is is acting within
	 * its defined behaviour, false if not - for example, if the terrain is invalid.
	 */
	protected boolean handlePathAction()
	{
		nodeActionFinished = true;
		return true;
	}

	protected boolean setDoingTask()
	{
		waitingForNotify = true;
		actionCleared = false;
		return true;
	}
	
    protected boolean setDoingTaskAndHold()
	{
		waitingForNotify = true;
		actionCleared = false;
		setMaintainPosOnWait(Vec3.createVectorHelper(theEntity.posX, theEntity.posY, theEntity.posZ));
		theEntity.setIsHoldingIntoLadder(true);
		return true;
	}
	
    protected boolean setDoingTaskAndHoldOnPoint()
	{
		waitingForNotify = true;
		actionCleared = false;
		setMaintainPosOnWait(Vec3.createVectorHelper(activeNode.getXCoord() + 0.5, activeNode.getYCoord(), activeNode.getZCoord() + 0.5));
		theEntity.setIsHoldingIntoLadder(true);
		return true;
	}
    
    protected void resetStatus()
    {
    	setNoMaintainPos();
    	theEntity.setIsHoldingIntoLadder(false);
        nodeActionFinished = true;
        actionCleared = true;
        waitingForNotify = false;
    }

    protected Vec3 getEntityPosition()
    {
        return Vec3.createVectorHelper(theEntity.posX, getPathableYPos(), theEntity.posZ);
    }
    
    protected EntityIMLiving getEntity()
    {
    	return theEntity;
    }

    /**
     * Gets the safe pathing Y position for the entity depending on if it can path swim or not
     */
    private int getPathableYPos()
    {
        if (!theEntity.isInWater() || !canSwim)
        {
            return (int)(theEntity.boundingBox.minY + 0.5D);
        }

        int i = (int)theEntity.boundingBox.minY;
        int j = theEntity.worldObj.getBlockId(MathHelper.floor_double(theEntity.posX), i, MathHelper.floor_double(theEntity.posZ));
        int k = 0;

        while (j == Block.waterMoving.blockID || j == Block.waterStill.blockID)
        {
            i++;
            j = theEntity.worldObj.getBlockId(MathHelper.floor_double(theEntity.posX), i, MathHelper.floor_double(theEntity.posZ));

            if (++k > 16)
            {
                return (int)theEntity.boundingBox.minY;
            }
        }

        return i;
    }

    /**
     * If on ground or swimming and can swim
     */
    protected boolean canNavigate()
    {
        return true; //theEntity.onGround || canSwim && isInLiquid();
    }

    protected boolean isInLiquid()
    {
        return theEntity.isInWater() || theEntity.handleLavaMovement();
    }
    
    /**
     * Tries to find a point the entity can stand at in the direction of the specified x,z
     * coordinates. Returns a point if found, otherwise returns null.
     * 
     * @param x Target coord x
     * @param z Target coord z
     * @param min The minimum distance the point can be from the entity
     * @param max The maximum distance the point can be from the entity
     * @param verticalRange The minimum and maximum difference from the entity's y coord
     *                      the point can be within.
     *                      
     * @return Position vector of final point
     */
    protected Vec3 findValidPointNear(double x, double z, int min, int max, int verticalRange)
    {
    	double xOffset = x - theEntity.posX;
    	double zOffset = z - theEntity.posZ;
		double h = Math.sqrt(xOffset * xOffset + zOffset * zOffset);		
		// Return if too close; also avoids divide by zero
		if(h < 0.5)
			return null;
		
		// Calculate proportional x, z increment towards target for a random distance
		double distance = min + theEntity.getRNG().nextInt(max - min);
		int xi = MathHelper.floor_double(xOffset * (distance / h) + theEntity.posX);
		int zi = MathHelper.floor_double(zOffset * (distance / h) + theEntity.posZ);
		int y = MathHelper.floor_double(theEntity.posY);
		
		// Search for a valid point to path to in the vicinity of the chosen point
		Path entityPath = null;
		for(int vertical = 0; vertical < verticalRange; vertical = (vertical > 0) ? (vertical * -1) : (vertical * -1 + 1))
		{
    		for(int i = - XZPATH_HORIZONTAL_SEARCH; i <= XZPATH_HORIZONTAL_SEARCH; i++)
    		{
    			for(int j = - XZPATH_HORIZONTAL_SEARCH; j <= XZPATH_HORIZONTAL_SEARCH; j++)
        		{
    				if(theEntity.canStandAtAndIsValid(theEntity.worldObj, xi + i, y + vertical, zi + j))
    				{
    					return Vec3.createVectorHelper(xi + i, y + vertical, zi + j);
    				}
        		}
    		}
		}
		
		return null;
    }

    /**
     * Trims path data from the end to the first sun covered block
     */
    protected void removeSunnyPath()
    {
        if (theEntity.worldObj.canBlockSeeTheSky(MathHelper.floor_double(theEntity.posX), (int)(theEntity.boundingBox.minY + 0.5D), MathHelper.floor_double(theEntity.posZ)))
        {
            return;
        }

        for (int i = 0; i < path.getCurrentPathLength(); i++)
        {
            PathNode pathpoint = path.getPathPointFromIndex(i);

            if (theEntity.worldObj.canBlockSeeTheSky(pathpoint.xCoord, pathpoint.yCoord, pathpoint.zCoord))
            {
                path.setCurrentPathLength(i - 1);
                return;
            }
        }
    }

    protected boolean isDirectPathBetweenPoints(Vec3 pos1, Vec3 pos2, int xSize, int ySize, int zSize)
    {
        int x = MathHelper.floor_double(pos1.xCoord);
        int z = MathHelper.floor_double(pos1.zCoord);
        double dX = pos2.xCoord - pos1.xCoord;
        double dZ = pos2.zCoord - pos1.zCoord;
        double dXZsq = dX * dX + dZ * dZ;

        if (dXZsq < 1E-008D)
        {
            return false;
        }

        double scale = 1.0D / Math.sqrt(dXZsq);
        dX *= scale;
        dZ *= scale;
        xSize += 2;
        zSize += 2;

        if (!isSafeToStandAt(x, (int)pos1.yCoord, z, xSize, ySize, zSize, pos1, dX, dZ))
        {
            return false;
        }

        xSize -= 2;
        zSize -= 2;
        double xIncrement = 1.0D / Math.abs(dX);
        double zIncrement = 1.0D / Math.abs(dZ);
        double xOffset = (x * 1) - pos1.xCoord;
        double zOffset = (z * 1) - pos1.zCoord;

        if (dX >= 0.0D)
        {
            xOffset++;
        }

        if (dZ >= 0.0D)
        {
            zOffset++;
        }

        xOffset /= dX;
        zOffset /= dZ;
        byte xDirection = ((byte)(dX >= 0.0D ? 1 : -1));
        byte zDirection = ((byte)(dZ >= 0.0D ? 1 : -1));
        int x2 = MathHelper.floor_double(pos2.xCoord);
        int z2 = MathHelper.floor_double(pos2.zCoord);
        int xDiff = x2 - x;

        for (int i = z2 - z; xDiff * xDirection > 0 || i * zDirection > 0;)
        {
            if (xOffset < zOffset)
            {
                xOffset += xIncrement;
                x += xDirection;
                xDiff = x2 - x;
            }
            else
            {
                zOffset += zIncrement;
                z += zDirection;
                i = z2 - z;
            }

            if (!isSafeToStandAt(x, (int)pos1.yCoord, z, xSize, ySize, zSize, pos1, dX, dZ))
            {
                return false;
            }
        }

        return true;
    }

    protected boolean isSafeToStandAt(int xOffset, int yOffset, int zOffset, int xSize, int ySize, int zSize, Vec3 entityPostion, double par8, double par10)
    {
        int i = xOffset - xSize / 2;
        int j = zOffset - zSize / 2;

        if (!isPositionClear(i, yOffset, j, xSize, ySize, zSize, entityPostion, par8, par10))
        {
            return false;
        }

        // Checks if there is ground beneath in appropriate area
        for (int k = i; k < i + xSize; k++)
        {
            for (int l = j; l < j + zSize; l++)
            {
                double d = (k + 0.5D) - entityPostion.xCoord;
                double d1 = (l + 0.5D) - entityPostion.zCoord;

                if (d * par8 + d1 * par10 < 0.0D)
                {
                    continue;
                }

                int i1 = theEntity.worldObj.getBlockId(k, yOffset - 1, l);

                if (i1 <= 0)
                {
                    return false;
                }

                Material material = Block.blocksList[i1].blockMaterial;

                if (material == Material.water && !theEntity.isInWater())
                {
                    return false;
                }

                if (material == Material.lava)
                {
                    return false;
                }
            }
        }

        return true;
    }

    protected boolean isPositionClear(int xOffset, int yOffset, int zOffset, int xSize, int ySize, int zSize, Vec3 entityPostion, double vecX, double vecZ)
    {
        for (int i = xOffset; i < xOffset + xSize; i++)
        {
            for (int j = yOffset; j < yOffset + ySize; j++)
            {
                for (int k = zOffset; k < zOffset + zSize; k++)
                {
                    double d = (i + 0.5D) - entityPostion.xCoord;
                    double d1 = (k + 0.5D) - entityPostion.zCoord;

                    if (d * vecX + d1 * vecZ < 0.0D)
                    {
                        continue;
                    }

                    int l = theEntity.worldObj.getBlockId(i, j, k);

                    if (l > 0 && !Block.blocksList[l].getBlocksMovement(theEntity.worldObj, i, j, k))
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }
    
    protected boolean isPositionClearFrom(int x1, int y1, int z1, int x2, int y2, int z2, EntityIMLiving entity)
    {
    	if(y2 > y1)
    	{
    		int id = theEntity.worldObj.getBlockId(x1, y1 + entity.getCollideSize().getYCoord(), z1);
    		if (id > 0 && !Block.blocksList[id].getBlocksMovement(theEntity.worldObj, x1, y1 + entity.getCollideSize().getYCoord(), z1))
            {
                return false;
            }
    	}
    	
    	return isPositionClear(x2, y2, z2, entity);
    }
    
    protected boolean isPositionClear(int x, int y, int z, EntityIMLiving entity)
    {
        CoordsInt size = entity.getCollideSize();
    	return isPositionClear(x, y, z, size.getXCoord(), size.getYCoord(), size.getZCoord());
    }
    
    protected boolean isPositionClear(int x, int y, int z, int xSize, int ySize, int zSize)
    {
    	for (int i = x; i < x + xSize; i++)
        {
            for (int j = y; j < y + ySize; j++)
            {
                for (int k = z; k < z + zSize; k++)
                {
                    int id = theEntity.worldObj.getBlockId(i, j, k);

                    if (id > 0 && !Block.blocksList[id].getBlocksMovement(theEntity.worldObj, i, j, k))
                    {
                        return false;
                    }
                }
            }
        }

        return true;
    }
    
    protected ChunkCache getChunkCache(int x1, int y1, int z1, int x2, int y2, int z2, float axisExpand)
    {
    	int d = (int)axisExpand;
        
        // Calculate box coordinates for the terrain cache to pathfind with - c1 to c2.
        // This will take the box between the start and end points and expand it
        // by f on each axis. This ensures, in all likelyhood, the relevant terrain
        // is included with little excess.
        int cX1, cY1, cZ1, cX2, cY2, cZ2;
        if(x1 < x2)
        {
        	cX1 = x1 - d;
        	cX2 = x2 + d;
        }
        else
        {
        	cX2 = x1 + d;
        	cX1 = x2 - d;
        }
        if(y1 < y2)
        {
        	cY1 = y1 - d;
        	cY2 = y2 + d;
        }
        else
        {
        	cY2 = y1 + d;
        	cY1 = y2 - d;
        }
        if(z1 < z2)
        {
        	cZ1 = z1 - d;
        	cZ2 = z2 + d;
        }
        else
        {
        	cZ2 = z1 + d;
        	cZ1 = z2 - d;
        }
        return new ChunkCache(theEntity.worldObj, cX1, cY1, cZ1, cX2, cY2, cZ2, 0);
    }
}
