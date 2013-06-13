package mods.invmod.common;

import java.io.File;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet3Chat;

import cpw.mods.fml.common.FMLCommonHandler;

public class ProxyCommon
{
	public void preloadTexture(String texture)
	{
	}
	
	public int addTextureOverride(String fileToOverride, String fileToAdd)
	{
		return 0;
	}
	
	public void registerEntityRenderingHandler(Class<? extends Entity> entityClass, Render renderer)
	{
	}
	
	public void broadcastToAll(String message)
	{
		FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendPacketToAllPlayers(new Packet3Chat(message));
	}
	
	public void printGuiMessage(String message)
	{
	}

	public void registerEntityRenderers()
	{
	}
	
	public void loadAnimations()
	{
	}
	
	public File getFile(String fileName)
	{
		return FMLCommonHandler.instance().getMinecraftServerInstance().getFile(fileName);
	}
}
