class SampleCreationSampleSearchCriteria(object):

    def __init__(self, search_criteria_class):
        self.search_criteria_class = search_criteria_class

    def get_search_criteria(self, specific_creations):
        search_criterias = []
        for creation in specific_creations:
            search_criteria = self.search_criteria_class()
            search_criteria.withAndOperator()
            if creation.code is not None:
                search_criteria.withCode().thatEquals(creation.code)
                if creation.experimentId is not None:
                    search_criteria.withExperiment().withCode().thatEquals(creation.experimentId.creationId)
                else:
                    search_criteria.withoutExperiment()

                if creation.projectId is not None:
                    search_criteria.withProject().withCode().thatEquals(creation.projectId.creationId)
                else:
                    search_criteria.withoutProject()

                if creation.spaceId is not None:
                    search_criteria.withSpace().withCode().thatEquals(creation.spaceId.creationId)
                else:
                    search_criteria.withoutSpace()

                search_criterias.append(search_criteria)

        return search_criterias


class DefaultCreationElementSearchCriteria(object):

    def __init__(self, search_criteria_class):
        self.search_criteria = search_criteria_class()

    def get_search_criteria(self, specific_creations):
        if 'withCodes' in dir(self.search_criteria):
            self.search_criteria.withCodes().thatIn([creation.code for creation in specific_creations])
        else:
            for creation in specific_creations:
                self.search_criteria.withCode().thatEquals(creation.code)
            self.search_criteria.withOrOperator()
        return self.search_criteria


class ScriptCreationScriptSearchCriteria(object):

    def __init__(self, search_criteria_class):
        self.search_criteria = search_criteria_class()

    def get_search_criteria(self, specific_creations):
        for creation in specific_creations:
            self.search_criteria.withName().thatEquals(creation.name)
        self.search_criteria.withOrOperator()
        return self.search_criteria


class EntityCreationEntityTypeSearchCriteria(object):

    def __init__(self, search_criteria_class):
        self.search_criteria = search_criteria_class()

    def get_search_criteria(self, specific_creations):
        self.search_criteria.withCodes().thatIn([creation.typeId.permId for creation in specific_creations])
        return self.search_criteria
