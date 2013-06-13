package mods.invmod.client.render;

import mods.invmod.common.util.PosRotate3D;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelBurrower2 extends ModelBase
{
	public ModelBurrower2(int numberOfSegments)
	{
		textureWidth = 64;
		textureHeight = 32;

		head = new ModelRenderer(this, 0, 0);
		head.addBox(-1F, -3F, -3F, 2, 6, 6);
		head.setRotationPoint(0F, 0F, 0F);
		head.setTextureSize(64, 32);
		head.mirror = true;
		setRotation(head, 0F, 0F, 0F);
		segments = new ModelRenderer[numberOfSegments];
		for(int i = 0; i < numberOfSegments; i++)
		{
			segments[i] = new ModelRenderer(this, 0, 0);
			
			if(i % 2 == 0)
				segments[i].addBox(-0.5F, -3.5F, -3.5F, 2, 7, 7);
			else
				segments[i].addBox(-0.5F, -2.5F, -2.5F, 2, 5, 5);
			
			segments[i].setRotationPoint(-4F, 0F, 0F);
			segments[i].setTextureSize(64, 32);
			segments[i].mirror = true;
			setRotation(segments[i], 0F, 0F, 0F);
		}
	}

	public void render(Entity entity, float partialTick, PosRotate3D[] pos, float modelScale)
	{
		super.render(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, modelScale);
		
		// We use setRotationPoint() here to take advantage of of the glTranslatef() call
		// in ModelRenderer.render(), rather than push another transformation on to the
		// stack or extend ModelRenderer. Preferred long term solution is to extend if
		// this grows more complex.
		head.setRotationPoint((float)pos[0].getPosX(), (float)pos[0].getPosY(), (float)pos[0].getPosZ());
		setRotation(head, pos[0].getRotX(), pos[0].getRotY(), pos[0].getRotZ());
		for(int i = 0; i < segments.length; i++)
		{
			segments[i].setRotationPoint((float)pos[i + 1].getPosX(), (float)pos[i + 1].getPosY(), (float)pos[i + 1].getPosZ());
			setRotation(segments[i], pos[i + 1].getRotX(), pos[i + 1].getRotY(), pos[i + 1].getRotZ());
			segments[i].render(modelScale);
		}
		head.render(modelScale);
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
	ModelRenderer[] segments;
}
