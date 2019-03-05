from ch.ethz.sis.openbis.generic.asapi.v3.dto.operation import SynchronousOperationExecutionOptions
from parsers import get_creations_from, CreationToOperationParser
from processors import OpenbisDuplicatesHandler, PropertiesLabelHandler, DuplicatesHandler, unify_properties_representation_of
from search_engines import SearchEngine
from utils import FileHandler


def process(context, parameters):
    xls_byte_arrays = parameters.get('xls')
    scripts = parameters.get('scripts')
    creations = get_creations_from(xls_byte_arrays, FileHandler(scripts))
    distinct_creations = DuplicatesHandler.get_distinct_creations(creations)
    sessionToken = context.sessionToken
    api = context.applicationService
    search_engine = SearchEngine(api, sessionToken)
    existing_elements = search_engine.find_all_existing_elements(distinct_creations)
    server_duplicates_handler = OpenbisDuplicatesHandler(distinct_creations, existing_elements)
    creations = server_duplicates_handler.remove_existing_elements_from_creations()
    creations = server_duplicates_handler.rewrite_parentchild_creationid_to_permid()
    entity_kinds = search_engine.find_existing_entity_kind_definitions_for(creations)
    existing_vocabularies = search_engine.find_all_existing_vocabularies()
    existing_unified_kinds = unify_properties_representation_of(creations, entity_kinds, existing_vocabularies, existing_elements)
    creations = PropertiesLabelHandler.rewrite_property_labels_to_codes(creations, existing_unified_kinds)
    operations = CreationToOperationParser.parse(creations)
    res = str(api.executeOperations(sessionToken, operations, SynchronousOperationExecutionOptions()).getResults())
    return res
