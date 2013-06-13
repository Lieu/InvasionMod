package mods.invmod.common.util;

public class PolarAngle implements IPolarAngle
{	
	public PolarAngle(int angle)
	{
		this.angle = angle;
	}
	
	@Override
	public int getAngle()
	{
		return angle;
	}
	
	public void setAngle(int angle)
	{
		this.angle = angle;
	}
	
	private int angle;
}
