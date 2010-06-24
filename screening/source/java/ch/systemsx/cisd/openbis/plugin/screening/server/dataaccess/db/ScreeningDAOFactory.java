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

package ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.db;

import java.sql.Connection;
import java.sql.SQLException;

import net.lemnik.eodsql.QueryTool;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.dbmigration.DBMigrationEngine;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.IScreeningDAOFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingQueryDAO;

/**
 * @author Piotr Buczek
 */
public class ScreeningDAOFactory implements IScreeningDAOFactory
{
    /** Current version of the database. */
    public static final String DATABASE_VERSION = "003"; // S83

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ScreeningDAOFactory.class);

    private final IImagingQueryDAO imagingQueryDAO;

    private final DatabaseConfigurationContext imagingDatabaseContext;

    public ScreeningDAOFactory(DatabaseConfigurationContext context)
    {
        this.imagingDatabaseContext = context;
        DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(context, DATABASE_VERSION);
        Connection connection = null;
        try
        {
            connection = context.getDataSource().getConnection();
        } catch (SQLException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        // TODO this is the same solution as in PhosphoNetX - is it ok to use connection here?
        // shouldn't it be a data source passed here in constructor?
        imagingQueryDAO = QueryTool.getQuery(connection, IImagingQueryDAO.class);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("DAO factory for Screening created.");
        }
    }

    public DatabaseConfigurationContext getContext()
    {
        return imagingDatabaseContext;
    }

    public IImagingQueryDAO getImagingQueryDAO()
    {
        return imagingQueryDAO;
    }
}
