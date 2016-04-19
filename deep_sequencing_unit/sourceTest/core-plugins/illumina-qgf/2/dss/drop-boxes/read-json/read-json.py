'''
@copyright:
2016 ETH Zuerich, SIS
    
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

import re
import glob
import os
from itertools import islice
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

# The following module is located in the path defined in the datastore_server.conf
# Look for: -Dpython.path}
from gfb_utils import *

import json


def get_json_from_file(file):
    with open(file, 'r') as f:
        read_json = f.read()
    return json.loads(read_json)[0]

def get_lane_count(json_string):
    return int(json_string['lanecount'])


def get_flowcell_id(json_string):
    return json_string['name']


def register_in_openbis(transaction, json_string):
    lane_count = get_lane_count(json_string)
    flowcell_id = get_flowcell_id(json_string)

    found_flow_cell = search_unique_sample(transaction, flowcell_id)
    search_service = transaction.getSearchService()
    
    flowcell = found_flow_cell[0]

    get_flowcell_with_contained_samples = search_service.getSample(flowcell.getSampleIdentifier())
    flowlanes = get_flowcell_with_contained_samples.getContainedSamples()
    for lane in flowlanes:
        mutable_lane = transaction.getSampleForUpdate(lane.getSampleIdentifier())
        lane_number = lane.getCode().split(":")[-1]
        
        try:
            aligned_mean = json_string['cluster_lane_dict'][str(lane_number)]['aligned'][0]['mean']
        except KeyError:
            print ("Problem occurred with aligned value for PhiX")
            aligned_mean = -1
        
        try:
            cluster_density_mean = json_string['cluster_lane_dict'][str(lane_number)]['clusterDensity'][0]['mean']
        except KeyError:
            print ("Problem occurred with cluster density")
            cluster_density_mean = -1
            
        try:
            error_rate_mean = json_string['error_metrics'][str(lane_number)]['mean']
        except KeyError:
            print ("Problem occurred with error rate")
            error_rate_mean = -1
        
        mutable_lane.setPropertyValue("PERC_PHIX_ALIGNED", str(aligned_mean))
        mutable_lane.setPropertyValue("CLUSTER_DENSITY", str(cluster_density_mean))
        mutable_lane.setPropertyValue("ERROR_RATE", str(error_rate_mean))


def process(transaction):
    incoming = transaction.getIncoming()
    incoming_path = incoming.getAbsolutePath()
    
    json_file = glob.glob(os.path.join(incoming_path, "*.json"))
    
    if json_file:
        json_string = get_json_from_file(json_file[0])
        register_in_openbis(transaction, json_string)
    else:
        print("No json file found!")