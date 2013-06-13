package mods.invmod.common.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;

/**
 * Makes a NavigatorIM object partially compatible with a PathNavigate object.
 * Setting paths and updating is preserved, but otherwise setting or getting
 * internal state is not, such as getting PathEntity objects.
 * 
 * This adapter exists to ease base minecraft classes, especially AI, accessing
 * the invasion navigator.
 * 
 * @author Lieu
 */
public class PathNavigateAdapter extends PathNavigate
{
	private NavigatorIM navigator;
	
	public PathNavigateAdapter(NavigatorIM navigator)
	{
		super(navigator.getEntity(), navigator.getEntity().worldObj, 0);
		this.navigator = navigator;
	}
	
	@Override
	public void onUpdateNavigation()
    {
		navigator.onUpdateNavigation();
    }
	
	@Override
	public boolean noPath()
	{
		return navigator.noPath();
	}
	
	@Override
	public void clearPathEntity()
	{
		navigator.clearPath();
	}
	
	/**
     * Sets the move speed
     */
	@Override
	public void setSpeed(float speed)
    {
        navigator.setSpeed(speed);
    }

    /**
     * Try to find and set a path to XYZ. Returns true if successful.
     */
    @Override
	public boolean tryMoveToXYZ(double x, double y, double z, float movespeed)
    {
        return navigator.tryMoveToXYZ(x, y, z, movespeed);
    }
	
    /**
     * Try to find and set a path to EntityLiving. Returns true if successful.
     */
    @Override
	public boolean tryMoveToEntityLiving(EntityLiving entity, float movespeed)
    {
    	return navigator.tryMoveToEntity(entity, movespeed);
    }
    
    /**
     * Sets the active path if valid
     */
    public boolean setPath(Path entity, float movespeed)
    {
    	return navigator.setPath(entity, movespeed);
    }
    
    // -------------------------------------------------------------------------- //
    
    /**
     * Masked adapter method. Does nothing.
     */
    @Override
	public boolean setPath(PathEntity entity, float movespeed)
    {
    	return false;
    }
    
    /**
     * Masked adapter method. Does nothing.
     */
    @Override
	public PathEntity getPathToXYZ(double x, double y, double z)
    {
        return null;
    }
	
	/**
     * Masked adapter method. Does nothing.
     */
	@Override
	public void setAvoidsWater(boolean par1)
    {
    }

	/**
     * Masked adapter method. Does nothing.
     */
    @Override
	public boolean getAvoidsWater()
    {
        return false;
    }

    /**
     * Masked adapter method. Does nothing.
     */
    @Override
	public void setBreakDoors(boolean par1)
    {
    }

    /**
     * Masked adapter method. Does nothing.
     */
    @Override
	public void setEnterDoors(boolean par1)
    {
    }

    /**
     * Masked adapter method. Does nothing.
     */
    @Override
	public boolean getCanBreakDoors()
    {
        return false;
    }

    /**
     * Masked adapter method. Does nothing.
     */
    @Override
	public void setAvoidSun(boolean par1)
    {
    }

    /**
     * Masked adapter method. Does nothing.
     */
    @Override
	public void setCanSwim(boolean par1)
    {
    }

    /**
     * Masked adapter method. Does nothing.
     */
    @Override
	public PathEntity getPathToEntityLiving(EntityLiving par1EntityLiving)
    {
    	return null;
    }

    /**
     * Masked adapter method. Does nothing.
     */
    @Override
	public PathEntity getPath()
    {
        return null;
    }
}
