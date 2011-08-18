"""
This is the dropbox which registers raw images, segmentation images and analysis results
at the same time.
Each directory should contain:
- raw images on the main level
- optionally: segmentation images in the directory containin "_ROITiff"
- optionally: analysis results in the xml file 
If all kinds of data are provided, 3 datasets will be registered for each incoming directory. 
Segmentation and analysis dataset will be linked to the raw images dataset.
"""

import utils
import config
import plateinitializer
import notifications 
import registration

from ch.systemsx.cisd.openbis.generic.shared.basic.dto.api import ValidationException

from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria 
from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria import MatchClause, MatchClauseAttribute

reload(utils)
reload(config)
# Can be switched on manually only after the module's code has been changed on the fly:
#reload(plateinitializer)
#reload(notifications)
#reload(registration)

def createEmailUtils():
    global experiment
    global plateCode

    return notifications.EmailUtils(state, incoming, experiment, plateCode)

def commit_transaction(service, transaction):
    createEmailUtils().sendSuccessConfirmation()

def rollback_service(service, ex):
    createEmailUtils().sendRollbackError(ex)
    
def findPlateByCode(transaction, code):
    """
       Finds a plate (openBIS sample) matching a specified bar code.
    """
    criteria = SearchCriteria()
    criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE, config.PLATE_TYPE))
    criteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, code))
    
    searchService = transaction.getSearchService()
    platesFound = list(searchService.searchForSamples(criteria))
    
    if not platesFound:
        raise ValidationException("Plate with code '%(code)s' does not exist in openBIS. "
                                  "Please check if the barcode provided in the folder name is correct "
                                  "or register the plate in openBIS." % vars())
    if len(platesFound) > 1:
        raise ValidationException("There is more than one plate with code '%(code)s' in openBIS.\n"
                                  "Plate barcodes should be unique, you may have to delete some plates from openBIS." % vars())
        
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
    

# ---------------------


global experiment
global plateCode 

if incoming.isDirectory():
    experiment = None
    plateCode = None
    
    transaction = service.transaction(incoming, factory)
    
    (batchName, plateCode) = parseIncomingDirname(incoming.getName())
    plate = findPlateByCode(transaction, plateCode)
    if not plate.getExperiment():
        raise ValidationException("Plate with code '%(plateCode)s' is not associated with experiment" % vars())
    
    experimentId = plate.getExperiment().getExperimentIdentifier()
    experiment = transaction.getExperiment(experimentId)
    
    # reload the sample with all contained samples
    plate = transaction.getSample(plate.getSampleIdentifier())
    if len(plate.getContainedSamples()) == 0:
        plateInitializer = plateinitializer.PlateInitializer(transaction, state, plate, experiment, config.TEST_MODE)
        plateInitializer.createWellsAndMaterials()
        
    imageDataSet = registration.createRawImagesDataset(incoming, plate, batchName, transaction, factory)
    imageDataSetCode = imageDataSet.getDataSetCode()

    analysisProcedureCode = None
    
    # transform and move analysis file
    analysisFile = utils.findFileByExt(incoming, "xml")
    if analysisFile is not None:
        analysisProcedureCode = registration.registerAnalysisData(analysisFile, plate, imageDataSetCode, transaction, factory)

    # check for overlays folder
    overlaysDir = utils.findDir(incoming, config.OVERLAYS_DIR_PATTERN)
    if overlaysDir is not None:
        registration.registerSegmentationImages(overlaysDir, plate, imageDataSetCode, analysisProcedureCode, transaction, factory)
    
    transaction.moveFile(incoming.getPath(), imageDataSet)
      
