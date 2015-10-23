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

@description:
Registers an incoming directory as a data set in openBIS. The name of the directory is used to
search for the matching sample. 

@note: 
print statements go to: <openBIS_HOME>/datastore_server/log/startup_log.txt

@author:
Manuel Kohler
'''

import re
import glob
import os
from itertools import islice
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

# The following module is located in the path defined in the datastore_server.conf
# Look for: -Dpython.path}
from gfb_utils import *

BCL2FASTQ_BASECALLSTATS_TYPE = "BCL2FASTQ_BASECALLSTATS"
AGGREGATED_BASECALL_STATS_TYPE = "AGGREGATED_BASECALL_STATS"

def process(transaction):
    aggregated_run = False
    incoming = transaction.getIncoming()
    incoming_path = incoming.getAbsolutePath()
    run_id = incoming.getName()
    
    thread_property_dict = get_thread_properties(transaction)
    absolutePath = os.path.dirname(os.path.realpath(thread_property_dict['script-path']))
    
    try:
        run_date, sequencer_id, running_number, tray_and_fcId, lane = run_id.split("_")
        dataSet = transaction.createNewDataSet(BCL2FASTQ_BASECALLSTATS_TYPE)
        model = get_model(run_id.rsplit("_",1)[:-1][0])
    except:
        run_date, sequencer_id, running_number, tray_and_fcId = run_id.split("_")
        dataSet = transaction.createNewDataSet(AGGREGATED_BASECALL_STATS_TYPE)
        aggregated_run = True
        model = get_model(run_id)
        
    print(os.path.basename(absolutePath) + ": Auto-detected Illumina model: " + model)
    
    tray = tray_and_fcId[0]
    if model in [Sequencers.MISEQ]:
        fc_id = tray_and_fcId
    else:
        fc_id = tray_and_fcId[1:]

    if aggregated_run:
        sample_code = fc_id
    else:
        sample_code = fc_id + ":" + lane
    found_sample = search_unique_sample(transaction, sample_code)
    search_service = transaction.getSearchService()
    
    dataSet.setMeasuredData(False)
    transaction.moveFile(incoming_path, dataSet)
    dataSet.setSample(found_sample[0])
