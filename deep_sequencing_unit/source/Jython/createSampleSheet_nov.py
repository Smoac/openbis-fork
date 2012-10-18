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

from ch.systemsx.cisd.openbis.dss.client.api.v1 import OpenbisServiceFacadeFactory
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria
from java.util import EnumSet

lineending = {'win32':'\r\n', 'linux':'\n', 'mac':'\r'}
COMMA = ','

def login(configMap, logger):
  logger.info('Logging into ' + configMap['openbisServer'])
  try:
    service = OpenbisServiceFacadeFactory.tryCreate(configMap['openbisUserName'],
                                                  configMap['openbisPassword'],
                                                  configMap['openbisServer'],
                                                  configMap['connectionTimeout'])
  except:
    raise ('Could not connect to ' + configMap['openbisServer'] + '. Please check if the server ' +
    'address is OK, the firewall is not blocking the communication or openBIS is down.')

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
  parser.add_option('-d', '--debug',
                  dest='debug',
                  default=False,
                  action='store_true',
                  help='Verbose debug logging. Default: False')

  (options, args) = parser.parse_args()

  if options.outdir[-1] <> '/':
    options.outdir = options.outdir + '/'

  if options.flowcell is None:
    parser.print_help()
    exit(-1)
  return options

def parseConfigurationFile(logger, propertyFile='etc/createSampleSheet_nov.properties'):
  '''
  Parses the given config files and returns the values
  '''
  logger.info('Reading config file ' + propertyFile)
  config = SafeConfigParser()
  config.read(propertyFile)
  config.sections()
  return config

def readConfig(logger):

  GENERAL = 'GENERAL'
  OPENBIS = 'OPENBIS'
  ILLUMINA = 'ILLUMINA'

  configMap = {}

  logger.info('Reading config file')
  configParameters = parseConfigurationFile(logger)

  configMap['facilityName'] = configParameters.get(GENERAL, 'facilityName')
  configMap['facilityNameShort'] = configParameters.get(GENERAL, 'facilityNameShort')
  configMap['facilityInstitution'] = configParameters.get(GENERAL, 'facilityInstitution')
  configMap['sampleSheetFileName'] = configParameters.get(GENERAL, 'sampleSheetFileName')
  configMap['lanePrefix'] = configParameters.get(GENERAL, 'lanePrefix')
  configMap['separator'] = configParameters.get(GENERAL, 'separator')
  configMap['indexSeparator'] = configParameters.get(GENERAL, 'indexSeparator')

  configMap['openbisUserName'] = configParameters.get(OPENBIS, 'openbisUserName')
  configMap['openbisPassword'] = configParameters.get(OPENBIS, 'openbisPassword', raw=True)
  configMap['openbisServer'] = configParameters.get(OPENBIS, 'openbisServer')
  configMap['connectionTimeout'] = configParameters.getint(OPENBIS, 'connectionTimeout')
  configMap['illuminaFlowCellTypeName'] = configParameters.get(OPENBIS, 'illuminaFlowCellTypeName')
  configMap['index1Name'] = configParameters.get(OPENBIS, 'index1Name')
  configMap['index2Name'] = configParameters.get(OPENBIS, 'index2Name')
  configMap['species'] = configParameters.get(OPENBIS, 'species')
  configMap['sampleName'] = configParameters.get(OPENBIS, 'sampleName')
  configMap['operator'] = configParameters.get(OPENBIS, 'operator')
  configMap['endType'] = configParameters.get(OPENBIS, 'endType')
  configMap['readLength'] = configParameters.get(OPENBIS, 'readLength')
  configMap['lengthIndex1'] = configParameters.get(OPENBIS, 'lengthIndex1')
  configMap['lengthIndex2'] = configParameters.get(OPENBIS, 'lengthIndex2')
  configMap['gaNumber'] = configParameters.get(OPENBIS, 'gaNumber')

  configMap['hiSeqNames'] = configParameters.get(ILLUMINA, 'hiSeqNames')
  configMap['hiSeqHeader'] = configParameters.get(ILLUMINA, 'hiSeqHeader')

  return configMap

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

  # set the criteria for getting the parents when providing the child name
  sampleSc = SearchCriteria()
  sampleSc.addSubCriteria(SearchSubCriteria.createSampleChildCriteria(sc))
  foundParentSamples = service.searchForSamples(sampleSc)

  return foundParentSamples

def getContainedSampleProperties(logger, containedSamples, service):
  '''
  Takes a  list of contained samples, retrieves the parents and their properties and returns it
  as a dictionary. The key is the sample name, the value is a list of the properties
  '''
  laneParentDict = {}

  for lane in containedSamples:
    parents = getParents (lane.getCode(), service)

    for parent in parents:
      parentCode = parent.getCode()
      parentProperties = parent.getProperties()

      propertyDict = {}
      for property in parentProperties:
        propertyDict[property] = parentProperties.get(property)

      propertyDict['LANE'] = lane.getCode()
      propertyDict['SAMPLE_TYPE'] = parent.getSampleTypeCode()
      myKey = sanitizeString(parentCode + '_' + lane.getCode())
      laneParentDict[myKey] = propertyDict

  logger.info('Found ' + str(len(laneParentDict)) + ' samples on the flow cell.')
  return laneParentDict


def convertSampleToDict(foundFlowCell, configMap):
  '''
  converts <type 'ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample'> to a python dict
  '''
  flowCellDict = {}
  fcProperties = foundFlowCell.getProperties()
  for property in fcProperties:
    flowCellDict[property] = fcProperties.get(property)
  flowCellDict['Name'] = foundFlowCell.getIdentifier().split('/')[-1]
  flowCellDict['CODE'] = foundFlowCell.getCode()
  return flowCellDict

def getIndex(indx1, indx2, index1ReadLength, indexRead2Length, properties, configMap):
  if indx1 in properties and index1ReadLength > 0:
    index = properties[indx1][0:index1ReadLength]
  else:
    index = ''
  if indx2 in properties and indexRead2Length > 0:
    index = index + configMap['indexSeparator'] + properties[indx2][0:index1ReadLength]
  return index


def getSampleProperties(parentsKey, service, logger):
  for sample in parentsKey:
    sampleProperties = sample.getProperties()
    logger.debug(sample.getSampleTypeCode() + ' ' + sample.getCode())
    parentSamples = getParents(sample.getCode(), service)
    for parentSample in parentSamples:
      logger.debug(parentSample.getSampleTypeCode() + ' ' + parentSample.getCode())
      parentSampleProperties = parentSample.getProperties()

  return parentSample, parentSampleProperties


def createSampleSheetDict(configMap, control, sampleSheetDict, flowCellName, flowCellOperator,
                           end_type, cycles, lane, gaNumber, index, sample, sampleProperties):
  sampleSheetDict[lane + '_' + sample.getCode()] = [
    flowCellName + COMMA + configMap['lanePrefix'] + lane + COMMA + sample.getCode() + COMMA +
    sampleProperties[configMap['species']] + COMMA + index + COMMA +
    sanitizeString(sampleProperties[configMap['sampleName']]) + COMMA + control + COMMA +
    end_type + '_' + cycles + COMMA + flowCellOperator + COMMA + gaNumber]

def createHiseqSampleSheet(laneParentDict, flowCellDict, configMap, service, logger, myoptions):
  '''
    Builds up a dictionary with all entries in the Sample Sheet
  '''
  control = 'N'
  # the illlumina pipeline uses always one base less than the sequencer is sequencing
  demultiplexIndexLengthPenalty = 0

  sampleSheetDict = {}
  # Making sure this is on the top of the Sample Sheet
  sampleSheetDict[u'!'] = ([configMap['hiSeqHeader']])

  indx1 = configMap['index1Name']
  indx2 = configMap['index2Name']

  flowCellName = flowCellDict['CODE']
  flowCellOperator = flowCellDict[configMap['operator']]
  end_type = flowCellDict[configMap['endType']]
  cycles = flowCellDict[configMap['readLength']]
  index1ReadLength = int(flowCellDict[configMap['lengthIndex1']]) + demultiplexIndexLengthPenalty
  indexRead2Length = int(flowCellDict[configMap['lengthIndex2']]) + demultiplexIndexLengthPenalty

  for key in laneParentDict.keys():
    lane = laneParentDict[key]['LANE'][-1:]
    properties = laneParentDict[key]
    sampleName = laneParentDict[key]['LIBRARYID']

    # already Library with index
    if indx1 in properties:
      gaNumber = laneParentDict[key][configMap['gaNumber']]
      index = getIndex(indx1, indx2, index1ReadLength, indexRead2Length, properties, configMap)
      sample, sampleProperties = getSampleProperties(getParents(sampleName, service), service, logger)

      createSampleSheetDict(configMap, control, sampleSheetDict, flowCellName, flowCellOperator,
                          end_type, cycles, lane, gaNumber, index, sample, sampleProperties)
    else:
      for library in getParents(sampleName, service):
        libraryProperties = library.getProperties()
        gaNumber = libraryProperties[configMap['gaNumber']]
        if not gaNumber:
          logger.warning('No GA number found for ' + library.getCode())
        index = getIndex(indx1, indx2, index1ReadLength, indexRead2Length, libraryProperties, configMap)
        if not index:
          logger.warning('No index found for ' + library.getCode())
        sample, sampleProperties = getSampleProperties(getParents(library.getCode(), service), service, logger)

        createSampleSheetDict(configMap, control, sampleSheetDict, flowCellName, flowCellOperator,
                          end_type, cycles, lane, gaNumber, index, sample, sampleProperties)

  logger.debug(sampleSheetDict)
  sortedSampleSheetList = sampleSheetDict.keys()
  sortedSampleSheetList.sort()
  writeSampleSheet(flowCellName, sampleSheetDict, sortedSampleSheetList, myoptions, logger, fileName=myoptions.outdir +
                      configMap['sampleSheetFileName'])

def writeSampleSheet(flowCellName, sampleSheetDict, sortedSampleSheetList, myoptions, logger, fileName):
  '''
  Write the given dictionary to a csv file
  '''
  newline = lineending[myoptions.lineending]
  myFile = fileName + '_' + flowCellName + '.csv'
  try:
    with open(myFile, 'w') as sampleSheetFile:
      for listElement in sortedSampleSheetList:
        sampleSheetFile.write(sampleSheetDict[listElement][0] + newline)

      logger.info('Writing file ' + myFile)
      print ('Written ' + myFile)
  except IOError:
    logger.error('File error: ' + str(err))
    print ('File error: ' + str(err))


def main():
  '''
  Main script
  '''
  logger = setUpLogger('log/')
  logger.info('Started Creation of Sample Sheet...')

  myoptions = parseOptions(logger)
  if myoptions.debug:
    logger.setLevel(logging.DEBUG)

  flowCellName = myoptions.flowcell
  configMap = readConfig(logger)
  service = login(configMap, logger)

  foundFlowCell, containedSamples = getFlowCell(configMap['illuminaFlowCellTypeName'], flowCellName, service, logger)
  flowCellName = foundFlowCell.getCode()
  flowCellDict = convertSampleToDict(foundFlowCell, configMap)

  laneParentDict = getContainedSampleProperties(logger, containedSamples, service)
  createHiseqSampleSheet(laneParentDict, flowCellDict, configMap, service, logger, myoptions)

  logout(service, logger)
  print('DONE')

if __name__ == "__main__":
    main()
