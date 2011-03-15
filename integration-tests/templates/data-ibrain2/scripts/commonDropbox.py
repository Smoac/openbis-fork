#! /usr/bin/env python

import os
from java.io import File

class AbstractPropertiesParser:
	_propertiesDict = None

	def __init__(self, incoming, fileName):
		path = os.path.join(incoming, fileName)
		self._propertiesDict = self._parseMetadata(path)
	
	def _findFile(self, incoming, prefix):
		for file in os.listdir(incoming):
			if file.startswith(prefix):
				return file
		raise Exception("No file with prefix '"+prefix+"' has been found!")

	# Parses the metadata file from the given incoming directory.
	# Each line should have a form:
	#   key = value
	# Keys should be unique in the file.
	# Returns: 
	#	a dictionary with keys and values from the file.
	def _parseMetadata(self, path):		
		f = open(path)
		myDict = {}
		for line in f:
			line = line.strip()
			if len(line) == 0 or line.startswith("#"):
				continue
	
			ix = line.find("=")
			if ix == -1:
				raise Exception("Cannot find '=' in line '"+line+"' in file: "+path)
			key = line[:ix].strip()
			value = line[ix+1:].strip()
			if key in myDict:
				raise Exception("Duplicated key '"+key+"' in file: "+path)		
			myDict[key] = value
		return myDict

	def get(self, propertyName):
		return self._propertiesDict[propertyName]

	# All properties in the file.
	# Returns:
	#	an iterator which yields (propertyName, propertyValue) pairs 
	def getPropertiesIter(self):
		return [ (key, value) for key, value in self._propertiesDict.iteritems() ]

	# All dataset properties.
	# Returns:
	#	an iterator which yields (propertyCode, propertyValue) pairs 
	def getDatasetPropertiesIter(self):
		return [ (key, value) for key, value in self._propertiesDict.iteritems() if key.startswith(self.DATASET_PROPERTY_PREFIX) ]

class AbstractMetadataParser(AbstractPropertiesParser):
	METADATA_FILE="metadata.properties"

	IBRAIN2_DATASET_ID_PROPERTY = "ibrain2.dataset.id" 
	DATASET_PROPERTY_PREFIX = "ibrain2."
	DATASET_TYPE_PROPERTY = "dataset.type"

	def __init__(self, incoming):
		AbstractPropertiesParser.__init__(self, incoming, self.METADATA_FILE)

	def getDatasetType(self):
		return self.get(self.DATASET_TYPE_PROPERTY)

	def getIBrain2DatasetId(self):
		return self.get(self.IBRAIN2_DATASET_ID_PROPERTY)

# --- concrete parser classes ----------------------

class AcquiredDatasetMetadataParser(AbstractMetadataParser):
	PLATE_CODE_PRPOPERTY = "barcode"
	INSTRUMENT_PROPERTY = "instrument.id"
	TIMESTAMP_PROPERTY = "timestamp" # not used
	
	# All dataset properties.
	# Returns:
	#	an iterator which yields (propertyCode, propertyValue) pairs 
	def getDatasetPropertiesIter(self):
		properties = AbstractPropertiesParser.getDatasetPropertiesIter(self)
		properties = [ (key, value) for (key, value) in properties if key != "ibrain2.assay.id" ]
		properties.append((self.INSTRUMENT_PROPERTY, self.get(self.INSTRUMENT_PROPERTY)))
		return properties
	
	def getPlateCode(self):
		return self.get(self.PLATE_CODE_PRPOPERTY)

class DerivedDatasetMetadataParser(AbstractMetadataParser):
	WORKFLOW_FILE_PREFIX = "workflow_"
	PARENT_DATSASET_PERMID_PRPOPERTY = "storage_provider.parent.dataset.id"
	DATASET_TYPE_PROPERTY = "dataset.type"
	WORKFLOW_NAME_PROPERTY = "ibrain2.workflow.name"
	WORKFLOW_AUTHOR_PROPERTY = "ibrain2.workflow.author"
		
	_workflowName = None
	_workflowAuthor = None
	
	def __init__(self, incoming):
		AbstractMetadataParser.__init__(self, incoming)
		workflowFile = self._findFile(incoming, self.WORKFLOW_FILE_PREFIX)
		basename = os.path.splitext(workflowFile)[0]
		tokens = basename.split("_")
		if len(tokens) < 3:
			raise Exception("Cannot parse workflow name and author from: "+workflowFile)
		self._workflowName = tokens[1]
		self._workflowAuthor = tokens[2]

	def getDatasetPropertiesIter(self):
		properties = AbstractMetadataParser.getDatasetPropertiesIter(self)
		properties.append((self.WORKFLOW_NAME_PROPERTY, self._workflowName))
		properties.append((self.WORKFLOW_AUTHOR_PROPERTY, self._workflowAuthor))
		return properties
		
	def getParentDatasetPermId(self):
		return self.get(self.PARENT_DATSASET_PERMID_PRPOPERTY)
	
	def getDatasetType(self):
		return self.get(self.DATASET_TYPE_PROPERTY)

class AssayParser(AbstractPropertiesParser):
	ASSAY_FILE_PREFIX="assay_"

	ASSAY_ID_PROPERTY = "assay.id"
	ASSAY_TYPE_PROPERTY = "assay.type"
	ASSAY_DESC_PROPERTY = "assay.description"
	LAB_LEADER_PROPERTY = "labinfo.pi"
	EXPERIMENTER_PROPERTY = "experimenter.login"
	WORKFLOW_NAME_PROPERTY = "workflow.name"
	WORKFLOW_AUTHOR_PROPERTY = "workflow.author"

	def __init__(self, incoming):
		AbstractPropertiesParser.__init__(self, incoming, self._findFile(incoming, self.ASSAY_FILE_PREFIX))

class RegistrationConfirmationUtils:
	""" path to the registration confirmation directory relative to the incoming dataset """
	CONFIRMATION_DIRECTORY = "registration-status"
	
	STATUS_PROPERTY = "storage_provider.storage.status"
	STATUS_OK = "STORAGE_SUCCESS"
	STATUS_ERROR = "STORAGE_FAILED"
	ERROR_MSG_PROPERTY = "storage_provider.message"

	OPENBIS_DATASET_ID_PROPERTY = "storage_provider.dataset.id"
	IBRAIN2_STATUS_FILE_PREFIX = "ibrain2_dataset_id_"
	IBRAIN2_STATUS_FILE_SUFFIX = ".properties"

	def _getDestinationDir(self, incoming):
		return File(incoming).getParentFile().getParentFile().getParent() + "/" + self.CONFIRMATION_DIRECTORY
	
	def _getConfirmationFileName(self, ibrain2DatasetId):
		return self.IBRAIN2_STATUS_FILE_PREFIX + ibrain2DatasetId + self.IBRAIN2_STATUS_FILE_SUFFIX

	def _getStatusFilePath(self, ibrain2DatasetId, incoming):
		return self._getDestinationDir(incoming) + "/" + self._getConfirmationFileName(ibrain2DatasetId)

	def _prop(self, name, value):
		return "" + name + " = " + value + "\n"
	
	def _writeConfirmationFile(self, ibrain2DatasetId, fileContent, incoming):
		confirmationFile = self._getStatusFilePath(ibrain2DatasetId, incoming)
		self._writeFile(confirmationFile, fileContent)
		
	def _writeFile(self, file, fileContent):
		file = open(file, "w")
		file.write(fileContent)
		file.close()

	def createSuccessStatus(self, ibrain2DatasetId, openbisDatasetId, incoming):
		fileContent  = self._prop(self.STATUS_PROPERTY, self.STATUS_OK)
		fileContent += self._prop(AbstractMetadataParser.IBRAIN2_DATASET_ID_PROPERTY, ibrain2DatasetId)
		fileContent += self._prop(self.OPENBIS_DATASET_ID_PROPERTY, openbisDatasetId)
		self._writeConfirmationFile(ibrain2DatasetId, fileContent, incoming)
			
	def createFailureStatus(self, ibrain2DatasetId, errorMessage, incoming):
		fileContent  = self._prop(self.STATUS_PROPERTY, self.STATUS_ERROR)
		fileContent += self._prop(AbstractMetadataParser.IBRAIN2_DATASET_ID_PROPERTY, ibrain2DatasetId)
		fileContent += self._prop(self.ERROR_MSG_PROPERTY, errorMessage)
		self._writeConfirmationFile(ibrain2DatasetId, fileContent, incoming)

# --------------

def setPropertiesAndRegister(imageDataset, iBrain2DatasetId, metadataParser, incoming, service, factory):
    imageRegistrationDetails = factory.createImageRegistrationDetails(imageDataset, incoming)
    for propertyCode, value in metadataParser.getDatasetPropertiesIter():
        imageRegistrationDetails.setPropertyValue(propertyCode, value)

    tr = service.transaction(incoming, factory)
    dataset = tr.createNewDataSet(imageRegistrationDetails)
    dataset.setParentDatasets([metadataParser.getParentDatasetPermId()])
    imageDataSetFolder = tr.moveFile(incoming.getPath(), dataset)
    ok = tr.commit()
    if ok:
		print "success", iBrain2DatasetId
		createSuccessStatus(iBrain2DatasetId, dataset, incoming.getPath())
    
"""
Returns:
   (plateSpace, plateCode) tuple for the plate connected with the specified dataset
   or (None, None) if the dataset does not exist or is not connected to the plate.
"""
def tryGetConnectedPlate(state, openbisDatasetId, iBrain2DatasetId, incomingPath):
	openbis = state.getOpenBisService()		
	dataset = openbis.tryGetDataSet(openbisDatasetId)
	if dataset != None:
		plate = dataset.getSample()
		if plate != None:
			return (plate.getSpace().getCode(), plate.getCode())
		else:
			errorMsg = "No plate is connected to the dataset: "+openbisDatasetId+"."
	else:
		errorMsg = "Dataset does not exist or is not accessible: "+openbisDatasetId+". Maybe the dataset has not been registered yet. Try again later."
	RegistrationConfirmationUtils().createFailureStatus(iBrain2DatasetId, errorMsg, incomingPath)
	return (None, None)

def createSuccessStatus(iBrain2DatasetId, dataset, incomingPath):
	datasetCode = dataset.getDataSetCode()
	RegistrationConfirmationUtils().createSuccessStatus(iBrain2DatasetId, datasetCode, incomingPath)

def createFailureStatus(iBrain2DatasetId, throwable, incoming):
    RegistrationConfirmationUtils().createFailureStatus(iBrain2DatasetId, throwable.getMessage(), incoming.getPath())
	
# -------------- TODO: remove tests

TEST_DIR = "/Users/tpylak/main/src/screening-demo/biozentrum/dropboxes/ibrain2-dropboxes-test"

def testMetadataParsers():
	print "-- acquired ---------------------------------"
	parser = AcquiredDatasetMetadataParser(TEST_DIR+"/HCS_IMAGE_RAW/ibrain2_dataset_id_32")
	print "dataset type:", parser.getDatasetType()
	print "plate:", parser.getPlateCode()
	print "properties"
	for key, value in parser.getDatasetPropertiesIter():
		print key, value
		
	print "\n-- derived ---------------------------------"
	parser = DerivedDatasetMetadataParser(TEST_DIR+"/HCS_IMAGE_OVERVIEW/ibrain2_dataset_id_48")
	print "dataset type:", parser.getDatasetType()
	print "parent perm id:", parser.getParentDatasetPermId()
	print "properties"
	for key, value in parser.getDatasetPropertiesIter():
		print key, value

def testAssayParsers():
	print "-- assay ---------------------------------"
	parser = AssayParser(TEST_DIR+"/HCS_IMAGE_RAW/ibrain2_dataset_id_32")
	print "properties"
	for key, value in parser.getPropertiesIter():
		print key, value

def testConfirmationFiles():
	IBRAIN2Utils().createSuccessStatus("123", "123123123123-12312", TEST_DIR+"/HCS_IMAGE_RAW/ibrain2_dataset_id_32")
	IBRAIN2Utils().createFailureStatus("321", "Global catastrophy!", TEST_DIR+"/HCS_IMAGE_RAW/ibrain2_dataset_id_32")

#testAssayParsers()
#testMetadataParsers()
#testConfirmationFiles()

