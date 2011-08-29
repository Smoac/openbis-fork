"""
This is the dropbox which accepts directories containing segmentation images and analysis results.

- if the incoming item is a file, then it is expected to contain analysis results and have xml extension.
The naming convention of the file is the following:
    <any-text>_<raw-image-dataset-code>.xml
e.g.
    myAnalysis_20110809142909177-826.xml

- if the incoming item is a directory, then it's expected to contain an xml file with analysis results
and a directory (with 'ROITiff' suffix) with image segmentation results.
Two datasets will be registered.
The naming convention of the incoming directory is the following:
    <any-text>_<raw-image-dataset-code>
e.g.
    myOverlaysAndAnalysis_20110809142909177-826

The dataset with the code specified in the file or directory name will become a parent of the new datasets.
The new datasets are supposed to contain segmentation and analysis results of the parent dataset. 
"""

import utils
import config
import notifications 
import registration

from ch.systemsx.cisd.openbis.generic.shared.basic.dto.api import ValidationException

reload(utils)
reload(config)
# Can be switched on manually only after the module's code has been changed on the fly:
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
    
def findConnectedPlate(incoming):
    transaction = service.transaction(incoming, factory)

    tokens = utils.extractFileBasename(incoming.getName()).split('_')
    if (len(tokens) < 2):
        raise ValidationException("Incoming directory name "+incoming.getName()+" does not adhere to the naming convention <any-text>_<raw-image-dataset-code>")
    parentDatasetCode = tokens[-1]
    (plate, experiment) = registration.findConnectedPlate(transaction, parentDatasetCode)
    return (transaction, parentDatasetCode, plate, experiment)
	
# ---------------------

global experiment
global plateCode 

experiment = None
plateCode = None

if incoming.isDirectory():
    transaction, parentDatasetCode, plate, experiment = findConnectedPlate(incoming)
    plateCode = plate.getCode()

    # transform and move analysis file
    analysisFile = utils.findFileByExt(incoming, "xml")
    if analysisFile is not None:
        analysisProcedureCode = registration.registerAnalysisData(analysisFile, plate, parentDatasetCode, transaction, factory)

        # check for overlays folder
        overlaysDir = utils.findDir(incoming, config.OVERLAYS_DIR_PATTERN)
        if overlaysDir is not None:
            registration.registerSegmentationImages(overlaysDir, plate, parentDatasetCode, analysisProcedureCode, transaction, factory)
else:
    fileExt = utils.getFileExt(incoming.getName())
    if fileExt == "xml":
        transaction, parentDatasetCode, plate, experiment = findConnectedPlate(incoming)
        plateCode = plate.getCode()

        registration.registerAnalysisData(incoming, plate, parentDatasetCode, transaction, factory)

