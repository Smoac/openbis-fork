#! /usr/bin/env python
# This is an example Jython dropbox for importing feature vectors coming from analysis of image datasets
 
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import *
import commonImageDropbox
import commonDropbox

SEPARATOR = ","

# Global variable storing AbstractPropertiesParser
datasetMetadataParser = None

def rollback_service(service, throwable):
    global datasetMetadataParser
    commonDropbox.createFailureStatus(datasetMetadataParser, throwable, incoming)
    
# Specific code which defines the feature vector values for the dataset.
# Usually you will parse the content of the incoming file or directory to get the values.
# Parameters
#     incomingCsvPath: path which points to the incoming CSV file
def defineFeatures(incomingCsvFile):
    featuresBuilder = factory.createFeaturesBuilder()
    file = open(incomingCsvFile)
    for header in file:
        headerTokens = header.split(SEPARATOR)
        featureCode = headerTokens[0]
        featureValues = featuresBuilder.defineFeature(featureCode)
        for rowValues in file:
            rowTokens = rowValues.split(SEPARATOR)
            rowLabel = rowTokens[0].strip()
            if len(rowLabel) == 0:
                break
            for column in range(1,len(headerTokens)):
                value = rowTokens[column].strip()
                well = rowLabel + str(column)
                featureValues.addValue(well, value)
    return featuresBuilder

def register(incomingPath):
    global datasetMetadataParser
    datasetMetadataParser = commonDropbox.DerivedDatasetMetadataParser(incomingPath)

    transaction = service.transaction(incoming, factory)
    incomingCsvFile = commonDropbox.findCSVFile(incomingPath)
    featuresBuilder = defineFeatures(incomingCsvFile)
    analysisRegistrationDetails = factory.createFeatureVectorRegistrationDetails(featuresBuilder, incoming)
    dataset = transaction.createNewDataSet(analysisRegistrationDetails)
    dataset.setDataSetType('HCS_ANALYSIS_WELL_RESULTS_SUMMARIES')
    dataset.setFileFormatType('CSV')
    commonDropbox.registerDerivedDataset(state, transaction, dataset, incoming, datasetMetadataParser)
 
if incoming.isDirectory():
    register(incoming.getPath())
