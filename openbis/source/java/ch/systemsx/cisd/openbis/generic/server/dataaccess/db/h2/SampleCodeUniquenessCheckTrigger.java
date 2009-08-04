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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.h2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.h2.api.Trigger;

import ch.systemsx.cisd.common.db.SQLStateUtils;

/**
 * A trigger to check the uniqueness of the sample code in the SAMPLES table.
 * 
 * @author Bernd Rinn
 */
public class SampleCodeUniquenessCheckTrigger implements Trigger
{
    private static final String GROU_ID = "GROU_ID";

    private static final String ID = "ID";

    private static final String CODE = "CODE";

    private static final String DBIN_ID = "DBIN_ID";

    private static final String SAMP_ID_PART_OF = "SAMP_ID_PART_OF";

    private final Map<String, Integer> nameToIdxMap = new HashMap<String, Integer>();

    private Class<?>[] columnClasses;

    public void init(Connection conn, String schemaName, String triggerName, String tableName,
            boolean before, int type) throws SQLException
    {
        final ResultSet columns = conn.getMetaData().getColumns(null, schemaName, tableName, null);
        final ArrayList<String> columnNames = new ArrayList<String>();
        final ArrayList<Class<?>> columnClassesList = new ArrayList<Class<?>>();
        while (columns.next())
        {
            columnNames.add(columns.getString("COLUMN_NAME"));
            int dbType = columns.getInt("DATA_TYPE");
            switch (dbType)
            {
                case Types.INTEGER:
                    columnClassesList.add(Integer.class);
                    break;
                case Types.BIGINT:
                    columnClassesList.add(Long.class);
                    break;
                case Types.FLOAT:
                    columnClassesList.add(Float.class);
                    break;
                case Types.DOUBLE:
                case Types.REAL:
                    columnClassesList.add(Double.class);
                    break;
                case Types.DATE:
                case Types.TIME:
                case Types.TIMESTAMP:
                    columnClassesList.add(java.sql.Date.class);
                    break;
                case Types.VARCHAR:
                case Types.LONGVARCHAR:
                    columnClassesList.add(String.class);
                    break;
                default:
                    throw new SQLException("Unexpected Types constant");
            }
        }
        columnClasses = new Class[columnClassesList.size()];
        columnClassesList.toArray(columnClasses);
        int idx = 0;
        for (String columnName : columnNames)
        {
            nameToIdxMap.put(columnName, idx++);
        }
        if (columnClasses[nameToIdxMap.get(SAMP_ID_PART_OF)] != Long.class)
        {
            throw new SQLException("Unexpected column type");
        }
    }

    private Long getLong(Object[] row, String colName)
    {
        return (Long) row[nameToIdxMap.get(colName)];
    }

    private String getString(Object[] row, String colName)
    {
        return (String) row[nameToIdxMap.get(colName)];
    }

    private int getQueryResult(Connection conn, String query) throws SQLException
    {
        final ResultSet rs = conn.createStatement().executeQuery(query);
        rs.next();
        final int result = rs.getInt(1);
        rs.close();
        return result;
    }

    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException
    {
        final long id = getLong(newRow, ID);
        final String code = getString(newRow, CODE);
        final Long dbinId = getLong(newRow, DBIN_ID);
        final Long groupId = getLong(newRow, GROU_ID);
        final Long sampIdPartOf = getLong(newRow, SAMP_ID_PART_OF);
        if (sampIdPartOf == null)
        {
            if (dbinId != null)
            {
                final int count =
                        getQueryResult(conn, String.format("SELECT count(*) FROM SAMPLES "
                                + "WHERE id != %d and code = '%s' and "
                                + "samp_id_part_of is NULL and dbin_id = %d", id, code, dbinId));
                if (count > 0)
                {
                    throw new SQLException(
                            "Insert/Update of Sample (Code: "
                                    + code
                                    + ") failed because database instance sample with the same code already exists.",
                            SQLStateUtils.UNIQUE_VIOLATION);
                }
            } else
            {
                final int count =
                        getQueryResult(conn, String
                                .format("SELECT count(*) FROM SAMPLES "
                                        + "WHERE id != %d and code = '%s' and "
                                        + "samp_id_part_of is NULL and grou_id = %d", id, code,
                                        groupId));
                if (count > 0)
                {
                    throw new SQLException(
                            "Insert/Update of Sample (Code: "
                                    + code
                                    + ") failed because group sample with the same code already exists.",
                            SQLStateUtils.UNIQUE_VIOLATION);
                }
            }
        } else
        {
            if (dbinId != null)
            {
                final int count =
                        getQueryResult(conn, String.format("SELECT count(*) FROM SAMPLES "
                                + "where id != %d and code = %s and "
                                + "samp_id_part_of = %d and dbin_id = %d", id, code,
                                sampIdPartOf, dbinId));
                if (count > 0)
                {
                    throw new SQLException("Insert/Update of Sample (Code: " + code
                            + ") failed because database instance sample with the same code "
                            + "and being the part of the same parent already exists.",
                            SQLStateUtils.UNIQUE_VIOLATION);
                }
            } else
            {
                final int count =
                        getQueryResult(conn, String.format("SELECT count(*) FROM SAMPLES "
                                + "where id != %d and code = %s and "
                                + "samp_id_part_of = %d and grou_id = %d", id, code,
                                sampIdPartOf, groupId));
                if (count > 0)
                {
                    throw new SQLException("Insert/Update of Sample (Code: " + code
                            + ") failed because group sample with the same code "
                            + "and being the part of the same parent already exists.",
                            SQLStateUtils.UNIQUE_VIOLATION);
                }
            }
        }
    }
}
