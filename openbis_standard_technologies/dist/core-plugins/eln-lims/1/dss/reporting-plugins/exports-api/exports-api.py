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

from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search import ProjectSearchCriteria
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search import ExperimentSearchCriteria
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search import SampleSearchCriteria
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search import DataSetSearchCriteria

from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions import ProjectFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions import ExperimentFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions import DataSetFetchOptions

def username(sessiontoken):
    m = re.compile('(.*)-[^-]*').match(sessiontoken)
    if m:
        return m.group(1)

def process(tr, params, tableBuilder):
	method = params.get("method");
	isOk = False;
	result = None;
	
	# Set user using the Dropbox
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
	sessionToken = params.get("sessionToken");
	entity = params.get("entity");
	entityAsPythonMap = { "type" : entity.get("type"), "permId" : entity.get("permId") };
	entitiesToExport = [entityAsPythonMap];
	entitiesToExpand = deque([entityAsPythonMap]);
	
	while entitiesToExpand:
		entityToExpand = entitiesToExpand.popleft();
		type =  entityToExpand["type"];
		permId = entityToExpand["permId"];
		print "Expanding type: " + str(type) + " permId: " + str(permId);
		v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi, OPENBISURL + IApplicationServerApi.SERVICE_URL, 30 * 1000);
		
		if type == "SPACE":
			criteria = ProjectSearchCriteria();
			criteria.withSpace().withCode().thatEquals(permId);
			results = v3.searchProjects(sessionToken, criteria, ProjectFetchOptions());
			print "Found: " + str(results.getTotalCount()) + " projects";
			for project in results.getObjects():
				entitiesToExport.append({ "type" : "PROJECT", "permId" : project.getPermId().getPermId() });
				entitiesToExpand.append({ "type" : "PROJECT", "permId" : project.getPermId().getPermId() });
		if type == "PROJECT":
			criteria = ExperimentSearchCriteria();
			criteria.withProject().withPermId().thatEquals(permId);
			results = v3.searchExperiments(sessionToken, criteria, ExperimentFetchOptions());
			print "Found: " + str(results.getTotalCount()) + " experiments";
			for experiment in results.getObjects():
				entitiesToExport.append({ "type" : "EXPERIMENT", "permId" : experiment.getPermId().getPermId() });
				entitiesToExpand.append({ "type" : "EXPERIMENT", "permId" : experiment.getPermId().getPermId() });
 		if type == "EXPERIMENT":
 			criteria = SampleSearchCriteria();
			criteria.withExperiment().withPermId().thatEquals(permId);
			results = v3.searchSamples(sessionToken, criteria, SampleFetchOptions());
			print "Found: " + str(results.getTotalCount()) + " samples";
			for sample in results.getObjects():
				entitiesToExport.append({ "type" : "SAMPLE", "permId" : sample.getPermId().getPermId() });
				entitiesToExpand.append({ "type" : "SAMPLE", "permId" : sample.getPermId().getPermId() });
 		if type == "SAMPLE":
 			criteria = DataSetSearchCriteria();
 			criteria.withSample().withPermId().thatEquals(permId);
 			results = v3.searchDataSets(sessionToken, criteria, DataSetFetchOptions());
 			print "Found: " + str(results.getTotalCount()) + " datasets";
			for dataset in results.getObjects():
				entitiesToExport.append({ "type" : "DATASET", "permId" : dataset.getPermId().getPermId() });
				entitiesToExpand.append({ "type" : "DATASET", "permId" : dataset.getPermId().getPermId() });
	
	print "Found " + str(len(entitiesToExport)) + " entities to export.";
	params.put("entities", entitiesToExport);
	return export(tr, params);

def export(tr, params):
	return True