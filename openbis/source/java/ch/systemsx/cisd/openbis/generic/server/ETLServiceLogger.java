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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatastoreServiceDescriptions;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * @author     Franz-Josef Elmer
 */
public class ETLServiceLogger extends AbstractServerLogger implements IETLService
{

    public ETLServiceLogger(final ISessionManager<Session> sessionManager,
            final boolean invocationSuccessful, final long elapsedTime)
    {
        super(sessionManager, invocationSuccessful, elapsedTime);
    }

    public String createDataSetCode(String sessionToken) throws UserFailureException
    {
        logTracking(sessionToken, "create_data_set_code", "");
        return null;
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
                "register_data_store_server_session_token",
                "CODE(%s) DOWNLOAD-URL(%s) PORT(%s) DSS-TOKEN(%s) REPORTING_PLUGINS(%s), PROCESSING_PLUGINS(%s)",
                code, downloadUrl, port, dssSessionToken, services
                        .getReportingServiceDescriptions(), services
                        .getProcessingServiceDescriptions());
    }

    public void registerSample(String sessionToken, NewSample newSample)
            throws UserFailureException
    {
        logTracking(sessionToken, "register_sample", "SAMPLE_TYPE(%s) SAMPLE(%S)", newSample
                .getSampleType(), newSample.getIdentifier());
    }

    public void registerDataSet(String sessionToken, SampleIdentifier sampleIdentifier,
            NewExternalData externalData) throws UserFailureException
    {
        logTracking(sessionToken, "register_data_set", "SAMPLE(%s) DATA_SET(%s)", sampleIdentifier,
                externalData);
    }

    public void registerDataSet(String sessionToken, ExperimentIdentifier experimentIdentifier,
            NewExternalData externalData) throws UserFailureException
    {
        logTracking(sessionToken, "register_data_set", "EXPERIMENT(%s) DATA_SET(%s)",
                experimentIdentifier, externalData);
    }

    public Experiment tryToGetExperiment(String sessionToken,
            ExperimentIdentifier experimentIdentifier) throws UserFailureException
    {
        logAccess(sessionToken, "get_experiment", "EXPERIMENT(%s)", experimentIdentifier);
        return null;
    }

    public List<Sample> listSamples(String sessionToken, ListSampleCriteria criteria)
    {
        logAccess(sessionToken, "list_samples", "CRITERIA(%s)", criteria);
        return null;
    }

    public Sample tryGetSampleWithExperiment(String sessionToken, SampleIdentifier sampleIdentifier)
            throws UserFailureException
    {
        logAccess(sessionToken, "get_sample_with_experiment", "SAMPLE(%s)", sampleIdentifier);
        return null;
    }

    public SampleType getSampleType(String sessionToken, String sampleTypeCode)
            throws UserFailureException
    {
        logAccess(sessionToken, "get_sample_type", "SAMPLE_TYPE(%s)", sampleTypeCode);
        return null;
    }

    public IEntityProperty[] tryToGetPropertiesOfTopSampleRegisteredFor(String sessionToken,
            SampleIdentifier sampleIdentifier) throws UserFailureException
    {
        logAccess(sessionToken, "get_properties_of_top_sample", "SAMPLE(%s)", sampleIdentifier);
        return null;
    }

    public ExternalData tryGetDataSet(String sessionToken, String dataSetCode)
            throws UserFailureException
    {
        logAccess(sessionToken, "try_get_data_set", "DATA_SET(%s)", dataSetCode);
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

    public SamplePE getSampleWithProperty(String sessionToken, String propertyTypeCode,
            GroupIdentifier groupIdentifier, String propertyValue)
    {
        logAccess(sessionToken, "get_sample_with_property",
                "PROPERTY_TYPE(%s) GROUP(%s) PROPERTY_VALUE(%s)", propertyTypeCode,
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

}
