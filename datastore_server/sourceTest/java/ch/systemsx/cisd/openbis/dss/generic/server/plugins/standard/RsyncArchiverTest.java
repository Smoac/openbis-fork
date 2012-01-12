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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractArchiverProcessingPlugin.SHARE_FINDER_KEY;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.BeanFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDeleter;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetStatusUpdater;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareFinder;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.IUnarchivingPreparation;
import ch.systemsx.cisd.openbis.dss.generic.shared.IncomingShareIdProviderTestWrapper;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.DatasetDescriptionBuilder;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses =
    { AbstractArchiverProcessingPlugin.class, RsyncArchiver.class })
public class RsyncArchiverTest extends AbstractFileSystemTestCase
{
    private static final String LOCATION = "location";

    private static final String DATA_STORE_CODE = "dss1";

    public static final class ShareFinder implements IShareFinder
    {
        private static Properties properties;

        private static SimpleDataSetInformationDTO recordedDataSet;

        private static List<Share> recordedShares;

        private boolean alwaysReturnNull = false;

        public ShareFinder(Properties properties)
        {
            ShareFinder.properties = properties;
            if (properties.containsKey("alwaysReturnNull"))
            {
                this.alwaysReturnNull = true;
            }
        }

        public Share tryToFindShare(SimpleDataSetInformationDTO dataSet, List<Share> shares)
        {
            ShareFinder.recordedDataSet = dataSet;
            ShareFinder.recordedShares = shares;
            if (shares.isEmpty() || alwaysReturnNull)
            {
                return null;
            } else
            {
                return shares.get(0);
            }
        }
    }

    private BufferedAppender logRecorder;

    private Mockery context;

    private IDataSetFileOperationsManager fileOperationsManager;

    private RsyncArchiver archiver;

    private IDataSetDirectoryProvider dataSetDirectoryProvider;

    private IUnarchivingPreparation unarchivingPreparation;

    private ArchiverTaskContext archiverTaskContext;

    private IDataSetStatusUpdater statusUpdater;

    private Properties properties;

    private BeanFactory beanFactory;

    private IConfigProvider configProvider;

    private IEncapsulatedOpenBISService service;

    private IShareIdManager shareIdManager;

    private File store;

    private File share1;

    private File share2;

    private IDataStoreServiceInternal dataStoreService;

    private IDataSetDeleter deleter;

    @BeforeMethod
    public void beforeMethod()
    {
        LogInitializer.init();
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
        context = new Mockery();
        fileOperationsManager = context.mock(IDataSetFileOperationsManager.class);
        dataSetDirectoryProvider = context.mock(IDataSetDirectoryProvider.class);
        unarchivingPreparation = context.mock(IUnarchivingPreparation.class);
        statusUpdater = context.mock(IDataSetStatusUpdater.class);
        configProvider = context.mock(IConfigProvider.class);
        service = context.mock(IEncapsulatedOpenBISService.class);
        shareIdManager = context.mock(IShareIdManager.class);
        deleter = context.mock(IDataSetDeleter.class);
        final Advised adviced = context.mock(Advised.class);
        final TargetSource targetSource = context.mock(TargetSource.class);
        dataStoreService = context.mock(IDataStoreServiceInternal.class);
        beanFactory = context.mock(BeanFactory.class);
        ServiceProviderTestWrapper.setApplicationContext(beanFactory);
        context.checking(new Expectations()
            {
                {
                    allowing(beanFactory).getBean("config-provider");
                    will(returnValue(configProvider));

                    allowing(beanFactory).getBean("openBIS-service");
                    will(returnValue(service));

                    allowing(beanFactory).getBean("share-id-manager");
                    will(returnValue(shareIdManager));

                    allowing(beanFactory).getBean("data-store-service");
                    will(returnValue(adviced));

                    allowing(adviced).getTargetSource();
                    will(returnValue(targetSource));

                    try
                    {
                        allowing(targetSource).getTarget();
                        will(returnValue(dataStoreService));
                    } catch (Exception ex)
                    {
                        // ignored
                    }
                }
            });

        IncomingShareIdProviderTestWrapper.setShareIds(Arrays.asList("1"));
        store = new File(workingDirectory, "store");
        store.mkdirs();
        share1 = new File(store, "1");
        share1.mkdir();
        File ds1 = new File(share1, LOCATION);
        ds1.mkdir();
        FileUtilities.writeToFile(new File(ds1, "ds1"), "hello world");
        share2 = new File(store, "2");
        share2.mkdir();
        properties = new Properties();
        archiver = new RsyncArchiver(properties, store, fileOperationsManager);
        archiver.statusUpdater = statusUpdater;
        archiverTaskContext = new ArchiverTaskContext(dataSetDirectoryProvider);
    }

    @AfterMethod
    public void afterMethod(Method method)
    {
        System.out.println("======= Log content for " + method.getName() + "():");
        System.out.println(logRecorder.getLogContent());
        System.out.println("======================");
        logRecorder.reset();
        ServiceProviderTestWrapper.restoreApplicationContext();
        IncomingShareIdProviderTestWrapper.restoreOriginalShareIds();
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
    public void testSuccessfulArchivingIfDataSetPresentInArchive()
    {
        final DatasetDescription ds1 =
                new DatasetDescriptionBuilder("ds1").location(LOCATION).getDatasetDescription();
        context.checking(new Expectations()
            {
                {
                    one(shareIdManager).getShareId("ds1");
                    will(returnValue("1"));

                    one(service).updateShareIdAndSize("ds1", "1", 11L);

                    one(dataSetDirectoryProvider).getDataSetDirectory(ds1);
                    File file = new File(share1, LOCATION);
                    will(returnValue(file));

                    one(fileOperationsManager).isSynchronizedWithDestination(file, ds1);
                    will(returnValue(BooleanStatus.createTrue()));

                    one(dataStoreService).getDataSetDeleter();
                    will(returnValue(deleter));

                    one(deleter).scheduleDeletionOfDataSets(Arrays.asList(ds1));

                    one(statusUpdater).update(Arrays.asList("ds1"),
                            DataSetArchivingStatus.ARCHIVED, true);
                }
            });

        ProcessingStatus status = archiver.archive(Arrays.asList(ds1), archiverTaskContext, true);

        assertEquals("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds1']",
                logRecorder.getLogContent());
        assertEquals("[]", status.getErrorStatuses().toString());
    }

    @Test
    public void testSuccessfulArchiving()
    {
        final DatasetDescription ds1 =
                new DatasetDescriptionBuilder("ds1").location(LOCATION).size(42L)
                        .getDatasetDescription();
        context.checking(new Expectations()
            {
                {
                    exactly(3).of(dataSetDirectoryProvider).getDataSetDirectory(ds1);
                    File file = new File(share1, LOCATION);
                    will(returnValue(file));

                    one(fileOperationsManager).isSynchronizedWithDestination(file, ds1);
                    will(returnValue(BooleanStatus.createFalse()));

                    one(fileOperationsManager).copyToDestination(file, ds1);
                    will(returnValue(Status.OK));

                    one(fileOperationsManager).isSynchronizedWithDestination(file, ds1);
                    will(returnValue(BooleanStatus.createTrue()));

                    one(statusUpdater).update(Arrays.asList("ds1"),
                            DataSetArchivingStatus.AVAILABLE, true);
                }
            });

        ProcessingStatus status = archiver.archive(Arrays.asList(ds1), archiverTaskContext, false);

        assertEquals("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Archiving of the following datasets has been requested: [Dataset 'ds1']",
                logRecorder.getLogContent());
        assertEquals("[]", status.getErrorStatuses().toString());
    }

    @Test
    public void testSuccessfulUnarchivingWithRealUnarchivingPreparation()
    {
        properties.setProperty(SHARE_FINDER_KEY + ".class", ShareFinder.class.getName());
        properties.setProperty(SHARE_FINDER_KEY + ".p1", "property 1");
        archiverTaskContext.setUnarchivingPreparation(unarchivingPreparation);
        final DatasetDescription ds1 =
                new DatasetDescriptionBuilder("ds1").databaseInstance("db").experiment("exp1")
                        .location("loc1").project("p1").sample("s1").space("space").size(11l)
                        .type("my-type").getDatasetDescription();
        context.checking(new Expectations()
            {
                {
                    one(configProvider).getDataStoreCode();
                    will(returnValue(DATA_STORE_CODE));

                    one(service).listDataSets();
                    SimpleDataSetInformationDTO dataSet = new SimpleDataSetInformationDTO();
                    dataSet.setDataSetCode("ds1");
                    dataSet.setDataStoreCode(DATA_STORE_CODE);
                    dataSet.setDataSetShareId("1");
                    dataSet.setDataSetLocation(LOCATION);
                    dataSet.setDataSetSize(11L);
                    will(returnValue(Arrays.asList(dataSet)));

                    one(shareIdManager).getShareId("ds1");
                    will(returnValue("2"));

                    one(dataSetDirectoryProvider).getDataSetDirectory(ds1);
                    File file = new File(store, LOCATION);
                    will(returnValue(file));

                    one(service).updateShareIdAndSize("ds1", "1", 11L);
                    one(shareIdManager).setShareId("ds1", "1");

                    one(fileOperationsManager).retrieveFromDestination(file, ds1);
                    will(returnValue(Status.OK));

                    one(statusUpdater).update(Arrays.asList("ds1"),
                            DataSetArchivingStatus.AVAILABLE, true);
                }
            });

        ProcessingStatus status = archiver.unarchive(Arrays.asList(ds1), archiverTaskContext);

        assertEquals("INFO  OPERATION.AbstractDatastorePlugin - "
                + "Unarchiving of the following datasets has been requested: [Dataset 'ds1']",
                logRecorder.getLogContent());
        assertEquals("[]", status.getErrorStatuses().toString());
        assertEquals("{class=" + ShareFinder.class.getName() + "\np1=property 1}",
                ShareFinder.properties.toString());
        assertEquals("ds1", ShareFinder.recordedDataSet.getDataSetCode());
        assertEquals("db", ShareFinder.recordedDataSet.getDatabaseInstanceCode());
        assertEquals("loc1", ShareFinder.recordedDataSet.getDataSetLocation());
        assertEquals(null, ShareFinder.recordedDataSet.getDataSetShareId());
        assertEquals("my-type", ShareFinder.recordedDataSet.getDataSetType());
        assertEquals("exp1", ShareFinder.recordedDataSet.getExperimentCode());
        assertEquals("space", ShareFinder.recordedDataSet.getGroupCode());
        assertEquals("p1", ShareFinder.recordedDataSet.getProjectCode());
        assertEquals("s1", ShareFinder.recordedDataSet.getSampleCode());
        assertEquals(new Long(11L), ShareFinder.recordedDataSet.getDataSetSize());
        assertEquals(share1, ShareFinder.recordedShares.get(0).getShare());
        assertEquals(share2, ShareFinder.recordedShares.get(1).getShare());
        assertEquals(2, ShareFinder.recordedShares.size());
    }

    @Test
    public void testFailingUnarchivingWhenNoShareHasBeenFound()
    {
        properties.setProperty(SHARE_FINDER_KEY + ".class", ShareFinder.class.getName());
        properties.setProperty(SHARE_FINDER_KEY + ".alwaysReturnNull", "true");
        archiverTaskContext.setUnarchivingPreparation(unarchivingPreparation);
        final DatasetDescription ds1 =
                new DatasetDescriptionBuilder("ds1").databaseInstance("db").experiment("exp1")
                        .location("loc1").project("p1").sample("s1").space("space").size(11l)
                        .type("my-type").getDatasetDescription();
        context.checking(new Expectations()
            {
                {
                    one(configProvider).getDataStoreCode();
                    will(returnValue(DATA_STORE_CODE));

                    one(service).listDataSets();
                    SimpleDataSetInformationDTO dataSet = new SimpleDataSetInformationDTO();
                    dataSet.setDataSetCode("ds1");
                    dataSet.setDataStoreCode(DATA_STORE_CODE);
                    dataSet.setDataSetShareId("1");
                    dataSet.setDataSetLocation(LOCATION);
                    dataSet.setDataSetSize(11L);
                    will(returnValue(Arrays.asList(dataSet)));

                    one(statusUpdater).update(Arrays.asList("ds1"),
                            DataSetArchivingStatus.ARCHIVED, true);
                }
            });

        ProcessingStatus status = archiver.unarchive(Arrays.asList(ds1), archiverTaskContext);

        assertEquals(1, status.getErrorStatuses().size());
        Status errorStatus = status.getErrorStatuses().get(0);
        assertEquals("Unarchiving failed: Unarchiving of data set 'ds1' has failed, because no "
                + "appropriate destination share was found. Most probably there is not enough "
                + "free space in the data store.", errorStatus.tryGetErrorMessage());
    }

    @Test
    public void testUnarchivingWithDefaultShareFinder()
    {
        final DatasetDescription ds1 = new DatasetDescriptionBuilder("ds1").getDatasetDescription();
        final DatasetDescription ds2 = new DatasetDescriptionBuilder("ds2").getDatasetDescription();
        context.checking(new Expectations()
            {
                {
                    one(configProvider).getDataStoreCode();
                    will(returnValue(DATA_STORE_CODE));

                    one(service).listDataSets();
                    SimpleDataSetInformationDTO dataSet1 = new SimpleDataSetInformationDTO();
                    dataSet1.setDataSetCode("ds1");
                    dataSet1.setDataStoreCode(DATA_STORE_CODE);
                    dataSet1.setDataSetShareId("1");
                    dataSet1.setDataSetLocation(LOCATION);
                    dataSet1.setDataSetSize(11L);
                    SimpleDataSetInformationDTO dataSet2 = new SimpleDataSetInformationDTO();
                    dataSet2.setDataSetCode("ds2");
                    dataSet2.setDataStoreCode(DATA_STORE_CODE);
                    dataSet2.setDataSetShareId("1");
                    dataSet2.setDataSetLocation(LOCATION);
                    dataSet2.setDataSetSize(22L);
                    will(returnValue(Arrays.asList(dataSet1, dataSet2)));

                    one(statusUpdater).update(Arrays.asList("ds1", "ds2"),
                            DataSetArchivingStatus.ARCHIVED, true);
                }
            });

        ProcessingStatus status = archiver.unarchive(Arrays.asList(ds1, ds2), archiverTaskContext);
        assertEquals("[ERROR: \"Unarchiving failed: null\"]", status.getErrorStatuses().toString());
    }

    @Test
    public void testDeleteFromArchivePermanently()
    {
        properties.setProperty(RsyncArchiver.ONLY_MARK_AS_DELETED_KEY, "false");
        final DatasetLocation datasetLocation = new DatasetLocation();
        datasetLocation.setDataSetLocation("my-location");
        context.checking(new Expectations()
            {
                {
                    one(fileOperationsManager).deleteFromDestination(datasetLocation);
                    will(returnValue(Status.OK));
                }
            });

        archiver = new RsyncArchiver(properties, store, fileOperationsManager);
        archiver.deleteFromArchive(Arrays.asList(datasetLocation));
    }
    
    @Test
    public void testDeleteFromArchiveOnlyMarkAsDeleted()
    {
        final DatasetLocation datasetLocation = new DatasetLocation();
        datasetLocation.setDataSetLocation("my-location");
        context.checking(new Expectations()
        {
            {
                one(fileOperationsManager).markAsDeleted(datasetLocation);
                will(returnValue(Status.OK));
            }
        });
        
        archiver = new RsyncArchiver(properties, store, fileOperationsManager);
        archiver.deleteFromArchive(Arrays.asList(datasetLocation));
    }

}
