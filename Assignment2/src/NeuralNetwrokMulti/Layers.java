package neuralnetwork;

import java.util.ArrayList;

public class Layers {
	
	ArrayList<Node> nodes = new ArrayList<Node>();
	ArrayList<Neurons> neuronsF = new ArrayList<Neurons>();
	ArrayList<Node> previousNodes = new ArrayList<Node>();
	
	int units;
	
	
	public Layers(int numNodes,int numNeurons,Layers layer){
		
		
		for(int j=0;j<numNeurons;j++){
			this.neuronsF.add(new Neurons());
		
		}	
		
		
		
		for(int i=0;i<numNodes;i++){
			
			this.nodes.add(new Node());
			
			for(int j=0;j<(numNeurons/numNodes);j++){
				
				this.nodes.get(i).neuronsForward.add(this.neuronsF.get(i+j*(numNodes)));
			}	
			
			
		}
		if(layer!=null){
			
			for(int i=0;i<this.nodes.size();i++){
				
				for(int j=0;j<layer.nodes.size();j++){
				
					this.previousNodes.add(layer.nodes.get(j));
					this.nodes.get(i).neuronsPrevious.add(layer.neuronsF.get(j+i*layer.nodes.size()));
					
				}
				
			}
		}
		
		
	}
	
	
	
	public void setWeights(double[] weight){
		
		for(int i=0;i<this.neuronsF.size();i++){
			
			this.neuronsF.get(i).setWeight(weight[i]);
		}
		
		
	}
	
	
	

}
