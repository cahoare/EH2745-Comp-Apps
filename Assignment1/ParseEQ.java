import java.io.File; // Related to system in and out
import java.util.*;

import javax.xml.parsers.DocumentBuilderFactory; // Related to HTML file doc parsing
import javax.xml.parsers.DocumentBuilder; 
import org.w3c.dom.Document; 
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;


import java.sql.*;

// DO WE NEED TO CLOSE CONNECTIONS TO THE MAIN DATABASE? 
// IS IT GOOD ARCHITECTURE FOR PARSE EQ TO DO BOTH??
// STATIC SETS AND GETS, WHICH WAY SHOULD THEY BE CONFIGURED


public class ParseEQ {

	private static File EQFile;
	private static ArrayList <NodeList> listEquip;
	private static ArrayList <EquipItem> allEquip;
	
	
	// Equipment types
	private static String [] equip; 
	// data types
	private static String [][] dataNames;
			
	// Hash table of date required for each type
	private static int [][] dataIndex;

	
	
	// JDBC driver name and database URL - passed from Run
	static String JDBC_DRIVER; 
	static String DB_URL; 
	static String DB_NAME;
	
	// Database credentials - passed from Run
	static String USER; 
	static String PASS;
	

	
	public ParseEQ (File EQFile, String [] equip, String [][] dataNames, int [][] dataIndex) {
		ParseEQ.EQFile = EQFile;
		ParseEQ.equip = equip;
		ParseEQ.dataNames = dataNames;
		ParseEQ.dataIndex = dataIndex;
		eqParse();	
	
	}

	private static void eqParse () {
		
		
		listEquip = new ArrayList<NodeList>(); 
		allEquip = new ArrayList <EquipItem>();
		
		try {  // catch dbFactory exception
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance(); // parses XML file
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(EQFile);  
			doc.getDocumentElement().normalize(); // on this checks that each element only has text where it should be. If something has been out onto multiple lines will amend into each element. 

			
			for (int i=0; i<equip.length; i++) { // Search for each equipment type sequentially and add the list to the array
				listEquip.add(doc.getElementsByTagName("cim:" + equip[i]));

				for (int c=0; c < listEquip.get(i).getLength(); c++) { // build an item for each type within the list. item type corresponds to equip array
					allEquip.add(new EquipItem(listEquip.get(i).item(c), i, equip, dataIndex)) ;
				}
					
			}
		
		
		}
		catch(Exception e){
			e.printStackTrace();
		}
				
	}

	public static boolean dbBuild(String [] dbSetup){

		
		JDBC_DRIVER = dbSetup[0];
		DB_URL = dbSetup[1];
		USER = dbSetup[2];
		PASS= dbSetup[3];
		DB_NAME = dbSetup[4];
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet resultSet = null;
		
		try{
			// Register JDBC driver
			Class.forName(JDBC_DRIVER);
			// Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL+"?user="+USER+"&password="+PASS+"&autoReconnect=true&useSSL=false");
			
			// Set-up the statement connection
			stmt = conn.createStatement();
			String sql; // Sql string used for statments 
			
			// Check if database exists and drop if it does
			System.out.println("Checking if database exists...");
			
			resultSet = conn.getMetaData().getCatalogs();
	        while (resultSet.next()) {

	          String nameTest = resultSet.getString(1);
	          nameTest = nameTest.toLowerCase(); 

	          if(nameTest.equals(DB_NAME.toLowerCase())){
	                System.out.println("Database exists, dropping database...");
	                sql = "DROP DATABASE " + DB_NAME;
	                stmt.executeUpdate(sql);
	                System.out.println("Database dropped");
	          }
	        }
	        resultSet.close();
	        
	        // Create the database 
	        System.out.println("Creating database...");
	        sql = "CREATE DATABASE "  + DB_NAME;
	        stmt.executeUpdate(sql);
			System.out.println("Database created successfully...");
			
			conn.close();
			stmt.close();
			
			// 	Connect to the new database
			conn = DriverManager.getConnection(DB_URL+DB_NAME+"?user="+USER+"&password="+PASS+"&autoReconnect=true&useSSL=false");
			stmt = conn.createStatement();
			System.out.println("Connected to database " + DB_NAME +"...");
		
			//	Create tables in database
			for(int i=0; i<equip.length; i++) {
				sql = "CREATE TABLE "  + equip[i] +" ("+dataNames[dataIndex[i][0]][0] + " " + dataNames[dataIndex[i][0]][1];
				for(int c=1; c < dataIndex[i].length; c++) {
					sql = sql + ", " + dataNames[dataIndex[i][c]][0] + " " + dataNames[dataIndex[i][c]][1];
				}
				sql = sql+")";
				System.out.println(sql);
				stmt.executeUpdate(sql);
			}

			// Populate tables with data
			for(EquipItem item:allEquip) {
				sql = "INSERT INTO " + equip[item.type] + " (" + dataNames[dataIndex[item.type][0]][0];
				for(int i = 1; i< dataIndex[item.type].length;i++) {
					 sql = sql + ", " + dataNames[dataIndex[item.type][i]][0];
				}
				sql = sql + ") VALUES ('#" + item.data[0];
				for(int i = 1; i< dataIndex[item.type].length;i++) {
					 sql = sql + "', '" + item.data[i];
				}
				sql = sql + "')";
				System.out.println(sql);
				stmt.executeUpdate(sql);
			}
			
			// Create RDF_ID table - could add to previous equipment item iteration step!!!!!
			
			sql = "CREATE TABLE "  + "RDF_ID" +" ("+dataNames[dataIndex[0][0]][0] + " " + dataNames[dataIndex[0][0]][1] + ", EquipmentType VARCHAR (38))";
			System.out.println(sql);
			stmt.executeUpdate(sql);
			
			for(EquipItem item:allEquip) {
				sql = "INSERT INTO RDF_ID (" + dataNames[dataIndex[0][0]][0] + ", EquipmentType)"+ " VALUES ('#" + item.data[0] +"'";
				sql = sql + ", '" + equip[item.type]+ "')";

				System.out.println(sql);
				stmt.executeUpdate(sql);
				
			}
			System.out.println("The table is updated...");			
			return true;
					
		}	
		catch(SQLException se){
		//Handle errors for JDBC
		se.printStackTrace();
		return false;
		}
		catch(Exception e){
		//Handle errors for Class.forName
		e.printStackTrace();
		return false;
		}
		finally {
		    try { if (resultSet != null) resultSet.close(); } catch (Exception e) {};
		    try { if (stmt != null) stmt.close(); } catch (Exception e) {};
		    try { if (conn != null) conn.close(); } catch (Exception e) {};
		}
	}
		
}		
