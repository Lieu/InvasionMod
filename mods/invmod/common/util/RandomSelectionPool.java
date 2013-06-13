package mods.invmod.common.util;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mods.invmod.common.mod_Invasion;


/**
 * A pool of entries that are randomly returned according to
 * their probability weights
 */
public class RandomSelectionPool<T> implements ISelect<T>
{
	public RandomSelectionPool()
	{
		pool = new ArrayList<Pair<ISelect<T>, Float>>();
		totalWeight = 0;
		rand = new Random();
		//pool.add(new Object());
	}
	
	/**
	 * Adds a new entry to the pool, with a probability weight
	 */
	public void addEntry(T entry, float weight)
	{
		SingleSelection<T> selection = new SingleSelection<T>(entry);
		addEntry(selection, weight);
	}
	
	/**
	 * Adds a new entry to the pool, with a probability weight
	 */
	public void addEntry(ISelect<T> entry, float weight)
	{
		pool.add(new Pair<ISelect<T>, Float>(entry, weight));
		totalWeight += weight;
	}
	
	@Override
	public T selectNext()
	{
		float r = rand.nextFloat() * totalWeight;
		for(Pair<ISelect<T>, Float> entry : pool)
		{
			if(r < entry.getVal2())
			{
				return entry.getVal1().selectNext();
			}
			else
			{
				r -= entry.getVal2();
			}
		}
		
		// Determine way to fail
		if(pool.size() > 0)
		{
			mod_Invasion.log("RandomSelectionPool invalid setup or rounding error. Failing safe.");
			return pool.get(0).getVal1().selectNext();
		}
		return null;
	}
	
	@Override
	public RandomSelectionPool<T> clone()
	{
		RandomSelectionPool<T> clone = new RandomSelectionPool<T>();
		for(Pair<ISelect<T>, Float> entry : pool)
		{
			clone.addEntry(entry.getVal1(), entry.getVal2());
		}
		
		return clone;
	}
	
	@Override
	public void reset() { }
	
	@Override
	public String toString()
	{
		String s = "RandomSelectionPool@" + Integer.toHexString(hashCode()) + "#Size=" + pool.size();
		for(int i = 0; i < pool.size(); i++)
		{
			s += "\n\tEntry " + i + "   Weight: " + pool.get(i).getVal2();
			s += "\n\t" + pool.get(i).getVal1().toString();
		}
		return s;
	}
	
	private List<Pair<ISelect<T>, Float>> pool;
	private float totalWeight;
	private Random rand;
}
