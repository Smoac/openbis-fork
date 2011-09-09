'''
expected incoming Name for HiSeq2000 runs: 110715_SN792_0054_BC035RACXX
expected incoming Name for GAII runs: 110812_6353WAAXX

Note: 
print statements go to: ~openbis/sprint/datastore_server/log/startup_log.txt
'''

import os
from time import *
from datetime import *
import xml.etree.ElementTree as etree
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

IS_HISEQ_RUN=False
RUNPARAMETERS = 'runParameters.xml'
RUNINFO = 'RunInfo.xml'
FLOWCELL_SPACE='/BSSE_FLOWCELLS/'
FLOWCELL_PROJECT='FLOWCELLS/'
EXPERIMENT_TYPE_CODE='HT_SEQUENCING'

# Mapping between XML file naming and used in here
RUNPARAMETERS_XML = {'FLOWCELL':'Flowcell', 'RTAVERSION':'RTAVersion',
  'CONTROLLANE':'ControlLane', 'SBS':'Sbs', 'INDEX':'Index',
  'CYCLES_REQUESTED_BY_CUSTOMER':'Read1', 'PE':'Pe'}
RUNINFO_XML = {'LANECOUNT':'LaneCount', 'SURFACECOUNT':'SurfaceCount',
   'SWATHCOUNT':'SwathCount', 'TILECOUNT':'TileCount'}
INSTRUMENT = {'SN792':'RUA', 'BS-DSU-ELLAC':'ELLAC'}

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

# Create a "transaction" -- a way of grouping operations together so they all
# happen or none of them do.
transaction = service.transaction()

incomingPath = incoming.getAbsolutePath()

# Get the incoming name 
name = incoming.getName()

split=name.split("_")
if (len(split) == 4):
  IS_HISEQ_RUN=True
if (len(split) == 2):
  pass

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
 
if IS_HISEQ_RUN:
  run = runInfo.getAllchildren('Run')[0].attrib
  if (run['Id'] != name):
    raise NameError('Flowcell names do not match between directory name '+ name +
          ' and ' + RUNINFO + 'property file: ' + run['Id'])

  # The HiSeq is providing more infos, which we will parse here:
  runParameters = parseXmlFile(incomingPath + '/' + RUNPARAMETERS)
  
  newFlowCell.setPropertyValue("ILLUMINA_PIPELINE_VERSION", runParameters.getXmlElement(RUNPARAMETERS_XML['RTAVERSION']))
  newFlowCell.setPropertyValue("FLOWCELLTYPE", runParameters.getXmlElement(RUNPARAMETERS_XML['FLOWCELL']))
  newFlowCell.setPropertyValue("CONTROL_LANE", runParameters.getXmlElement(RUNPARAMETERS_XML['CONTROLLANE']))
  newFlowCell.setPropertyValue("SBS_KIT", runParameters.getXmlElement(RUNPARAMETERS_XML['SBS']))
  
  read1 = runParameters.getAllchildren('Read1')
  newFlowCell.setPropertyValue("CYCLES_REQUESTED_BY_CUSTOMER", read1[0].text)
  
  read2 = runParameters.getAllchildren('Read2')
  if (str(read2[0].text) == '0'):
    newFlowCell.setPropertyValue("END_TYPE", "SINGLE_READ") 
  else:
    newFlowCell.setPropertyValue("END_TYPE", "PAIRED_END") 
    newFlowCell.setPropertyValue("PAIRED_END_KIT", runParameters.getXmlElement(RUNPARAMETERS_XML['PE']))
  
  indexRead = runParameters.getAllchildren('IndexRead')
  newFlowCell.setPropertyValue("INDEXREAD", indexRead[0].text) 

  def setFcProperty(searchId, dict):
    children = runInfo.getAllchildren(searchId) 
    for element in (dict):
      if (element <> '') and (dict[element] <> ''):
        newFlowCell.setPropertyValue(element, children[0].attrib[dict[element]])

  setFcProperty('FlowcellLayout', RUNINFO_XML)


sequencer = runInfo.getAllchildren('Instrument')
newFlowCell.setPropertyValue("SEQUENCER", INSTRUMENT[sequencer[0].text])

newFlowCell.setPropertyValue("FLOW_CELL_SEQUENCED_ON", create_openbis_timestamp())
if IS_HISEQ_RUN:
  maxLanes = runInfo.getAllchildren('FlowcellLayout')[0].attrib[RUNINFO_XML['LANECOUNT']]
else:
  maxLanes = len(runInfo.getAllchildren('Tiles')[0])

# -----------------------------------------------------------------------------

def registerFlowLane(a_lane):
  '''
  Registers a new Flow lane 
  '''
  newFlowLane = transaction.createNewSample(FLOWCELL_SPACE + name + ':' + str(a_lane), "ILLUMINA_FLOW_LANE")
  newFlowLane.setContainer(newFlowCell)
  
[registerFlowLane(lane) for lane in range(1,int(maxLanes)+1)]
