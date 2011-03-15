#! /usr/bin/env python
# This is a dropbox for importing HCS segmentation image datasets

from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import *
import commonImageDropbox
import commonDropbox

reload(commonImageDropbox)
reload(commonDropbox)

# Global variable where we set the iBrain2 id of the dataset at the beginning, 
# so that the rollback can use it as well.
iBrain2DatasetId = None

def register(incomingPath):
    metadataParser = commonDropbox.DerivedDatasetMetadataParser(incomingPath)
    global iBrain2DatasetId
    iBrain2DatasetId = metadataParser.getIBrain2DatasetId()
    openbisDatasetParent = metadataParser.getParentDatasetPermId()

    (space, plate) = commonDropbox.tryGetConnectedPlate(state, openbisDatasetParent, iBrain2DatasetId, incomingPath)
    if plate == None:
        return
    
    imageDataset = commonImageDropbox.IBrain2SegmentationImageDataSetConfig()
    imageDataset.setSegmentationImageDatasetType()
    imageDataset.setPlate(space, plate)
    imageDataset.setFileFormatType("TIFF")
    imageDataset.setRecognizedImageExtensions(["tif"])
    imageDataset.setGenerateThumbnails(True)
    imageDataset.setMaxThumbnailWidthAndHeight(imageDataset.THUMBANAIL_SIZE)
    imageDataset.setGenerateHighQualityThumbnails(True)
    imageDataset.setStoreChannelsOnExperimentLevel(False)
    imageDataset.setOriginalDataStorageFormat(OriginalDataStorageFormat.HDF5)
  
    commonDropbox.setPropertiesAndRegister(imageDataset, iBrain2DatasetId, metadataParser, incoming, service, factory)

def rollback_transaction(service, transaction, algorithmRunner, throwable):
    print 'failure', iBrain2DatasetId
    commonDropbox.createFailureStatus(iBrain2DatasetId, throwable, incoming)
            
if incoming.isDirectory():
    register(incoming.getPath())
