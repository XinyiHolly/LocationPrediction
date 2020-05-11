package org.tweet.location;
import java.util.HashMap;
import java.util.Iterator;

///singleton pattern
public class PlaceApiKeyManager {
	
	public HashMap<String, Integer> apiMap = new HashMap<String, Integer>();
	
	private PlaceApiKeyManager(){}
	
	private static class PlaceApiKeyManagerHolder {
		private static final PlaceApiKeyManager INSTANCE = new PlaceApiKeyManager();
	}
	
	public static PlaceApiKeyManager getInstance() {
		return PlaceApiKeyManagerHolder.INSTANCE;
	}
	
	
	public void initialApiKeys()
	{
		apiMap.put("AIzaSyAGdzfVQ2FgPEezyAteYgvruoz7rs6ae6o", 0);
		apiMap.put("AIzaSyBj9C3A-NinGJhwKXwicg9st9PKXqc4sYU", 0);
		apiMap.put("AIzaSyAm67JxZVq78Uxhyj1uUaX0Ar1g0KSQB_w", 0);
		apiMap.put("AIzaSyACePYENjzIdhLlnCU0B4Ty2pA6KtbGVe4", 0);
		apiMap.put("AIzaSyDyb5dmjl8vFeHTC6RFfN3DwMFuupvPpKI", 0);
	}
	
	String getAvailiableApiKey()
	{
		Iterator<String> keySetIterator = apiMap.keySet().iterator();
		while(keySetIterator.hasNext())
		{
			String key = keySetIterator.next();
			int time = apiMap.get(key);
			if(time > 996)
			{
				apiMap.remove(key);
				keySetIterator = apiMap.keySet().iterator();
				continue;
			}
			else
			{
				time++;
				apiMap.put(key, time);
				return key;
			}
		}
		return "";
	}
	
	public int getKeyNum()
	{
		return apiMap.size();
	}
	
	public static void main(String[] args) {
		
		PlaceApiKeyManager.getInstance().initialApiKeys();
		int count = 0;
		while(PlaceApiKeyManager.getInstance().getKeyNum() > 0)
		{
			System.out.println(PlaceApiKeyManager.getInstance().getAvailiableApiKey());
			count++;
		}
		System.out.println(count);
		
	}

}
