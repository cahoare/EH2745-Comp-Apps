package Project;


import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

public class Identification {
	
	// JDBC driver name and database URL - passed from Run
	private static String JDBC_DRIVER;
	private static String DB_URL;
	private static String DB_NAME;
	
	// Database credentials - passed from Run
	private static String USER;
	private static String PASS;
		
	// ESCENARIOS INFORMATION
	/* private ArrayList<Escenario> EscenariosList = new ArrayList<Escenario>();*/
	
	public Identification(String[] dbSetup) {
		
		

		JDBC_DRIVER = dbSetup[0];
		DB_URL = dbSetup[1];
		USER = dbSetup[2];
		PASS = dbSetup[3];
		DB_NAME = dbSetup[4];
		
		
	}
	
	public void Shape (ArrayList<Escenario> EscenariosList) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		
		try {
			// Register JDBC driver
			Class.forName(JDBC_DRIVER);

			// Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL+DB_NAME+"?user="+USER+"&password="+PASS+"&autoReconnect=true&useSSL=false");

			stmt = conn.createStatement();

			String sql = "USE SUBTABLES";
			stmt.executeUpdate(sql);
			
			stmt = conn.createStatement();
			sql = "Select * From analog_values";
	        ResultSet escenarios = stmt.executeQuery(sql);
	        String x1,x2,x5;
	        double x3,x4;
	       
	      
	        int g =0;
	        while (escenarios.next()) {
	        	x1=escenarios.getString("name");
	        	x2=escenarios.getString("time");
	        	x3=escenarios.getDouble("value");
	        	x4=escenarios.getDouble("value");
	        	x5=escenarios.getString("sub_rdfid");
				Escenario escenario =  new Escenario(x1,x2,x3,x4,x5);
				EscenariosList.add(escenario);
	        }
	        double angle;
	        g=1;
	       System.out.println(EscenariosList.size());
	       int size = EscenariosList.size();
	        while(g<=size){
	        	if(g%2!=0){	        	
	        		angle = EscenariosList.get(size-g).getAngle();
	        		EscenariosList.get(size-g-1).replaceAngle(angle);   
	        		EscenariosList.remove(size-g);}
	        	else{} 
	        	g++;}

	        System.out.println("Something in between");
	        System.out.println(EscenariosList.size());
	        
	        g=0;
	        /* while(g<EscenariosList.size()){
	        	
	        	System.out.println(EscenariosList.get(g).getName()+" "+EscenariosList.get(g).getVoltage()+" "+EscenariosList.get(g).getAngle()+" "+EscenariosList.get(g).getTimeestamp());
	        g++;
	        }
	      
	     	*/	
	       
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
			
			
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();}
			
	
	}

	}
