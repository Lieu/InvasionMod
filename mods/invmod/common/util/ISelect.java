package mods.invmod.common.util;

public interface ISelect<T>
{
	/**
	 * Returns a next selection as determined by
	 * the object
	 */
	public T selectNext();
	
	/**
	 * Sets to an initial state if it had one
	 */
	public void reset();
}
