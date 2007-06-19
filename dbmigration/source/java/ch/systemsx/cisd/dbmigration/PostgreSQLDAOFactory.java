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

package ch.systemsx.cisd.dbmigration;

import javax.sql.DataSource;

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

    /**
     * Creates an instance based on the specified configuration context.
     */
    public PostgreSQLDAOFactory(DatabaseConfigurationContext context)
    {
        databaseDAO = new PostgreSQLAdminDAO(context.getAdminDataSource(), context.getOwner(), context.getDatabaseName());
        DataSource dataSource = context.getDataSource();
        sqlScriptExecutor = new SqlScriptExecutor(dataSource);
        databaseVersionLogDAO = new DatabaseVersionLogDAO(dataSource, context.getLobHandler());
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

}
