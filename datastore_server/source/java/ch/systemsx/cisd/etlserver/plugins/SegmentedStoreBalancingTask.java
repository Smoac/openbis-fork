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
import java.util.List;
import java.util.Properties;

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
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Maintenance task which tries to balance a segmented store.
 *
 * @author Franz-Josef Elmer
 */
public class SegmentedStoreBalancingTask implements IMaintenanceTask
{
    private static final ISegmentedStoreBalancer DUMMY_BALANCER = new ISegmentedStoreBalancer()
        {
            private static final int N = 3;

            public void balanceStore(List<Share> shares, IEncapsulatedOpenBISService service,
                    IDataSetMover dataSetMover, ISimpleLogger logger)
            {
                logger.log(INFO, "Data Store Shares:");
                for (Share share : shares)
                {
                    List<SimpleDataSetInformationDTO> dataSets = share.getDataSetsOrderedBySize();
                    logger.log(
                            INFO,
                            "   Share "
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

    @Private static final String BALANCER_SECTION_NAME = "balancer";
    @Private static final String CLASS_PROPERTY_NAME = "class";
    
    private static final Logger operationLog =
        LogFactory.getLogger(LogCategory.OPERATION, SegmentedStoreBalancingTask.class);
    
    private final IEncapsulatedOpenBISService service;
    private final IDataSetMover dataSetMover;
    private final IFreeSpaceProvider freeSpaceProvider;
    private final ISimpleLogger operationLogger;
    
    private File storeRoot;
    private String dataStoreCode;
    @Private ISegmentedStoreBalancer balancer;
    
    public SegmentedStoreBalancingTask()
    {
        this(ServiceProvider.getOpenBISService(), new SimpleFreeSpaceProvider(),
                new IDataSetMover()
                    {

                        public void moveDataSetToAnotherShare(File dataSetDirInStore, File share)
                        {
                            SegmentedStoreUtils.moveDataSetToAnotherShare(dataSetDirInStore, share,
                                    ServiceProvider.getOpenBISService());
                        }
                    }, new Log4jSimpleLogger(operationLog));
    }

    SegmentedStoreBalancingTask(final IEncapsulatedOpenBISService service,
            IFreeSpaceProvider freeSpaceProvider, IDataSetMover dataSetMover, ISimpleLogger logger)
    {
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
                        DssPropertyParametersUtil.DSS_CODE_KEY);
        storeRoot =
                new File(PropertyUtils.getMandatoryProperty(properties,
                        DssPropertyParametersUtil.STOREROOT_DIR_KEY));
        if (storeRoot.isDirectory() == false)
        {
            throw new ConfigurationFailureException(
                    "Store root does not exists or is not a directory: "
                            + storeRoot.getAbsolutePath());
        }
        balancer = createBalancer(properties);
        operationLog.info("Plugin '" + pluginName + "' initialized: balancer: "
                + balancer.getClass().getName() + ", data store code: " + dataStoreCode
                + ", data store root: " + storeRoot.getAbsolutePath());
    }
    
    private ISegmentedStoreBalancer createBalancer(Properties properties)
    {
        Properties balancerProps =
                PropertyParametersUtil.extractSingleSectionProperties(properties,
                        BALANCER_SECTION_NAME, false).getProperties();
        String className = balancerProps.getProperty(CLASS_PROPERTY_NAME);
        if (className == null)
        {
            return DUMMY_BALANCER;
        }
        try
        {
            return ClassUtils.create(ISegmentedStoreBalancer.class, className, balancerProps);
        } catch (ConfigurationFailureException ex)
        {
            throw ex;
        } catch (Exception ex)
        {
            throw new ConfigurationFailureException("Cannot find balancer class '" + className
                    + "'", CheckedExceptionTunnel.unwrapIfNecessary(ex));
        }
    }

    public void execute()
    {
        operationLog.info("Starting segmented store balancing.");
        List<Share> shares =
                SegmentedStoreUtils.getDataSetsPerShare(storeRoot, dataStoreCode,
                        freeSpaceProvider, service, operationLogger);
        balancer.balanceStore(shares, service, dataSetMover, operationLogger);
        operationLog.info("Segmented store balancing finished.");
    }

}
