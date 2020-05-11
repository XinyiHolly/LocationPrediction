package org.tweet;

public class TimeUnit {

	public int[] count = new int[24];
	public double threshold = 0.5;
	public double upper = 0.0;
	public double lower = 0.0;
	
	public TimeUnit()
	{
		for(int i = 0; i < count.length; i++)
		{
			count[i] = 0;
		}
	}
	
	public boolean isPossible(double previous)
	{
		int current = (int) previous;
		for(int i = current; i < count.length; i++)
		{
			if(count[current] > 0)
				return true;
		}
		return false;
	}
	
	public int GetCount(int index)
	{
		if(index < 0 || index > count.length - 1)
			return -1;
	
		return count[index];
	}
	
	public void SetUpTime(double time)
	{
		upper = time;
	}
	
	public void SetLowTime(double time)
	{
		lower = time;
	}
	
	public double GetUpTime()
	{
		return upper;
	}
	
	public double GetLowTime()
	{
		return lower;
	}
	
	public void SetThreshold(double t)
	{
		threshold = t;
	}
	
	public double GetThreshold()
	{
		return threshold;
	}
	
	public String toString()
	{
		return Double.toString(threshold) + " " +  Double.toString(lower) + " " + Double.toString(upper);
	}
	
}
