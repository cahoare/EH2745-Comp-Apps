import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import java.util.StringJoiner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.sql.*;
import java.util.Arrays;
import com.mathworks.engine.*;
import java.util.concurrent.*;
import javax.swing.filechooser.*;


public class Run {

	static String XmlFile = null;
	static String [] dbSetup = { "com.mysql.cj.jdbc.Driver","jdbc:mysql://localhost:3306/", "root", "root", "students"};
	
	// If the data collected is updated there are several variations of the index table used in the program that must be updated
	// These are - here, in equip item (note the rdf resource split index!), the switch statement in equip data, and the index in topology 
	
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
			{"B", "FLOAT (30,20)"}, //11 - What is this?
			{"State", "BOOLEAN"}, //12 - SSH values
			{"Step", "INT"}, //13 - SSH values
			{"Enabled", "BOOLEAN"}, // 14 - SSH values
			{"referencePriority", "INT"}, // **** 15 - SSH values
			{"qPercent", "FLOAT(16,10)"}, // **** 16 
			//{"bpersection", "FLOAT(16,10)"}, //17
			//{"gpersection", "FLOAT(16,10)"}, //18
			{"RDF_ID_Region", "VARCHAR (38)"},//19
			{"RDF_ID_Substation", "VARCHAR (38)"}, //20
			{"RDF_ID_BaseVoltage", "VARCHAR (38)"}, //21
			{"RDF_ID_EquipmentContainer", "VARCHAR (38)"}, //22
			{"RDF_ID_GeneratingUnit", "VARCHAR (38)"},//23
			{"RDF_ID_RegulatingControl", "VARCHAR (38)"}, //24
			{"RDF_ID_PowerTransformer", "VARCHAR (38)"}, //25
			{"RDF_ID_BaseVoltage", "VARCHAR (38)"}, //26 - added to deal with CIM inconsistency
			{"RDF_ID_ConnectivityContainer", "VARCHAR (38)"}, //27 - for connectivity nodes
			{"RDF_ID_ConnectivityNode", "VARCHAR (38)"}, //28 - for connectivity nodes
			{"RDF_ID_ConductingEquipmentResource", "VARCHAR (38)"}, //29 - for conducting equipment resource
			{"RDF_ID_EquipmentContainer", "VARCHAR (38)"}}; //30 - EquipmentContainer-Find Voltage Level in a busbar

	
	
	// Equipment types
	private static String [] equip = {"BaseVoltage","Substation", "VoltageLevel", "GeneratingUnit", "SynchronousMachine", "RegulatingControl", 
			"PowerTransformer", "EnergyConsumer", "PowerTransformerEnd", "Breaker", "RatioTapChanger", "ConnectivityNode", "Terminal","BusbarSection" ,"ACLineSegment","LinearShuntCompensator" };  
	// Hash table of date required for each type
	private static int [][] dataIndex= {{0, 1}, 	// BaseVoltage							
			{0,	2,	17},				// Substation					
			{0,	2,	18,	19},			// VoltageLevel				
			{0,	2,	3,	4,	20},		// Gen Unit		
			{0,	2,	5, 16,  21,	22,	20}, 	// SynchMachine. Remove 6 and 7 because they are SSH. ALSO REMOVE BASE VOLTAGE RDF (15). NOT THERE??
			{0,	2},						// Reg Control - removed 14 SSH value 	
			{0,	2,	20},				// Power Tx		
			{0,	2,	20},				// Energy Con	 - Removed 15 ALSO NO BASE VOLTAGE . No 6 and 7 because SSH	
			{0,	2,	9,  10, 23,	24},	// Power Tx End 		
			{0,	2,	20},				// Breaker	- Removed 11 ALSO NO BASE VOLTAGE 	
			{0,	2}, 					// Ratio Tap Changer - Removed 12 SSH
			{0,	2, 25},					// Connectivity Node 
			{0, 2, 26, 27},				// Terminals
			{0, 2, 28}, 				// BusbarSection
			{0, 9, 10, 11},				// Lines
			{0,2, 22}};					// Linear Shunt Capacitor
					
	 				
	
	// Hash table for SSH data
	private static int [] [] dataSSHIndex =  {{}, 	// BaseVoltage							
			{},				// Substation					
			{},			// VoltageLevel				
			{},		// Gen Unit		
			{0,6,7,15}, // SynchMachine.
			{0,8,14},						// Reg Control 
			{},				// Power Tx		
			{0,6,7},				// Energy Con	 	
			{},	// Power Tx End 		
			{0,12},				// Breaker	
			{0,13},				// Ratio Tap Changer
			{},					//Conectivy Nodes
			{},					// Terminals
			{},						// BusbarSection
			{},					// AC Line Segment
			{}}; 				// Linear shunt capacitor
		
	protected Shell shlUserInterface;

	private String defaultFile = "No File Selected";
	private String eqFileName;
	private String sshFileName;
	private static String matPowerPath; //= "C:\\Users\\Callum\\Documents\\MATLAB\\matpower6.0";
	private boolean eqImported;
	private boolean sshImported;
	private boolean topProcessed;
	private boolean caseFileBuilt = false; //slight inconsistency in methods
	
	private static String baseS;
	private static String [][] yBus;
	
	private static String [][] busData;
	private static String [][] genData;
	private static String [][] branchData;
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public static void main(String[] args) {


		// Structure
		// Run allows EQ file to be added
		// Calls the EQfile parser which creates the elements and then creates a database
		// EQ file parser uses arraylists to hold the node lists and equipment lists. each equipment item has a type and string array of data. the information held in data is determined by type. 
		
		// two key symplifying assumptions made -
		//	1 - the tap changers arent used in transformers
		//	2 - that a shunt capacitor regulates to the required voltage
		//  3 - that the reference generator is connected. doesnt look for islanding
		// 4 -how is gen min and max Q calculated???
		// 5 - considers only the simple transformer model - no shunt impedance...
		
		// need to change shunt capacitors? look up cim documentation to understand how this works.....
		// so shunt compensators do have a regulating control but it needs to be enabled. It looks like if not enabled then it is static...
		
		// there is regulating control on the three shunt compensators and the synch gen
		// need to add this secondary processing to make sure bare requirements are included in database

		try {
			Run window = new Run();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	/*
	 * Method - Build case file for matpower to use
	 * 
	 * Description - uses the folder directory chosen from GUI. Writes the
	 * values from topology processing into format needed by matpower. Saves
	 * as file "casefile". Will overwrite file if in directory. 
	 * 
	 */
	
	public static boolean caseFileBuild() {
		
        try {
            File caseFile = new File(matPowerPath+"\\casefile.m"); // Saves to current directory
            FileOutputStream fs = new FileOutputStream(caseFile);
            OutputStreamWriter osw = new OutputStreamWriter(fs);    
            Writer w = new BufferedWriter(osw);
           
            w.write("function mpc = casefile \n mpc.version = '2'; \n mpc.baseMVA = "+ baseS +";");

            // bus data
            w.write("%% bus data \r\n %	bus_i	type	Pd      Qd		Gs      Bs	area	Vm	Va	baseKV	zone	Vmax	Vmin \r\n");
            w.write("mpc.bus = [ \r\n");
            for (int i=0; i <busData.length; i++) {
            	for (int c=0; c<busData[i].length; c++) {
            		w.write(busData[i][c] + " ");
            	}
            	w.write("; \r\n");
            }
            w.write("] \r\n");
            
            // gen data
            w.write("%% generator data \r\n %	bus	Pg	Qg	Qmax	Qmin	Vg	mBase	status	Pmax	Pmin	Pc1	Pc2	Qc1min	Qc1max	Qc2min	Qc2max	ramp_agc	ramp_10	ramp_30	ramp_q	apf \r\n");
            w.write("mpc.gen = [ \r\n");
            for (int i=0; i <genData.length; i++) {
            	for (int c=0; c<genData[i].length; c++) {
            		w.write(genData[i][c] + " ");
            	}
            	w.write("; \r\n");
            }
            w.write("] \r\n");
            
            // branch data
            w.write("%% branch data \r\n %	fbus	tbus	r	x	b	rateA	rateB	rateC	ratio	angle	status	angmin	angmax\r\n");
            w.write("mpc.branch= [ \r\n");
            for (int i=0; i <branchData.length; i++) {
            	for (int c=0; c<branchData[i].length; c++) {
            		w.write(branchData[i][c] + " ");
            	}
            	w.write("; \r\n");
            }
            w.write("] \r\n");
                      
            w.close();
            System.out.println("File Creation Successful");
            return true;
            
        } catch (IOException e) {
        	System.err.println("Problem writing the file");
        	return false;
        }
		
	}
	
	
	/*
	 * Method - Runs the powerflow calc from Matlab
	 * 
	 * Description - starts matlab asynchornously. Evaluates the function
	 * Returns a stringwriter so output can be printed to screen. 
	 * 
	 */
	public static Writer runPowerFlow() {
		
        Writer output = new StringWriter();
        Writer error =  new StringWriter();
		
		System.out.println("Starting Matlab");
	    try {
			// Start MATLAB asynchronously
			Future<MatlabEngine> eng = MatlabEngine.startMatlabAsync();
			// Get engine instance
			MatlabEngine ml = eng.get();
			
			// Evaluate the command to cd to your function
			ml.eval("cd "+matPowerPath);
			
			System.out.println("Calling function");
			// Evaluate the function
			ml.eval("runpf('casefile')", output, error);
			
			System.out.println("Successful Matlab Function");
			return output;
	    }
	    catch (EngineException e) {
	    	
	    	System.out.println("Matlab couldn't start");
			e.printStackTrace();
			return null;
	    }
	    catch (InterruptedException e) {
	    	// catch general interrupt exception
	    	e.printStackTrace();
	    	return null;
	    }
	    catch (ExecutionException e) {
	    	// catch general interrupt exception
	    	e.printStackTrace();
	    	return null;
	    }
	}
	    

	/*
	 * Method - Runs the ybus calc from Matlab
	 * 
	 * Description - starts matlab asynchornously. Evaluates the function
	 * Returns a stringwriter so output can be printed to screen. 
	 * 
	 */
	public static Writer runYbus() {
      
		Writer output = new StringWriter();
        Writer error =  new StringWriter();
		
		System.out.println("Starting Matlab");
	    try {
			// Start MATLAB asynchronously
			Future<MatlabEngine> eng = MatlabEngine.startMatlabAsync();
			// Get engine instance
			MatlabEngine ml = eng.get();
			
			// Evaluate the command to cd to your function
			ml.eval("cd " + matPowerPath);
			
			System.out.println("Calling function");
			// Evaluate the function
			ml.eval("makeYbus(casefile)", output, error);
			
			System.out.println("Successful Matlab Function");
			return output;
	    }
	    catch (EngineException e) {
	    	
	    	System.out.println("Matlab couldn't start");
			e.printStackTrace();
			return null; // possibly dont require. legacy remaining
	    }
	    catch (InterruptedException e) {
	    	// catch general interrupt exception
	    	e.printStackTrace();
	    	return null;
	    }
	    catch (ExecutionException e) {
	    	// catch general interrupt exception
	    	e.printStackTrace();
	    	return null;
	    }
	}
	

	/*
	 * Method - Creates an EQ parser and parses the EQ file
	 * 
	 * Description - 
	 * 
	 */
	public void eqImport() {
		try {
			File EQFile = new File(eqFileName);
			ParseEQ parserEQ = new ParseEQ(EQFile, equip, dataNames, dataIndex);
			eqImported = parserEQ.dbBuild(dbSetup);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	/*
	 * Method - Creates an SSH parser and parses the SSH file
	 * 
	 * Description - 
	 * 
	 */
	public void sshImport() {
		try{
			File SSHFile = new File(sshFileName);
			ParseSSH parserSSH = new ParseSSH(SSHFile, equip, dataNames, dataSSHIndex);
			sshImported = parserSSH.dbUpdate(dbSetup);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	/*
	 * Method - Creates a topology processer and processes the topology
	 * 
	 * Description - 
	 * 
	 */
	public void topProcess() {
		try{
			Topology topProcessor = new Topology(dbSetup,equip, dataNames,dataIndex, dataSSHIndex);
			
			topProcessor.buildJavaYBus();
			baseS = topProcessor.dbBuildtopology();
			Run.busData = topProcessor.busBuild();
			Run.genData = topProcessor.genBuild();
			Run.branchData = topProcessor.branchBuild();
			Run.yBus = topProcessor.buildJavaYBus(); 
			topProcessed = true;
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	
	/*
	 * Graphics - open the window
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlUserInterface.open();
		shlUserInterface.layout();
		while (!shlUserInterface.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/*
	 * Graphics - create contents in the window
	 */
	protected void createContents() {
		shlUserInterface = new Shell();
		shlUserInterface.setSize(450, 348);
		shlUserInterface.setText("User Interface");
		
		Button eqfiledialog = new Button(shlUserInterface, SWT.NONE);
		eqfiledialog.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		

		eqfiledialog.setBounds(16, 53, 123, 37);
		eqfiledialog.setText("Choose EQ File");
		
		Label eqfilelabel = new Label(shlUserInterface, SWT.BORDER | SWT.WRAP);
		eqfilelabel.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		eqfilelabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
		eqfilelabel.setBounds(145, 55, 139, 35);
		eqfilelabel.setText(defaultFile);
		
		Button eqimport = new Button(shlUserInterface, SWT.NONE);
		eqimport.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		eqimport.setBounds(290, 53, 123, 37);
		eqimport.setText("Import EQ");
		eqimport.setEnabled(false);
		
		Button sshfiledialog = new Button(shlUserInterface, SWT.NONE);
		sshfiledialog.setText("Choose SSH File");
		sshfiledialog.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		sshfiledialog.setEnabled(false);
		sshfiledialog.setBounds(16, 96, 123, 37);
		
		Label sshfilelabel = new Label(shlUserInterface, SWT.BORDER | SWT.WRAP);
		sshfilelabel.setText("No File Selected");
		sshfilelabel.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		sshfilelabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
		sshfilelabel.setBounds(145, 98, 139, 35);
		
		Button sshimport = new Button(shlUserInterface, SWT.NONE);
		sshimport.setText("Import SSH");
		sshimport.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		sshimport.setEnabled(false);
		sshimport.setBounds(290, 96, 123, 37);
		
		Label header = new Label(shlUserInterface, SWT.NONE);
		header.setFont(SWTResourceManager.getFont("Calibri", 12, SWT.BOLD));
		header.setBounds(128, 17, 155, 25);
		header.setText("EQ2745 - Assignment 1");
		
		Button topprocess = new Button(shlUserInterface, SWT.NONE);
		topprocess.setText("Process Topology");
		topprocess.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		topprocess.setEnabled(false);
		topprocess.setBounds(16, 139, 123, 37);
		
		Label topLabel = new Label(shlUserInterface, SWT.BORDER | SWT.WRAP);
		topLabel.setText("No equipment data");
		topLabel.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		topLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
		topLabel.setBounds(145, 141, 139, 35);
		
		Button viewTop = new Button(shlUserInterface, SWT.NONE);
		viewTop.setText("View Topology Data");
		viewTop.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		viewTop.setEnabled(false);
		viewTop.setBounds(290, 139, 123, 37);
		
		Label mpPathLbl = new Label(shlUserInterface, SWT.BORDER | SWT.WRAP);
		mpPathLbl.setText("Select Matpower Path");
		mpPathLbl.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		mpPathLbl.setBackground(SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));
		mpPathLbl.setBounds(145, 184, 139, 35);
		
		Button chooseMatPath = new Button(shlUserInterface, SWT.NONE);
		chooseMatPath.setText("Find Matpower \r\nDir");
		chooseMatPath.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		chooseMatPath.setEnabled(false);
		chooseMatPath.setBounds(16, 182, 123, 37);
		
		Button buildFile = new Button(shlUserInterface, SWT.NONE);
		buildFile.setText("Build File");
		buildFile.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		buildFile.setEnabled(false);
		buildFile.setBounds(290, 182, 123, 37);
		
		Label label = new Label(shlUserInterface, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setBounds(16, 225, 397, 2);
		
		Button helpButt = new Button(shlUserInterface, SWT.NONE);
		helpButt.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		helpButt.setBounds(385, 15, 26, 25);
		helpButt.setText("?");
		
		Button btnPF = new Button(shlUserInterface, SWT.NONE);
		btnPF.setText("Run Power Flow");
		btnPF.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		btnPF.setEnabled(false);
		btnPF.setBounds(16, 252, 123, 37);
		
		Button btnYBus = new Button(shlUserInterface, SWT.NONE);
		btnYBus.setText("Run Y Bus");
		btnYBus.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		btnYBus.setEnabled(false);
		btnYBus.setBounds(153, 252, 123, 37);
		
		Button btnRunJavaYbus = new Button(shlUserInterface, SWT.NONE);
		btnRunJavaYbus.setText("Run Java Y-Bus");
		btnRunJavaYbus.setFont(SWTResourceManager.getFont("Calibri", 9, SWT.NORMAL));
		btnRunJavaYbus.setEnabled(false);
		btnRunJavaYbus.setBounds(290, 252, 123, 37);
		
		Label lblMatpowerFunctions = new Label(shlUserInterface, SWT.NONE);
		lblMatpowerFunctions.setText("MatPower Functions - see Help");
		lblMatpowerFunctions.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.BOLD));
		lblMatpowerFunctions.setBounds(19, 233, 222, 13);
		
		Label label_1 = new Label(shlUserInterface, SWT.SEPARATOR | SWT.VERTICAL);
		label_1.setBounds(282, 225, 2, 64);
		
		Label lblJavaYbus = new Label(shlUserInterface, SWT.NONE);
		lblJavaYbus.setText("Java Y-Bus");
		lblJavaYbus.setFont(SWTResourceManager.getFont("Calibri", 10, SWT.BOLD));
		lblJavaYbus.setBounds(293, 233, 66, 13);

		eqfiledialog.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				FileDialog dialog = new FileDialog(shlUserInterface, SWT.OPEN);
				dialog.setFilterExtensions(new String [] {"*.xml"});
				dialog.setFilterPath("c:\\");
				eqFileName = dialog.open();
				if (eqFileName == null) {
					if(!eqImported) {
						eqfilelabel.setText(defaultFile);
					}
				}
				else if(!eqFileName.contains("EQ")){
				    JOptionPane.showMessageDialog(null, "EQ file name must contain EQ");
				    eqFileName = null;
					if(!eqImported) {
						eqfilelabel.setText(defaultFile);
					}
				}
				else {
					String [] dispName = eqFileName.split("\\\\");
					eqfilelabel.setText(dispName[dispName.length-1]);
					System.out.println(dispName[dispName.length-1]);
					eqimport.setEnabled(true);

				}

			}
			
		});
		
		sshfiledialog.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				FileDialog dialog = new FileDialog(shlUserInterface, SWT.OPEN);
				dialog.setFilterExtensions(new String [] {"*.xml"});
				dialog.setFilterPath("c:\\");
				sshFileName = dialog.open();
				if (sshFileName == null) {
					if(!sshImported) {
						sshfilelabel.setText(defaultFile);
					}
				}
				else if(!sshFileName.contains("SSH")){
				    JOptionPane.showMessageDialog(null, "SSH file name must contain SSH");
				    sshFileName = null;
					sshfilelabel.setText(defaultFile);
				}
				else {
					String [] dispName = sshFileName.split("\\\\");
					sshfilelabel.setText(dispName[dispName.length-1]);
					System.out.println(dispName[dispName.length-1]);
					sshimport.setEnabled(true);

				}

			}
			
		});
		
		eqimport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sshImported = false;
				topProcessed = false;
				eqImport();
				if (!eqImported) {
				    JOptionPane.showMessageDialog(null, "Import failed - Please retry");
				    eqFileName = null;
					eqfilelabel.setText(defaultFile);
				}
				else {
					eqfilelabel.setText("Successful Import");
					sshfiledialog.setEnabled(true);
					eqimport.setEnabled(false);
					viewTop.setEnabled(false);
					topprocess.setEnabled(false);
					buildFile.setEnabled(false);
					chooseMatPath.setEnabled(false);
					mpPathLbl.setText("Select Matpower Path");
			    	sshfilelabel.setText(defaultFile);
			    	btnRunJavaYbus.setEnabled(false);
					btnPF.setEnabled(false);
					btnYBus.setEnabled(false);
				}
			}
		});
		
		sshimport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				topProcessed = false;
				if(eqImported) {
					sshImport();
					if (!sshImported) {
				    	JOptionPane.showMessageDialog(null, "Import failed - Please retry");
				    	sshFileName = null;
				    	sshfilelabel.setText(defaultFile);
					}
					else {
						sshfilelabel.setText("Successful Import");
						sshimport.setEnabled(false);
						topprocess.setEnabled(true);
						buildFile.setEnabled(false);
						viewTop.setEnabled(false);
						chooseMatPath.setEnabled(false);
						btnRunJavaYbus.setEnabled(false);
						mpPathLbl.setText("Select Matpower Path");
						btnPF.setEnabled(false);
						btnYBus.setEnabled(false);
					}
				}
				else {
					JOptionPane.showMessageDialog(null, "Cannot confirm database - please reload EQ");
					eqfilelabel.setText(defaultFile);
					sshfilelabel.setText(defaultFile);
					sshimport.setEnabled(false);
					eqimport.setEnabled(false);
					topprocess.setEnabled(false);
					eqFileName = null;
					sshFileName = null;
				}
			}
		});
		
		topprocess.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(sshImported) {
					topProcess();
					if (!topProcessed) {
				    	JOptionPane.showMessageDialog(null, "Topology processing failed - Please retry");
				    	topLabel.setText("Please retry");
					}
					else {
						topLabel.setText("Successfully Processed");
						topprocess.setEnabled(true);
						viewTop.setEnabled(true);
						chooseMatPath.setEnabled(true);
						btnRunJavaYbus.setEnabled(true);
					}
				}
				else {
					JOptionPane.showMessageDialog(null, "Cannot confirm database - please reload EQ");
					eqfilelabel.setText(defaultFile);
					sshfilelabel.setText(defaultFile);
					sshimport.setEnabled(false);
					eqimport.setEnabled(false);
					topprocess.setEnabled(false);
					eqFileName = null;
					sshFileName = null;
				}
			}
	
		});
	
		viewTop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {			
				// there is some duplication with the casefile builder but considered easier to replicate as not a critical function
				String topText = "";

	            // bus data
	            topText = topText +("bus data \r\n 	bus_i	type	Pd      Qd		Gs      Bs	area	Vm	Va	baseKV	zone	Vmax	Vmin \r\n");
	            topText = topText +("bus = [ \r\n");
	            for (int i=0; i <busData.length; i++) {
	            	for (int c=0; c<busData[i].length; c++) {
	            		topText = topText +(busData[i][c] + "\t\t");
	            	}
	            	topText = topText +("; \r\n");
	            }
	            topText = topText +("] \r\n");
	            
	            // gen data
	            topText = topText +("generator data \r\n 	bus	Pg	Qg	Qmax	Qmin	Vg	mBase	status	Pmax	Pmin	Pc1	Pc2	Qc1min	Qc1max	Qc2min	Qc2max	ramp_agc	ramp_10	ramp_30	ramp_q	apf \r\n");
	            topText = topText +("gen = [ \r\n");
	            for (int i=0; i <genData.length; i++) {
	            	for (int c=0; c<genData[i].length; c++) {
	            		topText = topText +(genData[i][c] + "\t\t");
	            	}
	            	topText = topText +("; \r\n");
	            }
	            topText = topText +("] \r\n");
	            
	            // branch data
	            topText = topText +"branch data \r\n 	fbus	tbus	r	x	b	rateA	rateB	rateC	ratio	angle	status	angmin	angmax\r\n";
	            topText = topText +"branch= [ \r\n";
	            for (int i=0; i <branchData.length; i++) {
	            	for (int c=0; c<branchData[i].length; c++) {
	            		topText = topText +(branchData[i][c] + "\t\t");
	            	}
	            	topText = topText +("; \r\n");
	            }
	            topText = topText + "] \r\n";
				
				if(topProcessed) {
					try {
						DataViewer topViewer = new DataViewer();
						topViewer.open(topText);
					} catch (Exception f) {
						f.printStackTrace();
					}
				}
			}
		});
		
		chooseMatPath.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				try {
					JFileChooser chooser = new JFileChooser();
					chooser.setDialogTitle("Choose MatPower Directory");
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					chooser.setAcceptAllFileFilterUsed(false);
					if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
						matPowerPath = chooser.getSelectedFile().getCanonicalPath();
					}
	
				    // ideally would check that the powerflow mfile is in this directory...
					if (matPowerPath != null) {
						mpPathLbl.setText(matPowerPath);
						buildFile.setEnabled(true);
					}
				}
				catch (IOException mpath){
					JOptionPane.showMessageDialog(null, "Error occurred - please retry");
					mpath.printStackTrace();
				}
			}			
		});
	
		buildFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (topProcessed) {
					caseFileBuilt = caseFileBuild();
					if (!caseFileBuilt) {
						JOptionPane.showMessageDialog(null, "File Build Failed - Please retry");
						mpPathLbl.setText("Please retry");
					} else {
						mpPathLbl.setText("Casefile Built!");
						buildFile.setEnabled(false);
						btnPF.setEnabled(true);
						btnYBus.setEnabled(true);
						
					}
				} else {
					JOptionPane.showMessageDialog(null, "Cannot confirm topology - please reload EQ");
					eqfilelabel.setText(defaultFile);
					sshfilelabel.setText(defaultFile);
					sshimport.setEnabled(false);
					eqimport.setEnabled(false);
					topprocess.setEnabled(false);
					buildFile.setEnabled(false);
					viewTop.setEnabled(false);
					chooseMatPath.setEnabled(false);
					mpPathLbl.setText("Select Matpower Path");
					eqFileName = null;
					sshFileName = null;
				}
			}
		});
		
		helpButt.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					Help helpViewer = new Help();
					helpViewer.open();
				} catch (Exception f) {
					f.printStackTrace();
				}
			}
		});
		
		btnPF.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Writer output = runPowerFlow();
				
				String pfText;
				if (output !=null) {
					// 	Create window
					pfText = output.toString();
					try {
						DataViewer pfViewer = new DataViewer();
						pfViewer.open(pfText);
					} catch (Exception f) {
						f.printStackTrace();
					}
				}
				else {
					JOptionPane.showMessageDialog(null, "Error Occurred - Please retry");
				}
			}
		});
		
		
		btnYBus.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Writer output = runYbus();
				
				String ybusText;
				if (output !=null) {
					// 	Create window
					ybusText = output.toString();
					try {
						DataViewer ybusViewer = new DataViewer();
						ybusViewer.open(ybusText);
					} catch (Exception f) {
						f.printStackTrace();
					}
				}
				else {
					JOptionPane.showMessageDialog(null, "Error Occurred - Please retry");
				}
				
			}
		});
		btnRunJavaYbus.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (topProcessed) {
					
					String text = "This is the Y-bus built from Java in per unit values. \r\n Ybus = \r\n";
					//Convert 2D yBus to string
					StringJoiner sj = new StringJoiner(System.lineSeparator());
					for (String[] row : yBus) {
					    sj.add(Arrays.toString(row));
					}
					text = text + sj.toString();
					
					try {
						DataViewer ybusViewer = new DataViewer();
						ybusViewer.open(text);
					} catch (Exception f) {
						f.printStackTrace();
					}
				}
			}
		});
	}
}

	