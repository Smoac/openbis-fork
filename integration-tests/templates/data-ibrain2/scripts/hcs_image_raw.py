#! /usr/bin/env python

from commonImageDropbox import IBrain2ImageDataSetConfig 
from commonDropbox import AcquiredDatasetMetadataParser

""" Plate geometry which will be used. Other possible value: 96_WELLS_8X12 """
PLATE_GEOMETRY = "384_WELLS_16X24"

""" sample type code of the plate, needed if a new sample is registered automatically """
PLATE_TYPE_CODE = "PLATE"
SIRNA_EXP_TYPE = "SIRNA_HCS"
PLATE_GEOMETRY_PROPERTY_CODE = "$PLATE_GEOMETRY"

def createPlateWithExperimentIfNeeded(transaction, assayParser, plate, space):
    project = assayParser.get(assayParser.EXPERIMENTER_PROPERTY)
    experiment = assayParser.get(assayParser.ASSAY_ID_PROPERTY)
    experimentDesc = assayParser.get(assayParser.ASSAY_DESC_PROPERTY)
	experimentType = assayParser.get(assayParser.ASSAY_TYPE_PROPERTY)
    
    sampleIdentifier = "/"+space+"/"+plate
    plate = transaction.getSample(sampleIdentifier)
    if plate == None:
        expIdentifier = "/"+space+"/"+project+"/"+experiment
        experiment = transaction.getExperiment(expIdentifier)
        if experiment == None:
            experiment = transaction.createNewExperiment(expIdentifier, SIRNA_EXP_TYPE)
            openbisExpDesc = experimentDesc + "\ntype: "+experimentType
            experiment.setPropertyValue("DESCRIPTION", openbisExpDesc)

        plate = transaction.createNewSample(sampleIdentifier, PLATE_TYPE_CODE)
        plate.setPropertyValue(PLATE_GEOMETRY_PROPERTY_CODE, PLATE_GEOMETRY)
        plate.setExperiment(experiment)

"""
TODO:
- 
"""
if incoming.isDirectory():
    imageDataset = IBrain2ImageDataSetConfig()
    imageDataset.setRawImageDatasetType()
	metadataParser = AcquiredDatasetMetadataParser(incoming)
	assayParser = AssayParser(incoming)

    plate = metadataParser.getPlateCode()
    space = assayParser.get(assayParser.LAB_LEADER_PROPERTY)

    imageDataset.setPlate(space, plate)
    imageDataset.setFileFormatType("TIFF")
    imageDataset.setGenerateThumbnails(True)
    imageDataset.setMaxThumbnailWidthAndHeight(imageDataset.THUMBANAIL_SIZE)
    imageDataset.setRecognizedImageExtensions(["tif, tiff"])    
    imageDataset.setStoreChannelsOnExperimentLevel(False)

    imageRegistrationDetails = factory.createImageRegistrationDetails(imageDataset, incoming)
	for propertyCode, value in metadataParser.getPropertiesIter():
		imageRegistrationDetails.setPropertyValue(propertyCode, value)

    tr = service.transaction(incoming, factory)

    createPlateWithExperimentIfNeeded(tr, assayParser, plate, space)	    
    dataset = tr.createNewDataSet(imageRegistrationDetails)
    imageDataSetFolder = tr.moveFile(incoming.getPath(), dataset)
    imageDatasetCode = dataset.getDataSetCode()
    IBRAIN2Utils().createSuccessStatus(metadataParser.getIBrain2DatasetId(), imageDatasetCode, incoming)
    print "Registered dataset:", imageDatasetCode

# TODO: test this !!!
def rollback_transaction(service, transaction, algorithmRunner, throwable):
	incoming = service.incomingDataSetFile
	iBrain2DatasetId = AcquiredDatasetMetadataParser(incoming).getIBrain2DatasetId()
	IBRAIN2Utils().createFailureStatus(iBrain2DatasetId, throwable.getMessage(), incoming)
