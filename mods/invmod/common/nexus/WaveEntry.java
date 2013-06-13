
package mods.invmod.common.nexus;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mods.invmod.common.mod_Invasion;
import mods.invmod.common.util.ISelect;


/**
 * Defines a component of a wave
 * 
 * @author Lieu
 */
public class WaveEntry
{	
	public WaveEntry(int timeBegin, int timeEnd, int amount, int granularity, ISelect<IEntityIMPattern> mobPool)
	{
		this(timeBegin, timeEnd, amount, granularity, mobPool, -180, 180, 1);
	}
	
	public WaveEntry(int timeBegin, int timeEnd, int amount, int granularity, ISelect<IEntityIMPattern> mobPool, int angleRange, int minPointsInRange)
	{
		this(timeBegin, timeEnd, amount, granularity, mobPool, 0, 0, minPointsInRange);
		minAngle = new Random().nextInt(360) - 180;
		maxAngle = minAngle + angleRange;
		while(maxAngle > 180)
			maxAngle -= 360;
	}
	
	public WaveEntry(int timeBegin, int timeEnd, int amount, int granularity, ISelect<IEntityIMPattern> mobPool, int minAngle, int maxAngle, int minPointsInRange)
	{
		spawnList = new ArrayList<EntityConstruct>();
		alerts = new HashMap<Integer, String>();
		this.timeBegin = timeBegin;
		this.timeEnd = timeEnd;
		this.amount = amount;
		this.granularity = granularity;
		this.mobPool = mobPool;
		this.minAngle = minAngle;
		this.maxAngle = maxAngle;
		this.minPointsInRange = minPointsInRange;
		amountQueued = 0;
		elapsed = 0;
		toNextSpawn = 0;
		nextAlert = Integer.MAX_VALUE;
	}
	
	/**
	 * Returns mob constructs of the next spawns according to time elapsed
	 * since the last call. Will eventually return up to the maximum amount
	 * of mobs set.
	 */
	public int doNextSpawns(int elapsedMillis, ISpawnerAccess spawner)
	{
		toNextSpawn -= elapsedMillis;
		if(nextAlert <= elapsed - toNextSpawn)
		{
			sendNextAlert(spawner);
		}
		
		if(toNextSpawn <= 0)
		{
			elapsed += granularity;
			toNextSpawn += granularity;
			if(toNextSpawn < 0)
			{
				elapsed -= toNextSpawn;
				toNextSpawn = 0;
			}
				
			int amountToSpawn = Math.round(amount * elapsed / (float)(timeEnd - timeBegin)) - amountQueued;
			if(amountToSpawn > 0)
			{
				if(amountToSpawn + amountQueued > amount)
					amountToSpawn = amount - amountQueued;
				
				while(amountToSpawn > 0)
				{
					IEntityIMPattern pattern = mobPool.selectNext();
					if(pattern != null)
					{
						EntityConstruct mobConstruct = pattern.generateEntityConstruct(minAngle, maxAngle);
						if(mobConstruct != null)
						{
							amountToSpawn--;
							amountQueued++;
							spawnList.add(mobConstruct);
						}
					}
					else
					{
						mod_Invasion.log("A selection pool in wave entry " + toString() + " returned empty");
						mod_Invasion.log("Pool: " + mobPool.toString());
					}
				}			
			}
		}
		
		if(spawnList.size() > 0)
		{
			int numberOfSpawns = 0;
			if(spawner.getNumberOfPointsInRange(minAngle, maxAngle, SpawnType.HUMANOID) >= minPointsInRange)
			{
				for(int i = spawnList.size() - 1; i >= 0; i--)
				{
					if(spawner.attemptSpawn(spawnList.get(i), minAngle, maxAngle))
					{
						numberOfSpawns++;
						spawnList.remove(i);
					}
				}
			}
			else
			{
				reviseSpawnAngles(spawner);
			}
			return numberOfSpawns;
		}
		return 0;
	}
	
	/**
	 * Returns the wave entry to its initial pre-spawn state
	 */
	public void resetToBeginning()
	{
		elapsed = 0;
		amountQueued = 0;
		mobPool.reset();
	}
	
	/**
	 * Sets the wave entry to the specified total time elapsed
	 * without spawning or resetting
	 */
	public void setToTime(int millis)
	{
		elapsed = millis;
	}
	
	/**
	 * Returns the time in milliseconds this entry begins spawning
	 */
	public int getTimeBegin()
	{
		return timeBegin;
	}
	
	/**
	 * Returns the time in milliseconds this entry ends spawning
	 */
	public int getTimeEnd()
	{
		return timeEnd;
	}
	
	/**
	 * Returns the number of mobs it will spawn between timeBegin() and timeEnd()
	 */
	public int getAmount()
	{
		return amount;
	}

	/**
	 * Returns the amount of time between groups of spawns
	 */
	public int getGranularity()
	{
		return granularity;
	}
	
	public void addAlert(String message, int timeElapsed)
	{
		alerts.put(timeElapsed, message);
		if(timeElapsed < nextAlert)
			nextAlert = timeElapsed;
	}
	
	@Override
	public String toString()
	{
		return "WaveEntry@" + Integer.toHexString(hashCode()) + "#time=" + timeBegin + "-" + timeEnd + "#amount=" + amount;
	}
	
	private void sendNextAlert(ISpawnerAccess spawner)
	{
		String message = alerts.remove(nextAlert);
		if(message != null)	
			spawner.sendSpawnAlert(message);
			
		nextAlert = Integer.MAX_VALUE;
		if(alerts.size() > 0)
		{
			for(Integer key : alerts.keySet())
			{
				if(key < nextAlert)
					nextAlert = key;
			}
		}
	}
	
	private void reviseSpawnAngles(ISpawnerAccess spawner)
	{	
		int angleRange = maxAngle - minAngle;
		while(angleRange < 0)
			angleRange += 360;
		if(angleRange == 0) // Invalid values came from somewhere
			angleRange = 360;
		
		List<Integer> validAngles = new ArrayList<Integer>();
		int nextAngle;
		for(int angle = -180; angle < 180; angle += angleRange)
		{
			nextAngle = angle + angleRange;
			if(nextAngle >= 180)
				nextAngle -= 360;
			if(spawner.getNumberOfPointsInRange(angle, nextAngle, SpawnType.HUMANOID) >= minPointsInRange)
				validAngles.add(angle);
		}
		
		if(validAngles.size() > 0)
		{
			minAngle = validAngles.get(new Random().nextInt(validAngles.size()));
			maxAngle = minAngle + angleRange;
			while(maxAngle >= 180)
				maxAngle -= 360;
		}
		else
		{
			if(minPointsInRange > 1)
			{
				mod_Invasion.log("Can't find a direction with enough spawn points: " + minPointsInRange
								+ ". Lowering requirement.");
				minPointsInRange = 1;
			}
			else if(maxAngle - minAngle < 360)
			{
				mod_Invasion.log("Can't find a direction with enough spawn points: " + minPointsInRange
								+ ". Switching to 360 degree mode for this entry");	
				
				minAngle = -180;
				maxAngle = 180;
			}
			else
			{
				mod_Invasion.log("Wave entry cannot find a single spawn point");
				spawner.noSpawnPointNotice();
			}
		}
	}

	private int timeBegin;
	private int timeEnd;
	private int amount;
	private int granularity;
	private int amountQueued;
	private int elapsed;
	private int toNextSpawn;
	private int minAngle;
	private int maxAngle;
	private int minPointsInRange;
	private int nextAlert;
	private ISelect<IEntityIMPattern> mobPool;
	private List<EntityConstruct> spawnList;
	private Map<Integer, String> alerts;
}