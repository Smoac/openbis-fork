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

def process(tr, parameters, tableBuilder):
	method = parameters.get("method");
	
	isOk = False;
	if method == "insertSample":
		isOk = insertSample(tr, parameters, tableBuilder);
	if method == "updateSample":
		isOk = insertSample(tr, parameters, tableBuilder);

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

def insertSample(tr, parameters, tableBuilder):

	#Mandatory parameters
	sampleSpace = parameters.get("sampleSpace"); #String
	sampleProject = parameters.get("sampleProject"); #String
	sampleCode = parameters.get("sampleCode"); #String
	sampleType = parameters.get("sampleType"); #String
	sampleProperties = parameters.get("sampleProperties"); #java.util.LinkedHashMap<String, String> where the key is the name
	
	#Optional parameters
	sampleParents = parameters.get("sampleParents"); #List<String> Identifiers are in SPACE/CODE format
	
	#Only used to create an experiment with the same code as the sample for the case of the ELN experiment
	sampleExperimentCreate = parameters.get("sampleExperimentCreate"); #Boolean
	sampleExperimentCode = parameters.get("sampleExperimentCode"); #String
	sampleExperimentType = parameters.get("sampleExperimentType"); #String
	sampleExperimentProject = parameters.get("sampleExperimentProject"); #String
	
	#Create/Get for update sample	
	sampleIdentifier = '/' + sampleSpace + '/' + sampleCode

	method = parameters.get("method");
	if method == "insertSample":
		sample = tr.createNewSample(sampleIdentifier, sampleType); #Create Sample given his id
		
	if method == "updateSample":
		sample = tr.getSampleForUpdate(sampleIdentifier) #Retrieve Sample
	
	#Obtain experiment identifier
	experimentIdentifier = None
	if sampleSpace != None and sampleExperimentProject != None and sampleExperimentCode != None:
		experimentIdentifier = '/' +sampleSpace+ '/' + sampleExperimentProject + '/' +sampleExperimentCode;

	#Obtain experiment
	experiment = None
	if sampleExperimentCreate and experimentIdentifier != None and sampleExperimentType != None:
		experiment = tr.createNewExperiment(experimentIdentifier, sampleExperimentType);
	elif experimentIdentifier != None:
		experiment = tr.getExperiment(experimentIdentifier);
	
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