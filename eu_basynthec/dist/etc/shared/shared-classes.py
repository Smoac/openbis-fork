"""
Code that is shared between the handlers and validators for different data set types.

Includes a class for reading time series data from Excel, and code for validating BaSynthec data, in particular strains.
"""
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
      if line[0] is not None:
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
  
  def validateStrain(self):
    """Verify that the strain is specified and of the correct format"""
    if not self.checkIsSpecified("STRAIN", "strain"):
      return
    strain = self.metadataMap.get("STRAIN")
    if not isStrainIdValid(strain):
      self.errors.append(createFileValidationError("Strain " + strainValidationErrorMessageFragment(strain)))
      
  def validateDefaultHeaderFormat(self):
    """Validate that header format is either not specified or matches default (TIME)"""
    if self.metadataMap.get("HEADER FORMAT") is None:
      return
    format = self.metadataMap.get("HEADER FORMAT")
    expected_format = "TIME"
    if expected_format != format:
      self.errors.append(createFileValidationError("Header format must be " + expected_format + " (not " + format + ")."))
      
  def validateExplicitHeaderFormat(self, expected_format):
    """Validate that header format is specified and matches the expected_format argument"""
    if not self.checkIsSpecified("HEADER FORMAT", "header format"):
      return
    format = self.metadataMap.get("HEADER FORMAT")
    if expected_format != format:
      self.errors.append(createFileValidationError("Header format must be " + expected_format + " (not " + format + ")."))
      
  def validateControlledVocabularyProperty(self, property, displayName, allowedValues, allowedValuesDisplay):
    """Validate that the property is specified and in the list of allowed values"""
    value = self.metadataMap.get(property)
    isControlledVocabularyPropertyValid(value, displayName, allowedValues, allowedValuesDisplay, self.errors)
        
  def validateStartDataRowCol(self):
    if self.checkIsSpecified("START DATA ROW", "Start Data Row"):
      value = self.metadataMap.get("START DATA ROW")
      match = re.match("[0-9]+", value)
      if match is None:
        self.errors.append(createFileValidationError("The Start Data Row must be a number (not " + value + ")."))
    if self.checkIsSpecified("START DATA COL", "Start Data Col"):
      value = self.metadataMap.get("START DATA COL")
      match = re.match("[A-Z]", value)
      if match is None:
        self.errors.append(createFileValidationError("The Start Data Col must be a letter between A and Z (not " + value + ")."))
  
strainIdRegex = re.compile("^JJS-MGP[0-9]{1,3}|^JJS-DIN[0-9]{1,3}|^MS|CHASSIS\s*[1-3]|WT 168 TRP\+")
def isStrainIdValid(strainId):
  """Return true if the strain id passes validation (has the form sepecified in the regex)"""
  strainId = strainId.strip().upper()
  match = strainIdRegex.match(strainId)
  if match is None:
    return False
  return match.end() == len(strainId)
  
def strainValidationErrorMessageFragment(strain):
    """Return a sentence fragment describing the strain validation error."""
    return "must be either JJS-MGP[0-999], JJS-DIN[0-999], MS, CHASSIS [1-3], or WT 168 TRP+ (instead of " + strain + ")."

def isControlledVocabularyPropertyValid(value, displayName, allowedValues, allowedValuesDisplay, errors):
    """Validate that the property is specified and in the list of allowed values"""
    if value is None:
      errors.append(ValidationError.createFileValidationError("A " + displayName + " must be specified."))
      return False
    value = value.upper()
    if value not in allowedValues:
      if len(allowedValues) > 1:
        errors.append(createFileValidationError("The " + displayName + " must be one of " + allowedValuesDisplay + " (not " + value + ")."))
        return False
      else:
        errors.append(createFileValidationError("The " + displayName + " must be " + allowedValuesDisplay + " (not " + value + ")."))
        return False
    return True
  
def getInitialDataRowAndCol(metadata):
  """Extract the initial row and column as specified in the metadata. Returns an array with [row, col]."""
  # get the raw value from the map
  first_data_row = metadata.get("START DATA ROW")
  first_data_col = metadata.get("START DATA COL")

  # convert the row numeric string to an int
  if first_data_row is None:
    first_data_row = 0
  else:
    try:
      first_data_row = int(float(first_data_row)) - 1
    except:
      first_data_row = 0

  # convert the column spreadsheet value to an int
  if first_data_col is None:
    first_data_col = 0
  else:
    # columns start at A
    try:
      first_data_col = ord(first_data_col) - ord('A')
    except:
      first_data_cal = 0
  return [first_data_row, first_data_col]
