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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.InvalidAuthenticationException;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class DataStoreServiceLogger implements IDataStoreService
{
    private static final String RESULT_SUCCESS = "";
    private static final String RESULT_FAILURE = " ...FAILED";

    private final Logger operationLog;
    private final boolean invocationSuccessful;

    DataStoreServiceLogger(Logger operationLog, boolean invocationSuccessful)
    {
        this.operationLog = operationLog;
        this.invocationSuccessful = invocationSuccessful;
    }
    
    private final void log(final String commandName,
            final String parameterDisplayFormat, final Object... parameters)
    {
        for (int i = 0; i < parameters.length; i++)
        {
            final Object parameter = parameters[i];
            if (parameter == null)
            {
                parameters[i] = "<UNDEFINED>";
            } else
            {
                parameters[i] = "'" + parameter + "'";
            }
        }
        final String message = String.format(parameterDisplayFormat, parameters);
        final String invocationStatusMessage = invocationSuccessful ? RESULT_SUCCESS : RESULT_FAILURE;
        // We put on purpose 2 spaces between the command and the message derived from the
        // parameters.
        operationLog.info(String.format("%s  %s%s", commandName, message, invocationStatusMessage));
    }
    
    public int getVersion(String sessionToken)
    {
        log("get_version", "SESSION(%s)", sessionToken);
        return 0;
    }

    public List<String> getKnownDataSets(String sessionToken, List<String> dataSetLocations)
            throws InvalidAuthenticationException
    {
        log("get_known_data_sets", "LOCATIONS(%s)", dataSetLocations);
        return null;
    }

    public void deleteDataSets(String sessionToken, List<String> dataSetLocations)
            throws InvalidAuthenticationException
    {
        log("delete_data_sets", "LOCATIONS(%s)", dataSetLocations);
    }

    public void uploadDataSetsToCIFEX(String sessionToken, List<ExternalDataPE> dataSets,
            DataSetUploadContext context) throws InvalidAuthenticationException
    {
        StringBuilder builder = new StringBuilder();
        for (ExternalDataPE externalDataPE : dataSets)
        {
            builder.append(' ').append(externalDataPE.getCode());
        }
        log("upload_data_sets", "USER(%s) LOCATIONS(%s)", context.getUserID(), builder.toString().trim());
    }

}
