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


def splitFolderName (folderName):
    runFolder, lane = folderName.rsplit("_", 1)
    return runFolder + ":" + lane

def searchSample (transaction, sampleCode):
    # Get the search service
    search_service = transaction.getSearchService()

    print("Searching for " + sampleCode)
    # Search for the sample
    sc = SearchCriteria()
    sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sampleCode))
    foundSamples = search_service.searchForSamples(sc)
    assert(len(foundSamples), 1)
    return foundSamples[0]

def process(transaction):

    # expected incoming folder names:
    # 120917_SN792_0158_AC125KACXX_4

    incomingFolder = transaction.getIncoming().getName()
    flowLaneName = splitFolderName(incomingFolder) 
    flowLane = searchSample(transaction, flowLaneName)

    dataSet = transaction.createNewDataSet("ALIGNMENT")
    dataSet.setMeasuredData(False)
    dataSet.setSample(flowLane)
    transaction.moveFile(transaction.getIncoming().getPath(), dataSet)
