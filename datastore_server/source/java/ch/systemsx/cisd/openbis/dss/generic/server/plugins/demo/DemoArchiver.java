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
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractArchiverProcessingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Piotr Buczek
 */
public class DemoArchiver extends AbstractArchiverProcessingPlugin
{
    private static final long serialVersionUID = 1L;

    private final static Set<String/* data set code */> archiveContents = new HashSet<String>();

    public DemoArchiver(Properties properties, File storeRoot)
    {
        super(properties, storeRoot, null, null);
    }

    @Override
    protected DatasetProcessingStatuses doArchive(List<DatasetDescription> datasets,
            ArchiverTaskContext context) throws UserFailureException
    {
        System.out.println("DemoArchiver - Archived: " + datasets);
        archiveContents.addAll(DatasetDescription.extractCodes(datasets));
        return createStatuses(Status.OK, datasets, Operation.ARCHIVE);
    }

    @Override
    protected DatasetProcessingStatuses doUnarchive(List<DatasetDescription> datasets,
            ArchiverTaskContext context) throws UserFailureException
    {
        System.out.println("DemoArchiver - Unarchived: " + datasets);
        return createStatuses(Status.OK, datasets, Operation.UNARCHIVE);
    }

    @Override
    public BooleanStatus isDataSetPresentInArchive(DatasetDescription dataset,
            ArchiverTaskContext context)
    {
        boolean present = archiveContents.contains(dataset.getDatasetCode());
        return BooleanStatus.createFromBoolean(present);
    }

    @Override
    public DatasetProcessingStatuses doDeleteFromArchive(List<DeletedDataSet> dataSets)
    {
        List<String> datasetCodes = DeletedDataSet.extractDataSetCodes(dataSets);
        archiveContents.addAll(datasetCodes);
        System.out.println("DemoArchiver - deleteFromArchive: " + datasetCodes);
        DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();
        for (String dataset : datasetCodes)
        {
            statuses.addResult(dataset, Status.OK, Operation.DELETE_FROM_ARCHIVE);
        }
        return statuses;
    }

}
