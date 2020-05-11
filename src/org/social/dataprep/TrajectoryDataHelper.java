package org.social.dataprep;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.social.dataprep.RelationalDBUtility;
import org.tweet.location.ReportedLocation;
import org.tweet.location.ReportedLocation.Emotion;
import org.tweet.location.ReportedLocation.Risk;

public class TrajectoryDataHelper {

	private String dataFilePath = "C:/Completed Participant Location Files_1126_2018/";
	private static RelationalDBUtility db;
	private static int lid = 0;

	List<ReportedLocation> reportedLocations;	
	
	public static void main(String[] args) {
		String suffix = "Locations.xlsx";
		int amount = 150;
		String dbName = "madison_gps";
		String tableName = "user_poi";
		db = new RelationalDBUtility(dbName);
		for (int i = 1; i < amount; i++) {
			String filename;
			if (i < 10) {
				filename = "00" + i + "_" + suffix;
			} else if (i < 100) {
				filename = "0" + i + "_" + suffix;
			} else {
				filename = i + "_" + suffix;
			}
			TrajectoryDataHelper helper = new TrajectoryDataHelper();
			helper.readReportedLocationsFromExcel(filename, 0);			
			helper.updateReportedLocationsInPostgreSQL(tableName, i);
		}
	}

	public TrajectoryDataHelper(){

		reportedLocations = new ArrayList<>();
	}
	
	private void updateReportedLocationsInPostgreSQL(String tableName, int fileid) {
		
		for (int i = 0; i < reportedLocations.size(); i ++) {
			lid ++;
			ReportedLocation location = reportedLocations.get(i);
			long utc = location.getUTC();
			String streetaddress = location.getStreetAddress();
			streetaddress = streetaddress.replaceAll("'", "\"");
			String city = location.getCity();
			String state = location.getState();
			String placetype = location.getPlaceType();
			Boolean drank = location.getDrank();
			Boolean alcohol = location.getAlcohol();
			String emotion = location.getEmotion().name();
			String risk = location.getRisk().name();
			Boolean avoid = location.getAvoid();
			Boolean vacation = location.getVacation();
			String fulladdress = location.getFullAddress();
			fulladdress = fulladdress.replaceAll("'", "\"");
			double lat = location.getLatitude();
			double lon = location.getLongitude();
			String insertClusterSQL = "INSERT INTO " + tableName
					+ "(lid, fileid, utc, streetaddress, city, state, placetype, drank, alcohol, emotion, risk, avoid, vacation, fulladdress, lat, lon) VALUES"
					+ "(" + lid + "," + fileid + "," + utc + ",'" + streetaddress + "','" + city  + "','" + state + "','" + placetype + "',"+ drank + ","+ alcohol + ",'"
					+ emotion + "','" + risk + "'," + avoid + "," + vacation + ",'" + fulladdress + "'," + lat + "," + lon
					+");";
			//System.out.println(insertClusterSQL);
			
			db.modifyDB(insertClusterSQL);
		}		
	}

	private void readReportedLocationsFromExcel(String filename, int sheetIndex) {

		XSSFWorkbook workbook;
		try {
			File tmpDir = new File(dataFilePath + filename);
			if (!tmpDir.exists()) {
				return;
			}
			InputStream inp = new FileInputStream(dataFilePath + filename);
			workbook = (XSSFWorkbook) WorkbookFactory.create(inp);
            XSSFSheet sheet = workbook.getSheetAt(0);
            int rowsCount = sheet.getLastRowNum();
            System.out.println("filename: " + filename + "  Total Number of Rows: " + (rowsCount + 1));
            for (int i = 1; i <= rowsCount; i++) {
                XSSFRow row = sheet.getRow(i);
                //System.out.println("Total Number of Cols: " + colCounts);
                long utc = (long)row.getCell(0).getNumericCellValue();
                String address = row.getCell(1).getStringCellValue();
                String city = row.getCell(2).getStringCellValue();
                String state = row.getCell(3).getStringCellValue();
                String type = row.getCell(4).getStringCellValue();
                Boolean drank = false;
                if (row.getCell(5).getStringCellValue().equals("YES")) {
                	drank = true;
                }
                Boolean alcohol = false;
                if (row.getCell(6).getStringCellValue().equals("YES")) {
                	alcohol = true;
                }
                String emotion = Emotion.UNKNOWN.name();
                if (!row.getCell(7).getStringCellValue().isEmpty()) {
                	emotion = row.getCell(7).getStringCellValue();
                }
                String risk = Risk.UNKNOWN.name();
                if (!row.getCell(8).getStringCellValue().isEmpty()) {
                	risk = row.getCell(8).getStringCellValue();
                }
                Boolean avoid = false;
                if (row.getCell(9).getStringCellValue().equals("YES")) {
                	avoid = true;
                }
                Boolean vacation = false;
                if (row.getCell(10).getStringCellValue().equals("YES")) {
                	vacation = true;
                }
                String full_address = row.getCell(11).getStringCellValue();
                double lat = row.getCell(12).getNumericCellValue();
                double lon = row.getCell(13).getNumericCellValue();
                ReportedLocation location = new ReportedLocation.ReportedLocationBuilder(utc, address)
                		.city(city)
                		.state(state)
                		.placetype(type)
                		.drank(drank)
                		.alcohol(alcohol)
                		.emotion(Emotion.valueOf(emotion))
                		.risk(Risk.valueOf(risk))
                		.avoid(avoid)
                		.vacation(vacation)
                		.full_address(full_address)
                		.latitude(lat)
                		.longitude(lon)
                		.build();
                reportedLocations.add(location);
                try {
                    inp.close();
                } catch (IOException e) {
                	e.printStackTrace();
                }
            }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
