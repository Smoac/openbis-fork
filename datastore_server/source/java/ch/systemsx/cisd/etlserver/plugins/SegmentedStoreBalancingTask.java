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

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
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

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SegmentedStoreBalancingTask implements IMaintenanceTask
{
    private static final String BALANCER_SECTION_NAME = "balancer";
    private static final String CLASS_PROPERTY_NAME = "class";
    
    private static final Logger operationLog =
        LogFactory.getLogger(LogCategory.OPERATION, SegmentedStoreBalancingTask.class);
    
    private final IEncapsulatedOpenBISService service;
    private final IFreeSpaceProvider freeSpaceProvider;
    private final ISimpleLogger operationLogger;
    
    private File storeRoot;
    private String dataStoreCode;
    private ISegmentedStoreBalancer balancer;
    
    public SegmentedStoreBalancingTask()
    {
        this(ServiceProvider.getOpenBISService(), new SimpleFreeSpaceProvider(),
                new Log4jSimpleLogger(operationLog));
    }

    SegmentedStoreBalancingTask(IEncapsulatedOpenBISService service,
            IFreeSpaceProvider freeSpaceProvider, ISimpleLogger logger)
    {
        LogInitializer.init();
        this.freeSpaceProvider = freeSpaceProvider;
        this.service = service;
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
            return new NonBalancer();
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
        List<Share> shares =
                SegmentedStoreUtils.getDataSetsPerShare(storeRoot, dataStoreCode,
                        freeSpaceProvider, service, operationLogger);
        balancer.balanceStore(shares, service, operationLogger);
    }

}
