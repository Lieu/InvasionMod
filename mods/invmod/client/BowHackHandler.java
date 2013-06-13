package mods.invmod.client;

public class BowHackHandler
{
	private int bowDrawTime;
	private boolean bowDrawing;
	
	public void onUpdate()
	{
		if(bowDrawing)
		{
			bowDrawTime += 50;
		}
	}
	
	public void setBowDrawing(boolean flag)
	{
		bowDrawing = flag;
	}
	
	public void setBowReleased()
	{
		bowDrawing = false;
		bowDrawTime = 0;
	}
	
	public int getDrawTimeLeft()
	{
		return bowDrawTime;
	}
	
	public boolean isBowDrawing()
	{
		return bowDrawing;
	}
}
