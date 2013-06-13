
package mods.invmod.client.render;

import mods.invmod.common.entity.EntityIMImp;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLiving;

import org.lwjgl.opengl.GL11;


public class RenderImp extends RenderLiving
{
	public RenderImp(ModelBase modelbase, float f)
    {
        super(modelbase, f);
    }
	
	protected void preRenderScale(EntityIMImp entity, float f)
    {
        GL11.glScalef(0.7F, 1.0F, 1.0F);
    }

    @Override
	protected void preRenderCallback(EntityLiving entityliving, float f)
    {
        preRenderScale((EntityIMImp)entityliving, f);
    }
}
