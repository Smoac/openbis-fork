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
 '''

'''
@author Manuel Kohler

Creates Excel-based invoices for the Quantitative Genomics Facility, 
D-BSSE, ETH Zurich

'''

import os
import re
import sys
from datetime import *
from java.io import FileOutputStream
from org.apache.poi.hssf.usermodel import HSSFWorkbook
from org.apache.poi.poifs.filesystem import POIFSFileSystem
from org.apache.poi.xssf.usermodel import XSSFWorkbook
from ch.systemsx.cisd.openbis.dss.client.api.v1 import OpenbisServiceFacadeFactory
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria


facilityName = "Quantitative Genomics Facility"
facilityInstitution = "D-BSSE, ETHZ"
facilityNameShort = "QGF"
subHeader = "Detailed Sample Overview"
PRINCIPAL_INVESTIGATOR = "Principal Investigator"
CONTACT_PERSON = "Contact person"
FLOW_CELL_ID = "Flow Cell ID"
defaultFonts = "Calibri"

excelFormats = {"xls": "HSSFWorkbook()" , "xlsx": "XSSFWorkbook()"}

columnHeaders = ["EXTERNAL_SAMPLE_NAME", "BARCODE", "BIOLOGICAL_SAMPLE_ARRIVED", "KIT_PREPARED",
                 "KIT", "FRAGMENT_SIZE_PREPARED_ILLUMINA", "CONCENTRATION_PREPARED_ILLUMINA",
                 "NOTES", "PRICE"]

columnHeadersMap = {"EXTERNAL_SAMPLE_NAME": "Sample Name",
                    "BARCODE": "Index", 
                    "BIOLOGICAL_SAMPLE_ARRIVED": "Received", 
                    "PREPARED_BY" : "Prepared by", 
                    "FRAGMENT_SIZE_PREPARED_ILLUMINA" : "Frag Size", 
                    "CONCENTRATION_PREPARED_ILLUMINA" : "Conc",
                    "NOTES" : "Notes",
                    "PRICE" : "Price"}

fcPropertyList = ["SEQUENCER", "FLOW_CELL_SEQUENCED_ON", "END_TYPE", "ILLUMINA_PIPELINE_VERSION",
                    "CYCLES_REQUESTED_BY_CUSTOMER", "FLOWCELLTYPE", "SBS_KIT"]

dataSetFileNameEntitySeparator = ":"

outputPath = "/local0/openbis/invoices"

invoicePropertyName = "INVOICE"
pIPropertyName = "PRINCIPAL_INVESTIGATOR_NAME"
contactPersonNamePropertyName = "CONTACT_PERSON_NAME"

'''
class invoiceDataSet:
  def __init__(self):
    self.code = ''
    self.externalSampleName = ''
    self.sampleID = ''
    self.biologicalSampleArrived = ''
    self.preparedBy = ''
    self.preparedDate = ''
    self.kitUsed = ''
    self.fragmentSizePrepared = ''
    self.concentrationPrepared = ''
    self.dataTransferred = ''
    self.price = ''
    

  def setDefaultHeader(self):
    code = "Sample code"
    externalSampleName = "External Sample Name" 
    sampleID = "Internal #"
    biologicalSampleArrived = "Received"
    preparedBy = "Prepared by"
    preparedDate = "Prepared on" 
    kitUsed = "Kit used"
    fragmentSizePrepared = "Fragment size prepared in bp"
    concentrationPrepared = "concentration prepared in ul/ng" 
    dataTransferred = "Data transferred"
    price = "Price excl. VAT"
''' 
    
class flowCell:
  def __init__(self):
    self.id = ''
    self.sequencer = ''
    self.flowCellSequencedOn = ''
    self.endType = ''
    self.illuminaPipelineVersion = ''
    self.cyclesRequestedByCustomer = ''
    self.flowCellType = ''
    self.sbsKit = '' 
     
    
    
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
  return d.strftime("%A, %d of %B %Y")

def setFont(wb, fontSize = 10):
  font = wb.createFont()
  font.setFontHeightInPoints(fontSize)
  font.setFontName(defaultFonts)
  font.setItalic(False)
  font.setStrikeout(False)
  # Fonts are set into a style so create a new one to use.
  style = wb.createCellStyle()
  style.setFont(font)
  return style


def getVocabulary(vocabularyCode):
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
  

def getNonbilledSamples (sampleType):
  '''Getting all RAW samples where no Invoice was sent '''

  sc = SearchCriteria();
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, sampleType));
  sc.addMatchClause(SearchCriteria.MatchClause.createPropertyMatch(invoicePropertyName, "false"));
  foundSamples = service.searchForSamples(sc)
  return foundSamples

def groupByPi (sampleList):
  principalInvestigatorDict = {}

  for sample in sampleList:
    pI = sample.getProperties().get(pIPropertyName)
    if principalInvestigatorDict.has_key(pI):
      principalInvestigatorDict[pI].append(sample)
    else: 
      principalInvestigatorDict[pI] = [sample]
  return principalInvestigatorDict

def groupByContactPerson (sampleList):
  groupedByPersonDict = {}

  for sample in sampleList:
    contact = sample.getProperties().get(contactPersonNamePropertyName)
    if groupedByPersonDict.has_key(contact):
      groupedByPersonDict[contact].append(sample)
    else: 
      groupedByPersonDict[contact] = [sample]    
  return groupedByPersonDict

def getContainer(sample):
  
  flowcell = None
  
  parentSearchCriteria = SearchCriteria()
  # define Parent
  parentSearchCriteria.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sample.getCode()))
  childrenSearchCriteria = SearchCriteria()
  # we look for children with this parent
  childrenSearchCriteria.addSubCriteria(SearchSubCriteria.createSampleParentCriteria(parentSearchCriteria))
  children = service.searchForSamples(childrenSearchCriteria)
  
  for child in children:
    child = child.getCode()
    try:
      containerName, lane = child.split(dataSetFileNameEntitySeparator)
    except:
      # No container
      containerName = ""
      lane = ""
    
    if containerName <> "":
      containerSearch = SearchCriteria()
      containerSearch.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, containerName))
      containerList = service.searchForSamples(containerSearch)

      for container in containerList:
        flowcell = flowCell ()
        flowcell.id = containerName
        '''flowcell.sequencer = container.getProperties().get("SEQUENCER")
        flowcell.flowCellSequencedOn = container.getProperties().get("FLOW_CELL_SEQUENCED_ON")
        flowcell.endType = container.getProperties().get("END_TYPE")
        flowcell.illuminaPipelineVersion = container.getProperties().get("ILLUMINA_PIPELINE_VERSION")
        flowcell.cyclesRequestedByCustomer = container.getProperties().get("CYCLES_REQUESTED_BY_CUSTOMER")
        flowcell.flowCellType = container.getProperties().get("FLOWCELLTYPE")
        flowcell.sbsKit = container.getProperties().get("SBS_KIT") '''
  return flowcell
        
def writeExcel(piName, groupedByPersonDict, format = "xls"):
  '''
  '''
  
  myRows = uniqueRow()
  kitVocabulary = getVocabulary("KIT")
  setOfFlowcells = set ()
  
  def writeHeader():
    # Write header
    row = sheet.createRow(myRows.getNextRow())
    row.createCell(0).setCellValue(facilityName + ", " + facilityInstitution)
    row.getCell(0).setCellStyle(setFont(wb, 14))
    row1 = sheet.createRow(myRows.getNextRow())
    row1.createCell(0).setCellValue(getDate())
    row1.getCell(0).setCellStyle(setFont(wb, 10))
    
  
  def createRow(key = "", value = "", rowNumber = 0, fontSize = 10):
    '''
    '''
    if rowNumber == 0: 
      row = sheet.createRow(myRows.getNextRow())
    else:
      row = rowNumber
    row.createCell(0).setCellValue(key)
    row.createCell(1).setCellValue(value)
    row.getCell(0).setCellStyle(setFont(wb, fontSize))
    row.getCell(1).setCellStyle(setFont(wb, fontSize))
    return row
  
  def writeFooter(sheet):
    footer = sheet.getFooter()
    footer.setRight("generated on " + datetime.now().strftime("%H:%M - %d.%m.%Y"))

  wb = (eval(excelFormats[format]))
  createHelper = wb.getCreationHelper()
  sheet = wb.createSheet(facilityNameShort)
  # 3/2 = 150 percent magnification when opening the workbook
  sheet.setZoom(3, 2)

  writeHeader()
  createRow(PRINCIPAL_INVESTIGATOR, piName)
  contactrow = myRows.setRow(myRows.getNextRow())
   
  for contactPerson in groupedByPersonDict: 
    myColumns = uniqueColumn()
    createRow(CONTACT_PERSON, contactPerson, 0, 14)
    
    sampleList = groupedByPersonDict[contactPerson]
    
    sampleHeader = sheet.createRow(myRows.getNextRow())
    sampleHeader.createCell(myColumns.getNextColumn()).setCellValue("Flow Cell")
    sampleHeader.getCell(myColumns.getCurrentColumn()).setCellStyle(setFont(wb, 10))
    sampleHeader.createCell(myColumns.getNextColumn()).setCellValue("Sample Code")
    sampleHeader.getCell(myColumns.getCurrentColumn()).setCellStyle(setFont(wb, 10))
    for c in columnHeaders:
      sampleHeader.createCell(myColumns.getNextColumn()).setCellValue(c)
      sampleHeader.getCell(myColumns.getCurrentColumn()).setCellStyle(setFont(wb, 10))
  
    for singleSample in sampleList:
      singleSampleColumns = uniqueColumn()
      fc = getContainer(singleSample)
      
      rowN = sheet.createRow(myRows.getNextRow())
      contactrow += 3
      
      code = singleSample.getCode()
      if fc is not None:
        rowN.createCell(singleSampleColumns.getNextColumn()).setCellValue(fc.id)
        rowN.getCell(singleSampleColumns.getCurrentColumn()).setCellStyle(setFont(wb, 10))
        setOfFlowcells.add(fc.id)
      rowN.createCell(singleSampleColumns.getNextColumn()).setCellValue(code)
      rowN.getCell(singleSampleColumns.getCurrentColumn()).setCellStyle(setFont(wb, 10))
      
      for column in columnHeaders:
        if column == "KIT":
          if singleSample.getProperties().get(column) is not None:        
            val = kitVocabulary[singleSample.getProperties().get(column)]
        else:
          val = singleSample.getProperties().get(column)
        rowN.createCell(singleSampleColumns.getNextColumn()).setCellValue(val)
        rowN.getCell(singleSampleColumns.getCurrentColumn()).setCellStyle(setFont(wb, 10))

    createRow()
  
  
  createRow("Flow Cell Details", "", 0, 14)
  
  for FC in setOfFlowcells:
    containerSearch = SearchCriteria()
    containerSearch.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, FC))
    containerList = service.searchForSamples(containerSearch)
    
    print (len(containerList))
    
    for container in containerList:
      #index = containerList.index(container)
      #createRow("Number",index+1)
      createRow(FLOW_CELL_ID, FC)
      for property in fcPropertyList:
        createRow(property, container.getProperties().get(property))
      createRow()
  
  # adjust width
  for i in range(0, 20):
    sheet.autoSizeColumn(i)

  # set layout to landscape
  sheet.getPrintSetup().setLandscape(True)
  
  writeFooter(sheet)
  
  #  Write the output to a file
  fileOut = FileOutputStream(outputPath + "/" + facilityNameShort + "_" + piName.replace(" ", "_") + 
                             datetime.now().strftime("_%d_%m_%Y.") + format)
  wb.write(fileOut);
  fileOut.close();  

def progressBar(toolbar_width = 40):
  '''
  '''
  # setup toolbar
  sys.stdout.write("[%s]" % (" " * toolbar_width))
  sys.stdout.flush()
  sys.stdout.write("\b" * (toolbar_width+1)) # return to start of line, after '[' 
  

'''
Main script
'''

# for now setting the format by hand
format = "xlsx" 

service = OpenbisServiceFacadeFactory.tryCreate("tracker", "jadfs8r3jahu12", "http://openbis-dsu.bsse.ethz.ch:8080", 5000)

foundSamples = getNonbilledSamples("ILLUMINA_SEQUENCING")
groupByPiDict = groupByPi(foundSamples)

print "Found " + str(len(groupByPiDict)) + " Principal Investigators"

progressBar(len(groupByPiDict))

for principalInvestigator in groupByPiDict:
  groupedByPersonDict = groupByContactPerson(groupByPiDict[principalInvestigator])
  # create an Excel file for each PI
  writeExcel(principalInvestigator, groupedByPersonDict, format)
  sys.stdout.write("*")
  sys.stdout.flush()
sys.stdout.write("\n")

service.logout()
print("DONE")
