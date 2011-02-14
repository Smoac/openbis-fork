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

import static ch.systemsx.cisd.etlserver.plugins.SegmentedStoreBalancingTask.BALANCER_SECTION_NAME;
import static ch.systemsx.cisd.etlserver.plugins.SegmentedStoreBalancingTask.CLASS_PROPERTY_NAME;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses=SegmentedStoreBalancingTask.class)
public class SegmentedStoreBalancingTaskTest extends AbstractFileSystemTestCase
{
    public static final class Balancer implements ISegmentedStoreBalancer
    {
        private final Properties properties;
        private List<Share> shares;
        private IEncapsulatedOpenBISService service;
        private IDataSetMover dataSetMover;
        private ISimpleLogger logger;
        
        public Balancer(Properties properties)
        {
            this.properties = properties;
        }

        public void balanceStore(List<Share> list, IEncapsulatedOpenBISService openBisService,
                IDataSetMover mover, ISimpleLogger simpleLogger)
        {
            shares = list;
            service = openBisService;
            dataSetMover = mover;
            logger = simpleLogger;
        }
    }
    
    private Mockery context;
    private IEncapsulatedOpenBISService service;
    private IFreeSpaceProvider spaceProvider;
    private IDataSetMover dataSetMover;
    private ISimpleLogger logger;
    private SegmentedStoreBalancingTask balancerTask;
    private File storeRoot;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        spaceProvider = context.mock(IFreeSpaceProvider.class);
        dataSetMover = context.mock(IDataSetMover.class);
        logger = context.mock(ISimpleLogger.class);
        balancerTask = new SegmentedStoreBalancingTask(service, spaceProvider, dataSetMover, logger);
        storeRoot = new File(workingDirectory, "store");
        storeRoot.mkdirs();
    }
    
    @AfterMethod
    public void afterMethod()
    {
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testExecute()
    {
        Properties properties = new Properties();
        properties.setProperty(DssPropertyParametersUtil.DSS_CODE_KEY, "data-store-1");
        properties.setProperty(DssPropertyParametersUtil.STOREROOT_DIR_KEY, storeRoot.getPath());
        properties.setProperty(BALANCER_SECTION_NAME + "." + CLASS_PROPERTY_NAME,
                SegmentedStoreBalancingTaskTest.Balancer.class.getName());
        balancerTask.setUp("mock-balancer", properties);
        context.checking(new Expectations()
            {
                {
                    one(service).listDataSets();
                    SimpleDataSetInformationDTO ds = new SimpleDataSetInformationDTO();
                    ds.setDataStoreCode("other data store");
                    will(returnValue(Arrays.asList(ds)));
                }
            });
        
        balancerTask.execute();
        
        Balancer balancer = (SegmentedStoreBalancingTaskTest.Balancer) balancerTask.balancer;
        assertEquals("{class=" + balancer.getClass().getName() + "}",
                balancer.properties.toString());
        assertEquals("[]", balancer.shares.toString());
        assertSame(service, balancer.service);
        assertSame(dataSetMover, balancer.dataSetMover);
        assertSame(logger, balancer.logger);
        context.assertIsSatisfied();
    }
    
    @Test
    public void testDefaultBalancer()
    {
        Properties properties = new Properties();
        properties.setProperty(DssPropertyParametersUtil.DSS_CODE_KEY, "data-store-1");
        properties.setProperty(DssPropertyParametersUtil.STOREROOT_DIR_KEY, storeRoot.getPath());
        balancerTask.setUp("mock-balancer", properties);
        context.checking(new Expectations()
            {
                {
                    one(service).listDataSets();
                    will(returnValue(Arrays.asList()));
                    
                    one(logger).log(LogLevel.INFO, "Data Store Shares:");
                }
            });

        balancerTask.execute();
        
        context.assertIsSatisfied();
    }
}
