'''
@copyright:
2012 ETH Zuerich, CISD
    
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
Parses the two Illumina provided files 'runParameters.xml' and 'RunInfo.xml'
and creates one Sample of type 'ILLUMINA_FLOW_CELL' and sets Sample properties 
from those two XML files. Additionally the number of lanes are read out and
are created as contained samples of type 'ILLUMINA_FLOW_LANE'.

@note: 
print statements go to: <openBIS_HOME>/datastore_server/log/startup_log.txt
expected incoming Name for HiSeq runs: 110715_SN792_0054_BC035RACXX
expected incoming Name for GAII runs: 110812_6353WAAXX

@author:
Manuel Kohler
'''

import os
import shutil
import  re
from time import *
from datetime import *
import xml.etree.ElementTree as etree
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

IS_HISEQ_RUN=False
RUNPARAMETERS = 'runParameters.xml'
RUNINFO = 'RunInfo.xml'
FLOWCELL_SPACE='BSSE_FLOWCELLS'
FLOWCELL_PROJECT='FLOWCELLS'
EXPERIMENT_TYPE_CODE='HT_SEQUENCING'

FLOWCELL_PROJECT_ID = "/%(FLOWCELL_SPACE)s/%(FLOWCELL_PROJECT)s/" % vars()

# Mapping between XML file naming and used in here
RUNPARAMETERS_XML = {'FLOWCELL':'Flowcell', 'RTAVERSION':'RTAVersion',
  'CONTROLLANE':'ControlLane', 'SBS':'Sbs', 'INDEX':'Index',
  'CYCLES_REQUESTED_BY_CUSTOMER':'Read1', 'PE':'Pe', 'RUN_MODE': 'RunMode'}
RUNINFO_XML = {'LANECOUNT':'LaneCount', 'SURFACECOUNT':'SurfaceCount',
   'SWATHCOUNT':'SwathCount', 'TILECOUNT':'TileCount'}

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

def registerFlowLane(transaction, a_lane, name, newFlowCell):
    '''
    Registers a new Flow lane 
    '''
    newFlowLane = transaction.createNewSample('/' + FLOWCELL_SPACE + '/' + name + ':' + str(a_lane), "ILLUMINA_FLOW_LANE")
    newFlowLane.setContainer(newFlowCell)
    newFlowLane.setPropertyValue('CONCENTRATION_FLOWLANE', str(0))

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

def sanitizeString(myString):
    return re.sub('[^A-Za-z0-9]+', '_', myString)

# -----------------------------------------------------------------------------

def setFcProperty(searchId, dict, newFlowCell, runInfo):
    children = runInfo.getAllchildren(searchId) 
    for element in (dict):
        if (element <> '') and (dict[element] <> ''):
            newFlowCell.setPropertyValue(element, children[0].attrib[dict[element]])

# -----------------------------------------------------------------------------

def process(transaction):
    incoming = transaction.getIncoming()
    incomingPath = incoming.getAbsolutePath()
    space = None

    # Get the incoming name 
    name = incoming.getName()
    split=name.split("_")

    # Parse the RunInfo.xml file
    runInfo = parseXmlFile(incomingPath + '/' + RUNINFO) 
    maxLanes = runInfo.getAllchildren('FlowcellLayout')[0].attrib[RUNINFO_XML['LANECOUNT']]

    # Search for the sample and check if there is already sample with this name
    search_service = transaction.getSearchService()
    sc = SearchCriteria()
    sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, name));
    foundSamples = search_service.searchForSamples(sc)
    if foundSamples.size() > 0:
        print('Already found a Flow Cell with the following name: '+ name + ". Will re-set the properties...")
        newFlowCell = transaction.getSampleForUpdate(foundSamples[0].getSampleIdentifier())
    else:
        # Create a new Flow Cell and set the experiment
        print ("Creating new Flow Cell " + name)
        
        newFlowCell = transaction.createNewSample('/' + FLOWCELL_SPACE + '/' + name, "ILLUMINA_FLOW_CELL")
        exp = transaction.getExperiment(FLOWCELL_PROJECT_ID + datetime.now().strftime("%Y.%m"))
        if exp == None:
            exp = transaction.createNewExperiment(FLOWCELL_PROJECT_ID + datetime.now().strftime("%Y.%m"),
                                                  EXPERIMENT_TYPE_CODE)
        newFlowCell.setExperiment(exp)
        [registerFlowLane(transaction, lane, name, newFlowCell) for lane in range(1,int(maxLanes)+1)]

    run = runInfo.getAllchildren('Run')[0].attrib
    if (run['Id'] != name):
      raise NameError('Flowcell names do not match between directory name '+ name +
            ' and ' + RUNINFO + 'property file: ' + run['Id'])

    # The HiSeq is providing more infos, which we will parse here:
    runParameters = parseXmlFile(incomingPath + '/' + RUNPARAMETERS)
    addVocabularyTerm(transaction, "PIPELINE_VERSION", runParameters.getXmlElement(RUNPARAMETERS_XML['RTAVERSION']))
    
    newFlowCell.setPropertyValue("ILLUMINA_PIPELINE_VERSION", runParameters.getXmlElement(RUNPARAMETERS_XML['RTAVERSION']))
    newFlowCell.setPropertyValue("FLOWCELLTYPE", runParameters.getXmlElement(RUNPARAMETERS_XML['FLOWCELL']))
    newFlowCell.setPropertyValue("CONTROL_LANE", runParameters.getXmlElement(RUNPARAMETERS_XML['CONTROLLANE']))
    newFlowCell.setPropertyValue("SBS_KIT", runParameters.getXmlElement(RUNPARAMETERS_XML['SBS']))
    
    runMode = sanitizeString(runParameters.getXmlElement(RUNPARAMETERS_XML['RUN_MODE']))
    addVocabularyTerm(transaction, "RUN_MODE_VOCABULARY", runMode)
    
    newFlowCell.setPropertyValue("RUN_MODE", runMode)
    newFlowCell.setPropertyValue("RUN_NAME_FOLDER", name)

    numberOfCycles = runParameters.getAllchildren('Read1')[0].text
    cyclesVocabularyName = "CYCLES" 
    addVocabularyTerm(transaction, cyclesVocabularyName, numberOfCycles)
    newFlowCell.setPropertyValue("CYCLES_REQUESTED_BY_CUSTOMER", numberOfCycles)
    
    read2 = runParameters.getAllchildren('Read2')
    if (str(read2[0].text) == '0'):
      newFlowCell.setPropertyValue("END_TYPE", "SINGLE_READ") 
    else:
      newFlowCell.setPropertyValue("END_TYPE", "PAIRED_END") 
      newFlowCell.setPropertyValue("PAIRED_END_KIT", runParameters.getXmlElement(RUNPARAMETERS_XML['PE']))
    
    indexRead1 = runParameters.getAllchildren('IndexRead1')
    newFlowCell.setPropertyValue("INDEXREAD", indexRead1[0].text) 
    
    indexRead2 = runParameters.getAllchildren('IndexRead2')
    newFlowCell.setPropertyValue("INDEXREAD2", indexRead2[0].text) 
  
    setFcProperty('FlowcellLayout', RUNINFO_XML, newFlowCell, runInfo)

    sequencer = runInfo.getAllchildren('Instrument')
    newFlowCell.setPropertyValue("SEQUENCER", sequencer[0].text)
    newFlowCell.setPropertyValue("FLOW_CELL_SEQUENCED_ON", createOpenbisTimeStamp(incomingPath + '/' + RUNPARAMETERS))
