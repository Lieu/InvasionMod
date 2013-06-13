package mods.invmod.common.nexus;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import mods.invmod.common.mod_Invasion;
import mods.invmod.common.entity.AttackerAI;
import mods.invmod.common.entity.EntityIMBolt;
import mods.invmod.common.entity.EntityIMLiving;
import mods.invmod.common.entity.EntityIMTrap;
import mods.invmod.common.util.ComparatorEntityDistance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class TileEntityNexus extends TileEntity implements INexusAccess, IInventory
{
	public TileEntityNexus()
    {
		this(null);
    }

    public TileEntityNexus(World world)
    {
    	worldObj = world;
    	spawnRadius = 52;
    	waveSpawner = new IMWaveSpawner(this, spawnRadius);
    	waveBuilder = new IMWaveBuilder();
    	nexusItemStacks = new ItemStack[2];  	
    	boundingBoxToRadius = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
    	boundingBoxToRadius.setBounds(xCoord - (spawnRadius + 10), yCoord - (spawnRadius + 40), zCoord - (spawnRadius + 10), xCoord + (spawnRadius + 10), yCoord + (spawnRadius + 40), zCoord + (spawnRadius + 10));
    	boundPlayers = new HashMap<String, Long>();
    	mobList = new ArrayList<EntityIMLiving>();
    	attackerAI = new AttackerAI(this);
    	activationTimer = 0;
    	cookTime = 0;
    	currentWave = 0;
    	nexusLevel = 1;
    	nexusKills = 0;
    	generation = 0;
    	maxHp = hp = lastHp = 100;
    	mode = 0;
    	powerLevelTimer = 0;
    	powerLevel = 0;
    	lastPowerLevel = 0;
    	mobsLeftInWave = 0;
    	nextAttackTime = 0;
    	daysToAttack = 0;
    	lastWorldTime = 0;
    	errorState = 0;
    	tickCount = 0;
    	timer = 0;
    	zapTimer = 0;
		cleanupTimer = 0;
		waveDelayTimer = -1;
		waveDelay = 0;
		continuousAttack = false;
		mobsSorted = false;
    	resumedFromNBT = false;
    }
    
    //@SideOnly(Side.SERVER)
    @Override
	public void updateEntity()
    {
    	/*for(int i = xCoord - 50; i < xCoord + 50; i++)
    	{
    		for(int j = yCoord - 50; j < yCoord + 50; j++)
        	{
    			for(int k = zCoord - 50; k < zCoord + 50; k++)
    	    	{
    	    		int id = worldObj.getBlockId(i, j, k);
    	    		if(id == Block.lavaStill.blockID || id == Block.lavaMoving.blockID)
    	    			worldObj.setBlockWithNotify(i, j, k, 0);
    	    	}
        	}
    	}*/
    	
    	//killAllMobs();
    	
    	if(worldObj.isRemote)
    	{
    		return;
    	}
    	
    	
    	//Update nexus status from item slots, etc
    	updateStatus();
    	
    	updateAI();

    	//Handle active nexus state
    	if(mode == 1 || mode == 2 || mode == 3)
    	{
    		if(resumedFromNBT)
    		{
    			boundingBoxToRadius.setBounds(xCoord - (spawnRadius + 10), 0, zCoord - (spawnRadius + 10), xCoord + (spawnRadius + 10), 127, zCoord + (spawnRadius + 10));
    			if(mode == 2 && continuousAttack)
    			{
    				if(resumeSpawnerContinuous())
    				{
    					mobsLeftInWave = lastMobsLeftInWave += acquireEntities();
    					mod_Invasion.log("mobsLeftInWave: " + mobsLeftInWave);
    					mod_Invasion.log("mobsToKillInWave: " + mobsToKillInWave);
    				}
    			}
    			else
    			{
    				resumeSpawnerInvasion();
    				acquireEntities();
    			}
    			
    			attackerAI.onResume();
    			
    			resumedFromNBT = false;
    		}
    		
    		try
    		{
    			tickCount++;
    	    	if(tickCount == 60)
    	    	{
    	    		tickCount -= 60;
    	    		bindPlayers();
    	    		updateMobList();
    	    	}
    	    	
        		if(mode == 1 || mode == 3)
	        		doInvasion(50);
	        	else if(mode == 2)
	        		doContinuous(50);
			}
    		catch (WaveSpawnerException e)
			{
				mod_Invasion.log(e.getMessage());
				e.printStackTrace();
				stop();
			}
    	}
    	
    	if(cleanupTimer++ > 40)
    	{
    		cleanupTimer = 0;
    		if(worldObj.getBlockId(xCoord, yCoord, zCoord) != mod_Invasion.blockNexus.blockID)
    		{
    			mod_Invasion.setInvasionEnded(this);
    			stop();
    			invalidate();    			
    			mod_Invasion.log("Stranded nexus entity trying to delete itself...");
    		}
    	}
	}
    
    public void emergencyStop()
    {
    	mod_Invasion.log("Nexus manually stopped by command");
    	stop();
    	killAllMobs();
    }
    
    public void debugStatus()
    {
    	mod_Invasion.broadcastToAll("Current Time: " + worldObj.getWorldTime());
    	mod_Invasion.broadcastToAll("Time to next: " + nextAttackTime);
    	mod_Invasion.broadcastToAll("Days to attack: " + daysToAttack);
    	mod_Invasion.broadcastToAll("Mobs left: " + mobsLeftInWave);
    	mod_Invasion.broadcastToAll("Mode: " + mode);
    }
    
    public void debugStartInvaion(int startWave)
    {
    	mod_Invasion.tryGetInvasionPermission(this);
    	startInvasion(startWave);
    }
    
    public void createBolt(int x, int y, int z, int t)
    {
    	EntityIMBolt bolt = new EntityIMBolt(worldObj, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, x + 0.5,  y + 0.5,  z + 0.5, t, 1);
    	worldObj.spawnEntityInWorld(bolt);
    }
    
    public boolean setSpawnRadius(int radius)
    {
    	if(!waveSpawner.isActive() && radius > 8)
    	{
    		spawnRadius = radius;
    		waveSpawner.setRadius(radius);
    		boundingBoxToRadius.setBounds(xCoord - (spawnRadius + 10), 0, zCoord - (spawnRadius + 10), xCoord + (spawnRadius + 10), 127, zCoord + (spawnRadius + 10));
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }

    @Override
	public void attackNexus(int damage)
    {
    	hp -= damage;
    	if(hp <= 0)
    	{
    		hp = 0;
    		if(mode == 1)
    			theEnd();
    	}
    	
    	while(hp + 5 <= lastHp)
    	{
    		mod_Invasion.broadcastToAll("Nexus at " + (lastHp - 5) + " hp");
    		lastHp = lastHp - 5;
    	}
    }
    
	@Override
	public void registerMobDied()
    {
    	nexusKills++;
    	mobsLeftInWave--;    	
    	if(mobsLeftInWave <= 0)
    	{
    		if(lastMobsLeftInWave > 0)
    		{
	    		mod_Invasion.broadcastToAll("Nexus rift stable again!");
	    		mod_Invasion.broadcastToAll("Unleashing tapped energy...");
	    		lastMobsLeftInWave = mobsLeftInWave;
    		}
    		return;
    	}
    	while(mobsLeftInWave + mobsToKillInWave * 0.1F <= lastMobsLeftInWave)
    	{
    		mod_Invasion.broadcastToAll("Nexus rift stabilised to " + (100 - (int)(100 * mobsLeftInWave / (float)mobsToKillInWave)) + "%");
    		lastMobsLeftInWave -= mobsToKillInWave * 0.1F;
    	}
    }
	
	public void registerMobClose()
	{
		
	}
    
	@Override
	public boolean isActivating()
    {
    	return activationTimer > 0 && activationTimer < 400;
    }
	@Override
	public int getMode()
    {
    	return mode;
    }
    
	@Override
	public int getActivationTimer()
    {
    	return activationTimer;
    }

	@Override
	public int getSpawnRadius()
	{
		return spawnRadius;
	}
	
	@Override
	public int getNexusKills()
	{
		return nexusKills;
	}
	
	@Override
	public int getGeneration()
	{
		return generation;
	}
	
	@Override
	public int getNexusLevel()
	{
		return nexusLevel;
	}
	
	public int getPowerLevel()
	{
		return powerLevel;
	}
	
	public int getCookTime()
	{
		return cookTime;
	}
	
	public int getNexusID()
	{
		return -1;
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
	
	@Override
	public World getWorld()
	{
		return worldObj;
	}
   
	@Override
	public List<EntityIMLiving> getMobList()
	{
		return mobList;
	}
	
    public int getActivationProgressScaled(int i)
    {
    	return (activationTimer * i) / 400;
    }
    
    public int getGenerationProgressScaled(int i)
    {
    	return (generation * i) / 3000;
    }
    
    public int getCookProgressScaled(int i)
    {
    	return (cookTime * i) / 1200;
    }

	public int getNexusPowerLevel()
	{
		return powerLevel;
	}
    
    @Override
	public int getCurrentWave()
    {
    	return currentWave;
    }
    
    @Override
	public int getSizeInventory()
    {
        return nexusItemStacks.length;
    }
    
    @Override
	public String getInvName()
    {
        return "Nexus";
    }
    
    @Override
	public int getInventoryStackLimit()
    {
        return 64;
    }
    
    @Override
	public boolean isInvNameLocalized()
    {
		return false;
	}

	@Override
	public boolean isStackValidForSlot(int i, ItemStack itemstack)
	{
		return true;
	}
    
    @Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
    {
        nexusItemStacks[i] = itemstack;
        if(itemstack != null && itemstack.stackSize > getInventoryStackLimit())
        {
            itemstack.stackSize = getInventoryStackLimit();
        }
    }
    
    @Override
	public ItemStack getStackInSlot(int i)
    {
        return nexusItemStacks[i];
    }
    
    @Override
	public ItemStack decrStackSize(int i, int j)
    {
        if(nexusItemStacks[i] != null)
        {
            if(nexusItemStacks[i].stackSize <= j)
            {
                ItemStack itemstack = nexusItemStacks[i];
                nexusItemStacks[i] = null;
                return itemstack;
            }
            ItemStack itemstack1 = nexusItemStacks[i].splitStack(j);
            if(nexusItemStacks[i].stackSize == 0)
            {
                nexusItemStacks[i] = null;
            }
            return itemstack1;
        } else
        {
            return null;
        }
    }
    
    @Override
	public void openChest()
    {
    }

    @Override
	public void closeChest()
    {
    }

    @Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
    {
    	return true;
    }
    
	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		return null;
	}
    
    @Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
    {
    	mod_Invasion.log("Restoring TileEntityNexus from NBT");
        super.readFromNBT(nbttagcompound);
        NBTTagList nbttaglist = nbttagcompound.getTagList("Items");
        nexusItemStacks = new ItemStack[getSizeInventory()];
        for(int i = 0; i < nbttaglist.tagCount(); i++)
        {
            NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.tagAt(i);
            byte byte0 = nbttagcompound1.getByte("Slot");
            if(byte0 >= 0 && byte0 < nexusItemStacks.length)
            {
            	nexusItemStacks[byte0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
            }
        }
        
        
        nbttaglist = nbttagcompound.getTagList("boundPlayers");
        for(int i = 0; i < nbttaglist.tagCount(); i++)
        {
        	boundPlayers.put(((NBTTagCompound)nbttaglist.tagAt(i)).getString("name"), System.currentTimeMillis());
        	mod_Invasion.log("Added bound player: " + ((NBTTagCompound)nbttaglist.tagAt(i)).getString("name"));
        }

        activationTimer = nbttagcompound.getShort("activationTimer");
        mode = nbttagcompound.getInteger("mode");
        currentWave = nbttagcompound.getShort("currentWave");
        spawnRadius = nbttagcompound.getShort("spawnRadius");
        nexusLevel = nbttagcompound.getShort("nexusLevel");
        hp = nbttagcompound.getShort("hp");
        nexusKills = nbttagcompound.getInteger("nexusKills");
        generation = nbttagcompound.getShort("generation");
        powerLevel = nbttagcompound.getInteger("powerLevel");
        lastPowerLevel = nbttagcompound.getInteger("lastPowerLevel");
        nextAttackTime = nbttagcompound.getInteger("nextAttackTime");
        daysToAttack = nbttagcompound.getInteger("daysToAttack");
        continuousAttack = nbttagcompound.getBoolean("continuousAttack");
        
        boundingBoxToRadius.setBounds(xCoord - (spawnRadius + 10), yCoord - (spawnRadius + 40), zCoord - (spawnRadius + 10), xCoord + (spawnRadius + 10), yCoord + (spawnRadius + 40), zCoord + (spawnRadius + 10));
        
        mod_Invasion.log("activationTimer = " + activationTimer);
        mod_Invasion.log("mode = " + mode);
        mod_Invasion.log("currentWave = " + currentWave);
        mod_Invasion.log("spawnRadius = " + spawnRadius);
        mod_Invasion.log("nexusLevel = " + nexusLevel);
        mod_Invasion.log("hp = " + hp);
        mod_Invasion.log("nexusKills = " + nexusKills);
        mod_Invasion.log("powerLevel = " + powerLevel);
        mod_Invasion.log("lastPowerLevel = " + lastPowerLevel);
        mod_Invasion.log("nextAttackTime = " + nextAttackTime);
        //mod_Invasion.log("daysToAttack = " + daysToAttack);
        
        //Restore wave spawner state
        waveSpawner.setRadius(spawnRadius);
        if(mode == 1 || mode == 3 || (mode == 2 && continuousAttack))
        {
        	mod_Invasion.log("Nexus is active; flagging for restore");
        	resumedFromNBT = true;
        	spawnerElapsedRestore = nbttagcompound.getLong("spawnerElapsed");
        	mod_Invasion.log("spawnerElapsed = " + spawnerElapsedRestore);
        }
        
        attackerAI.readFromNBT(nbttagcompound);
    }

    @Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
    {
    	if(mode != 0)
    		mod_Invasion.setNexusUnloaded(this);
    	
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setShort("activationTimer", (short)activationTimer);
        nbttagcompound.setShort("currentWave", (short)currentWave);
        nbttagcompound.setShort("spawnRadius", (short)spawnRadius);
        nbttagcompound.setShort("nexusLevel", (short)nexusLevel);
        nbttagcompound.setShort("hp", (short)hp);
        nbttagcompound.setInteger("nexusKills", nexusKills);
        nbttagcompound.setShort("generation", (short)generation);
        nbttagcompound.setLong("spawnerElapsed", waveSpawner.getElapsedTime());
        nbttagcompound.setInteger("mode", mode);
        nbttagcompound.setInteger("powerLevel", powerLevel);
        nbttagcompound.setInteger("lastPowerLevel", lastPowerLevel);
        nbttagcompound.setInteger("nextAttackTime", nextAttackTime);
        nbttagcompound.setInteger("daysToAttack", daysToAttack);
        nbttagcompound.setBoolean("continuousAttack", continuousAttack);
        
        NBTTagList nbttaglist = new NBTTagList();
        for(int i = 0; i < nexusItemStacks.length; i++)
        {
            if(nexusItemStacks[i] != null)
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)i);
                nexusItemStacks[i].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }
        nbttagcompound.setTag("Items", nbttaglist);
        
        NBTTagList nbttaglist2 = new NBTTagList();
        for(Entry<String, Long> entry : boundPlayers.entrySet())
        {
        	NBTTagCompound nbttagcompound1 = new NBTTagCompound();
        	nbttagcompound1.setString("name", entry.getKey());
        	nbttaglist2.appendTag(nbttagcompound1);
        }
        nbttagcompound.setTag("boundPlayers", nbttaglist2);
        
        attackerAI.writeToNBT(nbttagcompound);
    }
    
    @Override
	public void askForRespawn(EntityIMLiving entity)
    {
    	mod_Invasion.log("Stuck entity asking for respawn: " + entity.toString() + "  " + entity.posX + ", " + entity.posY + ", " + entity.posZ);
    	waveSpawner.askForRespawn(entity);
    }
    
    @Override
	public AttackerAI getAttackerAI()
    {
    	return attackerAI;
    }
    
    protected void setActivationTimer(int i)
    {
    	activationTimer = i;
    }
    
    protected void setNexusLevel(int i)
    {
    	nexusLevel = i;
    }
    
    protected void setNexusKills(int i)
    {
    	nexusKills = i;
    }
    
    protected void setGeneration(int i)
    {
    	generation = i;
    }
    
    protected void setNexusPowerLevel(int i)
    {
    	powerLevel = i;
    }
    
    protected void setCookTime(int i)
    {
    	cookTime = i;
    }
    
	protected void setWave(int wave)
    {
    	currentWave = wave;
    }
    
	private void startInvasion(int startWave)
    {
    	boundingBoxToRadius.setBounds(xCoord - (spawnRadius + 10), yCoord - (spawnRadius + 40), zCoord - (spawnRadius + 10), xCoord + (spawnRadius + 10), yCoord + (spawnRadius + 40), zCoord + (spawnRadius + 10));
    	if(mode == 2 && continuousAttack)
    	{
    		mod_Invasion.broadcastToAll("Can't activate nexus when already under attack!");
    		return;
    	}
    		
    	if(mode == 0 || mode == 2)
    	{
	    	if(waveSpawner.isReady())
	    	{
	    		try
	    		{
	    			currentWave = startWave;
		    		waveSpawner.beginNextWave(currentWave);
		    		if(mode == 0)
		    			setMode(1);
		    		else
		    			setMode(3);	
		    		
			    	bindPlayers();
			    	hp = maxHp;
			    	lastHp = maxHp;
			    	waveDelayTimer = -1;
			    	timer = System.currentTimeMillis();
		    		mod_Invasion.broadcastToAll("The first wave is coming soon!");
		    		mod_Invasion.playGlobalSFX("invsound.rumble");
	    		}
	    		catch (WaveSpawnerException e)
	    		{
	    			stop();
	    			mod_Invasion.log(e.getMessage());
	    			mod_Invasion.broadcastToAll(e.getMessage());
	    		}
	    	}
	    	else
	    	{
	    		mod_Invasion.log("Wave spawner not in ready state");
	    	}
    	}
    	else
    	{
    		mod_Invasion.log("Tried to activate nexus while already active");
    	}
    }
    
    private void startContinuousPlay()
    {
    	boundingBoxToRadius.setBounds(xCoord - (spawnRadius + 10), 0, zCoord - (spawnRadius + 10), xCoord + (spawnRadius + 10), 127, zCoord + (spawnRadius + 10));
    	if(mode == 4 && waveSpawner.isReady() && mod_Invasion.tryGetInvasionPermission(this))
    	{
    		setMode(2);
	    	hp = maxHp;
	    	lastHp = maxHp;
	    	lastPowerLevel = powerLevel;
	    	lastWorldTime = worldObj.getWorldTime();
	    	nextAttackTime = (int)((lastWorldTime / 24000) * 24000) + 14000;
	    	if(lastWorldTime % 24000 > 12000 && lastWorldTime % 24000 < 16000)
	    	{
	    		mod_Invasion.broadcastToAll("The night looms around the nexus...");
	    	}
	    	else
	    	{
	    		mod_Invasion.broadcastToAll("Nexus activated and stable");
	    	}
    	}
    	else
    	{
    		mod_Invasion.broadcastToAll("Couldn't activate nexus");
    	}
    }
    
    private void doInvasion(int elapsed) throws WaveSpawnerException
    {
    	//worldObj.spawnParticle("heart", xCoord, yCoord + 1, zCoord, 0, 0, 0);
    	if(waveSpawner.isActive())
		{
			if(hp <= 0)
    		{
    			theEnd();
    		}
			else
			{
				generateFlux(1);
				if(waveSpawner.isWaveComplete())
    			{
					//Delay until time for next wave
    				if(waveDelayTimer == -1)
    				{
    					mod_Invasion.broadcastToAll("Wave " + currentWave + " almost complete!");
    					mod_Invasion.playGlobalSFX("invsound.chime");
    					waveDelayTimer = 0;
    					waveDelay = waveSpawner.getWaveRestTime();
    				}
    				else
    				{
    					waveDelayTimer += elapsed;
    					if(waveDelayTimer > waveDelay)
    					{
    						currentWave++;
    						mod_Invasion.broadcastToAll("Wave " + currentWave + " about to begin"); 
    	    				waveSpawner.beginNextWave(currentWave);
    	    				waveDelayTimer = -1;
    	    				mod_Invasion.playGlobalSFX("invsound.rumble");
    	    				if(currentWave > nexusLevel)
    	    					nexusLevel = currentWave;
    					}
    				}	
    			}
    			else
    			{
    				//If in a wave, perform spawning duties
    				waveSpawner.spawn(elapsed);
    			}
			}
		}
    }
    
    private void doContinuous(int elapsed)
    {
    	powerLevelTimer += elapsed;
    	if(powerLevelTimer > 2200)
    	{
    		powerLevelTimer -= 2200;
    		generateFlux(5 + (int)(5 * powerLevel / 1550F));
    		if(nexusItemStacks[0] == null || nexusItemStacks[0].getItem().itemID != mod_Invasion.itemDampingAgent.itemID)
    			powerLevel++;
    	}
    	
    	// Check separately for fine-grained reduction and for stopping the nexus
    	if(nexusItemStacks[0] != null && nexusItemStacks[0].getItem().itemID == mod_Invasion.itemStrongDampingAgent.itemID)
    	{	
    		if(powerLevel >= 0 && !continuousAttack)
    		{
    			powerLevel--;
    			if(powerLevel < 0)
    				stop();
    		}
    	}

    	if(!continuousAttack)
    	{
    		long currentTime = worldObj.getWorldTime();
    		int timeOfDay = (int)(lastWorldTime % 24000);
    		if(timeOfDay < 12000 && currentTime % 24000 >= 12000 && currentTime + 12000 > nextAttackTime)
				mod_Invasion.broadcastToAll("The night looms around the nexus...");
    		
    		if(lastWorldTime > currentTime) // Time flowed backwards
    			nextAttackTime -= lastWorldTime - currentTime; // Correct the next attack time
    		
    		lastWorldTime = currentTime;
    		
	    	if(lastWorldTime >= nextAttackTime)
	    	{
	    		float difficulty = 1.0F + powerLevel / 4500;
	    		float tierLevel = 1.0F + powerLevel / 4500;
	    		int timeSeconds = 240;
	    		try
	    		{
	    			Wave wave = waveBuilder.generateWave(difficulty, tierLevel, timeSeconds);
	    			mobsLeftInWave = lastMobsLeftInWave = mobsToKillInWave = (int)(wave.getTotalMobAmount() * 0.8F);
	    			waveSpawner.beginNextWave(wave);
	    			continuousAttack = true;
	    			int days = mod_Invasion.getMinContinuousModeDays() + worldObj.rand.nextInt(1 + mod_Invasion.getMaxContinuousModeDays() - mod_Invasion.getMinContinuousModeDays());
	    			nextAttackTime = (int)((currentTime / 24000) * 24000) + 14000 + (days * 24000);
	    			hp = lastHp = 100;	    			
	    			zapTimer = 0;
	    			waveDelayTimer = -1;
	    			mod_Invasion.broadcastToAll("Forces are destabilising the nexus!");
	    			mod_Invasion.playGlobalSFX("invsound.rumble");
	    		}
	    		catch (WaveSpawnerException e)
				{
					mod_Invasion.log(e.getMessage());
					e.printStackTrace();
					stop();
				}
	    	}
    	}
    	else
    	{
    		if(hp <= 0)
    		{
    			continuousAttack = false;
    			continuousNexusHurt();
    		}
    		else if(waveSpawner.isWaveComplete())
    		{
    			if(mobsLeftInWave > 0)
    				return;
    			
    			//Delay until finishing
				if(waveDelayTimer == -1)
				{
					waveDelayTimer = 0;
					waveDelay = waveSpawner.getWaveRestTime();
				}
				else
				{
					waveDelayTimer += elapsed;
					if(waveDelayTimer > waveDelay && zapTimer < -200)
					{
	    				waveDelayTimer = -1;
	    				continuousAttack = false;
	    				waveSpawner.stop();
		    			hp = 100;
		    			lastHp = 100;
		    			lastPowerLevel = powerLevel;
					}
				}
				
				zapTimer--;
				if(mobsLeftInWave <= 0)
				{
					if(zapTimer <= 0 && zapEnemy(1))
					{
						zapEnemy(0);
						zapTimer = 23;
					}
				}
    		}
    		else
    		{
	    		try
	    		{
	    			waveSpawner.spawn(elapsed);
	    		}
	    		catch (WaveSpawnerException e)
				{
					mod_Invasion.log(e.getMessage());
					e.printStackTrace();
					stop();
				}
    		}
    	}
    }
    
    private void updateStatus()
    {
    	if(nexusItemStacks[0] != null)
    	{
    		if(nexusItemStacks[0].getItem().itemID == mod_Invasion.itemIMTrap.itemID && nexusItemStacks[0].getItemDamage() == 0)
    		{
	    		if(cookTime < 1200)
	    		{
	    			if(mode == 0)
	    				cookTime++;
	    			else
	    				cookTime += 9;
	    		}
	    		
	    		if(cookTime >= 1200)
	    		{
	    			// Add item to output slot if not blocked
	    			if(nexusItemStacks[1] == null)
	                {
	    				nexusItemStacks[1] = new ItemStack(mod_Invasion.itemIMTrap, 1, 1);
	    				if(--nexusItemStacks[0].stackSize <= 0)
	    					nexusItemStacks[0] = null;
	    				cookTime = 0;
	                }
	    			else if(nexusItemStacks[1].getItem().itemID == mod_Invasion.itemIMTrap.itemID && nexusItemStacks[1].getItemDamage() == EntityIMTrap.TRAP_RIFT)
	    			{
	    				nexusItemStacks[1].stackSize++;
	    				if(--nexusItemStacks[0].stackSize <= 0)
	    					nexusItemStacks[0] = null;
	    				cookTime = 0;
	    			}    				
	    		}
    		}
    		else if(nexusItemStacks[0].getItem().itemID == mod_Invasion.itemRiftFlux.itemID && nexusItemStacks[0].getItemDamage() == 1)
    		{
    			if(cookTime < 1200 && nexusLevel >= 10)
	    		{
	    			cookTime += 5;
	    		}
    			
    			if(cookTime >= 1200)
	    		{
	    			// Add item to output slot if not blocked
	    			if(nexusItemStacks[1] == null)
	                {
	    				nexusItemStacks[1] = new ItemStack(mod_Invasion.itemStrongCatalyst, 1);
	    				if(--nexusItemStacks[0].stackSize <= 0)
	    					nexusItemStacks[0] = null;
	    				cookTime = 0;
	                }				
	    		}
    		}
    	}
    	else
    	{
    		cookTime = 0;
    	}
    	
    	if(activationTimer >= 400)
		{
    		activationTimer = 0;
			if(mod_Invasion.tryGetInvasionPermission(this) && nexusItemStacks[0] != null)
			{
				if(nexusItemStacks[0].getItem().itemID == mod_Invasion.itemNexusCatalyst.itemID)
                {
					nexusItemStacks[0].stackSize--;
                    if(nexusItemStacks[0].stackSize == 0)
                    	nexusItemStacks[0] = null;                        
                    startInvasion(1);
                }
				else if(nexusItemStacks[0].getItem().itemID == mod_Invasion.itemStrongCatalyst.itemID)
				{
					nexusItemStacks[0].stackSize--;
                    if(nexusItemStacks[0].stackSize == 0)
                    	nexusItemStacks[0] = null;                        
                    startInvasion(10);
				}
				else if(nexusItemStacks[0].getItem().itemID == mod_Invasion.itemStableNexusCatalyst.itemID)
				{
					nexusItemStacks[0].stackSize--;
                    if(nexusItemStacks[0].stackSize == 0)
                    	nexusItemStacks[0] = null;   
                    startContinuousPlay();
				}
			}
			
		}
    	else if(mode == 0 || mode == 4)
    	{
			if(nexusItemStacks[0] != null)
			{
				if(nexusItemStacks[0].getItem().itemID == mod_Invasion.itemNexusCatalyst.itemID
						|| nexusItemStacks[0].getItem().itemID == mod_Invasion.itemStrongCatalyst.itemID)
				{
					activationTimer++;
					mode = 0;
				}
				else if(nexusItemStacks[0].getItem().itemID == mod_Invasion.itemStableNexusCatalyst.itemID)
				{
					activationTimer++;
					mode = 4;
				}
			}
			else
			{
				activationTimer = 0;
			}
    	}
    	else if(mode == 2)
    	{
    		if(nexusItemStacks[0] != null)
    		{
    			if(nexusItemStacks[0].getItem().itemID == mod_Invasion.itemNexusCatalyst.itemID
    					|| nexusItemStacks[0].getItem().itemID == mod_Invasion.itemStrongCatalyst.itemID)
					activationTimer++;
    		}
    		else
			{
				activationTimer = 0;
			}
    	}
    }
    
    private void generateFlux(int increment)
    {
    	generation += increment;
    	if(generation >= 3000)
		{
			if(nexusItemStacks[1] == null)
            {
				nexusItemStacks[1] = new ItemStack(mod_Invasion.itemRiftFlux, 1, 1);
				generation -= 3000;
            }
			else if(nexusItemStacks[1].getItem().itemID == mod_Invasion.itemRiftFlux.itemID)
			{
				nexusItemStacks[1].stackSize++;
				generation -= 3000;
			}
		}
    }
    
    private void stop()
    {
    	if(mode == 3)
    	{
    		setMode(2);
    		int days = mod_Invasion.getMinContinuousModeDays() + worldObj.rand.nextInt(1 + mod_Invasion.getMaxContinuousModeDays() - mod_Invasion.getMinContinuousModeDays());
			nextAttackTime = (int)((worldObj.getWorldTime() / 24000) * 24000) + 14000 + (days * 24000);
    	}
    	else
    	{
    		setMode(0);
    	}
    	
    	waveSpawner.stop();
    	mod_Invasion.setInvasionEnded(this);
    	activationTimer = 0;
    	currentWave = 0;
    	errorState = 0;
    }
    
   //Binds all players' lives to the nexus for BIND_EXPIRE_TIME
    private void bindPlayers()
    {
		@SuppressWarnings("unchecked")
		List<EntityPlayer> players = worldObj.getEntitiesWithinAABB(EntityPlayer.class, boundingBoxToRadius);
    	for(EntityPlayer entityPlayer : players)
    	{
    		long time = System.currentTimeMillis();
    		if(!boundPlayers.containsKey(entityPlayer.username))
    		{
    			mod_Invasion.broadcastToAll(entityPlayer.username + (entityPlayer.username.toLowerCase().endsWith("s") ? "'" : "'s" ) + " life is now bound to the nexus");
    		}
    		else if(time - boundPlayers.get(entityPlayer.username) > BIND_EXPIRE_TIME)
    		{
    			mod_Invasion.broadcastToAll(entityPlayer.username + (entityPlayer.username.toLowerCase().endsWith("s") ? "'" : "'s" ) + " life is now bound to the nexus");
    		}
    		boundPlayers.put(entityPlayer.username, time);
    	}
    }
    
	@SuppressWarnings("unchecked")
	private void updateMobList()
    {
    	mobList = worldObj.getEntitiesWithinAABB(EntityIMLiving.class, boundingBoxToRadius);
    	mobsSorted = false;
    }
    
    // Should be private but the GUI data design is fail
    protected void setMode(int i)
    {
    	mode = i;
    	if(mode == 0)
    		setActive(false);
    	else
    		setActive(true);
    }
    
    private void setActive(boolean flag)
    {
    	if(worldObj != null)
    	{
	    	int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
	    	if(flag)
	    	{
		        worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, (meta & 4) == 0 ? meta + 4 : meta, 3);
	    	}
	    	else
	    	{
	    		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, (meta & 4) == 4 ? meta - 4 : meta, 3);
	    	}
	    	
	    	//worldObj.notifyBlockChange(xCoord, yCoord, zCoord, mod_Invasion.blockNexus.blockID);
    	}
    }
    
    //Re-acquire entites in range after game loaded
    private int acquireEntities()
    {
    	AxisAlignedBB bb = boundingBoxToRadius.expand(10, 128, 10);
    	@SuppressWarnings("unchecked")
		List<EntityIMLiving> entities = worldObj.getEntitiesWithinAABB(EntityIMLiving.class, bb);
    	for(EntityIMLiving entity : entities)
    	{
    		entity.acquiredByNexus(this);
    	}
    	mod_Invasion.log("Acquired " + entities.size() + " entities after state restore");
    	return entities.size();
    }
    
    //Ends the gamemode in a finale
    private void theEnd()
    {
    	if(!worldObj.isRemote)
    	{
	    	stop();	    	
	    	long time = System.currentTimeMillis();
	    	for(Entry<String, Long> entry : boundPlayers.entrySet())
	    	{
	    		if(time - entry.getValue() < BIND_EXPIRE_TIME)
	    		{
	    			EntityPlayer player = worldObj.getPlayerEntityByName(entry.getKey());
	    			if(player != null)
	    			{
	    				player.attackEntityFrom(DamageSource.magic, 500);
	    				mod_Invasion.playGlobalSFX("random.explode");
	    			}
	    			else
	    			{
	    				//SMP
	    				//mod_Invasion.addToDeathList(entry.getKey(), entry.getValue());  				
	    			}
	    		}
	    	}
	    	boundPlayers.clear();	    	
	    	killAllMobs();
    	}
    }
    
    private void continuousNexusHurt()
    {
    	mod_Invasion.broadcastToAll("Nexus severely damaged!");
    	mod_Invasion.playGlobalSFX("random.explode");
    	killAllMobs();
    	waveSpawner.stop();
    	powerLevel = (int)((powerLevel - (powerLevel - lastPowerLevel)) * 0.7F);
    	lastPowerLevel = powerLevel;
    	if(powerLevel < 0)
    	{
    		powerLevel = 0;
    		stop();
    	}
    }
    
    /**
     * Kills every mob in nexus range + 10 (its range bounding box)
     */
    private void killAllMobs()
    {
    	@SuppressWarnings("unchecked")
		List<EntityIMLiving> mobs = worldObj.getEntitiesWithinAABB(EntityIMLiving.class, boundingBoxToRadius);
    	for(EntityIMLiving mob : mobs)
    	{
    		mob.attackEntityFrom(DamageSource.magic, 500);
    	}
    }
    
    private boolean zapEnemy(int sfx)
    {
    	if(mobList.size() > 0)
    	{
	    	if(!mobsSorted)
	    		Collections.sort(mobList, new ComparatorEntityDistance(xCoord, yCoord, zCoord));
	    	
	    	EntityIMLiving mob = mobList.remove(mobList.size() - 1);
	    	mob.attackEntityFrom(DamageSource.magic, 500);
	    	EntityIMBolt bolt = new EntityIMBolt(worldObj, xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, mob.posX, mob.posY, mob.posZ, 15, sfx);
	    	worldObj.spawnEntityInWorld(bolt);
	    	return true;
    	}
    	else
    	{
    		return false;
    	}
    }
    
    private boolean resumeSpawnerContinuous()
    {
    	try
    	{
    		mod_Invasion.tryGetInvasionPermission(this);
    		float difficulty = 1.0F + powerLevel / 4500;
    		float tierLevel = 1.0F + powerLevel / 4500;
    		int timeSeconds = 240;
        	Wave wave = waveBuilder.generateWave(difficulty, tierLevel, timeSeconds);
			mobsToKillInWave = (int)(wave.getTotalMobAmount() * 0.8F);
			mod_Invasion.log("Original mobs to kill: " + mobsToKillInWave);
			mobsLeftInWave = lastMobsLeftInWave = mobsToKillInWave - waveSpawner.resumeFromState(wave, spawnerElapsedRestore, spawnRadius);
			return true;
    	}
    	catch(WaveSpawnerException e)
    	{
    		mod_Invasion.log("Error resuming spawner:" + e.getMessage());
    		waveSpawner.stop();
    		return false;
    	}
    	finally
    	{
    		mod_Invasion.setInvasionEnded(this);
    	}
    }
    
    private boolean resumeSpawnerInvasion()
    {
    	try
    	{
    		mod_Invasion.tryGetInvasionPermission(this);
        	waveSpawner.resumeFromState(currentWave, spawnerElapsedRestore, spawnRadius);
        	return true;
    	}
    	catch(WaveSpawnerException e)
    	{
    		mod_Invasion.log("Error resuming spawner:" + e.getMessage());
    		waveSpawner.stop();
    		return false;
    	}
    	finally
    	{
    		mod_Invasion.setInvasionEnded(this);
    	}
    }
    
    @SuppressWarnings("unused")
	private void playSoundTo()
    {
    	
    }
    
    private void updateAI()
    {
    	attackerAI.update();
    }
    
    private IMWaveSpawner waveSpawner;
    private IMWaveBuilder waveBuilder;
    private ItemStack nexusItemStacks[];
    private AxisAlignedBB boundingBoxToRadius;
    private HashMap<String, Long> boundPlayers;
    private List<EntityIMLiving> mobList;
    private AttackerAI attackerAI;
    private int activationTimer;
    private int currentWave;
    private int spawnRadius;
    private int nexusLevel;
    private int nexusKills;
    private int generation;
    private int cookTime;
    private int maxHp;
    private int hp;
    private int lastHp;
    private int mode;
    private int powerLevel;
    private int lastPowerLevel;
    private int powerLevelTimer;
    private int mobsLeftInWave;
    private int lastMobsLeftInWave;
    private int mobsToKillInWave;
    private int nextAttackTime;
	private int daysToAttack;
	private long lastWorldTime;
	private int zapTimer;
    private int errorState;
    private int tickCount;
    private int cleanupTimer;
    private long spawnerElapsedRestore;
    private long timer;
	private long waveDelayTimer;
	private long waveDelay;
    private boolean continuousAttack;
    private boolean mobsSorted;
    private boolean resumedFromNBT;
    
    private static final long BIND_EXPIRE_TIME = 300000;
}