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
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.etlserver.plugins.IDataSetMover;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses=EagerShufflingTask.class)
public class EagerShufflingTaskTest extends AbstractFileSystemTestCase
{
    private static final String SHARDING = "sharding/";
    private static final String DATA_STORE_SERVER_CODE = "DSS";
    private static final String DATA_SET_CODE1 = "ds-1";
    private BufferedAppender logRecorder;
    private Mockery context;

    private IEncapsulatedOpenBISService service;
    private IShareIdManager shareIdManager;
    private IFreeSpaceProvider freeSpaceProvider;
    private IDataSetMover dataSetMover;

    private IConfigProvider configProvider;
    private ISimpleLogger logger;
    private File store;
    private File share1;
    private File share2;
    private File share3;
    private File share4;
    private File ds1File;

    @BeforeMethod
    public void beforeMethod()
    {
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.INFO);
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        shareIdManager = context.mock(IShareIdManager.class);
        freeSpaceProvider = context.mock(IFreeSpaceProvider.class);
        configProvider = context.mock(IConfigProvider.class);
        dataSetMover = context.mock(IDataSetMover.class);
        logger = context.mock(ISimpleLogger.class);
        store = new File(workingDirectory.getAbsolutePath(), "store");
        store.mkdirs();
        share1 = new File(store, "1");
        share1.mkdir();
        new File(share1, SHARDING).mkdirs();
        ds1File = new File(share1, SHARDING + DATA_SET_CODE1);
        FileUtilities.writeToFile(ds1File, "hello data set 1");
        share2 = new File(store, "2");
        share2.mkdir();
        share3 = new File(store, "3");
        share3.mkdir();
        share4 = new File(store, "4");
        share4.mkdir();
    }
    
    @AfterMethod(alwaysRun = true)
    public void afterMethod()
    {
        logRecorder.reset();
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testShufflingIntoAnExtensionShare()
    {
        prepareConfigProvider();
        EagerShufflingTask task = createTask();
        RecordingMatcher<HostAwareFile> hostAwareFileMatcher =
                new RecordingMatcher<HostAwareFile>();
        prepareFreeSpaceProvider(hostAwareFileMatcher, 200, 100, 300, 400);
        prepareListDataSets();
        prepareGetShareId();
        RecordingMatcher<String> infoMessageMatcher = prepareLogging(LogLevel.INFO);
        context.checking(new Expectations()
            {
                {
                    one(dataSetMover).moveDataSetToAnotherShare(ds1File, share4, logger);
                }
            });
        
        IPostRegistrationTaskExecutor executor = task.createExecutor(DATA_SET_CODE1);
        executor.createCleanupTask();
        executor.execute();
        
        assertEquals("Data set ds-1 successfully moved from share 1 to 4.",
                infoMessageMatcher.recordedObject());
        assertHostAwareFile(hostAwareFileMatcher);
        context.assertIsSatisfied();
    }

    @Test
    public void testShufflingIntoAnotherIncomingShare()
    {
        prepareConfigProvider();
        EagerShufflingTask task = createTask();
        RecordingMatcher<HostAwareFile> hostAwareFileMatcher =
                new RecordingMatcher<HostAwareFile>();
        prepareFreeSpaceProvider(hostAwareFileMatcher, 100, 200, 10, 0);
        prepareListDataSets();
        prepareGetShareId();
        RecordingMatcher<String> infoMessageMatcher = prepareLogging(LogLevel.INFO);
        context.checking(new Expectations()
            {
                {
                    one(dataSetMover).moveDataSetToAnotherShare(ds1File, share2, logger);
                }
            });

        IPostRegistrationTaskExecutor executor = task.createExecutor(DATA_SET_CODE1);
        executor.createCleanupTask();
        executor.execute();

        assertEquals("Data set ds-1 successfully moved from share 1 to 2.",
                infoMessageMatcher.recordedObject());
        assertHostAwareFile(hostAwareFileMatcher);
        context.assertIsSatisfied();
    }
    
    @Test
    public void testShufflingButNoShareFoundExceptTheOwnOne()
    {
        prepareConfigProvider();
        EagerShufflingTask task = createTask();
        RecordingMatcher<HostAwareFile> hostAwareFileMatcher =
                new RecordingMatcher<HostAwareFile>();
        prepareFreeSpaceProvider(hostAwareFileMatcher, 200, 10, 10, 0);
        prepareListDataSets();

        RecordingMatcher<String> logMessageMatcher = prepareLogging(LogLevel.WARN);
        
        IPostRegistrationTaskExecutor executor = task.createExecutor(DATA_SET_CODE1);
        executor.createCleanupTask();
        executor.execute();
        
        assertEquals("No share found for shuffling data set ds-1.",
                logMessageMatcher.recordedObject());
        assertHostAwareFile(hostAwareFileMatcher);
        context.assertIsSatisfied();
    }

    private RecordingMatcher<String> prepareLogging(final LogLevel level)
    {
        final RecordingMatcher<String> logMessageMatcher = new RecordingMatcher<String>();
        context.checking(new Expectations()
            {
                {
                    one(logger).log(with(level), with(logMessageMatcher));
                }
            });
        return logMessageMatcher;
    }

    private void prepareListDataSets()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).listDataSets();
                    will(returnValue(Arrays.asList(dataSet("1", DATA_SET_CODE1))));
                }
            });
    }

    private void prepareConfigProvider()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(configProvider).getDataStoreCode();
                    will(returnValue(DATA_STORE_SERVER_CODE));
                    allowing(configProvider).getStoreRoot();
                    will(returnValue(store));
                }
            });
    }

    private void prepareGetShareId()
    {
        context.checking(new Expectations()
        {
            {
                one(shareIdManager).getShareId(DATA_SET_CODE1);
                will(returnValue("1"));
            }
        });
    }
    
    private void prepareFreeSpaceProvider(
            final RecordingMatcher<HostAwareFile> hostAwareFileMatcher, final long free1,
            final long free2, final long free3, final long free4)
    {
        final Sequence sequence = context.sequence("free");
        context.checking(new Expectations()
            {
                {
                    try
                    {
                        one(freeSpaceProvider).freeSpaceKb(with(hostAwareFileMatcher));
                        will(returnValue(free1));
                        inSequence(sequence);
                        one(freeSpaceProvider).freeSpaceKb(with(hostAwareFileMatcher));
                        will(returnValue(free2));
                        inSequence(sequence);
                        one(freeSpaceProvider).freeSpaceKb(with(hostAwareFileMatcher));
                        will(returnValue(free3));
                        inSequence(sequence);
                        one(freeSpaceProvider).freeSpaceKb(with(hostAwareFileMatcher));
                        will(returnValue(free4));
                        inSequence(sequence);
                    } catch (IOException ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }
                }
            });
    }
    
    private void assertHostAwareFile(final RecordingMatcher<HostAwareFile> hostAwareFileMatcher)
    {
        List<HostAwareFile> files = hostAwareFileMatcher.getRecordedObjects();
        assertEquals(share1, files.get(0).getFile());
        assertEquals(share2, files.get(1).getFile());
        assertEquals(share3, files.get(2).getFile());
        assertEquals(share4, files.get(3).getFile());
        assertEquals(4, files.size());
    }

    private SimpleDataSetInformationDTO dataSet(String shareId, String dataSetCode)
    {
        SimpleDataSetInformationDTO dataSet = new SimpleDataSetInformationDTO();
        dataSet.setDataSetCode(dataSetCode);
        dataSet.setDataSetShareId(shareId);
        dataSet.setDataStoreCode(DATA_STORE_SERVER_CODE);
        dataSet.setDataSetLocation(SHARDING + dataSetCode);
        dataSet.setDataSetSize(47110L);
        return dataSet;
    }

    private EagerShufflingTask createTask()
    {
        Properties properties = new Properties();
        properties.setProperty(EagerShufflingTask.SHARE_FINDER_KEY + ".class",
                SimpleShareFinder.class.getName());
        return createTask(properties);
    }

    private EagerShufflingTask createTask(Properties properties)
    {
        return new EagerShufflingTask(properties,
                new LinkedHashSet<String>(Arrays.asList("1", "2")), service, shareIdManager,
                freeSpaceProvider, dataSetMover, configProvider, logger);
    }
    
}
