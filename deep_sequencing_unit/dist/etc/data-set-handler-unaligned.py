'''
Expects as incoming folder: <FlowCell>/Unaligned_no_mismatch


Note: 
print statements go to: ~openbis/sprint/datastore_server/log/startup_log.txt
'''

import os
import glob
import shutil
import time
from java.lang import RuntimeException
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria

BASECALL_STATS_FOLDER = 'Basecall_Stats_'
REGEX_FILES = '*.*'
REGEX_MAKEFILE = 'Make*'
REGEX_LANES='/P*'
REGEX_UNDETERMINED = 'U*/Sample*'
UNALIGNED_FOLDER='Unaligned_no_mismatch'
LANE_FOLDER='/links/shared/dsu-dss/dss/incoming-jython-lanes/'
MARKER_STRING='.MARKER_is_finished_'

def touch_markerfile(filename):
  try:  
    # do a touch
    open(filename, 'w').close()
  except:
    print('Could not touch ' + filename)  


# Create a "transaction" -- a way of grouping operations together so they all
# happen or none of them do.
transaction = service.transaction()

incomingPath = incoming.getAbsolutePath()

# Get the incoming name and set the Data Set Type based on this 
name = incoming.getName()
split=name.split("_")
if (len(split) == 4):
  DSTYPE='ILLUMINA_HISEQ_OUTPUT'
  flowcell=name.split("_")[-1][1:]
if (len(split) ==2):
  DSTYPE='ILLUMINA_GA_OUTPUT'
  flowcell=name.split("_")[-1]
  # fix Illumina Script error: the GA FC Name does not contain a suffix A or B so noes not need to be removed
  # but Illumina removes it anyway, so we just revert this 
  os.rename(incomingPath + '/' + UNALIGNED_FOLDER + '/' +  BASECALL_STATS_FOLDER + name.split("_")[-1][1:], incomingPath + '/' + UNALIGNED_FOLDER +'/' +  BASECALL_STATS_FOLDER + flowcell)

#move Lanes into a different drop box
#print(incomingPath + '/'+ UNALIGNED_FOLDER + REGEX_LANES)
laneList=glob.glob(incomingPath + '/'+ UNALIGNED_FOLDER + REGEX_LANES)
laneList.sort()
[shutil.move(lane, LANE_FOLDER+lane.split('/')[-1]) for lane in laneList]
undeterminedList=glob.glob(incomingPath + '/'+ UNALIGNED_FOLDER + REGEX_UNDETERMINED)
[shutil.move(undeterminedLane, LANE_FOLDER+laneList[undeterminedLane.split('/')[-1][-1]]) for undeterminedLane in undeterminedList]
markerFileList = [touch_markerfile(LANE_FOLDER+MARKER_STRING+lane.split('/')[-1]) for lane in laneList]

# Create a data set and set type
dataSet = transaction.createNewDataSet("BASECALL_STATS")
dataSet.setMeasuredData(False)
  
# Build up a list of file which are part of the data set
fileList=glob.glob(incomingPath + '/'+ UNALIGNED_FOLDER + '/' + REGEX_FILES)
[fileList.append(i) for i in glob.glob(incomingPath + '/' + UNALIGNED_FOLDER +'/' +REGEX_MAKEFILE)]

# Add the incoming file into the data set
transaction.createNewDirectory(dataSet, UNALIGNED_FOLDER) 
# move all files is data set
[transaction.moveFile(file, dataSet, UNALIGNED_FOLDER)  for file in fileList]
# move base call stat dir into data set
transaction.moveFile(incomingPath + '/' + UNALIGNED_FOLDER + '/' +  BASECALL_STATS_FOLDER + flowcell, dataSet, UNALIGNED_FOLDER + '/')
  
# Get the search service
search_service = transaction.getSearchService()

# Search for the sample
sc = SearchCriteria()
sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, name))
foundSamples = search_service.searchForSamples(sc)

if foundSamples.size() > 0:
  dataSet.setSample(foundSamples[0])

  # Search for another data set of the same sample and make it a child of it 
  dataSetSc = SearchCriteria()
  dataSetSc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, DSTYPE))
  dataSetSc.addSubCriteria(SearchSubCriteria.createSampleCriteria(sc))
  foundDataSets = search_service.searchForDataSets(dataSetSc)
  if foundDataSets.size() > 0:
    dataSet.setParentDatasets([ds.getDataSetCode() for ds in foundDataSets])

shutil.rmtree(incomingPath)
