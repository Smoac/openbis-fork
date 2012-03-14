'''

Note: 
print statements go to: ~openbis/sprint/datastore_server/log/startup_log.txt
'''

import os
import fnmatch
import re
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria

FOLDER='/links/shared/dsu-dss/dss/incoming-jython-bigwig/'
BIGWIGINFO='/links/application/dsu/bigWig/bigWigInfo '
BW_PATTERN='*.bw'

matches = []

# Create a "transaction" -- a way of grouping operations together so they all
# happen or none of them do.
transaction = service.transaction()
 
# Create a data set and set type
dataSet = transaction.createNewDataSet("BIGWIGGLE")
dataSet.setMeasuredData(False)

incomingPath = incoming.getAbsolutePath()

# Get the incoming name 
# expected: 
# Project_110907_SN792_0059_AC012FACXX_3/BSSE-DSU-1662_CGATGTA_L003_R1_001_sorted.bw
name = incoming.getName()
split=name.split('_')
if (len(split) == 6): 
  incoming_sample=split[1]+ '_'+ split[2] + '_' + split[3] + '_' + split[4]+ ':' + split[-1]
if (len(split) ==4):
  incoming_sample=split[1]+ '_'+ split[2] + ':' + split[-1]


# Looking for BWs:
for root, dirnames, filenames in os.walk(FOLDER + name):
  for filename in fnmatch.filter(filenames, BW_PATTERN):
      matches.append(os.path.join(root, filename))

# -----------------------------------------------------------------------------

def listSearch (myList, searchString):
  '''
  Searches for a given String in a list.
  Only lines matching  the start of a line a considerd as a match
  '''
  matches = []
  for i in range (0, len(myList)):
    if(re.match(searchString, myList[i])):
      matches.append(myList[i])
  return (matches)

# -----------------------------------------------------------------------------

def translateBoolean (value):
  if (value.lower() == 'yes') | (value.lower() =='y'):
    return True
  else:
    return False

def sanitizeInt (intNumber):
   return intNumber.replace(',','')

# -----------------------------------------------------------------------------

def convertListToDict(prop):
  d={}
  for i in range(0,len(properties)-1):
    lineSplit = prop[i].split(':')
    d[lineSplit[0].strip()] = lineSplit[1].strip()
  return(d)

# -----------------------------------------------------------------------------

# Extract values from a samtools view and set the results as DataSet properties 
# Command: samtools view -H ETHZ_BSSE_110429_63558AAXX_1_sorted.bam

arguments = BIGWIGINFO + matches[0]
#print('Arguments: '+ arguments)
cmdResult=os.popen(arguments).read()

properties=cmdResult.split("\n")
dictProp = convertListToDict(properties)
#print(dictProp)

dataSet.setPropertyValue("VERSION", dictProp['version'])
dataSet.setPropertyValue("ISCOMPRESSED", str(translateBoolean(dictProp['isCompressed'])))
dataSet.setPropertyValue("ISSWAPPED", dictProp['isSwapped'])
dataSet.setPropertyValue("PRIMARYDATASIZE", sanitizeInt(dictProp['primaryDataSize']))
dataSet.setPropertyValue("PRIMARYINDEXSIZE", sanitizeInt(dictProp['primaryIndexSize']))
dataSet.setPropertyValue("ZOOMLEVELS", dictProp['zoomLevels'])
dataSet.setPropertyValue("CHROMCOUNT", dictProp['chromCount'])
dataSet.setPropertyValue("BASESCOVERED", sanitizeInt(dictProp['basesCovered']))
dataSet.setPropertyValue("MEAN", dictProp['mean'])
dataSet.setPropertyValue("MIN", dictProp['min'])
dataSet.setPropertyValue("MAX", dictProp['max'])
dataSet.setPropertyValue("STD", dictProp['std'])

# Add the incoming file into the data set
transaction.moveFile(incomingPath, dataSet)

# Get the search service
search_service = transaction.getSearchService()

# Search for the sample
sc = SearchCriteria()
sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, incoming_sample));
foundSamples = search_service.searchForSamples(sc)

if foundSamples.size() > 0:
  dataSet.setSample(foundSamples[0])
  
  # Search for parent data set of the same sample
  dataSetSc = SearchCriteria()
  dataSetSc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, 'FASTQ_GZ'))
  dataSetSc.addSubCriteria(SearchSubCriteria.createSampleCriteria(sc))
  foundDataSets = search_service.searchForDataSets(dataSetSc)
  if foundDataSets.size() > 0:
    dataSet.setParentDatasets([ds.getDataSetCode() for ds in foundDataSets])
