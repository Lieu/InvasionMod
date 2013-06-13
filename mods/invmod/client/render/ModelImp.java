
package mods.invmod.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelImp extends ModelBase
{

 public ModelImp()
 {
     this(0.0F);
 }

 public ModelImp(float f)
 {
     this(f, 0.0F);
 }

 public ModelImp(float f, float f1)
 {     
     head = new ModelRenderer(this, 44, 0);
     head.addBox(-2.733333F, -3F, -2F, 5, 3, 4);
     head.setRotationPoint(-0.4F, 9.8F, -3.3F);
     head.rotateAngleX = 0.15807F;
     head.rotateAngleY = 0F;
     head.rotateAngleZ = 0F;
     head.mirror = false;
     body = new ModelRenderer(this, 23, 1);
     body.addBox(-4F, 0F, -4F, 7, 4, 3);
     body.setRotationPoint(0F, 9.1F, -0.8666667F);
     body.rotateAngleX = 0.64346F;
     body.rotateAngleY = 0F;
     body.rotateAngleZ = 0F;
     body.mirror = false;
     rightarm = new ModelRenderer(this, 26, 9);
     rightarm.addBox(-2F, -0.7333333F, -1.133333F, 2, 7, 2);
     rightarm.setRotationPoint(-4F, 10.8F, -2.066667F);
     rightarm.rotateAngleX = 0F;
     rightarm.rotateAngleY = 0F;
     rightarm.rotateAngleZ = 0F;
     rightarm.mirror = false;
     leftarm = new ModelRenderer(this, 18, 9);
     leftarm.addBox(0F, -0.8666667F, -1F, 2, 7, 2);
     leftarm.setRotationPoint(3F, 10.8F, -2.1F);
     leftarm.rotateAngleX = 0F;
     leftarm.rotateAngleY = 0F;
     leftarm.rotateAngleZ = 0F;
     leftarm.mirror = false;
     rightleg = new ModelRenderer(this, 0, 17);
     rightleg.addBox(-1F, 0F, -2F, 2, 4, 3);
     rightleg.setRotationPoint(-2F, 16.9F, -1F);
     rightleg.rotateAngleX = -0.15807F;
     rightleg.rotateAngleY = 0F;
     rightleg.rotateAngleZ = 0F;
     rightleg.mirror = false;
     leftleg = new ModelRenderer(this, 0, 24);
     leftleg.addBox(-1F, 0F, -2F, 2, 4, 3);
     leftleg.setRotationPoint(1F, 17F, -1F);
     leftleg.rotateAngleX = -0.15919F;
     leftleg.rotateAngleY = 0F;
     leftleg.rotateAngleZ = 0F;
     leftleg.mirror = false;
     rshin = new ModelRenderer(this, 10, 17);
     rshin.addBox(-2F, 0.6F, -4.4F, 2, 3, 2);
     rshin.setRotationPoint(-1F, 16.9F, -1F);
     rshin.rotateAngleX = 0.82623F;
     rshin.rotateAngleY = 0F;
     rshin.rotateAngleZ = 0F;
     rshin.mirror = false;
     rfoot = new ModelRenderer(this, 18, 18);
     rfoot.addBox(-2F, 4.2F, -1.0F, 2, 3, 2);
     rfoot.setRotationPoint(-1F, 16.9F, -1F);
     rfoot.rotateAngleX = -0.01403F;
     rfoot.rotateAngleY = 0F;
     rfoot.rotateAngleZ = 0F;
     rfoot.mirror = false;
     lshin = new ModelRenderer(this, 10, 22);
     lshin.addBox(-1F, 0.6F, -4.433333F, 2, 3, 2);
     lshin.setRotationPoint(1F, 17F, -1F);
     lshin.rotateAngleX = 0.82461F;
     lshin.rotateAngleY = 0F;
     lshin.rotateAngleZ = 0F;
     lshin.mirror = false;
     lfoot = new ModelRenderer(this, 10, 27);
     lfoot.addBox(-1F, 4.2F, -1F, 2, 3, 2);
     lfoot.setRotationPoint(1F, 17F, -1F);
     lfoot.rotateAngleX = -0.01214F;
     lfoot.rotateAngleY = 0F;
     lfoot.rotateAngleZ = 0F;
     lfoot.mirror = false;
     rhorn = new ModelRenderer(this, 0, 0);
     rhorn.addBox(0F, 0F, 0F, 1, 1, 1);
     rhorn.setRotationPoint(-2.5F, 6F, -5F);
     rhorn.rotateAngleX = 0F;
     rhorn.rotateAngleY = 0F;
     rhorn.rotateAngleZ = 0F;
     rhorn.mirror = false;
     lhorn = new ModelRenderer(this, 0, 2);
     lhorn.addBox(0F, 0F, 0F, 1, 1, 1);
     lhorn.setRotationPoint(0.5F, 6F, -5F);
     lhorn.rotateAngleX = 0F;
     lhorn.rotateAngleY = 0F;
     lhorn.rotateAngleZ = 0F;
     lhorn.mirror = false;
     bodymid = new ModelRenderer(this, 1, 1);
     bodymid.addBox(0F, 0F, 0F, 7, 5, 3);
     bodymid.setRotationPoint(-4F, 12.46667F, -2.266667F);
     bodymid.rotateAngleX = -0.15807F;
     bodymid.rotateAngleY = 0F;
     bodymid.rotateAngleZ = 0F;
     bodymid.mirror = false;
     neck = new ModelRenderer(this, 44, 7);
     neck.addBox(0F, 0F, 0F, 3, 2, 2);
     neck.setRotationPoint(-2F, 9.6F, -4.033333F);
     neck.rotateAngleX = 0.27662F;
     neck.rotateAngleY = 0F;
     neck.rotateAngleZ = 0F;
     neck.mirror = false;
     bodychest = new ModelRenderer(this, 0, 9);
     bodychest.addBox(0F, -1F, 0F, 7, 6, 2);
     bodychest.setRotationPoint(-4F, 12.36667F, -3.8F);
     bodychest.rotateAngleX = 0.31614F;
     bodychest.rotateAngleY = 0F;
     bodychest.rotateAngleZ = 0F;
     bodychest.mirror = false;
     tail = new ModelRenderer(this, 18, 23);
     tail.addBox(0F, 0F, 0F, 1, 8, 1);
     tail.setRotationPoint(-1F, 15F, -0.6666667F);
     tail.rotateAngleX = 0.47304F;
     tail.rotateAngleY = 0F;
     tail.rotateAngleZ = 0F;
     tail.mirror = false;
     tail2 = new ModelRenderer(this, 22, 23);
     tail2.addBox(0F, 0F, 0F, 1, 4, 1);
     tail2.setRotationPoint(-1F, 22.1F, 2.9F);
     tail2.rotateAngleX = 1.38309F;
     tail2.rotateAngleY = 0F;
     tail2.rotateAngleZ = 0F;
     tail2.mirror = false;

 }

 @Override
public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
 {
	 super.render(entity, f, f1, f2, f3, f4, f5);
     setRotationAngles(f, f1, f2, f3, f4, f5, entity);
     head.render(f5);
     body.render(f5);
     rightarm.render(f5);
     leftarm.render(f5);
     rightleg.render(f5);
     leftleg.render(f5);
     rshin.render(f5);
     rfoot.render(f5);
     lshin.render(f5);
     lfoot.render(f5);
     rhorn.render(f5);
     lhorn.render(f5);
     bodymid.render(f5);
     neck.render(f5);
     bodychest.render(f5);
     tail.render(f5);
     tail2.render(f5);
 }
 
 @Override
public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
 {
     head.rotateAngleY = f3 / 57.29578F;
     head.rotateAngleX = f4 / 57.29578F;
     rightarm.rotateAngleX = MathHelper.cos(f * 0.6662F + 3.141593F) * 2.0F * f1 * 0.5F;
     leftarm.rotateAngleX = MathHelper.cos(f * 0.6662F) * 2.0F * f1 * 0.5F;
     rightarm.rotateAngleZ = 0.0F;
     leftarm.rotateAngleZ = 0.0F;
     
     rightleg.rotateAngleX = MathHelper.cos(f * 0.6662F) * 1.4F * f1 - 0.158F;
     rshin.rotateAngleX = MathHelper.cos(f * 0.6662F) * 1.4F * f1 + 0.82623F;
     rfoot.rotateAngleX = MathHelper.cos(f * 0.6662F) * 1.4F * f1 - 0.01403F;
     
     leftleg.rotateAngleX = MathHelper.cos(f * 0.6662F + 3.141593F) * 1.4F * f1 - 0.15919F;
     lshin.rotateAngleX = MathHelper.cos(f * 0.6662F + 3.141593F) * 1.4F * f1 + 0.82461F;
     lfoot.rotateAngleX = MathHelper.cos(f * 0.6662F + 3.141593F) * 1.4F * f1 - 0.01214F;
     
     rightleg.rotateAngleY = 0.0F;
     rshin.rotateAngleY = 0.0F;
     rfoot.rotateAngleY = 0.0F;
     
     leftleg.rotateAngleY = 0.0F;
     lshin.rotateAngleY = 0.0F;
     lfoot.rotateAngleY = 0.0F;

     /*if(heldItemLeft)
     {
         leftarm.rotateAngleX = leftarm.rotateAngleX * 0.5F - 0.3141593F;
     }
     if(heldItemRight)
     {
         rightarm.rotateAngleX = rightarm.rotateAngleX * 0.5F - 0.3141593F;
     }*/
     
     rightarm.rotateAngleY = 0.0F;
     leftarm.rotateAngleY = 0.0F;
    
     rightarm.rotateAngleZ += MathHelper.cos(f2 * 0.09F) * 0.05F + 0.05F;
     leftarm.rotateAngleZ -= MathHelper.cos(f2 * 0.09F) * 0.05F + 0.05F;
     rightarm.rotateAngleX += MathHelper.sin(f2 * 0.067F) * 0.05F;
     leftarm.rotateAngleX -= MathHelper.sin(f2 * 0.067F) * 0.05F;
 }

	 ModelRenderer head;
	 ModelRenderer body;
	 ModelRenderer rightarm;
	 ModelRenderer leftarm;
	 ModelRenderer rightleg;
	 ModelRenderer leftleg;
	 ModelRenderer rshin;
	 ModelRenderer rfoot;
	 ModelRenderer lshin;
	 ModelRenderer lfoot;
	 ModelRenderer rhorn;
	 ModelRenderer lhorn;
	 ModelRenderer bodymid;
	 ModelRenderer neck;
	 ModelRenderer bodychest;
	 ModelRenderer tail;
	 ModelRenderer tail2;
}
