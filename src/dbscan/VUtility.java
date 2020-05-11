package dbscan;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.tweet.TweetRecord;
import org.tweet.VdCoordinate;

public class VUtility {
	
	public static List<VdCoordinate> VisitList = new ArrayList<VdCoordinate>();
	
	public static double getDistance (VdCoordinate p, VdCoordinate q) {
		double dx = p.getX() - q.getX();
		double dy = p.getY() - q.getY();
		double distance = Math.sqrt (dx * dx + dy * dy);
		return distance;
	}
	
	public static double getTimeDistance(VdCoordinate p, VdCoordinate q) {
		// time difference in minutes
		return Math.abs(p.getMinuteTime() - q.getMinuteTime());
	}
	
	public static List<VdCoordinate> getNeighbours(VdCoordinate p) {
		List<VdCoordinate> neigh = new ArrayList<VdCoordinate>();
		Iterator<VdCoordinate> points = VDbscan.pointList.iterator();
		while(points.hasNext()) {
			VdCoordinate q = points.next();
			if(getDistance(p,q) <= p.getEps()) {
				neigh.add(q);
			}
		}
		return neigh;
	}

	public static void Visited(VdCoordinate d) {
		VisitList.add(d);
	}

	public static boolean isVisited(VdCoordinate c) {
		if(VisitList.contains(c)) {
			return true;
		} else {
			return false;
		}
	}

	public static List<VdCoordinate> Merge(List<VdCoordinate> a, List<VdCoordinate> b) {	
		Iterator<VdCoordinate> it5 = b.iterator();
		while(it5.hasNext()) {
			VdCoordinate t = it5.next();
			if (!a.contains(t) ) {
				a.add(t);
			}
		}
		return a;
	}

	public static List<VdCoordinate> getList() {
		List<VdCoordinate> newList = new ArrayList<>();
		newList.clear();
		return newList;
	}

	public static Boolean equalPoints(TweetRecord m , TweetRecord n) {
		if((m.getX()==n.getX())&&(m.getY()==n.getY()))
			return true;
		else
			return false;
	}
}







