import ch.systemsx.cisd.openbis.generic.server.ComponentNames as ComponentNames
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider as CommonServiceProvider
import ch.systemsx.cisd.common.exceptions.UserFailureException as UserFailureException
import base64

isOpenBIS2020 = True;
enableNewSearchEngine = isOpenBIS2020;

##
## Grid related functions
## These functions should be the same as in javascript, currently found on Util.js
##
alphabet = [None,'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'];
def getLetterForNumber(number): # TODO Generate big numbers
    return alphabet[number];

def getNumberFromLetter(letter): # TODO Generate big numbers
    i = None;
    for alphabetLetter in alphabet:
        if i == None:
            i = 0;
        else:
            i = i + 1;
        if letter == alphabetLetter:
            return i;
    return None;

def process(context, parameters):
    method = parameters.get("method");
    result = None;

    if method == "getServiceProperty":
        result = getServiceProperty(context, parameters);
    elif method == "getNextSequenceForType":
        result = getNextSequenceForType(context, parameters);
    elif method == "getNextExperimentCode":
        result = getNextExperimentCode(context, parameters);
    elif method == "doSpacesBelongToDisabledUsers":
        result = doSpacesBelongToDisabledUsers(context, parameters);
    elif method == "trashStorageSamplesWithoutParents":
        result = trashStorageSamplesWithoutParents(context, parameters);
    elif method == "isValidStoragePositionToInsertUpdate":
        result = isValidStoragePositionToInsertUpdate(context, parameters);
    elif method == "setCustomWidgetSettings":
        result = setCustomWidgetSettings(context, parameters);
    elif method == "getUserManagementMaintenanceTaskConfig":
        result = getUserManagementMaintenanceTaskConfig(context, parameters)
    elif method == "saveUserManagementMaintenanceTaskConfig":
        result = saveUserManagementMaintenanceTaskConfig(context, parameters)
    elif method == "executeUserManagementMaintenanceTask":
        result = executeUserManagementMaintenanceTask(context, parameters)
    elif method == "getUserManagementMaintenanceTaskReport":
        result = getUserManagementMaintenanceTaskReport(context, parameters)
    elif method == "removeUserManagementMaintenanceTaskReport":
        result = removeUserManagementMaintenanceTaskReport(context, parameters)
    elif method == "importSamples":
        result = importSamples(context, parameters)
    elif method == "getSamplesImportTemplate":
        result = getSamplesImportTemplate(context, parameters)
    return result;

def getSamplesImportTemplate(context, parameters):
    from java.io import ByteArrayOutputStream
    from org.apache.poi.xssf.usermodel import XSSFWorkbook
    from org.apache.poi.ss.usermodel import IndexedColors
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id import EntityTypePermId
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleTypeFetchOptions

    allowedSampleTypes = [EntityTypePermId(code) for code in parameters.get("allowedSampleTypes")]
    importMode = parameters.get("importMode")
    sessionToken = context.getSessionToken()
    api = context.getApplicationService()
    fetchOptions = SampleTypeFetchOptions()
    fetchOptions.withPropertyAssignments().withPropertyType()
    sampleTypes = api.getSampleTypes(sessionToken, allowedSampleTypes, fetchOptions)
    workbook = XSSFWorkbook()
    sheet = workbook.createSheet()
    kind_style = _create_style(workbook, IndexedColors.LIGHT_ORANGE)
    type_style = _create_style(workbook, bold=True)
    header_style = _create_style(workbook, IndexedColors.GREY_25_PERCENT, True)
    row_index = 0
    max_number_of_columns = 0
    for sampleTypeId in allowedSampleTypes:
        row = sheet.createRow(row_index)
        _create_cell(row, 0, kind_style, "SAMPLE")
        row = sheet.createRow(row_index + 1)
        _create_cell(row, 0, None, "Sample Type")
        row = sheet.createRow(row_index + 2)
        _create_cell(row, 0, type_style, sampleTypeId.getPermId())
        row = sheet.createRow(row_index + 3)
        cell_index = _create_cell(row, 0, header_style, "$")
        cell_index = _create_cell(row, cell_index, header_style, "Code")
        if importMode == "UPDATE":
            cell_index = _create_cell(row, cell_index, header_style, "Identifier")
            cell_index = _create_cell(row, cell_index, header_style, "Experiment")
            cell_index = _create_cell(row, cell_index, header_style, "Project")
            cell_index = _create_cell(row, cell_index, header_style, "Space")
        cell_index = _create_cell(row, cell_index, header_style, "Parents")
        for propertyAssignment in sampleTypes.get(sampleTypeId).getPropertyAssignments():
            cell_index = _create_cell(row, cell_index, header_style, propertyAssignment.getPropertyType().getLabel())
        max_number_of_columns = max(max_number_of_columns, cell_index)
        row_index += 6
    for i in range(max_number_of_columns):
        sheet.autoSizeColumn(i)
    baos = ByteArrayOutputStream()
    workbook.write(baos)
    return baos.toByteArray()

def _create_style(workbook, color=None, bold=None):
    from org.apache.poi.ss.usermodel import FillPatternType

    style = workbook.createCellStyle()
    if color is not None:
        style.setFillForegroundColor(color.getIndex())
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
    if bold:
        font = workbook.createFont()
        font.setBold(True)
        style.setFont(font)
    return style

def _create_cell(row, cell_index, style, value):
    cell = row.createCell(cell_index)
    cell.setCellStyle(style)
    cell.setCellValue(value)
    return cell_index + 1

def importSamples(context, parameters):
    from org.apache.commons.io import IOUtils

    sessionKey = parameters.get("sessionKey")
    allowedSampleTypes = parameters.get("allowedSampleTypes")
    experimentsByType = parameters.get("experimentsByType", {})
    spacesByType = parameters.get("spacesByType", {})
    mode = parameters.get("mode")
    contextProvider = CommonServiceProvider.getApplicationContext().getBean("request-context-provider")
    uploadedFiles = contextProvider.getHttpServletRequest().getSession(False).getAttribute(sessionKey)
    results = []
    for file in uploadedFiles.iterable():
        file_name = file.getOriginalFilename()
        bytes = IOUtils.toByteArray(file.getInputStream())
        validateSampleImport(context, bytes, file_name, allowedSampleTypes, mode)
        results.append(importData(context, bytes, file_name, experimentsByType, spacesByType, mode, False))
    return results

def validateSampleImport(context, bytes, file_name, allowedSampleTypes, mode):
    definitions = importData(context, bytes, file_name, None, None, mode, True)
    for definition in definitions:
        row_number = definition.row_number
        entityType = definition.type
        if entityType != 'SAMPLE':
            raise UserFailureException("Error in row %s: Type %s is not allowed to import." % (row_number, entityType))
        key = 'sample type'
        if key not in definition.attributes:
            raise UserFailureException("Error in row %s: First cell should contain '%s'." % (row_number + 1, key))
        sampleType = definition.attributes[key]
        if sampleType not in allowedSampleTypes:
            raise UserFailureException("Error in row %s: Sample type %s is not allowed to import." % (row_number + 2, sampleType))
        row_number += 3
        for properties in definition.properties:
            if properties.get("$") == "$":
                raise UserFailureException("Empty row expected before row %s" % (row_number - 2))
            row_number += 1

def importData(context, bytes, file_name, experimentsByType, spacesByType, mode, definitionsOnly):
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id import CustomASServiceCode
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.service import CustomASServiceExecutionOptions
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id import ExperimentIdentifier

    sessionToken = context.getSessionToken()
    api = context.getApplicationService()
    props = CustomASServiceExecutionOptions().withParameter('xls', [bytes])
    props.withParameter('xls_name', file_name)
    props.withParameter('update_mode', mode)
    props.withParameter('ignore_versioning', True)
    if definitionsOnly:
        props.withParameter('definitions_only', True)
    if experimentsByType is not None:
        props.withParameter('experiments_by_type', experimentsByType)
    if spacesByType is not None:
        props.withParameter('spaces_by_type', spacesByType)
    return api.executeCustomASService(sessionToken, CustomASServiceCode("xls-import-api"), props)

def getUserManagementMaintenanceTaskConfig(context, parameters):
    from ch.systemsx.cisd.common.filesystem import FileUtilities

    configfile = _getUserManagementMaintenanceTaskConfigFile(context)
    if configfile is not None:
        return FileUtilities.loadToString(configfile)
    return None

def saveUserManagementMaintenanceTaskConfig(context, parameters):
    from ch.systemsx.cisd.common.filesystem import FileUtilities

    configfile = _getUserManagementMaintenanceTaskConfigFile(context)
    config = parameters.get("config")
    if configfile is not None and config is not None:
        FileUtilities.writeToFile(configfile, config)

def executeUserManagementMaintenanceTask(context, parameters):
    task = _getUserManagementMaintenanceTask(context)
    return task.executeAsync()

def getUserManagementMaintenanceTaskReport(context, parameters):
    task = _getUserManagementMaintenanceTask(context)
    id = int(parameters.get("id"))
    report = task.getReportById(id)
    return (report.getLog(), report.getAuditLog(), report.getErrorReport())

def removeUserManagementMaintenanceTaskReport(context, parameters):
    task = _getUserManagementMaintenanceTask(context)
    id = int(parameters.get("id"))
    task.removeReport(id)

def _getUserManagementMaintenanceTaskConfigFile(context):
    task = _getUserManagementMaintenanceTask(context)
    return task.getConfigurationFile() if task is not None else None

def _getUserManagementMaintenanceTask(context):
    from ch.systemsx.cisd.openbis.generic.server import MaintenanceTaskStarter

    if _isInstanceAdmin(context):
        for plugin in CommonServiceProvider.getApplicationContext().getBean(MaintenanceTaskStarter).getPlugins():
            parameters = plugin.getParameters()
            if parameters.getClassName() == "ch.systemsx.cisd.openbis.generic.server.task.UserManagementMaintenanceTask":
                return plugin.getTask()
    return None

def _isInstanceAdmin(context):
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id import Me
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions import PersonFetchOptions
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment import Role
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment import RoleLevel

    me = Me()
    fetchOptions = PersonFetchOptions()
    fetchOptions.withRoleAssignments()
    person = context.applicationService.getPersons(context.sessionToken, [me], fetchOptions)[me]
    for roleAssignment in person.roleAssignments:
        if roleAssignment.roleLevel == RoleLevel.INSTANCE and roleAssignment.role == Role.ADMIN:
            return True
    return False

def setCustomWidgetSettings(context, parameters):
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.update import PropertyTypeUpdate
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id import PropertyTypePermId
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue import ListUpdateActionAdd
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search import PropertyTypeSearchCriteria
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions import PropertyTypeFetchOptions

    widgetSettingsById = {PropertyTypePermId(ws["Property Type"]):ws for ws in parameters.get("widgetSettings")}
    sessionToken = context.applicationService.loginAsSystem();
    searchCriteria = PropertyTypeSearchCriteria()
    fetchOptions = PropertyTypeFetchOptions()
    ptus = [];
    propertyTypes = context.applicationService.searchPropertyTypes(sessionToken, searchCriteria, fetchOptions)
    for propertyType in propertyTypes.getObjects():
        id = propertyType.getPermId()
        ptu = PropertyTypeUpdate();
        ptu.setTypeId(id);
        metaData = ptu.getMetaData()
        if id in widgetSettingsById:
            metaData.add([{"custom_widget" : widgetSettingsById[id]["Widget"] }])
        else:
            metaData.remove(["custom_widget"])
        ptus.append(ptu);

    context.applicationService.updatePropertyTypes(sessionToken, ptus);
    return True

def isValidStoragePositionToInsertUpdate(context, parameters):
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions import SampleFetchOptions
    from ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search import SampleSearchCriteria
    from ch.systemsx.cisd.common.exceptions import UserFailureException

    samplePermId = parameters.get("samplePermId");
    sampleProperties = parameters.get("sampleProperties");
    storageCode = sampleProperties.get("$STORAGE_POSITION.STORAGE_CODE");
    storageRackRow = sampleProperties.get("$STORAGE_POSITION.STORAGE_RACK_ROW");
    storageRackColumn = sampleProperties.get("$STORAGE_POSITION.STORAGE_RACK_COLUMN");
    storageBoxName = sampleProperties.get("$STORAGE_POSITION.STORAGE_BOX_NAME");
    storageBoxSize = sampleProperties.get("$STORAGE_POSITION.STORAGE_BOX_SIZE");
    storageBoxPosition = sampleProperties.get("$STORAGE_POSITION.STORAGE_BOX_POSITION");

    storageUser = sampleProperties.get("$STORAGE_POSITION.STORAGE_USER");

    # 1. Obtain Storage to retrieve Storage Validation Level
    if storageCode is None:
        raise UserFailureException("Storage code missing");

    sessionToken = context.applicationService.loginAsSystem();
    searchCriteria = SampleSearchCriteria();
    searchCriteria.withCode().thatEquals(storageCode);
    searchCriteria.withType().withCode().thatEquals("STORAGE");

    fetchOptions = SampleFetchOptions();
    fetchOptions.withProperties();

    storage = None;
    storageValidationLevel = None;
    sampleSearchResults = context.applicationService.searchSamples(sessionToken, searchCriteria, fetchOptions).getObjects();
    if sampleSearchResults.size() == 1:
        storage = sampleSearchResults.get(0);
        storageValidationLevel = storage.getProperty("$STORAGE.STORAGE_VALIDATION_LEVEL");
    else:
        raise UserFailureException("Found: " + str(sampleSearchResults.size()) + " storages for storage code: " + storageCode);

    # 2. Check that the state of the sample is valid for the Storage Validation Level
    if storageRackRow is None or storageRackColumn is None:
        raise UserFailureException("Storage rack row or column missing");
    elif storageBoxName is None and (storageValidationLevel == "BOX" or storageValidationLevel == "BOX_POSITION"):
        raise UserFailureException("Storage box name missing");
    elif storageBoxSize is None and (storageValidationLevel == "BOX" or storageValidationLevel == "BOX_POSITION"):
        raise UserFailureException("Storage box size missing");
    elif storageBoxPosition is None and storageValidationLevel == "BOX_POSITION":
        raise UserFailureException("Storage box position missing");
    else:
        pass

    # 3. IF $STORAGE.STORAGE_VALIDATION_LEVEL >= RACK
    # 3.1 Check the rack exists, it should always be specified as an integer, failing the conversion is a valid error
    storageNumOfRowsAsInt = int(storage.getProperty("$STORAGE.ROW_NUM"));
    storageNumOfColAsInt = int(storage.getProperty("$STORAGE.COLUMN_NUM"));
    storageRackRowAsInt = int(storageRackRow)
    storageRackColAsInt = int(storageRackColumn)
    if storageRackRowAsInt > storageNumOfRowsAsInt or storageRackColAsInt > storageNumOfColAsInt:
        raise UserFailureException("Out of range row or column for the rack");

    # 4. IF $STORAGE.STORAGE_VALIDATION_LEVEL >= BOX
    if storageValidationLevel == "BOX" or storageValidationLevel == "BOX_POSITION":
        # 4.1 The number of total different box names on the rack including the given one should be below $STORAGE.BOX_NUM
        searchCriteriaStorageRack = SampleSearchCriteria();
        searchCriteriaStorageRack.withType().withCode().thatEquals("STORAGE_POSITION");
        searchCriteriaStorageRack.withProperty("$STORAGE_POSITION.STORAGE_CODE").thatEquals(storageCode);
        searchCriteriaStorageRack.withNumberProperty("$STORAGE_POSITION.STORAGE_RACK_ROW").thatEquals(int(storageRackRow));
        searchCriteriaStorageRack.withNumberProperty("$STORAGE_POSITION.STORAGE_RACK_COLUMN").thatEquals(int(storageRackColumn));
        searchCriteriaStorageRackResults = context.applicationService.searchSamples(sessionToken, searchCriteriaStorageRack, fetchOptions).getObjects();
        storageRackBoxes = {storageBoxName};
        for sample in searchCriteriaStorageRackResults:
            storageRackBoxes.add(sample.getProperty("$STORAGE_POSITION.STORAGE_BOX_NAME"));
        # 4.2 $STORAGE.BOX_NUM is only checked in is configured
        storageBoxNum = storage.getProperty("$STORAGE.BOX_NUM");
        if storageBoxNum is not None:
            storageBoxNumAsInt = int(storageBoxNum);
            if len(storageRackBoxes) > storageBoxNumAsInt:
                raise UserFailureException("Number of boxes in rack exceeded, use an existing box.");

    # 5. IF $STORAGE.STORAGE_VALIDATION_LEVEL >= BOX_POSITION
    if storageValidationLevel == "BOX_POSITION":
        # Storage position format validation (typical mistakes to check before doing any validation requiring database queries)
        if "," in storageBoxPosition:
            raise UserFailureException("Box positions are not separated by ',' but just a white space.");

        if "-" in storageBoxPosition:
            raise UserFailureException("Box positions can't contain ranges '-' .");

        storageBoxPositionRowsAndCols = storageBoxSize.split("X");
        storageBoxPositionNumRows = int(storageBoxPositionRowsAndCols[0]);
        storageBoxPositionNumCols = int(storageBoxPositionRowsAndCols[1]);
        for storageBoxSubPosition in storageBoxPosition.split(" "):
            storageBoxPositionRowNumber = getNumberFromLetter(storageBoxSubPosition[0]);
            if storageBoxPositionRowNumber is None:
                raise UserFailureException("Incorrect format for box position found ''" + storageBoxSubPosition + "'. The first character should be a letter.");
            if storageBoxPositionRowNumber > storageBoxPositionNumRows:
                raise UserFailureException("Row don't fit on the box for position: " + storageBoxSubPosition);
            if not storageBoxSubPosition[1:].isdigit():
                raise UserFailureException("Incorrect format for box position found ''" + storageBoxSubPosition + "'. After the first character only digits are allowed.");
            storageBoxPositionColNumber = int(storageBoxSubPosition[1:]);
            if storageBoxPositionColNumber > storageBoxPositionNumCols:
                raise UserFailureException("Column don't fit on the box for position: " + storageBoxSubPosition);
        #

        for storageBoxSubPosition in storageBoxPosition.split(" "):
            searchCriteriaStorageBoxPosition = SampleSearchCriteria();
            searchCriteriaStorageBoxPosition.withType().withCode().thatEquals("STORAGE_POSITION");
            searchCriteriaStorageBoxPosition.withProperty("$STORAGE_POSITION.STORAGE_CODE").thatEquals(storageCode);
            searchCriteriaStorageBoxPosition.withNumberProperty("$STORAGE_POSITION.STORAGE_RACK_ROW").thatEquals(int(storageRackRow));
            searchCriteriaStorageBoxPosition.withNumberProperty("$STORAGE_POSITION.STORAGE_RACK_COLUMN").thatEquals(int(storageRackColumn));

            if enableNewSearchEngine:
                searchCriteriaStorageBoxPosition.withProperty("$STORAGE_POSITION.STORAGE_BOX_NAME").thatEquals(storageBoxName);
            else: # Patch for Lucene
                import org.apache.lucene.queryparser.classic.QueryParserBase as QueryParserBase
                searchCriteriaStorageBoxPosition.withProperty("$STORAGE_POSITION.STORAGE_BOX_NAME").thatEquals(QueryParserBase.escape(storageBoxName));
            searchCriteriaStorageBoxPosition.withProperty("$STORAGE_POSITION.STORAGE_BOX_POSITION").thatContains(storageBoxSubPosition);
            searchCriteriaStorageBoxResults = context.applicationService.searchSamples(sessionToken, searchCriteriaStorageBoxPosition, fetchOptions).getObjects();
            # 5.1 If the given box position dont exists (the list is empty), is new
            for sample in searchCriteriaStorageBoxResults:
                if sample.getPermId().getPermId() != samplePermId and sample.getProperty("$STORAGE_POSITION.STORAGE_BOX_NAME") == storageBoxName and sample.getProperty("$STORAGE_POSITION.STORAGE_CODE") == storageCode:
                    # 5.3 If the given box position already exists, with a different permId -> Is an error
                    raise UserFailureException("Box Position " + storageBoxSubPosition + " is already used by " + sample.getPermId().getPermId());
                else:
                    # 5.2 If the given box position already exists with the same permId -> Is an update
                    pass

    return True

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
    sampleTypePrefix = querySampleTypePrefix.uniqueResult().upper();
    sampleTypePrefixLengthPlusOneAsString = str((len(sampleTypePrefix) + 1));
    querySampleCount = currentSession.createSQLQuery("SELECT COALESCE(MAX(CAST(substring(code, " + sampleTypePrefixLengthPlusOneAsString + ") as int)), 0) FROM samples_all WHERE saty_id = :sampleTypeId AND code ~ :codePattern");
    querySampleCount.setParameter("sampleTypeId", sampleTypeId);
    querySampleCount.setParameter("codePattern", "^" + sampleTypePrefix + "[0-9]+$");
    sampleCount = querySampleCount.uniqueResult();

    return (sampleCount + 1)

def getNextExperimentCode(context, parameters):
    daoFactory = CommonServiceProvider.getApplicationContext().getBean(ComponentNames.DAO_FACTORY);
    currentSession = daoFactory.getSessionFactory().getCurrentSession();

    projectId = int(parameters.get("projectId"));

    queryProjectCode = currentSession.createSQLQuery("SELECT code from projects WHERE id = :projectId");
    queryProjectCode.setParameter("projectId", projectId);
    projectCode = queryProjectCode.uniqueResult();

    experimentPrefix = projectCode + '_EXP_'
    experimentPrefixLengthPlusOneAsString = str((len(experimentPrefix) + 1));

    queryExperimentCount = currentSession.createSQLQuery("SELECT COALESCE(MAX(CAST(substring(code, " + experimentPrefixLengthPlusOneAsString + ") as int)), 0) FROM experiments_all WHERE proj_id = :projectId AND code ~ :codePattern");
    queryExperimentCount.setParameter("projectId", projectId);
    queryExperimentCount.setParameter("codePattern", "^" + experimentPrefix + "[0-9]+$");

    experimentCount = queryExperimentCount.uniqueResult();
    return experimentPrefix + str(experimentCount + 1)

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