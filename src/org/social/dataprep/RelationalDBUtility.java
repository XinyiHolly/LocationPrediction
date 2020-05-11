package org.social.dataprep;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database object to load drivers and perform queries
 * @author Abdulsalam Umar blog.salamtura.com
 */
public class RelationalDBUtility {

	private static final String Driver = "org.postgresql.Driver";
	private static String ConnectionString = "jdbc:postgresql://localhost:5432/tweets";
	private static final String USER = "postgres";
	private static final String PWD = "admin";

	/**
	 * create Database object
	 */
	public RelationalDBUtility(String dbName) {
				
		ConnectionString = "jdbc:postgresql://127.0.0.1:5432/" + dbName;
	}
	
	/**
	 * create Database object
	 */
	public RelationalDBUtility(String dbName, int port) {
				
		ConnectionString = "jdbc:postgresql://127.0.0.1:" + port +"/" + dbName;
	}
	
	public RelationalDBUtility() {
		
		ConnectionString = "jdbc:postgresql://127.0.0.1:5432/" + "tweets";
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
	

	/**update table records */
	public static void modifyDB(RelationalDBUtility db, String sql){
		db.modifyDB(sql);	

	}

	
	/** postgresql db can not insert characters with single quote (e.g., QY's account)
	 * Escaping single quotes ' by doubling them up -> '' is the standard way to address this problem
	 * This method will double every single quote, e.g., turn QY's account to QY''s account
	 */	
	public static String processSingleQuote(String textWithSingleQuote){
		String[]  strArray = textWithSingleQuote.split("'");		
		int length = strArray.length;
		if(length >1){
			StringBuffer sb = new StringBuffer();
			for(int i= 0; i< length-1; i++){				
				sb.append(strArray[i]);
				sb.append("''");
			}
			sb.append(strArray[length-1]);	
			textWithSingleQuote = sb.toString();
		}
		/** Need additional process for the last single quote, such as  J a z m i n e'*/
		String currentText = textWithSingleQuote;

		while(currentText.endsWith("'")){
			textWithSingleQuote += "'";
			currentText = currentText.substring(0, currentText.length()-1);
			//System.out.println("current Text: " + currentText);
		}		
		return textWithSingleQuote;
	}
	
	
	

	
}