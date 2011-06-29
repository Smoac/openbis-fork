def validate_data(time_series_data, errors):
	gene_locus_regex = re.compile("^BSU[0-9]+|^BSU_misc_RNA_[0-9]+|^VMG_[0-9]+_[0-9]+(_c)?")
	column_header_regex = re.compile("(\+|-)?[0-9]+::(value|mean|median|std|var|error|iqr)")
	dataLines = time_series_data.getRawDataLines()
	lineCount = 0
	for line in dataLines:
		# The header needs to be GeneLocus
		if lineCount is 0:
			if line[0] != "GeneLocus":
				errors.append(createFileValidationError("The first data column must be 'GeneLocus'"))
				break
			lineCount = lineCount + 1
			has_human_readable = line[1] == "HumanReadable"
			
			if has_human_readable:
				range_start = 2
			else:
				range_start = 1
			for i in range(range_start, len(line)):
				if not column_header_regex.match(line[i].lower()):
					errors.append(createFileValidationError("Column " + str(i) + " header must be of the format Timepoint::(value|mean|median|std|var|error|iqr), (instead of " + line[i] + ")."))
			continue

		# The compound id should be one of these forms
		gene_locus = line[0]
		if not gene_locus_regex.match(gene_locus):
			errors.append(createFileValidationError("Line " + str(lineCount + 1) + ", column 1 must be of the format 'BSU#', 'BSU_misc_RNA_#', 'VMG_#_#', or 'VMG_#_#_c' (instead of " + gene_locus + ")."))
		lineCount = lineCount + 1
		
def validate_metadata(time_series_data, errors):
	metadata = time_series_data.getMetadataMap()
	validationHelper = ValidationHelper(metadata, errors)
	
	# validate the strain
	validationHelper.validateStrain()
	
	# validate the header format
	validationHelper.validateExplicitHeaderFormat("TIME::VALUE_TYPE")
	
	# validate the timepoint type
	validationHelper.validateControlledVocabularyProperty("TIMEPOINT TYPE", 
		"time point type", ['EX', 'IN', 'SI'], "'EX', 'IN', 'SI'")

	# validate the cell location		
	validationHelper.validateControlledVocabularyProperty("CELL LOCATION",
		 "cell location", ['CE', 'ES', 'ME', 'CY', 'NC'], "'CE', 'ES', 'ME', 'CY', 'NC'")

	# validate the value unit
	validationHelper.validateControlledVocabularyProperty("VALUE UNIT", 
		"value unit", ['MM', 'UM', 'PERCENT', 'RATIOT1', 'RATIOCS', 'AU', 'DIMENSIONLESS'], "'mM', 'uM', 'Percent', 'RatioT1', 'RatioCs', 'AU', 'Dimensionless'")
	
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
