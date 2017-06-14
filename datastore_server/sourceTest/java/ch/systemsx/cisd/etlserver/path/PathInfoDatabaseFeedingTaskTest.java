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

import static ch.systemsx.cisd.common.action.IDelegatedAction.DO_NOTHING;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.MockTimeProvider;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.IHierarchicalContentFactory;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = PathInfoDatabaseFeedingTask.class)
public class PathInfoDatabaseFeedingTaskTest extends AbstractFileSystemTestCase
{
    private static final String DATA_SET_CODE = "ds1";

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private IDataSetDirectoryProvider directoryProvider;

    private IShareIdManager shareIdManager;

    private IPathsInfoDAO dao;

    private IHierarchicalContentFactory contentFactory;

    private IHierarchicalContentNode node;

    private IHierarchicalContent content;

    private PathInfoDatabaseFeedingTask task;

    private File dataSetFolder;

    @BeforeMethod
    public void beforeMethod()
    {
        LogInitializer.init();
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        directoryProvider = context.mock(IDataSetDirectoryProvider.class);
        shareIdManager = context.mock(IShareIdManager.class);
        context.checking(new Expectations()
            {
                {
                    allowing(directoryProvider).getShareIdManager();
                    will(returnValue(shareIdManager));
                }
            });
        dao = context.mock(IPathsInfoDAO.class);
        contentFactory = context.mock(IHierarchicalContentFactory.class);
        content = context.mock(IHierarchicalContent.class);
        node = context.mock(IHierarchicalContentNode.class);
        task = createTask(0, 0, 0);
        dataSetFolder = new File(workingDirectory, "ds1");
        dataSetFolder.mkdirs();
    }

    @AfterMethod
    public void tearDown(Method method)
    {
        try
        {
            context.assertIsSatisfied();
        } catch (Throwable t)
        {
            // assert expectations were met, including the name of the failed method
            throw new Error(method.getName() + "() : ", t);
        }
    }

    @Test
    public void testAsMaintenanceTask()
    {
        final SimpleDataSetInformationDTO ds1 = new SimpleDataSetInformationDTO();
        ds1.setDataSetCode("ds1");
        ds1.setDataSetLocation("abc1");
        ds1.setRegistrationTimestamp(new Date(78000));
        ds1.setStorageConfirmed(true);
        final SimpleDataSetInformationDTO ds2 = new SimpleDataSetInformationDTO();
        ds2.setDataSetCode("ds2");
        ds2.setDataSetLocation("abc2");
        ds2.setRegistrationTimestamp(new Date(79000));
        ds2.setStorageConfirmed(true);
        context.checking(new Expectations()
            {
                {
                    one(dao).getRegistrationTimestampOfLastFeedingEvent();
                    will(returnValue(null));

                    one(service).listOldestPhysicalDataSets(12);
                    will(returnValue(Arrays.asList(ds1, ds2)));
                }
            });
        prepareHappyCase(ds1);
        prepareFailing(ds2);
        prepareCreateLastFeedingEvent(ds2.getRegistrationTimestamp());

        createTask(12, 3, 0).execute();
    }

    @Test
    public void testNonConfirmed()
    {
        final SimpleDataSetInformationDTO ds1NonConfirmed = new SimpleDataSetInformationDTO();
        ds1NonConfirmed.setDataSetCode("ds1");
        ds1NonConfirmed.setDataSetLocation("abc1");
        ds1NonConfirmed.setRegistrationTimestamp(new Date(78000));
        ds1NonConfirmed.setStorageConfirmed(false);

        final SimpleDataSetInformationDTO ds1Confirmed = new SimpleDataSetInformationDTO();
        ds1Confirmed.setDataSetCode("ds1");
        ds1Confirmed.setDataSetLocation("abc1");
        ds1Confirmed.setRegistrationTimestamp(new Date(78000));
        ds1Confirmed.setStorageConfirmed(true);

        context.checking(new Expectations()
            {
                {
                    allowing(dao).getRegistrationTimestampOfLastFeedingEvent();
                    will(returnValue(null));

                    one(service).listOldestPhysicalDataSets(12);
                    will(returnValue(Arrays.asList(ds1NonConfirmed)));

                    one(service).listOldestPhysicalDataSets(12);
                    will(returnValue(Arrays.asList(ds1Confirmed)));

                }
            });

        prepareHappyCase(ds1Confirmed);
        prepareCreateLastFeedingEvent(ds1Confirmed.getRegistrationTimestamp());

        createTask(12, 3, 0).execute();
    }

    @Test
    public void testAsMaintenanceTaskWithFiniteNumberOfChunks()
    {
        final SimpleDataSetInformationDTO ds1 = dataSet(1000);
        final SimpleDataSetInformationDTO ds2 = dataSet(2000);
        final SimpleDataSetInformationDTO ds3 = dataSet(3000);
        final SimpleDataSetInformationDTO ds4 = dataSet(4000);
        final SimpleDataSetInformationDTO ds5 = dataSet(5000);
        final SimpleDataSetInformationDTO ds6 = dataSet(6000);
        final Sequence chunkReadingSequence = context.sequence("chunkReadingSequence");
        context.checking(new Expectations()
            {
                {
                    one(dao).getRegistrationTimestampOfLastFeedingEvent();
                    will(returnValue(null));
                    inSequence(chunkReadingSequence);

                    one(service).listOldestPhysicalDataSets(2);
                    will(returnValue(Arrays.asList(ds1, ds2)));
                    inSequence(chunkReadingSequence);

                    one(dao).getRegistrationTimestampOfLastFeedingEvent();
                    Date timeStamp = new Date(2000);
                    will(returnValue(timeStamp));
                    inSequence(chunkReadingSequence);

                    one(service).listOldestPhysicalDataSets(timeStamp, 2);
                    will(returnValue(Arrays.asList(ds3, ds4)));
                    inSequence(chunkReadingSequence);

                    one(dao).getRegistrationTimestampOfLastFeedingEvent();
                    Date timeStamp2 = new Date(4000);
                    will(returnValue(timeStamp2));
                    inSequence(chunkReadingSequence);

                    one(service).listOldestPhysicalDataSets(timeStamp2, 2);
                    will(returnValue(Arrays.asList(ds5, ds6)));
                    inSequence(chunkReadingSequence);
                }
            });
        prepareHappyCase(ds1, ds4, ds5);
        prepareFailing(ds2, ds3, ds6);
        prepareCreateLastFeedingEvent(ds2.getRegistrationTimestamp());
        prepareCreateLastFeedingEvent(ds4.getRegistrationTimestamp());
        prepareCreateLastFeedingEvent(ds6.getRegistrationTimestamp());

        createTask(2, 3, 0).execute();
    }

    @Test
    public void testAsMaintenanceTaskWithFiniteTimeLimit()
    {
        final SimpleDataSetInformationDTO ds1 = dataSet(1000);
        final SimpleDataSetInformationDTO ds2 = dataSet(2000);
        final SimpleDataSetInformationDTO ds3 = dataSet(3000);
        final SimpleDataSetInformationDTO ds4 = dataSet(4000);
        final Sequence chunkReadingSequence = context.sequence("chunkReadingSequence");
        context.checking(new Expectations()
            {
                {
                    one(dao).getRegistrationTimestampOfLastFeedingEvent();
                    will(returnValue(null));
                    inSequence(chunkReadingSequence);

                    one(service).listOldestPhysicalDataSets(2);
                    will(returnValue(Arrays.asList(ds1, ds2)));
                    inSequence(chunkReadingSequence);

                    one(dao).getRegistrationTimestampOfLastFeedingEvent();
                    Date timeStamp = new Date(2000);
                    will(returnValue(timeStamp));
                    inSequence(chunkReadingSequence);

                    one(service).listOldestPhysicalDataSets(timeStamp, 2);
                    will(returnValue(Arrays.asList(ds3, ds4)));
                    inSequence(chunkReadingSequence);

                }
            });
        prepareHappyCase(ds1, ds4);
        prepareFailing(ds2, ds3);
        prepareCreateLastFeedingEvent(ds2.getRegistrationTimestamp());
        prepareCreateLastFeedingEvent(ds4.getRegistrationTimestamp());

        createTask(2, 0, 1500).execute();
    }

    @Test
    public void testAsMaintenanceTaskUnlimited()
    {
        final SimpleDataSetInformationDTO ds1 = dataSet(1000);
        final SimpleDataSetInformationDTO ds2 = dataSet(2000);
        final SimpleDataSetInformationDTO ds3 = dataSet(3000);
        final Sequence chunkReadingSequence = context.sequence("chunkReadingSequence");
        context.checking(new Expectations()
            {
                {
                    one(dao).getRegistrationTimestampOfLastFeedingEvent();
                    will(returnValue(null));
                    inSequence(chunkReadingSequence);

                    one(service).listOldestPhysicalDataSets(2);
                    will(returnValue(Arrays.asList(ds1, ds2)));
                    inSequence(chunkReadingSequence);

                    one(dao).getRegistrationTimestampOfLastFeedingEvent();
                    Date timeStamp = new Date(2000);
                    will(returnValue(timeStamp));
                    inSequence(chunkReadingSequence);

                    one(service).listOldestPhysicalDataSets(timeStamp, 2);
                    will(returnValue(Arrays.asList(ds3)));
                    inSequence(chunkReadingSequence);

                }
            });
        prepareHappyCase(ds1, ds3);
        prepareFailing(ds2);
        prepareCreateLastFeedingEvent(ds2.getRegistrationTimestamp());
        prepareCreateLastFeedingEvent(ds3.getRegistrationTimestamp());

        createTask(2, 0, 0).execute();
    }

    @Test
    public void testToSmallChunkSizeBecauseAllRegistrationTimeStampAreTheSame()
    {
        final SimpleDataSetInformationDTO ds1 = dataSet("ds1", 1000);
        final SimpleDataSetInformationDTO ds2 = dataSet("ds2", 1000);
        final SimpleDataSetInformationDTO ds3 = dataSet("ds3", 1000);

        final Sequence chunkReadingSequence = context.sequence("chunkReadingSequence");
        context.checking(new Expectations()
            {
                {
                    one(dao).getRegistrationTimestampOfLastFeedingEvent();
                    will(returnValue(null));
                    inSequence(chunkReadingSequence);

                    one(service).listOldestPhysicalDataSets(2);
                    will(returnValue(Arrays.asList(ds1, ds2)));
                    inSequence(chunkReadingSequence);

                    one(service).listOldestPhysicalDataSets(4);
                    will(returnValue(Arrays.asList(ds1, ds2, ds3)));
                    inSequence(chunkReadingSequence);

                    one(dao).getRegistrationTimestampOfLastFeedingEvent();
                    Date timeStamp = new Date(1000);
                    will(returnValue(timeStamp));
                    inSequence(chunkReadingSequence);

                    one(service).listOldestPhysicalDataSets(timeStamp, 2);
                    will(returnValue(Arrays.asList(ds1, ds2)));
                    inSequence(chunkReadingSequence);

                    one(service).listOldestPhysicalDataSets(timeStamp, 4);
                    will(returnValue(Arrays.asList(ds1, ds2, ds3)));
                    inSequence(chunkReadingSequence);

                }
            });
        prepareHappyCase(ds1, ds2, ds3);
        prepareCreateLastFeedingEvent(ds3.getRegistrationTimestamp());

        createTask(2, 0, 0).execute();
    }

    @Test
    public void testToSmallChunkSizeBecauseOfSameRegistrationTimeStamp()
    {
        final SimpleDataSetInformationDTO ds1 = dataSet("ds1", 1000);
        final SimpleDataSetInformationDTO ds2 = dataSet("ds2", 1000);
        final SimpleDataSetInformationDTO ds3 = dataSet("ds3", 1000);
        final SimpleDataSetInformationDTO ds4 = dataSet("ds4", 2000);

        final Sequence chunkReadingSequence = context.sequence("chunkReadingSequence");
        context.checking(new Expectations()
            {
                {
                    one(dao).getRegistrationTimestampOfLastFeedingEvent();
                    will(returnValue(null));
                    inSequence(chunkReadingSequence);

                    one(service).listOldestPhysicalDataSets(2);
                    will(returnValue(Arrays.asList(ds1, ds2)));
                    inSequence(chunkReadingSequence);

                    one(service).listOldestPhysicalDataSets(4);
                    will(returnValue(Arrays.asList(ds1, ds2, ds3, ds4)));
                    inSequence(chunkReadingSequence);

                    one(dao).getRegistrationTimestampOfLastFeedingEvent();
                    Date timeStamp = new Date(2000);
                    will(returnValue(timeStamp));
                    inSequence(chunkReadingSequence);

                    one(service).listOldestPhysicalDataSets(timeStamp, 2);
                    will(returnValue(Arrays.asList()));
                    inSequence(chunkReadingSequence);

                }
            });
        prepareHappyCase(ds1, ds2, ds3, ds4);
        prepareCreateLastFeedingEvent(ds4.getRegistrationTimestamp());

        createTask(2, 0, 0).execute();
    }

    @Test
    public void testPostRegistrationHappyCase()
    {
        final PhysicalDataSet dataSet =
                new DataSetBuilder().code(DATA_SET_CODE).location("abc").getDataSet();
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet(DATA_SET_CODE);
                    will(returnValue(dataSet));

                }
            });
        prepareHappyCase(dataSet);

        task.createExecutor(DATA_SET_CODE, false).execute();
    }

    @Test
    public void testPostRegistrationFailingCase()
    {
        final PhysicalDataSet dataSet =
                new DataSetBuilder().code(DATA_SET_CODE).location("abc").getDataSet();
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet(DATA_SET_CODE);
                    will(returnValue(dataSet));
                }
            });
        prepareFailing(dataSet);

        task.createExecutor(DATA_SET_CODE, false).execute();
    }

    @Test
    public void testNonExistingDataSetFolder()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet(DATA_SET_CODE);
                    PhysicalDataSet dataSet =
                            new DataSetBuilder().code(DATA_SET_CODE).location("abc").getDataSet();
                    will(returnValue(dataSet));

                    one(shareIdManager).lock(DATA_SET_CODE);

                    one(directoryProvider).getDataSetDirectory(dataSet);
                    will(returnValue(new File(workingDirectory, "blabla")));

                    exactly(2).of(shareIdManager).releaseLocks();
                }
            });

        task.createExecutor(DATA_SET_CODE, false).execute();
    }

    @Test
    public void testAlreadyExistingDataSetInDatabaser()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet(DATA_SET_CODE);
                    PhysicalDataSet dataSet =
                            new DataSetBuilder().code(DATA_SET_CODE).location("abc").getDataSet();
                    will(returnValue(dataSet));

                    one(shareIdManager).lock(DATA_SET_CODE);

                    one(directoryProvider).getDataSetDirectory(dataSet);
                    will(returnValue(dataSetFolder));

                    one(dao).tryGetDataSetId(dataSet.getDataSetCode());
                    will(returnValue(42L));

                    one(shareIdManager).releaseLocks();
                }
            });

        task.createExecutor(DATA_SET_CODE, false).execute();
    }

    private void prepareHappyCase(final IDatasetLocation... dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    for (IDatasetLocation dataSet : dataSets)
                    {
                        one(shareIdManager).lock(dataSet.getDataSetCode());

                        one(directoryProvider).getDataSetDirectory(dataSet);
                        will(returnValue(dataSetFolder));

                        one(dao).tryGetDataSetId(dataSet.getDataSetCode());
                        will(returnValue(null));

                        one(dao).createDataSet(dataSet.getDataSetCode(),
                                dataSet.getDataSetLocation());
                        will(returnValue(101L));

                        one(contentFactory).asHierarchicalContent(dataSetFolder, DO_NOTHING);
                        will(returnValue(content));

                        one(contentFactory).asHierarchicalContentNode(content, dataSetFolder);
                        will(returnValue(node));

                        one(node).exists();
                        will(returnValue(true));

                        one(node).getName();
                        will(returnValue("ds1-root"));

                        one(node).getFileLength();
                        will(returnValue(12345L));

                        one(node).getChecksumCRC32();
                        will(returnValue(789));

                        one(node).isDirectory();
                        will(returnValue(false));

                        one(node).getLastModified();
                        will(returnValue(42L));

                        one(dao).createDataSetFiles(
                                with(equal(Collections.singletonList(new PathEntryDTO(101L, null,
                                        "", "ds1-root", 12345L, 789, null, false, new Date(42))))));

                        one(dao).commit();
                        one(shareIdManager).releaseLocks();
                    }
                }
            });
    }

    private void prepareFailing(final IDatasetLocation... dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    for (IDatasetLocation dataSet : dataSets)
                    {
                        one(shareIdManager).lock(dataSet.getDataSetCode());

                        one(directoryProvider).getDataSetDirectory(dataSet);
                        will(returnValue(dataSetFolder));

                        one(dao).tryGetDataSetId(dataSet.getDataSetCode());
                        will(returnValue(null));

                        one(dao).createDataSet(dataSet.getDataSetCode(), dataSet.getDataSetLocation());
                        will(returnValue(101L));

                        one(contentFactory).asHierarchicalContent(dataSetFolder, DO_NOTHING);
                        will(throwException(new RuntimeException("Oophs!")));

                        one(dao).rollback();
                        one(shareIdManager).releaseLocks();
                    }
                }
            });
    }

    private void prepareCreateLastFeedingEvent(final Date timestamp)
    {
        context.checking(new Expectations()
            {
                {
                    one(dao).deleteLastFeedingEvent();
                    one(dao).createLastFeedingEvent(timestamp);
                    one(dao).commit();
                }
            });
    }

    private SimpleDataSetInformationDTO dataSet(long timeStamp)
    {
        return dataSet("DS-" + timeStamp, timeStamp);
    }

    private SimpleDataSetInformationDTO dataSet(String dataSetCode, long timeStamp)
    {
        SimpleDataSetInformationDTO dataSet = new SimpleDataSetInformationDTO();
        dataSet.setDataSetCode(dataSetCode);
        dataSet.setRegistrationTimestamp(new Date(timeStamp));
        dataSet.setDataSetLocation("abc " + dataSetCode);
        dataSet.setStorageConfirmed(true);
        return dataSet;
    }

    private PathInfoDatabaseFeedingTask createTask(int chunkSize, int maxNumberOfChunks,
            long timeLimite)
    {
        return new PathInfoDatabaseFeedingTask(service, directoryProvider, dao, contentFactory,
                new MockTimeProvider(0, 1000), true, chunkSize, maxNumberOfChunks, timeLimite);
    }

}
