/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.dbmigration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;

/**
 * Generic merger of two databases
 *
 * @author Franz-Josef Elmer
 */
public class DatabaseMerger
{
    private static final class TableConnection
    {
        private final TableDefinition dependentTable;

        private final TableColumnDefinition dependentColumn;

        public TableConnection(TableDefinition dependentTable, TableColumnDefinition dependentColumn)
        {
            this.dependentTable = dependentTable;
            this.dependentColumn = dependentColumn;
        }

        public final TableDefinition getDependentTable()
        {
            return dependentTable;
        }

        public final TableColumnDefinition getDependentColumn()
        {
            return dependentColumn;
        }

        @Override
        public String toString()
        {
            return dependentTable.getTableName() + "." + dependentColumn.getColumnName();
        }

    }

    private static final class TableColumnDefinition implements Iterable<TableConnection>
    {
        private boolean primaryKey;
        
        private long largestPrimaryKey = -1;

        private boolean foreignKey;

        private String columnName;

        private String dataTypeName;

        private List<TableConnection> connections = new ArrayList<TableConnection>();

        public void addConnection(TableDefinition fkTableDefinition,
                TableColumnDefinition fkColumnDefinition)
        {
            connections.add(new TableConnection(fkTableDefinition, fkColumnDefinition));
        }

        public Iterator<TableConnection> iterator()
        {
            return connections.iterator();
        }

        public final long getLargestPrimaryKey()
        {
            return largestPrimaryKey;
        }

        public final void setLargestPrimaryKey(long nextFreePrimaryKey)
        {
            this.largestPrimaryKey = nextFreePrimaryKey;
        }

        public final boolean isForeignKey()
        {
            return foreignKey;
        }

        public final void setForeignKey(boolean foreignKey)
        {
            this.foreignKey = foreignKey;
        }

        public final boolean isPrimaryKey()
        {
            return primaryKey;
        }

        public final void setPrimaryKey(boolean primaryKey)
        {
            this.primaryKey = primaryKey;
        }

        public final String getColumnName()
        {
            return columnName;
        }

        public final void setColumnName(String columnName)
        {
            this.columnName = columnName;
        }

        public final String getDataTypeName()
        {
            return dataTypeName;
        }

        public final void setDataTypeName(String dataType)
        {
            this.dataTypeName = dataType;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj instanceof TableColumnDefinition == false)
            {
                return false;
            }
            TableColumnDefinition columnDefinition = (TableColumnDefinition) obj;
            return columnDefinition.columnName.equals(columnName);
        }

        @Override
        public int hashCode()
        {
            return columnName.hashCode();
        }

        @Override
        public String toString()
        {
            return (primaryKey ? "*" : " ") + columnName + " " + dataTypeName
                    + (foreignKey ? " (fk) " : " ")
                    + (largestPrimaryKey < 0 ? "" : Long.toString(largestPrimaryKey) + " ")
                    + (connections.isEmpty() ? "" : connections.toString());
        }
    }

    private static final class TableDefinition implements Iterable<TableColumnDefinition>,
            Comparable<TableDefinition>
    {
        private final String tableName;

        private final Map<String, TableColumnDefinition> columns =
                new TreeMap<String, TableColumnDefinition>();

        TableDefinition(String tableName)
        {
            this.tableName = tableName;
        }

        public final String getTableName()
        {
            return tableName;
        }

        public void add(TableColumnDefinition columnDefinition)
        {
            columns.put(columnDefinition.getColumnName(), columnDefinition);
        }

        public void defineColumnAsPrimaryKey(String columnName)
        {
            getColumnDefinition(columnName).setPrimaryKey(true);
        }

        public TableColumnDefinition getColumnDefinition(String columnName)
        {
            TableColumnDefinition columnDefinition = columns.get(columnName);
            if (columnDefinition == null)
            {
                throw new IllegalArgumentException("No column '" + columnName
                        + "' defined^in table '" + tableName + "'.");
            }
            return columnDefinition;
        }

        public Iterator<TableColumnDefinition> iterator()
        {
            return columns.values().iterator();
        }

        public int compareTo(TableDefinition tableDefinition)
        {
            return tableName.compareTo(tableDefinition.getTableName());
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append(tableName).append(":");
            for (TableColumnDefinition columnDefinition : columns.values())
            {
                builder.append("\n  ").append(columnDefinition);
            }
            return builder.toString();
        }

    }

    private static final class DatabaseDefinition implements Iterable<TableDefinition>
    {
        private final Map<String, TableDefinition> tableDefinitions =
                new TreeMap<String, TableDefinition>();

        public void add(TableDefinition tableDefinition)
        {
            tableDefinitions.put(tableDefinition.getTableName(), tableDefinition);
        }

        public TableDefinition getTableDefinition(String tableName)
        {
            TableDefinition tableDefinition = tableDefinitions.get(tableName);
            if (tableDefinition == null)
            {
                throw new IllegalArgumentException("Unknown table '" + tableName + "'.");
            }
            return tableDefinition;
        }

        public void connect(String pkTableName, String pkColumnName, String fkTableName,
                String fkColumnName)
        {
            TableDefinition pkTableDefinition = getTableDefinition(pkTableName);
            TableColumnDefinition pkColumnDefinition =
                    pkTableDefinition.getColumnDefinition(pkColumnName);
            TableDefinition fkTableDefinition = getTableDefinition(fkTableName);
            TableColumnDefinition fkColumnDefinition =
                    fkTableDefinition.getColumnDefinition(fkColumnName);
            fkColumnDefinition.setForeignKey(true);
            pkColumnDefinition.addConnection(fkTableDefinition, fkColumnDefinition);
        }

        public Iterator<TableDefinition> iterator()
        {
            return tableDefinitions.values().iterator();
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            for (TableDefinition tableDefinition : tableDefinitions.values())
            {
                builder.append(tableDefinition).append('\n');
            }
            return builder.toString();
        }

    }

    private DatabaseDefinition databaseDefinition;

    /**
     * Creates an instance for the specified data source. Gathers all necessary metadata. 
     */
    public DatabaseMerger(DataSource dataSource)
    {
        databaseDefinition = new DatabaseDefinition();
        try
        {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet rs = metaData.getTables(null, null, null, new String[]
                { "TABLE" });
            while (rs.next())
            {
                databaseDefinition.add(new TableDefinition(rs.getString("TABLE_NAME")));
            }
            for (TableDefinition tableDefinition : databaseDefinition)
            {
                String tableName = tableDefinition.getTableName();
                rs = metaData.getColumns(null, "%", tableName, "%");
                while (rs.next())
                {
                    TableColumnDefinition columnDefinition = new TableColumnDefinition();
                    columnDefinition.setColumnName(rs.getString("COLUMN_NAME"));
                    columnDefinition.setDataTypeName(rs.getString("TYPE_NAME"));
                    tableDefinition.add(columnDefinition);
                }
            }
            for (TableDefinition tableDefinition : databaseDefinition)
            {
                String tableName = tableDefinition.getTableName();
                rs = metaData.getPrimaryKeys(null, null, tableName);
                while (rs.next())
                {
                    tableDefinition.defineColumnAsPrimaryKey(rs.getString("COLUMN_NAME"));
                }
                rs = metaData.getExportedKeys(null, null, tableName);
                while (rs.next())
                {
                    String pkTableName = rs.getString("PKTABLE_NAME");
                    String pkColumnName = rs.getString("PKCOLUMN_NAME");
                    String fkTableName = rs.getString("FKTABLE_NAME");
                    String fkColumnName = rs.getString("FKCOLUMN_NAME");
                    databaseDefinition
                            .connect(pkTableName, pkColumnName, fkTableName, fkColumnName);
                }
            }
            for (TableDefinition tableDefinition : databaseDefinition)
            {
                for (TableColumnDefinition columnDefinition : tableDefinition)
                {
                    if (columnDefinition.isPrimaryKey())
                    {
                        String tableName = tableDefinition.getTableName();
                        String columnName = columnDefinition.getColumnName();
                        String sql = "select max(" + columnName + ") from " + tableName;
                        rs = statement.executeQuery(sql);
                        rs.next();
                        columnDefinition.setLargestPrimaryKey(rs.getLong(1));
                    }
                }
            }
            statement.close();
            connection.close();
        } catch (SQLException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    /**
     * Returns database definition necessary for merging.
     */
    public DatabaseDefinition getDatabaseDefinition()
    {
        return databaseDefinition;
    }

    /**
     * Returns the definitions of all tables depending directly or indirectly on the specified
     * tables.
     */
    public Set<TableDefinition> getTablesDependingOn(String... tableNames)
    {
        Set<TableDefinition> collection = new TreeSet<TableDefinition>();
        Set<String> visitedTableDefinitions = new HashSet<String>();
        for (String tableName : tableNames)
        {
            collectTablesDependingOn(databaseDefinition.getTableDefinition(tableName), collection,
                    visitedTableDefinitions);
        }

        return collection;
    }

    private void collectTablesDependingOn(TableDefinition tableDefinition,
            Set<TableDefinition> collection, Set<String> visitedTableDefinitions)
    {
        if (visitedTableDefinitions.contains(tableDefinition.getTableName()))
        {
            return;
        }
        visitedTableDefinitions.add(tableDefinition.getTableName());
        for (TableColumnDefinition tableColumnDefinition : tableDefinition)
        {
            for (TableConnection tableConnection : tableColumnDefinition)
            {
                TableDefinition dependentTable = tableConnection.getDependentTable();
                collection.add(dependentTable);
                collectTablesDependingOn(dependentTable, collection, visitedTableDefinitions);
            }
        }
    }

    public static void main(String[] args)
    {
        String database = "lims_dev";
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost/" + database);
        dataSource.setUsername("postgres");
        dataSource.setPassword("");
        dataSource.setMinIdle(0);
        dataSource.setMaxIdle(0);

        DatabaseMerger merger = new DatabaseMerger(dataSource);
        DatabaseDefinition databaseDefinition = merger.getDatabaseDefinition();
        System.out.println(databaseDefinition);
        
        System.out.print("Tables which do not depend on table 'database_instances': ");
        Set<TableDefinition> tables = merger.getTablesDependingOn("database_instances");
        for (TableDefinition tableDefinition : databaseDefinition)
        {
            if (tables.contains(tableDefinition) == false)
            {
                System.out.print(tableDefinition.getTableName() + " ");
            }
        }
        System.out.println();
    }
}
