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
	"""Factory method for the TimeSeriesData object. Returns None if it cannot be created"""
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
		This class ported from Java, thus the camelCase naming. 
	"""
	def __init__(self, metadataMap, errors):
		self.metadataMap = metadataMap
		self.errors = errors

	def checkIsSpecified(self, property, displayName):
		if self.metadataMap.get(property) is None:
			self.errors.append(ValidationError.createFileValidationError("A " + displayName
					+ " must be specified."))
			return False
		return True


def validate_data(time_series_data, errors):
	chebiRegex = re.compile("^CHEBI:[0-9]+")
	bsbmeRegex = re.compile("^BSBME:[0-9]+")
	dataLines = time_series_data.getRawDataLines()
	lineCount = 0
	for line in dataLines:
		# The header needs to be CompoundID
		if lineCount is 0:
			if line[0] != "CompoundID":
				errors.append(createFileValidationError("The first data column must be 'CompoundID'"))
				break
			lineCount = lineCount + 1
			continue

		# The compound id should be one of these forms
		compoundId = line[0]
		if not chebiRegex.match(compoundId):
			if not bsbmeRegex.match(compoundId):
				errors.append(createFileValidationError("Line " + str(lineCount + 1) + ", column 1 must be of the format 'CHEBI:#' or 'BSBME:#' (instead of " + compoundId + ")."))
		lineCount = lineCount + 1
		
def validate_metadata(time_series_data, errors):
	metadata = time_series_data.getMetadataMap()
	validationHelper = ValidationHelper(metadata, errors)
	
	# validate the strain
	validationHelper.checkIsSpecified("STRAIN", "strain")
	
	# validate the timepoint type
	if validationHelper.checkIsSpecified("TIMEPOINT TYPE", "time point type"):
		if metadata.get("TIMEPOINT TYPE").upper() not in ['EX', 'IN', 'SI']:
			errors.append(createFileValidationError("The timepoint type must be one of 'EX', 'IN', 'SI'"))
			
	# validate the cell location
	if validationHelper.checkIsSpecified("CELL LOCATION", "cell location"):
		if metadata.get("CELL LOCATION").upper() not in ['CE', 'ES', 'ME', 'CY', 'NC']:
			errors.append(createFileValidationError("The cell location must be one of 'CE', 'ES', 'ME', 'CY', 'NC'"))
		
	# validate the value type
	if validationHelper.checkIsSpecified("VALUE TYPE", "value type"):
		if metadata.get("VALUE TYPE").lower() not in ['value', 'mean', 'median', 'std', 'var', 'error', 'iqr']:
			errors.append(createFileValidationError("The value type must be one of 'Value', 'Mean', 'Median', 'Std', 'Var', 'Error', 'Iqr'"))

	# validate the value unit
	if validationHelper.checkIsSpecified("VALUE UNIT", "value unit"):
		if metadata.get("VALUE UNIT").lower() not in ['mm', 'um', 'ratiot1', 'ratiocs']:
			errors.append(createFileValidationError("The value unit must be one of 'mM', 'uM', 'RatioT1', 'RatioCs'"))
	
	# validate the value type
	if validationHelper.checkIsSpecified("SCALE", "scale"):
		if metadata.get("SCALE").lower() not in ['lin', 'log2', 'log10', 'ln']:
			errors.append(createFileValidationError("The scale must be one of 'lin', 'log2', 'log10', 'ln'"))

def validate_data_set_file(file):
	errors = []
	time_series_data = create_time_series_excel(file.getAbsolutePath())
	if time_series_data is None:
		errors.append(createFileValidationError(file.getName() + " is not an Excel file."))
		return errors
		
	# validate the metadata
	validate_metadata(time_series_data, errors)
			
	# validate the data
	validate_data(time_series_data, errors)
	
	return errors
