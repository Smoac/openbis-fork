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

def login(configMap):
  logger.info('Logging into ' + configMap['openbisServer'])
  service = OpenbisServiceFacadeFactory.tryCreate(configMap['openbisUserName'],
                                                  configMap['openbisPassword'],
                                                  configMap['openbisServer'],
                                                  configMap['connectionTimeout'])
  return service

def logout (service):
  service.logout()
  logger.info('Logged out')

def setUpLogger(logPath, logLevel = logging.INFO):
  logFileName = 'createSampleSheet'
  d=datetime.now()
  logFileName = logFileName + '_' + d.strftime('%Y-%m-%d_%H_%M_%S') + '.log'
  logging.basicConfig(filename=logPath + logFileName, format='%(asctime)s %(message)s', level=logLevel)
  logger = logging.getLogger(logFileName)
  return logger
  

def parseConfigurationFile(propertyFile = 'etc/createSampleSheet.properties'):
  '''
  Parses the given config files and returns the values
  '''
  config = SafeConfigParser()
  config.read(propertyFile)
  config.sections()
  return config

def parseOptions():
  logger.info('Parsing command line parameters')
  parser = OptionParser(version='%prog 1.0')
  parser.add_option('-f', '--flowcell',
                  dest = 'flowcell',
                  help = 'The flowcell which is used to create the SampleSheet.csv',
                  metavar = '<flowcell>')
  parser.add_option('-m', '--mailist',
                  dest = 'maillist',
                  default=False,
                  action='store_true',
                  help = 'Generated Sample Sheet will be addtionally sent as email to the defined list of recipients',
                  metavar = '<maillist>')
  parser.add_option('-l', '--lineending',
                  dest = 'lineending',
                  type='choice',
                  action='store',
                  choices=['win32', 'linux', 'mac'],
                  default='linux',
                  help = 'Specify end of line separator: win32, linux, mac. Default: linux' ,
                  metavar = '<lineending>')
  parser.add_option('-o', '--outdir',
                  dest = 'outdir',
                  default='./',
                  help = 'Specify the ouput directory. Default: ./' ,
                  metavar = '<outdir>')
  parser.add_option('-s', '--singlelane',
                  dest = 'singlelane',
                  default=False,
                  action='store_true',
                  help = 'Creates a single Sample Sheet for each lane. Default: False')
  parser.add_option('-d', '--debug',
                  dest = 'debug',
                  default=False,
                  action='store_true',
                  help = 'Verbose debug logging. Default: False')

  (options, args) = parser.parse_args()
  
  if options.outdir[-1] <> '/':
    options.outdir = options.outdir + '/'
  
  if options.flowcell is None:
    parser.print_help()
    exit(-1)
  return options

def readConfig():
  logger.info('Reading config file')
  configMap = {}
  
  configParameters = parseConfigurationFile()
  configMap['facilityName'] = configParameters.get('GENERAL', 'facilityName')
  configMap['facilityNameShort'] = configParameters.get('GENERAL', 'facilityNameShort')
  configMap['facilityInstitution'] = configParameters.get('GENERAL', 'facilityInstitution')
  configMap['mailList'] = configParameters.get('GENERAL', 'mailList')
  configMap['mailFrom'] = configParameters.get('GENERAL', 'mailFrom')
  configMap['smptHost'] = configParameters.get('GENERAL', 'smptHost')
  configMap['SampleSheetFileName'] = configParameters.get('GENERAL', 'SampleSheetFileName')
  configMap['separator'] = configParameters.get('GENERAL', 'separator')
  configMap['indexSeparator'] = configParameters.get('GENERAL', 'indexSeparator')
  
  configMap['openbisUserName'] = configParameters.get('OPENBIS', 'openbisUserName')
  configMap['openbisPassword'] = configParameters.get('OPENBIS', 'openbisPassword', raw=True)
  configMap['openbisServer'] = configParameters.get('OPENBIS', 'openbisServer')
  configMap['connectionTimeout'] = configParameters.getint('OPENBIS', 'connectionTimeout')
  configMap['illuminaFlowCellTypeName'] = configParameters.get('OPENBIS', 'illuminaFlowCellTypeName')
  configMap['index1Name'] = configParameters.get('OPENBIS', 'index1Name')
  configMap['index2Name'] = configParameters.get('OPENBIS', 'index2Name')

  configMap['hiSeqNames'] = configParameters.get('ILLUMINA', 'hiSeqNames')
  configMap['miSeqNames'] = configParameters.get('ILLUMINA', 'miSeqNames')
  configMap['hiSeqHeader'] = configParameters.get('ILLUMINA', 'hiSeqHeader')
  
  configMap['miSeqHeaderSection'] = configParameters.get('ILLUMINA', 'miSeqHeaderSection')
  configMap['miSeqReadsSection'] = configParameters.get('ILLUMINA', 'miSeqReadsSection')
  configMap['miSeqSettingsSection'] = configParameters.get('ILLUMINA', 'miSeqSettingsSection')
  configMap['miSeqDataSection'] = configParameters.get('ILLUMINA', 'miSeqDataSection')
  configMap['miSeqWorkflow'] = configParameters.get('ILLUMINA', 'miSeqWorkflow')
  configMap['miSeqApplication'] = configParameters.get('ILLUMINA', 'miSeqApplication')
  configMap['miSeqChemistry'] = configParameters.get('ILLUMINA', 'miSeqChemistry')
  
  configMap['truSeqAdapter'] = configParameters.get('ILLUMINA', 'truSeqAdapter')
  configMap['nexteraAdapter'] = configParameters.get('ILLUMINA', 'nexteraAdapter')
  configMap['iemFileVersion'] = configParameters.get('ILLUMINA', 'iemFileVersion')
  return configMap

def getDate():
  d = datetime.now()
  return d.strftime('%A, %d of %B %Y')

def sanitizeString(myString):
  return re.sub('[^A-Za-z0-9]+', '_', myString)

def getVocabulary(vocabularyCode):
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

def sendMail(emails, files):
  '''
  Send out an email to the specified recipients
  '''
  COMMASPACE = ', '
  listofEmails = emails.split()
  
  msg = MIMEMultipart()
  msg['From'] = configMap['mailFrom']
  msg['To'] = COMMASPACE.join(listofEmails)
  msg['Date'] = formatdate(localtime=True)
  msg['Subject'] = 'Generated SampleSheet.csv'
  
  msg.attach( MIMEText('my Test') )
  
  for f in files:
        part = MIMEBase('application', 'octet-stream')
        part.set_payload( open(f,'rb').read() )
        Encoders.encode_base64(part)
        part.add_header('Content-Disposition', 'attachment; filename="%s"' % os.path.basename(f))
        msg.attach(part)

  smtp = smtplib.SMTP(configMap['smptHost'])
  smtp.sendmail(configMap['mailFrom'], listofEmails, msg.as_string())
  smtp.close()


def getFlowCell (illuminaFlowCellTypeName, flowCellName):
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
    return None, None 
  
  logger.info('Found ' + foundSample[0].getCode() + ' in openBIS')
  # Search for contained samples
  sampleSc = SearchCriteria()
  sampleSc.addSubCriteria(SearchSubCriteria.createSampleContainerCriteria(sc))
  foundContainedSamples = service.searchForSamples(sampleSc)
 
  return foundSample[0], foundContainedSamples


def getParents(sampleName):
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
 
def getContainedSampleProperties(containedSamples):
  '''
  Takes a  list of contained samples, retrieves the parents and their properties and returns it
  as a dictionary. The key is the sample name, the value is a list of the properties
  
  Additionally a dictionary with the lane (key) and the number of samples (value) is returned  
  '''
  parentDict = {}
  samplesPerLaneDict = {}
  
  for lane in containedSamples:
    parents = getParents (lane.getCode())
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

def createHiseqSampleSheet(parentDict, flowCellDict, samplesPerLaneDict):
  '''
  Builds up a dictionary with all entries in the Sample Sheet
  '''
  sampleSheetDict = {}
  # the illlumina pipeline uses always one base less than the sequencer is sequencing 
  demultiplexIndexlengthPenalty = -1
  
  # Maing sure the header is always a the top of the file
  sampleSheetDict[u'!'] = ([configMap['hiSeqHeader']])
  endType = flowCellDict['END_TYPE']
  cycles = flowCellDict['CYCLES_REQUESTED_BY_CUSTOMER']
  indexread  = int(flowCellDict['INDEXREAD']) + demultiplexIndexlengthPenalty
  indexread2 = int(flowCellDict['INDEXREAD2']) + demultiplexIndexlengthPenalty
  
  for key in parentDict.keys():
    index = ''
    lane = parentDict[key]['LANE'][-1:]
    
    if 'BARCODE' in parentDict[key] and indexread > 0:
      index = parentDict[key]['BARCODE'][0:indexread]
    
    if 'INDEX2' in parentDict[key] and indexread2 > 0:
      index = index + configMap['indexSeparator'] + parentDict[key]['INDEX2'][0:indexread2]
    
    # little hack to make the non-indexed control lane and non-indexed runs also part of
    # the sample sheet
    if len(index) == 0 and samplesPerLaneDict[lane] == 1:
      index = ' '
    
    # Set flag if this lane is a control lane. has no influence on the result, but makes reading
    # the demultiplex statistics easier
    if lane == flowCellDict['CONTROL_LANE']:
      control = 'Y'
    else:
      control = 'N'
    
    if len(index) > 0:
      sampleSheetDict[lane + '_' + key] = [flowCellName + ',' 
                            + lane + ',' 
                            + key + ','
                            + parentDict[key]['NCBI_ORGANISM_TAXONOMY'] + ','
                            + index + ','
                            + sanitizeString(parentDict[key]['EXTERNAL_SAMPLE_NAME']) + ',' 
                            + control + ','
                            + endType + '_' + cycles + ',' 
                            + configMap['facilityInstitution'] + ',' 
                            + foundFlowCell.getCode() + '_' + lane
                            ]

  sortedSampleSheetList = sampleSheetDict.keys()
  sortedSampleSheetList.sort()
  
  # if single lane demultiplexing is activated
  if myoptions.singlelane:
    for lane in range(1,int(flowCellDict['LANECOUNT'])+1):
      laneSeparatedList = [sample for sample in sortedSampleSheetList if sample[0] == str(lane)]
      # Making sure the header is also included at the top
      laneSeparatedList.insert(0, u'!')
      logger.debug(laneSeparatedList)
      writeSampleSheet(sampleSheetDict, laneSeparatedList, fileName = myoptions.outdir + 
                      configMap['SampleSheetFileName'] + '_' + str(lane))   
  else:
    writeSampleSheet(sampleSheetDict, sortedSampleSheetList, fileName = myoptions.outdir + 
                      configMap['SampleSheetFileName'])

def writeSampleSheet(sampleSheetDict, sortedSampleSheetList, fileName):
  '''
  Writes the given dictionary out to a csv file. The additional list is sorted and is used to write
  the dictionary in a sorted order.   
  '''
  newline = lineending[myoptions.lineending]

  myFile = fileName + '_' + flowCellName + '.csv'
  try:
    with open(myFile, 'w') as sampleSheetFile:
      for listElement in sortedSampleSheetList:
        sampleSheetFile.write(sampleSheetDict[listElement][0] + newline)
        
      logger.info('Writing file ' + myFile)
  except IOError, err:
    logger.error('File error: ' + str(err))
    print ('File error: ' + str(err))  

def writeMiSeqSampleSheet(sampleSheetDict, headerList, fileName):
  '''
  Writes the given dictionary to a csv file. The order does not matter. As the header is not fixed
  we first need to write the headerList in the file. This is specific to MiSeq
  '''
  newline = lineending[myoptions.lineending]
  
  myFile = fileName + '_' + flowCellName + '.csv'
  try:
    with open(myFile, 'wb') as sampleSheetFile:
      for listElement in headerList:
        sampleSheetFile.write(listElement + newline)
      for sample in sampleSheetDict:
        sampleSheetFile.write(sampleSheetDict[sample][0] + newline)
        
      logger.info('Writing file ' + myFile)
  except IOError:
    logger.error('File error: ' + str(err))
    print ('File error: ' + str(err))  
    
  return myFile



def createMiSeqSampleSheet(parentDict, flowCellDict, configMap, index1Vocabulary, index2Vocabulary):
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
  headerList.append(miSeqHeaderSection.pop().strip() + separator + '' )
  headerList.append(miSeqHeaderSection.pop().strip() + separator + flowCellDict['END_TYPE'] + '_' +flowCellDict['CYCLES_REQUESTED_BY_CUSTOMER'])
  headerList.append(miSeqHeaderSection.pop().strip() + separator + configMap['miSeqChemistry'])
  headerList.append('')
  
  miSeqReadsSection = configMap['miSeqReadsSection'].split(separator)
  miSeqReadsSection.reverse()
  headerList.append(miSeqReadsSection.pop())
  headerList.append(flowCellDict['CYCLES_REQUESTED_BY_CUSTOMER'])
  if (flowCellDict['END_TYPE'] == 'PAIRED_END'):
    headerList.append(flowCellDict['CYCLES_REQUESTED_BY_CUSTOMER'])
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
    #index2 = parentDict[key][configMap['index2Name']]
    
    sampleSheetDict[lane + '_' + key] = [key + separator
                            + sanitizeString(parentDict[key]['EXTERNAL_SAMPLE_NAME']) + separator
                            + separator
                            + separator
                            + index1Vocabulary[index1] + separator
                            + index1 + separator
     #                       + index2Vocabulary[index2].split()[2] + separator
      #                      + index2 + separator
                            + separator
                            + key + '_' + flowCellName
                            ]
  #print headerList
  #print sampleSheetDict
  sampleSheetFile = writeMiSeqSampleSheet(sampleSheetDict, headerList, fileName = myoptions.outdir + SampleSheetFileName)
  return sampleSheetFile 

'''
Main script
'''

logger = setUpLogger('log/')
logger.info('Started Creation of Sample Sheet...')

myoptions = parseOptions()

if myoptions.debug: 
  logger.setLevel(logging.DEBUG)
  
flowCellName = myoptions.flowcell
configMap = readConfig()
service = login(configMap)

foundFlowCell, containedSamples = getFlowCell(configMap['illuminaFlowCellTypeName'], flowCellName)
parentDict, samplesPerLaneDict = getContainedSampleProperties(containedSamples)
logger.info('Found ' + str(len(parentDict)) + ' samples on the flow cell ' + flowCellName)

sampleSheetList = []
flowCellName = foundFlowCell.getCode().split('_')[3][1:]
flowCellDict = convertSampleToDict(foundFlowCell)

hiseqs = configMap['hiSeqNames'].split()
miseqs = configMap['miSeqNames'].split()

index1Vocabulary = getVocabulary('BARCODES')
index2Vocabulary = getVocabulary('INDEX2')

createHiseqSampleSheet(parentDict, flowCellDict, samplesPerLaneDict)
#SampleSheetFile = createMiSeqSampleSheet(parentDict, flowCellDict, configMap, index1Vocabulary, index2Vocabulary)

#ncbi_tax =  parentDict['BSSE-QGF-7771']['NCBI_ORGANISM_TAXONOMY']

if myoptions.maillist:
  sendMail(configMap['mailList'], [SampleSheetFile])

logout(service)
print('DONE')
