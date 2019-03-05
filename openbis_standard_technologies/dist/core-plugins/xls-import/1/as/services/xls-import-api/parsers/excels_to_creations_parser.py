from .definition_to_creation import DefinitionToCreationParser
from .excel_to_poi import ExcelToPoiParser
from .poi_to_definition import PoiToDefinitionParser


def merge_dicts(product, dict_to_merge):
    for dict_key, dict_val in dict_to_merge.items():
        if dict_key not in product:
            product[dict_key] = dict_val
        else:
            product[dict_key].extend(dict_val)
    return product


def get_creations_from(xls_byte_arrays, context):
    creations = {}
    for excel_byte_array in xls_byte_arrays:
        poi_definitions = ExcelToPoiParser.parse(excel_byte_array)
        definitions = PoiToDefinitionParser.parse(poi_definitions)
        partial_creations = DefinitionToCreationParser.parse(definitions, context)
        creations = merge_dicts(creations, partial_creations)
    return creations
