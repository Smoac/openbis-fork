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
from ch.systemsx.cisd.openbis.dss.client.api.v1 import DssComponentFactory
from org.apache.commons.io import IOUtils
from java.io import File
from java.io import FileOutputStream
from java.lang import System
from net.lingala.zip4j.core import ZipFile
from ch.systemsx.cisd.common.exceptions import UserFailureException
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClause, SearchOperator, MatchClauseAttribute
from ch.ethz.ssdm.eln import PlasMapperUploaderWrapper
import time
import subprocess
import os.path
from ch.systemsx.cisd.common.ssl import SslCertificateHelper;

#Plasmapper Configuration
#Configure ./PlasMapper/WEB-INF/classes/ca/ualberta/xdong/plasMapper/annotate/plasMapConfiguration_en_CA

PLASMAPPER_BASE_URL = "https://localhost:8443/PlasMapper"
PLASMAPPER_SERVER_ROOT = "/Users/juanf/Documents/installations/S200/servers/openBIS-server/jetty/webapps"
            
def getSampleByIdentifierForUpdate(tr, identifier):
	space = identifier.split("/")[1];
	code = identifier.split("/")[2];
	
	criteria = SearchCriteria();
	criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.SPACE, space));
	criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, code));
	criteria.setOperator(SearchOperator.MATCH_ALL_CLAUSES);
	
   	searchService = tr.getSearchService();
   	found = list(searchService.searchForSamples(criteria));
   	if len(found) == 1:
   		return tr.makeSampleMutable(found[0]);
   	else:
   		raise UserFailureException(identifier + " Not found by search service.");
   	
def process(tr, parameters, tableBuilder):
	method = parameters.get("method");
	
	isOk = False;
	
	# Obtain the user using the dropbox
	sessionToken = parameters.get("sessionToken"); #String
	sessionId = sessionToken.split("-")[0]; #String
	tr.setUserId(sessionId);
	
	if method == "init":
		isOk = init(tr, parameters, tableBuilder);
	if method == "registerUserPassword":
		isOk = registerUserPassword(tr, parameters, tableBuilder);
	
	if method == "insertProject":
		isOk = insertUpdateProject(tr, parameters, tableBuilder);
	if method == "updateProject":
		isOk = insertUpdateProject(tr, parameters, tableBuilder);
	
	if method == "insertExperiment":
		isOk = insertUpdateExperiment(tr, parameters, tableBuilder);
	if method == "updateExperiment":
		isOk = insertUpdateExperiment(tr, parameters, tableBuilder);
	
	if method == "copySample":
		isOk = copySample(tr, parameters, tableBuilder);
	if method == "insertSample":
		isOk = insertUpdateSample(tr, parameters, tableBuilder);
	if method == "updateSample":
		isOk = insertUpdateSample(tr, parameters, tableBuilder);
	
	if method == "insertDataSet":
		isOk = insertDataSet(tr, parameters, tableBuilder);
	if method == "updateDataSet":
		isOk = updateDataSet(tr, parameters, tableBuilder);
	
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

def init(tr, parameters, tableBuilder):
	inventorySpace = tr.getSpace("DEFAULT_LAB_NOTEBOOK");
	if inventorySpace == None:
		tr.createNewSpace("MATERIALS", None);
		
		tr.createNewProject("/MATERIALS/REAGENTS");
		tr.createNewExperiment("/MATERIALS/REAGENTS/ANTIBODIES", 		"ANTIBODY");
		tr.createNewExperiment("/MATERIALS/REAGENTS/CHEMICALS", 		"BACTERIA");
		tr.createNewExperiment("/MATERIALS/REAGENTS/ENZYMES", 			"CHEMICAL");
		tr.createNewExperiment("/MATERIALS/REAGENTS/MEDIA", 			"MEDIA");
		tr.createNewExperiment("/MATERIALS/REAGENTS/SOLUTIONS_BUFFERS",	"SOLUTION_BUFFER");
		
		tr.createNewProject("/MATERIALS/BACTERIA");
		tr.createNewExperiment("/MATERIALS/BACTERIA/BACTERIA_COLLECTION_1",		"BACTERIA");
		tr.createNewProject("/MATERIALS/CELL_LINES");
		tr.createNewExperiment("/MATERIALS/CELL_LINES/CELL_LINE_COLLECTION_1",	"CELL_LINE");
		tr.createNewProject("/MATERIALS/FLIES");
		tr.createNewExperiment("/MATERIALS/FLIES/FLY_COLLECTION_1",				"FLY");
		tr.createNewProject("/MATERIALS/YEASTS");
		tr.createNewExperiment("/MATERIALS/YEASTS/YEAST_COLLECTION_1",			"YEAST");
		tr.createNewProject("/MATERIALS/PLASMIDS");
		tr.createNewExperiment("/MATERIALS/PLASMIDS/PLASMID_COLLECTION_1",		"PLASMID");
		tr.createNewProject("/MATERIALS/POLYNUCLEOTIDES");
		tr.createNewExperiment("/MATERIALS/POLYNUCLEOTIDES/OLIGO_COLLECTION_1",	"OLIGO");
		tr.createNewExperiment("/MATERIALS/POLYNUCLEOTIDES/RNA_COLLECTION_1",	"RNA");
		
		tr.createNewSpace("METHODS", None);
		
		tr.createNewProject("/METHODS/PROTOCOLS");
		tr.createNewExperiment("/METHODS/PROTOCOLS/GENERAL_PROTOCOLS", 			"GENERAL_PROTOCOL");
		tr.createNewExperiment("/METHODS/PROTOCOLS/PCR_PROTOCOLS", 				"PCR_PROTOCOL");
		tr.createNewExperiment("/METHODS/PROTOCOLS/WESTERN_BLOTTING_PROTOCOLS", 	"WESTERN_BLOTTING_PROTOCOL");
		
		tr.createNewSpace("DEFAULT_LAB_NOTEBOOK", None);
		
		tr.createNewProject("/DEFAULT_LAB_NOTEBOOK/DEFAULT_PROJECT");
		tr.createNewExperiment("/DEFAULT_LAB_NOTEBOOK/DEFAULT_PROJECT/DEFAULT_EXPERIMENT", 	"DEFAULT_EXPERIMENT");
	
	return True;

def registerUserPassword(tr, parameters, tableBuilder):
	userId = parameters.get("userId"); #String
	password = parameters.get("password"); #String
	path = '../openBIS-server/jetty/bin/passwd.sh';
	if os.path.isfile(path):
		subprocess.call([path, 'add', userId, '-p', password]) #Adds the user, if the user exists, will fail
		subprocess.call([path, 'change', userId, '-p', password]) #Changes the user pass, works always
		return True;
	else:
		return False;
	
def getThreadProperties(transaction):
  threadPropertyDict = {}
  threadProperties = transaction.getGlobalState().getThreadParameters().getThreadProperties()
  for key in threadProperties:
    try:
      threadPropertyDict[key] = threadProperties.getProperty(key)
    except:
      pass
  return threadPropertyDict
  
def insertUpdateProject(tr, parameters, tableBuilder):
	method = parameters.get("method"); #String
	projectIdentifier = parameters.get("projectIdentifier"); #String
	projectDescription = parameters.get("projectDescription"); #String
	
	project = None;
	if method == "insertProject":
		project = tr.createNewProject(projectIdentifier);
	if method == "updateProject":
		project = tr.getProjectForUpdate(projectIdentifier);
	
	project.setDescription(projectDescription);
	
	#Return from the call
	return True;
	
def updateDataSet(tr, parameters, tableBuilder):
	dataSetCode = parameters.get("dataSetCode"); #String
	metadata = parameters.get("metadata"); #java.util.LinkedHashMap<String, String> where the key is the name
	dataSet = tr.getDataSetForUpdate(dataSetCode);
	
	#Assign Data Set properties
	for key in metadata.keySet():
		propertyValue = unicode(metadata[key]);
		if propertyValue == "":
			propertyValue = None;
		
		dataSet.setPropertyValue(key,propertyValue);
	
	#Return from the call
	return True;

def insertDataSet(tr, parameters, tableBuilder):
	#Mandatory parameters
	sampleIdentifier = parameters.get("sampleIdentifier"); #String
	dataSetType = parameters.get("dataSetType"); #String
	folderName = parameters.get("folderName"); #String
	fileNames = parameters.get("filenames"); #List<String>
	isZipDirectoryUpload = parameters.get("isZipDirectoryUpload"); #String
	metadata = parameters.get("metadata"); #java.util.LinkedHashMap<String, String> where the key is the name
		
	#Create Dataset
	dataSetSample = getSampleByIdentifierForUpdate(tr, sampleIdentifier);
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
		if 	temFile.getName().endswith(".fasta") and dataSetType == "SEQ_FILE" and PLASMAPPER_BASE_URL != None and PLASMAPPER_SERVER_ROOT != None:
			futureSVG = File(tempDir + "/" + folderName + "/generated/" + temFile.getName().replace(".fasta", ".svg"));
			futureHTML = File(tempDir + "/" + folderName + "/generated/" + temFile.getName().replace(".fasta", ".html"));
			print "SVG: " + futureSVG.getAbsolutePath();
			print "SVG: " + str(futureSVG.exists());
			print "BEFORE PLASMAPPER";
			#Only for development
			#SslCertificateHelper.trustAnyCertificate("https://localhost:8443/PlasMapper");
			PlasMapperUploaderWrapper.uploadAndCopyGeneratedFile(
                PLASMAPPER_BASE_URL,
                PLASMAPPER_SERVER_ROOT,
                temFile.getAbsolutePath(),
                futureSVG.getAbsolutePath()
        	);
			print "AFTER PLASMAPPER";
			print "SVG: " + str(futureSVG.exists());
			tr.moveFile(temFile.getParentFile().getAbsolutePath(), dataSet);
		else:
			tr.moveFile(temFile.getAbsolutePath(), dataSet);
	#Clean Files from workspace
	for fileName in fileNames:
		dss_component.deleteSessionWorkspaceFile(fileName);
	
	#Return from the call
	return True;
	
def copySample(tr, parameters, tableBuilder):
	#Store Children to copy later
	sampleSpace = parameters.get("sampleSpace"); #String
	sampleCode = parameters.get("sampleCode"); #String
	sampleIdentifier = '/' + sampleSpace + '/' + sampleCode;
	
	sampleChildren = parameters.get("sampleChildren"); #List<String> Identifiers are in SPACE/CODE format
	parameters.put("sampleChildren", []); #List<String> Identifiers are in SPACE/CODE format
	
	#Create new Sample
	parameters.put("method", "insertSample"); #List<String> Identifiers are in SPACE/CODE format
	insertUpdateSample(tr, parameters, tableBuilder);
	
	#Copy children and attach to Sample
	if sampleChildren != None:
		for sampleChildIdentifier in sampleChildren:
			child = getSampleByIdentifierForUpdate(tr, sampleChildIdentifier); #Retrieve Sample child to copy
			
			copyChildCode = None
			try: #For autogenerated children that have the code as sufix
				indexFromCopiedChildrenParentCode = child.getCode().index('_')
				copyChildCode = parameters.get("sampleCode") + child.getCode()[indexFromCopiedChildrenParentCode:];
			except: #For all other children
				copyChildCode = parameters.get("sampleCode") + "_" + child.getCode();

			copyChildIdentifier = "/" + parameters.get("sampleSpace") + "/" + copyChildCode;
			
			# Create new sample children
			childCopy = tr.createNewSample(copyChildIdentifier, child.getSampleType()); #Create Sample given his id
			childParents = childCopy.getParentSampleIdentifiers();
			childParents.add(sampleIdentifier);
			childCopy.setParentSampleIdentifiers(childParents);
			searchService = tr.getSearchService();
			propertiesDefinitions = searchService.listPropertiesDefinitionsForSampleType(child.getSampleType());
			for propertyDefinition in propertiesDefinitions:
				propCode = propertyDefinition.getPropertyTypeCode();
				propValue = getCopySampleChildrenPropertyValue(
					propCode,
					child.getPropertyValue(propCode),
					parameters.get("notCopyProperties"),
					parameters.get("defaultBenchPropertyList"),
					parameters.get("defaultBenchProperties")
				);
				if propValue != None:
					childCopy.setPropertyValue(propCode, propValue);
			
	return True;

#This method is used to return the properties, deleting the storage ones and setting the default storage
def getCopySampleChildrenPropertyValue(propCode, propValue, notCopyProperties, defaultBenchPropertyList, defaultBenchProperties):
	isPropertyToSkip = any(propCode == s for s in notCopyProperties);
	isDefaultBenchProperty = any(propCode == s for s in defaultBenchPropertyList);
	print propCode + " " + str(isPropertyToSkip) + " " + str(isDefaultBenchProperty);
	if isPropertyToSkip:
		return None;
	elif isDefaultBenchProperty:
		return str(defaultBenchProperties[propCode]);
	else:
		return propValue;
	
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
		sample = getSampleByIdentifierForUpdate(tr, sampleIdentifier); #Retrieve Sample
	
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
			child = getSampleByIdentifierForUpdate(tr, sampleChildIdentifier); #Retrieve Sample
			if child != None: #The new created ones will not be found
				childParents = child.getParentSampleIdentifiers();
				childParents.add(sampleIdentifier);
				child.setParentSampleIdentifiers(childParents);

	#Remove sample children
	if sampleChildrenRemoved != None:
		for sampleChildIdentifier in sampleChildrenRemoved:
			child = getSampleByIdentifierForUpdate(tr, sampleChildIdentifier); #Retrieve Sample
			if child != None: #The new created ones will not be found
				childParents = child.getParentSampleIdentifiers();
				childParents.remove(sampleIdentifier);
				child.setParentSampleIdentifiers(childParents);

	#Return from the call
	return True;
	
def insertUpdateExperiment(tr, parameters, tableBuilder):
	
	#Mandatory parameters
	experimentType = parameters.get("experimentType"); #String
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