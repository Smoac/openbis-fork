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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Franz-Josef Elmer
 */
class DataStoreServiceLogger implements IDataStoreService
{
    private static final String RESULT_SUCCESS = "";

    private static final String RESULT_FAILURE = " ...FAILED";

    private final Logger operationLog;

    private final boolean invocationSuccessful;

    private final long elapsedTime;

    DataStoreServiceLogger(Logger operationLog, boolean invocationSuccessful, long elapsedTime)
    {
        this.operationLog = operationLog;
        this.invocationSuccessful = invocationSuccessful;
        this.elapsedTime = elapsedTime;
    }

    private final void log(final String commandName, final String parameterDisplayFormat,
            final Object... parameters)
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
        final String invocationStatusMessage =
                invocationSuccessful ? RESULT_SUCCESS : RESULT_FAILURE;
        // We put on purpose 2 spaces between the command and the message derived from the
        // parameters.
        operationLog.info(String.format("%s  %s%s (%s ms)", commandName, message,
                invocationStatusMessage, elapsedTime));
    }

    public int getVersion(String sessionToken)
    {
        log("getVersion", "SESSION(%s)", sessionToken);
        return 0;
    }

    public List<String> getKnownDataSets(String sessionToken, List<String> dataSetLocations)
            throws InvalidAuthenticationException
    {
        log("getKnownDataSets", "LOCATIONS(%s)", dataSetLocations);
        return null;
    }

    public void deleteDataSets(String sessionToken, List<String> dataSetLocations)
            throws InvalidAuthenticationException
    {
        log("deleteDataSets", "LOCATIONS(%s)", dataSetLocations);
    }

    public void uploadDataSetsToCIFEX(String sessionToken, List<ExternalData> dataSets,
            DataSetUploadContext context) throws InvalidAuthenticationException
    {
        StringBuilder builder = new StringBuilder();
        for (ExternalData externalDataPE : dataSets)
        {
            builder.append(' ').append(externalDataPE.getCode());
        }
        log("uploadDataSetsToCIFEX", "USER(%s) DATASETS(%s)", context.getUserID(), builder
                .toString().trim());
    }

    public TableModel createReportFromDatasets(String sessionToken, String serviceKey,
            List<DatasetDescription> datasets)
    {
        log("createReportFromDatasets", "TASK_ID(%s) NO_OF_DATASETS(%s)", serviceKey, datasets
                .size());
        return null;
    }

    public void processDatasets(String sessionToken, String serviceKey,
            List<DatasetDescription> datasets, String userEmailOrNull)
    {
        log("processDatasets", "TASK_ID(%s) NO_OF_DATASETS(%s) USER_EMAIL(%s)", serviceKey,
                datasets.size(), userEmailOrNull);
    }

}
