'''
expected incoming Name for HiSeq2000 runs: 110715_SN792_0054_BC035RACXX
expected incoming Name for GAII runs: 110812_6353WAAXX

Note: 
print statements go to: ~openbis/sprint/datastore_server/log/startup_log.txt
'''

from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

IS_HISEQ_RUN = False

DATASET_TYPE_HISEQ = "ILLUMINA_HISEQ_OUTPUT"
DATASET_TYPE_GA = "ILLUMINA_GA_OUTPUT"

# Create a "transaction" -- a way of grouping operations together so they all
# happen or none of them do.
transaction = service.transaction()

incomingPath = incoming.getAbsolutePath()

# Get the incoming name 
name = incoming.getName()

split = name.split("_")
if (len(split) == 4):
  dataSet = transaction.createNewDataSet(DATASET_TYPE_HISEQ)
  IS_HISEQ_RUN = True
if (len(split) == 2):
  dataSet = transaction.createNewDataSet(DATASET_TYPE_GA)

# Create a data set and set type
dataSet.setMeasuredData(False)
  
# Get the search service
search_service = transaction.getSearchService()
 
# Search for the sample
sc = SearchCriteria()
sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, name));
foundSamples = search_service.searchForSamples(sc)

if foundSamples.size() > 0:
  # Add the incoming file into the data set
  transaction.moveFile(incomingPath, dataSet)
  dataSet.setSample(foundSamples[0])
