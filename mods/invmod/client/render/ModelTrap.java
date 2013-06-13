
package mods.invmod.client.render;

import mods.invmod.common.entity.EntityIMTrap;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelTrap extends ModelBase
{
    ModelRenderer Core;
    ModelRenderer CoreFire;
    ModelRenderer Clasp1a;
    ModelRenderer Clasp1b;
    ModelRenderer Clasp2b;
    ModelRenderer Clasp2a;
    ModelRenderer Clasp3a;
    ModelRenderer Clasp3b;
    ModelRenderer Clasp4a;
    ModelRenderer Clasp4b;
    ModelRenderer Base;
    ModelRenderer BaseS1;
    ModelRenderer BaseS2;
  
  public ModelTrap()
  {
	  textureWidth = 32;
      textureHeight = 32;    
      Core = new ModelRenderer(this, 0, 13);
      Core.addBox(0F, 0F, 0F, 4, 2, 4);
      Core.setRotationPoint(-2F, -2F, -2F);
      Core.setTextureSize(32, 32);
      Core.mirror = true;
      CoreFire = new ModelRenderer(this, 5, 7);
      CoreFire.addBox(0F, 0F, 0F, 4, 2, 4);
      CoreFire.setRotationPoint(-2F, -2F, -2F);
      CoreFire.setTextureSize(32, 32);
      CoreFire.mirror = true;
      setRotation(Core, 0F, 0F, 0F);
      Clasp1a = new ModelRenderer(this, 0, 0);
      Clasp1a.addBox(0F, 0F, 0F, 2, 2, 1);
      Clasp1a.setRotationPoint(-1F, -2F, 2F);
      Clasp1a.setTextureSize(32, 32);
      Clasp1a.mirror = true;
      setRotation(Clasp1a, 0F, 0F, 0F);
      Clasp1b = new ModelRenderer(this, 0, 7);
      Clasp1b.addBox(0F, 0F, 0F, 2, 1, 2);
      Clasp1b.setRotationPoint(-1F, -1F, 3F);
      Clasp1b.setTextureSize(32, 32);
      Clasp1b.mirror = true;
      setRotation(Clasp1b, 0F, 0F, 0F);
      Clasp2b = new ModelRenderer(this, 0, 19);
      Clasp2b.addBox(0F, 0F, 0F, 2, 1, 2);
      Clasp2b.setRotationPoint(3F, -1F, -1F);
      Clasp2b.setTextureSize(32, 32);
      Clasp2b.mirror = true;
      setRotation(Clasp2b, 0F, 0F, 0F);
      Clasp2a = new ModelRenderer(this, 0, 3);
      Clasp2a.addBox(0F, 0F, 0F, 1, 2, 2);
      Clasp2a.setRotationPoint(2F, -2F, -1F);
      Clasp2a.setTextureSize(32, 32);
      Clasp2a.mirror = true;
      setRotation(Clasp2a, 0F, 0F, 0F);
      Clasp3a = new ModelRenderer(this, 0, 0);
      Clasp3a.addBox(0F, 0F, 0F, 2, 2, 1);
      Clasp3a.setRotationPoint(-1F, -2F, -3F);
      Clasp3a.setTextureSize(32, 32);
      Clasp3a.mirror = true;
      setRotation(Clasp3a, 0F, 0F, 0F);
      Clasp3b = new ModelRenderer(this, 0, 7);
      Clasp3b.addBox(0F, 0F, 0F, 2, 1, 2);
      Clasp3b.setRotationPoint(-1F, -1F, -5F);
      Clasp3b.setTextureSize(32, 32);
      Clasp3b.mirror = true;
      setRotation(Clasp3b, 0F, 0F, 0F);
      Clasp4a = new ModelRenderer(this, 0, 3);
      Clasp4a.addBox(0F, 0F, 0F, 1, 2, 2);
      Clasp4a.setRotationPoint(-3F, -2F, -1F);
      Clasp4a.setTextureSize(32, 32);
      Clasp4a.mirror = true;
      setRotation(Clasp4a, 0F, 0F, 0F);
      Clasp4b = new ModelRenderer(this, 0, 19);
      Clasp4b.addBox(0F, 0F, 0F, 2, 1, 2);
      Clasp4b.setRotationPoint(-5F, -1F, -1F);
      Clasp4b.setTextureSize(32, 32);
      Clasp4b.mirror = true;
      setRotation(Clasp4b, 0F, 0F, 0F);
      Base = new ModelRenderer(this, 0, 23);
      Base.addBox(0F, 0F, 0F, 4, 1, 2);
      Base.setRotationPoint(-2F, -1F, -1F);
      Base.setTextureSize(32, 32);
      Base.mirror = true;
      setRotation(Base, 0F, 0F, 0F);
      BaseS1 = new ModelRenderer(this, 0, 27);
      BaseS1.addBox(0F, 0F, 0F, 2, 1, 1);
      BaseS1.setRotationPoint(-1F, -1F, 1F);
      BaseS1.setTextureSize(32, 32);
      BaseS1.mirror = true;
      setRotation(BaseS1, 0F, 0F, 0F);
      BaseS2 = new ModelRenderer(this, 0, 27);
      BaseS2.addBox(0F, 0F, 0F, 2, 1, 1);
      BaseS2.setRotationPoint(-1F, -1F, -2F);
      BaseS2.setTextureSize(32, 32);
      BaseS2.mirror = true;
      setRotation(BaseS2, 0F, 0F, 0F);
  }
  
  public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5, boolean isEmpty, int type)
  {
    super.render(entity, f, f1, f2, f3, f4, f5);
    setRotationAngles(f, f1, f2, f3, f4, f5, entity);

    if(!isEmpty)
    {    	
    	if(type == EntityIMTrap.TRAP_RIFT)
    		Core.render(f5);
    	else if(type == EntityIMTrap.TRAP_FIRE)
    		CoreFire.render(f5);
    }
    
    Clasp1a.render(f5);
    Clasp1b.render(f5);
    Clasp2b.render(f5);
    Clasp2a.render(f5);
    Clasp3a.render(f5);
    Clasp3b.render(f5);
    Clasp4a.render(f5);
    Clasp4b.render(f5);
    Base.render(f5);
    BaseS1.render(f5);
    BaseS2.render(f5);
  }
  
  @Override
public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
  {
	  render(entity, f, f1, f2, f3, f4, f5, false, EntityIMTrap.TRAP_DEFAULT);
  }
  
  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = x;
    model.rotateAngleY = y;
    model.rotateAngleZ = z;
  }

  @Override
public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
  {
    super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
  }

}
