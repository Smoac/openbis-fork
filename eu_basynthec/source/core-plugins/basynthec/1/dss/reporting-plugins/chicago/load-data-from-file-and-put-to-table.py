#! /usr/bin/env python

import csv
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClause
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClauseAttribute


FIELD_STRAIN = 'strain'

def loadDataFromFile(filePath):
  file = None
  try:
    file = open(filePath, 'r')
    dataList = []
    
    for line in file:
      dataList.append(line)
      
    return dataList
  except IOError, err:
    print 'Could not read the data from a file: ' + str(filePath)
    raise
  finally:
    if file != None:
      file.close()

def indexDataByStrainId(data):
  dataById = {}
  for strainData in data:
    tokens = strainData.strip("{}  \n").split(',')
    theIds = [token for token in tokens if token.startswith('"id"')]
    if (len(theIds) < 1):
       continue
    theId = theIds[0].split(':')[1].strip('"  \n')
    dataById[theId] = strainData
    
  return dataById

def addDataToTable(dataList, table):
  table.addHeader(FIELD_STRAIN)
  
  for dataItem in dataList:
    row = table.addRow()
    row.setCell(FIELD_STRAIN, dataItem)
    
def addStrainsNotInChicagoDb(strainsNotInChicagoDb, table):
  for strain in strainsNotInChicagoDb:
    row = table.addRow()
    dataItem = '{"id": "' + strain + '"}'
    row.setCell(FIELD_STRAIN, dataItem)
    
def allDataSets():
  searchCriteria = SearchCriteria()
  searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, '*'))
  return searchService.searchForDataSets(searchCriteria)
  
def filterToUnknownStrains(dataSets, dataById):
  unknownStrains = set()
  for dataSet in dataSets:
    strainNames = dataSet.getPropertyValue('STRAIN_NAMES')
    if (strainNames is None):
      continue
    strains = strainNames.split(",")
    for strain in strains:
      if dataById.get(strain) is None:
        unknownStrains.add(strain)
  print unknownStrains
  return unknownStrains

def aggregate(parameters, table):
    data = loadDataFromFile('TODO_PATH_TO_FILE_WITH_SERVER_DATA')
    addDataToTable(data, table)
    
    # Now get the strains in out db. We want to find the ones that might not be in their system
    dataById = indexDataByStrainId(data)    
    dataSets = allDataSets()
    strainsNotInChicagoDb = filterToUnknownStrains(dataSets, dataById)
    addStrainsNotInChicagoDb(strainsNotInChicagoDb, table)