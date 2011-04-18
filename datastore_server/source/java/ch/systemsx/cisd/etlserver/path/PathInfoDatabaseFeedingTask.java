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

package ch.systemsx.cisd.etlserver.path;

import java.io.File;
import java.util.Properties;

import net.lemnik.eodsql.QueryTool;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.io.HierarchicalContentFactory;
import ch.systemsx.cisd.common.io.IHierarchicalContentFactory;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.postregistration.AbstractPostRegistrationTask;
import ch.systemsx.cisd.etlserver.postregistration.ICleanupTask;
import ch.systemsx.cisd.etlserver.postregistration.IPostRegistrationTaskExecutor;
import ch.systemsx.cisd.etlserver.postregistration.NoCleanupTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
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
    
    private final IDataSetDirectoryProvider directoryProvider;

    private final IPathsInfoDAO dao;

    private final IHierarchicalContentFactory hierarchicalContentFactory;

    public PathInfoDatabaseFeedingTask(Properties properties, IEncapsulatedOpenBISService service)
    {
        this(properties, service, ServiceProvider.getDataStoreService()
                .getDataSetDirectoryProvider(), QueryTool.getQuery(
                PathInfoDataSourceProvider.getDataSource(), IPathsInfoDAO.class),
                new HierarchicalContentFactory());
    }
    
    @Private
    PathInfoDatabaseFeedingTask(Properties properties, IEncapsulatedOpenBISService service,
            IDataSetDirectoryProvider directoryProvider, IPathsInfoDAO dao,
            IHierarchicalContentFactory hierarchicalContentFactory)
    {
        super(properties, service);
        this.directoryProvider = directoryProvider;
        this.dao = dao;
        this.hierarchicalContentFactory = hierarchicalContentFactory;
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
            IShareIdManager shareIdManager = directoryProvider.getShareIdManager();
            shareIdManager.lock(dataSetCode);
            File dataSetRoot = directoryProvider.getDataSetDirectory(dataSet);
            if (dataSetRoot.exists() == false)
            {
                operationLog.error("Root directory of data set " + dataSetCode
                        + " does not exists: " + dataSetRoot);
                shareIdManager.releaseLocks();
                return;
            }

            try
            {
                DatabaseBasedDataSetPathsInfoFeeder feeder =
                        new DatabaseBasedDataSetPathsInfoFeeder(dao, hierarchicalContentFactory);
                feeder.addPaths(dataSetCode, dataSet.getLocation(), dataSetRoot);
                dao.commit();
                operationLog.info("Successfully added paths inside data set " + dataSetCode
                        + " to database.");
            } catch (Exception ex)
            {
                handleException(ex);
            } finally
            {
                shareIdManager.releaseLocks();
            }
        }

        private void handleException(Exception ex)
        {
            operationLog.error("Couldn't feed database with path infos of data set " + dataSetCode,
                    ex);
            dao.rollback();
        }
        
    }

}
