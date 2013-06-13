
package mods.invmod.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelThrower extends ModelBase
{

 public ModelThrower()
 {
     this(0.0F);
 }

 public ModelThrower(float f)
 {
     this(f, 0.0F);
 }

 public ModelThrower(float f, float f1)
 {
     heldItemLeft = false;
     heldItemRight = false;
     isSneak = false;
     
     bipedHead = new ModelRenderer(this, 16, 14);
		bipedHead.addBox(-2F, -2F, -2F, 4, 2, 4, 0F);
		bipedHead.setRotationPoint(0F, 16F, 4F);
		bipedHead.rotateAngleX = 0F;
		bipedHead.rotateAngleY = 0F;
		bipedHead.rotateAngleZ = 0F;
		bipedHead.mirror = false;
		bipedBody = new ModelRenderer(this, 0, 1);
		bipedBody.addBox(-7F, 2F, -4F, 12, 4, 9, 0F);
		bipedBody.setRotationPoint(-0.4F, 16F, 3F);
		bipedBody.rotateAngleX = 0F;
		bipedBody.rotateAngleY = 0F;
		bipedBody.rotateAngleZ = 0F;
		bipedBody.mirror = false;
		bipedRightArm = new ModelRenderer(this, 39, 22);
		bipedRightArm.addBox(-3F, 0F, -1.466667F, 3, 7, 3, 0F);
		bipedRightArm.setRotationPoint(-6.566667F, 16F, 5F);
		bipedRightArm.rotateAngleX = 0F;
		bipedRightArm.rotateAngleY = 0F;
		bipedRightArm.rotateAngleZ = 0F;
		bipedRightArm.mirror = false;
		bipedLeftArm = new ModelRenderer(this, 40, 16);
		bipedLeftArm.addBox(0F, 0F, -1F, 2, 4, 2, 0F);
		bipedLeftArm.setRotationPoint(5F, 16F, 5F);
		bipedLeftArm.rotateAngleX = 0F;
		bipedLeftArm.rotateAngleY = 0F;
		bipedLeftArm.rotateAngleZ = 0F;
		bipedLeftArm.mirror = false;
		bipedRightLeg = new ModelRenderer(this, 0, 14);
		bipedRightLeg.addBox(-2F, 0F, -2F, 4, 2, 4, 0F);
		bipedRightLeg.setRotationPoint(-4.066667F, 22F, 4F);
		bipedRightLeg.rotateAngleX = 0F;
		bipedRightLeg.rotateAngleY = 0F;
		bipedRightLeg.rotateAngleZ = 0F;
		bipedRightLeg.mirror = false;
		bipedLeftLeg = new ModelRenderer(this, 0, 14);
		bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 2, 4, 0F);
		bipedLeftLeg.setRotationPoint(3F, 22F, 4F);
		bipedLeftLeg.rotateAngleX = 0F;
		bipedLeftLeg.rotateAngleY = 0F;
		bipedLeftLeg.rotateAngleZ = 0F;
		bipedLeftLeg.mirror = false;
		bipedBody2 = new ModelRenderer(this, 0, 23);
		bipedBody2.addBox(-3.666667F, 0F, 0F, 12, 2, 7, 0F);
		bipedBody2.setRotationPoint(-3F, 16F, 0F);
		bipedBody2.rotateAngleX = 0F;
		bipedBody2.rotateAngleY = 0F;
		bipedBody2.rotateAngleZ = 0F;
		bipedBody2.mirror = false;
 }

 @Override
public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
 {
     setRotationAngles(f, f1, f2, f3, f4, f5, entity);
     bipedHead.render(f5);
     bipedBody.render(f5);
     bipedBody2.render(f5);
     bipedRightArm.render(f5);
     bipedLeftArm.render(f5);
     bipedRightLeg.render(f5);
     bipedLeftLeg.render(f5);
 }
 
 @Override
public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
 {
     bipedHead.rotateAngleY = f3 / 57.29578F;
     bipedHead.rotateAngleX = f4 / 57.29578F;
     bipedRightArm.rotateAngleX = MathHelper.cos(f * 0.6662F + 3.141593F) * 2.0F * f1 * 0.5F;
     bipedLeftArm.rotateAngleX = MathHelper.cos(f * 0.6662F) * 2.0F * f1 * 0.5F;
     bipedRightArm.rotateAngleZ = 0.0F;
     bipedLeftArm.rotateAngleZ = 0.0F;
     bipedRightLeg.rotateAngleX = MathHelper.cos(f * 0.6662F) * 1.4F * f1;
     bipedLeftLeg.rotateAngleX = MathHelper.cos(f * 0.6662F + 3.141593F) * 1.4F * f1;
     bipedRightLeg.rotateAngleY = 0.0F;
     bipedLeftLeg.rotateAngleY = 0.0F;
     if(isRiding)
     {
         bipedRightArm.rotateAngleX += -0.6283185F;
         bipedLeftArm.rotateAngleX += -0.6283185F;
         bipedRightLeg.rotateAngleX = -1.256637F;
         bipedLeftLeg.rotateAngleX = -1.256637F;
         bipedRightLeg.rotateAngleY = 0.3141593F;
         bipedLeftLeg.rotateAngleY = -0.3141593F;
     }
     if(heldItemLeft)
     {
         bipedLeftArm.rotateAngleX = bipedLeftArm.rotateAngleX * 0.5F - 0.3141593F;
     }
     if(heldItemRight)
     {
         bipedRightArm.rotateAngleX = bipedRightArm.rotateAngleX * 0.5F - 0.3141593F;
     }
     bipedRightArm.rotateAngleY = 0.0F;
     bipedLeftArm.rotateAngleY = 0.0F;
    /* if(onGround > -9990F)
     {
         float f6 = onGround;
         bipedBody.rotateAngleY = MathHelper.sin(MathHelper.sqrt_float(f6) * 3.141593F * 2.0F) * 0.2F;
         bipedRightArm.rotationPointZ = MathHelper.sin(bipedBody.rotateAngleY) * 5F;
         bipedRightArm.rotationPointX = -MathHelper.cos(bipedBody.rotateAngleY) * 5F;
         bipedLeftArm.rotationPointZ = -MathHelper.sin(bipedBody.rotateAngleY) * 5F;
         bipedLeftArm.rotationPointX = MathHelper.cos(bipedBody.rotateAngleY) * 5F;
         bipedRightArm.rotateAngleY += bipedBody.rotateAngleY;
         bipedLeftArm.rotateAngleY += bipedBody.rotateAngleY;
         bipedLeftArm.rotateAngleX += bipedBody.rotateAngleY;
         f6 = 1.0F - onGround;
         f6 *= f6;
         f6 *= f6;
         f6 = 1.0F - f6;
         float f7 = MathHelper.sin(f6 * 3.141593F);
         float f8 = MathHelper.sin(onGround * 3.141593F) * -(bipedHead.rotateAngleX - 0.7F) * 0.75F;
         bipedRightArm.rotateAngleX -= (double)f7 * 1.2D + (double)f8;
         bipedRightArm.rotateAngleY += bipedBody.rotateAngleY * 2.0F;
         bipedRightArm.rotateAngleZ = MathHelper.sin(onGround * 3.141593F) * -0.4F;
     }
     if(isSneak)
     {
         bipedBody.rotateAngleX = 0.5F;
         bipedRightLeg.rotateAngleX -= 0.0F;
         bipedLeftLeg.rotateAngleX -= 0.0F;
         bipedRightArm.rotateAngleX += 0.4F;
         bipedLeftArm.rotateAngleX += 0.4F;
         bipedRightLeg.rotationPointZ = 4F;
         bipedLeftLeg.rotationPointZ = 4F;
         bipedRightLeg.rotationPointY = 9F;
         bipedLeftLeg.rotationPointY = 9F;
         bipedHead.rotationPointY = 1.0F;
     } else
     {
         bipedBody.rotateAngleX = 0.0F;
         bipedRightLeg.rotationPointZ = 0.0F;
         bipedLeftLeg.rotationPointZ = 0.0F;
         bipedRightLeg.rotationPointY = 12F;
         bipedLeftLeg.rotationPointY = 12F;
         bipedHead.rotationPointY = 0.0F;
     }*/
     bipedRightArm.rotateAngleZ += MathHelper.cos(f2 * 0.09F) * 0.05F + 0.05F;
     bipedLeftArm.rotateAngleZ -= MathHelper.cos(f2 * 0.09F) * 0.05F + 0.05F;
     bipedRightArm.rotateAngleX += MathHelper.sin(f2 * 0.067F) * 0.05F;
     bipedLeftArm.rotateAngleX -= MathHelper.sin(f2 * 0.067F) * 0.05F;
 }
/*
 public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5)
 {
 	super.setRotationAngles(f, f1, f2, f3, f4, f5);
 }*/

 public ModelRenderer bipedHead;
 public ModelRenderer bipedBody;
 public ModelRenderer bipedBody2;
 public ModelRenderer bipedRightArm;
 public ModelRenderer bipedLeftArm;
 public ModelRenderer bipedRightLeg;
 public ModelRenderer bipedLeftLeg;
 public boolean heldItemLeft;
 public boolean heldItemRight;
 public boolean isSneak;
}
