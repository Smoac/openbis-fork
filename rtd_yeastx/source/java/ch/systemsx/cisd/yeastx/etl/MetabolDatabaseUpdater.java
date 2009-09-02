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
import java.util.Date;
import java.util.List;

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
public class MetabolDatabaseUpdater implements IMaintenanceTask
{

    private static final String SYNCHRONIZATION_TABLE = "EVENTS";

    private static final String SYNCHRONIZATION_TIMESTAMP = "EVENT_DATE";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, MetabolDatabaseUpdater.class);

    private Connection connection;

    private IEncapsulatedOpenBISService openBISService;

    private DatabaseConfigurationContext context;

    public void setUp(String pluginName)
    {
        LogInitializer.init();
        context = DBUtils.createDefaultDBContext();
        DBUtils.init(context);
        try
        {
            connection = context.getDataSource().getConnection();
            getPreviousSynchronizationDate();
            connection.close();
        } catch (SQLException ex)
        {
            throw new ConfigurationFailureException("Initialization failed", ex);
        }
        openBISService = ServiceProvider.getOpenBISService();
        operationLog.info("Plugin initialized");
    }

    public void execute()
    {
        operationLog.info("Synchronizing data set information");
        try
        {
            connection = context.getDataSource().getConnection();
            Date previousSyncDate = getPreviousSynchronizationDate();
            List<DeletedDataSet> deletedDataSets =
                    openBISService.listDeletedDataSets(previousSyncDate);
            if (deletedDataSets.size() > 0)
            {
                boolean autoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);
                deleteDatasets(deletedDataSets);
                updateSynchronizationDate(previousSyncDate, deletedDataSets);
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
        connection.createStatement().execute(
                String.format("DELETE FROM data_sets WHERE perm_id IN (%s)",
                        joinIds(deletedDataSets)));
    }

    private void updateSynchronizationDate(Date previousSyncDate, List<DeletedDataSet> deleted)
            throws SQLException
    {
        Date newSynchncDate = previousSyncDate;
        for (DeletedDataSet dds : deleted)
        {
            Date date = dds.getDeletionDate();
            if (newSynchncDate == null || date.after(newSynchncDate))
            {
                newSynchncDate = date;
            }
        }
        if (previousSyncDate == null || newSynchncDate.after(previousSyncDate))
        {
            PreparedStatement statement =
                    connection.prepareStatement("INSERT INTO " + SYNCHRONIZATION_TABLE + " ("
                            + SYNCHRONIZATION_TIMESTAMP + ") VALUES('" + newSynchncDate + "')");
            statement.executeUpdate();
        }
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

    private Date getPreviousSynchronizationDate() throws SQLException
    {
        Date lastDeleted = null;
        ResultSet result =
                connection.createStatement().executeQuery(
                        "SELECT MAX(" + SYNCHRONIZATION_TIMESTAMP + ") AS "
                                + SYNCHRONIZATION_TIMESTAMP + " FROM " + SYNCHRONIZATION_TABLE);
        while (result.next())
        {
            if (lastDeleted == null
                    || lastDeleted.before(result.getTimestamp(SYNCHRONIZATION_TIMESTAMP)))
            {
                lastDeleted = result.getTimestamp(SYNCHRONIZATION_TIMESTAMP);
            }
        }
        return lastDeleted;
    }
}
