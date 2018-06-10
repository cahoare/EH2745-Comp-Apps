package Project;

import java.util.ArrayList;
import java.util.Arrays;


// Notes for improvement...
// Add in initialisation randomisation... ??? Easy

public class Kmeans {

	private static ArrayList<Integer> times; //timestamps
	private static ArrayList<double[]> values; //actual data
	private final double error = 0.01; 
	
	// TABLE information
	private static String[] tableFormat;

	public Kmeans(Identification data, String[] tableFormat) {
		values = data.getDataTable();
		times = data.getTimeStamps();
		Kmeans.tableFormat = tableFormat;
	}

	/*
	 * Method called to cluster the values with different number of clusters and different initialisation
	 * Args: the number of clusters desired and a random variable 
	 * Returns: the clustered data
	 */
	public ArrayList<ArrayList<double[]>> clusterData(int numClusters, int rand) {

		// create the number of clusters depending on passed input
		// hold this in an arraylist
		// try for four clusters first
		ArrayList<double[]> centroids = new ArrayList<double[]>(); 
		ArrayList <double[]> newCentroids = new ArrayList<double[]>(); 
		ArrayList<ArrayList<double[]>> clusters = new ArrayList<ArrayList<double[]>>();
		
		// initialise the variables
		// add the number of clusters
		// set the initial centroids as the value of individual states in the data
		for (int i = 0; i < numClusters; i++) {
			clusters.add(new ArrayList<double[]>());
			// randomise the initial clusters using rand variable. Assumes some randomisation in data
			newCentroids.add(values.get((rand+i*values.size()/numClusters)%values.size()));
		}
	
		// while the centroids are still going to move more than a min distance
		// assign the centroids the new centroid location
		// sort the values into new cluster on the basis of these centroid locations
		// check whether the distance criteria is met and if not repeat
		boolean distGreater = true;
		while (distGreater) {			
			centroids = newCentroids;
			clusters = sortClusters(centroids, clusters);		
			newCentroids = calCentroid(clusters);
			distGreater = checkDist(centroids, newCentroids);
		}
		
		return clusters;
	}

	/*
	 * Checks the distance between the centroids against a specified error
	 * Args: the two centroid locations
	 * Returns: boolean true if distance is greater than the error
	 */
	private boolean checkDist(ArrayList<double[]> oldCentroids, ArrayList<double[]> newCentroids) {
		
		double [] totalDist = new double [oldCentroids.size()];
		boolean distGreater = false;
		
		// for each cluster
		for (int i = 0; i<oldCentroids.size(); i++) {
			// check the difference in the old centroid vs the new
			// example solution normalises this?? dont believe this is correct
			for (int j = 0; j < tableFormat.length; j++) {
				totalDist[i] += Math.abs(oldCentroids.get(i)[j] - newCentroids.get(i)[j]);
			}
			if (totalDist[i] > error) 
				distGreater =true;
		}
		
		return distGreater;
	}
	
	/*
	 * Calculates the centroid locations
	 * Args: the clustered data
	 * Returns: the centroid locations of the clusters. Public is also called from run. 
	 */
	public ArrayList<double[]> calCentroid(ArrayList<ArrayList<double[]>> clusters) {
		
		ArrayList <double[]> newCentroids = new ArrayList<double[]>();

		// for all the clusters
		for (int i=0; i<clusters.size(); i++) {
			// this is variable for new centroid for each cluster
			double [] centroidHold = new double[tableFormat.length];
			// for each entry in that cluster
			for (int j = 0; j<clusters.get(i).size();j++) {
				// calculate the average value of each table element for that cluster
				for (int x = 0; x<tableFormat.length; x++) {
					centroidHold[x] += clusters.get(i).get(j)[x]/clusters.get(i).size();
				}
			}
			// this is the new centroid for that cluster
			newCentroids.add(centroidHold);
		}	
		return newCentroids;		
	}

	/*
	 * Sorts the data by best fitting cluster
	 * Args: existing clustered data and centroid locations
	 * Returns: the new data clusters
	 */
	private ArrayList<ArrayList<double[]>> sortClusters(ArrayList <double[]> centroids, ArrayList<ArrayList<double[]>> clusters) {
		
		// Clear the clusters. (Possibly may be more optimal way of updating? Investigate...)
		for (int i = 0; i < clusters.size(); i++) {
			clusters.get(i).clear();
		}
		
		// for each time state 
		for (int i = 0; i < values.size(); i++) {
			
			// array for determining how far a certain state is from each cluster (first index for cluster 1, etc.). Re-init with each new value
			double[] clusterDist = new double[centroids.size()];
			
			// for each element in the time state
			for (int j = 0; j< tableFormat.length; j++) {
				// calculate the distance between the corresponding element in each cluster
				for (int x = 0; x < clusterDist.length; x++) {
					clusterDist[x] += Math.pow((values.get(i)[j]-centroids.get(x)[j]),2);
				}
			}
			
			// take the square root to find the euclidean distance
			for (int x = 0; x < clusterDist.length; x++) {
				clusterDist[x] = Math.sqrt(clusterDist[x]);
			}

			// find the minimum distance cluster and add to that cluster's arraylist
			int minDistIndex = 0;
			for (int x = 1; x < clusterDist.length; x++) {
				if (clusterDist[x] < clusterDist[minDistIndex])
					minDistIndex = x;
			}
			clusters.get(minDistIndex).add(values.get(i));
		}
	
		return clusters;
	}
	
	/*
	 * Finds the overall cost value of the clusters
	 * Args: the clustered data
	 * Returns: the cost value
	 */
	public double calCost(ArrayList<ArrayList<double[]>> clusters) {
		
		ArrayList<double[]> centroids = calCentroid(clusters);
		double totCost = 0;
		
		// for each cluster
		for (int i = 0; i<clusters.size(); i++){
			
			// for each value in the cluster
			for(int j = 0; j<clusters.get(i).size(); j++) {
				
				double distHold = 0;
				
				//for each element in that value
				for(int x = 0; x< tableFormat.length; x++) {
					//calculate the distance between the element and the centroid element
					distHold = Math.pow((clusters.get(i).get(j)[x]-centroids.get(i)[x]),2);
				}
				totCost += Math.sqrt(distHold);
			}
		}	
		return totCost;
	}
	
	/* 
	 * Classify the states from the centroids
	 * Args: the clustered data
	 * Returns: a string array of each state
	 */
	public int[] clustClassify(ArrayList<ArrayList<double[]>> clusters, int [][] lineData, int[] busData) {
		
		ArrayList<double[]> centroids = calCentroid(clusters);
		double [] lineLimits = {0, 3.5, 10};
		
		// return array - which cluster to which state. {gen, line, peak, low}
		int [] states = new int [centroids.size()];
		
		// Based on rules described in classification document. Slack bus flow based. Slack bus always 0 angle can just base on Bus 4 angle
		for(int i =0; i<centroids.size(); i++) {
			double deltaAng14 = -centroids.get(i)[7];
			System.out.println(deltaAng14);
			if(deltaAng14 < lineLimits[0]) {
				states[i] = 3; // Low load
			}
			else if (deltaAng14 < lineLimits[1]) {
				states[i] = 1; // Line break
			}
			else if (deltaAng14 < lineLimits[2]) {
				states[i] = 0; // Gen disconnected
			}
			else {
				states[i] = 2; // Peak load
			}

		}
		return states;
	
	}	
	
}
