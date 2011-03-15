/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IArchiverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;

/**
 * Maintenance task that removes data sets from the data store archive after they have been deleted
 * in openBIS.
 * 
 * @author Kaloyan Enimanev
 */
public class DeleteFromArchiveMaintenanceTask extends
        AbstractDataSetDeletionPostProcessingMaintenanceTask
{

    // a file name to store the last seen event id
    private static final String STATUS_FILENAME = "status-filename";

    private File lastSeenEventIdFile;

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        super.setUp(pluginName, properties);
        String eventIdFileName = PropertyUtils.getMandatoryProperty(properties, STATUS_FILENAME);
        lastSeenEventIdFile = new File(eventIdFileName);
    }

    @Override
    protected Long getLastSeenEventId()
    {
        Long result = null;
        if (lastSeenEventIdFile.exists())
        {
            try
            {
                String statusFileContent = FileUtilities.loadToString(lastSeenEventIdFile);
                result = Long.parseLong(statusFileContent);
            } catch (Exception ex)
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug("Cannot load last seen event id from file :"
                            + lastSeenEventIdFile, ex);
                }
            }
        }
        return result;
    }

    @Override
    protected void updateLastSeenEventId(Long newLastSeenEventId)
    {
        try
        {
            // create a temporary file (not an atomic operation)
            File tmpFile =
                    File.createTempFile(lastSeenEventIdFile.getName(), "tmp",
                            lastSeenEventIdFile.getParentFile());
            String fileContent = String.valueOf(newLastSeenEventId);
            FileUtilities.writeToFile(tmpFile, fileContent);

            // move operation (should be atomic)
            tmpFile.renameTo(lastSeenEventIdFile);
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @Override
    protected void execute(List<DeletedDataSet> datasets)
    {
        List<DeletedDataSet> datasetsToBeArchived = datasets;
        if (lastSeenEventIdFile.exists() == false)
        {
            datasetsToBeArchived = filterOldStyleHistoryEvents(datasets);
        }

        IArchiverPlugin archiverPlugin = ServiceProvider.getDataStoreService().getArchiverPlugin();
        archiverPlugin.deleteFromArchive(datasetsToBeArchived);

        String logMessage =
                String.format("Deleted %s dataset from archive: '%s'", datasetsToBeArchived.size(),
                        CollectionUtils.abbreviate(datasetsToBeArchived, 10));
        operationLog.info(logMessage);

    }

    // NOTE: Before the introduction of eager-archiving the column which now corresponds to
    // "deletedDataset.location" used to contain the dataset identifier. To avoid triggering
    // deletion from archive for such invalid events we filter them out. This only happens when in
    // the first run of the maintenance task.
    private List<DeletedDataSet> filterOldStyleHistoryEvents(List<DeletedDataSet> datasets)
    {
        List<DeletedDataSet> result = new ArrayList<DeletedDataSet>();
        for (DeletedDataSet dataset : datasets) {
            if (dataset.getIdentifier() != null
                    && false == dataset.getIdentifier().equals(dataset.getLocation()))
            {
                result.add(dataset);
            }
        }
        return result;
    }

}
