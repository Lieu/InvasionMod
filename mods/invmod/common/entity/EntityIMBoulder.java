
package mods.invmod.common.entity;


import java.util.List;

import mods.invmod.common.mod_Invasion;
import mods.invmod.common.nexus.TileEntityNexus;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;


public class EntityIMBoulder extends Entity
{

    public EntityIMBoulder(World world)
    {
        super(world);
        xTile = -1;
        yTile = -1;
        zTile = -1;
        inTile = 0;
        inData = 0;
        life = 60;
        inGround = false;
        doesArrowBelongToPlayer = false;
        arrowShake = 0;
        ticksInAir = 0;
        arrowCritical = false;
        setSize(0.5F, 0.5F);
    }

    public EntityIMBoulder(World world, double d, double d1, double d2)
    {
        super(world);
        xTile = -1;
        yTile = -1;
        zTile = -1;
        inTile = 0;
        inData = 0;
        life = 60;
        inGround = false;
        doesArrowBelongToPlayer = false;
        arrowShake = 0;
        ticksInAir = 0;
        arrowCritical = false;
        setSize(0.5F, 0.5F);
        setPosition(d, d1, d2);
        yOffset = 0.0F;
    }

    public EntityIMBoulder(World world, EntityLiving entityliving, float f)
    {
        super(world);
        xTile = -1;
        yTile = -1;
        zTile = -1;
        inTile = 0;
        inData = 0;
        life = 60;
        inGround = false;
        doesArrowBelongToPlayer = false;
        arrowShake = 0;
        ticksInAir = 0;
        arrowCritical = false;
        shootingEntity = entityliving;
        doesArrowBelongToPlayer = entityliving instanceof EntityPlayer;
        setSize(0.5F, 0.5F);
        setLocationAndAngles(entityliving.posX, entityliving.posY + entityliving.getEyeHeight(), entityliving.posZ, entityliving.rotationYaw, entityliving.rotationPitch);
        posX -= MathHelper.cos((rotationYaw / 180F) * (float)Math.PI) * 0.16F;
        posY -= 0.1D;
        posZ -= MathHelper.sin((rotationYaw / 180F) * (float)Math.PI) * 0.16F;
        setPosition(posX, posY, posZ);
        yOffset = 0.0F;
        motionX = -MathHelper.sin((rotationYaw / 180F) * (float)Math.PI) * MathHelper.cos((rotationPitch / 180F) * (float)Math.PI);
        motionZ = MathHelper.cos((rotationYaw / 180F) * (float)Math.PI) * MathHelper.cos((rotationPitch / 180F) * (float)Math.PI);
        motionY = -MathHelper.sin((rotationPitch / 180F) * (float)Math.PI);
        setBoulderHeading(motionX, motionY, motionZ, f, 1.0F);
    }

    @Override
	protected void entityInit()
    {
    }

    public void setBoulderHeading(double x, double y, double z, float speed, float variance)
    {
    	//Get vector of magnitude 1.0
        float distance = MathHelper.sqrt_double(x * x + y * y + z * z);
        x /= distance;
        y /= distance;
        z /= distance;
        
        x += rand.nextGaussian() * variance;
        y += rand.nextGaussian() * variance;
        z += rand.nextGaussian() * variance;
        x *= speed;
        y *= speed;
        z *= speed;
        motionX = x;
        motionY = y;
        motionZ = z;
        float xzDistance = MathHelper.sqrt_double(x * x + z * z);
        prevRotationYaw = rotationYaw = (float)((Math.atan2(x, z) * 180D) / Math.PI);
        prevRotationPitch = rotationPitch = (float)((Math.atan2(y, xzDistance) * 180D) / Math.PI);
        ticksInGround = 0;
    }

    @Override
	public void setVelocity(double d, double d1, double d2)
    {
        motionX = d;
        motionY = d1;
        motionZ = d2;
        if(prevRotationPitch == 0.0F && prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt_double(d * d + d2 * d2);
            prevRotationYaw = rotationYaw = (float)((Math.atan2(d, d2) * 180D) / 3.1415927410125732D);
            prevRotationPitch = rotationPitch = (float)((Math.atan2(d1, f) * 180D) / 3.1415927410125732D);
            prevRotationPitch = rotationPitch;
            prevRotationYaw = rotationYaw;
            setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
            ticksInGround = 0;
        }
    }
    
    @Override
	public void onUpdate()
    {
        super.onUpdate();
        if(prevRotationPitch == 0.0F && prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
            prevRotationYaw = rotationYaw = (float)((Math.atan2(motionX, motionZ) * 180D) / Math.PI);
            prevRotationPitch = rotationPitch = (float)((Math.atan2(motionY, f) * 180D) / Math.PI);
        }
        
        //Set state to in ground if it is
        int i = worldObj.getBlockId(xTile, yTile, zTile);
        if(i > 0)
        {
            Block.blocksList[i].setBlockBoundsBasedOnState(worldObj, xTile, yTile, zTile);
            AxisAlignedBB axisalignedbb = Block.blocksList[i].getCollisionBoundingBoxFromPool(worldObj, xTile, yTile, zTile);
            if(axisalignedbb != null && axisalignedbb.isVecInside(Vec3.createVectorHelper(posX, posY, posZ)))
            {
                inGround = true;
            }
        }
        
        //Handle in ground
        if(inGround || life-- <= 0)
        {
        	setDead();
        	return;
        }
        
        ticksInAir++;
        
        //Find vector for next position, including block collision
        Vec3 vec3d = Vec3.createVectorHelper(posX, posY, posZ);
        Vec3 vec3d1 = Vec3.createVectorHelper(posX + motionX, posY + motionY, posZ + motionZ);
        MovingObjectPosition movingobjectposition = worldObj.rayTraceBlocks_do_do(vec3d, vec3d1, false, true);
        vec3d = Vec3.createVectorHelper(posX, posY, posZ);
        vec3d1 = Vec3.createVectorHelper(posX + motionX, posY + motionY, posZ + motionZ);
        if(movingobjectposition != null)
        {
            vec3d1 = Vec3.createVectorHelper(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
        }
        
        //Check for collision with entities
        Entity entity = null;
        List list = worldObj.getEntitiesWithinAABBExcludingEntity(this, boundingBox.addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
        double d = 0.0D;
        for(int l = 0; l < list.size(); l++)
        {
            Entity entity1 = (Entity)list.get(l);
            if(!entity1.canBeCollidedWith() || entity1 == shootingEntity && ticksInAir < 5)
            {
                continue;
            }
            float f5 = 0.3F;
            AxisAlignedBB axisalignedbb1 = entity1.boundingBox.expand(f5, f5, f5);
            MovingObjectPosition movingobjectposition1 = axisalignedbb1.calculateIntercept(vec3d, vec3d1);
            if(movingobjectposition1 == null)
            {
                continue;
            }
            double d1 = vec3d.distanceTo(movingobjectposition1.hitVec);
            if(d1 < d || d == 0.0D)
            {
                entity = entity1;
                d = d1;
            }
        }

        // If collision, handle it
        if(entity != null)
        {
            movingobjectposition = new MovingObjectPosition(entity);
        }
        if(movingobjectposition != null)
        {
        	// Handle entity hit
            if(movingobjectposition.entityHit != null)
            {
            	// Flight time = damage
            	int damage = (int)(Math.max((ticksInAir / 20F), 1F) * 6F);
            	if(damage > 14) { damage = 14; }
                if(movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeMobDamage(shootingEntity), damage))
                {
                    if(movingobjectposition.entityHit instanceof EntityLiving)
                    {
                    	if(!worldObj.isRemote)
                        {
                            EntityLiving entityLiving = (EntityLiving)movingobjectposition.entityHit;
                            entityLiving.setArrowCountInEntity(entityLiving.getArrowCountInEntity() + 1);
                        }
                    }
                    worldObj.playSoundAtEntity(this, "random.explode", 1.0F, 0.9F / (rand.nextFloat() * 0.2F + 0.9F));
                    setDead();
                }
            }
            else // Handle block hit
            {
                xTile = movingobjectposition.blockX;
                yTile = movingobjectposition.blockY;
                zTile = movingobjectposition.blockZ;
                inTile = worldObj.getBlockId(xTile, yTile, zTile);
                inData = worldObj.getBlockMetadata(xTile, yTile, zTile);
                motionX = (float)(movingobjectposition.hitVec.xCoord - posX);
                motionY = (float)(movingobjectposition.hitVec.yCoord - posY);
                motionZ = (float)(movingobjectposition.hitVec.zCoord - posZ);
                float f2 = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
                posX -= (motionX / f2) * 0.05D;
                posY -= (motionY / f2) * 0.05D;
                posZ -= (motionZ / f2) * 0.05D;
                worldObj.playSoundAtEntity(this, "random.explode", 1.0F, 0.9F / (rand.nextFloat() * 0.2F + 0.9F));
                inGround = true;
                arrowCritical = false;
                
                //Break block
                int id = worldObj.getBlockId(xTile, yTile, zTile);
                Block block = Block.blocksList[id];
                if(id == mod_Invasion.blockNexus.blockID)
        		{
                	TileEntityNexus tileEntityNexus = (TileEntityNexus)worldObj.getBlockTileEntity(xTile, yTile, zTile);
                	if(tileEntityNexus != null)
                	{
                		tileEntityNexus.attackNexus(2);
                	}
        		}
        		else if(id != Block.bedrock.blockID)
        		{
	            	if(block != null && id != mod_Invasion.blockNexus.blockID && id != 54)
	            	{
            			if(EntityIMLiving.getBlockSpecial(id) == BlockSpecial.DEFLECTION_1 && rand.nextInt(2) == 0)
            			{
            				setDead();
            				return;
            			}
	        			int meta = worldObj.getBlockMetadata(xTile, yTile, zTile);
	        			worldObj.setBlock(xTile, yTile, zTile, 0);
	        			block.onBlockDestroyedByPlayer(worldObj, xTile, yTile, zTile, meta);
	        			block.dropBlockAsItem(worldObj, xTile, yTile, zTile, meta, 0);
	            	}
        		}
            }
        }
        
        //Crit particles
        if(arrowCritical)
        {
            for(int i1 = 0; i1 < 4; i1++)
            {
                worldObj.spawnParticle("crit", posX + (motionX * i1) / 4D, posY + (motionY * i1) / 4D, posZ + (motionZ * i1) / 4D, -motionX, -motionY + 0.2D, -motionZ);
            }

        }
        
        //Perform movement
        posX += motionX;
        posY += motionY;
        posZ += motionZ;
        
        //Calculate new velocity and bound angles to valid range
        float xyVelocity = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
        rotationYaw = (float)((Math.atan2(motionX, motionZ) * 180D) / Math.PI);
        for(rotationPitch = (float)((Math.atan2(motionY, xyVelocity) * 180D) / Math.PI); rotationPitch - prevRotationPitch < -180F; prevRotationPitch -= 360F) { }
        for(; rotationPitch - prevRotationPitch >= 180F; prevRotationPitch += 360F) { }
        for(; rotationYaw - prevRotationYaw < -180F; prevRotationYaw -= 360F) { }
        for(; rotationYaw - prevRotationYaw >= 180F; prevRotationYaw += 360F) { }
        rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
        rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
        float airResistance = 1.0F;
        float gravityAcel = 0.025F;
        if(isInWater())
        {
            for(int k1 = 0; k1 < 4; k1++)
            {
                float f7 = 0.25F;
                worldObj.spawnParticle("bubble", posX - motionX * f7, posY - motionY * f7, posZ - motionZ * f7, motionX, motionY, motionZ);
            }

            airResistance = 0.8F;
        }
        motionX *= airResistance;
        motionY *= airResistance;
        motionZ *= airResistance;
        motionY -= gravityAcel;
        setPosition(posX, posY, posZ);
    }
    
    @Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
        nbttagcompound.setShort("xTile", (short)xTile);
        nbttagcompound.setShort("yTile", (short)yTile);
        nbttagcompound.setShort("zTile", (short)zTile);
        nbttagcompound.setByte("inTile", (byte)inTile);
        nbttagcompound.setByte("inData", (byte)inData);
        nbttagcompound.setByte("shake", (byte)arrowShake);
        nbttagcompound.setByte("inGround", (byte)(inGround ? 1 : 0));
        nbttagcompound.setBoolean("player", doesArrowBelongToPlayer);
    }

    @Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
        xTile = nbttagcompound.getShort("xTile");
        yTile = nbttagcompound.getShort("yTile");
        zTile = nbttagcompound.getShort("zTile");
        inTile = nbttagcompound.getByte("inTile") & 0xff;
        inData = nbttagcompound.getByte("inData") & 0xff;
        arrowShake = nbttagcompound.getByte("shake") & 0xff;
        inGround = nbttagcompound.getByte("inGround") == 1;
        doesArrowBelongToPlayer = nbttagcompound.getBoolean("player");
    }

    @Override
	public void onCollideWithPlayer(EntityPlayer entityplayer)
    {
        if(worldObj.isRemote)
        {
            return;
        }
    }

    @Override
	public float getShadowSize()
    {
        return 0.0F;
    }
    
    public int getFlightTime()
    {
    	return ticksInAir;
    }

    private int xTile;
    private int yTile;
    private int zTile;
    private int inTile;
    private int inData;
    private boolean inGround;
    private int life;
    public boolean doesArrowBelongToPlayer;
    public int arrowShake;
    public EntityLiving shootingEntity;
    private int ticksInGround;
    private int ticksInAir;
    public boolean arrowCritical;
}
