
package mods.invmod.common.nexus;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;


public class ContainerNexus extends Container
{

    public ContainerNexus(InventoryPlayer inventoryplayer, TileEntityNexus tileEntityNexus)
    {
    	mode = 0;
    	activationTimer = 0;
    	currentWave = 0;
    	nexusLevel = 0;
    	nexusKills = 0;
    	spawnRadius = 0;
    	generation = 0;
    	powerLevel = 0;
    	cookTime = 0;
        nexus = tileEntityNexus;
        addSlotToContainer(new Slot(tileEntityNexus, 0, 32, 33));
        addSlotToContainer(new SlotOutput(tileEntityNexus, 1, 102, 33));
        for(int i = 0; i < 3; i++)
        {
            for(int k = 0; k < 9; k++)
            {
                addSlotToContainer(new Slot(inventoryplayer, k + i * 9 + 9, 8 + k * 18, 84 + i * 18));
            }

        }

        for(int j = 0; j < 9; j++)
        {
            addSlotToContainer(new Slot(inventoryplayer, j, 8 + j * 18, 142));
        }

    }

    @Override
	public void detectAndSendChanges()
    {
    	super.detectAndSendChanges();
        for(int i = 0; i < crafters.size(); i++)
        {
            ICrafting icrafting = (ICrafting)crafters.get(i);
            if(activationTimer != nexus.getActivationTimer())
            {
                icrafting.sendProgressBarUpdate(this, 0, nexus.getActivationTimer());
            }
            if(mode != nexus.getMode())
            {
                icrafting.sendProgressBarUpdate(this, 1, nexus.getMode());
            }
            if(currentWave != nexus.getCurrentWave())
            {
                icrafting.sendProgressBarUpdate(this, 2, nexus.getCurrentWave());
            }
            if(nexusLevel != nexus.getNexusLevel())
            {
                icrafting.sendProgressBarUpdate(this, 3, nexus.getNexusLevel());
            }
            if(nexusKills != nexus.getNexusKills())
            {
                icrafting.sendProgressBarUpdate(this, 4, nexus.getNexusKills());
            }
            if(spawnRadius != nexus.getSpawnRadius())
            {
                icrafting.sendProgressBarUpdate(this, 5, nexus.getSpawnRadius());
            }
            if(generation != nexus.getGeneration())
            {
                icrafting.sendProgressBarUpdate(this, 6, nexus.getGeneration());
            }
            if(generation != nexus.getNexusPowerLevel())
            {
                icrafting.sendProgressBarUpdate(this, 7, nexus.getNexusPowerLevel());
            }
            if(generation != nexus.getCookTime())
            {
                icrafting.sendProgressBarUpdate(this, 9, nexus.getCookTime());
            }
        }

        activationTimer = nexus.getActivationTimer();
        mode = nexus.getMode();
        currentWave = nexus.getCurrentWave();
        nexusLevel = nexus.getNexusLevel();
        nexusKills = nexus.getNexusKills();
        spawnRadius = nexus.getSpawnRadius();
        generation = nexus.getGeneration();
        powerLevel = nexus.getNexusPowerLevel();
        cookTime = nexus.getCookTime();
    }

    @Override
	public void updateProgressBar(int i, int j)
    {
        if(i == 0)
        {
            nexus.setActivationTimer(j);
        }
        else if(i == 1)
        {
            nexus.setMode(j);
        }
        else if(i == 2)
        {
            nexus.setWave(j);
        }
        else if(i == 3)
        {
            nexus.setNexusLevel(j);
        }
        else if(i == 4)
        {
            nexus.setNexusKills(j);
        }
        else if(i == 5)
        {
            nexus.setSpawnRadius(j);
        }
        else if(i == 6)
        {
            nexus.setGeneration(j);
        }
        else if(i == 7)
        {
            nexus.setNexusPowerLevel(j);
        }
        else if(i == 8)
        {
            nexus.setCookTime(j);
        }
    }

    @Override
	public boolean canInteractWith(EntityPlayer entityplayer)
    {
        return nexus.isUseableByPlayer(entityplayer);
    }

    @Override
	public ItemStack transferStackInSlot(EntityPlayer player, int i)
    {
        ItemStack itemstack = null;
        Slot slot = (Slot)inventorySlots.get(i);
        if(slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if(i == 1)
            {
                if(!mergeItemStack(itemstack1, 2, 38, true))
                {
                    return null;
                }
            } else
            if(i >= 2 && i < 29)
            {
                if(!mergeItemStack(itemstack1, 29, 38, false))
                {
                    return null;
                }
            } else
            if(i >= 29 && i < 38)
            {
                if(!mergeItemStack(itemstack1, 2, 29, false))
                {
                    return null;
                }
            } else
            if(!mergeItemStack(itemstack1, 2, 38, false))
            {
                return null;
            }
            if(itemstack1.stackSize == 0)
            {
                slot.putStack(null);
            } else
            {
                slot.onSlotChanged();
            }
            if(itemstack1.stackSize != itemstack.stackSize)
            {
                slot.onPickupFromSlot(player, itemstack1);
            } else
            {
                return null;
            }
        }
        return itemstack;
    }

    private TileEntityNexus nexus;
    private int activationTimer;
    private int currentWave;
    private int nexusLevel;
    private int nexusKills;
    private int spawnRadius;
    private int generation;
    private int powerLevel;
    private int cookTime;
    private int mode;
}
