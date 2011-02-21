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


import static org.apache.commons.io.FileUtils.ONE_MB;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses=SimpleShuffling.class)
public class SimpleShufflingTest extends AbstractFileSystemTestCase
{
    private static final String STORE_PATH = "01/02/03/";

    private static final class MockSpaceProvider implements IFreeSpaceProvider
    {
        private final Map<File, List<Long>> freeSpace = new HashMap<File, List<Long>>();
        
        void addFreeSpaceExpectationFor(Share share, long valueInKb)
        {
            List<Long> list = freeSpace.get(share.getShare());
            if (list == null)
            {
                list = new ArrayList<Long>();
                freeSpace.put(share.getShare(), list);
            }
            list.add(valueInKb);
        }
        
        @SuppressWarnings("null")
        public long freeSpaceKb(HostAwareFile path) throws IOException
        {
            File file = path.getFile();
            List<Long> list = freeSpace.get(file);
            assertTrue("Unknown file " + file, list != null);
            assertFalse("Unexpected invocation for file " + file, list.isEmpty());
            return list.remove(0);
        }
        
        void assertIsSatiesfied()
        {
            Set<Entry<File, List<Long>>> entrySet = freeSpace.entrySet();
            for (Entry<File, List<Long>> entry : entrySet)
            {
                assertEquals("Unfullfilled free space assertions for " + entry.getKey().getPath(),
                        0, entry.getValue().size());
            }
        }
    }
    
    private MockSpaceProvider spaceProvider;
    private Mockery context;
    private IEncapsulatedOpenBISService service;
    private IDataSetMover dataSetMover;
    private ISimpleLogger logger;
    private SimpleShuffling balancer;
    private File store;

    @BeforeMethod
    public void beforeMethod()
    {
        spaceProvider = new MockSpaceProvider();
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        dataSetMover = context.mock(IDataSetMover.class);
        logger = context.mock(ISimpleLogger.class);
        final ITimeProvider timeProvider = context.mock(ITimeProvider.class);
        context.checking(new Expectations()
            {
                {
                    allowing(timeProvider).getTimeInMilliseconds();
                    will(returnValue(0L));
                }
            });
        Properties properties = new Properties();
        properties.setProperty(SimpleShuffling.MINIMUM_FREE_SPACE_KEY, "2");
        balancer = new SimpleShuffling(properties, timeProvider);
        store = new File(workingDirectory, "store");
    }
    
    @AfterMethod
    public void afterMethod()
    {
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void test()
    {
        final Share share1 = new Share(new File(store, "1"), spaceProvider);
        share1.addDataSet(dataSet("ds1", 2000));
        share1.addDataSet(dataSet("ds2", ONE_MB));
        share1.addDataSet(dataSet("ds3", ONE_MB + 100));
        spaceProvider.addFreeSpaceExpectationFor(share1, 100l);
        spaceProvider.addFreeSpaceExpectationFor(share1, 100l);
        spaceProvider.addFreeSpaceExpectationFor(share1, 1100l);
        spaceProvider.addFreeSpaceExpectationFor(share1, 2100l);
        final Share share2 = new Share(new File(store, "2"), spaceProvider);
        share2.addDataSet(dataSet("ds4", 2 * ONE_MB));
        spaceProvider.addFreeSpaceExpectationFor(share2, 500l);
        spaceProvider.addFreeSpaceExpectationFor(share2, 500l);
        final Share share3 = new Share(new File(store, "3"), spaceProvider);
        spaceProvider.addFreeSpaceExpectationFor(share3, 4 * 1024l);
        spaceProvider.addFreeSpaceExpectationFor(share3, 3 * 1024l);
        spaceProvider.addFreeSpaceExpectationFor(share3, 1024l);
        final Share share4 = new Share(new File(store, "4"), spaceProvider);
        spaceProvider.addFreeSpaceExpectationFor(share4, 3 * 1024l);
        context.checking(new Expectations()
            {
                {
                    one(logger).log(LogLevel.INFO, "BEGIN Computing number of data sets to move for share 1");
                    one(logger).log(LogLevel.INFO, "\tSpace needed to free: 1994752 bytes (1948.00 kB, 1.90 MB)");
                    one(logger).log(LogLevel.INFO, "\tInspecting 3 data sets.");
                    one(logger).log(LogLevel.INFO, "END Computing number of data sets to move for share 1");
                    one(logger).log(LogLevel.INFO, "\t2 data sets to move, available space : 102500");
                    
                    one(logger).log(LogLevel.INFO, "Moving data set ds3 from share 1 to share 3 ...");
                    one(dataSetMover).moveDataSetToAnotherShare(new File(share1.getShare(), STORE_PATH + "ds3"), share3.getShare());
                    one(logger).log(LogLevel.INFO, "Moving data set ds3 from share 1 to share 3 took 0 seconds.");
                    
                    one(logger).log(LogLevel.INFO, "Moving data set ds2 from share 1 to share 3 ...");
                    one(dataSetMover).moveDataSetToAnotherShare(new File(share1.getShare(), STORE_PATH + "ds2"), share3.getShare());
                    one(logger).log(LogLevel.INFO, "Moving data set ds2 from share 1 to share 3 took 0 seconds.");
                    
                    one(logger).log(LogLevel.INFO, "BEGIN Computing number of data sets to move for share 2");
                    one(logger).log(LogLevel.INFO, "\tSpace needed to free: 1585152 bytes (1548.00 kB, 1.51 MB)");
                    one(logger).log(LogLevel.INFO, "\tInspecting 1 data sets.");
                    one(logger).log(LogLevel.INFO, "END Computing number of data sets to move for share 2");
                }
            });
        
        balancer.shuffleDataSets(Arrays.asList(share1, share2),
                Arrays.asList(share1, share2, share3, share4), service, dataSetMover, logger);
        
        assertEquals(1, share1.getDataSetsOrderedBySize().size());
        assertEquals(1, share2.getDataSetsOrderedBySize().size());
        assertEquals("ds3", share3.getDataSetsOrderedBySize().get(0).getDataSetCode());
        assertEquals("ds2", share3.getDataSetsOrderedBySize().get(1).getDataSetCode());
        assertEquals(2, share3.getDataSetsOrderedBySize().size());
        assertEquals(0, share4.getDataSetsOrderedBySize().size());
        spaceProvider.assertIsSatiesfied();
        context.assertIsSatisfied();
    }
    
    private SimpleDataSetInformationDTO dataSet(String code, long size)
    {
        SimpleDataSetInformationDTO dataSet = new SimpleDataSetInformationDTO();
        dataSet.setDataSetCode(code);
        dataSet.setDataSetLocation(STORE_PATH + code);
        dataSet.setDataSetSize(size);
        return dataSet;
    }
}
