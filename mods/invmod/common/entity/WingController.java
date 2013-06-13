package mods.invmod.common.entity;

import mods.invmod.client.render.animation.AnimationAction;
import mods.invmod.client.render.animation.AnimationState;

public class WingController
{
	private EntityIMBird theEntity;
	private AnimationState animationFlap;
	private int timeAttacking;
	private float flapEffort;
	private float[] flapEffortSamples;
	private int sampleIndex;
	
	public WingController(EntityIMBird entity, AnimationState stateObject)
	{
		theEntity = entity;
		animationFlap = stateObject;
		timeAttacking = 0;
		flapEffort = 1.0F;
		flapEffortSamples = new float[] { 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F };
		sampleIndex = 0;
	}
	
	public void update()
	{
		AnimationAction currAnimation = animationFlap.getCurrentAction();
		AnimationAction nextAnimation = animationFlap.getNextSetAction();
		boolean wingAttack = theEntity.isAttackingWithWings();
		if(!wingAttack)
			timeAttacking = 0;
		else
			timeAttacking++;
		
		if(theEntity.ticksExisted % 5 == 0)
		{
			if(++sampleIndex >= flapEffortSamples.length)
				sampleIndex = 0;
			
			float sample = theEntity.getThrustEffort();
			flapEffort -= flapEffortSamples[sampleIndex] / flapEffortSamples.length;
			flapEffort += sample / flapEffortSamples.length;
			flapEffortSamples[sampleIndex] = sample;
		}
		
		if(theEntity.getFlyState() != FlyState.GROUNDED)
		{
			if(currAnimation == AnimationAction.WINGTUCK)
			{
				ensureAnimation(animationFlap, AnimationAction.WINGSPREAD, 2.2F, true);
			}
			else
			{
				if(theEntity.isThrustOn())
				{
					ensureAnimation(animationFlap, AnimationAction.WINGFLAP, 2.0F * flapEffort, false);
				}
				else
				{
					ensureAnimation(animationFlap, AnimationAction.WINGGLIDE, 0.7F, false);
				}
			}
		}
		else
		{
			boolean wingsActive = false;
			if(theEntity.getMoveState() == MoveState.RUNNING)
			{
				if(currAnimation == AnimationAction.WINGTUCK)
				{
					ensureAnimation(animationFlap, AnimationAction.WINGSPREAD, 2.2F, true);
				}
				else
				{
					ensureAnimation(animationFlap, AnimationAction.WINGFLAP, 1.0F, false);
					if(!wingAttack && currAnimation == AnimationAction.WINGSPREAD && animationFlap.getCurrentAnimationPercent() >= 0.65F)
					{
						animationFlap.setPaused(true);
					}
				}
				wingsActive = true;
			}
			
			if(wingAttack)
			{
				float speed = (float)(1.0 / Math.min(timeAttacking / 40 * 0.6 + 0.4, 1.0));
				ensureAnimation(animationFlap, AnimationAction.WINGFLAP, speed, false);
				wingsActive = true;
			}
			
			if(!wingsActive)
			{
				ensureAnimation(animationFlap, AnimationAction.WINGTUCK, 1.8F, true);
			}
		}
		animationFlap.update();
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
