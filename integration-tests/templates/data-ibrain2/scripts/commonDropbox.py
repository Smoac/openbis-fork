#! /usr/bin/env python

import os

class AbstractPropertiesParser:
	_propertiesDict = None

	def __init__(self, incoming, fileName):
		path = os.path.join(incoming, fileName)
		self._propertiesDict = self._parseMetadata(path)
	
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
		for key, value in self._propertiesDict.iteritems():
			yield (key, value)	
				
	# All dataset properties.
	# Returns:
	#	an iterator which yields (propertyCode, propertyValue) pairs 
	def getDatasetPropertiesIter(self):
		for key, value in self._propertiesDict.iteritems():
			if key.startswith(self.DATASET_PROPERTY_PREFIX):
				yield (key, value)		

class AbstractMetadataParser(AbstractPropertiesParser):
	METADATA_FILE="metadata.properties"

	IBRAIN2_DATASET_ID_PROPERTY = "brain2.dataset.id" 
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
		for propertyCode, value in AbstractPropertiesParser.getDatasetPropertiesIter(self):
			yield (propertyCode, value)
		yield (self.INSTRUMENT_PROPERTY, self.get(self.INSTRUMENT_PROPERTY))
	
	def getPlateCode(self):
		return self.get(self.PLATE_CODE_PRPOPERTY)

class DerivedDatasetMetadataParser(AbstractMetadataParser):
	PARENT_DATSASET_PERMID_PRPOPERTY = "storage_provider.parent.dataset.id"
	DATASET_TYPE_PROPERTY = "dataset.type"
		
	def getDatasetPropertiesIter(self):
		return AbstractMetadataParser.getDatasetPropertiesIter(self)
		
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

	def _findAssayFile(self, incoming):
		for file in os.listdir(incoming):
			print file
			if file.startswith(self.ASSAY_FILE_PREFIX):
				return file
		raise Exception("Assay file not found!")
		
	def __init__(self, incoming):
		AbstractPropertiesParser.__init__(self, incoming, self._findAssayFile(incoming))

class IBRAIN2Utils:
	""" path to the registration confirmation directory relative to the incoming dataset """
	CONFIRMATION_DIRECTORY = "../../registration-status"
	
	STATUS_PROPERTY = "storage_provider.storage.status"
	OK = "STORAGE_SUCCESS"
	ERROR = "STORAGE_FAILED"
	ERROR_MSG_PROPERTY = "storage_provider.message"

	OPENBIS_DATASET_ID_PROPERTY = "storage_provider.dataset.id"
	IBRAIN2_STATUS_FILE_PREFIX = "ibrain2_dataset_id_"
	IBRAIN2_STATUS_FILE_SUFFIX = ".properties"

	def _getStatusFileName(self, ibrain2DatasetId, incoming):
		return incoming + "/" + self.CONFIRMATION_DIRECTORY + "/" + self.IBRAIN2_STATUS_FILE_PREFIX + ibrain2DatasetId + self.IBRAIN2_STATUS_FILE_SUFFIX

	def _prop(self, name, value):
		return name + " = " + value + "\n"
	
	def _writeConfirmationFile(self, ibrain2DatasetId, fileContent, incoming):
		confirmationFile = self._getStatusFileName(ibrain2DatasetId, incoming)
		file = open(confirmationFile, "w")
		file.write(fileContent)
		file.close()
		
	def createSuccessStatus(self, ibrain2DatasetId, openbisDatasetId, incoming):
		fileContent  = self._prop(self.STATUS_PROPERTY, self.OK)
		fileContent += self._prop(AbstractMetadataParser.IBRAIN2_DATASET_ID_PROPERTY, ibrain2DatasetId)
		fileContent += self._prop(self.OPENBIS_DATASET_ID_PROPERTY, openbisDatasetId)
		self._writeConfirmationFile(ibrain2DatasetId, fileContent, incoming)

	def createFailureStatus(self, ibrain2DatasetId, errorMessage, incoming):
		fileContent  = self._prop(self.STATUS_PROPERTY, self.ERROR)
		fileContent += self._prop(AbstractMetadataParser.IBRAIN2_DATASET_ID_PROPERTY, ibrain2DatasetId)
		fileContent += self._prop(self.ERROR_MSG_PROPERTY, errorMessage)
		self._writeConfirmationFile(ibrain2DatasetId, fileContent, incoming)
		
		
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

