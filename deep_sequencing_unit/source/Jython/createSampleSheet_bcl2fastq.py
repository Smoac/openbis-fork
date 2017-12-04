'''
@copyright:
Copyright 2016 ETH Zuerich, SIS
 
@license:
Licensed under the Apache License, Version 2.0 (the 'License');
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an 'AS IS' BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author:
Manuel Kohler

@description:
Creates the SampleSheet.csv out of values from openBIS for Demultiplexing 
used in the Illumina pipeline (bcl2fastq) 

@attention:
Runs under Jython

@note:
Takes into account to replace special characters with an underscore so that the Illumina script
does not fail

HiSeq Header Description
========================
Column Header  Description
FCID  Flow cell ID
Lane  Positive integer, indicating the lane number (1-8)
SampleID  ID of the sample
SampleRef  The reference used for alignment for the sample
Index  Index sequences. Multiple index reads are separated by a hyphen (for example, ACCAGTAA-GGACATGA).
Description  Description of the sample
Control  Y indicates this lane is a control lane, N means sample
Recipe Recipe used during sequencing
Operator Name or ID of the operator
SampleProject  The project the sample belongs to
'''

import os
import logging
import re
import sys
import string
import smtplib
import argparse
from ConfigParser import SafeConfigParser
from datetime import *
from collections import OrderedDict

from email.MIMEMultipart import MIMEMultipart
from email.MIMEBase import MIMEBase
from email.MIMEText import MIMEText
from email.Utils import COMMASPACE, formatdate
from email import Encoders

from ch.systemsx.cisd.openbis.dss.client.api.v1 import OpenbisServiceFacadeFactory
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria

lineending = {'win32':'\r\n', 'linux':'\n', 'mac':'\r'}
COMMA = ','
CSV = ".csv"


class Sequencers:
    HISEQ_4000, HISEQ_3000, HISEQ_2500, HISEQ_2000, HISEQ_X, NEXTSEQ_500, MISEQ , UNIDENTIFIED = \
        ('Illumina HiSeq 4000', 'Illumina HiSeq 3000', 'Illumina HiSeq 2500', 'Illumina HiSeq 2000',
         'Illumina HiSeq X', 'Illumina NextSeq 500', 'Illumina MiSeq', 'Unidentified')
HISEQ_LIST = [Sequencers.HISEQ_2000, Sequencers.HISEQ_2500, Sequencers.HISEQ_3000, Sequencers.HISEQ_4000, Sequencers.HISEQ_X]


def login(logger, config_dict):
    logger.info('Logging into ' + config_dict['openbisServer'])
    service = OpenbisServiceFacadeFactory.tryCreate(config_dict['openbisUserName'],
                                                    config_dict['openbisPassword'],
                                                    config_dict['openbisServer'],
                                                    config_dict['connectionTimeout'])
    return service


def logout (service, logger):
    service.logout()
    logger.info('Logged out')


def setUpLogger(logPath, logLevel=logging.INFO):
    logFileName = 'create_sample_sheet_dict'
    d = datetime.now()
    logFileName = logFileName + '_' + d.strftime('%Y-%m-%d_%H_%M_%S') + '.log'
    logging.basicConfig(filename=logPath + logFileName,
                      format='%(asctime)s [%(levelname)s] %(message)s', level=logLevel)
    logger = logging.getLogger(logFileName)
    return logger


def parseOptions(logger):
    logger.info('Parsing command line parameters')
    parser = argparse.ArgumentParser(version='%prog 1.0', description='Process some integers.')
    parser.add_argument('-f', '--flowcell',
                  dest='flowcell',
                  help='The flowcell which is used to create the SampleSheet.csv',
                  metavar='<flowcell>')
    parser.add_argument('-m', '--mailist',
                  dest='maillist',
                  default=False,
                  action='store_true',
                  help='Generated Sample Sheet will be addtionally sent as email to the defined list of recipients')
    parser.add_argument('-l', '--lineending',
                  dest='lineending',
                  action='store',
                  choices=['win32', 'linux', 'mac'],
                  default='win32',
                  help='Specify end of line separator: win32, linux, mac. Default: win32' ,
                  metavar='<lineending>')
    parser.add_argument('-o', '--outdir',
                  dest='outdir',
                  default='./',
                  help='Specify the ouput directory. Default: ./' ,
                  metavar='<outdir>')
    parser.add_argument('-s', '--singlelane',
                  dest='singlelane',
                  default=True,
                  action='store_true',
                  help='Creates a single Sample Sheet for each lane. Default: False')
    parser.add_argument('-d', '--debug',
                  dest='debug',
                  default=False,
                  action='store_true',
                  help='Verbose debug logging. Default: False')
    parser.add_argument('--verbose',
                  dest='verbose',
                  default=False,
                  action='store_true',
                  help='Write Sample Sheet to stout. Default: False')

    args = parser.parse_args()
    
    if args.outdir[-1] <> '/':
        args.outdir = args.outdir + '/'

    if args.flowcell is None:
        parser.print_help()
        exit(-1)
    return args


def parseConfigurationFile(propertyFile='etc/createSampleSheet.properties'):
    '''
    Parses the given config files and returns the values
    '''
    config = SafeConfigParser()
    config.read(propertyFile)
    config.sections()
    return config


def readConfig(logger):
    GENERAL = 'GENERAL'
    OPENBIS = 'OPENBIS'
    ILLUMINA = 'ILLUMINA'
    
    logger.info('Reading config file')
    config_dict = {}
    
    configParameters = parseConfigurationFile()
    config_dict['facilityName'] = configParameters.get(GENERAL, 'facilityName')
    config_dict['facilityNameShort'] = configParameters.get(GENERAL, 'facilityNameShort')
    config_dict['facilityInstitution'] = configParameters.get(GENERAL, 'facilityInstitution')
    config_dict['mailList'] = configParameters.get(GENERAL, 'mailList')
    config_dict['mailFrom'] = configParameters.get(GENERAL, 'mailFrom')
    config_dict['smptHost'] = configParameters.get(GENERAL, 'smptHost')
    config_dict['SampleSheetFileName'] = configParameters.get(GENERAL, 'SampleSheetFileName')
    config_dict['separator'] = configParameters.get(GENERAL, 'separator')
    config_dict['indexSeparator'] = configParameters.get(GENERAL, 'indexSeparator')
    
    config_dict['openbisUserName'] = configParameters.get(OPENBIS, 'openbisUserName')
    config_dict['openbisPassword'] = configParameters.get(OPENBIS, 'openbisPassword', raw=True)
    config_dict['openbisServer'] = configParameters.get(OPENBIS, 'openbisServer')
    config_dict['connectionTimeout'] = configParameters.getint(OPENBIS, 'connectionTimeout')
    config_dict['illuminaFlowCellTypeName'] = configParameters.get(OPENBIS, 'illuminaFlowCellTypeName')
    config_dict['index1Name'] = configParameters.get(OPENBIS, 'index1Name')
    config_dict['index2Name'] = configParameters.get(OPENBIS, 'index2Name')
    config_dict['index1Length'] = configParameters.get(OPENBIS, 'index1Length')
    config_dict['index2Length'] = configParameters.get(OPENBIS, 'index2Length')
    config_dict['endType'] = configParameters.get(OPENBIS, 'endType')
    config_dict['cycles'] = configParameters.get(OPENBIS, 'cycles')
    config_dict['controlLane'] = configParameters.get(OPENBIS, 'controlLane')
    config_dict['ncbi'] = configParameters.get(OPENBIS, 'ncbi')
    config_dict['externalSampleName'] = configParameters.get(OPENBIS, 'externalSampleName')
    config_dict['laneCount'] = configParameters.get(OPENBIS, 'laneCount')
    config_dict['kit'] = configParameters.get(OPENBIS, 'kit')
    config_dict['10XSampleType'] = configParameters.get(OPENBIS, '10XSampleType')
    config_dict['10XIndexSet'] = configParameters.get(OPENBIS, '10XIndexSet')
    config_dict['recovered_cells'] = configParameters.get(OPENBIS, 'recovered_cells')
    
    config_dict['headerSection'] = configParameters.get(ILLUMINA, 'headerSection')
    config_dict['readsSection'] = configParameters.get(ILLUMINA, 'readsSection')
    config_dict['settingsSection'] = configParameters.get(ILLUMINA, 'settingsSection')
    config_dict['dataSectionSingleRead'] = configParameters.get(ILLUMINA, 'dataSectionSingleRead')
    config_dict['dataSectionDualRead'] = configParameters.get(ILLUMINA, 'dataSectionDualRead')
    config_dict['workflow'] = configParameters.get(ILLUMINA, 'workflow')
    config_dict['application'] = configParameters.get(ILLUMINA, 'application')
    config_dict['chemistry'] = configParameters.get(ILLUMINA, 'chemistry')
    
    config_dict['truSeqAdapter1'] = configParameters.get(ILLUMINA, 'truSeqAdapter1')
    config_dict['truSeqAdapter2'] = configParameters.get(ILLUMINA, 'truSeqAdapter2')
    config_dict['nexteraAdapter'] = configParameters.get(ILLUMINA, 'nexteraAdapter')
    config_dict['iemFileVersion'] = configParameters.get(ILLUMINA, 'iemFileVersion')
    
    config_dict['configureBclToFastqPath'] = configParameters.get(ILLUMINA, 'configureBclToFastqPath')
    config_dict['failedReads'] = configParameters.get(ILLUMINA, 'failedReads')
    config_dict['clusterCount'] = configParameters.get(ILLUMINA, 'clusterCount')
    config_dict['clusterCountNumber'] = configParameters.get(ILLUMINA, 'clusterCountNumber')
    config_dict['outputDir'] = configParameters.get(ILLUMINA, 'outputDir')
    config_dict['sampleSheetName'] = configParameters.get(ILLUMINA, 'sampleSheetName')
    config_dict['baseMask'] = configParameters.get(ILLUMINA, 'baseMask')
    
    return config_dict


def getDate():
    d = datetime.now()
    return d.strftime('%A, %d of %B %Y')


def sanitize_string(myString):
    return re.sub('[^A-Za-z0-9]+', '_', myString)


def get_vocabulary(vocabulary_code, service):
    """
    Returns the vocabulary terms and vocabulary labels of a vocabulary in a dictionary
    specified by the parameter vocabularyCode
    """
    
    terms = []
    vocabularies = service.listVocabularies()
    vocabulary_dict = {}
    for vocabulary in vocabularies:
        if vocabulary.getCode() == vocabulary_code:
            terms = vocabulary.getTerms()
    if terms:
        for term in terms:
            vocabulary_dict[term.getCode()] = term.getLabel()
    else:
        print ('No vocabulary found for ' + vocabulary_code)
#     print(vocabulary_dict)
    return vocabulary_dict


def send_email(emails, flowCellName, config_dict, logger, subject, body, files=""):
    """
    Send out an email to the specified recipients
    """
    COMMASPACE = ', '
    emails_list = emails.split()
    
    msg = MIMEMultipart()
    msg['From'] = config_dict['mailFrom']
    msg['To'] = COMMASPACE.join(emails_list)
    msg['Date'] = formatdate(localtime=True)
    msg['Subject'] = subject
    
    msg.attach(MIMEText(body))
    
    for f in files:
        part = MIMEBase('application', 'octet-stream')
        part.set_payload(open(f, 'rb').read())
        Encoders.encode_base64(part)
        part.add_header('Content-Disposition', 'attachment; filename="%s"' % os.path.basename(f))
        msg.attach(part)
    
    smtp = smtplib.SMTP(config_dict['smptHost'])
    smtp.sendmail(config_dict['mailFrom'], emails_list, msg.as_string())
    smtp.close()
    logger.info('Sent email to ' + COMMASPACE.join(emails_list))


def get_flowcell (illuminaFlowCellTypeName, flowCellName, service, logger):
    """
    Getting the the matching FlowCell
    """
    sc = SearchCriteria();
    sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, illuminaFlowCellTypeName));
    sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, flowCellName));
    foundSample = service.searchForSamples(sc)
    try:
        assert foundSample.size() == 1
    except AssertionError:
        print (str(foundSample.size()) + ' flow cells found which match.')
        exit(1)

    logger.info('Found ' + foundSample[0].getCode() + ' in openBIS')
    # Search for contained samples
    sampleSc = SearchCriteria()
    sampleSc.addSubCriteria(SearchSubCriteria.createSampleContainerCriteria(sc))
    foundContainedSamples = service.searchForSamples(sampleSc)

    return foundSample[0], foundContainedSamples


def get_reverse_complement(sequence):
    lookup_table = {'A': 'T', 'T': 'A', 'G': 'C', 'C': 'G'}
    reverse_complement = ''
    for nucleotide in reversed(sequence):
        reverse_complement += lookup_table[nucleotide]
    return reverse_complement


def get_model(run_id):
    """
    Guesses the sequencer model from the run folder name

    Current Naming schema for Illumina run folders, as far as I know,
    no documentation found on this, Illumina introduced a field called
    <InstrumentID> on the NextSeq runParameters.xml. That might be an
    option for the future. Alternatively a combination of the fields
    <ApplicationName> and <ApplicationVersion>.

    MiSeq: 150130_M01761_0114_000000000-ACUR0
    NextSeq: 150202_NS500318_0047_AH3KLMBGXX
    HiSeq 2000: 130919_SN792_0281_BD2CHRACXX
    HiSeq 2500: 150203_D00535_0052_AC66RWANXX
    HiSeq 3000: 150724_J00121_0017_AH2VYMBBXX
    HiSeq 4000: 150210_K00111_0013_AH2372BBXX
    HiSeq X: 141121_ST-E00107_0356_AH00C3CCXX
    """
    date, machine_id, run_number, fc_string = os.path.basename(run_id).split("_")

    if machine_id.startswith("NS"):
        model = Sequencers.NEXTSEQ_500
    elif machine_id.startswith("M"):
        model = Sequencers.MISEQ
    elif machine_id.startswith("D"):
        model = Sequencers.HISEQ_2500
    elif machine_id.startswith("SN"):
        model = Sequencers.HISEQ_2000
    elif machine_id.startswith("J"):
        model = Sequencers.HISEQ_3000
    elif machine_id.startswith("K"):
        model = Sequencers.HISEQ_4000
    elif machine_id.startswith("ST"):
        model = Sequencers.HISEQ_X
    else:
        model = Sequencers.UNIDENTIFIED
    return model


def get_parents(sampleName, service):
    """
    Returns a list of parents of a sample 
    """
    sc = SearchCriteria();
    sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sampleName));
    foundSample = service.searchForSamples(sc)
    
    try:
        assert foundSample.size() == 1
    except AssertionError:
        print (str(foundSample.size()) + ' flow lanes found which match.')
    
    # set the criteria for getting the parents when providing the child name
    sampleSc = SearchCriteria()
    sampleSc.addSubCriteria(SearchSubCriteria.createSampleChildCriteria(sc))
    foundParentSamples = service.searchForSamples(sampleSc)
    
    return foundParentSamples


def get_lane_sample_properties(lane, service, config_dict):
    parentDict = {}
    parents = get_parents (lane.getCode(), service)

    try:
        assert parents.size() >= 1
    except AssertionError:
        pass

    for parent in parents:
        parentCode = parent.getCode()
        parentProperties = parent.getProperties()
        propertyDict = {}
        for property in parentProperties:
            propertyDict[property] = parentProperties.get(property)

        propertyDict['LANE'] = lane.getCode()
        
        myKey = sanitize_string(parentCode + '_' + lane.getCode())
        parentDict[myKey] = propertyDict
        
    return parentDict



def get_contained_sample_properties(contained_samples, service, config_dict):
    """
    Takes a  list of contained samples, retrieves the parents and their properties and returns it
    as a dictionary. The key is the sample name, the value is a list of the properties

    Additionally a dictionary with the lane (key) and the number of samples (value) is returned
    """
    parentDict = {}
    samplesPerLaneDict = {}
    barcodesPerLaneDict = {}

    for lane in contained_samples:
        parents = get_parents (lane.getCode(), service)

        try:
            assert parents.size() >= 1
        except AssertionError:
            pass
#             print (str(parents.size()) + ' parents found for lane ' + lane.getCode())

        samplesPerLaneDict[lane.getCode()[-1]] = len(parents)

        for parent in parents:
            parentCode = parent.getCode()
            parentProperties = parent.getProperties()
            propertyDict = {}
            for property in parentProperties:
                propertyDict[property] = parentProperties.get(property)

            propertyDict['LANE'] = lane.getCode()
            plain_lane = lane.getCode().split(':')[-1]
            
            try:
                if barcodesPerLaneDict.has_key(plain_lane):
                    barcodesPerLaneDict[plain_lane].append(propertyDict[config_dict['index1Name']])
                else:
                    barcodesPerLaneDict[plain_lane] = [propertyDict[config_dict['index1Name']]]
            except:
                  pass

            try:
                if barcodesPerLaneDict.has_key(plain_lane):
                    barcodesPerLaneDict[plain_lane].append(propertyDict[config_dict['10XIndexSet']])
                else:
                    barcodesPerLaneDict[plain_lane] = [propertyDict[config_dict['10XIndexSet']]]
            except:
                  pass

            myKey = sanitize_string(parentCode + '_' + lane.getCode())
            parentDict[myKey] = propertyDict

    return parentDict, samplesPerLaneDict, barcodesPerLaneDict


def transform_sample_to_dict(foundFlowCell):
    """
    converts <type 'ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample'> to a python dict
    """
    flowCellDict = {}
    fcProperties = foundFlowCell.getProperties()
    
    for property in fcProperties:
        flowCellDict[property] = fcProperties.get(property)
    flowCellDict['Project'] = foundFlowCell.getExperimentIdentifierOrNull().split('/')[-1]
    flowCellDict['Name'] = foundFlowCell.getIdentifier().split('/')[-1]
    return flowCellDict


def write_sample_sheet_single_lane(model, ordered_sample_sheet_dict, flowCellDict, index_length_dict,
                                         parentDict, config_dict, myoptions, logger, csv_file):
    
    newline = lineending[myoptions.lineending]
    
    if (model is Sequencers.NEXTSEQ_500):
        lane_number = 2
    else:
        lane_number = int(flowCellDict[config_dict['laneCount']]) + 1

    for lane in range(1, lane_number):
        header_list = create_header_section (model, config_dict, parentDict, flowCellDict, index_length_dict, lane)
        per_lane_dict = [ordered_sample_sheet_dict[key] for key in ordered_sample_sheet_dict.keys() if int(key[0]) == lane]
        csv_file_path = myoptions.outdir + csv_file + "_" + str(lane) + CSV
        try:
            with open(csv_file_path, 'wb') as sample_sheet_file:                
                for header_element in header_list:
                    sample_sheet_file.write(header_element + newline)
                    if myoptions.verbose:
                        print(header_element + newline)
                for sample in per_lane_dict:
                    sample_sheet_file.write(str(sample[0]) + newline)
                    if myoptions.verbose:
                        print(str(sample[0]) + newline)
        except IOError:
            logger.error('File error: ' + str(err))
            print ('File error: ' + str(err))

def write_marker_file(flowCellDict, myoptions):
    marker_file_path = myoptions.outdir + ".10x_run_" + flowCellDict['Name']
    with open(marker_file_path, 'wb') as marker_file:
        for flowcell_prop in flowCellDict:
            marker_file.write(flowcell_prop)


def create_header_section (model, config_dict, parentDict, flowCellDict, index_length_dict, lane):

    kitsDict = {"CHIP_SEQ_SAMPLE_PREP" : ["", ""],
                "TRUSEQ_RNA_SAMPLEPREPKIT_V2_ILLUMINA" : ["A", "TruSeq LT"],
                "NEXTERA_XT_DNA_SAMPLE_PREPARATION_KIT_ILLUMINA" : ["S", "Nextera XT"],
                "TRUSEQ_CHIP_SAMPLE_PREP_KIT" : ["A", "TruSeq LT"],
                "MRNA_SEQ_SAMPLE_PREP" : ["", ""],
                "TRUSEQRNA_SAMPLE_PREP_KIT" : ["A", "TruSeq LT"],
                "NEBNEXT_DNA_SAMPLE_PREP_MASTER_MIX_SET1" : ["A", "TruSeq LT"],
                "NEBNEXT_CHIP-SEQ_LIBRARY_PREP_REAGENT_SET" : ["A", "TruSeq LT"],
                "RIBOZERO_SCRIPTSEQ_MRNA-SEQ_KIT" : ["", ""],
                "NEXTERA_DNA_SAMPLE_PREPARATION_KIT_ILLUMINA" : ["N", "Nextera"],
                "GENOMICDNA_SAMPLE_PREP" : ["", ""],
                "AGILENT_SURESELECTXT_AUTOMATEDLIBRARYPREP" : ["", ""],
                "TRUSEQ_DNA_SAMPLE_PREP_KIT" : ["A", "TruSeq LT"],
                "NEXTERA_DNA_SAMPLE_PREP_KITS" : ["N", "Nextera"],
                "AGILENT_SURESELECT_ENRICHMENTSYSTEM" : ["", ""],
                "TRUSEQ_DNA_SAMPLE_PREP_KIT_V2" : ["A", "TruSeq LT"],
                "AGILENT_SURESELECT_HUMAN_ALL_EXON_V5_UTRS" : ["", ""],
                "POLYA_SCRIPTSEQ_MRNA-SEQ_KIT" : ["", ""],
                "AGILENT_SURESELECTXT2_MOUSE_ALL_EXON" : ["", ""],
                "PAIRED_END_DNA_SAMPLE_PREP" : ["", ""],
                "NEXTERA_DNA_SAMPLE_PREP_KIT_BUFFER_HMW" : ["N", "Nextera"]
    }
    
    separator = config_dict['separator']
    header_list = []
    
    len_index1 = index_length_dict[int(lane)][0]
    len_index2 = index_length_dict[int(lane)][1]
    
    # here we take the first sample to determine the Sample Prep Kit 
    try:
        assay = kitsDict [parentDict.itervalues().next()[config_dict['kit']]][1]
    except:
#         print "No Kit set for sample. Will not set the assay value in the sample sheet"
        assay = ""
    
    header_section = config_dict['headerSection'].split(separator)
    header_section.reverse()
    header_list = [header_section.pop().strip()]
    header_list.append(header_section.pop().strip() + separator + config_dict['iemFileVersion'])
    header_list.append(header_section.pop().strip() + separator + config_dict['facilityInstitution'])
    header_list.append(header_section.pop().strip() + separator + config_dict['facilityName'])
    header_list.append(header_section.pop().strip() + separator + flowCellDict['Name'])
    header_list.append(header_section.pop().strip() + separator + datetime.now().strftime('%m/%d/%Y'))
    header_list.append(header_section.pop().strip() + separator + config_dict['workflow'])
    header_list.append(header_section.pop().strip() + separator + config_dict['application'])
    header_list.append(header_section.pop().strip() + separator + assay) 
    header_list.append(header_section.pop().strip() + separator + flowCellDict[config_dict['endType']] + '_' + flowCellDict[config_dict['cycles']])
    header_list.append(header_section.pop().strip() + separator + config_dict['chemistry'])
#     if config_dict['10XIndexSet']:
#         header_list.append(config_dict['recovered_cells'])
    header_list.append('')

    reads_section = config_dict['readsSection'].split(separator)
    reads_section.reverse()
    header_list.append(reads_section.pop())
    header_list.append(flowCellDict[config_dict['cycles']])
    if (flowCellDict[config_dict['endType']] == 'PAIRED_END'):
        header_list.append(flowCellDict[config_dict['cycles']])
    header_list.append('')

    settings_section = config_dict['settingsSection'].split(separator)
    settings_section.reverse()
    header_list.append(settings_section.pop())
#     if ('nextera' in assay.lower()):
#         header_list.append(config_dict['nexteraAdapter'])
#     if ('truseq' in assay.lower()):
#         header_list.append(config_dict['truSeqAdapter1'])
#         header_list.append(config_dict['truSeqAdapter2'])
    header_list.append('')

    if int(flowCellDict['INDEXREAD2']) > 0 and len_index2 > 0:
        SeqDataSection = config_dict['dataSectionDualRead'].split(',')
    else:
        SeqDataSection = config_dict['dataSectionSingleRead'].split(',')

    SeqDataSection.reverse()
    header_list.append(SeqDataSection.pop())
    
    if model in Sequencers.NEXTSEQ_500:
        # leaving out the 'Lane', as there are four but treat them as one
        header_list.append(','.join(SeqDataSection.pop().strip().split()[1:]))
    else:
        header_list.append(','.join(SeqDataSection.pop().strip().split()))
    
    return header_list
    

def verify_index_length (parentDict, flowCellDict, config_dict, logger):
    
    index_length_dict = {}
    verified_per_lane_dict = []
    
    flowcell_len_index1 = int(flowCellDict['INDEXREAD'])
    flowcell_len_index2 = int(flowCellDict['INDEXREAD2'])
    
    logger.info("Flowcell has index length [" + str(flowcell_len_index1) + ", " + str(flowcell_len_index2) + "]")

    for lane in range(1, int(flowCellDict['LANECOUNT']) + 1):
        index1_set = set ()
        index2_set = set ()
        index1_length = 0
        index2_length = 0
        
        logger.info("Lane: " + str(lane))
        per_lane_list = [parentDict[key] for key in parentDict.keys() if int(key[-1]) == lane]
        
        for sample in per_lane_list:
            # If no index then just skip this  sample
            if (config_dict['index1Name'] not in sample) or (sample[config_dict['index1Name']] == 'NOINDEX'):
                continue
            index1 = sample[config_dict['index1Name']]
            index2 = ""
            if config_dict['index2Name'] in sample:
                index2 = sample[config_dict['index2Name']]
            
            index1_set.add(len(index1))
            if index2:
                index2_set.add(len(index2))
            else:
                index2_set.add(0)
                
        # adding the index length of the flow cell to make sure that dual-indexed 
        # samples also work on a single-indexed run
        index1_set.add(flowcell_len_index1)
        index2_set.add(flowcell_len_index2)
         
        if index1_set:
            index1_length = min(index1_set)
        if index2_set:
            index2_length = min(index2_set)

        index_length_dict[lane] = [index1_length, index2_length]
        logger.info("Index1 Length Set: " + str(index1_set))
        logger.info("Index2 Length Set: " + str(index2_set))
        logger.info("Final length of index1 " + str(index1_length))
        logger.info("Final length of index2 " + str(index2_length))
        # print("Lane " + str(lane) + " [" + str(index1_length) + "," + str(index2_length) + "]")
                    
    return index_length_dict

  
def create_sample_sheet_dict(service, barcodesPerLaneDict, containedSamples, samplesPerLaneDict, model, parentDict, index_length_dict, flowCellDict,
                             config_dict, index1Vocabulary, index2Vocabulary, flowCellName, logger):

    sampleSheetDict = {}
    _10_run = False
    separator = config_dict['separator']
    
    for lane in containedSamples: 
        lane_sample_properties = get_lane_sample_properties(lane, service, config_dict)
        lane_int = lane.getCode().split(':')[-1]
        single_index_set = False

        try:
            logger.info(barcodesPerLaneDict[lane_int])
        except:
            print("No index found for lane " + str(lane_int) + ". Using the first sample which is not phix.")
            for key in lane_sample_properties.keys():
                if lane_sample_properties[key][u'NCBI_ORGANISM_TAXONOMY'] != u'10847' and not single_index_set: 
                    index1 = ""
                    lane_string = ""
                    if model in HISEQ_LIST or model in Sequencers.MISEQ:
                        lane_string = lane_int + separator

                    line = separator.join([lane_string + key, key + '_' + sanitize_string(lane_sample_properties[key][config_dict['externalSampleName']]), "", "", "", "", key, ""])
                    sampleSheetDict[lane_int + '_' + key] = [line]
                    single_index_set = True
    

        for key in lane_sample_properties.keys():
            
            # If no index or 'NOINDEX' assigned then just skip this  sample
            if ((config_dict['index1Name'] not in lane_sample_properties[key] or lane_sample_properties[key][config_dict['index1Name']] == 'NOINDEX') and
                config_dict['10XIndexSet'] not in lane_sample_properties[key]):
                continue
            
            index1 = "" 
            index2 = ""
                        
            if config_dict['index1Name'] in lane_sample_properties[key]:
                index1 = lane_sample_properties[key][config_dict['index1Name']]
                len_index1 = index_length_dict[int(lane_int)][0]
                    
            if config_dict['10XIndexSet'] in lane_sample_properties[key]:
                index1 = lane_sample_properties[key][config_dict['10XIndexSet']]
                # Do not modify the index length, as these are index sets provided by 10x 
                len_index1 = len(index1)
                _10_run = True
                                 
            if config_dict['index2Name'] in lane_sample_properties[key]:
                index2 = lane_sample_properties[key][config_dict['index2Name']]
                # Not needed, won't use it any more
                indexNumber = index2Vocabulary[lane_sample_properties[key][config_dict['index2Name']]].split()[2]
        
            # try:
            #    kit = lane_sample_properties[key][config_dict['kit']]
            #    prefix = kitsDict[kit][0]
            # except:
            #    prefix = ""
    
            len_index2 = index_length_dict[int(lane_int)][1]
    
            lane_string = ""
            if model in HISEQ_LIST or model in Sequencers.MISEQ:
                lane_string = lane_int + separator
            
            if int(flowCellDict['INDEXREAD2']) > 0 and len_index2 > 0:
                if model in Sequencers.NEXTSEQ_500:
                    index2_processed = get_reverse_complement(index2[0:len_index2])
                else:
                    index2_processed = index2
              
                line = separator.join([lane_string + key,
                                    key + '_' + sanitize_string(lane_sample_properties[key][config_dict['externalSampleName']]) + '_' + index1[0:len_index1] + '_' + index2[0:len_index2],
                                    "", "", "", index1[0:len_index1], "", index2_processed, key, ""])
                sampleSheetDict[lane_int + '_' + key] = [line]

            else:
                line = separator.join([lane_string + key, key + '_' + sanitize_string(lane_sample_properties[key][config_dict['externalSampleName']]) + '_' + index1[0:len_index1],
                                       "", "", "", index1[0:len_index1], key, ""])
                sampleSheetDict[lane_int + '_' + key] = [line]
    
    csv_file_name = config_dict['SampleSheetFileName'] + '_' + flowCellName
    ordered_sample_sheet_dict = OrderedDict(sorted(sampleSheetDict.items(), key=lambda t: t[0]))
    
    return _10_run, ordered_sample_sheet_dict, csv_file_name

'''
Main script
'''

def main ():

    logger = setUpLogger('log/')
    logger.info('Started Creation of Sample Sheet...')

    myoptions = parseOptions(logger)

    if myoptions.debug:
        logger.setLevel(logging.DEBUG)

    flowCellName = myoptions.flowcell
    config_dict = readConfig(logger)
    service = login(logger, config_dict)

    foundFlowCell, containedSamples = get_flowcell(config_dict['illuminaFlowCellTypeName'], flowCellName,
                                                service, logger)
    parentDict, samplesPerLaneDict, barcodesPerLaneDict = get_contained_sample_properties(containedSamples, service, config_dict)
    flowCellName = foundFlowCell.getCode()
    flowCellDict = transform_sample_to_dict(foundFlowCell)
    model = get_model(flowCellDict['RUN_NAME_FOLDER'])
    if not myoptions.verbose:
        print("Auto-detected: " + model)
    logger.info("Auto-detected: " + model)

    index1Vocabulary = get_vocabulary(config_dict['index1Name'], service)
    index2Vocabulary = get_vocabulary(config_dict['index2Name'], service)
    index_length_dict = verify_index_length(parentDict, flowCellDict, config_dict, logger)
    _10_run, ordered_sample_sheet_dict, csv_file_name = create_sample_sheet_dict(service, barcodesPerLaneDict, containedSamples,
                                                                        samplesPerLaneDict, model, parentDict, index_length_dict,
                                                                        flowCellDict, config_dict, index1Vocabulary, index2Vocabulary,
                                                                        flowCellName, logger)
    if len(ordered_sample_sheet_dict) < len(containedSamples):
        subject = "Warning: Creation of Sample Sheet (" + flowCellName + ") failed. No indices found."
        body = "Warning: No parents/libraries assigned to one of the flowlanes of " + flowCellName + \
        ". Either it is a non-indexed lane or the parents are not set.\n" + \
        "Check in the following map for missing lanes:\n" + str(ordered_sample_sheet_dict)
        send_email(config_dict['mailList'], flowCellName, config_dict, logger, subject, body)
    else:
        write_sample_sheet_single_lane(model, ordered_sample_sheet_dict, flowCellDict, index_length_dict,
                                          parentDict, config_dict, myoptions, logger, csv_file_name)
        if _10_run:
            write_marker_file(flowCellDict, myoptions)

    logout(service, logger)

if __name__ == "__main__":
    main()
