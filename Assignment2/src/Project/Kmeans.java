package Project;


import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

public class Kmeans {
	
	
		
	// ESCENARIOS INFORMATION	
	private ArrayList<Escenario> EscenariosData = new ArrayList<Escenario>();
	
	private ArrayList<Integer> type1 = new ArrayList<Integer>();
	private ArrayList<Integer> type2 = new ArrayList<Integer>();
	private ArrayList<Integer> type3 = new ArrayList<Integer>();
	private ArrayList<Integer> type4 = new ArrayList<Integer>();
	

	
	private double [][] values; // rows: number of escenarios, columns: number of variables 2
	private double [][] centroids; // Number of rows is the number of klusters
	private double [][] cluster1; 
	private double [][] cluster2;
	private double [][] cluster3;
	private double [][] cluster4;
	private double dist1,dist2, dist3, dist4;
	private int index1,index2,index3,index4;
	 

	
	public Kmeans(ArrayList<Escenario> List) {
		
		EscenariosData = (ArrayList<Escenario>)List.clone();

		int g = 0;
		while(g<EscenariosData.size()){ 	
	    System.out.println(EscenariosData.get(g).getName()+" "+EscenariosData.get(g).getVoltage()+" "+EscenariosData.get(g).getAngle()+" "+EscenariosData.get(g).getTimeestamp());
	    g++;    }
	}
		
	public void Clustering () {
		
		
		
		values=new double[EscenariosData.size()][2]; 
		centroids=new double[4][2]; 
		cluster1=new double[EscenariosData.size()][2];
		cluster2=new double[EscenariosData.size()][2];
		cluster3=new double[EscenariosData.size()][2];
		cluster4=new double[EscenariosData.size()][2];
		int g=0;
		while(g<EscenariosData.size()){ 
			values[g][0]=EscenariosData.get(g).getVoltage();
			values[g][1]=EscenariosData.get(g).getAngle();
			
		    g++;    }
		
		
		initialize();
		cal_centroids();
		K_clusters();
		System.out.println(type1.size());
		System.out.println(type2.size());
		System.out.println(type3.size());
		System.out.println(type4.size());
	}		
		
    public void initialize(){
		// TODO Auto-generated method stub
		int x = values.length/6;
	
		for(int i=0;i<2;i++)
		{
			centroids[0][i]=values[x][i];
			centroids[1][i]=values[2*x][i];
			centroids[2][i]=values[4*x][i];
			centroids[3][i]=values[6*x-1][i];
					
		}
		
		index1=0;
	  	index2=0;
	  	index3=0;
	  	index4=0;
		for(int i=0; i<values.length; i++)
		 {
			dist1=0;
			dist2=0;
			dist3=0;
			dist4=0;
		  	for(int j=0; j<2; j++)
			{
				dist1 += ((centroids[0][j]-values[i][j])*(centroids[0][j]-values[i][j]));
				
				dist2 += ((centroids[1][j]-values[i][j])*(centroids[1][j]-values[i][j]));
				
				dist3 += ((centroids[2][j]-values[i][j])*(centroids[2][j]-values[i][j]));
				
				dist4 += ((centroids[3][j]-values[i][j])*(centroids[3][j]-values[i][j]));
			
				
			}
		  	dist1=Math.sqrt(dist1);	
			dist2=Math.sqrt(dist2);	
			dist3=Math.sqrt(dist3);	
			dist4=Math.sqrt(dist4);	
			
			
			if(dist1 < dist2)
			{
				if(dist1 < dist3)
				{
					if(dist1 < dist4)
					{
						for(int j=0; j<2; j++)
						{
								cluster1[index1][j]=values[i][j];
								
						}
						index1++;
						
					}
					else
					{
						for(int j=0; j<2; j++)
						{
					 		cluster4[index4][j]=values[i][j];
							
						}
						index4++;
					}
				}
				
				else if(dist3 < dist4)
				{
					for(int j=0; j<2; j++)
					{
				 		cluster3[index3][j]=values[i][j];
				 		
					}
					index3++;
				 	
				}
				else
				{
					for(int j=0; j<2; j++)
					{
				 		cluster4[index4][j]=values[i][j];
				 		
					}
					index4++;
				 	
				}
			}
		  	else if(dist2 < dist3)
			{
				if(dist2 < dist4)
				{
					for(int j=0; j<2; j++)
					{
							cluster2[index2][j]=values[i][j];
					}
					index2++;
					
					
					
				}
				else
				{
					for(int j=0; j<2; j++)
					{
				 		cluster4[index4][j]=values[i][j];
				 		
					}
					index4++;
				 	
				}
			}
			
			else if(dist3 < dist4)
			{
				for(int j=0; j<2; j++)
				{
			 		cluster3[index3][j]=values[i][j];
			 		
				}
				index3++;
		 		
			}
			else
			{
				for(int j=0; j<2; j++)
				{
			 		cluster4[index4][j]=values[i][j];
			 		
				}
				index4++;
			 	
			}
			
		 }
	}
    public void cal_centroids() {
		// TODO Auto-generated method stub
		double [][] new_centroids = {{0,0,0,0},
									 {0,0,0,0},
									 {0,0,0,0},
									 {0,0,0,0}
									 };
		for(int j=0; j<2; j++)
		{
			for(int i=0; i<index1; i++)
			{
				new_centroids[0][j] += (cluster1[i][j]/(index1));  
				centroids[0][j]=new_centroids[0][j];
			}
		}
		
		for(int j=0; j<2; j++)
		{
			for(int i=0; i<index2; i++)
			{
				new_centroids[1][j] += (cluster2[i][j]/(index2));  
				centroids[1][j]=new_centroids[1][j];
		
			}
		}
		
		for(int j=0; j<2; j++)
		{
			for(int i=0; i<index3; i++)
			{
				new_centroids[2][j] += (cluster3[i][j]/(index3));  
				centroids[2][j]=new_centroids[2][j];
			}
		}
		
		for(int j=0; j<2; j++)
		{
			for(int i=0; i<index4; i++)
			{
				new_centroids[3][j] += (cluster4[i][j]/(index4));  
				centroids[3][j]=new_centroids[3][j];
			}
		} 	
	}
    public void K_clusters() {
		// TODO Auto-generated method stub
		double [][] temp_values = new double[values.length][2];
		double [][] old_centroids = new double [4][2];
		double dif1=0,dif2=0,dif3=0,dif4=0;
		double tol=0.001;
		while(true)
	{
		type1.clear();
		type2.clear();
		type3.clear();
		type4.clear();
		
			
			for(int i=0; i<index1; i++)
		 {
			for(int j=0; j<2; j++)
		 	{
				temp_values[i][j]= cluster1[i][j];
					  		
			}
		 }
		for(int i=0; i<index2; i++)
		 {
			for(int j=0; j<2; j++)
		 	{
				temp_values[i+index1][j]= cluster2[i][j];
					  		
			}
		 }
		for(int i=0; i<index3; i++)
		 {
			for(int j=0; j<2; j++)
		 	{
				temp_values[i+index1+index2][j]= cluster3[i][j];
					  		
			}
		 }
		for(int i=0; i<index4; i++)
		 {
			for(int j=0; j<2; j++)
		 	{
				temp_values[i+index1+index2+index3][j]= cluster4[i][j];
					  		
			}
		 }
		
		
		index1=0;
	  	index2=0;
	  	index3=0;
	  	index4=0;
	
	  	for(int i=0; i<values.length; i++)
		 {
	  		dist1=0;
	  		dist2=0;
	  		dist3=0;
	  		dist4=0;
	  		
	  		
		  	for(int j=0; j<2; j++)
			{
		  		dist1 += ((centroids[0][j]-values[i][j])*(centroids[0][j]-values[i][j]));
				
				dist2 += ((centroids[1][j]-values[i][j])*(centroids[1][j]-values[i][j]));
				
				dist3 += ((centroids[2][j]-values[i][j])*(centroids[2][j]-values[i][j]));
				
				dist4 += ((centroids[3][j]-values[i][j])*(centroids[3][j]-values[i][j]));
			
				
				
			}
		  	dist1=Math.sqrt(dist1);	
			dist2=Math.sqrt(dist2);	
			dist3=Math.sqrt(dist3);	
			dist4=Math.sqrt(dist4);	
		
			if(dist1 < dist2)
			{
				if(dist1 < dist3)
				{
					if(dist1 < dist4)
					{
						for(int j=0; j<2; j++)
						{
								cluster1[index1][j]=values[i][j];
																
						}
						index1++;
						type1.add(i);
						
					}
					else
					{
						for(int j=0; j<2; j++)
						{
					 		cluster4[index4][j]=values[i][j];
					 		
						}
						index4++;
						type4.add(i);
						
					}
				}
				
				else if(dist3 < dist4)
				{
					for(int j=0; j<2; j++)
					{
				 		cluster3[index3][j]=values[i][j];
				 		
					}
					index3++;
					type3.add(i);
					
				}
				else
				{
					for(int j=0; j<2; j++)
					{
				 		cluster4[index4][j]=values[i][j];
				 		
					}
					index4++;
					type4.add(i);
					
				}
			}
		  	else if(dist2 < dist3)
			{
				if(dist2 < dist4)
				{
					for(int j=0; j<2; j++)
					{
							cluster2[index2][j]=values[i][j];
							
					}
					index2++;
					type2.add(i);
					
					
					
				}
				else
				{
					for(int j=0; j<2; j++)
					{
				 		cluster4[index4][j]=values[i][j];
				 		
					}
					index4++;
					type4.add(i);
					
				}
			}
			
			else if(dist3 < dist4)
			{
				for(int j=0; j<2; j++)
				{
			 		cluster3[index3][j]=values[i][j];
			 		
				}
				index3++;
				type3.add(i);
				
			}
			else
			{
				for(int j=0; j<2; j++)
				{
			 		cluster4[index4][j]=values[i][j];
			 		
				}
				index4++;
				type4.add(i);
				
			}
			
		 }
		
		for(int j=0; j<2; j++)
		{
				old_centroids[0][j] = centroids[0][j];
				old_centroids[1][j] = centroids[1][j];
				old_centroids[2][j] = centroids[2][j];
				old_centroids[3][j] = centroids[3][j];
				
		}
		
		dif1=0;
		dif2=0;
		dif3=0;
		dif4=0;
		cal_centroids();
		
		for(int j=0; j<2; j++)
		{
				dif1+=(Math.sqrt(((old_centroids[0][j] - centroids[0][j])*(old_centroids[0][j] - centroids[0][j]))))/index1;
				dif2+=(Math.sqrt(((old_centroids[1][j] - centroids[1][j])*(old_centroids[1][j] - centroids[1][j]))))/index2;
				dif3+=(Math.sqrt(((old_centroids[2][j] - centroids[2][j])*(old_centroids[2][j] - centroids[2][j]))))/index3;
				dif4+=(Math.sqrt(((old_centroids[3][j] - centroids[3][j])*(old_centroids[3][j] - centroids[3][j]))))/index4;
				
		}
		
		
		
		if(dif1<=tol&&dif2<=tol&&dif3<=tol&&dif4<=tol)
		{
			break;
		}
		
		
	}
	
	}
}
	
	

