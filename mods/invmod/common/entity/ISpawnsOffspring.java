package mods.invmod.common.entity;

import net.minecraft.entity.Entity;


/**
 * Represents the ability to spawn new entities.
 * 
 * @author Lieu
 */
public interface ISpawnsOffspring
{
	/**
	 * Returns offspring this entity creates, with partner as a mate if applicable
	 */
	public abstract Entity[] getOffspring(Entity partner);
}
