
package mods.invmod.client.render;

import mods.invmod.common.entity.EntityIMPigEngy;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLiving;

import org.lwjgl.opengl.GL11;


public class RenderImpB extends RenderLiving
{
	public RenderImpB(ModelBase modelbase, float f)
    {
        super(modelbase, f);
    }
	
	protected void preRenderScale(EntityIMPigEngy entity, float f)
    {
        GL11.glScalef(0.91F, 1.3F, 1.3F);
    }

    @Override
	protected void preRenderCallback(EntityLiving entityliving, float f)
    {
        preRenderScale((EntityIMPigEngy)entityliving, f);
    }
}
