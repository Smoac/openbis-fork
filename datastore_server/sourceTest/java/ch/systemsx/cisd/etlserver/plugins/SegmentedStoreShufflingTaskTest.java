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

import static ch.systemsx.cisd.etlserver.plugins.SegmentedStoreShufflingTask.SHUFFLING_SECTION_NAME;
import static ch.systemsx.cisd.etlserver.plugins.SegmentedStoreShufflingTask.CLASS_PROPERTY_NAME;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
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
@Friend(toClasses=SegmentedStoreShufflingTask.class)
public class SegmentedStoreShufflingTaskTest extends AbstractFileSystemTestCase
{
    private static final String DATA_STORE_CODE = "data-store-1";

    public static final class Balancer implements ISegmentedStoreShuffling
    {
        private final Properties properties;
        private List<Share> sourceShares;
        private List<Share> targetShares;
        private IEncapsulatedOpenBISService service;
        private IDataSetMover dataSetMover;
        private ISimpleLogger logger;
        
        public Balancer(Properties properties)
        {
            this.properties = properties;
        }

        public void shuffleDataSets(List<Share> sources, List<Share> targets,
                IEncapsulatedOpenBISService openBisService, IDataSetMover mover,
                ISimpleLogger simpleLogger)
        {
            sourceShares = sources;
            targetShares = targets;
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
    private SegmentedStoreShufflingTask balancerTask;
    private File storeRoot;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        spaceProvider = context.mock(IFreeSpaceProvider.class);
        dataSetMover = context.mock(IDataSetMover.class);
        logger = context.mock(ISimpleLogger.class);
        LinkedHashSet<String> incomingShareIds = new LinkedHashSet<String>(Arrays.asList("1"));
        balancerTask =
                new SegmentedStoreShufflingTask(incomingShareIds, service, spaceProvider,
                        dataSetMover, logger);
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
        properties.setProperty(DssPropertyParametersUtil.DSS_CODE_KEY, DATA_STORE_CODE);
        properties.setProperty(DssPropertyParametersUtil.STOREROOT_DIR_KEY, storeRoot.getPath());
        properties.setProperty(SHUFFLING_SECTION_NAME + "." + CLASS_PROPERTY_NAME,
                SegmentedStoreShufflingTaskTest.Balancer.class.getName());
        balancerTask.setUp("mock-balancer", properties);
        File share1 = new File(storeRoot, "1");
        share1.mkdirs();
        FileUtilities.writeToFile(new File(share1, "ds1"), "hello ds1");
        File share2 = new File(storeRoot, "2");
        share2.mkdirs();
        FileUtilities.writeToFile(new File(share2, "ds2"), "hello ds2");
        context.checking(new Expectations()
            {
                {
                    one(service).listDataSets();
                    SimpleDataSetInformationDTO ds1 = new SimpleDataSetInformationDTO();
                    ds1.setDataStoreCode(DATA_STORE_CODE);
                    ds1.setDataSetShareId("1");
                    ds1.setDataSetSize(10l);
                    ds1.setDataSetLocation("ds1");
                    SimpleDataSetInformationDTO ds2 = new SimpleDataSetInformationDTO();
                    ds2.setDataStoreCode(DATA_STORE_CODE);
                    ds2.setDataSetShareId("2");
                    ds2.setDataSetSize(20l);
                    ds2.setDataSetLocation("ds2");
                    SimpleDataSetInformationDTO ds3 = new SimpleDataSetInformationDTO();
                    ds3.setDataStoreCode("other data store");
                    will(returnValue(Arrays.asList(ds1, ds2, ds3)));
                }
            });
        
        balancerTask.execute();
        
        Balancer balancer = (SegmentedStoreShufflingTaskTest.Balancer) balancerTask.shuffling;
        assertEquals("{class=" + balancer.getClass().getName() + "}",
                balancer.properties.toString());
        assertEquals("1", balancer.sourceShares.get(0).getShareId());
        assertEquals(1, balancer.sourceShares.size());
        assertEquals("1", balancer.targetShares.get(0).getShareId());
        assertEquals("2", balancer.targetShares.get(1).getShareId());
        assertEquals(2, balancer.targetShares.size());
        assertSame(service, balancer.service);
        assertSame(dataSetMover, balancer.dataSetMover);
        assertSame(logger, balancer.logger);
        context.assertIsSatisfied();
    }
    
    @Test
    public void testDefaultBalancer()
    {
        Properties properties = new Properties();
        properties.setProperty(DssPropertyParametersUtil.DSS_CODE_KEY, DATA_STORE_CODE);
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
