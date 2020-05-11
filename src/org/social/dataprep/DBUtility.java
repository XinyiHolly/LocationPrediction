package org.social.dataprep;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Database object to establish connection and perform queries
 * @author Qunying Huang @ UW-Madison
 * 2014.4.5
 */
public class DBUtility {	
	private static final String Driver = "org.postgresql.Driver";  // the JDBC driver 
	private static String ConnectionString = "jdbc:postgresql://localhost:5432/madison";
	private static String USER = "postgres";
	private static String PWD = "postgres";
	/**
	 * Constructor to create Database object
	 */
	public DBUtility() {
	}
	
	public DBUtility(String user_name, String password) {
		USER = user_name;
		PWD = password;
		
	}
	
	
	public DBUtility( String dbName, String user_name, String password) {
		USER = user_name;
		PWD = password;
		ConnectionString = "jdbc:postgresql://localhost:5432/" + dbName;
		
	}
	
	/**
	 * to establish the database connection
	 * @return a database connection
	 */
	public Connection connectDB()  {
		Connection conn = null;
		try {
			Class.forName(Driver);
			conn = DriverManager.getConnection(ConnectionString, USER,PWD);        
			return conn;
		} catch (ClassNotFoundException ex) {
			System.out.println(ex.getMessage());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

	

	private JSONObject convertToGeoJson( ResultSet rs ){

		ResultSetMetaData rsmd;
		JSONObject result = null;
		try {
			rsmd = rs.getMetaData();
			result = new JSONObject();
			JSONArray featureArray = new JSONArray();
			while(rs.next()) {
				int numColumns = rsmd.getColumnCount();
				JSONObject feature = new JSONObject();
				JSONObject geom = null;
				JSONObject properties = new JSONObject();
				for (int i=1; i<numColumns+1; i++) {
					String column_name = rsmd.getColumnName(i);		
					if(column_name.equalsIgnoreCase("geom")){
						geom = new JSONObject(rs.getString(column_name));
					}else{
						if(rsmd.getColumnType(i)==java.sql.Types.ARRAY){
							properties.put(column_name, rs.getArray(column_name));
						}else if(rsmd.getColumnType(i)==java.sql.Types.BIGINT){
							properties.put(column_name, rs.getInt(column_name));
						}else if(rsmd.getColumnType(i)==java.sql.Types.BOOLEAN){
							properties.put(column_name, rs.getBoolean(column_name));
						}else if(rsmd.getColumnType(i)==java.sql.Types.BLOB){
							properties.put(column_name, rs.getBlob(column_name));
						}else if(rsmd.getColumnType(i)==java.sql.Types.DOUBLE){
							properties.put(column_name, rs.getDouble(column_name)); 
						}else if(rsmd.getColumnType(i)==java.sql.Types.FLOAT){
							properties.put(column_name, rs.getFloat(column_name));
						}else if(rsmd.getColumnType(i)==java.sql.Types.INTEGER){
							properties.put(column_name, rs.getInt(column_name));
						}else if(rsmd.getColumnType(i)==java.sql.Types.NVARCHAR){
							properties.put(column_name, rs.getNString(column_name));
						}else if(rsmd.getColumnType(i)==java.sql.Types.VARCHAR){
							properties.put(column_name, rs.getString(column_name));						
						}else if(rsmd.getColumnType(i)==java.sql.Types.TINYINT){
							properties.put(column_name, rs.getInt(column_name));
						}else if(rsmd.getColumnType(i)==java.sql.Types.SMALLINT){
							properties.put(column_name, rs.getInt(column_name));
						}else if(rsmd.getColumnType(i)==java.sql.Types.DATE){
							properties.put(column_name, rs.getDate(column_name));
						}else if(rsmd.getColumnType(i)==java.sql.Types.TIMESTAMP){
							properties.put(column_name, rs.getTimestamp(column_name));   
						}else if(rsmd.getColumnType(i)==java.sql.Types.NUMERIC){
							properties.put(column_name, rs.getBigDecimal(column_name));   
						}else {									
							properties.put(column_name, rs.getObject(column_name));
						}												
					}
				}
				
				feature.put("type", "Feature");
				feature.put("geometry", geom);
				feature.put("properties", properties);
				featureArray.put(feature);
				//jsonArray..add(obj);
			}			
			if(featureArray != null){
				result.put("type", "FeatureCollection");
				result.put("features", featureArray);
			}			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
		
	}	
	/**
	 * Convert resultset to JSONArray
	 * @param rs
	 * @return JSONArray
	 */
	private JSONArray convertJson( ResultSet rs ){
		JSONArray json = new JSONArray();
		ResultSetMetaData rsmd;
		try {
			rsmd = rs.getMetaData();

			while(rs.next()) {
				int numColumns = rsmd.getColumnCount();
				JSONObject obj = new JSONObject();

				for (int i=1; i<numColumns+1; i++) {
					String column_name = rsmd.getColumnName(i);

					if(rsmd.getColumnType(i)==java.sql.Types.ARRAY){

						obj.put(column_name, rs.getArray(column_name));
					}
					else if(rsmd.getColumnType(i)==java.sql.Types.BIGINT){
						obj.put(column_name, rs.getInt(column_name));
					}
					else if(rsmd.getColumnType(i)==java.sql.Types.BOOLEAN){
						obj.put(column_name, rs.getBoolean(column_name));
					}
					else if(rsmd.getColumnType(i)==java.sql.Types.BLOB){
						obj.put(column_name, rs.getBlob(column_name));
					}
					else if(rsmd.getColumnType(i)==java.sql.Types.DOUBLE){
						obj.put(column_name, rs.getDouble(column_name)); 
					}
					else if(rsmd.getColumnType(i)==java.sql.Types.FLOAT){
						obj.put(column_name, rs.getFloat(column_name));
					}
					else if(rsmd.getColumnType(i)==java.sql.Types.INTEGER){
						obj.put(column_name, rs.getInt(column_name));
					}
					else if(rsmd.getColumnType(i)==java.sql.Types.NVARCHAR){
						obj.put(column_name, rs.getNString(column_name));
					}
					else if(rsmd.getColumnType(i)==java.sql.Types.VARCHAR){
						obj.put(column_name, rs.getString(column_name));
					}
					else if(rsmd.getColumnType(i)==java.sql.Types.TINYINT){
						obj.put(column_name, rs.getInt(column_name));
					}
					else if(rsmd.getColumnType(i)==java.sql.Types.SMALLINT){
						obj.put(column_name, rs.getInt(column_name));
					}
					else if(rsmd.getColumnType(i)==java.sql.Types.DATE){
						obj.put(column_name, rs.getDate(column_name));
					}
					else if(rsmd.getColumnType(i)==java.sql.Types.TIMESTAMP){
						obj.put(column_name, rs.getTimestamp(column_name));   
					}

					else{
						obj.put(column_name, rs.getObject(column_name));
					}

				}

				json.put(obj);
			}		

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;	
	}	

	/**
	 * to get a result set of a query
	 * @param sql custom query
	 * @return a collection of JSON elements as a result of custom query
	 */
	public JSONObject queryDBAsGeoJsonArray(String sql) {
		Connection conn = connectDB();
		ResultSet res = null;
		JSONObject jsonObj = null;
		//System.out.println("sql is : " + sql);
		try {
			if (conn != null) {
				//System.out.println("You made it, take control your database now!");
				Statement stmt;
				stmt = conn.createStatement();
				res = stmt.executeQuery(sql);
				jsonObj = convertToGeoJson(res);	
				conn.close();			
			} else {
				System.out.println("Failed to make connection!");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return jsonObj;
	}

	//json only - not geojson
	public JSONArray queryDBAsJsonArray(String sql) {
		Connection conn = connectDB();
		ResultSet res = null;
		JSONArray jsonObj = null;
		//System.out.println("sql is : " + sql);
		try {
			if (conn != null) {
				//System.out.println("You made it, take control your database now!");
				Statement stmt;
				stmt = conn.createStatement();
				res = stmt.executeQuery(sql);
				jsonObj = convertJson(res);	
				conn.close();			
			} else {
				System.out.println("Failed to make connection!");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return jsonObj;
	}
	
//method for concatenating arrays
	public JSONArray concatArray(JSONArray... arrs)
	        throws JSONException {
	    JSONArray result = new JSONArray();
	    for (JSONArray arr : arrs) {
	        for (int i = 0; i < arr.length(); i++) {
	            result.put(arr.get(i));
	        }
	    }
	    return result;
	}
	

	/**
	 * to get a result set of a query
	 * @param sql custom query
	 * @return a result set of custom query
	 */
	public ResultSet queryDB(String sql) {
		Connection conn = connectDB();
		ResultSet res = null;
		try {
			if (conn != null) {
				//System.out.println("You made it, take control your database now!");
				Statement stmt;
				stmt = conn.createStatement();
				res = stmt.executeQuery(sql);
				conn.close();			
			} else {
				System.out.println("Failed to make connection!");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return res;
	}

	/**
	 * to run an update query such as update, delete
	 * @param sql custom query
	 */
	public void modifyDB(String sql) {
		Connection conn = connectDB();
		try {
			if (conn != null) {
				//System.out.println("You made it, take control your database now!");					
				Statement stmt;
				stmt = conn.createStatement();
				stmt.execute(sql);					
				stmt.close();	
				conn.close();
			}	
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}
}