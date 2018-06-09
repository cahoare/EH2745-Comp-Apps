
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.*;
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

	// TABLE information
	private static String[] tableFormat;
	private static String process;

	// Set up the variables to be stored
	// state is the hold for each time stamp, allStates is the table for all time, timeStamps is a separate int array to store the times
	private ArrayList<double[]> allStates = new ArrayList<double[]>();
	private ArrayList<Integer> timeStamps = new ArrayList<Integer>();
	
//	public ArrayList<Escenario> EscenariosData = new ArrayList<Escenario>();

	public Identification(String[] dbSetup, String[] tableFormat,String process) {

		JDBC_DRIVER = dbSetup[0];
		DB_URL = dbSetup[1];
		USER = dbSetup[2];
		PASS = dbSetup[3];
		DB_NAME = dbSetup[4];
		Identification.process= process;
		Identification.tableFormat = tableFormat;

		importMeasure();

	}

	public void importMeasure() {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		try {
			// Register JDBC driver
			Class.forName(JDBC_DRIVER);

			// Open a connection
			conn = DriverManager.getConnection(
					DB_URL + DB_NAME + "?user=" + USER + "&password=" + PASS + "&autoReconnect=true&useSSL=false");

			stmt = conn.createStatement();
			String sql = "USE SUBTABLES";
			stmt.executeUpdate(sql);

			// Find number of entries in table
			if( process=="kmeans"){
			sql = "Select count(*) From measurements";}
			else{
				sql = "Select count(*) From analog_values";
			}
			rs = stmt.executeQuery(sql);
			int entriesTot = 0;
			while (rs.next()) {
				entriesTot = rs.getInt("count(*)");
			}


			// Search through database for each time (assumes that database is roughly time indexed close to 0)
			// Creates an array sorted on time (assumes database is not sorted)
			double[] state;
			int entriesSorted = 0;
			int time = 0;
			while (entriesSorted < entriesTot) {
				sql = "Select * From measurements where time = '" + time + "'";
				rs = stmt.executeQuery(sql);

				state = new double[18];
				int newEntries = 0;
				// if the time exists for each entry add it to this state
				while (rs.next()) {
					int dataType = 0;
					// find the index of this datatype (switch more efficient???)
					while (!rs.getString("name").equals(tableFormat[dataType]))
						dataType++;
					state[dataType] = rs.getDouble("value");
					newEntries++;
				}
				// add to the state and time table if an entry found
				if (newEntries != 0) {
					if (newEntries % tableFormat.length != 0) // throw custom exception if a value missing
						throw new EntryMissingException();
					allStates.add(state);
					timeStamps.add(time);
					entriesSorted += newEntries;
				}
				
				time++;
				
				if (time > 20000)
					throw new EntryMissingException(); //throw custom exception if time ran too long
			}
		} catch (EntryMissingException dbError) {
			// Handle error for a missing db
			dbError.printStackTrace();
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		}

	}

	
	public ArrayList<double[]> getDataTable(){
		return allStates;
	}
	
	public ArrayList<Integer> getTimeStamps(){
		return timeStamps;
	}
}
