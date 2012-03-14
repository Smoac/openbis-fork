'''
expected incoming Name for HiSeq2000 runs: 110715_SN792_0054_BC035RACXX
expected incoming Name for GAII runs: 110812_6353WAAXX

Note: 
print statements go to: ~openbis/sprint/datastore_server/log/startup_log.txt
'''

import os
import shutil
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria

# Create a "transaction" -- a way of grouping operations together so they all
# happen or none of them do.
transaction = service.transaction()

incomingPath = incoming.getAbsolutePath()
folders=[]
folders=os.listdir(incomingPath)

# Get the incoming name 
name = incoming.getName()

split=name.split("_")
if (len(split) == 4):
  IS_HISEQ_RUN=True
if (len(split) == 2):
  IS_HISEQ_RUN=False

# Get the search service
search_service = transaction.getSearchService()
 
# Search for the sample
sc = SearchCriteria()
sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, name));
foundSamples = search_service.searchForSamples(sc)

if foundSamples.size() > 0:
 
  # Search for another data set of the same sample and make it a child of it 
  sampleSc = SearchCriteria()
  sampleSc.addSubCriteria(SearchSubCriteria.createSampleContainerCriteria(sc))
  foundContainedSamples = search_service.searchForSamples(sampleSc)
  if foundContainedSamples.size() > 0:
    for fcs in range(0,foundContainedSamples.size()):
      dataSet = transaction.createNewDataSet("RUNINFO")
      dataSet.setMeasuredData(False)
      dataSet.setSample(foundContainedSamples[fcs])
      # Add the incoming file into the data set
      shutil.copytree(incomingPath, incomingPath + "_" + str(fcs))
      transaction.moveFile(incomingPath + "_" + str(fcs), dataSet)
  
shutil.rmtree(incomingPath)
