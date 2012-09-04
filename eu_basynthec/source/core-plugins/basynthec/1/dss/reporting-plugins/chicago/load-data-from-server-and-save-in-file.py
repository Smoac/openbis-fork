#! /usr/bin/env python

import urllib
import json
import csv

FIELD_ID = 'id'
FIELD_PREDICTIONS = 'predictions'
FIELD_PHENOTYPES = 'phenotypes'
FIELD_HAS_PREDICTIONS = 'hasPredictions'
FIELD_HAS_PHENOTYPES = 'hasPhenotypes'

def loadDataFromServer(serverUrl):
  url = urllib.urlopen(serverUrl)
  return url.read().decode('utf8')

def convertData(dataString):
  jsonList = json.loads(dataString)
  dataList = []

  for jsonItem in jsonList:
    id = jsonItem[FIELD_ID].upper() 
    hasPredictions = jsonItem[FIELD_PREDICTIONS] != None and len(jsonItem[FIELD_PREDICTIONS]) > 0
    hasPhenotypes = jsonItem[FIELD_PHENOTYPES] != None and len(jsonItem[FIELD_PHENOTYPES]) > 0
    dataList.append({FIELD_ID: id, FIELD_HAS_PREDICTIONS: hasPredictions, FIELD_HAS_PHENOTYPES: hasPhenotypes})

  return dataList

def writeDataToFile(filePath, dataList):
  file = None
  try:

    file = open(filePath,'w')
    writer = csv.writer(file)
    writer.writerow([FIELD_ID, FIELD_HAS_PREDICTIONS, FIELD_HAS_PHENOTYPES]);

    for dataItem in dataList:
      id = dataItem[FIELD_ID]
      hasPredictions = dataItem[FIELD_HAS_PREDICTIONS]
      hasPhenotypes = dataItem[FIELD_HAS_PHENOTYPES]
      writer.writerow([id, hasPredictions, hasPhenotypes])

  except IOError as err:
    print 'Could not write the data to a file: ' + str(filePath)
    raise
  finally:
    if file != None:
      file.close()

data = loadDataFromServer('http://pubseed.theseed.org/model-prod/StrainServer.cgi?user=reviewer&pass=reviewer&method=getAllPhenotypesAndPredictions&encoding=json')
writeDataToFile('data-from-server.csv', convertData(data))

