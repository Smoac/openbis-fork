import json
import os
from ch.ethz.sis.openbis.generic.asapi.v3.dto.operation import SynchronousOperationExecutionOptions
from ch.systemsx.cisd.common.exceptions import UserFailureException
from ch.systemsx.cisd.openbis.generic.server import CommonServiceProvider
from parsers import get_creations_from, get_definitions_from_xls, get_definitions_from_csv, get_creation_metadata_from, \
    CreationOrUpdateToOperationParser, versionable_types
from processors import OpenbisDuplicatesHandler, PropertiesLabelHandler, DuplicatesHandler, \
    unify_properties_representation_of, validate_creations
from search_engines import SearchEngine
from utils import FileHandler
from utils.openbis_utils import get_version_name_for, get_metadata_name_for


def validate_data(xls_byte_arrays, csv_strings, update_mode, xls_name):
    if xls_byte_arrays is None and csv_strings is None:
        raise UserFailureException('Nor Excel sheet nor csv has not been provided. "xls" and "csv" parameters are None')
    if update_mode not in ['IGNORE_EXISTING', 'FAIL_IF_EXISTS', 'UPDATE_IF_EXISTS']:
        raise UserFailureException(
            'Update mode has to be one of following: IGNORE_EXISTING FAIL_IF_EXISTS UPDATE_IF_EXISTS but was ' + (
                str(update_mode) if update_mode else 'None'))
    if xls_name is None:
        raise UserFailureException('Excel name has not been provided.  parameter is mandatory')


def get_property(key, defaultValue):
    propertyConfigurer = CommonServiceProvider.getApplicationContext().getBean("propertyConfigurer")
    properties = propertyConfigurer.getResolvedProps()
    return properties.getProperty(key, defaultValue)


def read_versioning_information(xls_version_filepath):
    if os.path.exists(xls_version_filepath):
        with open(xls_version_filepath, 'r') as f:
            return json.load(f)
    else:
        return {}


def save_versioning_information(versioning_information, xls_version_filepath):
    filepath_new = "%s.new" % xls_version_filepath
    with open(filepath_new, 'w') as f:
        json.dump(versioning_information, f)
    os.rename(filepath_new, xls_version_filepath)


def create_versioning_information(all_versioning_information, creations, creations_metadata, update_mode,
                                  xls_version_name):
    if xls_version_name in all_versioning_information:
        versioning_information = all_versioning_information[xls_version_name]
        for creation_type, creation_collection in creations.items():
            if creation_type in versionable_types:
                for creation in creation_collection:
                    code = get_metadata_name_for(creation_type, creation)
                    if code in versioning_information:
                        version = versioning_information[code]
                    else:
                        version = creations_metadata.get_metadata_for(creation_type,
                                                                      creation).version if update_mode != "UPDATE_IF_EXISTS" else 0
                    versioning_information[code] = int(version)
    else:
        versioning_information = {}
        for creation_type, creation_collection in creations.items():
            if creation_type in versionable_types:
                for creation in creation_collection:
                    code = get_metadata_name_for(creation_type, creation)
                    versioning_information[code] = creations_metadata.get_metadata_for(creation_type,
                                                                                       creation).version if update_mode != "UPDATE_IF_EXISTS" else 0
    return versioning_information


def process(context, parameters):
    """
        Excel import AS service.
        For extensive documentation of usage and Excel layout, 
        please visit https://wiki-bsse.ethz.ch/display/openBISDoc/Excel+import+service
        
        :param context: Standard Openbis AS Service context object
        :param parameters: Contains two elements
                        {
                            'xls' : excel byte blob,    - mandatory
                            'xls_name': identifier of excel file - mandatory
                            'scripts': {                - optional
                                file path: loaded file
                            },
                            update_mode: [IGNORE_EXISTING|FAIL_IF_EXISTS|UPDATE_IF_EXISTS] - optional, default FAIL_IF_EXISTS
                                                                                             This only takes duplicates that are ON THE SERVER
                        }
        :return: Openbis's execute operations result string. It should contain report on what was created.
    """
    api, session_token = context.applicationService, context.sessionToken
    search_engine = SearchEngine(api, session_token)

    xls_byte_arrays = parameters.get('xls', None)
    csv_strings = parameters.get('csv', None)
    xls_name = parameters.get('xls_name', None)
    scripts = parameters.get('scripts', {})
    update_mode = parameters.get('update_mode', None)
    validate_data(xls_byte_arrays, csv_strings, update_mode, xls_name)
    definitions = get_definitions_from_xls(xls_byte_arrays)
    definitions.extend(get_definitions_from_csv(csv_strings))
    creations = get_creations_from(definitions, FileHandler(scripts))
    validate_creations(creations)
    creations_metadata = get_creation_metadata_from(definitions)
    creations = DuplicatesHandler.get_distinct_creations(creations)
    xls_version_filepath = get_property("xls-import.version-data-file", "../../../xls-import-version-info.json")
    xls_version_name = get_version_name_for(xls_name)
    all_versioning_information = read_versioning_information(xls_version_filepath)
    versioning_information = create_versioning_information(all_versioning_information, creations, creations_metadata,
                                                           update_mode, xls_version_name)
    existing_elements = search_engine.find_all_existing_elements(creations)
    entity_kinds = search_engine.find_existing_entity_kind_definitions_for(creations)
    existing_vocabularies = search_engine.find_all_existing_vocabularies()
    existing_unified_kinds = unify_properties_representation_of(creations, entity_kinds, existing_vocabularies,
                                                                existing_elements)
    creations = PropertiesLabelHandler.rewrite_property_labels_to_codes(creations, existing_unified_kinds)
    server_duplicates_handler = OpenbisDuplicatesHandler(creations, creations_metadata, existing_elements,
                                                         versioning_information, update_mode)
    creations = server_duplicates_handler.rewrite_parentchild_creationid_to_permid()
    creations = server_duplicates_handler.handle_existing_elements_in_creations()
    entity_type_creation_operations, entity_creation_operations, entity_type_update_operations, entity_update_operations = CreationOrUpdateToOperationParser.parse(
        creations)

    entity_type_update_results = str(api.executeOperations(session_token, entity_type_update_operations,
                                                           SynchronousOperationExecutionOptions()).getResults())
    entity_type_creation_results = str(api.executeOperations(session_token, entity_type_creation_operations,
                                                             SynchronousOperationExecutionOptions()).getResults())
    entity_creation_results = str(api.executeOperations(session_token, entity_creation_operations,
                                                        SynchronousOperationExecutionOptions()).getResults())
    entity_update_results = str(api.executeOperations(session_token, entity_update_operations,
                                                      SynchronousOperationExecutionOptions()).getResults())

    all_versioning_information[xls_version_name] = versioning_information
    save_versioning_information(all_versioning_information, xls_version_filepath)
    return "Update operations performed: {} and {} \n Creation operations performed: {} and {}".format(
        entity_type_update_results, entity_update_results,
        entity_type_creation_results, entity_creation_results)
