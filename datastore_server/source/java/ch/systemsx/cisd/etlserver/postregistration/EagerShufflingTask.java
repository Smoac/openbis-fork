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
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil;
import ch.systemsx.cisd.etlserver.plugins.DataSetMover;
import ch.systemsx.cisd.etlserver.plugins.IDataSetMover;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareFinder;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.IncomingShareIdProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Post registration task which move the data set to share which has enough space.
 *
 * @author Franz-Josef Elmer
 */
public class EagerShufflingTask extends AbstractPostRegistrationTask
{
    @Private public static final String SHARE_FINDER_KEY = "share-finder";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            EagerShufflingTask.class);

    private static SimpleDataSetInformationDTO findDataSet(List<Share> shares, String dataSetCode)
    {
        for (Share share : shares)
        {
            List<SimpleDataSetInformationDTO> dataSets = share.getDataSetsOrderedBySize();
            for (SimpleDataSetInformationDTO dataSet : dataSets)
            {
                if (dataSet.getDataSetCode().equals(dataSetCode))
                {
                    return dataSet;
                }
            }
        }
        throw new IllegalStateException("Data set " + dataSetCode + " not found.");
    }

    private final IShareIdManager shareIdManager;

    private final IFreeSpaceProvider freeSpaceProvider;

    private final IDataSetMover dataSetMover;

    private final ISimpleLogger logger;

    private final File storeRoot;

    private final String dataStoreCode;

    private final Set<String> incomingShares;

    private IShareFinder finder;
    
    public EagerShufflingTask(Properties properties, IEncapsulatedOpenBISService service)
    {
        this(properties, IncomingShareIdProvider.getIdsOfIncomingShares(), service, ServiceProvider
                .getShareIdManager(), new SimpleFreeSpaceProvider(), new DataSetMover(service,
                ServiceProvider.getShareIdManager()), ServiceProvider.getConfigProvider(),
                new Log4jSimpleLogger(operationLog));
    }

    @Private public EagerShufflingTask(Properties properties, Set<String> incomingShares,
            IEncapsulatedOpenBISService service, IShareIdManager shareIdManager,
            IFreeSpaceProvider freeSpaceProvider, IDataSetMover dataSetMover,
            IConfigProvider configProvider, ISimpleLogger logger)
    {
        super(properties, service);
        this.incomingShares = incomingShares;
        this.shareIdManager = shareIdManager;
        this.freeSpaceProvider = freeSpaceProvider;
        this.dataSetMover = dataSetMover;
        this.logger = logger;

        dataStoreCode = configProvider.getDataStoreCode();
        storeRoot = configProvider.getStoreRoot();
        if (storeRoot.isDirectory() == false)
        {
            throw new ConfigurationFailureException(
                    "Store root does not exists or is not a directory: "
                            + storeRoot.getAbsolutePath());
        }
        Properties props =
                PropertyParametersUtil.extractSingleSectionProperties(properties, SHARE_FINDER_KEY,
                        false).getProperties();
        finder = ClassUtils.create(IShareFinder.class, props.getProperty("class"), props);
    }

    public boolean requiresDataStoreLock()
    {
        return true;
    }

    public IPostRegistrationTaskExecutor createExecutor(String dataSetCode)
    {
        return new Executor(dataSetCode);
    }
    
    private final class Executor implements IPostRegistrationTaskExecutor
    {
        private final String dataSetCode;

        private SimpleDataSetInformationDTO dataSet;
        private Share shareWithMostFreeOrNull;
        
        Executor(String dataSetCode)
        {
            this.dataSetCode = dataSetCode;
        }

        public ICleanupTask createCleanupTask()
        {
            List<Share> shares =
                SegmentedStoreUtils.getDataSetsPerShare(storeRoot, dataStoreCode, incomingShares, 
                        freeSpaceProvider, service, logger);
            dataSet = findDataSet(shares, dataSetCode);
            shareWithMostFreeOrNull = finder.tryToFindShare(dataSet, shares);
            if (shareWithMostFreeOrNull == null)
            {
                logger.log(LogLevel.WARN, "No share found for shuffling data set " + dataSetCode
                        + ".");
                return new NoCleanupTask();
            }
            return new CleanupTask(dataSet, storeRoot, shareWithMostFreeOrNull.getShareId());
        }
        
        public void execute()
        {
            if (shareWithMostFreeOrNull != null)
            {
                File share = new File(storeRoot, shareIdManager.getShareId(dataSetCode));
                dataSetMover.moveDataSetToAnotherShare(
                        new File(share, dataSet.getDataSetLocation()),
                        shareWithMostFreeOrNull.getShare(), logger);
                logger.log(LogLevel.INFO, "Data set " + dataSetCode
                        + " successfully moved from share " + dataSet.getDataSetShareId() + " to "
                        + shareWithMostFreeOrNull.getShareId() + ".");
            }
        }
    }
    
    
    private static final class CleanupTask implements ICleanupTask
    {
        private static final long serialVersionUID = 1L;

        private final SimpleDataSetInformationDTO dataSet;
        private final File storeRoot;
        private final String newShareId;
        
        CleanupTask(SimpleDataSetInformationDTO dataSet, File storeRoot, String newShareId)
        {
            this.dataSet = dataSet;
            this.storeRoot = storeRoot;
            this.newShareId = newShareId;
        }

        public void cleanup(ISimpleLogger logger)
        {
            IShareIdManager shareIdManager = ServiceProvider.getShareIdManager();
            SegmentedStoreUtils.cleanUp(dataSet, storeRoot, newShareId, shareIdManager, logger);
        }
    }

}
