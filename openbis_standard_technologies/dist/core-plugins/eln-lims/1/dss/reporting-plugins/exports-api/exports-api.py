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
import jarray

#Java Core
from java.io import ByteArrayInputStream
from org.apache.commons.io import IOUtils
from java.lang import String

#Zip Format
from java.io import File;
from java.io import FileInputStream;
from java.io import FileNotFoundException;
from java.io import FileOutputStream;
from java.util.zip import ZipEntry;
from java.util.zip import ZipOutputStream;

#To obtain the openBIS URL
from ch.systemsx.cisd.openbis.dss.generic.server import DataStoreServer;
OPENBISURL = DataStoreServer.getConfigParameters().getServerURL() + "/openbis/openbis";
V3_DSS_BEAN = "data-store-server_INTERNAL";

#V3 API - Metadata
from ch.systemsx.cisd.common.spring import HttpInvokerUtils;
from ch.ethz.sis.openbis.generic.asapi.v3 import IApplicationServerApi;

from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search import ProjectSearchCriteria;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search import ExperimentSearchCriteria;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search import SampleSearchCriteria;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search import DataSetSearchCriteria;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions import ProjectFetchOptions;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions import ExperimentFetchOptions;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleFetchOptions;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions import DataSetFetchOptions;

#V3 API - Files
from ch.ethz.sis.openbis.generic.dssapi.v3 import IDataStoreServerApi;
from ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search import DataSetFileSearchCriteria;
from ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions import DataSetFileFetchOptions;
from ch.systemsx.cisd.openbis.dss.generic.shared import ServiceProvider;
from ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.id import DataSetFilePermId;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id import DataSetPermId;
from ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.download import DataSetFileDownloadOptions;

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
	v3d = ServiceProvider.getApplicationContext().getBean(V3_DSS_BEAN);
	
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
				entityFound = { "type" : "PROJECT", "permId" : project.getPermId().getPermId() };
				entitiesToExport.append(entityFound);
				entitiesToExpand.append(entityFound);
		if type == "PROJECT":
			criteria = ExperimentSearchCriteria();
			criteria.withProject().withPermId().thatEquals(permId);
			results = v3.searchExperiments(sessionToken, criteria, ExperimentFetchOptions());
			print "Found: " + str(results.getTotalCount()) + " experiments";
			for experiment in results.getObjects():
				entityFound = { "type" : "EXPERIMENT", "permId" : experiment.getPermId().getPermId() };
				entitiesToExport.append(entityFound);
				entitiesToExpand.append(entityFound);
		if type == "EXPERIMENT":
			criteria = SampleSearchCriteria();
			criteria.withExperiment().withPermId().thatEquals(permId);
			results = v3.searchSamples(sessionToken, criteria, SampleFetchOptions());
			print "Found: " + str(results.getTotalCount()) + " samples";
			for sample in results.getObjects():
				entityFound = { "type" : "SAMPLE", "permId" : sample.getPermId().getPermId() };
				entitiesToExport.append(entityFound);
				entitiesToExpand.append(entityFound);
		if type == "SAMPLE":
			criteria = DataSetSearchCriteria();
			criteria.withSample().withPermId().thatEquals(permId);
			results = v3.searchDataSets(sessionToken, criteria, DataSetFetchOptions());
			print "Found: " + str(results.getTotalCount()) + " datasets";
			for dataset in results.getObjects():
				entityFound = { "type" : "DATASET", "permId" : dataset.getPermId().getPermId() };
				entitiesToExport.append(entityFound);
				entitiesToExpand.append(entityFound);
		if type == "DATASET":
			criteria = DataSetFileSearchCriteria();
			criteria.withDataSet().withPermId().thatEquals(permId);
			results = v3d.searchFiles(sessionToken, criteria, DataSetFileFetchOptions());
			print "Found: " + str(results.getTotalCount()) + " files";
			for file in results.getObjects():
				entityFound = { "type" : "FILE", "permId" : permId, "path" : file.getPath(), "isDirectory" : file.isDirectory() };
				entitiesToExport.append(entityFound);
				entitiesToExpand.append(entityFound);
	print "Found " + str(len(entitiesToExport)) + " entities to export.";
	params.put("entities", entitiesToExport);
	return export(tr, params);

def getFileName(spaceCode, projCode, expCode, sampCode, dataCode):
	fileName = "";
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
	return fileName;

def addToZipFile(path, file, zos):
		fis = FileInputStream(file);
		zipEntry = ZipEntry(path);
		zos.putNextEntry(zipEntry);

		bytes = jarray.zeros(1024, "b");
		length = fis.read(bytes);
		while length >= 0:
			zos.write(bytes, 0, length);
			length = fis.read(bytes);
		
		zos.closeEntry();
		fis.close();

def export(tr, params):
	sessionToken = params.get("sessionToken");
	v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi, OPENBISURL + IApplicationServerApi.SERVICE_URL, 30 * 1000);
	v3d = ServiceProvider.getApplicationContext().getBean(V3_DSS_BEAN);
	objectCache = {};
	objectMapper = GenericObjectMapper();
	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	
	#Create temporal folder
	rootDirFile = File.createTempFile(str(time.time()), None);
	rootDirFile.delete();
	rootDirFile.mkdir();
	rootDir = rootDirFile.getCanonicalPath();
	
	#Create Zip File
	fos = FileOutputStream(rootDir + ".zip");
	zos = ZipOutputStream(fos);
			
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
			entityFilePath = getFileName(entityObj.getSpace().getCode(), entityObj.getCode(), None, None, None);
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
			entityFilePath = getFileName(entityObj.getProject().getSpace().getCode(), entityObj.getProject().getCode(), entityObj.getCode(), None, None);
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
			entityFilePath = getFileName(entityObj.getExperiment().getProject().getSpace().getCode(), entityObj.getExperiment().getProject().getCode(), entityObj.getExperiment().getCode(), entityObj.getCode(), None);
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
			entityFilePath = getFileName(entityObj.getSample().getExperiment().getProject().getSpace().getCode(), entityObj.getSample().getExperiment().getProject().getCode(), entityObj.getSample().getExperiment().getCode(), entityObj.getSample().getCode(), entityObj.getCode());
		if type == "FILE" and not entity["isDirectory"]:
			datasetEntityObj = objectCache[entity["permId"]];
			datasetEntityFilePath = getFileName(datasetEntityObj.getSample().getExperiment().getProject().getSpace().getCode(), datasetEntityObj.getSample().getExperiment().getProject().getCode(), datasetEntityObj.getSample().getExperiment().getCode(), datasetEntityObj.getSample().getCode(), datasetEntityObj.getCode());
			filePath = datasetEntityFilePath + "/" + entity["path"];
			rawFileInputStream = v3d.downloadFiles(sessionToken, [DataSetFilePermId(DataSetPermId(permId), entity["path"])], DataSetFileDownloadOptions());
			rawFile = File(rootDir + filePath + ".json");
			rawFile.getParentFile().mkdirs();
			IOUtils.copyLarge(rawFileInputStream, FileOutputStream(rawFile));
			addToZipFile(filePath, rawFile, zos);
			
		if entityObj is not None:
			objectCache[permId] = entityObj;
		
		if entityObj is not None and entityFilePath is not None:
			entityJson = String(objectMapper.writeValueAsString(entityObj));
			jsonEntityFile = File(rootDir + entityFilePath + ".json");
			jsonEntityFile.getParentFile().mkdirs();
			IOUtils.write(entityJson.getBytes(), FileOutputStream(jsonEntityFile));
			addToZipFile(entityFilePath + ".json", jsonEntityFile, zos);
			
	zos.close();
	fos.close();
	print "Zip file found at: " + rootDir + ".zip"
	return True