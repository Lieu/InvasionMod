package mods.invmod.common.entity;


import java.util.List;

import mods.invmod.common.IPathfindable;
import mods.invmod.common.nexus.INexusAccess;
import mods.invmod.common.util.CoordsInt;
import mods.invmod.common.util.Distance;
import mods.invmod.common.util.IPosition;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class Scaffold implements IPathfindable, IPosition
{
	private static final int MIN_SCAFFOLD_HEIGHT = 4;
	
	private int xCoord;
	private int yCoord;
	private int zCoord;
	private int targetHeight;
	private int orientation;
	private int[] platforms;
	private IPathfindable pathfindBase;
	private INexusAccess nexus;
	private float latestPercentCompleted;
	private float latestPercentIntact;
	private float initialCompletion;
	
	public Scaffold(INexusAccess nexus)
	{
		this.nexus = nexus;
		initialCompletion = 0.01F;
		calcPlatforms();
	}
	
	public Scaffold(int x, int y, int z, int height, INexusAccess nexus)
	{
		xCoord = x;
		yCoord = y;
		zCoord = z;
		targetHeight = height;
		latestPercentCompleted =0;
		latestPercentIntact = 0;
		initialCompletion = 0.01F;
		this.nexus = nexus;
		calcPlatforms();
	}
	
	public void setPosition(int x, int y, int z)
	{
		xCoord = x;
		yCoord = y;
		zCoord = z;
	}
	
	public void setInitialIntegrity()
	{
		initialCompletion = evaluateIntegrity();
		if(initialCompletion == 0)
			initialCompletion = 0.01F;
	}
	
	public void setOrientation(int i)
	{
		orientation = i;
	}
	
	public int getOrientation()
	{
		return orientation;
	}
	
	public void setHeight(int height)
	{
		targetHeight = height;
		calcPlatforms();
	}
	
	/**
	 * Returns the height, from the base, this scaffold is set to reach
	 */
	public int getTargetHeight()
	{
		return targetHeight;
	}
	
	/**
	 * Updates the status of the scaffold
	 */
	public void forceStatusUpdate()
	{
		latestPercentIntact = (evaluateIntegrity() - initialCompletion) / (1.0F - initialCompletion);
		if(latestPercentIntact > latestPercentCompleted)
			latestPercentCompleted = latestPercentIntact;
	}
	
	/**
	 * Returns the percentage of the the scaffold that exists
	 */
	public float getPercentIntactCached()
	{
		return latestPercentIntact;
	}
	
	/**
	 * Returns the greatest percent completion this scaffold has reached
	 */
	public float getPercentCompletedCached()
	{
		return latestPercentCompleted;
	}
	
	@Override
	public int getXCoord()
	{
		return xCoord;
	}
	
	@Override
	public int getYCoord()
	{
		return yCoord;
	}
	
	@Override
	public int getZCoord()
	{
		return zCoord;
	}
	
	public INexusAccess getNexus()
	{
		return nexus;
	}
	
	public void setPathfindBase(IPathfindable base)
	{
		pathfindBase = base;
	}
	
	public boolean isLayerPlatform(int height)
	{
		if(height == targetHeight - 1)
			return true;
		
		if(platforms != null)
		{
			// There will only be a few elements in the array and the current platform structure isn't permanent
			for(int i : platforms)
			{
				if(i == height)
					return true;
			}
		}
		return false;
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound)
    {
		xCoord = nbttagcompound.getInteger("xCoord");
		yCoord = nbttagcompound.getInteger("yCoord");
		zCoord = nbttagcompound.getInteger("zCoord");
		targetHeight = nbttagcompound.getInteger("targetHeight");
		orientation = nbttagcompound.getInteger("orientation");
		initialCompletion = nbttagcompound.getFloat("initialCompletion");
		latestPercentCompleted = nbttagcompound.getFloat("latestPercentCompleted");
		calcPlatforms();
    }
	
	public void writeToNBT(NBTTagCompound nbttagcompound)
    {
		nbttagcompound.setInteger("xCoord", xCoord);
		nbttagcompound.setInteger("yCoord", yCoord);
		nbttagcompound.setInteger("zCoord", zCoord);
		nbttagcompound.setInteger("targetHeight", targetHeight);
		nbttagcompound.setInteger("orientation", orientation);
		nbttagcompound.setFloat("initialCompletion", initialCompletion);
		nbttagcompound.setFloat("latestPercentCompleted", latestPercentCompleted);
    }
	
	/**
	 * Calculates the number and positions of the platforms on this scaffold,
	 * storing it in platforms[]. This method functions to initialise platforms[].
	 */
	private void calcPlatforms()
	{
		// Set number of platforms, influenced by aesthetics. 1 spanning platform appears at
		// height 8 (spacing = 4), otherwise minimum spacing is 5 for 2 or more spanning platforms.
		int spanningPlatforms = targetHeight < 16 ? (targetHeight / 4) - 1 : (targetHeight / 5) - 1;
		if(spanningPlatforms > 0)
		{
			// Set platforms at even spacings
			int avgSpace = targetHeight / (spanningPlatforms + 1);
			int remainder = targetHeight % (spanningPlatforms + 1) - 1;
			platforms = new int[spanningPlatforms];
			for(int i = 0; i < spanningPlatforms; i++)
				platforms[i] = avgSpace * (i + 1) - 1;
			
			// Distribute the remaining space evenly from the top down
			int i = spanningPlatforms - 1;
			while(remainder > 0)
			{
				platforms[i]++;
				if(--i < 0)
				{
					i = spanningPlatforms - 1;
					remainder--;
				}
				remainder--;
			}
		}
		else
		{
			platforms = new int[0];
		}
	}
	
	/**
	 * Returns the percentage of the scaffold that is considered to exist.
	 */
	private float evaluateIntegrity()
	{
		if(nexus != null)
		{
			int existingMainSectionBlocks = 0;
			int existingMainLadderBlocks = 0;
			int existingPlatformBlocks = 0;
			World world = nexus.getWorld(); // Should keep in mind the world reference from a tile entity is null during loading
			for(int i = 0; i < targetHeight; i++)
			{
				if(world.isBlockNormalCube(xCoord + CoordsInt.offsetAdjX[orientation], yCoord + i, zCoord + CoordsInt.offsetAdjZ[orientation]))
					existingMainSectionBlocks++;
				
				if(world.getBlockId(xCoord, yCoord + i, zCoord) == Block.ladder.blockID)
					existingMainLadderBlocks++;
				
				if(isLayerPlatform(i))
				{
					for(int j = 0; j < 8; j++)
					{
						if(world.isBlockNormalCube(xCoord + CoordsInt.offsetRing1X[j], yCoord + i, zCoord + CoordsInt.offsetRing1Z[j]))
							existingPlatformBlocks++;
					}
				}
			}
			
			
			float mainSectionPercent = targetHeight > 0 ? ((float)existingMainSectionBlocks / targetHeight) : 0F;
			float ladderPercent = targetHeight > 0 ? ((float)existingMainLadderBlocks / targetHeight) : 0F;
			
			// 70% for main section and ladder, 30% for platforms
			return 0.7F * (0.7F * mainSectionPercent +  0.3F *ladderPercent) + 0.3F * ((float)existingPlatformBlocks / ((platforms.length + 1) * 8));
		}
		return 0;
	}
	
	@Override
	public float getBlockPathCost(PathNode prevNode, PathNode node,	IBlockAccess terrainMap)
	{
		float materialMultiplier = terrainMap.getBlockMaterial(node.xCoord, node.yCoord, node.zCoord).isSolid() ? 2.2F : 1.0F;
		if(node.action == PathAction.SCAFFOLD_UP)
		{
			if(prevNode.action != PathAction.SCAFFOLD_UP)
				materialMultiplier *= 3.4F;
			
			return prevNode.distanceTo(node) * 0.85F * materialMultiplier;
		}
		else if(node.action == PathAction.BRIDGE)
		{
			if(prevNode.action == PathAction.SCAFFOLD_UP)
				materialMultiplier = 0;
			
			return prevNode.distanceTo(node) * 1.1F * materialMultiplier;
		}
		else if(node.action == PathAction.LADDER_UP_NX || node.action == PathAction.LADDER_UP_NZ || node.action == PathAction.LADDER_UP_PX || node.action == PathAction.LADDER_UP_PZ)
		{
			return prevNode.distanceTo(node) * 1.5F * materialMultiplier;
		}

		if(pathfindBase != null)
			return pathfindBase.getBlockPathCost(prevNode, node, terrainMap);
		
		return prevNode.distanceTo(node);
	}

	@Override
	public void getPathOptionsFromNode(IBlockAccess terrainMap,	PathNode currentNode, PathfinderIM pathFinder)
	{
		if(pathfindBase != null)
			pathfindBase.getPathOptionsFromNode(terrainMap, currentNode, pathFinder);
		
		int id = terrainMap.getBlockId(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord);
		if(currentNode.previous != null && currentNode.previous.action == PathAction.SCAFFOLD_UP && !avoidsBlock(id))
		{
			pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.SCAFFOLD_UP);
			return;
		}
		
		// Find scaffold path upwards, but first check if too near existing scaffolds
		if(nexus != null)
		{
			List<Scaffold> scaffolds = nexus.getAttackerAI().getScaffolds();
			int minDistance = nexus.getAttackerAI().getMinDistanceBetweenScaffolds();
			for(Scaffold scaffold : scaffolds)
			{
				if(Distance.distanceBetween(scaffold, currentNode.xCoord, currentNode.yCoord, currentNode.zCoord) < minDistance)
					return;
			}
		}
		
		// Now check position is valid for new scaffold
		if(id == 0 && terrainMap.getBlockMaterial(currentNode.xCoord, currentNode.yCoord - 2, currentNode.zCoord).isSolid())
		{
			boolean flag = false;
			for(int i = 1; i < MIN_SCAFFOLD_HEIGHT; i++)
			{
				if(terrainMap.getBlockId(currentNode.xCoord, currentNode.yCoord + i, currentNode.zCoord) != 0)
				{
					flag = true;
					break;
				}
			}
			
			if(!flag)
				pathFinder.addNode(currentNode.xCoord, currentNode.yCoord + 1, currentNode.zCoord, PathAction.SCAFFOLD_UP);
		}
	}
	
	private boolean avoidsBlock(int id)
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
}
