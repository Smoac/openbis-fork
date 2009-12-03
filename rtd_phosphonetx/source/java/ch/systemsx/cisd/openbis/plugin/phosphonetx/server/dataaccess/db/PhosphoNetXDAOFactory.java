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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.db;

import java.sql.SQLException;

import net.lemnik.eodsql.QueryTool;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.dbmigration.DBMigrationEngine;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IPhosphoNetXDAOFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.dataaccess.IProteinQueryDAO;

/**
 * @author Franz-Josef Elmer
 */
public class PhosphoNetXDAOFactory implements IPhosphoNetXDAOFactory
{
    /** Current version of the database. */
    public static final String DATABASE_VERSION = "001";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, PhosphoNetXDAOFactory.class);

    private final IProteinQueryDAO firstProteinQueryDAO;

    private final IProteinQueryDAO secondProteinQueryDAO;

    public PhosphoNetXDAOFactory(DatabaseConfigurationContext context)
    {
        DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(context, DATABASE_VERSION);
        firstProteinQueryDAO = createQuery(context);
        secondProteinQueryDAO = createQuery(context);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("DAO factory for PhosphoNetX created.");
        }
    }

    private IProteinQueryDAO createQuery(DatabaseConfigurationContext context)
    {
        try
        {
            return QueryTool.getQuery(context.getDataSource().getConnection(),
                    IProteinQueryDAO.class);
        } catch (SQLException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public IProteinQueryDAO getProteinQueryDAO()
    {
        return firstProteinQueryDAO;
    }

    public IProteinQueryDAO getSecondProteinQueryDAO()
    {
        return secondProteinQueryDAO;
    }

}
