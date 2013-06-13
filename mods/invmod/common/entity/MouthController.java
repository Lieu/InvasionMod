package mods.invmod.common.entity;

import mods.invmod.client.render.animation.AnimationAction;
import mods.invmod.client.render.animation.AnimationState;

public class MouthController
{
	private EntityIMLiving theEntity;
	private AnimationState mouthState;
	private int mouthOpenTime;
	
	public MouthController(EntityIMBird entity, AnimationState stateObject)
	{
		theEntity = entity;
		mouthState = stateObject;
		mouthOpenTime = 0;
	}
	
	public void update()
	{
		if(mouthOpenTime > 0)
		{
			mouthOpenTime--;
			ensureAnimation(mouthState, AnimationAction.MOUTH_OPEN, 1.0F, true);
		}
		else
		{
			ensureAnimation(mouthState, AnimationAction.MOUTH_CLOSE, 1.0F, true);
		}
		mouthState.update();
	}
	
	/**
	 * Sets the entity's mouth to open or close. The mouth will open for the
	 * time given, from present. If that time is 0 or less it indicates the
	 * mouth is set to be closed.
	 */
	public void setMouthState(int timeOpen)
	{
		mouthOpenTime = timeOpen;
	}
	
	private void ensureAnimation(AnimationState state, AnimationAction action, float animationSpeed, boolean pauseAfterAction)
	{
		if(state.getNextSetAction() != action)
		{
			state.setNewAction(action, animationSpeed, pauseAfterAction);
		}
		else
		{
			state.setAnimationSpeed(animationSpeed);
			state.setPauseAfterSetAction(pauseAfterAction);
			state.setPaused(false);
		}
	}
}
