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

package eu.basysbio.cisd.dss;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractDatastorePlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.SimpleTableModelBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;

/**
 * Shows the header properties of time series data sets.
 * 
 * @author Izabela Adamczyk
 */
public class TimeSeriesPropertiesReportingPlugin extends AbstractDatastorePlugin implements
        IReportingPluginTask
{
    private static final long serialVersionUID = 1L;

    public TimeSeriesPropertiesReportingPlugin(Properties properties, File storeRoot)
    {
        super(properties, storeRoot);
    }

    public TableModel createReport(List<DatasetDescription> datasets)
    {
        List<String> headers = new ArrayList<String>();
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
        addHeader(builder, headers, "CODE");
        for (TimeSeriesPropertyType pt : HeaderUtils.TIME_SERIES_HEADER_PROPERTIES)
        {
            addHeader(builder, headers, pt.name());
        }
        for (DatasetDescription dataset : datasets)
        {
            List<ISerializableComparable> row = new ArrayList<ISerializableComparable>();
            for (int i = 0, n = headers.size(); i < n; i++)
            {
                row.add(new StringTableCell(""));
            }
            addTableCellValue(row, headers, "CODE", dataset.getDatasetCode());
            File file = getDataSubDir(dataset);
            List<NewProperty> properies = HeaderUtils.extractHeaderProperties(file, true);
            for (NewProperty p : properies)
            {
                addTableCellValue(row, headers, p.getPropertyCode(), p.getValue());
            }
            builder.addRow(row);
        }
        return builder.getTableModel();
    }

    private static void addTableCellValue(List<ISerializableComparable> row, List<String> headers,
            String key, String value)
    {
        row.set(headers.indexOf(key), new StringTableCell(value));

    }

    private static void addHeader(SimpleTableModelBuilder builder, List<String> headers,
            String title)
    {
        builder.addHeader(title);
        headers.add(title);
    }

}
