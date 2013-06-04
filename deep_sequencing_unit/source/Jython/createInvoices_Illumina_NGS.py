# -*- coding: utf-8 -*-
'''
Copyright 2013 ETH Zuerich, CISD
  
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
Creates Excel-based invoices for the Quantitative Genomics Facility,
D-BSSE, ETH Zurich

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
from ch.systemsx.cisd.openbis.generic.shared.dto import NewRoleAssignment

excelFormats = {"xls": "HSSFWorkbook()" , "xlsx": "XSSFWorkbook()"}

# This list is imply used to keep the order of elements of the 'columnHeadersMap'
columnHeadersList = ["EXTERNAL_SAMPLE_NAME", "BARCODE", "INDEX2", "CONTACT_PERSON_NAME",
                     "BIOLOGICAL_SAMPLE_ARRIVED", "PREPARED_BY", "KIT", "QC_AT_DBSSE",
                     "CELL_PLASTICITY_SYSTEMSX",  "PRICE", "NOTES"]

columnHeadersMap = {"EXTERNAL_SAMPLE_NAME": "Sample Name",
                    "BARCODE": "Index",
                    "INDEX2": "Index2",
                    "PREPARED_BY" : "Prepared by",
                    "KIT" : "Kit",
                    "QC_AT_DBSSE" : "QC at D-BSSE",
                    "CONTACT_PERSON_NAME" : "Contact Person",
                    "NOTES" : "Notes",
                    "BIOLOGICAL_SAMPLE_ARRIVED": "Received",
                    "CELL_PLASTICITY_SYSTEMSX": "Cell Plasticity",
                    "PRICE" : "Price"}

SAMPLETYPE = 'SAMPLETYPE'
EXTERNAL_SAMPLE_NAME = 'EXTERNAL_SAMPLE_NAME'
RUN_FOLDER_NAME = 'RUN_FOLDER_NAME'

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


class openbisRow ():
  '''
  Represents a single row in an Excel file with the relevant properties,
  code and pi are for internal organization 
  '''
  def __init__(self):
    self.code = 'code'
    self.pi = 'PRINCIPAL_INVESTIGATOR_NAME'
    self.externalSampleName = 'EXTERNAL_SAMPLE_NAME'
    self.index1 = 'INDEX1'
    self.index2 = 'INDEX2'
    self.preparedBy = 'PREPARED_BY'
    self.kit = 'KIT'
    self.qcAtBsse = 'QC_AT_DBSSE'
    self.contactPersonName = 'CONTACT_PERSON_NAME'
    self.notes = 'NOTES'
    self.biologicalSampleArrived = 'BIOLOGICAL_SAMPLE_ARRIVED'
    self.cellPlasticitySystemsX = ''
    self.price = ''
    
  def __str__(self):
    return "code: %s, pi: %s, externalSampleName: %s, index1: %s, index2: %s, preparedBy: %s, kit: %s," \
           " qcAtBsse: %s, contactPersonName: %s, notes: %s, biologicalSampleArrived: %s," \
           " cellPlasticitySystemsX: %s, price: %s" \
        % (self.code, self.pi, self.externalSampleName, self.index1, self.index2, self.preparedBy, self.kit,
           self.qcAtBsse, self.contactPersonName, self.notes, self.biologicalSampleArrived,
           self.cellPlasticitySystemsX, self.price)
    
def getDate():
  d = datetime.now()
  return d.strftime("%A, %d-%B-%Y")

def setFont(wb, configDict, fontSize=10):
  font = wb.createFont()
  font.setFontHeightInPoints(fontSize)
  font.setFontName(configDict["defaultFonts"])
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

def writeExcel(myoptions, configDict, service, piName, piDict,
               piSpace, flowCellProperties, flowcell, logger,
               listOfCodeList, listOfPropertyDicts, format="xls"):
  
  '''
  Writes out all data to an Excel file
  '''
  myRows = uniqueRow()
  sequencerVocabulary = getVocabulary(service, "SEQUENCER")
  setOfFlowcells = set ()

  def getValueOrEmptyString (key, myDict):
    if key in myDict:
      return myDict[key]
    else:
      return ''

  def writeHeader():
    # Write header
    row = sheet.createRow(myRows.getNextRow())
    row.createCell(0).setCellValue(configDict["facilityName"] + ", " + configDict["facilityInstitution"])
    row.getCell(0).setCellStyle(setFont(wb, configDict, 10))
    row1 = sheet.createRow(myRows.getNextRow())
    row1.createCell(0).setCellValue(getDate())
    row1.getCell(0).setCellStyle(setFont(wb, configDict, 10))

  def createRow(key="", value="", rowNumber=0, fontSize=10):
    '''
    '''
    if rowNumber == 0:
      row = sheet.createRow(myRows.getNextRow())
    else:
      row = rowNumber
    row.createCell(0).setCellValue(key)
    row.createCell(1).setCellValue(value)
    row.getCell(0).setCellStyle(setFont(wb, configDict, fontSize))
    row.getCell(1).setCellStyle(setFont(wb, configDict, fontSize))
    return row

  def writeFooter(service, sheet):
    footer = sheet.getFooter()
    footer.setRight("generated on " + datetime.now().strftime("%H:%M - %d.%m.%Y"))


  wb = (eval(excelFormats[format]))
  createHelper = wb.getCreationHelper()
  sheet = wb.createSheet(configDict["facilityNameShort"])
  # 3/2 = 150 percent magnification when opening the workbook
  sheet.setZoom(3, 2)

  writeHeader()
  createRow("Principal Investigator", piName.replace("_", " "))
  createRow("Data Space", piSpace)
  createRow("Run Folder Name", getValueOrEmptyString(RUN_FOLDER_NAME, flowCellProperties))
  createRow()

  myColumns = uniqueColumn()

  sampleHeader = sheet.createRow(myRows.getNextRow())
  sampleHeader.createCell(myColumns.getNextColumn()).setCellValue("Flow Cell:Lane")
  sampleHeader.getCell(myColumns.getCurrentColumn()).setCellStyle(setFont(wb, configDict, 10))
  sampleHeader.createCell(myColumns.getNextColumn()).setCellValue("Sample Code")
  sampleHeader.getCell(myColumns.getCurrentColumn()).setCellStyle(setFont(wb, configDict, 10))
  for c in columnHeadersList:
    sampleHeader.createCell(myColumns.getNextColumn()).setCellValue(columnHeadersMap[c])
    sampleHeader.getCell(myColumns.getCurrentColumn()).setCellStyle(setFont(wb, configDict, 10))

  listofLanes = piDict[piName]
  listofLanes.sort()
  logger.debug(listofLanes)
  
  for lane in listofLanes:
    singleSampleColumns = uniqueColumn()
    
    sampleCodeForInvoicing = []
 
    for code in listOfCodeList[lane-1]:
      logger.info ('Processing: ' + code)
      sampleProperties = listOfPropertyDicts[lane-1][code]
      
      if (sampleProperties[SAMPLETYPE] == configDict['masterTypeName']):
        newRow.biologicalSampleArrived =  getValueOrEmptyString(newRow.biologicalSampleArrived, sampleProperties)
        newRow.contactPersonName = getValueOrEmptyString(newRow.contactPersonName, sampleProperties)
        newRow.pi = sanitizeString(getValueOrEmptyString(newRow.pi, sampleProperties))

        if (newRow.pi != piName):
          logger.info('This is a shared lane.Lane PI and sample PI are not the same. We do not write ' 
                       'the row to the Excel file')
          continue
             
        logger.info('Excel Sheet line: ')
        logger.info(newRow)
        
        rowN = sheet.createRow(myRows.getNextRow())
        rowN.createCell(singleSampleColumns.getNextColumn()).setCellValue(flowcell + ":" + str(lane))
        rowN.getCell(singleSampleColumns.getCurrentColumn()).setCellStyle(setFont(wb, configDict, 10))
        
        #Code  Sample Name  Index  Index2  Contact Person  Received  Prepared by  Kit  QC at D-BSSE  Cell Plasticity  Price  Notes
        rowN.createCell(singleSampleColumns.getNextColumn()).setCellValue(newRow.code)
        rowN.getCell(singleSampleColumns.getCurrentColumn()).setCellStyle(setFont(wb, configDict, 10))
        
        rowN.createCell(singleSampleColumns.getNextColumn()).setCellValue(newRow.externalSampleName)
        rowN.getCell(singleSampleColumns.getCurrentColumn()).setCellStyle(setFont(wb, configDict, 10))
        
        rowN.createCell(singleSampleColumns.getNextColumn()).setCellValue(newRow.index1)
        rowN.getCell(singleSampleColumns.getCurrentColumn()).setCellStyle(setFont(wb, configDict, 10))
        
        rowN.createCell(singleSampleColumns.getNextColumn()).setCellValue(newRow.index2)
        rowN.getCell(singleSampleColumns.getCurrentColumn()).setCellStyle(setFont(wb, configDict, 10))

        rowN.createCell(singleSampleColumns.getNextColumn()).setCellValue(newRow.contactPersonName)
        rowN.getCell(singleSampleColumns.getCurrentColumn()).setCellStyle(setFont(wb, configDict, 10))

        rowN.createCell(singleSampleColumns.getNextColumn()).setCellValue(newRow.biologicalSampleArrived)
        rowN.getCell(singleSampleColumns.getCurrentColumn()).setCellStyle(setFont(wb, configDict, 10))

        rowN.createCell(singleSampleColumns.getNextColumn()).setCellValue(newRow.preparedBy)
        rowN.getCell(singleSampleColumns.getCurrentColumn()).setCellStyle(setFont(wb, configDict, 10))

        rowN.createCell(singleSampleColumns.getNextColumn()).setCellValue(newRow.kit)
        rowN.getCell(singleSampleColumns.getCurrentColumn()).setCellStyle(setFont(wb, configDict, 10))

        rowN.createCell(singleSampleColumns.getNextColumn()).setCellValue(newRow.qcAtBsse)
        rowN.getCell(singleSampleColumns.getCurrentColumn()).setCellStyle(setFont(wb, configDict, 10))
        
        rowN.createCell(singleSampleColumns.getNextColumn()).setCellValue(newRow.cellPlasticitySystemsX)
        rowN.getCell(singleSampleColumns.getCurrentColumn()).setCellStyle(setFont(wb, configDict, 10))
        
        rowN.createCell(singleSampleColumns.getNextColumn()).setCellValue(newRow.price)
        rowN.getCell(singleSampleColumns.getCurrentColumn()).setCellStyle(setFont(wb, configDict, 10))

        rowN.createCell(singleSampleColumns.getNextColumn()).setCellValue(newRow.notes)
        rowN.getCell(singleSampleColumns.getCurrentColumn()).setCellStyle(setFont(wb, configDict, 10))

      # if the LIBRARY has no External Name we try to take the one from the RAW sample  
      if (sampleProperties[SAMPLETYPE] == configDict['rawTypeName']):
        if not newRow.externalSampleName:
           newRow.externalSampleName = 'Raw Sample Name: ' + getValueOrEmptyString(EXTERNAL_SAMPLE_NAME, sampleProperties)
      
      if (sampleProperties[SAMPLETYPE] == configDict['libraryTypeName']):
        newRow = openbisRow()
        sampleCodeForInvoicing.append(code)
        
        newRow.code = code
        newRow.externalSampleName = getValueOrEmptyString(newRow.externalSampleName, sampleProperties)
        newRow.index1 = getValueOrEmptyString(newRow.index1, sampleProperties)
        newRow.index2 = getValueOrEmptyString(newRow.index2, sampleProperties)
        newRow.preparedBy = getValueOrEmptyString(newRow.preparedBy, sampleProperties)
        newRow.kit = getValueOrEmptyString(newRow.kit, sampleProperties)
        newRow.qcAtBsse = getValueOrEmptyString(newRow.qcAtBsse, sampleProperties)
        newRow.notes = getValueOrEmptyString(newRow.notes, sampleProperties)
      
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

  # sanitizeString(piName) + datetime.now().strftime("_%d_%m_%Y.") + format
  #  Write the output to a file
  fileName = myoptions.outdir + configDict["facilityNameShort"] + "_" + flowcell + "_" + \
            sanitizeString(piName) + "." + format
  fileOut = FileOutputStream(fileName)
  # need this print for use as an openBIS webapp
  print fileName

  wb.write(fileOut);
  fileOut.close();
  
  return sampleCodeForInvoicing

def sanitizeString(myString):
  myString = myString.replace(u'ä', 'ae')
  myString = myString.replace(u'ü', 'ue')
  myString = myString.replace(u'ö', 'oe')
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
  configDict = {}

  configParameters = parseConfigurationFile()
  configDict['facilityName'] = configParameters.get(GENERAL, 'facilityName')
  configDict['facilityNameShort'] = configParameters.get(GENERAL, 'facilityNameShort')
  configDict['facilityInstitution'] = configParameters.get(GENERAL, 'facilityInstitution')
  configDict['mailList'] = configParameters.get(GENERAL, 'mailList')
  configDict['mailFrom'] = configParameters.get(GENERAL, 'mailFrom')
  configDict['smptHost'] = configParameters.get(GENERAL, 'smptHost')
  configDict['separator'] = configParameters.get(GENERAL, 'separator')
  configDict['indexSeparator'] = configParameters.get(GENERAL, 'indexSeparator')

  configDict['openbisUserName'] = configParameters.get(OPENBIS, 'openbisUserName')
  configDict['openbisPassword'] = configParameters.get(OPENBIS, 'openbisPassword', raw=True)
  configDict['openbisServer'] = configParameters.get(OPENBIS, 'openbisServer')
  configDict['connectionTimeout'] = configParameters.getint(OPENBIS, 'connectionTimeout')
  configDict['pIPropertyName'] = configParameters.get(OPENBIS, 'pIPropertyName')
  configDict['invoiceName'] = configParameters.get(OPENBIS, 'invoiceName')
  configDict['sampleCodePrefix'] = configParameters.get(OPENBIS, 'sampleCodePrefix')
  
  configDict['illuminaFlowLaneTypeName'] = configParameters.get(OPENBIS, 'illuminaFlowLaneTypeName')
  configDict['libraryPoolTypeName'] = configParameters.get(OPENBIS, 'libraryPoolTypeName')
  configDict['libraryTypeName'] = configParameters.get(OPENBIS, 'libraryTypeName')
  configDict['rawTypeName'] = configParameters.get(OPENBIS, 'rawTypeName')
  configDict['masterTypeName'] = configParameters.get(OPENBIS, 'masterTypeName')

  configDict['defaultFonts'] = configParameters.get(EXCEL, 'defaultFonts')

  return configDict

def login(logger, configDict):
  logger.info('Logging into ' + configDict['openbisServer'])
  service = OpenbisServiceFacadeFactory.tryCreate(configDict['openbisUserName'],
                                                  configDict['openbisPassword'],
                                                  configDict['openbisServer'],
                                                  configDict['connectionTimeout'])
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



def getLaneAncestors(service, lane, logger):
  '''
  Gets all ancestors and corresponding properties of a sample
  '''
  fetchOptions = EnumSet.of(SampleFetchOption.ANCESTORS, SampleFetchOption.PROPERTIES)
  sc = SearchCriteria();
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, lane));
  dList = service.searchForSamples(sc, fetchOptions)

  def recursiveSamples(list, codeList, propertyDict):
    '''
    Recursively walks through the sample tree and write the sample codes
    to a list and the properties into a dictionary. The sample code is
    the key, the properties are the values
    '''
       
    for element in list:
      elementCode = element.getCode()
      codeList.append(elementCode)
      elementProperties = element.getProperties()
      
      tmpDict = {}
      
      for eP in elementProperties:
        tmpDict[eP] = elementProperties[eP]
        
      tmpDict[SAMPLETYPE] = element.getSampleTypeCode()
      tmpDict['SPACE'] = element.getSpaceCode()
      propertyDict[elementCode] = tmpDict

      elementParents = element.getParents()
      
      if (elementParents.size() > 0):
        recursiveSamples(elementParents, codeList, propertyDict)
 
  codeList = []
  propertyDict = {}
    
  recursiveSamples(dList, codeList, propertyDict)
  
  logger.debug(codeList)
  logger.debug(propertyDict)
  
  return codeList, propertyDict


def getFLowcellData(service, configDict, flowcell, logger):

  fetchOptions = EnumSet.of(SampleFetchOption.ANCESTORS, SampleFetchOption.PROPERTIES)

  sc = SearchCriteria();
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, flowcell));
  fcList = service.searchForSamples(sc, fetchOptions)
  
  try:
    assert fcList.size() == 1
  except AssertionError:
    print (str(fcList.size()) + ' flow cells found which match.')
    exit(1)

  for p in fcList:
    flowCellProperties = p.getProperties()

  numberOfLanes = int(flowCellProperties['LANECOUNT'])

  piDict = {}
  spaceDict = {}
  invoiceDict = {}
  listOfCodeList = []
  listOfPropertyDicts = []

  for lane in range(1, numberOfLanes + 1):
    myLane = flowcell + ":" + str(lane)
    codeList, propertyDict = getLaneAncestors(service, myLane, logger)
    listOfCodeList.append(codeList)
    listOfPropertyDicts.append(propertyDict)
    
    for code in codeList:
      sampleProperties = propertyDict[code]
      logger.debug ('Processing ' + code)
      
      if (sampleProperties[SAMPLETYPE] == configDict['libraryTypeName']):
        invoiceProperty = sampleProperties[configDict['invoiceName']]
        # if sample got created via Excel upload, the property could be not set, which is represented by None
        if (invoiceProperty is None):
          invoiceProperty = 'false'
        sentInvoice = {'true': True, 'false': False}.get(invoiceProperty.lower())
      
      if (sampleProperties[SAMPLETYPE] == configDict['masterTypeName']):
        pi = sanitizeString(sampleProperties[configDict['pIPropertyName']])
        logger.debug("PI for " + code + ": " + pi)
        logger.debug("Invoice sent for " + code + ": " + str(sentInvoice))
        
        if piDict.has_key(pi):
          piDict[pi].append(lane)
          # Making the lanes unique
          piDict[pi] = list(set(piDict[pi]))
        else:
          piDict[pi] = [lane]

        if not sentInvoice:
          if invoiceDict.has_key(pi):
            invoiceDict[pi].append(lane)
            # Making the lanes unique
            invoiceDict[pi] = list(set(invoiceDict[pi]))
          else:
            invoiceDict[pi] = [lane]

        spaceDict[pi] = sampleProperties['SPACE']
        
  logger.debug(spaceDict)

  logger.info("Found the following PIs on the lanes: ")
  logger.info(piDict)
  logger.info("Found the following PIs with non-invoiced samples : ")
  logger.info(invoiceDict)

  # simply sort the hashmap
  treeMap = TreeMap (flowCellProperties)
  return piDict, treeMap, spaceDict, invoiceDict, listOfCodeList, listOfPropertyDicts


'''
Main script
'''

def main():

  # for now setting the format by hand
  format = "xlsx"
  magicString = "@Invoice@"

  logger = setUpLogger('log/')
  logger.info('Started Creation Invoices...')

  myoptions = parseOptions(logger)
  configDict = readConfig(logger)
  if myoptions.debug:
    logger.setLevel(logging.DEBUG)

  service = login(logger, configDict)
  flowcellName = myoptions.flowcell
  piDict, flowCellProperties, spaceDict, invoiceDict, listOfCodeList, listOfPropertyDicts =\
             getFLowcellData(service, configDict, flowcellName, logger)

  piSampleCodeDict = {}

  for piName in piDict:
    # create an Excel file for each PI
    sampleCodeForInvoicing = writeExcel(myoptions, configDict, service, piName, piDict,
               spaceDict[piName], flowCellProperties, flowcellName, logger,
               listOfCodeList, listOfPropertyDicts, format)
    
    piSampleCodeDict[piName] = sampleCodeForInvoicing
  
  for invoicePi in invoiceDict: 
    tmpString = magicString + invoicePi
    for samplecode in  piSampleCodeDict[invoicePi]:
      tmpString = tmpString + '#' + samplecode
    print tmpString
  
  service.logout()

if __name__ == "__main__":
    main()
