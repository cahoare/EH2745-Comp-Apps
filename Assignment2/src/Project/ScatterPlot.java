package Project;

import java.util.ArrayList;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.axis.NumberAxis;

//Leveraged example code available at https://www.boraji.com/jfreechart-scatter-chart-example

public class ScatterPlot extends JFrame {
	private static final long serialVersionUID = 14050321;

	public ScatterPlot(String title, ArrayList<ArrayList<double[]>> clusters) {
		super(title);

		XYSeriesCollection dataSet = new XYSeriesCollection();

		// Plot needs to make the 18 datapoints appear in 2d
		// Simple way is to find average of voltage and voltage angles.
		// Wouldnt neccessarily work but tested and found to give good depiction
		for (int i = 0; i < clusters.size(); i++) { // each cluster is a dataset
			XYSeries dataSeries = new XYSeries("Cluster " + i);

			for (int j = 0; j < clusters.get(i).size(); j++) { // add each element in the cluster
				double xHold = 0;
				// Make X the voltage magnitude averages
				for (int x = 0; x < clusters.get(i).get(j).length; x = x + 2) {
					xHold += clusters.get(i).get(j)[x];
				}

				double yHold = 0;
				// Make X the voltage magnitude averages
				for (int y = 1; y < clusters.get(i).get(j).length; y = y + 2) {
					yHold += clusters.get(i).get(j)[y];
				}
				xHold = xHold / (clusters.get(i).get(j).length / 2);
				yHold = yHold / (clusters.get(i).get(j).length / 2);
				dataSeries.add(xHold, yHold);
			}
			dataSet.addSeries(dataSeries);
		}

		// Plot the chart
		JFreeChart chart = ChartFactory.createScatterPlot("", "Avg voltage magnitude of all buses",
				"Avg voltage angles of all buses", dataSet);

		// Create Panel
		ChartPanel panel = new ChartPanel(chart);
		setContentPane(panel);
	}

	public ScatterPlot(String title, ArrayList<ArrayList<double[]>> clusters, ArrayList<Double> allClustCosts) {
		super(title);

		XYSeriesCollection dataSet = new XYSeriesCollection();
		XYSeries series = new XYSeries("Costs");
		XYSeries point = new XYSeries ("Selected K");

		
		// Line plot to show the decrease in cost
		for (int i = 0; i < allClustCosts.size(); i++) { // each cluster is a dataset
			series.add(i+Run.startKCluster, allClustCosts.get(i));
		}

		point.add(Run.startKCluster,  allClustCosts.get(clusters.size()-Run.startKCluster));
		point.add(clusters.size(), allClustCosts.get(clusters.size()-Run.startKCluster));
		point.add(clusters.size(), 0);
		
		dataSet.addSeries(series);
		dataSet.addSeries(point);
		
		// Plot the chart
		JFreeChart chart = ChartFactory.createXYLineChart("", "Number of clusters",
				"Cost function of each cluster", dataSet);
		XYPlot plot = (XYPlot) chart.getPlot();
		XYLineAndShapeRenderer renderer =
			    (XYLineAndShapeRenderer) plot.getRenderer();
		renderer.setBaseShapesVisible(true);
		NumberAxis domain = (NumberAxis) plot.getDomainAxis();
		domain.setRange(Run.startKCluster, allClustCosts.size()+Run.startKCluster-1);

		// Create Panel
		ChartPanel panel = new ChartPanel(chart);
		setContentPane(panel);

	}

}