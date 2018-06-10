RUN

%%%% General comments %%%%%
% To interpret the program the main thing to note is the way that the clusters are stored 
% Three dimensional data structure of ArrayList <ArrayList <double []>> is used to store the data
% The hierachy is - cluster type -> each data point in the cluster -> data values
% ArrayLists where preferred to separate objects to provide hierachy visability 
% These also provided easy looping ability to populate for various cluster numbers
%%%%%%%%%%%%%%%%%%%%%%%%%%%

setUpTest() 
 - set up test data from GUI

findOptimalK() 
 - finds the optimal k for nearest neighbour using a K finder object
 - will look to maximise k up until errors are found
 - maximum k is hard coded at 15

runKNN() 
 - run K Neighbours in  k the classifier file 
 - Note that this will add the new data to the existing clusters
 - Every subsequent run will add further data in. This was considered and decided most appropriate

setUpData()
 - Set up kMeans data from gui

outputFileBuild() 
 - Build csv file of clusters for interrogation
 - Description - uses the folder directory chosen from GUI. Writes the values from topology processing into format needed by matpower.
   Saves as file "casefile". Will overwrite file if in directory.

printCentroids() 
 - Print centroids to a string

printClassify() 
 - Find states and print to string

findOptKCluster() 
 - Select optimal cluster number

clusterData() 
 - Cluster the data using the clusterer

*************************
Identification

importMeasure ()
 - Reads in the data from the SQL file, once loaded into SQL manually

*************************
Kmeans

clusterData() 
 - Method called to cluster the values with different number of clusters and different initialisation
 - Args: the number of clusters desired and a random variable 
 - Returns: the clustered data

checkDist()
 - Checks the distance between the centroids against a specified error
 - Args: the two centroid locations
 - Returns: boolean true if distance is greater than the error

calCentroid()
 - Calculates the centroid locations
 - Args: the clustered data
 - Returns: the centroid locations of the clusters. Public is also called from run. 

sortClusters ()
 - Sorts the data by best fitting cluster
 - Args: existing clustered data and centroid locations
 - Returns: the new data clusters

calCost ()
 - Finds the overall cost value of the clusters
 - Args: the clustered data
 - Returns: the cost value
	
clustClassify ()
 - Classify the states from the centroids. Uses the method described in the accompanying pdf.
 - Args: the clustered data
 - Returns: a string array of each state


*************************
KNNfinder

optimumk()
 - Find optimum k for kNN
 - Description - Uses the training data to find the best k value
 - Returns - Best value for k 

distancecalculation()
 - Calculate the distance from the sample to each other value in the clusters
 - Param - Is the sampled training data
 - Returns - Sorted distance array with values

public int getIndexOfLargest()
 - Finds the index of the array that value should be stored in


*************************
kNN

classification() 
 - Method - calculates distance between the value and all terms and returns distance
 - Param - sample being tested
 - Returns - sorted array of distances

distancecalculation()
 - Method - calculates distance between the value and all terms and returns distance
 - Param - sample being tested
 - Returns - sorted array of distances

getIndexOfLargest()
 - Finds the index of the array that value should be stored in




