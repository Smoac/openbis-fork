#! /usr/bin/env python

import csv

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

def addDataToTable(dataList, table):
  table.addHeader(FIELD_STRAIN)
  
  for dataItem in dataList:
    row = table.addRow()
    row.setCell(FIELD_STRAIN, dataItem)

def aggregate(parameters, table):
    data = loadDataFromFile('TODO_PATH_TO_FILE_WITH_SERVER_DATA')
    addDataToTable(data, table)
