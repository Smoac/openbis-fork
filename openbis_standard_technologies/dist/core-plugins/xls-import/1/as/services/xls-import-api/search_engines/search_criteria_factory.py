from utils.dotdict import dotdict
from ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample import FullSampleIdentifier


class SampleCreationSampleChildrenParentSearchCriteria(object):

    def __init__(self, search_criteria_class):
        self.search_criteria_class = search_criteria_class

    def get_search_criteria(self, specific_creations):
        search_criterias = []
        for creation in specific_creations:
            if creation.parentIds is not None:
                for parent in creation.parentIds:
                    self._get_criteria_from_string(str(parent), search_criterias)
            if creation.childIds is not None:
                for child in creation.childIds:
                    self._get_criteria_from_string(str(child), search_criterias)
        return search_criterias

    def _get_criteria_from_string(self, sample_identifier_or_permid, search_criterias):
        identifier = None
        try:
            identifier = FullSampleIdentifier(sample_identifier_or_permid, None)
        except:
            # No other way to see if this is a SampleIdentifier or not
            # Adapted easier do and ask for forgiveness strategy
            # Exception is passed with assumption parent string was not
            # intended to be SampleIdentifier
            pass

        if identifier is not None:
            search_criteria = self.search_criteria_class()
            search_criteria.withAndOperator()
            identier_parts = identifier.getParts()
            space_code = identier_parts.spaceCodeOrNull
            project_code = identier_parts.projectCodeOrNull
            sample_code = identifier.sampleCode
            search_criteria.withCode().thatEquals(sample_code)
            if project_code is not None:
                search_criteria.withProject().withCode().thatEquals(project_code)
            else:
                search_criteria.withoutProject()

            if space_code is not None:
                search_criteria.withSpace().withCode().thatEquals(space_code)
            else:
                search_criteria.withoutSpace()
            search_criterias.append(search_criteria)
        else:
            search_criteria = self.search_criteria_class()
            search_criteria.withPermId().thatEquals(sample_identifier_or_permid)
            search_criterias.append(search_criteria)


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


class SpaceFromPropertiesSearchCriteria(object):

    def __init__(self, search_criteria_class):
        self.search_criteria = search_criteria_class

    def get_search_criteria(self, specific_creations):
        space_codes = [dotdict({'code': str(creation.spaceId)}) for creation in specific_creations if creation.spaceId is not None]
        default_search_criteria_builder = DefaultCreationElementSearchCriteria(self.search_criteria)
        return default_search_criteria_builder.get_search_criteria(space_codes)


class ProjectFromPropertiesSearchCriteria(object):

    def __init__(self, search_criteria_class):
        self.search_criteria = search_criteria_class

    def get_search_criteria(self, specific_creations):
        project_codes = [dotdict({'code': str(creation.projectId)}) for creation in specific_creations if creation.projectId is not None]
        default_search_criteria_builder = DefaultCreationElementSearchCriteria(self.search_criteria)
        return default_search_criteria_builder.get_search_criteria(project_codes)


class ExperimentFromPropertiesSearchCriteria(object):

    def __init__(self, search_criteria_class):
        self.search_criteria = search_criteria_class

    def get_search_criteria(self, specific_creations):
        experiment_codes = [dotdict({'code': str(creation.experimentId)}) for creation in specific_creations if creation.experimentId is not None]
        default_search_criteria_builder = DefaultCreationElementSearchCriteria(self.search_criteria)
        return default_search_criteria_builder.get_search_criteria(experiment_codes)


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


class FindAllSearchCriteria(object):

    def __init__(self, search_criteria_class):
        self.search_criteria = search_criteria_class()

    def get_search_criteria(self, *args):
        return self.search_criteria
