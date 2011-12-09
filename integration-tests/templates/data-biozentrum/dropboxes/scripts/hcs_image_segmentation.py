#! /usr/bin/env python
# This is a dropbox for importing HCS segmentation image datasets

from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import OriginalDataStorageFormat
import commonImageDropbox
import commonDropbox

# Global variable storing AbstractPropertiesParser
datasetMetadataParser = None

def rollback_service(service, throwable):
    global datasetMetadataParser
    commonDropbox.createFailureStatus(datasetMetadataParser, throwable, incoming)
        
def setImageDatasetPropertiesAndRegister(imageDataset, metadataParser, incoming, service, factory, tr = None):
    iBrain2DatasetId = metadataParser.getIBrain2DatasetId()
    imageRegistrationDetails = factory.createImageRegistrationDetails(imageDataset, incoming)
    for propertyCode, value in metadataParser.getDatasetPropertiesIter():
        imageRegistrationDetails.setPropertyValue(propertyCode, value)
    # Workaround: set the property as the registration details are a SimpleImageDataConfig which doesn't have a setter for it
    imageRegistrationDetails.setPropertyValue('$ANALYSIS_PROCEDURE', metadataParser.getAnalysisProcedure())

    if tr is None:
        tr = service.transaction(incoming, factory)
    dataset = tr.createNewDataSet(imageRegistrationDetails)
    dataset.setParentDatasets([metadataParser.getParentDatasetPermId()])
    imageDataSetFolder = tr.moveFile(incoming.getPath(), dataset)
    if tr.commit():
        commonDropbox.createSuccessStatus(iBrain2DatasetId, dataset, incoming.getPath())

def register(incomingPath):
    global datasetMetadataParser
    datasetMetadataParser = commonDropbox.DerivedDatasetMetadataParser(incomingPath)
    iBrain2DatasetId = datasetMetadataParser.getIBrain2DatasetId()
    openbisDatasetParentPermIds = datasetMetadataParser.getParentDatasetPermId()
    
    for openbisDatasetParentPermId in openbisDatasetParentPermIds:
        (space, plate) = commonDropbox.tryGetConnectedPlate(state, openbisDatasetParentPermId, iBrain2DatasetId, incomingPath)
        if plate != None:
            break
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
    imageDataset.setUseImageMagicToGenerateThumbnails(False)
    imageDataset.setAllowedMachineLoadDuringThumbnailsGeneration(1/2.0)
    imageDataset.setImageLibrary("BioFormats", "TiffDelegateReader")

    setImageDatasetPropertiesAndRegister(imageDataset, datasetMetadataParser, incoming, service, factory)
          
if incoming.isDirectory():
    register(incoming.getPath())
