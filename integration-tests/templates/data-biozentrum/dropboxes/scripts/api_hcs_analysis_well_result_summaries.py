#! /usr/bin/env python
# Jython dropbox which is not used by iBrain2.
# It is suitable to upload CSV files with feature vectors with the API.
 
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import *

SEPARATOR = ","

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

transaction = service.transaction(incoming, factory)
featuresBuilder = defineFeatures(incoming.getPath())
analysisRegistrationDetails = factory.createFeatureVectorRegistrationDetails(featuresBuilder, incoming)
dataset = transaction.createNewDataSet(analysisRegistrationDetails)
dataset.setFileFormatType('CSV')
dataset.setDataSetType('HCS_ANALYSIS_WELL_RESULTS_SUMMARIES')
transaction.moveFile(incoming.getPath(), dataset)

