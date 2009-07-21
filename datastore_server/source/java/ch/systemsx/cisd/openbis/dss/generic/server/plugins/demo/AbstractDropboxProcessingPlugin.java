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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.demo;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.AbstractDatasetDropboxHandler;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Tomasz Pylak
 */
public class AbstractDropboxProcessingPlugin extends AbstractDatastorePlugin implements
        IProcessingPluginTask
{
    private static final long serialVersionUID = 1L;

    final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AbstractDatasetDropboxHandler.class);

    private final AbstractDatasetDropboxHandler dropboxHandler;

    /**
     * Note that this class is not a valid processing plugin as it does not provide the appropriate
     * constructor.
     */
    public AbstractDropboxProcessingPlugin(Properties properties, File storeRoot,
            AbstractDatasetDropboxHandler dropboxHandler)
    {
        super(properties, storeRoot);
        this.dropboxHandler = dropboxHandler;
    }

    public void process(List<DatasetDescription> datasets)
    {
        for (DatasetDescription dataset : datasets)
        {
            File originalDir = getOriginalDir(dataset);
            if (originalDir.isDirectory() == false)
            {
                operationLog
                        .warn("Dataset directory does not exist and will be silently excluded from the processing: "
                                + originalDir.getPath());
                continue;
            }
            File[] datasetFiles = FileUtilities.listFiles(originalDir);
            if (datasetFiles.length == 1)
            {
                DataSetInformation datasetInfo = createDatasetInfo(dataset);
                dropboxHandler.handle(datasetFiles[0], datasetInfo);
            } else if (datasetFiles.length > 1)
            {
                operationLog.error(String.format(
                        "Exactly one item was expected in the '%s' directory,"
                                + " but %d have been found. Nothing will be processed.",
                        originalDir.getParent(), datasetFiles.length));
            }
        }
    }

    private DataSetInformation createDatasetInfo(DatasetDescription dataset)
    {
        DataSetInformation datasetInfo = new DataSetInformation();
        datasetInfo.setSampleCode(dataset.getSampleCode());
        datasetInfo.setGroupCode(dataset.getGroupCode());
        datasetInfo.setDataSetCode(dataset.getDatasetCode());
        return datasetInfo;
    }
}
