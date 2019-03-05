from parsers import ScriptDefinitionToCreationParser, SampleDefinitionToCreationParser


class DuplicatesHandler(object):

    @staticmethod
    def get_distinct_creations(creations):
        distinct_creations = {}
        for creation_type, creations in creations.items():
            if creation_type == ScriptDefinitionToCreationParser.type:
                distinct_creations[creation_type] = dict((creation.name, creation) for creation in creations).values()
            elif creation_type not in [SampleDefinitionToCreationParser.type]:
                distinct_creations[creation_type] = dict((creation.code, creation) for creation in creations).values()
            else:
                distinct_creations[creation_type] = list(creations)
        return distinct_creations
