/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.demo;

import java.io.File;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractDatastorePlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IArchiverTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivizationStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Piotr Buczek
 */
// TODO 2010-03-19, PTR: check HighWaterMark
public class DemoArchiver extends AbstractDatastorePlugin implements IArchiverTask
{
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private File storeRoot;

    public DemoArchiver(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
        this.storeRoot = storeRoot;
    }

    public ProcessingStatus archive(List<DatasetDescription> datasets)
    {
        System.out.println("Archivization of the following datasets has been requested: "
                + datasets);
        return handleDatasets(datasets, DataSetArchivizationStatus.ARCHIVED,
                DataSetArchivizationStatus.ACTIVE, new IDatasetDescriptionHandler()
                    {

                        public Status handle(DatasetDescription dataset)
                        {
                            try
                            {
                                // TODO 2010-03-19, Piotr Buczek: perform archivization
                            } catch (UserFailureException ex)
                            {
                                return Status.createError(ex.getMessage());
                            }
                            return Status.OK;
                        }

                    });
    }

    public ProcessingStatus unarchive(List<DatasetDescription> datasets)
    {
        System.out.println("Unarchivization of the following datasets has been requested: "
                + datasets);
        return handleDatasets(datasets, DataSetArchivizationStatus.ACTIVE,
                DataSetArchivizationStatus.ARCHIVED, new IDatasetDescriptionHandler()
                    {

                        public Status handle(DatasetDescription dataset)
                        {
                            try
                            {
                                // TODO 2010-03-19, Piotr Buczek: perform unarchivization
                            } catch (UserFailureException ex)
                            {
                                return Status.createError(ex.getMessage());
                            }
                            return Status.OK;
                        }

                    });
    }

    private ProcessingStatus handleDatasets(List<DatasetDescription> datasets,
            DataSetArchivizationStatus success, DataSetArchivizationStatus failure,
            IDatasetDescriptionHandler handler)
    {
        final ProcessingStatus result = new ProcessingStatus();
        for (DatasetDescription dataset : datasets)
        {
            Status status = handler.handle(dataset);
            DataSetArchivizationStatus newStatus = status.isError() ? failure : success;
            System.out.println(dataset + " changing status: " + newStatus);
            ServiceProvider.getOpenBISService().updateDataSetStatus(dataset.getDatasetCode(),
                    newStatus);
            result.addDatasetStatus(dataset, status);
        }
        return result;
    }

    private interface IDatasetDescriptionHandler
    {
        public Status handle(DatasetDescription dataset);
    }

}
