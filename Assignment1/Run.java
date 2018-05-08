import java.io.File;

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
import javax.swing.JOptionPane;
import java.sql.*;

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
	
		
	protected Shell shlUserInterface;

	
/**
 * Test variables
 */
	private String defaultFile = "No File Selected";
	private String eqFileName;
	private String sshFileName;
	private boolean eqImported;
	private boolean sshImported;
	private boolean topProcessed;
	
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public static void main(String[] args) {


		// Structure
		// Run allows EQ file to be added
		// Calls the EQfile parser which creates the elements and then creates a database
		// EQ file parser uses arraylists to hold the node lists and equipment lists. each equipment item has a type and string array of data. the information held in data is determined by type. 
		
	
		try {
			Run window = new Run();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
			
	}

	public void eqImport() {
		try {
			File EQFile = new File("Assignment_EQ_reduced.xml");
			ParseEQ parserEQ = new ParseEQ(EQFile, equip, dataNames, dataIndex);
			eqImported = parserEQ.dbBuild(dbSetup);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	
	public void sshImport() {
		try{
			File SSHFile = new File("Assignment_SSH_reduced.xml");
			ParseSSH parserSSH = new ParseSSH(SSHFile, equip, dataNames, dataSSHIndex);
			sshImported = parserSSH.dbUpdate(dbSetup);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	
	/**
	 * Open the window.
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

	
	protected void createContents() {
		shlUserInterface = new Shell();
		shlUserInterface.setSize(450, 300);
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
				}
			}
		});
		
		sshimport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
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
					}
				}
				else {
					JOptionPane.showMessageDialog(null, "Cannot confirm database - please reload EQ");
					eqfilelabel.setText(defaultFile);
					sshfilelabel.setText(defaultFile);
					sshimport.setEnabled(false);
					eqimport.setEnabled(false);
					eqFileName = null;
					sshFileName = null;
				}
			}
		});
		
		
	}
}

	