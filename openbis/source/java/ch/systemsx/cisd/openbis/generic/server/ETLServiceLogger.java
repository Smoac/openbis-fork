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

import java.util.List;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ArchiverDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatastoreServiceDescriptions;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
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
                code, downloadUrl, port, dssSessionToken, services
                        .getReportingServiceDescriptions(), services
                        .getProcessingServiceDescriptions());
    }

    public long registerSample(String sessionToken, NewSample newSample, String userIDOrNull)
            throws UserFailureException
    {
        logTracking(sessionToken, "registerSample", "SAMPLE_TYPE(%s) SAMPLE(%S) USER(%s)",
                newSample.getSampleType(), newSample.getIdentifier(), userIDOrNull);
        return 0;
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

    public List<ExternalData> listDataSetsBySampleID(final String sessionToken,
            final TechId sampleId, final boolean showOnlyDirectlyConnected)
    {
        logAccess(sessionToken, "listDataSetsBySampleID", "SAMPLE_ID(%s)", sampleId);
        return null;
    }

    public IEntityProperty[] tryToGetPropertiesOfTopSampleRegisteredFor(String sessionToken,
            SampleIdentifier sampleIdentifier) throws UserFailureException
    {
        logAccess(sessionToken, "tryToGetPropertiesOfTopSampleRegisteredFor", "SAMPLE(%s)",
                sampleIdentifier);
        return null;
    }

    public void checkDataSetAccess(String sessionToken, String dataSetCode)
            throws UserFailureException
    {
        logAccess(sessionToken, "checkDataSetAccess", "DATA_SET(%s)", dataSetCode);
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

    public SamplePE getSampleWithProperty(String sessionToken, String propertyTypeCode,
            GroupIdentifier groupIdentifier, String propertyValue)
    {
        logAccess(sessionToken, "getSampleWithProperty",
                "PROPERTY_TYPE(%s) SPACE(%s) PROPERTY_VALUE(%s)", propertyTypeCode,
                groupIdentifier, propertyValue);
        return null;
    }

    public List<DeletedDataSet> listDeletedDataSets(String sessionToken,
            Long lastSeenDeletionEventIdOrNull)
    {
        logAccess(sessionToken, "listDeletedDataSets", "LAST_SEEN_EVENT(%s)",
                (lastSeenDeletionEventIdOrNull == null ? "all" : "id > "
                        + lastSeenDeletionEventIdOrNull));
        return null;
    }

    public void addPropertiesToDataSet(String sessionToken, List<NewProperty> properties,
            String dataSetCode, SpaceIdentifier space) throws UserFailureException
    {
        logTracking(sessionToken, "updateDataSet", "DATA_SET_CODE(%s) PROPERTIES(%s)", dataSetCode,
                properties.size());
    }

    public void updateDataSetStatus(String sessionToken, String dataSetCode,
            DataSetArchivingStatus newStatus) throws UserFailureException
    {
        logTracking(sessionToken, "updateDataSetStatus", "DATA_SET_CODE(%s) STATUS(%s)",
                dataSetCode, newStatus);
    }

}
