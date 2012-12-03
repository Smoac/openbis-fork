#! /usr/bin/env python

import urllib
import json
import csv
import shutil

FIELD_ID = 'id'
FIELD_PREDICTIONS = 'predictions'
FIELD_PHENOTYPES = 'phenotypes'

def loadDataFromServer(serverUrl):
  url = urllib.urlopen(serverUrl)
  return url.read().decode('utf8')

def convertData(dataString):
  jsonList = json.loads(dataString)
  dataList = []

  for jsonItem in jsonList:
    id = jsonItem[FIELD_ID].upper() 
    predictions = jsonItem[FIELD_PREDICTIONS]
    phenotypes = jsonItem[FIELD_PHENOTYPES]
    dataList.append({FIELD_ID: id, FIELD_PREDICTIONS: predictions, FIELD_PHENOTYPES: phenotypes})

  return dataList

def writeDataToFile(filePath, dataList):
  file = None
  try:

    file = open(filePath,'w')

    for dataItem in dataList:
        file.write(json.dumps(dataItem) + '\n')

  except IOError as err:
    print 'Could not write the data to a file: ' + str(filePath)
    raise
  finally:
    if file != None:
      file.close()

data = loadDataFromServer('http://pubseed.theseed.org/model-prod/StrainServer.cgi?user=reviewer&pass=reviewer&method=getAllPhenotypesAndPredictions&encoding=json')
writeDataToFile('data-from-server.csv.tmp', convertData(data))
shutil.move('data-from-server.csv.tmp', 'data-from-server.csv')

