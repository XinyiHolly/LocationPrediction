package org.social.spatialcluster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.tweet.TweetRecord;
import org.tweet.VdCoordinate;

import dbscan.dbscan;
import dbscan.VDbscan;


class SortbyVEpsDistance implements Comparator<VdCoordinate> {
	@Override
	public int compare(VdCoordinate v1, VdCoordinate v2) {
		return Double.compare(v1.getEps(), v2.getEps());
	}
}

class SortbyVEtDistance implements Comparator<VdCoordinate> {
	@Override
	public int compare(VdCoordinate o1, VdCoordinate o2) {
		// TODO Auto-generated method stub
		return Double.compare(o1.getEt(), o2.getEt());
	}
}

class SortbyKDistance implements Comparator<Double> {
	@Override
	public int compare(Double d1, Double d2) {
		return Double.compare(d1, d2);
	}
}

public class MDBSCANCluster implements SpatialCluster {
	private List<VdCoordinate> pointList;
	private int threshold;
	private int k;	
	
	public MDBSCANCluster(List<TweetRecord> tweets, int threshold) {
		pointList = new ArrayList<>();
		for (int i = 0; i < tweets.size(); i++) {
			VdCoordinate r = new VdCoordinate(tweets.get(i));
			pointList.add(r);
		}
		this.threshold = threshold; 
		k = 0;
	}
	
	/*public static void main(String[] args) {
		
		long user_id = 560268128;
		int threshold = 4;
		
		String dbName = "madison";
		//String tableName = "madisontweets";
		String tableName = "mt_ultraselected";

		MDBSCANCluster rc = new MDBSCANCluster(user_id, dbName, tableName, threshold);		
		
		//RunClustering rc = new  RunClustering(clusterName, eps, minpt, threshold);		
	}*/
	
	/**this variable is used to control the minimum eps*/
	public double mineps = 20.0; 
	public double maxeps = 300; 
	@Override
	public List<List<?>> cluster() {		
		// reset point list and result list
		List<List<?>> resultList = new ArrayList<>();
		
		int size = Integer.MAX_VALUE;
		int rank = 1;
				
		dbscan dbscan = null;
		// base case 1: limited rest points
		while (pointList.size() >= threshold) {	
			if (pointList.size() < size) {
				rank = 1;
			} else {
				rank ++;
			}			
			double eps = GetEps(rank);	
			size = pointList.size();
			
			System.out.println("eps and size : " + eps + "," +  size);
			
			// base case 2: use up possible k with no cluster found
			if ( eps < 0  ) {
				break;
			}
			
			// if it is too large, then no meaningful
			if ( eps >  maxeps ) {				
				break;
			}
			
			// if the esp is less the minimum value, then make it as the minimum value
			if ( eps < mineps ) {
				
				eps = mineps;				
			}
			
				
				
			try {
				if (k > 0) {
					dbscan = new dbscan(eps, k);
					for (int i = 0; i < pointList.size(); i ++) {
						dbscan.addPoint(pointList.get(i));
					}
					dbscan.applyDbscan();
					// Reset point list
					List<List<?>> clusters = dbscan.getResultList();
					ResetPoints(eps, k, clusters, resultList);
				} else {
					System.out.println("Error!");
				}	
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}	
		return resultList;
	}
	
	@SuppressWarnings("unchecked")
	private void ResetPoints(double eps, int k, List<List<?>> clusters, List<List<?>> resultList) {		
		for (int i = 0; i < clusters.size(); i++) {		
			List<VdCoordinate> cluster = (List<VdCoordinate>) clusters.get(i);			
			if (cluster.size() < threshold) {
				clusters.remove(i);
				i--;
				continue;
			}
			resultList.add(cluster);
			for (int j = 0; j < cluster.size(); j++) {
				for (int h = 0; h < pointList.size(); h++) {
					if (cluster.get(j).equals(pointList.get(h))) {
						pointList.get(h).setEps(eps);
						pointList.remove(h);
						h --;
					}
				}
			}
			System.out.println("pointList size: " + pointList.size());
		}
	}
	
	public double GetEps(int rank) {
		//get average distance from point(i) to other points
		double sumDistance = 0;
		//System.out.println("total point size : " + points.size());

		for (int i = 0; i < pointList.size(); i++) {

			List<Double> distances = new ArrayList<>();
			for (int j = 0; j < pointList.size(); j++) {
				if (i == j)
					continue;

				double distance = Math.sqrt((pointList.get(i).getX() - pointList.get(j).getX()) * (pointList.get(i).getX() - pointList.get(j).getX())
						        + (pointList.get(i).getY() - pointList.get(j).getY()) * (pointList.get(i).getY() - pointList.get(j).getY()));

				//System.out.println("distance: " + distance  );
				if (distance > 500) //300
					continue;
				
				distances.add(distance);
			}
			
			double curDistDifference = 0.0;
			double sumDistDifference = 0.0;
			double averageDistDifference = Double.MIN_VALUE;
			double averageDistance = 0;			
			int disSize = distances.size();
			
			if(disSize == 0) {
				// noise? remove this points
				// System.out.println("it is here");
				pointList.remove(i);
				 i--;
			} else if(disSize == 1) {
				averageDistance = distances.get(0);
				sumDistance += averageDistance;
			} else {
				//System.out.println("total distance size : " + distances.size());
				Collections.sort(distances);
				averageDistance = distances.get(0);
				double ptNum = 1;
				
				for(int k = 1; k < distances.size(); k++) {					
					
					curDistDifference = distances.get(k) - distances.get(k-1);

					sumDistDifference += curDistDifference;

					averageDistDifference = sumDistDifference/ptNum; 

					//System.out.println("DistDifference : " + distances.get(k) + "," +  distances.get(k-1) + "," + curDistDifference + "," + averageDistDifference );	

					if ((k >= distances.size() * 0.75) && (Math.abs(curDistDifference) > 4 * averageDistDifference) && (averageDistDifference) > 1.0) {

						//System.out.println(Double.toString(curDistDifference) + " " + Double.toString(averageDistDifference)) ;
						break;
						
					} else {
						averageDistance += distances.get(k);
						ptNum++;
					}
				}				
				//ptNum = distances.size();
				averageDistance = averageDistance / ptNum;
				sumDistance += averageDistance;				
			}
		}	

		double avgd = sumDistance / pointList.size();
		//System.out.println("final average distance : " + avgd);
		List<Integer> kArray = new ArrayList<Integer>();
		
		for (int i = 0; i < pointList.size(); i++) {
			int k = 0;
			double eps = Double.MIN_VALUE;
			for (int j = 0; j < pointList.size(); j++) {
				if (i == j)
					continue;
				double radius = Math.sqrt((pointList.get(i).getX() - pointList.get(j).getX()) * (pointList.get(i).getX() - pointList.get(j).getX())
						      + (pointList.get(i).getY() - pointList.get(j).getY()) * (pointList.get(i).getY() - pointList.get(j).getY()));
				if (radius < avgd) {
					k++;
					if (radius > eps)
						eps = radius;
				}
			}
			k = k - 1; 
			pointList.get(i).setK(k);			
			if(k > 0) {
				kArray.add(k);
			}		
		}

		// find mode of k values and calculate eps
		k = VDbscan.getMode(kArray, rank);
		// sign: use up possible k with no cluster found 
		if (k == -2) {
			return -1;
		}		

		for(int i = 0; i < pointList.size(); i++) {
			List<Double> distances = new ArrayList<Double>();
			for (int j = 0; j < pointList.size(); j++) {
				if (i == j)
					continue;
				double radius = Math.sqrt((pointList.get(i).getX() - pointList.get(j).getX()) * (pointList.get(i).getX() - pointList.get(j).getX())
						      + (pointList.get(i).getY() - pointList.get(j).getY()) * (pointList.get(i).getY() - pointList.get(j).getY()));
				distances.add(radius);
			}
			Collections.sort(distances, new SortbyKDistance());

			if (distances.size() < (k + 1)) {
				pointList.get(i).setEps(distances.get(distances.size() - 1));
			} else {
				pointList.get(i).setEps(distances.get(k));
			}
		}
				
		// sort by kst distance
		Collections.sort(pointList, new SortbyVEpsDistance());		

		// find eps
		double eps = 0.0;
		int start = 0;
		int end = 0;
		boolean isFlat = true;
		double curDifference = 0.0;
		double averageDifference = Double.MIN_VALUE;
		double sumDifference = 0.0;
		double y_0 = pointList.get(0).getEps();
		double x_0 = 1.0;
 		double y_n = pointList.get(pointList.size()-1).getEps();
		double x_n = (double)pointList.size();

		for (int i = 0; i < pointList.size() - 1; i++) {
			if (pointList.get(end).getEps() > 1000 || i == pointList.size() - 2) {
				y_n = pointList.get(i).getEps();
				x_n = (double)(i+1);
				double deta_x = x_n - x_0;
		 		double deta_y = y_n - y_0;
		 		double A = deta_y/deta_x;
		 		double B = -1.0;
		 		double C = y_0 - deta_y/deta_x;
		 		double maxDistance = 0.0;   
		 		int knee = (int)x_0 + 1;
		 		for (int j = start; j <= i; j++) {			
		 			double y = pointList.get(j).getEps();
		 			double x = (double)(j+1);
		 			double distance = Math.abs(A*x+B*y+C)/Math.sqrt(A*A+B*B);
		 			if (distance > maxDistance) {
		 				maxDistance = distance;
		 				knee = j;
		 			}
		 		}   
		 		eps = pointList.get(knee).getEps();
				break;
			}
			curDifference = pointList.get(i + 1).getEps() - pointList.get(i).getEps();
			sumDifference += curDifference;
			if (i == 0){
				averageDifference = Math.abs(curDifference);
			}else{
				averageDifference = sumDifference/(i+1);
			}
			//System.out.println("current different, average "  + Double.toString(curDifference) + " " + Double.toString(averageDifference)) ;
			if (isFlat == false && Math.abs(curDifference) < averageDifference) {
				isFlat = true;
				//System.out.println("current different, average " + Double.toString(curDifference) + " " + Double.toString(averageDifference)) ;
				y_n = pointList.get(i).getEps();
				x_n = (double)(i+1);
				double deta_x = x_n - x_0;
		 		double deta_y = y_n - y_0;
		 		double A = deta_y/deta_x;
		 		double B = -1.0;
		 		double C = y_0 - deta_y/deta_x;
		 		double maxDistance = 0.0;   
		 		int knee = (int)x_0 + 1;
		 		for (int j = start; j <= i; j++) {			
		 			double y = pointList.get(j).getEps();
		 			double x = (double)(j+1);
		 			double distance = Math.abs(A*x+B*y+C)/Math.sqrt(A*A+B*B);
		 			if (distance > maxDistance) {
		 				maxDistance = distance;
		 				knee = j;
		 			}
		 		}   
		 		eps = pointList.get(knee).getEps();
				break;
				
			} else {
				if (Math.abs(curDifference) > 4 * averageDifference && averageDifference > 0.0) {
					isFlat = false;
				} else if (Math.abs(curDifference) > averageDifference){
					averageDifference = Math.abs(curDifference);
				}
			}					
			end++;
		}
		return eps;
	}
	
}
