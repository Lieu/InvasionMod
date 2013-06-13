package mods.invmod.common.entity;

import mods.invmod.client.render.animation.AnimationAction;
import mods.invmod.client.render.animation.AnimationState;

public class LegController
{
	private EntityIMBird theEntity;
	private AnimationState animationRun;
	private int timeAttacking;
	private float flapEffort;
	private float[] flapEffortSamples;
	private int sampleIndex;
	
	public LegController(EntityIMBird entity, AnimationState stateObject)
	{
		theEntity = entity;
		animationRun = stateObject;
		timeAttacking = 0;
		flapEffort = 1.0F;
		flapEffortSamples = new float[] { 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F };
		sampleIndex = 0;
	}
	
	public void update()
	{
		AnimationAction currAnimation = animationRun.getCurrentAction();
		if(theEntity.getMoveState() == MoveState.RUNNING)
		{
			double dX = theEntity.posX - theEntity.lastTickPosX;
			double dZ = theEntity.posZ - theEntity.lastTickPosZ;
			double dist = Math.sqrt(dX*dX + dZ*dZ);
			float speed = 0.2F + (float)dist * 1.3F;
			
			if(animationRun.getNextSetAction() != AnimationAction.RUN)
			{
				if(dist >= 0.00001)
				{
					if(currAnimation == AnimationAction.STAND)
					{
						ensureAnimation(animationRun, AnimationAction.STAND_TO_RUN, speed, false);
					}
					else if(currAnimation == AnimationAction.STAND_TO_RUN)
					{
						ensureAnimation(animationRun, AnimationAction.RUN, speed, false);
					}
					else 
					{
						// Otherwise just revert to standing as a transition point
						ensureAnimation(animationRun, AnimationAction.STAND, 1.0F, true);
					}
				}
			}
			else
			{
				animationRun.setAnimationSpeed(speed);
				if(dist < 0.00001)
				{
					ensureAnimation(animationRun, AnimationAction.STAND, 0.2F, true);
				}
			}
		}
		else if(theEntity.getMoveState() == MoveState.STANDING)
		{
			ensureAnimation(animationRun, AnimationAction.STAND, 1.0F, true);
		}
		else if(theEntity.getMoveState() == MoveState.FLYING)
		{
			if(theEntity.getClawsForward())
			{
				if(currAnimation == AnimationAction.STAND)
				{
					ensureAnimation(animationRun, AnimationAction.LEGS_CLAW_ATTACK_P1, 1.5F, true);
				}
				else if(animationRun.getNextSetAction() != AnimationAction.LEGS_CLAW_ATTACK_P1)
				{
					ensureAnimation(animationRun, AnimationAction.STAND, 1.5F, true);
				}
			}
			else if(theEntity.getFlyState() == FlyState.FLYING && currAnimation != AnimationAction.LEGS_RETRACT)
			{
				if(currAnimation == AnimationAction.STAND)
				{
					ensureAnimation(animationRun, AnimationAction.LEGS_RETRACT, 1.0F, true);
				}
				else if(currAnimation == AnimationAction.LEGS_CLAW_ATTACK_P1)
				{
					ensureAnimation(animationRun, AnimationAction.LEGS_CLAW_ATTACK_P2, 1.0F, false);
				}
				else
				{
					ensureAnimation(animationRun, AnimationAction.STAND, 1.0F, true);
				}
			}
			
		}
		animationRun.update();
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
