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
	 * Returns: the centroid locations of the clusters
	 */
	private ArrayList<double[]> calCentroid(ArrayList<ArrayList<double[]>> clusters) {
		
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
	
	/* ONLY SET UP FOR 4 CLUSTERS
	 * Classify the states from the centroids
	 * Args: the clustered data
	 * Returns: a string array of each state
	 */
	public int[] clustClassify(ArrayList<ArrayList<double[]>> clusters, int [][] lineData, int[] busData) {
		
		ArrayList<double[]> centroids = calCentroid(clusters);
		
		// return array - which cluster to which state. {gen, line, peak, low}
		int [] states = new int [clusters.size()];
		
		// Based on voltage angle differences
		// Basic rules are as follows:
		// If the delta on a generator bus is 0 then the generator must be off
		// If the total delta (in and out) at a branch bus then a line must be tripped
		// The two remaining states are high load and low load, which can be found summing the delta at the load buses
		
		// Static references - could fix. Gen bus data is in first three line data rows
		// Check gen data
		try {
		boolean found = false;
		for (int i = 0; i < centroids.size(); i++) {
			for (int j = 0; j<3; j++) {
				double genPF = centroids.get(i)[2*lineData[j][0]-1] - centroids.get(i)[2*lineData[j][1]-1];
				if(genPF<0.1) {
					found = true;
					states[0] = i;
					break;
				}
			}

		}
		// if for some reason a bus isnt found throw exception
		if (!found) 
			throw new EntryMissingException();

		// Check line data
		found = false;
		for (int i = 0; i < clusters.size(); i++) {
			// Calc Bus 4 = (1-4) - (4-5) - (4-9)
			double pb4 =  (centroids.get(i)[2*1-1] - centroids.get(i)[2*4-1]) +
					(centroids.get(i)[2*5-1] - centroids.get(i)[2*4-1]) +
					(centroids.get(i)[2*9-1] - centroids.get(i)[2*4-1]);
			// Calc Bus 6 = (3-6) + (5-6) - (6-7)
			double pb6 =  (centroids.get(i)[2*3-1] - centroids.get(i)[2*6-1]) +
					(centroids.get(i)[2*5-1] - centroids.get(i)[2*6-1]) -
					(centroids.get(i)[2*6-1] - centroids.get(i)[2*7-1]);
			// Calc Bus 8 = (2-8) + (7-8) - (8-9)
			double pb8 =  (centroids.get(i)[2*2-1] - centroids.get(i)[2*8-1]) +
					(centroids.get(i)[2*7-1] - centroids.get(i)[2*8-1]) -
					(centroids.get(i)[2*8-1] - centroids.get(i)[2*9-1]);
			System.out.println("Lines"+ (pb4+pb6+pb8));
			if(Math.abs(pb4+pb6+pb8) > 20) {
				found = true;
				states[1] = i;
				break;
			}
		}
		// if for some reason a line isnt found throw exception
		if (!found) 
			throw new EntryMissingException();
		
		// For the two remaining clusters calculate the one with highest load
		int [] remainClust = {1,1,1,1};
		remainClust[states[0]] = 0; 
		remainClust[states[1]] = 0;
		
		int index = 0;
		double [] loadTot = new double [2];
		for (int i = 0; i < clusters.size(); i++) {
			if (remainClust[i]==1) {
				double load1 = (centroids.get(i)[2*4-1] - centroids.get(i)[2*5-1]) +
						(centroids.get(i)[2*5-1] - centroids.get(i)[2*6-1]);
				double load2 = (centroids.get(i)[2*6-1] - centroids.get(i)[2*7-1]) +
						(centroids.get(i)[2*8-1] - centroids.get(i)[2*7-1]);
				double load3 = (centroids.get(i)[2*4-1] - centroids.get(i)[2*9-1]) +
						(centroids.get(i)[2*8-1] - centroids.get(i)[2*9-1]);
				loadTot[index] = load1+load2+load3;
				System.out.println(loadTot[index]);
				states[2+index] = i;
				index++;
			}
		}
		if (loadTot[1]>loadTot[0]) {
			int hold = states[2];
			states[2] = states[3];
			states[3] = hold;
		}
		
		return states;
		}catch(EntryMissingException e) {
			System.out.println("Classify failed");
			e.printStackTrace();
			return null;
		}
	}
}
