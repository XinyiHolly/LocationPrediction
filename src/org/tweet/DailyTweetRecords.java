package org.tweet;
import java.util.ArrayList;


public class DailyTweetRecords {

	public String strDay;
	public ArrayList<TweetRecord> tweets = new ArrayList<TweetRecord>();
	public ArrayList<TweetRecord> representativeTweets = new ArrayList<TweetRecord>();
	public ArrayList<TransferLocation> transitions = new ArrayList<TransferLocation>();
	public ArrayList<TransitionEx> exTransitions = new ArrayList<TransitionEx>();
	
	public DailyTweetRecords()
	{
		
	}
	
	public void addRepresentativeTweet(TweetRecord record)
	{
		representativeTweets.add(record);
	}
	
	public void addTweet(TweetRecord record)
	{
		tweets.add(record);
	}
	
	public int getTweetNumber()
	{
		return tweets.size();
	}
	
	public void addTransition(TransferLocation tl)
	{
		transitions.add(tl);
	}
	
	public void addExTransition(TransitionEx et)
	{
		exTransitions.add(et);
	}
	
	public int getTransitionNumber()
	{
		return transitions.size();
	}
	
	public int getExTransitionNumber()
	{
		return exTransitions.size();
	}

}
