from ch.ethz.sis.openbis.generic.asapi.v3.dto.property import DataType


class VocabularyLabelHandler(object):

    @staticmethod
    def rewrite_vocabularies(creations_map, entity_types):
        for creations_type, creations in creations_map.iteritems():
            if creations_type in entity_types:
                matching_entity_types = entity_types[creations_type]
                for creation in creations:
                    for matching_entity_type in matching_entity_types:
                        if creation.typeId.permId == matching_entity_type.permId.permId:
                            vocabulary_property_types = VocabularyLabelHandler.extract_vocabulary_from_entity_type(matching_entity_type)
                            for vocabulary_property_type in vocabulary_property_types:
                                if vocabulary_property_type.code.lower() in creation.properties:
                                    vocabulary_label = creation.properties[vocabulary_property_type.code.lower()]
                                    if vocabulary_label is not None:
                                        for term in vocabulary_property_type.vocabulary.terms:
                                            if vocabulary_label.lower() == term.label.lower():
                                                creation.properties[vocabulary_property_type.code.lower()] = term.code
                                                break
        return creations_map

    @staticmethod
    def extract_vocabulary_from_entity_type(matching_entity_type):
        vocabularies = []
        for property_assignment in matching_entity_type.propertyAssignments:
            if property_assignment.propertyType.dataType == DataType.CONTROLLEDVOCABULARY:
                vocabularies.append(property_assignment.propertyType)
        return vocabularies
