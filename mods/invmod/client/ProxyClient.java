package mods.invmod.client;


import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mods.invmod.client.render.*;
import mods.invmod.client.render.animation.Animation;
import mods.invmod.client.render.animation.AnimationAction;
import mods.invmod.client.render.animation.AnimationPhaseInfo;
import mods.invmod.client.render.animation.BonesMouth;
import mods.invmod.client.render.animation.BonesWings;
import mods.invmod.client.render.animation.InterpType;
import mods.invmod.client.render.animation.KeyFrame;
import mods.invmod.client.render.animation.ModelAnimator;
import mods.invmod.client.render.animation.BonesBirdLegs;
import mods.invmod.client.render.animation.Transition;
import mods.invmod.common.ProxyCommon;
import mods.invmod.common.util.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderPig;
import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ProxyClient extends ProxyCommon
{
	@Override
	public void preloadTexture(String texture)
	{
		MinecraftForgeClient.preloadTexture(texture);
	}

	@Override
	public int addTextureOverride(String fileToOverride, String fileToAdd)
	{
		return RenderingRegistry.addTextureOverride(fileToOverride, fileToAdd);
	}

	@Override
	public void registerEntityRenderingHandler(Class<? extends Entity> entityClass, Render renderer)
	{
		RenderingRegistry.registerEntityRenderingHandler(entityClass, renderer);
	}

	@Override
	public void printGuiMessage(String message)
	{
		FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().printChatMessage(message);
	}

	@Override
	public void registerEntityRenderers()
	{
		RenderingRegistry.registerEntityRenderingHandler(mods.invmod.common.entity.EntityIMZombie.class, new RenderIMZombie(new ModelZombie(0, true), 0.5F));
		RenderingRegistry.registerEntityRenderingHandler(mods.invmod.common.entity.EntityIMSkeleton.class, new RenderBiped(new ModelIMSkeleton(), 0.5F));
		RenderingRegistry.registerEntityRenderingHandler(mods.invmod.common.entity.EntityIMSpider.class, new RenderSpiderIM());
		RenderingRegistry.registerEntityRenderingHandler(mods.invmod.common.entity.EntityIMPigEngy.class, new RenderBiped(new ModelBiped(), 0.5F));
		RenderingRegistry.registerEntityRenderingHandler(mods.invmod.common.entity.EntityIMImp.class, new RenderImp(new ModelImp(), 0.3F));
		RenderingRegistry.registerEntityRenderingHandler(mods.invmod.common.entity.EntityIMThrower.class, new RenderThrower(new ModelThrower(), 1.5F));
		RenderingRegistry.registerEntityRenderingHandler(mods.invmod.common.entity.EntityIMBurrower.class, new RenderBurrower());
		RenderingRegistry.registerEntityRenderingHandler(mods.invmod.common.entity.EntityIMBoulder.class, new RenderBoulder());
		RenderingRegistry.registerEntityRenderingHandler(mods.invmod.common.entity.EntityIMTrap.class, new RenderTrap(new ModelTrap()));
		RenderingRegistry.registerEntityRenderingHandler(mods.invmod.common.entity.EntityIMArrowOld.class, new RenderPenArrow());
		RenderingRegistry.registerEntityRenderingHandler(mods.invmod.common.entity.EntityIMBolt.class, new RenderBolt());
		RenderingRegistry.registerEntityRenderingHandler(mods.invmod.common.entity.EntitySFX.class, new RenderInvis());
		RenderingRegistry.registerEntityRenderingHandler(mods.invmod.common.entity.EntityIMSpawnProxy.class, new RenderInvis());
		RenderingRegistry.registerEntityRenderingHandler(mods.invmod.common.entity.EntityIMEgg.class, new RenderEgg());
		//proxyLoader.registerEntityRenderingHandler(invmod.EntityIMEgg.class, new RenderBiped(new ModelSkeleton(), 0.5F));
		RenderingRegistry.registerEntityRenderingHandler(mods.invmod.common.entity.EntityIMCreeper.class, new RenderIMCreeper());
		RenderingRegistry.registerEntityRenderingHandler(mods.invmod.common.entity.EntityIMBird.class, new RenderB());
		RenderingRegistry.registerEntityRenderingHandler(mods.invmod.common.entity.EntityIMGiantBird.class, new RenderGiantBird());
	}

	@Override
	public void loadAnimations()
	{
		EnumMap<BonesBirdLegs, List<KeyFrame>> allKeyFrames = new EnumMap<BonesBirdLegs, List<KeyFrame>>(BonesBirdLegs.class);
		List<AnimationPhaseInfo> animationPhases = new ArrayList<AnimationPhaseInfo>(2);
		int x = 17;
		float totalFrames = 331 + x;
		
		// Stand
		Map<AnimationAction, Transition> transitions = new HashMap<AnimationAction, Transition>(1);
		Transition defaultTransition = new Transition(AnimationAction.STAND, 1F / totalFrames, 0);
		transitions.put(AnimationAction.STAND, defaultTransition); // Keep standing
		transitions.put(AnimationAction.STAND_TO_RUN, new Transition(AnimationAction.STAND_TO_RUN, 1F / totalFrames, 1F / totalFrames));
		transitions.put(AnimationAction.LEGS_RETRACT, new Transition(AnimationAction.LEGS_RETRACT, 1F / totalFrames, (211F + x) / totalFrames));
		transitions.put(AnimationAction.LEGS_CLAW_ATTACK_P1, new Transition(AnimationAction.LEGS_CLAW_ATTACK_P1, 1F / totalFrames, (171F + x) / totalFrames));
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.STAND, 0, 1F / totalFrames, defaultTransition, transitions));
		
		
		// Stand to running
		transitions = new HashMap<AnimationAction, Transition>(1);
		defaultTransition = new Transition(AnimationAction.RUN, 38F / totalFrames, 38F / totalFrames);
		transitions.put(AnimationAction.RUN, defaultTransition); // Continue
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.STAND_TO_RUN, 1F / totalFrames, 38F / totalFrames, defaultTransition, transitions));
		
		// Running
		transitions = new HashMap<AnimationAction, Transition>(1);
		defaultTransition = new Transition(AnimationAction.RUN, (170F + x) / totalFrames, 38F / totalFrames);
		transitions.put(AnimationAction.RUN, defaultTransition); // Cycle
		transitions.put(AnimationAction.STAND, new Transition(AnimationAction.STAND, (170F + x) / totalFrames, 0));
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.RUN, 38F / totalFrames, (170F + x) / totalFrames, defaultTransition, transitions));
		
		// Standing to legs swung back
		transitions = new HashMap<AnimationAction, Transition>(1);
		defaultTransition = new Transition(AnimationAction.LEGS_UNRETRACT, (251F + x) / totalFrames, (251F + x) / totalFrames);
		transitions.put(AnimationAction.LEGS_UNRETRACT, defaultTransition);
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.LEGS_RETRACT, (211F + x) / totalFrames, (251F + x) / totalFrames, defaultTransition, transitions));
		
		// Swung back to standing
		transitions = new HashMap<AnimationAction, Transition>(1);
		defaultTransition = new Transition(AnimationAction.STAND, (291F + x) / totalFrames, 0);
		transitions.put(AnimationAction.STAND, defaultTransition);
		transitions.put(AnimationAction.LEGS_RETRACT, new Transition(AnimationAction.LEGS_RETRACT, (291F + x) / totalFrames, (211F + x) / totalFrames));
		transitions.put(AnimationAction.LEGS_CLAW_ATTACK_P1, new Transition(AnimationAction.LEGS_CLAW_ATTACK_P1, (291F + x) / totalFrames, (291F + x) / totalFrames));
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.LEGS_UNRETRACT, (251F + x) / totalFrames, (291F + x) / totalFrames, defaultTransition, transitions));
		
		// Standing to claws forward
		transitions = new HashMap<AnimationAction, Transition>(1);
		defaultTransition = new Transition(AnimationAction.LEGS_CLAW_ATTACK_P2, (331F + x) / totalFrames, (171F + x) / totalFrames);
		transitions.put(AnimationAction.LEGS_CLAW_ATTACK_P2, defaultTransition);
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.LEGS_CLAW_ATTACK_P1, (291F + x) / totalFrames, (331F + x) / totalFrames, defaultTransition, transitions));

		// Claws forward to standing
		transitions = new HashMap<AnimationAction, Transition>(1);
		defaultTransition = new Transition(AnimationAction.STAND, (211F + x) / totalFrames, 0);
		transitions.put(AnimationAction.STAND, defaultTransition);
		transitions.put(AnimationAction.LEGS_RETRACT, new Transition(AnimationAction.LEGS_RETRACT, (211F + x) / totalFrames, (211F + x) / totalFrames));
		transitions.put(AnimationAction.LEGS_CLAW_ATTACK_P1, new Transition(AnimationAction.LEGS_CLAW_ATTACK_P1, (211F + x) / totalFrames, (291F + x) / totalFrames));
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.LEGS_CLAW_ATTACK_P2, (171F + x) / totalFrames, (211F + x) / totalFrames, defaultTransition, transitions));
		
		float frameUnit = 1.0F / totalFrames;
		float runBegin = 38 * frameUnit;
		float runEnd = (170 + x) * frameUnit;
		

		// Left thigh
		List<KeyFrame> leftThighFrames = new ArrayList<KeyFrame>(13);
		leftThighFrames.add(new KeyFrame(0, -15, 0, -5, InterpType.LINEAR));
		leftThighFrames.add(new KeyFrame(1 * frameUnit, -15, 0, -5, InterpType.LINEAR));
		leftThighFrames.add(new KeyFrame(5 * frameUnit, -12.6F, 0.2F, 5F, InterpType.LINEAR));
		leftThighFrames.add(new KeyFrame(10 * frameUnit, 21.2F, -0.6F, 5.2F, InterpType.LINEAR));
		leftThighFrames.add(new KeyFrame(15 * frameUnit, -32, -1.7F, 5.7F, InterpType.LINEAR));
		leftThighFrames.add(new KeyFrame(25 * frameUnit, -57.F, -6.4F, 9, InterpType.LINEAR));
		leftThighFrames.add(new KeyFrame(35 * frameUnit, -76.5F, -19.3F, 21.2F, InterpType.LINEAR));
		KeyFrame.toRadians(leftThighFrames);
		List<KeyFrame> leftThighRunCycle = new ArrayList<KeyFrame>(7);
		leftThighRunCycle.add(new KeyFrame(38 * frameUnit, -74.1F, 0, -6.5F, InterpType.LINEAR));
		leftThighRunCycle.add(new KeyFrame(44 * frameUnit, -63.7F, 0, -6.5F, InterpType.LINEAR));
		leftThighRunCycle.add(new KeyFrame((80 + x) * frameUnit, 13.1F, 0, -6.5F, InterpType.LINEAR));
		leftThighRunCycle.add(new KeyFrame((101 + x) * frameUnit, 35.7F, 0, -6.5F, InterpType.LINEAR));
		leftThighRunCycle.add(new KeyFrame((110 + x) * frameUnit, 20, 0, -6.5F, InterpType.LINEAR));
		leftThighRunCycle.add(new KeyFrame((140 + x) * frameUnit, -33, 0, -6.5F, InterpType.LINEAR));
		leftThighRunCycle.add(new KeyFrame((170 + x) * frameUnit, -74.1F, 0, -6.5F, InterpType.LINEAR));
		leftThighRunCycle.add(new KeyFrame((171 + x) * frameUnit, -76, 0, -5.6F, InterpType.LINEAR));
		leftThighRunCycle.add(new KeyFrame((211 + x) * frameUnit, -15, 0, -5F, InterpType.LINEAR));
		leftThighRunCycle.add(new KeyFrame((251 + x) * frameUnit, 9F, 0, 0F, InterpType.LINEAR));
		leftThighRunCycle.add(new KeyFrame((291 + x) * frameUnit, -15, 0, -5F, InterpType.LINEAR));
		leftThighRunCycle.add(new KeyFrame((331 + x) * frameUnit, -76, 0, -5.6F, InterpType.LINEAR));
		KeyFrame.toRadians(leftThighRunCycle);

		// Right thigh
		List<KeyFrame> rightThighFrames = new ArrayList<KeyFrame>(13);
		rightThighFrames.add(new KeyFrame(0, -15, 0, 0, InterpType.LINEAR));
		rightThighFrames.add(new KeyFrame(1 * frameUnit, -15, 0, 0, InterpType.LINEAR));
		rightThighFrames.add(new KeyFrame(37 * frameUnit, -15, 0, 0, InterpType.LINEAR));
		KeyFrame.toRadians(rightThighFrames);
		List<KeyFrame> rightThighRunCycle = KeyFrame.cloneFrames(leftThighRunCycle);
		KeyFrame.mirrorFramesX(rightThighRunCycle);
		KeyFrame.offsetFramesCircular(rightThighRunCycle, runBegin, runEnd, (runEnd - runBegin) / 2);

		// Concatenate and add thigh animations
		leftThighFrames.addAll(leftThighRunCycle);
		rightThighFrames.addAll(rightThighRunCycle);
		allKeyFrames.put(BonesBirdLegs.LEFT_KNEE, leftThighFrames);
		allKeyFrames.put(BonesBirdLegs.RIGHT_KNEE, rightThighFrames);

		// Left leg
		List<KeyFrame> leftLegFrames = new ArrayList<KeyFrame>(19);
		leftLegFrames.add(new KeyFrame(0, -41, 0, 0, InterpType.LINEAR));
		leftLegFrames.add(new KeyFrame(1 * frameUnit, -41, 0, 0, InterpType.LINEAR));
		leftLegFrames.add(new KeyFrame(10 * frameUnit, -80.3F, 0, 0, InterpType.LINEAR));
		leftLegFrames.add(new KeyFrame(25 * frameUnit, -44.2F, 0, 0, InterpType.LINEAR));
		leftLegFrames.add(new KeyFrame(35 * frameUnit, -5.6F, 0, 0, InterpType.LINEAR));
		KeyFrame.toRadians(leftLegFrames);
		List<KeyFrame> leftLegRunCycle = new ArrayList<KeyFrame>(16);
		leftLegRunCycle.add(new KeyFrame(38 * frameUnit, 6.6F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame(44 * frameUnit, 6.5F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame(47 * frameUnit, -11, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame(50 * frameUnit, -24, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame(53 * frameUnit, -32.9F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame(56 * frameUnit, -40.8F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame(59 * frameUnit, -46.7F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame(62 * frameUnit, -45.8F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((70 + 12) * frameUnit, -45.6F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((80 + 17) * frameUnit, -17.1F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((85 + x) * frameUnit, 0.75F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((90 + x) * frameUnit, -0.4F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((101 + x) * frameUnit, -43, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((115 + x) * frameUnit, -60.1F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((154 + x) * frameUnit, -50.5F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((170 + x) * frameUnit, 6.6F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((171 + x) * frameUnit, -37F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((211 + x) * frameUnit, -41F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((251 + x) * frameUnit, 15F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((291 + x) * frameUnit, -41F, 0, 0, InterpType.LINEAR));
		leftLegRunCycle.add(new KeyFrame((331 + x) * frameUnit, -37F, 0, 0, InterpType.LINEAR));
		KeyFrame.toRadians(leftLegRunCycle);

		// Right leg
		List<KeyFrame> rightLegFrames = new ArrayList<KeyFrame>(19);
		rightLegFrames.add(new KeyFrame(0, -41, 0, 0, InterpType.LINEAR));
		rightLegFrames.add(new KeyFrame(37 * frameUnit, -41, 0, 0, InterpType.LINEAR));
		KeyFrame.toRadians(rightLegFrames);

		List<KeyFrame> rightLegRunCycle = KeyFrame.cloneFrames(leftLegRunCycle);
		KeyFrame.mirrorFramesX(rightLegRunCycle);
		KeyFrame.offsetFramesCircular(rightLegRunCycle, runBegin, runEnd, (runEnd - runBegin) / 2);

		// Concat and set animation
		leftLegFrames.addAll(leftLegRunCycle);
		rightLegFrames.addAll(rightLegRunCycle);
		allKeyFrames.put(BonesBirdLegs.LEFT_ANKLE, leftLegFrames);
		allKeyFrames.put(BonesBirdLegs.RIGHT_ANKLE, rightLegFrames);


		// Left ankle
		List<KeyFrame> leftAnkleFrames = new ArrayList<KeyFrame>(27);
		leftAnkleFrames.add(new KeyFrame(0, -0.4F, -5, 0, InterpType.LINEAR));
		leftAnkleFrames.add(new KeyFrame(1 * frameUnit, -0.4F, -5, 0, InterpType.LINEAR));
		leftAnkleFrames.add(new KeyFrame(5 * frameUnit, 31.7F, -5, 0, InterpType.LINEAR));
		leftAnkleFrames.add(new KeyFrame(10 * frameUnit, 45, -5, 0, InterpType.LINEAR));
		leftAnkleFrames.add(new KeyFrame(20 * frameUnit, 52.8F, -5, 0, InterpType.LINEAR));
		leftAnkleFrames.add(new KeyFrame(25 * frameUnit, 51.6F, -5, 0, InterpType.LINEAR));
		leftAnkleFrames.add(new KeyFrame(30 * frameUnit, 42.3F, -5, 0, InterpType.LINEAR));
		KeyFrame.toRadians(leftAnkleFrames);
		List<KeyFrame> leftAnkleRunCycle = new ArrayList<KeyFrame>(21);
		leftAnkleRunCycle.add(new KeyFrame(38 * frameUnit, 28.8F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame(44 * frameUnit, -0.4F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame(47 * frameUnit, 7.6F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame(50 * frameUnit, 12.4F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame(53 * frameUnit, 12.6F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame(56 * frameUnit, 11.8F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame(59 * frameUnit, 8.5F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame(62 * frameUnit, 1.6F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((70 + 12) * frameUnit, -1, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((73 + 14) * frameUnit, -5.5F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((75 + 15) * frameUnit, -0.7F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((77 + 16) * frameUnit, 6.8F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((80 + 17) * frameUnit, -4.6F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((85 + x) * frameUnit, 20.7F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((95 + x) * frameUnit, 34.2F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((100 + x) * frameUnit, 45.6F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((110 + x) * frameUnit, 36.6F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((115 + x) * frameUnit, 38.4F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((124 + x) * frameUnit, 50, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((140 + x) * frameUnit, 45.3F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((154 + x) * frameUnit, 52.9F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((170 + x) * frameUnit, 25, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((171 + x) * frameUnit, -38F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((211 + x) * frameUnit, 0, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((251 + x) * frameUnit, 22F, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((291 + x) * frameUnit, 0, -5, 0, InterpType.LINEAR));
		leftAnkleRunCycle.add(new KeyFrame((331 + x) * frameUnit, -38F, -5, 0, InterpType.LINEAR));
		KeyFrame.toRadians(leftAnkleRunCycle);

		// Right ankle
		List<KeyFrame> rightAnkleFrames = new ArrayList<KeyFrame>(27);
		rightAnkleFrames.add(new KeyFrame(0, -0.4F, -5, 0, InterpType.LINEAR));
		rightAnkleFrames.add(new KeyFrame(1 * frameUnit, -0.4F, -5, 0, InterpType.LINEAR));
		rightAnkleFrames.add(new KeyFrame(37 * frameUnit, -0.4F, -5, 0, InterpType.LINEAR));
		KeyFrame.toRadians(rightAnkleFrames);
		List<KeyFrame> rightAnkleRunCycle = KeyFrame.cloneFrames(leftAnkleRunCycle);
		KeyFrame.mirrorFramesX(rightAnkleRunCycle);
		KeyFrame.offsetFramesCircular(rightAnkleRunCycle, runBegin, runEnd, (runEnd - runBegin) / 2);

		// Concat and set animation
		leftAnkleFrames.addAll(leftAnkleRunCycle);
		rightAnkleFrames.addAll(rightAnkleRunCycle);
		allKeyFrames.put(BonesBirdLegs.LEFT_METATARSOPHALANGEAL_ARTICULATIONS, leftAnkleFrames);
		allKeyFrames.put(BonesBirdLegs.RIGHT_METATARSOPHALANGEAL_ARTICULATIONS, rightAnkleFrames);
		
		// Left back claw
		List<KeyFrame> leftBackClawFrames = new ArrayList<KeyFrame>(21);
		leftBackClawFrames.add(new KeyFrame(0, 77F, 0, 0, InterpType.LINEAR));
		leftBackClawFrames.add(new KeyFrame((170 + x) * frameUnit, 77F, 0, 0, InterpType.LINEAR));
		leftBackClawFrames.add(new KeyFrame((171 + x) * frameUnit, 84F, 0, 0, InterpType.LINEAR));
		leftBackClawFrames.add(new KeyFrame((211 + x) * frameUnit, 77F, 0, 0, InterpType.LINEAR));
		leftBackClawFrames.add(new KeyFrame((251 + x) * frameUnit, -7.5F, 0, 0, InterpType.LINEAR));
		leftBackClawFrames.add(new KeyFrame((291 + x) * frameUnit, 77F, 0, 0, InterpType.LINEAR));
		leftBackClawFrames.add(new KeyFrame((331 + x) * frameUnit, 84F, 0, 0, InterpType.LINEAR));
		
		//Right back claw
		KeyFrame.toRadians(leftBackClawFrames);
		List<KeyFrame> rightBackClawFrames = KeyFrame.cloneFrames(leftBackClawFrames);
		KeyFrame.mirrorFramesX(rightBackClawFrames);
		
		allKeyFrames.put(BonesBirdLegs.LEFT_BACK_CLAW, leftBackClawFrames);
		allKeyFrames.put(BonesBirdLegs.RIGHT_BACK_CLAW, rightBackClawFrames);
		
		Animation<BonesBirdLegs> birdRun = new Animation<BonesBirdLegs>(BonesBirdLegs.class, 1.0F, 1.0F / 21.5F, allKeyFrames, animationPhases);
		AnimationRegistry.instance().registerAnimation("bird_run", birdRun);
		
		
		EnumMap<BonesWings, List<KeyFrame>> allKeyFramesWings = new EnumMap<BonesWings, List<KeyFrame>>(BonesWings.class);
		animationPhases = new ArrayList<AnimationPhaseInfo>(3);
		
		// Flap phase
		transitions = new HashMap<AnimationAction, Transition>(1);
		defaultTransition = new Transition(AnimationAction.WINGFLAP, 60F / 221F, 0);
		transitions.put(AnimationAction.WINGFLAP, defaultTransition);
		transitions.put(AnimationAction.WINGTUCK, new Transition(AnimationAction.WINGTUCK, 15F / 221F, 61F / 221F));
		transitions.put(AnimationAction.WINGGLIDE, new Transition(AnimationAction.WINGGLIDE, 15F / 221F, 181F / 221F));
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.WINGFLAP, 0, 60F / 221F, defaultTransition, transitions));
		
		// Retract wings
		transitions = new HashMap<AnimationAction, Transition>(1);
		defaultTransition = new Transition(AnimationAction.WINGSPREAD, 120F / 221F, 121F / 221F);
		transitions.put(AnimationAction.WINGSPREAD, defaultTransition);
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.WINGTUCK, 61F / 221F, 120F / 221F, defaultTransition, transitions));
		
		// Spread wings
		transitions = new HashMap<AnimationAction, Transition>(1);
		defaultTransition = new Transition(AnimationAction.WINGTUCK, 181F / 221F, 61F / 221F);
		transitions.put(AnimationAction.WINGTUCK, defaultTransition);
		transitions.put(AnimationAction.WINGFLAP, new Transition(AnimationAction.WINGFLAP, 181F / 221F, 15F / 221F));
		transitions.put(AnimationAction.WINGGLIDE, new Transition(AnimationAction.WINGGLIDE, 181F / 221F, 181F / 221F));
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.WINGSPREAD, 121F / 221F, 181F / 221F, defaultTransition, transitions));
		
		// Glide
		transitions = new HashMap<AnimationAction, Transition>(1);
		defaultTransition = new Transition(AnimationAction.WINGGLIDE, 221F / 221F, 181F / 221F);
		transitions.put(AnimationAction.WINGGLIDE, defaultTransition);
		transitions.put(AnimationAction.WINGFLAP, new Transition(AnimationAction.WINGFLAP, 221F / 221F, 15F / 221F));
		transitions.put(AnimationAction.WINGTUCK, new Transition(AnimationAction.WINGTUCK, 221F / 221F, 61F / 221F));
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.WINGGLIDE, 181F / 221F, 221F / 221F, defaultTransition, transitions));

		frameUnit = 1.0F / 221.0F;
		List<KeyFrame> rightInnerWingFrames = new ArrayList<KeyFrame>(12);
		rightInnerWingFrames.add(new KeyFrame(0, 2F, -48F, 0, 7, -8F, 6, InterpType.LINEAR));
		rightInnerWingFrames.add(new KeyFrame(5 * frameUnit, 4, -38, 0, InterpType.LINEAR));
		rightInnerWingFrames.add(new KeyFrame(10 * frameUnit, 5.5F, -27.5F, 0, InterpType.LINEAR));
		rightInnerWingFrames.add(new KeyFrame(15 * frameUnit, 5.5F, -7F, 0, InterpType.LINEAR));
		rightInnerWingFrames.add(new KeyFrame(20 * frameUnit, 5.5F, 15F, 0, InterpType.LINEAR));
		rightInnerWingFrames.add(new KeyFrame(25 * frameUnit, 4.5F, 30F, 0, InterpType.LINEAR));
		rightInnerWingFrames.add(new KeyFrame(30 * frameUnit, 2F, 38F, 9F, InterpType.LINEAR));
		rightInnerWingFrames.add(new KeyFrame(35 * frameUnit, 1F, 20F, 0, InterpType.LINEAR));
		rightInnerWingFrames.add(new KeyFrame(40 * frameUnit, 1F, 3.5F, 0, InterpType.LINEAR));
		rightInnerWingFrames.add(new KeyFrame(45 * frameUnit, 1F, -19F, 0, InterpType.LINEAR));
		rightInnerWingFrames.add(new KeyFrame(50 * frameUnit, -3F, -38F, 0, InterpType.LINEAR));
		rightInnerWingFrames.add(new KeyFrame(55 * frameUnit, -1F, -48F, 0, InterpType.LINEAR));
		rightInnerWingFrames.add(new KeyFrame(60 * frameUnit, 2F, -48F, 0, InterpType.LINEAR));
		rightInnerWingFrames.add(new KeyFrame(61 * frameUnit, 5.5F, -7F, 0F, 7F, -8F, 6F, InterpType.LINEAR));
		rightInnerWingFrames.add(new KeyFrame(121 * frameUnit, 0.71F, 88.6F, 0, 11F, -8F, 9F, InterpType.LINEAR));
		rightInnerWingFrames.add(new KeyFrame(181 * frameUnit, 5.5F, -7F, 0F, 7F, -8F, 6F, InterpType.LINEAR));
		rightInnerWingFrames.add(new KeyFrame(209 * frameUnit, 5.5F, -5F, 0F, InterpType.LINEAR));
		rightInnerWingFrames.add(new KeyFrame(221 * frameUnit, 5.5F, -7F, 0F, InterpType.LINEAR));
		
		KeyFrame.toRadians(rightInnerWingFrames);
		List<KeyFrame> leftInnerWingFrames = KeyFrame.cloneFrames(rightInnerWingFrames);
		KeyFrame.mirrorFramesX(leftInnerWingFrames);
		allKeyFramesWings.put(BonesWings.LEFT_SHOULDER, rightInnerWingFrames);
		allKeyFramesWings.put(BonesWings.RIGHT_SHOULDER, leftInnerWingFrames);
		
		List<KeyFrame> rightOuterWingFrames = new ArrayList<KeyFrame>(13);
		rightOuterWingFrames.add(new KeyFrame(0, 2F, 34.5F, 0, 23, 1, 0, InterpType.LINEAR));
		rightOuterWingFrames.add(new KeyFrame(5 * frameUnit, 5F, 13F, -7F, InterpType.LINEAR));
		rightOuterWingFrames.add(new KeyFrame(10 * frameUnit, 7F, 8.5F, -10F, InterpType.LINEAR));
		rightOuterWingFrames.add(new KeyFrame(15 * frameUnit, 7.5F, -2.5F, -10F, InterpType.LINEAR));
		rightOuterWingFrames.add(new KeyFrame(25 * frameUnit, 5F, 7F, -10F, InterpType.LINEAR));
		rightOuterWingFrames.add(new KeyFrame(30 * frameUnit, 2F, 15F, 0, InterpType.LINEAR));
		rightOuterWingFrames.add(new KeyFrame(35 * frameUnit, -3F, 37F, 12F, InterpType.LINEAR));
		rightOuterWingFrames.add(new KeyFrame(40 * frameUnit, -9F, 56F, 27F, InterpType.LINEAR));
		rightOuterWingFrames.add(new KeyFrame(45 * frameUnit, -13F, 68F, 28F, InterpType.LINEAR));
		rightOuterWingFrames.add(new KeyFrame(50 * frameUnit, -13.5F, 70F, 31.5F, InterpType.LINEAR));
		rightOuterWingFrames.add(new KeyFrame(53 * frameUnit, -9F, 71F, 31F, InterpType.LINEAR));
		rightOuterWingFrames.add(new KeyFrame(55 * frameUnit, -3.5F, 65.5F, 22F, InterpType.LINEAR));
		rightOuterWingFrames.add(new KeyFrame(58 * frameUnit, 0, 52F, 8F, InterpType.LINEAR));
		rightOuterWingFrames.add(new KeyFrame(60 * frameUnit, 2F, 34.5F, 0, InterpType.LINEAR));
		rightOuterWingFrames.add(new KeyFrame(61 * frameUnit, -5F, -2.5F, -10, 23, 1, 0, InterpType.LINEAR));
		rightOuterWingFrames.add(new KeyFrame(76 * frameUnit, 0, 0, 15, 22, 1, 0, InterpType.LINEAR));
		rightOuterWingFrames.add(new KeyFrame(101 * frameUnit, 0, 0, 83, 20.33F, 1, 0, InterpType.LINEAR));
		rightOuterWingFrames.add(new KeyFrame(121 * frameUnit, 0, 0, 90, 19, 1, 0, InterpType.LINEAR));
		rightOuterWingFrames.add(new KeyFrame(141 * frameUnit, 0, 0, 83, 20.33F, 1, 0, InterpType.LINEAR));
		rightOuterWingFrames.add(new KeyFrame(166 * frameUnit, 0, 0, 15, 22, 1, 0, InterpType.LINEAR));
		rightOuterWingFrames.add(new KeyFrame(181 * frameUnit, -5F, -2.5F, -10, 23, 1, 0, InterpType.LINEAR));
		rightOuterWingFrames.add(new KeyFrame(209 * frameUnit, -5F, -1.3F, -10, InterpType.LINEAR));
		rightOuterWingFrames.add(new KeyFrame(221 * frameUnit, -5F, -2.5F, -10, InterpType.LINEAR));
		KeyFrame.toRadians(rightOuterWingFrames);
		List<KeyFrame> leftOuterWingFrames = KeyFrame.cloneFrames(rightOuterWingFrames);
		KeyFrame.mirrorFramesX(leftOuterWingFrames);
		allKeyFramesWings.put(BonesWings.LEFT_ELBOW, rightOuterWingFrames);
		allKeyFramesWings.put(BonesWings.RIGHT_ELBOW, leftOuterWingFrames);
		
		Animation<BonesWings> wingFlap= new Animation<BonesWings>(BonesWings.class, 1.0F, 1F / 60F, allKeyFramesWings, animationPhases);
		AnimationRegistry.instance().registerAnimation("wing_flap_2_piece", wingFlap);
		
		
		
		// Beak
		EnumMap<BonesMouth, List<KeyFrame>> allKeyFramesBeak = new EnumMap<BonesMouth, List<KeyFrame>>(BonesMouth.class);
		animationPhases = new ArrayList<AnimationPhaseInfo>(3);
		
		// Open
		transitions = new HashMap<AnimationAction, Transition>(1);
		defaultTransition = new Transition(AnimationAction.MOUTH_CLOSE, 60F / 120F, 61F / 120F);
		transitions.put(AnimationAction.MOUTH_CLOSE, defaultTransition);
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.MOUTH_OPEN, 0, 60F / 120F, defaultTransition, transitions));
		
		// Close
		transitions = new HashMap<AnimationAction, Transition>(1);
		defaultTransition = new Transition(AnimationAction.MOUTH_OPEN, 120F / 120F, 0);
		transitions.put(AnimationAction.MOUTH_OPEN, defaultTransition);
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.MOUTH_CLOSE, 60F / 120F, 120F / 120F, defaultTransition, transitions));
		
		frameUnit = 1.0F / 120.0F;
		List<KeyFrame> upperBeakFrames = new ArrayList<KeyFrame>(3);
		upperBeakFrames.add(new KeyFrame(0 * frameUnit, 0, 0, 0, InterpType.LINEAR));
		upperBeakFrames.add(new KeyFrame(60 * frameUnit, -8F, 0, 0, InterpType.LINEAR));
		upperBeakFrames.add(new KeyFrame(120 * frameUnit, 0, 0, 0, InterpType.LINEAR));
		KeyFrame.toRadians(upperBeakFrames);
		allKeyFramesBeak.put(BonesMouth.UPPER_MOUTH, upperBeakFrames);
		
		List<KeyFrame> lowerBeakFrames = new ArrayList<KeyFrame>(3);
		lowerBeakFrames.add(new KeyFrame(0 * frameUnit, 0, 0, 0, InterpType.LINEAR));
		lowerBeakFrames.add(new KeyFrame(60 * frameUnit, 20F, 0, 0, InterpType.LINEAR));
		lowerBeakFrames.add(new KeyFrame(120 * frameUnit, 0, 0, 0, InterpType.LINEAR));
		KeyFrame.toRadians(lowerBeakFrames);
		allKeyFramesBeak.put(BonesMouth.LOWER_MOUTH, lowerBeakFrames);
		
		Animation<BonesMouth> beak = new Animation<BonesMouth>(BonesMouth.class, 1.0F, 1.0F / 10F, allKeyFramesBeak, animationPhases);
		AnimationRegistry.instance().registerAnimation("bird_beak", beak);
	}

	@Override
	public File getFile(String fileName)
	{
		return new File(FMLClientHandler.instance().getClient().getMinecraftDir().getPath() + fileName);
	}
}
