package dbscan;


import java.io.IOException;
import java.util.*;

import org.tweet.TweetRecord;


public class dbscan {
	private double e;
	private int minpt;	
	private List<List<?>> resultList;
	private List<TweetRecord> pointList;
	private List<TweetRecord> noiseList;
	private List<TweetRecord> Neighbours;
	
	public dbscan(double eps, int minPts) {
		e = eps;
		minpt = minPts;
		resultList = new ArrayList<>();
		pointList = new ArrayList<>();
		noiseList = new ArrayList<>();
	}
	
	public void addPoint(TweetRecord p) {
		pointList.add(p);
	}
	
	public void clearPoints() {
		pointList.clear();
	}

	public int getMaxiClusterIndex() {
		int maxIndex = 0;
		int maxptSize = 0;
		for(int i = 0; i <resultList.size(); i++) {
			List<?> clusterList = resultList.get(i);
			int ptSize = clusterList.size();
			if(maxptSize < ptSize) {
				maxptSize = ptSize;
				maxIndex = i;
			}
		}	
		return maxIndex;
	}
	
	public List<TweetRecord> getNoiseList() {		
		noiseList.clear();		
		int totalPoints = pointList.size();
		System.out.println(totalPoints);
		
		for(int i = 0; i < totalPoints; i++) {
			TweetRecord m = pointList.get(i);
			boolean bFound = false;
			for(int j = 0; j < resultList.size(); j++) {
				List<?> clusterList = resultList.get(j);
				for(int k = 0; k < clusterList.size(); k++) {
					TweetRecord n = (TweetRecord) clusterList.get(k);
					if(Utility.equalPoints(m, n)) {
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
	
	public List<List<?>> applyDbscan() throws NumberFormatException, IOException {
		
		resultList.clear();

		Utility.VisitList.clear();

		int index = 0;

		while(pointList.size() > index) {
			
			TweetRecord p = pointList.get(index);

			if(!Utility.isVisited(p)) {
				
				Utility.Visited(p);

				Neighbours = Utility.getNeighbors(p, pointList, e);
				
				if (Neighbours.size() >= minpt) {
					
					int ind = 0;
					
					while(Neighbours.size() > ind) {
						
						TweetRecord r = Neighbours.get(ind);
						
						if(!Utility.isVisited(r)) {
							
							Utility.Visited(r);
							
							List<TweetRecord> Neighbours2 = Utility.getNeighbors(r, pointList, e);
							
							if (Neighbours2.size() >= minpt) {
								
								Neighbours = Utility.Merge(Neighbours, Neighbours2);
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
	
	public List<List<?>> getResultList() {
		return resultList;
	}

	public static void main(String args[]) throws NumberFormatException, IOException {

		//dbscan.pointList = Utility.getListFromDatabase();
		dbscan dbscan = new dbscan(20.0, 4);
		
		dbscan.applyDbscan();

		System.out.println(dbscan.resultList.size());

		for(int i = 0; i < dbscan.resultList.size(); i++) {
			
			List<?> l = dbscan.resultList.get(i);

			if(l.size() < 10)
				continue;
		}
	}
}









