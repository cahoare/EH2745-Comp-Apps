

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
	private int [][] dataIndex;	
	
	public EquipItem(Node item, int type, String [] equipNames, int [][] dataIndex) {

		this.item = item; //passed node
		this.type = type; //passed type
		this.dataIndex = dataIndex; //passed hash table 
		dataGen(equipNames);
		
	}
	
	private void dataGen(String [] equipNames) {	
		
		
		// data types
		String [] dataNames= {"rdf:ID", //0
				equipNames[type]+"."+"nominalVoltage",	//1
				"IdentifiedObject.name", 				//2
				equipNames[type]+"."+"maxOperatingP", 	//3 
				equipNames[type]+"."+"minOperatingP", 	//4
				"RotatingMachine.ratedS", 				//5
				"P", 									//6 - SSH values
				"Q", 									//7 - SSH Values
				"targetValue", 							//8 - SSH values
				equipNames[type]+"."+"r", 				//9
				equipNames[type]+"."+"x", 				//10
				equipNames[type]+"."+"bch", 			//11
				"state", 								//12 - SSH values
				"step", 								//13 - SSH values
				"enabled",								//14
				equipNames[type]+"."+"Region", 			//15
				equipNames[type]+"."+"Substation", 		//16
				equipNames[type]+"."+"BaseVoltage", 	//17
				"Equipment.EquipmentContainer", 		//18
				"RotatingMachine.GeneratingUnit",		//19
				"RegulatingCondEq.RegulatingControl", 	//20
				equipNames[type]+"."+"PowerTransformer", //21
				"TransformerEnd.BaseVoltage",			//22 - added to deal with CIM inconsistency
				equipNames[type]+"."+"ConnectivityNodeContainer", //23
				equipNames[type]+"."+"ConnectivityNode", //24 
				equipNames[type]+"."+"ConductingEquipment",		//25
				"Equipment.EquipmentContainer"}; //26
				
				 
	
		
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
