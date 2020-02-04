import ch.systemsx.cisd.openbis.generic.server.ComponentNames as ComponentNames
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider as CommonServiceProvider
import ch.systemsx.cisd.common.exceptions.UserFailureException as UserFailureException

def process(context, parameters):
    method = parameters.get("method");
    result = None;

    if method == "getServiceProperty":
        result = getServiceProperty(context, parameters);
    elif method == "getNextSequenceForType":
        result = getNextSequenceForType(context, parameters);
    elif method == "doSpacesBelongToDisabledUsers":
        result = doSpacesBelongToDisabledUsers(context, parameters);
    elif method == "trashStorageSamplesWithoutParents":
        result = trashStorageSamplesWithoutParents(context, parameters);
    return result;

def getServiceProperty(context, parameters):
    propertyKey = parameters.get("propertyKey")
    if propertyKey not in ["ui.unarchiving.threshold.relative", "ui.unarchiving.threshold.absolute"]:
        raise UserFailureException("Invalid property: %s" % propertyKey)
    property = CommonServiceProvider.tryToGetBean("propertyConfigurer").getResolvedProps().getProperty(propertyKey)
    if property is None:
        return parameters.get("defaultValue")
    return property

def getNextSequenceForType(context, parameters):
    sampleTypeCode = parameters.get("sampleTypeCode");
    daoFactory = CommonServiceProvider.getApplicationContext().getBean(ComponentNames.DAO_FACTORY);
    currentSession = daoFactory.getSessionFactory().getCurrentSession();
    querySampleTypeId = currentSession.createSQLQuery("SELECT id from sample_types WHERE code = :sampleTypeCode");
    querySampleTypeId.setParameter("sampleTypeCode", sampleTypeCode);
    sampleTypeId = querySampleTypeId.uniqueResult();

    querySampleTypePrefix = currentSession.createSQLQuery("SELECT generated_code_prefix from sample_types WHERE code = :sampleTypeCode");
    querySampleTypePrefix.setParameter("sampleTypeCode", sampleTypeCode);
    sampleTypePrefix = querySampleTypePrefix.uniqueResult();
    sampleTypePrefixLengthPlusOneAsString = str((len(sampleTypePrefix) + 1));
    querySampleCount = currentSession.createSQLQuery("SELECT COALESCE(MAX(CAST(substring(code, " + sampleTypePrefixLengthPlusOneAsString + ") as int)), 0) FROM samples_all WHERE saty_id = :sampleTypeId AND code ~ :codePattern");
    querySampleCount.setParameter("sampleTypeId", sampleTypeId);
    querySampleCount.setParameter("codePattern", "^" + sampleTypePrefix + "[0-9]+$");
    sampleCount = querySampleCount.uniqueResult();

    return (sampleCount + 1)

def doSpacesBelongToDisabledUsers(context, parameters):
    daoFactory = CommonServiceProvider.getApplicationContext().getBean(ComponentNames.DAO_FACTORY);
    currentSession = daoFactory.getSessionFactory().getCurrentSession();

    # TO-DO Replace generating SQL manually by variable substitution

    spaceCodes = parameters.get("spaceCodes");
    if spaceCodes is None or len(spaceCodes) == 0:
        return []

    spaceCodesList = "("
    isFirst = True
    for spaceCode in spaceCodes:
        if not isFirst:
            spaceCodesList = spaceCodesList + ","
        else:
            isFirst = False
        spaceCodesList = spaceCodesList + "'" + spaceCode + "'"
    spaceCodesList = spaceCodesList + ")"

    disabled_spaces = currentSession.createSQLQuery("SELECT sp.code FROM spaces sp WHERE sp.id IN(SELECT p.space_id FROM persons p WHERE p.space_id IN (SELECT s.id FROM spaces s WHERE s.code IN " + spaceCodesList + ") AND p.is_active = FALSE)");
    disabled_spaces_result = disabled_spaces.list()
    return disabled_spaces_result

def trashStorageSamplesWithoutParents(context, parameters):
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id import SamplePermId
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleFetchOptions
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete import SampleDeletionOptions
    from ch.systemsx.cisd.common.exceptions import UserFailureException

    permIds = [];
    for permId in parameters.get("samplePermIds"):
        permIds.append(SamplePermId(permId));
    fetchOptions = SampleFetchOptions();
    fetchOptions.withType();
    fetchOptions.withParents();
    sessionToken = context.applicationService.loginAsSystem();
    samplesMapByPermId = context.applicationService.getSamples(sessionToken, permIds, fetchOptions);
    for permId in permIds:
        sample = samplesMapByPermId[permId];
        # Is an storage position
        if sample.getType().getCode() != "STORAGE_POSITION":
            raise UserFailureException("Sample with PermId " + sample.getPermId().getPermId() + " is not an STORAGE_POSITION but instead " + sample.getType().getCode());
        # Doesn't have parents
        if len(sample.getParents()) > 0:
            raise UserFailureException("Sample with PermId " + sample.getPermId().getPermId() + " has " + str(len(sample.getParents())) + " parents.");
    # Delete
    deleteOptions = SampleDeletionOptions();
    deleteOptions.setReason(parameters.get("reason"));
    deletionId = context.applicationService.deleteSamples(sessionToken, permIds, deleteOptions);
    return True