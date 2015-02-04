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

@description:
Registers an incoming directory as a data set in openBIS. The name of the directory is used to
search for the matching sample. 

@note: 
print statements go to: <openBIS_HOME>/datastore_server/log/startup_log.txt
expected incoming Name for HiSeq runs: 110715_SN792_0054_BC035RACXX
expected incoming Name for GAII runs: 110812_6353WAAXX

@author:
Manuel Kohler
'''

import os
import subprocess
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

def process(transaction):

    args = ['/links/application/dsu/export_QGF_master_data/export_QGF_master_data.sh']

    try:
        p = subprocess.Popen(args, stdout=subprocess.PIPE)
        print(p.communicate()[0])
    except:
        print("Could not run: " + str(args))

    incomingPath = transaction.getIncoming().getPath()
    name = transaction.getIncoming().getName()

    split=name.split("_")
    if (len(split) == 4):
        dataSet = transaction.createNewDataSet("ILLUMINA_HISEQ_OUTPUT")

    dataSet.setMeasuredData(False)
  
    search_service = transaction.getSearchService()  
    sc = SearchCriteria()
    sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, name));
    foundSamples = search_service.searchForSamples(sc)

    if foundSamples.size() > 0:
        transaction.moveFile(incomingPath, dataSet)
        dataSet.setSample(foundSamples[0])