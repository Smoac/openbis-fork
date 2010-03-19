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
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IArchiverTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ProcessingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Piotr Buczek
 */
public class DemoArchiver implements IArchiverTask
{
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private File storeRoot;

    public DemoArchiver(Properties properties, File storeRoot)
    {
        this.storeRoot = storeRoot;
    }

    public ProcessingStatus archive(List<DatasetDescription> datasets)
    {
        System.out.println("Archivization of the following datasets has been requested: "
                + datasets);
        return doNothing(datasets);
    }

    public ProcessingStatus unarchive(List<DatasetDescription> datasets)
    {
        System.out.println("Unarchivization of the following datasets has been requested: "
                + datasets);
        return doNothing(datasets);
    }

    private ProcessingStatus doNothing(List<DatasetDescription> datasets)
    {
        // TODO PTR: check HighWaterMark
        final ProcessingStatus result = new ProcessingStatus();
        for (DatasetDescription dataset : datasets)
        {
            result.addDatasetStatus(dataset, Status.OK);
        }
        return result;
    }

}
