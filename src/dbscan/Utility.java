package dbscan;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.tweet.TweetRecord;

public class Utility {
	public static List<TweetRecord> VisitList = new ArrayList<TweetRecord>();
	
	public static double getDistance (TweetRecord p, TweetRecord q) {
		double dx = p.getX() - q.getX();
		double dy = p.getY() - q.getY();
		double distance = Math.sqrt (dx * dx + dy * dy);
		return distance;
	}
	
	public static double getTimeDistance(TweetRecord p, TweetRecord q) {
		// time difference in minutes
		return Math.abs(p.getMinuteTime() - q.getMinuteTime());
	}

	public static List<TweetRecord> getNeighbors(TweetRecord p, List<TweetRecord> pointList, double eps) {
		List<TweetRecord> neigh = new ArrayList<TweetRecord>();
		Iterator<TweetRecord> points = pointList.iterator();
		while(points.hasNext()) {
			TweetRecord q = points.next();		
			if(getDistance(p,q) <= eps) {
				neigh.add(q);
			}
		}
		return neigh;
	}
	
	/*public static Vector<TweetRecord> getStNeighborsNew(TweetRecord p) {
		Vector<TweetRecord> neigh = new Vector<TweetRecord>();
		Iterator<TweetRecord> points = stDbscanNew.pointList.iterator();
		while(points.hasNext()) {
			TweetRecord q = points.next();
			if(getDistance(p,q)<= stDbscanNew.e && getTimeDistance(p, q) <= stDbscanNew.et) {
				neigh.add(q);
			}
		}
		return neigh;		
	}*/

	public static void Visited(TweetRecord d) {
		VisitList.add(d);
	}

	public static boolean isVisited(TweetRecord c) {
		if(VisitList.contains(c)) {
			return true;
		}
		else {
			return false;
		}
	}

	public static List<TweetRecord> Merge(List<TweetRecord> a, List<TweetRecord> b) {
	    int label = a.get(0).getClusterLabel();
		Iterator<TweetRecord> it5 = b.iterator();
		while(it5.hasNext()) {
			TweetRecord t = it5.next();
			if (!a.contains(t)) {
				t.setClusterLabel(label);
				a.add(t);
			}
		}
		return a;
	}
	
	public static Boolean equalPoints(TweetRecord m, TweetRecord n) {
		if (m.getX() == n.getX() && m.getY() == n.getY())
			return true;
		else
			return false;
	}	
	
	public static Boolean equalSTPoints(TweetRecord m, TweetRecord n) {
		if (m.getX() == n.getX() && m.getY() == n.getY() && m.getHourTime() == n.getHourTime())
			return true;
		else
			return false;
	}	
	
	// The algorithm for determine Eps, as described in Huang and Wong 2015, Annals of AAG
	public static double determineEps(List<List<TweetRecord>> resultList, int minPts, int threshold) {
		double e = 0.0;
		
		for(int i = 0; i < resultList.size(); i++) {
			List<TweetRecord> cluster = resultList.get(i);
			
			if(cluster.size() < threshold) 
				continue;

			int s = cluster.size();
			double[] dists = new double[s];
			
			double sumMinPtsND = 0.0;
			for(int m = 0; m < s; m++) {
				TweetRecord tweet1 = cluster.get(m);
				for(int n = 0; n < s; n++) {
					TweetRecord tweet2 = cluster.get(n);
					dists[n] = getDistance(tweet1, tweet2);
				}
				Arrays.sort(dists, 0, s);
				sumMinPtsND += dists[minPts-1];
			}
			double AMinPtsi = sumMinPtsND / s;
			if(AMinPtsi > e)
				e = AMinPtsi;
		}		
		return e;
	}
	
	// The counterpart algorithm for determine TEps
	public static double determineTEps(List<List<TweetRecord>> resultList, int minPts, int threshold) {
		double et = 0.0; // time interval in minute
		
		for(int i = 0; i < resultList.size(); i++) {
			List<TweetRecord> cluster = resultList.get(i);
			
			if(cluster.size() < threshold) 
				continue;

			int s = cluster.size();
			double[] dists = new double[s];
			
			double sumMinPtsND = 0.0;
			for(int m = 0; m < s; m++) {
				TweetRecord tweet1 = cluster.get(m);
				for(int n = 0; n < s; n++) {
					TweetRecord tweet2 = cluster.get(n);
					dists[n] = getTimeDistance(tweet1, tweet2);
				}
				Arrays.sort(dists, 0, s);
				sumMinPtsND += dists[minPts-1];
			}
			double AMinPtsi = sumMinPtsND / s;
			if(AMinPtsi > et) {
				et = AMinPtsi;
			}
		}	
		return et;
	}
}







