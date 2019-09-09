from java.lang import UnsupportedOperationException
from ..definition import Definition
from .poi_cleaner import PoiCleaner


class DefinitionParserFactory(object):

    @staticmethod
    def get_parser(definition_type):
        if definition_type in ['VOCABULARY_TYPE', 'SAMPLE_TYPE', 'EXPERIMENT_TYPE', 'DATASET_TYPE', 'EXPERIMENT',
                               'SAMPLE']:
            return GeneralDefinitionParser
        elif definition_type in ['PROPERTY_TYPE', 'SPACE', 'PROJECT']:
            return PropertiesOnlyDefinitionParser
        else:
            raise UnsupportedOperationException(
                "Definition of " + str(definition_type) + " is not supported (probably yet).")


class PropertiesOnlyDefinitionParser(object):

    @staticmethod
    def parse(poi_definition):
        DEFINITION_TYPE_ROW = 0
        DEFINITION_TYPE_CELL = 0
        PROPERTIES_HEADER_ROW = 1
        PROPERTIES_VALUES_ROW_START = 2

        row_numbers = {
            'DEFINITION_TYPE_ROW': DEFINITION_TYPE_ROW,
            'DEFINITION_TYPE_CELL': DEFINITION_TYPE_CELL,
            'ATTRIBUTES_HEADER_ROW': None,
            'ATTRIBUTES_VALUES_ROW': None,
            'PROPERTIES_HEADER_ROW': PROPERTIES_HEADER_ROW,
            'PROPERTIES_VALUES_ROW_START': PROPERTIES_VALUES_ROW_START
        }

        poi_definition = PoiCleaner.clean_data(poi_definition, row_numbers)
        definition = Definition()
        definition.type = poi_definition[DEFINITION_TYPE_ROW][DEFINITION_TYPE_CELL]
        if PropertiesOnlyDefinitionParser.hasProperties(poi_definition):
            properties_headers = poi_definition[PROPERTIES_HEADER_ROW]

            for property_definitions in poi_definition[PROPERTIES_VALUES_ROW_START:]:
                property = {}
                for col, header in properties_headers.items():
                    property[header] = property_definitions[col]
                definition.properties.append(property)

        return definition

    @staticmethod
    def hasProperties(poi_definition):
        PROPERTIES_HEADER_ROW = 1
        return len(poi_definition) > PROPERTIES_HEADER_ROW


class GeneralDefinitionParser(object):

    @staticmethod
    def parse(poi_definition):
        DEFINITION_TYPE_ROW = 0
        DEFINITION_TYPE_CELL = 0
        ATTRIBUTES_HEADER_ROW = 1
        ATTRIBUTES_VALUES_ROW = 2
        PROPERTIES_HEADER_ROW = 3
        PROPERTIES_VALUES_ROW_START = 4

        row_numbers = {
            'DEFINITION_TYPE_ROW': DEFINITION_TYPE_ROW,
            'DEFINITION_TYPE_CELL': DEFINITION_TYPE_CELL,
            'ATTRIBUTES_HEADER_ROW': ATTRIBUTES_HEADER_ROW,
            'ATTRIBUTES_VALUES_ROW': ATTRIBUTES_VALUES_ROW,
            'PROPERTIES_HEADER_ROW': PROPERTIES_HEADER_ROW,
            'PROPERTIES_VALUES_ROW_START': PROPERTIES_VALUES_ROW_START
        }

        poi_definition = PoiCleaner.clean_data(poi_definition, row_numbers)

        definition = Definition()
        definition.type = poi_definition[DEFINITION_TYPE_ROW][DEFINITION_TYPE_CELL]
        for col, header in poi_definition[ATTRIBUTES_HEADER_ROW].items():
            cell_value = poi_definition[ATTRIBUTES_VALUES_ROW][col]
            definition.attributes[header] = cell_value

        if GeneralDefinitionParser.hasProperties(poi_definition):
            properties_headers = poi_definition[PROPERTIES_HEADER_ROW]

            for property_definitions in poi_definition[PROPERTIES_VALUES_ROW_START:]:
                property = {}
                for col, header in properties_headers.items():
                    property[header] = property_definitions[col]
                definition.properties.append(property)

        return definition

    @staticmethod
    def hasProperties(poi_definition):
        PROPERTIES_HEADER_ROW = 3
        return len(poi_definition) > PROPERTIES_HEADER_ROW
