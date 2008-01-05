/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.dbmigration.postgresql;

import java.sql.SQLException;

import javax.sql.DataSource;

import ch.systemsx.cisd.common.db.ISqlScriptExecutor;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.dbmigration.DatabaseVersionLogDAO;
import ch.systemsx.cisd.dbmigration.IDAOFactory;
import ch.systemsx.cisd.dbmigration.IDatabaseAdminDAO;
import ch.systemsx.cisd.dbmigration.IDatabaseVersionLogDAO;
import ch.systemsx.cisd.dbmigration.IMassUploader;
import ch.systemsx.cisd.dbmigration.SqlScriptExecutor;

/**
 * Implementation of {@link IDAOFactory} for PostgreSQL.
 * 
 * @author Franz-Josef Elmer
 */
public class PostgreSQLDAOFactory implements IDAOFactory
{
    private final IDatabaseAdminDAO databaseDAO;

    private final ISqlScriptExecutor sqlScriptExecutor;

    private final IDatabaseVersionLogDAO databaseVersionLogDAO;

    private final IMassUploader massUploader;

    /**
     * Creates an instance based on the specified configuration context.
     */
    public PostgreSQLDAOFactory(DatabaseConfigurationContext context)
    {
        final DataSource dataSource = context.getDataSource();
        sqlScriptExecutor = new SqlScriptExecutor(dataSource, context.isScriptSingleStepMode());
        databaseVersionLogDAO = new DatabaseVersionLogDAO(dataSource, context.getLobHandler());
        try
        {
            massUploader = new PostgreSQLMassUploader(dataSource, context.getSequenceNameMapper());
        } catch (SQLException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        }
        databaseDAO =
                new PostgreSQLAdminDAO(context.getAdminDataSource(), sqlScriptExecutor, massUploader, context
                        .getOwner(), context.getDatabaseName());
    }

    public IDatabaseAdminDAO getDatabaseDAO()
    {
        return databaseDAO;
    }

    public ISqlScriptExecutor getSqlScriptExecutor()
    {
        return sqlScriptExecutor;
    }

    public IDatabaseVersionLogDAO getDatabaseVersionLogDAO()
    {
        return databaseVersionLogDAO;
    }

    public IMassUploader getMassUploader()
    {
        return massUploader;
    }

}
