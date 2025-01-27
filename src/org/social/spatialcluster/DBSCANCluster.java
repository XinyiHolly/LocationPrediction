package org.social.spatialcluster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.tweet.TweetRecord;
import dbscan.dbscan;

public class DBSCANCluster implements SpatialCluster {
	private List<TweetRecord> pointList;
	private List<Integer> tweetSize;
	private double eps;
	private int minpt;
	private int threshold;
   
	public DBSCANCluster(List<TweetRecord> tweets, double eps, int minpt, int threshold) {
		pointList = new ArrayList<>(tweets);
		tweetSize = new ArrayList<>();
		this.eps = eps;
		this.minpt = minpt;
		this.threshold = threshold;
	}
	
	@Override
	public List<List<?>> cluster() {		
		List<List<?>> resultList = new ArrayList<>();
		dbscan dbscan = new dbscan(eps, minpt);
					
		for(int j = 0; j < pointList.size(); j++) {
			dbscan.addPoint(pointList.get(j));								    									
		}
		tweetSize.add(pointList.size());
		try {
			dbscan.applyDbscan();			
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		int num = 0;
		List<List<?>> clusters = dbscan.getResultList();
		for (int i = 0; i < clusters.size(); i++) {
			List<?> cluster = clusters.get(i);
			if(cluster.size() < threshold) // used to control if a cluster is a representative cluster
				continue;				
			resultList.add(cluster);
			//System.out.print("cluster " + num +  " [" + cluster.size() + "]\n");
			num ++;
		}	
		int clustersize = resultList.size();
		System.out.println("total cluster size : " + clustersize);
		return resultList;
	}
}
