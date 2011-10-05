import os

import utils
import config

from java.lang import RuntimeException
from java.io import File
from java.util import Properties

from ch.systemsx.cisd.common.fileconverter import FileConverter, Tiff2PngConversionStrategy
from ch.systemsx.cisd.openbis.generic.shared.basic.dto.api import ValidationException

from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.transformations import ImageTransformationBuffer 
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageDataConfig, ImageMetadata, OriginalDataStorageFormat, Location 

from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import Channel
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import ChannelColorRGB

from ch.systemsx.cisd.openbis.dss.etl.custom.incell import IncellImageMetadataParser
from ch.systemsx.cisd.openbis.dss.etl.custom.geexplorer import GEExplorerImageAnalysisResultParser
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry

#reload(config)

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
   
    def getAvailableChannelTransformations(self, channelCode):
        buffer = ImageTransformationBuffer()
        buffer.appendAllBitShiftsFor12BitGrayscale()
        buffer.appendAutoRescaleGrayscaleIntensity(0, "Original contrast")
        
        # This is an example how to add a transformation which is performed
        # by the ImageMagic convert command line tool.
        # Any convert parameters can be specified and many different transformations
        # can be made available.
        # It is usually useful to add "-depth 12" parameter additionally
        # if the original image has color depth > 8 bits.
        # buffer.appendImageMagicConvert("-edge 1 -depth 12", "Edge detection")

        return buffer.getTransformations()

    # sets the basic colors 
    def createChannel(self, channelCode):
        channel = Channel(channelCode, channelCode)
        channelCode = channelCode.upper()
        if (channelCode == "DAPI"):
            channel.setChannelColorRGB(ChannelColorRGB(0, 77, 255))
        elif (channelCode == "FITC"):
            channel.setChannelColorRGB(ChannelColorRGB(56, 255, 0))
        elif (channelCode == "CY5"):
            channel.setChannelColorRGB(ChannelColorRGB(244, 0, 0))
            
        channel.setAvailableTransformations(self.getAvailableChannelTransformations(channelCode))
        # color of other channels will be set automatically
        return channel
            
class RawImageDataSetConfig(MyImageDataSetConfig):
    _codeToWavelengthMap = None
    
    def __init__(self, incomingDir):
        MyImageDataSetConfig.__init__(self, incomingDir)
        self._codeToWavelengthMap = self._tryReadCodeToWavelengthMap(incomingDir)
        
    def _tryReadCodeToWavelengthMap(self, incomingDir):
        imageMetadataFile = utils.findFileByExt(incomingDir, "xdce")
        if imageMetadataFile is not None:
            parser = IncellImageMetadataParser(imageMetadataFile.getPath())
            channelCodes = parser.getChannelCodes()
            channelWavelengths = parser.getChannelWavelengths()
            codeToWavelengthMap = {}
            for i in range(0, len(channelCodes)):
                codeToWavelengthMap[channelCodes[i].upper()] = channelWavelengths[i]
            return codeToWavelengthMap
        return None
    
    # overrides the basic colors by using the wavelengths found in the xdce file
    def createChannel(self, channelCode):
        channel = MyImageDataSetConfig.createChannel(self, channelCode)
        channelCode = channelCode.upper()
        if self._codeToWavelengthMap is not None:
            if channelCode in self._codeToWavelengthMap:
                wavelength = self._codeToWavelengthMap[channelCode]
                # This will override the color from the superclass and set it 
                # using Bruton's algorithm.
                channel.setWavelengthAndColor(wavelength)
        return channel
    
def createRawImagesDataset(incoming, plate, batchName, transaction, factory):
    imageDatasetConfig = RawImageDataSetConfig(incoming)
    imageDatasetConfig.setRawImageDatasetType()
    imageDatasetConfig.setFileFormatType(config.IMAGE_DATASET_FILE_FORMAT)
    imageDatasetConfig.setUseImageMagicToGenerateThumbnails(config.USE_IMAGE_MAGIC_CONVERT_TOOL)
   
    imageDatasetConfig.setImageLibrary("BioFormats", "TiffDelegateReader")
    
    #imageDatasetConfig.setComputeCommonIntensityRangeOfAllImagesForChannels(["DAPI"])
    #imageDatasetConfig.setComputeCommonIntensityRangeOfAllImagesIsDefault(False)
    #imageDatasetConfig.setComputeCommonIntensityRangeOfAllImagesForAllChannels()
    #imageDatasetConfig.setComputeCommonIntensityRangeOfAllImagesThreshold(0.01)

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

def registerSegmentationImages(overlaysDir, plate, imageDataSetCode, analysisProcedureCode, transaction, factory):
    convertToPng(overlaysDir.getPath(), config.OVERLAYS_TRANSPARENT_COLOR)
    overlayDatasetConfig = MyImageDataSetConfig(overlaysDir)
    overlayDatasetConfig.setSegmentationImageDatasetType()
    overlayDatasetConfig.setFileFormatType(config.OVERLAY_IMAGE_FILE_FORMAT)
    overlayDatasetConfig.setUseImageMagicToGenerateThumbnails(config.USE_IMAGE_MAGIC_CONVERT_TOOL)
    overlayDatasetConfig.setGenerateHighQualityThumbnails(True)
    # Available in the next release:
    #overlayDatasetConfig.setThumbnailsGenerationImageMagicParams(["-contrast-stretch", "0"])

    overlayDatasetDetails = factory.createImageRegistrationDetails(overlayDatasetConfig, overlaysDir)
    if analysisProcedureCode:
        overlayDatasetDetails.setPropertyValue("$ANALYSIS_PROCEDURE", analysisProcedureCode)
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
    return analysisProcedureCode
        
        
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
