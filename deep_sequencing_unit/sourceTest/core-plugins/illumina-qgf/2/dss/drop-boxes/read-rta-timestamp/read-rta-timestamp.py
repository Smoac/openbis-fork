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
Reads out the timestamp of the RTAComplete.txt file to register the timestamp in openBIS

@note: 
print statements go to: <openBIS_HOME>/datastore_server/log/startup_log.txt

@author:
Manuel Kohler
'''

import os
import sys
import shutil
from time import *
from datetime import *
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from os.path import basename

# The following module is located in the path defined in the datastore_server.conf
# Look for: -Dpython.path}
from gfb_utils import *

MARKER_RUN_COMPLETE = 'RTAComplete.txt'  

def process(transaction):

    print "HELLO PATH: " + str(sys.path)

    incoming = transaction.getIncoming()
    incoming_path = incoming.getAbsolutePath()
    run_id = incoming.getName()
    model = get_model(run_id)
    
    thread_property_dict = get_thread_properties(transaction)
    absolutePath = os.path.dirname(os.path.realpath(thread_property_dict['script-path']))
    print(basename(absolutePath) + ": Auto-detected Illumina model: " + model)
    
    run_date, sequencer_id, running_number, tray_and_fcId = run_id.split("_")
    tray = tray_and_fcId[0]
    if model in [Sequencers.MISEQ]:
        fc_id = tray_and_fcId
    else:
        fc_id = tray_and_fcId[1:]

    marker_file = os.path.join(incoming_path, MARKER_RUN_COMPLETE)
    
    found_samples = search_unique_sample(transaction, fc_id)
    
    sa = transaction.getSampleForUpdate(found_samples[0].getSampleIdentifier())
    sa.setPropertyValue("SEQUENCER_FINISHED", create_openbis_timestamp(marker_file))
