
package mods.invmod.client.render;

import mods.invmod.common.entity.EntityIMBurrower;
import mods.invmod.common.util.PosRotate3D;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;

import org.lwjgl.opengl.GL11;


public class RenderBurrower extends Render
{
    public RenderBurrower()
    {
    	modelBurrower = new ModelBurrower2(EntityIMBurrower.NUMBER_OF_SEGMENTS);
    }

    public void renderBurrower(EntityIMBurrower entityBurrower, double x, double y, double z, float yaw, float partialTick)
    {
    	// Calculate the render positions for each segment from the absolute logical positions of that segment
    	// Need to do interpolation too. We need to calculate each segment individually because each requires
    	// a unique translation, hence the lack of the typical GL11.glTranslatef(x, y, z) seen in renderEntity.
    	// Head: i = 0
    	// Segments: i = 1, 2, ...
    	PosRotate3D[] pos = entityBurrower.getSegments3D();
		PosRotate3D[] lastPos = entityBurrower.getSegments3DLastTick();
		PosRotate3D[] renderPos = new PosRotate3D[EntityIMBurrower.NUMBER_OF_SEGMENTS + 1];
		renderPos[0] = new PosRotate3D();
		renderPos[0].setPosX(x * -7.27F);
		renderPos[0].setPosY(y * -7.27F);
		renderPos[0].setPosZ(z * 7.27F);
		renderPos[0].setRotX(entityBurrower.getPrevRotX() + partialTick * (entityBurrower.getRotX() - entityBurrower.getPrevRotX()));
		renderPos[0].setRotY(entityBurrower.getPrevRotY() + partialTick * (entityBurrower.getRotY() - entityBurrower.getPrevRotY()));
		renderPos[0].setRotZ(entityBurrower.getPrevRotZ() + partialTick * (entityBurrower.getRotZ() - entityBurrower.getPrevRotZ()));
		
		for(int i = 0; i < EntityIMBurrower.NUMBER_OF_SEGMENTS; i++)
		{
			renderPos[i + 1] = new PosRotate3D();
			renderPos[i + 1].setPosX((lastPos[i].getPosX() + partialTick * (pos[i].getPosX() - lastPos[i].getPosX()) - RenderManager.renderPosX) * -7.27F);
			renderPos[i + 1].setPosY((lastPos[i].getPosY() + partialTick * (pos[i].getPosY() - lastPos[i].getPosY()) - RenderManager.renderPosY) * -7.27F);
			renderPos[i + 1].setPosZ((lastPos[i].getPosZ() + partialTick * (pos[i].getPosZ() - lastPos[i].getPosZ()) - RenderManager.renderPosZ) * 7.27F);
			renderPos[i + 1].setRotX(lastPos[i].getRotX() + partialTick * (pos[i].getRotX() - lastPos[i].getRotX()));
			renderPos[i + 1].setRotY(lastPos[i].getRotY() + partialTick * (pos[i].getRotY() - lastPos[i].getRotY()));
			renderPos[i + 1].setRotZ(lastPos[i].getRotZ() + partialTick * (pos[i].getRotZ() - lastPos[i].getRotZ()));
		}
    	   	 
		GL11.glPushMatrix();
    	GL11.glEnable(32826 /*GL_RESCALE_NORMAL_EXT*/);
    	GL11.glScalef(-1F, -1F, 1.0F);
    	GL11.glScalef(2.2F, 2.2F, 2.2F);
    	loadTexture("/mods/invmod/textures/burrower.png");
    	modelBurrower.render(entityBurrower, partialTick, renderPos, 0.0625F);
    	GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);
    	GL11.glPopMatrix();
    }

    @Override
	public void doRender(Entity entity, double d, double d1, double d2, float yaw, float partialTick)
    {
        renderBurrower((EntityIMBurrower)entity, d, d1, d2, yaw, partialTick);
    }
    
    private ModelBurrower2 modelBurrower;
}
