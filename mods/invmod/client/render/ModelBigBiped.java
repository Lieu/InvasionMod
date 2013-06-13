package mods.invmod.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelBigBiped extends ModelBase
{
	private ModelRenderer head;
	private ModelRenderer body;
	private ModelRenderer rightArm;
	private ModelRenderer leftArm;
	private ModelRenderer rightLeg;
	private ModelRenderer leftLeg;
	private ModelRenderer headwear;
	private int heldItemLeft;
	private int heldItemRight;
	private boolean isSneaking;
	private boolean aimedBow;

	public ModelBigBiped()
	{
		isSneaking = false;
		textureWidth = 64;
		textureHeight = 32;

		head = new ModelRenderer(this, 0, 0);
		head.addBox(-3.533333F, -7F, -3.5F, 7, 7, 7);
		head.setRotationPoint(0F, 0F, 0F);
		head.setTextureSize(64, 32);
		head.mirror = true;
		setRotation(head, 0F, 0F, 0F);
		body = new ModelRenderer(this, 16, 15);
		body.addBox(-5F, 0F, -3F, 10, 12, 5);
		body.setRotationPoint(0F, 0F, 0F);
		body.setTextureSize(64, 32);
		body.mirror = true;
		setRotation(body, 0F, 0F, 0F);
		rightArm = new ModelRenderer(this, 46, 15);
		rightArm.addBox(-3F, -2F, -2F, 4, 13, 4);
		rightArm.setRotationPoint(-6F, 2F, 0F);
		rightArm.setTextureSize(64, 32);
		rightArm.mirror = true;
		setRotation(rightArm, 0F, 0F, 0F);
		leftArm = new ModelRenderer(this, 46, 15);
		leftArm.addBox(-1F, -2F, -2F, 4, 13, 4);
		leftArm.setRotationPoint(6F, 2F, 0F);
		leftArm.setTextureSize(64, 32);
		leftArm.mirror = true;
		setRotation(leftArm, 0F, 0F, 0F);
		rightLeg = new ModelRenderer(this, 0, 16);
		rightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
		rightLeg.setRotationPoint(-2F, 12F, 0F);
		rightLeg.setTextureSize(64, 32);
		rightLeg.mirror = true;
		setRotation(rightLeg, 0F, 0F, 0F);
		leftLeg = new ModelRenderer(this, 0, 16);
		leftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
		leftLeg.setRotationPoint(2F, 12F, 0F);
		leftLeg.setTextureSize(64, 32);
		leftLeg.mirror = true;
		setRotation(leftLeg, 0F, 0F, 0F);
		
		headwear = new ModelRenderer(this, 32, 0);
        headwear.addBox(-3.533333F, -7F, -3.5F, 7, 7, 7, 0.5F);
        headwear.setRotationPoint(0.0F, 0.0F, 0.0F);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		head.render(f5);
		body.render(f5);
		rightArm.render(f5);
		leftArm.render(f5);
		rightLeg.render(f5);
		leftLeg.render(f5);
	}
	
	public void setSneaking(boolean flag)
	{
		isSneaking = flag;
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
	
	@Override
	public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity)
    {
        head.rotateAngleY = par4 / (180F / (float)Math.PI);
        head.rotateAngleX = par5 / (180F / (float)Math.PI);
        headwear.rotateAngleY = head.rotateAngleY;
        headwear.rotateAngleX = head.rotateAngleX;
        rightArm.rotateAngleX = MathHelper.cos(par1 * 0.6662F + (float)Math.PI) * 2.0F * par2 * 0.5F;
        leftArm.rotateAngleX = MathHelper.cos(par1 * 0.6662F) * 2.0F * par2 * 0.5F;
        rightArm.rotateAngleZ = 0.0F;
        leftArm.rotateAngleZ = 0.0F;
        rightLeg.rotateAngleX = MathHelper.cos(par1 * 0.6662F) * 1.4F * par2;
        leftLeg.rotateAngleX = MathHelper.cos(par1 * 0.6662F + (float)Math.PI) * 1.4F * par2;
        rightLeg.rotateAngleY = 0.0F;
        leftLeg.rotateAngleY = 0.0F;

        if (isRiding)
        {
            rightArm.rotateAngleX += -((float)Math.PI / 5F);
            leftArm.rotateAngleX += -((float)Math.PI / 5F);
            rightLeg.rotateAngleX = -((float)Math.PI * 2F / 5F);
            leftLeg.rotateAngleX = -((float)Math.PI * 2F / 5F);
            rightLeg.rotateAngleY = ((float)Math.PI / 10F);
            leftLeg.rotateAngleY = -((float)Math.PI / 10F);
        }

        if (heldItemLeft != 0)
        {
            leftArm.rotateAngleX = leftArm.rotateAngleX * 0.5F - ((float)Math.PI / 10F) * heldItemLeft;
        }

        if (heldItemRight != 0)
        {
            rightArm.rotateAngleX = rightArm.rotateAngleX * 0.5F - ((float)Math.PI / 10F) * heldItemRight;
        }

        rightArm.rotateAngleY = 0.0F;
        leftArm.rotateAngleY = 0.0F;

        if (onGround > -9990F)
        {
            float f = onGround;
            body.rotateAngleY = MathHelper.sin(MathHelper.sqrt_float(f) * (float)Math.PI * 2.0F) * 0.2F;
            rightArm.rotationPointZ = MathHelper.sin(body.rotateAngleY) * 5F;
            rightArm.rotationPointX = -MathHelper.cos(body.rotateAngleY) * 5F;
            leftArm.rotationPointZ = -MathHelper.sin(body.rotateAngleY) * 5F;
            leftArm.rotationPointX = MathHelper.cos(body.rotateAngleY) * 5F;
            rightArm.rotateAngleY += body.rotateAngleY;
            leftArm.rotateAngleY += body.rotateAngleY;
            leftArm.rotateAngleX += body.rotateAngleY;
            f = 1.0F - onGround;
            f *= f;
            f *= f;
            f = 1.0F - f;
            float f2 = MathHelper.sin(f * (float)Math.PI);
            float f4 = MathHelper.sin(onGround * (float)Math.PI) * -(head.rotateAngleX - 0.7F) * 0.75F;
            rightArm.rotateAngleX -= f2 * 1.2D + f4;
            rightArm.rotateAngleY += body.rotateAngleY * 2.0F;
            rightArm.rotateAngleZ = MathHelper.sin(onGround * (float)Math.PI) * -0.4F;
        }

        
        if (isSneaking)
        {
            body.rotateAngleX = 0.7F;
            body.rotationPointY = 1.5F;
            rightLeg.rotateAngleX -= 0.0F;
            leftLeg.rotateAngleX -= 0.0F;
            rightArm.rotateAngleX += 0.4F;
            leftArm.rotateAngleX += 0.4F;
            rightLeg.rotationPointZ = 7F;
            leftLeg.rotationPointZ = 7F;
            rightLeg.rotationPointY = 12F;
            leftLeg.rotationPointY = 12F;
            rightArm.rotationPointY = 3.5F;
            leftArm.rotationPointY = 3.5F;
            head.rotationPointY = 3.0F;
        }
        else
        {
            body.rotateAngleX = 0.0F;
            body.rotationPointY = 0F;
            rightLeg.rotationPointZ = 0.0F;
            leftLeg.rotationPointZ = 0.0F;
            rightLeg.rotationPointY = 12F;
            leftLeg.rotationPointY = 12F;
            rightArm.rotationPointY = 2F;
            leftArm.rotationPointY = 2F;
            head.rotationPointY = 0.0F;
            rightArm.rotationPointX = -6F;
            leftArm.rotationPointX = 6F;
        }

        rightArm.rotateAngleZ += MathHelper.cos(par3 * 0.09F) * 0.05F + 0.05F;
        leftArm.rotateAngleZ -= MathHelper.cos(par3 * 0.09F) * 0.05F + 0.05F;
        rightArm.rotateAngleX += MathHelper.sin(par3 * 0.067F) * 0.05F;
        leftArm.rotateAngleX -= MathHelper.sin(par3 * 0.067F) * 0.05F;

        if (aimedBow)
        {
            float f1 = 0.0F;
            float f3 = 0.0F;
            rightArm.rotateAngleZ = 0.0F;
            leftArm.rotateAngleZ = 0.0F;
            rightArm.rotateAngleY = -(0.1F - f1 * 0.6F) + head.rotateAngleY;
            leftArm.rotateAngleY = (0.1F - f1 * 0.6F) + head.rotateAngleY + 0.4F;
            rightArm.rotateAngleX = -((float)Math.PI / 2F) + head.rotateAngleX;
            leftArm.rotateAngleX = -((float)Math.PI / 2F) + head.rotateAngleX;
            rightArm.rotateAngleX -= f1 * 1.2F - f3 * 0.4F;
            leftArm.rotateAngleX -= f1 * 1.2F - f3 * 0.4F;
            rightArm.rotateAngleZ += MathHelper.cos(par3 * 0.09F) * 0.05F + 0.05F;
            leftArm.rotateAngleZ -= MathHelper.cos(par3 * 0.09F) * 0.05F + 0.05F;
            rightArm.rotateAngleX += MathHelper.sin(par3 * 0.067F) * 0.05F;
            leftArm.rotateAngleX -= MathHelper.sin(par3 * 0.067F) * 0.05F;
        }
    }
	
	public void itemArmPostRender(float scale)
	{
		rightArm.postRender(scale);
	}

}
