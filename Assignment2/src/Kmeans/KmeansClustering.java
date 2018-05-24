package kmeans;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;




public class KmeansClustering {
	
	static ArrayList<Flower> flowerList = new ArrayList<Flower>();
	static ArrayList<String> type = new ArrayList<String>(); // array of the types
	
	static ArrayList<String> type1 = new ArrayList<String>();
	static ArrayList<String> type2 = new ArrayList<String>();
	static ArrayList<String> type3 = new ArrayList<String>();
	static ArrayList<String> type4 = new ArrayList<String>();
	
	static double [][] values = new double [150][4]; // array of the values
	static double [][] centroids = new double [4][4];
	static double [][] cluster1 = new double [150][4];
	static double [][] cluster2 = new double [150][4];
	static double [][] cluster3 = new double [150][4];
	static double [][] cluster4 = new double [150][4];
	static double dist1,dist2,dist3,dist4;
	static int index1,index2,index3,index4;
	
	//static ArrayList<Double> values = new ArrayList<Double>();
		
	   
	public static void main(String[] args) {
	   
		String dataFile = "C:\\Users\\Callum\\eclipse-workspace\\EH2745\\Assignments\\Assignment2\\iris.csv";
		
		
		
		read_data(dataFile);
	  
		for(int i=0; i<150; i++)
		 {
		  	for(int j=0; j<4; j++)
			{
		  		
				System.out.println("Value at " + i + " and " + j + " is "+ values[i][j]);
				
			}
		 }
	  
	  
		initialize();
		cal_centroids();
	  	K_clusters();
	  	
	  System.out.println(type1.size());
	  System.out.println(type2.size());
	  System.out.println(type3.size());
	  System.out.println(type4.size());
	  
	
	  System.out.println(type1);
	  System.out.println(type2);
	  System.out.println(type3);
	  System.out.println(type4);
	
	}//end main
	
	public static void read_data(String dataFile) {
			// TODO Auto-generated method stub
			
		BufferedReader br = null;
		String line = "";
		String SplitBy = ",";
	  
		 
		try {
		// use code from Java exercise IV to create internal database of iris flowers
			// param - sepal length, sepal width, petal length and petal width
			// type - setosa, versicolor or virginica.
			
			br = new BufferedReader(new FileReader(dataFile));
			
			while ((line = br.readLine()) != null) {
	 	     // use comma as separator
				String[] flower = line.split(SplitBy);
				double[] param = {0.0,0.0,0.0,0.0};
				double[] outType={0,0,0};
				
				for (int j = 0; j < 4; j++)	{
			
				param[j] = Double.parseDouble(flower[j]);
				
				}
				
				
				if(flower[4].equals("Iris-setosa")){
					
					outType[0]=1;
					
				}
				else if(flower[4].equals("Iris-versicolor")){
					
					outType[1]=1;
					
				}
				else{
	
					outType[2]=1;
					
				}
				
				flowerList.add(new Flower(param,flower[4],outType));
			

		
			}
			} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(int i=0;i<flowerList.size();i++){
			
			for(int j=0;j<flowerList.get(i).param.length;j++){
				
				values[i][j]=flowerList.get(i).param[j];
			}
			
			type.add(flowerList.get(i).type);
			
		}

	}
	

	public static void initialize() {
		// TODO Auto-generated method stub
		int x = values.length/6; //divide values up into 6
	
		for(int i=0;i<4;i++)
		{
			centroids[0][i]=values[x][i]; //create the centroids array - initialise into 6 for some reason?? Random?
			centroids[1][i]=values[2*x][i];
			centroids[2][i]=values[4*x][i];
			centroids[3][i]=values[6*x-1][i];
					
		}
		
		index1=0;
	  	index2=0;
	  	index3=0;
	  	index4=0;
	  	
	  	double dist1a;
		double dist2a;
		double dist3a;
		double dist4a;
	  	
	  	
	  	// we want to allocate to the clusters with lowest distance??
		for(int i=0; i<150; i++)
		 {
			dist1 = 0.0;
			dist2 = 0.0;
			dist3 = 0.0;
			dist4 = 0.0;
			
		  	for(int j=0; j<4; j++)
			{
				dist1 += ((centroids[0][j]-values[i][j])*(centroids[0][j]-values[i][j]));
		//		dist1a +=((centroids[0][j]-values[i][j])*(centroids[0][j]-values[i][j]));
				dist2 += ((centroids[1][j]-values[i][j])*(centroids[1][j]-values[i][j]));
		//		dist2a += ((centroids[1][j]-values[i][j])*(centroids[1][j]-values[i][j]));
				dist3 += ((centroids[2][j]-values[i][j])*(centroids[2][j]-values[i][j]));
		//		dist3a+= ((centroids[2][j]-values[i][j])*(centroids[2][j]-values[i][j]));
				dist4 += ((centroids[3][j]-values[i][j])*(centroids[3][j]-values[i][j]));
		//		dist4a += ((centroids[3][j]-values[i][j])*(centroids[3][j]-values[i][j]));
			
		//		System.out.println(i +" : "+ dist1 +","+dist2 +","+dist3+","+dist4);
		//		System.out.println(i +" : "+dist1a +","+dist2a +","+dist3a+","+dist4a);
				
			}
		 // 	dist1a = Math.sqrt(dist1a);
		  	dist1=Math.sqrt(dist1);	
			dist2=Math.sqrt(dist2);	
			dist3=Math.sqrt(dist3);	
			dist4=Math.sqrt(dist4);	
		//	dist2a=Math.sqrt(dist2a);	
		//	dist3a=Math.sqrt(dist3a);	
		//	dist4a=Math.sqrt(dist4a);	
			
			System.out.println(i +" : "+ dist1 +","+dist2 +","+dist3+","+dist4);
		//	System.out.println(i +" : "+dist1a +","+dist2a +","+dist3a+","+dist4a);
			
			if(dist1 < dist2)
			{
				if(dist1 < dist3)
				{
					if(dist1 < dist4)
					{
						for(int j=0; j<4; j++)
						{
								cluster1[index1][j]=values[i][j];
								
						}
						index1++;
						
					}
					else
					{
						for(int j=0; j<4; j++)
						{
					 		cluster4[index4][j]=values[i][j];
							
						}
						index4++;
					}
				}
				
				else if(dist3 < dist4)
				{
					for(int j=0; j<4; j++)
					{
				 		cluster3[index3][j]=values[i][j];
				 		
					}
					index3++;
				 	
				}
				else
				{
					for(int j=0; j<4; j++)
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
					for(int j=0; j<4; j++)
					{
							cluster2[index2][j]=values[i][j];
					}
					index2++;
					
					
					
				}
				else
				{
					for(int j=0; j<4; j++)
					{
				 		cluster4[index4][j]=values[i][j];
				 		
					}
					index4++;
				 	
				}
			}
			
			else if(dist3 < dist4)
			{
				for(int j=0; j<4; j++)
				{
			 		cluster3[index3][j]=values[i][j];
			 		
				}
				index3++;
		 		
			}
			else
			{
				for(int j=0; j<4; j++)
				{
			 		cluster4[index4][j]=values[i][j];
			 		
				}
				index4++;
			 	
			}
			
		 }
}
	public static void cal_centroids() {
		// TODO Auto-generated method stub
		double [][] new_centroids = {{0,0,0,0},
									 {0,0,0,0},
									 {0,0,0,0},
									 {0,0,0,0}
									 };
		for(int j=0; j<4; j++)
		{
			for(int i=0; i<index1; i++)
			{
				new_centroids[0][j] += (cluster1[i][j]/(index1));  
				
			}
			centroids[0][j]=new_centroids[0][j];
		}
		
		for(int j=0; j<4; j++)
		{
			for(int i=0; i<index2; i++)
			{
				new_centroids[1][j] += (cluster2[i][j]/(index2));  
				centroids[1][j]=new_centroids[1][j];
		
			}
		}
		
		for(int j=0; j<4; j++)
		{
			for(int i=0; i<index3; i++)
			{
				new_centroids[2][j] += (cluster3[i][j]/(index3));  
				centroids[2][j]=new_centroids[2][j];
			}
		}
		
		for(int j=0; j<4; j++)
		{
			for(int i=0; i<index4; i++)
			{
				new_centroids[3][j] += (cluster4[i][j]/(index4));  
				centroids[3][j]=new_centroids[3][j];
			}
		} 	
	}
	
	
	public static void K_clusters() {
		// TODO Auto-generated method stub
		double [][] temp_values = new double[150][4];
		double [][] old_centroids = new double [4][4];
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
			for(int j=0; j<4; j++)
		 	{
				temp_values[i][j]= cluster1[i][j];
					  		
			}
		 }
		for(int i=0; i<index2; i++)
		 {
			for(int j=0; j<4; j++)
		 	{
				temp_values[i+index1][j]= cluster2[i][j];
					  		
			}
		 }
		for(int i=0; i<index3; i++)
		 {
			for(int j=0; j<4; j++)
		 	{
				temp_values[i+index1+index2][j]= cluster3[i][j];
					  		
			}
		 }
		for(int i=0; i<index4; i++)
		 {
			for(int j=0; j<4; j++)
		 	{
				temp_values[i+index1+index2+index3][j]= cluster4[i][j];
					  		
			}
		 }
		
		
		index1=0;
	  	index2=0;
	  	index3=0;
	  	index4=0;
	
	  	for(int i=0; i<150; i++)
		 {
		  	for(int j=0; j<4; j++)
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
						for(int j=0; j<4; j++)
						{
								cluster1[index1][j]=values[i][j];
																
						}
						index1++;
						type1.add(type.get(i));
						
					}
					else
					{
						for(int j=0; j<4; j++)
						{
					 		cluster4[index4][j]=values[i][j];
					 		
						}
						index4++;
						type4.add(type.get(i));
						
					}
				}
				
				else if(dist3 < dist4)
				{
					for(int j=0; j<4; j++)
					{
				 		cluster3[index3][j]=values[i][j];
				 		
					}
					index3++;
					type3.add(type.get(i));
					
				}
				else
				{
					for(int j=0; j<4; j++)
					{
				 		cluster4[index4][j]=values[i][j];
				 		
					}
					index4++;
					type4.add(type.get(i));
					
				}
			}
		  	else if(dist2 < dist3)
			{
				if(dist2 < dist4)
				{
					for(int j=0; j<4; j++)
					{
							cluster2[index2][j]=values[i][j];
							
					}
					index2++;
					type2.add(type.get(i));
					
					
					
				}
				else
				{
					for(int j=0; j<4; j++)
					{
				 		cluster4[index4][j]=values[i][j];
				 		
					}
					index4++;
					type4.add(type.get(i));
					
				}
			}
			
			else if(dist3 < dist4)
			{
				for(int j=0; j<4; j++)
				{
			 		cluster3[index3][j]=values[i][j];
			 		
				}
				index3++;
				type3.add(type.get(i));
				
			}
			else
			{
				for(int j=0; j<4; j++)
				{
			 		cluster4[index4][j]=values[i][j];
			 		
				}
				index4++;
				type4.add(type.get(i));
				
			}
			
		 }
		
		for(int j=0; j<4; j++)
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
		
		for(int j=0; j<4; j++)
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
