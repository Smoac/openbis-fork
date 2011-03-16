#! /usr/bin/env python

import commonImageDropbox
import commonDropbox
from java.util import Properties

reload(commonImageDropbox)
reload(commonDropbox)

# Global variable where we set the iBrain2 id of the dataset at the beginning, 
# so that the rollback can use it as well.
iBrain2DatasetId = None

def rollback_transaction(service, transaction, algorithmRunner, throwable):
    commonDropbox.createFailureStatus(iBrain2DatasetId, throwable, incoming)

def getConfigurationProperties():
    config = Properties()
    config.setProperty("separator", ",")
    config.setProperty("well-name-row", "File_Name")
    config.setProperty("well-name-col", "File_Name")
    config.setProperty("well-name-col-is-alphanum", "true")
    return config
    
def register(incomingPath):
    metadataParser = commonDropbox.DerivedDatasetMetadataParser(incomingPath)
    global iBrain2DatasetId
    iBrain2DatasetId = metadataParser.getIBrain2DatasetId()

    transaction = service.transaction(incoming, factory)
    configProps = getConfigurationProperties()
    incomingCsvFile = commonDropbox.findCSVFile(incomingPath)
    analysisRegistrationDetails = factory.createFeatureVectorRegistrationDetails(incomingCsvFile, configProps)

    dataset = transaction.createNewDataSet(analysisRegistrationDetails)
    dataset.setDataSetType('HCS_ANALYSIS_WELL_QUALITY')
    dataset.setFileFormatType('CSV')
    commonDropbox.registerDerivedDataset(state, transaction, dataset, incoming, metadataParser)
 
def rollback_transaction(service, transaction, algorithmRunner, throwable):
    commonDropbox.createFailureStatus(iBrain2DatasetId, throwable, incoming)
            
if incoming.isDirectory():
    register(incoming.getPath())