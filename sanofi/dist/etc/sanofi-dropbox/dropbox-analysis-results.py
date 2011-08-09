"""
This is the analysis result dropbox which accepts single files with xml extension.
Each file should contain image analysis results on the well level.
It will be registered as one dataset. 
The naming convention of the xml file is the following:
    <raw-image-dataset-code>_<any-text>.xml
e.g.
    20110809142909177-826_LC80463-RS101202.xml
The dataset with the code specified in the file name will become a parent of the new dataset.     
"""
import utils
import notifications 
import registration

from ch.systemsx.cisd.openbis.generic.shared.basic.dto.api import ValidationException

reload(utils)
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
    
# ---------------------

global experiment
global plateCode 

fileExt = utils.getFileExt(incoming.getName())
if fileExt == "xml":
    experiment = None
    plateCode = None
    
    transaction = service.transaction(incoming, factory)
    
    parentDatasetCode = utils.extractFileBasename(incoming.getName()).split('_')[0]
    (plate, experiment) = registration.findConnectedPlate(transaction, parentDatasetCode)
    plateCode = plate.getCode()
    
    registration.registerAnalysisData(incoming, plate, parentDatasetCode, transaction, factory)