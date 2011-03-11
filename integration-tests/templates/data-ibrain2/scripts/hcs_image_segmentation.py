#! /usr/bin/env python
# This is a dropbox for importing HCS segmentation image datasets
 
import IBrain2ImageDataSetConfig from common-image-dropbox
 
"""
TODO:
- check if parent exists and exit otherwise (ask Eva)
- 
"""
if incoming.isDirectory():
    imageDataset = IBrain2ImageDataSetConfig()
    imageDataset.setSegmentationImageDatasetType()
    plate = incoming.getName().split("_")[2][1:]
    space = "IBRAIN2"
    #space = "TEST"
    imageDataset.setPlate(space, plate)
    imageDataset.setFileFormatType("PNG")
    imageDataset.setGenerateThumbnails(True)
    imageDataset.setMaxThumbnailWidthAndHeight(imageDataset.THUMBANAIL_SIZE)
    imageDataset.setRecognizedImageExtensions(["png"])    
    imageDataset.setStoreChannelsOnExperimentLevel(False)
    imageDataset.setOriginalDataStorageFormat(OriginalDataStorageFormat.HDF5)

    imageRegistrationDetails = factory.createImageRegistrationDetails(imageDataset, incoming)
    info = imageRegistrationDetails.getDataSetInformation()
    info.getImageStorageConfiguraton().getThumbnailsStorageFormat().setHighQuality(True)
    tr = service.transaction(incoming, factory)
    
    dataset = tr.createNewDataSet(imageRegistrationDetails)
    imageDataSetFolder = tr.moveFile(incoming.getPath(), dataset)
    imageDatasetCode = dataset.getDataSetCode()
    print "Registered dataset:", imageDatasetCode
    