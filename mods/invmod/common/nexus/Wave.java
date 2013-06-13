
package mods.invmod.common.nexus;

import java.util.List;

public class Wave
{	
	public Wave(int waveTotalTime, int waveBreakTime, List<WaveEntry> entries)
	{
		this.entries = entries;
		this.waveTotalTime = waveTotalTime;
		this.waveBreakTime = waveBreakTime;
		elapsed = 0;
	}
	
	public void addEntry(WaveEntry entry)
	{
		entries.add(entry);
	}
	
	public int doNextSpawns(int elapsedMillis, ISpawnerAccess spawner)
	{
		int numberOfSpawns = 0;
		elapsed += elapsedMillis;
		for(WaveEntry entry : entries)
		{
			if(elapsed >= entry.getTimeBegin() && elapsed < entry.getTimeEnd())
			{
				numberOfSpawns += entry.doNextSpawns(elapsedMillis, spawner);
			}
		}
		return numberOfSpawns;
	}

	public int getTimeInWave()
	{
		return elapsed;
	}

	public int getWaveTotalTime()
	{
		return waveTotalTime;
	}

	public int getWaveBreakTime()
	{
		return waveBreakTime;
	}
	
	public boolean isComplete()
	{
		return elapsed > waveTotalTime;
	}
	
	public void resetWave()
	{
		elapsed = 0;
		for(WaveEntry entry : entries)
		{
			entry.resetToBeginning();
		}
	}
	
	public void setWaveToTime(int millis)
	{
		elapsed = millis;
	}
	
	public int getTotalMobAmount()
	{
		int total = 0;
		for(WaveEntry entry : entries)
		{
			total += entry.getAmount();
		}
		return total;
	}
	
	private List<WaveEntry> entries;
	private int elapsed;
	private int waveTotalTime;
	private int waveBreakTime;
}