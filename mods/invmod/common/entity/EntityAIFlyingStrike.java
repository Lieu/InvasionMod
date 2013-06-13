package mods.invmod.common.entity;

import mods.invmod.common.util.MathUtil;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class EntityAIFlyingStrike extends EntityAIBase
{
	private EntityIMBird theEntity;

    public EntityAIFlyingStrike(EntityIMBird entity)
    {
        theEntity = entity;
    }
	
	@Override
	public boolean shouldExecute()
	{
		// Start executing before goal==FLYING_STRIKE because we need to take
		// control immediately, saving 1 tick of delay. The entity may be
		// moving very quickly.
		return theEntity.getAIGoal() == Goal.FLYING_STRIKE || theEntity.getAIGoal() == Goal.SWOOP;
	}
	
	@Override
	public boolean continueExecuting()
	{
		return shouldExecute();
	}
	
	@Override
	public void updateTask()
	{
		if(theEntity.getAIGoal() == Goal.FLYING_STRIKE)
			doStrike();
	}
	
	private void doStrike()
	{
		EntityLiving target = theEntity.getAttackTarget();
		if(target == null)
		{
			theEntity.transitionAIGoal(Goal.NONE);
			return;
		}
		
		
		float flyByChance = 1.0F;
		float tackleChance = 0.0F;
		float pickUpChance = 0.0F;
		if(theEntity.getClawsForward())
		{
			flyByChance = 0.5F;
			tackleChance = 100.0F;
			pickUpChance = 1.0F;
		}
		
		float pE = flyByChance + tackleChance + pickUpChance;
		float r = theEntity.worldObj.rand.nextFloat();
		if(r <= flyByChance / pE)
		{
			doFlyByAttack(target);
			theEntity.transitionAIGoal(Goal.STABILISE);
			theEntity.setClawsForward(false);
		}
		else if(r <= (flyByChance + tackleChance) / pE)
		{
			theEntity.transitionAIGoal(Goal.TACKLE_TARGET);
			theEntity.setClawsForward(false);
		}
		else
		{
			theEntity.transitionAIGoal(Goal.PICK_UP_TARGET);
		}
	}

    private void doFlyByAttack(EntityLiving entity)
    {
    	theEntity.attackEntityAsMob(entity, 5);
    }
}
