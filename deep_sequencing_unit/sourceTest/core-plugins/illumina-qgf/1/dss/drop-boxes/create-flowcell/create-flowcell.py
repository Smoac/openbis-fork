'''
@copyright:
2015 ETH Zuerich, SIS
    
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
Parses the two Illumina provided files 'RunParameters.xml' / 'runParameters.xml
'and 'RunInfo.xml' and creates one Sample of type 'ILLUMINA_FLOW_CELL'
and sets Sample properties from those two XML files. Additionally the
number of lanes are read out and are created as contained samples of
type 'ILLUMINA_FLOW_LANE'.

@note: 
print statements go to: <openBIS_HOME>/datastore_server/log/startup_log.txt

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
ALTERNATIVE_RUNPARAMETERS = 'runParameters.xml'
RUNINFO = 'RunInfo.xml'
FLOWCELL_SPACE='/BSSE_FLOWCELLS/'
FLOWCELL_PROJECT='FLOWCELLS/'
EXPERIMENT_TYPE_CODE='HT_SEQUENCING'
CYCLES_VOCABULARY_NAME = "CYCLES" 

# Mapping between XML file naming and openBIS properties

RUNINFO_XML = {'LANECOUNT':'LaneCount', 'SURFACECOUNT':'SurfaceCount',
   'SWATHCOUNT':'SwathCount', 'TILECOUNT':'TileCount'}
RUNPARAMETERS_XML = {'FLOWCELL':'Flowcell', 'RTAVERSION':'RTAVersion',
  'CONTROLLANE':'ControlLane', 'SBS':'Sbs', 'INDEX':'Index',
  'CYCLES_REQUESTED_BY_CUSTOMER':'Read1', 'PE':'Pe', 'RUN_MODE': 'RunMode',
  'CONTROL_SOFTWARE_VERSION': 'ApplicationVersion'}

PERSISTENT_KEY_MAP = "persistent_key_map"

SEQUENCER_DICT = {'HISEQ_4000': 'Illumina HiSeq 4000',
                  'HISEQ_3000': 'Illumina HiSeq 3000',
                  'HISEQ_2500': 'Illumina HiSeq 2500',
                  'HISEQ_2000': 'Illumina HiSeq 2000',
                  'HISEQ_X': 'Illumina HiSeq X',
                  'NEXTSEQ_500': 'Illumina NextSeq 500',
                  'MISEQ': 'Illumina MiSeq',
                  'UNIDENTIFIED': 'Unidentified'}


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
        model = SEQUENCER_DICT['NEXTSEQ_500']
    elif machine_id.startswith("M"):
        model = SEQUENCER_DICT['MISEQ']
    elif machine_id.startswith("D"):
        model = SEQUENCER_DICT['HISEQ_2500']
    elif machine_id.startswith("SN"):
        model = SEQUENCER_DICT['HISEQ_2000']
    elif machine_id.startswith("J"):
        model = SEQUENCER_DICT['HISEQ_3000']
    elif machine_id.startswith("K"):
        model = SEQUENCER_DICT['HISEQ_4000']
    elif machine_id.startswith("ST"):
        model = SEQUENCER_DICT['HISEQ_X']
    else:
        model = SEQUENCER_DICT['UNIDENTIFIED']
    return model


def createOpenbisTimeStamp(file):
  '''
  Creates a openBIS compatible time stamp of a file time stamp
  '''
  mtime = os.path.getmtime(file)
  lt = localtime(mtime)
  tz = localtime().tm_hour - gmtime().tm_hour
  return (strftime("%Y-%m-%d %H:%M:%S GMT" + "%+.2d" % tz + ":00", lt))


def registerFlowLane(a_lane, transaction, name, newFlowCell):
  '''
  Registers a new Flow lane 
  '''
  newFlowLane = transaction.createNewSample(FLOWCELL_SPACE + name + ':' + str(a_lane), "ILLUMINA_FLOW_LANE")
  newFlowLane.setPropertyValue('CONCENTRATION_FLOWLANE', str(0))
  newFlowLane.setContainer(newFlowCell)


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


def send_email(mailClient, persistent_map):

    replyTo = EMailAddress("no-reply@bsse.ethz.ch")
    fromAddress = replyTo
    manuel = EMailAddress("manuel.kohler@id.ethz.ch")
    ina = EMailAddress("ina.nissen@bsse.ethz.ch")
    christian = EMailAddress("christian.beisel@bsse.ethz.ch")
    katja = EMailAddress("katja.eschbach@bsse.ethz.ch")
    philippe = EMailAddress("philippe.demougin@unibas.ch")
    elodie = EMailAddress("belodie@ethz.ch")

    subject = "Automatically created new " + persistent_map[1] + " flow cell " + persistent_map[0]  + " in openBIS"
    body = "A new run got started with flow cell: " + persistent_map [0] + "\nHave a good day!"
    mailClient.sendEmailMessage(subject, body, replyTo, fromAddress, ina, katja, christian,
                                 philippe, manuel, elodie);


def post_storage(context):
    mailClient = context.getGlobalState().getMailClient()
    persistent_map = context.getPersistentMap().get(PERSISTENT_KEY_MAP)
    send_email(mailClient, persistent_map) 


def searchSample(transaction, sampleName):

    # Search for the sample and check if there is already sample with this ID
    search_service = transaction.getSearchService()
    sc = SearchCriteria()
    sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sampleName));
    foundSamples = search_service.searchForSamples(sc)
    return foundSamples


def setFcProperty(searchId, my_dict, runInfo, newFlowCell):
    children = runInfo.getAllchildren(searchId)
    for element in (my_dict):
        if (element != '') and (my_dict[element] != ''):
            newFlowCell.setPropertyValue(element, children[0].attrib[my_dict[element]])


def set_index_lengths (readMap, newFlowCell):

    indexCount = 0
    readCount = 0

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


def sanitizeString(myString):
    return re.sub('[^A-Za-z0-9]+', '_', myString)


def get_version(my_path):
    return my_path[-4:-2]


def create_or_update_flowcell(transaction, fcId):

    found_samples = searchSample(transaction, fcId)
    # if flow cell already exists then just get it for an update
    if found_samples.size() > 0:
        print('Already found a Flow Cell with the following name: ' + fcId + ". Will re-set the properties...")
        new_flowcell = transaction.getSampleForUpdate(found_samples[0].getSampleIdentifier())
    else:
        new_flowcell = transaction.createNewSample(FLOWCELL_SPACE + fcId, "ILLUMINA_FLOW_CELL")
    exp = transaction.getExperiment(FLOWCELL_SPACE + FLOWCELL_PROJECT + datetime.now().strftime("%Y.%m"))
    if exp is None:
        exp = transaction.createNewExperiment(FLOWCELL_SPACE + FLOWCELL_PROJECT +
                                              datetime.now().strftime("%Y.%m"), EXPERIMENT_TYPE_CODE)
    new_flowcell.setExperiment(exp)
    return new_flowcell


def set_run_mode(transaction, new_flowcell, run_mode):
    addVocabularyTerm(transaction, "RUN_MODE_VOCABULARY", run_mode)
    new_flowcell.setPropertyValue("RUN_MODE", run_mode)

def process(transaction):

    incoming = transaction.getIncoming()
    incomingPath = incoming.getAbsolutePath()

    run_id = incoming.getName()
    model = get_model(run_id)
    print("Auto-detected Illumina model: " + model)

    run_date, sequencer_id, running_number, tray_and_fcId = run_id.split("_")
    tray = tray_and_fcId[0]
    if model is SEQUENCER_DICT['MISEQ']:
        fc_id = tray_and_fcId
    else:
        fc_id = tray_and_fcId[1:]
    transaction.getRegistrationContext().getPersistentMap().put(PERSISTENT_KEY_MAP, [fc_id, model])

    # Parse the RunInfo.xml and RunParameters.xml
    runInfo = parseXmlFile(os.path.join(incomingPath, RUNINFO)) 
    try:
        runParameters = parseXmlFile(os.path.join(incomingPath, RUNPARAMETERS))
    except:
        runParameters = parseXmlFile(os.path.join(incomingPath, ALTERNATIVE_RUNPARAMETERS))
  
    # get the number of lanes
    max_lanes = runInfo.getAllchildren('FlowcellLayout')[0].attrib[RUNINFO_XML['LANECOUNT']]
    new_flowcell = create_or_update_flowcell(transaction, fc_id)

    flow_lanes = new_flowcell.getContainedSamples()
    if len(flow_lanes) is 0:
        if model in SEQUENCER_DICT['NEXTSEQ_500']:
            max_lanes = 1
        [registerFlowLane(lane, transaction, fc_id, new_flowcell) for lane in range(1,int(max_lanes)+1)]

    # NextSeq specific
    if model in SEQUENCER_DICT['NEXTSEQ_500']:
        run_mode = sanitizeString(runParameters.getAllchildren('Chemistry')[0].text)
        set_run_mode(transaction, new_flowcell, run_mode)
        recipe_folder = (runParameters.getAllchildren('RecipeFolder'))[0].text
        major_version = get_version(recipe_folder)
        new_flowcell.setPropertyValue("SBS_KIT", major_version)
        new_flowcell.setPropertyValue("CONTROL_SOFTWARE_VERSION", runParameters.getAllchildren('ApplicationVersion')[0].text)

    # MiSeq specific
    if model in SEQUENCER_DICT['MISEQ']:
        ReagentKitBarcode = (runParameters.getAllchildren('ReagentKitBarcode'))[0].text
        new_flowcell.setPropertyValue("SBS_KIT", ReagentKitBarcode)
        new_flowcell.setPropertyValue("CONTROL_SOFTWARE_VERSION", runParameters.getAllchildren('ApplicationVersion')[0].text)

    # HiSeq specific
    if model in [SEQUENCER_DICT['HISEQ_2500'], SEQUENCER_DICT['HISEQ_3000'], SEQUENCER_DICT['HISEQ_4000'], SEQUENCER_DICT['HISEQ_X']]:
        run_mode = sanitizeString(runParameters.getXmlElement(RUNPARAMETERS_XML['RUN_MODE']))
        new_flowcell.setPropertyValue("FLOWCELLTYPE", runParameters.getXmlElement(RUNPARAMETERS_XML['FLOWCELL']))
        new_flowcell.setPropertyValue("SBS_KIT", runParameters.getXmlElement(RUNPARAMETERS_XML['SBS']))
        new_flowcell.setPropertyValue("CONTROL_SOFTWARE_VERSION", runParameters.getXmlElement(RUNPARAMETERS_XML['CONTROL_SOFTWARE_VERSION']))
        if (new_flowcell.getPropertyValue("END_TYPE")) == "PAIRED_END":
            new_flowcell.setPropertyValue("PAIRED_END_KIT", runParameters.getXmlElement(RUNPARAMETERS_XML['PE']))
        set_run_mode(transaction, new_flowcell, run_mode)

    rta_version = (runParameters.getAllchildren('RTAVersion'))[0].text
    addVocabularyTerm(transaction, "PIPELINE_VERSION", rta_version)
    new_flowcell.setPropertyValue("ILLUMINA_PIPELINE_VERSION", rta_version)

    # Reading out <FlowcellLayout LaneCount="1" SurfaceCount="1" SwathCount="1" TileCount="12" />
    setFcProperty('FlowcellLayout', RUNINFO_XML, runInfo, new_flowcell)

    sequencer = runInfo.getAllchildren('Instrument')
    addVocabularyTerm(transaction, "SEQUENCER", sequencer[0].text)
    new_flowcell.setPropertyValue("SEQUENCER", sequencer[0].text)
    new_flowcell.setPropertyValue("FLOW_CELL_SEQUENCED_ON", createOpenbisTimeStamp(os.path.join(incomingPath, RUNINFO)))
    new_flowcell.setPropertyValue("RUN_NAME_FOLDER", run_id)

    readMap = {}
    reads = runInfo.getAllchildren('Reads')
    read = reads[0].findall('Read')

    for r in read:
        cycles = r.get('NumCycles', 'str')
        number = r.get('Number', 'str')
        is_indexed = r.get('IsIndexedRead', 'str')
        readMap[number] = [cycles, is_indexed]

    # example of readMap: {'1': ['151', 'N'], '2': ['8', 'Y'], '3': ['8', 'Y'], '4': ['151', 'N']}
    number_of_cycles = readMap['1'][0]
    addVocabularyTerm(transaction, CYCLES_VOCABULARY_NAME, number_of_cycles)
    new_flowcell.setPropertyValue("CYCLES_REQUESTED_BY_CUSTOMER", number_of_cycles)

    set_index_lengths(readMap, new_flowcell)