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

import java.io.File;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * A command for deleting data sets, based on their location relative to the data store root.
 *
 * @author Franz-Josef Elmer
 */
class DeletionCommand extends AbstractDataSetDescriptionBasedCommand
{
    private static final long serialVersionUID = 1L;
    
    private final static Logger operationLog =
        LogFactory.getLogger(LogCategory.OPERATION, DeletionCommand.class);
    
    DeletionCommand(List<DatasetDescription> dataSets)
    {
        super(dataSets);
    }

    public void execute(final IDataSetDirectoryProvider dataSetDirectoryProvider)
    {
        final IShareIdManager shareIdManager = dataSetDirectoryProvider.getShareIdManager();
        final ISimpleLogger logger = createLogger();
        ThreadPoolExecutor executor =
                new ThreadPoolExecutor(1, 10, 360, TimeUnit.SECONDS,
                        new LinkedBlockingQueue<Runnable>());
        for (final DatasetDescription dataSet : dataSets)
        {
            executor.submit(new Runnable()
                {
                    public void run()
                    {
                        File dataSetDirectory =
                                dataSetDirectoryProvider.getDataSetDirectory(dataSet);
                        SegmentedStoreUtils.deleteDataSet(dataSet.getDatasetCode(),
                                dataSetDirectory, shareIdManager, logger);
                    }
                });
        }
    }

    @Private ISimpleLogger createLogger()
    {
        return new Log4jSimpleLogger(operationLog);
    }
    
    public String getDescription()
    {
        final StringBuilder b = new StringBuilder();
        b.append("Delete data sets: ");
        for (DatasetDescription dataset : dataSets)
        {
            b.append(dataset.getDatasetCode());
            b.append(',');
        }
        b.setLength(b.length() - 1);
        return b.toString();
    }

}
