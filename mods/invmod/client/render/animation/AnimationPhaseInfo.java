package mods.invmod.client.render.animation;

import java.util.HashMap;
import java.util.Map;

public class AnimationPhaseInfo
{
	private AnimationAction action;
	private float timeBegin;
	private float timeEnd;
	private Map<AnimationAction, Transition> transitions;
	private Transition defaultTransition;
	
	public AnimationPhaseInfo(AnimationAction action, float timeBegin, float timeEnd, Transition defaultTransition)
	{
		this(action, timeBegin, timeEnd, defaultTransition, new HashMap<AnimationAction, Transition>(1));
		transitions.put(defaultTransition.getNewAction(), defaultTransition);
	}
	
	public AnimationPhaseInfo(AnimationAction action, float timeBegin, float timeEnd, Transition defaultTransition, Map<AnimationAction, Transition> transitions)
	{
		this.action = action;
		this.timeBegin = timeBegin;
		this.timeEnd = timeEnd;
		this.defaultTransition = defaultTransition;
		this.transitions = transitions;
	}
	
	public AnimationAction getAction()
	{
		return action;
	}
	
	public float getTimeBegin()
	{
		return timeBegin;
	}
	
	public float getTimeEnd()
	{
		return timeEnd;
	}
	
	public boolean hasTransition(AnimationAction newAction)
	{
		return transitions.containsKey(newAction);
	}
	
	public Transition getTransition(AnimationAction newAction)
	{
		return transitions.get(newAction);
	}
	
	public Transition getDefaultTransition()
	{
		return defaultTransition;
	}
}
