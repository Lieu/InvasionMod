
package mods.invmod.common.entity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;


public class EntityIMArrowOld extends Entity
{
    private int xTile;
    private int yTile;
    private int zTile;
    private int inTile;
    private int inData;
    private boolean inGround;
    public boolean doesArrowBelongToPlayer;
    public int arrowShake;
    public Entity shootingEntity;
    private int ticksInGround;
    private int ticksInAir;
    private int targetsHit;
    public boolean arrowCritical;
    
    private List<Integer> entitiesHit;
    
    public EntityIMArrowOld(World world)
    {
        super(world);
        xTile = -1;
        yTile = -1;
        zTile = -1;
        inTile = 0;
        inData = 0;
        targetsHit = 0;
        entitiesHit = new ArrayList<Integer>();
        inGround = false;
        doesArrowBelongToPlayer = false;
        arrowShake = 0;
        ticksInAir = 0;
        arrowCritical = false;
        setSize(0.5F, 0.5F);
        setFire(5);
    }

    public EntityIMArrowOld(World world, double d, double d1, double d2)
    {
        super(world);
        xTile = -1;
        yTile = -1;
        zTile = -1;
        inTile = 0;
        inData = 0;
        targetsHit = 0;
        entitiesHit = new ArrayList<Integer>();
        inGround = false;
        doesArrowBelongToPlayer = false;
        arrowShake = 0;
        ticksInAir = 0;
        arrowCritical = false;
        setSize(0.5F, 0.5F);
        setPosition(d, d1, d2);
        setFire(5);
        yOffset = 0.0F;
    }

    public EntityIMArrowOld(World world, EntityLiving entityliving, float f)
    {
        super(world);
        xTile = -1;
        yTile = -1;
        zTile = -1;
        inTile = 0;
        inData = 0;
        inGround = false;
        doesArrowBelongToPlayer = false;
        arrowShake = 0;
        ticksInAir = 0;
        arrowCritical = false;
        targetsHit = 0;
        entitiesHit = new ArrayList<Integer>();
        shootingEntity = entityliving;
        doesArrowBelongToPlayer = entityliving instanceof EntityPlayer;
        setSize(0.5F, 0.5F);
        setFire(5);
        setLocationAndAngles(entityliving.posX, entityliving.posY + entityliving.getEyeHeight(), entityliving.posZ, entityliving.rotationYaw, entityliving.rotationPitch);
        posX -= MathHelper.cos((rotationYaw / 180F) * 3.141593F) * 0.16F;
        posY -= 0.10000000149011612D;
        posZ -= MathHelper.sin((rotationYaw / 180F) * 3.141593F) * 0.16F;
        setPosition(posX, posY, posZ);
        yOffset = 0.0F;
        motionX = -MathHelper.sin((rotationYaw / 180F) * 3.141593F) * MathHelper.cos((rotationPitch / 180F) * 3.141593F);
        motionZ = MathHelper.cos((rotationYaw / 180F) * 3.141593F) * MathHelper.cos((rotationPitch / 180F) * 3.141593F);
        motionY = -MathHelper.sin((rotationPitch / 180F) * 3.141593F);
        setArrowHeading(motionX, motionY, motionZ, f * 1.5F, 1.0F);
    }

    @Override
	protected void entityInit()
    {
    }

    public void setArrowHeading(double d, double d1, double d2, float f, float f1)
    {
        float f2 = MathHelper.sqrt_double(d * d + d1 * d1 + d2 * d2);
        d /= f2;
        d1 /= f2;
        d2 /= f2;
        d += rand.nextGaussian() * 0.0074999998323619366D * f1;
        d1 += rand.nextGaussian() * 0.0074999998323619366D * f1;
        d2 += rand.nextGaussian() * 0.0074999998323619366D * f1;
        d *= f;
        d1 *= f;
        d2 *= f;
        motionX = d;
        motionY = d1;
        motionZ = d2;
        float f3 = MathHelper.sqrt_double(d * d + d2 * d2);
        prevRotationYaw = rotationYaw = (float)((Math.atan2(d, d2) * 180D) / 3.1415927410125732D);
        prevRotationPitch = rotationPitch = (float)((Math.atan2(d1, f3) * 180D) / 3.1415927410125732D);
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
            prevRotationYaw = rotationYaw = (float)((Math.atan2(motionX, motionZ) * 180D) / 3.1415927410125732D);
            prevRotationPitch = rotationPitch = (float)((Math.atan2(motionY, f) * 180D) / 3.1415927410125732D);
        }
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
        if(arrowShake > 0)
        {
            arrowShake--;
        }
        
        if(inGround)
        {
            int j = worldObj.getBlockId(xTile, yTile, zTile);
            int k = worldObj.getBlockMetadata(xTile, yTile, zTile);
            if(j == inTile && k == inData)
            {
            	ticksInGround++;
                if(ticksInGround == 1200)
                {
                    setDead();
                }
            }
            else
            {
                inGround = false;
                motionX *= rand.nextFloat() * 0.2F;
                motionY *= rand.nextFloat() * 0.2F;
                motionZ *= rand.nextFloat() * 0.2F;
                ticksInGround = 0;
                ticksInAir = 0;
            }
        }
        else
        {
	        ticksInAir++;
	        Vec3 vec3d = Vec3.createVectorHelper(posX, posY, posZ);
	        Vec3 vec3d1 = Vec3.createVectorHelper(posX + motionX, posY + motionY, posZ + motionZ);
	        MovingObjectPosition movingobjectposition = worldObj.rayTraceBlocks_do_do(vec3d, vec3d1, false, true);
	        vec3d = Vec3.createVectorHelper(posX, posY, posZ);
	        vec3d1 = Vec3.createVectorHelper(posX + motionX, posY + motionY, posZ + motionZ);
	        if(movingobjectposition != null)
	        {
	            vec3d1 = Vec3.createVectorHelper(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
	        }
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
	
	        if(entity != null)
	        {
	            movingobjectposition = new MovingObjectPosition(entity);
	        }
	        if(movingobjectposition != null)
	        {
	            if(movingobjectposition.entityHit != null)
	            {
	                float f1 = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
	                int j1 = (int)Math.ceil(f1 * 2D);
	                if(arrowCritical)
	                {
	                    j1 = (j1 * 3) / 2 + 1;
	                }
	                if(movingobjectposition.entityHit instanceof EntityLiving)
	                {
	                	boolean alreadyHit = false;
	                	for(Integer id : entitiesHit)
	                	{
	                		if(movingobjectposition.entityHit.entityId == id)
	                		{
	                			alreadyHit = true;
	                		}
	                	}
	                	
	                	if(!alreadyHit)
	                	{
		                	entitiesHit.add(movingobjectposition.entityHit.entityId);
		                	if(!worldObj.isRemote)
	                        {
	                            EntityLiving entityLiving = (EntityLiving)movingobjectposition.entityHit;
	                            entityLiving.setArrowCountInEntity(entityLiving.getArrowCountInEntity() + 1);
	                        }
		                	if(targetsHit == 0)
		                	{
		                		targetsHit++;
		                		movingobjectposition.entityHit.attackEntityFrom(DamageSource.generic, j1);
		                		movingobjectposition.entityHit.setFire(8);
		                	}
		                	else if(targetsHit < 8)
		                	{
		                		targetsHit++;
		                		movingobjectposition.entityHit.setFire(8);
		                	}
		                	else
		                	{
		                		movingobjectposition.entityHit.setFire(8);
			                	worldObj.playSoundAtEntity(this, "random.drr", 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
			                    setDead();
		                	}
	                	}
	                } else
	                {
	                    motionX *= -0.10000000149011612D;
	                    motionY *= -0.10000000149011612D;
	                    motionZ *= -0.10000000149011612D;
	                    rotationYaw += 180F;
	                    prevRotationYaw += 180F;
	                    ticksInAir = 0;
	                }
	            } else
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
	                posX -= (motionX / f2) * 0.05000000074505806D;
	                posY -= (motionY / f2) * 0.05000000074505806D;
	                posZ -= (motionZ / f2) * 0.05000000074505806D;
	                worldObj.playSoundAtEntity(this, "random.drr", 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
	                inGround = true;
	                arrowShake = 7;
	                arrowCritical = false;
	            }
	        }
	        //if(arrowCritical)
	        {
	            for(int i1 = 0; i1 < 4; i1++)
	            {
	                worldObj.spawnParticle("lava", posX + (motionX * i1) / 4D, posY + (motionY * i1) / 4D, posZ + (motionZ * i1) / 4D, -motionX, -motionY + 0.2D, -motionZ);
	            }
	
	        }
	        posX += motionX;
	        posY += motionY;
	        posZ += motionZ;
	        float f3 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
	        rotationYaw = (float)((Math.atan2(motionX, motionZ) * 180D) / 3.1415927410125732D);
	        for(rotationPitch = (float)((Math.atan2(motionY, f3) * 180D) / 3.1415927410125732D); rotationPitch - prevRotationPitch < -180F; prevRotationPitch -= 360F) { }
	        for(; rotationPitch - prevRotationPitch >= 180F; prevRotationPitch += 360F) { }
	        for(; rotationYaw - prevRotationYaw < -180F; prevRotationYaw -= 360F) { }
	        for(; rotationYaw - prevRotationYaw >= 180F; prevRotationYaw += 360F) { }
	        rotationPitch = prevRotationPitch + (rotationPitch - prevRotationPitch) * 0.2F;
	        rotationYaw = prevRotationYaw + (rotationYaw - prevRotationYaw) * 0.2F;
	        float f4 = 0.99F;
	        float f6 = 0.05F;
	        if(isInWater())
	        {
	            for(int k1 = 0; k1 < 4; k1++)
	            {
	                float f7 = 0.25F;
	                worldObj.spawnParticle("bubble", posX - motionX * f7, posY - motionY * f7, posZ - motionZ * f7, motionX, motionY, motionZ);
	            }
	
	            f4 = 0.8F;
	        }
	        motionX *= f4;
	        motionY *= f4;
	        motionZ *= f4;
	        motionY -= f6;
	        setPosition(posX, posY, posZ);
        }
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
        if(inGround && doesArrowBelongToPlayer && arrowShake <= 0 && entityplayer.inventory.addItemStackToInventory(new ItemStack(Item.arrow, 1)))
        {
            worldObj.playSoundAtEntity(this, "random.pop", 0.2F, ((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            entityplayer.onItemPickup(this, 1);
            setDead();
        }
    }

    @Override
	public float getShadowSize()
    {
        return 0.0F;
    }
}
