#
# Copyright 2014 ETH Zuerich, Scientific IT Services
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

# IDataSetRegistrationTransactionV2 Class
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClause, SearchOperator, MatchClauseAttribute
from ch.systemsx.cisd.openbis.dss.generic.shared import ServiceProvider

from org.apache.commons.io import IOUtils
from java.io import File
from java.io import FileOutputStream
from java.lang import System
from net.lingala.zip4j.core import ZipFile
from ch.systemsx.cisd.common.exceptions import UserFailureException

import time
import subprocess
import os.path
import re

def username(sessiontoken):
	m = re.compile('(.*)-[^-]*').match(sessiontoken)
	if m:
		return m.group(1)

def process(tr, parameters, tableBuilder):
	method = parameters.get("method");

	isOk = False;
	result = None;
	# Obtain the user using the service
	tr.setUserId(userId);
	
	if method == "insertDataSet":
		sampleIdentifier = parameters.get("sampleIdentifier"); #String
		experimentIdentifier = parameters.get("experimentIdentifier"); #String
		dataSetType = parameters.get("dataSetType"); #String
		folderName = parameters.get("folderName"); #String
		fileNames = parameters.get("filenames"); #List<String>
		isZipDirectoryUpload = parameters.get("isZipDirectoryUpload"); #String
		metadata = parameters.get("metadata"); #java.util.LinkedHashMap<String, String> where the key is the name
		isOk, result = insertDataSet(tr, sampleIdentifier, experimentIdentifier, dataSetType, folderName, fileNames, isZipDirectoryUpload, metadata, []);
	else: #pybis don't set the method, so we guess is a call from pybis
		sampleIdentifier = parameters.get("sampleId"); #String
		experimentIdentifier = parameters.get("experimentId"); #String
		parentIds = []
		if parameters.get('parentIds') is not None:
			for parentId in parameters.get('parentIds'):
				parentIds.append(parentId)

		if parameters.get("dataSets") is not None:
			for ds in parameters.get("dataSets"):
				dataSetType = ds.get("dataSetType"); #String
				metadata = ds.get("properties"); #java.util.LinkedHashMap<String, String> where the key is the name
				folderName = ds.get("folder"); #String
				if folderName is None:
					folderName = "default"
				fileNames = ds.get("fileNames"); #List<String>
				isOk, result = insertDataSet(tr, sampleIdentifier, experimentIdentifier, dataSetType, folderName, fileNames, False, metadata, parentIds);
	
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

def getThreadProperties(transaction):
  threadPropertyDict = {}
  threadProperties = transaction.getGlobalState().getThreadParameters().getThreadProperties()
  for key in threadProperties:
    try:
      threadPropertyDict[key] = threadProperties.getProperty(key)
    except:
      pass
  return threadPropertyDict

def insertDataSet(tr, sampleIdentifier, experimentIdentifier, dataSetType, folderName, fileNames, isZipDirectoryUpload, metadata, parentIds):
		
	#Create Dataset
	dataSet = tr.createNewDataSet(dataSetType);
	dataSet.setParentDatasets(parentIds);
	if sampleIdentifier is not None:
		dataSetSample = tr.getSampleForUpdate(sampleIdentifier);
		dataSet.setSample(dataSetSample);
	elif experimentIdentifier is not None:
		dataSetExperiment = tr.getExperimentForUpdate(experimentIdentifier);
		dataSet.setExperiment(dataSetExperiment);
		
	#Assign Data Set properties
	for key in metadata.keySet():
		propertyValue = unicode(metadata[key]);
		if propertyValue == "":
			propertyValue = None;
		dataSet.setPropertyValue(key,propertyValue);
	
	#Move All Files using a tmp directory close to the datastore
	threadProperties = getThreadProperties(tr);
	tempDir =  threadProperties[u'incoming-dir'] + "/tmp_eln/" + str(time.time());
	tempDirFile = File(tempDir);
	tempDirFile.mkdirs();
	
	#tempDir = System.getProperty("java.io.tmpdir");
	dss_service = ServiceProvider.getDssServiceRpcGeneric().getService();

	for fileName in fileNames:
		folderFile = File(tempDir + "/" + folderName);
		folderFile.mkdir();
		temFile = File(tempDir + "/" + folderName + "/" + fileName);
		inputStream = dss_service.getFileFromSessionWorkspace(userSessionToken, fileName);
		outputStream = FileOutputStream(temFile);
		IOUtils.copyLarge(inputStream, outputStream);
		IOUtils.closeQuietly(inputStream);
		IOUtils.closeQuietly(outputStream);
		
	#CASE - 1: Only one file as zip, uncompressed on the folder
	if fileNames.size() == 1 and isZipDirectoryUpload:
		temFile = File(tempDir + "/" + folderName + "/" + fileNames.get(0));
		tempFolder = tempDir + "/" +  folderName;
		zipFile = ZipFile(temFile.getAbsolutePath());
		zipFile.extractAll(tempFolder);
		temFile.delete();
		tr.moveFile(tempFolder, dataSet);
	elif fileNames.size() > 1: #CASE - 2: Multiple files on the folder
		temFile = File(tempDir + "/"+ folderName);
		tr.moveFile(temFile.getAbsolutePath(), dataSet);
	else: #CASE - 3: One file only
		temFile = File(tempDir + "/" + folderName + "/" + fileNames.get(0));
		tr.moveFile(temFile.getAbsolutePath(), dataSet);
	
	#Clean Files from workspace
	for fileName in fileNames:
		dss_service.deleteSessionWorkspaceFile(userSessionToken, fileName);
	
	#Return from the call
	return True, dataSet.getDataSetCode()
