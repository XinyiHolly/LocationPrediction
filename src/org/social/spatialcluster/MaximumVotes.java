package org.social.spatialcluster;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.social.dataprep.RelationalDBUtility;

public class MaximumVotes implements ActivityType {
	
	private RelationalDBUtility db;
	private String zoneTable;
	private String tweetsTable;
	private int bufferZone;
	private Integer nexttime;
	
	private String dbclusterfield;
	private String zonetypefield;
	private String user_field;
	private String idSelect;
	
	private Map<String, List<String>> activityNameMap = new HashMap<>();
	
	public MaximumVotes(RelationalDBUtility db, String zoneTable, String tweetsTable, String dbclusterfield, String zonetypefield, String user_field, int bufferZone, Integer nexttime) {
		this.db = db;
		this.zoneTable = zoneTable;
		this.tweetsTable = tweetsTable;
		this.dbclusterfield = dbclusterfield;
		this.zonetypefield = zonetypefield;
		this.user_field = user_field;
		this.bufferZone = bufferZone;
		this.nexttime = nexttime;
	}

	/*public static void main(String[] args) {

		MaximumVotes zt = new MaximumVotes("madison", "activityzone", "mt_ultraselected", "mdbscanid", "zone_mdbscan", 100, 5);
	}*/
	
	private void initialNameCategory() {
		List<String> homeZone = Arrays.asList("condominium","dormatory","dormitory","Dormitory","family_house","house","house;garage","house;yes","houseboat","houses","mobile_home","residential","shed","townhouse","yes;house","yes;roof","apartment","apartments","condominiums","construction","home","university_apartment","BLDG", "BRKS", "CMPL","CMPLA","CMPMN","CMPO","CMPRF","CVNT","DEVH","FCL","HERM","HSE","HSEC","HUT","HUTS","LEPC","MSSN","MSTY","PPL","PPLL","PPLR","PPLS","RLGR","STLMT","TRB","SHPF","HMSD","apartment", "condo", "house");
		List<String> trafficZone = Arrays.asList("dock","ferry_terminal","dam","ford","fuel","motorway_junction","service","slipway","stop","airplane","Airport","station","terminal","train_station","transportation","turning_circle","bridge","car_park","CSWY","AIRF","AIRH","AIRP","AIRS","ANCH","BDG","DCK","FY","HBR","HBRX","LDNG","MAR","NRWS","PASS","PRT","PTGE","QUAY","RD","RDB","RDCR","RDCUT","RDJCT","RJCT","RR","RSD","RSGNL","RSTN","RSTP","RTE","RYD","STRT","TNL","TNLC","TNLRD","TNLRR","TRANT","WHRF","DCKB","RDST","PKLT","ferry_terminal", "mini_roundabout", "lock_gate", "crossing", "slipway", "street_lamp", "motorway_junction", "track","airport", "ford", "service", "parking", "parking_multistorey", "transit_station", "turning_circle", "traffic_signals", "rv_park", "subway_station", "dam", "marina", "taxi_stand", "travel_agency");
		List<String> streetZone = Arrays.asList("canal","taxi","tram_stop","railway_halt","bus_stop","bus_station","railway_station","crossing","mini_roundabout","parking","parking_bicycle","parking_multistorey","parking_underground","speed_camera","street_lamp","traffic_signals","turning_circle","parking_shelter","PKLT", "PKLT","BUSTN","BUSTP","GHAT","MTRO","ST","STKR","TRL","stop","camera_surveillance");
		List<String> workZone = Arrays.asList("agricultural","factory","Livestock_Barn","barn","courthouse","data_center","industrial","Madison City Hall","offices","works","bank","brewery","Business","business","central_office","civic","company","company_store","farm","government","municipal","office","FRMT", "HMSD", "SHPF", "VIN", "VINS", "AGRC","AGRF","AQC","CRRL","FISH","MFG","MFGB","MFGC","MFGCU","MFGLM","MFGM","MFGN","MFGPH","MFGSG","ML","MLM","MLO","MLSG","MLSW","MLWND","MLWTR","MN","MNA","MNAU","MNC","MNCR","MNCU","MNFE","MNN","MNQR","NSY","OCH","OILR","SWT","PMPO","STBL","FRM","FRMS","FRST","GASF","GOSP","GRAZ","GRSLD","GRVC","GRVO","GRVP","GRVPN","RNCH","FNDY","DARY","ESTO","ESTR","ESTSG","ESTT","PAL","INDS","ADM1","ADM1H","ADM2","ADM2H","ADM3","ADM3H","ADM4","ADM4H","ADM5","ADMD","ADMDH","ADMF","BLDO","CSTM","CTHSE","CTRA","CTRS","DIP","GOVL","ITTR","PCL","PCLD","PCLH","PCLI","PCLIX","PCLS","PO","PP","PPLA","PPLA2","PPLA3","PPLA4","PPLC","PPLCH","PPLG","STNB","STNC","STNE","STNF","STNI","STNM","STNR","STNS","STNW","TWO","USGE","PMPW","courthouse", "public_building", "post_office", "fire_station", "town_hall", "embassy", "police", "accounting", "city_hall", "community_centre", "finance", "general_contractor", "lawyer", "local_government_office");
		List<String> eatingZone = Arrays.asList("caboose","cafeteria","Cafeteria","restaurant","MALL","REST", "food_court", "cafe", "bakery", "restaurant", "fast_food", "food", "meal_delivery", "meal_takeaway");
		List<String> entertainmentZone = Arrays.asList("stream","river","reservoir","wetland","water","lock_gate","waterfall","weir","cliff","spring","peak","glacier","cave_entrance","beach","volcano","tree","canopy","amphitheater","amphitheatre","arboretum","Art_Building","Auditorium","auditorium","bar","bleachers","boat","boat_house","boathouse","casino","cinema","clubhouse","conservatory","country_club","gazebo","glasshouse","grandstand","greenhouse","Gym","gym","gymnasium","historic","leisure","museum","pavilion","recreation_ground","recreational","stable","stadium","stands","terrace","PIER", "AMTH", "AMUS", "ANS", "APNU", "ARCH", "ATHF", "BCH", "BCHS", "BDLD", "BTL", "CARN", "CMN", "CSNO", "CSTL", "CTRCM", "FLLS", "FLLSX", "GDN", "GYSR", "HSTS", "ISLM", "LK", "LKC", "MNMT", "MUS", "OPRA", "PRK", "PRKGT", "PRMN", "PYR", "PYRS", "RECG", "RECR", "RESV", "RESW", "RKRY", "RLG", "RSRT", "SPA", "SQR", "STDM", "THTR", "TOWR", "TRR", "ZOO", "OBPT", "OBS", "PRKHQ", "PGDA", "VIN", "VINS", "theatre", "golf_course", "castle", "dog_park", "viewpoint", "fountain", "arts_centre", "bench", "picnic_site", "hairdresser", "attraction", "theme_park", "cinema", "caravan_site", "camp_site", "memorial", "playground", "monument", "nightclub", "casino", "amusement_park", "tourist_info", "aquarium", "swimming_pool", "biergarten", "archaeological", "art_gallery", "artwork", "shelter", "alpine_hut", "pub", "pitch", "beauty_salon", "bowling_alley", "gym", "sports_centre", "ice_rink", "hunting_stand", "hair_care", "movie_theater", "museum", "battlefield", "night_club", "painter", "park", "spa", "stadium", "zoo", "wayside_cross");
		List<String> shoppingZone = Arrays.asList("country_store","mall","retail","shop","shopping center","shopping_center","strip_mall","supermarket","CTRB","MKT","RET","ZNF","MALL","newsagent", "greengrocer", "supermarket", "gift_shop", "bookshop", "mobile_phone_shop", "bicycle_shop", "computer_shop", "beverages", "furniture_shop", "mall", "beauty_shop", "butcher", "video_shop", "jeweller", "kiosk", "clothes", "toy_shop", "shoe_shop", "garden_centre", "stationery", "vending_cigarette", "outdoor_shop", "sports_shop", "doityourself", "clothing_store", "store", "bicycle_store", "book_store", "convenience_store", "convenience", "department_store", "electronics_store", "florist", "grocery_or_supermarket", "furniture_store", "hardware_store", "home_goods_store", "jewelry_store", "liquor_store", "pet_store", "shoe_store", "shopping_mall", "store");
		List<String> educationZone =  Arrays.asList("academic","Academic","academic_building","Academic_Building","college","college;yes","library","Middle_School","school","university","LIBR", "NOV", "SCH", "SCHA", "SCHC", "SCHL", "SCHM", "SCHN", "SCHT", "SECP", "UNIP", "UNIV", "college", "kindergarten", "university", "school", "campground", "library");
		List<String> healthZone = Arrays.asList("clinic","hospital","hostel","medical","Medical_Office","CTRM", "HSP", "HSPC", "HSPD", "HSPL", "SNTR", "hospital", "doctors", "doctor", "dentist", "chemist", "health", "pharmacy", "physiotherapist", "nursing_home", "optician");
		List<String> serviceZone = Arrays.asList("tower","post_box","fuel","drain","buddhist","christian","christian_anglican","christian_catholic","christian_evangelica","christian_lutheran","christian_methodist","christian_orthodox","christian_protestant","hindu","jewish","muslim","muslim_shia","muslim_sunni","sikh","taoist","bunker","burial_vault","cabin","car_repair","car_wash","carport","cathedral","chapel","church","convent","entrance","fire_station","garage;house","garages","gas_station","gasometer","gatehouse","guardhouse","hotel","hut","military","monastery","mosque","motel","post_office","roof","service","shelter","silo","storage","storage tank","storage_tank","synagogue","tank","temple","trailer","warehouse","water tower","Water_Tower","yes;hotel","PMPW", "RDST", "PGDA", "AIRB","ASYL","ATM","BANK","BCN","BRKW","BSTN","BTYD","CH","CMTY","COMC","CTRF","CTRR","DCKD","DCKY","DIKE","DTCHD","DTCHI","FT","GATE","GHSE","GRVE","HLT","HTL","LNDF","LTHSE","MSQE","OBSR","OILT","OILW","PRSH","PS","PSH","PSTB","PSTC","PSTP","RHSE","SHRN","SHSE","SLCE","SYSI","TMB","TMPL","TRMO","VETF","WEIR","WLL","WLLS","WTRW","PIER","FRMT","chalet", "travel_agent", "wastewater_plant", "water_mill", "veterinary", "comms_tower", "bed_and_breakfast", "fort", "vending_machine", "telephone", "drinking_water", "car_dealership", "recycling_metal", "hostel", "guesthouse", "water_tower", "bicycle_rental", "toilet", "recycling", "graveyard", "windmill", "vending_parking", "recycling_clothes", "recycling_glass", "water_works", "ruins", "vending_any", "wayside_shrine", "hotel", "waste_basket", "observation_tower", "lighthouse", "motel", "water_well", "car_sharing", "recycling_paper", "atm", "car_dealer", "car_rental", "car_repair", "car_wash", "cemetery", "church", "electrician", "funeral_home", "gas_station", "hindu_temple", "incurance_agency", "laundry", "locksmith", "lodging", "place_of_worship", "plumber", "real_estate_agency", "roofing_contractor", "storage", "synagogue", "veterinary_car");
		List<String> prisonZone = Arrays.asList("PRN", "prison");
		
		activityNameMap.put("home", homeZone);
		activityNameMap.put("transportation", trafficZone);
		activityNameMap.put("transportation_network", streetZone);
		activityNameMap.put("work", workZone);
		activityNameMap.put("eating", eatingZone);
		activityNameMap.put("entertainment", entertainmentZone);
		activityNameMap.put("shopping", shoppingZone);
		activityNameMap.put("health", healthZone);
		activityNameMap.put("education", educationZone);
		activityNameMap.put("service", serviceZone);
		activityNameMap.put("prison", prisonZone);				
	}
	
	@Override
	public void setActivityType(long user_id, String clusterName) {
		
		///for dc dataset
		///1-Dwelling
		///2-Eating
		///3-Entertainment
		///4-Shopping
		///5-Service
		///6-Education
		///7-Health
		///8-Work
		///9-Transportation
		///10-Transportation_network
		///0-Others(unknown)
		
		initialNameCategory();
		
		idSelect = " is not null";
		if (user_id != 0) {
			idSelect = " = " + user_id;
		}
		
		String sql = "select distinct cid from " + zoneTable + " where clustername = '" + clusterName + "'" + " and user_id = " + user_id;
		ResultSet rs = db.queryDB(sql);
		List<Integer> cidlist = new ArrayList<>();
		try {
			while (rs.next()){				
				cidlist.add(rs.getInt("cid"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		for (int i = 0; i < cidlist.size(); i++) {
			int cid = cidlist.get(i);
			String landUseType = GetLandusetype(clusterName, user_id, cid);	
			System.out.println("cluster " + cid + " land use type: " + landUseType);
			String zoneType = GetZonetype(landUseType, clusterName, user_id, cid);
			System.out.println("cluster " + cid + " zone type: " + zoneType);
			
			/**Update landusetype and zonetype*/
				
			sql = "update " + zoneTable + " set "
				+ " zonetype = '" + zoneType + "'"
				+ " , landusetype = '" + landUseType + "'"
				+ " where cid = " + cid 
				+ " and clustername = '" + clusterName + "'"
				+ " and user_id = '" + user_id + "'"
				;
			System.out.println("update zone table: " + sql);
			db.modifyDB(sql);	  
			
			sql = "update " + tweetsTable + " set " 
				+ zonetypefield + " = '" + zoneType 
				+ "' where " + dbclusterfield + " = " + cid
				+ " and " + user_field + " = '" + user_id + "'"
				;
			System.out.println("update tweets table: " + sql);
			db.modifyDB(sql);
		}	
	}
	
	public String GetLandusetype(String clusterName, long user_id, int clusterNo) {
		
		String target = "landuse_wi";
		
		String srid = "3071";
		
		String target_geom = "pj" + srid + "_geom";
		
		String convex_geom = "pj" + srid + "_convex_geom";
		
		String median_geom = "pj" + srid + "_median_geom";
		
		convex_geom = "ST_Buffer(Y." + convex_geom + "," + bufferZone + ",'quad_segs=8')";
		
		median_geom = "ST_Buffer(Y." + median_geom + "," + bufferZone + ",'quad_segs=8')";
			
		String sql = "select X.zonetype as landusetype, ST_Union(ST_Intersection(X." + target_geom + ", " + median_geom + ")) as median_geom, ST_Union(ST_Intersection(X." + target_geom + ", " + convex_geom + ")) as geom from " + target + " X, " + zoneTable + " Y "
				  
				   + "where (ST_Intersects(X." + target_geom + "," + median_geom + ") or ST_Intersects(X." + target_geom + "," + convex_geom + ")) "
				   
				   + "and (Y.cid = " + clusterNo + " and Y.clustername = '" + clusterName + "' and Y.user_id = " + user_id + ") "  
				  
				   + "group by X.zonetype "
		
				   + "order by ST_Area(ST_Union(ST_Intersection(X." + target_geom + ", " + convex_geom + "))) desc, ST_Area(ST_Union(ST_Intersection(X." + target_geom + ", " + median_geom + "))) desc";
		
		System.out.println(sql);
		
		ResultSet rs = db.queryDB(sql);

		String landusetype = "unknown";
		
		try {
			while (rs.next()){				
				landusetype = rs.getString("landusetype");
				break;				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
		
		return landusetype;
	}

	private String GetZonetype(String landUseType, String clusterName, long user_id, int clusterNo) {
				
		String target = "pois_wi";
		
		String srid = "3071";
		
		String target_geom = "pj" + srid + "_geom";
		
		String geom = "pj" + srid + "_convex_geom";
		
		String median = "pj" + srid + "_median_geom";
		
		List<String> poiList = new ArrayList<String>();
	
		String sql = "select X.fclass, X." + target_geom + ", Y." + geom + " from " + target + " X, " + zoneTable + " Y "
				  
				   + "where (ST_Intersects(X." + target_geom + ", ST_Buffer(Y." + geom + "," + bufferZone + ",'quad_segs=8')) or ST_Intersects(X." + target_geom + ", ST_Buffer(Y." + median + "," + bufferZone + ",'quad_segs=8'))) "
				  
				   + " and (Y.cid = " + clusterNo + " and Y.clustername = '" + clusterName + "' and Y.user_id = " + user_id + ")";
		
		System.out.println(sql);
		
		ResultSet rs = db.queryDB(sql);
	
		try {
			while (rs.next()){				
				String poi_type = rs.getString("fclass");
				poiList.add(poi_type);			
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		target = "pois_a_wi";
		
		sql = "select X.fclass, X." + target_geom + ", Y." + geom + " from " + target + " X, " + zoneTable + " Y "
				  
			+ "where (ST_Intersects(X." + target_geom + ", ST_Buffer(Y." + geom + "," + bufferZone + ",'quad_segs=8')) or ST_Intersects(X." + target_geom + ", ST_Buffer(Y." + median + "," + bufferZone + ",'quad_segs=8'))) "
				  
			+ "and (Y.cid = " + clusterNo + " and Y.clustername = '" + clusterName + "' and Y.user_id = " + user_id + ")";
		
		System.out.println(sql);
		
		rs = db.queryDB(sql);
	
		try {
			while (rs.next()){				
				String poi_type = rs.getString("fclass");
				poiList.add(poi_type);			
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	
		System.out.println("POI type in cluster" + clusterNo + ": " + poiList.toString());
	
		Map<String, Integer> typecount = new HashMap<String, Integer>();
		if (poiList.isEmpty()) {
			///for madison dataset
			///Dwelling
			///Entertainment
			///Service
			///Health
			///Work
			///Commercial
			///Others(unknown)
			if (landUseType.equals("entertainment"))
				return "Entertainment";
			else if (landUseType.equals("shopping"))
				return "Shopping";
			else if (landUseType.equals("working"))
				return "Work";
			else if (landUseType.equals("health"))
				return "Health";
			else if (landUseType.equals("service"))
				return "Service";
			else if (landUseType.equals("dwelling")) {
				return "Dwelling";			
			}
			return "Others";
		}
			
		///type
		String zoneType = null;
		
		if(landUseType == null) {
			///others
			zoneType = "Others"; 				
		} else if (landUseType.equals("working")) {
			///Business and industry land use type	        
			zoneType = "Work";			
		} else if (landUseType.equals("health")) {
			///Health land use type
	        zoneType = "Health";
		} else if (landUseType.equals("service")) {
			///Service land use type
	        zoneType = "Service";
		} else if (landUseType.equals("dwelling")) {
			///Residential land use type
	        zoneType = "Dwelling";
		} else if (landUseType.equals("shopping")) {
			///Shopping land use type
			zoneType = "Shopping";
		} else if (landUseType.equals("entertainment")) {
			///Recreational and open space land use type
			zoneType = "Entertainment";			
		} else {
			///mixed land use type / commercial land use type 
			zoneType = "Others";
		}
		
		for(String type : poiList) {
			
			String tempType = zoneType;
			
			if (activityNameMap.get("health").contains(type)) {
				tempType = "Health";
			}
			else if (activityNameMap.get("education").contains(type)) {
				tempType = "Education";
			}
			else if (activityNameMap.get("eating").contains(type)) {
				tempType = "Eating";
			}
			else if (activityNameMap.get("shopping").contains(type)) {
				tempType = "Shopping";
			}
			else if (activityNameMap.get("work").contains(type)) {
				tempType = "Work";
			}			
			else if (activityNameMap.get("entertainment").contains(type)) {
				tempType = "Entertainment";
			}
			else if (activityNameMap.get("service").contains(type)) {
				tempType = "Service";
			}			
			else if (activityNameMap.get("home").contains(type)) {
				tempType = "Dwelling";
			}
			else if (activityNameMap.get("transportation_network").contains(type)) {
				tempType = "Transportation_network";
			}
			else if (activityNameMap.get("transportation").contains(type)) {
				tempType = "Transportation";
			}	
			else {
				System.out.println(type + ": unknown POI classification; tempType: " + tempType);
			}
			
			if (typecount.containsKey(tempType)) {
				int tcount = typecount.get(tempType);
				typecount.put(tempType, tcount+1);
			} else {
				typecount.put(tempType, 1);
			}		
		} 
		
		typecount = sortByComparator(typecount);
		List<String> keys = new ArrayList<String>(typecount.keySet());
		
		System.out.println("type count in cluster" + clusterNo + ": " + typecount.toString());
		
		/**Update poilist*/		
		sql = "update " + zoneTable 
			+ " set poilist = '" + poiList.toString() + "'"				
			+ " , typecount = '" + typecount.toString() + "'"				
			+ " where cid = " + clusterNo
			+ " and clustername = '" + clusterName + "'"
			+ " and user_id = '" + user_id + "'"
			;
		
		db.modifyDB(sql);	
		
		if (typecount.containsKey("Shopping") || typecount.containsKey("Eating")) {
			for (String key : keys) {
				if (key.equals("Shopping") || key.equals("Eating")) {
					return key;
		}	}	}
		else if (typecount.containsKey("Education") || typecount.containsKey("Work") || typecount.containsKey("Health")) {
			for (String key : keys) {
				if (key.equals("Education") || key.equals("Work") || key.equals("Health")) {
					return key;
		}	}	}
		else if (typecount.containsKey("Entertainment") || typecount.containsKey("Service") || typecount.containsKey("Dwelling")) {
			for (String key : keys) {
				if (key.equals("Entertainment") || key.equals("Service") || key.equals("Dwelling")) {
					return key;
		}   }   }
		else if (typecount.containsKey("Transportation") || typecount.containsKey("Transportation_network")) {
			for (String key : keys) {
				if (key.equals("Transportation") || key.equals("Transportation_network")) {
					return key;
		}	}	}
		else {
			zoneType = "Others";
		}	
		
		return zoneType;
	}
		
	private static HashMap<String, Integer> sortByComparator(Map<String, Integer> unsortMap) {
		
        List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Integer>>() {
            public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        // Maintaining insertion order with the help of LinkedList
        HashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
}
