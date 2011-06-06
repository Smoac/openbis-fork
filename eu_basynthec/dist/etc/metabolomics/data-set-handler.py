from datetime import datetime
from eu.basynthec.cisd.dss import TimeSeriesDataExcel

def retrieve_or_create_experiment(tr):
	# Create an experiment
	now_str = datetime.today().strftime('%Y%m%d-%H%M%S')
	expid = "/SHARED/SHARED/" + now_str
	exp = tr.getExperiment(expid)

	if None == exp:
		exp = tr.createNewExperiment(expid, 'BASYNTHEC')
		exp.setPropertyValue("DESCRIPTION", "An experiment created on " + now_str)
		
	return exp

propertyNameMap = {
	"STRAIN":"STRAIN", 
	"TIMEPOINT TYPE": "TIMEPOINT_TYPE", 
	"CELL LOCATION": "CELL_LOCATION", 
	"VALUE TYPE": "VALUE_TYPE", 
	"VALUE UNIT": "VALUE_UNIT", 
	"SCALE": "SCALE"
	}

tr = service.transaction(incoming)
exp = retrieve_or_create_experiment(tr)

timeSeriesData = TimeSeriesDataExcel.createTimeSeriesDataExcel(incoming.getAbsolutePath())

# create the data set and assign the metadata from the file
dataSet = tr.createNewDataSet("METABOLITE_INTENSITIES")
metadata = timeSeriesData.getMetadataMap()

for prop in metadata.keySet():
	key = propertyNameMap.get(prop)
	if key is not None:
		value = metadata.get(prop)
		if (key == "STRAIN"):
			value = value + " (STRAIN)"
		dataSet.setPropertyValue(key, value.upper())

dataSet.setExperiment(exp)
tr.moveFile(incoming.getAbsolutePath(), dataSet)

