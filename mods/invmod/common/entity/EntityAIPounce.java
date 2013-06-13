package mods.invmod.common.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.MathHelper;

public class EntityAIPounce extends EntityAIBase
{
	private EntityIMSpider theEntity;
	private boolean isPouncing;
	private int pounceTimer;
	private int cooldown;
	private float minPower;
	private float maxPower;
	
	public EntityAIPounce(EntityIMSpider entity, float minPower, float maxPower, int cooldown)
	{
		theEntity = entity;
		isPouncing = false;
		this.minPower = minPower;
		this.maxPower = maxPower;
		this.cooldown = cooldown;
	}

	@Override
	public boolean shouldExecute()
	{
		EntityLiving target = theEntity.getAttackTarget();
		if(--pounceTimer <= 0 && target != null && theEntity.canEntityBeSeen(target) && theEntity.onGround)
		{
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean continueExecuting()
	{
		return isPouncing;
	}
	
	@Override
	public void startExecuting()
    {
		EntityLiving target = theEntity.getAttackTarget();
		if(pounce(target.posX, target.posY, target.posZ))
		{
			theEntity.setAirborneTime(0);
			isPouncing = true;
			theEntity.getNavigatorNew().haltForTick();
		}
		else
		{
			isPouncing = false;
		}
    }

	@Override
	public void updateTask()
	{
		theEntity.getNavigatorNew().haltForTick();
		int airborneTime = theEntity.getAirborneTime();
		if(airborneTime > 20 && theEntity.onGround)
		{
			isPouncing = false;
			pounceTimer = cooldown;
			theEntity.setAirborneTime(0);
			theEntity.getNavigatorNew().clearPath();
		}
		else
		{
			theEntity.setAirborneTime(airborneTime + 1);
		}
	}
	
	protected boolean pounce(double x, double y, double z)
	{
		double dX = x - theEntity.posX;
        double dY = y - theEntity.posY;
        double dZ = z - theEntity.posZ;
        double dXZ = MathHelper.sqrt_double(dX * dX + dZ * dZ);
        double a = Math.atan(dY / dXZ);
        if(a > (0 - Math.PI / 4) && a < Math.PI / 4)
        {
	        double rratio = (1 - Math.tan(a)) * (1 / Math.cos(a));
	        double r = dXZ / rratio;
	        double v = 1 / (Math.sqrt((1 / theEntity.getGravity()) / r));	        
	        if(v > minPower && v < maxPower)
	        {              
	        	double distance = MathHelper.sqrt_double(2 * (dXZ * dXZ));
	        	theEntity.motionX = v * dX / distance;
	        	theEntity.motionY = v * dXZ / distance;
	        	theEntity.motionZ = v * dZ / distance;
	        	return true;
	        }
        }
        return false;
	}
}
