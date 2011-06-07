from datetime import datetime
from eu.basynthec.cisd.dss import TimeSeriesDataExcel

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
		"STRAIN":"STRAIN", 
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
			
def convert_data_to_tsv(tr, dataset, location):
	"""Create a tsv file containing the data and add it to the data set."""
	tr.createNewDirectory(dataset, location)
	tsvFileName = tr.createNewFile(dataset, location, incoming.getName() + ".tsv")
	tsv = open(tsvFileName, 'w')
	for line in timeSeriesData.getRawDataLines():
		for i in range(0, len(line) - 1):
			tsv.write(line[i])
			tsv.write("\t")
		tsv.write(line[len(line) - 1])
		tsv.write("\n")
	tsv.close()
	
def store_original_data(tr, dataset, location):
	"""Put the original data into the data set."""
	tr.createNewDirectory(dataset, location)
	tr.moveFile(incoming.getAbsolutePath(), dataset, location + "/" + incoming.getName())


tr = service.transaction(incoming)
timeSeriesData = TimeSeriesDataExcel.createTimeSeriesDataExcel(incoming.getAbsolutePath())

# create the data set and assign the metadata from the file
dataset = tr.createNewDataSet("METABOLITE_INTENSITIES")
metadata = timeSeriesData.getMetadataMap()
assign_properties(dataset, metadata)
		
# Convert the data into a tsv file, and put that and the original data into the data set
convert_data_to_tsv(tr, dataset, "data/tsv")
store_original_data(tr, dataset, "data/xls")

# If no experiment has been set, then get the experiment from the excel file
if dataset.getExperiment() is None:
	exp_id = metadata.get("EXPERIMENT")
	exp = retrieve_experiment(tr, exp_id)
	if exp is not None:
		dataset.setExperiment(exp)


