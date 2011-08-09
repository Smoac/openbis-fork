import os

import utils
import config

from java.lang import RuntimeException
from java.io import File
from java.util import Properties

from ch.systemsx.cisd.common.fileconverter import FileConverter, Tiff2PngConversionStrategy
from ch.systemsx.cisd.openbis.generic.shared.basic.dto.api import ValidationException

from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageDataConfig, ImageMetadata, OriginalDataStorageFormat, Location 
from ch.systemsx.cisd.openbis.dss.etl.custom.geexplorer import GEExplorerImageAnalysisResultParser
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry


class MyImageDataSetConfig(SimpleImageDataConfig):
    
    def __init__(self, incomingDir):
        self.incomingDir = incomingDir
        self.setStorageConfiguration()
        
    def setStorageConfiguration(self):
        self.setStoreChannelsOnExperimentLevel(config.STORE_CHANNELS_ON_EXPERIMENT_LEVEL)
        self.setOriginalDataStorageFormat(config.ORIGINAL_DATA_STORAGE_FORMAT)
        if config.GENERATE_THUMBNAILS:
            self.setGenerateThumbnails(True)
            #self.setUseImageMagicToGenerateThumbnails(True)
            self.setAllowedMachineLoadDuringThumbnailsGeneration(config.ALLOWED_MACHINE_LOAD_DURING_THUMBNAIL_GENERATION)
            self.setMaxThumbnailWidthAndHeight(config.MAX_THUMNAIL_WIDTH_AND_HEIGHT)
    
    """
    Creates ImageFileInfo for a given ImageTokens.
    Converts tile number to coordinates on the 'well matrix'.
    Example file name: A - 1(fld 1 wv Cy5 - Cy5).tif
    Returns:
        ImageTokens
    """
    def extractImageMetadata(self, path):
        imageTokens = ImageMetadata()
        
        imageFile = File(self.incomingDir, path)
        if not self.incomingDir.equals(imageFile.getParentFile()):
            return None
    
        basename = os.path.splitext(imageFile.name)[0]
        wellText = basename[0:utils.find(basename, "(")] # A - 1
        imageTokens.well = wellText.replace(" - ", "")
        
        if " wv " in basename:
            fieldText = basename[utils.find(basename, "fld ") + 4 : utils.find(basename, " wv")]
            imageTokens.channelCode = basename[utils.rfind(basename, " - ") + 3 :-1]
        else:
            fieldText = basename[utils.find(basename, "fld ") + 4 : utils.find(basename, ")")]
            imageTokens.channelCode = "DEFAULT"
        
        try:
            imageTokens.tileNumber = int(fieldText)
        except ValueError:
            raise ValidationException("Cannot parse field number from '" + fieldText + "' in '" + basename + "' file name.")
    
        return imageTokens
    
    def getTileCoordinates(self, tileNumber, tileGeometry):
        columns = tileGeometry.getWidth()
        row = ((tileNumber - 1) / columns) + 1
        col = ((tileNumber - 1) % columns) + 1
        return Location(row, col)
    
    def getTileGeometry(self, imageMetadataList, maxTile):
        if maxTile % 4 == 0 and maxTile != 4:
            (cols, rows) = (maxTile / 4, 4)
        elif maxTile % 3 == 0:
            (cols, rows) = (maxTile / 3, 3)
        elif maxTile % 2 == 0:
            (cols, rows) = (maxTile / 2, 2)
        else:
            (cols, rows) = (maxTile, 1)
        
        return Geometry.createFromRowColDimensions(cols, rows);

def createRawImagesDataset(incoming, plate, batchName, transaction, factory):
    imageDatasetConfig = MyImageDataSetConfig(incoming)
    imageDatasetConfig.setRawImageDatasetType()
    imageDatasetConfig.setFileFormatType(config.IMAGE_DATASET_FILE_FORMAT)
    imageDatasetConfig.setUseImageMagicToGenerateThumbnails(config.USE_IMAGE_MAGIC_CONVERT_TOOL)
    # Available in the next release:
    #imageDatasetConfig.setThumbnailsGenerationImageMagicParams(["-contrast-stretch", "0"])
    imageDatasetDetails = factory.createImageRegistrationDetails(imageDatasetConfig, incoming)
    imageDataSet = transaction.createNewDataSet(imageDatasetDetails)
    imageDataSet.setPropertyValue(config.IMAGE_DATASET_BATCH_PROPCODE, batchName)
    imageDataSet.setSample(plate)
    return imageDataSet

def convertToPng(dir, transparentColor):
    delete_original_files = True
    strategy = Tiff2PngConversionStrategy(transparentColor, 0, delete_original_files)
    # Uses cores * machineLoad threads for the conversion, but not more than maxThreads
    machineLoad = config.ALLOWED_MACHINE_LOAD_DURING_THUMBNAIL_GENERATION
    maxThreads = 100
    errorMsg = FileConverter.performConversion(File(dir), strategy, machineLoad, maxThreads)
    if errorMsg:
        raise RuntimeException("Error converting overlays:" + errorMsg)

def registerSegmentationImages(overlaysDir, plate, imageDataSetCode, transaction, factory):
    convertToPng(overlaysDir.getPath(), config.OVERLAYS_TRANSPARENT_COLOR)
    overlayDatasetConfig = MyImageDataSetConfig(overlaysDir)
    overlayDatasetConfig.setSegmentationImageDatasetType()
    overlayDatasetConfig.setFileFormatType(config.OVERLAY_IMAGE_FILE_FORMAT)
    overlayDatasetConfig.setUseImageMagicToGenerateThumbnails(config.USE_IMAGE_MAGIC_CONVERT_TOOL)
    # Available in the next release:
    #overlayDatasetConfig.setThumbnailsGenerationImageMagicParams(["-contrast-stretch", "0"])

    overlayDatasetDetails = factory.createImageRegistrationDetails(overlayDatasetConfig, overlaysDir)
    overlayDataset = transaction.createNewDataSet(overlayDatasetDetails)
    overlayDataset.setSample(plate)
    overlayDataset.setParentDatasets([ imageDataSetCode ])
    transaction.moveFile(overlaysDir.getPath(), overlayDataset, "overlays")

def registerAnalysisData(analysisXmlFile, plate, parentDatasetCode, transaction, factory):
    analysisCSVFile = File(analysisXmlFile.getPath() + ".csv")
    geXmlParser = GEExplorerImageAnalysisResultParser(analysisXmlFile.getPath())
    geXmlParser.writeCSV(analysisCSVFile)
    
    featureProps = Properties()
    featureProps.setProperty("separator", ",")
    featureProps.setProperty("well-name-row", "Well")
    featureProps.setProperty("well-name-col", "Well")
    
    analysisDataSetDetails = factory.createFeatureVectorRegistrationDetails(analysisCSVFile.getPath(), featureProps)
    analysisProcedureCode = geXmlParser.getAnalysisProcedureName()
    analysisDataSetDetails.getDataSetInformation().setAnalysisProcedure(analysisProcedureCode)
    analysisDataSet = transaction.createNewDataSet(analysisDataSetDetails)
    analysisDataSet.setSample(plate)
    analysisDataSet.setParentDatasets([ parentDatasetCode ])
    analysisDataSet.setFileFormatType(config.ANALYSIS_FILE_FORMAT)
    parentDirName = "analysis"
    transaction.createNewDirectory(analysisDataSet, parentDirName)
    transaction.moveFile(analysisCSVFile.getPath(), analysisDataSet, parentDirName)
    transaction.moveFile(analysisXmlFile.getPath(), analysisDataSet, parentDirName)
        
        
""" 
Returns a tuple: 
 (plate sample connected to the dataset with the specified code, experiment connected to the plate) 
Raises exception is no sample is connected to the dataset.
"""
def findConnectedPlate(transaction, datasetCode):
    parentDataset = transaction.getDataSet(datasetCode)
    if not parentDataset:
        raise ValidationException("Cannot find a dataset with code '%s'" % (datasetCode))
    plate = parentDataset.getSample()
    if not plate:
        raise ValidationException("Dataset with code '%s' is not connected to any plate" % (datasetCode))
    
    # fetch the sample again to fetch connected experiment 
    plateIdentifier = plate.getSampleIdentifier()
    plate = transaction.getSample(plateIdentifier)
    plateExperiment = plate.getExperiment()
    if not plateExperiment:
        raise ValidationException("Cannot find experiment of a plate '%s' (connected to dataset with code '%s')" % (plateIdentifier, datasetCode))
    experimentIdent = plateExperiment.getExperimentIdentifier()
    # fetch the experiment again to have all its properties
    experiment = transaction.getExperiment(experimentIdent)

    return (plate, experiment)
