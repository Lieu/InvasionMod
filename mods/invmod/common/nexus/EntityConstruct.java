package mods.invmod.common.nexus;


/**
 * Defines a mob's construction to a MobBuilder or other
 * builder object.
 */
public class EntityConstruct
{
	public EntityConstruct(IMEntityType mobType, int tier, int texture, int flavour, float scaling, int minAngle, int maxAngle)
	{
		this.entityType = mobType;
		this.texture = texture;
		this.tier = tier;
		this.flavour = flavour;
		this.scaling = scaling;
		this.minAngle = minAngle;
		this.maxAngle = maxAngle;
	}
	
	/*public EntityConstruct(Packet230ModLoader p) throws Exception
	{
		if(p.dataInt.length != 3 || p.dataFloat.length != 1)
			throw new Exception("invalid inv mob spawn packet");
		
		texture = p.dataInt[0];
		tier = p.dataInt[1];
		flavour = p.dataInt[2];
		scaling = p.dataFloat[0];
	}
	
	public Packet230ModLoader convertToPacket(int modId, int packetType)
	{
		Packet230ModLoader p = new Packet230ModLoader();
		p.modId = modId;
		p.packetType = packetType;
		p.dataInt = new int[] { texture, tier, flavour };
		p.dataFloat = new float[] { scaling };
		return p;
	}*/
	
	public IMEntityType getMobType()
	{
		return entityType;
	}
	
	public int getTexture()
	{
		return texture;
	}
	
	public int getTier()
	{
		return tier;
	}
	
	public int getFlavour()
	{
		return flavour;
	}
	
	public float getScaling()
	{
		return scaling;
	}
	
	public int getMinAngle()
	{
		return minAngle;
	}
	
	public int getMaxAngle()
	{
		return maxAngle;
	}

	private IMEntityType entityType;
	private int texture;
	private int tier;
	private int flavour;
	private int minAngle;
	private int maxAngle;
	private float scaling;	
}
