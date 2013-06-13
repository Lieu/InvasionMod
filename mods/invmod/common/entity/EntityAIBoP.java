package mods.invmod.common.entity;

import mods.invmod.common.entity.INavigationFlying.MoveType;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIBoP extends EntityAIBase
{
	private static final int PATIENCE = 500;
	private EntityIMFlying theEntity;
    private int timeWithGoal;
    private int timeWithTarget;
    private int patienceTime;
    private int lastHealth;
    private Goal lastGoal;
    private EntityLiving lastTarget;

    public EntityAIBoP(EntityIMFlying entity)
    {
        theEntity = entity;
        timeWithGoal = 0;
        patienceTime = 0;
        lastHealth = entity.getHealth();
        lastGoal = entity.getAIGoal();
        lastTarget = entity.getAttackTarget();
    }
	
	@Override
	public boolean shouldExecute()
	{
		return true;
	}
	
	@Override
	public void startExecuting()
    {
		timeWithGoal = 0;
		patienceTime = 0;
    }

    @Override
	public void updateTask()
    {
    	// Update some state info
    	timeWithGoal++;
    	if(theEntity.getAIGoal() != lastGoal)
    	{
    		lastGoal = theEntity.getAIGoal();
    		timeWithGoal = 0;
    	}
    	
    	timeWithTarget++;
    	if(theEntity.getAttackTarget() != lastTarget)
    	{
    		lastTarget = theEntity.getAttackTarget();
    		timeWithTarget = 0;
    	}
    	
    	// Handle state transitions if necessary
    	if(theEntity.getAttackTarget() == null)
    	{
    		if(theEntity.getNexus() != null)
    		{
    			if(theEntity.getAIGoal() != Goal.BREAK_NEXUS)
    				theEntity.transitionAIGoal(Goal.BREAK_NEXUS);
    		}
    		else
    		{
    			if(theEntity.getAIGoal() != Goal.CHILL)
    			{
    				theEntity.transitionAIGoal(Goal.CHILL);
    				theEntity.getNavigatorNew().clearPath();
    				theEntity.getNavigatorNew().setMovementType(MoveType.PREFER_WALKING);
    				theEntity.getNavigatorNew().setLandingPath();
    			}
    		}
    	}
    	else if(theEntity.getAIGoal() == Goal.CHILL || theEntity.getAIGoal() == Goal.NONE)
    	{
    		chooseTargetAction(theEntity.getAttackTarget());
    	}
    	
    	
    	if(theEntity.getAIGoal() == Goal.STAY_AT_RANGE)
    	{

    	}
    	else if(theEntity.getAIGoal() == Goal.MELEE_TARGET)
    	{
    		if(timeWithGoal > 600)
    		{
    			theEntity.transitionAIGoal(Goal.STAY_AT_RANGE);
    		}
    	}
    }
    
    protected void chooseTargetAction(EntityLiving target)
    {
    	if(theEntity.getMoveState() != MoveState.FLYING)
    	{
    		if(theEntity.getDistanceToEntity(target) < 10 && theEntity.worldObj.rand.nextFloat() > 0.3F)
    		{
    			theEntity.transitionAIGoal(Goal.MELEE_TARGET);
    			return;
    		}
    	}
    	theEntity.transitionAIGoal(Goal.STAY_AT_RANGE);
    }
}
