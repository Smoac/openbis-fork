from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update import VocabularyUpdate, VocabularyTermUpdate
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.update import PropertyTypeUpdate
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update import SampleTypeUpdate, SampleUpdate
from ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update import ListUpdateValue
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update import ExperimentTypeUpdate, ExperimentUpdate
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update import DataSetTypeUpdate
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update import SpaceUpdate
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update import ProjectUpdate
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.update import PluginUpdate
from java.lang import UnsupportedOperationException
from ..definition_to_creation import PropertyTypeDefinitionToCreationType, VocabularyDefinitionToCreationType, VocabularyTermDefinitionToCreationType, \
                    PropertyAssignmentDefinitionToCreationType, SampleTypeDefinitionToCreationType, ExperimentTypeDefinitionToCreationType, \
                    DatasetTypeDefinitionToCreationType, SpaceDefinitionToCreationType, ProjectDefinitionToCreationType, ExperimentDefinitionToCreationType, \
                    SampleDefinitionToCreationType, ScriptDefinitionToCreationType
from .update_types import PropertyTypeCreationToUpdateType, VocabularyCreationToUpdateType, PropertyAssignmentCreationToUpdateType, \
                        SampleTypeCreationToUpdateType, ExperimentTypeCreationToUpdateType, DatasetTypeCreationToUpdateType, \
                        SpaceCreationToUpdateType, ProjectCreationToUpdateType, ExperimentCreationToUpdateType, \
                        SampleCreationToUpdateType, ScriptCreationToUpdateType, VocabularyTermCreationToUpdateType


class CreationToUpdateParserFactory(object):

    @staticmethod
    def get_parsers(creation_type):
        if creation_type == VocabularyDefinitionToCreationType:
            return [VocabularyCreationToUpdateParser()]
        if creation_type == VocabularyTermDefinitionToCreationType:
            return [VocabularyTermCreationToUpdateParser()]
        elif creation_type == PropertyTypeDefinitionToCreationType:
            return [PropertyTypeCreationToUpdateParser()]
        elif creation_type == SampleTypeDefinitionToCreationType:
            return [SampleTypeCreationToUpdateParser()]
        elif creation_type == ExperimentTypeDefinitionToCreationType:
            return [ExperimentTypeCreationToUpdateParser()]
        elif creation_type == DatasetTypeDefinitionToCreationType:
            return [DatasetTypeCreationToUpdateParser()]
        elif creation_type == SpaceDefinitionToCreationType:
            return [SpaceCreationToUpdateParser()]
        elif creation_type == ProjectDefinitionToCreationType:
            return [ProjectCreationToUpdateParser()]
        elif creation_type == ExperimentDefinitionToCreationType:
            return [ExperimentCreationToUpdateParser()]
        elif creation_type == SampleDefinitionToCreationType:
            return [SampleCreationToUpdateParser()]
        elif creation_type == ScriptDefinitionToCreationType:
            return [ScriptCreationToUpdateParser()]
        else:
            raise UnsupportedOperationException(
                "Creation type of " + creation_type + " is not supported.")


class VocabularyCreationToUpdateParser(object):

    def parse(self, creation, existing_vocabulary):
        vocabulary_update = VocabularyUpdate()
        vocabulary_update.vocabularyId = existing_vocabulary.permId
        vocabulary_update.setDescription(creation.description)
        return vocabulary_update

    def get_type(self):
        return VocabularyCreationToUpdateType


class VocabularyTermCreationToUpdateParser(object):

    def parse(self, creation, existing_term):
        vocabulary_term_update = VocabularyTermUpdate()
        vocabulary_term_update.vocabularyTermId = existing_term.permId
        vocabulary_term_update.setLabel(creation.label)
        vocabulary_term_update.setDescription(creation.description)
        return vocabulary_term_update

    def get_type(self):
        return VocabularyTermCreationToUpdateType


class PropertyTypeCreationToUpdateParser(object):

    def parse(self, creation, existing_property_type):
        property_type_update = PropertyTypeUpdate()
        property_type_update.typeId = existing_property_type.permId
        property_type_update.setLabel(creation.label)
        property_type_update.setDescription(creation.description)
        return property_type_update

    def get_type(self):
        return PropertyTypeCreationToUpdateType


class EntityTypeCreationToUpdateParser(object):

    def parseAssignments(self, creation, existing_entity_type):
        assignments_update = ListUpdateValue()
        for property_assignment in existing_entity_type.propertyAssignments:
            assignments_update.remove(property_assignment.permId)
        for property_assignment in creation.propertyAssignments:
            assignments_update.add(property_assignment)
        return assignments_update.getActions()


class SampleTypeCreationToUpdateParser(EntityTypeCreationToUpdateParser):

    def parse(self, creation, existing_sample_type):
        sample_type_update = SampleTypeUpdate()
        sample_type_update.typeId = existing_sample_type.permId
        sample_type_update.setAutoGeneratedCode(creation.autoGeneratedCode)
        sample_type_update.setGeneratedCodePrefix(creation.generatedCodePrefix)
        sample_type_update.setDescription(creation.description)
        sample_type_update.setValidationPluginId(creation.validationPluginId)
        sample_type_update.setPropertyAssignmentActions(self.parseAssignments(creation, existing_sample_type))

        return sample_type_update

    def get_type(self):
        return SampleTypeCreationToUpdateType


class ExperimentTypeCreationToUpdateParser(EntityTypeCreationToUpdateParser):

    def parse(self, creation, existing_experiment_type):
        experiment_type_update = ExperimentTypeUpdate()
        experiment_type_update.typeId = existing_experiment_type.permId
        experiment_type_update.setDescription(creation.description)
        experiment_type_update.setValidationPluginId(creation.validationPluginId)
        assignments_update = ListUpdateValue()
        experiment_type_update.setPropertyAssignmentActions(self.parseAssignments(creation, existing_experiment_type))
        return experiment_type_update

    def get_type(self):
        return ExperimentTypeCreationToUpdateType


class DatasetTypeCreationToUpdateParser(EntityTypeCreationToUpdateParser):

    def parse(self, creation, existing_dataset_type):
        dataset_type_update = DataSetTypeUpdate()
        dataset_type_update.typeId = existing_dataset_type.permId
        dataset_type_update.setValidationPluginId(creation.validationPluginId)
        assignments_update = ListUpdateValue()
        dataset_type_update.setPropertyAssignmentActions(self.parseAssignments(creation, existing_dataset_type))
        return dataset_type_update

    def get_type(self):
        return DatasetTypeCreationToUpdateType


class SpaceCreationToUpdateParser(object):

    def parse(self, creation, existing_space):
        space_update = SpaceUpdate()
        space_update.spaceId = existing_space.permId
        space_update.setDescription(creation.description)
        return space_update

    def get_type(self):
        return SpaceCreationToUpdateType


class ProjectCreationToUpdateParser(object):

    def parse(self, creation, existing_project):
        project_update = ProjectUpdate()
        project_update.projectId = existing_project.permId
        project_update.setDescription(creation.description)
        project_update.setSpaceId(creation.spaceId)
        return project_update

    def get_type(self):
        return ProjectCreationToUpdateType


class ExperimentCreationToUpdateParser(object):

    def parse(self, creation, existing_experiment):
        experiment_update = ExperimentUpdate()
        experiment_update.experimentId = existing_experiment.permId
        experiment_update.setProjectId(creation.projectId)
        experiment_update.setProperties(creation.properties)
        return experiment_update

    def get_type(self):
        return ExperimentCreationToUpdateType


class SampleCreationToUpdateParser(object):

    def parse(self, creation, existing_sample):
        sample_update = SampleUpdate()
        sample_update.sampleId = existing_sample.permId
        sample_update.setExperimentId(creation.experimentId)
        sample_update.setProjectId(creation.projectId)
        sample_update.setSpaceId(creation.spaceId)
        sample_update.setProperties(creation.properties)
        sample_update.childIds.remove([parent.permId for parent in existing_sample.children])
        sample_update.parentIds.remove([child.permId for child in existing_sample.parents])
        sample_update.childIds.add(creation.childIds)
        sample_update.parentIds.add(creation.parentIds)

        return sample_update

    def get_type(self):
        return SampleCreationToUpdateType


class ScriptCreationToUpdateParser(object):

    def parse(self, creation, existing_plugin):
        script_update = PluginUpdate()
        script_update.pluginId = existing_plugin.permId
        script_update.setScript(creation.script)
        return script_update

    def get_type(self):
        return ScriptCreationToUpdateType

