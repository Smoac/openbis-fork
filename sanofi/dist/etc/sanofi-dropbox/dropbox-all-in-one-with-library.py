import re
import os

from java.io import File

from ch.systemsx.cisd.common.geometry import Point, ConversionUtils
from ch.systemsx.cisd.common.mail import From
from ch.systemsx.cisd.common.fileconverter import FileConverter, Tiff2PngConversionStrategy

from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria 
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClause, MatchClauseAttribute
from ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1 import MaterialIdentifierCollection
from ch.systemsx.cisd.openbis.dss.etl.dto.api.v1 import SimpleImageDataConfig, ImageMetadata, OriginalDataStorageFormat, Location 
from ch.systemsx.cisd.openbis.dss.etl.custom.geexplorer import GEExplorerImageAnalysisResultParser
from ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto import ScreeningConstants
from ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto import Geometry

""" Switch to False for the Sanofi production environment """
TEST_MODE=True

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
ANALYSIS_RUN_PROPCODE = "$ANALYSIS_PROCEDURE"
    
""" should thumbnails be generated? """
GENERATE_THUMBNAILS = True

""" the maximal width and height of the generated thumbnails """
MAX_THUMNAIL_WIDTH_AND_HEIGHT = 256

"""
number of threads that are used for thumbnail generation will be equal to:
   this constant * number of processor cores
"""
ALLOWED_MACHINE_LOAD_DURING_THUMBNAIL_GENERATION = 1.0

""" should all dataset in one experiment use the same channels? """
STORE_CHANNELS_ON_EXPERIMENT_LEVEL = False

""" should the original data be stored in the original form or should we pack them into one container? """
ORIGINAL_DATA_STORAGE_FORMAT = OriginalDataStorageFormat.UNCHANGED

# =================================
#  Generic utility functions
# =================================

""" 
Finds first occurence of the patter from the right.
Throws exception if the pattern cannot be found.
"""
def rfind(text, pattern):
    ix = text.rfind(pattern)
    ensurePatternFound(ix, text, pattern)
    return ix

""" 
Finds first occurence of the patter from the left. 
Throws exception if the pattern cannot be found.
"""
def find(text, pattern):
    ix = text.find(pattern)
    ensurePatternFound(ix, text, pattern)
    return ix

def ensurePatternFound(ix, file, pattern):
    if ix == -1:
        raise Exception("Cannot find '" + pattern + "' pattern in file name '" + file + "'")    

""" Returns: name of the file without the extension """
def extractFileBasename(filename):
    lastDot = filename.rfind(".")
    if lastDot != -1:
        return filename[0:lastDot]
    else:
        return filename

""" Returns: extension of the file """
def getFileExt(file):
    return os.path.splitext(file)[1][1:].lower()

""" Returns: java.io.File - first file with the specified extension or None if no file matches """
def findFileByExt(incomingFile, expectedExt):
    if not incomingFile.isDirectory():
        return None
    incomingPath = incomingFile.getPath()
    for file in os.listdir(incomingPath):
        ext = getFileExt(file)
        if ext.upper() == expectedExt.upper():
            return File(incomingPath, file)
    return None

""" Returns: java.io.File - subdirectory which contains the specified marker in the name """
def findDir(incomingFile, dirNameMarker):
    if not incomingFile.isDirectory():
        return None
    incomingPath = incomingFile.getPath()
    for file in os.listdir(incomingPath):
        if dirNameMarker.upper() in file.upper():
            return File(incomingPath, file)
    return None

""" Removes trailing empty strings from a list """
def removeTrailingEmptyElements(list):
    pos = len(list)
    while (pos > 0):
        pos = pos - 1
        if not list[pos].strip():
            del list[pos]
        else:
            break
    return list


# ======================================
# end generic utility functions 
# ======================================

def rollback_service(service, ex):
    incomingFileName = incoming.getName()
    errorMessage = ex.getMessage()
    if not errorMessage:
        errorMessage = ex.toString()
        
    if plateCode:
        plateLink = createPlateLink(OPENBIS_URL, plateCode)
        sendEmail("openBIS: Data registration failed for %s" % (plateCode), """
    Dear openBIS user,
    
      Registering new data for plate %(plateLink)s has failed with error '%(errorMessage)s'.
      The name of the incoming folder '%(incomingFileName)s' was added to '.faulty_paths'. Please,
      repair the problem and remove the entry from '.faulty_paths' to retry registration.
       
      This email has been generated automatically.
      
    Administrator
        """ % vars(), False)
    else:
        sendEmail("openBIS: Data registration failed for folder '%s'" % (incomingFileName), """
    Dear user,
    
      openBIS was unable to understand the name of an incoming folder. 
      Detailed error message was '%(errorMessage)s'.
      
      This email has been generated automatically.
      
    Administrator
        """ % vars(), False)
    

def commit_transaction(service, transaction):
    incomingFileName = incoming.getName()
    plateLink = createPlateLink(OPENBIS_URL, plateCode)
    sendEmail("openBIS: New data registered for %s" % (plateCode), """
    Dear openBIS user,
    
      New data from folder '%(incomingFileName)s' has been successfully registered in plate %(plateLink)s.
       
      This email has been generated automatically.
      
      Have a nice day!
      
    Administrator
    """ % vars(), True)

def sendEmail(title, content, isError):
    recipients = []
    experimentDetected = False
    
    if vars().has_key("experiment"):
        experimentDetected = True
        recipientsProp = experiment.getPropertyValue(EXPERIMENT_RECIPIENTS_PROPCODE)
        if recipientsProp:
           recipients = [ email.strip() for email in recipientsProp.split(",") ]
        
    if not recipients and isError:
       recipients = [ email.tryGetEmailAddress() for email in state.getErrorEmailRecipients() ]
        
    if not recipients:
        if experimentDetected:
            experimentMsg = ("Please, fill in e-mail recipients list in the property '%s' of experiment '%s'." % (EXPERIMENT_RECIPIENTS_PROPCODE, experiment.getExperimentIdentifier()))
        else :
            experimentMsg = "" 
        state.operationLog.error("Failed to detected e-mail recipients for incoming folder '%s'.%s"
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
        raise RuntimeError("No plate with code '%(code)s' found in the openBIS database" % vars())
    
    return platesFound[0]

def parseIncomingDirname(dirName):
    """
       Parses the name of an incoming dataset folder from the format
       '<ACQUISITION_BATCH_NAME>_<BAR_CODE>_<TIMESTAMP>' to a tuple (acquisitionBatch, plateCode)
    """
    tokens = dirName.split("_")
    if len(tokens) < 2:
        raise RuntimeError("Data set directory name does not match the pattern '<ACQUISITION_BATCH_NAME>_<BAR_CODE>_<TIMESTAMP>': " + dirName)
    
    acquisitionBatch = tokens[0]
    plateCode = tokens[1].split('.')[0]
    return (acquisitionBatch, plateCode)
    
class SanofiMaterial:
    """
       A data structure class holding compound materials as they exist in the Abase (Sanofi) database.
    """
    def __init__(self, wellCode, materialCode, sanofiId, sanofiBatchId):
        self.wellCode = self.normalizeWellCode(wellCode)
        self.materialCode = materialCode
        self.sanofiId = sanofiId
        self.sanofiBatchId = sanofiBatchId
    
    def normalizeWellCode(self, wellCode):
        """ normalizes Sanofi wellCodes openBIS wellCodes e.g. AB007 to AB7 """
        return re.sub("(?<=\w)(0+)(?=\d)", "", wellCode)
            
class PlateInitializer:
    ABASE_DATA_SOURCE = "abase-datasource"
    ABASE_PRODUCTION_QUERY = """select
                                    ptodwellreference WELL_CODE,
                                    translate(objdbatchref,'{/:()+','{_____') MATERIAL_CODE,
                                    objdbatchref ABASE_COMPOUND_BATCH_ID,
                                    objdid ABASE_COMPOUND_ID,
                                    olptid ABASE_PLATE_CODE
                                from sysadmin.plteobjd
                                    where olptid = ?{1}"""

    # used for integration testing from openBIS team members    
    ABASE_TEST_MODE_QUERY = """select 
                                   WELL_CODE, MATERIAL_CODE, ABASE_COMPOUND_ID, 
                                   ABASE_COMPOUND_BATCH_ID, ABASE_PLATE_CODE 
                               from plates 
                                   where ABASE_PLATE_CODE = ?{1}"""
                        
    LIBRARY_TEMPLATE_PROPNAME = "LIBRARY_TEMPLATE"
    
    POSITIVE_CONTROL_TYPE = "POSITIVE_CONTROL"
    NEGATIVE_CONTROL_TYPE = "NEGATIVE_CONTROL"
    
    COMPOUND_WELL_TYPE = "COMPOUND_WELL"
    COMPOUND_WELL_CONCENTRATION_PROPNAME = "CONCENTRATION_M"
    COMPOUND_WELL_MATERIAL_PROPNAME = "COMPOUND"
    
    MATERIAL_TYPE = "COMPOUND"
    MATERIAL_ID_PROPNAME = "COMPOUND_ID"
    MATERIAL_BATCH_ID_PROPNAME = "COMPOUND_BATCH_ID"
            
    def __init__(self, transaction, plate):
        self.transaction = transaction
        self.plate = plate
        self.plateCode = plate.getCode()
        self.experimentId = plate.getExperiment().getExperimentIdentifier()
        
    def getWellCode(self, x, y):
        return ConversionUtils.convertToSpreadsheetLocation(Point(x,y))
    
    def getPlateGeometryDimensions(self):
        plateGeometryString = self.plate.getPropertyValue(ScreeningConstants.PLATE_GEOMETRY)
        geometry = Geometry.createFromPlateGeometryString(plateGeometryString)
        return (geometry.height, geometry.width)
    
    def validateLibraryDimensions(self, tsvLines):
        (plateHeight, plateWidth) = self.getPlateGeometryDimensions()
        
        numLines = len(tsvLines)
        if plateHeight < len(tsvLines) :
            raise RuntimeError("The property %s of experiment '%s' contains %s rows, but the"
                               " geometry of plate '%s' allows a maximum of %s rows. You should either reduce"
                               " the number of rows in the library template or change the plate geometry." % 
                               (self.LIBRARY_TEMPLATE_PROPNAME, self.experimentId, numLines, self.plateCode, plateHeight))
            
        for i in range(0, len(tsvLines)):
            lineWidth = len(tsvLines[i])
            if plateWidth < lineWidth:
                raise RuntimeError("The property %s of experiment '%s' contains %s columns in row %s, but the"
                                   " geometry of plate '%s' allows a maximum of %s columns. You should either reduce"
                                   " the number of columns in the library template or change the plate geometry." % 
                                   (self.LIBRARY_TEMPLATE_PROPNAME, self.experimentId, lineWidth, (i + 1), self.plateCode, plateHeight))
        
    def parseLibraryTemplate(self):
        template = experiment.getPropertyValue(self.LIBRARY_TEMPLATE_PROPNAME)
        if not template:
            raise RuntimeError("Experiment %s has no library template value in property %s" \
                               % (self.experimentId, self.LIBRARY_TEMPLATE_PROPNAME))
        
        lines = template.splitlines()
        lines = removeTrailingEmptyElements(lines)
        tsvLists = [ removeTrailingEmptyElements(line.split("\t")) for line in lines ]
                
        self.validateLibraryDimensions(tsvLists)
        
        library = {}
        for x in range(0, len(tsvLists)):
            for y in range(0, len(tsvLists[0])):
                wellCode = self.getWellCode(x,y)
                library[wellCode] = tsvLists[x][y].strip()
                 
        return library
    
    def upperCaseKeys(self, map):
        result = {}
        for entry in map.entrySet():
            result[entry.key.upper()] = entry.value
        return result
    
    def fetchPlateCompounds(self):
        """
           Fetch well metadata from the Abase database.
           
           @return: a list of tuples (one per well) in the form 
                    (wellCode, openBisCompoundCode, abaseCompoundBatchId, abaseCompoundId). 
                    In case the plate is not found in Abase return None.
        """
        if TEST_MODE:
            query = self.ABASE_TEST_MODE_QUERY
        else:
            query = self.ABASE_PRODUCTION_QUERY
            
        queryService = state.getDataSourceQueryService()
        queryResult = queryService.select(self.ABASE_DATA_SOURCE, query, [self.plateCode])
        
        sanofiMaterials = []
        for resultMap in list(queryResult):
            materialMap = self.upperCaseKeys(resultMap)
            def val(code):
                if code in materialMap:
                    return str(materialMap[code])
                else:
                    raise RuntimeError("No column '%s' in the query results from the ABASE Database" % (code))
                
            material = SanofiMaterial(val('WELL_CODE'), val('MATERIAL_CODE'), \
                                      val('ABASE_COMPOUND_ID'), val('ABASE_COMPOUND_BATCH_ID'))
                
            sanofiMaterials.append(material)
            
        queryResult.close()
        
        return sanofiMaterials
    
    def createMaterial(self, sanofiMaterial):
        material = self.transaction.createNewMaterial(sanofiMaterial.materialCode, self.MATERIAL_TYPE)
        material.setPropertyValue(self.MATERIAL_ID_PROPNAME, sanofiMaterial.sanofiId)
        material.setPropertyValue(self.MATERIAL_BATCH_ID_PROPNAME, sanofiMaterial.sanofiBatchId)
        return material
    
    def getOrCreateMaterials(self, template, materialsByCode):
        materialIdentifiers = MaterialIdentifierCollection()
        for materialCode in materialsByCode:
            materialIdentifiers.addIdentifier(self.MATERIAL_TYPE, materialCode)
        searchService = self.transaction.getSearchService() 
        existingMaterials = list(searchService.listMaterials(materialIdentifiers))
        
        existingMaterialsByCode = {}
        for material in existingMaterials:
            existingMaterialsByCode[ material.getCode() ] = material
            
        for materialCode in materialsByCode:
            if not materialCode in existingMaterialsByCode:
                sanofiMaterial = materialsByCode[materialCode]
                openbisMaterial = self.createMaterial(sanofiMaterial)
                existingMaterialsByCode[materialCode] = openbisMaterial 
        
        return existingMaterialsByCode
            
    
    def getByWellCode(self, wellCode, materialsByCode):
        for sanofiMaterial in materialsByCode.values():
            if wellCode == sanofiMaterial.wellCode:
                return sanofiMaterial
            
        return None
    
    def isCompoundWell(self, libraryValue):        
       try:
           float(libraryValue)
           return True
       except ValueError:
           return False
       
    def createWells(self, template, sanofiMaterials, openbisMaterials):
        controlWellTypes = { "H" : self.POSITIVE_CONTROL_TYPE, 
                             "L" : self.NEGATIVE_CONTROL_TYPE};
                             
        for wellCode in template:
           if template[wellCode] in ["", "-"]:
               continue
               
           templateValue = template[wellCode].upper()
           wellIdentifier = self.plate.getSampleIdentifier() + ":" + wellCode
           
           if templateValue in controlWellTypes:
               # CONTROL_WELL
               wellType = controlWellTypes[templateValue]
               well = self.transaction.createNewSample(wellIdentifier, wellType)
               well.setContainer(self.plate)
               
           elif self.isCompoundWell(templateValue): 
               # COMPOUND_WELL
               well = self.transaction.createNewSample(wellIdentifier, self.COMPOUND_WELL_TYPE)
               well.setContainer(self.plate)
               well.setPropertyValue(self.COMPOUND_WELL_CONCENTRATION_PROPNAME, templateValue)
               sanofiMaterial = self.getByWellCode(wellCode, sanofiMaterials)
               materialCode = sanofiMaterial.materialCode
               material = openbisMaterials[materialCode]
               well.setPropertyValue(self.COMPOUND_WELL_MATERIAL_PROPNAME, material.getMaterialIdentifier())
               
           else:
               raise RuntimeError("The specified value for well '%s' in the property "  
                                  " '%s' of experiment '%s' is invalid. Allowed values are 'H', 'L'"
                                  " or a number, but '%s' was found." % 
               (wellCode, self.LIBRARY_TEMPLATE_PROPNAME, self.experimentId, templateValue))
       
    def validate(self, template, sanofiMaterialsByCode):
        for wellCode in template:
           if self.isCompoundWell(template[wellCode]):
               sanofiMaterial = self.getByWellCode(wellCode, sanofiMaterialsByCode)
               if not sanofiMaterial:
                   raise RuntimeError("Error registering library for plate '%s'. The library template"
                                      " specified in property '%s' of experiment '%s' contains"
                                      " concentration value for well '%s', but no"
                                      " mapping to a material was found in the ABASE DB." % 
                                      (self.plateCode, self.LIBRARY_TEMPLATE_PROPNAME, self.experimentId, wellCode))
                   
        for sanofiMaterial in sanofiMaterialsByCode.values():
            wellCode = sanofiMaterial.wellCode
            templateValue = template.get(wellCode, None)
            
            if not templateValue or not self.isCompoundWell(templateValue): 
               val = templateValue and ("'%s'" % templateValue) or "no value"
               raise RuntimeError("Error registering library for plate '%s'. The ABASE DB contains"
                                  " a material definition for well '%s', but no valid concentration"
                                  " was found in the library template of experiment '%s'. The library"
                                  " template should contain a number for '%s' but %s was found" % 
                                  (self.plateCode, wellCode, self.experimentId, wellCode, val))
        

    def createWellsAndMaterials(self):
        template = self.parseLibraryTemplate()
        sanofiMaterials = self.fetchPlateCompounds()
        
        materialsByCode = {}
        for sanofiMaterial in sanofiMaterials:
            materialsByCode[ sanofiMaterial.materialCode ] = sanofiMaterial
        
        self.validate(template, materialsByCode)
        
        openbisMaterials = self.getOrCreateMaterials(template, materialsByCode)
        self.createWells(template, materialsByCode, openbisMaterials)

# ------------
# Image dataset registration
# ------------

def convertToPng(dir, transparentColor):
    delete_original_files = True
    strategy = Tiff2PngConversionStrategy(transparentColor, 0, delete_original_files)
    # Uses cores * machineLoad threads for the conversion, but not more than maxThreads
    machineLoad = ALLOWED_MACHINE_LOAD_DURING_THUMBNAIL_GENERATION
    maxThreads = 100
    errorMsg = FileConverter.performConversion(File(dir), strategy, machineLoad, maxThreads)
    if errorMsg != None:
        raise Exception("Error", errorMsg)

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
        wellText = basename[0:find(basename, "(")] # A - 1
        imageTokens.well = wellText.replace(" - ", "")
        
        if " wv " in basename:
            fieldText = basename[find(basename, "fld ") + 4 : find(basename, " wv")]
            imageTokens.channelCode = basename[rfind(basename, " - ") + 3 :-1]
        else:
            fieldText = basename[find(basename, "fld ") + 4 : find(basename, ")")]
            imageTokens.channelCode = "DEFAULT"
        
        try:
            imageTokens.tileNumber = int(fieldText)
        except ValueError:
            raise Exception("Cannot parse field number from '" + fieldText + "' in '" + basename + "' file name.")
    
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


if incoming.isDirectory():
    transaction = service.transaction(incoming, factory)
    
    (batchName, plateCode) = parseIncomingDirname(incoming.getName())
    plate = findPlateByCode(plateCode)
    if not plate.getExperiment():
        raise RuntimeError("Plate with code '%(plateCode)s' is not associated with experiment" % vars())
    
    experimentId = plate.getExperiment().getExperimentIdentifier()
    experiment = transaction.getExperiment(experimentId)
    
    # reload the sample with all contained samples
    plate = transaction.getSample(plate.getSampleIdentifier())
    if len(plate.getContainedSamples()) == 0:
        plateInitializer = PlateInitializer(transaction, plate)
        plateInitializer.createWellsAndMaterials()
        
    imageDatasetConfig = MyImageDataSetConfig(incoming, incoming)
    imageDatasetConfig.setRawImageDatasetType()
    imageDatasetConfig.setFileFormatType(IMAGE_DATASET_FILE_FORMAT)
    imageDatasetDetails = factory.createImageRegistrationDetails(imageDatasetConfig, incoming)
    imageDataSet = transaction.createNewDataSet(imageDatasetDetails)
    imageDataSet.setPropertyValue(IMAGE_DATASET_BATCH_PROPCODE, batchName)
    imageDataSet.setSample(plate)
    
    # check for overlays folder
    overlaysDir = findDir(incoming, OVERLAYS_DIR_PATTERN)
    if overlaysDir is not None:
        convertToPng(overlaysDir.getPath(), OVERLAYS_TRANSPARENT_COLOR)
        overlayDatasetConfig = MyImageDataSetConfig(overlaysDir, overlaysDir)
        overlayDatasetConfig.setSegmentationImageDatasetType()
        overlayDatasetConfig.setFileFormatType(OVERLAY_IMAGE_FILE_FORMAT)
        overlayDatasetDetails = factory.createImageRegistrationDetails(overlayDatasetConfig, overlaysDir)
        overlayDataset = transaction.createNewDataSet(overlayDatasetDetails)
        overlayDataset.setSample(imageDataSet.getSample())
        overlayDataset.setParentDatasets([ imageDataSet.getDataSetCode() ])
        transaction.moveFile(overlaysDir.getPath(), overlayDataset, "overlays")
    
    # transform and move analysis file
    analysisFile = findFileByExt(incoming, "xml")
    if analysisFile is not None:
        analysisDataSet = transaction.createNewDataSet(ScreeningConstants.DEFAULT_ANALYSIS_WELL_DATASET_TYPE)
        analysisDataSet.setSample(imageDataSet.getSample())
        analysisDataSet.setParentDatasets([ imageDataSet.getDataSetCode() ])
        analysisDataSet.setFileFormatType(ANALYSIS_FILE_FORMAT)
        analysisDataSet.setMeasuredData(False)
        analysisDataSet.setPropertyValue(ANALYSIS_RUN_PROPCODE, extractFileBasename(analysisFile.getName()))
        analysisDataSetFile = transaction.createNewFile(analysisDataSet, analysisFile.getName())
        GEExplorerImageAnalysisResultParser(analysisFile.getPath()).writeCSV(File(analysisDataSetFile))
    
    imageDataSetFolder = transaction.moveFile(incoming.getPath(), imageDataSet)
