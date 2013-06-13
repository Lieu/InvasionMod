
package mods.invmod.client.render;

import mods.invmod.common.entity.EntityIMBoulder;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;

import org.lwjgl.opengl.GL11;


public class RenderBoulder extends Render
{
    public RenderBoulder()
    {
    	modelBoulder = new ModelBoulder();
    }

    public void renderBoulder(EntityIMBoulder entityBoulder, double d, double d1, double d2, float f, float f1)
    {
    	 GL11.glPushMatrix();
         GL11.glTranslatef((float)d, (float)d1, (float)d2);
         GL11.glEnable(32826 /*GL_RESCALE_NORMAL_EXT*/);
         GL11.glScalef(2.2F, 2.2F, 2.2F);
         loadTexture("/mods/invmod/textures/boulder.png");
         float spin = (entityBoulder.getFlightTime() % 20) / 20F;
         modelBoulder.render(entityBoulder, spin, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
         GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);
         GL11.glPopMatrix();
    }

    @Override
	public void doRender(Entity entity, double d, double d1, double d2, float f, float f1)
    {
        renderBoulder((EntityIMBoulder)entity, d, d1, d2, f, f1);
    }
    
    private ModelBoulder modelBoulder;
}
