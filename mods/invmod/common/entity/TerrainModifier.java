package mods.invmod.common.entity;


import java.util.ArrayList;
import java.util.List;

import mods.invmod.common.INotifyTask;
import mods.invmod.common.mod_Invasion;
import mods.invmod.common.util.Distance;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;

/**
 * Component to modify terrain blocks. Supports queued actions,
 * notify caller upon completion and physical reach.
 * 
 * @author Lieu
 */
public class TerrainModifier implements ITerrainModify
{
	private static final float DEFAULT_REACH = 2.0F;

	private EntityLiving theEntity;
	private INotifyTask taskSetter;
	private INotifyTask blockNotify;
	private List<ModifyBlockEntry> modList;
	private ModifyBlockEntry nextEntry;
	private ModifyBlockEntry lastEntry;
	private int entryIndex;
	private int timer;
	private float reach;
	private boolean outOfRangeFlag;
	private boolean terrainFailFlag;
	
	public TerrainModifier(EntityLiving entity, float defaultReach)
	{
		theEntity = entity;
		modList = new ArrayList<ModifyBlockEntry>();
		entryIndex = 0;
		timer = 0;
		reach = defaultReach;
	}

	/**
	 * Method to call on regular basis, proportional to passage of time.
	 */
	public void onUpdate()
	{
		taskUpdate();
	}
	
	/**
	 * Returns true if can accept new entries for the specified notify object
	 */
	@Override
	public boolean isReadyForTask(INotifyTask asker)
	{
		return modList.size() == 0 || taskSetter == asker;
	}
	
	public void cancelTask()
	{
		endTask();
	}
	
	public boolean isBusy()
	{
		return timer > 0;
	}
	
	/**
	 * Adds new set of block modifications to carry out if the object does
	 * not currently have a task *or* the caller is the same as the current task.
	 * Returns true if task accepted. Blocks will be always be modified in order.
	 * Block is skipped if the existing block is identical to the entry, by ID and
	 * metadata. Block fails to be modified if entity is out of specified reach distance.
	 * 
	 * Notifies when task is completed and once after each block change, any action
	 * being successful or not. Providing objects to be notified is optional.
	 */
	@Override
	public boolean requestTask(ModifyBlockEntry[] entries, INotifyTask onFinished, INotifyTask onBlockChange)
	{
		if(isReadyForTask(onFinished))
		{
			for(ModifyBlockEntry entry : entries)
				modList.add(entry);
			
			taskSetter = onFinished;
			blockNotify = onBlockChange;
			return true;
		}
		return false;
	}
	
	/**
	 * Returns the most recently carried out block change, whether it was
	 * successful or not.
	 */
	@Override
	public ModifyBlockEntry getLastBlockModified()
	{
		return lastEntry;
	}
	
	/**
	 * Increments task time and modifies blocks in order according
	 * to modList. Notifies upon task completion.
	 */
	private void taskUpdate()
	{
		if(timer > 1)
		{
			timer--;
			return;
		}
		else if(timer == 1)
		{
			entryIndex++;
			timer = 0;
			int result = changeBlock(nextEntry) ? 0 : 1;
			lastEntry = nextEntry;
			if(blockNotify != null)
				blockNotify.notifyTask(result);
		}
		
		// BuildTimer is 0 and entry was completed, so check for more entries
		if(entryIndex < modList.size())
		{
			nextEntry = modList.get(entryIndex);
			while(isTerrainIdentical(nextEntry))
			{
				entryIndex++;
				if(entryIndex < modList.size())
				{
					nextEntry = modList.get(entryIndex);
				}
				else
				{
					endTask();
					return;
				}
			}
			
			timer = nextEntry.getCost();
			if(timer == 0)
				timer = 1;
		}
		else if(modList.size() > 0)
		{
			endTask();
		}
	}
	
	/**
	 * Clears status of an old task, ready for new requests.
	 */
	private void endTask()
	{
		entryIndex = 0;
		timer = 0;
		modList.clear();
		if(taskSetter != null)
			taskSetter.notifyTask(terrainFailFlag ? 2 : (outOfRangeFlag ? 1 : 0));
	}
	
	/**
	 * Attempts to modify block in entity's World, according to entry and reach.
	 * Sets neutral blocks' orientation. Returns success or failure of block replacement.
	 */
	private boolean changeBlock(ModifyBlockEntry entry)
	{
		if(Distance.distanceBetween(theEntity.posX, theEntity.posY + theEntity.height / 2, theEntity.posZ, entry.getXCoord() + 0.5, entry.getYCoord() + 0.5, entry.getZCoord() + 0.5) > reach)
		{
			outOfRangeFlag = true;
			return false;
		}
		
		int newId = entry.getNewBlockId();
		int oldId = theEntity.worldObj.getBlockId(entry.getXCoord(), entry.getYCoord(), entry.getZCoord());
		int oldMeta = theEntity.worldObj.getBlockMetadata(entry.getXCoord(), entry.getYCoord(), entry.getZCoord());
		entry.setOldBlockId(oldId);
		if(oldId == mod_Invasion.blockNexus.blockID)
		{
			terrainFailFlag = true;
			return false;
		}
		
		boolean succeeded = theEntity.worldObj.setBlock(entry.getXCoord(), entry.getYCoord(), entry.getZCoord(), entry.getNewBlockId(), entry.getNewBlockMeta(), 3);
		if(succeeded)
		{
			if(newId == 0)
			{
				Block block = Block.blocksList[oldId];
				block.onBlockDestroyedByPlayer(theEntity.worldObj, entry.getXCoord(), entry.getYCoord(), entry.getZCoord(), oldMeta);
				block.dropBlockAsItem(theEntity.worldObj, entry.getXCoord(), entry.getYCoord(), entry.getZCoord(), oldMeta, 0);
			}
			if(newId == Block.ladder.blockID)
			{
				int meta = Block.blocksList[newId].onBlockPlaced(theEntity.worldObj, entry.getXCoord(), entry.getYCoord(), entry.getZCoord(), 0, 0, 0, 0, oldMeta);
				theEntity.worldObj.setBlockMetadataWithNotify(entry.getXCoord(), entry.getYCoord(), entry.getZCoord(), meta, 3);
				//Block.blocksList[Block.ladder.blockID].onBlockPlacedBy(theEntity.worldObj, entry.getXCoord(), entry.getYCoord(), entry.getZCoord(), theEntity);
				Block.blocksList[Block.ladder.blockID].onPostBlockPlaced(theEntity.worldObj, entry.getXCoord(), entry.getYCoord(), entry.getZCoord(), meta);
			}
		}
		else
		{
			terrainFailFlag = true;
		}
		return succeeded;
	}
	
	/**
	 * Returns true if the terrain contains the same block as the
	 * entry and has the same metadata.
	 */
	private boolean isTerrainIdentical(ModifyBlockEntry entry)
	{
		if(theEntity.worldObj.getBlockId(entry.getXCoord(), entry.getYCoord(), entry.getZCoord()) == entry.getNewBlockId()
				&& theEntity.worldObj.getBlockMetadata(entry.getXCoord(), entry.getYCoord(), entry.getZCoord()) == entry.getNewBlockMeta())
		{
				return true;
		}
		return false;
	}
}
