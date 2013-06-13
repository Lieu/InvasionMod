package mods.invmod.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A pool of entries that are semi-randomly returned. Probability
 * of selection is proportional to amount remaining in the pool.
 * The pool is refreshed once all entries have been exhausted.
 */
public class FiniteSelectionPool<T> implements ISelect<T>
{
	public FiniteSelectionPool()
	{
		currentPool = new ArrayList<Pair<ISelect<T>, Integer>>();
		originalPool = new ArrayList<Integer>();
		totalAmount = 0;
		rand = new Random();
	}
	
	/**
	 * Adds a new entry to the pool, with the amount of times it can be selected.
	 */
	public void addEntry(T entry, int amount)
	{
		SingleSelection<T> selection = new SingleSelection<T>(entry);
		addEntry(selection, amount);
	}
	
	/**
	 * Adds a new entry to the pool, with the amount of times it can be selected.
	 */
	public void addEntry(ISelect<T> entry, int amount)
	{
		// Order is guaranteed by List; both pools will have consistent indexes (without implementation error)
		currentPool.add(new Pair<ISelect<T>, Integer>(entry, amount));
		originalPool.add(amount);
		originalAmount = totalAmount += amount;
	}
	
	@Override
	public T selectNext()
	{
		if(totalAmount < 1)
			regeneratePool();
		
		float r = rand.nextInt(totalAmount);
		for(Pair<ISelect<T>, Integer> entry : currentPool)
		{
			int amountLeft = entry.getVal2();
			if(r < amountLeft)
			{
				entry.setVal2(amountLeft - 1);
				totalAmount--;
				return entry.getVal1().selectNext();
			}
			else
			{
				r -= amountLeft;
			}
		}
		
		return null;
	}
	
	@Override
	public FiniteSelectionPool<T> clone()
	{
		FiniteSelectionPool<T> clone = new FiniteSelectionPool<T>();
		for(int i = 0; i < currentPool.size(); i++)
		{
			clone.addEntry(currentPool.get(i).getVal1(), originalPool.get(i));
		}
		
		return clone;
	}
	
	@Override
	public void reset()
	{
		regeneratePool();
	}
	
	@Override
	public String toString()
	{
		String s = "FiniteSelectionPool@" + Integer.toHexString(hashCode()) + "#Size=" + currentPool.size();
		for(int i = 0; i < currentPool.size(); i++)
		{
			s += "\n\tEntry " + i + "   Amount: " + originalPool.get(i);
			s += "\n\t" + currentPool.get(i).getVal1().toString();
		}
		return s;
	}

	/**
	 *  Returns the amount values in the pool to their original values
	 */
	private void regeneratePool()
	{
		totalAmount = originalAmount;
		for(int i = 0; i < currentPool.size(); i++)
		{
			currentPool.get(i).setVal2(originalPool.get(i));
		}
	}
	
	private List<Pair<ISelect<T>, Integer>> currentPool;
	private List<Integer> originalPool;
	private int totalAmount;
	private int originalAmount;
	private Random rand;
}
