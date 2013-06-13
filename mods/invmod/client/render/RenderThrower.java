
package mods.invmod.client.render;

import mods.invmod.common.entity.EntityIMThrower;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLiving;

import org.lwjgl.opengl.GL11;


public class RenderThrower extends RenderLiving
{
	public RenderThrower(ModelBase modelbase, float f)
    {
        super(modelbase, f);
    }
	
	protected void preRenderScale(EntityIMThrower entity, float f)
    {
        GL11.glScalef(2.4F, 2.8F, 2.4F);
    }

    @Override
	protected void preRenderCallback(EntityLiving entityliving, float f)
    {
        preRenderScale((EntityIMThrower)entityliving, f);
    }

}
