package mods.invmod.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Config
{
	protected Properties properties;
	
	public void loadConfig(File configFile)
	{
		mod_Invasion.log("Loading config");
		properties = new Properties();
		//File configFile = new File(Minecraft.getMinecraftDir().getPath() + "/invasion_config.txt");		
		try
		{
			if(!configFile.exists())
			{
				mod_Invasion.log("Config not found. Creating file 'invasion_config.txt' in minecraft directory");
				if(!configFile.createNewFile())
					mod_Invasion.log("Unable to create new config file.");
			}
			else
			{
				FileReader configRead = new FileReader(configFile);
				try
				{
					properties.load(configRead);
				}
				finally
				{
					configRead.close();
				}
			}
		}
		catch(FileNotFoundException e)
		{
			// Might mean the file was inaccessible
			mod_Invasion.log(e.getMessage());
			mod_Invasion.log("Proceeding with default config");
		}
		catch(IOException e)
		{
			mod_Invasion.log(e.getMessage());
			mod_Invasion.log("Proceeding with default config");
		}
	}
	
	public void writeProperty(BufferedWriter writer, String key) throws IOException
	{
		writeProperty(writer, key, null);
	}
	
	public void writeProperty(BufferedWriter writer, String key, String comment) throws IOException
	{		
		if(comment != null)
		{
			writer.write("# " + comment);
			writer.newLine();
		}
		
		writer.write(key + "=" + properties.getProperty(key));
		writer.newLine();
	}
	
	public void setProperty(String key, String value)
	{
		properties.setProperty(key, value);
	}
	
	public String getProperty(String key, String defaultValue)
	{
		return properties.getProperty(key, defaultValue);
	}
	
	public int getPropertyValueInt(String keyName, int defaultValue)
	{
		String property = properties.getProperty(keyName, "null");
		if(!property.equals("null"))
		{
			return Integer.parseInt(property);
		}
		else
		{
			properties.setProperty(keyName, Integer.toString(defaultValue));
			return defaultValue;
		}
	}
	
	public float getPropertyValueFloat(String keyName, float defaultValue)
	{
		String property = properties.getProperty(keyName, "null");
		if(!property.equals("null"))
		{
			return Float.parseFloat(property);
		}
		else
		{
			properties.setProperty(keyName, Float.toString(defaultValue));
			return defaultValue;
		}
	}
	
	public boolean getPropertyValueBoolean(String keyName, boolean defaultValue)
	{
		String property = properties.getProperty(keyName, "null");
		if(!property.equals("null"))
		{
			return Boolean.parseBoolean(property);
		}
		else
		{
			properties.setProperty(keyName, Boolean.toString(defaultValue));
			return defaultValue;
		}
	}
	
	public String getPropertyValueString(String keyName, String defaultValue)
	{
		String property = properties.getProperty(keyName, "null");
		if(!property.equals("null"))
		{
			return property;
		}
		else
		{
			properties.setProperty(keyName, defaultValue);
			return defaultValue;
		}
	}
}
