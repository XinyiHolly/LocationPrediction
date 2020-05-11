package dbscan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.tweet.VdCoordinate;
import org.tweet.location.ClusteredLocation;

public class VDbscan {

	public static int minpt = 4;
	public static List<List<VdCoordinate>> resultList = new ArrayList<>();
	public static List<VdCoordinate> pointList = new ArrayList<>();
	public static List<VdCoordinate> noiseList = new ArrayList<>();
	public static List<VdCoordinate> Neighbours;		
	
	public static int getMode(List<Integer> array)
	{
	    HashMap<Integer,Integer> hm = new HashMap<Integer,Integer>();
	    int max  = 1;
	    int temp = 0;

	    for(int i = 0; i < array.size(); i++) {

	        if (hm.get(array.get(i)) != null) {

	            int count = hm.get(array.get(i));
	            count++;
	            hm.put(array.get(i), count);

	            if(count > max) {
	                max  = count;
	                temp = array.get(i);
	            }
	        }
	        else 
	            hm.put(array.get(i),1);
	    }
	    return temp;
	}
	
	public static int getMode(List<Integer> array, int rank)
	{
		int temp = 0;
		HashMap<Integer,Integer> hm = new HashMap<Integer,Integer>(); // key k; value: count;	   
	    for(int i = 0; i < array.size(); i++) {

	        if (hm.get(array.get(i)) != null) {

	            int count = hm.get(array.get(i));
	            count++;
	            hm.put(array.get(i), count);
	        }
	        else 
	            hm.put(array.get(i),1);
	    }
	    
	    hm = sortByComparator(hm);	        
		
		while (rank > 0) {
			
			//System.out.println("get k mode: ");
			List<Integer> keys = new ArrayList<>(hm.keySet());	
		    
			// use up possible k while no cluster found
		    if(keys.isEmpty())
		    	return -2;
				    
		    temp = (int) keys.get(0);
		    int max  = hm.get(temp);
		    for(int i = 0; i < keys.size(); i++) {
				int key = (int) keys.get(i);			
				if (max - hm.get(key) < 5 && key > temp) {
					temp = key;
				}
				//System.out.println(key + " = " + hm.get(key));
			}
		    
/*		    Iterator<Entry<Integer, Integer>> it = hm.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry<Integer, Integer> pair = (Map.Entry<Integer, Integer>)it.next();
		    }
			*/
			rank --;
			hm.remove(temp);
		}
			     
	    return temp;
	}
	
	Comparator<ClusteredLocation> comparator = new Comparator<ClusteredLocation>()
	{
        @Override
        public int compare(ClusteredLocation l1, ClusteredLocation l2) {	        	
	        return (int) (l1.getStartTime() - l2.getStartTime());
        }
	};
	
	private static HashMap<Integer, Integer> sortByComparator(Map<Integer, Integer> unsortMap)
    {
        List<Entry<Integer, Integer>> list = new LinkedList<Entry<Integer, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<Integer, Integer>>()
        {
            public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2)
            {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        // Maintaining insertion order with the help of LinkedList
        HashMap<Integer, Integer> sortedMap = new LinkedHashMap<Integer, Integer>();
        for (Entry<Integer, Integer> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
	
	public static double getDoubleArrayMode(List<Double> array)
	{
	    HashMap<Double,Integer> hm = new HashMap<Double,Integer>();
	    int max  = 1;
	    double temp = 0.0;

	    for(int i = 0; i < array.size(); i++) {

	        if (hm.get(array.get(i)) != null) {

	            int count = hm.get(array.get(i));
	            count++;
	            hm.put(array.get(i), count);

	            if(count > max) {
	                max  = count;
	                temp = array.get(i);
	            }
	        }
	        else 
	            hm.put(array.get(i),1);
	    }
	    return temp;
	}
	
	// Added by Xinyi @ 2018-02-18
    public static List<VdCoordinate> getNoiseList(){
		
		noiseList.clear();
		
		int totalPoints = VDbscan.pointList.size();
		//System.out.println(totalPoints);
		
		for(int i = 0; i < totalPoints; i++)
		{
			VdCoordinate m = pointList.get(i);
			boolean bFound = false;
			for(int j = 0; j < VDbscan.resultList.size(); j++)
			{
				List<VdCoordinate> clusterList = VDbscan.resultList.get(j);
				for(int k = 0; k < clusterList.size(); k++)
				{
					VdCoordinate n = clusterList.get(k);
					if(Utility.equalSTPoints(m, n))
					{
						bFound = true;
						break;
					}
				}
				if(bFound)
					break;
			}
			
			if(!bFound)
				noiseList.add(m);
		}
				
		return noiseList;		
	}
	
	public static List<List<VdCoordinate>> applyVDbscan() {
		resultList.clear();
		VUtility.VisitList.clear();
		int index = 0;
		while(pointList.size() > index) {
			VdCoordinate p = pointList.get(index);
			if (!VUtility.isVisited(p)) {
				VUtility.Visited(p);
				Neighbours = VUtility.getNeighbours(p);
				if (Neighbours.size() >= minpt) {
					int ind = 0;
					while(Neighbours.size() > ind) {
						VdCoordinate r = Neighbours.get(ind);
						if(!VUtility.isVisited(r)) {
							VUtility.Visited(r);
							List<VdCoordinate> Neighbours2 = VUtility.getNeighbours(r);
							if (Neighbours2.size() >= minpt)
							{
								Neighbours = VUtility.Merge(Neighbours, Neighbours2);
							}
						}
						ind++;
					}
					resultList.add(Neighbours);
				}
			}
			index++;
		}
		return resultList;
	}
}
