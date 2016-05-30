#
# Copyright 2016 ETH Zuerich, Scientific IT Services
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

from collections import deque
import re

#V3 API
from ch.systemsx.cisd.common.spring import HttpInvokerUtils;
from ch.ethz.sis.openbis.generic.asapi.v3 import IApplicationServerApi

#To obtain the openBIS URL
from ch.systemsx.cisd.openbis.dss.generic.server import DataStoreServer
OPENBISURL = DataStoreServer.getConfigParameters().getServerURL() + "/openbis/openbis"



def username(sessiontoken):
    m = re.compile('(.*)-[^-]*').match(sessiontoken)
    if m:
        return m.group(1)

def process(tr, params, tableBuilder):
	method = params.get("method");
	isOk = False;
	result = None;
	
	# Set user using the dropbox
	tr.setUserId(userId);
	
	if method == "exportAll":
		isOk = exportAll(tr, params);

	if isOk:
		tableBuilder.addHeader("STATUS");
		tableBuilder.addHeader("MESSAGE");
		tableBuilder.addHeader("RESULT");
		row = tableBuilder.addRow();
		row.setCell("STATUS","OK");
		row.setCell("MESSAGE", "Operation Successful");
		row.setCell("RESULT", result);
	else :
		tableBuilder.addHeader("STATUS");
		tableBuilder.addHeader("MESSAGE");
		row = tableBuilder.addRow();
		row.setCell("STATUS","FAIL");
		row.setCell("MESSAGE", "Operation Failed");
		
def exportAll(tr, params):
	entitiesToExport = [];
	
	entitiesToExpand = deque([params.get("entity")]);
	while entitiesToExpand:
		entityToExpand = entitiesToExpand.popleft();
		type =  entityToExpand.get("type");
		permId = entityToExpand.get("permId");
		print "Expanding type: " + type + " permId: " + permId;
		v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi, OPENBISURL + IApplicationServerApi.SERVICE_URL, 30 * 1000);
# 		if type == "SPACE":
# 			getSpaceProjects();
# 		if type == "PROJECT":
# 			getProjectSamples(); 
# 			getProjectExperiments();
# 		if type == "EXPERIMENT":
# 			getExperimentSamples();
# 			getExperimentDataSets();
# 		if type == "SAMPLE":
# 			getSampleDataSets();
	
	params.put("entities", entitiesToExport);
	return export(tr, params);

def export(tr, params):
	return True