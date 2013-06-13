package mods.invmod.client.render;

import mods.invmod.common.entity.EntityIMZombie;
import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

public class RenderIMZombie extends RenderLiving
{
	protected ModelBiped modelBiped;
    protected ModelBigBiped modelBigBiped;

    public RenderIMZombie(ModelBiped model, float par2)
    {
        this(model, par2, 1.0F);
    }

    public RenderIMZombie(ModelBiped model, float par2, float par3)
    {
        super(model, par2);
        modelBiped = model;
        modelBigBiped = new ModelBigBiped();
    }
    
    @Override
	public void doRenderLiving(EntityLiving entity, double par2, double par4, double par6, float par8, float par9)
    {
    	if(entity instanceof EntityIMZombie)
    	{
    		if(((EntityIMZombie)entity).isBigRenderTempHack())
    		{
    			mainModel = modelBigBiped;
    			modelBigBiped.setSneaking(entity.isSneaking());
    		}
    		else
    		{
    			mainModel = modelBiped;
    		}
    		super.doRenderLiving(entity, par2, par4, par6, par8, par9);
    	}
    }
    
    @Override
	protected void preRenderCallback(EntityLiving par1EntityLiving, float par2)
    {
    	float f = ((EntityIMZombie)par1EntityLiving).scaleAmount();
    	GL11.glScalef(f, (2.0F + f) / 3.0F, f);
    }

    @Override
	protected void renderEquippedItems(EntityLiving entity, float par2)
    {
        super.renderEquippedItems(entity, par2);
        ItemStack itemstack = entity.getHeldItem();

        if (itemstack != null)
        {
            GL11.glPushMatrix();
            if(((EntityIMZombie)entity).isBigRenderTempHack())
            	modelBigBiped.itemArmPostRender(0.0625F);
    		else
    			modelBiped.bipedRightArm.postRender(0.0625F);
           
            GL11.glTranslatef(-0.0625F, 0.4375F, 0.0625F);

            if (itemstack.itemID < 256 && RenderBlocks.renderItemIn3d(Block.blocksList[itemstack.itemID].getRenderType()))
            {
                float f = 0.5F;
                GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
                f *= 0.75F;
                GL11.glRotatef(20F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45F, 0.0F, 1.0F, 0.0F);
                GL11.glScalef(f, -f, f);
            }
            else if (itemstack.itemID == Item.bow.itemID)
            {
                float f1 = 0.625F;
                GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
                GL11.glRotatef(-20F, 0.0F, 1.0F, 0.0F);
                GL11.glScalef(f1, -f1, f1);
                GL11.glRotatef(-100F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45F, 0.0F, 1.0F, 0.0F);
            }
            else if (Item.itemsList[itemstack.itemID].isFull3D())
            {
                float f2 = 0.625F;
                GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
                GL11.glScalef(f2, -f2, f2);
                GL11.glRotatef(-100F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45F, 0.0F, 1.0F, 0.0F);
            }
            else
            {
                float f3 = 0.375F;
                GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
                GL11.glScalef(f3, f3, f3);
                GL11.glRotatef(60F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(-90F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(20F, 0.0F, 0.0F, 1.0F);
            }

            renderManager.itemRenderer.renderItem(entity, itemstack, 0);

            if (itemstack.getItem().requiresMultipleRenderPasses())
            {
                for (int x = 1; x < itemstack.getItem().getRenderPasses(itemstack.getItemDamage()); x++)
                {
                    this.renderManager.itemRenderer.renderItem(entity, itemstack, x);
                }
            }

            GL11.glPopMatrix();
        }
    }
}
