
//package code;

import java.sql.Connection;
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
	// TERMINALS AND CONNECT. NODES INFO
	private ArrayList<String> rdfID_resource;
	private ArrayList<String> common_connectnode;
	// BREAKERS
	private ArrayList<String> rdfID_breaker;
	private ArrayList<String> state_breaker;
	// LINES
	private ArrayList<String> rdfID_line;
	private ArrayList<String> R_line;
	private ArrayList<String> X_line;
	private ArrayList<String> bsh_line;
	// TRANSFORMER
	private ArrayList<String> rdfID_transformer;
	private ArrayList<String> R_transformer;
	private ArrayList<String> X_transformer;
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
	// LOADS
	private ArrayList<String> rdfID_load;
	private ArrayList<String> p_load;
	private ArrayList<String> q_load;
	
	
	// FOR TOPOLOGY BUILD
	private ArrayList<String[]> futurePaths = new ArrayList<String[]>();
	private ArrayList<ArrayList<String>> pastPaths = new ArrayList<ArrayList<String>>();
	private ArrayList<String> equipType = new ArrayList<String>();
	private int futurePathsIndex;
	private ArrayList<String[]> busEquipment = new ArrayList<String[]>(); // Records the equipment connected to each bus when a bus is discovered. [0] is bus num, [1] is equip rdf id and [2] is equiptype 

	// PASSED VARIABLES
	private static String[] equip;
	private static String[][] dataNames;

	public Topology(String[] equip, String[][] dataNames) {
		Topology.equip = equip;
		Topology.dataNames = dataNames;
		busbar_name = new ArrayList<String>();
		busbar_rdfID = new ArrayList<String>();
		busbar_equipmentCont = new ArrayList<String>();
		voltagelevel_rdfID = new ArrayList<String>(); // identification
		voltagelevel_value = new ArrayList<String>(); // value
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
	}
	
	public void dbBuildtopology(String[] dbSetup) {

		Connection conn = null;
		Statement stmt = null;

		JDBC_DRIVER = dbSetup[0];
		DB_URL = dbSetup[1];
		USER = dbSetup[2];
		PASS = dbSetup[3];
		DB_NAME = dbSetup[4];

		try {
			// Register JDBC driver
			Class.forName(JDBC_DRIVER);

			// Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			stmt = conn.createStatement();

			String sql = "USE STUDENTS";
			stmt.executeUpdate(sql);
			ResultSet rs;

			// Data needed
			int[][] dataReq = { {}, 
					{}, 
					{ 0, 2 }, // VoltageLevel - rdf_ID, name
					{0,3,4}, // Gen unit
					{0,21,5,6,7, 15, 16}, // Synch Machine
					{},
					{}, 
					{0,6,7}, // EnergyConsumer - None
					{ 0, 9, 10 }, // PowerTransformerEnd
					{ 0, 12 }, // Breaker - rdf_ID, state
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
				System.out.println(sql);

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
						rdfID_transformer.add(rs.getString(dataNames[dataReq[equipReq[i]][0]][0]));
						R_transformer.add(rs.getString(dataNames[dataReq[equipReq[i]][1]][0]));
						X_transformer.add(rs.getString(dataNames[dataReq[equipReq[i]][2]][0]));
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
					}
					break;
				case 7:
					while (rs.next()) {
						rdfID_load.add(rs.getString(dataNames[dataReq[equipReq[i]][0]][0]));
						p_load.add(rs.getString(dataNames[dataReq[equipReq[i]][1]][0]));
						q_load.add(rs.getString(dataNames[dataReq[equipReq[i]][2]][0]));
					}
					break;
				}
			}
			

			
			
			// We create the busbar matrix here.
			int nbus = busbar_rdfID.size();
			Double[][] Ymatrix_re = new Double[nbus][nbus];
			Double[][] Ymatrix_im = new Double[nbus][nbus];
			for (int k = 0; k < nbus; k++) {
				for (int o = 0; o < nbus; o++) {
					Ymatrix_re[k][o] = 0.0;
					Ymatrix_im[k][o] = 0.0;
				}
			}

			/*
			 * Could delete? double R=0; double X=0; double one_over_impedance_re=0; double
			 * one_over_impedance_im=0;
			 */

			// rdfID_resource is the RDF_ID of the equipment from equipment column of the
			// terminal table
			// common_connectnode is the RDFID of the connectivity node, from CN column of
			// the terminal table

			// Initialisation step - add the equipment type to the conducting equipment in
			// terminal table (could be done in sql...)

			// equipType =new ArrayList<String>(); // ...doesnt need to be arraylist could
			// be string array. Initialised in class

			for (int i = 0; i < rdfID_resource.size(); i++) {
				sql = "SELECT EquipmentType FROM rdf_id WHERE rdf_ID=\"" + rdfID_resource.get(i) + "\"";
				rs = stmt.executeQuery(sql);
				while (rs.next()) { // Is this while needed.........
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

			
			for (int i = 0; i < busEquipment.size();i++) {
				System.out.println(Arrays.toString(busEquipment.get(i)));
			}
			
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
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

		String debugHold = "";
		for (int c = 0; c < currentPath.size(); c++) {
			debugHold = debugHold + currentPath.get(c) + " ";
		}
		System.out.println(debugHold);

		return currentPath;

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
			//	0		1		2	3	4	5	6		7	8	9		10		11		12
			//	bus_i	type	Pd	Qd	Gs	Bs	area	Vm	Va	baseKV	zone	Vmax	Vmin
			// 	x		x		x	x	0	0	1		1	0	x		1		1.1		0.9
		}
		
		return busData;
	}
	
	public static String [][] branchBuild(){
		
		// TEST METHOD!!!
		
		String [] [] busData ={{"1",	"2", "0.00281",	"0.0281",	"0.00712",	"400", "400",	"400",	"0",	"0",	"1",	"-360",	"360"},
				{"1",	"4",	"0.00304",	"0.0304",	"0.00658",	"0",	"0",	"0",	"0",	"0",	"1",	"-360",	"360"},
				{"1",	"5",	"0.00064",	"0.0064",	"0.03126",	"0",	"0",	"0",	"0",	"0",	"1",	"-360",	"360"},
				{"2",	"3",	"0.00108",	"0.0108",	"0.01852",	"0",	"0",	"0",	"0",	"0",	"1",	"-360",	"360"},
				{"3",	"4",	"0.00297",	"0.0297",	"0.00674",	"0",	"0",	"0",	"0",	"0",	"1",	"-360",	"360"},
				{"4",	"5",	"0.00297",	"0.0297",	"0.00674",	"240",	"240",	"240",	"0",	"0",	"1",	"-360",	"360"}};
		return busData;
	}
	
	
	public void YmatrixCalculation(ArrayList<ArrayList<String>> Branchesy, ArrayList<String> rdfIDbreaker,
			ArrayList<String> rdfIDtransformer, ArrayList<String> rdfIDline, ArrayList<String> statebreaker,
			ArrayList<String> Rline, ArrayList<String> Xline, ArrayList<String> Rtransformer,
			ArrayList<String> Xtransformer, ArrayList<String> bshline, Double[][] Ymatrixre, Double[][] Ymatrixim,
			ArrayList<String> busrdfid, String bus) {
		int branchlength = Branchesy.size();
		int rowy = 0;
		int endbus = 0;
		int nlines = rdfIDline.size();
		int nbreaker = rdfIDbreaker.size();
		int ntransformer = rdfIDtransformer.size();
		ArrayList<ArrayList<Double>> Realpart = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> Imaginarypart = new ArrayList<ArrayList<Double>>();
		ArrayList<Double> Realpart_auxiliar_array = new ArrayList<Double>();
		ArrayList<Double> Imaginarypart_auxiliar_array = new ArrayList<Double>();
		Double Realpart_auxiliar = 0.0;
		Double Imaginarypart_auxiliar = 0.0;
		Double parallel_re = 0.0;
		Double parallel_im = 0.0;

		ArrayList<String> Endbus_rdf = new ArrayList<String>(); // WIth this we check if there are parallel branches

		for (int i = 0; i < busrdfid.size(); i++) {
			if (busrdfid.get(i).equals(bus)) {// Then we are working on the row rowy of the Y matrixes
				rowy = i;
			}
		}
		for (int i = 0; i < Branchesy.size(); i++) {
			Realpart_auxiliar = 0.0;
			Imaginarypart_auxiliar = 0.0;
			for (int j = 0; j < Branchesy.get(i).size(); j++) {
				System.out.println("did we get in?");
				System.out.println(Branchesy.get(i).get(j));

				if (rdfIDbreaker.indexOf(Branchesy.get(i).get(j)) != -1) {// if its -1 it means it's not in that array
					// we have a breaker// We don't really do anything. The open breakers are
					// deleted before
					// if(rdfIDbreaker.indexOf(Branchesy.get(i).get(j))!=0.000){}
					// else{ClosedBreaker.remove(i); ClosedBreaker.add(false); // This is an open
					// breaker}
					System.out.println("We have a breaker breaker");
				}
				if (rdfIDline.indexOf(Branchesy.get(i).get(j)) != -1) {
					Realpart.get(i).add(Double.parseDouble(Rline.get(rdfIDline.indexOf(Branchesy.get(i).get(j)))));
					Imaginarypart.get(i).add(Double.parseDouble(Rline.get(rdfIDline.indexOf(Branchesy.get(i).get(j)))));
					Ymatrixim[rowy][rowy] = Double.parseDouble(bshline.get(rdfIDline.indexOf(Branchesy.get(i).get(j))));
					Imaginarypart_auxiliar = Imaginarypart_auxiliar + Realpart.get(i).get(j); // Series resistance in
																								// the branch j
					Realpart_auxiliar = Realpart_auxiliar + Imaginarypart.get(i).get(j);// Series reactance in the
																						// branch j
					System.out.println("We have a line");
				}
				if (rdfIDtransformer.indexOf(Branchesy.get(i).get(j)) != -1) {
					// we have a transformer
					Realpart.get(i).add(
							Double.parseDouble(Rtransformer.get(rdfIDtransformer.indexOf(Branchesy.get(i).get(j)))));
					Imaginarypart.get(i).add(
							Double.parseDouble(Rtransformer.get(rdfIDtransformer.indexOf(Branchesy.get(i).get(j)))));
					Imaginarypart_auxiliar = Imaginarypart_auxiliar + Realpart.get(i).get(j); // Series resistance in
																								// the branch j
					Realpart_auxiliar = Realpart_auxiliar + Imaginarypart.get(i).get(j);// Series reactance in the
																						// branch j
					System.out.println("We have a transformer");
				}
				if (j == Branchesy.get(i).size() - 1) {// End of this branch
					for (int p = 0; p < busrdfid.size(); p++) {
						if (busrdfid.get(p).equals(Branchesy.get(i).get(j))) {// Then we are working on the column p of
																				// the Y matrixes // for(int f=0;
																				// f<Realpart.get(i).size();f++){
							// The end busbar is the p one of the busrdfid vector
							Endbus_rdf.add(Branchesy.get(i).get(j)); // this is the end bus in the i branch
							Realpart_auxiliar_array.add(Realpart_auxiliar);
							Imaginarypart_auxiliar_array.add(Imaginarypart_auxiliar);

						}
					}

					for (int r = 0; r < Endbus_rdf.size() - 1; r++) { // this is to check if there are parallel lines
																		// and calculate it

						if (Endbus_rdf.get(r).equals(Endbus_rdf.get(Endbus_rdf.size() - 1))) {// with this you compare
																								// each end bus already
																								// found with the one
																								// found now
							// The r branch is in parallel with the last one calculated // which is the i
							// branch
							parallel_re = division_re(
									times_re(Realpart_auxiliar_array.get(r), Imaginarypart_auxiliar_array.get(r),
											Realpart_auxiliar_array.get(i), Imaginarypart_auxiliar_array.get(i)),
									times_im(Realpart_auxiliar_array.get(r), Imaginarypart_auxiliar_array.get(r),
											Realpart_auxiliar_array.get(i), Imaginarypart_auxiliar_array.get(i)),
									Realpart_auxiliar_array.get(r) + Realpart_auxiliar_array.get(i),
									Imaginarypart_auxiliar_array.get(r) + Imaginarypart_auxiliar_array.get(i));
							parallel_im = division_im(
									times_re(Realpart_auxiliar_array.get(r), Imaginarypart_auxiliar_array.get(r),
											Realpart_auxiliar_array.get(i), Imaginarypart_auxiliar_array.get(i)),
									times_im(Realpart_auxiliar_array.get(r), Imaginarypart_auxiliar_array.get(r),
											Realpart_auxiliar_array.get(i), Imaginarypart_auxiliar_array.get(i)),
									Realpart_auxiliar_array.get(r) + Realpart_auxiliar_array.get(i),
									Imaginarypart_auxiliar_array.get(r) + Imaginarypart_auxiliar_array.get(i));
							System.out.println("HIjo puta hasta aqui funciona");
							Realpart_auxiliar_array.set(r, parallel_re); // we replace the value we had in the r branch
							Imaginarypart_auxiliar_array.set(r, parallel_im);
							// Now we can also delete the one we have used for the parallel not to repeat
							// this calculation;
							Endbus_rdf.remove(Endbus_rdf.get(Endbus_rdf.size() - 1));
							Realpart_auxiliar_array
									.remove(Realpart_auxiliar_array.get(Realpart_auxiliar_array.size() - 1));
							Imaginarypart_auxiliar_array
									.remove(Imaginarypart_auxiliar_array.get(Imaginarypart_auxiliar_array.size() - 1));

						}
					}
				}

			}
		}

		for (int x = 0; x < Endbus_rdf.size(); x++) {
			for (int y = 0; y < busrdfid.size(); y++) {
				if (Endbus_rdf.get(x) == busrdfid.get(y)) {
					Ymatrixim[rowy][y] = Imaginarypart_auxiliar_array.get(x);
					Ymatrixre[rowy][y] = Imaginarypart_auxiliar_array.get(x);
				}
			}
		}
	}

	private  String toString(Double re, Double im) {
		if (im == 0)
			return re + "";
		if (re == 0)
			return im + "i";
		if (im < 0)
			return re + " - " + (-im) + "i";
		return re + " + " + im + "i";
	}

	private  Double LoadR(Double voltage, String p) {
		loadre = voltage * voltage / Double.parseDouble(p);
		return loadre;
	}

	private Double LoadI(Double voltage, String q) {
		loadim = voltage * voltage / Double.parseDouble(q);
		return loadim;
	}

	private Double divisionre(Double Re, Double Reac) {
		return (Re) / (Re * Re + Reac * Reac);
	}

	private Double divisionim(Double Re, Double Reac) {
		return (-Reac) / (Re * Re + Reac * Reac);
	}

	private Double times_re(Double are, Double aim, Double bre, Double bim) {

		return are * bre - aim * bim;

	}

	private Double times_im(Double are, Double aim, Double bre, Double bim) {

		return are * bim + aim * bre;

	}

	private Double division_re(Double c1r, Double c1i, Double c2r, Double c2i) {

		return (c1r * c2r + c1i * c2i) / (c2r * c2r + c2i * c2i);

	}

	private Double division_im(Double c1r, Double c1i, Double c2r, Double c2i) {

		return (c1i * c2r - c1r * c2i) / (c2r * c2r + c2i * c2i);

	}
}
