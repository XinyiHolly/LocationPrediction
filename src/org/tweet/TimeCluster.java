package org.tweet;

import java.util.ArrayList;
import org.tweet.TweetRecord;


public class TimeCluster {

	public double threshold = 0.8;
	public int sectionNum = 24;
	public int[] section = new int[24];
	public ArrayList<TweetRecord> tweets = new ArrayList<TweetRecord>();
	public double lowerTime = 0;
	public double upperTime = 24;
	public double nightStartTime = 18.0;
	public double nightEndTime = 24.0;
	
	
	public TimeCluster(double t)
	{
		threshold = t;
	}
	
	public void getTweetsData(ArrayList<TweetRecord> records)
	{
		tweets.clear();
		
		for(int i = 0; i < records.size(); i++)
		{
			TweetRecord r = records.get(i);
			tweets.add(r);
		}
	}
	
	public void getTweetRecords(ArrayList<TweetRecord> tweetArray)
	{
		for(int i = 0; i < tweetArray.size(); i++)
		{
			tweets.add(tweetArray.get(i));
		}
	}
	
	/**calculate the upper and lower time range*/
	public void calculateSectionData()
	{
		System.out.println("total point size: " +  tweets.size());
		for(int i = 0; i < sectionNum; i++)
		{
			section[i] = 0;
		}
		
		for(int i = 0; i < tweets.size(); i++)
		{
			int h = (int) tweets.get(i).getTime();
			
			//System.out.println(" hour is " + "," + h );;
			section[h]++;
		}
		
		ArrayList<Integer> sectionArray = new ArrayList<Integer>();
		int max = -1;
		int maxone = 0;
		for(int i = 0; i < sectionNum; i++)
		{
			if(section[i] > max)
			{
				max = section[i];
				maxone = i;
			}
		}
		
	
		
		sectionArray.add(maxone);
		double v = (double) max / (double) tweets.size();
		
		int lower = maxone - 1;
		int upper = maxone + 1;
		
		System.out.println(" maxone and v is " + "," + maxone  + "," + v);
		while(v < threshold)
		{
			if(lower < 0)
			{
				v = v + (double)section[upper] / (double) tweets.size();
				sectionArray.add(upper);
				upper++;
				continue;
			}
			
			if(upper > 23)
			{
				v = v + (double)section[lower] / (double) tweets.size();
				sectionArray.add(lower);
				lower--;
				continue;	
			}
			
			if(section[lower] > section[upper])
			{
				v = v + (double) section[lower] / (double) tweets.size();
				if(section[lower] > 0)
					sectionArray.add(lower);
				lower--;
			}
		    else if(section[lower] == section[upper])
		    {
			    if(section[lower] > 0 && section[upper] > 0)
			    {
			    	v = v + (double) section[lower] / (double) tweets.size();
			    	v = v + (double) section[upper] / (double) tweets.size();
				    sectionArray.add(lower);
				    sectionArray.add(upper);			   
			    }
			    lower--;upper++;
		    }
		    else
		    {
			    v = v + (double)section[upper] / (double)tweets.size();
			    if(section[upper] > 0)
					 sectionArray.add(upper);
			    upper++;	   
		    }
		}
		
	   int count = sectionArray.size();
	   
	   int maxi = -1;
	   int mini = 24;
	   for(int i = 0; i < count; i++)
	   {
			if(sectionArray.get(i) > maxi)
				maxi = sectionArray.get(i);
			if(sectionArray.get(i) < mini)
				mini = sectionArray.get(i);
	   }

	   lowerTime = mini;
	   upperTime = maxi;
	   //System.out.println(Double.toString(lowerTime) + "-" + Double.toString(upperTime));
	}
	
	public double getPercentAtNight()
	{
		double percent = 0.0;
		int tweetNumAtNight = 0;
		for(int i = 0; i < tweets.size(); i++)
		{
			double time = tweets.get(i).getTime();
			if(time > nightStartTime && time < nightEndTime)
			{
				tweetNumAtNight++;
			}
		}
		percent = ((double) tweetNumAtNight / (double) tweets.size()) * 100.0;
		return percent;
	}
	
	
	public String getIntensiveTimeSpan()
	{
		String strSpan = Double.toString(lowerTime) + "," + Double.toString(upperTime);
		return strSpan;
	}

}
