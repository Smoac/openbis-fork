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

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ETLServiceLogger extends AbstractServerLogger implements IETLService
{

    public ETLServiceLogger(ISessionManager<Session> sessionManager, boolean invocationSuccessful)
    {
        super(sessionManager, invocationSuccessful);
    }

    public String createDataSetCode(String sessionToken) throws UserFailureException
    {
        logTracking(sessionToken, "create_data_set_code", "");
        return null;
    }

    public DatabaseInstancePE getHomeDatabaseInstance(String sessionToken)
    {
        return null;
    }

    public void registerDataStoreServer(String sessionToken, int port, String dssSessionToken)
    {
        logTracking(sessionToken, "register_data_store_server_session_token", "PORT(%s) DSS-TOKEN(%s)",
                port, dssSessionToken);
    }

    public void registerDataSet(String sessionToken, SampleIdentifier sampleIdentifier,
            String procedureTypeCode, ExternalData externalData) throws UserFailureException
    {
        logTracking(sessionToken, "register_data_set", "SAMPLE(%s) PROCEDURE(%s) DATA_SET(%s)",
                sampleIdentifier, procedureTypeCode, externalData);
    }

    public ExperimentPE tryToGetBaseExperiment(String sessionToken,
            SampleIdentifier sampleIdentifier) throws UserFailureException
    {
        logAccess(sessionToken, "get_experiment", "SAMPLE(%s)", sampleIdentifier);
        return null;
    }

    public SamplePropertyPE[] tryToGetPropertiesOfTopSampleRegisteredFor(String sessionToken,
            SampleIdentifier sampleIdentifier) throws UserFailureException
    {
        logAccess(sessionToken, "get_properties_of_top_sample", "SAMPLE(%s)", sampleIdentifier);
        return null;
    }

    public String authenticate(String user, String password) throws UserFailureException
    {
        return null;
    }

    public void closeSession(String sessionToken) throws UserFailureException
    {
    }

    public DataStorePE getDataStore(String sessionToken, ExperimentIdentifier experimentIdentifier,
            String dataSetTypeCode) throws UserFailureException
    {
        logAccess(sessionToken, "get_data_store", "EXPERIMENT(%s) DATA_SET(%s)",
                experimentIdentifier, dataSetTypeCode);
        return null;
    }

    public ExternalDataPE tryGetDataSet(String sessionToken, String dataSetCode)
            throws UserFailureException
    {
        logAccess(sessionToken, "try_get_data_set", "DATA_SET(%s)", dataSetCode);
        return null;
    }

}
