"""
This is the segmentation images dropbox which accepts directories containing images.
Each directory will be registered as one dataset. 
The naming convention of the directory is the following:
    <raw-image-dataset-code>_<any-text>
e.g.
    20110809142909177-826_my_overlays
The dataset with the code specified in the directory name will become a parent of the new dataset.
The new dataset is supposed to contain segmentation results (as images) of the parent dataset. 
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

if incoming.isDirectory():
    experiment = None
    plateCode = None
    
    transaction = service.transaction(incoming, factory)
    
    parentDatasetCode = utils.extractFileBasename(incoming.getName()).split('_')[0]
    (plate, experiment) = registration.findConnectedPlate(transaction, parentDatasetCode)
    plateCode = plate.getCode()
    
    registration.registerSegmentationImages(incoming, plate, parentDatasetCode, transaction, factory)