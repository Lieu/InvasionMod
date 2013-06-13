package mods.invmod.common.entity;

import mods.invmod.common.util.IPosition;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIWanderIM extends EntityAIBase
{
	private static final int MIN_HORIZONTAL_PATH = 1;
	private static final int MAX_HORIZONTAL_PATH = 6;
	private static final int MAX_VERTICAL_PATH = 4;
	
	private EntityIMLiving theEntity;
    private IPosition movePosition;
    private float moveSpeed;

    public EntityAIWanderIM(EntityIMLiving entity, float moveSpeed)
    {
        theEntity = entity;
        this.moveSpeed = moveSpeed;
        setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
	public boolean shouldExecute()
    {
        /*if (theEntity.getAge() >= 100)
        {
            return false;
        }*/

        if (theEntity.getRNG().nextInt(120) == 0) // 15% per second
        {
	        int x = theEntity.getXCoord() + theEntity.getRNG().nextInt(1 + MAX_HORIZONTAL_PATH * 2) - MAX_HORIZONTAL_PATH;
	    	int z = theEntity.getZCoord() + theEntity.getRNG().nextInt(1 + MAX_HORIZONTAL_PATH * 2) - MAX_HORIZONTAL_PATH;
	    	Path path = theEntity.getNavigatorNew().getPathTowardsXZ(x, z, MIN_HORIZONTAL_PATH, MAX_HORIZONTAL_PATH, MAX_VERTICAL_PATH);
	    	if(path != null)
	    	{
	    		theEntity.getNavigatorNew().setPath(path, moveSpeed);
	    		return true;
	    	}
        }
    	
    	return false;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
	public boolean continueExecuting()
    {
        return !theEntity.getNavigatorNew().noPath() && theEntity.getNavigatorNew().getStuckTime() < 40;
    }
}
