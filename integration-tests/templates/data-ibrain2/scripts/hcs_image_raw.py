#! /usr/bin/env python

import commonImageDropbox
import commonDropbox

reload(commonImageDropbox)
reload(commonDropbox)

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
            openbisExpDesc = experimentDesc + " (type: "+experimentType + ")"
            experiment.setPropertyValue("DESCRIPTION", openbisExpDesc)

        plate = transaction.createNewSample(sampleIdentifier, PLATE_TYPE_CODE)
        plate.setPropertyValue(PLATE_GEOMETRY_PROPERTY_CODE, PLATE_GEOMETRY)
        plate.setExperiment(experiment)
    return plate


iBrain2DatasetId = None

if incoming.isDirectory():
    incomingPath = incoming.getPath()
    metadataParser = commonDropbox.AcquiredDatasetMetadataParser(incomingPath)
    iBrain2DatasetId = metadataParser.getIBrain2DatasetId()
    assayParser = commonDropbox.AssayParser(incomingPath)

    imageDataset = commonImageDropbox.IBrain2ImageDataSetConfig()
    imageDataset.setRawImageDatasetType()
    imageDataset.setFileFormatType("TIFF")
    imageDataset.setRecognizedImageExtensions(["tif", "tiff"])    
    imageDataset.setStoreChannelsOnExperimentLevel(False)
    imageDataset.setGenerateThumbnails(True)
    imageDataset.setMaxThumbnailWidthAndHeight(imageDataset.THUMBANAIL_SIZE)

    imageRegistrationDetails = factory.createImageRegistrationDetails(imageDataset, incoming)
    for propertyCode, value in metadataParser.getDatasetPropertiesIter():
		imageRegistrationDetails.setPropertyValue(propertyCode, value)

    tr = service.transaction(incoming, factory)

    plate = metadataParser.getPlateCode()
    space = assayParser.get(assayParser.LAB_LEADER_PROPERTY)
    plate = createPlateWithExperimentIfNeeded(tr, assayParser, plate, space)	    

    dataset = tr.createNewDataSet(imageRegistrationDetails)
    dataset.setSample(plate)
    imageDataSetFolder = tr.moveFile(incomingPath, dataset)
    tr.commit()
    commonDropbox.createSuccessStatus(iBrain2DatasetId, dataset, incomingPath)

def rollback_transaction(service, transaction, algorithmRunner, throwable):
    commonDropbox.createFailureStatus(iBrain2DatasetId, throwable, incoming)