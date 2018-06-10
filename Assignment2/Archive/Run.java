package Project;

import java.util.ArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.StringWriter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.FileDialog;
//import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import java.util.StringJoiner;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.util.concurrent.*;
import javax.swing.filechooser.*;




public class Run {

	
	// Database entry and format
	private static String[] dbSetup = { "com.mysql.cj.jdbc.Driver", "jdbc:mysql://localhost:3306/", "root", "root",
			"SUBTABLES" };
	private static String[] tableFormat = { "CLAR_VOLT", "CLAR_ANG", "AMHE_VOLT", "AMHE_ANG", "WINL_VOLT", "WINL_ANG",
			"BOWM_VOLT", "BOWM_ANG", "TROY_VOLT", "TROY_ANG", "MAPL_VOLT", "MAPL_ANG", "GRAN_VOLT", "GRAN_ANG",
			"WAUT_VOLT", "WAUT_ANG", "CROSS_VOLT", "CROSS_ANG" };
	private static int [] busType = {0, 0, 0, 2, 1,2, 1,2,1};   	// Bus Types: 0 - Gen, 1 - Load, 2 - Branch 
	private static int [] [] lineData = {{1,4}, {2,8}, {3,6}, {4,5}, {4,9}, {5,6}, {6,7}, {7,8}, {8,9}};
	private static String [] stateNames = { "Gen Disconnected", "Line Disconnected", "Peak Load", "Low Load"};
	
	// Define global variables for accessing in gui
	private static ArrayList <ArrayList<ArrayList<double []>>> allClusters = new ArrayList <ArrayList<ArrayList<double []>>> (); // List of all k clusters
	private static ArrayList <Double> allClustCosts = new ArrayList <Double> (); // All k cluster costs
	private static ArrayList<ArrayList<double []>> clusters = new ArrayList<ArrayList<double[]>> (); // Selected cluster when picked
	private final static int startKCluster = 3; 
	private final static int endKCluster =10;
	private static Identification data;
	private static Kmeans clusterer;
	private static int [] statesClust;
	
	
	public static void main(String[] args) {

		
		String process= "kmeans";
		data = new Identification(dbSetup, tableFormat, process); // Define a new data importer object
		clusterer = new Kmeans(data, tableFormat); // Create new clusterer object
		
		clusterData();		
		// Select best cluster
		findOptKCluster();
		System.out.println("Optimal number of clusters: " + Integer.toString(clusters.size()));
		// Print Centroids				
		System.out.print(printCentroids());
		// Print cluster classification
		System.out.println(printClassify());
		// Print to output file
		String pathDir = "C:\\Users\\Media\\Documents\\outputFile.csv";
		outputFileBuild(clusters,pathDir);
		
		int k=0;
		kNeighbours findk = new kNeighbours(clusters,k);
		k=findk.optimumk();
		System.out.println("Optimum number of k:" +k);
		process= "kNN";
		Identification dataTesting = new Identification(dbSetup, tableFormat,process); // Define a new data importer object
		kNN classification = new kNN(dataTesting, tableFormat, k , clusters, statesClust);
		
		
		System.out.println("After kNN classification:");
		for (int x = 0; x < clusters.size(); x++) {
			
			System.out.println("Cluster " + x + " - Size " + clusters.get(x).size());
			
		}
	
		
		
	}
	
	/*
	 * Method - Build csv file of clusters for interrogation
	 * 
	 * Description - uses the folder directory chosen from GUI. Writes the
	 * values from topology processing into format needed by matpower. Saves
	 * as file "casefile". Will overwrite file if in directory. 
	 * 
	 */
	public static void outputFileBuild(ArrayList<ArrayList<double []>> clusters, String path) {
		
		 try {
            File outFile = new File(path); // Choose save directory
            FileOutputStream fs = new FileOutputStream(outFile);
            OutputStreamWriter osw = new OutputStreamWriter(fs);    
            Writer w = new BufferedWriter(osw);
           
            w.write(",");
            for(int i = 0; i< tableFormat.length; i++) {
            	w.write(tableFormat[i]+",");
            }
            w.write("\r\n");
            
            // Cluster data
            
            // for each cluster
			for (int x = 0; x < clusters.size(); x++) {
				// for each value in the cluster
				for(int j = 0; j<clusters.get(x).size(); j++) {
					w.write("Cluster "+Integer.toString(x+1));
					for (int k = 0; k<tableFormat.length; k++) {
						w.write("," + clusters.get(x).get(j)[k]);
					}
					w.write("\r\n");
				}
			}
            
			w.close();
			
        } catch (IOException e) {
        	e.printStackTrace();
        	System.err.println("Problem writing the file");
        }
		
	}
	
	/*
	 * Print centroids to a string
	 * 
	 */
	public static String printCentroids () { 
		String printCentroids = "";
		ArrayList <double []> centroids = clusterer.calCentroid(clusters);
		for (int i =0; i < centroids.size(); i++) {
			printCentroids = printCentroids + Arrays.toString(centroids.get(i)) + "\r\n"; 
		}
		return printCentroids;
	}	

	/*
	 * Find states and print to string
	 */
	public static String printClassify() {
		statesClust = clusterer.clustClassify(clusters, lineData, busType);
		String statePrint = "";
		for (int i = 0; i < statesClust.length; i++) {
			statePrint = statePrint + "Cluster " + i + " is state: " + stateNames[statesClust[i]] + "\r\n";
		}
		return statePrint;
	}
	
	/*
	 * Select optimal cluster number
	 */
	public static void findOptKCluster() {
		int indElbowK = 0;
		double prevCost = 10000;
		try {
			while (allClustCosts.get(indElbowK)<prevCost*0.5){ 
				prevCost = allClustCosts.get(indElbowK);
				indElbowK ++; 
			}
		} catch (IndexOutOfBoundsException elbowFindError) {
			// Handle error for a missing db
			elbowFindError.printStackTrace();
			System.out.println("Couldn't find elbow");
		}
		indElbowK --;
		clusters = allClusters.get(indElbowK);
	}
	
	/*
	 * Cluster the data using the clusterer
	 * 
	 */
	public static void clusterData() {

		// Generate random number for Kmeans
		int rand = ThreadLocalRandom.current().nextInt(0, data.getDataTable().size() + 1);

		// Define a new cluster
		ArrayList<ArrayList<double[]>> newClusters;

		// For different cluster sizes
		for (int i = startKCluster; i <= endKCluster; i++) {
			double clustCost = 100000; // intialise the cost which will be improved on
			// For each cluster size initialise with 10 different random variables
			for (int j = 0; j < 20; j++) {
				rand = ThreadLocalRandom.current().nextInt(0, data.getDataTable().size() + 1);
				newClusters = clusterer.clusterData(i, rand);
				double newClustCost = clusterer.calCost(newClusters);

				if (clustCost > newClustCost) {
					clusters = newClusters;
					clustCost = newClustCost;
				}
				System.out.println("Cluster size " + i + " - Cost " + clustCost);
			}
			// save the best cluster of each size and the cluster cost
			allClusters.add(clusters);
			allClustCosts.add(clustCost);
		}
	}

	
}
