package mods.invmod.client.render.animation;

public class Transition
{
	private AnimationAction newAction;
	private float sourceTime;
	private float destTime;
	
	public Transition(AnimationAction newAction, float sourceTime, float destTime)
	{
		this.newAction = newAction;
		this.sourceTime = sourceTime;
		this.destTime = destTime;
	}
	
	public AnimationAction getNewAction()
	{
		return newAction;
	}
	
	public float getSourceTime()
	{
		return sourceTime;
	}
	
	public float getDestTime()
	{
		return destTime;
	}
}
