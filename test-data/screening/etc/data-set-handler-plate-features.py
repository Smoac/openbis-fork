#!/usr/bin/env python

"""
An Jython dropbox for importing a CSV file with feature vectors.
"""

import os
from java.util import Properties

def getConfigurationProperties():
    config = Properties()
    config.setProperty("separator", ",")
    config.setProperty("well-name-row", "Well")
    config.setProperty("well-name-col", "Well")
    config.setProperty("well-name-col-is-alphanum", "true")
    return config

space="PLATONIC"
transaction = service.transaction(incoming, factory);
splittedFileName = incoming.getName().split('.')
plateCode = splittedFileName[0]
sample = transaction.getSample("/" + space + "/" + plateCode)
configProps = getConfigurationProperties()
analysisRegistrationDetails = factory.createFeatureVectorRegistrationDetails(incoming.getPath(), configProps)

dataset = transaction.createNewDataSet(analysisRegistrationDetails)
dataset.setDataSetType("HCS_ANALYSIS_WELL_FEATURES")
dataset.setFileFormatType('CSV')
if len(splittedFileName) > 1:
    dataset.setPropertyValue("$ANALYSIS_PROCEDURE", splittedFileName[1])
dataset.setSample(sample)
transaction.moveFile(incoming.getPath(), dataset)

