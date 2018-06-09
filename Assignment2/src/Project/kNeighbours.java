package Project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

public class kNeighbours {
	
	private static ArrayList<ArrayList<double []>> trainingvalues = new ArrayList<ArrayList<double []>>();
	private static ArrayList<ArrayList<double []>> clustersvalues = new ArrayList<ArrayList<double []>>();
	private static ArrayList<ArrayList<double []>> clustersvaluescloned = new ArrayList<ArrayList<double []>>();
	private static ArrayList<ArrayList<double []>> distancesmatrix = new ArrayList<ArrayList<double []>>();
	private static int ntsamples = 5; // Number of samples per cluster taken as training samples for calculating the optimum number of k neighbours
	public kNeighbours(ArrayList<ArrayList<double []>> clustersvalues, int k){
		int x=0;
	    int i;
		while(x<clustersvalues.size()){  // to clone the arrayList cluestersvalues
			i=0;
			ArrayList<double[]> auxiliar = new ArrayList<double[]>();
			while(i<clustersvalues.get(x).size()){ // We take as training set the nt first samples of each cluster - later we will compare the results
						auxiliar.add(clustersvalues.get(x).get(i));
						i++;
						}
			clustersvaluescloned.add(auxiliar);
		x++;}
		x=0;
		Random rand = new Random();
		while(x<clustersvalues.size()){ 
			i=0;
			ArrayList<double[]> auxiliar = new ArrayList<double[]>();
			while(i<ntsamples){ // We take as training set the nt first samples of each cluster - later we will compare the results
						
				int randomIndex = rand.nextInt(clustersvaluescloned.get(x).size());
				auxiliar.add(clustersvaluescloned.get(x).get(randomIndex));
				clustersvaluescloned.get(x).remove(randomIndex);
						auxiliar.add(clustersvaluescloned.get(x).get(i));
						i++;
						}
		trainingvalues.add(auxiliar);
		x++;}
		/*System.out.println("Lets see");
		System.out.println(trainingvalues.get(0).size());
		System.out.println(trainingvalues.get(1).get(4));
		System.out.println(Arrays.toString(trainingvalues.get(1).get(4)));
		System.out.println(Arrays.toString(trainingvalues.get(2).get(4)));
		System.out.println(Arrays.toString(trainingvalues.get(3).get(4)));
		int a;
		for(a=0; a<trainingvalues.get(1).get(4).length; a++){
		System.out.println(trainingvalues.get(1).get(4)[a]);}*/
			
	}
	
	public int optimumk(){
		int optk=0;
		int bestmatching=0;
		int maxk=10; // the number of neighbours tried is maxk-1
	
		int[] bestk=new int[maxk];
		for(int k=0; k<maxk;k++){
			for(int o=0; o<trainingvalues.size();o++){
				for(int b=0; b<trainingvalues.get(o).size(); b++){
					int[] firstkn=new int[trainingvalues.size()]; 
					ArrayList<double []> sorteddistances = new ArrayList<double []>();
					sorteddistances=distancecalculation(trainingvalues.get(o).get(b));
					for(int u=0; u<=k;u++){ // 
							firstkn[(int)sorteddistances.get(u)[1]]++; // how many times we find a certain cluster 
					}
				
					if(getIndexOfLargest(firstkn)==o){ // which is the cluster that appears more times in the first k neighbours
					bestk[k]++;} // if the cluster that appears more times is the same as the real cluster of the sample give by "o"
					// then the process was successful and therefore this k is good
					
				}
				
			}
		
		}
		
		// the best k is the one that was more successful 
		bestmatching=bestk[0];
		for(int i=0; i< bestk.length;i++){
			
			if(bestk[i]>bestmatching){
				bestmatching=bestk[i];
				optk=i;
				}
		}
		return optk+1;
	}
	
	public ArrayList<double []> distancecalculation (double[] trainingsample){
		
		ArrayList<double []> distancelist = new ArrayList<double []>();
		ArrayList<double []> distancelistsorted = new ArrayList<double []>();
		ArrayList<Double> distances= new ArrayList<Double>();
		ArrayList<Double> cluster= new ArrayList<Double>();
		
		for(int o=0; o<clustersvaluescloned.size();o++){
				for(int p=0; p<clustersvaluescloned.get(o).size();p++){
					double[] distance = new double[2];
					distance[0] =0 ;
					for(int a=0;a<clustersvaluescloned.get(o).get(p).length;a++){
					distance[0] += Math.pow((trainingsample[a]-clustersvaluescloned.get(o).get(p)[a]),2);
					}
					distance[0]= Math.sqrt(distance[0]);
					distance[1]=(double)(o); // to indicate that distance is to a esceario in cluster o
					distancelist.add(distance);
				}	
		}
		for(int a=0;a<distancelist.size();a++){
			distances.add(distancelist.get(a)[0]);
			cluster.add(distancelist.get(a)[1]);
		}
		 Collections.sort(distances); // to sort the distances
		 
		for(int a=0;a<distances.size();a++){
			for(int b=0;b<distancelist.size();b++){
				if(distances.get(b).equals(distancelist.get(a)[0])){
					double[] aux= new double[2];
					aux[0]=distances.get(a);
					aux[1]=distancelist.get(b)[1];
					distancelistsorted.add(aux);
				}
				else{}
			}
		}
	 return distancelistsorted;	
	}
			

public int getIndexOfLargest( int[] array )
{
  if ( array == null || array.length == 0 ) return -1; // null or empty

  int largest = 0;
  for ( int i = 1; i < array.length; i++ )
  {
      if ( array[i] > array[largest] ) largest = i;
  }
  return largest; // position of the first largest found
}

}
