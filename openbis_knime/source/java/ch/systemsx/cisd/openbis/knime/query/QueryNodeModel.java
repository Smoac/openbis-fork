/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.query;

import java.io.File;
import java.io.IOException;
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
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.FacadeFactory;
import ch.systemsx.cisd.openbis.plugin.query.client.api.v1.IQueryApiFacade;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableColumn;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class QueryNodeModel extends NodeModel
{
    static final String URL_KEY = "url";
    static final String USER_KEY = "user";
    static final String PASSWORD_KEY = "password";
    static final String QUERY_DESCRIPTION_KEY = "query-description";
    
    private String url;
    private String userID;
    private String password;
    private QueryDescription queryDescription;
    private ParameterBindings parameterBindings = new ParameterBindings();

    protected QueryNodeModel()
    {
        super(0, 1);
    }
    
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException
    {
        return new DataTableSpec[1];
    }

    @Override
    protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException
    {
    }
    
    @Override
    protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException
    {
        url = settings.getString(URL_KEY);
        userID = settings.getString(USER_KEY);
        password = Util.getDecryptedPassword(settings);
        queryDescription = Util.deserializeQueryDescription(settings.getByteArray(QUERY_DESCRIPTION_KEY));
        parameterBindings.loadValidatedSettingsFrom(settings);
    }

    @Override
    protected void saveSettingsTo(NodeSettingsWO settings)
    {
        settings.addString(URL_KEY, url);
        settings.addString(USER_KEY, userID);
        settings.addString(PASSWORD_KEY, Util.getEncryptedPassword(password.toCharArray()));
        settings.addByteArray(QUERY_DESCRIPTION_KEY, Util
                .serializeQueryDescription(queryDescription));
        parameterBindings.saveSettingsTo(settings);
    }
    
    @Override
    protected void reset()
    {
    }

    @Override
    protected void saveInternals(File arg0, ExecutionMonitor arg1) throws IOException,
            CanceledExecutionException
    {
    }

    @Override
    protected void loadInternals(File arg0, ExecutionMonitor arg1) throws IOException,
    CanceledExecutionException
    {
    }
    
    @Override
    protected BufferedDataTable[] execute(BufferedDataTable[] inData, ExecutionContext exec)
            throws Exception
    {
        IQueryApiFacade facade = createFacade(url, userID, password);
        QueryTableModel result = facade.executeQuery(queryDescription.getId(), parameterBindings.getBindings());
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
    }
    
    protected IQueryApiFacade createFacade(String serverURL, String user, String psswrd)
    {
       return FacadeFactory.create(serverURL, user, psswrd); 
    }

}
