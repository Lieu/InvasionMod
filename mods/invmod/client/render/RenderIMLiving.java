package mods.invmod.client.render;

import mods.invmod.common.entity.EntityIMLiving;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLiving;

public class RenderIMLiving extends RenderLiving
{
	public RenderIMLiving(ModelBase model, float shadowWidth)
	{
		super(model, shadowWidth);
	}

	public void doRenderLiving(EntityIMLiving entity, double renderX, double renderY, double renderZ, float interpYaw, float parTick)
	{
		super.doRenderLiving(entity, renderX, renderY, renderZ, interpYaw, parTick);
		if(entity.shouldRenderLabel())
		{
			String s = entity.getRenderLabel();
			String[] labels = s.split("\n");
			for(int i = 0; i < labels.length; i++)
			{
				renderLivingLabel(entity, labels[i], renderX, renderY + (labels.length - 1 - i) * 0.22, renderZ, 32);
			}
		}
	}
}
