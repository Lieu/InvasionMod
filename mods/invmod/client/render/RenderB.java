package mods.invmod.client.render;

import mods.invmod.common.entity.EntityIMBird;
import mods.invmod.common.entity.EntityIMEgg;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBat;
import net.minecraft.client.model.ModelDragon;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

import org.lwjgl.opengl.GL11;

public class RenderB extends RenderLiving
{
	private ModelBird modelBird;

	public RenderB()
	{
		super(new ModelBird(), 0.4F);
		modelBird = (ModelBird)mainModel;
	}

	public void renderBz(EntityIMBird entityBird, double renderX, double renderY, double renderZ, float interpYaw, float partialTick)
	{
		if(entityBird.hasFlyingDebug())
		{
			renderNavigationVector(entityBird, renderX, renderY, renderZ);
		}
		
		float flapProgress = entityBird.getWingAnimationState().getCurrentAnimationTimeInterp(partialTick);
		modelBird.setFlyingAnimations(flapProgress, entityBird.getLegSweepProgress(), entityBird.getRotationRoll());
		super.doRenderLiving(entityBird, renderX, renderY, renderZ, interpYaw, partialTick);
		
//		GL11.glPushMatrix();
//		GL11.glTranslatef((float)d, (float)d1, (float)d2);
//		
//		GL11.glRotatef(entityBird.rotationPitch, 0.0F, 0.0F, 1.0F);
//		GL11.glRotatef(-entityBird.rotationYaw, 0.0F, 1.0F, 0.0F);
//		//System.out.println(entityBird.rotationPitch);
//		GL11.glEnable(32826 /*GL_RESCALE_NORMAL_EXT*/);
//		GL11.glScalef(-1F, -1F, 1.0F);
//		//GL11.glScalef(2.2F, 2.2F, 2.2F);
//		//loadTexture("/mods/invmod/textures/spideregg.png");
//		loadTexture("/mob/bat.png");
//		modelEgg.render(entityBird, 0, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
//
//
//		GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);
//		GL11.glPopMatrix();
	}

	@Override
	public void doRender(Entity entity, double d, double d1, double d2, float f, float f1)
	{
		renderBz((EntityIMBird)entity, d, d1, d2, f, f1);
	}
	
	private void renderNavigationVector(EntityIMBird entityBird, double entityRenderOffsetX, double entityRenderOffsetY, double entityRenderOffsetZ)
	{
		Tessellator tessellator = Tessellator.instance;
		GL11.glPushMatrix();
		
		GL11.glDisable(3553 /*GL_TEXTURE_2D*/);
		GL11.glDisable(2896 /*GL_LIGHTING*/);
		GL11.glEnable(3042 /*GL_BLEND*/);
		GL11.glBlendFunc(770, 1);
		
		Vec3 target = entityBird.getFlyTarget();
		double drawWidth = 0.1;
		
		tessellator.startDrawing(5);
		tessellator.setColorRGBA_F(1.0F, 0.0F, 0.0F, 1.0F);
		for(int j = 0; j < 5; j++)
		{
			double xOffset = drawWidth;
			double zOffset = drawWidth;
			if(j == 1 || j == 2)
			{
				xOffset += drawWidth * 2;
			}
			if(j == 2 || j == 3)
			{
				zOffset += drawWidth * 2;
			}
			tessellator.addVertex(entityRenderOffsetX - entityBird.width / 2 + xOffset, entityRenderOffsetY + entityBird.height / 2, entityRenderOffsetZ - entityBird.width / 2  + zOffset);
			tessellator.addVertex(target.xCoord + xOffset - RenderManager.renderPosX, target.yCoord - RenderManager.renderPosY, target.zCoord + zOffset - RenderManager.renderPosZ);	                
		}
		tessellator.draw();

		GL11.glDisable(3042 /*GL_BLEND*/);
		GL11.glEnable(2896 /*GL_LIGHTING*/);
		GL11.glEnable(3553 /*GL_TEXTURE_2D*/);
		
		GL11.glPopMatrix();
	}
}
