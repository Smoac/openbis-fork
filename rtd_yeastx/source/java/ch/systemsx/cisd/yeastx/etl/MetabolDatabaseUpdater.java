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
package ch.systemsx.cisd.yeastx.etl;

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
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.etlserver.IMaintenanceTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.yeastx.db.DBUtils;

/**
 * Maintenance task deleting from metabol database data sets which have been deleted from openbis.
 * 
 * @author Izabela Adamczyk
 */
// TODO 2009-12-07, Franz-Josef Elmer: Extend from DataSetDeletionMaintenanceTask and use
// properties to setup database configuration
public class MetabolDatabaseUpdater implements IMaintenanceTask
{

    private static final String SYNCHRONIZATION_TABLE = "EVENTS";

    private static final String LAST_SEEN_EVENT_ID = "LAST_SEEN_DELETION_EVENT_ID";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, MetabolDatabaseUpdater.class);

    private final IEncapsulatedOpenBISService openBISService;

    private final DatabaseConfigurationContext context;

    private Connection connection;

    public MetabolDatabaseUpdater()
    {
        LogInitializer.init();
        // NOTE: hard-coded database name - should be moved to spring configuration file
        context = DBUtils.createDefaultDBContext();
        context.setDatabaseKind("productive");
        context.setScriptFolder("sql");

        DBUtils.init(context);
        checkDatabseConnection();
        openBISService = ServiceProvider.getOpenBISService();
    }

    public void setUp(String pluginName, Properties properties)
    {
        operationLog.info("Plugin initialized: " + pluginName);
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
                deleteDatasets(deletedDataSets);
                updateSynchronizationDate(lastSeenEventId, deletedDataSets);
                connection.commit();
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
                "Synchronizing deletions of %d datasets with the metabolomics database.",
                deletedDataSets.size()));
        connection.createStatement().execute(
                String.format("DELETE FROM data_sets WHERE perm_id IN (%s)",
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
            executeSql("delete from events");
            executeSql("INSERT INTO " + SYNCHRONIZATION_TABLE + " (" + LAST_SEEN_EVENT_ID
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
                        "SELECT MAX(" + LAST_SEEN_EVENT_ID + ") AS " + LAST_SEEN_EVENT_ID
                                + " FROM " + SYNCHRONIZATION_TABLE);
        while (result.next())
        {
            long newLastSeenEventId = result.getLong(LAST_SEEN_EVENT_ID);
            if (maxLastSeenEventId == null || maxLastSeenEventId < newLastSeenEventId)
            {
                maxLastSeenEventId = newLastSeenEventId;
            }
        }
        return maxLastSeenEventId;
    }
}
