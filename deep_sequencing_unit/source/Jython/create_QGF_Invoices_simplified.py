'''
Copyright 2012 ETH Zuerich, CISD
  
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author Manuel Kohler

@description:
Creates Excel-based invoices for the Quantitative Genomics Facility, D-BSSE, ETH Zurich

@attention:
Runs under Jython

@note:

'''

import os
import re
import sys
import logging
from datetime import *
from ConfigParser import SafeConfigParser
from optparse import OptionParser
from java.io import FileOutputStream
from org.apache.poi.hssf.usermodel import HSSFWorkbook
from org.apache.poi.poifs.filesystem import POIFSFileSystem
from org.apache.poi.xssf.usermodel import XSSFWorkbook
from ch.systemsx.cisd.openbis.dss.client.api.v1 import OpenbisServiceFacadeFactory
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria
from java.util import EnumSet
from java.util import TreeMap
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SampleFetchOption

excelFormats = {"xls": "HSSFWorkbook()" , "xlsx": "XSSFWorkbook()"}

columnHeadersMap = {"EXTERNAL_SAMPLE_NAME": "Sample Name",
                    "BARCODE": "Index",
                    "INDEX2": "Index2",
                    "PREPARED_BY" : "Prepared by",
                    "KIT" : "Kit",
                    "CONTACT_PERSON_NAME" : "Contact Person",
                    "NOTES" : "Notes",
                    "PRICE" : "Price"}

class uniqueRow():
  '''
  Little helper class which ensures the unique use of a row 
  '''
  def __init__(self):
    self.row = -1

  def getNextRow (self):
    self.row += 1
    return self.row

  def setRow(self, rowNumber):
    self.row = rowNumber
    return self.row

class uniqueColumn():
  '''
  Little helper class which ensures the unique use of a column 
  '''
  def __init__(self):
    self.column = -1

  def getCurrentColumn(self):
    return self.column

  def getNextColumn (self):
    self.column += 1
    return self.column

  def setColumn(self, columnNumber):
    self.column = columnNumber
    return self.column

def getDate():
  d = datetime.now()
  return d.strftime("%A, %d-%B-%Y")

def setFont(wb, configMap, fontSize=10):
  font = wb.createFont()
  font.setFontHeightInPoints(fontSize)
  font.setFontName(configMap["defaultFonts"])
  font.setItalic(False)
  font.setStrikeout(False)
  # Fonts are set into a style so create a new one to use.
  style = wb.createCellStyle()
  style.setFont(font)
  return style

def getVocabulary(service, vocabularyCode):
  ''' Returns the vocabulary term and vocabulary label of a vocabulary specified by the parameter
  vocabularyCode in a map'''

  vocabularies = service.listVocabularies()
  vocabularyMap = {}
  for vocabulary in vocabularies:
    if (vocabulary.getCode() == vocabularyCode):
      terms = vocabulary.getTerms()
  for term in terms:
    vocabularyMap[term.getCode()] = term.getLabel()
  return vocabularyMap

def writeExcel(myoptions, configMap, service, piName, laneDict, sampleDict, piDict,
                flowCellProperties, flowcellName, format="xls"):
  '''
  Writes out all data to an Excel file
  '''

  myRows = uniqueRow()
  sequencerVocabulary = getVocabulary(service, "SEQUENCER")
  setOfFlowcells = set ()
  runDate, seqId, runningNumber, flowcell = flowcellName.split("_")
  flowcell = flowcell[1:]

  def writeHeader():
    # Write header
    row = sheet.createRow(myRows.getNextRow())
    row.createCell(0).setCellValue(configMap["facilityName"] + ", " + configMap["facilityInstitution"])
    row.getCell(0).setCellStyle(setFont(wb, configMap, 14))
    row1 = sheet.createRow(myRows.getNextRow())
    row1.createCell(0).setCellValue(getDate())
    row1.getCell(0).setCellStyle(setFont(wb, configMap, 10))

  def createRow(key="", value="", rowNumber=0, fontSize=10):
    '''
    '''
    if rowNumber == 0:
      row = sheet.createRow(myRows.getNextRow())
    else:
      row = rowNumber
    row.createCell(0).setCellValue(key)
    row.createCell(1).setCellValue(value)
    row.getCell(0).setCellStyle(setFont(wb, configMap, fontSize))
    row.getCell(1).setCellStyle(setFont(wb, configMap, fontSize))
    return row

  def writeFooter(service, sheet):
    footer = sheet.getFooter()
    footer.setRight("generated on " + datetime.now().strftime("%H:%M - %d.%m.%Y"))

  wb = (eval(excelFormats[format]))
  createHelper = wb.getCreationHelper()
  sheet = wb.createSheet(configMap["facilityNameShort"])
  # 3/2 = 150 percent magnification when opening the workbook
  sheet.setZoom(3, 2)

  writeHeader()
  createRow("Principal Investigator", piName)

  createRow("Run Folder Name", flowcellName)
  createRow()

  myColumns = uniqueColumn()

  sampleHeader = sheet.createRow(myRows.getNextRow())
  sampleHeader.createCell(myColumns.getNextColumn()).setCellValue("Flow Cell:Lane")
  sampleHeader.getCell(myColumns.getCurrentColumn()).setCellStyle(setFont(wb, configMap, 10))
  sampleHeader.createCell(myColumns.getNextColumn()).setCellValue("Sample Code")
  sampleHeader.getCell(myColumns.getCurrentColumn()).setCellStyle(setFont(wb, configMap, 10))
  for c in columnHeadersMap:
    sampleHeader.createCell(myColumns.getNextColumn()).setCellValue(columnHeadersMap[c])
    sampleHeader.getCell(myColumns.getCurrentColumn()).setCellStyle(setFont(wb, configMap, 10))

  listofLanes = piDict[piName]
  for lane in listofLanes:
    singleSampleColumns = uniqueColumn()
    for sample in sampleDict[lane].keys():
      rowN = sheet.createRow(myRows.getNextRow())
      rowN.createCell(singleSampleColumns.getNextColumn()).setCellValue(flowcellName + ":" + str(lane))
      rowN.getCell(singleSampleColumns.getCurrentColumn()).setCellStyle(setFont(wb, configMap, 10))
      rowN.createCell(singleSampleColumns.getNextColumn()).setCellValue(sample)
      rowN.getCell(singleSampleColumns.getCurrentColumn()).setCellStyle(setFont(wb, configMap, 10))

      sampleValues = sampleDict[lane][sample]

      for column in columnHeadersMap.keys():
        rowN.createCell(singleSampleColumns.getNextColumn()).setCellValue(sampleValues[column])
        rowN.getCell(singleSampleColumns.getCurrentColumn()).setCellStyle(setFont(wb, configMap, 10))

      singleSampleColumns = uniqueColumn()

  createRow()
  createRow("Flow Cell Details", "", 0, 14)

  createRow("Flow Cell", flowcell)
  for property in flowCellProperties:
    if (property == "SEQUENCER"):
      val = sequencerVocabulary[flowCellProperties[property]]
    else:
      val = flowCellProperties[property]

    createRow(property, val)
  createRow()

  # adjust width
  for i in range(0, 20):
    sheet.autoSizeColumn(i)

  # set layout to landscape
  sheet.getPrintSetup().setLandscape(True)

  writeFooter(service, sheet)

  #  Write the output to a file
  fileName = myoptions.outdir + configMap["facilityNameShort"] + "_" + flowcell + "_" + \
            sanitizeString(piName) + datetime.now().strftime("_%d_%m_%Y.") + format
  fileOut = FileOutputStream(fileName)
  print fileName

  wb.write(fileOut);
  fileOut.close();

def sanitizeString(myString):
  return re.sub('[^A-Za-z0-9]+', '_', myString)

def setUpLogger(logPath, logLevel=logging.INFO):
  logFileName = 'createInvoices'
  d = datetime.now()
  logFileName = logFileName + '_' + d.strftime('%Y-%m-%d_%H_%M_%S') + '.log'
  logging.basicConfig(filename=logPath + logFileName,
                      format='%(asctime)s [%(levelname)s] %(message)s', level=logLevel)
  logger = logging.getLogger(logFileName)
  return logger


def parseConfigurationFile(propertyFile='etc/service.properties'):
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
  EXCEL = 'EXCEL'

  logger.info('Reading config file')
  configMap = {}

  configParameters = parseConfigurationFile()
  configMap['facilityName'] = configParameters.get(GENERAL, 'facilityName')
  configMap['facilityNameShort'] = configParameters.get(GENERAL, 'facilityNameShort')
  configMap['facilityInstitution'] = configParameters.get(GENERAL, 'facilityInstitution')
  configMap['mailList'] = configParameters.get(GENERAL, 'mailList')
  configMap['mailFrom'] = configParameters.get(GENERAL, 'mailFrom')
  configMap['smptHost'] = configParameters.get(GENERAL, 'smptHost')
  configMap['separator'] = configParameters.get(GENERAL, 'separator')
  configMap['indexSeparator'] = configParameters.get(GENERAL, 'indexSeparator')

  configMap['openbisUserName'] = configParameters.get(OPENBIS, 'openbisUserName')
  configMap['openbisPassword'] = configParameters.get(OPENBIS, 'openbisPassword', raw=True)
  configMap['openbisServer'] = configParameters.get(OPENBIS, 'openbisServer')
  configMap['connectionTimeout'] = configParameters.getint(OPENBIS, 'connectionTimeout')
  configMap['pIPropertyName'] = configParameters.get(OPENBIS, 'pIPropertyName')

  configMap['defaultFonts'] = configParameters.get(EXCEL, 'defaultFonts')

  return configMap

def login(logger, configMap):
  logger.info('Logging into ' + configMap['openbisServer'])
  service = OpenbisServiceFacadeFactory.tryCreate(configMap['openbisUserName'],
                                                  configMap['openbisPassword'],
                                                  configMap['openbisServer'],
                                                  configMap['connectionTimeout'])
  return service

def parseOptions(logger):
  logger.info('Parsing command line parameters')
  parser = OptionParser(version='%prog 1.0')
  parser.add_option('-f', '--flowcell',
                  dest='flowcell',
                  help='The flowcell which is used to create the SampleSheet.csv',
                  metavar='<flowcell>')
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


def getFLowcellData(service, configMap, flowcell, logger):

  fetchOptions = EnumSet.of(SampleFetchOption.ANCESTORS, SampleFetchOption.PROPERTIES)
  laneFetchOptions = EnumSet.of(SampleFetchOption.ANCESTORS, SampleFetchOption.PROPERTIES)

  sc = SearchCriteria();
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, flowcell));
  fcList = service.searchForSamples(sc, fetchOptions)

  for p in fcList:
    flowCellProperties = p.getProperties()

  numberOfLanes = int(flowCellProperties['LANECOUNT'])

  laneDict = {}
  sampleDict = {}
  piDict = {}

  for lane in range(1, numberOfLanes + 1):
    myLane = flowcell + ":" + str(lane)
    laneSc = SearchCriteria();
    laneSc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, myLane));
    laneList = service.searchForSamples(laneSc, fetchOptions)

    for l in laneList:
      laneProperties = l.getProperties()
      laneDict[lane] = laneProperties
      laneParents = l.getParents()

      s = {}
      for samples in laneParents:
        sampleCode = samples.getCode()
        sampleProperties = samples.getProperties()
        s[sampleCode] = sampleProperties
        sampleDict[lane] = s
        pi = sampleProperties[configMap["pIPropertyName"]]

      if piDict.has_key(pi):
        piDict[pi].append(lane)
      else:
        piDict[pi] = [lane]

  logger.info("Found the following PIs on the lanes: ")
  logger.info(piDict)

  # simply sort the hashmap
  treeMap = TreeMap (flowCellProperties)
  return laneDict, sampleDict, piDict, treeMap


'''
Main script
'''

def main():

  # for now setting the format by hand
  format = "xlsx"

  logger = setUpLogger('log/')
  logger.info('Started Creation Invoices...')

  myoptions = parseOptions(logger)
  configMap = readConfig(logger)

  service = login(logger, configMap)
  flowcellName = myoptions.flowcell
  laneDict, sampleDict, piDict, flowCellProperties = getFLowcellData(service, configMap, flowcellName, logger)

  for piName in piDict:
    # create an Excel file for each PI
    writeExcel(myoptions, configMap, service, piName, laneDict, sampleDict, piDict,
               flowCellProperties, flowcellName, format)

  service.logout()

if __name__ == "__main__":
    main()
