/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.parallelizedExecutor;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.config.SyncConfig;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.ResourceListParserData.IncomingDataSet;
import ch.systemsx.cisd.common.concurrent.ITaskExecutor;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;

/**
 * 
 *
 * @author Ganime Betul Akin
 */
public final class DataSetRegistrationTaskExecutor implements ITaskExecutor<IncomingDataSet>
{
    private final File storeRoot;

    private final DataSetProcessingContext context;

    private final Logger operationLog;

    private DataSetSynchronizationSummary dsRegistrationSummary;

    private final SyncConfig config;

    public DataSetRegistrationTaskExecutor(DataSetSynchronizationSummary dsRegSummary, Logger operationLog, File storeRoot,
            DataSetProcessingContext context, SyncConfig config)
    {
        this.dsRegistrationSummary = dsRegSummary;
        this.operationLog = operationLog;
        this.storeRoot = storeRoot;
        this.context = context;
        this.config = config;
    }

    @Override
    public Status execute(IncomingDataSet dataSet)
    {
        Properties props = setProperties();

        DataSetRegistrationIngestionService ingestionService =
                new DataSetRegistrationIngestionService(props, storeRoot, dataSet.getDataSet(), operationLog);
        TableModel resultTable = ingestionService.createAggregationReport(new HashMap<String, Object>(), context);
        if (resultTable != null)
        {
            List<TableModelColumnHeader> headers = resultTable.getHeader();
            String[] stringArray = new String[headers.size()];
            for (int i = 0; i < stringArray.length; i++)
            {
                if (headers.get(i).getTitle().startsWith("Error"))
                {
                    String message = resultTable.getRows().get(0).getValues().toString();
                    dsRegistrationSummary.notRegisteredDataSetCodes.add(dataSet.getDataSet().getCode());
                    operationLog.error(message);
                    return Status.createError(message);
                }
                else if (headers.get(i).getTitle().startsWith("Added"))
                {
                    dsRegistrationSummary.addedDsCount.getAndIncrement();
                }
                else if (headers.get(i).getTitle().startsWith("Updated"))
                {
                    dsRegistrationSummary.updatedDsCount.getAndIncrement();
                }
            }
        }
        return Status.OK;
    }

    private Properties setProperties()
    {
        Properties props = new Properties();
        props.setProperty("user", config.getUser());
        props.setProperty("pass", config.getPassword());
        props.setProperty("as-url", config.getDataSourceOpenbisURL());
        props.setProperty("dss-url", config.getDataSourceDSSURL());
        props.setProperty("harvester-temp-dir", config.getHarvesterTempDir());
        props.setProperty("do-not-create-original-dir", "true");
        return props;
    }
}
