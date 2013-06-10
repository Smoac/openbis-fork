from datetime import datetime
from eu.basynthec.cisd.dss import TimeSeriesDataExcel

def set_data_type(data_set):
  data_set.setPropertyValue("DATA_TYPE", "PROTEIN_QUANTIFICATIONS")

def retrieve_experiment(tr, exp_id):
  """Get the specified experiment form the server. Return the experiment."""
  if exp_id is None:
    exp = None
  else:
    exp = tr.getExperiment(exp_id)
  return exp

def strain_canonical(strainId):
  """Return the canonical form of the strainId"""
  if strainId.lower().startswith('jjs-din'):
    return "JJS-DIn" + strainId[7:]
  else:
    return strainId.upper()

def assign_properties(dataset, metadata):
  """Assign properties to the data set from information in the data."""
  propertyNameMap = {
    "STRAIN":"STRAIN_NAMES", 
    "TIMEPOINT TYPE": "TIMEPOINT_TYPE", 
    "CELL LOCATION": "CELL_LOCATION",
    "VALUE UNIT": "VALUE_UNIT", 
    "SCALE": "SCALE"
    }
    
  for prop in metadata.keySet():
    key = propertyNameMap.get(prop)
    if key is not None:
      value = metadata.get(prop)
      if (key == "STRAIN"):
        value = value + " (STRAIN)"
      if (key == "VALUE_UNIT" and value == "fmol/ug"):
        value = "FMOL_UG"
      dataset.setPropertyValue(key, strain_canonical(value))
 
def convert_data_to_tsv(tr, dataset, location):
  """Create a tsv file containing the data and add it to the data set."""
  tr.createNewDirectory(dataset, location)
  tsvFileName = tr.createNewFile(dataset, location, incoming.getName() + ".tsv")
  tsv = open(tsvFileName, 'w')
  for line in timeSeriesData.getRawDataLines():
    for i in range(0, len(line) - 1):
      if (line[i]):
        tsv.write(line[i])
        tsv.write("\t")
    if (line[len(line) - 1]):
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
dataset = tr.createNewDataSet("PROTEIN_QUANTIFICATIONS")
metadata = timeSeriesData.getMetadataMap()
assign_properties(dataset, metadata)

# Store the original and tsv data in data sets                                                                                                                    
original_dataset = tr.createNewDataSet("EXCEL_ORIGINAL")
set_data_type(original_dataset)
store_original_data(tr, original_dataset, "xls")

tsv_dataset = tr.createNewDataSet("TSV_EXPORT")
set_data_type(tsv_dataset)
convert_data_to_tsv(tr, tsv_dataset, "tsv")

# Make the original contain these
contained_codes = [original_dataset.getDataSetCode(), tsv_dataset.getDataSetCode()]
dataset.setContainedDataSetCodes(contained_codes)


# If no experiment has been set, then get the experiment from the excel file
if dataset.getExperiment() is None:
  exp_id = metadata.get("EXPERIMENT")
  exp = retrieve_experiment(tr, exp_id)
  if exp is not None:
    dataset.setExperiment(exp)
    original_dataset.setExperiment(exp)
    tsv_dataset.setExperiment(exp)


