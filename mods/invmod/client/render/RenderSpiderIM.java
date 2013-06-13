
package mods.invmod.client.render;

import mods.invmod.common.entity.EntityIMSpider;
import net.minecraft.client.model.ModelSpider;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLiving;

import org.lwjgl.opengl.GL11;

public class RenderSpiderIM extends RenderLiving
{

    public RenderSpiderIM()
    {
        super(new ModelSpider(), 1.0F);
        setRenderPassModel(new ModelSpider());
    }

    protected float setSpiderDeathMaxRotation(EntityIMSpider entityspider)
    {
        return 180F;
    }

    protected int setSpiderEyeBrightness(EntityIMSpider entityspider, int i, float f)
    {
        if(i != 0)
        {
            return -1;
        } 
        else
        {
            loadTexture("/mob/spider_eyes.png");
            float f1 = 1.0F;
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
            int j = 61680;
            int k = j % 0x10000;
            int l = j / 0x10000;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, k / 1.0F, l / 1.0F);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, f1);
            return 1;
        }
    }

    protected void scaleSpider(EntityIMSpider entityspider, float f)
    {
        float f1 = entityspider.spiderScaleAmount();
        shadowSize = f1;
        GL11.glScalef(f1, f1, f1);
    }

    @Override
	protected void preRenderCallback(EntityLiving entityliving, float f)
    {
        scaleSpider((EntityIMSpider)entityliving, f);
    }

    @Override
	protected float getDeathMaxRotation(EntityLiving entityliving)
    {
        return setSpiderDeathMaxRotation((EntityIMSpider)entityliving);
    }

    @Override
	protected int shouldRenderPass(EntityLiving entityliving, int i, float f)
    {
        return setSpiderEyeBrightness((EntityIMSpider)entityliving, i, f);
    }
}
