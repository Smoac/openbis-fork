#! /usr/bin/env python

import commonImageDropbox
import commonDropbox

reload(commonImageDropbox)
reload(commonDropbox)

# Global variable where we set the iBrain2 id of the dataset at the beginning, 
# so that the rollback can use it as well.
iBrain2DatasetId = None

def register(incomingPath):
    metadataParser = commonDropbox.DerivedDatasetMetadataParser(incomingPath)
    iBrain2DatasetId = metadataParser.getIBrain2DatasetId()
    openbisDatasetParent = metadataParser.getParentDatasetPermId()

    (space, plate) = commonDropbox.tryGetConnectedPlate(state, openbisDatasetParent, iBrain2DatasetId, incomingPath)
    if plate == None:
        return
    
    imageDataset = commonImageDropbox.IBrain2ImageDataSetConfig()
    imageDataset.setOverviewImageDatasetType()
    imageDataset.setPlate(space, plate)
    imageDataset.setFileFormatType("JPG")
    imageDataset.setRecognizedImageExtensions(["jpg", "jpeg", "png", "gif"])    
    imageDataset.setStoreChannelsOnExperimentLevel(False)
    imageDataset.setGenerateThumbnails(True)
    imageDataset.setMaxThumbnailWidthAndHeight(imageDataset.THUMBANAIL_SIZE)

    commonDropbox.setPropertiesAndRegister(imageDataset, iBrain2DatasetId, metadataParser, incoming, service, factory)

def rollback_transaction(service, transaction, algorithmRunner, throwable):
    commonDropbox.createFailureStatus(iBrain2DatasetId, throwable, incoming)
            
if incoming.isDirectory():
    register(incoming.getPath())

