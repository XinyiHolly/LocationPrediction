package org.social.spatialcluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.tweet.TweetRecord;
import org.tweet.VdCoordinate;
import org.tweet.location.ClusteredLocation;
import dbscan.VDbscan;

public class GeometricMean implements RepresentativeLocation {
	private List<List<?>> resultList;
	
	public GeometricMean(List<List<?>> resultList) {
		this.resultList = resultList;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ClusteredLocation> center(String clusterName) {
		List<ClusteredLocation> locations = new ArrayList<>();
		double sumLat, sumLon, sumX, sumY;
		sumLat = sumLon = sumX = sumY = 0;
		List<Double> epsArray = new ArrayList<>();
		if (clusterName.equalsIgnoreCase("VDBSCAN") || clusterName.equalsIgnoreCase("MDBSCAN")) {
			for(int i = 0; i < resultList.size(); i++) {
				List<VdCoordinate> records = (List<VdCoordinate>)resultList.get(i);
				for(int j = 0; j < records.size(); j++) {			
					VdCoordinate record = records.get(j);
					record.setPlaceID(i + 1);		
					sumLat += record.getLatitude();
					sumLon += record.getLongitude();
					sumX += record.getX();
					sumY += record.getY();
					epsArray.add(record.getEps());
				}									
				double mean_centroidLat = sumLat / records.size();
				double mean_centroidLon = sumLon / records.size();
				double mean_centroidx = sumX / records.size();
				double mean_centroidy = sumY / records.size();
				double eps = VDbscan.getDoubleArrayMode(epsArray);
				int k = records.get(0).getK();
				ClusteredLocation location = new ClusteredLocation.ClusteredLocationBuilder(i + 1)
				    	.latitude(mean_centroidLat)
				    	.longitude(mean_centroidLon)
				    	.x(mean_centroidx)
				    	.y(mean_centroidy)
				    	.mean_latitude(mean_centroidLat)
				    	.mean_longitude(mean_centroidLon)
				    	.eps(eps)
				    	.k(k)
				    	.size(records.size())
				    	.records((List<TweetRecord>)resultList.get(i))
				    	.build();
				locations.add(location);
			}
			
		} else {
			for(int i = 0; i < resultList.size(); i++) {
				List<TweetRecord> records = (List<TweetRecord>)resultList.get(i);
				for(int j = 0; j < records.size(); j++) {			
					TweetRecord record = records.get(j);
					record.setPlaceID(i + 1);		
					sumLat += record.getLatitude();
					sumLon += record.getLongitude();
					sumX += record.getX();
					sumY += record.getY();
				}									
				double mean_centroidLat = sumLat / records.size();
				double mean_centroidLon = sumLon / records.size();
				double mean_centroidx = sumX / records.size();
				double mean_centroidy = sumY / records.size();
				ClusteredLocation location = new ClusteredLocation.ClusteredLocationBuilder(i + 1)
				    	.latitude(mean_centroidLat)
				    	.longitude(mean_centroidLon)
				    	.x(mean_centroidx)
				    	.y(mean_centroidy)
				    	.mean_latitude(mean_centroidLat)
				    	.mean_longitude(mean_centroidLon)
				    	.size(records.size())
				    	.records(records)
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
}
