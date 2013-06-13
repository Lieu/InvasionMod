package mods.invmod.client.render;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mods.invmod.client.render.animation.Animation;
import mods.invmod.client.render.animation.AnimationAction;
import mods.invmod.client.render.animation.AnimationPhaseInfo;
import mods.invmod.client.render.animation.BonesWings;
import mods.invmod.client.render.animation.KeyFrame;
import mods.invmod.client.render.animation.Transition;
import mods.invmod.common.mod_Invasion;

public class AnimationRegistry
{
	private static final AnimationRegistry instance;
	private Map<String, Animation> animationMap;
	private Animation emptyAnim;
	
	private AnimationRegistry()
	{
		animationMap = new HashMap<String, Animation>(4);
		EnumMap<BonesWings, List<KeyFrame>> allKeyFramesWings = new EnumMap<BonesWings, List<KeyFrame>>(BonesWings.class);
		List<AnimationPhaseInfo> animationPhases = new ArrayList<AnimationPhaseInfo>(1);
		animationPhases.add(new AnimationPhaseInfo(AnimationAction.STAND, 0, 1.0F, new Transition(AnimationAction.STAND, 1.0F, 0)));
		emptyAnim = new Animation<BonesWings>(BonesWings.class, 1.0F, 1.0F, allKeyFramesWings, animationPhases);
	}
	
	public void registerAnimation(String name, Animation animation)
	{
		if(!animationMap.containsKey(name))
		{
			animationMap.put(name, animation);
			return;
		}
		mod_Invasion.log("Register animation: Name \"" + name + "\" already assigned");
	}
	
	public Animation getAnimation(String name)
	{
		if(animationMap.containsKey(name))
		{
			return animationMap.get(name);
		}
		else
		{
			mod_Invasion.log("Tried to use animation \"" + name + "\" but it doesn't exist");
			return emptyAnim;
		}
	}
	
	public static AnimationRegistry instance()
	{
		return instance;
	}
	
	static
	{
		instance = new AnimationRegistry();
	}
}
