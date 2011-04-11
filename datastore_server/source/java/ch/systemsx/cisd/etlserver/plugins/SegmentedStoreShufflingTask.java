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

package ch.systemsx.cisd.etlserver.plugins;

import static ch.systemsx.cisd.common.logging.LogLevel.INFO;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.maintenance.IDataStoreLockingMaintenanceTask;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IncomingShareIdProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Maintenance task which shuffles data sets between shares of a segmented store. This task is
 * supposed to prevent incoming shares from having not enough space.
 * 
 * @author Franz-Josef Elmer
 */
public class SegmentedStoreShufflingTask implements IDataStoreLockingMaintenanceTask
{
    private static final ISegmentedStoreShuffling DUMMY_SHUFFLING = new ISegmentedStoreShuffling()
        {
            private static final int N = 3;

            public void init(ISimpleLogger logger)
            {
            }

            public void shuffleDataSets(List<Share> sourceShares, List<Share> targetShares,
                    IEncapsulatedOpenBISService service, IDataSetMover dataSetMover,
                    ISimpleLogger logger)
            {
                logger.log(INFO, "Data Store Shares:");
                for (Share share : targetShares)
                {
                    List<SimpleDataSetInformationDTO> dataSets = share.getDataSetsOrderedBySize();
                    logger.log(
                            INFO,
                            "   "
                                    + (share.isIncoming() ? "Incoming" : "External")
                                    + " share "
                                    + share.getShareId()
                                    + " (free space: "
                                    + FileUtils.byteCountToDisplaySize(share.calculateFreeSpace())
                                    + ") has "
                                    + dataSets.size()
                                    + " data sets occupying "
                                    + FileUtilities.byteCountToDisplaySize(share
                                            .getTotalSizeOfDataSets()) + ".");
                    for (int i = 0, n = Math.min(N, dataSets.size()); i < n; i++)
                    {
                        SimpleDataSetInformationDTO dataSet = dataSets.get(i);
                        logger.log(
                                INFO,
                                "      "
                                        + dataSet.getDataSetCode()
                                        + " "
                                        + FileUtilities.byteCountToDisplaySize(dataSet
                                                .getDataSetSize()));
                    }
                    if (dataSets.size() > N)
                    {
                        logger.log(INFO, "      ...");
                    }
                }
            }
        };

    @Private
    static final String SHUFFLING_SECTION_NAME = "shuffling";

    @Private
    static final String CLASS_PROPERTY_NAME = "class";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            SegmentedStoreShufflingTask.class);

    private final Set<String> incomingShares;

    private final IEncapsulatedOpenBISService service;

    private final IDataSetMover dataSetMover;

    private final IFreeSpaceProvider freeSpaceProvider;

    private final ISimpleLogger operationLogger;

    private File storeRoot;

    private String dataStoreCode;

    @Private
    ISegmentedStoreShuffling shuffling;

    public SegmentedStoreShufflingTask()
    {
        this(IncomingShareIdProvider.getIdsOfIncomingShares(), ServiceProvider.getOpenBISService(),
                new SimpleFreeSpaceProvider(), new DataSetMover(
                        ServiceProvider.getOpenBISService(), ServiceProvider.getShareIdManager()),
                new Log4jSimpleLogger(operationLog));
    }

    SegmentedStoreShufflingTask(Set<String> incomingShares, IEncapsulatedOpenBISService service,
            IFreeSpaceProvider freeSpaceProvider, IDataSetMover dataSetMover, ISimpleLogger logger)
    {
        this.incomingShares = incomingShares;
        LogInitializer.init();
        this.freeSpaceProvider = freeSpaceProvider;
        this.service = service;
        this.dataSetMover = dataSetMover;
        operationLogger = logger;
    }

    public void setUp(String pluginName, Properties properties)
    {
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
        shuffling = createShuffling(properties);
        shuffling.init(operationLogger);
        operationLog.info("Plugin '" + pluginName + "' initialized: shuffling strategy: "
                + shuffling.getClass().getName() + ", data store code: " + dataStoreCode
                + ", data store root: " + storeRoot.getAbsolutePath() + ", incoming shares: "
                + incomingShares);
    }

    private ISegmentedStoreShuffling createShuffling(Properties properties)
    {
        Properties shufflingProps =
                PropertyParametersUtil.extractSingleSectionProperties(properties,
                        SHUFFLING_SECTION_NAME, false).getProperties();
        String className = shufflingProps.getProperty(CLASS_PROPERTY_NAME);
        if (className == null)
        {
            return DUMMY_SHUFFLING;
        }
        try
        {
            return ClassUtils.create(ISegmentedStoreShuffling.class, className, shufflingProps);
        } catch (ConfigurationFailureException ex)
        {
            throw ex;
        } catch (Exception ex)
        {
            throw new ConfigurationFailureException("Cannot find shuffling class '" + className
                    + "'", CheckedExceptionTunnel.unwrapIfNecessary(ex));
        }
    }

    public void execute()
    {
        operationLog.info("Starting segmented store shuffling.");
        List<Share> shares =
                SegmentedStoreUtils.getDataSetsPerShare(storeRoot, dataStoreCode,
                        Collections.<String> emptySet(), freeSpaceProvider, service,
                        operationLogger);
        List<Share> sourceShares = new ArrayList<Share>();
        for (Share share : shares)
        {
            if (incomingShares.contains(share.getShareId()))
            {
                sourceShares.add(share);
            }
        }
        shuffling.shuffleDataSets(sourceShares, shares, service, dataSetMover, operationLogger);
        operationLog.info("Segmented store shuffling finished.");
    }

    /**
     * @see IDataStoreLockingMaintenanceTask#requiresDataStoreLock()
     */
    public boolean requiresDataStoreLock()
    {
        return true;
    }

}
