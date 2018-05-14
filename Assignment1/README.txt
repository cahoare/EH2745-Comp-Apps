JAR (mysql, sqt, engine[matlab]) files are included are need to be updated in buildpath (currently point to wrong directory). 

****
Run.java
****
Key variables
equip - Array of equipment to store from CIM
dataNames - Array of array of possible equipment properties
dataIndex - Array of array of the data from dataNames that each equipment has - indexed by index of equip
dataSSHIndex - Similar to dataIndex for SSH data 

Methods
caseFileBuild() - Builds and saves the case file for matpower
runPowerFlow() - Calls Matlab/matpower through Java Engine API 
runYbus() - Calls Matlab/matpower through Java Engine API 
eqImport() - Creates an eqParse object. Builds the EQ database
sshImport() - Creates an sshParse object. Updates the database with SSH data.
topProcess() - Creates the topology processor. Calls all Topology processing methods for future use. 

****
ParseEQ.java
****
eqParse() - Parses using DOM. Searches for each element of the passed equip array. Builds new equip item for each object and stores in equip item array.
dbBuild() - For each EquipItem in the equip item array, populates a table for each equip item type (with columns from dataName variable) and then populates based on the data associated with that type.

****
EquipItem.java
****
dataGen() - Equipment item object. Iteratively searches through passed DOM node using type index and data hash table. Data stored in public variable.

****
ParseSSH.java
sshParse() - Operates the same as Parse EQ without dropping database
dbUpdate() - Similar to EQ but checks whether table exists first to allow sequential storage.

****
EquipData.java
****
- dataGen() - Equipment data object. Same as equipment item except value found is based on switch statements.

****
Topology.java
****
Key variables
pastPaths - bus to bus conducting equipment traversals.
futurePaths - known paths yet to be explored. 

dbBuildTopology() - Interfaces to database and pulls all information back in arraylists. These arraylists replicate sql tables. Table indexes are used to find data. 
It builds the pastPaths array of bus to bus traversals. It selects an initial bus, searches for possible paths, selects one and stores other sfuture paths and then 
calls explore paths. 
explorePath() - Searches from a conducting equipment attached to a bus to the next bus and stores whole path of conducting equipment in pastPath class variable. 
Searches bus connectivity node at end of path for futurePaths if it hasnt visited node before. Stores future paths as bus, CN and CE. 
dbAddAssignReq() - Adds base voltage levels to database as required by assignment. 
genBuild() - builds generator table for matpower.
busBuild() - builds bus data for matpower power flow (number of assumptions made to enable which are documented in program) 
branchBuild() - builds branch matrix for matpower power flow (calls buildJavaYbus as linked)
buildJavaYbus() - sets up javaYbus and calls matrixcalculation
MatrixCalculation() - creats two matrices, one with the real parts and one with the imaginary parts of the Ymatrix. This is done by identifying each rdfID stored in the 
paths with a certain piece of conducting equipment. At the same time it is taken the impedance information related to this conducting equipment in order to compute each 
element of the Ymatrix.