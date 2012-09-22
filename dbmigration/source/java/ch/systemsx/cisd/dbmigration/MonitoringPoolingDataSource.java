/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.dbcp.DelegatingCallableStatement;
import org.apache.commons.dbcp.DelegatingConnection;
import org.apache.commons.dbcp.DelegatingPreparedStatement;
import org.apache.commons.dbcp.DelegatingStatement;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A {@link PoolingDataSource} that can log usage of active connections to help investigating
 * connection leaks.
 * 
 * @author Bernd Rinn
 */
class MonitoringPoolingDataSource extends PoolingDataSource
{
    private final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, MonitoringPoolingDataSource.class);

    private final static Logger notifyLog =
            LogFactory.getLogger(LogCategory.NOTIFY, MonitoringPoolingDataSource.class);

    private final long activeConnectionsLogInterval;

    private final int activeConnectionsLogThreshold;

    private long lastLogged;
    
    private int maxActiveSinceLastLogged;

    private volatile boolean logConnection;

    public MonitoringPoolingDataSource(ObjectPool pool, long activeConnectionsLogInternval,
            int activeConnectionsLogThreshold)
    {
        super(pool);
        this.activeConnectionsLogThreshold = activeConnectionsLogThreshold;
        this.activeConnectionsLogInterval = activeConnectionsLogInternval;
    }

    /**
     * Return a {@link java.sql.Connection} from my pool,
     * according to the contract specified by {@link ObjectPool#borrowObject}.
     */
    @SuppressWarnings("deprecation")
    @Override
    public Connection getConnection() throws SQLException
    {
        try
        {
            Connection conn = (Connection) (_pool.borrowObject());
            final long now = System.currentTimeMillis();
            final int numActive = _pool.getNumActive();
            maxActiveSinceLastLogged = Math.max(maxActiveSinceLastLogged, numActive);
            if (logConnection
                    || ((activeConnectionsLogInterval > 0)
                            && (now - lastLogged > activeConnectionsLogInterval) && numActive > 1))
            {
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info(String.format(
                            "Active database connections: %d.", maxActiveSinceLastLogged));
                }
                lastLogged = now;
                maxActiveSinceLastLogged = 0;
            }
            if (numActive > activeConnectionsLogThreshold)
            {
                if (logConnection == false)
                {
                    logConnection = true;
                    notifyLog.warn(String.format("Active database connections: %d > %d", numActive,
                            activeConnectionsLogThreshold));
                }
            } else
            {
                logConnection = false;
            }
            if (conn != null)
            {
                conn = new PoolGuardConnectionWrapper(conn);
            }
            return conn;
        } catch (SQLException e)
        {
            throw e;
        } catch (NoSuchElementException e)
        {
            throw new org.apache.commons.dbcp.SQLNestedException(
                    "Cannot get a connection, pool error " + e.getMessage(), e);
        } catch (RuntimeException e)
        {
            throw e;
        } catch (Exception e)
        {
            throw new org.apache.commons.dbcp.SQLNestedException(
                    "Cannot get a connection, general error", e);
        }
    }

    static StackTraceElement[] getStackTrace()
    {
        final Throwable th = new Throwable();
        th.fillInStackTrace();
        return th.getStackTrace();
    }

    static String tryGetServiceMethodName(StackTraceElement[] stackTrace)
    {
        for (StackTraceElement e : stackTrace)
        {
            if (e.getClassName().contains("$Proxy"))
            {
                return e.getMethodName();
            }
        }
        return null;
    }

    static String traceToString(StackTraceElement[] trace)
    {
        final StringBuilder builder = new StringBuilder();
        for (StackTraceElement te : trace)
        {
            builder.append("\tat ");
            builder.append(te.toString());
            builder.append('\n');
        }
        return builder.toString();
    }

    /**
     * PoolGuardConnectionWrapper is a Connection wrapper that makes sure a
     * closed connection cannot be used anymore.
     */
    private class PoolGuardConnectionWrapper extends DelegatingConnection
    {

        private Connection delegate;

        PoolGuardConnectionWrapper(Connection delegate)
        {
            super(delegate);
            this.delegate = delegate;
            log("Hand out database connection");
        }

        void log(String action)
        {
            if (logConnection && operationLog.isInfoEnabled())
            {
                final StackTraceElement[] stackTrace = getStackTrace();
                final String serviceMethod = tryGetServiceMethodName(stackTrace);
                if (serviceMethod == null)
                {
                    operationLog.info(action + ".\n" + traceToString(stackTrace));
                } else
                {
                    operationLog.info(action + ", service method: " + serviceMethod + ".");
                }
            }
        }

        @Override
        protected void checkOpen() throws SQLException
        {
            if (delegate == null)
            {
                throw new SQLException("Connection is closed.");
            }
        }

        @Override
        public void close() throws SQLException
        {
            log("Return database connection");
            if (delegate != null)
            {
                this.delegate.close();
                this.delegate = null;
                super.setDelegate(null);
            }
        }

        @Override
        public boolean isClosed() throws SQLException
        {
            if (delegate == null)
            {
                return true;
            }
            return delegate.isClosed();
        }

        @Override
        public void clearWarnings() throws SQLException
        {
            checkOpen();
            delegate.clearWarnings();
        }

        @Override
        public void commit() throws SQLException
        {
            checkOpen();
            delegate.commit();
        }

        @Override
        public Statement createStatement() throws SQLException
        {
            checkOpen();
            return new DelegatingStatement(this, delegate.createStatement());
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency)
                throws SQLException
        {
            checkOpen();
            return new DelegatingStatement(this, delegate.createStatement(resultSetType,
                    resultSetConcurrency));
        }

        @Override
        public boolean innermostDelegateEquals(Connection c)
        {
            Connection innerCon = super.getInnermostDelegate();
            if (innerCon == null)
            {
                return c == null;
            } else
            {
                return innerCon.equals(c);
            }
        }

        @Override
        public boolean getAutoCommit() throws SQLException
        {
            checkOpen();
            return delegate.getAutoCommit();
        }

        @Override
        public String getCatalog() throws SQLException
        {
            checkOpen();
            return delegate.getCatalog();
        }

        @Override
        public DatabaseMetaData getMetaData() throws SQLException
        {
            checkOpen();
            return delegate.getMetaData();
        }

        @Override
        public int getTransactionIsolation() throws SQLException
        {
            checkOpen();
            return delegate.getTransactionIsolation();
        }

        @SuppressWarnings(
            { "rawtypes", "unchecked" })
        @Override
        public Map getTypeMap() throws SQLException
        {
            checkOpen();
            return delegate.getTypeMap();
        }

        @Override
        public SQLWarning getWarnings() throws SQLException
        {
            checkOpen();
            return delegate.getWarnings();
        }

        @Override
        public int hashCode()
        {
            if (delegate == null)
            {
                return 0;
            }
            return delegate.hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (obj == this)
            {
                return true;
            }
            // Use superclass accessor to skip access test
            Connection conn = super.getInnermostDelegate();
            if (conn == null)
            {
                return false;
            }
            if (obj instanceof DelegatingConnection)
            {
                DelegatingConnection c = (DelegatingConnection) obj;
                return c.innermostDelegateEquals(conn);
            }
            else
            {
                return conn.equals(obj);
            }
        }

        @Override
        public boolean isReadOnly() throws SQLException
        {
            checkOpen();
            return delegate.isReadOnly();
        }

        @Override
        public String nativeSQL(String sql) throws SQLException
        {
            checkOpen();
            return delegate.nativeSQL(sql);
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException
        {
            checkOpen();
            return new DelegatingCallableStatement(this, delegate.prepareCall(sql));
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
                throws SQLException
        {
            checkOpen();
            return new DelegatingCallableStatement(this, delegate.prepareCall(sql, resultSetType,
                    resultSetConcurrency));
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException
        {
            checkOpen();
            return new DelegatingPreparedStatement(this, delegate.prepareStatement(sql));
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType,
                int resultSetConcurrency) throws SQLException
        {
            checkOpen();
            return new DelegatingPreparedStatement(this, delegate.prepareStatement(sql,
                    resultSetType, resultSetConcurrency));
        }

        @Override
        public void rollback() throws SQLException
        {
            checkOpen();
            delegate.rollback();
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException
        {
            checkOpen();
            delegate.setAutoCommit(autoCommit);
        }

        @Override
        public void setCatalog(String catalog) throws SQLException
        {
            checkOpen();
            delegate.setCatalog(catalog);
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException
        {
            checkOpen();
            delegate.setReadOnly(readOnly);
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException
        {
            checkOpen();
            delegate.setTransactionIsolation(level);
        }

        @Override
        @SuppressWarnings(
            { "rawtypes", "unchecked" })
        public void setTypeMap(Map map) throws SQLException
        {
            checkOpen();
            delegate.setTypeMap(map);
        }

        @Override
        public String toString()
        {
            if (delegate == null)
            {
                return "NULL";
            }
            return delegate.toString();
        }

        @Override
        public int getHoldability() throws SQLException
        {
            checkOpen();
            return delegate.getHoldability();
        }

        @Override
        public void setHoldability(int holdability) throws SQLException
        {
            checkOpen();
            delegate.setHoldability(holdability);
        }

        @Override
        public java.sql.Savepoint setSavepoint() throws SQLException
        {
            checkOpen();
            return delegate.setSavepoint();
        }

        @Override
        public java.sql.Savepoint setSavepoint(String name) throws SQLException
        {
            checkOpen();
            return delegate.setSavepoint(name);
        }

        @Override
        public void releaseSavepoint(java.sql.Savepoint savepoint) throws SQLException
        {
            checkOpen();
            delegate.releaseSavepoint(savepoint);
        }

        @Override
        public void rollback(java.sql.Savepoint savepoint) throws SQLException
        {
            checkOpen();
            delegate.rollback(savepoint);
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency,
                int resultSetHoldability) throws SQLException
        {
            checkOpen();
            return new DelegatingStatement(this, delegate.createStatement(resultSetType,
                    resultSetConcurrency, resultSetHoldability));
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType,
                int resultSetConcurrency, int resultSetHoldability) throws SQLException
        {
            checkOpen();
            return new DelegatingCallableStatement(this, delegate.prepareCall(sql, resultSetType,
                    resultSetConcurrency, resultSetHoldability));
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
                throws SQLException
        {
            checkOpen();
            return new DelegatingPreparedStatement(this, delegate.prepareStatement(sql,
                    autoGeneratedKeys));
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType,
                int resultSetConcurrency, int resultSetHoldability) throws SQLException
        {
            checkOpen();
            return new DelegatingPreparedStatement(this, delegate.prepareStatement(sql,
                    resultSetType, resultSetConcurrency, resultSetHoldability));
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
                throws SQLException
        {
            checkOpen();
            return new DelegatingPreparedStatement(this, delegate.prepareStatement(sql,
                    columnIndexes));
        }

        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames)
                throws SQLException
        {
            checkOpen();
            return new DelegatingPreparedStatement(this,
                    delegate.prepareStatement(sql, columnNames));
        }

        /**
         * @see org.apache.commons.dbcp.DelegatingConnection#getDelegate()
         */
        @Override
        public Connection getDelegate()
        {
            if (isAccessToUnderlyingConnectionAllowed())
            {
                return super.getDelegate();
            } else
            {
                return null;
            }
        }

        /**
         * @see org.apache.commons.dbcp.DelegatingConnection#getInnermostDelegate()
         */
        @Override
        public Connection getInnermostDelegate()
        {
            if (isAccessToUnderlyingConnectionAllowed())
            {
                return super.getInnermostDelegate();
            } else
            {
                return null;
            }
        }
    }
}
