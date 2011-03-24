/*
 * Copyright 2009 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.server;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetShareId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatastoreServiceDescriptions;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityCollectionForCreationOrUpdate;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author Franz-Josef Elmer
 */
public class ETLServiceLogger extends AbstractServerLogger implements IETLService
{

    public ETLServiceLogger(final ISessionManager<Session> sessionManager,
            IInvocationLoggerContext context)
    {
        super(sessionManager, context);
    }

    public String createDataSetCode(String sessionToken) throws UserFailureException
    {
        logTracking(sessionToken, "createDataSetCode", "");
        return null;
    }

    public String createPermId(String sessionToken) throws UserFailureException
    {
        logTracking(sessionToken, "createPermId", "");
        return null;
    }

    public long drawANewUniqueID(String sessionToken) throws UserFailureException
    {
        logTracking(sessionToken, "drawANewUniqueID", "");
        return 0;
    }

    public DatabaseInstance getHomeDatabaseInstance(String sessionToken)
    {
        return null;
    }

    public void registerDataStoreServer(String sessionToken, DataStoreServerInfo info)
    {
        String code = info.getDataStoreCode();
        String downloadUrl = info.getDownloadUrl();
        int port = info.getPort();
        String dssSessionToken = info.getSessionToken();
        DatastoreServiceDescriptions services = info.getServicesDescriptions();
        logTracking(
                sessionToken,
                "registerDataStoreServer",
                "CODE(%s) DOWNLOAD-URL(%s) PORT(%s) DSS-TOKEN(%s) REPORTING_PLUGINS(%s), PROCESSING_PLUGINS(%s)",
                code, downloadUrl, port, dssSessionToken,
                services.getReportingServiceDescriptions(),
                services.getProcessingServiceDescriptions());
    }

    public long registerSample(String sessionToken, NewSample newSample, String userIDOrNull)
            throws UserFailureException
    {
        logTracking(sessionToken, "registerSample", "SAMPLE_TYPE(%s) SAMPLE(%S) USER(%s)",
                newSample.getSampleType(), newSample.getIdentifier(), userIDOrNull);
        return 0;
    }

    public void updateSample(String sessionToken, SampleUpdatesDTO updates)
    {
        logTracking(sessionToken, "updateSample", "SAMPLE(%S)", updates.getSampleIdentifier());
    }

    public void registerEntities(String sessionToken, EntityCollectionForCreationOrUpdate collection)
            throws UserFailureException
    {
        List<NewExperiment> newExperiments = collection.getNewExperiments();
        List<NewExternalData> newDataSets = collection.getNewDataSets();
        logTracking(sessionToken, "registerEntities", "NEW_EXPERIMENTS(%s) NEW_DATA_SETS(%s)",
                newExperiments.size(), newDataSets.size());
    }

    public long registerExperiment(String sessionToken, NewExperiment experiment)
            throws UserFailureException
    {
        logTracking(sessionToken, "registerExperiment", "EXPERIMENT_TYPE(%s) EXPERIMENT(%S)",
                experiment.getExperimentTypeCode(), experiment.getIdentifier());
        return 0;
    }

    public void registerDataSet(String sessionToken, SampleIdentifier sampleIdentifier,
            NewExternalData externalData) throws UserFailureException
    {
        logTracking(sessionToken, "registerDataSet", "SAMPLE(%s) DATA_SET(%s)", sampleIdentifier,
                externalData);
    }

    public void registerDataSet(String sessionToken, ExperimentIdentifier experimentIdentifier,
            NewExternalData externalData) throws UserFailureException
    {
        logTracking(sessionToken, "registerDataSet", "EXPERIMENT(%s) DATA_SET(%s)",
                experimentIdentifier, externalData);
    }

    public void deleteDataSet(String sessionToken, String dataSetCode, String reason)
            throws UserFailureException
    {
        logTracking(sessionToken, "deleteDataSet", "DATA_SET(%s) REASON(%s)", dataSetCode, reason);
    }

    public Experiment tryToGetExperiment(String sessionToken,
            ExperimentIdentifier experimentIdentifier) throws UserFailureException
    {
        logAccess(sessionToken, "tryToGetExperiment", "EXPERIMENT(%s)", experimentIdentifier);
        return null;
    }

    public List<Sample> listSamples(String sessionToken, ListSampleCriteria criteria)
    {
        logAccess(sessionToken, "listSamples", "CRITERIA(%s)", criteria);
        return null;
    }

    public Sample tryGetSampleWithExperiment(String sessionToken, SampleIdentifier sampleIdentifier)
            throws UserFailureException
    {
        logAccess(sessionToken, "tryGetSampleWithExperiment", "SAMPLE(%s)", sampleIdentifier);
        return null;
    }

    public SampleIdentifier tryToGetSampleIdentifier(String sessionToken, String samplePermID)
            throws UserFailureException
    {
        logAccess(sessionToken, "tryToGetSampleIdentifier", "SAMPLE(%s)", samplePermID);
        return null;
    }

    public ExperimentType getExperimentType(String sessionToken, String experimentTypeCode)
            throws UserFailureException
    {
        logAccess(sessionToken, "getExperimentType", "EXPERIMENT_TYPE(%s)", experimentTypeCode);
        return null;
    }

    public SampleType getSampleType(String sessionToken, String sampleTypeCode)
            throws UserFailureException
    {
        logAccess(sessionToken, "getSampleType", "SAMPLE_TYPE(%s)", sampleTypeCode);
        return null;
    }

    public DataSetTypeWithVocabularyTerms getDataSetType(String sessionToken, String dataSetTypeCode)
    {
        logAccess(sessionToken, "getDataSetType", "DATA_SET_TYPE(%s)", dataSetTypeCode);
        return null;
    }

    public List<ExternalData> listDataSetsByExperimentID(String sessionToken, TechId experimentID)
            throws UserFailureException
    {
        logAccess(sessionToken, "listDataSetsByExperimentID", "EXPERIMENT_ID(%s)", experimentID);
        return null;
    }

    public List<ExternalData> listDataSetsBySampleID(final String sessionToken,
            final TechId sampleId, final boolean showOnlyDirectlyConnected)
    {
        logAccess(sessionToken, "listDataSetsBySampleID", "SAMPLE_ID(%s)", sampleId);
        return null;
    }

    public List<ExternalData> listDataSetsByCode(String sessionToken, List<String> dataSetCodes)
            throws UserFailureException
    {
        logAccess(sessionToken, "listDataSetsByCode", "DATA_SETS(%s)", dataSetCodes);
        return null;
    }

    public IEntityProperty[] tryToGetPropertiesOfTopSampleRegisteredFor(String sessionToken,
            SampleIdentifier sampleIdentifier) throws UserFailureException
    {
        logAccess(sessionToken, "tryToGetPropertiesOfTopSampleRegisteredFor", "SAMPLE(%s)",
                sampleIdentifier);
        return null;
    }

    public void checkInstanceAdminAuthorization(String sessionToken) throws UserFailureException
    {
        logAccess(sessionToken, "checkInstanceAdminAuthorization");
    }

    public void checkDataSetAccess(String sessionToken, String dataSetCode)
            throws UserFailureException
    {
        logAccess(sessionToken, "checkDataSetAccess", "DATA_SET(%s)", dataSetCode);
    }

    public void checkDataSetCollectionAccess(String sessionToken, List<String> dataSetCodes)
    {
        logTracking(sessionToken, "checkDataSetCollectionAccess", "DATA_SET_CODES(%s)",
                dataSetCodes);
    }

    public void checkSpaceAccess(String sessionToken, SpaceIdentifier spaceId)
            throws UserFailureException
    {
        logAccess(sessionToken, "checkSpaceAccess", "SPACE(%s)", spaceId);
    }

    public ExternalData tryGetDataSet(String sessionToken, String dataSetCode)
            throws UserFailureException
    {
        logAccess(sessionToken, "tryGetDataSet", "DATA_SET(%s)", dataSetCode);
        return null;
    }

    public List<Sample> listSamplesByCriteria(String sessionToken,
            ListSamplesByPropertyCriteria criteria) throws UserFailureException
    {
        logAccess(sessionToken, "listSamplesByCriteria", "criteria(%s)", criteria);
        return null;
    }

    public List<DataSetShareId> listShareIds(String sessionToken, String dataStore)
            throws UserFailureException
    {
        logAccess(sessionToken, "listShareIds", "DATA_STORE(%s)", dataStore);
        return null;
    }

    public List<SimpleDataSetInformationDTO> listDataSets(String sessionToken, String dataStore)
            throws UserFailureException
    {
        logAccess(sessionToken, "listDataSets", "DATA_STORE(%s)", dataStore);
        return null;
    }

    public List<ExternalData> listAvailableDataSets(String sessionToken, String dataStoreCode,
            ArchiverDataSetCriteria criteria)
    {
        logAccess(sessionToken, "listAvailableDataSets", "DATA_STORE(%s) CRITERIA(%s)",
                dataStoreCode, criteria);
        return null;
    }

    public List<ExternalData> listDataSets(String sessionToken, String dataStoreCode,
            TrackingDataSetCriteria criteria)
    {
        logAccess(sessionToken, "listDataSets", "DATA_STORE(%s) CRITERIA(%s)", dataStoreCode,
                criteria);
        return null;
    }

    public SamplePE getSampleWithProperty(String sessionToken, String propertyTypeCode,
            GroupIdentifier groupIdentifier, String propertyValue)
    {
        logAccess(sessionToken, "getSampleWithProperty",
                "PROPERTY_TYPE(%s) SPACE(%s) PROPERTY_VALUE(%s)", propertyTypeCode,
                groupIdentifier, propertyValue);
        return null;
    }

    public List<DeletedDataSet> listDeletedDataSets(String sessionToken,
            Long lastSeenDeletionEventIdOrNull, Date maxDeletionDateOrNull)
    {
        logAccess(sessionToken, "listDeletedDataSets", "LAST_SEEN_EVENT(%s)",
                (lastSeenDeletionEventIdOrNull == null ? "all" : "id > "
                        + lastSeenDeletionEventIdOrNull), (maxDeletionDateOrNull == null ? "all"
                        : "maxDeletionDate > " + maxDeletionDateOrNull));
        return null;
    }

    public void addPropertiesToDataSet(String sessionToken, List<NewProperty> properties,
            String dataSetCode, SpaceIdentifier space) throws UserFailureException
    {
        logTracking(sessionToken, "updateDataSet", "DATA_SET_CODE(%s) PROPERTIES(%s)", dataSetCode,
                properties.size());
    }

    public void updateShareIdAndSize(String sessionToken, String dataSetCode, String shareId,
            long size) throws UserFailureException
    {
        logTracking(sessionToken, "updateShareIdAndSize", "DATA_SET_CODE(%s) SHARE_ID(%s) SIZE(%s)",
                dataSetCode, shareId, size);
    }

    public void updateDataSetStatuses(String sessionToken, List<String> dataSetCodes,
            DataSetArchivingStatus newStatus, boolean presentInArchive) throws UserFailureException
    {
        logTracking(sessionToken, "updateDataSetStatus",
                "NO_OF_DATASETS(%s) STATUS(%s) PRESENT_IN_ARCHIVE(%s)", dataSetCodes.size(),
                newStatus, presentInArchive);
    }

    public boolean compareAndSetDataSetStatus(String token, String dataSetCode,
            DataSetArchivingStatus oldStatus, DataSetArchivingStatus newStatus,
            boolean newPresentInArchive) throws UserFailureException
    {
        logTracking(token, "compareAndSetDataSetStatus",
                "DATASET_COE(%s) OLD_STATUS(%s) NEW_STATUS(%s) NEW_PRESENT_IN_ARCHIVE(%s)",
                dataSetCode, oldStatus, newStatus, newPresentInArchive);
        return false;
    }

    public String getDefaultPutDataStoreBaseURL(String sessionToken)
    {
        logAccess(sessionToken, "getDefaultPutDataStoreBaseURL");
        return null;
    }

    public ExternalData tryGetDataSetForServer(String sessionToken, String dataSetCode)
            throws UserFailureException
    {

        logAccess(sessionToken, "tryGetDataSetForServer", "DATA_SET(%s)", dataSetCode);
        return null;
    }

    public Collection<VocabularyTerm> listVocabularyTerms(String sessionToken, String vocabulary)
            throws UserFailureException
    {
        logAccess(sessionToken, "listVocabularyTerms", "VOCABULARY(%s)", vocabulary);
        return null;
    }

    public void registerSamples(String sessionToken, List<NewSamplesWithTypes> newSamplesWithType,
            String userIdOrNull) throws UserFailureException
    {

        logTracking(sessionToken, "registerSamples", "NO_OF_SAMPLES(%s) USER(%s)",
                print(newSamplesWithType), userIdOrNull);

    }

    private String print(List<NewSamplesWithTypes> newSamplesWithType)
    {
        StringBuilder sb = new StringBuilder();
        for (NewSamplesWithTypes samples : newSamplesWithType)
        {
            if (sb.length() != 0)
            {
                sb.append(", ");
            }
            sb.append(samples.getSampleType().getCode());
            sb.append(":").append(samples.getNewSamples().size());
        }
        return sb.toString();
    }

    public List<String> generateCodes(String sessionToken, String prefix, int number)
    {
        logAccess(sessionToken, "generateCodes", "PREFIX(%s) NUMBER(%s)", prefix, number);
        return null;
    }

    public List<Person> listAdministrators(String sessionToken)
    {
        logAccess(sessionToken, "listAdministrators");
        return null;
    }

    public Person tryPersonWithUserIdOrEmail(String sessionToken, String useridOrEmail)
    {
        logAccess(sessionToken, "tryPersonWithUserIdOrEmail", "USERID_OR_EMAIL(%s)", useridOrEmail);
        return null;
    }

    public Sample registerSampleAndDataSet(String sessionToken, NewSample newSample,
            NewExternalData externalData, String userIdOrNull) throws UserFailureException
    {
        logAccess(sessionToken, "registerSampleAndDataSet",
                "SAMPLE_TYPE(%s) SAMPLE(%S) DATA_SET(%s) USER(%s)", newSample.getSampleType(),
                newSample.getIdentifier(), externalData, userIdOrNull);
        return null;
    }

    public Sample updateSampleAndRegisterDataSet(String sessionToken, SampleUpdatesDTO updates,
            NewExternalData externalData)
    {
        logAccess(sessionToken, "updateSampleAndRegisterDataSet", "SAMPLE(%S) DATA_SET(%s)",
                updates.getSampleIdentifier(), externalData);
        return null;
    }

    public AtomicEntityOperationResult performEntityOperations(String sessionToken,
            AtomicEntityOperationDetails operationDetails)
    {
        logAccess(sessionToken, "performEntityOperations", "%s", operationDetails);
        return null;
    }

    public Space tryGetSpace(String sessionToken, SpaceIdentifier spaceIdentifier)
    {
        logAccess(sessionToken, "tryGetSpace", "%s", spaceIdentifier);
        return null;
    }

    public Project tryGetProject(String sessionToken, ProjectIdentifier projectIdentifier)
    {
        logAccess(sessionToken, "tryGetProject", "%s", projectIdentifier);
        return null;
    }

}
