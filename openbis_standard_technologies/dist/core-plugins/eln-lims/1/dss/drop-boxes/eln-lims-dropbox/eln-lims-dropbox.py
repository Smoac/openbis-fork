import os

from ch.systemsx.cisd.openbis.generic.client.web.client.exception import UserFailureException
from ch.ethz.sis.openbis.generic.asapi.v3 import IApplicationServerApi
from ch.systemsx.cisd.openbis.dss.generic.server import DataStoreServer
from ch.systemsx.cisd.common.spring import HttpInvokerUtils;

INVALID_FORMAT_ERROR_MESSAGE = "Invalid format for the folder name, should follow the pattern <ENTITY_KIND>+<SPACE_CODE>+<PROJECT_CODE>[<EXPERIMENT_CODE|<SAMPLE_CODE>]+<OPTIONAL_DATASET_TYPE>+<OPTIONAL_NAME>";
FAILED_TO_PARSE_SAMPLE_ERROR_MESSAGE = "Failed to parse sample";
FAILED_TO_PARSE_EXPERIMENT_ERROR_MESSAGE = "Failed to parse experiment";
SAMPLE_MISSING_ERROR_MESSAGE = "Sample not found";
EXPERIMENT_MISSING_ERROR_MESSAGE = "Experiment not found";
MORE_THAN_ONE_FOLDER_ERROR_MESSAGE = "More than one folder found";

def process(transaction):
	incoming = transaction.getIncoming();
	folderName = incoming.getName();
	
	if not folderName.startswith('.'):
		datasetInfo = folderName.split("+");
		entityKind = None;
		sample = None;
		experiment = None;
		datasetType = None;
		name = None;
		
		# Parse entity Kind
		if len(datasetInfo) >= 1:
			entityKind = datasetInfo[0];
		else:
			raise UserFailureException(INVALID_FORMAT_ERROR_MESSAGE + ":" + FAILED_TO_PARSE_ERROR_MESSAGE);
		
		# Parse entity Kind Format
		if entityKind == "O":
			OPENBISURL = DataStoreServer.getConfigParameters().getServerURL() + "/openbis/openbis"
			v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi, OPENBISURL + IApplicationServerApi.SERVICE_URL, 30 * 1000);
			projectSamplesEnabled = v3.getServerInformation(transaction.getOpenBisServiceSessionToken())['project-samples-enabled'] == 'true'

			if len(datasetInfo) >= 4 and projectSamplesEnabled:
				sampleSpace = datasetInfo[1];
				projectCode = datasetInfo[2];
				sampleCode = datasetInfo[3];
				sample = transaction.getSample("/" +sampleSpace + "/" + projectCode + "/" + sampleCode);
				if sample is None:
					raise UserFailureException(INVALID_FORMAT_ERROR_MESSAGE + ":" + SAMPLE_MISSING_ERROR_MESSAGE);
				if len(datasetInfo) >= 5:
					datasetType = datasetInfo[4];
				if len(datasetInfo) >= 6:
					name = datasetInfo[5];
				if len(datasetInfo) > 6:
					raise UserFailureException(INVALID_FORMAT_ERROR_MESSAGE + ":" + FAILED_TO_PARSE_SAMPLE_ERROR_MESSAGE);
			elif len(datasetInfo) >= 3 and not projectSamplesEnabled:
				sampleSpace = datasetInfo[1];
				sampleCode = datasetInfo[2];
				sample = transaction.getSample("/" +sampleSpace + "/" + sampleCode);
				if sample is None:
					raise UserFailureException(INVALID_FORMAT_ERROR_MESSAGE + ":" + SAMPLE_MISSING_ERROR_MESSAGE);
				if len(datasetInfo) >= 4:
					datasetType = datasetInfo[3];
				if len(datasetInfo) >= 5:
					name = datasetInfo[4];
				if len(datasetInfo) > 5:
					raise UserFailureException(INVALID_FORMAT_ERROR_MESSAGE + ":" + FAILED_TO_PARSE_SAMPLE_ERROR_MESSAGE);
			else:
				raise UserFailureException(INVALID_FORMAT_ERROR_MESSAGE + ":" + FAILED_TO_PARSE_SAMPLE_ERROR_MESSAGE);
		if entityKind == "E":
			if len(datasetInfo) >= 4:
				experimentSpace = datasetInfo[1];
				projectCode = datasetInfo[2];
				experimentCode = datasetInfo[3];
				experiment = transaction.getExperiment("/" +experimentSpace + "/" + projectCode + "/" + experimentCode);
				if experiment is None:
					raise UserFailureException(INVALID_FORMAT_ERROR_MESSAGE + ":" + EXPERIMENT_MISSING_ERROR_MESSAGE);
				if len(datasetInfo) >= 5:
					datasetType = datasetInfo[4];
				if len(datasetInfo) >= 6:
					name = datasetInfo[5];
				if len(datasetInfo) > 6:
					raise UserFailureException(INVALID_FORMAT_ERROR_MESSAGE + ":" + FAILED_TO_PARSE_EXPERIMENT_ERROR_MESSAGE);
			else:
				raise UserFailureException(INVALID_FORMAT_ERROR_MESSAGE + ":" + FAILED_TO_PARSE_EXPERIMENT_ERROR_MESSAGE);
		
		# Create dataset
		dataSet = None;
		if datasetType is not None: #Set type if found
			dataSet = transaction.createNewDataSet(datasetType);
		else:
			dataSet = transaction.createNewDataSet();
		
		if name is not None:
			dataSet.setPropertyValue("$NAME", name); #Set name if found
		
		# Set sample or experiment
		if sample is not None:
			dataSet.setSample(sample);
		else:
			dataSet.setExperiment(experiment);
		
		# Move folder to dataset
		filesInFolder = incoming.listFiles();
		
		# Discard folders started with a . (hidden files)
		itemsInFolder = 0;
		datasetItem = None;
		for item in filesInFolder:
			#Exclude files starting with .
			#Exclude Mac .DS_Store
			#Exclude Windows Thumbs.db
			if (not item.getName().startswith('.')) and (not item.getName() == ".DS_Store") and (not item.getName() == "Thumbs.db"):
				itemsInFolder = itemsInFolder + 1;
				datasetItem = item;
		
		if itemsInFolder > 1:
			raise UserFailureException(MORE_THAN_ONE_FOLDER_ERROR_MESSAGE);
		else:
			transaction.moveFile(datasetItem.getAbsolutePath(), dataSet);