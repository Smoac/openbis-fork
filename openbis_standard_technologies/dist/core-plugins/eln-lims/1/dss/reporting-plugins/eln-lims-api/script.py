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
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClause, SearchOperator, MatchClauseAttribute
from ch.systemsx.cisd.openbis.generic.shared.basic.dto import DataTypeCode;
 
from java.util import ArrayList
from java.util import Date;
from java.text import SimpleDateFormat;

from ch.systemsx.cisd.common.spring import HttpInvokerUtils;
from org.apache.commons.io import IOUtils
from java.io import File
from java.io import FileOutputStream
from java.lang import System
from net.lingala.zip4j.core import ZipFile
from ch.systemsx.cisd.common.exceptions import UserFailureException

from ch.ethz.ssdm.eln import PlasmapperConnector

import time
import subprocess
import os.path
import re

from java.io import StringWriter
from org.htmlcleaner import HtmlCleaner
from org.htmlcleaner import SimpleHtmlSerializer
from org.htmlcleaner import CleanerProperties

#AS API
from ch.systemsx.cisd.openbis.generic.shared.api.v1 import IGeneralInformationService;

#For Screeening API
from ch.systemsx.cisd.openbis.common.api.client import ServiceFinder;
from java.util import Arrays;

from ch.ethz.sis.openbis.generic.asapi.v3 import IApplicationServerApi
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleFetchOptions;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search import SampleSearchCriteria;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search import SearchResult;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id import SampleIdentifier;
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id import SamplePermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id import ExperimentIdentifier;
from ch.ethz.sis.openbis.generic.server.sharedapi.v3.json import GenericObjectMapper;
from ch.systemsx.cisd.openbis.dss.generic.server import DataStoreServer
from ch.systemsx.cisd.common.shared.basic.string import StringUtils

#from ch.systemsx.cisd.common.ssl import SslCertificateHelper;

#Plasmapper server used
PLASMAPPER_BASE_URL = "http://wishart.biology.ualberta.ca"
OPENBISURL = DataStoreServer.getConfigParameters().getServerURL() + "/openbis/openbis"
def getConfigParameterAsString(propertyKey):
	properties = DataStoreServer.getConfigParameters().getProperties();
	property = properties.getProperty(propertyKey);
	if StringUtils.isBlank(property):
		return None;
	else:
		return property;

def getDirectLinkURL():
	
	#CIFS
	cifsServerEnable = getConfigParameterAsString("cifs.server.enable");
	cifsServerPort = getConfigParameterAsString("cifs.server.smb-port");
	
	#SFTP
	sftpPort = getConfigParameterAsString("ftp.server.sftp-port");
	
	#FTPS
	ftpServerEnable = getConfigParameterAsString("ftp.server.enable");
	ftpServerUseSsl = getConfigParameterAsString("ftp.server.use-ssl");
	useSsl = getConfigParameterAsString("use-ssl");
	ftpPortLegacy = getConfigParameterAsString("ftp.server.port");
	ftpPort = getConfigParameterAsString("ftp.server.ftp-port");
	
	protocol = None;
	port = None;
	UNCsuffix = None;
	if (cifsServerEnable == "true") and (cifsServerPort is not None):
		protocol = "cifs"
		port = cifsServerPort;
		UNCsuffix = "STORE/";
	elif (sftpPort is not None):
		protocol = "sftp";
		port = sftpPort;
	elif (ftpServerEnable == "true") and ((ftpPort is not None) or (ftpPortLegacy is not None)) and (ftpServerUseSsl == "true" or useSsl == "true"):
		protocol = "ftps";
		if ftpPort is not None:
			port = ftpPort;
		elif ftpPortLegacy is not None:
			port = ftpPortLegacy;

	return getJsonForData({
						"protocol" : protocol,
						"port" : port,
						"UNCsuffix" : UNCsuffix
						});

def isSampleTypeAvailable(sampleTypes, sampleTypeCode):
	for sampleType in sampleTypes:
		if sampleType.getCode() == sampleTypeCode:
			return True
	return False

def getSampleTypes(tr, parameters):
	sessionToken = parameters.get("sessionToken");
	servFinder = ServiceFinder("openbis", IGeneralInformationService.SERVICE_URL);
	infService = servFinder.createService(IGeneralInformationService, OPENBISURL);
	types = infService.listSampleTypes(sessionToken);
	return types

def getProperties(tr, parameters):
	sessionToken = parameters.get("sessionToken");
	servFinder = ServiceFinder("openbis", IGeneralInformationService.SERVICE_URL);
	infService = servFinder.createService(IGeneralInformationService, OPENBISURL);
	properties = infService.listPropertyTypes(sessionToken, False);
	return properties

def isPropertyRichText(properties, propertyCode):
	for property in properties:
		if property.getCode() == propertyCode and property.getCode() != "FREEFORM_TABLE_STATE":
			return property.getDataType() == DataTypeCode.MULTILINE_VARCHAR;
	return None;

def updateIfIsPropertyRichText(properties, propertyCode, propertyValue):
	if isPropertyRichText(properties, propertyCode):
		if propertyValue is not None:
			cleanerProperties = CleanerProperties();
			cleanerProperties.setPruneTags("meta, link, script");
			cleaner = HtmlCleaner(cleanerProperties);
			htmlSerializer = SimpleHtmlSerializer(cleanerProperties);
			propertytagNode = cleaner.clean(propertyValue);
			return htmlSerializer.getAsString(propertytagNode);
	return propertyValue;

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
   	
def username(sessiontoken):
    m = re.compile('(.*)-[^-]*').match(sessiontoken)
    if m:
        return m.group(1)

def process(tr, parameters, tableBuilder):
	method = parameters.get("method");
	
	isOk = False;
	result = None;
	# Obtain the user using the dropbox
	sessionToken = parameters.get("sessionToken"); #String
	sessionId = username(sessionToken); #String
	tr.setUserId(userId);
	
	if method == "init":
		isOk = init(tr, parameters, tableBuilder);
	if method == "searchSamples":
		result = searchSamples(tr, parameters, tableBuilder, sessionId);
		isOk = True;
	if method == "registerUserPassword":
		isOk = registerUserPassword(tr, parameters, tableBuilder);
	if method == "getDirectLinkURL":
		result = getDirectLinkURL();
		isOk = True;
	
	if method == "copyAndLinkAsParent":
		isOk = copyAndLinkAsParent(tr, parameters, tableBuilder);
	
	if method == "batchOperation":
		isOk = batchOperation(tr, parameters, tableBuilder);
		
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
	if method == "moveSample":
		isOk = moveSample(tr, parameters, tableBuilder);
	if method == "insertDataSet":
		isOk = insertDataSet(tr, parameters, tableBuilder);
	if method == "updateDataSet":
		isOk = updateDataSet(tr, parameters, tableBuilder);
	
	if method == "listFeatureVectorDatasetsPermIds":
		result = listFeatureVectorDatasetsPermIds(tr, parameters, tableBuilder);
		isOk = True;
	if method == "listAvailableFeatures":
		result = listAvailableFeatures(tr, parameters, tableBuilder);
		isOk = True;
	if method == "getFeaturesFromFeatureVector":
		result = getFeaturesFromFeatureVector(tr, parameters, tableBuilder);
		isOk = True;

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

def listFeatureVectorDatasets(sessionToken, samplePlatePermId):
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1 import IScreeningApiServer;
	from ch.systemsx.cisd.openbis.dss.screening.shared.api.v1 import IDssServiceRpcScreening;
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import PlateIdentifier;
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import FeatureVectorDatasetReference;
	
	screeningFinder = ServiceFinder("openbis", IScreeningApiServer.SERVICE_URL);
	screeningServiceAS = screeningFinder.createService(IScreeningApiServer, OPENBISURL);
	
	plateIdentifier = PlateIdentifier.createFromPermId(samplePlatePermId);
	featureVectorDatasets = screeningServiceAS.listFeatureVectorDatasets(sessionToken, Arrays.asList(plateIdentifier));
	return featureVectorDatasets;

def getJsonForData(data):
	objectMapper = GenericObjectMapper();
	jsonValue = objectMapper.writeValueAsString(data);
	return jsonValue;

def listFeatureVectorDatasetsPermIds(tr, parameters, tableBuilder):
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1 import IScreeningApiServer;
	from ch.systemsx.cisd.openbis.dss.screening.shared.api.v1 import IDssServiceRpcScreening;
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import PlateIdentifier;
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import FeatureVectorDatasetReference;
	
	sessionToken = parameters.get("sessionToken");
	samplePlatePermId = parameters.get("samplePlatePermId");

	featureVectorDatasets = listFeatureVectorDatasets(sessionToken, samplePlatePermId);
	featureVectorDatasetCodes = [];
	for featureVectorDataset in featureVectorDatasets:
		featureVectorDatasetCodes.append(featureVectorDataset.getDatasetCode());
	
	return getJsonForData(featureVectorDatasetCodes);

def listAvailableFeatures(tr, parameters, tableBuilder):
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1 import IScreeningApiServer;
	from ch.systemsx.cisd.openbis.dss.screening.shared.api.v1 import IDssServiceRpcScreening;
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import PlateIdentifier;
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import FeatureVectorDatasetReference;
		
	sessionToken = parameters.get("sessionToken");
	samplePlatePermId = parameters.get("samplePlatePermId");
	featureVectorDatasetPermId = parameters.get("featureVectorDatasetPermId");
	
	featureVectorDataset = None;
	featureVectorDatasets = listFeatureVectorDatasets(sessionToken, samplePlatePermId);
	for featureVectorDataset in featureVectorDatasets:
		if featureVectorDataset.getDatasetCode() == featureVectorDatasetPermId:
			featureVectorDataset = featureVectorDataset;
	
	from ch.systemsx.cisd.openbis.dss.generic.server import DssScreeningApplicationContext
	screeningServiceDSS = DssScreeningApplicationContext.getInstance().getBean("data-store-rpc-service-screening-logic-target")

	featureInformationList = screeningServiceDSS.listAvailableFeatures(sessionToken, [featureVectorDataset]);
	features = {};
	for featureInformation in featureInformationList:
		features[featureInformation.getCode()] = featureInformation.getLabel();
	
	return getJsonForData(features);

def getFeaturesFromFeatureVector(tr, parameters, tableBuilder):
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1 import IScreeningApiServer;
	from ch.systemsx.cisd.openbis.dss.screening.shared.api.v1 import IDssServiceRpcScreening;
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import PlateIdentifier;
	from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import FeatureVectorDatasetReference;
	
	sessionToken = parameters.get("sessionToken");
	samplePlatePermId = parameters.get("samplePlatePermId");
	featureVectorDatasetPermId = parameters.get("featureVectorDatasetPermId");
	featuresCodesFromFeatureVector = parameters.get("featuresCodesFromFeatureVector");
	
	featureVectorDataset = None;
	featureVectorDatasets = listFeatureVectorDatasets(sessionToken, samplePlatePermId);
	for featureVectorDatasetAux in featureVectorDatasets:
		if featureVectorDatasetAux.getDatasetCode() == featureVectorDatasetPermId:
			featureVectorDataset = featureVectorDatasetAux;
	
	from ch.systemsx.cisd.openbis.dss.generic.server import DssScreeningApplicationContext
	screeningServiceDSS = DssScreeningApplicationContext.getInstance().getBean("data-store-rpc-service-screening-logic-target")
	
	featuresFromFeatureVector = screeningServiceDSS.loadFeatures(sessionToken, [featureVectorDataset], featuresCodesFromFeatureVector);
	return getJsonForData(featuresFromFeatureVector);

def insertSpaceIfMissing(tr, spaceCode):
	space = tr.getSpace(spaceCode);
	if space == None:
		tr.createNewSpace(spaceCode, None);
	return space;

def insertProjectIfMissing(tr, projectIdentifier, projectsCache):
	if projectIdentifier in projectsCache:
		return projectsCache[projectIdentifier];
	project = tr.getProject(projectIdentifier);
	if project == None:
		tr.createNewProject(projectIdentifier);
	projectsCache[projectIdentifier] = project;
	return project;

def insertExperimentIfMissing(tr, experimentIdentifier, experimentType, experimentName):
	experiment = tr.getExperiment(experimentIdentifier);
	if experiment == None:
		experiment = tr.createNewExperiment(experimentIdentifier, 	experimentType);
		experiment.setPropertyValue("NAME", experimentName);
	return experiment;

def init(tr, parameters, tableBuilder):
	projectsCache = {};
	inventorySpace = tr.getSpace("DEFAULT_LAB_NOTEBOOK");
	methodsSpace = tr.getSpace("METHODS");
	materialsSpace = tr.getSpace("MATERIALS");
	isNewInstallation = inventorySpace == None and methodsSpace == None and inventorySpace == None;
			
	## Installing Mandatory Spaces/Projects on every login if missing
	insertSpaceIfMissing(tr, "METHODS");
	insertSpaceIfMissing(tr, "MATERIALS");
	insertSpaceIfMissing(tr, "STOCK_CATALOG");
	insertProjectIfMissing(tr, "/STOCK_CATALOG/PRODUCTS", projectsCache);
	insertExperimentIfMissing(tr, "/STOCK_CATALOG/PRODUCTS/PRODUCT_COLLECTION", "STOCK", "Product Collection");
	insertProjectIfMissing(tr, "/STOCK_CATALOG/SUPPLIERS", projectsCache);
	insertExperimentIfMissing(tr, "/STOCK_CATALOG/SUPPLIERS/SUPPLIER_COLLECTION", "STOCK", "Supplier Collection");
	insertProjectIfMissing(tr, "/STOCK_CATALOG/REQUESTS", projectsCache);
	insertExperimentIfMissing(tr, "/STOCK_CATALOG/REQUESTS/REQUEST_COLLECTION", "STOCK", "Request Collection");
	
	insertSpaceIfMissing(tr, "STOCK_ORDERS");
	insertProjectIfMissing(tr, "/STOCK_ORDERS/ORDERS", projectsCache);
	insertExperimentIfMissing(tr, "/STOCK_ORDERS/ORDERS/ORDER_COLLECTION", "STOCK", "Order Collection");
	
	## Default lab notebook only on new installations
	if isNewInstallation:
		tr.createNewSpace("DEFAULT_LAB_NOTEBOOK", None);
		tr.createNewProject("/DEFAULT_LAB_NOTEBOOK/DEFAULT_PROJECT");
		defaultExperiment = tr.createNewExperiment("/DEFAULT_LAB_NOTEBOOK/DEFAULT_PROJECT/DEFAULT_EXPERIMENT", 	"DEFAULT_EXPERIMENT");
		defaultExperiment.setPropertyValue("NAME", "Default Experiment");
	
	# On new installations check if the default types are installed to create their respective PROJECT/EXPERIMENTS
	if isNewInstallation:
		installedTypes = getSampleTypes(tr, parameters);
		if isSampleTypeAvailable(installedTypes, "ANTIBODY"):
			insertProjectIfMissing(tr, "/MATERIALS/REAGENTS", projectsCache);
			insertExperimentIfMissing(tr, "/MATERIALS/REAGENTS/ANTIBODY_COLLECTION_1", "MATERIALS", "Antibody Collection 1");
		
		if isSampleTypeAvailable(installedTypes, "CHEMICAL"):
			insertProjectIfMissing(tr, "/MATERIALS/REAGENTS", projectsCache);
			insertExperimentIfMissing(tr, "/MATERIALS/REAGENTS/CHEMICAL_COLLECTION_1", "MATERIALS", "Chemical Collection 1");
		
		if isSampleTypeAvailable(installedTypes, "ENZYME"):
			insertProjectIfMissing(tr, "/MATERIALS/REAGENTS", projectsCache);
			insertExperimentIfMissing(tr, "/MATERIALS/REAGENTS/ENZYME_COLLECTION_1", "MATERIALS", "Enzyme Collection 1");
		
		if isSampleTypeAvailable(installedTypes, "MEDIA"):
			insertProjectIfMissing(tr, "/MATERIALS/REAGENTS", projectsCache);
			insertExperimentIfMissing(tr, "/MATERIALS/REAGENTS/MEDIA_COLLECTION_1", "MATERIALS", "Media Collection 1");
		
		if isSampleTypeAvailable(installedTypes, "SOLUTION_BUFFER"):
			insertProjectIfMissing(tr, "/MATERIALS/REAGENTS", projectsCache);
			insertExperimentIfMissing(tr, "/MATERIALS/REAGENTS/SOLUTION_BUFFER_COLLECTION_1", "MATERIALS", "Solution Buffer Collection 1");
			
		if isSampleTypeAvailable(installedTypes, "BACTERIA"):
			insertProjectIfMissing(tr, "/MATERIALS/BACTERIA", projectsCache);
			insertExperimentIfMissing(tr, "/MATERIALS/BACTERIA/BACTERIA_COLLECTION_1", "MATERIALS", "Bacteria Collection 1");
		
		if isSampleTypeAvailable(installedTypes, "CELL_LINE"):
			insertProjectIfMissing(tr, "/MATERIALS/CELL_LINES", projectsCache);
			insertExperimentIfMissing(tr, "/MATERIALS/CELL_LINES/CELL_LINE_COLLECTION_1", "MATERIALS", "Cell Line Collection 1");
		
		if isSampleTypeAvailable(installedTypes, "FLY"):
			insertProjectIfMissing(tr, "/MATERIALS/FLIES", projectsCache);
			insertExperimentIfMissing(tr, "/MATERIALS/FLIES/FLY_COLLECTION_1", "MATERIALS", "Fly Collection 1");
		
		if isSampleTypeAvailable(installedTypes, "YEAST"):
			insertProjectIfMissing(tr, "/MATERIALS/YEASTS", projectsCache);
			insertExperimentIfMissing(tr, "/MATERIALS/YEASTS/YEAST_COLLECTION_1", "MATERIALS", "Yeast Collection 1");

		if isSampleTypeAvailable(installedTypes, "PLASMID"):
			insertProjectIfMissing(tr, "/MATERIALS/PLASMIDS", projectsCache);
			insertExperimentIfMissing(tr, "/MATERIALS/PLASMIDS/PLASMID_COLLECTION_1", "MATERIALS", "Plasmid Collection 1");

		if isSampleTypeAvailable(installedTypes, "OLIGO"):
			insertProjectIfMissing(tr, "/MATERIALS/POLYNUCLEOTIDES", projectsCache);
			insertExperimentIfMissing(tr, "/MATERIALS/POLYNUCLEOTIDES/OLIGO_COLLECTION_1", "MATERIALS", "Oligo Collection 1");
		
		if isSampleTypeAvailable(installedTypes, "RNA"):
			insertProjectIfMissing(tr, "/MATERIALS/POLYNUCLEOTIDES", projectsCache);
			insertExperimentIfMissing(tr, "/MATERIALS/POLYNUCLEOTIDES/RNA_COLLECTION_1", "MATERIALS", "RNA Collection 1");
		
		if isSampleTypeAvailable(installedTypes, "GENERAL_PROTOCOL"):
			insertProjectIfMissing(tr, "/METHODS/PROTOCOLS", projectsCache);
			insertExperimentIfMissing(tr, "/METHODS/PROTOCOLS/GENERAL_PROTOCOLS", "METHODS", "General Protocols");
		
		if isSampleTypeAvailable(installedTypes, "PCR_PROTOCOL"):
			insertProjectIfMissing(tr, "/METHODS/PROTOCOLS", projectsCache);
			insertExperimentIfMissing(tr, "/METHODS/PROTOCOLS/PCR_PROTOCOLS", "METHODS", "PCR Protocols");
		
		if isSampleTypeAvailable(installedTypes, "WESTERN_BLOTTING_PROTOCOL"):
			insertProjectIfMissing(tr, "/METHODS/PROTOCOLS", projectsCache);
			insertExperimentIfMissing(tr, "/METHODS/PROTOCOLS/WESTERN_BLOTTING_PROTOCOLS", "METHODS", "Western Botting Protocols");
	
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
	properties = getProperties(tr, parameters);
	#Hack - Fix Sample Lost bug from openBIS, remove when SSDM-1979 is fix
	#Found in S211: In new openBIS versions if you set the already existing sample when doing a dataset update is deleted
	#sampleIdentifier = parameters.get("sampleIdentifier"); #String
	#dataSetSample = getSampleByIdentifierForUpdate(tr, sampleIdentifier);
	#dataSet.setSample(dataSetSample);
	#Assign Data Set properties
	for key in metadata.keySet():
		propertyValue = unicode(metadata[key]);
		if propertyValue == "":
			propertyValue = None;
		else:
			propertyValue = updateIfIsPropertyRichText(properties, key, propertyValue);
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
	properties = getProperties(tr, parameters);
	
	#Create Dataset
	dataSetSample = getSampleByIdentifierForUpdate(tr, sampleIdentifier);
	dataSet = tr.createNewDataSet(dataSetType);
	dataSet.setSample(dataSetSample);
	
	#Assign Data Set properties
	for key in metadata.keySet():
		propertyValue = unicode(metadata[key]);
		if propertyValue == "":
			propertyValue = None;
		else:
			propertyValue = updateIfIsPropertyRichText(properties, key, propertyValue);
		dataSet.setPropertyValue(key,propertyValue);
	
	#Move All Files using a tmp directory close to the datastore
	threadProperties = getThreadProperties(tr);
	tempDir =  threadProperties[u'incoming-dir'] + "/tmp_eln/" + str(time.time());
	tempDirFile = File(tempDir);
	tempDirFile.mkdirs();
	
	#tempDir = System.getProperty("java.io.tmpdir");
	dss_component = DssComponentFactory.tryCreate(parameters.get("sessionID"), OPENBISURL);
	
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
		if 	temFile.getName().endswith(".fasta") and dataSetType == "SEQ_FILE" and PLASMAPPER_BASE_URL != None:
			futureSVG = File(tempDir + "/" + folderName + "/generated/" + temFile.getName().replace(".fasta", ".svg"));
			futureHTML = File(tempDir + "/" + folderName + "/generated/" + temFile.getName().replace(".fasta", ".html"));
			try:
				PlasmapperConnector.downloadPlasmidMap(
					PLASMAPPER_BASE_URL,
					tempDir + "/" + folderName + "/" + temFile.getName(),
					tempDir + "/" + folderName + "/generated/" + temFile.getName().replace(".fasta", ".svg"),
					tempDir + "/" + folderName + "/generated/" + temFile.getName().replace(".fasta", ".html")
				);
			except:
				raise UserFailureException("Plasmapper service unavailable, try to upload your dataset later."); 
			tr.moveFile(temFile.getParentFile().getAbsolutePath(), dataSet);
		else:
			tr.moveFile(temFile.getAbsolutePath(), dataSet);
	#Clean Files from workspace
	for fileName in fileNames:
		dss_component.deleteSessionWorkspaceFile(fileName);
	
	#Return from the call
	return True;

def copyAndLinkAsParent(tr, parameters, tableBuilder):
	newSampleIdentifier = parameters.get("newSampleIdentifier"); #String
	sampleIdentifierToCopyAndLinkAsParent = parameters.get("sampleIdentifierToCopyAndLinkAsParent"); #String
	experimentIdentifierToAssignToCopy = parameters.get("experimentIdentifierToAssignToCopy"); #String
	
	#Create new Sample
	sampleToCopyAndLinkAsParent = getSampleByIdentifierForUpdate(tr, sampleIdentifierToCopyAndLinkAsParent); #Retrieve Sample
	newSample = tr.createNewSample(newSampleIdentifier, sampleToCopyAndLinkAsParent.getSampleType()); #Create Sample given his id
	
	#Assign Parent
	newSample.setParentSampleIdentifiers([sampleIdentifierToCopyAndLinkAsParent]);
	
	#Assign properties
	propertiesDefinitions = searchService.listPropertiesDefinitionsForSampleType(sampleToCopyAndLinkAsParent.getSampleType());
	for propertyDefinition in propertiesDefinitions:
		propCode = propertyDefinition.getPropertyTypeCode();
		propValue = sampleToCopyAndLinkAsParent.getPropertyValue(propCode);
		if propValue is not None:
			newSample.setPropertyValue(propCode, propValue);
	
	#Assign Experiment
	experimentToAssignToCopy = tr.getExperiment(experimentIdentifierToAssignToCopy);
	newSample.setExperiment(experimentToAssignToCopy);
	
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
			if(child.getExperiment() is not None):
				childCopy.setExperiment(child.getExperiment());
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
	if isPropertyToSkip:
		return None;
	elif isDefaultBenchProperty:
		return str(defaultBenchProperties[propCode]);
	else:
		return propValue;

def batchOperation(tr, parameters, tableBuilder):
	for operationParameters in parameters.get("operations"):
		if operationParameters.get("method") == "updateSample":
			operationParameters["sessionToken"] = parameters.get("sessionToken");
			insertUpdateSample(tr, operationParameters, tableBuilder);
	return True;
	
def insertUpdateSample(tr, parameters, tableBuilder):
	
	#Mandatory parameters
	sampleSpace = parameters.get("sampleSpace"); #String
	sampleProject = parameters.get("sampleProject"); #String
	sampleExperiment = parameters.get("sampleExperiment"); #String
	sampleCode = parameters.get("sampleCode"); #String
	sampleType = parameters.get("sampleType"); #String
	sampleProperties = parameters.get("sampleProperties"); #java.util.LinkedHashMap<String, String> where the key is the name
	changesToDo = parameters.get("changesToDo");
	
	#Optional parameters
	sampleParents = parameters.get("sampleParents"); #List<String> Identifiers are in SPACE/CODE format
	sampleChildrenNew = parameters.get("sampleChildrenNew"); #List<java.util.LinkedHashMap<String, String>>
	sampleChildren = parameters.get("sampleChildren"); #List<String> Identifiers are in SPACE/CODE format
	sampleChildrenRemoved = parameters.get("sampleChildrenRemoved"); #List<String> Identifiers are in SPACE/CODE format
	sampleParentsNew = parameters.get("sampleParentsNew");
	
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
	
	#Add sample parents
	parentsToAdd = [];
	if sampleParentsNew != None:
		for newSampleParent in sampleParentsNew:
			parent = tr.createNewSample(newSampleParent.get("identifier"), newSampleParent.get("sampleTypeCode")); #Create Sample given his id
			if newSampleParent.get("annotations") != None and sampleProperties.get("ANNOTATIONS_STATE") != None:
				sample_ANNOTATIONS_STATE = sampleProperties.get("ANNOTATIONS_STATE");
				sample_ANNOTATIONS_STATE = sample_ANNOTATIONS_STATE.replace("PERM_ID_PLACEHOLDER_FOR" + newSampleParent.get("identifier"), parent.getPermId());
				sampleProperties.put("ANNOTATIONS_STATE", sample_ANNOTATIONS_STATE);
			if newSampleParent.get("experimentIdentifierOrNull") != None:
				parentExperiment = tr.getExperiment(newSampleParent.get("experimentIdentifierOrNull"));
				parent.setExperiment(parentExperiment);
			for key in newSampleParent.get("properties").keySet():
				propertyValue = unicode(newSampleParent.get("properties").get(key));
				if propertyValue == "":
					propertyValue = None;
				parent.setPropertyValue(key, propertyValue);
			
			if newSampleParent.get("parentsIdentifiers") != None:
				parent.setParentSampleIdentifiers(newSampleParent.get("parentsIdentifiers"));
			
			parentsToAdd.append(parent);
			if sampleParents == None:
				sampleParents = [];
			sampleParents = sampleParents + [parent.getSampleIdentifier()];
	
	#Assign sample properties
	if sampleProperties != None:
		properties = getProperties(tr, parameters);
		for key in sampleProperties.keySet():
			propertyValue = unicode(sampleProperties[key]);
			if propertyValue == "":
				propertyValue = None;
			else:
				propertyValue = updateIfIsPropertyRichText(properties, key, propertyValue);
			sample.setPropertyValue(key, propertyValue);
	
	#Add sample parents
	if sampleParents != None:
		sample.setParentSampleIdentifiers(sampleParents);
	
	#Create new sample children
	sampleChildrenNewIdentifiers = [];
	if sampleChildrenNew != None:
		for newSampleChild in sampleChildrenNew:
			child = tr.createNewSample(newSampleChild["identifier"], newSampleChild["sampleTypeCode"]); #Create Sample given his id
			sampleChildrenNewIdentifiers.append(newSampleChild["identifier"]);
			child.setParentSampleIdentifiers([sampleIdentifier]);
			if experiment != None:
				child.setExperiment(experiment);
			for key in newSampleChild["properties"].keySet():
				propertyValue = unicode(newSampleChild["properties"][key]);
				if propertyValue == "":
					propertyValue = None;
				
				child.setPropertyValue(key,propertyValue);
	
	#Add sample children that are not newly created
	if sampleChildren != None:
		for sampleChildIdentifier in sampleChildren:
			if sampleChildIdentifier not in sampleChildrenNewIdentifiers:
				child = getSampleByIdentifierForUpdate(tr, sampleChildIdentifier); #Retrieve Sample
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
	
	#Changes to do
	if changesToDo is not None:
		for change in changesToDo:
			sampleWithChanges = getSampleByIdentifierForUpdate(tr, change["identifier"]); #Retrieve Sample
			for key in change["properties"].keySet():
					propertyValue = unicode(change["properties"][key]);
					if propertyValue == "":
						propertyValue = None;
					sampleWithChanges.setPropertyValue(key,propertyValue);
		
	#Return from the call
	return True;
	
def moveSample(tr, parameters, tableBuilder):
	sampleIdentifier = parameters.get("sampleIdentifier"); #String
	experimentIdentifier = parameters.get("experimentIdentifier"); #String
	experimentType = parameters.get("experimentType"); #String
	
	sample = getSampleByIdentifierForUpdate(tr, sampleIdentifier); #Retrieve Sample
	experiment = tr.getExperiment(experimentIdentifier); #Retrieve Experiment
	
	if experiment is None:
		experiment = tr.createNewExperiment(experimentIdentifier, experimentType);
	
	sample.setExperiment(experiment);
	return True

def insertUpdateExperiment(tr, parameters, tableBuilder):
	
	#Mandatory parameters
	experimentType = parameters.get("experimentType"); #String
	experimentIdentifier = parameters.get("experimentIdentifier"); #String
	experimentProperties = parameters.get("experimentProperties"); #java.util.LinkedHashMap<String, String> where the key is the name
	properties = getProperties(tr, parameters);
	
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
		else:
			propertyValue = updateIfIsPropertyRichText(properties, key, propertyValue);
		experiment.setPropertyValue(key,propertyValue);
	
	return True;

def searchSamples(tr, parameters, tableBuilder, sessionId):
	v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi, OPENBISURL + IApplicationServerApi.SERVICE_URL, 30 * 1000);
	
	###############
	############### V3 Search
	###############
	fechOptions = parameters;
	
	# FreeText
	anyFieldContains = fechOptions.get("anyFieldContains");
	
	# Attributes
	samplePermId = fechOptions.get("samplePermId");
	withExperimentWithProjectPermId = fechOptions.get("withExperimentWithProjectPermId");
	sampleIdentifier = fechOptions.get("sampleIdentifier");
	sampleCode = fechOptions.get("sampleCode");
	sampleTypeCode = fechOptions.get("sampleTypeCode");
	registrationDate = fechOptions.get("registrationDate");
	modificationDate = fechOptions.get("modificationDate");
		
	# Properties
	properyKeyValueList = fechOptions.get("properyKeyValueList");
	
	# Sub Queries
	sampleExperimentIdentifier = fechOptions.get("sampleExperimentIdentifier");
	sampleContainerPermId = fechOptions.get("sampleContainerPermId");
	
	# Hierarchy Options
	withProperties = fechOptions.get("withProperties");
	withParents = fechOptions.get("withParents");
	withChildren = fechOptions.get("withChildren");
	withAncestors = fechOptions.get("withAncestors");
	withDescendants = fechOptions.get("withDescendants");

	#Search Setup
	criterion = SampleSearchCriteria();
	criterion.withAndOperator();
	fetchOptions = SampleFetchOptions();
	
	#Free Text
	if anyFieldContains is not None:
		words = anyFieldContains.split();
		for word in words:
			if (word != None) and (word != ""):
				criterion.withAnyField().thatContains(word);
	
	#Attributes
	if samplePermId is not None:
		criterion.withPermId().thatEquals(samplePermId);
	if withExperimentWithProjectPermId is not None:
		criterion.withExperiment().withProject().withPermId().thatEquals(withExperimentWithProjectPermId);
	if sampleIdentifier is not None:
		criterion.withId().thatEquals(SampleIdentifier(sampleIdentifier));
	if sampleCode is not None:
		criterion.withCode().thatEquals(sampleCode);
	if sampleTypeCode is not None:
		criterion.withType().withCode().thatEquals(sampleTypeCode);
	if registrationDate is not None:
		formatter = SimpleDateFormat("yyyy-MM-dd");
		registrationDateObject = formatter.parse(registrationDate);
		criterion.withRegistrationDate().thatEquals(registrationDateObject);
	if modificationDate is not None:
		formatter = SimpleDateFormat("yyyy-MM-dd");
		modificationDateObject = formatter.parse(modificationDate);
		criterion.withModificationDate().thatEquals(modificationDateObject);
	
	#Properties
	if properyKeyValueList is not None:
		for keyValuePair in properyKeyValueList:
			for propertyTypeCode in keyValuePair.keySet():
				propertyValue = keyValuePair.get(propertyTypeCode);
				criterion.withProperty(propertyTypeCode).thatEquals(propertyValue);
	
	#Sub queries
	if sampleExperimentIdentifier is not None:
		criterion.withExperiment().withId().thatEquals(ExperimentIdentifier(sampleExperimentIdentifier));
	if sampleContainerPermId is not None:
		criterion.withContainer().withPermId().thatEquals(sampleContainerPermId);

	#Hierarchy Fetch Options
	if withProperties:
		fetchOptions.withProperties();
	if withParents:	
		fetchOptionsParents = SampleFetchOptions();
		fetchOptionsParents.withProperties();
		fetchOptionsParents.withType();
		fetchOptionsParents.withSpace();
		fetchOptionsParents.withExperiment();
		fetchOptionsParents.withRegistrator();
		fetchOptionsParents.withModifier();
		fetchOptions.withParentsUsing(fetchOptionsParents);
	if withChildren:
		fetchOptionsChildren = SampleFetchOptions();
		fetchOptionsChildren.withProperties();
		fetchOptionsChildren.withType();
		fetchOptionsChildren.withSpace();
		fetchOptionsChildren.withExperiment();
		fetchOptionsChildren.withRegistrator();
		fetchOptionsChildren.withModifier();
		fetchOptions.withChildrenUsing(fetchOptionsChildren);
	if withAncestors:
		fetchOptionsAncestors = SampleFetchOptions();
		fetchOptionsAncestors.withProperties();
		fetchOptionsAncestors.withType();
		fetchOptionsAncestors.withSpace();
		fetchOptionsAncestors.withExperiment();
		fetchOptionsAncestors.withRegistrator();
		fetchOptionsAncestors.withModifier();
		fetchOptionsAncestors.withParentsUsing(fetchOptionsAncestors);
		fetchOptions.withParentsUsing(fetchOptionsAncestors);
	if withDescendants:
		fetchOptionsDescendants = SampleFetchOptions();
		fetchOptionsDescendants.withProperties();
		fetchOptionsDescendants.withType();
		fetchOptionsDescendants.withSpace();
		fetchOptionsDescendants.withExperiment();
		fetchOptionsDescendants.withRegistrator();
		fetchOptionsDescendants.withModifier();
		fetchOptionsDescendants.withChildrenUsing(fetchOptionsDescendants);
		fetchOptions.withChildrenUsing(fetchOptionsDescendants);
	
	#Standard Fetch Options, always use
	fetchOptions.withType();
	fetchOptions.withSpace();
	fetchOptions.withExperiment();
	fetchOptions.withRegistrator();
	fetchOptions.withModifier();
	
	###############
	###############
	###############
	
	
	##
	## Custom (Interceptor to modify standard results)
	##
	result = None;
	
	isCustom = parameters.get("custom"); #Boolean
	if isCustom:
		result = searchSamplesCustom(tr, parameters, tableBuilder, v3, criterion, fetchOptions);
	else:
		result = v3.searchSamples(parameters.get("sessionToken"), criterion, fetchOptions);
	
	##
	##
	##
	
	###
	### Json Conversion
	###
	objectMapper = GenericObjectMapper();
	resultAsString = objectMapper.writeValueAsString(result);
	return resultAsString;

def searchSamplesCustom(tr, parameters, tableBuilder, v3, criterion, fetchOptions):
	# SEARCH CUSTOM PLACEHOLDER START
	return [];
	# SEARCH CUSTOM PLACEHOLDER END
	
def searchSamplesNexus(tr, parameters, tableBuilder, v3, criterion, fetchOptions):
	
	toReturnPermIds = []; #
	#Right Givers: The sample with all his descendants
	#1. Request user search with all right givers
	descendantsFetchOptions = SampleFetchOptions();
	descendantsFetchOptions.withChildrenUsing(descendantsFetchOptions);
	requestedResults = v3.searchSamples(tr.getOpenBisServiceSessionToken(), criterion, descendantsFetchOptions);
 	
	if requestedResults.getTotalCount() > 0:
		#Prepare data structures for the rights givers to accelerate the process
		requestedToRigthsGivers = {};
		allRightsGivers = set();
		for requestedResult in requestedResults.getObjects():
			rigthsGivers = getDescendantsTreePermIdsStringSet([requestedResult]);
			allRightsGivers = allRightsGivers | rigthsGivers;
			requestedToRigthsGivers[requestedResult.getPermId().getPermId()] = rigthsGivers;
 		
		#2. Search for the visible right givers
 		
		visibleRightGivers = v3.getSamples(parameters.get("sessionToken"), getSamplePermIdsObjFromPermIdStrings(allRightsGivers), SampleFetchOptions());
		visibleRightGiversPermIds = getDescendantsTreePermIdsStringSet(visibleRightGivers.values());
		#3. Intersect what the user wants and is available to see and keep matches
		for requestedResultPermIdString in requestedToRigthsGivers:
			rigthsGiversPermIds = requestedToRigthsGivers[requestedResultPermIdString];
			intersection = rigthsGiversPermIds & visibleRightGiversPermIds;
			if len(intersection) > 0:
				toReturnPermIds.append(SamplePermId(requestedResultPermIdString));
 	
	#Now we complete those permIds with all information available for them using a search by the ETL server
	systemResultAsMap = v3.getSamples(tr.getOpenBisServiceSessionToken(), toReturnPermIds, fetchOptions);
	systemResult = ArrayList(systemResultAsMap.values());
	systemSearchResult = SearchResult(systemResult, systemResult.size());
 	
	return systemSearchResult
 
def getSamplePermIdsObjFromPermIdStrings(samplePermIds):
	values = [];
	for samplePermId in samplePermIds:
		values.append(SamplePermId(samplePermId));
	return values;
 	
def getDescendantsTreePermIdsStringSet(samples):
	descendantsPermIds = set();
	for sample in samples:
		descendantsQueue = [sample];
		while len(descendantsQueue) > 0:
			queueSample = descendantsQueue.pop();
			if queueSample.getPermId().getPermId() not in descendantsPermIds:
				descendantsPermIds.add(queueSample.getPermId().getPermId());
				if queueSample.getFetchOptions().hasChildren():
					for child in queueSample.getChildren():
						descendantsQueue.append(child);
	return descendantsPermIds;