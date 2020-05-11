package org.tweet;

public class VdCoordinate extends TweetRecord {

	private double eps;
	private double et;
	private int k;
	
	private VdCoordinate(VdCoordinateBuilder builder) {
		super(builder);
		this.eps = builder.eps;
		this.et = builder.et;
		this.k = builder.k;
	}
	
	public VdCoordinate(TweetRecord t) {
		super(new TweetRecordBuilder(t.id, t.user_id, t.latitude, t.longitude)
			                        .x(t.x).y(t.y).day(t.day).month(t.month).year(t.year).second(t.second).minute(t.minute).hour(t.hour)
			                        .create_at(t.create_at).time(t.time).placeId(t.place_id).distance(t.distance).cluster_label(t.cluster_label));
	}
	
	public double getEps() {
		return eps;
	}
	
	public void setEps(double eps) {
		this.eps = eps;
	}
	
	public double getEt() {
		return et;
	}
	
	public int getK() {
		return k;
	}
	
	public void setK(int k) {
		this.k = k;
	}
	
	public static class VdCoordinateBuilder extends TweetRecord.TweetRecordBuilder {
		private double eps = 0.0;
		private double et = 0.0;
		private int k = -1;
		
		public VdCoordinateBuilder(long id, long user_id, double lat, double lon) {
			super(id, user_id, lat, lon);
		}
		
		public TweetRecordBuilder eps(double eps) {
			this.eps = eps;
			return this;
		}
		
		public TweetRecordBuilder et(double et) {
			this.et = et;
			return this;
		}
		
		public TweetRecordBuilder k(int k) {
			this.k = k;
			return this;
		}
		
		public VdCoordinate build() {
			return new VdCoordinate(this);
		}	
	}
}
