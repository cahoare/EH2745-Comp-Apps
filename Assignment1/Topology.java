
//package code;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

public class Topology {
	// For working with complex
	private double re = 0; // the real part
	private double im = 0; // the imaginary part
	private static double loadre = 0; // the real part of the load
	private static double loadim = 0; // the imaginary part of the load
	// JDBC driver name and database URL - passed from Run
	static String JDBC_DRIVER;
	static String DB_URL;
	static String DB_NAME;

	// Database credentials - passed from Run
	static String USER;
	static String PASS;

	// BUSBAR INFO
	private ArrayList<String> busbar_name;
	private ArrayList<String> busbar_rdfID;
	private ArrayList<String> busbar_equipmentCont;
	// VOLTAGE LEVELS
	private ArrayList<String> voltagelevel_rdfID; // identification
	private ArrayList<String> voltagelevel_value; // value
	private ArrayList<String> voltagelevel_basevoltagerdfID; // value
	// TERMINALS AND CONNECT. NODES INFO
	private ArrayList<String> rdfID_resource;
	private ArrayList<String> common_connectnode;
	// BREAKERS
	private ArrayList<String> rdfID_breaker;
	private ArrayList<String> state_breaker;
	private ArrayList<String> rdfID_breakerCont;
	// LINES
	private ArrayList<String> rdfID_line;
	private ArrayList<String> R_line;
	private ArrayList<String> X_line;
	private ArrayList<String> bsh_line;
	// TRANSFORMER
	private ArrayList<String> rdfID_transformer;
	private ArrayList<String> R_transformer;
	private ArrayList<String> X_transformer;
	private ArrayList<String> container_transformer;
	// GENERATORS
	private ArrayList<String> rdfID_gen;
	private ArrayList<String> rdfID_syn;
	private ArrayList<String> rdfID_syn_genref;
	private ArrayList<String> p_gen;
	private ArrayList<String> q_gen;
	private ArrayList<String> p_max_gen;
	private ArrayList<String> p_min_gen;
	private ArrayList<String> q_perc_gen; // assumes 50% to save adding
	private ArrayList<String> ratedS_gen;
	private ArrayList<String> referenceP_gen; 
	private ArrayList<String> rdfID_genCont;
	// LOADS
	private ArrayList<String> rdfID_load;
	private ArrayList<String> p_load;
	private ArrayList<String> q_load;
	private ArrayList<String> rdfID_loadCont;
	
	// FOR TOPOLOGY BUILD
	private ArrayList<String[]> futurePaths = new ArrayList<String[]>();
	private ArrayList<ArrayList<String>> pastPaths = new ArrayList<ArrayList<String>>();
	private ArrayList<String> equipType = new ArrayList<String>();
	private int futurePathsIndex;
	private ArrayList<String[]> busEquipment = new ArrayList<String[]>(); // Records the equipment connected to each bus when a bus is discovered. [0] is bus num, [1] is equip rdf id and [2] is equiptype 

	// PASSED VARIABLES
	private static String[] equip;
	private static String[][] dataNames;
	private static int [][] dataIndex;
	private static int [][] dataSSHIndex;

	// PASSED VARIABLES FOR CALCULATION
	private double [] baseImpedance;
	
	// FOR BRANCH BUILD
	String[][] BranchInfo;
	
	
	public Topology(String[] dbSetup, String[] equip, String[][] dataNames, int [][] dataIndex, int [][] dataSSHIndex) {
		Topology.equip = equip;
		Topology.dataNames = dataNames;
		Topology.dataIndex = dataIndex;
		Topology.dataSSHIndex = dataSSHIndex;
		
		busbar_name = new ArrayList<String>();
		busbar_rdfID = new ArrayList<String>();
		busbar_equipmentCont = new ArrayList<String>();
		voltagelevel_rdfID = new ArrayList<String>(); // identification
		voltagelevel_value = new ArrayList<String>(); // value
		voltagelevel_basevoltagerdfID = new ArrayList<String>(); 
		rdfID_resource = new ArrayList<String>();
		common_connectnode = new ArrayList<String>();
		rdfID_breaker = new ArrayList<String>();
		state_breaker = new ArrayList<String>();
		rdfID_line = new ArrayList<String>();
		R_line = new ArrayList<String>();
		X_line = new ArrayList<String>();
		bsh_line = new ArrayList<String>();
		rdfID_transformer = new ArrayList<String>();
		R_transformer = new ArrayList<String>();
		X_transformer = new ArrayList<String>();
		container_transformer =  new ArrayList<String>();
		rdfID_gen = new ArrayList<String>();
		rdfID_syn = new ArrayList<String>();
		rdfID_syn_genref = new ArrayList<String>();
		p_gen = new ArrayList<String>();
		q_gen = new ArrayList<String>();
		p_max_gen = new ArrayList<String>();
		p_min_gen = new ArrayList<String>();
		q_perc_gen = new ArrayList<String>(); // assumes 50% to save adding
		ratedS_gen = new ArrayList<String>();
		referenceP_gen = new ArrayList<String>(); 
		rdfID_load = new ArrayList<String>();
		p_load = new ArrayList<String>();
		q_load = new ArrayList<String>();
		rdfID_genCont = new ArrayList<String>();
		rdfID_loadCont = new ArrayList<String>();
		rdfID_breakerCont = new ArrayList<String>();
		

		JDBC_DRIVER = dbSetup[0];
		DB_URL = dbSetup[1];
		USER = dbSetup[2];
		PASS = dbSetup[3];
		DB_NAME = dbSetup[4];
	
		
		
	}
	
	/*
	 * Method - Builds the topology of the network. 
	 * 
	 * Description - Uses arraylists to store all db data. Uses the indexes in each
	 * arraylist to join the data. Builds a new variable called pastPaths which contains
	 * info on the elements between buses.
	 * 
	 */
	
	public String dbBuildtopology() {
		
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

			String sql = "USE STUDENTS";
			stmt.executeUpdate(sql);


			// Data needed
			int[][] dataReq = { {}, 
					{}, 
					{ 0, 2, 19 }, // VoltageLevel - rdf_ID, name
					{0,3,4}, // Gen unit
					{0,21,5,6,7, 15, 16, 20}, // Synch Machine
					{},
					{}, 
					{0,6,7, 20}, // EnergyConsumer - None
					{ 9, 10, 23, 24 }, // PowerTransformerEnd
					{ 0, 12, 20 }, // Breaker - rdf_ID, state
					{}, 
					{}, 
					{ 26, 27 }, // Terminal - ConnectivityNode and ConductingEquipment
					{ 0, 2, 28 }, // Busbarsection - Name, RDFId
					{ 0, 9, 10, 11 }, // ACLineSegment
					{} }; // LinearShuntCompensator - None

			int[] equipReq = { 2, 8, 9, 12, 13, 14, 3, 4, 7 };

			for (int i = 0; i < equipReq.length; i++) {
				sql = "Select * From " + equip[equipReq[i]];
				rs = stmt.executeQuery(sql);

				switch (equipReq[i]) {
				case 13:
					while (rs.next()) {
						busbar_rdfID.add(rs.getString(dataNames[dataReq[equipReq[i]][0]][0]));
						busbar_name.add(rs.getString(dataNames[dataReq[equipReq[i]][1]][0]));
						busbar_equipmentCont.add(rs.getString(dataNames[dataReq[equipReq[i]][2]][0]));
					}
					break;
				case 2:
					while (rs.next()) {
						voltagelevel_rdfID.add(rs.getString(dataNames[dataReq[equipReq[i]][0]][0])); // identification
						voltagelevel_value.add(rs.getString(dataNames[dataReq[equipReq[i]][1]][0])); // value
						voltagelevel_basevoltagerdfID.add(rs.getString(dataNames[dataReq[equipReq[i]][2]][0])); // equipment rdfid new
					}
					break;
				case 12:
					while (rs.next()) {
						common_connectnode.add(rs.getString(dataNames[dataReq[equipReq[i]][0]][0]));
						rdfID_resource.add(rs.getString(dataNames[dataReq[equipReq[i]][1]][0]));
					}
					break;
				case 9:
					while (rs.next()) {
						rdfID_breaker.add(rs.getString(dataNames[dataReq[equipReq[i]][0]][0]));
						state_breaker.add(rs.getString(dataNames[dataReq[equipReq[i]][1]][0]));
						rdfID_breakerCont.add(rs.getString(dataNames[dataReq[equipReq[i]][2]][0]));
					}
					break;
				case 14:
					while (rs.next()) {
						rdfID_line.add(rs.getString(dataNames[dataReq[equipReq[i]][0]][0]));
						R_line.add(rs.getString(dataNames[dataReq[equipReq[i]][1]][0]));
						X_line.add(rs.getString(dataNames[dataReq[equipReq[i]][2]][0]));
						bsh_line.add(rs.getString(dataNames[dataReq[equipReq[i]][3]][0]));
					}
					break;
				case 8:
					while (rs.next()) {
						rdfID_transformer.add(rs.getString(dataNames[dataReq[equipReq[i]][2]][0]));
						R_transformer.add(rs.getString(dataNames[dataReq[equipReq[i]][0]][0]));
						X_transformer.add(rs.getString(dataNames[dataReq[equipReq[i]][1]][0]));
						container_transformer.add(rs.getString(dataNames[dataReq[equipReq[i]][3]][0]));
					}
					break;
				case 3:
					while (rs.next()) {
						rdfID_gen.add(rs.getString(dataNames[dataReq[equipReq[i]][0]][0]));
						p_max_gen.add(rs.getString(dataNames[dataReq[equipReq[i]][1]][0]));
						p_min_gen.add(rs.getString(dataNames[dataReq[equipReq[i]][2]][0]));
					}
					break;
				case 4:
					while (rs.next()) {
						rdfID_syn.add(rs.getString(dataNames[dataReq[equipReq[i]][0]][0]));
						rdfID_syn_genref.add(rs.getString(dataNames[dataReq[equipReq[i]][1]][0]));
						p_gen.add(rs.getString(dataNames[dataReq[equipReq[i]][3]][0]));
						q_gen.add(rs.getString(dataNames[dataReq[equipReq[i]][4]][0]));
						ratedS_gen.add(rs.getString(dataNames[dataReq[equipReq[i]][2]][0])); 
						q_perc_gen.add(rs.getString(dataNames[dataReq[equipReq[i]][6]][0]));
						referenceP_gen.add(rs.getString(dataNames[dataReq[equipReq[i]][5]][0]));
						rdfID_genCont.add(rs.getString(dataNames[dataReq[equipReq[i]][7]][0]));
					}
					break;
				case 7:
					while (rs.next()) {
						rdfID_load.add(rs.getString(dataNames[dataReq[equipReq[i]][0]][0]));
						p_load.add(rs.getString(dataNames[dataReq[equipReq[i]][1]][0]));
						q_load.add(rs.getString(dataNames[dataReq[equipReq[i]][2]][0]));
						rdfID_loadCont.add(rs.getString(dataNames[dataReq[equipReq[i]][3]][0]));
					}
					break;
				}
			}


			// Initialisation step - add the equipment type to the conducting equipment 
			for (int i = 0; i < rdfID_resource.size(); i++) {
				sql = "SELECT EquipmentType FROM rdf_id WHERE rdf_ID=\"" + rdfID_resource.get(i) + "\"";
				rs = stmt.executeQuery(sql);
				while (rs.next()) { 
					equipType.add(rs.getString(1));
				}

			}

			// Find initial bus and find conn node connected to that bus
			String initialBus = busbar_rdfID.get(0);
			int termTableRow = 0;
			while (!initialBus.equals(rdfID_resource.get(termTableRow)))
				termTableRow++;
			String connectNode = common_connectnode.get(termTableRow);

			String[] futurePathsInner = new String[3];

			futurePathsInner[0] = initialBus;

			// initialise the first future paths from initial bus bar
			for (int i = 0; i < common_connectnode.size(); i++) {
				if (common_connectnode.get(i).equals(connectNode)) {
					// Check that its a valid future path
					if (equipType.get(i).equals("EnergyConsumer")
							||equipType.get(i).equals("LinearShuntCompensator")
							||equipType.get(i).equals("SynchronousMachine")){
						String [] busEquipmentInner = new String [3];
						busEquipmentInner[0] = initialBus;
						busEquipmentInner[1] = rdfID_resource.get(i);
						busEquipmentInner[2] = equipType.get(i);
						busEquipment.add(busEquipmentInner);
					}
					else if (!rdfID_resource.get(i).equals(initialBus) && !equipType.get(i).equals("BusbarSection")) {
						futurePathsInner[1] = rdfID_resource.get(i);
						futurePathsInner[2] = common_connectnode.get(i);
						futurePaths.add(futurePathsInner);
					}
				}
			}

			// while there is still a future path to explore keep going
			futurePathsIndex = 0;
			while (futurePathsIndex < futurePaths.size()) {
				// Call explore path method
				ArrayList<String> currentPathHold = explorePath(futurePaths.get(futurePathsIndex));
				if (currentPathHold == null) {
				} else {
					pastPaths.add(currentPathHold);
				}
				futurePathsIndex++;
			}
			
			// add the required columns
			dbAddAssignReq(conn, stmt, rs); // Call the function to update tables with elements required by the assignment 
			// Calculate the base impedance
			baseImpedanceCalc(ratedS_gen.get(0));  // Use the first gen
			
			return ratedS_gen.get(0);

		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
			return null;
			
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
			return null;
			
		} finally {
		    try { if (rs != null) rs.close(); } catch (Exception e) {};
		    try { if (stmt != null) stmt.close(); } catch (Exception e) {};
		    try { if (conn != null) conn.close(); } catch (Exception e) {};
		}
	}

	public ArrayList<String> explorePath(String[] startingPoint) {

		ArrayList<String> currentPath = new ArrayList<String>();
		currentPath.add(startingPoint[0]);
		currentPath.add(startingPoint[1]);

		String conNodeHold = startingPoint[2];

		boolean foundBusBar = false;

		while (!foundBusBar) {

			// Find where the piece of conducting equipment next terminates
			boolean cont = true;
			int termTableRow = 0;
			while (cont) {
				if (rdfID_resource.get(termTableRow).equals(currentPath.get(currentPath.size() - 1))) {
					if (!common_connectnode.get(termTableRow).equals(conNodeHold)) {
						cont = false;
					}
				}
				termTableRow++;
			}
			termTableRow--; // the row in which it is the connectivity node. this will help us find the new
							// elements
			conNodeHold = common_connectnode.get(termTableRow);

			int numConnections = 0;
			// Search the conducting for number of connections if 2 then pass through node.
			// If > 2 then terminating node (busbar found)
			for (int i = 0; i < common_connectnode.size(); i++) {
				if (common_connectnode.get(i).equals(conNodeHold)) {
					numConnections++;
				}
			}

			// find the other piece of equipment connected to that node if not a busbar and
			// then progress
			if (numConnections == 2) {
				String pathHold = "";
				for (int i = 0; i < common_connectnode.size(); i++) {
					if (common_connectnode.get(i).equals(conNodeHold)
							&& !rdfID_resource.get(i).equals(currentPath.get(currentPath.size() - 1))) {
						pathHold = rdfID_resource.get(i);
					}
				}
				currentPath.add(pathHold);
			}

			// if a bus bar find the bus bar rf Id and add to explored path. Find any future
			// paths at that node.
			else {

				foundBusBar = true;
				String busRDFid = "";

				// before we add this path we need to check if we haven't entered through this
				// before. If so drop this path. Otherwise continue
				for (int i = 0; i < futurePathsIndex; i++) {
					if (futurePaths.get(i)[1].equals(currentPath.get(currentPath.size() - 1))) {
						return null;
					}
				}

				// find bus bar name first because we need to add to future paths array
				for (int i = 0; i < common_connectnode.size(); i++) {
					if (common_connectnode.get(i).equals(conNodeHold) && equipType.get(i).equals("BusbarSection")) {
						busRDFid = rdfID_resource.get(i);
					}
				}

				// before returning we need to add any further future paths to this node

				// first check if this has this bus been found before
				boolean previouslyFound = false;
				for (int i = 0; i < futurePaths.size(); i++) {
					if (futurePaths.get(i)[0].equals(busRDFid)) { // finds the future paths - string array element
						previouslyFound = true;
					}
				}
					
				// now look for further paths from this if it hasn't been found
				if (!previouslyFound) {
					for (int i = 0; i < common_connectnode.size(); i++) { // add the equipment from the bus to the equipment register for bus categorisation 
						if (common_connectnode.get(i).equals(conNodeHold)){
							if(equipType.get(i).equals("EnergyConsumer")
									||equipType.get(i).equals("LinearShuntCompensator")
									||equipType.get(i).equals("SynchronousMachine")){
								String [] busEquipmentInner = new String [3];
								busEquipmentInner[0] = busRDFid;
								busEquipmentInner[1] = rdfID_resource.get(i);
								busEquipmentInner[2] = equipType.get(i);
								busEquipment.add(busEquipmentInner);
							}
							else if (!equipType.get(i).equals("BusbarSection")
									&& !rdfID_resource.get(i).equals(currentPath.get(currentPath.size() - 1))){
								String[] futurePathsInner = new String[3];
								futurePathsInner[0] = busRDFid;
								futurePathsInner[1] = rdfID_resource.get(i);
								futurePathsInner[2] = common_connectnode.get(i);
								futurePaths.add(futurePathsInner);

							}
						}
							
					}
				}			
				currentPath.add(busRDFid);
			}
		}
		return currentPath;

	}

	/*
	 * Method - Add the Base Voltage RDF ID to synch machine, energy con and breaker DB tables
	 * 
	 * Description - Since Base Voltage RDF ID is not part of XML scheme for these three pieces of
	 * equipment but is required to be in table by assignment. Need to find and add to tables. This 
	 * requires the class variables of arraylists to be populated so can only be called after the 
	 * topology has been built. Hence, it is called directly from dbBuildTopology
	 * 
	 */	
	public void dbAddAssignReq(Connection conn, Statement stmt, ResultSet rs) {

		DatabaseMetaData md = null;
		String sql;
		
		// add the lists to an overall array so that it can be looped through
		ArrayList<ArrayList<String>> listHold = new ArrayList<ArrayList<String>>();
		listHold.add(rdfID_genCont);
		listHold.add(rdfID_loadCont);
		listHold.add(rdfID_breakerCont);
		
		
		// Add the new column to the table
		int [] updateEquip = {4,7,9};
		
		try{		
			// Each equipment links the equipment container to a voltage level RDF_ID, the voltage level RDF_ID table contains the base voltage RDF ID
			for (int i = 0; i < listHold.size(); i++) {
				// check that voltage level RDF_ID doesnt exist
				md = conn.getMetaData();
				rs = md.getColumns(null, null, equip[updateEquip[i]], dataNames[24][0]);
				if (!rs.next()) { // if base voltage doesnt exist then add the column and add the data
					System.out.println("Executing database for base voltage RDF_ID");
					sql = "ALTER TABLE " + equip[updateEquip[i]] + " ADD " + dataNames[24][0] + " " + dataNames[24][1];
					System.out.println(sql);
					stmt.executeUpdate(sql);
					for (int c= 0; c < listHold.get(i).size(); c++) {
						int vlInd = 0;
						System.out.println(listHold.get(i).get(c));
						
						while (!voltagelevel_rdfID.get(vlInd).equals(listHold.get(i).get(c))) {
							vlInd++;
							voltagelevel_rdfID.get(vlInd);
						}
						sql = "UPDATE " + equip[updateEquip[i]] + " SET " + dataNames[24][0] + "='"
								+ voltagelevel_basevoltagerdfID.get(vlInd) + "' WHERE " + dataNames[20][0] + "='"
								+ listHold.get(i).get(c) + "'";
						System.out.println(sql);
						stmt.executeUpdate(sql);
					}
				}

			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public String [][] genBuild(){		
			// generator data
			// 0	1	2	3		4		5	6		7		8		9		10	11	12		13		14		15		16			17		18		19		20
			// bus	Pg	Qg	Qmax	Qmin	Vg	mBase	status	Pmax	Pmin	Pc1	Pc2	Qc1min	Qc1max	Qc2min	Qc2max	ramp_agc	ramp_10	ramp_30	ramp_q	apf
			// x	x	x	x		x		1	x		1		x		x		0	0	0		0		0		0		0			0		0		0		0
			
			String [] [] genData = new String [rdfID_syn.size()][21]; // Assumes all buses are connected, main data	
		
			
			// This would be a lot better in sql
			for (int c = 0; c < rdfID_syn.size(); c++) {
				
				// Bus numbers are determined by position in array. NOT BUS NUMBER!
				// Find where the generator is connected - find the bus rdfID and then find the index
				int i = 0;
				while(!busEquipment.get(i)[1].equals(rdfID_syn.get(c))) i++;
				int busNum = 0;
				while(!busbar_rdfID.get(busNum).equals(busEquipment.get(i)[0])) busNum++;
				genData[c][0] = Integer.toString(busNum+1);
			
				
				// find the generating unit which corresponds to that synch machine
				int genIndex =0;
				while (!rdfID_gen.get(genIndex).equals(rdfID_syn_genref.get(c))) genIndex++;
				
				// Pg
				genData[c][1] = p_gen.get(c);
				// Qg
				genData[c][2] = q_gen.get(c);
				// Vg
				genData[c][5] = "1.0";
				// mBase
				genData[c][6] = ratedS_gen.get(c);
				// Status
				genData[c][7] = "1.0";
				// Pmax
				genData[c][8] = p_max_gen.get(genIndex);
				// Pmin
				genData[c][9] = p_min_gen.get(genIndex);				
				// Qmax
				double q_perc = Double.parseDouble(q_perc_gen.get(c));
				double p_maxHold =  Double.parseDouble(genData[c][8]);
				double qMax = p_maxHold *q_perc/100;
				genData[c][3] = Double.toString(qMax);
				// Qmin
				genData[c][4] = "-"+genData[c][3];
				
				for (int j =10; j<genData[c].length;j++) {
					genData[c][j]="0";
				}
			}			
			return genData;
	}
	
	public String [][] busBuild() {	
		// 	bus data
		//	0		1		2	3	4	5	6		7	8	9		10		11		12
		//	bus_i	type	Pd	Qd	Gs	Bs	area	Vm	Va	baseKV	zone	Vmax	Vmin
		// 	x		x		x	x	0	0	1		1	0	x		1		1.1		0.9
		
		String [] [] busData = new String [busbar_rdfID.size()][13]; // Assumes all buses are connected, main data 

		// Find the reference bus - (COULD BE ERROR HERE WITH SLACK BUS BEING DISCONNECTED....)
		// Find the generator with the highest reference priority
		String slackGen = rdfID_syn.get(0);
		int referenceP = Integer.parseInt(referenceP_gen.get(0));
		if (rdfID_syn.size()>1) {
			for (int i =1; i<rdfID_syn.size(); i++) {
				int currRefP = Integer.parseInt(referenceP_gen.get(i));
				if (currRefP<referenceP) {
					referenceP = currRefP;
					slackGen = rdfID_syn.get(i);
				}
			}
		}
		// Find the bus connected to that generator
		// slack bus rdfID
		int slackBusInd = 0;
		while(!busEquipment.get(slackBusInd)[1].equals(slackGen)) slackBusInd++;
		String slackBus = busEquipment.get(slackBusInd)[0];
		
		for (int i = 0 ; i < busbar_rdfID.size(); i++) {
			
			//bus_i
			busData[i][0] = Integer.toString(i+1);
			
			// IGNORED**** - if the transformer has a tap changer then the down stream bus would need to be a PU bus - THIS IS AN ERROR -shunt comp should be static and tap changers variable
			
			//type
			if (busbar_rdfID.get(i).equals(slackBus)){
				busData[i][1]="3";
			}
			else { // check if there is a linear shunt compensator connected. If so make it a PU bus. Otherwise PQ
				boolean shuntConn = false;
				for (int x = 0; x < busEquipment.size(); x++) {
					if(busEquipment.get(x)[0].equals(busbar_rdfID.get(i))&&busEquipment.get(x)[2].equals(equip[15])) {
						shuntConn = true;
					}
				}
				if(shuntConn) {
					busData[i][1]="2";
				}
				else {
					busData[i][1]="1";
				}
			}
			
			// connected loads
			double p_conn =0.0000;
			double q_conn =0.0000;
			for(int x =0; x < busEquipment.size(); x++) {
				if(busEquipment.get(x)[0].equals(busbar_rdfID.get(i))&&busEquipment.get(x)[2].equals(equip[7])) {
					// find the index for the load in the load array
					int j =0;
					while (!rdfID_load.get(j).equals(busEquipment.get(x)[1])) j++;
					p_conn= p_conn+Double.parseDouble(p_load.get(j));
					q_conn = q_conn+Double.parseDouble(q_load.get(j));
				}
			}
			busData[i][2]= Double.toString(p_conn);
			busData[i][3]= Double.toString(q_conn);
	
			// Gs
			busData[i][4] ="0";
			// Bs
			busData[i][5] ="0";			
			// Area
			busData[i][6]="1";		
			// Vm
			busData[i][7]="1";			
			// Va
			busData[i][8]="0";		
			// basekV
			// This needs to be found by searching for the equipment container reference in the voltage level 
			// The voltage level rdf_ID can then be used to find the voltage base level BUT in our case we will just use name which is comparable
			int g =0;
			while (!voltagelevel_rdfID.get(g).equals(busbar_equipmentCont.get(i))) g++;
			busData[i][9]=voltagelevel_value.get(g);	
			// zone
			busData[i][10]="1";		
			// Vmax
			busData[i][11]="1.1";	
			// Vmin
			busData[i][12]="0.9";		
		}
		
		return busData;
	}
	
	/*
	 * Method - build the branch data
	 * 
	 * Description - need to build the y bus within this branch build.
	 * 
	 */	
	public String [][] branchBuild(){
		
		// branch data
		//	fbus	tbus	r	x	b	
		
		///TEST DATA
		//String [] [] busData ={{"1",	"2", "0.00281",	"0.0281",	"0.00712",	"400", "400",	"400",	"0",	"0",	"1",	"-360",	"360"},
		//		{"1",	"4",	"0.00304",	"0.0304",	"0.00658",	"0",	"0",	"0",	"0",	"0",	"1",	"-360",	"360"},
		//		{"1",	"5",	"0.00064",	"0.0064",	"0.03126",	"0",	"0",	"0",	"0",	"0",	"1",	"-360",	"360"},
		//		{"2",	"3",	"0.00108",	"0.0108",	"0.01852",	"0",	"0",	"0",	"0",	"0",	"1",	"-360",	"360"},
		//		{"3",	"4",	"0.00297",	"0.0297",	"0.00674",	"0",	"0",	"0",	"0",	"0",	"1",	"-360",	"360"},
		//		{"4",	"5",	"0.00297",	"0.0297",	"0.00674",	"240",	"240",	"240",	"0",	"0",	"1",	"-360",	"360"}};
	
		
		String [][] hold = buildJavaYBus();
		
		String[] Info ={"From", "To", "R"," X"," B" , "rateA",	"rateB",	"rateC",	"ratio",	"angle"	,"status",	"angmin",	"angmax"};
		String[][] busData = new String[pastPaths.size()][Info.length];
		for (int i = 0; i < pastPaths.size(); i++) {
		    for (int j = 0; j < 5; j++) {
		    	busData[i][j]=BranchInfo[i][j];  
		    	if(busData[i][j]==null) { // Handle the no shunt capacitance in transformer
		    			busData[i][j]="0"; 
		    	}
		    }
		    for (int j=5; j < 10; j++) {
		    	busData[i][j]="0";
		    }
		    busData[i][10]=BranchInfo[i][5];
		    busData[i][11]="-360";
		    busData[i][12]="360";
	    }
		return busData;
	}
	
	
	public String[][] buildJavaYBus() {

		// Create the Y bus matrix 
		int nbus = busbar_rdfID.size();
		Double[][] Ymatrix_re = new Double[nbus][nbus];
		Double[][] Ymatrix_im = new Double[nbus][nbus];
		for (int k = 0; k < nbus; k++) { // initialise all values to 0
			for (int o = 0; o < nbus; o++) {
				Ymatrix_re[k][o] = 0.0;
				Ymatrix_im[k][o] = 0.0;
			}
		}
		
		BranchInfo = new String[busbar_rdfID.size()][6]; // branch is used but unsure what for
		
		// 
		MatrixCalculation(pastPaths,R_transformer,X_transformer,rdfID_transformer,container_transformer,
				rdfID_line,R_line,X_line,bsh_line,rdfID_breaker,state_breaker,voltagelevel_rdfID,voltagelevel_basevoltagerdfID,
				busbar_equipmentCont,busbar_rdfID, Ymatrix_re, Ymatrix_im, rdfID_resource, BranchInfo);

		// Join the two matrices together
		String[][] Ymatrix = new String[Ymatrix_im.length][Ymatrix_im.length];
		String Auxiliar = "";
		for (int i = 0; i < Ymatrix_im.length; i++) {
		    for (int j = 0; j < Ymatrix_im[i].length; j++) {
		    	if(Ymatrix_im[i][j]!=0.0){
		    		Auxiliar= " + " + String.valueOf(Ymatrix_im[i][j])+"j";
		    	
		    		Ymatrix[i][j]=String.valueOf(Ymatrix_re[i][j])+Auxiliar;
		    	}
		    	else{
		    			Ymatrix[i][j]=String.valueOf(Ymatrix_re[i][j])+"+"+String.valueOf(Ymatrix_im[i][j]);
		    	}		    	
		    }
	    }
		return Ymatrix;
	}
	
	
	public void MatrixCalculation(ArrayList<ArrayList<String>> Paths, ArrayList<String> Rtransformer,
			ArrayList<String> Xtransformer, ArrayList<String> rdfIDtransformer, ArrayList<String> Containertransformer,
			ArrayList<String> rdfIDline, ArrayList<String> Rline, ArrayList<String> Xline, ArrayList<String> bshline,
			ArrayList<String> rdfIDbreaker, ArrayList<String> statebreaker, ArrayList<String> voltagelevelrdfID,
			ArrayList<String> voltagelevelbasevoltagerdfID, ArrayList<String> busbarequipmentCont,
			ArrayList<String> busbarrdfID, Double[][] Ymatrixre, Double[][] Ymatrixim,
			ArrayList<String> rdfIDresource, String[][] Branch_info) {
		int row = 0;
		int location_new_baseimpedance = 0;
		int column = 0;
		int counter;
		double addmitance = 0;
		int counter2 = 0;
		double appliedZbase;
		Double Realpart_auxiliar;
		Double Imaginarypart_auxiliar;

		
		// 
		for (int a = 0; a < Paths.size(); a++) {
			addmitance = 0;
			for (int i = 0; i < busbarrdfID.size(); i++) {

				if (busbarrdfID.get(i).equals(Paths.get(a).get(0))) {// Then we are working in the row number row of the
																		// Y matrixes
					row = i;
					// Here we choose the Z base we are gonna use

				}
			}
			counter2 = 0;
			while (!busbarequipmentCont.get(row).equals(voltagelevelrdfID.get(counter2))) {
				counter2++;
			}
			appliedZbase = baseImpedance[counter2]; // We start by applying this base impedance. It might change.
			// It will be changed when going through a transformer
			Realpart_auxiliar = 0.0;

			Imaginarypart_auxiliar = 0.0;

			Branch_info[a][0] = String.valueOf(row + 1);
			for (int j = 1; j < Paths.get(a).size(); j++) { // We begin from 1 because 0 is busbar
				if (rdfIDbreaker.indexOf(Paths.get(a).get(j)) != -1) {
					Branch_info[a][5] = statebreaker.get(rdfIDbreaker.indexOf(Paths.get(a).get(j)));
					// We found a breaker
					/*
					 * if(statebreaker.get(rdfIDbreaker.indexOf(Paths.get(a).get(j)))!="1"){
					 * System.out.println("We break always"); break ; }else{}
					 */

				}
				if (rdfIDline.indexOf(Paths.get(a).get(j)) != -1) { // We found a line

					Realpart_auxiliar = Realpart_auxiliar
							+ Double.parseDouble(Rline.get(rdfIDline.indexOf(Paths.get(a).get(j)))) / appliedZbase; // Series
																													// resistance
																													// in
					// the branch j

					Imaginarypart_auxiliar = Imaginarypart_auxiliar
							+ Double.parseDouble(Xline.get(rdfIDline.indexOf(Paths.get(a).get(j)))) / appliedZbase;// Series
																													// reactance
																													// in
																													// the
																													// line

					if (j == 1 || j == 2) {
						addmitance = Double.parseDouble(bshline.get(rdfIDline.indexOf(Paths.get(a).get(j)))) * 0.5
								* appliedZbase;
						Ymatrixim[row][row] = Ymatrixim[row][row] + addmitance;
					}

					Branch_info[a][2] = String.valueOf(Realpart_auxiliar);
					Branch_info[a][3] = String.valueOf(Imaginarypart_auxiliar);
					Branch_info[a][4] = String.valueOf(addmitance * 2);

				}

				if (rdfIDtransformer.indexOf(Paths.get(a).get(j)) != -1) { // We found a transformer

					if (voltagelevelbasevoltagerdfID.get(counter2) == Containertransformer
							.get(rdfIDtransformer.indexOf(Paths.get(a).get(j)))
							&& (int) Double.parseDouble(
									Rtransformer.get(rdfIDtransformer.indexOf(Paths.get(a).get(j)))) == 0) {
						// We are going through the secondary side of the transformer, then we need to
						// change the appplied Zbase
						// to calculate the p.u. transformer impedance, since it is not zero as seen for
						// the other side
						location_new_baseimpedance = voltagelevelbasevoltagerdfID
								.indexOf(Containertransformer.get(rdfIDtransformer.indexOf(Paths.get(a).get(j)) - 1));
						appliedZbase = baseImpedance[location_new_baseimpedance];
						Realpart_auxiliar = Realpart_auxiliar
								+ Double.parseDouble(Rtransformer.get(rdfIDtransformer.indexOf(Paths.get(a).get(j))))
										/ appliedZbase;
						Imaginarypart_auxiliar = Imaginarypart_auxiliar
								+ Double.parseDouble(Xtransformer.get(rdfIDtransformer.indexOf(Paths.get(a).get(j))))
										/ appliedZbase;
						Branch_info[a][2] = String.valueOf(Realpart_auxiliar);
						Branch_info[a][3] = String.valueOf(Imaginarypart_auxiliar);
					} else {

						Realpart_auxiliar = Realpart_auxiliar
								+ Double.parseDouble(Rtransformer.get(rdfIDtransformer.indexOf(Paths.get(a).get(j))))
										/ appliedZbase; // Series resistance in
						// the branch

						Imaginarypart_auxiliar = Imaginarypart_auxiliar
								+ Double.parseDouble(Xtransformer.get(rdfIDtransformer.indexOf(Paths.get(a).get(j))))
										/ appliedZbase;// Series reactance in the
						Branch_info[a][2] = String.valueOf(Realpart_auxiliar);
						Branch_info[a][3] = String.valueOf(Imaginarypart_auxiliar);
						location_new_baseimpedance = voltagelevelbasevoltagerdfID
								.indexOf(Containertransformer.get(rdfIDtransformer.indexOf(Paths.get(a).get(j)) + 1));
						appliedZbase = baseImpedance[location_new_baseimpedance];
					}

					/*
					 * if(j==1){ In case we add b and g to the transformer
					 * Ymatrixim[row][row]=Ymatrixim[row][row]+Double.parseDouble(bshtransformer.get
					 * (rdfIDtransformer.indexOf(Paths.get(a).get(j))));
					 * Ymatrixre[row][row]=Ymatrixim[row][row]+Double.parseDouble(gtransformer.get(
					 * rdfIDtransformer.indexOf(Paths.get(a).get(j)))); }
					 */
				}

				if (j == Paths.get(a).size() - 1) { // End of the path

					counter = 0;
					while (!Paths.get(a).get(j).equals(busbarrdfID.get(counter))) { // Let's find which bus we have at
																					// the end
						counter++;
					}
					column = counter;
					Branch_info[a][1] = String.valueOf(counter + 1);
					Ymatrixim[column][column] = Ymatrixim[column][column] + addmitance; // The admitance of the line
																						// needs to be added to both bus
																						// ends!
				}

			} // Now we know in which row of the matrixes we have to put in the elements

			// We have to check if we have done a path among this two bus buses // lines in
			// parallel

			if (Ymatrixre[row][column] != 0.0 && Ymatrixim[row][column] != 0.0) {
				Ymatrixre[row][column] = parallelre(Ymatrixre[row][column], Ymatrixim[row][column], Realpart_auxiliar,
						Imaginarypart_auxiliar);
				Ymatrixim[row][column] = parallelim(Ymatrixre[row][column], Ymatrixim[row][column], Realpart_auxiliar,
						Imaginarypart_auxiliar);
				Ymatrixre[column][row] = Ymatrixre[row][column];
				Ymatrixim[column][row] = Ymatrixim[row][column];
			} else {// we have already gone through this path. We need to calculate the parallel
					// equivalent
				Ymatrixre[row][column] = Realpart_auxiliar;
				Ymatrixim[row][column] = Imaginarypart_auxiliar;
				Ymatrixre[column][row] = Ymatrixre[row][column]; // We never repeat paths so have to add it like this
																	// for the other path among the same buses
				Ymatrixim[column][row] = Ymatrixim[row][column];
			}

		}
		for (int i = 0; i < Ymatrixre.length; i++) {
			for (int e = 0; e < Ymatrixre.length; e++) {
				if (Ymatrixre[i][e] != 0.0 && e != i) {
					Ymatrixre[i][i] = Ymatrixre[i][i] + divisionre(Ymatrixre[i][e], Ymatrixim[i][e]);
					Ymatrixim[i][i] = Ymatrixim[i][i] + divisionim(Ymatrixre[i][e], Ymatrixim[i][e]);
				}
			}
		}
	}

	
	/*
	 * Method - Add a base impedance to the voltage level matrix  
	 * 
	 * Description - Called as part of topology build. Is referenced through javaYbus and
	 * the branch build methods. Use class variable.
	 *
	 */
	private void baseImpedanceCalc(String base_aparent_power) {
		baseImpedance = new double[voltagelevel_basevoltagerdfID.size()];
		// base_aparent_power Max Operating Power from the generating unit. 
		for (int i = 0; i < voltagelevel_basevoltagerdfID.size(); i++) {
			baseImpedance[i] = Math.pow(Double.parseDouble(voltagelevel_value.get(i)), 2)
					/ Double.parseDouble(base_aparent_power);
		}
	}

	private static Double divisionre(Double Re, Double Reac) { // 1/Impedance
		return (Re) / (Re * Re + Reac * Reac);
	}

	private static Double divisionim(Double Re, Double Reac) { // 1/Impedance
		return (-Reac) / (Re * Re + Reac * Reac);
	}
	private static Double parallelre(Double pre, Double pim, Double per2, Double pei2){
		
		return division_re(times_re(pre, pim, per2, pei2),times_im(pre, pim, per2, pei2),pre+per2,pim+pei2);
	}
	private static Double parallelim(Double lre, Double lim, Double ler2, Double lei2){
		
		return division_im(times_re(lre, lim, ler2, lei2),times_im(lre, lim, ler2, lei2),lre+ler2,lim+lei2);
	}
	private static Double times_re(Double are, Double aim, Double bre, Double bim) {

		return are * bre - aim * bim;

	}

	private static Double times_im(Double are, Double aim, Double bre, Double bim) {

		return are * bim + aim * bre;

	}

	private static Double division_re(Double c1r, Double c1i, Double c2r, Double c2i) {

		return (c1r * c2r + c1i * c2i) / (c2r * c2r + c2i * c2i);

	}

	private static Double division_im(Double c1r, Double c1i, Double c2r, Double c2i) {

		return (c1i * c2r - c1r * c2i) / (c2r * c2r + c2i * c2i);

	}
}
