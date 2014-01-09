#
# Copyright 2013 ETH Zuerich, CISD
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

def process(tr, parameters, tableBuilder):
	method = parameters.get("method");
	
	isOk = False;
	if method == "insertSample":
		isOk = insertSample(tr, parameters, tableBuilder);
	if method == "updateSample":
		isOk = insertSample(tr, parameters, tableBuilder);
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

def insertDataSet(tr, parameters, tableBuilder):
	#Mandatory parameters
	sampleIdentifier = parameters.get("sampleIdentifier"); #String
	dataSetType = parameters.get("dataSetType"); #String
	fileSessionKey = parameters.get("fileSessionKey"); #String
	filename = parameters.get("filename"); #String
	metadata = parameters.get("metadata"); #java.util.LinkedHashMap<String, String> where the key is the name
	
	dataSetSample = tr.getSample(sampleIdentifier);
	dataSet = tr.createNewDataSet(dataSetType);
	dataSet.setSample(dataSetSample);
	
	#Assign Data Set properties
	for key in metadata.keySet():
		propertyValue = unicode(metadata[key]);
		if propertyValue == "":
			propertyValue = None;
		
		dataSet.setPropertyValue(key,propertyValue);
	
	#Move File
	dss_component = DssComponentFactory.tryCreate(parameters.get("sessionID"), parameters.get("openBISURL"));
	inputStream = dss_component.getFileFromSessionWorkspace(fileSessionKey);
	rawFile = IOUtils.toByteArray(inputStream);
	tempDir = System.getProperty("java.io.tmpdir");
	file = File(tempDir + filename);
	FileOutputStream(file).write(rawFile);
	print file.getAbsolutePath();
	tr.moveFile(file.getAbsolutePath(), dataSet);
	
	#Clean File from workspace
	dss_component.deleteSessionWorkspaceFile(fileSessionKey);
	
	#Return from the call
	return True;
	
def insertSample(tr, parameters, tableBuilder):
	
	#Mandatory parameters
	sampleSpace = parameters.get("sampleSpace"); #String
	sampleProject = parameters.get("sampleProject"); #String
	sampleCode = parameters.get("sampleCode"); #String
	sampleType = parameters.get("sampleType"); #String
	sampleProperties = parameters.get("sampleProperties"); #java.util.LinkedHashMap<String, String> where the key is the name
	
	#Optional parameters
	sampleParents = parameters.get("sampleParents"); #List<String> Identifiers are in SPACE/CODE format
	
	#Used to create the experiment if doesn't exist already
	sampleExperimentCode = parameters.get("sampleExperimentCode"); #String
	sampleExperimentType = parameters.get("sampleExperimentType"); #String
	sampleExperimentProject = parameters.get("sampleExperimentProject"); #String
	
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
			
	#Obtain project
	project = None;
	if sampleSpace != None and sampleExperimentProject != None:
		projectIdentifier = '/' +sampleSpace+ '/' + sampleExperimentProject;
		project = tr.getProject(projectIdentifier);
		if project == None:
			project = tr.createNewProject(projectIdentifier);
	
	#Obtain experiment
	experiment = None;
	if sampleSpace != None and sampleExperimentProject != None and sampleExperimentCode != None:
		experimentIdentifier = '/' +sampleSpace+ '/' + sampleExperimentProject + '/' +sampleExperimentCode;
		experiment = tr.getExperiment(experimentIdentifier);
		if experiment == None:
			experiment = tr.createNewExperiment(experimentIdentifier, sampleExperimentType);
	
	#Assign experiment
	if experiment != None:
		sample.setExperiment(experiment);
	elif sample.getExperiment() != None:
		sample.setExperiment(None);
	
	#Assign sample properties
	for key in sampleProperties.keySet():
		propertyValue = unicode(sampleProperties[key]);
		if propertyValue == "":
			propertyValue = None;
		
		sample.setPropertyValue(key,propertyValue);
		
	#Add sample parents
	if sampleParents != None:
		sample.setParentSampleIdentifiers(sampleParents);
	
	#Return from the call
	return True;