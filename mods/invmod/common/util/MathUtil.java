package mods.invmod.common.util;

public class MathUtil
{
	public static boolean floatEquals(float f1, float f2, float tolerance)
    {
    	float diff = f1 - f2;
    	if(diff >= 0)
    		return diff < tolerance;
    	else
    		return -diff < tolerance;
    }
	
	/**
	 * Returns an angle bounded to between -pi(inclusive) and pi(exclusive)
	 */
	public static double boundAnglePiRad(double angle)
    {
		angle %= 2*Math.PI;
		if(angle >= Math.PI)
			angle -= 2*Math.PI;
		else if(angle < -Math.PI)
			angle += 2*Math.PI;

        return angle;
    }
	
	/**
	 * Returns an angle bounded to between -180(inclusive) and 180(exclusive)
	 */
	public static double boundAngle180Deg(double angle)
    {
		angle %= 360;
		if(angle >= 180)
			angle -= 360F;
		else if(angle < -180)
			angle += 360;

        return angle;
    }
	
	/**
	 * Calculates a linear interpolation between two angles in degrees where the range of
	 * values is periodic, ie f(-pi) equals f(pi), f(0) = f(2pi), etc. Always returns
	 * the interpolated value within the interval of -pi(inclusive) and pi(exclusive).
	 * 
	 * @param rot1 Value to be interpolated from (where t=0)
	 * @param rot2 Value to be interpolated to (where t=1)
	 * @param t The point at which to calculate the interpolated value
	 * 
	 * @see interpWrapped
	 */
	public static float interpRotationRad(float rot1, float rot2, float t)
    {
        return interpWrapped(rot1, rot2, t, (float)-Math.PI, (float)Math.PI);
    }
	
	/**
	 * Calculates a linear interpolation between two angles in degrees where the range of
	 * values is periodic, ie f(-180) equals f(180), f(0) = f(360), etc. Always returns
	 * the interpolated value within the interval of -180(inclusive) and 180(exclusive).
	 * 
	 * * @param rot1 Value to be interpolated from (where t=0)
	 * @param rot2 Value to be interpolated to (where t=1)
	 * @param t The point at which to calculate the interpolated value
	 * 
	 * @see interpWrapped
	 */
	public static float interpRotationDeg(float rot1, float rot2, float t)
    {
        return interpWrapped(rot1, rot2, t, -180, 180);
    }	
	
	/**
	 * Calculates a linear interpolation between two values where the range of
	 * values is periodic and "wraps around", ie f(t) is semantically equal to
	 * f(t + T), where t is time and T is the period.
	 * 
	 * This function always returns the interpolated value within the specified
	 * minimum(inclusive) and maximum(exclusive) interval: [min, max).
	 * 
	 * @param val1 Value to be interpolated from (where t=0)
	 * @param val2 Value to be interpolated to (where t=1)
	 * @param t The point at which to calculate the interpolated value
	 * @param min The minimum of the range, f(c<sub>0</sub>) where c is some constant
	 * @param max The maximum of the range, f(c<sub>0</sub> + T)
	 */
	public static float interpWrapped(float val1, float val2, float t, float min, float max)
    {
		float dVal = val2 - val1;
        while(dVal < min)
        {
        	dVal += max - min;
        }
        while(dVal >= max)
        {
            dVal -= max - min;
        }
        return val1 + t * dVal;
    }
	
	public static float unpackFloat(int i)
    {
    	return Float.intBitsToFloat(i);
    }
    
	public static int packFloat(float f)
    {
    	return Float.floatToIntBits(f);
    }
	
	public static int packAnglesDeg(float a1, float a2, float a3, float a4)
	{
		return packBytes((byte)(a1 / 360F * 256F), (byte)(a2 / 360F * 256F), (byte)(a3 / 360F * 256F), (byte)(a4 / 360F * 256F));
	}
	
	public static float unpackAnglesDeg_1(int i)
	{
		return unpackBytes_1(i) * 360F / 256F;
	}
	
	public static float unpackAnglesDeg_2(int i)
	{
		return unpackBytes_2(i) * 360F / 256F;
	}
	
	public static float unpackAnglesDeg_3(int i)
	{
		return unpackBytes_3(i) * 360F / 256F;
	}
	
	public static float unpackAnglesDeg_4(int i)
	{
		return unpackBytes_4(i) * 360F / 256F;
	}
	
	public static int packBytes(int i1, int i2, int i3, int i4)
	{
		return ((i1 << 24) & 0xFF000000) | ((i2 << 16) & 0xFF0000) | ((i3 << 8) & 0xFF00) | (i4 & 0xFF);
	}
	
	public static byte unpackBytes_1(int i)
	{
		return (byte)(i >>> 24);
	}
	
	public static byte unpackBytes_2(int i)
	{
		return (byte)((i >>> 16) & 0xFF);
	}
	
	public static byte unpackBytes_3(int i)
	{
		return (byte)((i >>> 8) & 0xFF);
	}
	
	public static byte unpackBytes_4(int i)
	{
		return (byte)(i & 0xFF);
	}
	
	public static int packShorts(int i1, int i2)
	{
		return i1 << 16 | i2 & 0xFFFF;
	}
	
	public static short unhopackSrts_1(int i)
	{
		return (short)(i >>> 16);
	}
	
	public static int unpackShorts_2(int i)
	{
		return (short)(i & 0xFFFF);
	}
}
