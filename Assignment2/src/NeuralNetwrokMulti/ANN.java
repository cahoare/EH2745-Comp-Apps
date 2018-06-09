package neuralnetwork;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;




public class ANN {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		int stop =0,count=0,correct=0;
		
		String dataFile = "iris.csv";
		BufferedReader br = null;
		String line = "";
		String SplitBy = ",";
		double errorRef =0.01001;
		Random rand = new Random();
		
		Layers inputLayer = new Layers(4,12,null);
		Layers hiddenLayer = new Layers(3,9,inputLayer);
		Layers outLayer = new Layers(3,0,hiddenLayer);
		
		/*double[] weightsInput = {0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,
								 0.05,0.05};//,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,
								 //0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,
								 //0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05};
		double[] weightsHidden = {0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05};//,0.05,
								  //0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,
								  //0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05};
		
		for(int i=0;i<weightsInput.length;i++){
			
			weightsInput[i]=rand.nextDouble();
		}
		
		for(int i=0;i<weightsHidden.length;i++){
			
			weightsHidden[i]=rand.nextDouble();
		}
		*/
		double[] weightsInput = {0.45896394125594075,0.8019065160132018,0.438790851849147,
								 0.8292589916101367,0.5859491358067971,0.8233649000404234,
								 0.48278561484395754,0.0852267839157147,0.2633286366062516,
								 0.5009189622652991,0.3731898775305126,0.748462953575443};
		double[] weightsHidden = {0.06998085259544029,0.5696797988071046,0.4313567583137967,
								  0.9630854765435932,0.7865474126068281,0.5632925030590037,
								  0.5305732722072676,0.9702178456160142,0.016127579772581058};

		BufferedWriter outputWriter = null;
	    try {
			outputWriter = new BufferedWriter(new FileWriter("weights"));
			
			for(int i=0;i<weightsInput.length;i++){
				
				outputWriter.write(Double.toString(weightsInput[i]));
			}
			
			outputWriter.newLine();
			
			for(int i=0;i<weightsHidden.length;i++){
				
				outputWriter.write(Double.toString(weightsHidden[i]));
			}
			outputWriter.close();
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
	    
		
		inputLayer.setWeights(weightsInput);
		hiddenLayer.setWeights(weightsHidden);
				 
		ArrayList<Flower> flowerList = new ArrayList<Flower>();
		ArrayList<Flower> learnList = new ArrayList<Flower>();
		ArrayList<Flower> testList = new ArrayList<Flower>();
		
			
	 
		try {
		// use code from Java exercise IV to create internal database of iris flowers
			br = new BufferedReader(new FileReader(dataFile));
			
			while ((line = br.readLine()) != null) {
	 	     // use comma as separator
				String[] flower = line.split(SplitBy);
				double[] param = {0.0,0.0,0.0,0.0};
				double outType[]={0,0,0};
				
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
		
		normalizeData(flowerList);
		
		
		int learn =0, test=0;
		for(Flower fl : flowerList){
			
			if(learn<30)
			{
				learnList.add(fl);
				learn++;
			}
			else
			{
				testList.add(fl);
				test++;
				if(test==20)
				{
					learn=0;
					test=0;
				}
			}
			
		}
		
		
		Collections.shuffle(learnList);
		while(count<learnList.size()){
			count=0;
			stop++;
			ArrayList<Double> error = new ArrayList<Double>();
			
			for(int i=0;i<learnList.size();i++){
			
				feedForward(inputLayer,hiddenLayer,outLayer,learnList.get(i),i);
				error.add(calError(outLayer,learnList.get(i),i));
				calDelta(hiddenLayer,outLayer,learnList.get(i),i);
				updateWeight(hiddenLayer,outLayer,i,0.3);	
				if(error.get(i)<errorRef){
					count++;
				}
			}			
			
			System.out.println("NextE");
			System.out.println(error);
			System.out.println("Small error");
			System.out.println(count);
		
			error.clear();
			if(stop==8000)
				break;
			clearValues(inputLayer,hiddenLayer,outLayer);
			
			
		
		}
		
		
		clearValues(inputLayer,hiddenLayer,outLayer);
		double cS=0, cV=0,cVir=0;
		
		for(int i=0;i<testList.size();i++){
			
			feedForward(inputLayer,hiddenLayer,outLayer,testList.get(i),i);
			
			double c1=0,c2=0,c3=0;
			c1=outLayer.nodes.get(0).value.get(i);
			c2=outLayer.nodes.get(1).value.get(i);
			c3=outLayer.nodes.get(2).value.get(i);
			
			String flowerType;
			if(c1>c2){
				if(c1>c3){
					flowerType="Iris-setosa";
					System.out.println("Actual is " + testList.get(i).type);
					System.out.println("Neural Network is " + c1 + " and " + c2 + " and " + c3);
					
				}
				else{
					flowerType="Iris-virginica";
					System.out.println("Actual is " + testList.get(i).type);
					System.out.println("Neural Network is " + c1 + " and " + c2 + " and " + c3);
					
				}
			}
			else if(c2>c3){
				flowerType="Iris-versicolor";
				System.out.println("Actual is " + testList.get(i).type);
				System.out.println("Neural Network is " + c1 + " and " + c2 + " and " + c3);
				
			}
			else{
				flowerType="Iris-virginica";
				System.out.println("Actual is " + testList.get(i).type);
				System.out.println("Neural Network is " + c1 + " and " + c2 + " and " + c3);
				
			}
			
			if(flowerType.equals(testList.get(i).type)){
				
				correct++;
				if(flowerType.equals("Iris-setosa"))
					cS++;
				else if(flowerType.equals("Iris-versicolor"))
					cV++;
				else if(flowerType.equals("Iris-virginica"))
					cVir++;
				
				
			}
				
			
		}
		
		System.out.println("Number of correct " + correct);
		System.out.println("Setose " + cS + " Versicolor " + cV + "Virginica " + cVir);
		
		
		
		
		
		

	}
	
	public static void normalizeData(ArrayList<Flower> flowerList){
		
		ArrayList<Double> col1 = new ArrayList<Double>();
		ArrayList<Double> col2 = new ArrayList<Double>();
		ArrayList<Double> col3 = new ArrayList<Double>();
		ArrayList<Double> col4 = new ArrayList<Double>();
		double maxCol1=0,maxCol2=0,maxCol3=0,maxCol4=0;
		double minCol1=0,minCol2=0,minCol3=0,minCol4=0;
	
		
		
		for(Flower fl : flowerList){
			
			col1.add(fl.param[0]);
			col2.add(fl.param[1]);
			col3.add(fl.param[2]);
			col4.add(fl.param[3]);
			
		}
	
		maxCol1= Collections.max(col1);
		maxCol2= Collections.max(col2);
		maxCol3= Collections.max(col3);
		maxCol4= Collections.max(col4);
		
		minCol1= Collections.min(col1);
		minCol2= Collections.min(col2);
		minCol3= Collections.min(col3);
		minCol4= Collections.min(col4);
			
		for(Flower fl: flowerList){
			
			fl.param[0]=(fl.param[0]-minCol1)/(maxCol1-minCol1);
			fl.param[1]=(fl.param[1]-minCol2)/(maxCol2-minCol2);
			fl.param[2]=(fl.param[2]-minCol3)/(maxCol3-minCol3);
			fl.param[3]=(fl.param[3]-minCol4)/(maxCol4-minCol4);
			
			
		}
	}
	
	public static void clearValues(Layers input, Layers hidden, Layers output){
		
		for(int i=0;i<input.nodes.size();i++){
			
			input.nodes.get(i).value.clear();
			
		}
		
		for(int i=0;i<hidden.nodes.size();i++){
			
			hidden.nodes.get(i).value.clear();
			
		}
		
		for(int i=0;i<output.nodes.size();i++){
	
			output.nodes.get(i).value.clear();
			
		}

	}
	
	public static double calError(Layers output, Flower fl, int index){
		
		double error=0;
		
		for(int i=0;i<output.nodes.size();i++){
			
			error+=Math.pow((output.nodes.get(i).value.get(index)-fl.outType[i]),2);
			
		}
		
		return error;
		
	}
	
	public static void feedForward(Layers input, Layers hidden, Layers output, Flower fl, int index){
		
		for(int i=0;i<input.nodes.size();i++){
			
			input.nodes.get(i).value.add(fl.param[i]);
			
		}
		
		for(int i=0;i<hidden.nodes.size();i++){
			
			double value1=0;
			double value2=0;
			
			for(int j=0;j<hidden.nodes.get(i).neuronsPrevious.size();j++){
				
				value1+=hidden.nodes.get(i).neuronsPrevious.get(j).weight*hidden.previousNodes.get(j).value.get(index);
			}
			value2 = 1/(1+Math.exp(-value1));
			hidden.nodes.get(i).value.add(value2);
			
		}
		
		for(int i=0;i<output.nodes.size();i++){
			
			double value1=0;
			double value2=0;
			
			for(int j=0;j<output.nodes.get(i).neuronsPrevious.size();j++){
				
				value1+=output.nodes.get(i).neuronsPrevious.get(j).weight*output.previousNodes.get(j).value.get(index);
			}
			value2 = 1/(1+Math.exp(-value1));
			output.nodes.get(i).value.add(value2);
			
		}
		
		
}
	
	public static void calDelta(Layers hidden, Layers output, Flower fl, int index){
		
		for(int i=0;i<output.nodes.size();i++){
		
			double value1=output.nodes.get(i).value.get(index);
			output.nodes.get(i).delta=value1*(1-value1)*(fl.outType[i]-value1);
			
		}
		
		for(int i=0;i<hidden.nodes.size();i++){
			double value1=hidden.nodes.get(i).value.get(index);
			double sum=0;
			for(int j=0;j<output.nodes.size();j++){
				
				sum+= (output.nodes.get(j).delta*hidden.nodes.get(i).neuronsForward.get(j).weight);
			
			}
			hidden.nodes.get(i).delta=value1*(1-value1)*(sum);
			
		}
		
	}
	
	public static void updateWeight(Layers hidden, Layers output, int index, double amp){
		
		for(int i=0;i<output.nodes.size();i++){
			
			
			for(int j=0;j<output.nodes.get(i).neuronsPrevious.size();j++){
				
				double deltaW=0,newW=0;
				
				deltaW=amp*output.nodes.get(i).delta*output.previousNodes.get(j).value.get(index);
				newW = deltaW + output.nodes.get(i).neuronsPrevious.get(j).weight;
				output.nodes.get(i).neuronsPrevious.get(j).updateWeight(newW);
				
			}
			
		}
		
		for(int i=0;i<hidden.nodes.size();i++){
			
			for(int j=0;j<hidden.nodes.get(i).neuronsPrevious.size();j++){
				
				double deltaW=0,newW=0;
				
				deltaW=amp*hidden.nodes.get(i).delta*hidden.previousNodes.get(j).value.get(index);
				newW = deltaW + hidden.nodes.get(i).neuronsPrevious.get(j).weight;
				hidden.nodes.get(i).neuronsPrevious.get(j).updateWeight(newW);
				
			}
			
		}
		
	}	
		
	
}


