package mods.invmod.common.entity;

import mods.invmod.common.INotifyTask;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

public interface INavigation extends INotifyTask
{
	PathAction getCurrentWorkingAction();

	void setSpeed(float par1);

	Path getPathToXYZ(double x, double y, double z);

	boolean tryMoveToXYZ(double x, double y, double z, float speed);

	/**
	 * Try and find a path to walk a random distance between min and max in the direction of x, z
	 */
	Path getPathTowardsXZ(double x, double z, int min, int max, int verticalRange);

	boolean tryMoveTowardsXZ(double x, double z, int min, int max, int verticalRange, float speed);

	Path getPathToEntity(Entity targetEntity);

	boolean tryMoveToEntity(Entity targetEntity, float speed);
	
	/**
	 * Moves to the specified entity taking into account arbitrary factors. May
	 * choose its own paths whenever needed as determined by the implementation.
	 * 
	 * @param target The target entity
	 */
	void autoPathToEntity(Entity target);

	/**
	 * Sets the active path if valid
	 */
	boolean setPath(Path par1PathEntity, float speed);
	
	boolean isWaitingForTask();

	/**
	 * gets the actively used PathEntity
	 */
	Path getPath();

	void onUpdateNavigation();

	int getLastActionResult();

	/**
	 * If null path or reached the end
	 */
	boolean noPath();

	/**
	 * Returns the number of ticks entity has been considered by
	 * the navigator to have made no progress
	 */
	int getStuckTime();

	/**
	 * Returns the distance between the end of the most recent path and that path's original target location
	 */
	float getLastPathDistanceToTarget();

	/**
	 * sets active PathEntity to null
	 */
	void clearPath();

	void haltForTick();

	Entity getTargetEntity();
	
	public String getStatus();
}