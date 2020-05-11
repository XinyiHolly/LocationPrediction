package org.tweet;

public class AcCoordinate extends TweetRecord{

	private int activityzoneid;
	private String zonetype;
	
	private AcCoordinate(AcCoordinateBuilder builder) {
		super(builder);
		this.activityzoneid = builder.activityzoneid;
		this.zonetype = builder.zonetype;
	}
	
	public AcCoordinate(TweetRecord t) {
		super(new TweetRecordBuilder(t.id, t.user_id, t.latitude, t.longitude)
			                        .x(t.x).y(t.y).day(t.day).month(t.month).year(t.year).second(t.second).minute(t.minute).hour(t.hour)
			                        .create_at(t.create_at).time(t.time).placeId(t.place_id).distance(t.distance).cluster_label(t.cluster_label));
	}
	
	public AcCoordinate copy() {
		return (AcCoordinate) new AcCoordinateBuilder(id, user_id, latitude, longitude)
				.x(x).y(y).day(day).month(month).year(year)
				.second(second).minute(minute).hour(hour).create_at(create_at).time(time)
				.placeId(place_id).distance(distance).build();
	}
	
	public int getActivityZoneId() {
		return activityzoneid;
	}
	
	public void setActivityZoneId(int zone_id) {
		this.activityzoneid = zone_id;
	}
	
	public String getZoneType() {
		return zonetype;
	}
	
	public void setZoneType(String zone_type) {
		this.zonetype = zone_type;
	}
	
	public static class AcCoordinateBuilder extends TweetRecord.TweetRecordBuilder {
		private int activityzoneid = -1;
		private String zonetype = null;
		
		public AcCoordinateBuilder(long id, long user_id, double lat, double lon) {
			super(id, user_id, lat, lon);
		}
		
		public TweetRecordBuilder activityzoneid(int activityzoneid) {
			this.activityzoneid = activityzoneid;
			return this;
		}
		
		public TweetRecordBuilder zonetype(String zonetype) {
			this.zonetype = zonetype;
			return this;
		}
		
		public AcCoordinate build() {
			return new AcCoordinate(this);
		}	
	}
}
