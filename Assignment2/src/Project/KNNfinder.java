package Project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

public class KNNfinder {

	private ArrayList<ArrayList<double[]>> trainingvalues = new ArrayList<ArrayList<double[]>>();
	private ArrayList<ArrayList<double[]>> clustersvalues = new ArrayList<ArrayList<double[]>>();
	private ArrayList<ArrayList<double[]>> clustersvaluescloned = new ArrayList<ArrayList<double[]>>();
	private ArrayList<ArrayList<double[]>> distancesmatrix = new ArrayList<ArrayList<double[]>>();
	private int ntsamples = 5; // Number of samples per cluster taken as training samples for calculating the
										// optimum number of k neighbours

	
	public KNNfinder(ArrayList<ArrayList<double[]>> clustersvalues) {

		int x = 0;
		int i;

		 // Clone the clustered data arraylist
		while (x < clustersvalues.size()) {
			i = 0;
			ArrayList<double[]> auxiliar = new ArrayList<double[]>(); 
			while (i < clustersvalues.get(x).size()) { // We take as training set the nt first samples of each cluster -
														// later we will compare the results
				auxiliar.add(clustersvalues.get(x).get(i).clone());
				i++;
			}
			clustersvaluescloned.add(auxiliar);
			x++;
		}
		
		// Separate the data into a test set for finding ideal k
		x = 0;
		Random rand = new Random();
		while (x < clustersvalues.size()) {  // 
			i = 0;
			ArrayList<double[]> auxiliar = new ArrayList<double[]>();
			while (i < ntsamples) { // We take as training set the nt first samples of each cluster - later we will
									// compare the results

				int randomIndex = rand.nextInt(clustersvaluescloned.get(x).size());
				auxiliar.add(clustersvaluescloned.get(x).get(randomIndex).clone());
				clustersvaluescloned.get(x).remove(randomIndex);
				i++;
			}
			trainingvalues.add(auxiliar);
			x++;
		}
	}

	/*
	 * Method - Find optimum k for kNN
	 * Description - Uses the training data to find the best k value
	 * Returns - Best value for k 
	 */
	public int optimumk() {
		int optk = 0;
		int bestmatching = 0;
		int maxk = 15; // the number of neighbours tried is maxk-1

		// for the number of k to trial
		int[] bestk = new int[maxk];
		for (int k = 0; k < maxk; k++) {
			// for each sub "unclustered" cluster in the training set
			for (int o = 0; o < trainingvalues.size(); o++) { 
				// for each value in the sub cluster
				for (int b = 0; b < trainingvalues.get(o).size(); b++) {
					
					// Number of clusters
					int[] firstkn = new int[trainingvalues.size()];
					ArrayList<double[]> sorteddistances = new ArrayList<double[]>();
					sorteddistances = distancecalculation(trainingvalues.get(o).get(b));
			
					// Check which cluster this would be assigned to on the basis of k neighbours
					for (int u = 0; u <= k; u++) { //
						firstkn[(int) sorteddistances.get(u)[1]]++; // how many times we find a certain cluster
					}
					// if it is correct then then increment the number of correct guesses for that sample
					// if the cluster that appears more times is the same as the real cluster of the
					// sample give by "o"
					// then the process was successful and therefore this k is good
					if (getIndexOfLargest(firstkn) == o) { // which is the cluster that appears more times in the first k neighbours
						bestk[k]++;
					} 
				}
			}
		}

		// the best k is the one that was more successful
		bestmatching = bestk[0];
		for (int i = 0; i < bestk.length; i++) {

			if (bestk[i] >= bestmatching) {
				bestmatching = bestk[i];
				optk = i;
			}
		}
		return optk + 1;
	}

	/*
	 * Calculate the distance from the sample to each other value in the clusters
	 * Param - Is the sampled training data
	 * Returns - Sorted distance array with values
	 */
	
	public ArrayList<double[]> distancecalculation(double[] trainingsample) {

		ArrayList<double[]> distancelist = new ArrayList<double[]>();
		ArrayList<double[]> distancelistsorted = new ArrayList<double[]>();
		ArrayList<Double> distanceSort = new ArrayList<Double>();

		for (int o = 0; o < clustersvaluescloned.size(); o++) {
			for (int p = 0; p < clustersvaluescloned.get(o).size(); p++) {
				double[] distance = new double[2];
				distance[0] = 0;
				for (int a = 0; a < clustersvaluescloned.get(o).get(p).length; a++) {
					distance[0] += Math.pow((trainingsample[a] - clustersvaluescloned.get(o).get(p)[a]), 2);
				}
				distance[0] = Math.sqrt(distance[0]);
				distance[1] = (double) (o); // to indicate that distance is to a esceario in cluster o
				distancelist.add(distance);
			}
		}
		
		// To allow the base sort method to be used copy to another arrayList and sort
		for (int a = 0; a < distancelist.size(); a++) {
			distanceSort.add(distancelist.get(a)[0]);
		}
		Collections.sort(distanceSort); 

		// Match the sorted array to the unsorted distance to find the cluster number
		for (int a = 0; a < distanceSort.size(); a++) {
			for (int b = 0; b < distancelist.size(); b++) {
				if (distanceSort.get(a).equals(distancelist.get(b)[0])) {
					double[] aux = new double[2];
					aux[0] = distanceSort.get(a);
					aux[1] = distancelist.get(b)[1];
					distancelistsorted.add(aux);
				}
			}
		}
		return distancelistsorted;
	}

	public int getIndexOfLargest(int[] array) {
		if (array == null || array.length == 0)
			return -1; // null or empty

		int largest = 0;
		for (int i = 1; i < array.length; i++) {
			if (array[i] > array[largest])
				largest = i;
		}
		return largest; // position of the first largest found
	}

}
