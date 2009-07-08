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
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileSystemUtils;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
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
        System.out.println("Reporting from the following datasets has been requested: " + datasets);
        StringBuffer sb = new StringBuffer();
        sb
                .append("<table border=1><tr><td>dataset_code</td><td>name</td><td>kind</td><td>size</td></tr>");
        for (DatasetDescription dataset : datasets)
        {
            File file = getOriginalDir(dataset);
            if (file.isDirectory())
            {
                describe(sb, dataset, file);
            } else
            {
                describeUnknown(sb, dataset, file);
            }
        }
        sb.append("</table>");
        return new TableModel(sb.toString());
    }

    private void describeUnknown(StringBuffer sb, DatasetDescription dataset, File file)
    {
        sb.append("<tr>");
        appendCell(sb, dataset.getDatasetCode());
        appendCell(sb, file.getName());
        appendCell(sb, "unknown");
        appendCell(sb, "[does not exist]");
        sb.append("</tr>");
    }

    private static void describe(StringBuffer sb, DatasetDescription dataset, File file)
    {
        sb.append("<tr>");
        appendCell(sb, dataset.getDatasetCode());
        appendCell(sb, file.getName());
        if (file.isFile())
        {
            appendCell(sb, "file");
            describeFile(sb, file);
        } else
        {
            appendCell(sb, "dir");
            File[] datasetFiles = FileUtilities.listFiles(file);
            for (File datasetFile : datasetFiles)
            {
                describe(sb, dataset, datasetFile);
            }
        }
        sb.append("</tr>");
    }

    private static void describeFile(StringBuffer sb, File file)
    {
        String size;
        try
        {
            size = "" + FileSystemUtils.freeSpaceKb(file.getCanonicalPath());
        } catch (IOException ex)
        {
            size = "unknown";
        }
        appendCell(sb, size);
    }

    private static void appendCell(StringBuffer sb, String text)
    {
        sb.append("<td>");
        sb.append(text);
        sb.append("</td>");
    }

}
