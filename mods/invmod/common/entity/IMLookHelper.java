package mods.invmod.common.entity;

import mods.invmod.common.util.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.util.MathHelper;

/**
 * Replacement for EntityLookHelper because it isn't extendable
 */
public class IMLookHelper extends EntityLookHelper
{
	private final EntityIMLiving entity;
	private float deltaLookYaw;
	private float deltaLookPitch;
	private boolean isLooking = false;
	private double posX;
	private double posY;
	private double posZ;

	public IMLookHelper(EntityIMLiving entity)
	{
		super(entity);
		this.entity = entity;
	}


	/**
	 * Sets position to look at using entity
	 */
	public void setLookPositionWithEntity(Entity par1Entity, float par2, float par3)
	{
		posX = par1Entity.posX;

		if (par1Entity instanceof EntityLiving)
		{
			posY = par1Entity.posY + (double)par1Entity.getEyeHeight();
		}
		else
		{
			posY = (par1Entity.boundingBox.minY + par1Entity.boundingBox.maxY) / 2.0D;
		}

		posZ = par1Entity.posZ;
		deltaLookYaw = par2;
		deltaLookPitch = par3;
		isLooking = true;
	}

	/**
	 * Sets position to look at
	 */
	public void setLookPosition(double par1, double par3, double par5, float par7, float par8)
	{
		posX = par1;
		posY = par3;
		posZ = par5;
		deltaLookYaw = par7;
		deltaLookPitch = par8;
		isLooking = true;
	}

	/**
	 * Updates look
	 */
	public void onUpdateLook()
	{
		if (isLooking)
		{
			isLooking = false;
			double d0 = posX - entity.posX;
			double d1 = posY - (entity.posY + (double)entity.getEyeHeight());
			double d2 = posZ - entity.posZ;
			double d3 = (double)MathHelper.sqrt_double(d0 * d0 + d2 * d2);
			float yaw = (float)MathUtil.boundAngle180Deg(entity.rotationYaw);
			float pitch = (float)MathUtil.boundAngle180Deg(entity.rotationPitch);
			float yawHeadOffset = (float)(Math.atan2(d2, d0) * 180.0D / Math.PI) - 90.0F - yaw;
			float pitchHeadOffset = (float)((Math.atan2(d1, d3) * 180.0D / Math.PI) + 40F - pitch);
			float f2 = (float)MathUtil.boundAngle180Deg(yawHeadOffset);
			float yawFinal;
			if(f2 > 100 || f2 < -100F)
				yawFinal = 0;
			else
				yawFinal = f2 / 6;
			
			//yawFinal = 0;
			
			entity.setRotationPitchHead(updateRotation(entity.getRotationPitchHead(), pitchHeadOffset, deltaLookPitch));
			entity.setRotationYawHeadIM(updateRotation(entity.getRotationYawHeadIM(), yawFinal, deltaLookYaw));
			
			//System.out.println(yawFinal + ",| " + entity.getRotationYawHeadIM() + ", " + f2);
			//System.out.println(pitchHead + ",| " + entity.getRotationPitchHead());
		}
		else
		{
			//entity.rotationYawHead = updateRotation(entity.rotationYawHead, entity.renderYawOffset, 10.0F);
			//entity.setRotationPitchHead(15F);
		}

		/*float f2 = MathHelper.wrapAngleTo180_float(entity.rotationYawHead - entity.renderYawOffset);

		if (!entity.getNavigator().noPath())
		{
			if (f2 < -75.0F)
			{
				entity.rotationYawHead = entity.renderYawOffset - 75.0F;
			}

			if (f2 > 75.0F)
			{
				entity.rotationYawHead = entity.renderYawOffset + 75.0F;
			}
		}*/
	}

	private float updateRotation(float par1, float par2, float par3)
	{
		float f3 = MathHelper.wrapAngleTo180_float(par2 - par1);

		if (f3 > par3)
		{
			f3 = par3;
		}

		if (f3 < -par3)
		{
			f3 = -par3;
		}

		return par1 + f3;
	}
}
