
package mods.invmod.common;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map.Entry;

import mods.invmod.client.BowHackHandler;
import mods.invmod.client.TickHandlerClient;
import mods.invmod.common.entity.EntityIMArrowOld;
import mods.invmod.common.entity.EntityIMBird;
import mods.invmod.common.entity.EntityIMBolt;
import mods.invmod.common.entity.EntityIMBoulder;
import mods.invmod.common.entity.EntityIMCreeper;
import mods.invmod.common.entity.EntityIMEgg;
import mods.invmod.common.entity.EntityIMGiantBird;
import mods.invmod.common.entity.EntityIMLiving;
import mods.invmod.common.entity.EntityIMPigEngy;
import mods.invmod.common.entity.EntityIMSkeleton;
import mods.invmod.common.entity.EntityIMSpawnProxy;
import mods.invmod.common.entity.EntityIMSpider;
import mods.invmod.common.entity.EntityIMThrower;
import mods.invmod.common.entity.EntityIMTrap;
import mods.invmod.common.entity.EntityIMWolf;
import mods.invmod.common.entity.EntityIMZombie;
import mods.invmod.common.item.ItemDebugWand;
import mods.invmod.common.item.ItemIM;
import mods.invmod.common.item.ItemIMBow;
import mods.invmod.common.item.ItemIMTrap;
import mods.invmod.common.item.ItemInfusedSword;
import mods.invmod.common.item.ItemProbe;
import mods.invmod.common.item.ItemRemnants;
import mods.invmod.common.item.ItemRiftFlux;
import mods.invmod.common.item.ItemStrangeBone;
import mods.invmod.common.nexus.BlockNexus;
import mods.invmod.common.nexus.IEntityIMPattern;
import mods.invmod.common.nexus.IMWaveBuilder;
import mods.invmod.common.nexus.MobBuilder;
import mods.invmod.common.nexus.TileEntityNexus;
import mods.invmod.common.util.ISelect;
import mods.invmod.common.util.RandomSelectionPool;
import net.minecraft.block.Block;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.network.packet.Packet3Chat;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarting;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod( modid = "mod_Invasion", name="Invasion", version="0.11.7")
@NetworkMod(
	     clientSideRequired = true,
	     serverSideRequired = false
	     //versionBounds = "[1.3]" // VERSION CHECKS!
	     )
public class mod_Invasion
{	
	@SidedProxy(clientSide = "mods.invmod.client.PacketHandlerClient", serverSide = "mods.invmod.common.PacketHandlerCommon")
	public static PacketHandlerCommon packetHandler;
	
	//@SidedProxy(clientSide = "invmod.client.ConnectionHandlerClient", serverSide = "invmod.common.ConnectionHandlerCommon")
	//public static ConnectionBridgeCommon connectionHandler;
	
	@SidedProxy(clientSide = "mods.invmod.client.SoundHandlerClient", serverSide = "mods.invmod.common.SoundHandlerCommon")
	public static SoundHandlerCommon soundHandler = new SoundHandlerCommon();
	
	@SidedProxy(clientSide = "mods.invmod.client.ProxyClient", serverSide = "mods.invmod.common.ProxyCommon")
	public static ProxyCommon proxy;
	public static ResourceLoader resourceLoader;
	public static GuiHandler guiHandler;
	public static ConfigInvasion configInvasion;
	private static File configFile;
	
	// Main components of this class
	private static boolean runFlag;
	private static long timer;
	private static long clientElapsed;
	private static long serverElapsed;
	private static boolean serverRunFlag;
	private static int killTimer;
	private static boolean loginFlag;
	private static HashMap<String, Long> deathList = new HashMap<String, Long>();
	private static MobBuilder defaultMobBuilder = new MobBuilder();
	private static BufferedWriter logOut;
	private static ISelect<IEntityIMPattern> nightSpawnPool1;
	private static TileEntityNexus focusNexus;
	private static TileEntityNexus activeNexus;
	private static boolean isInvasionActive = false;
	private static boolean soundInstalled = false;
	
	
	//---//
	
	public static final byte PACKET_SFX = 0;
	public static final byte PACKET_INV_MOB_SPAWN = 2;
	
	// Default config values
	private static final int DEFAULT_NEXUS_BLOCK_ID = 216;
	private static final int DEFAULT_GUI_ID_NEXUS = 76;
	private static final int DEFAULT_ITEM_ID_DEBUGWAND = 24399;
	private static final int DEFAULT_ITEM_ID_PHASECRYSTAL = 24400;
	private static final int DEFAULT_ITEM_ID_RIFTFLUX = 24401;
	private static final int DEFAULT_ITEM_ID_REMNANTS = 24402;
	private static final int DEFAULT_ITEM_ID_NEXUSCATALYST = 24403;
	private static final int DEFAULT_ITEM_ID_INFUSEDSWORD = 24404;
	private static final int DEFAULT_ITEM_ID_IMTRAP = 24405;
	private static final int DEFAULT_ITEM_ID_IMBOW = 24406;
	private static final int DEFAULT_ITEM_ID_CATAMIXTURE = 24407;
	private static final int DEFAULT_ITEM_ID_STABLECATAMIXTURE = 24408;
	private static final int DEFAULT_ITEM_ID_STABLENEXUSCATA = 24409;
	private static final int DEFAULT_ITEM_ID_DAMPINGAGENT = 24410;
	private static final int DEFAULT_ITEM_ID_STRONGDAMPINGAGENT = 24411;
	private static final int DEFAULT_ITEM_ID_STRANGEBONE = 24412;
	private static final int DEFAULT_ITEM_ID_PROBE = 24413;
	private static final int DEFAULT_ITEM_ID_STRONGCATALYST = 24414;
	private static final int DEFAULT_ITEM_ID_HAMMER = 24415;
	private static final boolean DEFAULT_SOUNDS_ENABLED = true;
	private static final boolean DEFAULT_CRAFT_ITEMS_ENABLED = true;
	private static final boolean DEFAULT_NIGHT_SPAWNS_ENABLED = false;
	private static final int DEFAULT_MIN_CONT_MODE_DAYS = 2;
	private static final int DEFAULT_MAX_CONT_MODE_DAYS = 3;
	private static final int DEFAULT_NIGHT_MOB_SIGHT_RANGE = 20;
	private static final int DEFAULT_NIGHT_MOB_SENSE_RANGE = 8;
	private static final int DEFAULT_NIGHT_MOB_SPAWN_CHANCE = 30;
	private static final int DEFAULT_NIGHT_MOB_MAX_GROUP_SIZE = 3;
	private static final int DEFAULT_NIGHT_MOB_LIMIT_OVERRIDE = 70;
	private static final float DEFAULT_NIGHT_MOB_STATS_SCALING = 1.0F;
	private static final boolean DEFAULT_NIGHT_MOBS_BURN = true;
	public static final String[] DEFAULT_NIGHT_MOB_PATTERN_1_SLOTS = { "zombie_t1_any", "zombie_t2_any_basic", "spider_t2_any", "none", "none", "none" };
	public static final float[] DEFAULT_NIGHT_MOB_PATTERN_1_SLOT_WEIGHTS = { 1.0F, 1.0F, 0.5F, 0, 0, 0 };
	
	//---//
	
	// Values for bow animation workaround
	public static final BowHackHandler bowHandler = new BowHackHandler();
	
	// General config
	private static boolean soundsEnabled;
	private static boolean craftItemsEnabled;
	private static boolean debugMode;
	
	// GUI IDs
	private static int guiIdNexus;
	
	// Nexus
	private static int minContinuousModeDays;
	private static int maxContinuousModeDays;
	
	// Night time spawn info
	private static boolean nightSpawnsEnabled;
	private static int nexusBlockId;
	private static int nightMobSightRange;
	private static int nightMobSenseRange;
	private static int nightMobSpawnChance;
	private static int nightMobMaxGroupSize;
	private static int maxNightMobs;
	private static float nightMobStatsScaling;
	private static boolean nightMobsBurnInDay;
	
	// Blocks and items
	public static BlockNexus blockNexus;
	public static Item itemPhaseCrystal;
	public static Item itemRiftFlux;
	public static Item itemRemnants;
	public static Item itemNexusCatalyst;
	public static Item itemInfusedSword;
	public static Item itemIMTrap;
	public static Item itemPenBow;
	public static Item itemCataMixture;
	public static Item itemStableCataMixture;
	public static Item itemStableNexusCatalyst;
	public static Item itemDampingAgent;
	public static Item itemStrongDampingAgent;
	public static Item itemStrangeBone;
	public static Item itemProbe;
	public static Item itemStrongCatalyst;
	public static Item itemEngyHammer;
	public static Item itemDebugWand;
	
	//@Instance
	public static mod_Invasion instance;
	
	public mod_Invasion()
	{
		instance = this;
		runFlag = true;
		serverRunFlag = true;
		loginFlag = false;
		timer = 0;
		clientElapsed = 0;
		guiHandler = new GuiHandler();
	}
	
	/**
	 * Loads config, sets up internals, and otherwise puts this object into a workable state,
	 * but does not load any content into the game.
	 */
	@PreInit
	public void preInit(FMLPreInitializationEvent event) 
	{
		// Set up log file
		File logFile = proxy.getFile("/invasion_log.log");	
		try
		{
			if(!logFile.exists())
				logFile.createNewFile();
			logOut = new BufferedWriter(new FileWriter(logFile));
		}
		catch(Exception e)
		{
			logOut = null;
			log("Couldn't write to logfile");
			log(e.getMessage());
		}
		
		// Set config location
		configFile = proxy.getFile("/invasion_config.txt");
		
		// Load config from file
		configInvasion = new ConfigInvasion();
		configInvasion.loadConfig(configFile);
		
		// Read properties and implicitly set property to default value if empty
		// General config
		soundsEnabled = configInvasion.getPropertyValueBoolean("sounds-enabled", DEFAULT_SOUNDS_ENABLED);
		craftItemsEnabled = configInvasion.getPropertyValueBoolean("craft-items-enabled", DEFAULT_CRAFT_ITEMS_ENABLED);
		debugMode = configInvasion.getPropertyValueBoolean("debug", false);
		
		// Gui ID
		guiIdNexus = configInvasion.getPropertyValueInt("guiID-Nexus", DEFAULT_GUI_ID_NEXUS);
		
		// Nexus
		minContinuousModeDays = configInvasion.getPropertyValueInt("min-days-to-attack", DEFAULT_MIN_CONT_MODE_DAYS);
		maxContinuousModeDays = configInvasion.getPropertyValueInt("max-days-to-attack", DEFAULT_MAX_CONT_MODE_DAYS);
		
		// Night spawns
		nightSpawnConfig();
		
		// Handle block strength overrides
		HashMap<Integer, Float> strengthOverrides = new HashMap<Integer, Float>();
		for(int i = 1; i < 4096; i++)
		{
			String property = configInvasion.getProperty("block" + i + "-strength", "null");
			if(property != "null")
			{
				float strength = Float.parseFloat(property);
				if(strength > 0)
				{
					strengthOverrides.put(i, strength);
					EntityIMLiving.putBlockStrength(i, strength);
					float pathCost = 1 + strength * 0.4F;  // 1.0 is air cost. Keeps 1.0 for strength 0, 2.0 for strength 2.5, 3.2 for 5.5, etc
					EntityIMLiving.putBlockCost(i, pathCost);
				}
			}
		}

		// Write the moderated properties back to file
		configInvasion.saveConfig(configFile, strengthOverrides, debugMode);
	}
	
	/**
	 * Main load method for this mod.
	 */
	@Init
	public void load(FMLInitializationEvent event)
	{
		//MinecraftForge.EVENT_BUS.register(new CommandHandler());
		
		
		NetworkRegistry.instance().registerGuiHandler(instance, guiHandler);
    	NetworkRegistry.instance().registerChannel(packetHandler, "data");
		TickRegistry.registerTickHandler(new TickHandlerClient(), Side.CLIENT);
		TickRegistry.registerTickHandler(new TickHandlerServer(), Side.SERVER);
		
		loadBlocks();
    	loadItems();
    	loadEntities();
    	loadNames();
    	
    	if(craftItemsEnabled)
    		addRecipes();
    	
    	if(nightSpawnsEnabled)
    	{
			BiomeGenBase[] biomes = new BiomeGenBase[] { BiomeGenBase.plains, BiomeGenBase.extremeHills, BiomeGenBase.forest,
					BiomeGenBase.taiga, BiomeGenBase.swampland, BiomeGenBase.forestHills, BiomeGenBase.taigaHills,
					BiomeGenBase.extremeHillsEdge, BiomeGenBase.jungle, BiomeGenBase.jungleHills };
    		EntityRegistry.addSpawn(EntityIMSpawnProxy.class, nightMobSpawnChance, 1, 1, EnumCreatureType.monster, biomes);
    		EntityRegistry.addSpawn(EntityZombie.class, 1, 1, 1, EnumCreatureType.monster, biomes);
    		EntityRegistry.addSpawn(EntitySpider.class, 1, 1, 1, EnumCreatureType.monster, biomes);
    		EntityRegistry.addSpawn(EntitySkeleton.class, 1, 1, 1, EnumCreatureType.monster, biomes);
    	}
    	
    	// Override maximum number of random mobs that may exist in the world
    	if(maxNightMobs != DEFAULT_NIGHT_MOB_LIMIT_OVERRIDE)
    	{
			try
			{
				System.out.println(EnumCreatureType.monster.getMaxNumberOfCreature());
				Class c = EnumCreatureType.class;
				Object[] consts = c.getEnumConstants();
				Class sub = consts[0].getClass();
				Field field = sub.getDeclaredField("maxNumberOfCreature");
				field.setAccessible(true);
				field.set(EnumCreatureType.monster, maxNightMobs);
				System.out.println(EnumCreatureType.monster.getMaxNumberOfCreature());
			}
			catch(Exception e)
			{
				log(e.getMessage());
			}
    	}
    	
    	String resourcePath = "mods/invmod/sounds/";
		String[] soundNames = new String[] {"scrape1.ogg", "scrape2.ogg", "scrape3.ogg", "chime1.ogg", "rumble1.ogg", "zap1.ogg",
				"zap2.ogg", "zap3.ogg", "fireball1.ogg", "bigzombie1.ogg", "egghatch1.ogg", "egghatch2.ogg", "v_squawk1.ogg",
				"v_squawk2.ogg", "v_squawk3.ogg", "v_squawk4.ogg", "v_hiss1.ogg", "v_screech1.ogg", "v_screech2.ogg",
				"v_screech3.ogg", "v_longscreech1.ogg", "v_death1.ogg"};
		
		resourceLoader = new ResourceLoader();
		soundHandler.installSounds(resourcePath, soundNames, resourceLoader);
		
		// Set sound mappings for packets
		soundHandler.addNetworkSoundMapping("random.explode", (byte)0);
		soundHandler.addNetworkSoundMapping("invsound.scrape", (byte)1);
		soundHandler.addNetworkSoundMapping("invsound.chime", (byte)2);
		soundHandler.addNetworkSoundMapping("invsound.rumble", (byte)3);
		soundHandler.addNetworkSoundMapping("invsound.zap", (byte)4);
		soundHandler.addNetworkSoundMapping("invsound.fireball", (byte)5);
		soundHandler.addNetworkSoundMapping("invsound.bigzombie", (byte)6);
		soundHandler.addNetworkSoundMapping("invsound.egghatch", (byte)7);
		soundHandler.addNetworkSoundMapping("invsound.v_squawk", (byte)8);
		soundHandler.addNetworkSoundMapping("invsound.v_hiss", (byte)9);
		soundHandler.addNetworkSoundMapping("invsound.v_screech", (byte)10);
		soundHandler.addNetworkSoundMapping("invsound.v_longscreech", (byte)11);
		soundHandler.addNetworkSoundMapping("invsound.v_death", (byte)12);
	}
	
	@ServerStarting
	public void onServerStart(FMLServerStartingEvent event)
	{
		ICommandManager commandManager = FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
		if(commandManager instanceof CommandHandler)
		{
			((CommandHandler)commandManager).registerCommand(new InvasionCommand());
		}
	}
	
	protected void loadBlocks()
	{
		blockNexus= new BlockNexus(configInvasion.getPropertyValueInt("blockID-Nexus", DEFAULT_NEXUS_BLOCK_ID));
		blockNexus.setResistance(6000000).setHardness(3.0F).setStepSound(Block.soundGlassFootstep).setUnlocalizedName("blockNexus");
		blockNexus.setCreativeTab(CreativeTabs.tabMisc);
		GameRegistry.registerBlock(blockNexus, "Nexus");
		GameRegistry.registerTileEntity(mods.invmod.common.nexus.TileEntityNexus.class, "Nexus");
	}
	
	protected void loadItems()
	{
		itemPhaseCrystal = new ItemIM(configInvasion.getPropertyValueInt("itemID-PhaseCrystal", DEFAULT_ITEM_ID_PHASECRYSTAL)).setUnlocalizedName("phaseCrystal").setMaxStackSize(1).setCreativeTab(CreativeTabs.tabMisc);
		itemRiftFlux = new ItemRiftFlux(configInvasion.getPropertyValueInt("itemID-RiftFlux", DEFAULT_ITEM_ID_RIFTFLUX)).setUnlocalizedName("riftFlux").setCreativeTab(CreativeTabs.tabMisc);;
		itemRemnants = new ItemRemnants(configInvasion.getPropertyValueInt("itemID-Remnants", DEFAULT_ITEM_ID_REMNANTS)).setUnlocalizedName("remnants").setCreativeTab(CreativeTabs.tabMisc);;
		itemNexusCatalyst = new ItemIM(configInvasion.getPropertyValueInt("itemID-NexusCatalyst", DEFAULT_ITEM_ID_NEXUSCATALYST)).setUnlocalizedName("nexusCatalyst").setMaxStackSize(1).setCreativeTab(CreativeTabs.tabMisc);;
		itemInfusedSword = new ItemInfusedSword(configInvasion.getPropertyValueInt("itemID-InfusedSword", DEFAULT_ITEM_ID_INFUSEDSWORD)).setUnlocalizedName("infusedSword").setMaxStackSize(1).setCreativeTab(CreativeTabs.tabMisc);;
		itemPenBow = new ItemIMBow(configInvasion.getPropertyValueInt("itemID-IMBow", DEFAULT_ITEM_ID_IMBOW)).setUnlocalizedName("searingBow").setCreativeTab(CreativeTabs.tabMisc);;
		itemCataMixture = new ItemIM(configInvasion.getPropertyValueInt("itemID-CataMixture", DEFAULT_ITEM_ID_CATAMIXTURE)).setUnlocalizedName("catalystMixture").setMaxStackSize(1).setCreativeTab(CreativeTabs.tabMisc);;
		itemStableCataMixture = new ItemIM(configInvasion.getPropertyValueInt("itemID-StableCataMixture", DEFAULT_ITEM_ID_STABLECATAMIXTURE)).setUnlocalizedName("stableCatalystMixture").setMaxStackSize(1).setCreativeTab(CreativeTabs.tabMisc);;
		itemStableNexusCatalyst = new ItemIM(configInvasion.getPropertyValueInt("itemID-StableNexusCatalyst", DEFAULT_ITEM_ID_STABLENEXUSCATA)).setUnlocalizedName("stableNexusCatalyst").setMaxStackSize(1).setCreativeTab(CreativeTabs.tabMisc);;
		itemDampingAgent = new ItemIM(configInvasion.getPropertyValueInt("itemID-DampingAgent", DEFAULT_ITEM_ID_DAMPINGAGENT)).setUnlocalizedName("dampingAgent").setMaxStackSize(1).setCreativeTab(CreativeTabs.tabMisc);;
		itemStrongDampingAgent = new ItemIM(configInvasion.getPropertyValueInt("itemID-StrongDampingAgent", DEFAULT_ITEM_ID_STRONGDAMPINGAGENT)).setUnlocalizedName("strongDampingAgent").setMaxStackSize(1).setCreativeTab(CreativeTabs.tabMisc);;
		itemStrangeBone = new ItemStrangeBone(configInvasion.getPropertyValueInt("itemID-StrangeBone", DEFAULT_ITEM_ID_STRANGEBONE)).setUnlocalizedName("strangeBone").setMaxStackSize(1).setCreativeTab(CreativeTabs.tabMisc);;
		itemStrongCatalyst = new ItemIM(configInvasion.getPropertyValueInt("itemID-StrongCatalyst", DEFAULT_ITEM_ID_STRONGCATALYST)).setUnlocalizedName("strongCatalyst").setMaxStackSize(1).setCreativeTab(CreativeTabs.tabMisc);
		itemEngyHammer = new ItemIM(configInvasion.getPropertyValueInt("itemID-EngyHammer", DEFAULT_ITEM_ID_HAMMER)).setUnlocalizedName("engyHammer").setMaxStackSize(1);
		itemProbe = new ItemProbe(configInvasion.getPropertyValueInt("itemID-Probe", DEFAULT_ITEM_ID_PROBE)).setUnlocalizedName("probe").setCreativeTab(CreativeTabs.tabMisc);;
		itemIMTrap = new ItemIMTrap(configInvasion.getPropertyValueInt("itemID-IMTrap", DEFAULT_ITEM_ID_IMTRAP)).setUnlocalizedName("trap").setCreativeTab(CreativeTabs.tabMisc);;
		
		if(debugMode)
		{
			itemDebugWand = new ItemDebugWand(configInvasion.getPropertyValueInt("itemID-DebugWand", DEFAULT_ITEM_ID_DEBUGWAND)).setUnlocalizedName("debugWand");
		}
		else
		{
			itemDebugWand = null;
		}
	}
	
	protected void loadEntities()
	{
		EntityRegistry.registerGlobalEntityID(EntityIMZombie.class, "IMZombie", 110);
    	EntityRegistry.registerGlobalEntityID(EntityIMSkeleton.class, "IMSkeleton", 111);
    	EntityRegistry.registerGlobalEntityID(EntityIMSpider.class, "IMSpider", 112);
    	EntityRegistry.registerGlobalEntityID(EntityIMPigEngy.class, "IMPigEngy", 113);
    	//EntityRegistry.registerGlobalEntityID(EntityIMBurrower.class, "IMBurrower", 114);
    	EntityRegistry.registerGlobalEntityID(EntityIMBird.class, "IMBird", 114);
    	EntityRegistry.registerGlobalEntityID(EntityIMThrower.class, "IMThrower", 116); 	
    	EntityRegistry.registerGlobalEntityID(EntityIMBoulder.class, "IMBoulder", 117);	
    	//EntityRegistry.registerGlobalEntityID(EntityImpT1.class, "ImpT1", xx);
    	EntityRegistry.registerGlobalEntityID(EntityIMTrap.class, "IMTrap", 109);
    	EntityRegistry.registerGlobalEntityID(EntityIMArrowOld.class, "IMPenArrow", 118);
    	EntityRegistry.registerGlobalEntityID(EntityIMWolf.class, "IMWolf", 119);
    	EntityRegistry.registerGlobalEntityID(EntityIMBolt.class, "IMBolt", 115);
    	EntityRegistry.registerGlobalEntityID(EntityIMEgg.class, "IMEgg", 108);
    	EntityRegistry.registerGlobalEntityID(EntityIMCreeper.class, "IMCreeper", 107);
    	EntityRegistry.registerGlobalEntityID(EntityIMGiantBird.class, "IMGiantBird", 106);
    	
    	EntityRegistry.registerModEntity(EntityIMBoulder.class, "IMBoulder", 1, this, 36, 5, true);
    	EntityRegistry.registerModEntity(EntityIMBolt.class, "IMBolt", 2, this, 36, 5, false);
    	EntityRegistry.registerModEntity(EntityIMTrap.class, "IMTrap", 3, this, 36, 5, false);
    	EntityRegistry.registerModEntity(EntityIMArrowOld.class, "IMArrow", 4, this, 36, 5, true);
    	
    	proxy.preloadTexture("/mods/invmod/textures/zombie_old.png");
    	proxy.preloadTexture("/mods/invmod/textures/zombieT2.png");
    	proxy.preloadTexture("/mods/invmod/textures/zombieT2.png");
    	proxy.preloadTexture("/mods/invmod/textures/zombieT2a.png");
    	proxy.preloadTexture("/mods/invmod/textures/zombietar.png");
    	proxy.preloadTexture("/mods/invmod/textures/zombieT1a.png");
    	proxy.preloadTexture("/mods/invmod/textures/spiderT2.png");
    	proxy.preloadTexture("/mods/invmod/textures/spiderT2b.png");
    	proxy.preloadTexture("/mods/invmod/textures/throwerT1.png");
    	proxy.preloadTexture("/mods/invmod/textures/pigengT1.png");
    	proxy.preloadTexture("/mods/invmod/textures/imp.png");
    	proxy.preloadTexture("/mods/invmod/textures/nexusgui.png");
    	proxy.preloadTexture("/mods/invmod/textures/boulder.png");
    	proxy.preloadTexture("/mods/invmod/textures/trap.png");
    	proxy.preloadTexture("/mods/invmod/textures/testmodel.png");
    	proxy.preloadTexture("/mods/invmod/textures/burrower.png");
    	proxy.preloadTexture("/mods/invmod/textures/spideregg.png");
    	proxy.preloadTexture("/mods/invmod/textures/zombieT3.png");
    	
    	proxy.loadAnimations();
    	proxy.registerEntityRenderers();
	}
	
	protected void loadNames()
	{
		LanguageRegistry.addName(blockNexus, "Nexus");
    	LanguageRegistry.addName(itemPhaseCrystal, "Phase Crystal");
    	LanguageRegistry.addName(itemNexusCatalyst, "Nexus Catalyst");
    	LanguageRegistry.addName(itemInfusedSword, "Infused Sword");
    	LanguageRegistry.addName(itemPenBow, "Searing Bow");
    	LanguageRegistry.addName(itemCataMixture, "Catalyst Mixture");
    	LanguageRegistry.addName(itemStableCataMixture, "Stable Catalyst Mixture");
    	LanguageRegistry.addName(itemStableNexusCatalyst, "Stable Catalyst");
    	LanguageRegistry.addName(itemDampingAgent, "Damping Agent");
    	LanguageRegistry.addName(itemStrongDampingAgent, "Strong Damping Agent");
    	LanguageRegistry.addName(itemStrangeBone, "Strange Bone");
    	LanguageRegistry.addName(itemProbe, "Probe");
    	LanguageRegistry.addName(itemStrongCatalyst, "Strong Catalyst");
    	LanguageRegistry.addName(new ItemStack(itemRemnants, 1, 0), ItemRemnants.remnantNames[0]);
    	LanguageRegistry.addName(new ItemStack(itemRemnants, 1, 1), ItemRemnants.remnantNames[1]);
    	LanguageRegistry.addName(new ItemStack(mod_Invasion.itemRemnants, 1, 2), ItemRemnants.remnantNames[2]);
    	LanguageRegistry.addName(new ItemStack(mod_Invasion.itemRiftFlux, 1, 0), ItemRiftFlux.fluxNames[0]);
    	LanguageRegistry.addName(new ItemStack(mod_Invasion.itemRiftFlux, 1, 1), ItemRiftFlux.fluxNames[1]);
    	LanguageRegistry.addName(new ItemStack(mod_Invasion.itemRiftFlux, 1, 2), ItemRiftFlux.fluxNames[2]);
    	LanguageRegistry.addName(new ItemStack(mod_Invasion.itemRiftFlux, 1, 3), ItemRiftFlux.fluxNames[3]);
    	LanguageRegistry.addName(new ItemStack(mod_Invasion.itemIMTrap, 1, 0), ItemIMTrap.trapNames[0]);
    	LanguageRegistry.addName(new ItemStack(mod_Invasion.itemIMTrap, 1, 1), ItemIMTrap.trapNames[1]);
    	LanguageRegistry.addName(new ItemStack(mod_Invasion.itemIMTrap, 1, 2), ItemIMTrap.trapNames[2]);
    	LanguageRegistry.addName(new ItemStack(mod_Invasion.itemProbe, 1, 0), ItemProbe.probeNames[0]);
    	LanguageRegistry.addName(new ItemStack(mod_Invasion.itemProbe, 1, 1), ItemProbe.probeNames[1]);
    	
    	if(debugMode)
    		LanguageRegistry.addName(itemDebugWand, "Debug Wand");
	}
	
	protected void addRecipes()
	{
		GameRegistry.addRecipe(new ItemStack(blockNexus, 1), new Object[] {
			" X ", "#D#", " # ", 'X', mod_Invasion.itemPhaseCrystal, '#', Item.redstone, 'D', Block.obsidian
		});
		GameRegistry.addRecipe(new ItemStack(itemPhaseCrystal, 1), new Object[] {
			" X ", "#D#", " X ", 'X', new ItemStack(Item.dyePowder, 1, 4), '#', Item.redstone, 'D', Item.diamond
		});
		GameRegistry.addRecipe(new ItemStack(itemPhaseCrystal, 1), new Object[] {
			" X ", "#D#", " X ", 'X', Item.redstone, '#', new ItemStack(Item.dyePowder, 1, 4), 'D', Item.diamond
		});
		GameRegistry.addRecipe(new ItemStack(itemRiftFlux, 1, 1), new Object[] {
			"XXX", "XXX", "XXX", 'X', new ItemStack(mod_Invasion.itemRemnants, 1, 1)
		});
		GameRegistry.addRecipe(new ItemStack(itemInfusedSword, 1), new Object[] {
			"X  ", "X# ", "X  ", 'X', new ItemStack(mod_Invasion.itemRiftFlux, 1, 1), '#', Item.swordDiamond
		});
		GameRegistry.addRecipe(new ItemStack(itemCataMixture, 1), new Object[] {
			"   ", "D#H", " X ", 'X', Item.bowlEmpty, '#', Item.redstone, 'D', Item.bone, 'H', Item.rottenFlesh
		});
		GameRegistry.addRecipe(new ItemStack(itemCataMixture, 1), new Object[] {
			"   ", "H#D", " X ", 'X', Item.bowlEmpty, '#', Item.redstone, 'D', Item.bone, 'H', Item.rottenFlesh
		});
		GameRegistry.addRecipe(new ItemStack(itemStableCataMixture, 1), new Object[] {
			"   ", "D#D", " X ", 'X', Item.bowlEmpty, '#', Item.coal, 'D', Item.bone, 'H', Item.rottenFlesh
		});
		GameRegistry.addRecipe(new ItemStack(itemDampingAgent, 1), new Object[] {
			"   ", "#X#", "   ", 'X', new ItemStack(itemRiftFlux, 1, 1), '#', new ItemStack(Item.dyePowder, 1, 4)
		});
		GameRegistry.addRecipe(new ItemStack(itemStrongDampingAgent, 1), new Object[] {
			" X ", " X ", " X ", 'X', itemDampingAgent
		});
		GameRegistry.addRecipe(new ItemStack(itemStrongDampingAgent, 1), new Object[] {
			"   ", "XXX", "   ", 'X', itemDampingAgent
		});
		GameRegistry.addRecipe(new ItemStack(itemStrangeBone, 1), new Object[] {
			"   ", "X#X", "   ", 'X', new ItemStack(itemRiftFlux, 1, 1), '#', Item.bone
		});
		GameRegistry.addRecipe(new ItemStack(itemPenBow, 1), new Object[] {
			"XXX", "X# ", "X  ", 'X', new ItemStack(itemRiftFlux, 1, 1), '#', Item.bow
		});
		GameRegistry.addRecipe(new ItemStack(Item.diamond, 1), new Object[] {
			" X ", " X ", " X ", 'X', new ItemStack(itemRiftFlux, 1, 1)
		});
		GameRegistry.addRecipe(new ItemStack(Item.diamond, 1), new Object[] {
			"   ", "XXX", "   ", 'X', new ItemStack(itemRiftFlux, 1, 1)
		});
		GameRegistry.addRecipe(new ItemStack(Item.ingotIron, 4), new Object[] {
			"   ", " X ", "   ", 'X', new ItemStack(itemRiftFlux, 1, 1)
		});
		GameRegistry.addRecipe(new ItemStack(Item.redstone, 24), new Object[] {
			"   ", "X X", "   ", 'X', new ItemStack(itemRiftFlux, 1, 1)
		});
		GameRegistry.addRecipe(new ItemStack(Item.dyePowder, 12, 4), new Object[] {
			" X ", "   ", " X ", 'X', new ItemStack(itemRiftFlux, 1, 1)
		});
		GameRegistry.addRecipe(new ItemStack(itemIMTrap, 1, 0), new Object[] {
			" X ", "X#X", " X ", 'X', Item.ingotIron, '#', new ItemStack(itemRiftFlux, 1, 1)
		});
		GameRegistry.addRecipe(new ItemStack(itemIMTrap, 1, EntityIMTrap.TRAP_FIRE), new Object[] {
			"   ", " # ", " X ", 'X', new ItemStack(itemIMTrap, 1, 0), '#', Item.bucketLava
		});
		GameRegistry.addRecipe(new ItemStack(itemProbe, 1, 0), new Object[] {
			" X ", "XX ", "XX ", 'X', Item.ingotIron
		});
		GameRegistry.addRecipe(new ItemStack(itemProbe, 1, 1), new Object[] {
			" D ", " # ", " X ", 'X', Item.blazeRod, '#', itemPhaseCrystal, 'D', new ItemStack(itemProbe, 1, 0)
		});
		
		if(debugMode)
		{
			/*GameRegistry.AddRecipe(new ItemStack(itemIMTrap, 8, 1), new Object[] {
				"   ", " X ", "   ", 'X', Block.dirt
			});
			GameRegistry.AddRecipe(new ItemStack(itemIMTrap, 8, 1), new Object[] {
				"   ", " X ", "   ", 'X', Block.stone
			});
			GameRegistry.AddRecipe(new ItemStack(itemIMTrap, 8, 2), new Object[] {
				"   ", "  X", "   ", 'X', Block.stone
			});
			GameRegistry.AddRecipe(new ItemStack(itemProbe, 1), new Object[] {
				"   ", " X ", "   ", 'X', Block.dirt
			});*/
			/*GameRegistry.addRecipe(new ItemStack(blockNexus, 1), new Object[] {
				"X  ", "   ", "   ", 'X', Block.dirt
			});
			GameRegistry.addRecipe(new ItemStack(blockNexus, 1), new Object[] {
				" X ", "   ", "   ", 'X', Block.planks
			});
			GameRegistry.addRecipe(new ItemStack(itemNexusCatalyst, 1), new Object[] {
				"   ", "   ", "  X", 'X', Block.dirt
			});
			GameRegistry.addRecipe(new ItemStack(itemInfusedSword, 1), new Object[] {
				"   ", "   ", " X ", 'X', Block.dirt
			});*/
			/*GameRegistry.AddRecipe(new ItemStack(itemRiftFlux, 1, 1), new Object[] {
				"   ", "   ", "X  ", 'X', Block.planks
			});*/
		}
		
		GameRegistry.addSmelting(itemCataMixture.itemID, new ItemStack(itemNexusCatalyst), 1.0F);
		GameRegistry.addSmelting(itemStableCataMixture.itemID, new ItemStack(itemStableNexusCatalyst), 1.0F);
	}
	
	protected void nightSpawnConfig()
	{
		nightSpawnsEnabled = configInvasion.getPropertyValueBoolean("night-spawns-enabled", DEFAULT_NIGHT_SPAWNS_ENABLED);
		nightMobSightRange = configInvasion.getPropertyValueInt("night-mob-sight-range", DEFAULT_NIGHT_MOB_SIGHT_RANGE);
		nightMobSenseRange = configInvasion.getPropertyValueInt("night-mob-sense-range", DEFAULT_NIGHT_MOB_SENSE_RANGE);
		nightMobSpawnChance = configInvasion.getPropertyValueInt("night-mob-spawn-chance", DEFAULT_NIGHT_MOB_SPAWN_CHANCE);
		nightMobMaxGroupSize = configInvasion.getPropertyValueInt("night-mob-max-group-size", DEFAULT_NIGHT_MOB_MAX_GROUP_SIZE);
		maxNightMobs = configInvasion.getPropertyValueInt("mob-limit-override", DEFAULT_NIGHT_MOB_LIMIT_OVERRIDE);
		nightMobsBurnInDay = configInvasion.getPropertyValueBoolean("night-mobs-burn-in-day", DEFAULT_NIGHT_MOBS_BURN);
		
		// Handle spawn pools for night mobs
		String[] pool1Patterns = new String[DEFAULT_NIGHT_MOB_PATTERN_1_SLOTS.length];
		float[] pool1Weights = new float[DEFAULT_NIGHT_MOB_PATTERN_1_SLOT_WEIGHTS.length];		
		RandomSelectionPool<IEntityIMPattern> mobPool = new RandomSelectionPool<IEntityIMPattern>(); // Keep a hold of the sub-class typed reference
		nightSpawnPool1 = mobPool;
		if(DEFAULT_NIGHT_MOB_PATTERN_1_SLOTS.length == DEFAULT_NIGHT_MOB_PATTERN_1_SLOT_WEIGHTS.length)
		{
			for(int i = 0; i < DEFAULT_NIGHT_MOB_PATTERN_1_SLOTS.length; i++)
			{
				pool1Patterns[i] = configInvasion.getPropertyValueString("nm-spawnpool1-slot" + (1 + i), DEFAULT_NIGHT_MOB_PATTERN_1_SLOTS[i]);
				pool1Weights[i] = configInvasion.getPropertyValueFloat("nm-spawnpool1-slot" + (1 + i) + "-weight", DEFAULT_NIGHT_MOB_PATTERN_1_SLOT_WEIGHTS[i]);
				if(IMWaveBuilder.isPatternNameValid(pool1Patterns[i]))
				{
					log("Added entry for pattern 1 slot " + (i + 1));
					mobPool.addEntry(IMWaveBuilder.getPattern(pool1Patterns[i]), pool1Weights[i]);
				}
				else
				{
					log("Pattern 1 slot " + (i + 1) + " in config not recognised. Proceeding as blank.");
					configInvasion.setProperty("nm-spawnpool1-slot" + (1 + i), "none");
				}
			}
		}
		else
		{
			log("Mob pattern table element mismatch. Ensure each slot has a probability weight");
		}	
	}
	
	public static boolean onClientTick()
    {
    	if(runFlag)
    	{
    		if(soundsEnabled && !soundHandler.soundsInstalled())
    		{
    			proxy.printGuiMessage("Invasion Mod Warning: Failed to auto-install sounds. You can disable this process in config or give a bug report");
    		}
    		runFlag = false;
    	}
	    
    	return true;
	}
	
	public static boolean onServerTick()
	{
		if(serverRunFlag)
    	{
    		timer = System.currentTimeMillis();
    		serverRunFlag = false;
    	}
    	
    	serverElapsed -= timer;
    	timer = System.currentTimeMillis();
    	serverElapsed += timer;
    	if(serverElapsed >= 100)
    	{
    		serverElapsed -= 100;
    		
    		if(loginFlag)
        	{
        		killTimer++;
        	}
        	
        	if(killTimer > 35) //Spawn protection is unreliable. So we have to wait an upper bound
        	{
        		killTimer = 0;
        		loginFlag = false;
    	    	for(Entry<String, Long> entry : deathList.entrySet())
    			{
    	    		if(System.currentTimeMillis() - entry.getValue() > 300000)
    	    		{
    	    			deathList.remove(entry.getKey());
    	    		}
    	    		else
    	    		{
    		    		for(World world : DimensionManager.getWorlds())
    		    		{
    		    			EntityPlayer player = world.getPlayerEntityByName(entry.getKey());
    		    			if(player != null)
    		    			{
    		    				player.attackEntityFrom(DamageSource.magic, 500);
    		    				player.setDead();
    							deathList.remove(player.username);
    							broadcastToAll("Nexus energies caught up to " + player.username);
    						}
    		    		}
    	    		}
    			}
        	}
    	}
    	
    	return true;
	}
	
	public static void addToDeathList(String username, long timeStamp)
	{
		deathList.put(username, timeStamp);
	}
	
	@Override
	public String toString()
	{
		return "mod_Invasion";
	}
	
	@Override
	protected void finalize() throws Throwable
	{	    
	    try
	    {
	    	if(logOut != null)
	    		logOut.close();
	    }
	    catch(Exception e)
	    {
	        logOut = null;
	        log("Error closing invasion log file");
	    }	    
	    finally
	    {	        
	        super.finalize();
	    }
	}
	
	public static boolean isInvasionActive()
	{
		return isInvasionActive;
	}
	
	public static boolean tryGetInvasionPermission(TileEntityNexus nexus)
	{
		if(nexus == activeNexus)
		{
			return true;
		}
		else if (nexus == null)
		{
			String s = "Nexus entity invalid";
			log(s);
		}
		else
		{
			activeNexus = nexus;
			isInvasionActive = true;
			return true;
		}
		return false;
	}
	
	public static void setInvasionEnded(TileEntityNexus nexus)
	{
		if(activeNexus == nexus)
		{
			isInvasionActive = false;
		}
	}
	
	public static void setNexusUnloaded(TileEntityNexus nexus)
	{
		if(activeNexus == nexus)
		{
			nexus = null;
			isInvasionActive = false;
		}
	}
	
	public static void setNexusClicked(TileEntityNexus nexus)
	{
		focusNexus = nexus;
	}
	
	public static TileEntityNexus getActiveNexus()
	{
		return activeNexus;
	}
	
	public static TileEntityNexus getFocusNexus()
	{
		return focusNexus;
	}
	
	public static Entity[] getNightMobSpawns1(World world)
	{
		ISelect<IEntityIMPattern> mobPool = getMobSpawnPool();
		int numberOfMobs = world.rand.nextInt(nightMobMaxGroupSize) + 1;
		Entity[] entities = new Entity[numberOfMobs];
		for(int i = 0; i < numberOfMobs; i++)
		{
			EntityIMLiving mob = getMobBuilder().createMobFromConstruct(mobPool.selectNext().generateEntityConstruct(), world, null);
			mob.setEntityIndependent();
			mob.setAggroRange(getNightMobSightRange());
			mob.setSenseRange(getNightMobSenseRange());
			mob.setBurnsInDay(getNightMobsBurnInDay());
			entities[i] = mob;
		}
		return entities;
	}
	
	public static MobBuilder getMobBuilder()
	{
		return defaultMobBuilder;
	}
	
	public static ISelect<IEntityIMPattern> getMobSpawnPool()
	{
		return nightSpawnPool1;
	}
	
	public static int getMinContinuousModeDays()
	{
		return minContinuousModeDays;
	}
	
	public static int getMaxContinuousModeDays()
	{
		return maxContinuousModeDays;
	}
	
	public static int getNightMobSightRange()
	{
		return nightMobSightRange;
	}
	
	public static int getNightMobSenseRange()
	{
		return nightMobSenseRange;
	}
	
	public static boolean getNightMobsBurnInDay()
	{
		return nightMobsBurnInDay;
	}
	
	public static ItemStack getRenderHammerItem()
	{
		return new ItemStack(itemEngyHammer, 1);
	}
	
	public static int getGuiIdNexus()
	{
		return guiIdNexus;
	}
	
	/**
	 * Returns a loaded instance of this mod class if there is one.
	 */
	public static mod_Invasion getLoadedInstance()
	{
		return instance;
	}
	
	/**
	 * Server-side only
	 */
	public static void sendInvasionPacketToAll(byte[] data)
	{
		Packet250CustomPayload packet = new Packet250CustomPayload();
        packet.channel = "data";
        packet.data = data;
        packet.length = packet.data.length;
        FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendPacketToAllPlayers(packet);
	}
	
	/**
	 * Server-side only
	 */
	public static void broadcastToAll(String message)
	{
		// TODO
		//FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(message);
		FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendPacketToAllPlayers(new Packet3Chat(message));
		//proxyLoader.broadcastToAll(message);
	}
	
	public static void sendMessageToPlayer(String user, String message)
	{
		EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().getPlayerForUsername(user);
		if(player != null)
		{
			player.sendChatToPlayer(message);
		}
	}
	
	/**
	 * Plays a sound to everyone. Only available on the server.
	 */
	public static void playGlobalSFX(String s)
	{
		soundHandler.playGlobalSFX(s);
	}
	
	/**
	 * Plays a sound client-side only.
	 */
	public static void playSingleSFX(String s)
	{
		soundHandler.playSingleSFX(s);
	}
	
	/**
	 * Plays a sound client-side only.
	 */
	public static void playSingleSFX(byte id)
	{
		soundHandler.playSingleSFX(id);
	}
	
	public static void log(String s)
	{
		if(s == null)
			return;
		
		try
		{
			if(logOut != null)
			{
				logOut.write(s);
				logOut.newLine();
				logOut.flush();
			}
			else
			{
				System.out.println(s);
			}
		}
		catch(IOException e)
		{
			System.out.println("Couldn't write to invasion log file");
			System.out.println(s);
		}
	}
	
	public static boolean isDebug()
	{
		return debugMode;
	}
	
	public static BowHackHandler getBowHackHandler()
	{
		return bowHandler;
	}
	
	
}