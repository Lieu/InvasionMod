package mods.invmod.client.render.animation;

import java.util.List;

import mods.invmod.common.util.Pair;

public class AnimationState<T extends Enum<T>>
{
	private Animation<T> animation;
	private float currentTime;
	private float animationSpeed;
	private boolean pauseAtTransition;
	private boolean pauseAfterSetAction;
	private boolean isPaused;
	private AnimationPhaseInfo currentPhase;
	private Transition nextTransition;
	private AnimationAction setAction;
	
	public AnimationState(Animation<T> animation)
	{
		this(animation, 0);
	}
	
	public AnimationState(Animation<T> animation, float startTime)
	{
		this.animation = animation;
		pauseAtTransition = false;
		pauseAfterSetAction = false;
		isPaused = false;
		currentTime = startTime;
		animationSpeed = animation.getBaseSpeed();
		updatePhase(currentTime);
		nextTransition = currentPhase.getDefaultTransition();
		setAction = nextTransition.getNewAction();
	}
	
	public void setNewAction(AnimationAction action)
	{
		setAction = action;
		updateTransition(action);
		pauseAtTransition = false;
		pauseAfterSetAction = false;
		isPaused = false;
	}
	
	public void setNewAction(AnimationAction action, float animationSpeedFactor, boolean pauseAfterAction)
	{
		setNewAction(action);
		setAnimationSpeed(animationSpeedFactor);
		setPauseAfterSetAction(pauseAfterAction);
	}
	
	public void setPauseAfterCurrentAction(boolean shouldPause)
	{
		pauseAtTransition = shouldPause;
	}
	
	public void setPauseAfterSetAction(boolean shouldPause)
	{
		pauseAfterSetAction = shouldPause;
	}
	
	public void setPaused(boolean isPaused)
	{
		this.isPaused = isPaused;
	}
	
	public void update()
	{
		if(isPaused)
			return;
		
		currentTime += animationSpeed;
		if(currentTime >= nextTransition.getSourceTime())
		{
			if(setAction == currentPhase.getAction() && pauseAfterSetAction)
			{
				pauseAfterSetAction = false;
				pauseAtTransition = true;
			}
			
			if(!pauseAtTransition)
			{
				float overflow = currentTime - nextTransition.getSourceTime();
				currentTime = nextTransition.getDestTime();
				updatePhase(currentTime);
				float phaseLength = currentPhase.getTimeEnd() - currentPhase.getTimeBegin();
				if(overflow > phaseLength)
					overflow = phaseLength;
				
				updateTransition(setAction);
				currentTime += overflow;
				isPaused = false;
			}
			else
			{
				currentTime = nextTransition.getSourceTime();
				isPaused = true;
			}
		}
	}
	
	public AnimationAction getNextSetAction()
	{
		return setAction;
	}
	
	public AnimationAction getCurrentAction()
	{
		return currentPhase.getAction();
	}
	
	public float getCurrentAnimationTime()
	{
		return currentTime;
	}
	
	public float getCurrentAnimationTimeInterp(float parTick)
	{
		if(isPaused)
			parTick = 0;
		
		float frameTime = currentTime + parTick * animationSpeed;
		if(frameTime < nextTransition.getSourceTime())
		{
			return frameTime;
		}
		else
		{
			float overFlow = frameTime - nextTransition.getSourceTime();
			float phaseLength = currentPhase.getTimeEnd() - currentPhase.getTimeBegin();
			if(overFlow > phaseLength)
				overFlow = phaseLength;
			
			return nextTransition.getDestTime() + overFlow;
		}
	}
	
	public float getCurrentAnimationPercent()
	{
		return (currentTime - currentPhase.getTimeBegin()) / (currentPhase.getTimeEnd() - currentPhase.getTimeBegin());
	}
	
	public float getAnimationSpeed()
	{
		return animationSpeed;
	}
	
	public Transition getNextTransition()
	{
		return nextTransition;
	}
	
	public float getAnimationPeriod()
	{
		return animation.getAnimationPeriod();
	}
	
	public float getBaseAnimationTime()
	{
		return animation.getBaseSpeed();
	}
	
	public List<AnimationPhaseInfo> getAnimationPhases()
	{
		return animation.getAnimationPhases();
	}
	
	public void setAnimationSpeed(float speedFactor)
	{
		animationSpeed = animation.getBaseSpeed() * speedFactor;
	}
	
	private boolean updateTransition(AnimationAction action)
	{
		if(currentPhase.hasTransition(action))
		{
			nextTransition = currentPhase.getTransition(action);
			if(currentTime > nextTransition.getSourceTime()) // Missed the opportunity
			{
				nextTransition = currentPhase.getDefaultTransition();
				return false;
			}
		}
		else
		{
			nextTransition = currentPhase.getDefaultTransition();
		}
		return true;
	}
	
	private void updatePhase(float time)
	{
		currentPhase = findPhase(time);
		if(currentPhase == null)
		{
			currentTime = 0;
			currentPhase = animation.getAnimationPhases().get(0);
		}
	}
	
	private AnimationPhaseInfo findPhase(float time)
	{
		for(AnimationPhaseInfo phase : animation.getAnimationPhases())
		{
			if(phase.getTimeBegin() <= time && phase.getTimeEnd() > time)
				return phase;
		}
		return null;
	}
}
