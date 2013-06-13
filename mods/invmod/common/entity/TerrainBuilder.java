package mods.invmod.common.entity;


import java.util.ArrayList;
import java.util.List;

import mods.invmod.common.INotifyTask;
import mods.invmod.common.util.CoordsInt;
import mods.invmod.common.util.IPosition;
import net.minecraft.block.Block;

public class TerrainBuilder implements ITerrainBuild
{
	public TerrainBuilder(EntityIMLiving entity, ITerrainModify modifier, float buildRate)
	{
		theEntity = entity;
		this.modifier = modifier;
		this.buildRate = buildRate;
	}
	
	public void setBuildRate(float buildRate)
	{
		this.buildRate = buildRate;
	}
	
	public float getBuildRate()
	{
		return buildRate;
	}

	@Override
	public boolean askBuildScaffoldLayer(IPosition pos, INotifyTask asker)
	{
		if(modifier.isReadyForTask(asker))
		{
			Scaffold scaffold = theEntity.getNexus().getAttackerAI().getScaffoldAt(pos);
			if(scaffold != null)
			{
				int height = pos.getYCoord() - scaffold.getYCoord();
				int xOffset = CoordsInt.offsetAdjX[scaffold.getOrientation()];
				int zOffset = CoordsInt.offsetAdjZ[scaffold.getOrientation()];
				int id = theEntity.worldObj.getBlockId(pos.getXCoord() + xOffset, pos.getYCoord() - 1, pos.getZCoord() + zOffset);
				List<ModifyBlockEntry> modList = new ArrayList<ModifyBlockEntry>();
				
				if(height == 1)
				{
					if(!Block.isNormalCube(id))
						modList.add(new ModifyBlockEntry(pos.getXCoord() + xOffset, pos.getYCoord() - 1, pos.getZCoord() + zOffset, Block.planks.blockID,  (int)(PLANKS_COST / buildRate)));
					
					id = theEntity.worldObj.getBlockId(pos.getXCoord(), pos.getYCoord() - 1, pos.getZCoord());
					if(id == 0)
						modList.add(new ModifyBlockEntry(pos.getXCoord(), pos.getYCoord() - 1, pos.getZCoord(), Block.ladder.blockID,  (int)(LADDER_COST / buildRate)));
				}
			
				id = theEntity.worldObj.getBlockId(pos.getXCoord() + xOffset, pos.getYCoord(), pos.getZCoord() + zOffset);
				if(!Block.isNormalCube(id))
					modList.add(new ModifyBlockEntry(pos.getXCoord() + xOffset, pos.getYCoord(), pos.getZCoord() + zOffset, Block.planks.blockID, (int)(PLANKS_COST / buildRate)));
				
				id = theEntity.worldObj.getBlockId(pos.getXCoord(), pos.getYCoord(), pos.getZCoord());
				if(id != Block.ladder.blockID)
					modList.add(new ModifyBlockEntry(pos.getXCoord(), pos.getYCoord(), pos.getZCoord(), Block.ladder.blockID,  (int)(LADDER_COST / buildRate)));
				
				
				if(scaffold.isLayerPlatform(height))
				{
					for(int i = 0; i < 8; i++)
					{
						if(CoordsInt.offsetRing1X[i] == xOffset && CoordsInt.offsetRing1Z[i] == zOffset)
							continue;
						
						id = theEntity.worldObj.getBlockId(pos.getXCoord() + CoordsInt.offsetRing1X[i], pos.getYCoord(), pos.getZCoord() + CoordsInt.offsetRing1Z[i]);
						if(!Block.isNormalCube(id))
							modList.add(new ModifyBlockEntry(pos.getXCoord() + CoordsInt.offsetRing1X[i], pos.getYCoord(), pos.getZCoord() + CoordsInt.offsetRing1Z[i], Block.planks.blockID, (int)(PLANKS_COST / buildRate)));
					}
				}
				
				if(modList.size() > 0)
					return modifier.requestTask(modList.toArray(new ModifyBlockEntry[modList.size()]), asker, null);
			}
		}
		return false;
	}
	
	@Override
	public boolean askBuildLadderTower(IPosition pos, int orientation, int layersToBuild, INotifyTask asker)
	{
		if(modifier.isReadyForTask(asker))
		{
			int xOffset = orientation == 0 ? 1 : (orientation == 1 ? -1 : 0);
			int zOffset = orientation == 2 ? 1 : (orientation == 3 ? -1 : 0);
			List<ModifyBlockEntry> modList = new ArrayList<ModifyBlockEntry>();
			
			int id = theEntity.worldObj.getBlockId(pos.getXCoord() + xOffset, pos.getYCoord() - 1, pos.getZCoord() + zOffset);
			if(!Block.isNormalCube(id))
				modList.add(new ModifyBlockEntry(pos.getXCoord() + xOffset, pos.getYCoord() - 1, pos.getZCoord() + zOffset, Block.planks.blockID, (int)(PLANKS_COST / buildRate)));
			
			id = theEntity.worldObj.getBlockId(pos.getXCoord(), pos.getYCoord() - 1, pos.getZCoord());
			if(id == 0)
				modList.add(new ModifyBlockEntry(pos.getXCoord(), pos.getYCoord() - 1, pos.getZCoord(), Block.ladder.blockID,  (int)(LADDER_COST / buildRate)));
			
			for(int i = 0; i < layersToBuild; i++)
			{
				id = theEntity.worldObj.getBlockId(pos.getXCoord() + xOffset, pos.getYCoord() + i, pos.getZCoord() + zOffset);
				if(!Block.isNormalCube(id))
					modList.add(new ModifyBlockEntry(pos.getXCoord() + xOffset, pos.getYCoord() + i, pos.getZCoord() + zOffset, Block.planks.blockID, (int)(PLANKS_COST / buildRate)));
				
				id = theEntity.worldObj.getBlockId(pos.getXCoord(), pos.getYCoord() + i, pos.getZCoord());
				if(id != Block.ladder.blockID)
					modList.add(new ModifyBlockEntry(pos.getXCoord(), pos.getYCoord() + i, pos.getZCoord(), Block.ladder.blockID,  (int)(LADDER_COST / buildRate)));
			}
			
			if(modList.size() > 0)
				return modifier.requestTask(modList.toArray(new ModifyBlockEntry[modList.size()]), asker, null);
		}
		return false;
	}
	
	@Override
	public boolean askBuildLadder(IPosition pos, INotifyTask asker)
	{
		if(modifier.isReadyForTask(asker))
		{
			List<ModifyBlockEntry> modList = new ArrayList<ModifyBlockEntry>();
			int id = theEntity.worldObj.getBlockId(pos.getXCoord(), pos.getYCoord(), pos.getZCoord());
			if(id != Block.ladder.blockID)
			{
				if(EntityIMPigEngy.canPlaceLadderAt(theEntity.worldObj, pos.getXCoord(), pos.getYCoord(), pos.getZCoord()))
					modList.add(new ModifyBlockEntry(pos.getXCoord(), pos.getYCoord(), pos.getZCoord(), Block.ladder.blockID,  (int)(LADDER_COST / buildRate)));
				else
					return false;
			}
			
			// Place base of ladder if on ground
			id = theEntity.worldObj.getBlockId(pos.getXCoord(), pos.getYCoord() - 2, pos.getZCoord());
			if(id > 0 && Block.blocksList[id].blockMaterial.isSolid())
			{
				if(EntityIMPigEngy.canPlaceLadderAt(theEntity.worldObj, pos.getXCoord(), pos.getYCoord() - 1, pos.getZCoord()))
						modList.add(new ModifyBlockEntry(pos.getXCoord(), pos.getYCoord() - 1, pos.getZCoord(), Block.ladder.blockID, (int)(LADDER_COST / buildRate)));
			}
			
			if(modList.size() > 0)
				return modifier.requestTask(modList.toArray(new ModifyBlockEntry[modList.size()]), asker, null);
		}
		return false;
	}
	
	@Override
	public boolean askBuildBridge(IPosition pos, INotifyTask asker)
	{
		if(modifier.isReadyForTask(asker))
		{
			List<ModifyBlockEntry> modList = new ArrayList<ModifyBlockEntry>();
			if(theEntity.worldObj.getBlockId(pos.getXCoord(), pos.getYCoord() - 1, pos.getZCoord()) == 0)
			{
				if(theEntity.avoidsBlock(theEntity.worldObj.getBlockId(pos.getXCoord(), pos.getYCoord() - 2, pos.getZCoord()))
						|| theEntity.avoidsBlock(theEntity.worldObj.getBlockId(pos.getXCoord(), pos.getYCoord() - 3, pos.getZCoord())))
    			{
    				modList.add(new ModifyBlockEntry(pos.getXCoord(), pos.getYCoord() - 1, pos.getZCoord(), Block.cobblestone.blockID, (int)(COBBLE_COST / buildRate)));
    			}
    			else
    			{
    				modList.add(new ModifyBlockEntry(pos.getXCoord(), pos.getYCoord() - 1, pos.getZCoord(), Block.planks.blockID, (int)(PLANKS_COST / buildRate)));
    			}
				
				if(modList.size() > 0)
					return modifier.requestTask(modList.toArray(new ModifyBlockEntry[modList.size()]), asker, null);
			}
		}
		return false;
	}
	
	private static final float LADDER_COST = 25;
	private static final float PLANKS_COST = 45;
	private static final float COBBLE_COST = 65;
	
	private EntityIMLiving theEntity;
	private ITerrainModify modifier;
	private float buildRate;
}
