package mods.invmod.common.util;

import net.minecraft.util.Vec3;

public class PosRotate3D
{
	public PosRotate3D()
	{
		this(0, 0, 0, 0, 0, 0);
	}
	
	public PosRotate3D(double posX, double posY, double posZ, float rotX, float rotY, float rotZ)
	{
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
	}
	
	public Vec3 getPos()
	{
		return Vec3.createVectorHelper(posX, posY, posZ);
	}
	
	public double getPosX()
	{
		return posX;
	}
	
	public double getPosY()
	{
		return posY;
	}
	
	public double getPosZ()
	{
		return posZ;
	}
	
	public float getRotX()
	{
		return rotX;
	}
	
	public float getRotY()
	{
		return rotY;
	}
	
	public float getRotZ()
	{
		return rotZ;
	}
	
	public void setPosX(double pos)
	{
		posX = pos;
	}
	
	public void setPosY(double pos)
	{
		posY = pos;
	}
	
	public void setPosZ(double pos)
	{
		posZ = pos;
	}
	
	public void setRotX(float rot)
	{
		rotX = rot;
	}
	
	public void setRotY(float rot)
	{
		rotY = rot;
	}
	
	public void setRotZ(float rot)
	{
		rotZ = rot;
	}
	
	private double posX;
	private double posY;
	private double posZ;
	private float rotX;
	private float rotY;
	private float rotZ;
}
