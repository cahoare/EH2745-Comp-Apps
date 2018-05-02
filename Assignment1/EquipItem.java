

import javax.xml.parsers.DocumentBuilderFactory; // Related to HTML file doc parsing
import javax.xml.parsers.DocumentBuilder; 
import org.w3c.dom.Document; 
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;


public class EquipItem {
	
	public String [] data; //actual data for object
	private Node item; //passed node from db builder
	public int type; //passed type of object. known from search. 	
		
	
	public EquipItem(Node item, int type, String [] equipNames) {

		this.item = item; //passed node
		this.type = type; //passed type

		dataGen(equipNames);
		
	}
	
	private void dataGen(String [] equipNames) {	
		
		
		// data types
		String [] dataNames= {"rdf:ID", //0
				equipNames[type]+"."+"nominalVoltage", //1
				"IdentifiedObject.name", //2
				equipNames[type]+"."+"maxOperatingP", //3 
				equipNames[type]+"."+"minOperatingP", //4
				"RotatingMachine.ratedS", //5
				"P", //6 - SSH values
				"Q", //7 - SSH Values
				"targetValue", //8 - SSH values
				equipNames[type]+"."+"r", //9
				equipNames[type]+"."+"x", //10
				"state", //11 - SSH values
				"step", //12 - SSH values
				equipNames[type]+"."+"Region", //13
				equipNames[type]+"."+"Substation", //14
				equipNames[type]+"."+"BaseVoltage", //15
				"Equipment.EquipmentContainer", //16
				"RotatingMachine.GeneratingUnit", //17
				"RegulatingCondEq.RegulatingControl", //18
				equipNames[type]+"."+"PowerTransformer", //19
				"TransformerEnd.BaseVoltage"}; //20 - added to deal with CIM inconsistency
		
		// Hash table of date required for each type
		int [][] dataIndex= {{0, 1}, 	// BaseVoltage							
				{0,	2,	13},				// Substation					
				{0,	2,	14,	15},			// VoltageLevel				
				{0,	2,	3,	4,	16},		// Gen Unit		
				{0,	2,	5,  17,	18,	16}, // SynchMachine. Remove 6 and 7 because they are SSH. ALSO REMOVE BASE VOLTAGE RDF (15). NOT THERE??
				{0,	2},						// Reg Control - removed 14 SSH value 	
				{0,	2,	16},				// Power Tx		
				{0,	2,	16},				// Energy Con	 - Removed 15 ALSO NO BASE VOLTAGE . No 6 and 7 because SSH	
				{0,	2,	9,  10, 19,	20},	// Power Tx End 		
				{0,	2,	16},				// Breaker	- Removed 15 ALSO NO BASE VOLTAGE 	
				{0,	2}}; 				// Ratio Tap Changer - Removed 12 SSH
		
		data = new String[dataIndex[type].length]; //init the string size based on type
		String dataNameHold = ""; //hold term for search string. used in exception handling /debugging

		
		try {
		
			Element element = (Element) item; //need to cast the node back to element to restore the attributes		
			data [0] = element.getAttribute("rdf:ID"); // all items have rdf:ID 

			
			for (int i=1; i<dataIndex[type].length;i++) { //for all required data types for element
				
				dataNameHold = "cim:"+dataNames[dataIndex[type][i]]; //set up search term
			
				if (dataIndex[type][i]<13){ // if it is not an rdf resource attribute - find the element text
					data[i] = element.getElementsByTagName(dataNameHold).item(0).getFirstChild().getNodeValue(); //the text gets stored as a node as well in DOM. Need to getElements by tag name
				}
				else { //if it is an rdf attribute. find the attribute data
					Element subElement = (Element) element.getElementsByTagName(dataNameHold).item(0);
					data[i] = subElement.getAttribute("rdf:resource");
				}
				
			}
		}
		catch (NullPointerException e){ //if the element can't be found in the DOM then skip but print 
		
			System.out.println("Error - search term doesn't exist in model: "+dataNameHold); //debug
			e.printStackTrace();
		}
		
	}
	
}
