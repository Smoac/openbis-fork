from __future__ import with_statement
import subprocess
import os
import time
import shutil
import sys
import datetime
import httplib
import urllib

from com.xhaus.jyson import JysonCodec as json
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria

ORGANISM_CODE = 'NCBI_ORGANISM_TAXONOMY'

# here we have a lookup table to look for the the bowtie2 index names given
# by the NCBI/openBIS taxonomy

ORGANISM_DICT = {"10090" : "mm10", # Mouse
                   "9606" : "hg19", # Human
                   "7227" : "dm3", # Fruit fly
                   "10847" : "phix", # phiX
                   "4932" : "sacCer3", # baker's yeast
                   "1773" : "myc_tub_h37rv_2", # M. tuberculosis H37Rv
}

column1 = "resultCode"
column2 = "resultString"

def process(tr, parameters, tableBuilder):
  '''
  Main function which controls the output
  '''
  method = parameters.get("method")
  permId = parameters.get("permId")
  userId = parameters.get("userId")
  
  tableBuilder.addHeader(column1) 
  tableBuilder.addHeader(column2) 

  if method == "startJob":
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

  outputDict = {}
  result = ""

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

  stop = False
  taxonomyURL = "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id="

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

  if organism:
    print organism
    try:
      org = ORGANISM_DICT[organism]
      outputDict[0] = [0, "Found " + org + "[" +organism + \
             "] as reference genome - defined in Sample " + \
               sampleCode ]
    except:
      outputDict[0] = [1, 'Given organism <a href=\"' + taxonomyURL + organism + '\" target=\"_blank\">' +  organism + '</a> from sample ' +  sampleCode + ' is not available as bowtie2 index.']
      #sys.exit("Given organism not available as bowtie2 index")
      stop = True

    if not bowtieParam:
      bowtieParam = '--phred33'

    if not stop:
      headers = {"Content-type": "application/x-www-form-urlencoded", "Accept": "text/plain"}
      workflow = open('/home/sbsuser/bee/bowtie2_bee.xml').read()
      params = urllib.urlencode({'workflow': workflow, 'api.datasetID': permId, 'api.organism': ORGANISM_DICT[organism], 'api.bowtieParams':bowtieParam, 'api.user' : userId})
      conn = httplib.HTTPConnection("127.0.0.1:9999")
      conn.request("POST", "/apiv1/processes", params, headers)
      resp = conn.getresponse()
      conn.close()
      outputDict[1] = [0, "Job started..."]
      #outputDict[1] = [0, str(resp.read())]
      #outputDict[2] = [0, str(resp.status)]

  else:
    outputDict[0] = [1, "No organism specified in the sample. Making an alignment is not possible."]

  #sampleId, fcDate, seqSn, runningNumber, fcID, lane, index, fullLane, readNumber, fileNumber = filename.split('-')[-1].split('_')

  returnDict(outputDict)
 
# --------------------------------------------------------------------

def pollJob(tr, parameters, userId, tableBuilder):
  
    permId = parameters.get("permId")

    conn = httplib.HTTPConnection("127.0.0.1:9999")
    conn.request("GET", "/apiv1/processes?inputId=" + permId)
    resp = conn.getresponse().read()
    conn.close()
    
    if resp != 'null':
        data = json.loads(resp)
        processes = data['workflowDTO']
        strOut = "Processing history: \n"
    else:
        strOut = "No Job started."
        processes = []

    isThereRunning = False

    if not isinstance(processes, list):
      processes = [processes] 

    for p in processes:
	d = datetime.datetime.fromtimestamp(float(p['startTime'])/1000.0)
	strOut += "Process started: " + str(d) + " is in state: " + p['status'] + "\n"
        if not isFinished(p['status']):
            isThereRunning = True
    
    column1 = "resultCode"
    column2 = "resultString"

    row = tableBuilder.addRow()
    if not isThereRunning:
      row.setCell(column1, 100 )
      row.setCell(column2, strOut )
    else:
      row.setCell(column1, 101 )
      row.setCell(column2, strOut )

def isFinished(status):
   return status == 'COMPLETE' or status == 'ABORTED' or status == 'ERROR' or status == 'WARNING'
