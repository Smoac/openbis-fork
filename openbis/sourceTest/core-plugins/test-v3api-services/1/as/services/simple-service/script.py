from ch.ethz.sis.openbis.generic.as.api.v3.dto.space.fetchoptions import SpaceFetchOptions
from ch.ethz.sis.openbis.generic.as.api.v3.dto.space.search import SpaceSearchCriteria

def process(parameters):
    name = parameters.get("name");
    searchCriteria = SpaceSearchCriteria();
    searchCriteria.withCode().thatEquals("CISD");
    result = applicationService.searchSpaces(sessionToken, searchCriteria, SpaceFetchOptions());
    return "hello %s. Spaces: %s" % (name, result.objects)
