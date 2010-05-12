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

package ch.systemsx.cisd.openbis.plugin.query.server.api.v1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableColumn;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableColumnDataType;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;

/**
 * @author Franz-Josef Elmer
 */
@Component(ResourceNames.QUERY_PLUGIN_SERVER)
public class QueryApiServer extends AbstractServer<IQueryApiServer> implements IQueryApiServer
{
    @Resource(name = ch.systemsx.cisd.openbis.plugin.query.shared.ResourceNames.QUERY_PLUGIN_SERVER)
    private IQueryServer queryServer;

    public IQueryApiServer createLogger(IInvocationLoggerContext context)
    {
        return new QueryApiLogger(sessionManager, context);
    }

    public String tryToAuthenticateAtQueryServer(String userID, String userPassword)
    {
        SessionContextDTO session = tryToAuthenticate(userID, userPassword);
        return session == null ? null : session.getSessionToken();
    }

    public List<QueryDescription> listQueries(String sessionToken)
    {
        List<QueryDescription> result = new ArrayList<QueryDescription>();
        List<QueryExpression> queries = queryServer.listQueries(sessionToken, QueryType.GENERIC);
        for (QueryExpression queryExpression : queries)
        {
            QueryDescription queryDescription = new QueryDescription();
            queryDescription.setId(queryExpression.getId());
            queryDescription.setName(queryExpression.getName());
            queryDescription.setDescription(queryExpression.getDescription());
            queryDescription.setParameters(queryExpression.getParameters());
            result.add(queryDescription);
        }
        return result;
    }

    public QueryTableModel executeQuery(String sessionToken, long queryID,
            Map<String, String> parameterBindings)
    {
        QueryParameterBindings bindings = new QueryParameterBindings();
        for (Entry<String, String> entry : parameterBindings.entrySet())
        {
            bindings.addBinding(entry.getKey(), entry.getValue());
        }
        TableModel result = queryServer.queryDatabase(sessionToken, new TechId(queryID), bindings);
        List<TableModelColumnHeader> headers = result.getHeader();
        ArrayList<QueryTableColumn> translatedHeaders = new ArrayList<QueryTableColumn>();
        for (TableModelColumnHeader header : headers)
        {
            String title = header.getTitle();
            QueryTableColumnDataType dataType = Util.translate(header.getDataType());
            translatedHeaders.add(new QueryTableColumn(title, dataType));
        }
        QueryTableModel tableModel = new QueryTableModel(translatedHeaders);
        List<TableModelRow> rows = result.getRows();
        for (TableModelRow row : rows)
        {
            List<ISerializableComparable> values = row.getValues();
            Serializable[] translatedValues = new Serializable[values.size()];
            for (int i = 0, n = values.size(); i < n; i++)
            {
                ISerializableComparable value = values.get(i);
                Serializable translatedValue = null;
                if (value instanceof IntegerTableCell)
                {
                    translatedValue = ((IntegerTableCell) value).getNumber();
                } else if (value instanceof DoubleTableCell)
                {
                    translatedValue = ((DoubleTableCell) value).getNumber();
                } else if (value instanceof StringTableCell)
                {
                    translatedValue = ((StringTableCell) value).toString();
                }
                translatedValues[i] = translatedValue;
            }
            tableModel.addRow(translatedValues);
        }
        return tableModel;
    }

    public int getMajorVersion()
    {
        return 1;
    }

    public int getMinorVersion()
    {
        return 0;
    }

}
