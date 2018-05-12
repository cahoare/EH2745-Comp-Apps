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


public class ParseSSH {

	private static File SSHFile;
	private static ArrayList <NodeList> listEquip;
	private static ArrayList <EquipData> equipDataList;
	
	
	// Equipment types
	private static String [] equip; 
	// data types
	private static String [][] dataNames;
			
	// Hash table of date required for each type
	private static int [][] dataIndex;

	// equipment where SSH data stored
	private static int [] sshEquip = {4,5,7,9,10};
	
	
	// JDBC driver name and database URL - passed from Run
	static String JDBC_DRIVER; 
	static String DB_URL; 
	static String DB_NAME;
	
	// Database credentials - passed from Run
	static String USER; 
	static String PASS;
	
	
	
	
	public ParseSSH (File SSHFile, String [] equip, String [][] dataNames, int [][] dataIndex) {
		ParseSSH.SSHFile = SSHFile;
		ParseSSH.equip = equip;
		ParseSSH.dataNames = dataNames;
		ParseSSH.dataIndex = dataIndex;
		sshParse();		
	}

	private static void sshParse () {
		
		
		// just need to find values for Synch machine, regulating control, energy con, Tap changer 
		
		listEquip = new ArrayList<NodeList>(); 
		equipDataList = new ArrayList<EquipData>();
		
		try {  // catch dbFactory exception
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance(); // parses XML file
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(SSHFile);  
			doc.getDocumentElement().normalize(); // on this checks that each element only has text where it should be. If something has been out onto multiple lines will amend into each element. 

			for (int i=0; i<sshEquip.length; i++) { // Search for each equipment type sequentially and add the list to the array
				listEquip.add(doc.getElementsByTagName("cim:" + equip[sshEquip[i]]));

				for (int c=0; c < listEquip.get(i).getLength(); c++) { // build an item for each type within the list. item type corresponds to equip array
					equipDataList.add(new EquipData(listEquip.get(i).item(c), sshEquip[i], equip)) ;
				}
					
			}
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
				
	}

	public static boolean dbUpdate(String [] dbSetup){

		
		JDBC_DRIVER = dbSetup[0];
		DB_URL = dbSetup[1];
		USER = dbSetup[2];
		PASS= dbSetup[3];
		DB_NAME = dbSetup[4];
		
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		DatabaseMetaData md = null;
		
		try{
			// Register JDBC driver
			Class.forName(JDBC_DRIVER);
			// Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL+"?user="+USER+"&password="+PASS+"&autoReconnect=true&useSSL=false");
			
			// 	Connect to the database - throw exception if it doesnt exist *****CURRENTLY NOT HANDLED, just stack
			conn = DriverManager.getConnection(DB_URL+DB_NAME+"?user="+USER+"&password="+PASS+"&autoReconnect=true&useSSL=false");
			stmt = conn.createStatement();
			System.out.println("Connected to database " + DB_NAME +"...");
			String sql;
			
			//	Alter tables in database
			for(int i=0; i<sshEquip.length; i++) {
				for(int c=1; c < dataIndex[sshEquip[i]].length; c++) {
					
					//Check if columns already exist
					md  = conn.getMetaData();
					rs = md.getColumns(null, null, equip[sshEquip[i]] ,  dataNames[dataIndex[sshEquip[i]][c]][0] );
					
					if (!rs.next()) { // if columns dont exist then add them
				
						sql = "ALTER TABLE "  + equip[sshEquip[i]] + " ADD " + dataNames[dataIndex[sshEquip[i]][c]][0] + " " + dataNames[dataIndex[sshEquip[i]][c]][1];
						System.out.println(sql);
						stmt.executeUpdate(sql);
					}
				}
			}

			// Populate tables with data
			for(EquipData item:equipDataList) {
				for(int i = 1; i< dataIndex[item.type].length;i++) {
					sql = "UPDATE " + equip[item.type] + " SET " + dataNames[dataIndex[item.type][i]][0] + "='" + item.data[i] + "' WHERE " + dataNames[dataIndex[item.type][0]][0] + "='" + item.data[0] +"'";
					System.out.println(sql);
					stmt.executeUpdate(sql);
				}

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
		    try { if (rs != null) rs.close(); } catch (Exception e) {};
		    try { if (stmt != null) stmt.close(); } catch (Exception e) {};
		    try { if (conn != null) conn.close(); } catch (Exception e) {};
		    try { if (md != null) conn.close(); } catch (Exception e) {};
		}

	}
		
}		
