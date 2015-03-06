'''
@copyright: 2012 ETH Zuerich, CISD

@license:
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author: Fabian Gemperle

@note: print statements go to ~/openbis/servers/datastore_server/log/startup_log.txt

'''

import time
import math
import os
import fnmatch
# Load Java-Library to import XML data:
import read_demultiplex_stats
# Load openBIS-Libraries:
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchSubCriteria


def process(transaction):
  '''
  Main method in corresponding openBIS dropbox
  '''

  # Constants:
  XML_FILENAME = 'ConversionStats.xml'
  TYPE_DATASET = 'FASTQ_GZ'
  INDEX_NO = 'NOINDEX'
  INDEX_UNKNOWN = 'UNKNOWN'
  INDEX_EMPTY = ''
  CODE_INDEX1 = 'BARCODE'
  CODE_INDEX2 = 'INDEX2'
  CODE_INDEX1LENGTH = 'INDEXREAD'
  CODE_INDEX2LENGTH = 'INDEXREAD2'

  ##########################################################
  def locate(pattern, root):
    '''Locate all files matching supplied filename pattern in and below supplied root directory.'''
    for path, dirs, files in os.walk(os.path.abspath(root)):
      for filename in fnmatch.filter(files, pattern):
        yield os.path.join(path, filename)

  ##########################################################
  def getInfoVocabularyTerms(vocabularyCode):
    '''
    Get information about Terms of certain Vocabulary in openBIS.
    Input: 
    - vocabularyCode: code of Vocabulary to be investigated
    Output: 
    - vocabularyTerms: list of Terms in Vocabulary
    '''
    vocabulary = transaction.getSearchService().searchForVocabulary(vocabularyCode)

    vocabularyTerms = []
    if (vocabulary is None):
      print '\nOCCURRED EXCEPTION: Vocabulary %s does not exist' % (vocabularyCode)
    else:
      for term in vocabulary.getTerms():
        vocabularyTerms.append(term.getCode())           
      vocabularyTerms.sort()

    return vocabularyTerms

  ##########################################################
  def getInfoSampleProperties(sampleCode):
    '''
    Get information about Properties of certain Sample in openBIS.
    Input: 
    - sampleCode: code of Sample to be investigated
    Outputs: 
    - propertiesCode: list of Properties' codes
    - propertiesCodeValue: dictionary of Properties' codes and values
    '''
    ss = transaction.getSearchService()

    scSample = SearchCriteria()
    scSample.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sampleCode));
    foundSamples = ss.searchForSamples(scSample)

    propertiesCode = []
    propertiesCodeValue = {}
    try:
      assert foundSamples.size() == 1
      properties = foundSamples[0].getSample().getProperties()
      for p in properties:
        codeProperty = p.getPropertyType().getSimpleCode()
        propertiesCode.append(codeProperty)
        propertiesCodeValue[codeProperty] = p.getValue()
      propertiesCode.sort()
    except AssertionError:
      print ('\nOCCURRED EXCEPTION: ' + str(foundSamples.size()) + ' Samples found which match the criteria code \"' + sampleCode + '\".')

    return propertiesCode, propertiesCodeValue

  ##########################################################
  def getInfoDataSetPropertiesOfSample(sampleCode):
    '''
    Get information about Properties of some DataSet of certain Sample in openBIS.
    Input: 
    - sampleCode: code of DataSet's Sample to be investigated
    Outputs: 
    - propertiesCode: list of Properties' codes
    - propertiesCodeValue: dictionary of Properties' codes and values
    '''
    ss = transaction.getSearchService()

    scSample = SearchCriteria()
    scSample.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sampleCode));
    #foundSamples = ss.searchForSamples(scSample)

    scDataSet = SearchCriteria()
    scDataSet.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, TYPE_DATASET))
    scDataSet.addSubCriteria(SearchSubCriteria.createSampleCriteria(scSample))
    foundDataSets = ss.searchForDataSets(scDataSet)

    propertiesCode = []
    propertiesCodeValue = {}
    try:
      assert foundDataSets.size() > 0
      codeProperties = foundDataSets[0].getAllPropertyCodes()
      for cp in codeProperties:
        propertiesCode.append(cp)
        propertiesCodeValue[cp] = foundDataSets[0].getPropertyValue(cp)
      propertiesCode.sort()
    except AssertionError:
      print ('\nOCCURRED EXCEPTION: ' + str(foundDataSets.size()) + ' DataSets found which Sample match the criteria code \"' + sampleCode + '\" and type \"' + TYPE_DATASET + '\".')

    return propertiesCode, propertiesCodeValue

  ##########################################################
  def getIndexesOfDataSetsOfSample(sampleFlowLaneCode):
    '''
    Get both indexes (parts of barcode) of all DataSets of certain FlowLane-Sample in openBIS.
    Inputs: 
    - sampleFlowLaneCode: code of DataSet's Sample
    Outputs: 
    - indexes1: list of first index of DataSets
    - indexes2: list of second index of DataSets
    '''
    ss = transaction.getSearchService()

    scSample = SearchCriteria()
    scSample.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sampleFlowLaneCode));
    #foundSamples = ss.searchForSamples(scSample)

    scDataSet = SearchCriteria()
    scDataSet.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, TYPE_DATASET))
    scDataSet.addSubCriteria(SearchSubCriteria.createSampleCriteria(scSample))
    foundDataSets = ss.searchForDataSets(scDataSet)
    
    indexes1 = []
    indexes2 = []
    try: 
      assert foundDataSets.size() > 0
    except AssertionError:
      print ('\nOCCURRED EXCEPTION: ' + str(foundDataSets.size()) + ' DataSets found which Sample match the criteria code \"' + sampleFlowLaneCode + '\" and type \"' + TYPE_DATASET + '\".')
    for ds in foundDataSets:
      indexes1.append(ds.getPropertyValue(CODE_INDEX1))
      indexes2.append(ds.getPropertyValue(CODE_INDEX2))

    return indexes1, indexes2

  ##########################################################
  def searchDataSetsOfSample(sampleFlowLaneCode, index1, index2):
    '''
    Search DataSets by corresponding indexes (parts of barcode) of certain FlowLane-Sample in openBIS.
    Inputs: 
    - sampleFlowLaneCode: code of DataSet's Sample
    - index1: first index of DataSet
    - index2: second index of DataSet
    Output: 
    - foundDataSets: DataSets corresponding to inputs and constant TYPE_DATASET
    '''
    ss = transaction.getSearchService()

    scSample = SearchCriteria()
    scSample.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, sampleFlowLaneCode));
    #foundSample = ss.searchForSamples(scSample)

    scDataSet = SearchCriteria()
    scDataSet.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, TYPE_DATASET))
    scDataSet.addMatchClause(SearchCriteria.MatchClause.createPropertyMatch(CODE_INDEX1, index1))
    scDataSet.addMatchClause(SearchCriteria.MatchClause.createPropertyMatch(CODE_INDEX2, index2))
    scDataSet.addSubCriteria(SearchSubCriteria.createSampleCriteria(scSample))
    foundDataSets = ss.searchForDataSets(scDataSet)
    for ds in foundDataSets:
      print "Index1 of found DataSet " + ds.getDataSetCode() + ": " + ds.getPropertyValue(CODE_INDEX1)
      print "Index2 of found DataSet " + ds.getDataSetCode() + ": " + ds.getPropertyValue(CODE_INDEX2)

    return foundDataSets

  ##########################################################
  def reversecomplement(sequence):
    '''
    Reverse sequence and replace each nucleotide by its complement.
    Input: 
    - sequence: sequence of nucleotides
    Output: 
    - reverse_complement_sequence: reversed and complemented sequence
    '''
    lookup_table = {'A': 'T', 'T': 'A', 'G': 'C', 'C': 'G'}
    reverse_complement_sequence = ''
    for nucleotide in reversed(sequence):
        reverse_complement_sequence += lookup_table[nucleotide]
    return reverse_complement_sequence

  ##########################################################
  
  def sampleSearch(transaction, code=''):
    sc = SearchCriteria()
    numberOfLanes = 0
    sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.CODE, code));
    search_service = transaction.getSearchService()
    flowcell = search_service.searchForSamples(sc)
    if flowcell.size() > 0:
      # Search for contained samples
      sampleSc = SearchCriteria()
      sampleSc.addSubCriteria(SearchSubCriteria.createSampleContainerCriteria(sc))
      lanes = search_service.searchForSamples(sampleSc)
      numberOfLanes = lanes.size()
    return flowcell, lanes, numberOfLanes
  
  ##########################################################
  
  def updateLane(transaction, codeSampleFlowCell, totalLaneStatistics):
      
      flowcell, lanes, numberOfLanes = sampleSearch(transaction, codeSampleFlowCell)
      lane = lanes[0]
      mutable_lane = transaction.getSampleForUpdate(lane.getSampleIdentifier())
      print("Setting Complete Lanes Statistics For: " + lane.getSampleIdentifier())
      
      mutable_lane.setPropertyValue("YIELD_MBASES", str(int(totalLaneStatistics.Sum_PfYield)))
      mutable_lane.setPropertyValue('RAW_YIELD_MBASES', str(int(totalLaneStatistics.Sum_RawYield)))
      mutable_lane.setPropertyValue('PERCENTAGE_PASSED_FILTERING',str(int(totalLaneStatistics.Percentage_PfClusterCount_RawClusterCount)))
      mutable_lane.setPropertyValue('PF_READS_SUM',str(int(totalLaneStatistics.Sum_PfClusterCount)))
      mutable_lane.setPropertyValue('RAW_READS_SUM',str(int(totalLaneStatistics.Sum_RawClusterCount)))
      mutable_lane.setPropertyValue('PFYIELDQ30PERCENTAGE', str(int(totalLaneStatistics.Percentage_PfYieldQ30_PfYield)))
      mutable_lane.setPropertyValue('PFMEANQUALITYSCORE', str(totalLaneStatistics.Fraction_PfQualityScoreSum_PfYield))
      mutable_lane.setPropertyValue('CLUSTERS_PF_WITHOUT_NOINDEX', str(int(totalLaneStatistics.Clusters_PfWithoutNoindex)))

  ##########################################################

  print('\nPROCESS RUNNING '+time.ctime())
  incomingPath = transaction.getIncoming().getPath()  
  FileGenerator= locate(XML_FILENAME, incomingPath)
  xmlfile = FileGenerator.next()
  print "File: " + xmlfile
  
  # Import data of XML file (independent of openBIS data):
  JavaClassToProcessXML = read_demultiplex_stats() # this function is implemented as Class in Java:
  samplestatisticslist = JavaClassToProcessXML.importXMLdata_and_calculateStatistics(xmlfile)
  totalLaneStatistics = JavaClassToProcessXML.calculateTotalLaneStatistics(samplestatisticslist)
  
  if len(samplestatisticslist) == 0:
    print "\nNo Projects/Samples/Barcodes are contained in XML-file " + xmlfile + "!"
    return

  # Prepare links between XML and openBIS w.r.t. Samples:
  codeSampleFlowCell = samplestatisticslist[0].Flowcell # expect just one equal FlowCell of all imported datasets
  codeSampleFlowLane = samplestatisticslist[0].Flowcell + ":1" # expect just one equal FlowLane of all imported datasets
  
  updateLane(transaction, codeSampleFlowCell, totalLaneStatistics)

  # Prepare links between XML and openBIS w.r.t to indexes in DataSet (openBIS):
  index1list, index2list = getIndexesOfDataSetsOfSample(codeSampleFlowLane)
  propertiesCode, propertiesCodeValue = getInfoSampleProperties(codeSampleFlowCell)
  index1length = int(propertiesCodeValue[CODE_INDEX1LENGTH])
  index2length = int(propertiesCodeValue[CODE_INDEX2LENGTH])

  nprocessedDataSets = 0
  for s in samplestatisticslist:
    print "\nContent in XML file:\n", s
    print "Connection to openBIS:"

    # Prepare link between XML and openBIS w.r.t to indexes in Barcode (XML):
    indexes = s.Barcode.split("-")
    if len(indexes) == 1: # only first part in Barcode
      index1search = indexes[0].upper()
      index2search = INDEX_EMPTY
    elif len(indexes) == 2: # both parts in Barcode
      index1search = indexes[0].upper()
      index2search = indexes[1].upper()
    else:
      index1search = INDEX_EMPTY
      index2search = INDEX_EMPTY

    # Set link between XML and openBIS w.r.t to indexes in DataSet (openBIS):
    if index1search == INDEX_EMPTY or index1search == INDEX_UNKNOWN: 
      index1 = INDEX_NO
    else: # Hint: just two cases were known about index1length, that is 8 or 6
      if index1length > 7:
        index1 = [ index1 for index1 in index1list if index1search == index1 ]
      else: # for smaller indexlength, the index is by 1 shorter in XML-file than in openBIS
        index1 = [ index1 for index1 in index1list if index1search == index1[:index1length] ]
      try:
        index1 = index1[0]
      except: 
        print '\nOCCURRED EXCEPTION: First index \"' + index1search + '\" of Barcode in XML file has no corresponding DataSet in openBIS!'
        index1 = 'MISSING'
    if index2search == INDEX_EMPTY or index2search == INDEX_UNKNOWN:
      index2 = INDEX_NO
    else: # Hint: just one case was known about index2length, that is 8
      if index2length > 7: # second and larger index must be reversed and complemented in contrast to first or smaller index
        index2 = [ index2 for index2 in index2list if reversecomplement(index2search) == index2 ]
      else: # second and smaller index is unknown how to handle
        index2 = [ index2 for index2 in index2list if reversecomplement(index2search) == index2 ]
      try:
        index2 = index2[0]
      except: 
        print '\nOCCURRED EXCEPTION: Second index \"' + index2search + '\" of Barcode in XML file has no corresponding DataSet in openBIS!'
        index2 = 'MISSING'

    # Get DataSet of openBIS corresponding to Project/Sample/Barcode of XML file:
    correspondingDataSet = searchDataSetsOfSample(codeSampleFlowLane, index1, index2)
    try:
      assert correspondingDataSet.size() == 1
    except AssertionError: 
      print ('\nOCCURRED EXCEPTION: ' + str(correspondingDataSet.size()) + ' DataSets found which Sample match the criteria index1 \"' + str(index1) + '\" and index2 \"' + str(index2) + '\" and code \"' + codeSampleFlowLane + '\" and type \"' + TYPE_DATASET + '\".')
      continue 
    
    # Modify Properties of corresponding DataSet:
    # (method setPropertyValue requires Strings as Input, but Number format must fit to Properties already defined in openBIS)
    ds = transaction.getDataSetForUpdate(correspondingDataSet[0].getDataSetCode())
    ds.setPropertyValue('YIELD_MBASES', str(int(s.Mega_PfYield)))
    ds.setPropertyValue('RAW_YIELD_MBASES', str(int(s.Mega_RawYield)))
    ds.setPropertyValue('PERCENTAGE_PASSED_FILTERING',str(s.Percentage_PfClusterCount_RawClusterCount))
    ds.setPropertyValue('PF_READS_SUM',str(int(s.Sum_PfClusterCount))) # convert first to Integer, then to String
    ds.setPropertyValue('RAW_READS_SUM',str(int(s.Sum_RawClusterCount))) # convert first to Integer, then to String
    ds.setPropertyValue('PERCENTAGE_RAW_CLUSTERS_PER_LANE', str(s.Percentage_RawClusterCount_AllRawClusterCounts))
    ds.setPropertyValue('PFYIELDQ30PERCENTAGE', str(s.Percentage_PfYieldQ30_PfYield))
    ds.setPropertyValue('PFMEANQUALITYSCORE', str(s.Fraction_PfQualityScoreSum_PfYield))
    print "Properties in DataSet \"" + correspondingDataSet[0].getDataSetCode() + "\" are modified."
    nprocessedDataSets += 1

  print "\n", nprocessedDataSets, " openBIS-DataSets were processed." 
  print len(samplestatisticslist), " XML-Projects/-Samples/-Barcodes were processed."
  print("PROCESS DONE "+time.ctime())
