package mods.invmod.common;

import java.util.HashMap;
import java.util.Map;

public class SoundHandlerCommon
{
	protected Map<Byte, String> sfxMapToString;
	protected Map<String, Byte> sfxMapToByte;
	protected boolean soundsInstalled;

	public SoundHandlerCommon()
	{
		sfxMapToString = new HashMap<Byte, String>();
		sfxMapToByte = new HashMap<String, Byte>();
		soundsInstalled = false;
	}
	
	/**
	 * Server method for sound loading.
	 */
	public boolean installSounds(String resourcePath, String[] soundNames, ResourceLoader resourceLoader)
	{
		soundsInstalled = true;
		return true;
	}
	
	/**
	 * Adds a mapping for sound packets
	 */
	public void addNetworkSoundMapping(String soundName, byte id)
	{
		sfxMapToByte.put(soundName, id);
		sfxMapToString.put(id, soundName);
	}
	
	
	/**
	 * Plays a sound to everyone. Only available on the server.
	 */
	public void playGlobalSFX(String s)
	{
		if(sfxMapToByte.containsKey(s))
		{
			mod_Invasion.sendInvasionPacketToAll(new byte[] { mod_Invasion.PACKET_SFX, sfxMapToByte.get(s) } );
		}
	}
	
	/**
	 * Plays a sound client-side only.
	 */
	public void playSingleSFX(String s)
	{
	}
	
	/**
	 * Plays a sound client-side only.
	 */
	public void playSingleSFX(byte id)
	{
	}
	
	public boolean soundsInstalled()
	{
		return soundsInstalled;
	}
}
