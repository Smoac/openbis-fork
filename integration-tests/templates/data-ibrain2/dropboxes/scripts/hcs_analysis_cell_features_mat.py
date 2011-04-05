#! /usr/bin/env python

import commonImageDropbox
import commonDropbox

reload(commonImageDropbox)
reload(commonDropbox)

# Global variable where we set the iBrain2 id of the dataset at the beginning, 
# so that the rollback can use it as well.
iBrain2DatasetId = None

def rollback_service(service, throwable):
    global iBrain2DatasetId
    commonDropbox.createFailureStatus(iBrain2DatasetId, throwable, incoming)
        
def rollback_transaction(service, transaction, algorithmRunner, throwable):
    rollback_service(service, throwable)

if incoming.isDirectory():
    metadataParser = commonDropbox.DerivedDatasetMetadataParser(incoming.getPath())
    iBrain2DatasetId = metadataParser.getIBrain2DatasetId()
    commonDropbox.registerDerivedBlackBoxDataset(state, service, factory, incoming, metadataParser, 'HCS_ANALYSIS_CELL_FEATURES_MAT', 'MAT')
