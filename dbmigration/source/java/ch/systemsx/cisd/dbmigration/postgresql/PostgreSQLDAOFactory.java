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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.SmartDataSource;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.db.ISequenceNameMapper;
import ch.systemsx.cisd.common.db.ISqlScriptExecutor;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.dbmigration.DatabaseVersionLogDAO;
import ch.systemsx.cisd.dbmigration.IDAOFactory;
import ch.systemsx.cisd.dbmigration.IDatabaseAdminDAO;
import ch.systemsx.cisd.dbmigration.IDatabaseVersionLogDAO;
import ch.systemsx.cisd.dbmigration.IMassUploader;
import ch.systemsx.cisd.dbmigration.SqlScriptExecutor;
import ch.systemsx.cisd.dbmigration.java.IMigrationStepExecutor;
import ch.systemsx.cisd.dbmigration.java.MigrationStepExecutor;

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

    private final IMigrationStepExecutor migrationStepExecutor;

    private final IMigrationStepExecutor migrationStepExecutorAdmin;

    /**
     * Creates an instance based on the specified configuration context.
     */
    public PostgreSQLDAOFactory(final DatabaseConfigurationContext context)
    {
        final DataSource dataSource = wrap(context.getDataSource());
        sqlScriptExecutor = new SqlScriptExecutor(dataSource, context.isScriptSingleStepMode());
        migrationStepExecutor = MigrationStepExecutor.createExecutor(dataSource, context);
        DataSource adminDataSource = context.getAdminDataSource();
        migrationStepExecutorAdmin = MigrationStepExecutor.createExecutorForAdmin(adminDataSource, context);
        databaseVersionLogDAO = new DatabaseVersionLogDAO(dataSource, context.getLobHandler());
        try
        {
            ISequenceNameMapper mapper = context.getSequenceNameMapper();
            boolean sequenceUpdateNeeded = context.isSequenceUpdateNeeded();
            massUploader = new PostgreSQLMassUploader(dataSource, mapper, sequenceUpdateNeeded);
        } catch (final SQLException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        }
        databaseDAO =
                new PostgreSQLAdminDAO(adminDataSource, sqlScriptExecutor,
                        massUploader, context.getOwner(), context.getReadOnlyGroup(), context
                                .getReadWriteGroup(), context.getDatabaseName(), context
                                .getDatabaseURL());
    }
    
    private DataSource wrap(final DataSource dataSource)
    {
        return new SmartDataSource()
        {
            
            @Override
            public <T> T unwrap(Class<T> iface) throws SQLException
            {
                return dataSource.unwrap(iface);
            }
            
            @Override
            public boolean isWrapperFor(Class<?> iface) throws SQLException
            {
                return dataSource.isWrapperFor(iface);
            }
            
            @Override
            public void setLoginTimeout(int seconds) throws SQLException
            {
                dataSource.setLoginTimeout(seconds);
            }
            
            @Override
            public void setLogWriter(PrintWriter out) throws SQLException
            {
                dataSource.setLogWriter(out);
            }
            
            @Override
            public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException
            {
                return dataSource.getParentLogger();
            }
            
            @Override
            public int getLoginTimeout() throws SQLException
            {
                return dataSource.getLoginTimeout();
            }
            
            @Override
            public PrintWriter getLogWriter() throws SQLException
            {
                return dataSource.getLogWriter();
            }
            
            @Override
            public Connection getConnection(String username, String password) throws SQLException
            {
                System.err.println("SqlScriptExecutor.SqlScriptExecutor(...).new DataSource() {...}.getConnection(" + username +")");
                return dataSource.getConnection(username, password);
            }
            
            private Connection connection;
            @Override
            public Connection getConnection() throws SQLException
            {
                if (connection == null)
                {
                    connection = dataSource.getConnection();
                    connection.setAutoCommit(false);
                    System.err.println("Connection: "+System.identityHashCode(connection));
                }
                return connection;
            }

            @Override
            public boolean shouldClose(Connection con)
            {
                return false;
            }
        };
    }

    @Override
    public IDatabaseAdminDAO getDatabaseDAO()
    {
        return databaseDAO;
    }

    @Override
    public ISqlScriptExecutor getSqlScriptExecutor()
    {
        return sqlScriptExecutor;
    }

    @Override
    public IDatabaseVersionLogDAO getDatabaseVersionLogDAO()
    {
        return databaseVersionLogDAO;
    }

    @Override
    public IMassUploader getMassUploader()
    {
        return massUploader;
    }

    @Override
    public IMigrationStepExecutor getMigrationStepExecutor()
    {
        return migrationStepExecutor;
    }

    @Override
    public IMigrationStepExecutor getMigrationStepExecutorAdmin()
    {
        return migrationStepExecutorAdmin;
    }

}
