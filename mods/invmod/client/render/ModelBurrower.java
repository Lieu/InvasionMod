package mods.invmod.client.render;

import mods.invmod.common.entity.EntityIMBurrower;
import mods.invmod.common.util.PosRotate3D;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelBurrower extends ModelBase
{
	public ModelBurrower()
	{
		textureWidth = 64;
		textureHeight = 32;

		head = new ModelRenderer(this, 0, 0);
		head.addBox(-2F, -2.5F, -2.5F, 4, 5, 5);
		head.setRotationPoint(0F, 0F, 0F);
		head.setTextureSize(64, 32);
		head.mirror = true;
		setRotation(head, 0F, 0F, 0F);
		seg1 = new ModelRenderer(this, 0, 0);
		seg1.addBox(-2F, -2.5F, -2.5F, 4, 5, 5);
		seg1.setRotationPoint(-4F, 0F, 0F);
		seg1.setTextureSize(64, 32);
		seg1.mirror = true;
		setRotation(seg1, 0F, 0F, 0F);
		seg2 = new ModelRenderer(this, 0, 0);
		seg2.addBox(-2F, -2.5F, -2.5F, 4, 5, 5);
		seg2.setRotationPoint(-8F, 0F, 0F);
		seg2.setTextureSize(64, 32);
		seg2.mirror = true;
		setRotation(seg2, 0F, 0F, 0F);
		seg3 = new ModelRenderer(this, 0, 0);
		seg3.addBox(-2F, -2.5F, -2.5F, 4, 5, 5);
		seg3.setRotationPoint(-12F, 0F, 0F);
		seg3.setTextureSize(64, 32);
		seg3.mirror = true;
		setRotation(seg3, 0F, 0F, 0F);
	}

	public void render(Entity entity, float partialTick, PosRotate3D[] pos, float modelScale)
	{
		super.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, modelScale);
		
		// We use setRotationPoint() here to take advantage of of the glTranslatef() call
		// in ModelRenderer.render(), rather than push another transformation on to the
		// stack or extend ModelRenderer. Preferred long term solution is to extend if
		// this grows more complex.
		if(pos.length >= EntityIMBurrower.NUMBER_OF_SEGMENTS)
		{
			head.setRotationPoint((float)pos[0].getPosX(), (float)pos[0].getPosY(), (float)pos[0].getPosZ());
			setRotation(head, pos[0].getRotX(), pos[0].getRotY(), pos[0].getRotZ());
			seg1.setRotationPoint((float)pos[1].getPosX(), (float)pos[1].getPosY(), (float)pos[1].getPosZ());
			setRotation(seg1, pos[1].getRotX(), pos[1].getRotY(), pos[1].getRotZ());
			seg2.setRotationPoint((float)pos[2].getPosX(), (float)pos[2].getPosY(), (float)pos[2].getPosZ());
			setRotation(seg2, pos[2].getRotX(), pos[2].getRotY(), pos[2].getRotZ());
			seg3.setRotationPoint((float)pos[3].getPosX(), (float)pos[3].getPosY(), (float)pos[3].getPosZ());
			setRotation(seg3, pos[3].getRotX(), pos[3].getRotY(), pos[3].getRotZ());
			head.render(modelScale);
			seg1.render(modelScale);
			seg2.render(modelScale);
			seg3.render(modelScale);
		}
	}

	/**
	 * Rotates the specified model. (This method will most certainly become inlined)
	 */
	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
	
	ModelRenderer head;
	ModelRenderer seg1;
	ModelRenderer seg2;
	ModelRenderer seg3;
}
