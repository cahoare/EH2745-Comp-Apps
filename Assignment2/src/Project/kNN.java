package Project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class kNN {

	private ArrayList<Integer> times; // timestamps
	private ArrayList<double[]> values; // actual data
	private final double error = 0.01;
	private ArrayList<ArrayList<double[]>> clustersvalues = new ArrayList<ArrayList<double[]>>();
	// TABLE information
	private String[] tableFormat;
	private int k;

	public kNN(Identification dataTesting, String[] tableFormat, int k, ArrayList<ArrayList<double[]>> clustersvalues,
			int[] states) {
		values = dataTesting.getDataTable();
		times = dataTesting.getTimeStamps();
		this.tableFormat = tableFormat;
		this.clustersvalues = clustersvalues;
		this.k = k;
		classification();
	}

	public void classification() {
		for (int b = 0; b < values.size(); b++) {

			int[] firstkn = new int[clustersvalues.size()]; // array of number of clusters size
			ArrayList<double[]> sorteddistances = new ArrayList<double[]>();
			sorteddistances = distancecalculation(values.get(b));
			for (int u = 0; u < k; u++) {
				firstkn[(int) sorteddistances.get(u)[1]]++;
			}
			clustersvalues.get(getIndexOfLargest(firstkn)).add(values.get(b));
		}

	}

	public ArrayList<double[]> distancecalculation(double[] trainingsample) {

		ArrayList<double[]> distancelist = new ArrayList<double[]>();
		ArrayList<double[]> distancelistsorted = new ArrayList<double[]>();
		ArrayList<Double> distances = new ArrayList<Double>();
		ArrayList<Double> cluster = new ArrayList<Double>();

		for (int o = 0; o < clustersvalues.size(); o++) {
			for (int p = 0; p < clustersvalues.get(o).size(); p++) {
				double[] distance = new double[2];
				distance[0] = 0;
				for (int a = 0; a < clustersvalues.get(o).get(p).length; a++) {
					distance[0] += Math.pow((trainingsample[a] - clustersvalues.get(o).get(p)[a]), 2);
				}
				distance[0] = Math.sqrt(distance[0]);
				distance[1] = (double) (o); // to indicate that distance is to a esceario in cluster o
				distancelist.add(distance);
			}
		}
		for (int a = 0; a < distancelist.size(); a++) {
			distances.add(distancelist.get(a)[0]);
			cluster.add(distancelist.get(a)[1]);
		}
		Collections.sort(distances); // to sort the distances

		for (int a = 0; a < distances.size(); a++) {
			for (int b = 0; b < distancelist.size(); b++) {
				if (distances.get(a).equals(distancelist.get(b)[0])) {
					double[] aux = new double[2];
					aux[0] = distances.get(a);
					aux[1] = distancelist.get(b)[1];
					distancelistsorted.add(aux);
				} else {
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
