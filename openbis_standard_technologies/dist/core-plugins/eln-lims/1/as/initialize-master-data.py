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
elnTypes.createVocabularyWithTerms(tr, elnTypes.HOST);
elnTypes.createVocabularyWithTerms(tr, elnTypes.DETECTION);
elnTypes.createVocabularyWithTerms(tr, elnTypes.STORAGE);
elnTypes.createVocabularyWithTerms(tr, elnTypes.CLONALITY);
elnTypes.createVocabularyWithTerms(tr, elnTypes.BACKBONE);
elnTypes.createVocabularyWithTerms(tr, elnTypes.BACTERIAL_ANTIBIOTIC_RESISTANCE);
elnTypes.createVocabularyWithTerms(tr, elnTypes.MARKER);
elnTypes.createVocabularyWithTerms(tr, elnTypes.STERILIZATION);
elnTypes.createVocabularyWithTerms(tr, elnTypes.GENETIC_BACKGROUND);
elnTypes.createVocabularyWithTerms(tr, elnTypes.MATING_TYPE);
elnTypes.createVocabularyWithTerms(tr, elnTypes.BACKGROUND_SPECIFIC_MARKERS);
elnTypes.createVocabularyWithTerms(tr, elnTypes.COMMON_MARKERS);
elnTypes.createVocabularyWithTerms(tr, elnTypes.ENDOGENOUS_PLASMID);
elnTypes.createVocabularyWithTerms(tr, elnTypes.DIRECTION);
elnTypes.createVocabularyWithTerms(tr, elnTypes.STRAND);
elnTypes.createVocabularyWithTerms(tr, elnTypes.RNA_TYPE);
elnTypes.createVocabularyWithTerms(tr, elnTypes.RNA_BACKBONE);
elnTypes.createVocabularyWithTerms(tr, elnTypes.ORIGIN);
elnTypes.createVocabularyWithTerms(tr, elnTypes.CHECK);
elnTypes.createVocabularyWithTerms(tr, elnTypes.PROTOCOL_TYPE);
elnTypes.createVocabularyWithTerms(tr, elnTypes.TEMPLATE);
elnTypes.createVocabularyWithTerms(tr, elnTypes.YES_NO_CHOICE);
elnTypes.createVocabularyWithTerms(tr, elnTypes.MEMBRANE);
elnTypes.createVocabularyWithTerms(tr, elnTypes.SPECIES);
elnTypes.createVocabularyWithTerms(tr, elnTypes.CELL_MEDIUM);
elnTypes.createVocabularyWithTerms(tr, elnTypes.OWNER);
elnTypes.createVocabularyWithTerms(tr, elnTypes.CELL_TYPE);
elnTypes.createVocabularyWithTerms(tr, elnTypes.ORGANISM);
elnTypes.createVocabularyWithTerms(tr, elnTypes.EXPERIMENTAL_READOUT);
elnTypes.createVocabularyWithTerms(tr, elnTypes.MACHINE);
elnTypes.createVocabularyWithTerms(tr, elnTypes.PLASMID_RELATIONSHIP);
elnTypes.createVocabularyWithTerms(tr, elnTypes.STORAGE_NAMES);
elnTypes.createVocabularyWithTerms(tr, elnTypes.STORAGE_BOX_SIZE);
elnTypes.createVocabularyWithTerms(tr, elnTypes.COLOR_ENCODED_ANNOTATIONS);
elnTypes.createVocabularyWithTerms(tr, elnTypes.ELN_TYPES_METADATA);

##
## Property Types for annotations
##
elnTypes.createProperty(tr, "COMMENTS", DataType.MULTILINE_VARCHAR, "Comments", "", None);
elnTypes.createProperty(tr, "QUANTITY", DataType.VARCHAR, "Quantity", "", None);
elnTypes.createProperty(tr, "PLASMID_ANNOTATION", DataType.VARCHAR, "Plasmid annotation", "", None);
elnTypes.createProperty(tr, "PLASMID_RELATIONSHIP", DataType.CONTROLLEDVOCABULARY, "Plasmid relationship", "", "PLASMID_RELATIONSHIP");
elnTypes.createProperty(tr, "CONTAINED", DataType.VARCHAR, "Contained", "", None);
elnTypes.createProperty(tr, "COLOR_ENCODED_ANNOTATION", DataType.CONTROLLEDVOCABULARY, "Color Annotation for plate wells", "", "COLOR_ENCODED_ANNOTATIONS");

##
## DataSet Types
##
elnTypes.getCommentsScript(tr, "DATA_SET");
elnTypes.createDataSetTypeWithProperties(tr, elnTypes.ELN_PREVIEW);
elnTypes.createDataSetTypeWithProperties(tr, elnTypes.SEQ_FILE);
elnTypes.createDataSetTypeWithProperties(tr, elnTypes.RAW_DATA);
elnTypes.createDataSetTypeWithProperties(tr, elnTypes.ANALYZED_DATA);
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
elnTypes.getCommentsScript(tr, "SAMPLE");
elnTypes.getGeneticModificationsScript(tr, "SAMPLE")

elnTypes.getAnnotationsScript(tr, "ANTIBODY");
elnTypes.createSampleTypeWithProperties(tr, elnTypes.ANTIBODY);

elnTypes.getAnnotationsScript(tr, "CHEMICAL");
elnTypes.createSampleTypeWithProperties(tr, elnTypes.CHEMICAL);

elnTypes.getAnnotationsScript(tr, "ENZYME");
elnTypes.createSampleTypeWithProperties(tr, elnTypes.ENZYME);

elnTypes.getAnnotationsScript(tr, "MEDIA");
elnTypes.createSampleTypeWithProperties(tr, elnTypes.MEDIA);

elnTypes.getAnnotationsScript(tr, "SOLUTION_BUFFER");
elnTypes.createSampleTypeWithProperties(tr, elnTypes.SOLUTION_BUFFER);

elnTypes.getAnnotationsScript(tr, "OLIGO");
elnTypes.createSampleTypeWithProperties(tr, elnTypes.OLIGO);

elnTypes.getAnnotationsScript(tr, "RNA");
elnTypes.createSampleTypeWithProperties(tr, elnTypes.RNA);

elnTypes.getAnnotationsScript(tr, "PLASMID");
elnTypes.createSampleTypeWithProperties(tr, elnTypes.PLASMID);

elnTypes.getAnnotationsScript(tr, "BACTERIA");
elnTypes.createSampleTypeWithProperties(tr, elnTypes.BACTERIA);

elnTypes.getAnnotationsScript(tr, "YEAST");
elnTypes.createSampleTypeWithProperties(tr, elnTypes.YEAST);

elnTypes.getAnnotationsScript(tr, "CELL_LINE");
elnTypes.createSampleTypeWithProperties(tr, elnTypes.CELL_LINE);

elnTypes.getAnnotationsScript(tr, "FLY");
elnTypes.createSampleTypeWithProperties(tr, elnTypes.FLY);

elnTypes.addPropertiesToSamples(tr, [
	"ANTIBODY",
	"BACTERIA",
	"CHEMICAL",
	"ENZYME",
	"CELL_LINE",
	"FLY",
	"MEDIA",
	"OLIGO",
	"PLASMID",
	"YEAST",
	"SOLUTION_BUFFER",
	"RNA"
	], elnTypes.STORAGE_PROPERTIES);

##
## Sample Types - Non Materials
##
elnTypes.getAnnotationsScript(tr, "EXPERIMENTAL_STEP");
elnTypes.createSampleTypeWithProperties(tr, elnTypes.EXPERIMENTAL_STEP);

elnTypes.getAnnotationsScript(tr, "GENERAL_PROTOCOL");
elnTypes.createSampleTypeWithProperties(tr, elnTypes.GENERAL_PROTOCOL);

elnTypes.getAnnotationsScript(tr, "PCR_PROTOCOL");
elnTypes.createSampleTypeWithProperties(tr, elnTypes.PCR_PROTOCOL);

elnTypes.getAnnotationsScript(tr, "WESTERN_BLOTTING_PROTOCOL");
elnTypes.createSampleTypeWithProperties(tr, elnTypes.WESTERN_BLOTTING_PROTOCOL);

elnTypes.getAnnotationsScript(tr, "DRAWING_BOARD");
elnTypes.createSampleTypeWithProperties(tr, elnTypes.DRAWING_BOARD_SAMPLE);