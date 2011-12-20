def validate_header(line, first_data_col, errors):
  """Validate the header, returning False if there is no point in continuing validation"""
  if line[0] != "Locustag":
    errors.append(createFileValidationError("The first data column must be 'Locustag' (not " + line[0] + ")."))
    return False
  header_regex = re.compile("^(.+)-(.*) ([0-9]+)")
  for i in range(first_data_col, len(line)):
    match = header_regex.match(line[i])
    if match is None:
      errors.append(createFileValidationError("The column header + " + str(i) + " must be of the form [STRAIN]-[BIOLOGICAL REPLICATE] [HYBRIDIZATION NUMBER]. " + line[i] + " is not."))
      continue
    strainName = match.group(1)
    if isStrainIdValid(strainName) is False:
      errors.append(createFileValidationError("The column header + " + str(i) + " must be of the form [STRAIN]-[BIOLOGICAL REPLICATE] [HYBRIDIZATION NUMBER]. " + strainName + " is not a recognized strain."))
      continue      
    


def validate_data(time_series_data, first_data_row, first_data_col, errors):
  gene_locus_regex = re.compile("^BSU[0-9]+|^BSU_misc_RNA_[0-9]+|^VMG_[0-9]+_[0-9]+(_c)?")
  dataLines = time_series_data.getRawDataLines()
  for i in range(first_data_row, len(dataLines)):
    line = dataLines[i]
    # The header needs to be CompoundID
    if i is first_data_row:
      if not validate_header(line, first_data_col, errors):
        break
      continue

    # The compound id should be one of these forms
    gene_locus = line[0]
    if not gene_locus_regex.match(gene_locus):
      errors.append(createFileValidationError("Line " + str(i + 1) + ", column 1 must be of the format 'BSU#', 'BSU_misc_RNA_#', 'VMG_#_#', or 'VMG_#_#_c' (instead of " + gene_locus + ")."))
    
def validate_metadata(time_series_data, errors):
  metadata = time_series_data.getMetadataMap()
  validationHelper = ValidationHelper(metadata, errors)
  
  # validate the header format
  validationHelper.validateExplicitHeaderFormat("STRAIN-BIOREP HYBRID")
  
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
    "value unit", ['MM', 'UM', 'PERCENT', 'RATIOT1', 'RATIOCS', 'AU', 'DIMENSIONLESS'], "'mM', 'uM', 'Percent', 'RatioT1', 'RatioCs', 'AU', 'Dimensionless'")
  
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
