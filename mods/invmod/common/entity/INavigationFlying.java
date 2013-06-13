package mods.invmod.common.entity;

import net.minecraft.entity.Entity;

public interface INavigationFlying extends INavigation
{
	enum MoveType
	{
		PREFER_WALKING, MIXED, PREFER_FLYING
	};
	
	void setMovementType(MoveType moveType);
	
	void setLandingPath();
	
	void setCirclingPath(Entity target, float preferredHeight, float preferredRadius);
	
	void setCirclingPath(double x, double y, double z, float preferredHeight, float preferredRadius);
	
	float getDistanceToCirclingRadius();
	
	boolean isCircling();
	
	void setFlySpeed(float speed);
	
	void setPitchBias(float pitch, float biasAmount);
	
	/**
	 * Sets the navigator to fly precisely towards the target, at expense
	 * of obstacle avoidance.
	 */
	public void enableDirectTarget(boolean enabled);
}
