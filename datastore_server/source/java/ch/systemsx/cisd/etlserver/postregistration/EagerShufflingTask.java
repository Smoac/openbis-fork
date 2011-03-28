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

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.ETLDaemon;
import ch.systemsx.cisd.etlserver.plugins.DataSetMover;
import ch.systemsx.cisd.etlserver.plugins.IDataSetMover;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
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
    
    public EagerShufflingTask(Properties properties, IEncapsulatedOpenBISService service)
    {
        this(properties, ETLDaemon.getIdsOfIncomingShares(), service, ServiceProvider.getShareIdManager(),
                new SimpleFreeSpaceProvider(), new DataSetMover(service,
                        ServiceProvider.getShareIdManager()), new Log4jSimpleLogger(operationLog));
    }

    EagerShufflingTask(Properties properties, Set<String> incomingShares, IEncapsulatedOpenBISService service,
            IShareIdManager shareIdManager, IFreeSpaceProvider freeSpaceProvider,
            IDataSetMover dataSetMover, ISimpleLogger logger)
    {
        super(properties, service);
        this.incomingShares = incomingShares;
        this.shareIdManager = shareIdManager;
        this.freeSpaceProvider = freeSpaceProvider;
        this.dataSetMover = dataSetMover;
        this.logger = logger;
        dataStoreCode =
                PropertyUtils.getMandatoryProperty(properties,
                        DssPropertyParametersUtil.DSS_CODE_KEY).toUpperCase();
        storeRoot =
                new File(PropertyUtils.getMandatoryProperty(properties,
                        DssPropertyParametersUtil.STOREROOT_DIR_KEY));
        if (storeRoot.isDirectory() == false)
        {
            throw new ConfigurationFailureException(
                    "Store root does not exists or is not a directory: "
                            + storeRoot.getAbsolutePath());
        }
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
                SegmentedStoreUtils.getDataSetsPerShare(storeRoot, dataStoreCode,
                        freeSpaceProvider, service, logger);
            dataSet = findDataSet(shares, dataSetCode);
            Share incomingShareWithMostFree = null;
            long incomingMaxFreeSpace = 0;
            Share extensionShareWithMostFree = null;
            long extensionsMaxFreeSpace = 0;
            for (Share share : shares)
            {
                long freeSpace = share.calculateFreeSpace();
                String shareId = share.getShareId();
                if (dataSet.getDataSetShareId().equals(shareId))
                {
                    continue;
                }
                if (incomingShares.contains(shareId))
                {
                    if (freeSpace > incomingMaxFreeSpace)
                    {
                        incomingMaxFreeSpace = freeSpace;
                        incomingShareWithMostFree = share;
                    }
                } else
                {
                    if (freeSpace > extensionsMaxFreeSpace)
                    {
                        extensionsMaxFreeSpace = freeSpace;
                        extensionShareWithMostFree = share;
                    }
                }
            }
            if (extensionShareWithMostFree != null)
            {
                shareWithMostFreeOrNull = extensionShareWithMostFree;
            } else if (incomingShareWithMostFree != null)
            {
                shareWithMostFreeOrNull = incomingShareWithMostFree;
            }
            if (shareWithMostFreeOrNull == null)
            {
                return new NoCleanupTask();
            }
            return new NoCleanupTask();
            // TODO, 2011-03-28, FJE: A better CleanupTask class is needed because
            // I'm not 100% sure that the data set might be delete in the new share even though 
            // shuffling is almost finished. 
            // The worst case is that the data set is deleted in both shares.
//            return new CleanupTask(dataSet, storeRoot, shareWithMostFreeOrNull.getShareId());
        }
        
        public void execute()
        {
            if (shareWithMostFreeOrNull != null)
            {
                File share = new File(storeRoot, shareIdManager.getShareId(dataSetCode));
                dataSetMover.moveDataSetToAnotherShare(
                        new File(share, dataSet.getDataSetLocation()),
                        shareWithMostFreeOrNull.getShare(), logger);
            }
        }
        
        
    }
    
//    private static final class CleanupTask implements ICleanupTask
//    {
//        private static final long serialVersionUID = 1L;
//
//        private final SimpleDataSetInformationDTO dataSet;
//        private final File storeRoot;
//        private final String newShareId;
//        
//        CleanupTask(SimpleDataSetInformationDTO dataSet, File storeRoot, String newShareId)
//        {
//            this.dataSet = dataSet;
//            this.storeRoot = storeRoot;
//            this.newShareId = newShareId;
//        }
//
//        public void cleanup()
//        {
//            IShareIdManager shareIdManager = ServiceProvider.getShareIdManager();
//            String currentShareId =
//                    shareIdManager.getShareId(dataSet.getDataSetCode());
//            if (currentShareId.equals(newShareId) == false)
//            {
//                File dataSetFolder =
//                        new File(new File(storeRoot, newShareId), dataSet.getDataSetLocation());
//                FileUtilities.deleteRecursively(dataSetFolder);
//            }
//        }
//    }

}
