'''

Note: 
print statements go to: ~openbis/sprint/datastore_server/log/startup_log.txt
'''

import os
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria

# Create a "transaction" -- a way of grouping operations together so they all
# happen or none of them do.
transaction = service.transaction()

incomingPath = incoming.getAbsolutePath()
folders=[]
# no walk in Jython 2.2
#for root, dirs, files in os.walk(folders):
#  folders.append(root)
folders=os.listdir(incomingPath)

# Get the incoming name 
name = incoming.getName()
# expected incoming Name, e.g.: 110715_SN792_0054_BC035RACXX

# Create a data set and set type
dataSet = transaction.createNewDataSet("BASECALL_STATS")
dataSet.setMeasuredData(False)

#dataSet.setPropertyValue("MISMATCH_IN_INDEX", r)
  
# Get the search service
search_service = transaction.getSearchService()
 
# Add the incoming file into the data set
transaction.moveFile(incomingPath, dataSet)
  
# Search for the sample
sc = SearchCriteria()
sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, name))
foundSamples = search_service.searchForSamples(sc)

if foundSamples.size() > 0:
  dataSet.setSample(foundSamples[0])

  # Search for another data set of the same sample
  dataSetSc = SearchCriteria()
  dataSetSc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, 'ILLUMINA_HISEQ_OUTPUT'))
  dataSetSc.addSubCriteria(SearchSubCriteria.createSampleCriteria(sc))
  foundDataSets = search_service.searchForDataSets(dataSetSc)
  if foundDataSets.size() > 0:
    dataSet.setParentDatasets([ds.getDataSetCode() for ds in foundDataSets])

