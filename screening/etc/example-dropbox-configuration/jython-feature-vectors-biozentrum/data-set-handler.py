#! /usr/bin/env python
# This is an example Jython dropbox for importing feature vectors coming from analysis of image datasets from Biozentrum

import os
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import *

DEFAULT_SPACE = "TEST"
SEP = ","

def parseFeaturesFile(featuresBuilder, incoming):
    file = open(incoming.getPath())
    for header in file:
        headerTokens = header.split(SEP)
        featureCode = headerTokens[0]
        featureDef = FeatureDefinition(featureCode)
        featureValues = featuresBuilder.defineFeature(featureDef)
        for rowValues in file:
            rowTokens = rowValues.split(SEP)
            rowLabel = rowTokens[0].strip()
            if len(rowLabel) == 0:
                break
            for column in range(1,len(headerTokens)):
                value = rowTokens[column].strip()
                well = rowLabel + str(column)
                #print featureCode, well, value
                featureValues.addValue(well, value)

featuresBuilder = factory.createFeaturesBuilder()
parseFeaturesFile(featuresBuilder, incoming) 
analysisRegistrationDetails = factory.createFeatureVectorRegistrationDetails(featuresBuilder, incoming)

tr = service.transaction(incoming, factory)

plateCode = os.path.splitext(incoming.getName())[0]
sampleIdentifier = "/"+DEFAULT_SPACE+"/"+plateCode
plate = tr.getSample(sampleIdentifier)

analysisDataset = tr.createNewDataSet(analysisRegistrationDetails)
analysisDataset.setPropertyValue("DESCRIPTION", "my dataset")
analysisDataset.setSample(plate)
tr.moveFile(incoming.getPath(), analysisDataset)
print "Registered dataset:", analysisDataset.getDataSetCode()
