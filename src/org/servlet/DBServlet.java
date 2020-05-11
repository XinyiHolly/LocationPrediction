package org.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.social.dataprep.DBUtility;

/**
 * Servlet implementation class DBServlet
 */
@WebServlet("/DBServlet")
public class DBServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final String user_field  = "user_id";      /** Name of the field representing user id, e.g., "user_id", "subid" */
	private final String userno_field  = "user_no";    /** Name of the field representing user no., e.g., "user_no" */
	private final String tweet_field = "tweet_id";     /** Name of the field representing record id, e.g., "tweet_id", "id" */
	private final String time_field  = "create_at";	   /** Name of the field representing time stamp, e.g., "create_at", "time" */
	String dbclusterfield = "";
	String zonetypefield = "";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DBServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		PrintWriter output = response.getWriter();

		try{	
			DBUtility dr = null ;		
			String jsonString = null;
			JSONObject result = null;
			String sql = null;
			String dbName = "madison"/*"madison_gps"*/;
			String tableName = "mt_ultraselected"/*"records_test"*/; 
			dr = new DBUtility(dbName, "postgres", "admin");

			int action = Integer.parseInt(request.getParameter("action"));	
			
			/** update user list*/
			if (action == 1) {
				sql = "SELECT distinct " + userno_field + " as user_no FROM " + tableName 
						
					+ " order by user_no";
				
				result  = dr.queryDBAsGeoJsonArray(sql);

				jsonString = result.toString();
				
			}else if(action == 3) {

				String trajectory_id = request.getParameter("user_no");		 					

				String clusterMethod = request.getParameter("clusterMethod");
				
				//String nexttime = request.getParameter("nexttime");

				String[] name = clusterMethod.split("_");
				clusterMethod = name[0];

				String eps = null, minpts = null;
				if (name.length>1) {
					eps = name[1];
					minpts = name[2];
				}
				
				//String table_name = " activityzone_uncertainty";
				
				String table_name = " activityzone";
				
			    //String geom = "concave_geom";
			
				//String geom = "geom";
			    String geom = "convex_geom";
				//sql = "select cid, ST_AsGeoJson( " +  geom + " ) as geom,  cluster_size ";
				
				sql = "select cid, ST_AsGeoJson(convex_geom) as geom,"
						+ " ST_AsGeoJson(concave_geom) as concave_geom, "
						+ " cluster_size ";
				sql += ", st_x(mean_geom) as mean_lon,  st_y(mean_geom) as mean_lat  ";
				sql += ", st_x(median_geom) as median_lon,  st_y(median_geom) as median_lat  ";
				
				sql += " from " + table_name + " where " ;
				
				if (clusterMethod.equalsIgnoreCase("DBSCANCluster")){
					sql += geom + " is not null "
						+ " and eps =  " + eps
						+ " and minpts =  " + minpts
						
						+ " and clustername =  '" + clusterMethod +"'"
						;

				}else {					
					sql += geom + " is not null "
						+ " and clustername =  '" + clusterMethod +"'"
						;
				}
				sql += " and " + userno_field + " = " + trajectory_id /*+ " and nexttime = " + nexttime*/; 
			
				System.out.println("select activityzone: " +  sql);
				//result  = dr.queryDBAsGeoJsonArray(sql);  	
				JSONArray resultArray = dr.queryDBAsJsonArray(sql);  
				jsonString = resultArray.toString();
				
				/** select clustering methods*/
			}else if (action == 2 || action == 4) {
				String trajectory_id = request.getParameter("user_no");		 					

				String clusterMethod = request.getParameter("clusterMethod");
				
				//String nexttime = request.getParameter("nexttime");

				sql = "SELECT ST_AsGeoJson(geom) as geom, " + time_field + " as create_at, content as content ";

				String[] name = clusterMethod.split("_");
				clusterMethod = name[0];
				String eps = null, minpt = null;
				if (name.length > 1) {
					eps = name[1];
					minpt = name[2];
				}

				if (clusterMethod.equalsIgnoreCase("DBSCAN")){

					/*					sql += ", dbscaneid_" + eps + "_" + minpt + " as clusterid"
							+ ", dbscanc_" + eps + "_" + minpt 
						   + " as center, eps_" + eps + "_" + minpt + " as eps "
						  	+ " FROM " +  tableName  
						   + " where (dbscaneid_" + eps + "_" + minpt + " is not null or dbscanc_" + eps + "_" 
						   + minpt + " is not null) and user_no = " + trajectory_id;	

					 */

					sql += ", dbscanid_" + eps + "_" + minpt + " as clusterid, dbscanc_" + eps + "_" + minpt + " as center, nextdist, accuracy, nexttime, type, appsource, placetype, zonetype"

						+ " FROM " +  tableName  

						+ " where nexttime > " /*+ nexttime*/
						
						+ " and " + user_field + " = " + trajectory_id
						
						+ " and (dbscanid_" + eps + "_" + minpt + " is not null)";	

				}else if (clusterMethod.equalsIgnoreCase("STDBSCAN")){

				}else if (clusterMethod.equalsIgnoreCase("STDBSCANNew")){

				}else if (clusterMethod.equalsIgnoreCase("VDBSCAN")){

					sql += ", vdbscanid as clusterid, vdbscanc as center, veps as eps FROM " +  tableName  + " where (vdbscanid is not null or vdbscanc is not null) and user_no = " + trajectory_id;

				}else if (clusterMethod.equalsIgnoreCase("MDBSCAN")){
					
					sql += ", mdbscanid as clusterid, mdbscanc as center, meps as eps, nextdist, accuracy, nexttime, type, appsource, placetype, zonetype FROM " +  tableName 
						
						+ " where nexttime > " /*+  nexttime*/
						
						+ " and " + user_field + " = " + trajectory_id
						
						+ " and mdbscanid is not null";
					
				}else if (clusterMethod.equalsIgnoreCase("ActivityCluster")){
					
					sql += ", smtcid as clusterid, smtcc as center, zone_activitycls FROM " +  tableName 
						
						//+ " where nexttime > " /*+  nexttime*/
						
						+ " where " + userno_field + " = " + trajectory_id
						
						+ " and smtcid is not null";
					
				}else if (clusterMethod.equalsIgnoreCase("RawData")){

					/*sql += ", nextdist, accuracy, nexttime, type, appsource, placetype FROM " +  tableName 
						+ " where  nexttime >   " +  nexttime
						+ " and " + user_field + " = " + trajectory_id;*/
					
					sql += ", labelid as clusterid, tweet_no FROM " +  tableName + " where user_no = " + trajectory_id;		
				}
				System.out.println("sql: " + sql);
				result  = dr.queryDBAsGeoJsonArray(sql);  	
				jsonString = result.toString();
			}	 	
			//System.out.println(jsonString);
			response.setContentType("text/json"); 	
			output.write(jsonString);
		}finally{	
			output.close();	
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter output = response.getWriter();

		try{	
			DBUtility dr = null ;		
			String jsonString = null;
			JSONObject result = new JSONObject();
			String sql = null;
			String dbName = "madison"/*"madison_gps"*/;
			String tableName = "mt_ultraselected"/*"records_test"*/; 
			dr = new DBUtility(dbName, "postgres", "admin");

			String trajectory_id = request.getParameter("user_no");
			int action = Integer.parseInt(request.getParameter("action"));
			if (action == 1) {
				String cluster_id = request.getParameter("clusterId");
				String points = request.getParameter("points");	
				String[] point_list = points.split(",");			

				for (int i=0; i<point_list.length; i++) {
					sql = "update " + tableName + " set labelid = " + cluster_id + " where user_no = " + trajectory_id + " and tweet_no = " + point_list[i];
					dr.modifyDB(sql);
				}			
				System.out.println("update success");
			}
			else if (action == 2) {

				String clusterMethod = request.getParameter("clusterMethod");
				//String nexttime = request.getParameter("nexttime");

				if (clusterMethod.equalsIgnoreCase("DBSCAN_50_4")){
					dbclusterfield = "dbscanid_50_4";
					zonetypefield = "zone_dbscan_50";

				}else if (clusterMethod.equalsIgnoreCase("DBSCAN_100_4")){
					dbclusterfield = "dbscanid_100_4";
					zonetypefield = "zone_dbscan_100";

				}else if (clusterMethod.equalsIgnoreCase("DBSCAN_200_4")){
					dbclusterfield = "dbscanid_200_4";
					zonetypefield = "zone_dbscan_200";

				}else if (clusterMethod.equalsIgnoreCase("DBSCAN_300_4")){
					dbclusterfield = "dbscanid_300_4";
					zonetypefield = "zone_dbscan_300";

				}else if (clusterMethod.equalsIgnoreCase("DBSCAN_20_4")){
					dbclusterfield = "dbscanid_20_4";		
					zonetypefield = "zone_dbscan_20";
					
				}else if (clusterMethod.equalsIgnoreCase("STDBSCANCluster")){

				}else if (clusterMethod.equalsIgnoreCase("STDBSCANClusterNew")){

				}else if (clusterMethod.equalsIgnoreCase("VDBSCAN")){					
					dbclusterfield = "vdbscanid";
					zonetypefield = "zone_vdbscan";

				}else if (clusterMethod.equalsIgnoreCase("MDBSCAN")){					
					dbclusterfield = "mdbscanid";
					zonetypefield = "zone_mdbscan";

				}else if (clusterMethod.equalsIgnoreCase("ActivityCluster")){
					dbclusterfield = "smtcid";
					zonetypefield = "zone_activitycls";
				}
				else {		//RawData			
					dbclusterfield = "labelid";
					zonetypefield = "placetest";
				}
				//sql = "select count(*) as all from " + tableName + " where " + user_field + " = " + trajectory_id + " and nexttime > " + nexttime + " and placetest != 'Others'";
				sql = "select count(*) as all from " + tableName + " where " + userno_field + " = " + trajectory_id + " and placetest != 'Others'";
				double all_num = 0, acc_num = 0;
				ResultSet all = dr.queryDB(sql);		 		
				while (all.next()){
					all_num = all.getDouble("all");
				}
				//sql = "select count(*) as accurate from " + tableName + " where " + user_field + " = " + trajectory_id + " and nexttime > " + nexttime + " and placetest = " + zonetypefield + " and placetype != 'Others'";
				sql = "select count(*) as accurate from " + tableName + " where " + userno_field + " = " + trajectory_id + " and placetest = " + zonetypefield + " and placetype != 'Others'";
				ResultSet accurate = dr.queryDB(sql);
				while (accurate.next()){					
					acc_num = accurate.getDouble("accurate");
				}
				double accuracy = acc_num/all_num;
				result.put("accuracy", accuracy);
			}	 		

			jsonString = result.toString();
			System.out.println(jsonString);
			response.setContentType("text/json"); 	
			output.write(jsonString);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{	
			output.close();	
		}
	}

}
