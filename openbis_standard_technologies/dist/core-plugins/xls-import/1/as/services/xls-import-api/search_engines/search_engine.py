from ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search import SearchOperator
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions import DataSetTypeFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search import DataSetTypeSearchCriteria, SearchDataSetTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions import ExperimentTypeFetchOptions, ExperimentFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search import ExperimentTypeSearchCriteria, \
    SearchExperimentTypesOperation, ExperimentSearchCriteria, SearchExperimentsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.operation import SynchronousOperationExecutionOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.fetchoptions import PluginFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.search import PluginSearchCriteria, SearchPluginsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions import ProjectFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search import ProjectSearchCriteria, SearchProjectsOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions import PropertyTypeFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search import PropertyTypeSearchCriteria, SearchPropertyTypesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleTypeFetchOptions, SampleFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search import SampleTypeSearchCriteria, \
    SearchSampleTypesOperation, SampleSearchCriteria, SearchSamplesOperation, \
    SampleSearchRelation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions import SpaceFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search import SpaceSearchCriteria, SearchSpacesOperation
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions import VocabularyFetchOptions
from ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search import VocabularySearchCriteria, SearchVocabulariesOperation
from parsers import VocabularyDefinitionToCreationParser, PropertyTypeDefinitionToCreationParser, SampleTypeDefinitionToCreationParser, \
                    ExperimentTypeDefinitionToCreationParser, DatasetTypeDefinitionToCreationParser, SpaceDefinitionToCreationParser, \
                    ProjectDefinitionToCreationParser, ExperimentDefinitionToCreationParser, ScriptDefinitionToCreationParser, SampleDefinitionToCreationParser
from search_criteria_factory import DefaultCreationElementSearchCriteria, SampleCreationSampleSearchCriteria, \
                                    ScriptCreationScriptSearchCriteria, EntityCreationEntityTypeSearchCriteria, \
                                    FindAllSearchCriteria, SpaceFromPropertiesSearchCriteria, ProjectFromPropertiesSearchCriteria, \
                                    ExperimentFromPropertiesSearchCriteria, SampleCreationSampleChildrenParentSearchCriteria


class SearchEngine():

    def __init__(self, api, sesstion_token):
        self.api = api
        self.session_token = sesstion_token

    def find_all_existing_vocabularies(self):
        vocabulary_fetch_options = VocabularyFetchOptions()
        vocabulary_fetch_options.withTerms()

        strategy = {
            'creations_type': VocabularyDefinitionToCreationParser.type,
            'search_criteria_build_strategy' : FindAllSearchCriteria,
            'search_criteria_class' : VocabularySearchCriteria,
            'search_operation':SearchVocabulariesOperation,
            'fetch_options':vocabulary_fetch_options
            }

        existing_elements = {}
        creations_type = strategy['creations_type']
        search_criteria_class = strategy['search_criteria_class']
        search_criteria_builder = strategy['search_criteria_build_strategy'](search_criteria_class)
        search_criterias = search_criteria_builder.get_search_criteria()
        existing_specific_elements = self._get_existing_elements(search_criterias, **strategy)
        if existing_specific_elements is not None:
            existing_elements[creations_type] = existing_specific_elements

        return existing_elements

    def find_all_existing_children_parent_candidates(self):

        return existing_elements

    def find_existing_entity_kind_definitions_for(self, creations):
        experiment_fetch_options = ExperimentTypeFetchOptions()
        experiment_fetch_options.withPropertyAssignments().withPropertyType().withVocabulary().withTerms()
        sample_fetch_options = SampleTypeFetchOptions()
        sample_fetch_options.withPropertyAssignments().withPropertyType().withVocabulary().withTerms()

        search_strategy = [
            {
            'creations_type': SampleDefinitionToCreationParser.type,
            'search_criteria_build_strategy' : EntityCreationEntityTypeSearchCriteria,
            'search_criteria_class' : SampleTypeSearchCriteria,
            'search_operation': SearchSampleTypesOperation,
            'fetch_options': sample_fetch_options
            },
            {
            'creations_type': ExperimentDefinitionToCreationParser.type,
            'search_criteria_build_strategy' : EntityCreationEntityTypeSearchCriteria,
            'search_criteria_class' : ExperimentTypeSearchCriteria,
            'search_operation': SearchExperimentTypesOperation,
            'fetch_options': experiment_fetch_options
            }
        ]

        existing_elements = {}
        for strategy in search_strategy:
            creations_type = strategy['creations_type']
            search_criteria_class = strategy['search_criteria_class']
            search_criteria_builder = strategy['search_criteria_build_strategy'](search_criteria_class)
            if creations_type in creations:
                search_criterias = search_criteria_builder.get_search_criteria(creations[creations_type])
                existing_specific_elements = self._get_existing_elements(search_criterias, **strategy)
                if existing_specific_elements is not None:
                    existing_elements[creations_type] = existing_specific_elements

        return existing_elements

    def find_all_existing_elements(self, creations):
        property_type_fetch_options = PropertyTypeFetchOptions()
        property_type_fetch_options.withVocabulary().withTerms()
        search_strategy = [
            {
            'creations_type': VocabularyDefinitionToCreationParser.type,
            'search_criteria_build_strategy' : DefaultCreationElementSearchCriteria,
            'search_criteria_class' : VocabularySearchCriteria,
            'search_operation':SearchVocabulariesOperation,
            'fetch_options':VocabularyFetchOptions()
            },
            {
            'creations_type': SampleDefinitionToCreationParser.type,
            'search_criteria_build_strategy' : SampleCreationSampleChildrenParentSearchCriteria,
            'search_criteria_class' : SampleSearchCriteria,
            'search_operation': SearchSamplesOperation,
            'fetch_options': SampleFetchOptions()
            },
            {
            'creations_type': PropertyTypeDefinitionToCreationParser.type,
            'search_criteria_build_strategy' : DefaultCreationElementSearchCriteria,
            'search_criteria_class' : PropertyTypeSearchCriteria,
            'search_operation': SearchPropertyTypesOperation,
            'fetch_options': property_type_fetch_options
            },
            {
            'creations_type': SampleTypeDefinitionToCreationParser.type,
            'search_criteria_build_strategy' : DefaultCreationElementSearchCriteria,
            'search_criteria_class' : SampleTypeSearchCriteria,
            'search_operation': SearchSampleTypesOperation,
            'fetch_options': SampleTypeFetchOptions()
            },
            {
            'creations_type': ExperimentTypeDefinitionToCreationParser.type,
            'search_criteria_build_strategy' : DefaultCreationElementSearchCriteria,
            'search_criteria_class' : ExperimentTypeSearchCriteria,
            'search_operation': SearchExperimentTypesOperation,
            'fetch_options': ExperimentTypeFetchOptions()
            },
            {
            'creations_type': DatasetTypeDefinitionToCreationParser.type,
            'search_criteria_build_strategy' : DefaultCreationElementSearchCriteria,
            'search_criteria_class' : DataSetTypeSearchCriteria,
            'search_operation': SearchDataSetTypesOperation,
            'fetch_options': DataSetTypeFetchOptions()
            },
            {
            'creations_type': SpaceDefinitionToCreationParser.type,
            'search_criteria_build_strategy' : DefaultCreationElementSearchCriteria,
            'search_criteria_class' : SpaceSearchCriteria,
            'search_operation': SearchSpacesOperation,
            'fetch_options': SpaceFetchOptions()
            },
            {
            'creations_type': ProjectDefinitionToCreationParser.type,
            'search_criteria_build_strategy' : DefaultCreationElementSearchCriteria,
            'search_criteria_class' : ProjectSearchCriteria,
            'search_operation': SearchProjectsOperation,
            'fetch_options': ProjectFetchOptions()
            },
            {
            'creations_type': ProjectDefinitionToCreationParser.type,
            'result_creations_type': SpaceDefinitionToCreationParser.type,
            'search_criteria_build_strategy' : SpaceFromPropertiesSearchCriteria,
            'search_criteria_class' : SpaceSearchCriteria,
            'search_operation': SearchSpacesOperation,
            'fetch_options': SpaceFetchOptions()
            },
            {
            'creations_type': ExperimentDefinitionToCreationParser.type,
            'search_criteria_build_strategy' : DefaultCreationElementSearchCriteria,
            'search_criteria_class' : ExperimentSearchCriteria,
            'search_operation': SearchExperimentsOperation,
            'fetch_options': ExperimentFetchOptions()
            },
            {
            'creations_type': ExperimentDefinitionToCreationParser.type,
            'result_creations_type': ProjectDefinitionToCreationParser.type,
            'search_criteria_build_strategy' : ProjectFromPropertiesSearchCriteria,
            'search_criteria_class' : ProjectSearchCriteria,
            'search_operation': SearchProjectsOperation,
            'fetch_options': ProjectFetchOptions()
            },
            {
            'creations_type': SampleDefinitionToCreationParser.type,
            'search_criteria_build_strategy' : SampleCreationSampleSearchCriteria,
            'search_criteria_class' : SampleSearchCriteria,
            'search_operation': SearchSamplesOperation,
            'fetch_options': SampleFetchOptions()
            },
            {
            'creations_type': SampleDefinitionToCreationParser.type,
            'result_creations_type': SpaceDefinitionToCreationParser.type,
            'search_criteria_build_strategy' : SpaceFromPropertiesSearchCriteria,
            'search_criteria_class' : SpaceSearchCriteria,
            'search_operation': SearchSpacesOperation,
            'fetch_options': SpaceFetchOptions()
            },
            {
            'creations_type': SampleDefinitionToCreationParser.type,
            'result_creations_type': ProjectDefinitionToCreationParser.type,
            'search_criteria_build_strategy' : ProjectFromPropertiesSearchCriteria,
            'search_criteria_class' : ProjectSearchCriteria,
            'search_operation': SearchProjectsOperation,
            'fetch_options': ProjectFetchOptions()
            },
            {
            'creations_type': SampleDefinitionToCreationParser.type,
            'result_creations_type': ExperimentDefinitionToCreationParser.type,
            'search_criteria_build_strategy' : ExperimentFromPropertiesSearchCriteria,
            'search_criteria_class' : ExperimentSearchCriteria,
            'search_operation': SearchExperimentsOperation,
            'fetch_options': ExperimentFetchOptions()
            },
            {
            'creations_type': ScriptDefinitionToCreationParser.type,
            'search_criteria_build_strategy' : ScriptCreationScriptSearchCriteria,
            'search_criteria_class': PluginSearchCriteria,
            'search_operation': SearchPluginsOperation,
            'fetch_options': PluginFetchOptions()
            }
        ]

        existing_elements = {}
        for strategy in search_strategy:
            creations_type = strategy['creations_type']
            search_criteria_class = strategy['search_criteria_class']
            search_criteria_builder = strategy['search_criteria_build_strategy'](search_criteria_class)
            if creations_type in creations:
                search_criterias = search_criteria_builder.get_search_criteria(creations[creations_type])
                existing_specific_elements = self._get_existing_elements(search_criterias, **strategy)
                results_key = strategy['result_creations_type'] if 'result_creations_type' in strategy else creations_type
                if existing_specific_elements is not None:
                    if results_key not in existing_elements:
                        existing_elements[results_key] = []
                    # May contain duplicates when i.e Project exists on the server but is also
                    # explicitly mentioned in xls sheet, it is ok.
                    existing_elements[results_key].extend(existing_specific_elements)
        return existing_elements

    def _get_existing_elements(self, search_criterias, **kwargs):
        search_operation = kwargs['search_operation']
        fetch_options = kwargs['fetch_options']
        if not search_criterias:
            return None
        search_criterias = search_criterias if type(search_criterias) == list else [search_criterias]
        operations = [search_operation(search_criteria, fetch_options) for search_criteria in search_criterias]
        return self._execute_search_operation(operations)

    def _execute_search_operation(self, operations):
        execution_results = self.api.executeOperations(self.session_token, operations, SynchronousOperationExecutionOptions())
        result_objects = []
        for search_result in execution_results.getResults():
            result_objects.extend(search_result.getSearchResult().getObjects())
        return result_objects

