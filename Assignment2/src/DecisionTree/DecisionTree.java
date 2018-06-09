package DecisionTree;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;



public class DecisionTree {
	
public static void main(String[] args) {
		
		String dataFile = "iris_2.csv";
		BufferedReader br = null;
		String line = "";
		String SplitBy = ",";
		
		ArrayList<Flower> flowerList = new ArrayList<Flower>();
	 
		try {
		// use code from Java exercise IV to create internal database of iris flowers
			br = new BufferedReader(new FileReader(dataFile));
			
			while ((line = br.readLine()) != null) {
	 	     // use comma as separator
				String[] flower = line.split(SplitBy);
				double[] param = {0.0,0.0,0.0,0.0};
				
				for (int j = 0; j < 4; j++)	{				
				param[j] = Double.parseDouble(flower[j]);
				
				}
				
				flowerList.add(new Flower(param,flower[4]));
		
			}
			} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// print out a list with one of the dimensional parameters
		showList(flowerList,3);
		
		//create 2 new ArrayLists to split original
		ArrayList<Flower> irisList1 = new ArrayList<Flower>();
		ArrayList<Flower> irisList2 = new ArrayList<Flower>();
		
		// define an optimal split point to get maximum entropy gain
		splitByGain(flowerList,1.8,irisList1,irisList2,3);
		
		System.out.println("Versicolor items in group1 - " + frequency(irisList1, "Iris-versicolor") + "\n" + "Virginica items in group1 - " + frequency(irisList1, "Iris-virginica"));

		System.out.println("Versicolor items in group2 - " + frequency(irisList2, "Iris-versicolor") + "\n" + "Virginica number in group 2 - " + frequency(irisList2, "Iris-virginica"));

		// find other split points to develop decision tree algorithm 
		
		showList(irisList1,2);
		
		//create 2 new ArrayLists to split original
		ArrayList<Flower> irisList3 = new ArrayList<Flower>();
		ArrayList<Flower> irisList4 = new ArrayList<Flower>();
		ArrayList<Flower> irisList5 = new ArrayList<Flower>();
		ArrayList<Flower> irisList6 = new ArrayList<Flower>();
		
		// define an optimal split point to get maximum entropy gain
		splitByGain(irisList1,5.1,irisList3,irisList4,2);
		
		System.out.println("Versicolor items in group1 - " + frequency(irisList3, "Iris-versicolor") + "\n" + "Virginica items in group1 - " + frequency(irisList3, "Iris-virginica"));

		System.out.println("Versicolor items in group2 - " + frequency(irisList4, "Iris-versicolor") + "\n" + "Virginica number in group 2 - " + frequency(irisList4, "Iris-virginica"));

		showList(irisList2,0);

		splitByGain(irisList2,6,irisList5,irisList6,0);
		
		System.out.println("Versicolor items in group1 - " + frequency(irisList5, "Iris-versicolor") + "\n" + "Virginica items in group1 - " + frequency(irisList5, "Iris-virginica"));

		System.out.println("Versicolor items in group2 - " + frequency(irisList6, "Iris-versicolor") + "\n" + "Virginica number in group 2 - " + frequency(irisList6, "Iris-virginica"));

		
		showList(irisList5,2);
		ArrayList<Flower> irisList7 = new ArrayList<Flower>();
		ArrayList<Flower> irisList8 = new ArrayList<Flower>();
		
		splitByGain(irisList5,4.9,irisList7,irisList8,2);
		
		System.out.println("Versicolor items in group1 - " + frequency(irisList7, "Iris-versicolor") + "\n" + "Virginica items in group1 - " + frequency(irisList7, "Iris-virginica"));

		System.out.println("Versicolor items in group2 - " + frequency(irisList8, "Iris-versicolor") + "\n" + "Virginica number in group 2 - " + frequency(irisList8, "Iris-virginica"));
		
		
		showList(irisList4,0);
		ArrayList<Flower> irisList9 = new ArrayList<Flower>();
		ArrayList<Flower> irisList10 = new ArrayList<Flower>();
		
		splitByGain(irisList4,6.1,irisList9,irisList10,0);
		
		System.out.println("Versicolor items in group1 - " + frequency(irisList9, "Iris-versicolor") + "\n" + "Virginica items in group1 - " + frequency(irisList9, "Iris-virginica"));

		System.out.println("Versicolor items in group2 - " + frequency(irisList10, "Iris-versicolor") + "\n" + "Virginica number in group 2 - " + frequency(irisList10, "Iris-virginica"));
		
		showList(irisList3,1);
		ArrayList<Flower> irisList11 = new ArrayList<Flower>();
		ArrayList<Flower> irisList12 = new ArrayList<Flower>();
		
		splitByGain(irisList3,2.6,irisList11,irisList12,1);
		
		System.out.println("Versicolor items in group1 - " + frequency(irisList11, "Iris-versicolor") + "\n" + "Virginica items in group1 - " + frequency(irisList11, "Iris-virginica"));

		System.out.println("Versicolor items in group2 - " + frequency(irisList12, "Iris-versicolor") + "\n" + "Virginica number in group 2 - " + frequency(irisList12, "Iris-virginica"));
		
		showList(irisList11,0);
		int correct=0;
		for(int i=0;i<flowerList.size();i++){
			String type;
			type=checkTest(flowerList.get(i));
			if(type.equals(flowerList.get(i).type))
				correct++;
		}
		
		System.out.println("Correct are " + correct);
		
		

	}
	
	public static String checkTest(Flower test){
		String type;
		if(test.param[3]<1.8){
			if(test.param[2]<5.1){
				if(test.param[1]<2.6){
					type="Iris-versicolor";
				}
				else{
					type="Iris-versicolor";
					
				}
			}
			else {
				if(test.param[0]<6.1){
					type="Iris-versicolor";
				
				}
				else{
					type="Iris-virginica";
					
				}
				
			}
			
			
		}
		else{
			if(test.param[0]<6){
				if(test.param[2]<4.9){
					type="Iris-versicolor";
					
				}
				else{
					type="Iris-virginica";
					
				}
			}
			else{
				type="Iris-virginica";
				
			}
			
		}
		return type;
				
		
	}

	// a method to count a number of a specific specie in a flower list
	public static int frequency (ArrayList<Flower> flowerList, String name){
		
		int count=0;
		
		for(Flower fl : flowerList){
			
			if(fl.type.equals(name)){
				count=count+1;

			}
		}


		// count apperances in the list

		return count;
	}
	
	// a method to calculate entropy of split
	public static double entropy (ArrayList<Flower> flowerList){
		
		double total_number = flowerList.size();
		double Virginica_number = frequency(flowerList, "Iris-virginica");
		double Versicolor_number = frequency(flowerList, "Iris-versicolor");
			
		double probability_virginica = Virginica_number/total_number;
		double probability_versicolor = Versicolor_number/total_number;
		double Entropy_total;
		
		if(probability_virginica==0){
			
			Entropy_total = ((-(probability_versicolor*Math.log(probability_versicolor)/Math.log(2))));

		}
		else if(probability_versicolor==0){
			
			Entropy_total = (-(probability_virginica*Math.log(probability_virginica)/Math.log(2)));

		}
		else{
			
			Entropy_total = (-(probability_virginica*Math.log(probability_virginica)/Math.log(2))+(-(probability_versicolor*Math.log(probability_versicolor)/Math.log(2))));

		}

		// calculate total entropy
	
		return Entropy_total;	
		
	}
	

	// a method to calculate information gain of the split
	public static void splitByGain (ArrayList<Flower> flowerList, double splitValue, ArrayList<Flower> irisList1, ArrayList<Flower> irisList2, int splitParameter){
		
		double flowerSize = flowerList.size();
		double entropyTotal = entropy(flowerList);
	
		// split a list of Flowers based on a splitParameter and SplitValue
		for(Flower fl : flowerList){
			
			if(fl.param[splitParameter]<splitValue){
				
				irisList1.add(fl);
			}
			else{
				
				irisList2.add(fl);
			}
			
		}
		
		double iris1size = irisList1.size();
		double iris2size = irisList2.size();
		double entropy1 = entropy(irisList1);
		double entropy2 = entropy(irisList2);

		// calculate information gain
		double gain = entropyTotal - (iris1size*entropy1/flowerSize + iris2size*entropy2/flowerSize);
			
		System.out.println("Gain of the split - " + gain);
		
	}
	  

	public static void showList(ArrayList <Flower> flowerList, int posit){
		System.out.println("\n\n\n");
		
		for(int x = 0; x < flowerList.size(); x++){
			System.out.println(flowerList.get(x).type + " ... " + flowerList.get(x).param[posit]);		
			}
	}
}
