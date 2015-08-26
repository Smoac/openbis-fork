'''
@copyright:
2015 ETH Zuerich, SIS
    
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

@note
print statements go to: <openBIS_HOME>/datastore_server/log/startup_log.txt

@author:
Manuel Kohler
'''

# The following module is located in the path defined in the datastore_server.conf
# Look for: -Dpython.path}
from gfb_utils import *


def process(transaction):

    incomingPath = transaction.getIncoming().getPath()
    incomingFolder = transaction.getIncoming().getName()
    
    split = incomingFolder.split("_")
    
    if (len(split) == 2):
      flow_cell, flow_lane = incomingFolder.split("_")
    elif (len(split) == 3):
        flow_cell = "-".join([split[0], split[1]])
        flow_lane = split[-1]
    
    connected_sample_code = ":".join([flow_cell, flow_lane])
    connected_sample = search_unique_sample(transaction, connected_sample_code)
    
    dataSet = transaction.createNewDataSet("FASTQC")
    dataSet.setMeasuredData(False)
    
    dataSet.setSample(connected_sample[0])
    transaction.moveFile(incomingPath, dataSet)