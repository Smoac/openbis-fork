from datetime import datetime
from eu.basynthec.cisd.dss import TimeSeriesDataExcel
import re

def getInitialDataRowAndCol(metadata):
	"""Extract the initial row and column as specified in the metadata. Returns an array with [row, col]."""
	# get the raw value from the map
	first_data_row = metadata.get("START DATA ROW")
	first_data_col = metadata.get("START DATA COL")

	# convert the row numeric string to an int
	if first_data_row is None:
		first_data_row = 0
	else:
		first_data_row = int(float(first_data_row)) - 1

	# convert the column spreadsheet value to an int
	if first_data_col is None:
		first_data_col = 0
	else:
		# columns start at A
		first_data_col = ord(first_data_col) - ord('A')
	return [first_data_row, first_data_col]


def retrieve_experiment(tr, exp_id):
	"""Get the specified experiment form the server. Return the experiment."""
	if exp_id is None:
		exp = None
	else:
		exp = tr.getExperiment(exp_id)
	return exp

def assign_properties(dataset, metadata):
	"""Assign properties to the data set from information in the data."""
	propertyNameMap = {
		"STRAIN_NAMES": "STRAIN_NAMES",
		"TIMEPOINT TYPE": "TIMEPOINT_TYPE", 
		"CELL LOCATION": "CELL_LOCATION", 
		"VALUE TYPE": "VALUE_TYPE", 
		"VALUE UNIT": "VALUE_UNIT", 
		"SCALE": "SCALE"
		}
		
	for prop in metadata.keySet():
		key = propertyNameMap.get(prop)
		if key is not None:
			value = metadata.get(prop)
			if (key == "STRAIN"):
				value = value + " (STRAIN)"
			dataset.setPropertyValue(key, value.upper())
			
def convert_data_to_tsv(tr, start_row, start_col, dataset, location):
	"""Create a tsv file containing the data and add it to the data set."""
	tr.createNewDirectory(dataset, location)
	tsvFileName = tr.createNewFile(dataset, location, incoming.getName() + ".tsv")
	tsv = open(tsvFileName, 'w')
	raw_data = timeSeriesData.getRawDataLines()
	for i in range(start_row, len(raw_data)):
		line = raw_data[i]
		# write the metabolite id
		tsv.write(line[0])
		tsv.write("\t")
		for j in range(start_col, len(line) - 1):
			tsv.write(line[j])
			tsv.write("\t")
		tsv.write(line[len(line) - 1])
		tsv.write("\n")
	tsv.close()
	
def store_original_data(tr, dataset, location):
	"""Put the original data into the data set."""
	tr.createNewDirectory(dataset, location)
	tr.moveFile(incoming.getAbsolutePath(), dataset, location + "/" + incoming.getName())

def extract_strains(start_row, start_col):
	"""Extract the strain names from the header."""
	strains = []
	line = timeSeriesData.getRawDataLines()[start_row]
	header_regex = re.compile("^(MGP[0-9]{1,3})-([0-9]) ([0-9]+)")
	for i in range(start_col, len(line)):
		match = header_regex.match(line[i])
		strains.append(match.group(1))
	return ",".join(strains)

		

tr = service.transaction(incoming)
timeSeriesData = TimeSeriesDataExcel.createTimeSeriesDataExcel(incoming.getAbsolutePath())
dataStart = getInitialDataRowAndCol(timeSeriesData.getMetadataMap())

# create the data set and assign the metadata from the file
dataset = tr.createNewDataSet("TRANSCRIPTOMICS")
metadata = timeSeriesData.getMetadataMap()
metadata["STRAIN_NAMES"] = extract_strains(dataStart[0], dataStart[1])
assign_properties(dataset, metadata)

# Store the original and tsv data in data sets
original_dataset = tr.createNewDataSet("EXCEL_ORIGINAL")
store_original_data(tr, original_dataset, "xls")

tsv_dataset = tr.createNewDataSet("TSV_MULTISTRAIN_EXPORT")
convert_data_to_tsv(tr, dataStart[0], dataStart[1], tsv_dataset, "tsv-multi")

# Make the original contain these
contained_codes = [original_dataset.getDataSetCode(), tsv_dataset.getDataSetCode()]
dataset.setContainedDataSetCodes(contained_codes)


# If no experiment has been set, then get the experiment from the excel file
if dataset.getExperiment() is None:
	exp_id = metadata.get("EXPERIMENT")
	exp = retrieve_experiment(tr, exp_id)
	if exp is not None:
		dataset.setExperiment(exp)


