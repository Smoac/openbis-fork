#
# Copyright 2014 ETH Zuerich, Scientific IT Services
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##
## Configuration
##
import elnTypes

# MasterDataRegistrationTransaction Class
import os
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType as DataType

def initBasicMasterData(tr):
    ##
    ## Vocabulary Types
    ##
    elnTypes.createVocabularyWithTerms(tr, elnTypes.OWNER);
    elnTypes.createVocabularyWithTerms(tr, elnTypes.EXPERIMENTAL_READOUT);
    elnTypes.createVocabularyWithTerms(tr, elnTypes.MACHINE);
    elnTypes.createVocabularyWithTerms(tr, elnTypes.STORAGE_VALIDATION_LEVEL);
    elnTypes.createVocabularyWithTerms(tr, elnTypes.STORAGE_BOX_SIZE);
    
    ##
    ## Property Types for annotations
    ##
    
    ##
    ## DataSet Types
    ##
    elnTypes.getCommentsScript(tr, "DATA_SET");
    elnTypes.createDataSetTypeWithProperties(tr, elnTypes.ELN_PREVIEW);
    elnTypes.createDataSetTypeWithProperties(tr, elnTypes.ATTACHMENT);
    
    ##
    ## Experiment Types
    ##
    elnTypes.getCommentsScript(tr, "EXPERIMENT");
    elnTypes.createExperimentTypeWithProperties(tr, elnTypes.MATERIALS);
    elnTypes.createExperimentTypeWithProperties(tr, elnTypes.METHODS);
    elnTypes.createExperimentTypeWithProperties(tr, elnTypes.COLLECTION);
    elnTypes.createExperimentTypeWithProperties(tr, elnTypes.DEFAULT_EXPERIMENT);
        
    ##
    ## Sample Types - Materials
    ##
    
    ##
    ## Sample Types - Non Materials
    ##
    elnTypes.createSampleTypeWithProperties(tr, elnTypes.GENERAL_ELN_SETTINGS);
    elnTypes.createSampleTypeWithProperties(tr, elnTypes.EXPERIMENTAL_STEP);
    elnTypes.createSampleTypeWithProperties(tr, elnTypes.STORAGE_RACK);
    elnTypes.createSampleTypeWithProperties(tr, elnTypes.STORAGE_POSITION);
    ##
    ## Ordering System
    ##
    elnTypes.createProperty(tr, "QUANTITY_OF_ITEMS", DataType.INTEGER, "Quantity", "", None);
     
    elnTypes.createVocabularyWithTerms(tr, elnTypes.LANGUAGE);
    elnTypes.createVocabularyWithTerms(tr, elnTypes.CURRENCY);
    elnTypes.createVocabularyWithTerms(tr, elnTypes.PREFERRED_ORDER_METHOD);
    elnTypes.createVocabularyWithTerms(tr, elnTypes.ORDER_STATUS);
     
    elnTypes.createExperimentTypeWithProperties(tr, elnTypes.STOCK);
     
    elnTypes.createSampleTypeWithProperties(tr, elnTypes.SUPPLIER);
    elnTypes.createSampleTypeWithProperties(tr, elnTypes.PRODUCT);
    elnTypes.createSampleTypeWithProperties(tr, elnTypes.REQUEST);
    elnTypes.createSampleTypeWithProperties(tr, elnTypes.ORDER);

# This script can be executed alone or included into a longer master data script.
# on the second case, service doesn't exist and will not execute if the initBasicMasterData is not called
tr = None;
try:
    tr = service.transaction();
    initBasicMasterData(tr);
except:
    pass