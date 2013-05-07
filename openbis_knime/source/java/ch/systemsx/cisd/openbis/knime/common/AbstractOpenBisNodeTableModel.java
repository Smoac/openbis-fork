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

package ch.systemsx.cisd.openbis.knime.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;

import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.FacadeFactory;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableColumn;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

/**
 * Abstract super class of node models based on {@link QueryTableModel} objects returned
 * by {@link IQueryApiFacade}.
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractOpenBisNodeTableModel extends AbstractOpenBisNodeModel
{
    protected abstract QueryTableModel getData(IQueryApiFacade facade);
    
    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec)
            throws Exception
    {
        IQueryApiFacade facade = FacadeFactory.create(url, userID, password);
        try
        {
            QueryTableModel result = getData(facade);
            List<QueryTableColumn> columns = result.getColumns();
            String[] columnTitles = new String[columns.size()];
            DataType[] dataTypes = new DataType[columns.size()];
            ColumnType[] columnTypes = new ColumnType[columns.size()];
            Map<String, Integer> columnNames = new HashMap<String, Integer>();
            for (int i = 0, n = columns.size(); i < n; i++)
            {
                QueryTableColumn column = columns.get(i);
                String title = column.getTitle();
                Integer count = columnNames.get(title);
                if (count == null)
                {
                    count = 0;
                }
                count++;
                columnNames.put(title, count);
                columnTitles[i] = count == 1 ? title : title + "[" + count + "]";
                columnTypes[i] = Util.getColumnType(column.getDataType());
                dataTypes[i] = columnTypes[i].getDataType();
            }
            DataTableSpec dataTableSpec = new DataTableSpec(columnTitles, dataTypes);
            BufferedDataContainer container = exec.createDataContainer(dataTableSpec);
            List<Serializable[]> rows = result.getRows();
            for (int i = 0, n = rows.size(); i < n; i++)
            {
                Serializable[] row = rows.get(i);
                DataCell[] cells = new DataCell[row.length];
                for (int c = 0; c < row.length; c++)
                {
                    cells[c] = columnTypes[c].createCell(row[c]);
                }
                container.addRowToTable(new DefaultRow(Integer.toString(i), cells));
            }
            container.close();
            return new BufferedDataTable[] {container.getTable()};
        } finally
        {
            facade.logout();
        }
    }

    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException
    {
        return new DataTableSpec[1];
    }
}
