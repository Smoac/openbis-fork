'''
@copyright:
Copyright 2012 ETH Zuerich, CISD
 
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
used in the Illumina pipeline (configureBclToFastq.pl) 

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

from __future__ import with_statement
import os
import logging
import re
import sys
import string
import smtplib
from ConfigParser import SafeConfigParser
from optparse import OptionParser
from datetime import *

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

class Sequencers:
    HISEQ_4000, HISEQ_3000, HISEQ_2500, HISEQ_2000, HISEQ_X, NEXTSEQ_500, MISEQ , UNIDENTIFIED= \
        ('Illumina HiSeq 4000','Illumina HiSeq 3000','Illumina HiSeq 2500','Illumina HiSeq 2000',
         'Illumina HiSeq X', 'Illumina NextSeq 500', 'Illumina MiSeq', 'Unidentified')
HISEQ_LIST = [Sequencers.HISEQ_2000, Sequencers.HISEQ_2500, Sequencers.HISEQ_3000, Sequencers.HISEQ_4000, Sequencers.HISEQ_X]


HISEQ = "HiSeq"
MISEQ = "MiSeq"
NEXTSEQ = "NextSeq"

def login(logger, configMap):
  logger.info('Logging into ' + configMap['openbisServer'])
  service = OpenbisServiceFacadeFactory.tryCreate(configMap['openbisUserName'],
                                                  configMap['openbisPassword'],
                                                  configMap['openbisServer'],
                                                  configMap['connectionTimeout'])
  return service

def logout (service, logger):
  service.logout()
  logger.info('Logged out')

def setUpLogger(logPath, logLevel=logging.INFO):
  logFileName = 'createSampleSheet'
  d = datetime.now()
  logFileName = logFileName + '_' + d.strftime('%Y-%m-%d_%H_%M_%S') + '.log'
  logging.basicConfig(filename=logPath + logFileName,
                      format='%(asctime)s [%(levelname)s] %(message)s', level=logLevel)
  logger = logging.getLogger(logFileName)
  return logger

def parseOptions(logger):
  logger.info('Parsing command line parameters')
  parser = OptionParser(version='%prog 1.0')
  parser.add_option('-f', '--flowcell',
                  dest='flowcell',
                  help='The flowcell which is used to create the SampleSheet.csv',
                  metavar='<flowcell>')
  parser.add_option('-m', '--mailist',
                  dest='maillist',
                  default=False,
                  action='store_true',
                  help='Generated Sample Sheet will be addtionally sent as email to the defined list of recipients',
                  metavar='<maillist>')
  parser.add_option('-l', '--lineending',
                  dest='lineending',
                  type='choice',
                  action='store',
                  choices=['win32', 'linux', 'mac'],
                  default='win32',
                  help='Specify end of line separator: win32, linux, mac. Default: win32' ,
                  metavar='<lineending>')
  parser.add_option('-o', '--outdir',
                  dest='outdir',
                  default='./',
                  help='Specify the ouput directory. Default: ./' ,
                  metavar='<outdir>')
  parser.add_option('-s', '--singlelane',
                  dest='singlelane',
                  default=False,
                  action='store_true',
                  help='Creates a single Sample Sheet for each lane. Default: False')
  parser.add_option('-d', '--debug',
                  dest='debug',
                  default=False,
                  action='store_true',
                  help='Verbose debug logging. Default: False')
  parser.add_option('-v', '--verbose',
                  dest='verbose',
                  default=False,
                  action='store_true',
                  help='Write Sample Sheet to stout. Default: False')


  (options, args) = parser.parse_args()

  if options.outdir[-1] <> '/':
    options.outdir = options.outdir + '/'

  if options.flowcell is None:
    parser.print_help()
    exit(-1)
  return options

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
  configMap = {}

  configParameters = parseConfigurationFile()
  configMap['facilityName'] = configParameters.get(GENERAL, 'facilityName')
  configMap['facilityNameShort'] = configParameters.get(GENERAL, 'facilityNameShort')
  configMap['facilityInstitution'] = configParameters.get(GENERAL, 'facilityInstitution')
  configMap['mailList'] = configParameters.get(GENERAL, 'mailList')
  configMap['mailFrom'] = configParameters.get(GENERAL, 'mailFrom')
  configMap['smptHost'] = configParameters.get(GENERAL, 'smptHost')
  configMap['SampleSheetFileName'] = configParameters.get(GENERAL, 'SampleSheetFileName')
  configMap['separator'] = configParameters.get(GENERAL, 'separator')
  configMap['indexSeparator'] = configParameters.get(GENERAL, 'indexSeparator')

  configMap['openbisUserName'] = configParameters.get(OPENBIS, 'openbisUserName')
  configMap['openbisPassword'] = configParameters.get(OPENBIS, 'openbisPassword', raw=True)
  configMap['openbisServer'] = configParameters.get(OPENBIS, 'openbisServer')
  configMap['connectionTimeout'] = configParameters.getint(OPENBIS, 'connectionTimeout')
  configMap['illuminaFlowCellTypeName'] = configParameters.get(OPENBIS, 'illuminaFlowCellTypeName')
  configMap['index1Name'] = configParameters.get(OPENBIS, 'index1Name')
  configMap['index2Name'] = configParameters.get(OPENBIS, 'index2Name')
  configMap['index1Length'] = configParameters.get(OPENBIS, 'index1Length')
  configMap['index2Length'] = configParameters.get(OPENBIS, 'index2Length')
  configMap['endType'] = configParameters.get(OPENBIS, 'endType')
  configMap['cycles'] = configParameters.get(OPENBIS, 'cycles')
  configMap['controlLane'] = configParameters.get(OPENBIS, 'controlLane')
  configMap['ncbi'] = configParameters.get(OPENBIS, 'ncbi')
  configMap['externalSampleName'] = configParameters.get(OPENBIS, 'externalSampleName')
  configMap['laneCount'] = configParameters.get(OPENBIS, 'laneCount')
  configMap['kit'] = configParameters.get(OPENBIS, 'kit')

  configMap['hiSeqNames'] = configParameters.get(ILLUMINA, 'hiSeqNames')
  configMap['miSeqNames'] = configParameters.get(ILLUMINA, 'miSeqNames')
  configMap['nextSeqNames'] = configParameters.get(ILLUMINA, 'nextSeqNames')
  configMap['hiSeqHeader'] = configParameters.get(ILLUMINA, 'hiSeqHeader')

  configMap['miSeqHeaderSection'] = configParameters.get(ILLUMINA, 'miSeqHeaderSection')
  configMap['miSeqReadsSection'] = configParameters.get(ILLUMINA, 'miSeqReadsSection')
  configMap['miSeqSettingsSection'] = configParameters.get(ILLUMINA, 'miSeqSettingsSection')
  configMap['miSeqDataSectionSingleRead'] = configParameters.get(ILLUMINA, 'miSeqDataSectionSingleRead')
  configMap['miSeqDataSectionDualRead'] = configParameters.get(ILLUMINA, 'miSeqDataSectionDualRead')
  configMap['miSeqWorkflow'] = configParameters.get(ILLUMINA, 'miSeqWorkflow')
  configMap['miSeqApplication'] = configParameters.get(ILLUMINA, 'miSeqApplication')
  configMap['miSeqChemistry'] = configParameters.get(ILLUMINA, 'miSeqChemistry')
  
  configMap['nextSeqDataSectionSingleRead'] = configParameters.get(ILLUMINA, 'nextSeqDataSectionSingleRead')
  configMap['nextSeqDataSectionDualRead'] = configParameters.get(ILLUMINA, 'nextSeqDataSectionDualRead')

  configMap['hiSeqDataSectionSingleRead'] = configParameters.get(ILLUMINA, 'hiSeqDataSectionSingleRead')
  configMap['hiSeqDataSectionDualRead'] = configParameters.get(ILLUMINA, 'hiSeqDataSectionDualRead')

  configMap['truSeqAdapter1'] = configParameters.get(ILLUMINA, 'truSeqAdapter1')
  configMap['truSeqAdapter2'] = configParameters.get(ILLUMINA, 'truSeqAdapter2')
  configMap['nexteraAdapter'] = configParameters.get(ILLUMINA, 'nexteraAdapter')
  configMap['iemFileVersion'] = configParameters.get(ILLUMINA, 'iemFileVersion')

  configMap['configureBclToFastqPath'] = configParameters.get(ILLUMINA, 'configureBclToFastqPath')
  configMap['failedReads'] = configParameters.get(ILLUMINA, 'failedReads')
  configMap['clusterCount'] = configParameters.get(ILLUMINA, 'clusterCount')
  configMap['clusterCountNumber'] = configParameters.get(ILLUMINA, 'clusterCountNumber')
  configMap['outputDir'] = configParameters.get(ILLUMINA, 'outputDir')
  configMap['sampleSheetName'] = configParameters.get(ILLUMINA, 'sampleSheetName')
  configMap['baseMask'] = configParameters.get(ILLUMINA, 'baseMask')

  return configMap

def getDate():
  d = datetime.now()
  return d.strftime('%A, %d of %B %Y')

def sanitizeString(myString):
  return re.sub('[^A-Za-z0-9]+', '_', myString)

def getVocabulary(vocabularyCode, service):
  ''' Returns the vocabulary terms and vocabulary labels of a vocabulary in a dictionary
      specified by the parameter vocabularyCode
      '''
  terms = []
  vocabularies = service.listVocabularies()
  vocabularyDict = {}
  for vocabulary in vocabularies:
    if (vocabulary.getCode() == vocabularyCode):
      terms = vocabulary.getTerms()
  if terms:
    for term in terms:
      vocabularyDict[term.getCode()] = term.getLabel() 
  else:
    print ('No vocabulary found for ' + vocabularyCode)
  return vocabularyDict

def sendMail(emails, files, flowCellName, configMap, logger):
  '''
  Send out an email to the specified recipients
  '''
  COMMASPACE = ', '
  listofEmails = emails.split()

  msg = MIMEMultipart()
  msg['From'] = configMap['mailFrom']
  msg['To'] = COMMASPACE.join(listofEmails)
  msg['Date'] = formatdate(localtime=True)
  msg['Subject'] = 'Generated Sample Sheet for flowcell ' + flowCellName

  msg.attach(MIMEText('Sample Sheet for ' + flowCellName + ' attached.'))

  for f in files:
        part = MIMEBase('application', 'octet-stream')
        part.set_payload(open(f, 'rb').read())
        Encoders.encode_base64(part)
        part.add_header('Content-Disposition', 'attachment; filename="%s"' % os.path.basename(f))
        msg.attach(part)

  smtp = smtplib.SMTP(configMap['smptHost'])
  smtp.sendmail(configMap['mailFrom'], listofEmails, msg.as_string())
  smtp.close()
  logger.info('Sent email to ' + COMMASPACE.join(listofEmails))


def getFlowCell (illuminaFlowCellTypeName, flowCellName, service, logger):
  '''
  Getting the the matching FlowCell
  '''
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


def getParents(sampleName, service):
  '''
  Returns a list of parents of a sample 
  '''
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

def getContainedSampleProperties(containedSamples, service):

  # TODO: update description and function name
  '''   
  Takes a  list of contained samples, retrieves the parents and their properties and returns it
  as a dictionary. The key is the sample name, the value is a list of the properties
  
  Additionally a dictionary with the lane (key) and the number of samples (value) is returned  
  '''
  parentDict = {}
  samplesPerLaneDict = {}

  for lane in containedSamples:
    parents = getParents (lane.getCode(), service)

    try:
      assert parents.size() >= 1
    except AssertionError:
      print (str(parents.size()) + ' parents found for lane ' + lane.getCode())

    samplesPerLaneDict[lane.getCode()[-1]] = len(parents)

    for parent in parents:
      parentCode = parent.getCode()
      parentProperties = parent.getProperties()
      propertyDict = {}
      for property in parentProperties:
        propertyDict[property] = parentProperties.get(property)

      propertyDict['LANE'] = lane.getCode()

      myKey = sanitizeString(parentCode + '_' + lane.getCode())
      parentDict[myKey] = propertyDict

  return parentDict, samplesPerLaneDict


def convertSampleToDict(foundFlowCell):
  '''
  converts <type 'ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample'> to a python dict
  '''
  flowCellDict = {}
  fcProperties = foundFlowCell.getProperties()
  for property in fcProperties:
    flowCellDict[property] = fcProperties.get(property)
  flowCellDict['Project'] = foundFlowCell.getExperimentIdentifierOrNull().split('/')[-1]
  flowCellDict['Name'] = foundFlowCell.getIdentifier().split('/')[-1]
  return flowCellDict


def pickleDemultiplexCommandList(logger, demultiplexCommandList, fileName):
  import pickle

  try:
    with open(fileName, 'w') as pickleDemux:
      pickle.dump(demultiplexCommandList, pickleDemux)
    logger.info('Writing file ' + fileName)
  except IOError, err:
    logger.error('File error: ' + str(err))
    print ('File error: ' + str(err))


def writeMiSeqSampleSheet(sampleSheetDict, headerList, flowCellName, myoptions, logger, fileName):
  '''
  Writes the given dictionary to a csv file. The order does not matter. As the header is not fixed
  we first need to write the headerList in the file. This is specific to MiSeq
  '''
  newline = lineending[myoptions.lineending]
  try:
    with open(fileName, 'wb') as sampleSheetFile:
      for listElement in headerList:
        if myoptions.verbose:
          print listElement
        sampleSheetFile.write(listElement + newline)
      for sample in sampleSheetDict:
        if myoptions.verbose:
          print sampleSheetDict[sample][0]
        sampleSheetFile.write(sampleSheetDict[sample][0] + newline)

      logger.info('Writing file ' + fileName)

  except IOError:
    logger.error('File error: ' + str(err))
    print ('File error: ' + str(err))

  return fileName

def get_reverse_complement(sequence):
    lookup_table = {'A': 'T', 'T': 'A', 'G': 'C', 'C': 'G'}
    reverse_complement = ''
    for nucleotide in reversed(sequence):
        reverse_complement += lookup_table[nucleotide]
    return reverse_complement


def createSampleSheet(model, parentDict, flowCellDict, configMap, index1Vocabulary, index2Vocabulary,
                            flowCellName, logger, myoptions):
  '''
  '''
    

  kitsDict = {"CHIP_SEQ_SAMPLE_PREP" : ["",""],
              "TRUSEQ_RNA_SAMPLEPREPKIT_V2_ILLUMINA" : ["A","TruSeq LT"],
              "NEXTERA_XT_DNA_SAMPLE_PREPARATION_KIT_ILLUMINA" : ["S", "Nextera XT"],
              "TRUSEQ_CHIP_SAMPLE_PREP_KIT" : ["A","TruSeq LT"],
              "MRNA_SEQ_SAMPLE_PREP" : ["",""],
              "TRUSEQRNA_SAMPLE_PREP_KIT" : ["A","TruSeq LT"],
              "NEBNEXT_DNA_SAMPLE_PREP_MASTER_MIX_SET1" : ["A","TruSeq LT"],
              "NEBNEXT_CHIP-SEQ_LIBRARY_PREP_REAGENT_SET" : ["A","TruSeq LT"],
              "RIBOZERO_SCRIPTSEQ_MRNA-SEQ_KIT" : ["",""],
              "NEXTERA_DNA_SAMPLE_PREPARATION_KIT_ILLUMINA" : ["N", "Nextera"],
              "GENOMICDNA_SAMPLE_PREP" : ["",""],
              "AGILENT_SURESELECTXT_AUTOMATEDLIBRARYPREP" : ["",""],
              "TRUSEQ_DNA_SAMPLE_PREP_KIT" : ["A","TruSeq LT"],
              "NEXTERA_DNA_SAMPLE_PREP_KITS" : ["N", "Nextera"],
              "AGILENT_SURESELECT_ENRICHMENTSYSTEM" : ["",""],
              "TRUSEQ_DNA_SAMPLE_PREP_KIT_V2" : ["A","TruSeq LT"],
              "AGILENT_SURESELECT_HUMAN_ALL_EXON_V5_UTRS" : ["",""],
              "POLYA_SCRIPTSEQ_MRNA-SEQ_KIT" : ["",""],
              "AGILENT_SURESELECTXT2_MOUSE_ALL_EXON" : ["",""],
              "PAIRED_END_DNA_SAMPLE_PREP" : ["",""],
              "NEXTERA_DNA_SAMPLE_PREP_KIT_BUFFER_HMW" : ["N", "Nextera"]
  }

  sampleSheetDict = {}
  headerList = []
  
  # here we take the first sample to determine the Sample Prep Kit 
  try:
    assay = kitsDict [parentDict.itervalues().next()[configMap['kit']]][1]
  except:
    print "No Kit set for sample. Will not set the assay value in the sample sheet"
    assay = ""
    pass

  separator = configMap['separator']

  miSeqHeaderSection = configMap['miSeqHeaderSection'].split(separator)
  miSeqHeaderSection.reverse()
  headerList = [miSeqHeaderSection.pop().strip()]
  headerList.append(miSeqHeaderSection.pop().strip() + separator + configMap['iemFileVersion'])
  headerList.append(miSeqHeaderSection.pop().strip() + separator + configMap['facilityInstitution'])
  headerList.append(miSeqHeaderSection.pop().strip() + separator + configMap['facilityName'])
  headerList.append(miSeqHeaderSection.pop().strip() + separator + flowCellDict['Name'])
  headerList.append(miSeqHeaderSection.pop().strip() + separator + datetime.now().strftime('%m/%d/%Y'))
  headerList.append(miSeqHeaderSection.pop().strip() + separator + configMap['miSeqWorkflow'])
  headerList.append(miSeqHeaderSection.pop().strip() + separator + configMap['miSeqApplication'])
  headerList.append(miSeqHeaderSection.pop().strip() + separator + assay) 
  headerList.append(miSeqHeaderSection.pop().strip() + separator + flowCellDict[configMap['endType']] + '_' + flowCellDict[configMap['cycles']])
  headerList.append(miSeqHeaderSection.pop().strip() + separator + configMap['miSeqChemistry'])
  headerList.append('')

  miSeqReadsSection = configMap['miSeqReadsSection'].split(separator)
  miSeqReadsSection.reverse()
  headerList.append(miSeqReadsSection.pop())
  headerList.append(flowCellDict[configMap['cycles']])
  if (flowCellDict[configMap['endType']] == 'PAIRED_END'):
    headerList.append(flowCellDict[configMap['cycles']])
  headerList.append('')

  miSeqSettingsSection = configMap['miSeqSettingsSection'].split(separator)
  miSeqSettingsSection.reverse()
  headerList.append(miSeqSettingsSection.pop())
  if ('nextera' in assay.lower()):
    headerList.append(configMap['nexteraAdapter'])
  if ('truseq' in assay.lower()):
    headerList.append(configMap['truSeqAdapter1'])
    headerList.append(configMap['truSeqAdapter2'])
  headerList.append('')

  if int(flowCellDict['INDEXREAD2']) > 0:
      if model in [Sequencers.NEXTSEQ_500]:
          SeqDataSection = configMap['nextSeqDataSectionDualRead'].split(',')
      if model in HISEQ_LIST:
          SeqDataSection = configMap['hiSeqDataSectionDualRead'].split(',')
      if model in [Sequencers.MISEQ]: 
          SeqDataSection = configMap['miSeqDataSectionDualRead'].split(',')
  else:
      if model in [Sequencers.NEXTSEQ_500]:
          SeqDataSection = configMap['nextSeqDataSectionSingleRead'].split(',')
      if model in HISEQ_LIST:
          SeqDataSection = configMap['hiSeqDataSectionSingleRead'].split(',')
      if model in [Sequencers.MISEQ]:
          SeqDataSection = configMap['miSeqDataSectionSingleRead'].split(',')
    
  SeqDataSection.reverse()
  headerList.append(SeqDataSection.pop())
  headerList.append(','.join(SeqDataSection.pop().strip().split()))

  for key in parentDict.keys():
    lane = parentDict[key]['LANE'][-1:]
    # If no index then just skip this  sample
    if (configMap['index1Name'] not in parentDict[key]) or (parentDict[key][configMap['index1Name']] == 'NOINDEX'):
      continue
    index1 = parentDict[key][configMap['index1Name']]

    if configMap['index2Name'] in parentDict[key]:
      index2 = parentDict[key][configMap['index2Name']]
      indexNumber = index2Vocabulary[parentDict[key][configMap['index2Name']]].split()[2]

    try:
      kit = parentDict[key][configMap['kit']]
      prefix = kitsDict[kit][0]
    except:
      print "Missing Kit on " + str(key)
      prefix = ""

    len_index1 = int(flowCellDict['INDEXREAD'])
    len_index2 = int(flowCellDict['INDEXREAD2'])

    lane_string =""
    if model in HISEQ_LIST or model in Sequencers.MISEQ:
        lane_string = lane + separator

    if int(flowCellDict['INDEXREAD2']) > 0:
      
      if model in Sequencers.NEXTSEQ_500:
          index2_processed = get_reverse_complement(index2[0:len_index2])
      else:
          index2_processed = index2
      
      
      sampleSheetDict[lane + '_' + key] = [
                            lane_string
                            + key + separator
                            + key + '_' + sanitizeString(parentDict[key][configMap['externalSampleName']]) + '_' + index1[0:len_index1] + '_' + index2[0:len_index2] + separator
                            + separator
                            + separator
                            + index1Vocabulary[index1].split()[1] + separator
                            + index1[0:len_index1] + separator
                            + prefix + indexNumber + separator
                            + index2_processed + separator
                            + key + separator
                            ]
    else:
            sampleSheetDict[lane + '_' + key] = [
                              lane_string
                            + key + separator
                            + key + '_' + sanitizeString(parentDict[key][configMap['externalSampleName']]) + '_' + index1[0:len_index1] + separator
                            + separator
                            + separator
                            + index1Vocabulary[index1].split()[1] + separator
                            + index1[0:len_index1] + separator
                            + key + separator
                            ]
    
  myFileName = myoptions.outdir + configMap['SampleSheetFileName'] + '_' + \
                   flowCellName + '.csv'

  sampleSheetFile = writeMiSeqSampleSheet(sampleSheetDict, headerList, flowCellName,
                                          myoptions, logger, fileName=myFileName)
  return sampleSheetFile

'''
Main script
'''

# def guessSequencerType(configMap, flowCellDict):
#     
#     if flowCellDict['SEQUENCER']:
#         sequencer_code = flowCellDict['SEQUENCER']
#     else:
#         print("No Sequencer code found (FLOWCELL property SEQUENCER)!")
#         return "Unspecified"
#         
#     hiseqList = configMap['hiSeqNames'].split()
#     miseqList = configMap['miSeqNames'].split()
#     nextseqList = configMap['nextSeqNames'].split()
#     
#     if sequencer_code in hiseqList:
#         return HISEQ
#     elif sequencer_code in miseqList:
#         return MISEQ
#     elif sequencer_code in nextseqList:
#         return NEXTSEQ
#     else:
#         print("WARNING: Sequencer Code " + sequencer_code + " not found in properties files!")


def main ():

  logger = setUpLogger('log/')
  logger.info('Started Creation of Sample Sheet...')

  myoptions = parseOptions(logger)

  if myoptions.debug:
    logger.setLevel(logging.DEBUG)

  flowCellName = myoptions.flowcell
  configMap = readConfig(logger)
  service = login(logger, configMap)

  foundFlowCell, containedSamples = getFlowCell(configMap['illuminaFlowCellTypeName'], flowCellName,
                                                service, logger)
  parentDict, samplesPerLaneDict = getContainedSampleProperties(containedSamples, service)
  logger.info('Found ' + str(len(parentDict)) + ' samples on the flow cell ' + flowCellName)

  flowCellName = foundFlowCell.getCode()
  flowCellDict = convertSampleToDict(foundFlowCell)
  model = get_model(flowCellDict['RUN_NAME_FOLDER'])

  index1Vocabulary = getVocabulary(configMap['index1Name'], service)
  index2Vocabulary = getVocabulary(configMap['index2Name'], service)
  SampleSheetFile = createSampleSheet(model, parentDict, flowCellDict, configMap,
                             index1Vocabulary, index2Vocabulary, flowCellName, logger, myoptions)

  if myoptions.maillist:
    sendMail(configMap['mailList'], [SampleSheetFile], flowCellName, configMap, logger)

  logout(service, logger)

if __name__ == "__main__":
    main()
