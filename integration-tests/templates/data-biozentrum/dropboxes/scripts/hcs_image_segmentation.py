#! /usr/bin/env python
# This is a dropbox for importing HCS segmentation image datasets

from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import *
import commonImageDropbox
import commonDropbox

# Global variable storing AbstractPropertiesParser
datasetMetadataParser = None

def rollback_service(service, throwable):
    global datasetMetadataParser
    commonDropbox.createFailureStatus(datasetMetadataParser, throwable, incoming)
        
def register(incomingPath):
    global datasetMetadataParser
    datasetMetadataParser = commonDropbox.DerivedDatasetMetadataParser(incomingPath)
    iBrain2DatasetId = datasetMetadataParser.getIBrain2DatasetId()
    openbisDatasetParentPermId = datasetMetadataParser.getParentDatasetPermId()

    (space, plate) = commonDropbox.tryGetConnectedPlate(state, openbisDatasetParentPermId, iBrain2DatasetId, incomingPath)
    if plate == None:
        return
    
    imageDataset = commonImageDropbox.IBrain2SegmentationImageDataSetConfig()
    imageDataset.setSegmentationImageDatasetType()
    imageDataset.setPlate(space, plate)
    imageDataset.setFileFormatType("TIFF")
    imageDataset.setRecognizedImageExtensions(["tif", "tiff"])
    imageDataset.setGenerateThumbnails(True)
    imageDataset.setMaxThumbnailWidthAndHeight(imageDataset.THUMBANAIL_SIZE)
    imageDataset.setGenerateHighQualityThumbnails(True)
    imageDataset.setStoreChannelsOnExperimentLevel(False)
    imageDataset.setOriginalDataStorageFormat(OriginalDataStorageFormat.HDF5)
    imageDataset.setConvertTransformationCliArguments("-contrast-stretch 0 -edge 1 -threshold 1 -transparent black")
    imageDataset.setUseImageMagicToGenerateThumbnails(True)

    commonDropbox.setImageDatasetPropertiesAndRegister(imageDataset, datasetMetadataParser, incoming, service, factory)
          
if incoming.isDirectory():
    register(incoming.getPath())
