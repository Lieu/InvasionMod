
package mods.invmod.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;


public class ModelTest extends ModelBase
{
	public ModelTest()
	{
		textureWidth = 64;
		textureHeight = 32;

		Shape1 = new ModelRenderer(this, 0, 0);
		Shape1.addBox(-3F, -1.5F, -1.5F, 7, 3, 3);
		Shape1.setRotationPoint(0F, 0F, 0F);
		Shape1.setTextureSize(64, 32);
		setRotation(Shape1, 0F, 0F, 0F);    
		Shape2 = new ModelRenderer(this, 0, 8);
		Shape2.addBox(4F, -0.5F, -0.5F, 1, 1, 1);
		Shape2.setRotationPoint(0F, 0F, 0F);
		Shape2.setTextureSize(64, 32);
		setRotation(Shape2, 0F, 0F, 0F);    
		Shape3 = new ModelRenderer(this, 0, 6);
		Shape3.addBox(2F, 1.5F, -0.5F, 1, 1, 1);
		Shape3.setRotationPoint(0F, 0F, 0F);
		Shape3.setTextureSize(64, 32);
		setRotation(Shape3, 0F, 0F, 0F);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		Shape1.render(f5);
		Shape2.render(f5);
		Shape3.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	@Override
	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
	{
		setRotation(Shape1, f, f1, f2);
		setRotation(Shape2, f, f1, f2);
		setRotation(Shape3, f, f1, f2);
	}

	ModelRenderer Shape1;
	ModelRenderer Shape2;
	ModelRenderer Shape3;
}
