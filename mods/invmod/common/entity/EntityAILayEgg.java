package mods.invmod.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAILayEgg extends EntityAIBase
{
    public EntityAILayEgg(EntityIMLiving entity, int eggs)
    {
        theEntity = entity;
        eggCount = eggs;
        isLaying = false;
    }
    
    public void addEggs(int eggs)
    {
    	eggCount += eggs;
    }
    
    @Override
	public boolean shouldExecute()
    {
    	if(theEntity.getAIGoal() == Goal.TARGET_ENTITY && eggCount > 0 && theEntity.getEntitySenses().canSee(theEntity.getAttackTarget()))
    		return true;
    	
    	return false;
    }
    
    @Override
	public void startExecuting()
    {
    	time = INITIAL_EGG_DELAY;
    }
    
    @Override
	public void updateTask()
    {
    	time--;
    	if(time <= 0)
    	{
    		if(!isLaying)
    		{
    			isLaying = true;
    			time = EGG_LAY_TIME;
    			setMutexBits(1); // Tries to stop moving
    		}
    		else
    		{
    			isLaying = false;
    			eggCount--;
    			time = NEXT_EGG_DELAY;
    			setMutexBits(0);
    			layEgg();
    		}
    	}
    }
    
    private void layEgg()
    {
    	Entity[] contents;
    	if(theEntity instanceof ISpawnsOffspring)
    		contents = ((ISpawnsOffspring)theEntity).getOffspring(null);
    	else
    		contents = null;
    	
    	theEntity.worldObj.spawnEntityInWorld(new EntityIMEgg(theEntity, contents, EGG_HATCH_TIME, EGG_HP));
    }
    
    private static final int EGG_LAY_TIME = 45;
    private static final int INITIAL_EGG_DELAY = 25;
    private static final int NEXT_EGG_DELAY = 230;
    private static final int EGG_HATCH_TIME = 125;
    private static final int EGG_HP = 12;
    
    private EntityIMLiving theEntity;
	private int time;
	private boolean isLaying;
	private int eggCount;
}
