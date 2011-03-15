#! /usr/bin/env python

import commonImageDropbox
import commonDropbox

reload(commonImageDropbox)
reload(commonDropbox)


def setPropertiesAndRegister(imageDataset, iBrain2DatasetId, metadataParser, incoming, factory):
    imageRegistrationDetails = factory.createImageRegistrationDetails(imageDataset, incoming)
    for propertyCode, value in metadataParser.getDatasetPropertiesIter():
        imageRegistrationDetails.setPropertyValue(propertyCode, value)

    tr = service.transaction(incoming, factory)
    dataset = tr.createNewDataSet(imageRegistrationDetails)
    imageDataSetFolder = tr.moveFile(incoming.getPath(), dataset)
    tr.commit()
    commonDropbox.createSuccessStatus(iBrain2DatasetId, dataset, incoming.getPath())

# -------

# Global variable where we set the iBrain2 id of the dataset at the beginning, 
# so that the rollback can use it as well.
iBrain2DatasetId = None

def register(incomingPath):
    metadataParser = commonDropbox.DerivedDatasetMetadataParser(incomingPath)
    iBrain2DatasetId = metadataParser.getIBrain2DatasetId()
    openbisDatasetParent = metadataParser.getParentDatasetPermId()

    (spaceOrError, plate) = commonDropbox.tryGetConnectedPlate(state, openbisDatasetParent)
    if plate == None:
        commonDropbox.RegistrationConfirmationUtils().createFailureStatus(iBrain2DatasetId, spaceOrError, incomingPath)
        return
    
    imageDataset = commonImageDropbox.IBrain2ImageDataSetConfig()
    imageDataset.setOverviewImageDatasetType()
    imageDataset.setPlate(spaceOrError, plate)
    imageDataset.setFileFormatType("JPG")
    imageDataset.setRecognizedImageExtensions(["jpg", "jpeg", "png", "gif"])    
    imageDataset.setStoreChannelsOnExperimentLevel(False)
    imageDataset.setGenerateThumbnails(True)
    imageDataset.setMaxThumbnailWidthAndHeight(imageDataset.THUMBANAIL_SIZE)

    setPropertiesAndRegister(imageDataset, iBrain2DatasetId, metadataParser, incoming, factory)

def rollback_transaction(service, transaction, algorithmRunner, throwable):
    commonDropbox.createFailureStatus(iBrain2DatasetId, throwable, incoming)
            
if incoming.isDirectory():
    register(incoming.getPath())

