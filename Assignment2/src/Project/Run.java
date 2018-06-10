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


import java.awt.Color;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import java.util.StringJoiner;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.util.concurrent.*;
import javax.swing.filechooser.*;
import org.eclipse.swt.widgets.Composite;
import javax.swing.JFrame;

import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class Run {

	// Database entry and format
	private static String[] dbSetup = { "com.mysql.cj.jdbc.Driver", "jdbc:mysql://localhost:3306/", "root", "root",
			"SUBTABLES" };
	private static String[] tableFormat = { "CLAR_VOLT", "CLAR_ANG", "AMHE_VOLT", "AMHE_ANG", "WINL_VOLT", "WINL_ANG",
			"BOWM_VOLT", "BOWM_ANG", "TROY_VOLT", "TROY_ANG", "MAPL_VOLT", "MAPL_ANG", "GRAN_VOLT", "GRAN_ANG",
			"WAUT_VOLT", "WAUT_ANG", "CROSS_VOLT", "CROSS_ANG" };
	private static int[] busType = { 0, 0, 0, 2, 1, 2, 1, 2, 1 }; // Bus Types: 0 - Gen, 1 - Load, 2 - Branch
	private static int[][] lineData = { { 1, 4 }, { 2, 8 }, { 3, 6 }, { 4, 5 }, { 4, 9 }, { 5, 6 }, { 6, 7 }, { 7, 8 },
			{ 8, 9 } };
	private static String[] stateNames = { "Gen Disconnected", "Line Disconnected", "Peak Load", "Low Load" };

	// Define kMeans class variables for accessing in gui
	private static ArrayList<ArrayList<ArrayList<double[]>>> allClusters = new ArrayList<ArrayList<ArrayList<double[]>>>(); // List of all clusters
	private static ArrayList<Double> allClustCosts = new ArrayList<Double>(); // All k cluster costs
	private static ArrayList<ArrayList<double[]>> clusters = new ArrayList<ArrayList<double[]>>(); // Selected cluster when picked
	public final static int startKCluster = 3;
	public final static int endKCluster = 10;
	private static Identification data;
	private static Kmeans clusterer;
	private static int[] statesClust;
	private static String process;
	private static String pathDir = "C:\\Users\\Media\\Documents\\outputFile.csv";
	
	// Define kNN class variables for accessing in GUI
	private static Identification dataTesting;
	private static int optimalKNN;
	
	protected Shell shlEhui;
	private Text userKbox;

	public static void main(String[] args) {

		// Launch the application
		try {
			Run window = new Run();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/*
	 * Method - set up test data from GUI
	 * 
	 */
	public static void setUpTest() {
		process = "kNN";
		dataTesting = new Identification(dbSetup, tableFormat, process); // Define a new data importer object
	}
	
	
	/*
	 * Method - find the optimal k
	 */
	public static void findOptimalK() {
		KNNfinder findk = new KNNfinder(clusters);
		optimalKNN = findk.optimumk();
		System.out.println("Optimum number of k:" + optimalKNN);
	}
	
	/*
	 * Method - run K Neighbours
	 * 
	 */
	public static void runKNN() {	
		
		kNN classification = new kNN(dataTesting, tableFormat, optimalKNN, clusters, statesClust);

		System.out.println("After kNN classification:");
		for (int x = 0; x < clusters.size(); x++) {

			System.out.println("Cluster " + x + " - Size " + clusters.get(x).size());

		}
	}
	
	
	/*
	 * Method - set up kMeans data from gui
	 * 
	 */
	public static void setUpData() {
		process = "kmeans";
		data = new Identification(dbSetup, tableFormat, process); // Define a new data importer object
		clusterer = new Kmeans(data, tableFormat); // Create new clusterer object
	}
	
	/*
	 * Method - Build csv file of clusters for interrogation
	 * 
	 * Description - uses the folder directory chosen from GUI. Writes the values
	 * from topology processing into format needed by matpower. Saves as file
	 * "casefile". Will overwrite file if in directory.
	 * 
	 */
	public static boolean outputFileBuild(ArrayList<ArrayList<double[]>> clusters, String path) {

		try {
			File outFile = new File(path+"\\kMeansDataExport.csv"); // Choose save directory
			FileOutputStream fs = new FileOutputStream(outFile);
			OutputStreamWriter osw = new OutputStreamWriter(fs);
			Writer w = new BufferedWriter(osw);

			w.write(",");
			for (int i = 0; i < tableFormat.length; i++) {
				w.write(tableFormat[i] + ",");
			}
			w.write("\r\n");

			// Cluster data

			// for each cluster
			for (int x = 0; x < clusters.size(); x++) {
				// for each value in the cluster
				for (int j = 0; j < clusters.get(x).size(); j++) {
					w.write("Cluster " + Integer.toString(x + 1));
					for (int k = 0; k < tableFormat.length; k++) {
						w.write("," + clusters.get(x).get(j)[k]);
					}
					w.write("\r\n");
				}
			}

			w.close();
			return true;

		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Problem writing the file");
			return false;
		}

	}

	/*
	 * Print centroids to a string
	 * 
	 */
	public static String printCentroids() {
		String printCentroids = "";
		ArrayList<double[]> centroids = clusterer.calCentroid(clusters);
		for (int i = 0; i < centroids.size(); i++) {
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
			while (allClustCosts.get(indElbowK) < prevCost * 0.5) {
				prevCost = allClustCosts.get(indElbowK);
				indElbowK++;
			}
		} catch (IndexOutOfBoundsException elbowFindError) {
			// Handle error for a missing db
			elbowFindError.printStackTrace();
			System.out.println("Couldn't find elbow");
		}
		indElbowK--;
		clusters = allClusters.get(indElbowK);
	}

	/*
	 * Cluster the data using the clusterer
	 * 
	 */
	public static void clusterData() {

		
		allClusters.clear();
		allClustCosts.clear();
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
		
			}
			// save the best cluster of each size and the cluster cost
			allClusters.add(clusters);
			allClustCosts.add(clustCost);
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlEhui.open();
		shlEhui.layout();
		while (!shlEhui.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlEhui = new Shell();
		shlEhui.setSize(417, 418);
		shlEhui.setText("EH2745-UI");

		Composite composite = new Composite(shlEhui, SWT.NONE);
		composite.setBounds(0, 10, 401, 359);

		Button importData = new Button(composite, SWT.NONE);
		importData.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));

		importData.setBounds(16, 71, 120, 25);
		importData.setText("Import Training Data");

		Label importSuccessBox = new Label(composite, SWT.BORDER | SWT.WRAP);
		importSuccessBox.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		importSuccessBox.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
		importSuccessBox.setBounds(142, 72, 120, 23);

		Button findClusters = new Button(composite, SWT.NONE);
		findClusters.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		findClusters.setBounds(16, 103, 120, 25);
		findClusters.setText("Find Clusters");
		findClusters.setEnabled(false);

		Button exportLocation = new Button(composite, SWT.NONE);
		exportLocation.setText("Export Location");
		exportLocation.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		exportLocation.setEnabled(false);
		exportLocation.setBounds(16, 165, 120, 25);

		Label clusterSuccessBox = new Label(composite, SWT.BORDER | SWT.WRAP);
		clusterSuccessBox.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		clusterSuccessBox.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
		clusterSuccessBox.setBounds(142, 104, 120, 23);

		Label header = new Label(composite, SWT.NONE);
		header.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.BOLD));
		header.setBounds(111, 12, 164, 25);
		header.setText("EH2745 - Assignment 2");

		Label exportSuccessBox = new Label(composite, SWT.BORDER | SWT.WRAP);
		exportSuccessBox.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		exportSuccessBox.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
		exportSuccessBox.setBounds(141, 167, 120, 23);

		Button viewClusters = new Button(composite, SWT.NONE);
		viewClusters.setText("View Clusters");
		viewClusters.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		viewClusters.setEnabled(false);
		viewClusters.setBounds(16, 134, 120, 25);
		
		Button viewOptimalK = new Button(composite, SWT.NONE);
		viewOptimalK.setText("View K Number");
		viewOptimalK.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		viewOptimalK.setEnabled(false);
		viewOptimalK.setBounds(141, 135, 120, 25);

		Button exportToCSV = new Button(composite, SWT.NONE);
		exportToCSV.setText("Export to CSV");
		exportToCSV.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		exportToCSV.setEnabled(false);
		exportToCSV.setBounds(269, 167, 120, 25);

		Label label = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setBounds(16, 196, 373, 13);

		Label lblMatpowerFunctions = new Label(composite, SWT.NONE);
		lblMatpowerFunctions.setText("k Means");
		lblMatpowerFunctions.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.BOLD));
		lblMatpowerFunctions.setBounds(18, 52, 222, 13);
		
		Button viewClustData = new Button(composite, SWT.NONE);
		viewClustData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		viewClustData.setText("View Cluster Data");
		viewClustData.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		viewClustData.setEnabled(false);
		viewClustData.setBounds(269, 135, 120, 25);
		
		Label label_1 = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label_1.setBounds(16, 41, 373, 13);
		
		Label lblKNearestNeighbour = new Label(composite, SWT.NONE);
		lblKNearestNeighbour.setText("k Nearest Neighbour");
		lblKNearestNeighbour.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.BOLD));
		lblKNearestNeighbour.setBounds(17, 208, 222, 13);
		
		Button importTestD = new Button(composite, SWT.NONE);
		importTestD.setText("Import Test Data");
		importTestD.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		importTestD.setEnabled(false);
		importTestD.setBounds(16, 227, 120, 25);
		
		Label importTestDLabel = new Label(composite, SWT.BORDER | SWT.WRAP);
		importTestDLabel.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		importTestDLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
		importTestDLabel.setBounds(141, 228, 120, 23);
		
		Button runKNN = new Button(composite, SWT.NONE);
		runKNN.setText("Sort Test Data");
		runKNN.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		runKNN.setEnabled(false);
		runKNN.setBounds(16, 289, 120, 25);
		
		Label runKNNLabel = new Label(composite, SWT.BORDER | SWT.WRAP);
		runKNNLabel.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		runKNNLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
		runKNNLabel.setBounds(142, 289, 120, 23);
		
		Button optK = new Button(composite, SWT.NONE);
		optK.setText("Find Optimal K");
		optK.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		optK.setEnabled(false);
		optK.setBounds(16, 258, 120, 25);
		
		userKbox = new Text(composite, SWT.BORDER);
		userKbox.setBounds(142, 260, 120, 23);
		
		Button userK = new Button(composite, SWT.NONE);
		userK.setText("Enter Own K");
		userK.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		userK.setEnabled(false);
		userK.setBounds(269, 258, 120, 25);

		importData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				setUpData();
				importSuccessBox.setText("Success!");
				findClusters.setEnabled(true);
				exportLocation.setEnabled(false);
				viewClusters.setEnabled(false);
				viewOptimalK.setEnabled(false);
				clusterSuccessBox.setText("");
				exportSuccessBox.setText("");
				exportToCSV.setEnabled(false);
				viewClustData.setEnabled(false);
				importTestD.setEnabled(false);
				importTestDLabel.setText("");
				userK.setEnabled(false);
				optK.setEnabled(false);
				
				
			
			}

		});

		findClusters.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				clusterData();
				// Select best cluster
				findOptKCluster();
				System.out.println("Optimal number of clusters: " + Integer.toString(clusters.size()));
				// Print Centroids
				System.out.print(printCentroids());
				// Print cluster classification
				System.out.println(printClassify());
				
				clusterSuccessBox.setText(Integer.toString(clusters.size()) + " Clusters!");
				exportLocation.setEnabled(true);
				viewClusters.setEnabled(true);
				viewOptimalK.setEnabled(true);
				viewClustData.setEnabled(true);
				importTestD.setEnabled(true);
				importTestDLabel.setText("");

			}
		});
	
		
		exportLocation.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				try {
					JFileChooser chooser = new JFileChooser();
					chooser.setDialogTitle("Choose Export Directory");
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					chooser.setAcceptAllFileFilterUsed(false);
					if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
						pathDir = chooser.getSelectedFile().getCanonicalPath();
					}
					
					if (pathDir != null) {
						exportSuccessBox.setText(pathDir);
						exportToCSV.setEnabled(true);
					}
				} catch (IOException exportPath) {
					JOptionPane.showMessageDialog(null, "Error occurred - please retry");
					exportPath.printStackTrace();
				}

			}

		});


		exportToCSV.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				// Print to output file
				if(outputFileBuild(clusters, pathDir)) {
					exportSuccessBox.setText("Success!");
				} else {
					exportSuccessBox.setText("Failed :(");
				}
				

			}
		});

	
		viewClusters.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			      ScatterPlot clusterPlot = new ScatterPlot("Clusters", clusters);
			      clusterPlot.setSize(800, 400);
			      clusterPlot.setLocationRelativeTo(null);
			      clusterPlot.setVisible(true);
				
			}
		});
		
		viewOptimalK.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			      ScatterPlot clusterPlot = new ScatterPlot("Clusters", clusters, allClustCosts);
			      clusterPlot.setSize(800, 400);
			      clusterPlot.setLocationRelativeTo(null);
			      clusterPlot.setVisible(true);
				
			}
		});
		
		viewClustData.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				// Format the data into a string for viewing
				// Present the cluster sizes and states
				String output =""; 
				
				for (int i =0; i<clusters.size(); i++) {
					output = output+ "Cluster " + i + " has " + clusters.get(i).size() + " entries and corresponds to state " + stateNames[statesClust[i]] + "\r\n";
				}

				// Create window
				try {
					DataViewer ybusViewer = new DataViewer();
					ybusViewer.open(output);
				} catch (Exception f) {
					f.printStackTrace();
				}

			}
		});
	
		importTestD.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setUpTest();
				importTestDLabel.setText("Success!");
				userK.setEnabled(true);
				optK.setEnabled(true);
			}
		});
		
		runKNN.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				runKNN();
				runKNNLabel.setText("Success!");
			}
		});
	
		
		optK.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				findOptimalK();
				userKbox.setText(Integer.toString(optimalKNN));
				runKNN.setEnabled(true);
			}
		});
	
		userK.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String userK = userKbox.getText();
				if(userK.matches("-?\\d+")) {
					if(Integer.parseInt(userK) <= 100 && Integer.parseInt(userK) > 0) {
						optimalKNN = Integer.parseInt(userK);
						runKNN.setEnabled(true);
						userKbox.setText(userK+" - User value read!");
					}
					else {
						JOptionPane.showMessageDialog(null, "Error Occurred - K must be between 1-100");
					}
				}
				else {
					JOptionPane.showMessageDialog(null, "Error Occurred - please enter a valid integer");
				}
			}
		});
		
	}
	
}
