package mods.invmod.client;


import java.io.File;

import mods.invmod.common.ResourceLoader;
import mods.invmod.common.SoundHandlerCommon;
import mods.invmod.common.mod_Invasion;
import net.minecraft.client.Minecraft;
import cpw.mods.fml.client.FMLClientHandler;

public class SoundHandlerClient extends SoundHandlerCommon
{
	/**
	 * Attempts to load extra sound resources if sound is enabled in config.
	 * Extracts resources from the classpath for the sound library to use.
	 */
	@Override
	public boolean installSounds(String resourcePath, String[] soundNames, ResourceLoader resourceLoader)
	{
		Minecraft minecraft = FMLClientHandler.instance().getClient();
		soundsInstalled = true;
		String p1 = "resources/";
		String p2 = "sound3/invsound/";
		
		File soundDir = new File(minecraft.mcDataDir, p1 + p2);
		if(!soundDir.exists())
		{
			mod_Invasion.log(p1 + p2 + " not found. Creating directories...");
			if(!soundDir.mkdirs())
				mod_Invasion.log("Failed to create directories");
		}
		
		for(String fname : soundNames)
		{
			File soundPath = new File(minecraft.mcDataDir, p1 + p2 + fname);
			if(!soundPath.exists())
    		{
				mod_Invasion.log("Sound '" + fname + "' not found in directory '" + p1 + p2);
				mod_Invasion.log("Copying file from jar...");
    			if(!resourceLoader.copyResource(minecraft.getClass().getClassLoader(), resourcePath + fname, soundPath))
    			{
    				mod_Invasion.log("Failed copy on " + fname + " to " + soundPath.getPath());
    				soundsInstalled = false;
    				continue;
    			}
    			else
    			{
    				mod_Invasion.log("Copied " + fname + " to " + soundPath.getPath());
    			}
    		}
    		
			// Last checks because installResource() gives no indication of success, and a bad resource can crash the game
			if(!soundPath.exists())
			{
				mod_Invasion.log("File " + fname + " still not recognised as existing");
				soundsInstalled = false;
				continue;
			}
			if(!soundPath.canRead())
			{
				mod_Invasion.log("File " + fname + " not readable");
				soundsInstalled = false;
				continue;
			}
			
			// Finally, take the plunge and call installResource()
			mod_Invasion.log("Found " + fname +". Registering...");
    		minecraft.installResource(p2 + fname, soundPath);
		}
		
		if(!soundsInstalled)
		{
			mod_Invasion.log("One or more sound resources failed to install.");
			return false;
		}
		else
		{
			mod_Invasion.log("Sound resources installed.");
			return true;
		}
	}
	
	@Override
	public void playSingleSFX(String s)
	{
		FMLClientHandler.instance().getClient().sndManager.playSoundFX(s, 1.0F, 1.0F);
	}
	
	@Override
	public void playSingleSFX(byte id)
	{
		if(soundsInstalled && sfxMapToString.containsKey(id))
		{
			FMLClientHandler.instance().getClient().sndManager.playSoundFX(sfxMapToString.get(id), 1.0F, 1.0F);
		}
	}
}
