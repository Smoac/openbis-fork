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
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractDatastorePlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.SimpleTableModelBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DateTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ImageTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Reporting plugin which can be used for demonstration purposes. Shows size of the datasets.
 * 
 * @author Tomasz Pylak
 */
public class DemoReportingPlugin extends AbstractDatastorePlugin implements IReportingPluginTask
{
    private static final long serialVersionUID = 1L;

    public DemoReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }

    public TableModel createReport(List<DatasetDescription> datasets)
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
        builder.addHeader("Dataset Code");
        builder.addHeader("Thumbnail");
        builder.addHeader("Name");
        builder.addHeader("Last Modified");
        builder.addHeader("Size", true);
        for (DatasetDescription dataset : datasets)
        {
            File file = getDataSubDir(dataset);
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

    private static void describe(SimpleTableModelBuilder builder, DatasetDescription dataset, File file)
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

    private void describeUnknown(SimpleTableModelBuilder builder, DatasetDescription dataset, File file)
    {
        String datasetCode = dataset.getDatasetCode();
        ISerializableComparable image = createImageCell(dataset, file);
        List<ISerializableComparable> row =
                Arrays.<ISerializableComparable> asList(new StringTableCell(datasetCode), image,
                        new StringTableCell(file.getName()),
                        new DateTableCell(new Date(file.lastModified())),
                        new DoubleTableCell(0));
        builder.addRow(row);
    }

    private static ISerializableComparable createImageCell(DatasetDescription dataset, File file)
    {
        if (ImageUtil.isImageFile(file))
        {
            String code = dataset.getDatasetCode();
            String location = dataset.getDataSetLocation();
            return new ImageTableCell(code, location, file.getPath(), 100, 60);
        }
        return new StringTableCell(file.getName());
    }

    private static void describeFile(SimpleTableModelBuilder builder, DatasetDescription dataset,
            File file)
    {
        ISerializableComparable image = createImageCell(dataset, file);
        List<ISerializableComparable> row =
                Arrays.<ISerializableComparable> asList(new StringTableCell(dataset
                        .getDatasetCode()), image, new StringTableCell(file.getName()),
                        new DateTableCell(new Date(file.lastModified())),
                        new DoubleTableCell(getSize(file)));
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
