'''
@copyright:
2012 ETH Zuerich, CISD
    
@license: 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    
http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@note: 
print statements go to: <openBIS_HOME>/datastore_server/log/startup_log.txt

@author:
Manuel Kohler
'''

import os
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria

def process(transaction):

  incomingPath = transaction.getIncoming().getPath()
  runFolder = transaction.getIncoming().getName()
  flowCellId = runFolder.split("_")[-1][1:]
  
  # Create a data set and set type
  dataSet = transaction.createNewDataSet("NEXTSEQ_BASECALL_STATS")
  dataSet.setMeasuredData(False)

  # Get the search service
  search_service = transaction.getSearchService()
   
  # Add the incoming file into the data set
  transaction.moveFile(incomingPath, dataSet)
    
  # Search for the sample
  sc = SearchCriteria()
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, flowCellId))
  foundSamples = search_service.searchForSamples(sc)

  if foundSamples.size() > 0:
    dataSet.setSample(foundSamples[0])
