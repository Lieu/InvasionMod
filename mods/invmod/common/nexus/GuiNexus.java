// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces deadcode 

package mods.invmod.common.nexus;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;

import org.lwjgl.opengl.GL11;

// Referenced classes of package net.minecraft.src:
//            GuiContainer, ContainerFurnace, FontRenderer, RenderEngine, 
//            TileEntityFurnace, InventoryPlayer

public class GuiNexus extends GuiContainer
{

    public GuiNexus(InventoryPlayer inventoryplayer, TileEntityNexus tileentityNexus)
    {
        super(new ContainerNexus(inventoryplayer, tileentityNexus));
        tileEntityNexus = tileentityNexus;
    }

    @Override
	protected void drawGuiContainerForegroundLayer(int x, int y)
    {
        fontRenderer.drawString("Nexus - Level " + tileEntityNexus.getNexusLevel(), 46, 6, 0x404040);
        fontRenderer.drawString(tileEntityNexus.getNexusKills() + " mobs killed", 96, 60, 0x404040);
        fontRenderer.drawString("R: " + tileEntityNexus.getSpawnRadius(), 142, 72, 0x404040);
        //fontRenderer.drawString("Inventory", 8, (ySize - 96) + 2, 0x404040);
        
        if(tileEntityNexus.getMode() == 1 || tileEntityNexus.getMode() == 3)
        {
        	fontRenderer.drawString("Activated!", 13, 62, 0x404040);
        	fontRenderer.drawString("Wave " + tileEntityNexus.getCurrentWave(), 55, 37, 0x404040);
        }
        else if(tileEntityNexus.getMode() == 2)
        {
        	fontRenderer.drawString("Power:", 56, 31, 0x404040);
        	fontRenderer.drawString("" + tileEntityNexus.getNexusPowerLevel(), 61, 44, 0x404040);
        }
        
        if(tileEntityNexus.isActivating() && tileEntityNexus.getMode() == 0)
        {
        	fontRenderer.drawString("Activating...", 13, 62, 0x404040);       	
        	if(tileEntityNexus.getMode() != 4)
        		fontRenderer.drawString("Are you sure?", 8, 72, 0x404040);
        }
    }

    @Override
	protected void drawGuiContainerBackgroundLayer(float f, int un1, int un2)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture("/mods/invmod/textures/nexusgui.png");
        int j = (width - xSize) / 2;
        int k = (height - ySize) / 2;
        drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
        
        int l = tileEntityNexus.getGenerationProgressScaled(26);
        drawTexturedModalRect(j + 126, (k + 28 + 26) - l, 185, 26 - l, 9, l);
        l = tileEntityNexus.getCookProgressScaled(18);
        drawTexturedModalRect(j + 31, k + 51, 204, 0, l, 2);
        
        if(tileEntityNexus.getMode() == 1 || tileEntityNexus.getMode() == 3)
        {
        	drawTexturedModalRect(j + 19, k + 29, 176, 0, 9, 31);
        	drawTexturedModalRect(j + 19, k + 19, 194, 0, 9, 9);
        }
        else if(tileEntityNexus.getMode() == 2)
        {
        	drawTexturedModalRect(j + 19, k + 29, 176, 31, 9, 31);
        }   
        
        if((tileEntityNexus.getMode() == 0 || tileEntityNexus.getMode() == 2) && tileEntityNexus.isActivating())
        {
            l = tileEntityNexus.getActivationProgressScaled(31);
            drawTexturedModalRect(j + 19, (k + 29 + 31) - l, 176, 31 - l, 9, l);       
        }
        else if(tileEntityNexus.getMode() == 4 && tileEntityNexus.isActivating())
        {
            l = tileEntityNexus.getActivationProgressScaled(31);
            drawTexturedModalRect(j + 19, (k + 29 + 31) - l, 176, 62 - l, 9, l);       
        }
    }

    private TileEntityNexus tileEntityNexus;
}
