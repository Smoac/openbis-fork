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
    runFolder, lane = folderName.rsplit("-", 1)
    
    # check if it is a NextSeq run, as we use only the Flowcell code as a Sample code 
    if ("NS" in runFolder):
        return runFolder.split("_")[-1][1:] + ":" + lane
        
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
    # MiSeq: 141217_D00535_0040_BC6GHLANXX-1
    # NextSeq: 141217_NS500318_0034_AH1766BGXX-1
    # HiSeq: 141204_D00535_0035_BC5LPVANXX-8

    incomingFolder = transaction.getIncoming().getName()
    flowLaneName = splitFolderName(incomingFolder) 
    flowLane = searchSample(transaction, flowLaneName)

    dataSet = transaction.createNewDataSet("UNDETERMINED_READS_DISTRIBUTION")
    dataSet.setMeasuredData(False)
    dataSet.setSample(flowLane)
    transaction.moveFile(transaction.getIncoming().getPath(), dataSet)
