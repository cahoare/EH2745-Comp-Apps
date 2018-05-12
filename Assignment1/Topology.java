
//package code;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.w3c.dom.NodeList;

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
	private static ArrayList<String> busbar_name = new ArrayList<String>();
	private static ArrayList<String> busbar_rdfID = new ArrayList<String>();
	private static ArrayList<String> busbar_equipmentCont = new ArrayList<String>();
	// VOLTAGE LEVELS
	private static ArrayList<String> voltagelevel_rdfID = new ArrayList<String>(); // identification
	private static ArrayList<String> voltagelevel_value = new ArrayList<String>(); // value
	// TERMINALS AND CONNECT. NODES INFO
	private static ArrayList<String> rdfID_resource = new ArrayList<String>();
	private static ArrayList<String> common_connectnode = new ArrayList<String>();
	// BREAKERS
	private static ArrayList<String> rdfID_breaker = new ArrayList<String>();
	private static ArrayList<String> state_breaker = new ArrayList<String>();
	// LINES
	private static ArrayList<String> rdfID_line = new ArrayList<String>();
	private static ArrayList<String> R_line = new ArrayList<String>();
	private static ArrayList<String> X_line = new ArrayList<String>();
	private static ArrayList<String> bsh_line = new ArrayList<String>();
	// TRANSFORMER
	private static ArrayList<String> rdfID_transformer = new ArrayList<String>();
	private static ArrayList<String> R_transformer = new ArrayList<String>();
	private static ArrayList<String> X_transformer = new ArrayList<String>();

	// FOR TOPOLOGY BUILD
	private static ArrayList<String[]> futurePaths = new ArrayList<String[]>();
	private static ArrayList<ArrayList<String>> pastPaths = new ArrayList<ArrayList<String>>();
	private static ArrayList<String> equipType = new ArrayList<String>();
	private static int futurePathsIndex;

	// PASSED VARIABLES
	private static String[] equip;
	private static String[][] dataNames;

	public static void dbBuildtopology(String[] dbSetup, String[] equip, String[][] dataNames) {

		Topology.equip = equip;
		Topology.dataNames = dataNames;

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
			int[][] dataReq = { {}, {}, { 0, 2 }, // VoltageLevel - rdf_ID, name
					{}, {}, {}, {}, {}, // EnergyConsumer - None
					{ 0, 9, 10 }, // PowerTransformerEnd
					{ 0, 12 }, // Breaker - rdf_ID, state
					{}, {}, { 24, 25 }, // Terminal - ConnectivityNode and ConductingEquipment
					{ 0, 2, 26 }, // Busbarsection - Name, RDFId
					{ 0, 9, 10, 11 }, // ACLineSegment
					{} }; // LinearShuntCompensator - None

			int[] equipReq = { 2, 8, 9, 12, 13, 14 };

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
				case 7:
					while (rs.next()) {
						rdfID_transformer.add(rs.getString(dataNames[dataReq[equipReq[i]][0]][0]));
						R_transformer.add(rs.getString(dataNames[dataReq[equipReq[i]][1]][0]));
						X_transformer.add(rs.getString(dataNames[dataReq[equipReq[i]][2]][0]));
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

			// equipType =new ArrayList<String>(); // ...doesnt need ot be arraylist could
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
					if (!rdfID_resource.get(i).equals(initialBus) && !equipType.get(i).equals("BusbarSection")
							&& !equipType.get(i).equals("EnergyConsumer")
							&& !equipType.get(i).equals("LinearShuntCompensator")
							&& !equipType.get(i).equals("SynchronousMachine")) {
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

		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		}
	}

	public static ArrayList<String> explorePath(String[] startingPoint) {

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
			termTableRow--; // the row in which it is the nconnectivity node. this will help us find the new
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
					for (int i = 0; i < common_connectnode.size(); i++) {
						if (common_connectnode.get(i).equals(conNodeHold)
								&& !rdfID_resource.get(i).equals(currentPath.get(currentPath.size() - 1))
								&& !equipType.get(i).equals("BusbarSection")
								&& !equipType.get(i).equals("EnergyConsumer")
								&& !equipType.get(i).equals("LinearShuntCompensator")
								&& !equipType.get(i).equals("SynchronousMachine")) {
							String[] futurePathsInner = new String[3];
							futurePathsInner[0] = busRDFid;
							futurePathsInner[1] = rdfID_resource.get(i);
							futurePathsInner[2] = common_connectnode.get(i);
							futurePaths.add(futurePathsInner);

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

	public static void YmatrixCalculation(ArrayList<ArrayList<String>> Branchesy, ArrayList<String> rdfIDbreaker,
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

	private static String toString(Double re, Double im) {
		if (im == 0)
			return re + "";
		if (re == 0)
			return im + "i";
		if (im < 0)
			return re + " - " + (-im) + "i";
		return re + " + " + im + "i";
	}

	private static Double LoadR(Double voltage, String p) {
		loadre = voltage * voltage / Double.parseDouble(p);
		return loadre;
	}

	private static Double LoadI(Double voltage, String q) {
		loadim = voltage * voltage / Double.parseDouble(q);
		return loadim;
	}

	private static Double divisionre(Double Re, Double Reac) {
		return (Re) / (Re * Re + Reac * Reac);
	}

	private static Double divisionim(Double Re, Double Reac) {
		return (-Reac) / (Re * Re + Reac * Reac);
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
