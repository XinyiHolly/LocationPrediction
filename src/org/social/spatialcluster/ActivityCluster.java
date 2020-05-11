package org.social.spatialcluster;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.social.dataprep.RelationalDBUtility;
import org.tweet.AcCoordinate;
import org.tweet.TweetRecord;

public class ActivityCluster implements SpatialCluster {
	private List<AcCoordinate> pointList;
	private int threshold;
	private int buffer_zone;
	private String dbName;
	private String tweetsTable;
	private String zoneTable;
	
	/*public static void main(String[] args) {
		
		long user_id = 560268128;
		int threshold = 4;
		
		String dbName = "madison";
		String tweetsTableName = "mt_ultraselected";
		String zoneTableName = "activityzone";

		ActivityCluster rc = new ActivityCluster(user_id, dbName, tweetsTableName, zoneTableName, threshold);
		
		System.out.println("finished semantic clustering...");	
	}*/
	
	public ActivityCluster(List<TweetRecord> tweets, int threshold, int buffer_zone, String dbName, String tweetsTable, String zoneTable) {
		pointList = new ArrayList<>();
		for (int i = 0; i < tweets.size(); i++) {
			AcCoordinate r = new AcCoordinate(tweets.get(i));
			pointList.add(r);
		}
		this.threshold = threshold;
		this.buffer_zone = buffer_zone;
		this.dbName = dbName;
		this.tweetsTable = tweetsTable;
		this.zoneTable = zoneTable;
	}
	
	@Override
	public List<List<?>> cluster() {
		List<List<?>> resultList = new ArrayList<>();
		RelationalDBUtility db = new RelationalDBUtility(dbName);
		
		Set<Integer> activityZoneId = new HashSet<>();
		Map<Integer, List<AcCoordinate>> zoneTypeMap = new HashMap<>();
		
		for (int i = 0; i < pointList.size(); i++) {
			
			AcCoordinate record = pointList.get(i);
			long tweet_id = record.getTweetID();
			
			String query = "select ST_Distance(tb1.pj3071_convex_geom, tb2.pj3071_geom) dist, tb1.cid as zone_id, tb1.zonetype as zone_type, tb1.pj3071_convex_geom as hotspot_geom, tb2.pj3071_geom as footprint_geom "
					
						 + "from " + zoneTable + " tb1, " + tweetsTable + " tb2 "
						 
						 + "where tb1.clustername = 'HotSpot_50_100_200' and (ST_Intersects(tb2.pj3071_geom, ST_Buffer(tb1.pj3071_convex_geom," + buffer_zone + ",'quad_segs=8'))) "
						 
						 + "and (tb1.pj3071_convex_geom is not null) "
						 
						 + "and (tb2.pj3071_geom is not null and tb2.tweet_id = " + tweet_id + " ) "
			
						 + "union "
						 
						 + "select ST_Distance(tb1.pj3071_median_geom, tb2.pj3071_geom) dist, tb1.cid as zone_id, tb1.zonetype as zone_type, tb1.pj3071_median_geom as hotspot_geom, tb2.pj3071_geom as footprint_geom "
						 
						 + "from " + zoneTable + " tb1, " + tweetsTable + " tb2 "
						 
						 + "where tb1.clustername = 'HotSpot_50_100_200' and (ST_Intersects(tb2.pj3071_geom, ST_Buffer(tb1.pj3071_median_geom," + buffer_zone + ",'quad_segs=8'))) "
						 
						 + "and (tb1.pj3071_median_geom is not null) "
						 
						 + "and (tb2.pj3071_geom is not null and tb2.tweet_id = " + tweet_id + " ) "
						 
						 + "order by dist limit 1";
			
			System.out.println("Select activityzones: " +  query);
			
			try {
			    ResultSet rs = db.queryDB(query);	    
				int zone_id;
				String zone_type;
			    while (rs.next()){			
					zone_id = rs.getInt("zone_id");
					zone_type = rs.getString("zone_type");
					record.setActivityZoneId(zone_id);
					record.setZoneType(zone_type);
					if (activityZoneId.add(zone_id)) {
						List<AcCoordinate> cluster = new ArrayList<>();				
						cluster.add(record);
						zoneTypeMap.put(zone_id, cluster);
					} else {
						zoneTypeMap.get(zone_id).add(record);
					}
				}				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//int cluster_id = 0;
		for (Map.Entry<Integer, List<AcCoordinate>> entry : zoneTypeMap.entrySet()) {					
			List<AcCoordinate> records = entry.getValue();		
			if (records != null && records.size() >= threshold) {
				//cluster_id ++;
				resultList.add(records);				
			}
		}
		return resultList;
	}
 
}
