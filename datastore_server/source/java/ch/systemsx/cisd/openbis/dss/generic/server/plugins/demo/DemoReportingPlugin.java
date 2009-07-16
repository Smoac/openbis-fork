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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.TableModelBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel.TableModelColumnType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Reporting plugin which can be used for demonstration purposes.
 * 
 * @author Tomasz Pylak
 */
public class DemoReportingPlugin extends AbstractDatastorePlugin implements IReportingPluginTask
{
    public DemoReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }

    public TableModel createReport(List<DatasetDescription> datasets)
    {
        TableModelBuilder builder = new TableModelBuilder();
        builder.addHeader("Dataset code", TableModelColumnType.TEXT);
        builder.addHeader("Name", TableModelColumnType.TEXT);
        builder.addHeader("Size", TableModelColumnType.INTEGER);
        for (DatasetDescription dataset : datasets)
        {
            File file = getOriginalDir(dataset);
            if (file.isDirectory())
            {
                describe(builder, dataset, file);
            } else
            {
                describeUnknown(builder, dataset, file);
            }
        }
        return builder.getTableModel();
    }

    private static void describe(TableModelBuilder builder, DatasetDescription dataset, File file)
    {
        if (file.isFile())
        {
            describeFile(builder, dataset, file);
        } else
        {
            File[] datasetFiles = FileUtilities.listFiles(file);
            for (File datasetFile : datasetFiles)
            {
                describe(builder, dataset, datasetFile);
            }
        }
    }

    private void describeUnknown(TableModelBuilder builder, DatasetDescription dataset, File file)
    {
        String datasetCode = dataset.getDatasetCode();
        List<String> row = Arrays.asList(datasetCode, file.getName(), "[does not exist]");
        builder.addRow(row);
    }

    private static void describeFile(TableModelBuilder builder, DatasetDescription dataset,
            File file)
    {
        List<String> row =
                Arrays.asList(dataset.getDatasetCode(), file.getName(), "" + getSize(file));
        builder.addRow(row);
    }

    private static long getSize(File file)
    {
        if (file.isFile())
        {
            return file.length();
        } else
        {
            return FileUtils.sizeOfDirectory(file);
        }
    }
}
