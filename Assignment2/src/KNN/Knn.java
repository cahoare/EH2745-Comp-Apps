package KNN;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;



public class Knn {
	
	public static void main(String[] args){
		  
		int K=3;
		String dataFile = "iris.csv";
		BufferedReader br = null;
		String line = "";
		String SplitBy = ",";
		int test=0, learn=0;
		
		ArrayList<Flower> flowerList = new ArrayList<Flower>();
		ArrayList<Flower> learnList = new ArrayList<Flower>();
		ArrayList<Flower> testList = new ArrayList<Flower>();
		 
		try {
			br = new BufferedReader(new FileReader(dataFile));
							
			while ((line = br.readLine()) != null) {
	 	     // use comma as separator
				String[] flower = line.split(SplitBy);
				double[] param = {0.0,0.0,0.0,0.0};
				
				for (int j = 0; j < 4; j++)	{				
				param[j] = Double.parseDouble(flower[j]);
			}
				
				
				if(learn<30)
				{
					learnList.add(new Flower(param,flower[4]));
					learn++;
				}
				else
				{
					testList.add(new Flower(param,flower[4]));
					test++;
					if(test==20)
					{
						learn=0;
						test=0;
					}
				}
				
				flowerList.add(new Flower(param,flower[4]));
		}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ArrayList<String> testType = new ArrayList<String>();	
		// a test value
		for(Flower testFlower : testList){
			
		
		
			
		// create object Result that contains distance to a specific flower and flower’s type
		ArrayList<Result> resultList = new ArrayList<Result>();
				
		for(Flower flower : learnList){
			double dist = 0.0;
			
			dist= Math.pow((flower.param[0]-testFlower.param[0]),2)+Math.pow((flower.param[1]-testFlower.param[1]),2)+Math.pow((flower.param[2]-testFlower.param[2]),2)+Math.pow((flower.param[3]-testFlower.param[3]),2);
		
		// calculate variable dist – square of Euclidean distance from query measurements to flower’s dimensional parameters

			double distance = Math.sqrt(dist);
			
			resultList.add(new Result(distance, flower.type));
		}
			
		Collections.sort(resultList, new DistanceComparator());
		
		int c1=0,c2=0,c3=0;
		for(int i=0; i<K; i++)
		{
			String c;
			c=resultList.get(i).label;
			switch(c){
			case "Iris-setosa":
				c1++;
				break;
			case "Iris-versicolor":
				c2++;
				break;
			case "Iris-virginica":
				c3++;
				break;
					
			}
		}
		
		if(c1>c2){
			if(c1>c3){
				testType.add("Iris-setosa");
			}
			else{
				testType.add("Iris-virginica");
			}		
		}
		else if(c2>c3){
			testType.add("Iris-versicolor");
		}
		else{
			testType.add("Iris-virginica");
		}
			
		
	}
		
	for(int x = 0; x < testList.size(); x++){
		System.out.println(testList.get(x).type + " Space " + testType.get(x));
	}
}
		
		
}




