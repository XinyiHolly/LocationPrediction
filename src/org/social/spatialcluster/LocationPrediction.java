package org.social.spatialcluster;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.social.dataprep.RelationalDBUtility;
import org.tweet.TweetRecord;
import org.tweet.VdCoordinate;
import org.tweet.location.ClusteredLocation;

/**
 * Location Prediction Project
 * Apply multiple spatial clustering methods to aggregate travel trajectory data, detect activity zone types and evaluate each type
 * @author Xinyi Liu
 */
public class LocationPrediction {

	private static String clusterName = "DBSCAN_50_4";  /** Spatial clustering mode, e.g., "MDBSCAN", "DBSCAN_50_4" */		
	private final static String centerName = "median";    /** Zone center detection mode, e.g., "median", "mean" */		
	private final static String sortName = "MAX_VOTES";   /** Activity detection mode, e.g., "MAX_VOTES_DR"(with drinking type), "MAX_VOTES" */
	private final static int    threshold = 4;  	      /** Smallest point amount to form an eligible cluster */
	private final int[] epsList = {100, 200, 300};         /** A list of eps values to merge spatial clusters*/ 
	
	private List<Long> users;
	private List<TweetRecord> tweets;
	private List<List<?>> resultList;
	private List<ClusteredLocation> locations;

	private final String[] timespan /*= null;*/    
						   = new String[]{"2017-11-09","2017-11-15"};
	private final String   dbName      = "madison_gps";           /** DB: "madison_gps", "la", "madison" */
	private final String   tweetsTable = "records_storymap";  	  /** Tweet/GPS records, e.g., "records", "records_test", "latweets", "mt_ultraselected"  */
	private final String   zoneTable   = "activityzone_storymap"; /** Activity zones detected by clustering, e.g., "activityzone" */
	private final String   referTable  = "";                  	  /** User reported locations, e.g., "user_poi" */
	private RelationalDBUtility db; 

	private final String user_field  = "subid";      /** Name of the field representing user id, e.g., "user_id", "subid" */
	private final String tweet_field = "id";     /** Name of the field representing record id, e.g., "tweet_id", "id" */
	private final String time_field  = "time";	   /** Name of the field representing time stamp, e.g., "create_at", "time" */
	private final String location_id = null;	       /** Name of the field representing reported location id, e.g., "lid" */;
	private       String dbclusterfield;        	   /** Name of the field representing cluster id, e.g., "mdbscanid", "dbscanid_50_4" */
	private       String dbrfield;		       		   /** Name of the field representing cluster center, e.g., "mdbscanc", "dbscanc_50_4" */
	private       String zonetypefield;         	   /** Name of the field representing zonetype using a specific clustering method, e.g., "zone_mdbscan", "zone_dbscan_50" */
	private final String srid           = "3071";      /** srid for a specific area, e.g., 3071 for Madison, 2846 for Houston */
	private final Integer nexttime      = null;		   /** value of frequency field e.g., "nexttime = 5" */
	private final int activity_buffer   = 100;		   /** Buffer zone size for activity zone type detection */
	private final int buffer_zone       = 300;		   /** Buffer zone size for activity clustering method */
	private final int evaluation_buffer = 100;		   /** Buffer zone size for evaluating activity zone type detection/prediction */

	private Map<String, List<String>> placeTypeMap = new HashMap<>(); /** Type mapping between reported location placetypes and activity zone types*/

	/**
	 * Main entry point of the project. 
	 * @param args [Flag1 Flag2 Flag3].<br>
	 * Flag1|2|3 can be 0, 1, 2. Flag value of 1 or 0 indicates a correspondent method will be run or not. Value of 2 indicates a looping execution<br>
	 * <table border="1">
	 * 	<caption>Input Flags</caption>
	 * 	<tr>
	 * 		<td>Flag1 = 1</td>
	 * 		<td>RUN a single clustering (MDBSCAN/DBSCAN).</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>Flag1 = 0</td>
	 * 		<td>NOT run clustering.</td>
	 * 	</tr>
	 *  <tr>
	 * 		<td>Flag1 = 2</td>
	 * 		<td>RUN a series of DBSCAN with different eps.</td>
	 * 	</tr>
	 *  <tr>
	 * 		<td>Flag1 = 3</td>
	 * 		<td>RUN a series of HotSpot clustering with different eps.</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>Flag2 = 1</td>
	 * 		<td>RUN activity type detection</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>Flag2 = 0</td>
	 * 		<td>NOT run activity type detection</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>Flag3 = 1</td>
	 * 		<td>RUN prediction evaluate</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>Flag3 = 0</td>
	 * 		<td>NOT run prediction evaluate</td>
	 * 	</tr>
	 * </table>
	 */
	public static void main(String[] args) {

		// Argument count.
		if(args.length <= 0 || args.length > 3) {
			System.out.println("Please enter valid arguments: [<Flag1> <Flag2> <Flag3>]");
			return;
		}

		switch(args[0]) {
		// case 1: run single spatial clustering method
		case "1":
		{
			LocationPrediction instance = new LocationPrediction();

			// Step 1: get activity zones by applying selected clustering method.
			instance.RunClustering();

			// Step 2: detect activity zone types.
			if (args[1].equals("1"))
				instance.ActivityTypeDetection();

			// Step 3: get the accuracy of predicted activity types and update table "user_poi" to update fields: "accuracy", "activityzonetype".
			if (args[2].equals("1"))
				instance.PredictionEvaluate();

			break;
		}
		// case 2: run multiple DBSCAN clusterings with different eps values
		case "2":
		{
			for (int i = 1; i <= 4; i++) {
				int index = i * 50;
				clusterName = "DBSCAN_" + index + "_4";
				LocationPrediction instance = new LocationPrediction();

				// Step 1.
				instance.RunClustering();

				// Step 2.
				if (args[1].equals("1"))
					instance.ActivityTypeDetection();

				// Step 3.
				if (args[2].equals("1"))
					instance.PredictionEvaluate();
			}
			break;
		}
		// case 3: run multiple HotSpot clusterings with different eps values
		case "3":
		{
			int eps = 25; 
			for (int i = 1; i <= 3; i++) {
				eps *= 2;
				clusterName = "HotSpot_" + eps + "_4";
				LocationPrediction instance = new LocationPrediction();
				// Step 1.
				instance.RunClustering();
				// Step 2.
				if (args[1].equals("1"))
					instance.ActivityTypeDetection();

				// Step 3.
				if (args[2].equals("1"))
					instance.PredictionEvaluate();
			}

			break;
		}
		// case 0: not run spatial clustering
		case "0":
		{
			LocationPrediction instance = new LocationPrediction();

			// Step 2.
			if (args[1].equals("1"))
				instance.ActivityTypeDetection();

			// Step 3.
			if (args[2].equals("1"))
				instance.PredictionEvaluate();
			
			// Step 3. Evaluate based on activityzone
			if (args[2].equals("2"))
				instance.EvaluatePrediction();

			break;
		}
		}	
	}

	/**
	 * Default constructor.
	 * @param clusterName Spatial clustering mode.
	 * @param centerName Cluster center detection mode.
	 * @param sortName Activity zone type detection mode.
	 * @param threshold Minimum point amount to form an eligible cluster.
	 */
	public LocationPrediction() {			
		
		this.db = new RelationalDBUtility(dbName);

		String[] name = clusterName.split("_");
		double eps = 0.0;		
		int minpt = 0;
		String nameHead = name[0];	

		readUsers(nameHead);
		
		if (nameHead.equalsIgnoreCase("DBSCAN") && name.length > 1) {		
			eps = Double.parseDouble(name[1]);
			minpt = Integer.parseInt(name[2]);
			dbclusterfield = "dbscanid_" + (int)eps + "_" + minpt;
			dbrfield = "dbscanc_" + (int)eps + "_" + minpt;
			zonetypefield = "zone_dbscan_" + (int)eps;

		} else if (nameHead.equalsIgnoreCase("VDBSCAN")) {
			dbclusterfield = "vdbscanid";
			dbrfield = "vdbscanc";
			zonetypefield = "zone_vdbscan";

		} else if (nameHead.equalsIgnoreCase("MDBSCAN")) {
			dbclusterfield = "mdbscanid";
			dbrfield = "mdbscanc";
			zonetypefield = "zone_mdbscan";

		} else if (nameHead.equalsIgnoreCase("ActivityCluster")) {
			dbclusterfield = "smtcid";
			dbrfield = "smtcc";
			zonetypefield = "zone_activitycls";

		} else if (nameHead.equalsIgnoreCase("HotSpot") && name.length > 1) {
			eps = Double.parseDouble(name[1]);
			minpt = Integer.parseInt(name[2]);
			dbclusterfield = "hotid_" + (int)eps + "_" + minpt;
			dbrfield = "hotc_" + (int)eps + "_" + minpt;
			zonetypefield = "zone_hotspot_" + (int)eps;
			if (timespan != null) {
				String[] starttime = timespan[0].split("-");
				String[] endtime = timespan[1].split("-");
				dbclusterfield += "_" + starttime[0] + starttime[1] + starttime[2] + "_" + endtime[0] + endtime[1] + endtime[2];
				dbrfield += "_" + starttime[0] + starttime[1] + starttime[2] + "_" + endtime[0] + endtime[1] + endtime[2];
				zonetypefield += "_" + starttime[0] + starttime[1] + starttime[2] + "_" + endtime[0] + endtime[1] + endtime[2];
			}

		} else if (nameHead.equalsIgnoreCase("HotSpot")) {
			dbclusterfield = "hotspotid";
			dbrfield = "hotspotc";
			zonetypefield = "zone_hotspot";
		}

		addColumn(dbclusterfield, tweetsTable, "integer");
		addColumn(dbrfield, tweetsTable, "integer");
		addColumn(zonetypefield, tweetsTable, "text");
		addColumn("poilist", zoneTable, "text");
		addColumn("typecount", zoneTable, "text");
	}
	
	private void readUsers (String nameHead) {
		users = new ArrayList<>();
		if (nameHead.equals("HotSpot")) {
			users.add((long) 0);
		} else {
			String sql = " select distinct " + user_field + " as user_id from " + tweetsTable
					   + " where " + user_field + " in (26) "
					   ; //74 51 CID 2
			
//			/**for activityzone zonetype detection */
//			String sql = " select distinct user_id "  + " as user_id from " + zoneTable 
//						//+ " where user_id not in (74, 51) "
//						+ " order by user_id "
//						; //74 51 CID 2
//			
//			/**for activityzone detection */
//			sql = "select distinct subid as user_id from records  where subid not in (select distinct user_id  from activityzone where zonetype is not null)"		
//				+ " order by user_id" 
//				;
				
			System.out.println(sql);
			ResultSet rs = db.queryDB(sql);
			try {
				while (rs.next()){				
					long user_id = rs.getLong("user_id");
					users.add(user_id);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("users: " +  users.size());
	}

	/**
	 * Check whether a certain column exists in the table. If not, add this column.
	 */
	private boolean addColumn(String columnName, String tableName, String columnType) {
		if (columnName == null || columnName == "") {
			return false;
		}
		boolean isAdded = false;
		String existColumn = "select exists (select column_name from information_schema.columns "
				+ "where table_name = '" + tableName + "' and column_name = '" + columnName + "')";
		ResultSet rs = db.queryDB(existColumn);
		try {
			while (rs.next()){	
				// Get the activity zone type of each point within the buffer zone.
				boolean exists = rs.getBoolean("exists");
				if (!exists) {
					String addColumn = "alter table " + tableName + " add column " + columnName + " " + columnType;
					db.modifyDB(addColumn);
					isAdded = true;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return isAdded;
	}

	/**
	 * Initialize placeTypeMap. Applied before evaluating activity zone type detection results.
	 */
	private void InitializeNameCategory() {
		List<String> homeType = Arrays.asList("HOME", "HOME OF FAMILY MEMBER", "HOME OF FRIEND");
		List<String> workType = Arrays.asList("WORK");
		List<String> eatingType = Arrays.asList("RESTAURANT", "COFFEE SHOP/CAFE");
		List<String> drinkingType = Arrays.asList("BAR", "LIQUOR STORE");
		List<String> entertainmentType = Arrays.asList("GYM/FITNESS CENTER", "AA/RECOVERY MEETING", "PARK");
		List<String> shoppingType = Arrays.asList("ERRANDS");
		List<String> educationType =  Arrays.asList("SCHOOL");
		List<String> healthType = Arrays.asList("HEALTH CARE");
		List<String> serviceType = Arrays.asList("VOLUNTEER", "CHURCH");
		List<String> otherType = Arrays.asList("OTHER", "CHEKC AT F2", "NA");

		placeTypeMap.put("Dwelling", homeType);
		placeTypeMap.put("Work", workType);
		placeTypeMap.put("Eating", eatingType);
		placeTypeMap.put("Drinking", drinkingType);
		placeTypeMap.put("Entertainment", entertainmentType);
		placeTypeMap.put("Shopping", shoppingType);
		placeTypeMap.put("Health", healthType);
		placeTypeMap.put("Education", educationType);
		placeTypeMap.put("Service", serviceType);	
		placeTypeMap.put("Others", otherType);
	}

	/**
	 * Perform spatial clustering and cluster center detection, 
	 * then update the trajectory records table and activity zone table.
	 */
	private void RunClustering() {	
	
		String[] name = clusterName.split("_");
		
		String nameHead = name[0];	
		
		System.out.println("begin to test RunClustering with: " +  clusterName);
		if (nameHead.equals("HotSpot") && name.length == 1) {
			// Merge hotspots generated using DBSCAN with different eps values.
			// Options: HDBSCAN/OPTICS/MDBSCAN.
			
			MergeHotSpots();

		} else {
			for (int i = 0; i < users.size(); i++) {	
				long user_id = users.get(i);
				ReadUserTweetPoints(user_id);

				SpatialCluster spatialCluster = this.getClusters(clusterName);
				resultList = spatialCluster.cluster();
				RepresentativeLocation representativeLocation = this.getCentroids(centerName);
				locations = representativeLocation.center(clusterName);

				// Update database after detection.
				UpdateClusteringResult(user_id);				
			}
		}
		System.out.println("Finished........");
	}

	/**
	 * Spatial clustering helper.
	 * @param clusterName.
	 */
	private SpatialCluster getClusters(String clusterName) {
		if (clusterName == null) {
			return null;
		}
		String[] name = clusterName.split("_");
		double eps = 0.0;		
		int minpt = 0;
		String nameHead = name[0];	
		if (nameHead.equalsIgnoreCase("DBSCAN") && name.length > 1) {		
			eps = Double.parseDouble(name[1]);
			minpt = Integer.parseInt(name[2]);
			return new DBSCANCluster(tweets, eps, minpt, threshold);

		} /*else if (nameHead.equalsIgnoreCase("VDBSCAN")) {
			return new VDBSCANCluster(tweets, threshold);

		} */else if (nameHead.equalsIgnoreCase("MDBSCAN")) {
			return new MDBSCANCluster(tweets, threshold);

		} else if (nameHead.equalsIgnoreCase("ActivityCluster")) {
			return new ActivityCluster(tweets, threshold, buffer_zone, dbName, tweetsTable, zoneTable);

		} else if (nameHead.equalsIgnoreCase("HotSpot") && name.length > 1) {
			eps = Double.parseDouble(name[1]);
			minpt = Integer.parseInt(name[2]);
			return new DBSCANCluster(tweets, eps, minpt, threshold);
		}
		return null;
	}

	/**
	 * Cluster center detection helper.
	 * @param centerName.
	 */
	private RepresentativeLocation getCentroids(String centerName) {
		if (centerName == null) {
			return null;
		}
		if (centerName.equalsIgnoreCase("MEAN")) {		
			return new GeometricMean(resultList);

		} else if (centerName.equalsIgnoreCase("MEDIAN")) {
			return new GeometricMedian(resultList);
		}
		return null;
	}		

	/**
	 * Perform activity zone type detection and update the trajectory records table and activity zone table.
	 */
	private void ActivityTypeDetection() {

		System.out.println("begin to test ActivityTypeDetection");

		for (int i = 0; i < users.size(); i++) {			
			long user_id = users.get(i);
			ActivityType activityType = this.getTypes(sortName);
			activityType.setActivityType(user_id, clusterName);
		}	
		System.out.println("Finished........");
	}

	/**
	 * Activity zone type detection helper.
	 * @param sortName.
	 */
	private ActivityType getTypes(String sortName) {
		if (sortName.equalsIgnoreCase("MAX_VOTES")) {				
			return new MaximumVotes(db, zoneTable, tweetsTable, dbclusterfield, zonetypefield, user_field, activity_buffer, nexttime);

		} else if (sortName.equalsIgnoreCase("MAX_VOTES_DR")) {				
			return new MaximumVotesWithDrinkingType(db, zoneTable, tweetsTable, dbclusterfield, zonetypefield, activity_buffer, nexttime);
		} 
		return null;
	}


	/**
	 * Update database after spatial clustering and cluster center detection 
	 * @param user_id
	 * @param clusterName
	 * */
	private void UpdateClusteringResult(long user_id) {

		String idSelect = " is not null";
		if (user_id != 0) {
			idSelect = " = " + user_id;
		}

		// Reset a specific user's trajectory records by setting their cluster id, cluster center id and meps/veps value as default(0).
		String eps_field = "eps";	
		if (clusterName.equalsIgnoreCase("MDBSCAN")) {
			eps_field = "meps";
		} else if (clusterName.equalsIgnoreCase("VDBSCAN")){
			eps_field = "veps";
		}
		// Add eps/veps/meps column.
		this.addColumn(eps_field, tweetsTable, "numeric(5,2)");
		// Add geometry columns with specific srid.
		this.addColumn("pj" + srid + "_convex_geom", zoneTable, "geometry(Polygon," + srid + ")");
		this.addColumn("pj" + srid + "_median_geom", zoneTable, "geometry(Point," + srid + ")");

		String clearSQL = " update " +  tweetsTable 
						+ " set " + dbrfield + " = 0, "
						+ dbclusterfield + " = 0, "
						+ zonetypefield + " = '', "
						+ eps_field + " = 0 "
						+ "where " + user_field + idSelect;
		db.modifyDB(clearSQL);

		// Clear a specific user's activity zone detection results.
		String deleteSQL = "delete from " + zoneTable + " where clustername = '" + clusterName + "' and user_id" + idSelect;
		if (timespan != null) {
			this.addColumn("timespan", zoneTable, "text");
			deleteSQL += " and timespan = '" + timespan[0] + " " + timespan[1] + "'";
		}
		db.modifyDB(deleteSQL);

		String veps = null;
		for (int j = 0; j < locations.size(); j++) {
			ClusteredLocation location = locations.get(j);
			long locationid = location.getRepId();

			// Set cluster center id.
			String sql = " update " +  tweetsTable 
					+ " set " + dbrfield + "="  +  1
					+ " where " + tweet_field + " = " + locationid;				
			db.modifyDB(sql);

			List<?> tweet_list = location.getTweetRecords();
			int clusterid = location.getClusterId();			
			int clusterSize = tweet_list.size();

			// Set cluster id.
			String prefix = " update " +  tweetsTable 
				          + " set " + dbclusterfield + " = " + clusterid;
			
			if(clusterName.equalsIgnoreCase("ActivityCluster")) {
				prefix += ", " + zonetypefield + " = '" + location.getZoneType() + "'";
			}
			for (int h = 0; h < clusterSize; h++) {	
				TweetRecord t = (TweetRecord)tweet_list.get(h);
				long tweet_id = t.getTweetID();
				sql = prefix;
				// Set meps/veps for MDBSCAN/VDBSCAN methods.
				if (clusterName.equalsIgnoreCase("MDBSCAN") || clusterName.equalsIgnoreCase("VDBSCAN")) {
					VdCoordinate v = (VdCoordinate)tweet_list.get(h);
					veps = Double.toString(v.getEps());
					sql += ", " + eps_field + " = " + veps;
				}
				sql += " where " + tweet_field + " = " + tweet_id;

				db.modifyDB(sql);				
			}

			// Calculate convex.
			String 	convex_geom = null;
			String 	mean_geom = null;
			String 	median_geom  = null;
			String 	concave_geom = null;

			int concave_size = 0, convex_size =0;

			// For each cluster, generate convexhull.			
			List<TweetRecord> convexPoints = location.getConvexHull();	
			System.out.println("cluster id, point and convex size: " + clusterid + "," + tweet_list.size() + "," + convexPoints.size());
			convex_size  = convexPoints.size();

			// Need at least 3 points to generate a convexhull.
			if (convex_size > 2) {
				convex_geom = "ST_GeomFromText('Polygon((";
				for (int h = 0; h < convex_size; h++) {	

					TweetRecord pt = convexPoints.get(h);
					convex_geom += 	pt.getLongitude() + " " + pt.getLatitude() + ",";
				}
				convex_geom += 	convexPoints.get(0).getLongitude() + " " + convexPoints.get(0).getLatitude();
				convex_geom +=   "))'"+", 4326)";
			}

			/*// For each cluster, generate concavehull
			List<TweetRecord> concavePoints = location.getConcaveHull();
			System.out.println("cluster id, point and concave size: " + clusterid + "," + tweet_list.size() +"," + concavePoints.size());
			concave_size  = concavePoints.size();
			// Need at least 3 points to generate a convexhull
            if(concave_size > 2) {
            	concave_geom = "ST_GeomFromText('Polygon((";
            	for(int h = 0;  h < concave_size;  h++){	
            		TweetRecord pt = concavePoints.get(h);
            		concave_geom +=  pt.getLongitude()+ " " + pt.getLatitude()+ "," ;
            	}
            	concave_geom +=  concavePoints.get(0).getLongitude() + " " + concavePoints.get(0).getLatitude()  ;
            	concave_geom +=  "))'"+", 4326)";
            }	*/		

			// Calculate mean & median
			double mean_lat = location.getMeanLatitude();
			double mean_lon = location.getMeanLongitude();
			double median_lat = location.getMedianLatitude();
			double median_lon = location.getMedianLongitude();

			mean_geom = "ST_GeomFromText('POINT("+ mean_lon + " " + mean_lat + ")'"+", 4326)";
			median_geom = "ST_GeomFromText('POINT("+ median_lon + " " + median_lat + ")'"+", 4326)";		

			double rep_eps = 0;
			if (veps != null) {
				rep_eps = Double.parseDouble(veps);
			}

			String insertClusterSQL;
			if (nexttime != null) {
				insertClusterSQL = "INSERT INTO " + zoneTable
								 + "(cid, mean_geom, median_geom, convex_geom, concave_geom, convex_size, concave_size, cluster_size, clusterName, user_id, eps, minpts, nexttime) VALUES"
								 + "(" + clusterid + "," + mean_geom + "," + median_geom  + "," + convex_geom + "," + concave_geom + ","+ convex_size + ","+ concave_size + ","
								 + clusterSize + ",'" + clusterName + "'," + user_id + "," + rep_eps + "," + threshold + "," + nexttime
								 +");";
			} else if (timespan != null) {
				insertClusterSQL = "INSERT INTO " + zoneTable
								 + "(cid, mean_geom, median_geom, convex_geom, concave_geom, convex_size, concave_size, cluster_size, clusterName, user_id, eps, minpts, zonetype, timespan) VALUES"
								 + "(" + clusterid + "," + mean_geom + "," + median_geom  + "," + convex_geom + "," + concave_geom + ","+ convex_size + ","+ concave_size + ","
							     + clusterSize + ",'" + clusterName + "'," + user_id + "," + rep_eps + "," + threshold + ",'" + location.getZoneType() + "','" + timespan[0] + " " + timespan[1] + "'"
							     +");";
			} else {
				insertClusterSQL = "INSERT INTO " + zoneTable
						 		 + "(cid, mean_geom, median_geom, convex_geom, concave_geom, convex_size, concave_size, cluster_size, clusterName, user_id, eps, minpts, zonetype) VALUES"
						 		 + "(" + clusterid + "," + mean_geom + "," + median_geom  + "," + convex_geom + "," + concave_geom + ","+ convex_size + ","+ concave_size + ","
						 		 + clusterSize + ",'" + clusterName + "'," + user_id + "," + rep_eps + "," + threshold + ",'" + location.getZoneType() + "'"
						 		 +");";
			}

			System.out.println(insertClusterSQL);			
			db.modifyDB(insertClusterSQL);			
		}

		// Update activity zones with specific srid (e.g., 3071 for Madison).
		String updateGeom = "update " + zoneTable + " set pj" + srid + "_convex_geom = ST_Transform(convex_geom," + srid + "), pj"
				+ srid + "_median_geom = ST_Transform(median_geom, " + srid + ") where clustername = '" + clusterName + "' and user_id" + idSelect;
		db.modifyDB(updateGeom);
	}

	/**
	 * Merge hotspots generated using DBSCAN with different eps values, based on their shared activity zone types 
	 * */
	private void MergeHotSpots() {
		int epsNum = epsList.length;
		for (int i = 1; i < epsNum; i++) {
			int preEps = epsList[i - 1];
			int eps = epsList[i];
			String preEpsStr = "HotSpot_" + preEps + "_4";
			String epsStr = "HotSpot_" + eps + "_4";
			String query = "select cid, zonetype, pj3071_convex_geom as convex_geom from " + zoneTable + " where clustername = '" + epsStr + "'";
			ResultSet rs = db.queryDB(query);
			try {
				while (rs.next()) {				
					int cid = rs.getInt("cid");
					String zonetype = rs.getString("zonetype");
					boolean replace = true;
					String convex_geom = rs.getString("convex_geom");

					if (convex_geom == null) {
						continue;
					}
					
					query = "select cid, zonetype from " + zoneTable + " where clustername = '" + preEpsStr
						  + "' and ST_Intersects(pj3071_convex_geom,'" + convex_geom + "')";

					ResultSet rs2 = db.queryDB(query);
					String preCids = "";
					while (rs2.next()) {
						String prezonetype = rs2.getString("zonetype");
						int precid = rs2.getInt("cid");
						if (!prezonetype.equals(zonetype) && !prezonetype.equals("Others")) {
							replace = false;
						}
						preCids += precid + ",";
					}
					if (!replace) {
						preCids = preCids.substring(0, preCids.length() - 1);
						String delete = "delete from " + zoneTable 
									  + " where clustername = '" + epsStr 
									  + "' and cid = " + cid 
									  + " and pj" + srid + "_convex_geom = '" + convex_geom + "'"
									  ;
						db.modifyDB(delete);						
						String insert = "insert into " + zoneTable + " (cid, clustername, zonetype, pj3071_median_geom, pj3071_convex_geom, user_id) "
								      + "select C.cid, '" + epsStr + "', C.zonetype, C.pj3071_median_geom, C.pj3071_convex_geom, C.user_id from " + zoneTable + " C "
								      + "where C.clustername = '" + preEpsStr + "' and cid in (" + preCids + ")"
								      ;
						db.modifyDB(insert);
					}
				}			
				/*String check = "insert into " + zoneTable + " (cid, clustername, zonetype, pj3071_median_geom, pj3071_convex_geom) "
						     + "select Z1.cid, '" + epsStr + "', Z1.zonetype, Z1.pj3071_median_geom, Z1.pj3071_convex_geom "
							 + "from " + zoneTable + " Z1 " 
							 + "where Z1.clustername = '" + preEpsStr + "' "
						  	 + "and not exists (select * from " + zoneTable + " Z2 where Z2.clustername = '" + epsStr + "' and "
						  	 + "ST_Intersects(Z1.pj3071_convex_geom,Z2.pj3071_convex_geom) "
						  	 + "or ST_Intersects(Z1.pj3071_median_geom,Z2.pj3071_convex_geom))"
						  	 ;
				db.modifyDB(check);*/
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String reset = "update " + zoneTable + " Z set cid = Z2.seqnum from "
						 + "(select Z2.*, row_number() over() as seqnum from " + zoneTable + " as Z2 "
						 + "where Z2.clustername = '" + epsStr + "') "
						 + "Z2 where Z2.pj3071_median_geom = Z.pj3071_median_geom "
						 + "and Z.clustername = '" + epsStr + "'"
						 ;
			db.modifyDB(reset);
		}
		String mergeName = "HotSpot";
		for (int eps : epsList) {
			mergeName += "_" + eps;
		}
		String deleteName = "delete from " + zoneTable + " where clustername = '" + mergeName + "'";
		db.modifyDB(deleteName);
		String updateName = "update " + zoneTable + " set clustername = '" + mergeName + "' "
						  + "where clustername = 'HotSpot_" + epsList[epsNum - 1] + "_4'";
		db.modifyDB(updateName);
	}


	/**
	 * Read trajectory records of a specific user 
	 * @param user_id
	 * */
	private void ReadUserTweetPoints(long user_id) {
		if (this.addColumn("x", tweetsTable, "numeric(38,8)")) {
			String updateX = "update " + tweetsTable + " set x = ST_X(ST_Transform(geom, " + srid + "))";	
			db.modifyDB(updateX);
		}
		if (this.addColumn("y", tweetsTable, "numeric(38,8)")) {
			String updateY = "update " + tweetsTable + " set y = ST_Y(ST_Transform(geom, " + srid + "))";
			db.modifyDB(updateY);
		}
		if (this.addColumn("pj" + srid + "_geom", tweetsTable, "geometry(Point," + srid + ")")) {
			String updateGeom = "update " + tweetsTable + " set pj" + srid + "_geom" + " = ST_Transform(geom, " + srid + ")";
			db.modifyDB(updateGeom);
		}
		tweets = this.ReadMadisonGPSDataFromPostgresql(user_id);
		//tweets = this.ReadHurricaneDataFromPostgresql(user_id, timespan);
		//tweets = this.readMadisonTweetDataFromPostgresql(user_id);	
	}

	/**
	 * read from Madison gps data or Madison tweet data
	 * @param user_id
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private List<TweetRecord> ReadMadisonGPSDataFromPostgresql(long user_id) {
		String idSelect = " is not null";
		if (user_id != 0) {
			idSelect = " = " + user_id;
		}
		String frequencyString = "";
		if (nexttime != null) {
			frequencyString = " and nexttime > " + nexttime;
		}
		List<TweetRecord> tweetRecords = new ArrayList<>();
		String query = "SELECT " +  tweet_field + " as tweet_id, x, y, ST_X(geom) as lon, ST_Y(geom) as lat, "
				     + time_field + " as create_at FROM " + tweetsTable 
				     + " where geom is not null" + frequencyString
				     + " and " + user_field + idSelect
				     + " and " + time_field + " between '" + timespan[0] + "'::timestamp and '" + timespan[1] + "'::timestamp"
				     ;
		query += " order by create_at";
		try {
			ResultSet rs = db.queryDB(query);
			//boolean b = rs.next();
			while (rs.next()) {				
				long tweet_id = rs.getLong("tweet_id");			
				double x  = rs.getDouble("x");
				double y = rs.getDouble("y");				
				double lat = rs.getDouble("lat");
				double lon = rs.getDouble("lon");

				String create_at = (String) rs.getString("create_at");
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date tweetTime = null;
				try {
					tweetTime = df.parse(create_at);	

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				TweetRecord record = new TweetRecord.TweetRecordBuilder(tweet_id, user_id, lat, lon)
						.x(x)
						.y(y)
						.day(tweetTime.getDate())
						.month(1 + tweetTime.getMonth())
						.year(1900 + tweetTime.getYear())
						.hour(tweetTime.getHours())
						.minute(tweetTime.getMinutes())
						.time(tweetTime.getTime())
						.build();	

				tweetRecords.add(record);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tweetRecords;
	}
	
	
	/** 
	 * read from Madison tweet data
	 * @param user_id
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private List<TweetRecord> readMadisonTweetDataFromPostgresql(long user_id) {
		List<TweetRecord> tweetRecords = new ArrayList<>();
		String query = "select tweet_id, x, " 
				     + "y, ST_X(geom) as lon, ST_Y(geom) as lat, create_at from " + tweetsTable
				     + " where user_id = " + user_id +" and geom is not null" 
			         ;
		query += " order by create_at";
		try {
		    ResultSet rs = db.queryDB(query);
		    //boolean b = rs.next();
			while (rs.next()) {				
				long tweet_id = rs.getLong("tweet_id");			
				double x  = rs.getDouble("x");
				double y = rs.getDouble("y");				
				double lat = rs.getDouble("lat");
				double lon = rs.getDouble("lon");

				String create_at = (String) rs.getString("create_at");
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date tweetTime = null;
				try {
					tweetTime = df.parse(create_at);	

					// only keep tweets on weekdays
					// if(tweetTime.getDay()==0 || tweetTime.getDay()==6)
					//     continue;				

					// keep tweets on weekends
					// if(tweetTime.getDay()!=0 && tweetTime.getDay()!=6)
					// 	   continue;

					// Added by Xinyi @ 2017-06-19
					// only keep tweets in certain years
					// int year = tweetTime.getYear();
					// if(tweetTime.getYear() == 115)
					//     continue;

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				TweetRecord record = new TweetRecord.TweetRecordBuilder(tweet_id, user_id, lat, lon)
					        .x(x)
						    .y(y)
						    .day(tweetTime.getDate())
						    .month(1 + tweetTime.getMonth())
						    .year(1900 + tweetTime.getYear())
						    .hour(tweetTime.getHours())
						    .minute(tweetTime.getMinutes())
						    .time(tweetTime.getTime())
						    .build();	

				tweetRecords.add(record);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tweetRecords;
	}

	
	/**
	 * read from Hurricane data
	 * @param user_id
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private List<TweetRecord> ReadHurricaneDataFromPostgresql(long user_id) {
		String idSelect = " is not null";
		if (user_id != 0) {
			idSelect = " = " + user_id;
		}
		String frequencyString = "";
		if (nexttime != null) {
			frequencyString = " and nexttime > " + nexttime;
		}
		List<TweetRecord> tweetRecords = new ArrayList<>();
		String query = "SELECT " +  tweet_field + " as tweet_id, x, y, ST_X(geom) as lon, ST_Y(geom) as lat, "
				     + time_field + " as create_at FROM " + tweetsTable 
				     + " where geom is not null" + frequencyString
				     + " and " + user_field + idSelect
				     + " and " + time_field + " between '" + timespan[0] + "'::timestamp and '" + timespan[1] + "'::timestamp"
				     ;
		query += " order by create_at";
		try {
			ResultSet rs = db.queryDB(query);
			//boolean b = rs.next();
			while (rs.next()) {				
				long tweet_id = rs.getLong("tweet_id");			
				double x  = rs.getDouble("x");
				double y = rs.getDouble("y");				
				double lat = rs.getDouble("lat");
				double lon = rs.getDouble("lon");

				String create_at = (String) rs.getString("create_at");
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date tweetTime = null;
				try {
					tweetTime = df.parse(create_at);	

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				TweetRecord record = new TweetRecord.TweetRecordBuilder(tweet_id, user_id, lat, lon)
						.x(x)
						.y(y)
						.day(tweetTime.getDate())
						.month(1 + tweetTime.getMonth())
						.year(1900 + tweetTime.getYear())
						.hour(tweetTime.getHours())
						.minute(tweetTime.getMinutes())
						.time(tweetTime.getTime())
						.build();	

				tweetRecords.add(record);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tweetRecords;
	}

	/**
	 * read from LA tweet data
	 * @param user_id
	 * @return
	 */
	/*@SuppressWarnings("deprecation")
	public List<TweetRecord> readLATweetDataFromPostgresql(long user_id) {
		List<TweetRecord> tweetRecords = new ArrayList<>();
		String query = "select tweet_id, x, " 
				     + "y, ST_X(geom) as lon, ST_Y(geom) as lat from " + tweetsTable
				     + " where user_id = " + user_id +" and geom is not null" 
			         ;
		try {
		    ResultSet rs = db.queryDB(query);
		    //boolean b = rs.next();
			while (rs.next()) {				
				long tweet_id = rs.getLong("tweet_id");			
				double x  = rs.getDouble("x");
				double y = rs.getDouble("y");				
				double lat = rs.getDouble("lat");
				double lon = rs.getDouble("lon");

				TweetRecord record = new TweetRecord.TweetRecordBuilder(tweet_id, user_id, lat, lon)
					        .x(x)
						    .y(y)
						    .build();	

				tweetRecords.add(record);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tweetRecords;
	}*/


	/**
	 * Perform prediction evaluate
	 * */
	private void PredictionEvaluate() {

		// Initialize type mapping between reported placetypes and detected activity zone types. 
		InitializeNameCategory();

		// Get all reported locations to be evaluated.
		Map<Integer, String> locations = new HashMap<>();
		String sql = " select " + location_id + " as lid, placetype as type from " + referTable
				+ " where fileid = 51  "
				; 
		System.out.println(sql);
		ResultSet rs = db.queryDB(sql);
		try {
			while (rs.next()){				
				int location_id = rs.getInt("lid");
				String type = rs.getString("type");
				locations.put(location_id, type);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("begin to test PredictionEvaluate");

		// Get detection accuracy of each reported location.
		for (int lid : locations.keySet()) {		

			String target_geom = "pj3071_geom";

			/***			
			 *  Select trajectory points located within the buffer zone of a certain reported location.
			 *  * tweetsTable: records_test ; referTable:  user_poi 
			 */
			sql = "select R." + zonetypefield + " as zonetype from " + tweetsTable + " R, " + referTable + " UP "
					+ "where ST_Intersects(" + "R." + target_geom + ", ST_Buffer( UP." + target_geom + "," + evaluation_buffer + ",'quad_segs=8')) "
					+ " and UP." + location_id + " = " + lid

					+ " and R.subid = UP.fileid "   // make sure they are the same users	    	
					;


			System.out.println("select zonetype: " + sql);

			List<String> types = new ArrayList<>();
			rs = db.queryDB(sql);
			try {
				while (rs.next()){	
					// Get the activity zone type of each point within the buffer zone.
					String type = rs.getString("zonetype");
					if (type != null) {
						types.add(type);
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			int accurate = 0;
			for (String type : types) {
				// Add one to accurate if the activity zone type of a certain point can be mapped to the reported location's placetype.
				if (placeTypeMap.get(type).contains(locations.get(lid))) {
					accurate ++;
				}
			}

			Double accuracy = null;
			if (!types.isEmpty()) {
				// Calculate detection accuracy for each reported location. 
				accuracy = (double) (accurate / types.size());
			}

			// Update accuracy and detected activity zone types for each reported location in table user_poi; referTable: user_poi: 
			String updateSQL = "update " + referTable + " set accuracy = " + accuracy + ", activityzonetype = '" + types.toString() + "' where " + location_id + " = " + lid;
			System.out.println("update accuracy: " + updateSQL);
			//db.modifyDB(updateSQL);
		}	
		System.out.println("finished PredictionEvaluate......");
	}

	/**
	 * Evaluate the activityzone detection accuracy
	 * */
	private void EvaluatePrediction() {

		int detected_home_count = 0;
		int reported_home_count = 0;
		double homeaccuracy = 0.0;

		int detected_work_count = 0;
		int reported_work_count = 0;
		double workaccuracy = 0.0;
		
		
		// Home zone accuracy

		String sql = " select count(*) as home_count from "  +  zoneTable   + "," +  referTable
				+ "  where user_report_lid  is not null and user_report_lid = lid " 
				+ " and placetype ilike '%HOME%' "
				+ " and Zonetype = 'Dwelling' "
				; 
		
		System.out.println(sql);

		ResultSet rs = db.queryDB(sql);
		try {
			while (rs.next()){				
				detected_home_count = rs.getInt("home_count");

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		sql = " select count(*) as home_count from "  +  zoneTable   + "," +  referTable
				+ "  where user_report_lid  is not null and user_report_lid = lid " 
				+ " and placetype ilike '%HOME%' "
				; 
		System.out.println(sql);
		
		rs = db.queryDB(sql);
		try {
			while (rs.next()){				
				reported_home_count = rs.getInt("home_count");

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		// Home zone accuracy

		sql = " select count(*) as work_count from "  +  zoneTable   + "," +  referTable
				+ "  where user_report_lid  is not null and user_report_lid = lid " 
				+ " and placetype ilike '%work%' "
				+ " and Zonetype = 'Work' "
				; 
		System.out.println(sql);
		rs = db.queryDB(sql);
		try {
			while (rs.next()){				
				detected_work_count = rs.getInt("work_count");

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		sql = " select count(*) as work_count from "  +  zoneTable   + "," +  referTable
				+ "  where user_report_lid  is not null and user_report_lid = lid " 
				+ " and placetype ilike '%work%' "
				; 
		System.out.println(sql);
		rs = db.queryDB(sql);
		try {
			while (rs.next()){				
				reported_work_count = rs.getInt("work_count");

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		if(reported_home_count !=0) {
			homeaccuracy = detected_home_count *1.0 /reported_home_count; 

		}

		if(reported_work_count !=0) {
			workaccuracy = detected_work_count *1.0 /reported_work_count; 

		}
		
		System.out.println("The detected home accuracy is: " + detected_home_count +",  " + reported_home_count + ",  "+ homeaccuracy );
				
		System.out.println("The detected work accuracy is: " + detected_work_count +", " + reported_work_count + ", "+  "," + workaccuracy);

	}
}
