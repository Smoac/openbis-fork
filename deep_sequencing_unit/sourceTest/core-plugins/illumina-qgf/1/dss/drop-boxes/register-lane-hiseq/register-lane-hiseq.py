'''
Processes each flow lane of a Sequencing run

Expects as incoming folder:
Project_<Flow Cell>_<Lane>
e.g.Project_110715_SN792_0054_BC035RACXX_1 or Project_110816_6354LAAXX_1

Note: 
print statements go to: ~openbis/sprint/datastore_server/log/startup_log.txt
'''

import os
import re
import fnmatch
import time
import shutil
import tempfile
import subprocess
from time import *
from datetime import *
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

FASTQ_GZ_PATTERN = "*.fastq.gz"
METADATA_FILE_SUFFIX = "_metadata.tsv"
# AFFILIATION= {'FMI': '/links/shared/dsu/dss/customers/fmi/drop-box/',
#               #'BIOCENTER_BASEL': '/links/shared/dsu/dss/customers/biozentrum/drop-box/',
#               'BIOCENTER_BASEL': '/Users/kohleman/tmp/biozentrum',
#               'SWISS_TPH' : '/Users/kohleman/tmp/biozentrum',
#               #'SWISS_TPH': '/links/shared/dsu/dss/customers/biozentrum/drop-box/',
#               'NEUROSTEMX': '/links/shared/dsu/dss/customers/biozentrum',}
AFFILIATION_PROPERTY_NAME='AFFILIATION'
INDEX1='BARCODE'
INDEX2='INDEX2'
EXTERNAL_SAMPLE_NAME='EXTERNAL_SAMPLE_NAME'
SAMPLE_TYPE = 'SAMPLE_TYPE'
SAMPLE_CODE = 'SAMPLE_CODE'
CRC32_PATH='lib/crc32'


DEFAULT_INDEX='NoIndex'

# -------------------------------------------------------------------------------

def getThreadProperties(transaction):
  threadPropertyDict = {}
  threadProperties = transaction.getGlobalState().getThreadParameters().getThreadProperties()
  for key in threadProperties:
    try:
      threadPropertyDict[key] = threadProperties.getProperty(key)
    except:
      pass
  return threadPropertyDict

# -------------------------------------------------------------------------------

def CRC32_from_file(filename, transaction):
    threadPropertyDict = getThreadProperties(transaction)
    absolutePath = os.path.dirname(os.path.realpath(threadPropertyDict['script-path']))
    fullPathCrc32 = (os.path.join(absolutePath, CRC32_PATH))
    if os.path.exists(fullPathCrc32):
        args = [fullPathCrc32, filename]
        p = subprocess.Popen(args, stdout=subprocess.PIPE)
        cksum = (p.communicate()[0])
        print("Calculated crc32 checksum for: "+ os.path.basename(filename) + " " + cksum)
    else:
        cksum = 0 & 0xFFFFFFFF
    return cksum


def getFileNames(path):
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

# -------------------------------------------------------------------------------

def sanitizeString(myString):
  return re.sub('[^A-Za-z0-9]+', '_', myString)

# -------------------------------------------------------------------------------

def get_vocabulary_descriptions (transaction, vocabulary_name):
  vocabulary_descriptions_dict = {}
  vocabulary = transaction.getVocabulary(vocabulary_name)
  vocabulary_terms = vocabulary.getTerms()
  for term in vocabulary_terms:
    vocabulary_descriptions_dict[term.getCode()] = term.getDescription()
  return vocabulary_descriptions_dict

# -------------------------------------------------------------------------------

def writeMetadataFile (transaction, fileName, parentPropertyTypes, parentPropertiesMap, fcMetaDataDict,
                        experiment, fastqFileList, flowLane):
  '''
  Writes a file of meta date related to one sample
  '''
  
  fastqFileList.sort()
  
  expId = experiment.getIdentifier()
  try:
    meta_data_file = open(fileName,'w')
    for propertyType in parentPropertyTypes:
      if (propertyType in [u'FLOW_CELL_PROPERTIES']):
        continue
      if propertyType in [SAMPLE_TYPE] or propertyType in [SAMPLE_CODE]:
         meta_data_file.write(propertyType.encode('utf-8') + "\t" +
                         str(parentPropertiesMap[propertyType])+ "\n")
      else:
        meta_data_file.write(propertyType.encode('utf-8') + "\t" +
                         parentPropertiesMap[propertyType].tryGetAsString().encode('utf-8').replace('\n',',') + "\n")
    
    meta_data_file.write("EXPERIMENT\t" + expId + "\n".encode('utf-8'))
    meta_data_file.write("\nFLOWCELL PROPERTIES\n".encode('utf-8'))
    fcMetaDataDict["LANE_NUMBER"] = flowLane
    keys = fcMetaDataDict.keys()
    keys.sort()
    
    sequencer_vocabulary_description = get_vocabulary_descriptions(transaction, 'SEQUENCER')
    meta_data_file.write('SEQUENCER_MODEL' + "\t" + sequencer_vocabulary_description[fcMetaDataDict['SEQUENCER']].encode('utf-8') + "\n")
    
    for k in keys:
      meta_data_file.write(k.encode('utf-8') + "\t" + fcMetaDataDict[k].encode('utf-8') + "\n")
  
    meta_data_file.write("\nFASTQ_FILES\n".encode('utf-8'))
    for file in fastqFileList:
        meta_data_file.write(os.path.basename(file) + "\t" + str(CRC32_from_file(file, transaction)) + "\n")
   
  except IOError:
    print ('File error, could not write '+ fileName)
  finally:
    meta_data_file.close()

def create_openbis_timestamp ():
  '''
  Create an openBIS conform timestamp
  '''
  tz=localtime()[3]-gmtime()[3]
  d=datetime.now()
  return d.strftime("%Y-%m-%d %H:%M:%S GMT"+"%+.2d" % tz+":00")

def extraCopy (affiliation_name, path):
  '''
  Handles the extra copies of the data for transfer with datamover via the
  bc2 network to the FMI and BIOCENTER
  For the BIOCENTER there is a folder created in which all data gets into
  '''
  if (affiliation_name in AFFILIATION):
    if (affiliation_name == 'BIOCENTER_BASEL' or affiliation_name == 'NEUROSTEMX'  or affiliation_name == 'SWISS_TPH'):
      dirname = AFFILIATION[affiliation_name] + datetime.now().strftime("%Y-%m-%d")
      if not os.path.exists(dirname):
        os.mkdir(dirname)
      shutil.copy(path, dirname)
    else:
      shutil.copy(path, AFFILIATION[affiliation_name])

# -------------------------------------------------------------------------------

def extraCopySciCore (affiliation_name, filePath, destinationFolder=""):
    '''
    Handles the extra copies of the data for transfer with datamover for SCICORE
    '''
    dirname = '' 
    
    dropBoxFolder = '/links/shared/dsu/dss/customers/biozentrum_scicore/drop-box/'
    #dropBoxFolder = '/Users/kohleman/tmp/scicore'
    basename = os.path.basename(filePath)

    splits = basename.split('_')
    folderName = "_".join(splits[0:8])

    if (affiliation_name == 'BIOCENTER_BASEL' or affiliation_name == 'NEUROSTEMX' or affiliation_name == 'SWISS_TPH'):
        dirname = os.path.join(dropBoxFolder, folderName)
        if destinationFolder:
          shutil.copy(filePath, destinationFolder)
        else:
          if not os.path.exists(dirname):
            os.mkdir(dirname)
          shutil.copy(filePath, dirname)
          print("Copied " + basename + " to " + dropBoxFolder + "\n")
    return dirname

# -------------------------------------------------------------------------------

def searchSample (sample_code, search_service):
  sc = SearchCriteria()
  print('Searching sample: '+ str(sample_code))
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sample_code));
  foundSamples = search_service.searchForSamples(sc)
  return foundSamples

# -------------------------------------------------------------------------------

def get_sample_properties (transaction, sample): 
  
  sample_properties_dict = {}
  # returns Map<String, String>
  sample_properties = sample.getSample().getProperties()
  sequencing_sample_type = sample.getSampleType()
  sequencing_sample_code = sample.getCode()
  sample_properties_dict[SAMPLE_TYPE] = sequencing_sample_type
  sample_properties_dict[SAMPLE_CODE] = sequencing_sample_code

  for property in sample_properties:
      code = property.getPropertyType().getSimpleCode()
      sample_properties_dict[code] = property.tryGetAsString()
  
  return sample_properties_dict

# -------------------------------------------------------------------------------

def getFlowCellMetaData (transaction, flowCellId):
  
  def sortedDictValues(adict):
    keys = adict.keys()
    keys.sort()
    return map(adict.get, keys)
  
  search = transaction.getSearchService()
  sc = SearchCriteria()
  print('Searching FlowCell: '+ str(flowCellId))
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, flowCellId));
  foundFlowCells = search.searchForSamples(sc)

  try:
    assert foundFlowCells.size() == 1
  except AssertionError:
    print (str(foundFlowCells.size()) + ' flow cells found which match the criterias: '+ flowCellId)
  
  fcPropertiesDict = {}
  fcPropertyTypes = []

  fcProperties = foundFlowCells[0].getSample().getProperties()
  for property in fcProperties:
      code = property.getPropertyType().getSimpleCode()
      fcPropertyTypes.append(code)
      fcPropertiesDict[code] = property
  
  fcPropertyTypes.sort()
  return fcPropertiesDict, fcPropertyTypes

# -------------------------------------------------------------------------------

def searchParents (search_service, parents):
  '''
  Searches for all parents and returns a java.util.List<ISampleImmutable>
  '''
  # search for the parents
  sc = SearchCriteria()
  # set the Search Criteria to an OR condition, default is AND
  sc.setOperator(SearchCriteria.SearchOperator.MATCH_ANY_CLAUSES)
  # Get the codes for all parents
  for parent in parents:
    parentSubCode = parent.getSubCode()
    sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, parentSubCode));
  # all parents of the flow lane
  foundParents = search_service.searchForSamples(sc)

  return foundParents

# -------------------------------------------------------------------------------

def getProperties (foundParent, fcMetaDataDict):
  '''

  '''
  parent = foundParent.getSample()
  # ArrayList
  parentProperties = parent.getProperties()
  # just get the current code
  parentCode = parent.getCode()
  #print("Found parent code: "+ parentCode)
  parentSampleType = parent.getSampleType()

  # reformat Java ArrayList and Sort
  parentPropertyTypes = []
  parentPropertiesMap = {}

  parentPropertyTypes.append(SAMPLE_TYPE)
  parentPropertyTypes.append(SAMPLE_CODE)
  parentPropertiesMap[SAMPLE_TYPE] = parentSampleType
  parentPropertiesMap[SAMPLE_CODE] = parentCode
  experiment = parent.getExperiment()

  for property in parentProperties:
    code = property.getPropertyType().getSimpleCode()
    parentPropertyTypes.append(code)
    parentPropertiesMap[code] = property
    try:
      barcode = parentPropertiesMap[INDEX1].tryGetAsString()
      if barcode == "NOINDEX":
	barcode = DEFAULT_INDEX
      else:
	barcode.split()[-1][:-1]
    except:
      barcode = DEFAULT_INDEX

    i2Length = int(fcMetaDataDict['INDEXREAD2'].tryGetAsString().encode('utf-8'))
    if i2Length > 0:
      try:
	index2 = parentPropertiesMap[INDEX2].tryGetAsString()
	if index2 == "NOINDEX":
	  index2 = DEFAULT_INDEX
	else:
          index2.split()[-1][:-1]
      except:
        index2 = DEFAULT_INDEX
    else:
      index2 = DEFAULT_INDEX

    completeBarcode=barcode + "-" + index2
    parentPropertyTypes.sort()
    #print("parentPropertiesMap")
    #print(parentPropertiesMap)
  return parentCode, parentProperties, parentPropertyTypes, parentPropertiesMap, experiment, barcode, index2, completeBarcode

# -------------------------------------------------------------------------------

def createMetDataFileName (parentCode, incoming_sample, completeBarcode, flowLane, METADATA_FILE_SUFFIX):
  return parentCode.replace('-','_') + "_" + incoming_sample.replace(':', '_') + "_" + completeBarcode + "_L00" + flowLane +METADATA_FILE_SUFFIX

# -------------------------------------------------------------------------------

def process(transaction):

  EXPERIMENT_CODE = 'EXPERIMENT'

  # useful for debugging:
  print("\n" + str(datetime.now()))

  incomingPath = transaction.getIncoming().getAbsolutePath()
  name = transaction.getIncoming().getName()

  folders=[]
  folders=os.listdir(incomingPath)

  # expected incoming Name, e.g.: Project_110715_SN792_0054_BC035RACXX_1
  split=name.split("_")
  if (len(split) == 6):
    runningDate = split[1]
    sequencerId = split[2]
    sequentialNumber = split[3]
    hiseqTray = split[4][0]
    flowCellId = split[4][1:]
    flowLane = split[-1]
    incoming_sample=runningDate+ '_'+ sequencerId + '_' + sequentialNumber + '_' + hiseqTray + flowCellId + ':' + flowLane

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

  foundParents = searchParents(search_service, parents)

  # -------------------------------------------------------------------------------

  flowcell_sample_immutable = searchSample (runningDate+ '_'+ sequencerId + '_' + sequentialNumber + '_' + hiseqTray + flowCellId , search_service)
  fcMetaDataDict_NEW = get_sample_properties(transaction, flowcell_sample_immutable[0])
  fcMetaDataDict, fcMetaDataList = getFlowCellMetaData(transaction, incoming_sample.split(":")[0])
  
  def addExternalSampleName (fastqFileList, usableExternalSampleName):
    externalNamesFastqFileDict = {}
    
    for file in fastqFileList:
        folder = os.path.dirname(file)
        fileName = os.path.basename(file)
        filepart, suffix = fileName.split('.',1)
        sanitizedExternalSampleName = sanitizeString(usableExternalSampleName)
        if sanitizedExternalSampleName:
            newFile =  folder + "/" + filepart + "_" + sanitizedExternalSampleName + "." + suffix
        else:
            newFile = file
        externalNamesFastqFileDict[file] = newFile
    return externalNamesFastqFileDict
  
  # loop over each Sample folder within a lane
  for f in range(0,len(folders)):
    # Create a data set and set type
    dataSet = transaction.createNewDataSet("FASTQ_GZ")
    dataSet.setMeasuredData(False)
    dataSet.setPropertyValue(INDEX1, DEFAULT_INDEX)
    dataSet.setPropertyValue(INDEX2, DEFAULT_INDEX)
    dirName = transaction.createNewDirectory(dataSet,folders[f])
    usableExternalSampleName = "EXTERNAL_NAME"

    # if multiplexed samples then there is more than one folder
    pathPerLane = incomingPath + '/' + folders[f]
    
    # get all fastqs in this dataSet
    fastqFileList=getFileNames(pathPerLane)

    # get all properties of the parent samples
    for foundParent in foundParents:

      parentCode, parentProperties, parentPropertyTypes, parentPropertiesMap, experiment, barcode, index2, completeBarcode = getProperties (foundParent, fcMetaDataDict)
      nameOfFile = createMetDataFileName (parentCode, incoming_sample, completeBarcode, flowLane, METADATA_FILE_SUFFIX)
      #print folders[f]
      folderSplit = '-'.join(folders[f].split('_')[1:4])
      #print "Folder split: " + folderSplit
      #print str(parentCode)
      organism = parentPropertiesMap['NCBI_ORGANISM_TAXONOMY'].tryGetAsString().split(":")[-1].strip() 

      if (parentCode == folderSplit):

        externalSampleName = parentPropertiesMap[EXTERNAL_SAMPLE_NAME].tryGetAsString()
        lengthLimit = 30
        if len(externalSampleName) > lengthLimit:
          usableExternalSampleName = externalSampleName[0:lengthLimit]
        else:
          usableExternalSampleName = externalSampleName

        nameOfFile = parentCode.replace('-','_') + "_" + incoming_sample.replace(':', '_') + "_" + completeBarcode + "_L00" + \
                                                     flowLane + "_" + sanitizeString(usableExternalSampleName) + METADATA_FILE_SUFFIX

        dataSet.setPropertyValue(INDEX1, barcode)
        dataSet.setPropertyValue(INDEX2, index2)
        dataSet.setPropertyValue(EXTERNAL_SAMPLE_NAME, parentPropertiesMap[EXTERNAL_SAMPLE_NAME].tryGetAsString())
        print("Creating metadata file:" + nameOfFile)
        # get a file from the IDataSetRegistrationTransaction so it is automatically part of the data set
        pathToFile = transaction.createNewFile(dataSet, folders[f], nameOfFile)
        externalNamesFastqFileDict = addExternalSampleName(fastqFileList, usableExternalSampleName)
        
        try:
          affiliation_name = parentPropertiesMap[AFFILIATION_PROPERTY_NAME].tryGetAsString()
        except:
          affiliation_name = 'NEUROSTEMX'
        
        for extFileNameFastQ in externalNamesFastqFileDict:
            print ("RENAME file " + str(os.path.basename(extFileNameFastQ)) + " TO " + str(os.path.basename(externalNamesFastqFileDict[extFileNameFastQ])))
            os.rename(extFileNameFastQ, externalNamesFastqFileDict[extFileNameFastQ])
            destinationFolder = ''
            extraCopySciCore (affiliation_name, externalNamesFastqFileDict[extFileNameFastQ], destinationFolder)
            
        writeMetadataFile(transaction, pathToFile, parentPropertyTypes, parentPropertiesMap, fcMetaDataDict_NEW, experiment, externalNamesFastqFileDict.values(), flowLane)
        extraCopySciCore (affiliation_name, pathToFile)
            
        for extFileNameFastQ in externalNamesFastqFileDict:
            transaction.moveFile(externalNamesFastqFileDict[extFileNameFastQ] , dataSet, folders[f])

      # Undetermined files #################################
      elif (folderSplit.startswith('lane') and barcode == "NoIndex" and index2 == "NoIndex" and organism !='10847'):
        usableExternalSampleName = ""
        print("********************** Creating metadata file for Undetermined:" + nameOfFile)
        
        # get a file from the IDataSetRegistrationTransaction so it is automatically part of the data set
        pathToFile = transaction.createNewFile(dataSet, folders[f], nameOfFile)
        externalNamesFastqFileDict = addExternalSampleName(fastqFileList, usableExternalSampleName)
        writeMetadataFile(transaction, pathToFile, parentPropertyTypes, parentPropertiesMap, fcMetaDataDict_NEW, experiment, externalNamesFastqFileDict.values(), flowLane)
        try:
          affiliation_name = parentPropertiesMap[AFFILIATION_PROPERTY_NAME].tryGetAsString()
        except:
          affiliation_name = 'NEUROSTEMX'
        
        dirName = extraCopySciCore (affiliation_name, pathToFile)

        externalNamesFastqFileDict = addExternalSampleName(fastqFileList, usableExternalSampleName)
        for extFileNameFastQ in externalNamesFastqFileDict:
            extraCopySciCore (affiliation_name, externalNamesFastqFileDict[extFileNameFastQ], dirName)

        # Write Meta data for Parents Files
        for foundParent in foundParents:
          addParentCode, parentProperties, parentPropertyTypes, parentPropertiesMap, experiment, barcode, index2, completeBarcode = getProperties (foundParent, fcMetaDataDict)
          nameOfFile = createMetDataFileName (addParentCode, incoming_sample, completeBarcode, flowLane, METADATA_FILE_SUFFIX)
          if (parentCode != addParentCode):
            tmpDir = tempfile.mkdtemp() 
            fileName = os.path.join(tmpDir, "PARENT_" + nameOfFile)
            writeMetadataFile(transaction, fileName, parentPropertyTypes, parentPropertiesMap, fcMetaDataDict_NEW, experiment, [], flowLane)
            try:
              affiliation_name = parentPropertiesMap[AFFILIATION_PROPERTY_NAME].tryGetAsString()
            except:
              affiliation_name = 'NEUROSTEMX'
#             extraCopy (affiliation_name, fileName)
            extraCopySciCore (affiliation_name, fileName, dirName)

        for extFileNameFastQ in externalNamesFastqFileDict:
            transaction.moveFile(externalNamesFastqFileDict[extFileNameFastQ] , dataSet, folders[f])
            
   
    if foundSamples.size() > 0:
      sa = transaction.getSampleForUpdate(foundSamples[0].getSampleIdentifier())
      sa.setPropertyValue("DATA_TRANSFERRED", create_openbis_timestamp())
      dataSet.setSample(foundSamples[0])

  shutil.rmtree(incomingPath)
