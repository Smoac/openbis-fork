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

package ch.systemsx.cisd.yeastx.etl;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.IDataSetHandler;

/**
 * {@link IDataSetHandler} implementation which for each dataset directory reads all the files
 * inside that directory and runs the primary dataset handler for it.
 * 
 * @author Tomasz Pylak
 */
public class BatchDataSetHandler implements IDataSetHandler
{
    private final IDataSetHandler delegator;

    public BatchDataSetHandler(Properties properties, IDataSetHandler delegator)
    {
        this.delegator = delegator;
    }

    public void handleDataSet(File datasetsParentDir)
    {
        if (datasetsParentDir.isDirectory())
        {
            TableMap<String, DataSetMappingInformation> datasetsMapping =
                    DatasetMappingUtil.tryGetDatasetsMapping(datasetsParentDir);
            if (datasetsMapping == null)
            {
                return;
            }
            Set<String> processedFiles = new HashSet<String>();
            List<File> files = listAll(datasetsParentDir);
            for (File file : files)
            {
                if (isValidDataset(file, datasetsMapping))
                {
                    try
                    {
                        delegator.handleDataSet(file);
                        processedFiles.add(file.getName().toLowerCase());
                    } catch (Throwable ex)
                    {
                        logError(ex, file);
                    }
                }
            }
            cleanMappingFile(datasetsParentDir, processedFiles);
            clean(datasetsParentDir, datasetsMapping.values().size() - processedFiles.size());
        } else
        {
            LogUtils.adminWarn("The path '%s' is not a directory and will not be processed.",
                    datasetsParentDir.getPath());
        }
    }

    private void cleanMappingFile(File datasetsParentDir, Set<String> processedFiles)
    {
        try
        {
            DatasetMappingUtil.cleanMappingFile(datasetsParentDir, processedFiles);
        } catch (IOException ex)
        {
            LogUtils.error(datasetsParentDir, "Cannot clean dataset mappings file: "
                    + ex.getMessage());
        }
    }

    private void logError(Throwable ex, File dataset)
    {
        LogUtils.error(dataset.getParentFile(), "Processing '%s' dataset failed: "
                + ex.getMessage());

    }

    private void clean(File datasetsParentDir, int unprocessedDatasetsCounter)
    {
        if (unprocessedDatasetsCounter == 0 && hasNoDatasetFiles(datasetsParentDir))
        {
            LogUtils.deleteUserLog(datasetsParentDir);
            DatasetMappingUtil.deleteMappingFile(datasetsParentDir);
            deleteEmptyDir(datasetsParentDir);
        }
    }

    private boolean isValidDataset(File file,
            TableMap<String, DataSetMappingInformation> datasetsMapping)
    {
        if (DatasetMappingUtil.isMappingFile(file))
        {
            return false;
        }
        // TODO 2009-05-26, Tomasz Pylak: check that the sample from the mapping exists and is
        // assigned to the experiment - we do not
        // want to move datasets to unidentified directory in this case
        return DatasetMappingUtil.hasMapping(file, datasetsMapping);
    }

    private void deleteEmptyDir(File dir)
    {
        boolean ok = dir.delete();
        if (ok == false)
        {
            LogUtils.adminError(
                    "The directory '%s' cannot be deleted although it seems to be empty.", dir
                            .getPath());
        }
    }

    private boolean hasNoDatasetFiles(File dir)
    {
        List<File> files = listAll(dir);
        int datasetsCounter = files.size();
        for (File file : files)
        {
            if (LogUtils.isUserLog(file) || DatasetMappingUtil.isMappingFile(file))
            {
                datasetsCounter--;
            }
        }
        return datasetsCounter == 0;
    }

    private List<File> listAll(File dataSet)
    {
        return FileUtilities.listFilesAndDirectories(dataSet, false, null);
    }
}
