# validate the header -- row 1 contains a strainid, row 2 a value type, row 3, a value unit
def validate_header_line(row, first_data_col, line, errors):
  # validate the strain
  if row is 0:
    for i in range(first_data_col, len(line)):
      strain = line[i]
      if not isStrainIdValid(strain):
        errors.append(createFileValidationError("Strain in col " + str(i + 1) + " " + strainValidationErrorMessageFragment(strain)))
  
  # validate the value type
  elif row is 1:
    for i in range(first_data_col, len(line)):
      isControlledVocabularyPropertyValid(line[i],
        "value type", ['VALUE', 'MEAN', 'MEDIAN', 'STD', 'VAR', 'ERROR', 'IQR'], 
        "'Value', 'Mean', 'Median', 'Std', 'Var', 'Error', 'Iqr'",
        errors)

  # validate the value unit
  else:
    for i in range(first_data_col, len(line)):
      isControlledVocabularyPropertyValid(line[i], 
        "value unit", ['MM', 'UM', 'RATIOT1', 'RATIOCS'], "'mM', 'uM', 'RatioT1', 'RatioCs'",
        errors)
      

def validate_data(time_series_data, first_data_row, first_data_col, errors):
  chebiRegex = re.compile("^CHEBI:[0-9]+")
  bsbmeRegex = re.compile("^BSBME:[0-9]+")
  dataLines = time_series_data.getRawDataLines()
  lineCount = 0
  for line in dataLines:
    # Dispatch to another function to validate the header
    if lineCount < first_data_row:
      validate_header_line(lineCount, first_data_col, line, errors)
      lineCount = lineCount + 1
      continue

    # The header needs to be CompoundID    
    if lineCount is first_data_row:
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
  
    # validate the header format
  validationHelper.validateExplicitHeaderFormat("METABOL HYBRID")
  
  # validate the timepoint type
  validationHelper.validateControlledVocabularyProperty("TIMEPOINT TYPE", 
    "time point type", ['EX', 'IN', 'SI'], "'EX', 'IN', 'SI'")

  # validate the cell location    
  validationHelper.validateControlledVocabularyProperty("CELL LOCATION",
     "cell location", ['CE', 'ES', 'ME', 'CY', 'NC'], "'CE', 'ES', 'ME', 'CY', 'NC'")
     
  # validate the scale
  validationHelper.validateControlledVocabularyProperty("SCALE", "scale",
    ['LIN', 'LOG2', 'LOG10', 'LN'], "'lin', 'log2', 'log10', 'ln'")
    
  # validate the data position specification
  validationHelper.validateStartDataRowCol()


def validate_data_set_file(file):
  errors = []
  time_series_data = create_time_series_excel(file.getAbsolutePath())
  if time_series_data is None:
    errors.append(createFileValidationError(file.getName() + " is not an Excel file."))
    return errors
    
  # validate the metadata
  validate_metadata(time_series_data, errors)
  
  data_start = getInitialDataRowAndCol(time_series_data.getMetadataMap())
      
  # validate the data
  validate_data(time_series_data, data_start[0], data_start[1], errors)
  
  return errors
