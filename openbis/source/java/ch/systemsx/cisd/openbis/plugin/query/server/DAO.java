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

package ch.systemsx.cisd.openbis.plugin.query.server;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;
import org.springframework.jdbc.support.JdbcUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;

/**
 * @author Franz-Josef Elmer
 */
class DAO extends SimpleJdbcDaoSupport
{
    private static DataTypeCode getDataTypeCode(int sqlType)
    {
        if (isInteger(sqlType))
        {
            return DataTypeCode.INTEGER;
        }
        if (isReal(sqlType))
        {
            return DataTypeCode.REAL;
        }
        return DataTypeCode.VARCHAR;
    }

    private static boolean isInteger(int sqlType)
    {
        return Types.BIGINT == sqlType || Types.INTEGER == sqlType || Types.SMALLINT == sqlType
                || Types.TINYINT == sqlType;
    }

    private static boolean isReal(int sqlType)
    {
        return Types.DECIMAL == sqlType || Types.DOUBLE == sqlType || Types.FLOAT == sqlType
                || Types.NUMERIC == sqlType || Types.REAL == sqlType;
    }

    public DAO(DataSource dataSource)
    {
        setDataSource(dataSource);
        afterPropertiesSet();
    }

    public TableModel query(String sqlQuery)
    {
        return query(sqlQuery, new QueryParameterBindings());
    }

    public TableModel query(String sqlQuery, QueryParameterBindings bindingsOrNull)
    {
        if (sqlQuery.toLowerCase().trim().startsWith("select") == false)
        {
            throw new UserFailureException("Sorry, only select statements are allowed.");
        }
        PreparedStatementCallback callback = new PreparedStatementCallback()
            {
                public Object doInPreparedStatement(PreparedStatement ps) throws SQLException,
                        DataAccessException
                {
                    ResultSet resultSet = ps.executeQuery();
                    ResultSetMetaData metaData = ps.getMetaData();
                    List<TableModelColumnHeader> headers = new ArrayList<TableModelColumnHeader>();
                    int columnCount = metaData.getColumnCount();
                    for (int i = 1; i <= columnCount; i++)
                    {
                        String columnName = JdbcUtils.lookupColumnName(metaData, i);
                        TableModelColumnHeader header =
                                new TableModelColumnHeader(columnName, i - 1);
                        header.setDataType(getDataTypeCode(metaData.getColumnType(i)));
                        headers.add(header);
                    }
                    List<TableModelRow> rows = new ArrayList<TableModelRow>();
                    while (resultSet.next())
                    {
                        List<ISerializableComparable> cells =
                                new ArrayList<ISerializableComparable>();
                        for (int i = 1; i <= columnCount; i++)
                        {
                            Object value = JdbcUtils.getResultSetValue(resultSet, i);
                            if (value instanceof Integer || value instanceof Long)
                            {
                                cells.add(new IntegerTableCell(((Number) value).longValue()));
                            } else if (value instanceof Number)
                            {
                                Number number = (Number) value;
                                cells.add(new DoubleTableCell(number.doubleValue()));
                            } else
                            {
                                cells
                                        .add(new StringTableCell(value == null ? "" : value
                                                .toString()));
                            }
                        }
                        rows.add(new TableModelRow(cells));
                    }
                    resultSet.close();
                    return new TableModel(headers, rows);
                }
            };
        return (TableModel) new NamedParameterJdbcTemplate(getJdbcTemplate()).execute(sqlQuery,
                tryExtractBindingsMap(bindingsOrNull), callback);
    }

    @SuppressWarnings("unchecked")
    private Map tryExtractBindingsMap(QueryParameterBindings bindingsOrNull)
    {
        return bindingsOrNull == null ? null : bindingsOrNull.getBindings();
    }
}
