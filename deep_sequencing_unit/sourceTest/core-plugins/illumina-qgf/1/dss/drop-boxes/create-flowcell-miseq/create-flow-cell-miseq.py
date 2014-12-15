'''
@copyright:
2012, 2013 ETH Zuerich, CISD
    
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

@description:
Parses the two Illumina provided files 'RunParameters.xml' and 'RunInfo.xml'
and creates one Sample of type 'ILLUMINA_FLOW_CELL' and sets Sample properties 
from those two XML files. Additionally the number of lanes are read out and
are created as contained samples of type 'ILLUMINA_FLOW_LANE'.

@note: 
print statements go to: <openBIS_HOME>/datastore_server/log/startup_log.txt
expected incoming Name for MiSeq runs: 120726_M00721_0011_A000000000-A1FVF

@author:
Manuel Kohler
'''

import os
import shutil
from time import *
from datetime import *
import xml.etree.ElementTree as etree
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.common.mail import EMailAddress

#RUNPARAMETERS = 'RunParameters.xml'
RUNPARAMETERS = 'runParameters.xml'
RUNINFO = 'RunInfo.xml'
FLOWCELL_SPACE='/BSSE_FLOWCELLS/'
FLOWCELL_PROJECT='FLOWCELLS/'
EXPERIMENT_TYPE_CODE='HT_SEQUENCING'

# Mapping between XML file naming and openBIS properties

RUNINFO_XML = {'LANECOUNT':'LaneCount', 'SURFACECOUNT':'SurfaceCount',
   'SWATHCOUNT':'SwathCount', 'TILECOUNT':'TileCount'}

#------------------------------------------------------------------------------

class parseXmlFile: 
    
  def __init__(self, xmlFile):
    self.xmlFile = xmlFile
    self.tree = etree.parse(self.xmlFile)
    self.root = self.tree.getroot()

  def getXmlElement (self, elementName):
    '''
    Returns the text value of a given XML element
    '''
    for e in self.root.getchildren():
      element = e.find(elementName)
      if element is None:
          return 'None'
      else:
          return element.text

  def getAllchildren (self, elementName):
    '''
    finds all children of a given XML Element and returns them as list
    '''
    for e in self.root.getchildren():
      # the '//' means look recursively for all children not only direct ones
      childList  = self.tree.findall('//' + elementName)
    return childList 

# -----------------------------------------------------------------------------

def create_openbis_timestamp (): 
  ''' 
  Create an openBIS conform timestamp
  '''
  tz=localtime()[3]-gmtime()[3]
  d=datetime.now()
  return d.strftime("%Y-%m-%d %H:%M:%S GMT"+"%+.2d" % tz+":00")

# -----------------------------------------------------------------------------

def createOpenbisTimeStamp(file):
  '''
  Creates a openBIS compatible time stamp of a file time stamp
  '''
  mtime = os.path.getmtime(file)
  lt = localtime(mtime)
  tz = localtime().tm_hour - gmtime().tm_hour
  return (strftime("%Y-%m-%d %H:%M:%S GMT" + "%+.2d" % tz + ":00", lt))

# -----------------------------------------------------------------------------

def registerFlowLane(a_lane, transaction, name, newFlowCell):
  '''
  Registers a new Flow lane 
  '''
  newFlowLane = transaction.createNewSample(FLOWCELL_SPACE + name + ':' + str(a_lane), "ILLUMINA_FLOW_LANE")
  newFlowLane.setPropertyValue('CONCENTRATION_FLOWLANE', str(0))
  newFlowLane.setContainer(newFlowCell)
  
# -----------------------------------------------------------------------------

def addVocabularyTerm(transaction, vocabularyName, vocabularyCode):
  modifiableVocabulary = transaction.getVocabularyForUpdate(vocabularyName)
  if not (modifiableVocabulary.containsTerm(vocabularyCode)):
    ordinals = []
    terms = modifiableVocabulary.getTerms()
    for term in terms:
      ordinals.append(term.getOrdinal())
            
    newOrdinal = max(ordinals)+1;

    newTerm = transaction.createNewVocabularyTerm();
    newTerm.setCode(vocabularyCode)
    newTerm.setOrdinal(newOrdinal)
    modifiableVocabulary.addTerm(newTerm)

# -----------------------------------------------------------------------------

def process(transaction):

  incoming = transaction.getIncoming()
  incomingPath = incoming.getAbsolutePath()

  name = incoming.getName()
  # ['120726', 'M00721', '0011', 'A000000000-A1FVF']
  runDate, MiseqID, runningNumber, trayAndFcId = name.split("_")
  tray = trayAndFcId[0]
  fcId = trayAndFcId[1:]

  # -----------------------------------------------------------------------------

  # Search for the sample and check if there is already sample with this name
  search_service = transaction.getSearchService()
  sc = SearchCriteria()
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, name));
  foundSamples = search_service.searchForSamples(sc)
  if foundSamples.size() > 0:
    raise NameError('Already found a Flow Cell with the following name: '+ name)  

  # Parse the RunInfo.xml file
  runInfo = parseXmlFile(incomingPath + '/' + RUNINFO) 

  # Create a new Flow Cell and set the experiment
  newFlowCell = transaction.createNewSample(FLOWCELL_SPACE + name, "ILLUMINA_FLOW_CELL")
  exp = transaction.getExperiment(FLOWCELL_SPACE + FLOWCELL_PROJECT + datetime.now().strftime("%Y.%m"))
  if exp == None:
    exp = transaction.createNewExperiment(FLOWCELL_SPACE + FLOWCELL_PROJECT + datetime.now().strftime("%Y.%m"),
	       EXPERIMENT_TYPE_CODE)
  newFlowCell.setExperiment(exp)
   
  run = runInfo.getAllchildren('Run')[0].attrib
  if (run['Id'] != name):
    raise NameError('Flowcell names do not match between directory name '+ name +
	  ' and ' + RUNINFO + 'property file: ' + run['Id'])

  runParameters = parseXmlFile(incomingPath + '/' + RUNPARAMETERS)
  RTAversion = (runParameters.getAllchildren('RTAVersion'))[0].text
  ReagentKitBarcode = (runParameters.getAllchildren('ReagentKitBarcode'))[0].text
  print RTAversion
  addVocabularyTerm(transaction, "PIPELINE_VERSION", RTAversion)
  newFlowCell.setPropertyValue("ILLUMINA_PIPELINE_VERSION", RTAversion)
  newFlowCell.setPropertyValue("SBS_KIT", ReagentKitBarcode)
    
  def setFcProperty(searchId, dict):
    children = runInfo.getAllchildren(searchId) 
    for element in (dict):
      if (element <> '') and (dict[element] <> ''):
          newFlowCell.setPropertyValue(element, children[0].attrib[dict[element]])
  
  # Reading out <FlowcellLayout LaneCount="1" SurfaceCount="1" SwathCount="1" TileCount="12" />
  setFcProperty('FlowcellLayout', RUNINFO_XML)

  sequencer = runInfo.getAllchildren('Instrument')
  newFlowCell.setPropertyValue("SEQUENCER", sequencer[0].text)

  readMap = {}
  reads = runInfo.getAllchildren('Reads')
  read = reads[0].findall('Read')

  for r in read:
    cycles = r.get('NumCycles', 'str')
    number = r.get('Number', 'str')
    isIndexed = r.get('IsIndexedRead', 'str')
    readMap[number] = [cycles, isIndexed]

    # example of readMap: {'1': ['151', 'N'], '2': ['8', 'Y'], '3': ['8', 'Y'], '4': ['151', 'N']}
    numberOfCycles = readMap['1'][0]   
    cyclesVocabularyName = "CYCLES" 

    addVocabularyTerm(transaction, cyclesVocabularyName, numberOfCycles)
         
    newFlowCell.setPropertyValue("CYCLES_REQUESTED_BY_CUSTOMER", numberOfCycles)
   
    indexCount = 0
    readCount = 0

    for e in readMap:
      if readMap[e][1] == 'Y':
	indexCount += 1
      else:
	readCount += 1

    if readCount == 2:
      newFlowCell.setPropertyValue("END_TYPE", "PAIRED_END")
    else:
      newFlowCell.setPropertyValue("END_TYPE", "SINGLE_READ")

    try:
      newFlowCell.setPropertyValue("INDEXREAD", readMap['2'][0])
    except:
      newFlowCell.setPropertyValue("INDEXREAD", '0') 

    try:
      if indexCount == 2:
        newFlowCell.setPropertyValue("INDEXREAD2", readMap['3'][0])
      else:
        newFlowCell.setPropertyValue("INDEXREAD2", '0')
    except:
      newFlowCell.setPropertyValue("INDEXREAD2", '0')

  newFlowCell.setPropertyValue("FLOW_CELL_SEQUENCED_ON", createOpenbisTimeStamp(incomingPath + '/' + RUNPARAMETERS))
  newFlowCell.setPropertyValue("RUN_NAME_FOLDER", name)

  # get the number of lanes
  maxLanes = runInfo.getAllchildren('FlowcellLayout')[0].attrib[RUNINFO_XML['LANECOUNT']]

  [registerFlowLane(lane, transaction, name, newFlowCell) for lane in range(1,int(maxLanes)+1)]

  replyTo = EMailAddress("manuel.kohler@bsse.ethz.ch")
  fromAddress = replyTo
  manuel = EMailAddress("manuel.kohler@bsse.ethz.ch")
  ina = EMailAddress("ina.nissen@bsse.ethz.ch")
  christian = EMailAddress("christian.beisel@bsse.ethz.ch")
  katja = EMailAddress("katja.eschbach@bsse.ethz.ch")

  transaction.getGlobalState().getMailClient().sendEmailMessage("Automatically created new flow cell " + name + " in openBIS", \
                "A new sequencing run got started using flow cell " + name, replyTo, fromAddress, ina, katja, christian, manuel);

  shutil.rmtree(incomingPath)
