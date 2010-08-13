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
package ch.systemsx.cisd.etlserver.plugins;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;

/**
 * Maintenance task deleting from a custom-specific database data sets which have been deleted from
 * openbis.
 * 
 * @author Izabela Adamczyk
 */
public class DataSetDeletionMaintenanceTask implements IMaintenanceTask
{

    private static final String DEFAULT_DATA_SET_PERM_ID = "PERM_ID";

    private static final String DATA_SET_PERM_ID_KEY = "data-set-perm-id";

    private static final String DATA_SET_TABLE_NAME_KEY = "data-set-table-name";

    private static final String DEFAULT_DATA_SET_TABLE_NAME = "data_sets";

    private static final String LAST_SEEN_EVENT_ID_COLUMN_KEY = "last-seen-event-id-column";

    private static final String SYNCHRONIZATION_TABLE_KEY = "synchronization-table";

    private static final String DEFAULT_SYNCHRONIZATION_TABLE = "EVENTS";

    private static final String DEFAULT_LAST_SEEN_EVENT_ID = "LAST_SEEN_DELETION_EVENT_ID";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DataSetDeletionMaintenanceTask.class);

    private final IEncapsulatedOpenBISService openBISService;

    private DataSource dataSource;

    private String synchronizationTable;

    private String lastSeenEventID;

    private String dataSetTableName;

    private String permIDColumn;

    public DataSetDeletionMaintenanceTask()
    {
        LogInitializer.init();
        openBISService = ServiceProvider.getOpenBISService();
    }

    public void setUp(String pluginName, Properties properties)
    {
        synchronizationTable =
                properties.getProperty(SYNCHRONIZATION_TABLE_KEY, DEFAULT_SYNCHRONIZATION_TABLE);
        lastSeenEventID =
                properties.getProperty(LAST_SEEN_EVENT_ID_COLUMN_KEY, DEFAULT_LAST_SEEN_EVENT_ID);
        dataSetTableName =
                properties.getProperty(DATA_SET_TABLE_NAME_KEY, DEFAULT_DATA_SET_TABLE_NAME);
        permIDColumn = properties.getProperty(DATA_SET_PERM_ID_KEY, DEFAULT_DATA_SET_PERM_ID);
        this.dataSource = ServiceProvider.getDataSourceProvider().getDataSource(properties);
        checkDatabseConnection();
        operationLog.info("Plugin initialized: " + pluginName);
    }

    private void checkDatabseConnection()
    {
        Connection connection = null;
        try
        {
            connection = createConnection();
            tryGetPreviousLastSeenEventId(connection);
        } catch (SQLException ex)
        {
            throw new ConfigurationFailureException("Initialization failed", ex);
        } finally
        {
            closeConnection(connection);
        }
    }

    public void execute()
    {
        operationLog.info("Synchronizing data set information");
        Connection connection = null;
        try
        {
            connection = createConnection();
            Long lastSeenEventId = tryGetPreviousLastSeenEventId(connection);
            List<DeletedDataSet> deletedDataSets =
                    openBISService.listDeletedDataSets(lastSeenEventId);
            if (deletedDataSets.size() > 0)
            {
                boolean autoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);
                long t0 = System.currentTimeMillis();
                deleteDatasets(deletedDataSets, connection);
                updateSynchronizationDate(lastSeenEventId, deletedDataSets, connection);
                connection.commit();
                operationLog.info("Synchronization task took "
                        + ((System.currentTimeMillis() - t0 + 500) / 1000) + " seconds.");
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException ex)
        {
            operationLog.error(ex);
        } finally
        {
            closeConnection(connection);
        }
    }

    private void closeConnection(Connection connectionOrNull)
    {
        if (connectionOrNull != null)
        {
            try
            {
                connectionOrNull.close();
            } catch (SQLException ex)
            {
                // suppress this exception
                operationLog.error(ex);
            }
        }
    }

    private Connection createConnection() throws SQLException
    {
        return dataSource.getConnection();
    }

    private void deleteDatasets(List<DeletedDataSet> deletedDataSets, Connection connection)
            throws SQLException
    {
        operationLog.info(String
                .format("Synchronizing deletions of %d datasets with the database.",
                        deletedDataSets.size()));
        connection.createStatement().execute(
                String.format("DELETE FROM " + dataSetTableName + " WHERE " + permIDColumn
                        + " IN (%s)", joinIds(deletedDataSets)));
    }

    private void updateSynchronizationDate(Long lastSeenEventIdOrNull,
            List<DeletedDataSet> deleted, Connection connection) throws SQLException
    {
        Long maxEventId = lastSeenEventIdOrNull;
        for (DeletedDataSet dds : deleted)
        {
            long eventId = dds.getEventId();
            if (maxEventId == null || eventId > maxEventId)
            {
                maxEventId = eventId;
            }
        }
        if (lastSeenEventIdOrNull == null || maxEventId > lastSeenEventIdOrNull)
        {
            // we store only the last update time, so all the others can be deleted
            executeSql("delete from " + synchronizationTable, connection);
            executeSql("INSERT INTO " + synchronizationTable + " (" + lastSeenEventID
                    + ") VALUES('" + maxEventId + "')", connection);
        }
    }

    private void executeSql(String sql, Connection connection) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.executeUpdate();
    }

    private String joinIds(List<DeletedDataSet> deleted)
    {
        StringBuilder sb = new StringBuilder();
        for (DeletedDataSet dds : deleted)
        {
            if (sb.length() != 0)
            {
                sb.append(", ");
            }
            sb.append("'" + StringEscapeUtils.escapeSql(dds.getIdentifier()) + "'");
        }
        String ids = sb.toString();
        return ids;
    }

    private long tryGetPreviousLastSeenEventId(Connection connection) throws SQLException
    {
        Long maxLastSeenEventId = null;
        ResultSet result =
                connection.createStatement().executeQuery(
                        "SELECT MAX(" + lastSeenEventID + ") AS " + lastSeenEventID + " FROM "
                                + synchronizationTable);
        while (result.next())
        {
            long newLastSeenEventId = result.getLong(lastSeenEventID);
            if (maxLastSeenEventId == null || maxLastSeenEventId < newLastSeenEventId)
            {
                maxLastSeenEventId = newLastSeenEventId;
            }
        }
        return maxLastSeenEventId;
    }
}
