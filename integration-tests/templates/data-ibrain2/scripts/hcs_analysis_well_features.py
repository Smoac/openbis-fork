#! /usr/bin/env python
# This is an example Jython dropbox for importing feature vectors coming from analysis of image datasets
 
import os
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import *

SEPARATOR = ","
 
# Specific code which defines the feature vector values for the dataset..
# Usually you will parse the content of the incoming file or directory to get the values.
# Here all the values are hard-coded for simplicity,
# but the example shows which calls you need to perform in your parser.
# Parameters
#     incoming: java.io.File which points to the incoming dataset
def defineFeatures(featuresBuilder, incoming):
    file = open(incoming.getPath())
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
 
# Returns the code of the plate to which the dataset should be connected.
# Parameters
#     incoming: java.io.File which points to the incoming dataset
def extractPlateCode(incoming):
    return os.path.splitext(incoming.getName())[0]
 
def extractSpaceCode(incoming):
    return "TEST"
 
# ----------------------------               
# --- boilerplate code which register one dataset with image analysis results on the well level
# --- Nothing has to be modified if your case is not complicated.
# ----------------------------               
 
featuresBuilder = factory.createFeaturesBuilder()
defineFeatures(featuresBuilder, incoming)
 
analysisRegistrationDetails = factory.createFeatureVectorRegistrationDetails(featuresBuilder, incoming)
tr = service.transaction(incoming, factory)
analysisDataset = tr.createNewDataSet(analysisRegistrationDetails)
 
# set plate to which the dataset should be connected
sampleIdentifier = "/"+extractSpaceCode(incoming)+"/"+extractPlateCode(incoming)
plate = tr.getSample(sampleIdentifier)
analysisDataset.setSample(plate)
 
# store the original file in the dataset.
tr.moveFile(incoming.getPath(), analysisDataset)
 
# ----------------------------               
# --- optional: other standard operations on analysisDataset can be performed (see IDataSet interface)
# ----------------------------               
 
analysisDataset.setFileFormatType("CSV")
analysisDataset.setDataSetType("HCS_ANALYSIS_WELL_FEATURES")
#analysisDataset.setParentDatasets(["20110302085840150-90"])