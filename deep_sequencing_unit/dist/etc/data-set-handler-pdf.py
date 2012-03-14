'''

Note: 
print statements go to: ~openbis/sprint/datastore_server/log/startup_log.txt
'''

import os
import fnmatch
import re
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria

FOLDER='/links/shared/dsu-dss/dss/incoming-jython-pdf/'
PDF_PATTERN='*.pdf'

matches = []
searchStrings = ['@PG']
programList = []

# Create a "transaction" -- a way of grouping operations together so they all
# happen or none of them do.
transaction = service.transaction()
 
# Create a data set and set type
dataSet = transaction.createNewDataSet("QUALITY_PDFS")
dataSet.setMeasuredData(False)

incomingPath = incoming.getAbsolutePath()

# Get the incoming name 
# expected: 
# Project_110907_SN792_0059_AC012FACXX_3/Sample_BSSE-DSU-1662/BSSE-DSU-1662_CGATGTA_L003_R1_001_sorted.pdf
name = incoming.getName()
split=name.split('_')
if (len(split) == 6): 
  incoming_sample=split[1]+ '_'+ split[2] + '_' + split[3] + '_' + split[4]+ ':' + split[-1]
if (len(split) ==4):
  incoming_sample=split[1]+ '_'+ split[2] + ':' + split[-1]


# Looking for PDFS
for root, dirnames, filenames in os.walk(FOLDER + name):
  for filename in fnmatch.filter(filenames, PDF_PATTERN):
      matches.append(os.path.join(root, filename))

 
# Add the incoming file into the data set
transaction.moveFile(incomingPath, dataSet)

# Get the search service
search_service = transaction.getSearchService()

# Search for the sample
sc = SearchCriteria()
sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, incoming_sample));
foundSamples = search_service.searchForSamples(sc)

if foundSamples.size() > 0:
  dataSet.setSample(foundSamples[0])
  
  # Search for parent data set of the same sample
  dataSetSc = SearchCriteria()
  dataSetSc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, 'FASTQ_GZ'))
  dataSetSc.addSubCriteria(SearchSubCriteria.createSampleCriteria(sc))
  foundDataSets = search_service.searchForDataSets(dataSetSc)
  if foundDataSets.size() > 0:
    dataSet.setParentDatasets([ds.getDataSetCode() for ds in foundDataSets])
