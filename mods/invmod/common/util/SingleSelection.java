package mods.invmod.common.util;

public class SingleSelection<T> implements ISelect<T>
{
	public SingleSelection(T object)
	{
		this.object = object;
	}
	
	@Override
	public T selectNext()
	{
		return object;
	}
	
	@Override
	public void reset() { }
	
	@Override
	public String toString()
	{
		return object.toString();
	}
	
	private T object;
}
