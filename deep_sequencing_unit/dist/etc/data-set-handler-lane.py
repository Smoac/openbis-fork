'''
Processes each flow lane of a Sequencing run

Expects as incoming folder:
Project_<Flow Cell>_<Lane>
e.g.Project_110715_SN792_0054_BC035RACXX_1 or Project_110816_6354LAAXX_1

Note: 
print statements go to: ~openbis/sprint/datastore_server/log/startup_log.txt
'''

import os
import fnmatch
import time
import shutil
from time import *
from datetime import *
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

FASTQ_GZ_PATTERN = "*.fastq.gz"
METADATA_FILE_SUFFIX = "_metadata.tsv"
AFFILIATION= {'FMI': '/links/shared/dsu-dss/dss/customers/fmi/','BIOCENTER_BASEL': '/links/shared/dsu-dss/dss/customers/biozentrum/'}
AFFILIATION_PROPERTY_NAME='AFFILIATION'

# -------------------------------------------------------------------------------

incomingPath = incoming.getAbsolutePath()
# useful for debugging:
print(datetime.now())

def getFileNames(path=incomingPath):
  '''
  Gets all files matching a PATTERN in a path recursively
  and returns the result as a list
  '''
  matches = []
  for root, dirnames, filenames in os.walk(path):
    for filename in fnmatch.filter(filenames, FASTQ_GZ_PATTERN):
        matches.append(os.path.join(root, filename))
  matches.sort()
  return(matches)

def writeMetadataFile (fileName, parentPropertyTypes, parentPropertiesMap):
  '''
  Writes a file of meta date related to one sample
  '''
  try:
    metaDataFile = open(fileName,'w')
    for propertyType in parentPropertyTypes:
      metaDataFile.write(propertyType.encode('utf-8') + "\t" +
      parentPropertiesMap[propertyType].tryGetAsString().encode('utf-8') + "\n")
  except IOError:
    print ('File error, could not write '+ fileName)
  finally:
    metaDataFile.close()

def create_openbis_timestamp ():
  '''
  Create an openBIS conform timestamp
  '''
  tz=localtime()[3]-gmtime()[3]
  d=datetime.now()
  return d.strftime("%Y-%m-%d %H:%M:%S GMT"+"%+.2d" % tz+":00")

def extraCopy (affiliationName, path):
  '''
  Handles the extra copies of the data for transfer with datamover via the
  bc2 network to the FMI and BIOCENTER
  For the BIOCENTER there is a folder created in which all data gets into
  '''
  if (affiliation_name in AFFILIATION):
    if (affiliation_name == 'BIOCENTER_BASEL'):
      dirname = AFFILIATION[affiliation_name] + datetime.now().strftime("%Y-%m-%d")
      if not os.path.exists(dirname):
        os.mkdir(dirname)
      shutil.copy(path, dirname)
    else:
      shutil.copy(path, AFFILIATION[affiliation_name])
# -------------------------------------------------------------------------------

# Create a "transaction" -- a way of grouping operations together so they all
# happen or none of them do.
transaction = service.transaction()

folders=[]
folders=os.listdir(incomingPath)

# Get the incoming name 
name = incoming.getName()
# expected incoming Name, e.g.: Project_110715_SN792_0054_BC035RACXX_1
split=name.split("_")
if (len(split) == 6):
  incoming_sample=split[1]+ '_'+ split[2] + '_' + split[3] + '_' + split[4]+ ':' + split[-1]
if (len(split) ==4):
  incoming_sample=split[1]+ '_'+ split[2] + ':' + split[-1]

# -------------------------------------------------------------------------------

# Get the search service
search_service = transaction.getSearchService()

# Search for the incoming_sample which is a Flow Lane
sc = SearchCriteria()
print('Processing sample: '+ str(incoming_sample))
sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, incoming_sample));
foundSamples = search_service.searchForSamples(sc)

# there should be only one sample because it is unique within one Flow Cell 
if (len(foundSamples) > 1):
  raise Exception("More than one sample found! No unique code: " + incoming_sample)
elif (len(foundSamples) == 0):
  raise Exception("No matching sample found for: " + incoming_sample)
else :
  sample = foundSamples[0].getSample()
  parents = sample.getParents()

# -------------------------------------------------------------------------------

# search for the parents
sc = SearchCriteria()
# set the Serach Criteria to an OR condition, default is AND
sc.setOperator(SearchCriteria.SearchOperator.MATCH_ANY_CLAUSES)
# Get the codes for all parents
for parent in parents:
  parentSubCode = parent.getSubCode()
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, parentSubCode));
# all parents of the flow lane
foundParents = search_service.searchForSamples(sc)

# -------------------------------------------------------------------------------

# loop over each Sample folder within a lane
for f in range(0,len(folders)):
  # Create a data set and set type
  dataSet = transaction.createNewDataSet("FASTQ_GZ")
  dataSet.setMeasuredData(False)
  dirName = transaction.createNewDirectory(dataSet,folders[f])

  # if multiplexed samples then there is more than one folder
  pathPerLane = incomingPath + '/' + folders[f]
  print ("pathPerLane: " + pathPerLane)
  
  # get all properties of the parent samples
  for foundParent in foundParents:
    parent = foundParent.getSample()
    # ArrayList 
    parentProperties = parent.getProperties()
    # just get the current code
    parentCode = parent.getCode()
    print("Found parent code: "+ parentCode)

    # reformat Java ArrayList and Sort
    parentPropertyTypes = []
    parentPropertiesMap = {}
    for property in parentProperties:
      code = property.getPropertyType().getSimpleCode()
      parentPropertyTypes.append(code)
      parentPropertiesMap[code] = property

    parentPropertyTypes.sort()
    nameOfFile = parentCode + METADATA_FILE_SUFFIX

    if (parentCode == folders[f].split('_')[-1]):
      # get a file from the IDataSetRegistrationTransaction so it is automatically part of the data set
      pathToFile = transaction.createNewFile(dataSet, folders[f], nameOfFile)
      # use this file path to write to this file
      writeMetadataFile(pathToFile, parentPropertyTypes, parentPropertiesMap)
 
      affiliation_name = parentPropertiesMap[AFFILIATION_PROPERTY_NAME].tryGetAsString()
      extraCopy (affiliation_name, pathToFile) 
 
  # get all fastqs in this dataSet
  fastqFileList=getFileNames(pathPerLane)
  
  # put the files into the dataSet 
  affiliation_name = parentPropertiesMap[AFFILIATION_PROPERTY_NAME].tryGetAsString()
  for file in fastqFileList:
    extraCopy (affiliation_name, file)
    # finally add the files to the data set     
    transaction.moveFile(file , dataSet, folders[f])
 
  if foundSamples.size() > 0:
    sa = transaction.getSampleForUpdate(foundSamples[0].getSampleIdentifier())
    sa.setPropertyValue("DATA_TRANSFERRED", create_openbis_timestamp())
    dataSet.setSample(foundSamples[0])

shutil.rmtree(incomingPath)
