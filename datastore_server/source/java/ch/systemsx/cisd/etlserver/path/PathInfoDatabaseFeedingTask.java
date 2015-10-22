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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import net.lemnik.eodsql.QueryTool;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.time.DateTimeUtils;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.etlserver.postregistration.ICleanupTask;
import ch.systemsx.cisd.etlserver.postregistration.IPostRegistrationTask;
import ch.systemsx.cisd.etlserver.postregistration.IPostRegistrationTaskExecutor;
import ch.systemsx.cisd.etlserver.postregistration.NoCleanupTask;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.DefaultFileBasedHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.IHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PathInfoDataSourceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Maintenance and post registration task which feeds pathinfo database with all data set paths.
 * 
 * @author Franz-Josef Elmer
 */
public class PathInfoDatabaseFeedingTask implements IMaintenanceTask, IPostRegistrationTask
{
    private static interface IStopCondition
    {
        void handle(SimpleDataSetInformationDTO dataSet);
        boolean fulfilled();
    }
    
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PathInfoDatabaseFeedingTask.class);

    static final String COMPUTE_CHECKSUM_KEY = "compute-checksum";
    
    static final String CHUNK_SIZE_KEY = "data-set-chunk-size";
    
    static final int DEFAULT_CHUNK_SIZE = 1000;

    static final String MAX_NUMBER_OF_CHUNKS_KEY = "max-number-of-chunks";
    
    static final int DEFAULT_MAX_NUMBER_OF_DATA_SETS = -1;
    
    static final String TIME_LIMIT_KEY = "time-limit";
    
    private static IPathsInfoDAO createDAO()
    {
        return QueryTool.getQuery(PathInfoDataSourceProvider.getDataSource(), IPathsInfoDAO.class);
    }

    private static IDataSetDirectoryProvider getDirectoryProvider()
    {
        return ServiceProvider.getDataStoreService().getDataSetDirectoryProvider();
    }

    private static IHierarchicalContentFactory createContentFactory()
    {
        return new DefaultFileBasedHierarchicalContentFactory();
    }

    private IEncapsulatedOpenBISService service;

    private IDataSetDirectoryProvider directoryProvider;

    private ITimeProvider timeProvider;
    
    private IPathsInfoDAO dao;

    private IHierarchicalContentFactory hierarchicalContentFactory; // filesystem based

    private boolean computeChecksum;

    private int chunkSize;

    private int maxNumerOfChunks;

    private long timeLimit;

    public PathInfoDatabaseFeedingTask()
    {
    }

    public PathInfoDatabaseFeedingTask(Properties properties, IEncapsulatedOpenBISService service)
    {
        this(service, getDirectoryProvider(), createDAO(), createContentFactory(),
                SystemTimeProvider.SYSTEM_TIME_PROVIDER, getComputeChecksumFlag(properties), 0, 0, 0);
    }

    @Private
    PathInfoDatabaseFeedingTask(IEncapsulatedOpenBISService service,
            IDataSetDirectoryProvider directoryProvider, IPathsInfoDAO dao,
            IHierarchicalContentFactory hierarchicalContentFactory, ITimeProvider timeProvider, boolean computeChecksum,
            int chunkSize, int maxNumberOfChunks, long timeLimit)
    {
        this.service = service;
        this.directoryProvider = directoryProvider;
        this.dao = dao;
        this.hierarchicalContentFactory = hierarchicalContentFactory;
        this.timeProvider = timeProvider;
        this.computeChecksum = computeChecksum;
        this.chunkSize = chunkSize;
        maxNumerOfChunks = maxNumberOfChunks;
        this.timeLimit = timeLimit;
    }

    @Override
    public boolean requiresDataStoreLock()
    {
        return false;
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        service = ServiceProvider.getOpenBISService();
        directoryProvider = getDirectoryProvider();
        timeProvider = SystemTimeProvider.SYSTEM_TIME_PROVIDER;
        dao = createDAO();
        hierarchicalContentFactory = createContentFactory();
        computeChecksum = getComputeChecksumFlag(properties);
        chunkSize = PropertyUtils.getInt(properties, CHUNK_SIZE_KEY, DEFAULT_CHUNK_SIZE);
        maxNumerOfChunks =
                PropertyUtils.getInt(properties, MAX_NUMBER_OF_CHUNKS_KEY,
                        DEFAULT_MAX_NUMBER_OF_DATA_SETS);
        timeLimit = DateTimeUtils.getDurationInMillis(properties, TIME_LIMIT_KEY, 0);
        StringBuilder builder = new StringBuilder(pluginName);
        builder.append(" intialized with chunk size = ").append(chunkSize).append(".");
        if (timeLimit > 0)
        {
            builder.append(" Time limit: ").append(DateTimeUtils.renderDuration(timeLimit));
        } else if (maxNumerOfChunks > 0)
        {
            builder.append(" Maximum number of chunks: ").append(maxNumerOfChunks);
        }
        operationLog.info(builder.toString());
    }

    private static boolean getComputeChecksumFlag(Properties properties)
    {
        return PropertyUtils.getBoolean(properties, COMPUTE_CHECKSUM_KEY, false);
    }

    private static final int waitingPeriodForStorageConfirmationInSeconds = 3600;

    @Override
    public void execute()
    {
        IStopCondition stopCondition = createStopCondition();
        List<SimpleDataSetInformationDTO> dataSets;
        int chunkCount = 0;
        operationLog.info("Start feeding.");
        Set<String> processedDataSets = new HashSet<>(); 
        do
        {
            dataSets = filteredDataSets(getNextChunk(), processedDataSets);
            operationLog.info("Feeding " + ++chunkCount + ". chunk. " + dataSets.size() + " data sets.");
            Date maxRegistrationTimestamp = null;
            for (SimpleDataSetInformationDTO dataSet : dataSets)
            {
                feedPathInfoDatabase(dataSet);
                processedDataSets.add(dataSet.getDataSetCode());
                Date registrationTimestamp = dataSet.getRegistrationTimestamp();
                if (maxRegistrationTimestamp == null || maxRegistrationTimestamp.getTime() < registrationTimestamp.getTime())
                {
                    maxRegistrationTimestamp = registrationTimestamp;
                }
                stopCondition.handle(dataSet);
            }
            if (maxRegistrationTimestamp != null)
            {
                dao.deleteLastFeedingEvent();
                dao.createLastFeedingEvent(maxRegistrationTimestamp);
                dao.commit();
            }
        } while (dataSets.size() >= chunkSize && stopCondition.fulfilled() == false);
        operationLog.info("Feeding finished.");
    }
    
    private List<SimpleDataSetInformationDTO> filteredDataSets(List<SimpleDataSetInformationDTO> dataSets, Set<String> processedDataSets)
    {
        List<SimpleDataSetInformationDTO> result = new ArrayList<>();
        for (SimpleDataSetInformationDTO dataSet : dataSets)
        {
            if (processedDataSets.contains(dataSet.getDataSetCode()) == false)
            {
                result.add(dataSet);
            }
        }
        return result;
    }

    private List<SimpleDataSetInformationDTO> getNextChunk()
    {
        long start = System.currentTimeMillis();
        // we will wait periods of time defined by exponential sequence with 1.5 exponent up to 1h
        float waitingTime = 1000;
        while (System.currentTimeMillis() - start < waitingPeriodForStorageConfirmationInSeconds * 1000)
        {
            List<SimpleDataSetInformationDTO> dataSets = listDataSets();
            SimpleDataSetInformationDTO nonConfirmedData = findFirstNonConfirmedDataSet(dataSets);
            if (nonConfirmedData == null)
            {
                return dataSets;
            }

            long waitingTimeInMiliseconds = (long) waitingTime;
            operationLog.info("One of the data sets selected for path-info feeding doesn't yet have storage confirmed "
                    + nonConfirmedData.getDataSetCode() + ". Will wait for "
                    + (waitingTimeInMiliseconds / 1000) + " seconds");
            try
            {
                Thread.sleep(waitingTimeInMiliseconds);
            } catch (InterruptedException ex)
            {
            }
            waitingTime *= 1.5;
        }
        List<SimpleDataSetInformationDTO> dataSets = listDataSets();
        List<SimpleDataSetInformationDTO> result = new ArrayList<SimpleDataSetInformationDTO>();
        for (SimpleDataSetInformationDTO dataSet : dataSets)
        {
            if (dataSet.isStorageConfirmed())
            {
                result.add(dataSet);
            } else
            {
                operationLog.error("Gave up on feeding path-info db for data set " + dataSet.getDataSetCode() + "");
            }
        }
        return result;
    }

    private SimpleDataSetInformationDTO findFirstNonConfirmedDataSet(List<SimpleDataSetInformationDTO> dataSets)
    {
        for (SimpleDataSetInformationDTO dataSet : dataSets)
        {
            if (dataSet.isStorageConfirmed() == false)
            {
                return dataSet;
            }
        }
        return null;
    }

    private List<SimpleDataSetInformationDTO> listDataSets()
    {
        Date timestamp = dao.getRegistrationTimestampOfLastFeedingEvent();
        return listDataSets(timestamp, chunkSize);
    }

    private List<SimpleDataSetInformationDTO> listDataSets(Date timestamp, int actualChunkSize)
    {
        List<SimpleDataSetInformationDTO> result;
        if (timestamp == null)
        {
            result = service.listOldestPhysicalDataSets(actualChunkSize);
        }
        else
        {
            result = service.listOldestPhysicalDataSets(timestamp, actualChunkSize);
        }
        if (result.size() < actualChunkSize || allRegistrationTimeStampsTheSame(result) == false)
        {
            return result;
        }
        operationLog.warn("There are at least " + actualChunkSize 
                + " data sets with same registration time stamp. Twice the chunk size will be tried.");
        return listDataSets(timestamp, 2 * actualChunkSize);
    }
    
    private boolean allRegistrationTimeStampsTheSame(List<SimpleDataSetInformationDTO> dataSets)
    {
        Set<Date> registrationTimeStamps = new HashSet<>();
        for (SimpleDataSetInformationDTO dataSet : dataSets)
        {
            registrationTimeStamps.add(dataSet.getRegistrationTimestamp());
        }
        return registrationTimeStamps.size() == 1;
    }

    @Override
    public IPostRegistrationTaskExecutor createExecutor(final String dataSetCode, boolean container)
    {
        return new IPostRegistrationTaskExecutor()
            {
                @Override
                public ICleanupTask createCleanupTask()
                {
                    return new NoCleanupTask();
                }

                @Override
                public void execute()
                {
                    AbstractExternalData dataSet = service.tryGetDataSet(dataSetCode);
                    if (dataSet == null)
                    {
                        operationLog.error("Data set " + dataSetCode + " unknown to openBIS.");
                        return;
                    }
                    if (false == dataSet.isContainer())
                    {
                        IDatasetLocation dataSetLocation = dataSet.tryGetAsDataSet();
                        feedPathInfoDatabase(dataSetLocation);
                    }
                }
            };
    }

    private void feedPathInfoDatabase(IDatasetLocation dataSet)
    {
        IShareIdManager shareIdManager = directoryProvider.getShareIdManager();
        String dataSetCode = dataSet.getDataSetCode();
        shareIdManager.lock(dataSetCode);

        try
        {
            File dataSetRoot = directoryProvider.getDataSetDirectory(dataSet);
            if (dataSetRoot.exists() == false)
            {
                operationLog.error("Root directory of data set " + dataSetCode
                        + " does not exists: " + dataSetRoot);
                shareIdManager.releaseLocks();
                return;
            }
            DatabaseBasedDataSetPathsInfoFeeder feeder =
                    new DatabaseBasedDataSetPathsInfoFeeder(dao, hierarchicalContentFactory,
                            computeChecksum);
            Long id = dao.tryGetDataSetId(dataSetCode);
            if (id == null)
            {
                feeder.addPaths(dataSetCode, dataSet.getDataSetLocation(), dataSetRoot);
                feeder.commit();
                operationLog.info("Paths inside data set " + dataSetCode
                        + " successfully added to database.");
            }
        } catch (Exception ex)
        {
            handleException(ex, dataSetCode);
        } finally
        {
            shareIdManager.releaseLocks();
        }
    }

    private void handleException(Exception ex, String dataSet)
    {
        operationLog.error("Couldn't feed database with path infos of data set " + dataSet, ex);
        dao.rollback();
    }

    private IStopCondition createStopCondition()
    {
        if (timeLimit > 0)
        {
            return createStopConditionForTimeLimit();
        }
        if (maxNumerOfChunks > 0)
        {
            return createStopConditionForMaxNumber();
        }
        return new IStopCondition()
            {

                @Override
                public void handle(SimpleDataSetInformationDTO dataSet)
                {
                }

                @Override
                public boolean fulfilled()
                {
                    return false;
                }
            };
    }

    private IStopCondition createStopConditionForMaxNumber()
    {
        return new IStopCondition()
            {
                private int count;

                @Override
                public void handle(SimpleDataSetInformationDTO dataSet)
                {
                }

                @Override
                public boolean fulfilled()
                {
                    return ++count >= maxNumerOfChunks;
                }
            };
    }

    private IStopCondition createStopConditionForTimeLimit()
    {
        return new IStopCondition()
            {
                private long startTime = timeProvider.getTimeInMilliseconds();


                @Override
                public void handle(SimpleDataSetInformationDTO dataSet)
                {
                }

                @Override
                public boolean fulfilled()
                {
                    return timeProvider.getTimeInMilliseconds() - startTime > timeLimit;
                }
            };
    }

}
