package Project;

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

	static String[] dbSetup = { "com.mysql.cj.jdbc.Driver", "jdbc:mysql://localhost:3306/", "root", "root",
			"SUBTABLES" };
	static String[] tableFormat = { "CLAR_VOLT", "CLAR_ANG", "AMHE_VOLT", "AMHE_ANG", "WINL_VOLT", "WINL_ANG",
			"BOWM_VOLT", "BOWM_ANG", "TROY_VOLT", "TROY_ANG", "MAPL_VOLT", "MAPL_ANG", "GRAN_VOLT", "GRAN_ANG",
			"WAUT_VOLT", "WAUT_ANG", "CROSS_VOLT", "CROSS_ANG" };
	// Bus Types: 0 - Gen, 1 - Load, 2 - Branch 
	static int [] busType = {0, 0, 0, 2, 1,2, 1,2,1};   
	static int [] [] lineData = {{1,4}, {2,8}, {3,6}, {4,5}, {4,9}, {5,6}, {6,7}, {7,8}, {8,9}};

	
	
	public static void main(String[] args) {

		// Potentially could initialise table format by searching for distinct data entry points - assuming voltage angle and magnitude for 9 buses 
		
		
		Identification data = new Identification(dbSetup, tableFormat); // Define a new data importer object
		Kmeans clusterer = new Kmeans(data, tableFormat); // Create new clusterer object
		
		// Generate random number for Kmeans
		int rand = ThreadLocalRandom.current().nextInt(0, data.getDataTable().size() + 1);
		// Find an initial cluster
		ArrayList<ArrayList<double []>> clusters = clusterer.clusterData(2, rand);
		// Find cost of that cluster
		double clustCost = clusterer.calCost(clusters);
		
		// Define a new cluster
		ArrayList<ArrayList<double []>> newClusters;
		
		for(int i = 4; i < 5; i++) {	
			// complete with 10 different random variables
			for (int j = 0; j < 10; j++) {
				rand = ThreadLocalRandom.current().nextInt(0, data.getDataTable().size() + 1);
				newClusters = clusterer.clusterData(i, rand);
				double newClustCost = clusterer.calCost(newClusters);

				if (clustCost > newClustCost) {
					clusters = newClusters;
					clustCost = newClustCost;
				}
				// print each cluster
				for (int x = 0; x < clusters.size(); x++) {
					// for each value in the cluster
					System.out.println("Cluster " + x + " - Size " + clusters.get(x).size());
					// for(int j = 0; j<clusters.get(x).size(); j++) {
					// System.out.println("Cluster " + x + " - Element " + j + ": " +
					// Arrays.toString(clusters.get(x).get(j)));
					// }
				}
				System.out.println("Cluster size " + i + " - Cost " + clustCost);
			}
			
		}
		int [] states = clusterer.clustClassify(clusters, lineData, busType);
		
		System.out.println("Gen Maint = Cluster " + states[0] + "\r\nLine down = Cluster " + states[1] + "\r\nHigh load = Cluster " + states[2] + "\r\nLow Load = Cluster " + states[3] );
		
		outputFileBuild(clusters);
	}
	
	/*
	 * Method - Build csv file of clusters for interrogation
	 * 
	 * Description - uses the folder directory chosen from GUI. Writes the
	 * values from topology processing into format needed by matpower. Saves
	 * as file "casefile". Will overwrite file if in directory. 
	 * 
	 */
	public static void outputFileBuild(ArrayList<ArrayList<double []>> clusters) {
		
        try {
            File outFile = new File("C:\\Users\\Callum\\eclipse-workspace\\EH2745\\Assignments\\Assignment2\\clusterOut.csv"); // Choose save directory
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
        	System.err.println("Problem writing the file");
        }
		
	}
}
