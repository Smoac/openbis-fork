from __future__ import with_statement
import subprocess
import os
import time
import shutil
import sys
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

ORGANISM_CODE = 'NCBI_ORGANISM_TAXONOMY'

# here we have a lookup table to look for the the bowtie2 index names given
# by the NCBI/openBIS taxonomy

ORGANISM_DICT = {"10090" : "mm10", # Mouse
                   "9606" : "hg19", # Human
                   "7227" : "dm3", # Fruit fly
                   "10847" : "phix", # phiX
                   "4932" : "sacCer3", # baker's yeast
}

SESSIONDIR="/home/sbsuser/openbis/sprint/servers/datastore_server/dss-tmp/"

column1 = "resultCode"
column2 = "resultString"

# --------------------------------------------------------------------

def fileExists (fileName):

  #if os.path.isfile(fileName): 
  #  return True
  #else:
  #  return False 
  
  try:
     with open(fileName):
       print fileName
       print "exists!"
       return True
  except IOError:
   print "no file"
   return False

# --------------------------------------------------------------------

def getSessionPath(permId, userId):
  return SESSIONDIR + permId + "_" + userId

# --------------------------------------------------------------------

def process(tr, parameters, tableBuilder):
  '''
  Main function which controls the output
  '''
  method = parameters.get("method")
  permId = parameters.get("permId")
  userId = parameters.get("userId")
  
  tableBuilder.addHeader(column1) 
  tableBuilder.addHeader(column2) 

  if (method == "startJob" and not os.path.exists(getSessionPath(permId, userId))):
    print "Start the job"
    startJob(tr, parameters, userId, tableBuilder)

  if method == "pollJob":
    pollJob(tr, parameters, userId, tableBuilder)

# --------------------------------------------------------------------

def startJob(tr, parameters, userId, tableBuilder):
  '''
  Starts job
  '''
  permId = parameters.get("permId")
  sendEmail = parameters.get("sendEmail")
  bowtieParam = parameters.get("bowtieParam")
  clusteroption = parameters.get("clusteroptionsParam")

  #print(permId)
  #print(sendEmail)
  #print(bowtieParam)
  #print(clusteroption)

  outputDict = {}
  result = ""

  CMD = "/home/sbsuser/openbis/bin/gc3_bowtie2.sh"

  def searchDs (tr, dscode):
    # search for the data set
    search_service = tr.getSearchService()
    sc = SearchCriteria()
    sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch( \
                      SearchCriteria.MatchClauseAttribute.CODE, dscode));
    myds = search_service.searchForDataSets(sc)
    try:
      assert myds.size() == 1
    except assertionerror:
      print (str(myds.size()) +  " " + myds.getcode())
    return myds

 # --------------------------------------------------------------------

  def searchSample (tr, sampleCode):
    search_service = tr.getSearchService()
    sc = SearchCriteria()
    sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch( \
                      SearchCriteria.MatchClauseAttribute.CODE, sampleCode));
    searchedSamples = search_service.searchForSamples(sc)
    try:
      assert searchedSamples.size() == 1
    except assertionerror:
      print (str(searchedSamples.size()) +  " " + searchedSamples.getcode())
    return searchedSamples

 # --------------------------------------------------------------------

  dataSet = searchDs(tr, permId)
  connectedSampleId = dataSet[0].getSample().getSampleIdentifier()

  # We have to do a second search for the sample, otherwise the peroperties 
  # are not present when using dataSet[0].getSample() fro accessing the sample

  content = contentProvider.getContent(permId)
  #print content
  pList = content.listMatchingNodes(".*.fastq.gz")
  #print pList

  def returnDict(outputDict):
    for entry in outputDict:
      row = tableBuilder.addRow()
      row.setCell(column1, outputDict[entry][0])
      row.setCell(column2, outputDict[entry][1])


  if pList:
    sampleCode = "-".join(str(pList[0].tryGetFile()).split("/")[-1].split("_")[0:3])

  print sampleCode
  foundSample = searchSample(tr, sampleCode)
  organism = foundSample[0].getPropertyValue(ORGANISM_CODE)
  print organism

  if organism:
    try:
      outputDict[0] = [0, "Found " + ORGANISM_DICT[organism] + "[" +organism + \
             "] as reference genome - defined in Sample " + \
               sampleCode ]
 
      for element in pList:
        print element.tryGetFile()
        file = str(element.tryGetFile())

        # here we actually start the job
        proc = subprocess.Popen([CMD, file, permId + "_" +  userId, ORGANISM_DICT[organism], "\'" + bowtieParam + "\'", clusteroption, "8" , getSessionPath(permId, userId)], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        strOut, err =  proc.communicate()
        print strOut
        print err

        filename = os.path.basename(file)
        if len(err) == 0:
          outputDict[5] = [0, strOut]
          outputDict[1] = [0, "Processing " + filename]
          outputDict[3] = [0, "Successfully submitted job to... "]
        else:
          outputDict[5] = [1, err]

    except:
      outputDict[0] = [1, "Given organism " + organism + " from sample " +  sampleCode + " is not available as bowtie2 index."]
  else:
    outputDict[0] = [1, "No organism specified in the sample. Making an alignment is not possible."]

  #sampleId, fcDate, seqSn, runningNumber, fcID, lane, index, fullLane, readNumber, fileNumber = filename.split('-')[-1].split('_')

  returnDict(outputDict)
 
# --------------------------------------------------------------------

def pollJob(tr, parameters, userId, tableBuilder):
   
    permId = parameters.get("permId")

    output = subprocess.Popen(["/home/sbsuser/bela_play/gc3Status.sh", getSessionPath(permId, userId)], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    strOut, err =  output.communicate()
    strOut = strOut.strip()

    column1 = "resultCode"
    column2 = "resultString"

    row = tableBuilder.addRow()
    if strOut == 'TERMINATED' or strOut == 'STOPPED' or strOut == 'Nope':
      row.setCell(column1, 100 )
      row.setCell(column2, strOut )
    else:
      row.setCell(column1, 101 )
      row.setCell(column2, strOut )

    if strOut == 'TERMINATED' or strOut == 'STOPPED':
      shutil.rmtree(getSessionPath(permId, userId))

