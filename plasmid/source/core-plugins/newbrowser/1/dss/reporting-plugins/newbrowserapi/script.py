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

from ch.systemsx.cisd.openbis.dss.client.api.v1 import DssComponentFactory
from org.apache.commons.io import IOUtils
from java.io import File
from java.io import FileOutputStream
from java.lang import System
from net.lingala.zip4j.core import ZipFile
import time

def process(tr, parameters, tableBuilder):
	method = parameters.get("method");
	
	isOk = False;
	if method == "insertSample":
		isOk = insertUpdateSample(tr, parameters, tableBuilder);
	if method == "updateSample":
		isOk = insertUpdateSample(tr, parameters, tableBuilder);
	if method == "insertExperiment":
		isOk = insertUpdateExperiment(tr, parameters, tableBuilder);
	if method == "updateExperiment":
		isOk = insertUpdateExperiment(tr, parameters, tableBuilder);
	if method == "insertDataSet":
		isOk = insertDataSet(tr, parameters, tableBuilder);

	if isOk:
		tableBuilder.addHeader("STATUS");
		tableBuilder.addHeader("MESSAGE");
		row = tableBuilder.addRow();
		row.setCell("STATUS","OK");
		row.setCell("MESSAGE", "Operation Successful");
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
  
def insertDataSet(tr, parameters, tableBuilder):
	#Mandatory parameters
	sampleIdentifier = parameters.get("sampleIdentifier"); #String
	dataSetType = parameters.get("dataSetType"); #String
	folderName = parameters.get("folderName"); #String
	fileNames = parameters.get("filenames"); #List<String>
	isZipDirectoryUpload = parameters.get("isZipDirectoryUpload"); #String
	metadata = parameters.get("metadata"); #java.util.LinkedHashMap<String, String> where the key is the name
		
	#Create Dataset
	dataSetSample = tr.getSample(sampleIdentifier);
	dataSet = tr.createNewDataSet(dataSetType);
	dataSet.setSample(dataSetSample);
	
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
	dss_component = DssComponentFactory.tryCreate(parameters.get("sessionID"), parameters.get("openBISURL"));
	
	for fileName in fileNames:
		folderFile = File(tempDir + "/" + folderName);
		folderFile.mkdir();
		temFile = File(tempDir + "/" + folderName + "/" + fileName);
		inputStream = dss_component.getFileFromSessionWorkspace(fileName);
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
		dss_component.deleteSessionWorkspaceFile(fileName);
	
	#Return from the call
	return True;
	
def insertUpdateSample(tr, parameters, tableBuilder):
	
	#Mandatory parameters
	sampleSpace = parameters.get("sampleSpace"); #String
	sampleProject = parameters.get("sampleProject"); #String
	sampleExperiment = parameters.get("sampleExperiment"); #String
	sampleCode = parameters.get("sampleCode"); #String
	sampleType = parameters.get("sampleType"); #String
	sampleProperties = parameters.get("sampleProperties"); #java.util.LinkedHashMap<String, String> where the key is the name
	
	#Optional parameters
	sampleParents = parameters.get("sampleParents"); #List<String> Identifiers are in SPACE/CODE format
	sampleChildrenNew = parameters.get("sampleChildrenNew"); #List<java.util.LinkedHashMap<String, String>>
	sampleChildren = parameters.get("sampleChildren"); #List<String> Identifiers are in SPACE/CODE format
	sampleChildrenRemoved = parameters.get("sampleChildrenRemoved"); #List<String> Identifiers are in SPACE/CODE format
	
	#Create/Get for update sample	
	sampleIdentifier = '/' + sampleSpace + '/' + sampleCode;
	
	method = parameters.get("method");
	if method == "insertSample":
		sample = tr.createNewSample(sampleIdentifier, sampleType); #Create Sample given his id
		
	if method == "updateSample":
		sample = tr.getSampleForUpdate(sampleIdentifier); #Retrieve Sample
	
	#Obtain space
	space = None;
	if sampleSpace != None:
		space = tr.getSpace(sampleSpace);
		if space == None:
			space = tr.createNewSpace(sampleSpace, None);
	
	#Obtain experiment
	experiment = None;
	if sampleSpace != None and sampleProject != None and sampleExperiment != None:
		experimentIdentifier = "/" + sampleSpace + "/" + sampleProject + "/" + sampleExperiment;
		experiment = tr.getExperiment(experimentIdentifier);
	
	#Assign experiment
	if experiment != None:
		sample.setExperiment(experiment);
	
	#Assign sample properties
	for key in sampleProperties.keySet():
		propertyValue = unicode(sampleProperties[key]);
		if propertyValue == "":
			propertyValue = None;
		
		sample.setPropertyValue(key,propertyValue);
		
	#Add sample parents
	if sampleParents != None:
		sample.setParentSampleIdentifiers(sampleParents);
	
	#Create new sample children
	if sampleChildrenNew != None:
		for newSampleChild in sampleChildrenNew:
			child = tr.createNewSample(newSampleChild["identifier"], newSampleChild["sampleTypeCode"]); #Create Sample given his id
			child.setParentSampleIdentifiers([sampleIdentifier]);
			for key in newSampleChild["properties"].keySet():
				propertyValue = unicode(newSampleChild["properties"][key]);
				if propertyValue == "":
					propertyValue = None;
				
				child.setPropertyValue(key,propertyValue);
		
	#Add sample children
	if sampleChildren != None:
		for sampleChildIdentifier in sampleChildren:
			child = tr.getSampleForUpdate(sampleChildIdentifier); #Retrieve Sample
			if child != None: #The new created ones will not be found
				childParents = child.getParentSampleIdentifiers();
				childParents.add(sampleIdentifier);
				child.setParentSampleIdentifiers(childParents);

	#Remove sample children
	if sampleChildrenRemoved != None:
		for sampleChildIdentifier in sampleChildrenRemoved:
			child = tr.getSampleForUpdate(sampleChildIdentifier); #Retrieve Sample
			if child != None: #The new created ones will not be found
				childParents = child.getParentSampleIdentifiers();
				childParents.remove(sampleIdentifier);
				child.setParentSampleIdentifiers(childParents);

	#Return from the call
	return True;
	
def insertUpdateExperiment(tr, parameters, tableBuilder):
	
	#Mandatory parameters
	experimentType = parameters.get("experimentType"); #String
	experimentSpace = parameters.get("experimentSpace"); #String
	experimentProject = parameters.get("experimentProject"); #String
	experimentCode = parameters.get("experimentCode"); #String
	experimentIdentifier = parameters.get("experimentIdentifier"); #String
	experimentProperties = parameters.get("experimentProperties"); #java.util.LinkedHashMap<String, String> where the key is the name
	
	experiment = None;
	method = parameters.get("method");
	if method == "insertExperiment":
		experiment = tr.createNewExperiment(experimentIdentifier, experimentType); #Create Experiment given his id
	if method == "updateExperiment":
		experiment = tr.getExperimentForUpdate(experimentIdentifier); #Retrieve Experiment
	
	for key in experimentProperties.keySet():
		propertyValue = unicode(experimentProperties[key]);
		if propertyValue == "":
			propertyValue = None;
		
		experiment.setPropertyValue(key,propertyValue);
	
	return True;