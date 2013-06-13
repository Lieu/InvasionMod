
package mods.invmod.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;


public class ModelBoulder extends ModelBase
{
    public ModelBoulder()
    {
    	boulder = new ModelRenderer(this, 0, 0);
        boulder.addBox(-4F, -4F, -4F, 8, 8, 8);
        boulder.setRotationPoint(0F, 0F, 0F);
        boulder.rotateAngleX = 0F;
        boulder.rotateAngleY = 0F;
        boulder.rotateAngleZ = 0F;
        boulder.mirror = false;
    }

    @Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
    	super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        boulder.render(f5);
    }

    @Override
	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
    {
    	boulder.rotateAngleX = f;
    	boulder.rotateAngleY = f1;
    	boulder.rotateAngleZ = f2;
    }

    ModelRenderer boulder;
}
