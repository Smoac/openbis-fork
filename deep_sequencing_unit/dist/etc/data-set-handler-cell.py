'''
expected incoming Name for HiSeq2000 runs: 110715_SN792_0054_BC035RACXX
expected incoming Name for GAII runs: 110812_6353WAAXX

Note: 
print statements go to: ~openbis/sprint/datastore_server/log/startup_log.txt
'''

import os
import shutil
import glob
import xml.etree.ElementTree as etree
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

IS_HISEQ_RUN=False
RUNPARAMETERS = 'runParameters.xml'
XML_ELEMENTS = {'FLOWCELL': 'Flowcell', 'RTAVERSION': 'RTAVersion',
  'CONTROLLANE': 'ControlLane', 'SBS': 'Sbs', 'INDEX': 'Index'}

RUNINFO_FOLDER='/links/shared/dsu-dss/dss/incoming-jython-runinfostatistics/'
REGEX_RUNINFO_SAMPLE = '/Data/Status*'
REGEX_RUNINFO_REPORTS = '/Data/reports'
MARKER_STRING='.MARKER_is_finished_'


def parseXml(xmlFile):
  '''
  Parses an XML file and returns the root leaf  
  '''
  tree = etree.parse(xmlFile)
  root = tree.getroot()
  return root


def getXmlElement (elementName):
  '''
  Returns the text value of a given XML element
  '''
  for e in root.getchildren():
    element = e.find(elementName)
    if element is None:
      return 'None'
    else:
      print(element.text)
      return element.text

def touch_markerfile(filename):
  try:
    # do a touch
    open(filename, 'w').close()
  except:
    print('Could not touch ' + filename)


# Create a "transaction" -- a way of grouping operations together so they all
# happen or none of them do.
transaction = service.transaction()

incomingPath = incoming.getAbsolutePath()
folders=[]
folders=os.listdir(incomingPath)

# Get the incoming name 
name = incoming.getName()

split=name.split("_")
if (len(split) == 4):
  dataSet = transaction.createNewDataSet("ILLUMINA_HISEQ_OUTPUT")
  IS_HISEQ_RUN=True
if (len(split) == 2):
  dataSet = transaction.createNewDataSet("ILLUMINA_GA_OUTPUT")

#move RunInfo into a different drop box
runInfoSample=glob.glob(incomingPath + REGEX_RUNINFO_SAMPLE)
runInfoReport=glob.glob(incomingPath + REGEX_RUNINFO_REPORTS)
runInfoList = runInfoSample + runInfoReport
for runInfo in runInfoList:
  try:
    if os.path.isdir(runInfo):
      shutil.copytree(runInfo, RUNINFO_FOLDER + name + '/Data/' + os.path.basename(runInfo))
    else:
      shutil.copy2(runInfo, RUNINFO_FOLDER + name + '/Data/')
  except (IOError, os.error), why:
    print (runInfo, RUNINFO_FOLDER + name, str(why))

touch_markerfile(RUNINFO_FOLDER+MARKER_STRING+name)

# Create a data set and set type
dataSet.setMeasuredData(False)
  
# Get the search service
search_service = transaction.getSearchService()
 
# Search for the sample
sc = SearchCriteria()
sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, name));
foundSamples = search_service.searchForSamples(sc)

if foundSamples.size() > 0:
  if IS_HISEQ_RUN:
    sa = transaction.getSampleForUpdate(foundSamples[0].getSampleIdentifier())
    root = parseXml(incomingPath + '/' + RUNPARAMETERS)
#    sa.setPropertyValue("ILLUMINA_PIPELINE_VERSION", getXmlElement(XML_ELEMENTS['RTAVERSION']))
#    sa.setPropertyValue("FLOWCELLTYPE", getXmlElement(XML_ELEMENTS['FLOWCELL']))
#    sa.setPropertyValue("CONTROL_LANE", getXmlElement(XML_ELEMENTS['CONTROLLANE']))
  
  # Add the incoming file into the data set
  transaction.moveFile(incomingPath, dataSet)
  
  dataSet.setSample(foundSamples[0])
