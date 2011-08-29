"""
This is the analysis result dropbox which accepts single files with xml extension.
Each file should contain image analysis results on the well level.
It will be registered as one dataset. 
The naming convention of the xml file is the following:
    <any-text>_<raw-image-dataset-code>.xml
e.g.
    LC80463-RS101202_20110809142909177-826.xml
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
    
    tokens = utils.extractFileBasename(incoming.getName()).split('_')
    if (len(tokens) < 2):
        raise ValidationException("Incoming directory name "+incoming.getName()+" does not adhere to the naming convention <any-text>_<raw-image-dataset-code>.xml")
    parentDatasetCode = tokens[-1]
    (plate, experiment) = registration.findConnectedPlate(transaction, parentDatasetCode)
    plateCode = plate.getCode()
    
    registration.registerAnalysisData(incoming, plate, parentDatasetCode, transaction, factory)