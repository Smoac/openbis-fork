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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IPostRegistrationDatasetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * The base class for processing plugins that employ a {@link IPostRegistrationDatasetHandler}.
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractDropboxProcessingPlugin extends AbstractDatastorePlugin implements
        IProcessingPluginTask
{
    private static final long serialVersionUID = 1L;

    final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AbstractDropboxProcessingPlugin.class);

    private final static String MISSING_DIRECTORY_MSG = "with missing directory";

    private final static String EMPTY_DIRECTORY_MSG = "with empty directory";

    private final static String MORE_THAN_ONE_ITEM_MSG = "with more than one item in the directory";

    private final IPostRegistrationDatasetHandler dropboxHandler;

    /**
     * Note that this class is not a valid processing plugin as it does not provide the appropriate
     * constructor.
     */
    public AbstractDropboxProcessingPlugin(Properties properties, File storeRoot,
            IPostRegistrationDatasetHandler dropboxHandler)
    {
        super(properties, storeRoot);
        this.dropboxHandler = dropboxHandler;
    }

    public ProcessingStatus process(List<DatasetDescription> datasets,
            DataSetProcessingContext context)
    {
        final ProcessingStatus result = new ProcessingStatus();
        for (DatasetDescription dataset : datasets)
        {
            Status status = processDataset(dataset, context.getParameterBindings());
            result.addDatasetStatus(dataset, status);
        }
        return result;
    }

    private Status processDataset(DatasetDescription dataset, Map<String, String> parameterBindings)
    {
        File originalDir = getDataSubDir(dataset);
        if (originalDir.isDirectory() == false)
        {
            operationLog
                    .warn("Dataset directory does not exist and will be silently excluded from the processing: "
                            + originalDir.getPath());
            return Status.createError(MISSING_DIRECTORY_MSG);
        }
        File[] datasetFiles = FileUtilities.listFiles(originalDir);
        if (datasetFiles.length == 1)
        {
            DataSetInformation datasetInfo = createDatasetInfo(dataset);
            return dropboxHandler.handle(datasetFiles[0], datasetInfo, parameterBindings);
        } else
        {
            operationLog.error(String.format("Exactly one item was expected in the '%s' directory,"
                    + " but %d have been found. Nothing will be processed.", originalDir
                    .getParent(), datasetFiles));
            final String errorMsg =
                    datasetFiles.length > 1 ? MORE_THAN_ONE_ITEM_MSG : EMPTY_DIRECTORY_MSG;
            return Status.createError(errorMsg);
        }
    }

    private DataSetInformation createDatasetInfo(DatasetDescription dataset)
    {
        DataSetInformation datasetInfo = new DataSetInformation();
        String datasetTypeCode = dataset.getDatasetTypeCode();
        datasetInfo.setDataSetType(new DataSetType(datasetTypeCode));
        datasetInfo.setSampleCode(dataset.getSampleCode());
        datasetInfo.setSpaceCode(dataset.getGroupCode());
        datasetInfo.setDataSetCode(dataset.getDatasetCode());
        ExperimentIdentifier expIdent =
                new ExperimentIdentifier(null, dataset.getGroupCode(), dataset.getProjectCode(),
                        dataset.getExperimentCode());
        datasetInfo.setExperimentIdentifier(expIdent);
        return datasetInfo;
    }

}
