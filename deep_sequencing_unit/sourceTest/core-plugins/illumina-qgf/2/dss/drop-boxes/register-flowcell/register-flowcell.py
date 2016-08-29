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

ILLUMINA_HISEQ_OUTPUT_DS_TYPE = "ILLUMINA_HISEQ_OUTPUT"
ILLUMINA_MISEQ_OUTPUT_DS_TYPE = "ILLUMINA_MISEQ_OUTPUT"
ILLUMINA_NEXTSEQ_OUTPUT_DS_TYPE = "ILLUMINA_NEXTSEQ_OUTPUT"


def get_bcl_version(file):
    pattern = re.compile("bcl2fastq")
    matching_line_list = []
    bcl_version = "Not specified"
    number_of_lines_to_read = 3

    if file:
        with open(file[0]) as nohup:
            head = list(islice(nohup, number_of_lines_to_read))
        for line in head:
            if re.search(pattern, line):
                matching_line_list.append(line)
    else:
        print("File " + str(file) + " not found!")

    if matching_line_list:
        bcl_version = matching_line_list[0].strip()
    print("GOT BCL2FASTQ Version: " + bcl_version)
    return bcl_version


def process(transaction):
    incoming = transaction.getIncoming()
    incoming_path = incoming.getAbsolutePath()
    run_id = incoming.getName()
    model = get_model(run_id)
    
    if model in HISEQ_LIST:
        DATASET_TYPE = ILLUMINA_HISEQ_OUTPUT_DS_TYPE
    elif model in [Sequencers.NEXTSEQ_500]:
        DATASET_TYPE = ILLUMINA_NEXTSEQ_OUTPUT_DS_TYPE
    elif model in [Sequencers.MISEQ]:
        DATASET_TYPE = ILLUMINA_MISEQ_OUTPUT_DS_TYPE
    else:
        print("Could set a data set type for flowcell data!")
    
    thread_property_dict = get_thread_properties(transaction)
    absolutePath = os.path.dirname(os.path.realpath(thread_property_dict['script-path']))
    print(os.path.basename(absolutePath) + ": Auto-detected Illumina model: " + model)
    
    run_date, sequencer_id, running_number, tray_and_fcId = run_id.split("_")
    tray = tray_and_fcId[0]
    if model in [Sequencers.MISEQ]:
        fc_id = tray_and_fcId
    else:
        fc_id = tray_and_fcId[1:]

    file = glob.glob(os.path.join(incoming_path, "nohup*"))
    bcl_version = get_bcl_version(file)

    found_flow_cell = search_unique_sample(transaction, fc_id)
    
    search_service = transaction.getSearchService()
    get_flowcell_with_contained_samples = search_service.getSample(found_flow_cell[0].getSampleIdentifier())
    flowlanes = get_flowcell_with_contained_samples.getContainedSamples()
    
    for lane in flowlanes:
        mutable_lane = transaction.getSampleForUpdate(lane.getSampleIdentifier())
        mutable_lane.setPropertyValue("BCL_VERSION", bcl_version)
    
    dataSet = transaction.createNewDataSet(DATASET_TYPE)
    dataSet.setMeasuredData(False)
    transaction.moveFile(incoming_path, dataSet)
    dataSet.setSample(found_flow_cell[0])
