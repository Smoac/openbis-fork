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

tr = service.transaction();

##
## Vocabulary Types
##
elnTypes.createVocabularyWithTerms(tr, elnTypes.OWNER);
elnTypes.createVocabularyWithTerms(tr, elnTypes.ELN_TYPES_METADATA_MINI);

##
## Property Types for annotations
##

##
## DataSet Types
##
elnTypes.getCommentsScript(tr, "DATA_SET");
elnTypes.createDataSetTypeWithProperties(tr, elnTypes.ELN_PREVIEW);
elnTypes.createDataSetTypeWithProperties(tr, elnTypes.ATTACHMENT);
elnTypes.createDataSetTypeWithProperties(tr, elnTypes.DRAWING_BOARD_EXPERIMENT);

##
## Experiment Types
##
elnTypes.getCommentsScript(tr, "EXPERIMENT");
elnTypes.createExperimentTypeWithProperties(tr, elnTypes.MATERIALS);
elnTypes.createExperimentTypeWithProperties(tr, elnTypes.METHODS);
elnTypes.createExperimentTypeWithProperties(tr, elnTypes.BOARDS);
elnTypes.createExperimentTypeWithProperties(tr, elnTypes.DEFAULT_EXPERIMENT);
    
##
## Sample Types - Materials
##

##
## Sample Types - Non Materials
##
elnTypes.createSampleTypeWithProperties(tr, elnTypes.EXPERIMENTAL_STEP_MINI);