import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


public class Run {

	static String XmlFile = null;
	static String [] dbSetup = { "com.mysql.cj.jdbc.Driver","jdbc:mysql://localhost:3306/", "root", "root", "STUDENTS"};
	
	// Database constructs pass to all programs
	private static String [][] dataNames= {{"rdf_ID", "VARCHAR (38)"},//0
			{"NominalVoltage", "FLOAT (16,10)"}, //1
			{"Name", "VARCHAR(30)"}, //2 *************IS THIS ENOUGH FOR NAME 
			{"MaxOperatingP", "FLOAT (16,10)"}, //3 
			{"MinOperatingP", "FLOAT (16,10)"}, //4
			{"RatedS", "FLOAT (16,10)"}, //5
			{"P", "FLOAT (16,10)"}, //6 - SSH values
			{"Q", "FLOAT (16,10)"}, //7 - SSH Values
			{"TargetValue", "FLOAT (16,10)"}, //8 - SSH values ******?
			{"R", "FLOAT (16,10)"}, //9
			{"X", "FLOAT (16,10)"}, //10
			{"State", "BOOLEAN"}, //11 - SSH values
			{"Step", "INT"}, //12 - SSH values
			{"RDF_ID_Region", "VARCHAR (38)"},//13
			{"RDF_ID_Substation", "VARCHAR (38)"}, //14
			{"RDF_ID_BaseVoltage", "VARCHAR (38)"}, //15
			{"RDF_ID_EquipmentContainer", "VARCHAR (38)"}, //16
			{"RDF_ID_GeneratingUnit", "VARCHAR (38)"},//17
			{"RDF_ID_RegulatingControl", "VARCHAR (38)"}, //18
			{"RDF_ID_PowerTransformer", "VARCHAR (38)"}, //19
			{"RDF_ID_BaseVoltage", "VARCHAR (38)"}, //20 - added to deal with CIM inconsistency
			{"RDF_ID_ConnectivityContainer", "VARCHAR (38)"}, //21 - for connectivity nodes
			{"RDF_ID_ConnectivityNode", "VARCHAR (38)"}}; //22 - for connectivity nodes
	// Equipment types
	private static String [] equip = {"BaseVoltage","Substation", "VoltageLevel", "GeneratingUnit", "SynchronousMachine", "RegulatingControl", 
			"PowerTransformer", "EnergyConsumer", "PowerTransformerEnd", "Breaker", "RatioTapChanger", "ConnectivityNode", "Terminal"}; 
	// Hash table of date required for each type
	private static int [][] dataIndex= {{0, 1}, 	// BaseVoltage							
			{0,	2,	13},				// Substation					
			{0,	2,	14,	15},			// VoltageLevel				
			{0,	2,	3,	4,	16},		// Gen Unit		
			{0,	2,	5,  17,	18,	16}, // SynchMachine. Remove 6 and 7 because they are SSH. ALSO REMOVE BASE VOLTAGE RDF (15). NOT THERE??
			{0,	2},						// Reg Control - removed 14 SSH value 	
			{0,	2,	16},				// Power Tx		
			{0,	2,	16},				// Energy Con	 - Removed 15 ALSO NO BASE VOLTAGE . No 6 and 7 because SSH	
			{0,	2,	9,  10, 19,	20},	// Power Tx End 		
			{0,	2,	16},				// Breaker	- Removed 11 ALSO NO BASE VOLTAGE 	
			{0,	2}, 				// Ratio Tap Changer - Removed 12 SSH
			{0,	2, 21},				// Connectivity Node 
			{0, 2, 22}}; 			// Terminals
	// Hash table for SSH data
	private static int [] [] dataSSHIndex =  {{}, 	// BaseVoltage							
			{},				// Substation					
			{},			// VoltageLevel				
			{},		// Gen Unit		
			{0,6,7}, // SynchMachine.
			{0,8},						// Reg Control 
			{},				// Power Tx		
			{0,6,7},				// Energy Con	 	
			{},	// Power Tx End 		
			{0,11},				// Breaker	
			{0,12},				// Ratio Tap Changer
			{},
			{}}; 				 
	
	
	
	public static void main(String[] args) {


		// Structure
		// Run allows EQ file to be added
		// Calls the EQfile parser which creates the elements and then creates a database
		// EQ file parser uses arraylists to hold the node lists and equipment lists. each equipment item has a type and string array of data. the information held in data is determined by type. 
		
		
		try {
			File EQFile = new File("Assignment_EQ_reduced.xml");
			ParseEQ parserEQ = new ParseEQ(EQFile, equip, dataNames, dataIndex);
			parserEQ.dbBuild(dbSetup);
			
			File SSHFile = new File("Assignment_SSH_reduced.xml");
			ParseSSH parserSSH = new ParseSSH(SSHFile, equip, dataNames, dataSSHIndex);
			parserSSH.dbUpdate(dbSetup);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}

}
