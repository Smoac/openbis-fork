import ch.systemsx.cisd.openbis.generic.server.ComponentNames as ComponentNames
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider as CommonServiceProvider

def process(context, parameters):
    method = parameters.get("method");
    result = None;
    
    if method == "getNextSequenceForType":
        result = getNextSequenceForType(context, parameters);
    if method == "doSpacesBelongToDisabledUsers":
        result = doSpacesBelongToDisabledUsers(context, parameters);
    
    return result;

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