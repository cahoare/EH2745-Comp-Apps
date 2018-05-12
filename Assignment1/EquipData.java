import javax.xml.parsers.DocumentBuilderFactory; // Related to HTML file doc parsing
import javax.xml.parsers.DocumentBuilder; 
import org.w3c.dom.Document; 
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;


public class EquipData {
	
	public String [] data; //actual data for object
	private Node item; //passed node from db builder
	public int type; //passed type of object. known from search. 	
		
	
	public EquipData(Node item, int type, String [] equipNames) {

		this.item = item; //passed node
		this.type = type; //passed type

		dataGen(equipNames);
		
	}
	
	/**
	 * 
	 * @param equipNames
	 */
	private void dataGen(String [] equipNames) {	
		
		// equipment where SSH data stored
		//private static int [] sshEquip = {4,5,7,9,10};
		
		String [] dataNameHold = {"cim:RotatingMachine.p","cim:RotatingMachine.q","cim:RegulatingControl.targetValue", "cim:EnergyConsumer.p", "cim:EnergyConsumer.q", "cim:Switch.open", "cim:TapChanger.step", "cim:RegulatingControl.enabled","cim:SynchronousMachine.referencePriority"};  //hold term for search string. used in exception handling /debugging
		
	
		try {
			
			Element element = (Element) item; //need to cast the node back to element to restore the attributes		
			
			switch(type) {
			case 4: //Synch machine
				data = new String[4];
				data[0] = element.getAttribute("rdf:about"); // all items have rdf:ID 
				data[1] = element.getElementsByTagName(dataNameHold[0]).item(0).getFirstChild().getNodeValue();
				data[2] = element.getElementsByTagName(dataNameHold[1]).item(0).getFirstChild().getNodeValue();
				data[3] = element.getElementsByTagName(dataNameHold[8]).item(0).getFirstChild().getNodeValue();
				break;
			case 5: //Reg control
				data = new String[3];
				data[0] = element.getAttribute("rdf:about"); // all items have rdf:ID 
				data[1] = element.getElementsByTagName(dataNameHold[2]).item(0).getFirstChild().getNodeValue();
				if(element.getElementsByTagName(dataNameHold[7]).item(0).getFirstChild().getNodeValue().equals("true")) {
					data[2] = "1"; //Switched breaker status around. 1 is closed, 0 is open. Saved as bool in db
				}
				else {
					data[2] = "0"; 
					System.out.println(element.getElementsByTagName(dataNameHold[7]).item(0).getFirstChild().getNodeValue() );
				}
				break;
			case 7: //Energy con
				data = new String[3];
				data[0] = element.getAttribute("rdf:about"); // all items have rdf:ID 
				data[1] = element.getElementsByTagName(dataNameHold[3]).item(0).getFirstChild().getNodeValue();
				data[2] = element.getElementsByTagName(dataNameHold[4]).item(0).getFirstChild().getNodeValue();
				break;
			case 9: //Breaker
				data = new String[2];
				data[0] = element.getAttribute("rdf:about"); // all items have rdf:ID 
				if(element.getElementsByTagName(dataNameHold[5]).item(0).getFirstChild().getNodeValue().equals("true")) {
					data[1] = "0"; //Switched breaker status around. 1 is closed, 0 is open. Saved as bool in db
				}
				else {
					data[1] = "1"; 
				}
				break;
			case 10: //Tap changer
				data = new String[2];
				data[0] = element.getAttribute("rdf:about"); // all items have rdf:ID 
				data[1] = element.getElementsByTagName(dataNameHold[6]).item(0).getFirstChild().getNodeValue();
				break;
			}
		}
		catch (NullPointerException e){ //if the element can't be found in the DOM then skip but print 
		
			System.out.println("Error - search term doesn't exist in model"); //debug
			e.printStackTrace();
		}
		
	}
	
}
