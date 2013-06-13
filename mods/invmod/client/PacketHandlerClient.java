package mods.invmod.client;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import mods.invmod.common.PacketHandlerCommon;
import mods.invmod.common.mod_Invasion;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.Player;

public class PacketHandlerClient extends PacketHandlerCommon
{
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
	{
		DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(packet.data));
        try
        {
        	byte type = dataStream.readByte();
        	if(type == mod_Invasion.PACKET_SFX)
        	{
        		byte id = dataStream.readByte();
        		mod_Invasion.playSingleSFX(id);
        	}
        }
        catch(IOException e)
        {
        	e.printStackTrace();
        }
	}
}
