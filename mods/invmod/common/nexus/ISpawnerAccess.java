package mods.invmod.common.nexus;


/**
 * Represents basic ability to spawn entities from IMEntityConstruct objects
 * 
 * @author Lieu
 */
public interface ISpawnerAccess
{
	/**
	 * Attempts to spawn an entity somewhere within a range specified,
	 * returning true if successful.
	 */
	public boolean attemptSpawn(EntityConstruct mobConstrct, int minAngle, int maxAngle);
	
	/**
	 * Returns number of valid points of a specified type at which entities can spawn
	 */
	public int getNumberOfPointsInRange(int minAngle, int maxAngle, SpawnType spawnType);
	
	/**
	 * Passes a message to the spawner, which it may display
	 */
	public void sendSpawnAlert(String message);
	
	/**
	 * Alerts the spawner to a fatal lack of spawn points
	 */
	public void noSpawnPointNotice();
}
