'''
@copyright:
2014 ETH Zuerich, CISD
    
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

@author:
Manuel Kohler
'''

import os
import shutil
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria

def renameFiles(dir, flowcellName):
  print dir 
  print flowcellName
  for root, dirs, files in os.walk(dir):
    for file in files:
      print root + file
      os.rename(root + '/' + file, root + "/" + flowcellName + "_" + file)


def process(transaction):

  # 140724_M01761_0084_000000000-A8TNL/

  incomingPath = transaction.getIncoming().getPath()
  incomingFolder = transaction.getIncoming().getName()
  flowLane = incomingFolder + ":1"

  # Get the search service
  search_service = transaction.getSearchService()

  # Search for the sample
  sc = SearchCriteria()
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, flowLane))
  foundSamples = search_service.searchForSamples(sc)

  if foundSamples.size() > 0:

    dataSet = transaction.createNewDataSet("UNDETERMINED_READS_DISTRIBUTION")
    dataSet.setMeasuredData(False)
    dataSet.setSample(foundSamples[0])
    transaction.moveFile(incomingPath, dataSet)
