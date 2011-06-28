import os
import re
import sys
import java.io.File
from java.io import IOException
from java.lang import IllegalArgumentException
from ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.validation import ValidationError, ValidationScriptRunner
from ch.systemsx.cisd.openbis.dss.generic.shared.utils import ExcelFileReader
from ch.systemsx.cisd.common.logging import LogFactory, LogCategory

operationLog = LogFactory.getLogger(LogCategory.OPERATION, ValidationScriptRunner)
OPENBIS_METADATA_SHEET_NAME = "openbis-metadata"
OPENBIS_DATA_SHEET_NAME = "openbis-data"

class TimeSeriesDataExcel:
	"""
		An abstraction for accessing time series data following the BaSynthec conventions 
		from an Excel file. This class ported from Java, thus the camelCase naming.
	"""
	def __init__(self, file, fileReader):
		self.file = file
		self.fileReader = fileReader
		
	def getRawMetadataLines(self):
		"""Get the raw lines of the metadata sheet."""
		try:
			return self.fileReader.readLines(OPENBIS_METADATA_SHEET_NAME);
		except IOException, ex:
			operationLog.error("Could not read data from [file: " + self.file.getPath() + ", sheet: "
					+ OPENBIS_METADATA_SHEET_NAME + "]", ex)
		return []

	def getRawDataLines(self):
		"""Get the raw lines of the data sheet."""
		try:
			return self.fileReader.readLines(OPENBIS_DATA_SHEET_NAME)
		except IOException, ex:
			operationLog.error("Could not read data from [file: " + file.getPath() + ", sheet: "
					+ OPENBIS_DATA_SHEET_NAME + "]", ex)
		return []

	def getMetadataMap(self):
		"""
			Return the metadata has a hashmap, with all keys uppercased.
			
			Assumes the metadata sheet corresponds to the following format: [Property] [Value] [... stuff
			that can be ignored], that is the property name is in column 1 and property value is in
			column 2, and everything else can be ignored.
		"""
		metadataMap = {}
		metadataLines = self.getRawMetadataLines()
		
		# Skip the first line, this is just the header
		for i in range(1, metadataLines.size()):
			line = metadataLines.get(i)
			value = line[1];
			if "BLANK" == value:
				value = None
			metadataMap[line[0].upper()] = value
		return metadataMap
		
def create_time_series_excel(fileName):
	"""Factory method for the TimeSeriesData object. Returns None if it cannot be created."""
	file = java.io.File(fileName)
	try:
		workbook = ExcelFileReader.getExcelWorkbook(file)
		fileReader = ExcelFileReader(workbook, True)
		return TimeSeriesDataExcel(file, fileReader)
	except IllegalArgumentException, ex:
		operationLog.error("Could not open file [" + fileName + "] as Excel data.", ex)
	except IOException, ex:
		operationLog.error("Could not open file [" + fileName + "] as Excel data.", ex)
	return None


		
class ValidationHelper:
	"""
		Methods for simplifying validation in BaSynthec.
		This class is ported from Java, thus the camelCase naming. 
	"""
	def __init__(self, metadataMap, errors):
		self.metadataMap = metadataMap
		self.errors = errors

	def checkIsSpecified(self, property, displayName):
		"""Verify that a property is specified; if not, add a validation error to the list."""
		if self.metadataMap.get(property) is None:
			self.errors.append(ValidationError.createFileValidationError("A " + displayName
					+ " must be specified."))
			return False
		return True
		
	
strainIdRegex = re.compile("^MGP[0-9]{1,3}")
def isStrainIdValid(strainId):
	"""Return true if the strain id passes validation (has the form MGP[:digit:]{1,3})"""
	match = strainIdRegex.match(strainId)
	if match is None:
		return False
	return match.end() == len(strainId)
