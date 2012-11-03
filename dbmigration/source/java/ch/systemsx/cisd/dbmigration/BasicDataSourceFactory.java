/*
 * Copyright 2010 ETH Zuerich, CISD
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

import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * @author Piotr Buczek
 */
public class BasicDataSourceFactory implements IDataSourceFactory
{

    /** @see GenericObjectPool#DEFAULT_MAX_ACTIVE */
    private static final int DEFAULT_MAX_ACTIVE = 20;

    /** @see GenericObjectPool#DEFAULT_MAX_IDLE */
    private static final int DEFAULT_MAX_IDLE = DEFAULT_MAX_ACTIVE;

    private static final int DEFAULT_MAX_WAIT = 60 * 1000;

    private static final int DEFAULT_ACTIVE_CONNECTIONS_LOG_INTERVAL = 3600 * 1000;

    private static final int DEFAULT_ACTIVE_NUM_CONNECTIONS_LOG_THRESHOLD =
            (int) (DEFAULT_MAX_ACTIVE * 0.8);

    private static final int DEFAULT_OLD_ACTIVE_CONNECTION_TIME = 0;

    private long maxWaitMillis = DEFAULT_MAX_WAIT;

    private int maxIdle = DEFAULT_MAX_IDLE;

    private int maxActive = DEFAULT_MAX_ACTIVE;

    private long activeConnectionsLogIntervalMillis = DEFAULT_ACTIVE_CONNECTIONS_LOG_INTERVAL;

    private int activeNumConnectionsLogThreshold = DEFAULT_ACTIVE_NUM_CONNECTIONS_LOG_THRESHOLD;
    
    private long oldActiveConnectionTimeMillis = DEFAULT_OLD_ACTIVE_CONNECTION_TIME;
    
    private boolean activeNumConnectionLogThresholdIsDefault = true;
    
    private boolean maxIdleIsDefault = true;
    
    private boolean logStackTraceOnConnectionLogging = false;

    //
    // IDataSourceFactory
    //

    @Override
    public final DataSource createDataSource(final String driver, final String url,
            final String owner, final String password, final String validationQuery)
    {
        final MonitoringDataSource dataSource = new MonitoringDataSource();
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(url);
        dataSource.setUsername(owner);
        dataSource.setPassword(password);
        dataSource.setMaxIdle(maxIdle);
        dataSource.setMaxActive(maxActive);
        dataSource.setMaxWait(maxWaitMillis * 1000L);
        dataSource.setActiveConnectionsLogInterval(activeConnectionsLogIntervalMillis);
        dataSource.setActiveConnectionsLogThreshold(activeNumConnectionsLogThreshold);
        dataSource.setOldActiveConnectionTimeMillis(oldActiveConnectionTimeMillis);
        dataSource.setLogStackTrace(logStackTraceOnConnectionLogging);
        dataSource.setValidationQuery(validationQuery);
        return dataSource;
    }

    @Override
    public long getMaxWait()
    {
        return maxWaitMillis;
    }
    
    @Override
    public void setMaxWait(long maxWait)
    {
        this.maxWaitMillis = maxWait;
    }

    @Override
    public int getMaxIdle()
    {
        return maxIdle;
    }
    
    @Override
    public void setMaxIdle(int maxIdle)
    {
        this.maxIdle = maxIdle;
        this.maxIdleIsDefault = false;
    }

    @Override
    public int getMaxActive()
    {
        return maxActive;
    }
    
    @Override
    public void setMaxActive(int maxActive)
    {
        this.maxActive = maxActive;
        if (activeNumConnectionLogThresholdIsDefault)
        {
            this.activeNumConnectionsLogThreshold = (int) (0.8 * maxActive);
        }
        if (maxIdleIsDefault)
        {
            this.maxIdle = maxActive;
        }
    }

    @Override
    public long getActiveConnectionsLogInterval()
    {
        return activeConnectionsLogIntervalMillis;
    }
    
    @Override
    public void setActiveConnectionsLogInterval(long activeConnectionLogIntervalMillis)
    {
        this.activeConnectionsLogIntervalMillis = activeConnectionLogIntervalMillis;
    }

    @Override
    public int getActiveNumConnectionsLogThreshold()
    {
        return activeNumConnectionsLogThreshold;
    }
    
    @Override
    public void setActiveNumConnectionsLogThreshold(int activeConnectionsLogThreshold)
    {
        this.activeNumConnectionsLogThreshold = activeConnectionsLogThreshold;
        this.activeNumConnectionLogThresholdIsDefault = false;
    }

    @Override
    public void setOldActiveConnectionTime(long oldActiveConnectionTimeMillis)
    {
        this.oldActiveConnectionTimeMillis = oldActiveConnectionTimeMillis;
    }

    @Override
    public long getOldActiveConnectionTime()
    {
        return oldActiveConnectionTimeMillis;
    }

    @Override
    public boolean isLogStackTraceOnConnectionLogging()
    {
        return logStackTraceOnConnectionLogging;
    }

    @Override
    public void setLogStackTraceOnConnectionLogging(boolean logStackTrace)
    {
        logStackTraceOnConnectionLogging = logStackTrace;
    }

}
