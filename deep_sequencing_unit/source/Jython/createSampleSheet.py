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
                  default='linux',
                  help='Specify end of line separator: win32, linux, mac. Default: linux' ,
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

  configMap['hiSeqNames'] = configParameters.get(ILLUMINA, 'hiSeqNames')
  configMap['miSeqNames'] = configParameters.get(ILLUMINA, 'miSeqNames')
  configMap['hiSeqHeader'] = configParameters.get(ILLUMINA, 'hiSeqHeader')

  configMap['miSeqHeaderSection'] = configParameters.get(ILLUMINA, 'miSeqHeaderSection')
  configMap['miSeqReadsSection'] = configParameters.get(ILLUMINA, 'miSeqReadsSection')
  configMap['miSeqSettingsSection'] = configParameters.get(ILLUMINA, 'miSeqSettingsSection')
  configMap['miSeqDataSection'] = configParameters.get(ILLUMINA, 'miSeqDataSection')
  configMap['miSeqWorkflow'] = configParameters.get(ILLUMINA, 'miSeqWorkflow')
  configMap['miSeqApplication'] = configParameters.get(ILLUMINA, 'miSeqApplication')
  configMap['miSeqChemistry'] = configParameters.get(ILLUMINA, 'miSeqChemistry')

  configMap['truSeqAdapter'] = configParameters.get(ILLUMINA, 'truSeqAdapter')
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


def createDemultiplexCommands(myoptions, configMap, laneIndexDict, endType, cycles, lane,
                              myFileName):
  '''
  Builds up a command line for the demultiplexing of a single flow lane
  '''

  newline = lineending[myoptions.lineending]
  indexlength = laneIndexDict[lane][0]
  if laneIndexDict[lane][1] == 'DUALINDEX':
    basesMask = 'Y' + cycles + COMMA + 'I' + str(indexlength / 2) + 'n' + COMMA + 'I' + \
                str(indexlength / 2) + 'n'
  else:
    basesMask = 'Y' + cycles + COMMA + 'I' + str(indexlength) + 'n' + COMMA + (indexlength + 1) * 'n'
  if endType == 'PAIRED_END':
    basesMask = basesMask + COMMA + 'Y' + cycles
  return ([' '.join([configMap['configureBclToFastqPath'], configMap['failedReads'],
                     configMap['clusterCount'], configMap['clusterCountNumber'],
                     configMap['outputDir'], '../../../Unaligned_' + str(lane), configMap['sampleSheetName'],
                     myFileName, configMap['baseMask'], basesMask, newline])])

def createHiseqSampleSheet(parentDict, flowCellDict, samplesPerLaneDict, flowCellName, configMap,
                           logger, myoptions):
  '''
  Builds up a dictionary with all entries in the Sample Sheet
  '''
  sampleSheetDict = {}
  laneIndexDict = {}
  # the illlumina pipeline uses always one base less than the sequencer is sequencing
  DEMULTIPLEX_INDEX_LENGTH_PENALTY = -1

  # Maing sure the header is always a the top of the file
  sampleSheetDict[u'!'] = ([configMap['hiSeqHeader']])
  endType = flowCellDict[configMap['endType']]
  cycles = flowCellDict[configMap['cycles']]

  index1Name = configMap['index1Name']
  index2Name = configMap['index2Name']

  indexRead1Length = int(flowCellDict[configMap['index1Length']]) + DEMULTIPLEX_INDEX_LENGTH_PENALTY
  indexRead2Length = int(flowCellDict[configMap['index2Length']]) + DEMULTIPLEX_INDEX_LENGTH_PENALTY

  for key in parentDict.keys():
    index = ''
    dualIndex = False
    lane = parentDict[key]['LANE'][-1:]

    if index1Name in parentDict[key] and indexRead1Length > 0:
      index = parentDict[key][index1Name][0:indexRead1Length]

    if index2Name in parentDict[key] and indexRead2Length > 0:
      index = index + configMap['indexSeparator'] + parentDict[key][index2Name][0:indexRead2Length]
      dualIndex = True

    # little hack to make the non-indexed control lane and non-indexed runs also part of
    # the sample sheet
    # eventually hide in a function
    if len(index) == 0 and samplesPerLaneDict[lane] == 1:
      index = ' '

    # Set flag if this lane is a control lane. has no influence on the result, but makes reading
    # the demultiplex statistics easier
    if lane == flowCellDict[configMap['controlLane']]:
      control = 'Y'
    else:
      control = 'N'

    if len(index) > 0:
      sampleSheetDict[lane + '_' + key] = [flowCellName + COMMA
                            + lane + COMMA
                            + key + COMMA
                            + parentDict[key][configMap['ncbi']] + COMMA
                            + index + COMMA
                            + sanitizeString(parentDict[key][configMap['externalSampleName']]) + COMMA
                            + control + COMMA
                            + endType + '_' + cycles + COMMA
                            + configMap['facilityInstitution'] + COMMA
                            + key.split('_', 3)[-1]
                            ]

      sampleEndType = 'DUALINDEX' if dualIndex else 'SINGLEINDEX'
      laneIndexDict[int(lane)] = [len(index), sampleEndType]

  sortedSampleSheetList = sampleSheetDict.keys()
  sortedSampleSheetList.sort()

    # if single lane demultiplexing is activated
  if myoptions.singlelane:
    demultiplexCommandList = []
    for lane in range(1, int(flowCellDict[configMap['laneCount']]) + 1):
      laneSeparatedList = [sample for sample in sortedSampleSheetList if sample[0] == str(lane)]
      # Making sure the header is also included at the top
      laneSeparatedList.insert(0, u'!')
      logger.debug(laneSeparatedList)
      myFileName = myoptions.outdir + configMap['SampleSheetFileName'] + '_' + str(lane) + '_' + \
                   flowCellName + '.csv'

      demultiplexCommandList.append(createDemultiplexCommands(myoptions, configMap, laneIndexDict,
                                         endType, cycles, lane, myFileName))
      SamplesheetFile = writeSampleSheet(myoptions, logger, sampleSheetDict, laneSeparatedList,
                       fileName=myFileName)
    writeDemultiplexCommandList(logger, demultiplexCommandList,
                                fileName=myoptions.outdir + flowCellName + '_DemultiplexCommandList.txt')
    pickleDemultiplexCommandList(logger, demultiplexCommandList,
                                fileName=myoptions.outdir + flowCellName + '_DemultiplexCommandList.pickle')
  else:
    myFileName = myoptions.outdir + configMap['SampleSheetFileName'] + '_' + \
                   flowCellName + '.csv'
    writeSampleSheet(myoptions, logger, sampleSheetDict, sortedSampleSheetList,
                     fileName=myFileName)
    return myFileName

def writeDemultiplexCommandList(logger, demultiplexCommandList,
                                fileName):
  try:
    with open(fileName, 'wb') as demuxFile:
      for listElement in demultiplexCommandList:
        demuxFile.write(*listElement)

      logger.info('Writing file ' + fileName)
      print('Written ' + fileName)
  except IOError, err:
    logger.error('File error: ' + str(err))
    print ('File error: ' + str(err))

def pickleDemultiplexCommandList(logger, demultiplexCommandList, fileName):
  import pickle

  try:
    with open(fileName, 'w') as pickleDemux:
      pickle.dump(demultiplexCommandList, pickleDemux)
    logger.info('Writing file ' + fileName)
  except IOError, err:
    logger.error('File error: ' + str(err))
    print ('File error: ' + str(err))



def writeSampleSheet(myoptions, logger, sampleSheetDict, sortedSampleSheetList, fileName):
  '''
  Writes the given dictionary out to a csv file. The additional list is sorted and is used to write
  the dictionary in a sorted order.   
  '''
  newline = lineending[myoptions.lineending]
  try:
    with open(fileName, 'w') as sampleSheetFile:
      for listElement in sortedSampleSheetList:
        if myoptions.verbose:
          print sampleSheetDict[listElement][0]
        sampleSheetFile.write(sampleSheetDict[listElement][0] + newline)

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



def createMiSeqSampleSheet(parentDict, flowCellDict, configMap, index1Vocabulary, index2Vocabulary,
                            flowCellName, logger, myoptions):
  '''
  '''
  sampleSheetDict = {}
  headerList = []

  separator = configMap['separator']

  miSeqHeaderSection = configMap['miSeqHeaderSection'].split(separator)
  miSeqHeaderSection.reverse()
  headerList = [miSeqHeaderSection.pop().strip()]
  headerList.append(miSeqHeaderSection.pop().strip() + separator + configMap['iemFileVersion'])
  headerList.append(miSeqHeaderSection.pop().strip() + separator + configMap['facilityInstitution'])
  headerList.append(miSeqHeaderSection.pop().strip() + separator + configMap['facilityName'])
  headerList.append(miSeqHeaderSection.pop().strip() + separator + flowCellDict['Name'])
  headerList.append(miSeqHeaderSection.pop().strip() + separator + datetime.now().strftime('%d.%m.%Y'))
  headerList.append(miSeqHeaderSection.pop().strip() + separator + configMap['miSeqWorkflow'])
  headerList.append(miSeqHeaderSection.pop().strip() + separator + configMap['miSeqApplication'])
  headerList.append(miSeqHeaderSection.pop().strip() + separator + '')
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
#  if ('nextera' in (separator + parentDict.itervalues().next()['KIT'].lower())):
#    headerList.append(configMap['nexteraAdapter'])
#  if ('truseq' in (separator + parentDict.itervalues().next()['KIT'].lower())):
#    headerList.append(configMap['truSeqAdapter'])
  headerList.append('')

  miSeqDataSection = configMap['miSeqDataSection'].split(',')
  miSeqDataSection.reverse()
  headerList.append(miSeqDataSection.pop())
  headerList.append(','.join(miSeqDataSection.pop().strip().split()))

  for key in parentDict.keys():
    lane = parentDict[key]['LANE'][-1:]
    # If no index then just skip this  sample
    if configMap['index1Name'] not in parentDict[key]:
      continue


    index1 = parentDict[key][configMap['index1Name']]
    index2 = parentDict[key][configMap['index2Name']]

    sampleSheetDict[lane + '_' + key] = [key + separator
                            + sanitizeString(parentDict[key][configMap['externalSampleName']]) + separator
                            + separator
                            + separator
                            + index1Vocabulary[index1] + separator
                            + index1 + separator
                            + index2Vocabulary[index2].split()[2] + separator
                            + index2 + separator
                            + separator
                            + key + '_' + flowCellName
                            ]
  myFileName = myoptions.outdir + configMap['SampleSheetFileName'] + '_' + \
                   flowCellName + '.csv'

  sampleSheetFile = writeMiSeqSampleSheet(sampleSheetDict, headerList, flowCellName,
                                          myoptions, logger, fileName=myFileName)
  return sampleSheetFile

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
  configMap = readConfig(logger)
  service = login(logger, configMap)

  foundFlowCell, containedSamples = getFlowCell(configMap['illuminaFlowCellTypeName'], flowCellName,
                                                service, logger)
  parentDict, samplesPerLaneDict = getContainedSampleProperties(containedSamples, service)
  logger.info('Found ' + str(len(parentDict)) + ' samples on the flow cell ' + flowCellName)

  # take this variable from the FlowCellDict

  flowCellName = foundFlowCell.getCode().split('_')[3][1:]
  flowCellDict = convertSampleToDict(foundFlowCell)

  hiseqList = configMap['hiSeqNames'].split()
  miseqList = configMap['miSeqNames'].split()

  runFolderName = flowCellDict['Name']

#  for hiseq in hiseqList:
#    if hiseq in runFolderName:
#      logger.info('Detected HiSeq run.')
#      SampleSheetFile = createHiseqSampleSheet(parentDict, flowCellDict, samplesPerLaneDict, flowCellName, configMap,
#                         logger, myoptions)
#      break

  SampleSheetFile = createHiseqSampleSheet(parentDict, flowCellDict, samplesPerLaneDict, flowCellName, configMap,
                         logger, myoptions)
#  for miseq in miseqList:
#    if miseq in runFolderName:
#      logger.info('Detected MiSeq run.')
#      index1Vocabulary = getVocabulary(configMap['index1Name'], service)
#      index2Vocabulary = getVocabulary(configMap['index2Name'], service)
#      SampleSheetFile = createMiSeqSampleSheet(parentDict, flowCellDict, configMap,
#                                    index1Vocabulary, index2Vocabulary, flowCellName, logger, myoptions)
#      break

  if myoptions.maillist:
    sendMail(configMap['mailList'], [SampleSheetFile], flowCellName, configMap, logger)

  logout(service, logger)


if __name__ == "__main__":
    main()
