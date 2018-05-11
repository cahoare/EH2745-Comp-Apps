
package code;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.w3c.dom.NodeList;
public class Topology {
	// For working with complex
	private double re = 0;   // the real part
    private double im = 0;   // the imaginary part
    private static double loadre=0;	// the real part of the load
    private static double loadim=0; // the imaginary part of the load
	// JDBC driver name and database URL - passed from Run
		static String JDBC_DRIVER; 
		static String DB_URL; 
		static String DB_NAME;
		
		// Database credentials - passed from Run
		static String USER; 
		static String PASS;
		//BUSBAR INFO
		private static ArrayList<String> busbar_name = new ArrayList<String>();
		private static ArrayList<String> busbar_rdfID = new ArrayList<String>();
		private static ArrayList<String> busbar_voltageref = new ArrayList<String>();
		//VOLTAGE LEVELS
		private static ArrayList<String> voltagelevel_rdfID = new ArrayList<String>(); // identification
		private static ArrayList<String> voltagelevel_value = new ArrayList<String>(); // value
		//TERMINALS AND CONNECT. NODES INFO
		private static ArrayList<String> rdfID_resource = new ArrayList<String>();
		private static ArrayList<String> common_connectnode = new ArrayList<String>();
		//LOADS
		private static ArrayList<String> rdfID_load = new ArrayList<String>();
		private static ArrayList<String> p_load = new ArrayList<String>();
		private static ArrayList<String> q_load = new ArrayList<String>();
		//SHUNTS
		private static ArrayList<String> rdfID_shunt = new ArrayList<String>();
		private static ArrayList<String> q_shunt = new ArrayList<String>();
		//BREAKERS
		private static ArrayList<String> rdfID_breaker = new ArrayList<String>();
		private static ArrayList<String> state_breaker = new ArrayList<String>();
		// LINES
		private static ArrayList<String> rdfID_line = new ArrayList<String>();
		private static ArrayList<String> R_line = new ArrayList<String>();
		private static ArrayList<String> X_line = new ArrayList<String>();
		private static ArrayList<String> bsh_line = new ArrayList<String>();
		//TRANSFORMER
		private static ArrayList<String> rdfID_transformer = new ArrayList<String>();
		private static ArrayList<String> R_transformer = new ArrayList<String>();
		private static ArrayList<String> X_transformer = new ArrayList<String>();
		
		
		static String found;
		
		private static String toString(Double re, Double im) {
	        if (im == 0) return re + "";
	        if (re == 0) return im + "i";
	        if (im <  0) return re + " - " + (-im) + "i";
	        return re + " + " + im + "i";
	    }
		private static Double LoadR(Double voltage, String p){
			loadre=voltage*voltage/Double.parseDouble(p);
			return loadre;
		}
		
		private static Double LoadI(Double voltage, String q){
			loadim=voltage*voltage/Double.parseDouble(q);
			return loadim;
		}
		private static Double divisionre(Double Re, Double Reac){
			return (Re)/(Re*Re+Reac*Reac);
		}
		private static Double divisionim(Double Re, Double Reac){
			return (-Reac)/(Re*Re+Reac*Reac);
		}
		
		public static void BranchesCalculation(ArrayList<ArrayList<String>> Matrixf,ArrayList<ArrayList<String>> Branchesf,ArrayList<String> rdfIDbreaker,ArrayList<String> statebreaker,
        		ArrayList<String> rdfIDresource,ArrayList<String> busbarrdfID, ArrayList<String> commonconnectnode, String foundnode, int elements, String bus){
			int elementsfound=0;
        	int busbarfound=0;
        	
        	System.out.println(foundnode);
        	System.out.println(bus);
        	ArrayList<String> components_rdf = new ArrayList<String>() ;
        	for(int i=0; i<rdfIDresource.size();i++){
        		if(foundnode.equals(commonconnectnode.get(i))&&!bus.equals(rdfIDresource.get(i))){
        			elementsfound=elementsfound+1; // number of elements connected to the same node	
        			components_rdf.add(rdfIDresource.get(i));
        			System.out.println(rdfIDresource.get(i));}	
        		else{}}
        	System.out.println("We have measure that there are a number of elements connected ");
        	System.out.println(elementsfound);
			if(Matrixf.size()==1){
				for(int i=0; i<elementsfound;i++){ // We create the auxiliary branches in the matrix
					ArrayList<String> new_auxiliar_branch = new ArrayList<String>() ;
					new_auxiliar_branch.add(Matrixf.get(0).get(0));
					new_auxiliar_branch.add(components_rdf.get(i));
					System.out.println("The Matrix Before the new elements");
					System.out.println(Matrixf);
					Matrixf.add(new_auxiliar_branch);
					System.out.println("The Matrix Before the afterelements");
					System.out.println(Matrixf);
					
					System.out.println(new_auxiliar_branch);
				}
		
				}
				else{
					for(int i=0; i<elementsfound;i++){
				System.out.println(elements);
				ArrayList<String> new_auxiliar_branch_two = new ArrayList<String>() ;
				new_auxiliar_branch_two=Matrixf.get(Matrixf.size()-elements-i);
				new_auxiliar_branch_two.add(components_rdf.get(i));
				Matrixf.add(new_auxiliar_branch_two);
				
				}}
				System.out.println("We just copied the branches from the previous bus, the result is:");
				System.out.println(Matrixf);
			
			
				for(int b=0; b<elementsfound;b++){
				System.out.println("To see if we get inside the loop with b");
			for(int a=0; a<busbarrdfID.size();a++){	
				if(busbarrdfID.get(a).equals(components_rdf.get(b))){//end of the branch
					Branchesf.add(Matrixf.get(Matrixf.size()-elementsfound+b));
					 Matrixf.remove(Matrixf.size()-elementsfound+b);
					 busbarfound=busbarfound+1;	
					 System.out.println("End of a branch");	 }
					 else{}
				}
				// if it is not the end of the branch we need to look for the new connectivity node
					 // if it is an open breaker we delete that branch too
			System.out.println(rdfIDresource.size());
					for(int c=0;c<rdfIDresource.size();c++){
						System.out.println("To see if we get inside the loop with c");
						if(rdfIDresource.get(c).equals(components_rdf.get(b))&&!commonconnectnode.get(c).equals(foundnode)){
							foundnode=commonconnectnode.get(c);
							System.out.println("We just found a new node connected to the elements");	
							//we have found the new node of connection
							if(rdfIDbreaker.indexOf(components_rdf.get(b))!=-1&& (int)Double.parseDouble(statebreaker.get(rdfIDbreaker.indexOf(components_rdf.get(b))))!=1)
							{
								// We have detected an open breaker!!
								// We perform as if we had found a endbus. We delete this branch.
								Matrixf.remove(Matrixf.size()-elementsfound+b);
								busbarfound=busbarfound+1;	
								System.out.println("We just found an open breaker");	
							}
							else{
								
								
								
							
								BranchesCalculation(Matrixf, Branchesf,rdfIDbreaker, statebreaker,
					        		rdfIDresource, busbarrdfID, commonconnectnode,  foundnode, elementsfound-busbarfound,bus);}
							System.out.println("We arrive till here");

						
						}else{}
					}
					
					
				   
				
			}
        	}
        	
        public static void YmatrixCalculation( ArrayList<ArrayList<String>> Branchesy,ArrayList<String> rdfIDbreaker,
        		ArrayList<String> rdfIDtransformer,ArrayList<String> rdfIDline,ArrayList<String> statebreaker,
        		ArrayList<String> Rline,ArrayList<String> Xline, ArrayList<String> Rtransformer, ArrayList<String> Xtransformer ,ArrayList<String> bshline,Double[][] Ymatrixre,Double[][] Ymatrixim,
        		ArrayList<String> busrdfid, String bus ){
        	int branchlength=Branchesy.size();
        	int rowy=0;
        	int endbus=0;
        	int nlines=rdfIDline.size();
        	int nbreaker=rdfIDbreaker.size();
        	int ntransformer=rdfIDtransformer.size();
        	ArrayList<ArrayList<Double>> Realpart = new ArrayList<ArrayList<Double>>();
        	ArrayList<ArrayList<Double>> Imaginarypart = new ArrayList<ArrayList<Double>>();
        	ArrayList<Double> Realpart_auxiliar_array =new ArrayList<Double>();
        	ArrayList<Double> Imaginarypart_auxiliar_array =new ArrayList<Double>();
        	Double Realpart_auxiliar =0.0;
        	Double Imaginarypart_auxiliar =0.0;
        	Double parallel_re = 0.0;
        	Double parallel_im = 0.0;
        	
        	ArrayList<String> Endbus_rdf = new ArrayList<String>(); // WIth this we check if there are parallel branches
        	
        	for(int i=0;i<busrdfid.size();i++){
        		if(busrdfid.get(i).equals(bus)){//Then we are working on the row rowy of the Y matrixes
        			rowy=i;
        		}
        	}
        	for(int i=0;i<Branchesy.size();i++){
        		Realpart_auxiliar =0.0;
        		Imaginarypart_auxiliar =0.0;
        		for(int j=0;j<Branchesy.get(i).size();j++){
        			System.out.println("did we get in?");
        			System.out.println(Branchesy.get(i).get(j));
        			
        			if(rdfIDbreaker.indexOf(Branchesy.get(i).get(j))!=-1){// if its -1 it means it's not in that array
        				//we have a breaker// We don't really do anything. The open breakers are deleted before
        				//if(rdfIDbreaker.indexOf(Branchesy.get(i).get(j))!=0.000){}
        				//else{ClosedBreaker.remove(i); ClosedBreaker.add(false); // This is an open breaker}
        				System.out.println("We have a breaker breaker");
        			}
        			if(rdfIDline.indexOf(Branchesy.get(i).get(j))!=-1){
        				Realpart.get(i).add(Double.parseDouble(Rline.get(rdfIDline.indexOf(Branchesy.get(i).get(j)))));
        				Imaginarypart.get(i).add(Double.parseDouble(Rline.get(rdfIDline.indexOf(Branchesy.get(i).get(j)))));
        				Ymatrixim[rowy][rowy]=Double.parseDouble(bshline.get(rdfIDline.indexOf(Branchesy.get(i).get(j))));
        				Imaginarypart_auxiliar=Imaginarypart_auxiliar+Realpart.get(i).get(j); // Series resistance in the branch j
        				Realpart_auxiliar=Realpart_auxiliar+Imaginarypart.get(i).get(j);// Series reactance in the branch j
        				System.out.println("We have a line");
        			}			
        			if(rdfIDtransformer.indexOf(Branchesy.get(i).get(j))!=-1){
        				//we have a transformer
        				Realpart.get(i).add(Double.parseDouble(Rtransformer.get(rdfIDtransformer.indexOf(Branchesy.get(i).get(j)))));
        				Imaginarypart.get(i).add(Double.parseDouble(Rtransformer.get(rdfIDtransformer.indexOf(Branchesy.get(i).get(j)))));
        				Imaginarypart_auxiliar=Imaginarypart_auxiliar+Realpart.get(i).get(j); // Series resistance in the branch j
        				Realpart_auxiliar=Realpart_auxiliar+Imaginarypart.get(i).get(j);// Series reactance in the branch j
        				System.out.println("We have a transformer");
        			}
        			if(j==Branchesy.get(i).size()-1){// End of this branch
        				for(int p=0;p<busrdfid.size();p++){
        	        		if(busrdfid.get(p).equals(Branchesy.get(i).get(j))){//Then we are working on the column p of the Y matrixes //        	        			for(int f=0; f<Realpart.get(i).size();f++){
        	        			// The end busbar is the p one of the busrdfid vector
        	        			Endbus_rdf.add(Branchesy.get(i).get(j)); // this is the end bus in the i branch
        	        			Realpart_auxiliar_array.add(Realpart_auxiliar);
        	        			Imaginarypart_auxiliar_array.add(Imaginarypart_auxiliar);
        	        			
        	        		}
        	        	}
        				
        				for(int r=0; r<Endbus_rdf.size()-1;r++){ // this is to check if there are parallel lines and calculate it

        					if(Endbus_rdf.get(r).equals(Endbus_rdf.get(Endbus_rdf.size()-1))){// with this you compare each end bus already found with the one found now
        					// The r branch is in parallel with the last one calculated // which is the i branch
        						parallel_re=division_re(times_re(Realpart_auxiliar_array.get(r),Imaginarypart_auxiliar_array.get(r),Realpart_auxiliar_array.get(i),Imaginarypart_auxiliar_array.get(i)),times_im(Realpart_auxiliar_array.get(r),Imaginarypart_auxiliar_array.get(r),Realpart_auxiliar_array.get(i),Imaginarypart_auxiliar_array.get(i)),Realpart_auxiliar_array.get(r)+Realpart_auxiliar_array.get(i),Imaginarypart_auxiliar_array.get(r)+Imaginarypart_auxiliar_array.get(i));
        						parallel_im=division_im(times_re(Realpart_auxiliar_array.get(r),Imaginarypart_auxiliar_array.get(r),Realpart_auxiliar_array.get(i),Imaginarypart_auxiliar_array.get(i)),times_im(Realpart_auxiliar_array.get(r),Imaginarypart_auxiliar_array.get(r),Realpart_auxiliar_array.get(i),Imaginarypart_auxiliar_array.get(i)),Realpart_auxiliar_array.get(r)+Realpart_auxiliar_array.get(i),Imaginarypart_auxiliar_array.get(r)+Imaginarypart_auxiliar_array.get(i));
        						System.out.println("HIjo puta hasta aqui funciona");
        						Realpart_auxiliar_array.set(r, parallel_re); // we replace the value we had in the r branch
        						Imaginarypart_auxiliar_array.set(r,parallel_im); 
        						// Now we can also delete the one we have used for the parallel not to repeat this calculation;
        						Endbus_rdf.remove(Endbus_rdf.get(Endbus_rdf.size()-1));
        						Realpart_auxiliar_array.remove(Realpart_auxiliar_array.get(Realpart_auxiliar_array.size()-1));
        						Imaginarypart_auxiliar_array.remove(Imaginarypart_auxiliar_array.get(Imaginarypart_auxiliar_array.size()-1));
        					
        					}
        				}
        			}
        			
        			
        		}	
        	}
        	
        	for(int x=0; x<Endbus_rdf.size(); x++){
        		for(int y=0; y<busrdfid.size(); y++){
        		if(Endbus_rdf.get(x)==busrdfid.get(y)){
        			Ymatrixim[rowy][y]=Imaginarypart_auxiliar_array.get(x);
        			Ymatrixre[rowy][y]=Imaginarypart_auxiliar_array.get(x);
        		}
        	}
        	}
        	
        	
        	
        	
        	
        	}
        private static Double times_re(Double are, Double aim, Double bre, Double bim) {
            
            return are * bre - aim * bim;
        
     
        }
        private static Double times_im(Double are, Double aim, Double bre, Double bim) {
    
            return are * bim + aim * bre;
         
        }
        
        private static Double division_re(Double c1r, Double c1i, Double c2r, Double c2i) {
            
            return  (c1r*c2r+c1i*c2i)/(c2r*c2r+c2i*c2i);
        
     
        }
        
        private static Double division_im(Double c1r, Double c1i, Double c2r, Double c2i) {
            
            return (c1i*c2r-c1r*c2i)/(c2r*c2r+c2i*c2i);
        
     
        }
        
		public static void dbBuildtopology(String [] dbSetup) {
			Connection conn = null;
			Statement stmt = null;
			Statement stmttwo = null;
			Statement stmtthree = null;
			Statement stmtfour = null;
			Statement stmtfive = null;
			Statement stmtsix = null;
			Statement stmtseven = null;
			Statement stmteight = null;
			
			JDBC_DRIVER = dbSetup[0];
			DB_URL = dbSetup[1];
			USER = dbSetup[2];
			PASS= dbSetup[3];
			DB_NAME = dbSetup[4];
			
			//String[] a = new String[5];
			//String[] b = new String[5];
			
			try{
				// Register JDBC driver
				Class.forName(JDBC_DRIVER);
				
				// Open a connection
				System.out.println("Connecting to database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				
				stmt = conn.createStatement();
				stmttwo = conn.createStatement();
				stmtthree = conn.createStatement();
				stmtfour = conn.createStatement();
				stmtfive = conn.createStatement();
				stmtsix = conn.createStatement();
				stmtseven = conn.createStatement();
				stmteight = conn.createStatement();
				
				String sql = "USE STUDENTS";
				stmt.executeUpdate(sql);
				
				//Extracting data from buses
				sql = "Select * From busbarsection";
		        ResultSet busdata= stmt.executeQuery(sql);
		        //Extracting data from terminals
		        String sqltwo = "Select * From terminal";
		        ResultSet terminaldata = stmttwo.executeQuery(sqltwo);
		        //Extracting data from loads
		        String sqlthree = "Select * From energyconsumer";
		        ResultSet loadsdata = stmtthree.executeQuery(sqlthree);
		        //Extracting data from shunts
		        String sqlfour = "Select * From regulatingcontrol";
		        ResultSet shuntsdata = stmtfour.executeQuery(sqlfour);
		       //Extracting data from breakers
		        String sqlfive = "Select * From breaker";
		        ResultSet breakersdata = stmtfive.executeQuery(sqlfive);
		      //Extracting data from voltage levels
		        String sqlsix = "Select * From voltagelevel";
		        ResultSet vleveldata = stmtsix.executeQuery(sqlsix);
		        //Extracting data from lines
		        String sqlseven = "Select * From aclinesegment";
		        ResultSet linedata = stmtseven.executeQuery(sqlseven);
		      //Extracting data from powertransformers
		        String sqleight = "Select * From powertransformerend";
		        ResultSet transformerdata = stmteight.executeQuery(sqleight);
		        
		        
		        
		        int g =0;
		        
		        while (busdata.next()) {
		      busbar_name.add(g, busdata.getString("Name"));
		      busbar_rdfID.add(g, busdata.getString("rdf_ID"));
		      busbar_voltageref.add(g, busdata.getString("RDF_ID_VoltageLevel"));
		        	 g=g+1;
		        }
		        // We create the busbar matrix here.
		        int nbus=busbar_rdfID.size();
		        Double[][] Ymatrix_re = new Double[nbus][nbus];
		        Double[][] Ymatrix_im = new Double[nbus][nbus];
		        for(int k=0; k<nbus;k++){
		        	for(int o=0; o<nbus; o++){
		        		Ymatrix_re[k][o]=0.0;
		        		Ymatrix_im[k][o]=0.0;
		        	}
		        }
		        
		        g=0;
		        while (terminaldata.next()) {	
		        // We do not need to know the RDF_ID of the terminal but the position in the table
		        rdfID_resource.add(g, terminaldata.getString("RDF_ID_ConductingEquipmentReource"));
		        common_connectnode.add(g, terminaldata.getString("RDF_ID_ConnectivityNode"));
		        g=g+1;
		        }
		        g=0;
		        while (loadsdata.next()) {	
		        // We do not need to know the RDF_ID of the terminal but the position in the table
		        rdfID_load.add(g, loadsdata.getString("rdf_ID"));
		        p_load.add(g, loadsdata.getString("P"));
		        q_load.add(g, loadsdata.getString("Q"));
		        g=g+1;
		        }
		        g=0;
		        while (shuntsdata.next()) {	
			        // We do not need to know the RDF_ID of the terminal but the position in the table
			        rdfID_shunt.add(g, shuntsdata.getString("rdf_ID"));
			        q_shunt.add(g, shuntsdata.getString("TargetValue"));
			  
			        g=g+1;
			        }
		        
		        //double[] q_shunt = new double[busbar_rdfID.size()];
		        g=0;
		        while (breakersdata.next()) {	
			        // We do not need to know the RDF_ID of the terminal but the position in the table
			        rdfID_breaker.add(g, breakersdata.getString("rdf_ID"));
			        state_breaker.add(g, breakersdata.getString("B"));
			        g=g+1;
			        }
		        g=0;
		        while (vleveldata.next()) {	
			        // We do not need to know the RDF_ID of the terminal but the position in the table
		        	voltagelevel_rdfID.add(g, vleveldata.getString("rdf_ID"));
		        	voltagelevel_value.add(g, vleveldata.getString("Name"));
			        g=g+1;
			        }
		        g=0;
		        while (linedata.next()) {	
			        // We do not need to know the RDF_ID of the terminal but the position in the table
		        	rdfID_line.add(g, linedata.getString("rdf_ID"));
		        	R_line.add(g, linedata.getString("R"));
		        	X_line.add(g, linedata.getString("X"));
		        	bsh_line.add(g, linedata.getString("B"));
			        g=g+1;
			        }
		        g=0;
		        while (transformerdata.next()) {	
			        // We do not need to know the RDF_ID of the terminal but the position in the table
		        	rdfID_transformer.add(g, transformerdata.getString("rdf_ID"));
		        	R_transformer.add(g, transformerdata.getString("R"));
		        	X_transformer.add(g, transformerdata.getString("X"));
			        g=g+1;
			        }

		        //We need to find out which voltage corresponds to each busbar
		        double[] voltage = new double[busbar_rdfID.size()];
		        for (int i = 0; i < busbar_rdfID.size(); i++){
		        	for (int p = 0; p < busbar_rdfID.size(); p++){
			        	if(busbar_voltageref.get(i).equals(voltagelevel_rdfID.get(p))){
			        		voltage[i]=Double.parseDouble(voltagelevel_value.get(p));
			        	}
			        	else{}
			        }
		        }
		        double R=0;
		        double X=0;
		        double one_over_impedance_re=0;
		        double one_over_impedance_im=0;
		        System.out.println("TIll her it works");
		        for (int i = 0; i < busbar_rdfID.size(); i++){
	        	
		        	for (int n = 0; n < rdfID_resource.size(); n++){
		        	
		        	if(busbar_rdfID.get(i).equals(rdfID_resource.get(n))){
		        		//we have found the terminal connected to the busbar, which is in the next connectivity node:
		        		found=common_connectnode.get(n);
		        		// the row related to this terminal can be deleted,
		        		
		        		for (int l = 0 ; l < rdfID_resource.size(); l++){
		        			if(found.equals(common_connectnode.get(l))&&l!=n){ //We have found another component connected to the same connectivity node
		        			
		        					// this component can just be a load a shunt or a breaker
		        					// the transformers or the lines just appear after a breaker
		        					if ( rdfID_load.indexOf(rdfID_resource.get(l))!=-1) {
		        						int index=rdfID_load.indexOf(rdfID_resource.get(l));
		        						R=LoadR(voltage[i],p_load.get(index));
		        						X=LoadI(voltage[i],q_load.get(index));
		        						one_over_impedance_re=divisionre(R,X);
		        						one_over_impedance_im=divisionim(R,X);
		        						//to do 1/impedance;
		        						
		        			            Ymatrix_re[i][i]=Ymatrix_re[i][i]+one_over_impedance_re;
		        			            Ymatrix_im[i][i]=Ymatrix_im[i][i]+one_over_impedance_im;
		        			            rdfID_resource.remove(l); common_connectnode.remove(l);
		        			        } else if (rdfID_shunt.indexOf(rdfID_resource.get(l))!=-1) {
		        						int index=rdfID_shunt.indexOf(rdfID_resource.get(l));
		        			        	R=LoadR(voltage[i],"0");
		        			        	X=LoadI(voltage[i],q_load.get(index));
		        			        	one_over_impedance_re=divisionre(R,X);
		        						one_over_impedance_im=divisionim(R,X);
		        						Ymatrix_im[i][i]=Ymatrix_im[i][i]+one_over_impedance_im;
		        						rdfID_resource.remove(l); common_connectnode.remove(l);
		        			        
		        			        	
		        			        } else {
		        			           
		        			        
		        				} 
		        			}
		        			else{}
		        			
		        		}
		        		
		        	}else{}
		   
		        		
		        	}
		        }
		        
		        
		        System.out.println(rdfID_resource);
		        System.out.println(common_connectnode);
		        System.out.println("TIll her it works too");
		        ArrayList<ArrayList<String>> Matrix = new ArrayList<ArrayList<String>>();
		        ArrayList<ArrayList<String>> Branches = new ArrayList<ArrayList<String>>();
		        ArrayList<String> Branch = new ArrayList<String>();
		        int components = 0; // this is something that the function needs
		        System.out.println(busbar_rdfID);
		        System.out.println(rdfID_resource);
		        for(int i=0;i<busbar_rdfID.size();i++){
		        	System.out.println("Launch the first loop ");
		        	for(int j=0;j<rdfID_resource.size();j++){
		        		
		        		if(busbar_rdfID.get(i).equals(rdfID_resource.get(j))){
		        			System.out.println("Found the busbar and the node ");
		        			System.out.println("Alright Bitch");
		        			System.out.println("WOrk madafaka");
		        			Branch.add(busbar_rdfID.get(i));
		        			
		        			Matrix.add(Branch);
		        			try{
		        			BranchesCalculation(Matrix,Branches,rdfID_breaker,state_breaker,rdfID_resource,busbar_rdfID,common_connectnode,common_connectnode.get(j), components, busbar_rdfID.get(i));
		        			
		        			
		        			}catch(Exception e){e.printStackTrace();
		        				
		        			
		        			}
		        	
		        	 System.out.println("Branches calculated");
		        	
		        	 Matrix.remove(0);Branch.remove(0); // TO USE THEM THE SAME WAY AGAIN
			        	YmatrixCalculation(Branches,rdfID_breaker, rdfID_transformer, rdfID_line, state_breaker, R_line, X_line, R_transformer, X_transformer, bsh_line,Ymatrix_re, Ymatrix_im, busbar_rdfID, busbar_rdfID.get(i));
			        	 System.out.println("A row of the Ymatrix should be calculated");
		        		}}
		        	 }
		        for(int k=0; k<nbus;k++){
		        	for(int o=0; o<nbus; o++){
		        		System.out.println(Ymatrix_re[k][o]);
		        		
		        	}
		        }
		        for(int k=0; k<nbus;k++){
		        	for(int o=0; o<nbus; o++){
		        		System.out.println(Ymatrix_re[k][o]);
		        		
		        	}
		        }
		        
		        
		        
				// Execute a query to create database
				System.out.println("It worked motherfucker");
				 
				
			}catch(SQLException se){
				//Handle errors for JDBC
				se.printStackTrace();
				}catch(Exception e){
				//Handle errors for Class.forName
				e.printStackTrace();}

		}
}



