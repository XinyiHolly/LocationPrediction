package org.tweet;

public class TransferLocation {

	public int fromId;
	public int endId;
	
	public TransferLocation(int f, int e)
	{
		fromId = f;
		endId = e;
	}
	
	public void setFromId(int id)
	{
		fromId = id;
	}
	
	public void setEndId(int id)
	{
		endId = id;
	}
	
	public int GetFromId()
	{
		return fromId;
	}
	
	public int GetEndId()
	{
		return endId;
	}
	
	public String toString()
	{
		return Integer.toString(fromId) + " -> " + Integer.toString(endId);
	}

}
