package mods.invmod.common.nexus;


/**
 * Generates EntityIMConstruct objects
 * 
 * @author Lieu
 */
public interface IEntityIMPattern
{
	/**
	 * Returns an EntityIMConstruct as determined by the pattern.
	 * Angles are defaulted to -180 and 180
	 */
	public EntityConstruct generateEntityConstruct();
	
	/**
	 * Returns an EntityIMConstruct as determined by the pattern, with
	 * the angles specified
	 */
	public EntityConstruct generateEntityConstruct(int minAngle, int maxAngle);
}
