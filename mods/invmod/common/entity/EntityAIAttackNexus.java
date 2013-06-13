package mods.invmod.common.entity;

import mods.invmod.common.mod_Invasion;
import mods.invmod.common.nexus.INexusAccess;
import mods.invmod.common.nexus.TileEntityNexus;
import mods.invmod.common.util.CoordsInt;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIAttackNexus extends EntityAIBase
{
    private EntityIMLiving theEntity;
    private boolean attacked;

    public EntityAIAttackNexus(EntityIMLiving par1EntityLiving)
    {
        theEntity = par1EntityLiving;
        setMutexBits(3);
    }

    @Override
	public boolean shouldExecute()
    {
    	// Only do full check if close enough
    	if(theEntity.attackTime == 0 && theEntity.getAIGoal() == Goal.BREAK_NEXUS && theEntity.findDistanceToNexus() > 4.0)
    	{
    		theEntity.attackTime = 5;
    		return false;
    	}
    	
    	return isNexusInRange();
    }

    @Override
	public void startExecuting()
    {
    	theEntity.attackTime = 40;
    }
    
    @Override
	public boolean continueExecuting()
    {
    	return !attacked;// && theEntity.;
    }

    @Override
	public void updateTask()
    {
        if(theEntity.attackTime == 0)
        {
        	if(isNexusInRange())
        	{
        		theEntity.getNexus().attackNexus(2);
        	}
            attacked = true;
        }
    }
    
    @Override
	public void resetTask()
    {
    	attacked = false;
    }
    
    private boolean isNexusInRange()
    {
    	// Check adjacent blocks on the entity's block collision size, including top and bottom
    	CoordsInt size = theEntity.getCollideSize();
    	int x = theEntity.getXCoord();
    	int y = theEntity.getYCoord();
    	int z = theEntity.getZCoord();
    	for(int i = 0; i < size.getYCoord(); i++)
    	{
    		// Sides parallel to x axis
	    	for(int j = 0; j < size.getXCoord(); j++)
	    	{
	    		if(theEntity.worldObj.getBlockId(x + j, y, z - 1) == mod_Invasion.blockNexus.blockID)
	    		{
	    			if(isCorrectNexus(x + j, y, z - 1))
	    				return true;
	    		}
	    		
	    		if(theEntity.worldObj.getBlockId(x + j, y, z + 1 + size.getZCoord()) == mod_Invasion.blockNexus.blockID)
	    		{
	    			if(isCorrectNexus(x + j, y, z + 1 + size.getZCoord()))
	    				return true;
	    		}
	    	}
	    	
	    	// Sides parallel to z axis
	    	for(int j = 0; j < size.getZCoord(); j++)
	    	{
	    		if(theEntity.worldObj.getBlockId(x - 1, y, z + j) == mod_Invasion.blockNexus.blockID)
	    		{
	    			if(isCorrectNexus(x - 1, y, z + j))
	    				return true;
	    		}
	    		
	    		if(theEntity.worldObj.getBlockId(x + 1 + size.getXCoord(), y, z + j) == mod_Invasion.blockNexus.blockID)
	    		{
	    			if(isCorrectNexus(x + 1 + size.getXCoord(), y, z + j))
	    				return true;
	    		}
	    	}
    	}
    	
    	// Top and bottom
    	for(int i = 0; i < size.getXCoord(); i++)
    	{
    		for(int j = 0; j < size.getZCoord(); j++)
        	{
    			if(theEntity.worldObj.getBlockId(x + i, y + 1 + size.getYCoord(), z + j) == mod_Invasion.blockNexus.blockID)
	    		{
	    			if(isCorrectNexus(x + i, y + 1 + size.getYCoord(), z + j))
	    				return true;
	    		}
    			
    			if(theEntity.worldObj.getBlockId(x + i, y - 1, z + j) == mod_Invasion.blockNexus.blockID)
	    		{
	    			if(isCorrectNexus(x + i, y - 1, z + j))
	    				return true;
	    		}
        	}
    	}
    	
    	return false;
    }
    
    private boolean isCorrectNexus(int x, int y, int z)
    {
    	INexusAccess nexus = (TileEntityNexus)(theEntity.worldObj.getBlockTileEntity(x, y, z));
		if(nexus != null && nexus == theEntity.getNexus())
			return true;
		else
			return false;
    }
}