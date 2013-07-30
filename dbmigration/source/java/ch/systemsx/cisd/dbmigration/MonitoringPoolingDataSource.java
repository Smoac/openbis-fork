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

import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.dbcp.DelegatingCallableStatement;
import org.apache.commons.dbcp.DelegatingConnection;
import org.apache.commons.dbcp.DelegatingPreparedStatement;
import org.apache.commons.dbcp.DelegatingStatement;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
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
    private static final long DEFAULT_PERIOD_TIMER_MILLIS = 10 * 1000L;

    private final static Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, MonitoringPoolingDataSource.class);

    private final static Logger notifyLog =
            LogFactory.getLogger(LogCategory.NOTIFY, MonitoringPoolingDataSource.class);

    // static fields for inter-datasource connection logging

    private static final Timer infoControlTimer = new Timer("db connection info", true);

    private static DatabaseConnectionInfoController infoController =
            new DatabaseConnectionInfoController();

    static final Map<Integer, BorrowedConnectionRecord> activeConnections =
            new ConcurrentHashMap<Integer, BorrowedConnectionRecord>();

    private static volatile boolean logStackTrace;

    private final long activeConnectionsLogInterval;

    private final int activeConnectionsLogThreshold;

    private final String url;

    private long lastLogged;

    private int maxActiveSinceLastLogged;

    private volatile boolean logConnection;

    static
    {
        infoControlTimer.schedule(infoController, DEFAULT_PERIOD_TIMER_MILLIS, DEFAULT_PERIOD_TIMER_MILLIS);
    }

    public MonitoringPoolingDataSource(ObjectPool pool, String url,
            long activeConnectionsLogInterval,
            int activeConnectionsLogThreshold)
    {
        super(pool);
        this.activeConnectionsLogThreshold = activeConnectionsLogThreshold;
        this.activeConnectionsLogInterval = activeConnectionsLogInterval;
        this.url = url;
    }

    // Kept for backward compatibility, but oldActiveConnectionTimeMillis and logStackTrace are
    // ignored now.
    public MonitoringPoolingDataSource(ObjectPool pool, String url,
            long activeConnectionsLogInterval,
            int activeConnectionsLogThreshold, long oldActiveConnectionTimeMillis,
            boolean logStackTrace)
    {
        this(pool, url, activeConnectionsLogInterval, activeConnectionsLogThreshold);
    }

    /**
     * A record of information on a connection borrowed from the pool.
     */
    private static class BorrowedConnectionRecord
    {
        final long timeOfBorrowing;

        final String dbUrl;

        final String threadName;

        final StackTraceElement[] borrowStackTraceOrNull;

        BorrowedConnectionRecord(long timeOfBorrowing, String dbUrl, String threadName,
                StackTraceElement[] borrowStackTraceOrNull)
        {
            this.timeOfBorrowing = timeOfBorrowing;
            this.dbUrl = dbUrl;
            this.threadName = threadName;
            this.borrowStackTraceOrNull = borrowStackTraceOrNull;
        }
    }

    /**
     * A class that controls printing of information about active database connections.
     */
    private static class DatabaseConnectionInfoController extends TimerTask
    {
        private final File controlDir = new File("control");

        @Override
        public void run()
        {
            final File[] controlFiles = controlDir.listFiles();
            if (controlFiles == null)
            {
                return;
            }
            for (File f : controlFiles)
            {
                boolean knownCommand = false;
                final String name = f.getName();
                if (name.startsWith("db-connections-print-active"))
                {
                    final String[] split = StringUtils.split(name, '.');
                    final long oldActiveConnTimeMillis =
                            (split.length > 1) ? NumberUtils.toLong(split[1], 0) : 0;
                    logActiveDatabaseConnections(System.currentTimeMillis(),
                            oldActiveConnTimeMillis);
                    knownCommand = true;
                } else if ("db-connections-stacktrace-on".equals(name))
                {
                    setLogStackTrace(true);
                    knownCommand = true;
                } else if ("db-connections-stacktrace-off".equals(name))
                {
                    setLogStackTrace(false);
                    knownCommand = true;
                }
                if (knownCommand == false)
                {
                    machineLog.warn("Don't know what to do with control file '" + name
                            + "', ignoring.");
                    machineLog.warn("Supported control files are:");
                    machineLog.warn("db-connections-print-active[.nnn] - print all active " +
                            "database connections older than nnn milli-seconds.");
                    machineLog.warn("db-connections-stacktrace-on - switch on recording " +
                            "stacktraces when handing out a database connection.");
                    machineLog.warn("db-connections-stacktrace-off - switch off recording " +
                            "stacktraces when handing out a database connection.");
                }
                f.delete();
            }
        }
    }

    /**
     * Re-schedules the database info controller with a new period.
     */
    public synchronized static void rescheduleInfoController(long periodInMillis)
    {
        infoController.cancel();
        infoControlTimer.purge();
        infoController = new DatabaseConnectionInfoController();
        infoControlTimer.schedule(infoController, periodInMillis, periodInMillis);
    }

    public static boolean isLogStackTrace()
    {
        return logStackTrace;
    }

    public static void setLogStackTrace(boolean logStackTrace)
    {
        MonitoringPoolingDataSource.logStackTrace = logStackTrace;
        machineLog.info((logStackTrace ? "Enable" : "Disable")
                + " stacktrace recording for database connections.");
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
            if (numActive > activeConnectionsLogThreshold)
            {
                if (logConnection == false)
                {
                    logConnection = true;
                    if (activeConnectionsLogThreshold > 0)
                    {
                        notifyLog.warn(String.format(
                                "Switch on database connection logging: %d > %d",
                                numActive, activeConnectionsLogThreshold));
                    } else
                    {
                        if (machineLog.isInfoEnabled())
                        {
                            machineLog.info(String.format(
                                    "Switch on database connection logging: %d > %d",
                                    numActive, activeConnectionsLogThreshold));
                        }
                    }
                }
            } else
            {
                if (logConnection)
                {
                    logConnection = false;
                    if (machineLog.isInfoEnabled())
                    {
                        machineLog.info(String.format(
                                "Switch off database connection logging: %d <= %d",
                                numActive, activeConnectionsLogThreshold));
                    }
                }
            }
            if ((activeConnectionsLogInterval > 0)
                    && (now - lastLogged > activeConnectionsLogInterval)
                    && maxActiveSinceLastLogged > 1)
            {
                if (machineLog.isInfoEnabled() && logConnection == false)
                {
                    machineLog.info(String.format(
                            "Active database connections: current: %d, peak: %d.", numActive,
                            maxActiveSinceLastLogged));
                }
                lastLogged = now;
                maxActiveSinceLastLogged = 0;
            }
            if (conn != null)
            {
                conn = new PoolGuardConnectionWrapper(conn);
                activeConnections.put(System.identityHashCode(conn),
                        new BorrowedConnectionRecord(now, url,
                                Thread.currentThread().getName(), logStackTrace ? getStackTrace()
                                        : null));
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

    static void logActiveDatabaseConnections(long now, long oldActiveConnTimeMillis)
    {
        final List<BorrowedConnectionRecord> records =
                new ArrayList<BorrowedConnectionRecord>(activeConnections.values());
        Collections.sort(records, new Comparator<BorrowedConnectionRecord>()
            {
                @Override
                public int compare(BorrowedConnectionRecord o1,
                        BorrowedConnectionRecord o2)
                {
                    return (int) (o2.timeOfBorrowing - o1.timeOfBorrowing);
                }
            });
        if (records.isEmpty()
                || now - records.get(0).timeOfBorrowing < oldActiveConnTimeMillis)
        {
            System.err.printf("There are no active database connections older than %d ms.\n",
                    oldActiveConnTimeMillis);
            return;
        }
        System.err
                .printf("\n>>>--- %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL " +
                        "---------------------------------->>>\n", now);
        if (oldActiveConnTimeMillis > 0)
        {
            System.err
                    .printf("Active database connection overview older than %d ms (sorted by age)\n",
                            oldActiveConnTimeMillis);
        } else
        {
            System.err.printf("Active database connection overview (sorted by age)\n");

        }
        System.err.printf("%-25s\t%-25s\t%-25s\n", "Time", "Thread", "Database");
        boolean hasStackTraces = false;
        for (BorrowedConnectionRecord record : records)
        {
            if (now - record.timeOfBorrowing > oldActiveConnTimeMillis)
            {
                System.err.printf(
                        "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL  \t%2$-25s\t%3$-25s\n",
                        record.timeOfBorrowing, record.threadName, record.dbUrl);
                hasStackTraces |= (record.borrowStackTraceOrNull != null);
            }
        }

        if (hasStackTraces)
        {
            System.err.println("\nActive database connection stacktraces");
            // Print stacktraces
            boolean noNewline = true;
            for (BorrowedConnectionRecord record : records)
            {
                if (now - record.timeOfBorrowing > oldActiveConnTimeMillis
                        && record.borrowStackTraceOrNull != null)
                {
                    if (noNewline)
                    {
                        noNewline = false;
                    } else
                    {
                        System.err.println();
                    }
                    System.err.printf("Time: %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL\n",
                            record.timeOfBorrowing);
                    System.err.printf("Database: %s\n", record.dbUrl);
                    System.err.printf("Thread: %s\n", record.threadName);
                    System.err.print("Stacktrace:\n"
                            + traceToString(record.borrowStackTraceOrNull));
                }
            }
        }
        System.err
                .printf("<<<--- %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL " +
                        "----------------------------------<<<\n", now);
    }

    static StackTraceElement[] getStackTrace()
    {
        final Throwable th = new Throwable();
        th.fillInStackTrace();
        return th.getStackTrace();
    }

    static String tryGetServiceMethodName(StackTraceElement[] stackTrace)
    {
        String innerMethodName = null;
        String outerMethodName = null;
        for (StackTraceElement e : stackTrace)
        {
            if (e.getClassName().contains("$Proxy"))
            {
                if (innerMethodName == null)
                {
                    innerMethodName = e.getMethodName();
                }
                outerMethodName = e.getMethodName();
            }
        }
        if (innerMethodName == null)
        {
            return null;
        }
        if (innerMethodName.equals(outerMethodName))
        {
            return outerMethodName;
        } else
        {
            return outerMethodName + " / " + innerMethodName;
        }
    }

    static String traceToString(StackTraceElement[] trace)
    {
        final StringBuilder builder = new StringBuilder();
        boolean skip = true;
        for (StackTraceElement te : trace)
        {
            if (skip)
            {
                skip = false;
                continue;
            }
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
        PoolGuardConnectionWrapper(Connection delegate)
        {
            super(delegate);
            log("Hand out database connection");
        }

        void log(String action)
        {
            if (logConnection && machineLog.isDebugEnabled())
            {
                final int numActive = _pool.getNumActive();
                final StackTraceElement[] stackTrace = getStackTrace();
                final String serviceMethod = tryGetServiceMethodName(stackTrace);
                if (serviceMethod == null)
                {
                    if (logStackTrace)
                    {
                        machineLog.debug(action + ", id=" + hashCode() + ", active=" + numActive
                                + ".\n" + traceToString(stackTrace));
                    } else
                    {
                        machineLog.debug(action + ", id=" + hashCode() + ", active=" + numActive
                                + ".");
                    }
                } else
                {
                    if (logStackTrace)
                    {
                        machineLog.debug(action + ", id=" + hashCode() + ", active=" + numActive
                                + ", service method: " + serviceMethod + ".\n"
                                + traceToString(stackTrace));
                    } else
                    {
                        machineLog.debug(action + ", id=" + hashCode() + ", active=" + numActive
                                + ", service method: " + serviceMethod + ".");
                    }
                }
            }
        }

        @Override
        protected void checkOpen() throws SQLException
        {
            if (_conn == null)
            {
                throw new SQLException("Connection is closed.");
            }
        }

        @Override
        public void close() throws SQLException
        {
            if (_conn != null)
            {
                this._conn.close();
                super.setDelegate(null);
                activeConnections.remove(System.identityHashCode(this));
                log("Return database connection");
            }
        }

        @Override
        public boolean isClosed() throws SQLException
        {
            if (_conn == null)
            {
                return true;
            }
            return _conn.isClosed();
        }

        @Override
        public void clearWarnings() throws SQLException
        {
            checkOpen();
            _conn.clearWarnings();
        }

        @Override
        public void commit() throws SQLException
        {
            checkOpen();
            _conn.commit();
        }

        @Override
        public Statement createStatement() throws SQLException
        {
            checkOpen();
            return new DelegatingStatement(this, _conn.createStatement());
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency)
                throws SQLException
        {
            checkOpen();
            return new DelegatingStatement(this, _conn.createStatement(resultSetType,
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
            return _conn.getAutoCommit();
        }

        @Override
        public String getCatalog() throws SQLException
        {
            checkOpen();
            return _conn.getCatalog();
        }

        @Override
        public DatabaseMetaData getMetaData() throws SQLException
        {
            checkOpen();
            return _conn.getMetaData();
        }

        @Override
        public int getTransactionIsolation() throws SQLException
        {
            checkOpen();
            return _conn.getTransactionIsolation();
        }

        @SuppressWarnings(
            { "rawtypes", "unchecked" })
        @Override
        public Map getTypeMap() throws SQLException
        {
            checkOpen();
            return _conn.getTypeMap();
        }

        @Override
        public SQLWarning getWarnings() throws SQLException
        {
            checkOpen();
            return _conn.getWarnings();
        }

        @Override
        public boolean isReadOnly() throws SQLException
        {
            checkOpen();
            return _conn.isReadOnly();
        }

        @Override
        public String nativeSQL(String sql) throws SQLException
        {
            checkOpen();
            return _conn.nativeSQL(sql);
        }

        @Override
        public CallableStatement prepareCall(String sql) throws SQLException
        {
            checkOpen();
            return new DelegatingCallableStatement(this, _conn.prepareCall(sql));
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
                throws SQLException
        {
            checkOpen();
            return new DelegatingCallableStatement(this, _conn.prepareCall(sql, resultSetType,
                    resultSetConcurrency));
        }

        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException
        {
            checkOpen();
            return new DelegatingPreparedStatement(this, _conn.prepareStatement(sql));
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType,
                int resultSetConcurrency) throws SQLException
        {
            checkOpen();
            return new DelegatingPreparedStatement(this, _conn.prepareStatement(sql,
                    resultSetType, resultSetConcurrency));
        }

        @Override
        public void rollback() throws SQLException
        {
            checkOpen();
            _conn.rollback();
        }

        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException
        {
            checkOpen();
            _conn.setAutoCommit(autoCommit);
        }

        @Override
        public void setCatalog(String catalog) throws SQLException
        {
            checkOpen();
            _conn.setCatalog(catalog);
        }

        @Override
        public void setReadOnly(boolean readOnly) throws SQLException
        {
            checkOpen();
            _conn.setReadOnly(readOnly);
        }

        @Override
        public void setTransactionIsolation(int level) throws SQLException
        {
            checkOpen();
            _conn.setTransactionIsolation(level);
        }

        @Override
        @SuppressWarnings(
            { "rawtypes", "unchecked" })
        public void setTypeMap(Map map) throws SQLException
        {
            checkOpen();
            _conn.setTypeMap(map);
        }

        @Override
        public String toString()
        {
            if (_conn == null)
            {
                return "NULL";
            }
            return _conn.toString();
        }

        @Override
        public int getHoldability() throws SQLException
        {
            checkOpen();
            return _conn.getHoldability();
        }

        @Override
        public void setHoldability(int holdability) throws SQLException
        {
            checkOpen();
            _conn.setHoldability(holdability);
        }

        @Override
        public java.sql.Savepoint setSavepoint() throws SQLException
        {
            checkOpen();
            return _conn.setSavepoint();
        }

        @Override
        public java.sql.Savepoint setSavepoint(String name) throws SQLException
        {
            checkOpen();
            return _conn.setSavepoint(name);
        }

        @Override
        public void releaseSavepoint(java.sql.Savepoint savepoint) throws SQLException
        {
            checkOpen();
            _conn.releaseSavepoint(savepoint);
        }

        @Override
        public void rollback(java.sql.Savepoint savepoint) throws SQLException
        {
            checkOpen();
            _conn.rollback(savepoint);
        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency,
                int resultSetHoldability) throws SQLException
        {
            checkOpen();
            return new DelegatingStatement(this, _conn.createStatement(resultSetType,
                    resultSetConcurrency, resultSetHoldability));
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType,
                int resultSetConcurrency, int resultSetHoldability) throws SQLException
        {
            checkOpen();
            return new DelegatingCallableStatement(this, _conn.prepareCall(sql, resultSetType,
                    resultSetConcurrency, resultSetHoldability));
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
                throws SQLException
        {
            checkOpen();
            return new DelegatingPreparedStatement(this, _conn.prepareStatement(sql,
                    autoGeneratedKeys));
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType,
                int resultSetConcurrency, int resultSetHoldability) throws SQLException
        {
            checkOpen();
            return new DelegatingPreparedStatement(this, _conn.prepareStatement(sql,
                    resultSetType, resultSetConcurrency, resultSetHoldability));
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
                throws SQLException
        {
            checkOpen();
            return new DelegatingPreparedStatement(this, _conn.prepareStatement(sql,
                    columnIndexes));
        }

        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames)
                throws SQLException
        {
            checkOpen();
            return new DelegatingPreparedStatement(this,
                    _conn.prepareStatement(sql, columnNames));
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
