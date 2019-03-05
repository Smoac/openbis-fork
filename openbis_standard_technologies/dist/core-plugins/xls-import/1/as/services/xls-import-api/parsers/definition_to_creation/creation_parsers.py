from ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id import CreationId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create import DataSetTypeCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id import EntityTypePermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create import ExperimentTypeCreation, ExperimentCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin import PluginType
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.create import PluginCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id import PluginPermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create import ProjectCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property import DataType
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create import PropertyAssignmentCreation, PropertyTypeCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id import PropertyTypePermId
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create import SampleTypeCreation, SampleCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create import SpaceCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create import VocabularyCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create import VocabularyTermCreation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id import VocabularyPermId
from java.lang import UnsupportedOperationException
from utils.openbis_utils import is_internal_namespace, get_script_name_for


def get_boolean_from_string(text):
    return True if text and text.lower() == u'true' else False


class DefinitionToCreationParserFactory(object):

    @staticmethod
    def get_parsers(definition, context):
        if definition.type == u'VOCABULARY_TYPE':
            return [VocabularyDefinitionToCreationParser()]
        elif definition.type == u'SAMPLE_TYPE':
            return [SampleTypeDefinitionToCreationParser(), PropertyTypeDefinitionToCreationParser(),
                    ScriptDefinitionToCreationParser(context)]
        elif definition.type == u'EXPERIMENT_TYPE':
            return [ExperimentTypeDefinitionToCreationParser(), PropertyTypeDefinitionToCreationParser(),
                    ScriptDefinitionToCreationParser(context)]
        elif definition.type == u'DATASET_TYPE':
            return [DatasetTypeDefinitionToCreationParser(), PropertyTypeDefinitionToCreationParser(),
                    ScriptDefinitionToCreationParser(context)]
        elif definition.type == u'SPACE':
            return [SpaceDefinitionToCreationParser()]
        elif definition.type == u'PROJECT':
            return [ProjectDefinitionToCreationParser()]
        elif definition.type == u'EXPERIMENT':
            return [ExperimentDefinitionToCreationParser()]
        elif definition.type == u'SAMPLE':
            return [SampleDefinitionToCreationParser()]
        elif definition.type == u'PROPERTY_TYPE':
            return [PropertyTypeDefinitionToCreationParser()]
        else:
            raise UnsupportedOperationException(
                "Definition of " + str(definition.type) + " is not supported (probably yet).")


class DefinitionToCreationParser(object):
    pass


class PropertyTypeDefinitionToCreationParser(DefinitionToCreationParser):
    type = "PropertyTypeCreation"

    def parse(self, definition):
        property_creations = []

        for prop in definition.properties:
            property_type_creation = PropertyTypeCreation()
            property_type_creation.code = prop.get(u'code')
            property_type_creation.label = prop.get(u'property label')
            property_type_creation.description = prop.get(u'description')
            property_type_creation.dataType = DataType.valueOf(prop.get(u'data type'))
            property_type_creation.internalNameSpace = is_internal_namespace(prop.get(u'code'))
            property_type_creation.vocabularyId = VocabularyPermId(prop.get(u'vocabulary code')) if prop.get(u'vocabulary code') is not None else None
            property_creations.append(property_type_creation)

        return property_creations

    def get_type(self):
        return PropertyTypeDefinitionToCreationParser.type


class VocabularyDefinitionToCreationParser(DefinitionToCreationParser):
    type = "VocabularyCreation"

    def parse(self, definition):
        code = definition.attributes.get(u'code')
        vocabulary_creation = VocabularyCreation()
        vocabulary_creation.code = code
        vocabulary_creation.internalNameSpace = is_internal_namespace(code)
        vocabulary_creation.description = definition.attributes.get(u'description')

        vocabulary_creations_terms = []
        for prop in definition.properties:
            vocabulary_creation_term = VocabularyTermCreation()
            vocabulary_creation_term.code = prop.get(u'code')
            vocabulary_creation_term.label = prop.get(u'label')
            vocabulary_creation_term.description = prop.get(u'description')
            vocabulary_creations_terms.append(vocabulary_creation_term)

        vocabulary_creation.terms = vocabulary_creations_terms

        return vocabulary_creation

    def get_type(self):
        return VocabularyDefinitionToCreationParser.type


class PropertyAssignmentDefinitionToCreationParser(DefinitionToCreationParser):
    type = "PropertyAssignmentCreation"

    def parse(self, prop):
        code = prop.get(u'code')
        property_assingment_creation = PropertyAssignmentCreation()
        is_mandatory = get_boolean_from_string(prop.get(u'mandatory'))
        property_assingment_creation.mandatory = is_mandatory
        should_show_in_edit_view = get_boolean_from_string(prop.get(u'show in edit views'))
        property_assingment_creation.showInEditView = should_show_in_edit_view
        property_assingment_creation.section = prop.get(u'section')
        property_assingment_creation.propertyTypeId = PropertyTypePermId(code)
        if u'dynamic script' in prop and prop.get(u'dynamic script') is not None:
            dynamic_script_path = prop.get(u'dynamic script')
            property_assingment_creation.pluginId = PluginPermId(get_script_name_for(code, dynamic_script_path))

        return property_assingment_creation

    def get_type(self):
        return PropertyAssignmentDefinitionToCreationParser.type


class SampleTypeDefinitionToCreationParser(DefinitionToCreationParser):
    type = "SampleTypeCreation"

    def parse(self, definition):
        code = definition.attributes.get(u'code')
        sample_creation = SampleTypeCreation()
        sample_creation.code = code
        should_auto_generate_codes = get_boolean_from_string(definition.attributes.get(u'auto generate codes'))
        sample_creation.autoGeneratedCode = should_auto_generate_codes
        generatedCodePrefix = definition.attributes.get(u'generated code prefix')
        if generatedCodePrefix is not None:
            sample_creation.generatedCodePrefix = generatedCodePrefix
        if u'validation script' in definition.attributes and definition.attributes.get(u'validation script') is not None:
            validation_script_path = definition.attributes.get(u'validation script')
            sample_creation.validationPluginId = PluginPermId(get_script_name_for(code, validation_script_path))

        property_assingment_creations = []
        property_assignment_parser = PropertyAssignmentDefinitionToCreationParser()
        for prop in definition.properties:
            property_assingment_creation = property_assignment_parser.parse(prop)
            property_assingment_creations.append(property_assingment_creation)

        sample_creation.propertyAssignments = property_assingment_creations
        return sample_creation

    def get_type(self):
        return SampleTypeDefinitionToCreationParser.type


class ExperimentTypeDefinitionToCreationParser(DefinitionToCreationParser):
    type = "ExperimentTypeCreation"

    def parse(self, definition):
        code = definition.attributes.get(u'code')
        experiment_type_creation = ExperimentTypeCreation()
        experiment_type_creation.code = code
        if u'validation script' in definition.attributes and definition.attributes.get(u'validation script') is not None:
            validation_script_path = definition.attributes.get(u'validation script')
            experiment_type_creation.validationPluginId = PluginPermId(get_script_name_for(code, validation_script_path))

        property_assingment_creations = []
        property_assignment_parser = PropertyAssignmentDefinitionToCreationParser()
        for prop in definition.properties:
            property_assingment_creation = property_assignment_parser.parse(prop)
            property_assingment_creations.append(property_assingment_creation)

        experiment_type_creation.propertyAssignments = property_assingment_creations
        return experiment_type_creation

    def get_type(self):
        return ExperimentTypeDefinitionToCreationParser.type


class DatasetTypeDefinitionToCreationParser(DefinitionToCreationParser):
    type = "DatasetTypeCreation"

    def parse(self, definition):
        dataset_type_creation = DataSetTypeCreation()
        code = definition.attributes.get(u'code')
        dataset_type_creation.code = code
        if u'validation script' in definition.attributes and definition.attributes.get(u'validation script') is not None:
            validation_script_path = definition.attributes.get(u'validation script')
            dataset_type_creation.validationPluginId = PluginPermId(get_script_name_for(code, validation_script_path))

        property_assingment_creations = []
        property_assignment_parser = PropertyAssignmentDefinitionToCreationParser()
        for prop in definition.properties:
            property_assingment_creation = property_assignment_parser.parse(prop)
            property_assingment_creations.append(property_assingment_creation)

        dataset_type_creation.propertyAssignments = property_assingment_creations
        return dataset_type_creation

    def get_type(self):
        return DatasetTypeDefinitionToCreationParser.type


class SpaceDefinitionToCreationParser(DefinitionToCreationParser):
    type = "SpaceCreation"

    def parse(self, definition):
        space_creations = []
        for prop in definition.properties:
            space_creation = SpaceCreation()
            space_creation.code = prop.get(u'code')
            space_creation.description = prop.get(u'description')
            creation_id = prop.get(u'code')
            space_creation.creationId = CreationId(creation_id)
            space_creations.append(space_creation)
        return space_creations

    def get_type(self):
        return SpaceDefinitionToCreationParser.type


class ProjectDefinitionToCreationParser(DefinitionToCreationParser):
    type = "ProjectCreation"

    def parse(self, definition):
        project_creations = []
        for prop in definition.properties:
            project_creation = ProjectCreation()
            project_creation.code = prop.get(u'code')
            project_creation.description = prop.get(u'description')
            project_creation.spaceId = CreationId(prop.get(u'space'))
            creation_id = prop.get(u'code')
            project_creation.creationId = CreationId(creation_id)
            project_creations.append(project_creation)
        return project_creations

    def get_type(self):
        return ProjectDefinitionToCreationParser.type


class ExperimentDefinitionToCreationParser(DefinitionToCreationParser):
    type = "ExperimentCreation"

    def parse(self, definition):
        experiments = []
        mandatory_attributes = [u'code', u'project']
        for experiment_properties in definition.properties:
            experiment_creation = ExperimentCreation()
            experiment_creation.typeId = EntityTypePermId(definition.attributes.get(u'experiment type'))
            experiment_creation.code = experiment_properties.get(u'code')
            experiment_creation.projectId = CreationId(experiment_properties.get(u'project'))
            creation_id = experiment_properties.get(u'code')
            experiment_creation.creationId = CreationId(creation_id)
            for prop, value in experiment_properties.items():
                if prop not in mandatory_attributes:
                    experiment_creation.setProperty(prop, value)
            experiments.append(experiment_creation)
        return experiments

    def get_type(self):
        return ExperimentDefinitionToCreationParser.type


class SampleDefinitionToCreationParser(DefinitionToCreationParser):
    type = "SampleCreation"

    def parse(self, definition):
        samples = []
        mandatory_attributes = [u'$', u'code', u'space', u'project', u'experiment', u'auto generate code', u'parents',
                                u'children']
        for sample_properties in definition.properties:
            sample_creation = SampleCreation()
            sample_creation.typeId = EntityTypePermId(definition.attributes.get(u'sample type'))
            if u'code' in sample_properties and sample_properties.get(u'code') is not None:
                sample_creation.code = sample_properties.get(u'code')
                creation_id = sample_properties.get(u'code')
                sample_creation.creationId = CreationId(creation_id)
            if u'$' in sample_properties and sample_properties.get(u'$') is not None:
                # may overwrite creationId from code, which is intended
                sample_creation.creationId = CreationId(sample_properties.get(u'$'))
            if u'auto generate code' in sample_properties and sample_properties.get(u'auto generate code') is not None and \
                    sample_properties.get(u'auto generate code') != '':
                sample_creation.autoGeneratedCode = get_boolean_from_string(sample_properties.get(u'auto generate code'))
            if u'space' in sample_properties and sample_properties.get(u'space') is not None:
                sample_creation.spaceId = CreationId(sample_properties.get(u'space'))
            if u'project' in sample_properties and sample_properties.get(u'project') is not None:
                sample_creation.projectId = CreationId(sample_properties.get(u'project'))
            if u'experiment' in sample_properties and sample_properties.get(u'experiment') is not None:
                sample_creation.experimentId = CreationId(sample_properties.get(u'experiment'))
            if u'parents' in sample_properties and sample_properties.get(u'parents') is not None:
                parent_creationids = []
                parents = sample_properties.get(u'parents').split('\n')
                for parent in parents:
                    parent_creationids.append(CreationId(parent))
                sample_creation.parentIds = parent_creationids
            if u'children' in sample_properties and sample_properties.get(u'children') is not None:
                child_creationids = []
                children = sample_properties.get(u'children').split('\n')
                for child in children:
                    child_creationids.append(CreationId(child))
                sample_creation.childIds = child_creationids

            for prop, value in sample_properties.items():
                if prop not in mandatory_attributes:
                    sample_creation.setProperty(prop, value)
            samples.append(sample_creation)
        return samples

    def get_type(self):
        return SampleDefinitionToCreationParser.type


class ScriptDefinitionToCreationParser(DefinitionToCreationParser):
    type = "ScriptCreation"

    def __init__(self, context=None):
        self.context = context

    def parse(self, definition):
        scripts = []
        if u'validation script' in definition.attributes:
            validation_script_path = definition.attributes.get(u'validation script')
            if validation_script_path is not None:
                code = definition.attributes.get(u'code')
                validation_script_creation = PluginCreation()
                script = self.context.get_script(validation_script_path)
                validation_script_creation.name = get_script_name_for(code, validation_script_path)
                validation_script_creation.script = script
                validation_script_creation.pluginType = PluginType.ENTITY_VALIDATION
                scripts.append(validation_script_creation)

        for prop in definition.properties:
            if u'dynamic script' in prop:
                dynamic_prop_script_path = prop.get(u'dynamic script')
                code = prop.get(u'code')
                if dynamic_prop_script_path is not None:
                    validation_script_creation = PluginCreation()
                    script = self.context.get_script(dynamic_prop_script_path)
                    validation_script_creation.name = get_script_name_for(code, dynamic_prop_script_path)
                    validation_script_creation.script = script
                    validation_script_creation.pluginType = PluginType.DYNAMIC_PROPERTY
                    scripts.append(validation_script_creation)

        return scripts

    def get_type(self):
        return ScriptDefinitionToCreationParser.type
