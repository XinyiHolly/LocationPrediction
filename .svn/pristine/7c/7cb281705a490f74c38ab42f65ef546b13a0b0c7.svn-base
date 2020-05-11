package org.tweet.location;

public class ReportedLocation {
	
	public enum Emotion {
		MIXED, PLEASANT, NEUTRAL, UNPLEASANT, UNKNOWN, NA;
	}

	public enum Risk {
		NO, LOW, MEDIUM, HIGH, UNKNOWN; 
	}

	private final long utc;
	private final String street_address;
	private String city;
	private String state;
	private String placetype;
	private Boolean drank;
	private Boolean alcohol;
	private Emotion emotion;
	private Risk risk;
	private Boolean avoid;
	private Boolean vacation;
	private String full_address;
	private double latitude;
	private double longitude;
	
	ReportedLocation(ReportedLocationBuilder builder) {
		this.utc = builder.utc;
		this.street_address = builder.street_address;
		this.city = builder.city;
		this.state = builder.state;
		this.placetype = builder.placetype;
		this.drank = builder.drank;
		this.alcohol = builder.alcohol;
		this.emotion = builder.emotion;
		this.risk = builder.risk;
		this.avoid = builder.avoid;
		this.vacation = builder.vacation;
		this.full_address = builder.full_address;
		this.latitude = builder.latitude;
		this.longitude = builder.longitude;		
	}
	
	public long getUTC() {
		return utc;
	}
	
	public String getStreetAddress() {
		return street_address;
	}
	
	public String getCity() {
		return city;
	}
	
	public String getState() {
		return state;
	}
	
	public String getPlaceType() {
		return placetype;
	}
	
	public Boolean getDrank() {
		return drank;
	}
	
	public Boolean getAlcohol() {
		return alcohol;
	}
	
	public Emotion getEmotion() {
		return emotion;
	}
	
	public Risk getRisk() {
		return risk;
	}
	
	public Boolean getAvoid() {
		return avoid;
	}
	
	public Boolean getVacation() {
		return vacation;
	}
	
	public String getFullAddress() {
		return full_address;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public static class ReportedLocationBuilder {
		private final long utc;
		private final String street_address;
		private String city = null;
		private String state = null;
		private String placetype = null;
		private Boolean drank = null;
		private Boolean alcohol = null;
		private Emotion emotion = null;
		private Risk risk = null;
		private Boolean avoid = null;
		private Boolean vacation = null;
		private String full_address = null;
		private double latitude = 0.0;
		private double longitude = 0.0;
		
		public ReportedLocationBuilder(long utc, String street_address) {
			this.utc = utc;
			this.street_address = street_address;
		}
		
		// all the following methods are used to set values for optional fields
		public ReportedLocationBuilder city(String city) {
			this.city = city;
			return this;
		}
		
		public ReportedLocationBuilder state(String state) {
			this.state = state;
			return this;
		}
		
		public ReportedLocationBuilder placetype(String placetype) {
			this.placetype = placetype;
			return this;
		}
		
		public ReportedLocationBuilder drank(Boolean drank) {
			this.drank = drank;
			return this;
		}
		
		public ReportedLocationBuilder alcohol(Boolean alcohol) {
			this.alcohol = alcohol;
			return this;
		}
		
		public ReportedLocationBuilder emotion(Emotion emotion) {
			this.emotion = emotion;
			return this;
		}
		
		public ReportedLocationBuilder risk(Risk risk) {
			this.risk = risk;
			return this;
		}
		
		public ReportedLocationBuilder avoid(Boolean avoid) {
			this.avoid = avoid;
			return this;
		}
		
		public ReportedLocationBuilder vacation(Boolean vacation) {
			this.vacation = vacation;
			return this;
		}
		
		public ReportedLocationBuilder full_address(String full_address) {
			this.full_address = full_address;
			return this;
		}
		
		public ReportedLocationBuilder latitude(double lat) {
			this.latitude = lat;
			return this;
		}
		
		public ReportedLocationBuilder longitude(double lon) {
			this.longitude = lon;
			return this;
		}
		
		public ReportedLocation build() {
			return new ReportedLocation(this);
		}
	}
}
