
package mods.invmod.common.nexus;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mods.invmod.common.mod_Invasion;
import mods.invmod.common.entity.EntityIMLiving;
import mods.invmod.common.entity.EntityIMZombie;
import net.minecraft.entity.EntityList;

/**
 * Spawns mobs into the world by reading off defined Wave objects.
 * 
 * @author Lieu *
 */
public class IMWaveSpawner implements ISpawnerAccess
{
	private final int MAX_SPAWN_TRIES = 20;
	private final int NORMAL_SPAWN_HEIGHT = 30;
	private final int MIN_SPAWN_POINTS_TO_KEEP = 15;
	private final int MIN_SPAWN_POINTS_TO_KEEP_BELOW_HEIGHT_CUTOFF = 20;
	private final int HEIGHT_CUTOFF = 35;
	private final float SPAWN_POINT_CULL_RATE = 0.3F;
	
	private SpawnPointContainer spawnPointContainer;
	private INexusAccess nexus;
	private MobBuilder mobBuilder;
	private Random rand;
	private Wave currentWave;
	private boolean active;
	private boolean waveComplete;
	private boolean spawnMode;
	private boolean debugMode;
	private int spawnRadius;
	private int currentWaveNumber;
	private int successfulSpawns;
	private long elapsed;
	
	public IMWaveSpawner(INexusAccess tileEntityNexus, int radius)
	{
		nexus = tileEntityNexus;
		active = false;
		waveComplete = false;
		spawnMode = true;
		debugMode = false;
		spawnRadius = radius;
		currentWaveNumber = 1;
		elapsed = 0;
		successfulSpawns = 0;
		rand = new Random();
		spawnPointContainer = new SpawnPointContainer();
		mobBuilder = new MobBuilder();
	}
	
	public long getElapsedTime()
	{
		return elapsed;
	}
	
	public void setRadius(int radius)
	{
		if(radius > 8)
		{
			spawnRadius = radius;
		}
	}
	
	/**
	 * Starts new wave of specified number
	 */
	public void beginNextWave(int waveNumber) throws WaveSpawnerException
	{				
		beginNextWave(IMWaveBuilder.generateMainInvasionWave(waveNumber));
	}
	
	public void beginNextWave(Wave wave) throws WaveSpawnerException
	{				
		if(!active)
		{
			generateSpawnPoints();
			//if(spawnPointContainer.getNumberOfSpawnPoints(SpawnType.HUMANOID) < 10)
				//throw new WaveSpawnerException("Not enough spawn points for type " + SpawnType.HUMANOID);
		}
		else
		{
			if(debugMode)
				mod_Invasion.log("Successful spawns last wave: " + successfulSpawns);
		}
		
	
		wave.resetWave();
		waveComplete = false;
		active = true;
		currentWave = wave;
		elapsed = 0;
		successfulSpawns = 0;
		
		if(debugMode)
			mod_Invasion.log("Defined mobs this wave: " + getTotalDefinedMobsThisWave());
	}
	
	/**
	 * Attempts to spawn mobs from Wave structure according to elapsed time
	 */
	public void spawn(int elapsedMillis) throws WaveSpawnerException
	{		
		elapsed += elapsedMillis;
		if(waveComplete || !active)
			return;
		
		// Number of valid locations could change
		if(spawnPointContainer.getNumberOfSpawnPoints(SpawnType.HUMANOID) < 10)
		{
			generateSpawnPoints();
			if(spawnPointContainer.getNumberOfSpawnPoints(SpawnType.HUMANOID) < 10)
				throw new WaveSpawnerException("Not enough spawn points for type " + SpawnType.HUMANOID);
		}
		
		currentWave.doNextSpawns(elapsedMillis, this);	
		
		if(currentWave.isComplete())
			waveComplete = true;
	}
	
	/**
	 * Attempts to restore the spawner after an NBT load
	 */
	public int resumeFromState(Wave wave, long elapsedTime, int radius) throws WaveSpawnerException
	{
		spawnRadius = radius;
		stop();
		beginNextWave(wave);
		
		setSpawnMode(false);
		int numberOfSpawns = 0;
		for(; elapsed < elapsedTime; elapsed += 100)
		{
			numberOfSpawns += currentWave.doNextSpawns(100, this);
		}
		setSpawnMode(true);
		return numberOfSpawns;
	}
	
	/**
	 * Attempts to restore the spawner after an NBT load
	 */
	public void resumeFromState(int waveNumber, long elapsedTime, int radius) throws WaveSpawnerException
	{
		spawnRadius = radius;
		stop();
		beginNextWave(waveNumber);
		
		setSpawnMode(false);
		for(; elapsed < elapsedTime; elapsed += 100)
		{
			currentWave.doNextSpawns(100, this);
		}
		setSpawnMode(true);
	}
	
	/**
	 * Sets the wave spawner to a non-spawning state
	 */
	public void stop()
	{
		active = false;
	}
	
	/**
	 * Returns true if the wave spawner is currently in a mob spawning state
	 */
	public boolean isActive()
	{
		return active;
	}
	
	public boolean isReady()
	{
		if(!active && nexus != null && nexus.getWorld() != null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Returns true if there are no more mobs to spawn in this wave
	 */
	public boolean isWaveComplete()
	{
		return waveComplete;
	}
	
	/**
	 * Returns duration of the wave
	 */
	public int getWaveDuration()
	{
		return currentWave.getWaveTotalTime();
	}
	
	/**
	 * Returns time in milliseconds of intended waiting after this wave
	 */
	public int getWaveRestTime()
	{
		return currentWave.getWaveBreakTime();
	}
	
	/**
	 * Returns the number of mob spawns that made it to world.entityJoinedWorld()
	 */
	public int getSuccessfulSpawnsThisWave()
	{
		return successfulSpawns;
	}
	
	/**
	 * Returns the number of spawns attempted from mob constructs received by wave definitions
	 */
	public int getTotalDefinedMobsThisWave()
	{
		return currentWave.getTotalMobAmount();
	}
	
	/**
	 * Will try to respawn the mob at a spawn point location
	 */
	public void askForRespawn(EntityIMLiving entity)
    {
		if(spawnPointContainer.getNumberOfSpawnPoints(SpawnType.HUMANOID) > 10)
		{
			SpawnPoint spawnPoint = spawnPointContainer.getRandomSpawnPoint(SpawnType.HUMANOID);
			entity.setLocationAndAngles(spawnPoint.getXCoord(), spawnPoint.getYCoord(), spawnPoint.getZCoord(), 0.0F, 0.0F);
		}
    }
	
	@Override
	public void sendSpawnAlert(String message)
	{
		if(debugMode)
			mod_Invasion.log(message);
		
		mod_Invasion.broadcastToAll(message);
	}
	
	@Override
	public void noSpawnPointNotice()
	{
		
	}
	
	/**
	 * Sets debug mode on or off. Debug mode gives much more logging.
	 */
	public void debugMode(boolean isOn)
	{
		debugMode = isOn;
	}
	
	/**
	 * Returns number of points of specified type within the specified range
	 */
	@Override
	public int getNumberOfPointsInRange(int minAngle, int maxAngle, SpawnType type)
	{
		return spawnPointContainer.getNumberOfSpawnPoints(type, minAngle, maxAngle);
	}
	
	/**
	 * Sets whether the spawner will physically spawn mobs in the world.
	 * The spawner acts as though it is successful in any case it would be otherwise.
	 */
	public void setSpawnMode(boolean flag)
	{
		spawnMode = flag;
	}
	
	/**
	 * Gives the spawner a custom selection of spawn points to use
	 */
	public void giveSpawnPoints(SpawnPointContainer spawnPointContainer)
	{
		this.spawnPointContainer = spawnPointContainer;
	}
	
	/**
	 * Tries to spawn specified mob and return true if successful. Still returns
	 * true if spawnMode is disabled and it would have been able to successfully spawn.
	 */
	@Override
	public boolean attemptSpawn(EntityConstruct mobConstruct, int minAngle, int maxAngle)
	{				
		if(nexus.getWorld() == null)
		{
			if(spawnMode)
				return false;
		}
		
		EntityIMLiving mob = mobBuilder.createMobFromConstruct(mobConstruct, nexus.getWorld(), nexus);
		if(mob == null)
		{
			mod_Invasion.log("Invalid entity construct");
			return false;
		}
		
		int spawnTries = getNumberOfPointsInRange(minAngle, maxAngle, SpawnType.HUMANOID);
		if(spawnTries > MAX_SPAWN_TRIES)
			spawnTries = MAX_SPAWN_TRIES;
		
		for(int j = 0; j < spawnTries; j++) // Expect low collision in practice
		{
			SpawnPoint spawnPoint;
			if(maxAngle - minAngle >= 360)
				spawnPoint = spawnPointContainer.getRandomSpawnPoint(SpawnType.HUMANOID);
			else
				spawnPoint = spawnPointContainer.getRandomSpawnPoint(SpawnType.HUMANOID, minAngle, maxAngle);
			
			if(spawnPoint == null)
				return false;
			
			if(!spawnMode)
			{
				successfulSpawns++;
				if(debugMode)
				{
					mod_Invasion.log("[Spawn] Time: " + currentWave.getTimeInWave() / 1000 + "  Type: " + mob.toString()
									+ "  Coords: " + spawnPoint.getXCoord() + ", " + spawnPoint.getYCoord() + ", " + spawnPoint.getZCoord() 
									+ "  \u03B8" + spawnPoint.getAngle() + "  Specified: " + minAngle + "," + maxAngle);
				}
				return true;
			}
			
			mob.setLocationAndAngles(spawnPoint.getXCoord(), spawnPoint.getYCoord(), spawnPoint.getZCoord(), 0.0F, 0.0F);
			if(mob.getCanSpawnHere())
			{
				successfulSpawns++;				
				nexus.getWorld().spawnEntityInWorld(mob);
				if(debugMode)
				{
					mod_Invasion.log("[Spawn] Time: " + currentWave.getTimeInWave() / 1000 + "  Type: " + mob.toString()
									+ "  Coords: " + mob.posX + ", " + mob.posY + ", " + mob.posZ + "  \u03B8" + spawnPoint.getAngle()
									+ "  Specified: " + minAngle + "," + maxAngle);
				}
				return true;
			}
		}		
		mod_Invasion.log("Could not find valid spawn for '" + EntityList.getEntityString(mob) + "' after " + spawnTries + " tries");
		return false;
	}
	
	private void generateSpawnPoints()
	{
		if(nexus.getWorld() == null)
			return;
		
		EntityIMZombie zombie = new EntityIMZombie(nexus.getWorld(), nexus);
		List<SpawnPoint> spawnPoints = new ArrayList<SpawnPoint>();
		int x = nexus.getXCoord();
		int y = nexus.getYCoord();
		int z = nexus.getZCoord();
		for(int vertical = 0; vertical < 128; vertical = (vertical > 0) ? (vertical * -1) : (vertical * -1 + 1))
		{
			if(y + vertical > 252)
				continue;
			
			for(int i = 0; i <= spawnRadius * 0.7 + 1; i++)
			{
				// Generate coordinate offsets for a circle
				int j = (int)Math.round(spawnRadius * Math.cos(Math.asin((double)i / (double)spawnRadius)));
				
				//Quadrant 1
				addValidSpawn(zombie, spawnPoints, x + i, y + vertical, z + j);
				addValidSpawn(zombie, spawnPoints, x + j, y + vertical, z + i);
				
				//Quadrant 2
				addValidSpawn(zombie, spawnPoints, x + i, y + vertical, z - j);
				addValidSpawn(zombie, spawnPoints, x + j, y + vertical, z - i);
				
				//Quadrant 3
				addValidSpawn(zombie, spawnPoints, x - i, y + vertical, z + j);
				addValidSpawn(zombie, spawnPoints, x - j, y + vertical, z + i);
				
				//Quadrant 4
				addValidSpawn(zombie, spawnPoints, x - i, y + vertical, z - j);
				addValidSpawn(zombie, spawnPoints, x - j, y + vertical, z - i);
			}
		}
		
		// Remove some outermost spawn points if they are far away enough and there are
		// lots of points (opt not to add them to the container). Also, remove points above
		// a certain difference in height if have enough points. Otherwise, add the rest.
		// List will already be sorted in order of height difference magnitude, so we can
		// use that assumption.
		if(spawnPoints.size() > MIN_SPAWN_POINTS_TO_KEEP)
		{
			// Find index splitting the list into points to keep and points to remove
			int amountToRemove = (int)((spawnPoints.size() - MIN_SPAWN_POINTS_TO_KEEP) * SPAWN_POINT_CULL_RATE);
			int i = spawnPoints.size() - 1;
			for(; i >= spawnPoints.size() - amountToRemove; i--)
			{
				if(Math.abs(spawnPoints.get(i).getYCoord() - y) < NORMAL_SPAWN_HEIGHT)
					break;
			}
				
			// Continue, but cull points above the height cutoff in this range
			for(; i >= MIN_SPAWN_POINTS_TO_KEEP_BELOW_HEIGHT_CUTOFF; i--)
			{
				SpawnPoint spawnPoint = spawnPoints.get(i);
				if(spawnPoint.getYCoord() - y <= HEIGHT_CUTOFF)
				{
					spawnPointContainer.addSpawnPointXZ(spawnPoint);
				}
			}
			
			// Continue, adding the remaining points
			for(; i >= 0; i--)
			{
				spawnPointContainer.addSpawnPointXZ(spawnPoints.get(i));
			}
		}
		
		
		
		
		//if(debugMode)
		{
			mod_Invasion.log("Num. Spawn Points: " + Integer.toString(spawnPointContainer.getNumberOfSpawnPoints(SpawnType.HUMANOID)));
		}
		
		//spawnPointContainer.pointDisplayTest(81, worldObj);
	}
	
	private void addValidSpawn(EntityIMLiving entity, List<SpawnPoint> spawnPoints, int x, int y, int z)
	{
		entity.setLocationAndAngles((x), (y), (z), 0.0F, 0.0F);
		if(entity.getCanSpawnHere())
		{
			// Calculate polar coordinate around origin nexus in degrees
			int angle = (int)(Math.atan2(nexus.getZCoord() - z, nexus.getXCoord() - x) * 180 / Math.PI);
			spawnPoints.add(new SpawnPoint(x, y, z, angle, SpawnType.HUMANOID));
		}
	}
	
	private void checkAddSpawn(EntityIMLiving entity, int x, int y, int z)
	{
		entity.setLocationAndAngles((x), (y), (z), 0.0F, 0.0F);
		if(entity.getCanSpawnHere())
		{
			// Calculate polar coordinate around origin nexus in degrees
			int angle = (int)(Math.atan2(nexus.getZCoord() - z, nexus.getXCoord() - x) * 180 / Math.PI);
			spawnPointContainer.addSpawnPointXZ(new SpawnPoint(x, y, z, angle, SpawnType.HUMANOID));
		}
	}
}