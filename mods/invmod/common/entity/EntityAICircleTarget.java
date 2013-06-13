package mods.invmod.common.entity;

import mods.invmod.common.entity.INavigationFlying.MoveType;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAICircleTarget extends EntityAIBase
{
	private static final int ATTACK_SEARCH_TIME = 400;
	private EntityIMFlying theEntity;
    private int time;
    private int patienceTime;
    private int patience;
    private float preferredHeight;
	private float preferredRadius;

    public EntityAICircleTarget(EntityIMFlying entity, int patience, float preferredHeight, float preferredRadius)
    {
        theEntity = entity;
        time = 0;
        patienceTime = 0;
        this.patience = patience;
        this.preferredHeight = preferredHeight;
        this.preferredRadius = preferredRadius;
    }
	
	@Override
	public boolean shouldExecute()
	{
		return theEntity.getAIGoal() == Goal.STAY_AT_RANGE && theEntity.getAttackTarget() != null;
	}

	@Override
	public boolean continueExecuting()
	{
		return (theEntity.getAIGoal() == Goal.STAY_AT_RANGE || isWaitingForTransition()) && theEntity.getAttackTarget() != null;
	}
	
	@Override
	public void startExecuting()
	{
		INavigationFlying nav = theEntity.getNavigatorNew();
		nav.setMovementType(MoveType.PREFER_FLYING);
		nav.setCirclingPath(theEntity.getAttackTarget(), preferredHeight, preferredRadius);
		time = 0;
		int extraTime = (int)(4 * nav.getDistanceToCirclingRadius());
		if(extraTime < 0)
			extraTime = 0;
		
		patienceTime = extraTime + theEntity.worldObj.rand.nextInt(patience) + patience / 3;
	}
	
	@Override
	public void updateTask()
	{
		time++;
		if(theEntity.getAIGoal() == Goal.STAY_AT_RANGE)
		{
			patienceTime--;
			if(patienceTime <= 0)
			{
				theEntity.transitionAIGoal(Goal.FIND_ATTACK_OPPORTUNITY);
				patienceTime = ATTACK_SEARCH_TIME;
			}
		}
		else if(isWaitingForTransition())
		{
			patienceTime--;
			if(patienceTime <= 0)
			{
				// Change target or do an approach
			}
		}
	}
	
	protected boolean isWaitingForTransition()
	{
		 return theEntity.getPrevAIGoal() == Goal.STAY_AT_RANGE && theEntity.getAIGoal() == Goal.FIND_ATTACK_OPPORTUNITY;
	}
}
