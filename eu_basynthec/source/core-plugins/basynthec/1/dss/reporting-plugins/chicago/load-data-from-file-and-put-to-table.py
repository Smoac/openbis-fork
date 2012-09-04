#! /usr/bin/env python

import csv

FIELD_ID = 'id'
FIELD_HAS_PREDICTIONS = 'hasPredictions'
FIELD_HAS_PHENOTYPES = 'hasPhenotypes'

def loadDataFromFile(filePath):
  file = None
  try:
    file = open(filePath, 'r')
    reader = csv.DictReader(file)
    dataList = []
    
    for row in reader:
      dataList.append(row)
      
    return dataList
  except IOError, err:
    print 'Could not read the data from a file: ' + str(filePath)
    raise
  finally:
    if file != None:
      file.close()

def addDataToTable(dataList, table):
  table.addHeader(FIELD_ID)
  table.addHeader(FIELD_HAS_PREDICTIONS)
  table.addHeader(FIELD_HAS_PHENOTYPES)
  
  for dataItem in dataList:
    id = dataItem[FIELD_ID]
    hasPredictions = dataItem[FIELD_HAS_PREDICTIONS]
    hasPhenotypes = dataItem[FIELD_HAS_PHENOTYPES]
    
    row = table.addRow()
    row.setCell(FIELD_ID, id)
    row.setCell(FIELD_HAS_PREDICTIONS, hasPredictions)
    row.setCell(FIELD_HAS_PHENOTYPES, hasPhenotypes)

def aggregate(parameters, table):
    data = loadDataFromFile('TODO_FILE_TO_FILE_WITH_SERVER_DATA')
    addDataToTable(data, table)
