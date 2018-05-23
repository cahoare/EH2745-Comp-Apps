package Project;

public class Escenario {
	// Define the variable that define the structure
		
		
		String name_busbar;
		String time_estamp;
		double voltage_value;
		double voltage_angle;
		String substation_rdfID; 
		
		//Build the constructor (input of variables)
		public Escenario (String name, String time, double voltage, double angle, String rdfID){
			
			
			String[] parts = name.split("_");
			name_busbar = parts[0]; 
			time_estamp=time;
			voltage_value=voltage;
			voltage_angle=angle;
			substation_rdfID=rdfID; 
		}
		
		public Double toArray(){
		
		return voltage_angle; }
		
		public Double getAngle(){
			return voltage_angle;
		}
		
		public Double getVoltage(){
			return voltage_value;
		}
		
		public String getTimeestamp(){
			return time_estamp;
		}
		
		public String getName(){
			return name_busbar;
		}
		
		public String getrdfID(){
			return substation_rdfID;
		}
		
		public void replaceAngle(double real_angle){
			voltage_angle=real_angle;
		}
			
}
