def validate_data(timeSeriesData, errors):
  dataLines = timeSeriesData.getRawDataLines()
  lineCount = 0
  for line in dataLines:
    # The header needs to be Abs
    if lineCount is 0:
      if line[0] != "Strain":
        errors.append(createFileValidationError("The first data column must be 'Strain'"))
        break
      lineCount = lineCount + 1
      continue

    # The compound id should be one of these forms
    strain = line[0]
    if not isStrainIdValid(strain):
      errors.append(createFileValidationError("Line " + str(lineCount + 1) + ", column 1 must be MGP[0-999] (instead of " + strain + ")."))
    lineCount = lineCount + 1

def validate_metadata(time_series_data, errors):
  metadata = time_series_data.getMetadataMap()
  validationHelper = ValidationHelper(metadata, errors)
  
  # validate the header format
  validationHelper.validateDefaultHeaderFormat()
  
  # validate the timepoint type
  validationHelper.validateControlledVocabularyProperty("TIMEPOINT TYPE", 
    "time point type", ['EX', 'IN', 'SI'], "'EX', 'IN', 'SI'")

  # validate the cell location    
  validationHelper.validateControlledVocabularyProperty("CELL LOCATION",
     "cell location", ['CE', 'ES', 'ME', 'CY', 'NC'], "'CE', 'ES', 'ME', 'CY', 'NC'")

  # validate the value type   
  validationHelper.validateControlledVocabularyProperty("VALUE TYPE",
     "value type", ['VALUE', 'MEAN', 'MEDIAN', 'STD', 'VAR', 'ERROR', 'IQR'], 
    "'Value', 'Mean', 'Median', 'Std', 'Var', 'Error', 'Iqr'")

  # validate the value unit
  validationHelper.validateControlledVocabularyProperty("VALUE UNIT", 
    "value unit", ['DIMENSIONLESS'], "'DIMENSIONLESS'")
  
  # validate the scale
  validationHelper.validateControlledVocabularyProperty("SCALE", "scale",
    ['LIN', 'LOG2', 'LOG10', 'LN'], "'lin', 'log2', 'log10', 'ln'")

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
