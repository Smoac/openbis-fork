from datetime import datetime
from eu.basynthec.cisd.dss import TimeSeriesDataExcel

def set_data_type(data_set):
  data_set.setPropertyValue("DATA_TYPE", "OD600")

def retrieve_experiment(tr, exp_id):
  """Get the specified experiment form the server. Return the experiment."""
  if exp_id is None:
    exp = None
  else:
    exp = tr.getExperiment(exp_id)
  return exp

def extract_strains():
  """Extract the strains from the data sheet"""
  strains = []
  lines = timeSeriesData.getRawDataLines()
  for i in range(1, len(lines)):
    line = lines[i]
    strains.append(line[0].upper())
  return ",".join(strains)

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
  
def convert_data_to_split_tsv(tr, dataset, location):
  """Create one tsv file per strain in the original data."""
  raw_data_lines = timeSeriesData.getRawDataLines()
  
  # Extract the header -- this is shared by all files
  header_line = raw_data_lines[0]
  # In the header we don't need the strain, but we start with a run number
  header = 'RunNumber\t' + '\t'.join(header_line[1:len(header_line)])

  tr.createNewDirectory(dataset, location)
  
  # Keep track of the strains, since a strain can be measured multiple times
  data_per_strain = {}
  
  lines_len = len(raw_data_lines)
  for i in range(1, len(raw_data_lines)):
    line = raw_data_lines[i]
    strain_name = line[0]
    strain_data = data_per_strain.setdefault(strain_name, [])
    # Append the line -- this is run number + the data
    strain_data.append(str(len(strain_data)) + '\t' + '\t'.join(line[1:len(line)]))
    
  # Create the files
  for strain in data_per_strain.iterkeys():
    tsvFileName = tr.createNewFile(dataset, location, incoming.getName() + "_" + strain + ".tsv")
    tsv = open(tsvFileName, 'w')
    tsv.write(header)
    
    strain_data = data_per_strain[strain]
    for line in strain_data:
      tsv.write("\n")
      tsv.write(line)
    tsv.close()
  
def store_original_data(tr, dataset, location):
  """Put the original data into the data set."""
  tr.createNewDirectory(dataset, location)
  tr.moveFile(incoming.getAbsolutePath(), dataset, location + "/" + incoming.getName())


tr = service.transaction(incoming)
timeSeriesData = TimeSeriesDataExcel.createTimeSeriesDataExcel(incoming.getAbsolutePath())

# create the data set and assign the metadata from the file
dataset = tr.createNewDataSet("OD600")
metadata = timeSeriesData.getMetadataMap()
# Strains are not in the metadata, but in the data, so extract them
metadata["STRAIN_NAMES"] = extract_strains()
assign_properties(dataset, metadata)

# Store the original and tsv data in data sets
original_dataset = tr.createNewDataSet("EXCEL_ORIGINAL")
set_data_type(original_dataset)
store_original_data(tr, original_dataset, "xls")

tsv_dataset = tr.createNewDataSet("TSV_MULTISTRAIN_EXPORT")
set_data_type(tsv_dataset)
convert_data_to_tsv(tr, tsv_dataset, "tsv-multi")

tsv_split_dataset = tr.createNewDataSet("TSV_EXPORT")
set_data_type(tsv_split_dataset)
convert_data_to_split_tsv(tr, tsv_split_dataset, "tsv")

# Make the original contain these
contained_codes = [original_dataset.getDataSetCode(), tsv_dataset.getDataSetCode(), tsv_split_dataset.getDataSetCode()]
dataset.setContainedDataSetCodes(contained_codes)


# If no experiment has been set, then get the experiment from the excel file
if dataset.getExperiment() is None:
  exp_id = metadata.get("EXPERIMENT")
  exp = retrieve_experiment(tr, exp_id)
  if exp is not None:
    dataset.setExperiment(exp)
    original_dataset.setExperiment(exp)
    tsv_dataset.setExperiment(exp)
    tsv_split_dataset.setExperiment(exp)
