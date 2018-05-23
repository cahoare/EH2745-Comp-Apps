package Project;

import java.util.ArrayList;


public class Run {
	
	static String [] dbSetup = { "com.mysql.cj.jdbc.Driver","jdbc:mysql://localhost:3306/", "root", "root", "SUBTABLES"};
	
	private static ArrayList<Escenario> EscenariosData = new ArrayList<Escenario>();
	
	public static void main(String[] args) {	
		
		
		Identification Data = new Identification(dbSetup);
		Data.Shape(EscenariosData);
		
		Kmeans Clustering = new Kmeans(EscenariosData);
		Clustering.Clustering();

	}
}
