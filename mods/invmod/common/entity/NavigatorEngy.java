package mods.invmod.common.entity;

import mods.invmod.common.IBlockAccessExtended;
import mods.invmod.common.entity.IPathSource.PathPriority;
import mods.invmod.common.nexus.INexusAccess;
import mods.invmod.common.util.Distance;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class NavigatorEngy extends NavigatorIM
{
	public NavigatorEngy(EntityIMPigEngy entity, IPathSource pathSource)
	{
		super(entity, pathSource);
		pigEntity = entity;
		setNoMaintainPos();
	}
	
	@Override
	protected Path createPath(EntityIMLiving entity, int x, int y, int z)
    {
		IBlockAccess terrainCache = getChunkCache(entity.getXCoord(), entity.getYCoord(), entity.getZCoord(), x, y, z, 16);
		INexusAccess nexus = pigEntity.getNexus();
        if(nexus != null)
        {
        	// Add entity density data
        	IBlockAccessExtended terrainCacheExt = nexus.getAttackerAI().wrapEntityData(terrainCache);
        	
        	// For pig engineers, the terrain data layer is needed to pathfind with scaffolds
        	nexus.getAttackerAI().addScaffoldDataTo(terrainCacheExt);
        	terrainCache = terrainCacheExt;
        }
        float maxSearchRange = 12 + (float)Distance.distanceBetween(entity, x, y, z);
    	if(pathSource.canPathfindNice(PathPriority.HIGH, maxSearchRange, pathSource.getSearchDepth(), pathSource.getQuickFailDepth()))
    		return pathSource.createPath(entity, x, y, z, maxSearchRange, terrainCache);
    	else
    		return null;
        
		/*IBlockAccess terrainCache = getChunkCache(entity.getXCoord(), entity.getYCoord(), entity.getZCoord(), x, y, z, axisExpand);
		INexusAccess nexus = pigEntity.getNexus();
        if(nexus != null)
        {
        	// Add entity density data
        	IBlockAccessExtended terrainCacheExt = nexus.getAttackerAI().wrapEntityData(terrainCache);
        	
        	// For pig engineers, the terrain data layer is needed to pathfind with scaffolds
        	nexus.getAttackerAI().addScaffoldDataTo(terrainCacheExt);
        	terrainCache = terrainCacheExt;
        }
        return (new PathfinderIM(terrainCache, 1500)).createEntityPathTo(entity, x, y, z, 12 + (float)Distance.distanceBetween(entity, x, y, z));*/
    }
	
	@Override
	protected boolean handlePathAction()
    {
		if(!actionCleared)
		{
			resetStatus();
			if(getLastActionResult() != 0)
				return false;
			
			return true;
		}
		
		if(activeNode.action == PathAction.LADDER_UP_PX || activeNode.action == PathAction.LADDER_UP_NX 
				|| activeNode.action == PathAction.LADDER_UP_PZ || activeNode.action == PathAction.LADDER_UP_NZ)
		{
			// Place upper/next section of ladder
			if(pigEntity.getTerrainBuildEngy().askBuildLadder(activeNode, this))
				return setDoingTaskAndHold();
		}
		else if(activeNode.action == PathAction.BRIDGE)
		{
			if(pigEntity.getTerrainBuildEngy().askBuildBridge(activeNode, this))
				return setDoingTaskAndHold();
		}
		else if(activeNode.action == PathAction.SCAFFOLD_UP)
		{
			if(pigEntity.getTerrainBuildEngy().askBuildScaffoldLayer(activeNode, this))
				return setDoingTaskAndHoldOnPoint();
		}
		else if(activeNode.action == PathAction.LADDER_TOWER_UP_PX)
		{
			if(pigEntity.getTerrainBuildEngy().askBuildLadderTower(activeNode, 0, 1, this))
				return setDoingTaskAndHold();
		}
		else if(activeNode.action == PathAction.LADDER_TOWER_UP_NX)
		{
			if(pigEntity.getTerrainBuildEngy().askBuildLadderTower(activeNode, 1, 1, this))
				return setDoingTaskAndHold();
		}
		else if(activeNode.action == PathAction.LADDER_TOWER_UP_PZ)
		{
			if(pigEntity.getTerrainBuildEngy().askBuildLadderTower(activeNode, 2, 1, this))
				return setDoingTaskAndHold();
		}
		else if(activeNode.action == PathAction.LADDER_TOWER_UP_NZ)
		{
			if(pigEntity.getTerrainBuildEngy().askBuildLadderTower(activeNode, 3, 1, this))
				return setDoingTaskAndHold();
		}
		
		nodeActionFinished = true;
		return true;
    }
	
	private final EntityIMPigEngy pigEntity;
}
