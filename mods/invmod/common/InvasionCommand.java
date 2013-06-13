package mods.invmod.common;

import mods.invmod.common.nexus.IMWaveBuilder;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class InvasionCommand extends CommandBase
{
	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
		String username = sender.getCommandSenderName();
        if(args.length > 0 && args.length <= 7)
        {
        	if(args[0].equals("begin"))
        	{
        		if(args.length == 2)
            	{
        			int startWave = Integer.parseInt(args[1]);
                	if(mod_Invasion.getFocusNexus() != null)
                	{
                		mod_Invasion.getFocusNexus().debugStartInvaion(startWave);
                	}
            	}
        	}
        	else if(args[0].equals("end"))
        	{
        		//configManager.sendChatMessageToAllOps(username + " attempting to end invasion");
            	if(mod_Invasion.getActiveNexus() != null)
            	{
            		mod_Invasion.getActiveNexus().emergencyStop();
            		mod_Invasion.broadcastToAll(username + " ended invasion");
            	}
            	else
            	{
            		sender.sendChatToPlayer(username + ": No invasion to end");
            	}
        	}
        	else if(args[0].equals("range"))
        	{
        		if(args.length == 2)
                {
                	int radius = Integer.parseInt(args[1]);
                	if(mod_Invasion.getFocusNexus() != null)
                	{
                    	if(radius >= 32 && radius <= 128)
                    	{
                    		if(mod_Invasion.getFocusNexus().setSpawnRadius(radius))
                    		{
                    			sender.sendChatToPlayer("Set nexus range to " + radius);
                    			//configManager.sendChatMessageToAllOps(username + " set nexus range to " + radius);
                    			//if(FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().)
                    			//{
                    			//	sender.sendChatToPlayer(username + " set nexus range to " + radius);
                    			//}
                    		}
                    		else
                    		{
                    			sender.sendChatToPlayer(username + ": Can't change range while nexus is active");
                    		}
                    	}
                    	else
                    	{
                    		sender.sendChatToPlayer(username + ": Range must be between 32 and 128");
                    	}
                	}
                	else
                	{
                		sender.sendChatToPlayer(username + ": Right-click the nexus first to set target for command");
                	}
                }
        	}
        	else if(args[0].equals("spawnertest"))
        	{
        		int startWave = 1;
        		int endWave = IMWaveBuilder.WAVES_DEFINED;
        		
        		if(args.length >= 4)
        			return;
        		if(args.length >= 3)
        			endWave = Integer.parseInt(args[2]);
        		if(args.length >= 2)
        			startWave = Integer.parseInt(args[1]);
        		
        		Tester tester = new Tester();
        		tester.doWaveSpawnerTest(startWave, endWave);
        	}
        	else if(args[0].equals("pointcontainertest"))
        	{
        		Tester tester = new Tester();
        		tester.doSpawnPointSelectionTest();
        	}
        	else if(args[0].equals("wavebuildertest"))
        	{
        		float difficulty = 1.0F;
        		float tierLevel = 1.0F;
        		int lengthSeconds = 160;
        		
        		if(args.length >= 5)
        			return;
        		if(args.length >= 4)
        			lengthSeconds = Integer.parseInt(args[3]);
        		if(args.length >= 3)
        			tierLevel = Float.parseFloat(args[2]);
        		if(args.length >= 2)
        			difficulty = Float.parseFloat(args[1]);
        		
        		Tester tester = new Tester();
        		tester.doWaveBuilderTest(difficulty, tierLevel, lengthSeconds);
        	}
        	else if(args[0].equals("nexusstatus"))
        	{
        		if(mod_Invasion.getFocusNexus() != null)
        			mod_Invasion.getFocusNexus().debugStatus();
        	}
        	else if(args[0].equals("bolt"))
        	{
        		if(mod_Invasion.getFocusNexus() != null)
        		{
        			int x = mod_Invasion.getFocusNexus().getXCoord();
        			int y = mod_Invasion.getFocusNexus().getYCoord();
        			int z = mod_Invasion.getFocusNexus().getZCoord();
        			int time = 40;
        			if(args.length >= 6)
            			return;
        			if(args.length >= 5)
        				time = Integer.parseInt(args[4]);
            		if(args.length >= 4)
            			z += Integer.parseInt(args[3]);
            		if(args.length >= 3)
            			y += Integer.parseInt(args[2]);
            		if(args.length >= 2)
            			x += Integer.parseInt(args[1]);
            		
            		mod_Invasion.getFocusNexus().createBolt(x, y, z, time);
        		}
        	}
        	else
        	{
        		// Coomand was invalid
        		return;
        	}
        }
	}

	@Override
	public String getCommandName()
	{
		return "invasion";
	}
}
