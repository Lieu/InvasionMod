package mods.invmod.client.render;

import mods.invmod.common.entity.EntityIMEgg;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;

import org.lwjgl.opengl.GL11;

public class RenderEgg extends Render
{
	private ModelEgg modelEgg;
	
	public RenderEgg()
    {
    	modelEgg = new ModelEgg();
    }

    public void renderEgg(EntityIMEgg entityEgg, double d, double d1, double d2, float f, float f1)
    {
    	 GL11.glPushMatrix();
         GL11.glTranslatef((float)d, (float)d1, (float)d2);
         GL11.glEnable(32826 /*GL_RESCALE_NORMAL_EXT*/);
         GL11.glScalef(-1F, -1F, 1.0F);
         //GL11.glScalef(2.2F, 2.2F, 2.2F);
         loadTexture("/mods/invmod/textures/spideregg.png");
         modelEgg.render(entityEgg, 0, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
         GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);
         GL11.glPopMatrix();
    }

    @Override
	public void doRender(Entity entity, double d, double d1, double d2, float f, float f1)
    {
        renderEgg((EntityIMEgg)entity, d, d1, d2, f, f1);
    }
}
