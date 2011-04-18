/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.postregistration;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.path.DatabaseBasedDataSetPathsInfoFeeder;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PathInfoDataSourceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;

/**
 * Post registration task which feeds pathinfo database with all data set paths.
 *
 * @author Franz-Josef Elmer
 */
public class PathInfoDatabaseFeedingTask extends AbstractPostRegistrationTask
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PathInfoDatabaseFeedingTask.class);
    
    private final IShareIdManager shareIdManager;
    private final IConfigProvider configProvider;
    private final DataSource dataSource;

    public PathInfoDatabaseFeedingTask(Properties properties, IEncapsulatedOpenBISService service)
    {
        this(properties, service, ServiceProvider.getShareIdManager(), ServiceProvider
                .getConfigProvider(), PathInfoDataSourceProvider.getDataSource());
    }
    
    @Private
    PathInfoDatabaseFeedingTask(Properties properties, IEncapsulatedOpenBISService service,
            IShareIdManager shareIdManager, IConfigProvider configProvider, DataSource dataSource)
    {
        super(properties, service);
        this.shareIdManager = shareIdManager;
        this.configProvider = configProvider;
        this.dataSource = dataSource;
    }

    public boolean requiresDataStoreLock()
    {
        return false;
    }

    public IPostRegistrationTaskExecutor createExecutor(String dataSetCode)
    {
        return new Executor(dataSetCode);
    }
    
    private final class Executor implements IPostRegistrationTaskExecutor
    {
        private final String dataSetCode;

        private Executor(String dataSetCode)
        {
            this.dataSetCode = dataSetCode;
        }
        
        public ICleanupTask createCleanupTask()
        {
            return new NoCleanupTask();
        }

        public void execute()
        {
            ExternalData dataSet = service.tryGetDataSet(dataSetCode);
            if (dataSet == null)
            {
                operationLog.error("Data set " + dataSetCode + " unknown by openBIS.");
                return;
            }
            String location = dataSet.getLocation();
            shareIdManager.lock(dataSetCode);
            File share =
                    new File(configProvider.getStoreRoot(), shareIdManager.getShareId(dataSetCode));
            File dataSetRoot = new File(share, location);
            if (dataSetRoot.exists() == false)
            {
                operationLog.error("Root directory of data set " + dataSetCode
                        + " does not exists: " + dataSetRoot);
                shareIdManager.releaseLocks();
                return;
            }

            Connection connection = null;
            try
            {
                connection = dataSource.getConnection();
                DatabaseBasedDataSetPathsInfoFeeder feeder =
                        new DatabaseBasedDataSetPathsInfoFeeder(connection);
                feeder.addPaths(dataSetCode, location, dataSetRoot);
                connection.commit();
                operationLog.info("Successfully added paths inside data set " + dataSetCode
                        + " to database.");
            } catch (SQLException ex)
            {
                handleException(ex, connection);
            } finally
            {
                closeConnection(connection);
                shareIdManager.releaseLocks();
            }
        }

        private void handleException(SQLException ex, Connection connection)
        {
            operationLog.error("Couldn't feed database with path infos of data set " + dataSetCode,
                    ex);
            if (connection != null)
            {
                try
                {
                    connection.rollback();
                } catch (SQLException ex1)
                {
                    operationLog.error("Couldn't rollback path info feeding for data set "
                            + dataSetCode, ex1);
                }
            }
        }

        private void closeConnection(Connection connection)
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                } catch (SQLException ex)
                {
                    operationLog.error("Couldn't close connection", ex);
                }
            }
        }
        
    }

}
