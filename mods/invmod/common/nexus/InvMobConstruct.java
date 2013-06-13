package mods.invmod.common.nexus;

//import net.minecraft.src.Packet230ModLoader;

public class InvMobConstruct
{
	public InvMobConstruct(int texture, int tier, int flavour, float scaling)
	{
		this.texture = texture;
		this.tier = tier;
		this.flavour = flavour;
		this.scaling = scaling;
	}
	
	/*public InvMobConstruct(Packet230ModLoader p) throws Exception
	{
		if(p.packetType != mod_Invasion.PACKET_INV_MOB_SPAWN || p.dataInt.length != 3 || p.dataFloat.length != 1)
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

	private int texture;
	private int tier;
	private int flavour;
	private float scaling;
}
