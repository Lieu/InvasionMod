package mods.invmod.client.render;

import mods.invmod.common.entity.EntityIMCreeper;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelCreeper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.MathHelper;

import org.lwjgl.opengl.GL11;

public class RenderIMCreeper extends RenderLiving
{
    private ModelBase field_27008_a;

    public RenderIMCreeper()
    {
        super(new ModelCreeper(), 0.5F);
        field_27008_a = new ModelCreeper(2.0F);
    }

    /**
     * Updates creeper scale in prerender callback
     */
    protected void updateCreeperScale(EntityIMCreeper par1EntityCreeper, float par2)
    {
        EntityIMCreeper entitycreeper = par1EntityCreeper;
        float f = entitycreeper.setCreeperFlashTime(par2);
        float f1 = 1.0F + MathHelper.sin(f * 100F) * f * 0.01F;

        if (f < 0.0F)
        {
            f = 0.0F;
        }

        if (f > 1.0F)
        {
            f = 1.0F;
        }

        f *= f;
        f *= f;
        float f2 = (1.0F + f * 0.4F) * f1;
        float f3 = (1.0F + f * 0.1F) / f1;
        GL11.glScalef(f2, f3, f2);
    }

    /**
     * Updates color multiplier based on creeper state called by getColorMultiplier
     */
    protected int updateCreeperColorMultiplier(EntityIMCreeper par1EntityCreeper, float par2, float par3)
    {
        EntityIMCreeper entitycreeper = par1EntityCreeper;
        float f = entitycreeper.setCreeperFlashTime(par3);

        if ((int)(f * 10F) % 2 == 0)
        {
            return 0;
        }

        int i = (int)(f * 0.2F * 255F);

        if (i < 0)
        {
            i = 0;
        }

        if (i > 255)
        {
            i = 255;
        }

        char c = '\377';
        char c1 = '\377';
        char c2 = '\377';
        return i << 24 | c << 16 | c1 << 8 | c2;
    }

    protected int func_27007_b(EntityIMCreeper par1EntityCreeper, int par2, float par3)
    {
        return -1;
    }

    /**
     * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
     * entityLiving, partialTickTime
     */
    @Override
	protected void preRenderCallback(EntityLiving par1EntityLiving, float par2)
    {
        updateCreeperScale((EntityIMCreeper)par1EntityLiving, par2);
    }

    /**
     * Returns an ARGB int color back. Args: entityLiving, lightBrightness, partialTickTime
     */
    @Override
	protected int getColorMultiplier(EntityLiving par1EntityLiving, float par2, float par3)
    {
        return updateCreeperColorMultiplier((EntityIMCreeper)par1EntityLiving, par2, par3);
    }

    @Override
	protected int inheritRenderPass(EntityLiving par1EntityLiving, int par2, float par3)
    {
        return func_27007_b((EntityIMCreeper)par1EntityLiving, par2, par3);
    }
}
