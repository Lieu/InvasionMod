package mods.invmod.common.nexus;

import mods.invmod.common.util.RandomSelectionPool;

/**
 * Builds EntityConstruct objects according to a pattern laid out.
 * EntityConstruct objects define the concrete construction of a mob by
 * a MobBuilder.
 */
public class EntityPattern implements IEntityIMPattern
{
	public EntityPattern(IMEntityType entityType)
	{
		this.entityType = entityType;
		tierPool = new RandomSelectionPool<Integer>();
		texturePool = new RandomSelectionPool<Integer>();
		flavourPool = new RandomSelectionPool<Integer>();
	}
	
	/**
	 * Returns a new entity construct with spawn angles of -180 to 180
	 */
	@Override
	public EntityConstruct generateEntityConstruct()
	{
		return generateEntityConstruct(-180, 180);
	}
	
	/**
	 * Returns a new entity construct according to the pattern. Spawn
	 * angles are recommended to be bounded between -180 and 180.
	 */
	@Override
	public EntityConstruct generateEntityConstruct(int minAngle, int maxAngle)
	{
		Integer tier = tierPool.selectNext();
		if(tier == null)
			tier = DEFAULT_TIER;
		
		Integer texture = texturePool.selectNext();
		if(texture == null)
			texture = OPEN_TEXTURE;
		
		Integer flavour = flavourPool.selectNext();
		if(flavour == null)
			flavour = DEFAULT_FLAVOUR;

		return new EntityConstruct(entityType, tier, texture, flavour, OPEN_SCALING, minAngle, maxAngle);
	}
	
	public void addTier(int tier, float weight)
	{
		tierPool.addEntry(tier, weight);
	}
	
	public void addTexture(int texture, float weight)
	{
		texturePool.addEntry(texture, weight);
	}
	
	public void addFlavour(int flavour, float weight)
	{
		flavourPool.addEntry(flavour, weight);
	}
	
	@Override
	public String toString()
	{
		return "EntityIMPattern@" + Integer.toHexString(hashCode()) + "#" + entityType;
	}
	
	private IMEntityType entityType; // Concrete entity type
	private RandomSelectionPool<Integer> tierPool;
	private RandomSelectionPool<Integer> texturePool;
	private RandomSelectionPool<Integer> flavourPool;
	
	private static final int DEFAULT_TIER = 1;
	private static final int DEFAULT_FLAVOUR = 0;
	private static final int OPEN_TEXTURE = 0;
	private static final int OPEN_SCALING = 0;
}
