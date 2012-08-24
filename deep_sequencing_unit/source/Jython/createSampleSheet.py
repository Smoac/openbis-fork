'''
@copyright: Copyright 2012 ETH Zuerich, CISD
 
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

@author:
Manuel Kohler

@description
Creates the SampleSheet.csv out of values from openBIS for Demultiplexing 
used in the Illumina pipeline (configureBclToFastq.pl) 

Runs under jython!
Takes into account to replace special characters with an underscore so that the Illumina script does not fail

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
from ConfigParser import SafeConfigParser
from optparse import OptionParser
from datetime import *

from ch.systemsx.cisd.openbis.dss.client.api.v1 import OpenbisServiceFacadeFactory
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria

def login(configMap):
  logging.info('Logging into ' + configMap['openbisServer'])
  service = OpenbisServiceFacadeFactory.tryCreate(configMap['openbisUserName'],
                                                  configMap['openbisPassword'],
                                                  configMap['openbisServer'],
                                                  configMap['connectionTimeout'])
  return service

def logout (service):
  service.logout()
  logging.info('Logged out')

def setUpLogger(logPath):
  d=datetime.now()
  logFileName = '/createSampleSheet_' + d.strftime("%Y-%m-%d_%H_%M_%S") + '.log'
  logging.basicConfig(filename=logPath + logFileName, format='%(asctime)s %(message)s', level=logging.DEBUG)

def parseConfigurationFile(propertyFile = 'etc/createSampleSheet.properties'):
  '''
  Parses the given config files and returns the values
  '''
  config = SafeConfigParser()
  config.read(propertyFile)
  config.sections()
  return config

def parseOptions():
  logging.info('Parsing command line parameters')
  usage = "usage: %prog [options]"
  parser = OptionParser(usage = usage, version="%prog 1.0")
  parser.add_option("-f", "--flowcell", dest = "flowcell",
                  help = "The flowcell which is used to create the SampleSheet.csv",
                  metavar = "FLOWCELL")
  parser.add_option("-m", "--mailist", dest = "maillist",
                  help = "Generated Sample Sheet will be addtionally sent as email to the defined list of recipients",
                  metavar = "MAILLIST")

  (options, args) = parser.parse_args()
  
  if options.flowcell is None:
    print usage
    exit(-1)
  return options

def readConfig():
  logging.info('Reading config file')
  configMap = {}
  
  configParameters = parseConfigurationFile()
  configMap['facilityName'] = configParameters.get('GENERAL', 'facilityName')
  configMap['facilityNameShort'] = configParameters.get('GENERAL', 'facilityNameShort')
  configMap['facilityInstitution'] = configParameters.get('GENERAL', 'facilityInstitution')
  configMap['mailList'] = configParameters.get('GENERAL', 'mailList')
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
  
  configMap['truSeqAdapter'] = configParameters.get('ILLUMINA', 'truSeqAdapter')
  configMap['nexteraAdapter'] = configParameters.get('ILLUMINA', 'nexteraAdapter')
  configMap['iemFileVersion'] = configParameters.get('ILLUMINA', 'iemFileVersion')
  return configMap

def getDate():
  d = datetime.now()
  return d.strftime("%A, %d of %B %Y")

def getVocabulary(vocabularyCode):
  ''' Returns the vocabulary terms and vocabulary labels of a vocabulary specified by the parameter
  vocabularyCode in a dictionary'''

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
  
  logging.info('Found ' + foundSample[0].getCode() + ' in openBIS')
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
 
def getSampleProperties(containedSamples):
  '''
  Takes a  list of contained samples, retrieves the parents and their properties and returns it
  as a dictionary. The key is the sample name, the value is a list of the properties
  '''
  parentDict = {}
  
  for lane in containedSamples:
    parents = getParents (lane.getCode())
    
    for parent in parents:
      parentCode = parent.getCode()
      parentProperties = parent.getProperties()
      
      propertyDict = {}
      for property in parentProperties:
        propertyDict[property] = parentProperties.get(property)
      
      propertyDict['LANE'] = lane.getCode()
      parentDict[parentCode] = propertyDict
  return parentDict


def writeSampleSheet(sampleSheetDict, sortedSampleSheetList, fileName):
  '''
  '''
  myFile = fileName + '_' + flowCellName + '.csv'
  try:
    with open(myFile, 'w') as sampleSheetFile:
      for listElement in sortedSampleSheetList:
        sampleSheetFile.write(sampleSheetDict[listElement][0] + '\n')
        
      logging.info('Writing file ' + myFile)
  except IOError:
    logging.error('File error: ' + str(err))
    print ('File error: ' + str(err))  
 

def convertSampleToDict(foundFlowCell):

  flowCellDict = {}
  fcProperties = foundFlowCell.getProperties()
  # convert <type 'ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample'> to a python dict
  for property in fcProperties:
    flowCellDict[property] = fcProperties.get(property)
  return flowCellDict

def sanitizeString(myString):
  return re.sub('[^A-Za-z0-9]+', '_', myString)

def createHiseqSampleSheet(parentDict):
  '''
  '''
  sampleSheetDict = {}
  sampleSheetDict[u'!'] = ([configMap['hiSeqHeader']])
  for key in parentDict.keys():
    if 'BARCODE' in parentDict[key]:
      index = parentDict[key]['BARCODE']
    else:
      index = ''
      
    if 'INDEX2' in parentDict[key]:
      index = index + configMap['indexSeparator'] + parentDict[key]['INDEX2']
  
    if parentDict[key]['LANE'][-1:] == controlLane:
      control = 'Y'
    else:
      control = 'N'
  
    lane = parentDict[key]['LANE'][-1:]
    sampleSheetDict[lane + '_' + key] = [flowCellName + ',' 
                            + lane + ',' 
                            + key + '_' + flowCellName + ','
                            + parentDict[key]['NCBI_ORGANISM_TAXONOMY'] + ','
                            + index + ','
                            + sanitizeString(parentDict[key]['EXTERNAL_SAMPLE_NAME']) + ',' 
                            + control + ','
                            + end_type + '_' + cycles + ',' 
                            + configMap['facilityInstitution'] + ',' 
                            + foundFlowCell.getCode() + '_' + lane
                            ]

  sortedSampleSheetList = sampleSheetDict.keys()
  sortedSampleSheetList.sort()
  writeSampleSheet(sampleSheetDict, sortedSampleSheetList, fileName = '../SampleSheet')

def createMiSeqSampleSheet(parentDict):
  '''
  '''
  sampleSheetDict = {}
  miSeqSections = []
  headerList = []
  
  separator = configMap['separator']
  
  miSeqHeaderSection = configMap['miSeqHeaderSection'].split(',')
  miSeqHeaderSection.reverse()
  headerList = [miSeqHeaderSection.pop()]
  headerList.append(miSeqHeaderSection.pop() + separator + configMap['iemFileVersion'])
  headerList.append(miSeqHeaderSection.pop() + separator )
  
  
  print headerList
  
  print sampleSheetDict
  print miSeqSections

'''
Main script
'''

setUpLogger('log')
logging.info('Started Creation of Sample Sheet...')

myoptions = parseOptions()
flowCellName = myoptions.flowcell
configMap = readConfig()
service = login(configMap)

foundFlowCell, containedSamples = getFlowCell(configMap['illuminaFlowCellTypeName'], flowCellName)
parentDict = getSampleProperties(containedSamples)
logging.info("Found " + str(len(parentDict)) + " samples on the flow cell " + flowCellName)

sampleSheetList = []
flowCellName = foundFlowCell.getCode().split('_')[3][1:]
flowCellDict = convertSampleToDict(foundFlowCell)

controlLane = flowCellDict['CONTROL_LANE']
end_type = flowCellDict['END_TYPE']
cycles = flowCellDict['CYCLES_REQUESTED_BY_CUSTOMER']

hiseqs = configMap['hiSeqNames'].split()
miseqs = configMap['miSeqNames'].split()

createHiseqSampleSheet(parentDict)
createMiSeqSampleSheet(parentDict)

#ncbi_tax =  parentDict['BSSE-QGF-7771']['NCBI_ORGANISM_TAXONOMY']
ncbiVocabulary = getVocabulary('NCBI_TAXONOMY')

logout(service)
print('DONE')
