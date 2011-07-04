#! /usr/bin/env python

import os
from java.io import File

class AbstractPropertiesParser:
	_propertiesDict = None
	_propertiesFilePath = None
	
	def __init__(self, incoming, fileName):
		self._propertiesFilePath = os.path.join(incoming, fileName)
		self._propertiesDict = self._parseMetadata(self._propertiesFilePath)
		
	
	# Returns: name of the file
	def _findFile(self, incoming, prefix):
		for file in os.listdir(incoming):
			if file.startswith(prefix):
				return file
		raise Exception("No file with prefix '" + prefix + "' has been found!")

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
				raise Exception("Cannot find '=' in line '" + line + "' in file: " + path)
			key = line[:ix].strip()
			value = line[ix + 1:].strip()
			if key in myDict:
				raise Exception("Duplicated key '" + key + "' in file: " + path)		
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
	METADATA_FILE = "metadata.properties"

	IBRAIN2_DATASET_ID_PROPERTY = "ibrain2.dataset.id" 
	DATASET_PROPERTY_PREFIX = "ibrain2."
	DATASET_TYPE_PROPERTY = "dataset.type"

	def __init__(self, incoming):
		AbstractPropertiesParser.__init__(self, incoming, self.METADATA_FILE)
		# remove the metadata file, so that it is not registered as a dataset part
		os.remove(self._propertiesFilePath)
		self._propertiesFilePath = None
		
	""" Recreates the metadata file, to make it easier to identify the rejected dataset """
	def recreateMetadataFile(self, incoming):
		outPath = incoming.getPath() + "/" + self.METADATA_FILE
		out = open(outPath, 'w')
		for key, value in self.getPropertiesIter():
			out.write(key + " = " + value +"\n");
		out.close()
		
	def getDatasetType(self):
		return self.get(self.DATASET_TYPE_PROPERTY)

	def getIBrain2DatasetId(self):
		return self.get(self.IBRAIN2_DATASET_ID_PROPERTY)

# --- concrete parser classes ----------------------

class AcquiredDatasetMetadataParser(AbstractMetadataParser):
	PLATE_CODE_PRPOPERTY = "barcode"
	INSTRUMENT_PROPERTY = "instrument.id"
	TIMESTAMP_PROPERTY = "acquisition.timestamp"
	
	# All dataset properties.
	# Returns:
	#	an iterator which yields (propertyCode, propertyValue) pairs 
	def getDatasetPropertiesIter(self):
		properties = AbstractPropertiesParser.getDatasetPropertiesIter(self)
		properties = [ (key, value) for (key, value) in properties if key != "ibrain2.assay.id" ]
		properties.append((self.INSTRUMENT_PROPERTY, self.get(self.INSTRUMENT_PROPERTY)))
		properties.append((self.TIMESTAMP_PROPERTY, self.get(self.TIMESTAMP_PROPERTY)))	
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
			raise Exception("Cannot parse workflow name and author from: " + workflowFile)
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
	ASSAY_FILE_PREFIX = "assay_"

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
	""" name of the registration confirmation directory """
	CONFIRMATION_DIRECTORY = "registration-status"
	""" name of the directory where duplicated dataset are moved """
	DUPLICATED_DATASETS_DIRECTORY = "duplicated-datasets"
	
	STATUS_PROPERTY = "storage_provider.storage.status"
	STATUS_OK = "STORAGE_SUCCESSFUL"
	STATUS_ERROR = "STORAGE_FAILED"
	ERROR_MSG_PROPERTY = "storage_provider.message"

	OPENBIS_DATASET_ID_PROPERTY = "storage_provider.dataset.id"
	IBRAIN2_STATUS_FILE_PREFIX = "ibrain2_dataset_id_"
	IBRAIN2_STATUS_FILE_SUFFIX = ".properties"

	""" Returns a directory 3 levels above the incoming directory """
	def _getTopLevelDir(self, incoming):
		return File(incoming).getParentFile().getParentFile().getParent()

	def _getDuplicatedDatasetsDir(self, incoming):
		return self._getTopLevelDir(incoming) + "/" + self.DUPLICATED_DATASETS_DIRECTORY

	def _ensureDirExists(self, dir):
		if not os.path.exists(dir):
			os.mkdir(dir)
	
	""" 
	Moves the incoming directory to the folder with duplicated datasets. 
	param duplicatedChildrenDatasetException - instance of DuplicatedChildrenDatasetException
	Returns: the path where the dataset has been moved
	"""
	def moveDuplicatedDataset(self, duplicatedChildrenDatasetException):
		incomingPath = duplicatedChildrenDatasetException.incomingPath
		duplicatedDir = self._getDuplicatedDatasetsDir(incomingPath)
		duplicatedTypedDir = duplicatedDir + "/" + duplicatedChildrenDatasetException.datasetTypeCode
		self._ensureDirExists(duplicatedDir)
		self._ensureDirExists(duplicatedTypedDir)
		destDir = duplicatedTypedDir + "/" + File(incomingPath).getName()
		os.rename(incomingPath, destDir)
		return destDir
		
	def _getDestinationDir(self, incoming):
		return self._getTopLevelDir(incoming) + "/" + self.CONFIRMATION_DIRECTORY
	
	def _getConfirmationFileName(self, ibrain2DatasetId):
		return self.IBRAIN2_STATUS_FILE_PREFIX + ibrain2DatasetId + self.IBRAIN2_STATUS_FILE_SUFFIX

	def _getStatusFilePath(self, ibrain2DatasetId, incoming):
		return self._getDestinationDir(incoming) + "/" + self._getConfirmationFileName(ibrain2DatasetId)

	def _prop(self, name, value):
		return "" + name + " = " + (value or "") + "\n"
	
	def _writeConfirmationFile(self, ibrain2DatasetId, fileContent, incoming):
		confirmationFile = self._getStatusFilePath(ibrain2DatasetId, incoming)
		self._writeFile(confirmationFile, fileContent)
		
	def _writeFile(self, file, fileContent):
		file = open(file, "w")
		file.write(fileContent)
		file.close()

	def createSuccessStatus(self, ibrain2DatasetId, openbisDatasetId, incoming):
		fileContent = self._prop(self.STATUS_PROPERTY, self.STATUS_OK)
		fileContent += self._prop(AbstractMetadataParser.IBRAIN2_DATASET_ID_PROPERTY, ibrain2DatasetId)
		fileContent += self._prop(self.OPENBIS_DATASET_ID_PROPERTY, openbisDatasetId)
		self._writeConfirmationFile(ibrain2DatasetId, fileContent, incoming)
			
	def createFailureStatus(self, ibrain2DatasetId, errorMessage, incoming):
		fileContent = self._prop(self.STATUS_PROPERTY, self.STATUS_ERROR)
		fileContent += self._prop(AbstractMetadataParser.IBRAIN2_DATASET_ID_PROPERTY, ibrain2DatasetId)
		errorMessage = errorMessage.replace("\"", "'")
		errorMessage = "\"" + errorMessage + "\""
		fileContent += self._prop(self.ERROR_MSG_PROPERTY, errorMessage)
		self._writeConfirmationFile(ibrain2DatasetId, fileContent, incoming)

# --------------

def setImageDatasetPropertiesAndRegister(imageDataset, metadataParser, incoming, service, factory, tr = None):
	iBrain2DatasetId = metadataParser.getIBrain2DatasetId()
   	imageRegistrationDetails = factory.createImageRegistrationDetails(imageDataset, incoming)
   	for propertyCode, value in metadataParser.getDatasetPropertiesIter():
   		imageRegistrationDetails.setPropertyValue(propertyCode, value)

	if tr is None: 
		tr = service.transaction(incoming, factory)
	dataset = tr.createNewDataSet(imageRegistrationDetails)
	dataset.setParentDatasets([metadataParser.getParentDatasetPermId()])
	imageDataSetFolder = tr.moveFile(incoming.getPath(), dataset)
	if tr.commit():
		createSuccessStatus(iBrain2DatasetId, dataset, incoming.getPath())

"""
param ensureSingleChild - if true, then it will be ensured that the parent dataset 
          had no children of 'datasetType' and if it is not the case an exception will be thrown.
"""
def registerDerivedBlackBoxDataset(state, service, factory, incoming, metadataParser, datasetType, fileFormatType, ensureSingleChild = False):
    transaction = service.transaction(incoming, factory)
    if ensureSingleChild:
    	ensureOrDieNoChildrenOfType(metadataParser.getParentDatasetPermId(), datasetType, incoming.getPath(), transaction)
    dataset = transaction.createNewDataSet()
    dataset.setDataSetType(datasetType)
    dataset.setFileFormatType(fileFormatType)
    registerDerivedDataset(state, transaction, dataset, incoming, metadataParser)
    
def registerDerivedDataset(state, transaction, dataset, incoming, metadataParser):
    iBrain2DatasetId = metadataParser.getIBrain2DatasetId()
    openbisDatasetParentPermId = metadataParser.getParentDatasetPermId()

    (space, plate) = tryGetConnectedPlate(state, openbisDatasetParentPermId, iBrain2DatasetId, incoming.getPath())
    if plate == None:
        return
    
    dataset.setSample(transaction.getSample('/' + space + '/' + plate))
    dataset.setMeasuredData(False)
    for propertyCode, value in metadataParser.getDatasetPropertiesIter():
        dataset.setPropertyValue(propertyCode, value)
    dataset.setParentDatasets([metadataParser.getParentDatasetPermId()])

    print "moving", incoming.getPath()
    transaction.moveFile(incoming.getPath(), dataset)
    if transaction.commit():
        createSuccessStatus(iBrain2DatasetId, dataset, incoming.getPath())

def findCSVFile(dir):
	for file in os.listdir(dir):
		if file.endswith(".csv"):
			return dir + "/" + file
	raise Exception("No CSV file has been found in " + dir)

"""
Returns:
   (plateSpace, plateCode) tuple for the plate connected with the specified dataset
   or (None, None) if the dataset does not exist or is not connected to the plate.
"""
def tryGetConnectedPlate(state, openbisDatasetPermId, iBrain2DatasetId, incomingPath):
	openbis = state.getOpenBisService()
	dataset = openbis.tryGetDataSet(openbisDatasetPermId)
	if dataset != None:
		plate = dataset.getSample()
		if plate != None:
			return (plate.getSpace().getCode(), plate.getCode())
		else:
			errorMsg = "No plate is connected to the dataset: " + openbisDatasetPermId + "."
	else:
		errorMsg = "Dataset does not exist or is not accessible: " + openbisDatasetPermId + ". Maybe the dataset has not been registered yet. Try again later."
	state.getOperationLog().error(errorMsg)
	RegistrationConfirmationUtils().createFailureStatus(iBrain2DatasetId, errorMsg, incomingPath)
	return (None, None)

""" 
Thrown if a dataset with code 'parentDataSetCode' has already children datasets of type 'datasetTypeCode', 
"""
class DuplicatedChildrenDatasetException(Exception):
	parentDataSetCode = None
	datasetTypeCode = None 
	incomingPath = None
	
	def __init__(self, parentDataSetCode, datasetTypeCode, incomingPath):
		self.parentDataSetCode = parentDataSetCode
		self.datasetTypeCode = datasetTypeCode
		self.incomingPath = incomingPath
		
	def __str__(self):
		return "Dataset "+ self.parentDataSetCode +" has already children of type "+self.datasetTypeCode+" registered."

def createSuccessStatus(iBrain2DatasetId, dataset, incomingPath):
	datasetCode = dataset.getDataSetCode()
	RegistrationConfirmationUtils().createSuccessStatus(iBrain2DatasetId, datasetCode, incomingPath)

def createFailureStatus(datasetMetadataParser, throwable, incoming):
	datasetMetadataParser.recreateMetadataFile(incoming)
	
	iBrain2DatasetId = datasetMetadataParser.getIBrain2DatasetId()
	msg = throwable.getMessage()
	if msg == None:
		msg = throwable.toString()

	try:
		# check if the field exists
		throwable.value
		if isinstance(throwable.value, DuplicatedChildrenDatasetException):
			newPath = RegistrationConfirmationUtils().moveDuplicatedDataset(throwable.value)
			msg += "\nMoving " + throwable.value.incomingPath + " to " + newPath
	except AttributeError:
	    pass
	
	RegistrationConfirmationUtils().createFailureStatus(iBrain2DatasetId, msg, incoming.getPath())
	
"""
Returns: all children of 'typeCode' type having dataset with code 'parentDataSetCode' as a parent
"""
def fetchChildrenOfType(dataSetCode, typeCode, transaction):
    container = transaction.getDataSet(dataSetCode)
    if None == container:
        return []
    return [dataSet for dataSet in container.getChildrenDataSets() if typeCode == dataSet.getDataSetType()]

""" 
If dataset with code 'parentDataSetCode' has already children datasets of type 'typeCode', 
then the 'incomingPath' is moved to the special directory with duplicated datasets 
and an exception is raised.
"""
def ensureOrDieNoChildrenOfType(dataSetCode, typeCode, incomingPath, transaction):
	children = fetchChildrenOfType(dataSetCode, typeCode, transaction)
	if len(children) > 0:
		raise DuplicatedChildrenDatasetException(dataSetCode, typeCode, incomingPath)

   
# -------------- TODO: remove tests

TEST_DIR = "/Users/tpylak/main/src/screening-demo/biozentrum/dropboxes/ibrain2-dropboxes-test"

def testMetadataParsers():
	print "-- acquired ---------------------------------"
	parser = AcquiredDatasetMetadataParser(TEST_DIR + "/HCS_IMAGE_RAW/ibrain2_dataset_id_32")
	print "dataset type:", parser.getDatasetType()
	print "plate:", parser.getPlateCode()
	print "properties"
	for key, value in parser.getDatasetPropertiesIter():
		print key, value
		
	print "\n-- derived ---------------------------------"
	parser = DerivedDatasetMetadataParser(TEST_DIR + "/HCS_IMAGE_OVERVIEW/ibrain2_dataset_id_48")
	print "dataset type:", parser.getDatasetType()
	print "parent perm id:", parser.getParentDatasetPermId()
	print "properties"
	for key, value in parser.getDatasetPropertiesIter():
		print key, value

def testAssayParsers():
	print "-- assay ---------------------------------"
	parser = AssayParser(TEST_DIR + "/HCS_IMAGE_RAW/ibrain2_dataset_id_32")
	print "properties"
	for key, value in parser.getPropertiesIter():
		print key, value

def testConfirmationFiles():
	IBRAIN2Utils().createSuccessStatus("123", "123123123123-12312", TEST_DIR + "/HCS_IMAGE_RAW/ibrain2_dataset_id_32")
	IBRAIN2Utils().createFailureStatus("321", "Global catastrophy!", TEST_DIR + "/HCS_IMAGE_RAW/ibrain2_dataset_id_32")

#testAssayParsers()
#testMetadataParsers()
#testConfirmationFiles()

