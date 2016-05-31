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
import time

#Java Core
from java.io import ByteArrayInputStream
from java.lang import String

#To obtain the openBIS URL
from ch.systemsx.cisd.openbis.dss.generic.server import DataStoreServer
from __builtin__ import None
OPENBISURL = DataStoreServer.getConfigParameters().getServerURL() + "/openbis/openbis"

#V1 API - To manage session workspace
from ch.systemsx.cisd.openbis.dss.client.api.v1 import DssComponentFactory

#V3 API
from ch.systemsx.cisd.common.spring import HttpInvokerUtils;
from ch.ethz.sis.openbis.generic.asapi.v3 import IApplicationServerApi

from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search import ProjectSearchCriteria
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search import ExperimentSearchCriteria
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search import SampleSearchCriteria
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search import DataSetSearchCriteria
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions import ProjectFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions import ExperimentFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions import DataSetFetchOptions

#JSON
from ch.ethz.sis.openbis.generic.server.sharedapi.v3.json import GenericObjectMapper;
from com.fasterxml.jackson.databind import SerializationFeature

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
	v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi, OPENBISURL + IApplicationServerApi.SERVICE_URL, 30 * 1000);
	entity = params.get("entity");
	entityAsPythonMap = { "type" : entity.get("type"), "permId" : entity.get("permId") };
	entitiesToExport = [entityAsPythonMap];
	entitiesToExpand = deque([entityAsPythonMap]);
	
	while entitiesToExpand:
		entityToExpand = entitiesToExpand.popleft();
		type =  entityToExpand["type"];
		permId = entityToExpand["permId"];
		print "Expanding type: " + str(type) + " permId: " + str(permId);
		
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

def getFileName(rootDir, spaceCode, projCode, expCode, sampCode, dataCode, ext):
	fileName = rootDir;
	if spaceCode is not None:
		fileName += "/" + spaceCode;
	if projCode is not None:
		fileName += "/" + projCode;
	if expCode is not None:
		fileName += "/" + expCode;
	if sampCode is not None:
		fileName += "/" + sampCode;
	if dataCode is not None:
		fileName += "/" + dataCode;
	fileName += "." + ext;
	return fileName;

def export(tr, params):
	sessionToken = params.get("sessionToken");
	v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi, OPENBISURL + IApplicationServerApi.SERVICE_URL, 30 * 1000);
	objectMapper = GenericObjectMapper();
	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	dss_component = DssComponentFactory.tryCreate(params.get("sessionToken"), OPENBISURL);
	rootDir = str(time.time());
	
	for entity in params.get("entities"):
		type =  entity["type"];
		permId = entity["permId"];
		print "exporting type: " + str(type) + " permId: " + str(permId);
		entityObj = None;
		entityFilePath = None;
		
		if type == "SPACE":
			pass #Do nothing
		if type == "PROJECT":
			criteria = ProjectSearchCriteria();
			criteria.withPermId().thatEquals(permId);
			fetchOps = ProjectFetchOptions();
			fetchOps.withSpace();
			fetchOps.withRegistrator();
			fetchOps.withModifier();
			entityObj = v3.searchProjects(sessionToken, criteria, fetchOps).getObjects().get(0);
			entityFilePath = getFileName(rootDir, entityObj.getSpace().getCode(), entityObj.getCode(), None, None, None, "json");
		if type == "EXPERIMENT":
			criteria = ExperimentSearchCriteria();
			criteria.withPermId().thatEquals(permId);
			fetchOps = ExperimentFetchOptions();
			fetchOps.withType();
			fetchOps.withProject().withSpace();
			fetchOps.withRegistrator();
			fetchOps.withModifier();
			fetchOps.withProperties();
			fetchOps.withTags();
			entityObj = v3.searchExperiments(sessionToken, criteria, fetchOps).getObjects().get(0);
			entityFilePath = getFileName(rootDir, entityObj.getProject().getSpace().getCode(), entityObj.getProject().getCode(), entityObj.getCode(), None, None, "json");
		if type == "SAMPLE":
			criteria = SampleSearchCriteria();
			criteria.withPermId().thatEquals(permId);
			fetchOps = SampleFetchOptions();
			fetchOps.withType();
			fetchOps.withExperiment().withProject().withSpace();
			fetchOps.withRegistrator();
			fetchOps.withModifier();
			fetchOps.withProperties();
			fetchOps.withTags();
			fetchOps.withParents();
			fetchOps.withChildren();
			entityObj = v3.searchSamples(sessionToken, criteria, fetchOps).getObjects().get(0);
			entityFilePath = getFileName(rootDir, entityObj.getExperiment().getProject().getSpace().getCode(), entityObj.getExperiment().getProject().getCode(), entityObj.getExperiment().getCode(), entityObj.getCode(), None, "json");
		if type == "DATASET":
			criteria = DataSetSearchCriteria();
			criteria.withPermId().thatEquals(permId);
			fetchOps = DataSetFetchOptions();
			fetchOps.withType();
			fetchOps.withSample().withExperiment().withProject().withSpace();
			fetchOps.withRegistrator();
			fetchOps.withModifier();
			fetchOps.withProperties();
			fetchOps.withTags();
			fetchOps.withParents();
			fetchOps.withChildren();
			entityObj = v3.searchDataSets(sessionToken, criteria, fetchOps).getObjects().get(0);
			entityFilePath = getFileName(rootDir, entityObj.getSample().getExperiment().getProject().getSpace().getCode(), entityObj.getSample().getExperiment().getProject().getCode(), entityObj.getSample().getExperiment().getCode(), entityObj.getSample().getCode(), entityObj.getCode(), "json");
			
		if entityObj is not None and entityFilePath is not None:
			entityJson = String(objectMapper.writeValueAsString(entityObj));
			dss_component.putFileToSessionWorkspace(entityFilePath, ByteArrayInputStream(entityJson.getBytes()));
			print entityJson;
			
	return True