#! /usr/bin/env python

import commonImageDropbox
import commonDropbox

reload(commonDropbox)
reload(commonImageDropbox)

# Global variable storing AbstractPropertiesParser
datasetMetadataParser = None

def rollback_service(service, throwable):
    global datasetMetadataParser
    commonDropbox.createFailureStatus(datasetMetadataParser, throwable, incoming)
        
if incoming.isDirectory():
    datasetMetadataParser = commonDropbox.DerivedDatasetMetadataParser(incoming.getPath())
    commonDropbox.registerDerivedBlackBoxDataset(state, service, factory, incoming, datasetMetadataParser, 'HCS_ANALYSIS_IMAGE_ACQUISITION_METADATA', 'CSV', ensureSingleChild = True)
