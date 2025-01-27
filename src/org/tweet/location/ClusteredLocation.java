package org.tweet.location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.tweet.TweetRecord;
/*import org.opensphere.geometry.algorithm.ConcaveHull;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Polygon;*/

public class ClusteredLocation {

	private final int cid;
	private long rep_id; // representative Tweet ID
	private double latitude;
	private double longitude;
	private double x;
	private double y;
	
	private double median_latitude;
	private double median_longitude;
	private double mean_latitude;
	private double mean_longitude;
	private double eps;
	private int k;
	
	private double start_time;
	private double end_time;
	private int size;
	private double percentage;
	private int zone_id;
	private String zone_type;
	
	public int inTimes;
	public int outTimes;
	
	private List<TweetRecord> records;
	
	public ClusteredLocation(int id)
	{
		cid = id;
	}
	
	ClusteredLocation(ClusteredLocationBuilder builder) {
		this.cid = builder.cid;
		this.rep_id = builder.rep_id;
		this.latitude = builder.latitude;
		this.longitude = builder.longitude;
		this.x = builder.x;
		this.y = builder.y;
		this.median_latitude = builder.median_latitude;
		this.median_longitude = builder.median_longitude;
		this.mean_latitude = builder.mean_latitude;
		this.mean_longitude = builder.mean_longitude;
		this.eps = builder.eps;
		this.k = builder.k;
		this.start_time = builder.start_time;
		this.end_time = builder.end_time;
		this.size = builder.size;
		this.percentage = builder.percentage;
		this.records = builder.records;
		this.zone_id = builder.zone_id;
		this.zone_type = builder.zone_type;
	}
	
	
	public void In()
	{
		inTimes++;
	}
	
	public void Out()
	{
		outTimes++;
	}
	
	
	public int getClusterId() {
		return cid;
	}
	
	public long getRepId() {
		return rep_id;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public double getMedianLatitude() {
		return median_latitude;
	}
	
	public double getMedianLongitude() {
		return median_longitude;
	}
	
	public double getMeanLatitude() {
		return mean_latitude;
	}
	
	public double getMeanLongitude() {
		return mean_longitude;
	}
	
	public double getEps() {
		return eps;
	}
	
	public int getK() {
		return k;
	}
	
	public double getStartTime() {
		return start_time;
	}
	
	public double getEndTime() {
		return end_time;
	}
	
	public int getSize() {
		return size;
	}
	
	public double getPercentage() {
		return percentage;
	}
	
	public List<TweetRecord> getTweetRecords(){
		return records;
	}
	
	public int getZoneId() {
		return zone_id;
	}
	
	public String getZoneType() {
		return zone_type;
	}
	
	/*public String GetLandUseTypeFromXY(HashMap<Geometry, String> map)
	{
		if(map == null){
			return null;
		}
		String type = "Other";
		Geometry pGeo = new GeometryFactory().createPoint(new Coordinate(x, y));
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			Geometry polygon = (Geometry) pairs.getKey();
			if(pGeo.within(polygon))
			{
				if(pairs.getValue() != null)
				{
					type = (String) pairs.getValue();
					return type;
				}
			}
		}
		return type;
	}
	*/
	/*public String GetLandUseType(HashMap<Geometry, String> map)
	{
		String type = "Other";
		Geometry pGeo = new GeometryFactory().createPoint(new Coordinate(longitude, latitude));
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			Geometry polygon = (Geometry) pairs.getKey();
			if(pGeo.within(polygon))
			{
				if(pairs.getValue() != null)
				{
					type = (String) pairs.getValue();
					return type;
				}
			}
		}
		return type;
	}*/
	
	/*public static String GetLandUseTypeByPoint(HashMap<Geometry, String> map, double x, double y)
	{
		String type = "";
		Geometry pGeo = new GeometryFactory().createPoint(new Coordinate(x, y));
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			Geometry polygon = (Geometry) pairs.getKey();
			if(pGeo.within(polygon))
			{
				type = (String) pairs.getValue();
				return type;
			}
		}
		return type;
	}*/
	
	public List<TweetRecord> getConvexHull() {
		
		List<TweetRecord> xSorted = new ArrayList<TweetRecord>(records); 
		Collections.sort(xSorted, new XCompare());

		int n = xSorted.size();

		TweetRecord[] lUpper = new TweetRecord[n];

		lUpper[0] = xSorted.get(0);
		lUpper[1] = xSorted.get(1);

		int lUpperSize = 2;

		for (int i = 2; i < n; i++) {
			lUpper[lUpperSize] = xSorted.get(i);
			lUpperSize++;

			while (lUpperSize > 2 && !rightTurn(lUpper[lUpperSize - 3], lUpper[lUpperSize - 2], lUpper[lUpperSize - 1])) {
				// Remove the middle point of the three last
				lUpper[lUpperSize - 2] = lUpper[lUpperSize - 1];
				lUpperSize--;
			}
		}
		
		TweetRecord[] lLower = new TweetRecord[n];
		lLower[0] = xSorted.get(n - 1);
		lLower[1] = xSorted.get(n - 2);

		int lLowerSize = 2;

		for (int i = n - 3; i >= 0; i--)
		{
			lLower[lLowerSize] = xSorted.get(i);
			lLowerSize++;

			while (lLowerSize > 2 && !rightTurn(lLower[lLowerSize - 3], lLower[lLowerSize - 2], lLower[lLowerSize - 1])) {
				// Remove the middle point of the three last
				lLower[lLowerSize - 2] = lLower[lLowerSize - 1];
				lLowerSize--;
			}
		}
		List<TweetRecord> result = new ArrayList<TweetRecord>();

		for (int i = 0; i < lUpperSize; i++) {
			result.add(lUpper[i]);
		}
		for (int i = 1; i < lLowerSize - 1; i++) {
			result.add(lLower[i]);
		}
		return result;
	}
	
	
	public static TweetRecord getTweetRecord(List<TweetRecord> tweets, double x, double y) {
		
		TweetRecord tr = null;
		for(int i = 0; i < tweets.size(); i++) {
			TweetRecord record = tweets.get(i);
			if(Math.abs(x - record.getX()) < 0.5 && Math.abs(y - record.getY()) < 0.5) {
				tr =record;
				break;
			}
		}
		return tr;
	}

	/*public List<TweetRecord> getConcaveHull() {
		
		List<TweetRecord> points = new ArrayList<TweetRecord>(records); 

		int size = points.size();
		GeometryFactory fac = new GeometryFactory();
	    Coordinate[] coords = new Coordinate[size];
	    for(int t = 0; t < size; t++) {
	        coords[t] = new Coordinate(points.get(t).getX(), points.get(t).getY());
	    }
	    
		MultiPoint mpoints = fac.createMultiPoint(coords);
		
		ConcaveHull ch = new ConcaveHull(mpoints, 5);
		Geometry concaveHull = ch.getConcaveHull();	
		
		List<TweetRecord> result = new ArrayList<TweetRecord>();
		
		if(concaveHull.getGeometryType() == "Polygon") {
			Polygon polygon = (Polygon) concaveHull;
			for(int i = 0; i < polygon.getCoordinates().length; i++) {
	
				double x = (int) polygon.getCoordinates()[i].x;
				double y = (int) polygon.getCoordinates()[i].y;
				
				TweetRecord tr = getTweetRecord(points, x, y);
				
				if(tr != null) {
					result.add(tr);
				}
			}
		}
		return result;
	}*/

	private boolean rightTurn(TweetRecord a, TweetRecord b, TweetRecord c) {		
		return (b.getX()- a.getX())*(c.getY()- a.getY()) - (b.getY() - a.getY())*(c.getX() - a.getX()) > 0;
	}

	public class XCompare implements Comparator<TweetRecord> {
		@Override
		public int compare(TweetRecord o1, TweetRecord o2) {
			return (new Double(o1.getX())).compareTo(new Double(o2.getX()));
		}
	}
	
	public static class ClusteredLocationBuilder {
		private final int cid;
		private long rep_id = 0; // representative Tweet ID
		private double latitude = 0;
		private double longitude = 0;
		private double x = 0;
		private double y = 0;
		
		private double median_latitude = 0;
		private double median_longitude = 0;
		private double mean_latitude = 0;
		private double mean_longitude = 0;
		private double eps = 0;
		private int k = -1;
		
		private double start_time = 0;
		private double end_time = 0;
		private int size = 0;
		private double percentage = 0;
		private int zone_id = 0;
		private String zone_type = "";
		
		private List<TweetRecord> records = null;
		
		public ClusteredLocationBuilder(int cid) {
			this.cid = cid;
		}
		
		// all the following methods are used to set values for optional fields
		public ClusteredLocationBuilder rep_id(long rep_id) {
			this.rep_id = rep_id;
			return this;
		}
		
		public ClusteredLocationBuilder latitude(double lat) {
			this.latitude = lat;
			return this;
		}
		
		public ClusteredLocationBuilder longitude(double lon) {
			this.longitude = lon;
			return this;
		}
		
		public ClusteredLocationBuilder x(double x) {
			this.x = x;
			return this;
		}
		
		public ClusteredLocationBuilder y(double y) {
			this.y = y;
			return this;
		}
		
		public ClusteredLocationBuilder median_latitude(double medianlat) {
			this.median_latitude = medianlat;
			return this;
		}
		
		public ClusteredLocationBuilder median_longitude(double medianlon) {
			this.median_longitude = medianlon;
			return this;
		}
		
		public ClusteredLocationBuilder mean_latitude(double meanlat) {
			this.mean_latitude = meanlat;
			return this;
		}
		
		public ClusteredLocationBuilder mean_longitude(double meanlon) {
			this.mean_longitude = meanlon;
			return this;
		}
		
		public ClusteredLocationBuilder eps(double eps) {
			this.eps = eps;
			return this;
		}
		
		public ClusteredLocationBuilder k(int k) {
			this.k = k;
			return this;
		}
		
		public ClusteredLocationBuilder start_time(double start_time) {
			this.start_time = start_time;
			return this;
		}
		
		public ClusteredLocationBuilder end_time(double end_time) {
			this.end_time = end_time;
			return this;
		}
		
		public ClusteredLocationBuilder size(int size) {
			this.size = size;
			return this;
		}
		
		public ClusteredLocationBuilder percentage(double percentage) {
			this.percentage = percentage;
			return this;
		}
		
		public ClusteredLocationBuilder records(List<TweetRecord> records) {
			this.records = records;
			return this;
		}
		
		public ClusteredLocationBuilder zone_id(int zone_id) {
			this.zone_id = zone_id;
			return this;
		}
		
		public ClusteredLocationBuilder zone_type(String zone_type) {
			this.zone_type = zone_type;
			return this;
		}
		
		public ClusteredLocation build() {
			return new ClusteredLocation(this);
		}
	}
}


