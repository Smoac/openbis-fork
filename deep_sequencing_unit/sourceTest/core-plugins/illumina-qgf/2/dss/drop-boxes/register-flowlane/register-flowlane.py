'''
@copyright:
2015 ETH Zuerich, SIS
    
@license: 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    
http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@note: 
print statements go to: <openBIS_HOME>/datastore_server/log/startup_log.txt

@author:
Manuel Kohler
'''


import os
import shutil
import re
import sys
import subprocess
from collections import OrderedDict
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from java.lang import System;
# The following module is located in the path defined in the datastore_server.conf
# Look for: -Dpython.path}
from gfb_utils import *

from __builtin__ import file

FASTQ_GZ_PATTERN = "*.fastq.gz"
METADATA_FILE_SUFFIX = "_metadata.tsv"
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
if System.getProperty('os.name')  == 'Mac OS X':
    CRC32_PATH='lib/a.out'

def checkOnFileSize(file):
    return os.stat(file).st_size == 0


def CRC32_from_file(filename, transaction):
    
    if checkOnFileSize(filename):
        raise Exception("FILE " + filename + " IS EMPTY!")
    
    threadPropertyDict = get_thread_properties(transaction)
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


def writeMetadataFile(transaction, folder_name, meta_data_file_name, sequencing_sample_properties_dict,
                      fcMetaDataDict, experiment, sample_space, fastqFileList, flowLane):
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
        meta_data_file.write('SEQUENCER_MODEL' + "\t" + 
                             sequencer_vocabulary_description[fcMetaDataDict['SEQUENCER']].encode('utf-8') + "\n")
        
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
    extraCopySciCore (sample_space, meta_data_file_name, destinationFolder)


def extraCopySciCore (sample_space, filePath, destinationFolder=""):
    '''
    Handles the extra copies of the data for transfer with datamover for SCICORE
    '''
    
    dropBoxFolder = '/links/shared/dsu/dss/customers/biozentrum_scicore/drop-box'
    if System.getProperty('os.name')  == 'Mac OS X':
        dropBoxFolder = '/Users/kohleman/tmp/scicore'
    
    # if a sample is part of this space list then it will be transferred to sciCore
    SPACE_LIST = ["DUW_SALZBURGER", "BIOZENTRUM_HANDSCHIN", "BIOZENTRUM_ZAVOLAN",
                  "BIOZENTRUM_KELLER", "BIOZENTRUM_NIMWEGEN", "BSSE_DBM_BIOZENTRUM_NEUROSTEMX",
                  "UNI_BASEL_STPH_UTZINGER", "UNI_BASEL_STPH_GAGNEUX",
                  "BIOZENTRUM_PAPASSOTIROPOULOS_BEERENWINKEL", "BIOZENTRUM_SPANG",
                  "BIOZENTRUM_JENAL"]
    
    basename = os.path.basename(filePath)
    
    if (sample_space in SPACE_LIST):
        dirname = os.path.join(dropBoxFolder, destinationFolder)
        if not os.path.exists(dirname):
            os.mkdir(dirname)
        print("COPYING " + filePath + " TO SCICORE FOLDER " + dirname)
        shutil.copy(filePath, dirname)
    else:
        print(sample_space + " not in SPACE_LIST. Sample will not be copied to BC2.\n")


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
    
    ordered_sample_properties_dict = OrderedDict(sorted(sample_properties_dict.items(), key=lambda t: t[0]))
    return ordered_sample_properties_dict


def searchParents (search_service, parents):

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


def put_files_to_dataset (transaction, dataSet, fastq_files, folder_name, flow_cell_id, sample_space, undetermined):

    for file in fastq_files:
        print("SAMPLE_SPACE")
        print(sample_space)
        extraCopySciCore (sample_space, file, folder_name)
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

    # expected Undetermined_H0W8YBGX_1
    elif (len(split) == 3):
        sample_code = ''
        flowCellId = split[-2]
        flowLane = split[-1]
        undetermined = True
        
    # MiSeq BSSE_QGF_36097_000000000_AH4PH_1
    elif (len(split) == 6):
        sample_code = '-'.join([split[0], split[1], split[2]])
        flowCellId = '-'.join([split[3],split[4]])
        flowLane = split[-1]
        undetermined = False
    
    #MiSeq Undetermined_000000000_AH4PH_1
    elif (len(split) == 4):
        sample_code = ''
        flowCellId = '-'.join([split[1], split[2]])
        flowLane = split[-1]
        undetermined = True
    else:
        print("Expected different naming schema!")
  
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


def process_regular_samples(transaction, name, sample_code, flowLane, fastq_files, first_fastq_file, search_unique_sample, fcMetaDataDict, dataSet):
    foundSample = search_unique_sample(transaction, sample_code)
    sequencing_sample = foundSample[0].getSample()
    experiment = sequencing_sample.getExperiment()
    sequencing_sample_code = sequencing_sample.getCode()
    print "sequencing_sample_code: " + sequencing_sample_code
    sequencing_sample_properties_dict = get_sample_properties(transaction, foundSample[0])
    if (INDEX1 in sequencing_sample_properties_dict) and (fcMetaDataDict[INDEXREAD1] > 0):
        #print(sequencing_sample_properties_dict[INDEX1])
        dataSet.setPropertyValue(INDEX1, sequencing_sample_properties_dict[INDEX1])
    if (INDEX2 in sequencing_sample_properties_dict) and (fcMetaDataDict[INDEXREAD2] > 0):
        dataSet.setPropertyValue(INDEX2, sequencing_sample_properties_dict[INDEX2])
    dataSet.setPropertyValue(EXTERNAL_SAMPLE_NAME, sequencing_sample_properties_dict[EXTERNAL_SAMPLE_NAME])
    sample_space = foundSample[0].getSpace()
    filepart, suffix = first_fastq_file.split('.', 1)
    meta_data_file_name = filepart.rsplit('_', 2)[0] + METADATA_FILE_SUFFIX
    # get a file from the IDataSetRegistrationTransaction so it is automatically part of the data set
    meta_data_file_path = transaction.createNewFile(dataSet, name, meta_data_file_name)
    writeMetadataFile(transaction, name, meta_data_file_path, sequencing_sample_properties_dict, fcMetaDataDict, experiment, sample_space, fastq_files, flowLane)
    
    return fastq_files, sample_space


def process_undetermined(transaction, undetermined, name, flowCellId, flowLane, fastq_files, first_fastq_file, search_service, fcMetaDataDict, parents, dataSet):
    sample_space = ""
    newFastqFiles = []
    sample_space_list = []
    lane_parents = searchParents(search_service, parents)
    print "Found " + str(lane_parents.size()) + " parents"
    newFastqFiles = renameFiles(fastq_files, undetermined, flowCellId)
    for parent in lane_parents:
        sequencing_sample_properties_dict = get_sample_properties(transaction, parent)
        parent_sample = parent.getSample()
        sample_code = parent_sample.getCode()
        experiment = parent_sample.getExperiment()
        sample_space = parent.getSpace()
        sample_space_list.append(sample_space)

        # Special Sample Types without index (e.g. ILLUMINA_SEQUENCING_NEUROSTEMX_SINGLECELL) are caught here.
        # as those samples do not have a NCBI ORGANISM TAXONOMY
        if NCBI_ORGANISM_TAXONOMY not in sequencing_sample_properties_dict:
            print sample_code + ": Processing Sample without NCBI ORGANISM TAXONOMY: ILLUMINA_SEQUENCING_NEUROSTEMX_SINGLECELL"
            meta_data_file_path = transaction.createNewFile(dataSet, name, sample_code + '_' + flowCellId + '_' + first_fastq_file.split('.')[0] + METADATA_FILE_SUFFIX)
            writeMetadataFile(transaction, name, meta_data_file_path, sequencing_sample_properties_dict, fcMetaDataDict, experiment, sample_space, newFastqFiles, flowLane)

        elif (INDEX1 not in sequencing_sample_properties_dict or sequencing_sample_properties_dict[INDEX1] == 'NOINDEX') and \
         (INDEX2 not in sequencing_sample_properties_dict or sequencing_sample_properties_dict[INDEX2] == 'NOINDEX') and \
          (sequencing_sample_properties_dict[NCBI_ORGANISM_TAXONOMY] != PHIX_TAXONOMY_ID):
            print 'NONINDEXED sample and Taxonomy id is NOT ' + PHIX_TAXONOMY_ID + ', probably a pool: ' + sample_code
            meta_data_file_path = transaction.createNewFile(dataSet, name, sample_code + '_' + flowCellId + '_' + first_fastq_file.split('.')[0] + METADATA_FILE_SUFFIX)
            writeMetadataFile(transaction, name, meta_data_file_path, sequencing_sample_properties_dict, fcMetaDataDict, experiment, sample_space, newFastqFiles, flowLane)

        else:
            print sample_code + ": Create parent meta data file"
            meta_data_file_path = transaction.createNewFile(dataSet, name, 'PARENT_' + sample_code + '_' + flowCellId + METADATA_FILE_SUFFIX)
            writeMetadataFile(transaction, name, meta_data_file_path, sequencing_sample_properties_dict, fcMetaDataDict, experiment, sample_space, [], flowLane)
   
    sample_space_set = set(sample_space_list)
    sample_space_set.remove("PHIX")
    sample_space = sample_space_set.pop()
    return newFastqFiles, sample_space

def process(transaction):

    undetermined = False
    print("\n" + str(datetime.now()))

    incomingPath = transaction.getIncoming().getAbsolutePath()
    name = transaction.getIncoming().getName()
    
    sample_code, flowCellId, flowLane, incoming_sample, undetermined =  split_incoming_folder_name (name)
    
    # get all fastqs
    fastq_files = get_file_names(incomingPath, FASTQ_GZ_PATTERN)
    
    # BSSE-QGF-22266-H0W8YBGXX-1-654-BC3-TTAGGC_S1_L001_R1_001.fastq.gz
    # BSSE-QGF-22051-H0T25AGXX-1-1-1-TAAGGCGA-CTCTCTAT_S46_L001_R1_001.fastq.gz
    first_fastq_file = os.path.basename(fastq_files[0])

    search_service = transaction.getSearchService()
    
    flowcell_sample_immutable = search_unique_sample (transaction, flowCellId)
    fcMetaDataDict = get_sample_properties(transaction, flowcell_sample_immutable[0])
    flow_lane_immutable = search_unique_sample (transaction, incoming_sample)
    
    sample = flow_lane_immutable[0].getSample()
    parents = sample.getParents()
    
    dataSet = transaction.createNewDataSet("FASTQ_GZ")
    dataSet.setMeasuredData(False)
    dataSet.setPropertyValue(INDEX1, DEFAULT_INDEX)
    dataSet.setPropertyValue(INDEX2, DEFAULT_INDEX)
    dirName = transaction.createNewDirectory(dataSet,name)

    if undetermined:
        fastq_files, sample_space = process_undetermined(transaction, undetermined, name, flowCellId, flowLane,
                                fastq_files, first_fastq_file, search_service, fcMetaDataDict, parents, dataSet) 
    else:
        fastq_files, sample_space = process_regular_samples(transaction, name, sample_code, flowLane,
                                fastq_files, first_fastq_file, search_unique_sample, fcMetaDataDict, dataSet)

    put_files_to_dataset (transaction, dataSet, fastq_files, name, flowCellId, sample_space, undetermined)

    sa = transaction.getSampleForUpdate(flow_lane_immutable[0].getSampleIdentifier())
    sa.setPropertyValue("DATA_TRANSFERRED", create_openbis_timestamp_now())
    dataSet.setSample(flow_lane_immutable[0])

