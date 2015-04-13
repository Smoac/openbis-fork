'''
@copyright:
2014 ETH Zuerich, CISD
    
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
expected incoming Name for NextSeq runs: 140729_NS500318_0002_AH0T25AGXX

@author:
Manuel Kohler
'''

import os
import shutil
import re
from time import *
from datetime import *
import xml.etree.ElementTree as etree
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.common.mail import EMailAddress

RUNPARAMETERS = 'RunParameters.xml'
RUNINFO = 'RunInfo.xml'
FLOWCELL_SPACE='/BSSE_FLOWCELLS/'
FLOWCELL_PROJECT='FLOWCELLS/'
EXPERIMENT_TYPE_CODE='HT_SEQUENCING'
CYCLES_VOCABULARY_NAME = "CYCLES" 

# Mapping between XML file naming and openBIS properties

RUNINFO_XML = {'LANECOUNT':'LaneCount', 'SURFACECOUNT':'SurfaceCount',
   'SWATHCOUNT':'SwathCount', 'TILECOUNT':'TileCount'}

PERSISTANT_KEY_MAP = "persistant_key_map"

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

def sendEmail(mailClient, fcId):

  replyTo = EMailAddress("manuel.kohler@bsse.ethz.ch")
  fromAddress = replyTo
  manuel = EMailAddress("manuel.kohler@bsse.ethz.ch")
  ina = EMailAddress("ina.nissen@bsse.ethz.ch")
  christian = EMailAddress("christian.beisel@bsse.ethz.ch")
  katja = EMailAddress("katja.eschbach@bsse.ethz.ch")
  philippe = EMailAddress("philippe.demougin@unibas.ch")
  elodie = EMailAddress("belodie@ethz.ch")

  mailClient.sendEmailMessage("Automatically created new NextSeq flow cell " + fcId  + " in openBIS", \
                "A new NextSeq run got started with flow cell: " + fcId , replyTo, fromAddress, ina, katja, christian, philippe, manuel, elodie);

# -----------------------------------------------------------------------------

def post_storage(context):
  mailClient = context.getGlobalState().getMailClient()
  results = context.getPersistentMap().get(PERSISTANT_KEY_MAP)
  sendEmail(mailClient, results[0]) 

# -----------------------------------------------------------------------------

def searchSample(transaction, sampleName):

  # Search for the sample and check if there is already sample with this fcId
  search_service = transaction.getSearchService()
  sc = SearchCriteria()
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sampleName));
  foundSamples = search_service.searchForSamples(sc)
  return foundSamples

# -----------------------------------------------------------------------------

def setFcProperty(searchId, dict, runInfo, newFlowCell):
  children = runInfo.getAllchildren(searchId)
  for element in (dict):
    if (element <> '') and (dict[element] <> ''):
      newFlowCell.setPropertyValue(element, children[0].attrib[dict[element]])

# -----------------------------------------------------------------------------

def setIndexLengths (readMap, newFlowCell):

  indexCount = 0
  readCount = 0

  print(readMap)
  for entry in readMap:
    if readMap[entry][1] == 'Y':
      indexCount += 1
    else:
      readCount += 1

  if readCount == 2:
    newFlowCell.setPropertyValue("END_TYPE", "PAIRED_END")
  else:
    newFlowCell.setPropertyValue("END_TYPE", "SINGLE_READ")

  try:
    if indexCount == 1:
      newFlowCell.setPropertyValue("INDEXREAD", readMap['2'][0])
    else:
      newFlowCell.setPropertyValue("INDEXREAD", '0')
  except:
    newFlowCell.setPropertyValue("INDEXREAD", '0') 

  try:
    if indexCount == 2:
      newFlowCell.setPropertyValue("INDEXREAD2", readMap['3'][0])
      newFlowCell.setPropertyValue("INDEXREAD", readMap['2'][0])
    else:
      newFlowCell.setPropertyValue("INDEXREAD2", '0')
  except:
    newFlowCell.setPropertyValue("INDEXREAD2", '0')

# -----------------------------------------------------------------------------

def sanitizeString(myString):
    return re.sub('[^A-Za-z0-9]+', '_', myString)

# -----------------------------------------------------------------------------

def get_version(my_path):
  return my_path[-4:-2]

# -----------------------------------------------------------------------------

def process(transaction):

  incoming = transaction.getIncoming()
  incomingPath = incoming.getAbsolutePath()

  name = incoming.getName()
  # ['120726', 'M00721', '0011', 'A000000000-A1FVF']
  # NextSeq: 140729_NS500318_0002_AH0T25AGXX
  
  runDate, MiseqID, runningNumber, trayAndFcId = name.split("_")
  tray = trayAndFcId[0]
  fcId = trayAndFcId[1:]
  transaction.getRegistrationContext().getPersistentMap().put(PERSISTANT_KEY_MAP, [fcId])

  # Parse the RunInfo.xml and RunParameters.xml
  runInfo = parseXmlFile(incomingPath + '/' + RUNINFO) 
  runParameters = parseXmlFile(incomingPath + '/' + RUNPARAMETERS)
  
  # get the number of lanes
  maxLanes = runInfo.getAllchildren('FlowcellLayout')[0].attrib[RUNINFO_XML['LANECOUNT']]

  foundSamples = searchSample(transaction, fcId)
  # if flow cell already exists then just get it for an update
  if foundSamples.size() > 0:
    print('Already found a Flow Cell with the following name: '+ name + ". Will re-set the properties...")
    newFlowCell = transaction.getSampleForUpdate(foundSamples[0].getSampleIdentifier())
  else:
  # Create a new Flow Cell
    newFlowCell = transaction.createNewSample(FLOWCELL_SPACE + fcId, "ILLUMINA_FLOW_CELL")
    
  exp = transaction.getExperiment(FLOWCELL_SPACE + FLOWCELL_PROJECT + datetime.now().strftime("%Y.%m"))
  if exp == None:
    exp = transaction.createNewExperiment(FLOWCELL_SPACE + FLOWCELL_PROJECT + datetime.now().strftime("%Y.%m"),
	  EXPERIMENT_TYPE_CODE)
  newFlowCell.setExperiment(exp)

  foundLanes = searchSample(transaction, fcId + ":1")
  if not foundLanes:
    # Create flow lanes, but we ignore the maxLanes, as we have 4 physical lanes but only one logical lane
    [registerFlowLane(lane, transaction, fcId, newFlowCell) for lane in range(1,int(1)+1)]

  run = runInfo.getAllchildren('Run')[0].attrib
  RTAversion = (runParameters.getAllchildren('RTAVersion'))[0].text
  addVocabularyTerm(transaction, "PIPELINE_VERSION", RTAversion)
  newFlowCell.setPropertyValue("ILLUMINA_PIPELINE_VERSION", RTAversion)
  
  runMode = sanitizeString(runParameters.getAllchildren('Chemistry')[0].text)
  addVocabularyTerm(transaction, "RUN_MODE_VOCABULARY", runMode)
  newFlowCell.setPropertyValue("RUN_MODE", runMode)

  recipe_folder = (runParameters.getAllchildren('RecipeFolder'))[0].text
  major_version = get_version(recipe_folder)
    
  # Reading out <FlowcellLayout LaneCount="1" SurfaceCount="1" SwathCount="1" TileCount="12" />
  setFcProperty('FlowcellLayout', RUNINFO_XML, runInfo, newFlowCell)

  sequencer = runInfo.getAllchildren('Instrument')
  newFlowCell.setPropertyValue("SEQUENCER", sequencer[0].text)
  newFlowCell.setPropertyValue("FLOW_CELL_SEQUENCED_ON", createOpenbisTimeStamp(incomingPath + '/' + RUNPARAMETERS))
  newFlowCell.setPropertyValue("RUN_NAME_FOLDER", name)
  newFlowCell.setPropertyValue("SBS_KIT", major_version)

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
  addVocabularyTerm(transaction, CYCLES_VOCABULARY_NAME, numberOfCycles)
  newFlowCell.setPropertyValue("CYCLES_REQUESTED_BY_CUSTOMER", numberOfCycles)

  setIndexLengths (readMap, newFlowCell)
  
