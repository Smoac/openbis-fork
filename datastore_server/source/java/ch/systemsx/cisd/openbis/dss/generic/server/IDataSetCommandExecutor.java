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

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Executor of commands operating on data sets in a data store. Commands are expected to be executed
 * asynchronously in the order they had been scheduled.
 * 
 * @author Franz-Josef Elmer
 */
interface IDataSetCommandExecutor
{
    /**
     * Starts up executor.
     */
    void start();

    /**
     * Schedules deletion of all data sets at specified locations.
     */
    void scheduleDeletionOfDataSets(List<String> locations);

    /**
     * Schedules uploading of all data sets to CIFEX using the specified upload context.
     * 
     * @param cifexServiceFactory Factory for creating CIFEX upload service.
     * @param mailClientParameters Parameters needed for sending an e-mail to the user if uploading
     *            failed.
     */
    void scheduleUploadingDataSetsToCIFEX(ICIFEXRPCServiceFactory cifexServiceFactory,
            MailClientParameters mailClientParameters, List<ExternalData> dataSets,
            DataSetUploadContext uploadContext);

    /**
     * Schedules the specified processing task for provided datasets.
     * 
     * @param userEmailOrNull Email of user who initiated processing and will get a message after
     *            the processing is finished. It may be null if the user doesn't have email and no
     *            message will be send in such case.
     */
    void scheduleProcessDatasets(IProcessingPluginTask task, List<DatasetDescription> datasets,
            String userEmailOrNull, DatastoreServiceDescription serviceDescription);
}