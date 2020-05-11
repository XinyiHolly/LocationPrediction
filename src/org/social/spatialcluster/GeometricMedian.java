package org.social.spatialcluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.tweet.AcCoordinate;
import org.tweet.TweetRecord;
import org.tweet.VdCoordinate;
import org.tweet.location.ClusteredLocation;

import dbscan.Utility;
import dbscan.VDbscan;

public class GeometricMedian implements RepresentativeLocation {
	private List<List<?>> resultList;
	
	public GeometricMedian(List<List<?>> resultList) {
		this.resultList = resultList;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ClusteredLocation> center(String clusterName) {	
		List<ClusteredLocation> locations = new ArrayList<>();	
		List<Double> epsArray = new ArrayList<>();
		if (clusterName.equalsIgnoreCase("VDBSCAN") || clusterName.equalsIgnoreCase("MDBSCAN")) {
			for (int i = 0; i < resultList.size(); i++) {
				epsArray = new ArrayList<>();
				List<VdCoordinate> records = (List<VdCoordinate>)resultList.get(i);
				int centerIndex = getCenterIndex(records);
				for(int j = 0; j < records.size(); j++) {			
					VdCoordinate record = records.get(j);
					epsArray.add(record.getEps());
					record.setPlaceID(i + 1);		
				}									
				VdCoordinate centroid = records.get(centerIndex);
				double eps = VDbscan.getDoubleArrayMode(epsArray);
				int k = records.get(0).getK();
				ClusteredLocation location = new ClusteredLocation.ClusteredLocationBuilder(i + 1)
						.rep_id(centroid.getTweetID())
				    	.latitude(centroid.getLatitude())
				    	.longitude(centroid.getLongitude())
				    	.x(centroid.getX())
				    	.y(centroid.getY())
				    	.median_latitude(centroid.getLatitude())
				    	.median_longitude(centroid.getLongitude())
				    	.eps(eps)
				    	.k(k)
				    	.size(records.size())
				    	.records((List<TweetRecord>)resultList.get(i))
				    	.build();
				locations.add(location);
			}
			
		} else if (clusterName.equalsIgnoreCase("ActivityCluster")) {
			
			for(int i = 0; i < resultList.size(); i++) {
				List<AcCoordinate> records = (List<AcCoordinate>)resultList.get(i);
				int centerIndex = getCenterIndex(records);
				for(int j = 0; j < records.size(); j++) {			
					AcCoordinate record = records.get(j);
					record.setPlaceID(i + 1);	// place id already exist??
				}									
				AcCoordinate centroid = records.get(centerIndex);
				ClusteredLocation location = new ClusteredLocation.ClusteredLocationBuilder(i + 1)
						.rep_id(centroid.getTweetID())
				    	.latitude(centroid.getLatitude())
				    	.longitude(centroid.getLongitude())
				    	.x(centroid.getX())
				    	.y(centroid.getY())
				    	.median_latitude(centroid.getLatitude())
				    	.median_longitude(centroid.getLongitude())
				    	.size(records.size())
				    	.zone_id(centroid.getActivityZoneId())
				    	.zone_type(centroid.getZoneType())
				    	.records((List<TweetRecord>)resultList.get(i))
				    	.build();
				locations.add(location);
			}
			
		} else {
			
			for(int i = 0; i < resultList.size(); i++) {
				List<TweetRecord> records = (List<TweetRecord>)resultList.get(i);
				int centerIndex = getCenterIndex(records);
				for(int j = 0; j < records.size(); j++) {			
					TweetRecord record = records.get(j);
					record.setPlaceID(i + 1);		
				}									
				TweetRecord centroid = records.get(centerIndex);
				ClusteredLocation location = new ClusteredLocation.ClusteredLocationBuilder(i + 1)
						.rep_id(centroid.getTweetID())
				    	.latitude(centroid.getLatitude())
				    	.longitude(centroid.getLongitude())
				    	.x(centroid.getX())
				    	.y(centroid.getY())
				    	.median_latitude(centroid.getLatitude())
				    	.median_longitude(centroid.getLongitude())
				    	.size(records.size())
				    	.records((List<TweetRecord>)resultList.get(i))
				    	.build();
				locations.add(location);
			}
		}
		Comparator<ClusteredLocation> comparator = new Comparator<ClusteredLocation>() {
	        @Override
	        public int compare(ClusteredLocation l1, ClusteredLocation l2) {	        	
		        return (int) (l1.getStartTime() - l2.getStartTime());
	        }
		};
		Collections.sort(locations, comparator);
		return locations;
	}
	
	private int getCenterIndex(List<?> records) {
		int centerIndex = 0;
		double minimum_sum_dist = Double.MAX_VALUE;
		for(int j = 0; j < records.size(); j++) {			
			TweetRecord record = (TweetRecord) records.get(j);		
			double sum_dis = 0.0;				
			for (int k = 0; k < records.size(); k++) {
				TweetRecord q = (TweetRecord) records.get(k);		
				sum_dis += Utility.getDistance(record, q);
			}			
			if (minimum_sum_dist > sum_dis) {
				minimum_sum_dist = sum_dis;
				centerIndex = j;
			}	
		}
		return centerIndex;
	}
}
