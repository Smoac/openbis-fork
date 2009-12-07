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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.etlserver.IMaintenanceTask;
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

    private static final String DATABASE_READ_WRITE_GROUP = "database.read-write-group";

    private static final String DATABASE_READ_ONLY_GROUP = "database.read-only-group";

    private static final String DATABASE_SCRIPT_FOLDER_KEY = "database.script-folder";

    private static final String DATABASE_KIND = "database.kind";

    private static final String BASIC_DATABASE_NAME_KEY = "database.basic-database-name";

    private static final String DATABASE_ENGINE_KEY = "database.engine";

    private static final String DEFAULT_DATABASE_ENGINE = "postgresql";
    
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

    private DatabaseConfigurationContext context;

    private Connection connection;

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
        context = createDatabaseConfigurationContext(properties);
        init(context);
        checkDatabseConnection();
        operationLog.info("Plugin initialized: " + pluginName);
    }

    /**
     * Creates a database configuration context from the specified properties.
     */
    protected DatabaseConfigurationContext createDatabaseConfigurationContext(Properties properties)
    {
        DatabaseConfigurationContext configurationContext = new DatabaseConfigurationContext();
        configurationContext.setDatabaseEngineCode(properties.getProperty(DATABASE_ENGINE_KEY,
                DEFAULT_DATABASE_ENGINE));
        configurationContext.setBasicDatabaseName(PropertyUtils.getMandatoryProperty(properties,
                BASIC_DATABASE_NAME_KEY));
        configurationContext.setDatabaseKind(PropertyUtils.getMandatoryProperty(properties,
                DATABASE_KIND));
        String scriptFolder = properties.getProperty(DATABASE_SCRIPT_FOLDER_KEY);
        if (scriptFolder != null)
        {
            configurationContext.setScriptFolder(scriptFolder + "/sql");
        }
        String readOnlyGroup = properties.getProperty(DATABASE_READ_ONLY_GROUP);
        if (readOnlyGroup != null)
        {
            configurationContext.setReadOnlyGroup(readOnlyGroup);
        }
        String readWriteGroup = properties.getProperty(DATABASE_READ_WRITE_GROUP);
        if (readWriteGroup != null)
        {
            configurationContext.setReadWriteGroup(readWriteGroup);
        }
        return configurationContext;
    }

    /**
     * Initializes the data base if necessary. This method should be overridden if needed because
     * this implementation does nothing.
     */
    protected void init(DatabaseConfigurationContext databaseConfigurationContext)
    {
        
    }

    private void checkDatabseConnection()
    {
        try
        {
            connection = context.getDataSource().getConnection();
            tryGetPreviousLastSeenEventId();
            connection.close();
        } catch (SQLException ex)
        {
            throw new ConfigurationFailureException("Initialization failed", ex);
        }
    }

    public void execute()
    {
        operationLog.info("Synchronizing data set information");
        try
        {
            connection = context.getDataSource().getConnection();
            Long lastSeenEventId = tryGetPreviousLastSeenEventId();
            List<DeletedDataSet> deletedDataSets =
                    openBISService.listDeletedDataSets(lastSeenEventId);
            if (deletedDataSets.size() > 0)
            {
                boolean autoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);
                long t0 = System.currentTimeMillis();
                deleteDatasets(deletedDataSets);
                updateSynchronizationDate(lastSeenEventId, deletedDataSets);
                connection.commit();
                operationLog.info("Synchronization task took "
                        + ((System.currentTimeMillis() - t0 + 500) / 100) + " seconds.");
                connection.setAutoCommit(autoCommit);
            }
            connection.close();
        } catch (SQLException ex)
        {
            operationLog.error(ex);
        }
    }

    private void deleteDatasets(List<DeletedDataSet> deletedDataSets) throws SQLException
    {
        operationLog.info(String.format(
                "Synchronizing deletions of %d datasets with the database.",
                deletedDataSets.size()));
        connection.createStatement().execute(
                String.format("DELETE FROM " + dataSetTableName + " WHERE " + permIDColumn + " IN (%s)",
                        joinIds(deletedDataSets)));
    }

    private void updateSynchronizationDate(Long lastSeenEventIdOrNull, List<DeletedDataSet> deleted)
            throws SQLException
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
            executeSql("delete from " + synchronizationTable);
            executeSql("INSERT INTO " + synchronizationTable + " (" + lastSeenEventID
                    + ") VALUES('" + maxEventId + "')");
        }
    }

    private void executeSql(String sql) throws SQLException
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

    private long tryGetPreviousLastSeenEventId() throws SQLException
    {
        Long maxLastSeenEventId = null;
        ResultSet result =
                connection.createStatement().executeQuery(
                        "SELECT MAX(" + lastSeenEventID + ") AS " + lastSeenEventID
                                + " FROM " + synchronizationTable);
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
