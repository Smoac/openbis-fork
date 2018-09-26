from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create import CreateDataSetTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create import CreateExperimentTypesOperation, CreateExperimentsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.create import CreatePluginsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create import CreateProjectsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create import CreatePropertyTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create import CreateSampleTypesOperation, CreateSamplesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create import CreateSpacesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create import CreateVocabulariesOperation
from ..definition_to_creation import VocabularyDefinitionToCreationParser, PropertyTypeDefinitionToCreationParser, SampleTypeDefinitionToCreationParser, \
                    ExperimentTypeDefinitionToCreationParser, DatasetTypeDefinitionToCreationParser, SpaceDefinitionToCreationParser, \
                    ProjectDefinitionToCreationParser, ExperimentDefinitionToCreationParser, ScriptDefinitionToCreationParser, SampleDefinitionToCreationParser


class CreationToOperationParser(object):

    @staticmethod
    def parse(creations):
        creation_operations = []
        if VocabularyDefinitionToCreationParser.type in creations:
            creation_operations.append(CreateVocabulariesOperation(creations[VocabularyDefinitionToCreationParser.type]))
        if PropertyTypeDefinitionToCreationParser.type in creations:
            creation_operations.append(CreatePropertyTypesOperation(creations[PropertyTypeDefinitionToCreationParser.type]))
        if SampleTypeDefinitionToCreationParser.type in creations:
            creation_operations.append(CreateSampleTypesOperation(creations[SampleTypeDefinitionToCreationParser.type]))
        if ExperimentTypeDefinitionToCreationParser.type in creations:
            creation_operations.append(CreateExperimentTypesOperation(creations[ExperimentTypeDefinitionToCreationParser.type]))
        if DatasetTypeDefinitionToCreationParser.type in creations:
            creation_operations.append(CreateDataSetTypesOperation(creations[DatasetTypeDefinitionToCreationParser.type]))
        if SpaceDefinitionToCreationParser.type in creations:
            creation_operations.append(CreateSpacesOperation(creations[SpaceDefinitionToCreationParser.type]))
        if ProjectDefinitionToCreationParser.type in creations:
            creation_operations.append(CreateProjectsOperation(creations[ProjectDefinitionToCreationParser.type]))
        if ExperimentDefinitionToCreationParser.type in creations:
            creation_operations.append(CreateExperimentsOperation(creations[ExperimentDefinitionToCreationParser.type]))
        if SampleDefinitionToCreationParser.type in creations:
            creation_operations.append(CreateSamplesOperation(creations[SampleDefinitionToCreationParser.type]))
        if ScriptDefinitionToCreationParser.type in creations:
            creation_operations.append(CreatePluginsOperation(creations[ScriptDefinitionToCreationParser.type]))
        return creation_operations
