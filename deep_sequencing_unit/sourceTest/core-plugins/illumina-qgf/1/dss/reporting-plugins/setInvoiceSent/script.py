def process(tr, parameters, tableBuilder):
  
  import subprocess
  import os
  import re
  from java.util import EnumSet
  from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
  from ch.systemsx.cisd.openbis.dss.client.api.v1 import OpenbisServiceFacadeFactory
  from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SampleFetchOption

  def sanitizeString(myString):
    myString = myString.replace(u'ä', 'ae')
    myString = myString.replace(u'ü', 'ue')
    myString = myString.replace(u'ö', 'oe')
    return re.sub('[^A-Za-z0-9]+', '_', myString)

  sampleId = parameters.get("sampleId")
  plainPi = parameters.get("pI")
  flowcellId = sampleId.split('/')[-1]

  # Search for the sample
  search_service = tr.getSearchService()
  sc = SearchCriteria()
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, flowcellId));
  myFC = search_service.searchForSamples(sc)
  try:
    assert myFC.size() == 1
  except AssertionError:
    print (str(myFC.size()) +  " " + flowcellId)

  for p in myFC:
    numberOfLanes = int(p.getPropertyValue('LANECOUNT'))

  for lane in range(1, numberOfLanes + 2):
    myLane = flowcellId + ":" + str(lane)
    laneSc = SearchCriteria();
    laneSc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, myLane));
    laneList = search_service.searchForSamples(laneSc)

    if not laneList:
      myLane = flowcellId + ":1_" + str(lane)
      print("myLane: " + myLane)
      laneSc = SearchCriteria();
      laneSc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, myLane));
      laneList = search_service.searchForSamples(laneSc)

    for l in laneList:
      laneParents = l.getParentSampleIdentifiers()

      for sampleCode in laneParents:
        Sample = tr.getSample(sampleCode)
        FullSamplePi =  Sample.getPropertyValue('PRINCIPAL_INVESTIGATOR_NAME')
        samplePi = sanitizeString(FullSamplePi)
        print samplePi + " == " + sanitizeString(plainPi) + " ?"

        if (samplePi == sanitizeString(plainPi)):
          print "Setting Property 'INVOICE_SENT' to true for sample: " + sampleCode
          updateableSample = tr.getSampleForUpdate(sampleCode)
          updateableSample.setPropertyValue('INVOICE', 'True')

  tableBuilder.addHeader("CSV") 

  row = tableBuilder.addRow()
  row.setCell("CSV",plainPi)

