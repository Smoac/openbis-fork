import re
import os

import utilfunctions as util
import plateinitializer as plateinit

from java.lang import RuntimeException
from java.io import File
from java.util import Properties

from org.apache.commons.lang.exception import ExceptionUtils
from ch.systemsx.cisd.common.mail import From
from ch.systemsx.cisd.common.fileconverter import FileConverter, Tiff2PngConversionStrategy
from ch.systemsx.cisd.openbis.generic.shared.basic.dto.api import ValidationException

from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria 
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClause, MatchClauseAttribute
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageDataConfig, ImageMetadata, OriginalDataStorageFormat, Location 
from ch.systemsx.cisd.openbis.dss.etl.custom.geexplorer import GEExplorerImageAnalysisResultParser
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry

# Switch this off if there is more then one dropbox using this module,
# in this case it should be switched on manually only after the module's code has been changed on the fly
reload(plateinit)

""" Switch to True in development environment to use a mock of Abase database """
TEST_MODE=False

""" the url of the Sanofi's openBIS installation """
OPENBIS_URL = "https://bwl27.sanofi-aventis.com:8443/openbis"

EXPERIMENT_RECIPIENTS_PROPCODE = "OBSERVER_EMAILS"

""" the sample type identifying plates """
PLATE_TYPE = "PLATE"

""" file format code of files in a new image dataset """
IMAGE_DATASET_FILE_FORMAT = "TIFF"
IMAGE_DATASET_BATCH_PROPCODE = "ACQUISITION_BATCH"

"""
Allows to recognize that the subdirectory of the incoming dataset directory contains overlay images.
This text has to appear in the subdirectory name.
"""
OVERLAYS_DIR_PATTERN = "_ROITiff"
""" file format of the image overlay dataset """
OVERLAY_IMAGE_FILE_FORMAT = "PNG"
""" name of the color which should be treated as transparent in overlays """
OVERLAYS_TRANSPARENT_COLOR = "black"

""" file format of the analysis dataset """
ANALYSIS_FILE_FORMAT = "CSV"
    
""" should thumbnails be generated? """
GENERATE_THUMBNAILS = True

""" the maximal width and height of the generated thumbnails """
MAX_THUMNAIL_WIDTH_AND_HEIGHT = 256

"""
Number of threads that are used for thumbnail generation will be equal to:
   this constant * number of processor cores
Set to 1/<number-of-cores> if ImageMagic 'convert' tool is not installed.
"""
ALLOWED_MACHINE_LOAD_DURING_THUMBNAIL_GENERATION = 1.0

""" should all dataset in one experiment use the same channels? """
STORE_CHANNELS_ON_EXPERIMENT_LEVEL = False

""" should the original data be stored in the original form or should we pack them into one container? """
ORIGINAL_DATA_STORAGE_FORMAT = OriginalDataStorageFormat.UNCHANGED

""" Change to 'False' if 'convert' tool is not installed """
USE_IMAGE_MAGIC_CONVERT_TOOL = True

def isUserError(ex):
    return ex.value and (ex.value.getClass() == ValidationException("").getClass())

def getAdminEmails():
    admins = state.getOpenBisService().listAdministrators()
    adminEmails = [ admin.getEmail() for admin in admins if admin.getEmail() ]
    return adminEmails

def getUserEmails():
    global experiment
    
    recipients = []
    
    if experiment:
        recipientsProp = experiment.getPropertyValue(EXPERIMENT_RECIPIENTS_PROPCODE)
        if recipientsProp:
           recipients = [ email.strip() for email in recipientsProp.split(",") ]
        
    return recipients

def getAllEmailRecipients():
    return getAdminEmails() + getUserEmails()

def commit_transaction(service, transaction):
    global plateCode
    
    incomingFileName = incoming.getName()
    plateLink = createPlateLink(OPENBIS_URL, plateCode)
    sendEmail("openBIS: New data registered for %s" % (plateCode), """
    Dear openBIS user,
    
      New data from folder '%(incomingFileName)s' has been successfully registered in plate %(plateLink)s.
       
      This email has been generated automatically.
      
      Have a nice day!
      
    Administrator
    """ % vars(), getAllEmailRecipients())

def rollback_service(service, ex):
    if isUserError(ex):
        shortErrorMessage = ex.getMessage()
        if not shortErrorMessage:
            shortErrorMessage = ex.value.getMessage()
        sendUserError(service, shortErrorMessage, getAllEmailRecipients())
    else:
        fullErrorMessage = ExceptionUtils.getFullStackTrace(ex)
        sendAdminError(service, fullErrorMessage, getAdminEmails())
        sendSystemErrorNotificationToUser(service, getUserEmails())
        
def sendUserError(service, errorDetails, recipients):        
    global plateCode
    
    localPlateCode = plateCode
    incomingFileName = incoming.getName()
        
    if localPlateCode:
        sendEmail("openBIS: Data registration failed for %s" % (localPlateCode), """
    Dear openBIS user,
    
      Registering new data for plate %(localPlateCode)s has failed with error '%(errorDetails)s'.
      The name of the incoming folder '%(incomingFileName)s' was added to '.faulty_paths'. Please,
      repair the problem and remove the entry from '.faulty_paths' to retry registration.
       
      This email has been generated automatically.
      
    Administrator
        """ % vars(), recipients)
    else:
        sendEmail("openBIS: Data registration failed for folder '%s'" % (incomingFileName), """
    Dear openBIS user,
    
      openBIS was unable to understand the name of an incoming folder. 
      Detailed error message was '%(errorDetails)s'.
      
      This email has been generated automatically.
      
    Administrator
        """ % vars(), recipients)
    
def sendAdminError(service, errorDetails, recipients):        
    incomingFileName = incoming.getName()
        
    sendEmail("openBIS System Error: Data registration failed for %s" % (incomingFileName), """
    Dear openBIS Administrator,
    
      The registration of data sets from incoming folder '%(incomingFileName)s' has failed
      in an unexpected way. The most probable cause is a misconfiguration of a bug in the system. 
      Here is a full description of the encountered error :
      
      %(errorDetails)s
      
      Please, repair the problem and remove the entry from '.faulty_paths' to retry registration.
      
    openBIS
        """ % vars(), recipients)
        
def sendSystemErrorNotificationToUser(service, recipients):        
    incomingFileName = incoming.getName()
        
    sendEmail("openBIS: Data registration failed for folder '%s'" % (incomingFileName), """
    Dear openBIS user,
    
      Registering new data from incoming folder '%(incomingFileName)s' has failed due to a system error.
      
      openBIS has sent a notification to the responsible system administrators and they should be 
      fixing the problem as soon as possible. 
      
      We are sorry for any inconveniences this may have caused. 
      
    openBIS Administrators
        """ % vars(), recipients)

def sendEmail(title, content, recipients):
    global experiment
    
    if not recipients:
        if experiment:
            experimentMsg = ("Please, fill in e-mail recipients list in the property '%s' of experiment '%s'." % (EXPERIMENT_RECIPIENTS_PROPCODE, experiment.getExperimentIdentifier()))
        else:
            experimentMsg = "" 
        state.operationLog.error("Failed to detect e-mail recipients for incoming folder '%s'.%s"
                                 " No e-mails will be sent." 
                                 "\nEmail title: %s" 
                                 "\nEmail content: %s" % 
                                 (incoming.getName(), experimentMsg, title, content))
        return
    
    fromAddress = From("openbis@sanofi-aventis.com")
    replyTo = None
    state.mailClient.sendMessage(title, content, replyTo, fromAddress, recipients)

def createPlateLink(openbisUrl, code):
    return "<a href='%(openbisUrl)s#entity=SAMPLE&sample_type=PLATE&action=SEARCH&code=%(code)s'>%(code)s</a>" % vars()

def findPlateByCode(code):
    """
       Finds a plate (openBIS sample) matching a specified bar code.
    """
    criteria = SearchCriteria()
    criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, PLATE_TYPE))
    criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, code))
    
    searchService = transaction.getSearchService()
    platesFound = list(searchService.searchForSamples(criteria))
    
    if not platesFound:
        raise ValidationException("No plate with code '%(code)s' found in the openBIS database" % vars())
    
    return platesFound[0]

def parseIncomingDirname(dirName):
    """
       Parses the name of an incoming dataset folder from the format
       '<ACQUISITION_BATCH_NAME>_<BAR_CODE>_<TIMESTAMP>' to a tuple (acquisitionBatch, plateCode)
    """
    tokens = dirName.split("_")
    if len(tokens) < 2:
        raise ValidationException("Data set directory name does not match the pattern '<ACQUISITION_BATCH_NAME>_<BAR_CODE>_<TIMESTAMP>': " + dirName)
    
    acquisitionBatch = tokens[0]
    plateCode = tokens[1]
    return (acquisitionBatch, plateCode)
    

def convertToPng(dir, transparentColor):
    delete_original_files = True
    strategy = Tiff2PngConversionStrategy(transparentColor, 0, delete_original_files)
    # Uses cores * machineLoad threads for the conversion, but not more than maxThreads
    machineLoad = ALLOWED_MACHINE_LOAD_DURING_THUMBNAIL_GENERATION
    maxThreads = 100
    errorMsg = FileConverter.performConversion(File(dir), strategy, machineLoad, maxThreads)
    if errorMsg != None:
        raise RuntimeException("Error converting overlays:", errorMsg)

# ---------------------

class MyImageDataSetConfig(SimpleImageDataConfig):
    
    def __init__(self, incomingDir, imageRootDir):
        self.incomingDir = incomingDir
        self.imageRootDir = imageRootDir
        self.setStorageConfiguration()
        
    def setStorageConfiguration(self):
        self.setStoreChannelsOnExperimentLevel(STORE_CHANNELS_ON_EXPERIMENT_LEVEL)
        self.setOriginalDataStorageFormat(ORIGINAL_DATA_STORAGE_FORMAT)
        if GENERATE_THUMBNAILS:
            self.setGenerateThumbnails(True)
            #self.setUseImageMagicToGenerateThumbnails(True)
            self.setAllowedMachineLoadDuringThumbnailsGeneration(ALLOWED_MACHINE_LOAD_DURING_THUMBNAIL_GENERATION)
            self.setMaxThumbnailWidthAndHeight(MAX_THUMNAIL_WIDTH_AND_HEIGHT)
    
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
        if not self.imageRootDir.equals(imageFile.getParentFile()):
            return None
    
        basename = os.path.splitext(imageFile.name)[0]
        wellText = basename[0:util.find(basename, "(")] # A - 1
        imageTokens.well = wellText.replace(" - ", "")
        
        if " wv " in basename:
            fieldText = basename[util.find(basename, "fld ") + 4 : util.find(basename, " wv")]
            imageTokens.channelCode = basename[util.rfind(basename, " - ") + 3 :-1]
        else:
            fieldText = basename[util.find(basename, "fld ") + 4 : util.find(basename, ")")]
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


global experiment
global plateCode 
global plate 

if incoming.isDirectory():
    experiment = None
    plateCode = None
    plate = None
    
    transaction = service.transaction(incoming, factory)
    
    (batchName, plateCode) = parseIncomingDirname(incoming.getName())
    plate = findPlateByCode(plateCode)
    if not plate.getExperiment():
        raise ValidationException("Plate with code '%(plateCode)s' is not associated with experiment" % vars())
    
    experimentId = plate.getExperiment().getExperimentIdentifier()
    experiment = transaction.getExperiment(experimentId)
    
    # reload the sample with all contained samples
    plate = transaction.getSample(plate.getSampleIdentifier())
    if len(plate.getContainedSamples()) == 0:
        plateInitializer = plateinit.PlateInitializer(transaction, state, plate, experiment, TEST_MODE)
        plateInitializer.createWellsAndMaterials()
        
    imageDatasetConfig = MyImageDataSetConfig(incoming, incoming)
    imageDatasetConfig.setRawImageDatasetType()
    imageDatasetConfig.setFileFormatType(IMAGE_DATASET_FILE_FORMAT)
    imageDatasetConfig.setUseImageMagicToGenerateThumbnails(USE_IMAGE_MAGIC_CONVERT_TOOL)
    # Available in the next release:
    #imageDatasetConfig.setThumbnailsGenerationImageMagicParams(["-contrast-stretch", "0"])
    imageDatasetDetails = factory.createImageRegistrationDetails(imageDatasetConfig, incoming)
    imageDataSet = transaction.createNewDataSet(imageDatasetDetails)
    imageDataSet.setPropertyValue(IMAGE_DATASET_BATCH_PROPCODE, batchName)
    imageDataSet.setSample(plate)
    
    # check for overlays folder
    overlaysDir = util.findDir(incoming, OVERLAYS_DIR_PATTERN)
    if overlaysDir is not None:
        convertToPng(overlaysDir.getPath(), OVERLAYS_TRANSPARENT_COLOR)
        overlayDatasetConfig = MyImageDataSetConfig(overlaysDir, overlaysDir)
        overlayDatasetConfig.setSegmentationImageDatasetType()
        overlayDatasetConfig.setFileFormatType(OVERLAY_IMAGE_FILE_FORMAT)
        overlayDatasetConfig.setUseImageMagicToGenerateThumbnails(USE_IMAGE_MAGIC_CONVERT_TOOL)
        # Available in the next release:
        #overlayDatasetConfig.setThumbnailsGenerationImageMagicParams(["-contrast-stretch", "0"])

        overlayDatasetDetails = factory.createImageRegistrationDetails(overlayDatasetConfig, overlaysDir)
        overlayDataset = transaction.createNewDataSet(overlayDatasetDetails)
        overlayDataset.setSample(imageDataSet.getSample())
        overlayDataset.setParentDatasets([ imageDataSet.getDataSetCode() ])
        transaction.moveFile(overlaysDir.getPath(), overlayDataset, "overlays")
    
    # transform and move analysis file
    analysisFile = util.findFileByExt(incoming, "xml")
    if analysisFile is not None:
        analysisCSVFile = File(analysisFile.getPath() + ".csv")
        GEExplorerImageAnalysisResultParser(analysisFile.getPath()).writeCSV(analysisCSVFile)
        
        featureProps = Properties()
        featureProps.setProperty("separator", ",")
        featureProps.setProperty("well-name-row", "Well")
        featureProps.setProperty("well-name-col", "Well")
        
        analysisDataSetDetails = factory.createFeatureVectorRegistrationDetails(analysisCSVFile.getPath(), featureProps)
        analysisProcedureCode = util.extractFileBasename(analysisFile.getName())
        analysisDataSetDetails.getDataSetInformation().setAnalysisProcedure(analysisProcedureCode)
        analysisDataSet = transaction.createNewDataSet(analysisDataSetDetails)
        analysisDataSet.setSample(imageDataSet.getSample())
        analysisDataSet.setParentDatasets([ imageDataSet.getDataSetCode() ])
        analysisDataSet.setFileFormatType(ANALYSIS_FILE_FORMAT)
        transaction.moveFile(analysisCSVFile.getPath(), analysisDataSet)
    
    imageDataSetFolder = transaction.moveFile(incoming.getPath(), imageDataSet)
