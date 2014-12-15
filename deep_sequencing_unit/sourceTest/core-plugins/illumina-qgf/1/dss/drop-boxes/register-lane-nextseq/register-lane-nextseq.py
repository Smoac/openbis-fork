'''
Processes each flow lane of a Sequencing run

Expects as incoming folder:
BSSE_QGF_22266_H0W8YBGXX_1
or
Undetermined_H0W8YBGXX

Note: 
print statements go to: ~openbis/sprint/datastore_server/log/startup_log.txt
'''

import os
import fnmatch
import time
import shutil
import re
import subprocess
from time import *
from datetime import *
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

FASTQ_GZ_PATTERN = "*.fastq.gz"
METADATA_FILE_SUFFIX = "_metadata.tsv"
AFFILIATION= {'FMI': '/links/shared/dsu/dss/customers/fmi/drop-box/',
              'BIOCENTER_BASEL': '/links/shared/dsu/dss/customers/biozentrum/drop-box/',
              'NEUROSTEMX': '/links/shared/dsu/dss/customers/biozentrum/drop-box/',
              'SWISS_TPH' : '/links/shared/dsu/dss/customers/biozentrum/drop-box/'}
AFFILIATION_PROPERTY_NAME='AFFILIATION'
INDEX1='BARCODE'
INDEX2='INDEX2'
EXTERNAL_SAMPLE_NAME='EXTERNAL_SAMPLE_NAME'
INDEXREAD1='INDEXREAD'
INDEXREAD2='INDEXREAD2'
SAMPLE_TYPE = 'SAMPLE_TYPE'
SAMPLE_CODE = 'SAMPLE_CODE'
NCBI_ORGANISM_TAXONOMY='NCBI_ORGANISM_TAXONOMY'
PHIX_TAXONOMY_ID='10847'
DEFAULT_INDEX='NoIndex'
CRC32_PATH='lib/crc32'

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

def writeMetadataFile(transaction, folder_name, meta_data_file_name, sequencing_sample_properties_dict,
                      fcMetaDataDict, experiment, affiliation_name, fastqFileList, flowLane):
  '''
  Writes a file of meta data related to one sample
  '''

  sequencing_sample_properties_list = sequencing_sample_properties_dict.keys()
  sequencing_sample_properties_list.sort()

  expId = experiment.getIdentifier()
  try:
    meta_data_file = open(meta_data_file_name,'w')
    for propertyType in sequencing_sample_properties_list:
      if (propertyType in [u'FLOW_CELL_PROPERTIES']):
        continue
      if propertyType in [SAMPLE_TYPE] or propertyType in [SAMPLE_CODE]:
         meta_data_file.write(propertyType.encode('utf-8') + "\t" +
                         str(sequencing_sample_properties_dict[propertyType])+ "\n")
      else:
        meta_data_file.write(propertyType.encode('utf-8') + "\t" +
                         sequencing_sample_properties_dict[propertyType].encode('utf-8').replace('\n',',') + "\n")

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
    print ('File error, could not write '+ file)
  finally:
    meta_data_file.close()
  
  destinationFolder = folder_name
  #extraCopy (affiliation_name, meta_data_file_name)
  extraCopySciCore (affiliation_name, meta_data_file_name, destinationFolder)

def create_openbis_timestamp ():
  '''
  Create an openBIS conform timestamp
  '''
  tz=localtime()[3]-gmtime()[3]
  d=datetime.now()
  return d.strftime("%Y-%m-%d %H:%M:%S GMT"+"%+.2d" % tz+":00")

# -------------------------------------------------------------------------------

  def sortedDictValues(adict):
    '''
    Given a dictionary it returns the values of this dict sorted 
    by the keys
    d = {2:1, 4:1, 1:1, 100:3, 3:5}
    sortedDictValues(d)
    [1, 1, 5, 1, 3]
    '''
    keys = adict.keys()
    keys.sort()
    return map(adict.get, keys)

# -------------------------------------------------------------------------------

def extraCopy (affiliation_name, path):
  '''
  @deprecated: replaced with extraCopySciCore
  Handles the extra copies of the data for transfer with datamover via the
  bc2 network to the FMI and BIOCENTER
  For the BIOCENTER there is a folder created in which all data gets into
  '''
  if (affiliation_name in AFFILIATION):
    if (affiliation_name == 'BIOCENTER_BASEL' or affiliation_name == 'NEUROSTEMX' ):
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
    
    #dropBoxFolder = '/tmp/scicore'
    dropBoxFolder = '/links/shared/dsu/dss/customers/biozentrum_scicore/drop-box'
    basename = os.path.basename(filePath)
    
    print("extraCopySciCore")
    print basename
    print affiliation_name
 
    if (affiliation_name in ['BIOCENTER_BASEL', 'NEUROSTEMX', 'SWISS_TPH']):
        dirname = os.path.join(dropBoxFolder, destinationFolder)
        if not os.path.exists(dirname):
            os.mkdir(dirname)
        print("COPYING " + filePath + " TO " + dirname)
        shutil.copy(filePath, dirname)

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

def searchParents (search_service, parents):

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

def sanitizeString(myString):
  return re.sub('[^A-Za-z0-9]+', '_', myString)

# -------------------------------------------------------------------------------

def searchSample (sample_code, search_service):
  sc = SearchCriteria()
  print('Searching sample: '+ str(sample_code))
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sample_code));
  foundSamples = search_service.searchForSamples(sc)
  return foundSamples


def renameFiles (fastq_files, undetermined, flow_cell_id):
    
    newFastqFileList = []
    for file in fastq_files:
        if undetermined:
            folder = os.path.dirname(file)
            fileName = os.path.basename(file)
            filepart, suffix = fileName.split('.',1)
            new_file =  folder + "/" + flow_cell_id + '_' + filepart + "." + suffix
            print ("Renaming file " + file + " to " + new_file)
            os.rename(file, new_file)
        else:
            new_file = file
        newFastqFileList.append(new_file)
    return newFastqFileList

# -------------------------------------------------------------------------------

def put_files_to_dataset (transaction, dataSet, fastq_files, folder_name, flow_cell_id, affiliation_name, undetermined):

    for file in fastq_files:
        extraCopySciCore (affiliation_name, file, folder_name)
        transaction.moveFile(file, dataSet, folder_name)

# -------------------------------------------------------------------------------

def split_incoming_folder_name (name):
  split=name.split("_")

  # expected incoming Name, e.g.: BSSE_QGF_22266_H0W8YBGXX_1
  if (len(split) == 5):
    sample_code = '-'.join([split[0], split[1], split[2]])
    flowCellId = split[3]
    flowLane = split[-1]
    undetermined = False

  # expected Undetermined_H0W8YBGXX
  if (len(split) == 2):
    sample_code = ''
    flowCellId = split[-1]
    flowLane = "1"
    undetermined = True
  
  incoming_sample = flowCellId + ':' + flowLane
  return sample_code, flowCellId, flowLane, incoming_sample, undetermined

# -------------------------------------------------------------------------------

def get_vocabulary_descriptions (transaction, vocabulary_name):
  vocabulary_descriptions_dict = {}
  vocabulary = transaction.getVocabulary(vocabulary_name)
  vocabulary_terms = vocabulary.getTerms()
  for term in vocabulary_terms:
    vocabulary_descriptions_dict[term.getCode()] = term.getDescription()
  return vocabulary_descriptions_dict

# -------------------------------------------------------------------------------

def process(transaction):

  undetermined = False

  print("\n" + str(datetime.now()))

  incomingPath = transaction.getIncoming().getAbsolutePath()
  name = transaction.getIncoming().getName()
  print("register-lane-nextseq.py is processing: " + name)
  sample_code, flowCellId, flowLane, incoming_sample, undetermined =  split_incoming_folder_name (name)

  # get all fastqs
  fastq_files=getFileNames(incomingPath)

  # BSSE-QGF-22266-H0W8YBGXX-1-654-BC3-TTAGGC_S1_L001_R1_001.fastq.gz
  # BSSE-QGF-22051-H0T25AGXX-1-1-1-TAAGGCGA-CTCTCTAT_S46_L001_R1_001.fastq.gz
  first_fastq_file = os.path.basename(fastq_files[0])

  # Get the search service
  search_service = transaction.getSearchService()

  flowcell_sample_immutable = searchSample (flowCellId, search_service)
  fcMetaDataDict = get_sample_properties(transaction, flowcell_sample_immutable[0])
  foundLane = searchSample (incoming_sample, search_service)

  # there should be only one sample because it is unique within one Flow Cell 
  if (len(foundLane) > 1):
    raise Exception("More than one sample found! No unique code: " + incoming_sample)
  elif (len(foundLane) == 0):
    raise Exception("No matching sample found for: " + incoming_sample)
  else :
    sample = foundLane[0].getSample()
    parents = sample.getParents()

  # Create a data set and set type
  dataSet = transaction.createNewDataSet("FASTQ_GZ")
  dataSet.setMeasuredData(False)
  dataSet.setPropertyValue(INDEX1, DEFAULT_INDEX)
  dataSet.setPropertyValue(INDEX2, DEFAULT_INDEX)
  dirName = transaction.createNewDirectory(dataSet,name)

  if not undetermined:
    newFastqFiles = fastq_files
    foundSample = searchSample (sample_code, search_service)
    sequencing_sample = foundSample[0].getSample()
    experiment = sequencing_sample.getExperiment()
    sequencing_sample_code = sequencing_sample.getCode()
    print("sequencing_sample_code: "+ sequencing_sample_code) 

    sequencing_sample_properties_dict = get_sample_properties (transaction, foundSample[0])
    
    if (INDEX1 in sequencing_sample_properties_dict) and (fcMetaDataDict[INDEXREAD1] > 0):
      #print(sequencing_sample_properties_dict[INDEX1])
      dataSet.setPropertyValue(INDEX1, sequencing_sample_properties_dict[INDEX1])
    if (INDEX2 in sequencing_sample_properties_dict) and (fcMetaDataDict[INDEXREAD2] > 0):
      dataSet.setPropertyValue(INDEX2, sequencing_sample_properties_dict[INDEX2])
    dataSet.setPropertyValue(EXTERNAL_SAMPLE_NAME, sequencing_sample_properties_dict[EXTERNAL_SAMPLE_NAME])
  
    if (AFFILIATION_PROPERTY_NAME in sequencing_sample_properties_dict):
      affiliation_name = sequencing_sample_properties_dict[AFFILIATION_PROPERTY_NAME]

    filepart, suffix = first_fastq_file.split('.',1)
    meta_data_file_name = filepart.rsplit('_',2)[0] +  METADATA_FILE_SUFFIX
    # get a file from the IDataSetRegistrationTransaction so it is automatically part of the data set
    meta_data_file_path = transaction.createNewFile(dataSet, name, meta_data_file_name)
    writeMetadataFile(transaction, name, meta_data_file_path, sequencing_sample_properties_dict,
                      fcMetaDataDict, experiment, affiliation_name, fastq_files, flowLane)
  
  # Undetermined Files
  else:
    affiliation_name = ""
    affiliation_for_Undetermined = ""
    newFastqFiles = []
    lane_parents = searchParents (search_service, parents)
    newFastqFiles = renameFiles(fastq_files, undetermined, flowCellId)
    for parent in lane_parents:
      sequencing_sample_properties_dict = get_sample_properties (transaction, parent)
      parent_sample = parent.getSample()
      sample_code = parent_sample.getCode()
      experiment = parent_sample.getExperiment()
      if (AFFILIATION_PROPERTY_NAME in sequencing_sample_properties_dict):
        affiliation_name = sequencing_sample_properties_dict[AFFILIATION_PROPERTY_NAME]
     
        # Special Sample Types without index (e.g. ILLUMINA_SEQUENCING_NEUROSTEMX_SINGLECELL) are caught here.
        # as those samples do not have a NCBI ORGANISM TAXONOMY    
        if NCBI_ORGANISM_TAXONOMY not in sequencing_sample_properties_dict:
            print(sample_code + ": Processing Sample without NCBI ORGANISM TAXONOMY: ILLUMINA_SEQUENCING_NEUROSTEMX_SINGLECELL")
            meta_data_file_path = transaction.createNewFile(dataSet, name, sample_code + '_' + flowCellId + '_' + first_fastq_file.split('.')[0] + METADATA_FILE_SUFFIX)
            writeMetadataFile(transaction, name, meta_data_file_path, sequencing_sample_properties_dict,
                       fcMetaDataDict, experiment, affiliation_name, newFastqFiles, flowLane)
            affiliation_for_Undetermined = affiliation_name
        
        elif (INDEX1 not in sequencing_sample_properties_dict) and (INDEX2 not in sequencing_sample_properties_dict) and \
              (sequencing_sample_properties_dict[NCBI_ORGANISM_TAXONOMY] != PHIX_TAXONOMY_ID):
          print('NONINDEXED sample and Taxonomy id is NOT' + PHIX_TAXONOMY_ID +', probably a pool: ' + sample_code)
          meta_data_file_path = transaction.createNewFile(dataSet, name, sample_code + '_' + flowCellId + '_' + first_fastq_file.split('.')[0] + METADATA_FILE_SUFFIX)
          writeMetadataFile(transaction, name, meta_data_file_path, sequencing_sample_properties_dict,
                         fcMetaDataDict, experiment, affiliation_name, newFastqFiles, flowLane)
          affiliation_for_Undetermined = affiliation_name
        # PARENTS:
        else:
          # Create Parent Meta data
          print(sample_code + ": Create parent meta data file")
          meta_data_file_path = transaction.createNewFile(dataSet, name, 'PARENT_' + sample_code + '_' + flowCellId + METADATA_FILE_SUFFIX)
          writeMetadataFile(transaction, name, meta_data_file_path, sequencing_sample_properties_dict,
                         fcMetaDataDict, experiment, affiliation_name, [], flowLane)
          continue
      
  put_files_to_dataset (transaction, dataSet, newFastqFiles, name, flowCellId, affiliation_for_Undetermined, undetermined)

  if foundLane.size() > 0:
    sa = transaction.getSampleForUpdate(foundLane[0].getSampleIdentifier())
    sa.setPropertyValue("DATA_TRANSFERRED", create_openbis_timestamp())
    dataSet.setSample(foundLane[0])
