/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.concurrent.ExecutionResult;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.filesystem.ssh.ISshCommandExecutor;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Piotr Buczek
 */
@Friend(toClasses = RsyncDataSetCopier.class)
public class RsyncDataSetCopierTest extends AbstractFileSystemTestCase
{
    private static final String DUMMY_ERROR_MESSAGE = "dummy error message";

    private static final ProcessResult OK_RESULT = new ProcessResult(Arrays.asList(""), 0, null,
            ExecutionResult.create(null), null, 0, (List<String>) null, null, null, null);

    private static final ProcessResult ERROR_RESULT = new ProcessResult(Arrays.asList(""), 0, null,
            ExecutionResult.createExceptional(new Exception(DUMMY_ERROR_MESSAGE)), null, 0,
            (List<String>) null, null, null, null);

    private static final long SSH_TIMEOUT_MILLIS = RsyncDataSetCopier.SSH_TIMEOUT_MILLIS;

    private static final String LOCATION_1 = "l1";

    private static final String DS1_CODE = "ds1";

    private static final String DS2_CODE = "ds2";

    private static final String DS1_LOCATION = LOCATION_1 + File.separator + DS1_CODE;

    private static final String DS2_LOCATION = LOCATION_1 + File.separator + DS2_CODE;

    private static final String DS1_DATA_FILE = "data1.txt";

    private static final String DS2_DATA_FILE = "data2.txt";

    private static final String DATA1 = "hello test 1";

    private static final String DATA2 = "hello test 2";

    private static final String ORIGINAL = "original";

    private static final String SHARE_ID = "42";

    private File storeRoot;

    private DatasetDescription ds1;

    private File ds1Location;

    private File ds1Data;

    private DatasetDescription ds2;

    private File ds2Location;

    private File ds2Data;

    private File ds1ArchivedLocationFile;

    private File ds1ArchivedDataFile;

    private File ds2ArchivedLocationFile;

    private File ds2ArchivedDataFile;

    private Mockery context;

    private IPathCopierFactory copierFactory;

    private IPathCopier copier;

    private ISshCommandExecutorFactory sshFactory;

    private ISshCommandExecutor sshExecutor;

    private File destination;

    private File rsyncExec;

    private File sshExec;

    @BeforeClass
    public void init()
    {
        LogInitializer.init();
    }

    @BeforeMethod
    public void beforeMethod() throws Exception
    {
        context = new Mockery();
        copierFactory = context.mock(IPathCopierFactory.class);
        copier = context.mock(IPathCopier.class);
        sshFactory = context.mock(ISshCommandExecutorFactory.class);
        sshExecutor = context.mock(ISshCommandExecutor.class);

        storeRoot = new File(workingDirectory, "store");
        storeRoot.mkdirs();
        File share = new File(storeRoot, SHARE_ID);

        ds1 = createDataSetDescription(DS1_CODE, DS1_LOCATION, true);
        ds1Location = new File(share, DS1_LOCATION);
        File ds1Folder = new File(ds1Location, ORIGINAL);
        ds1Folder.mkdirs();
        ds1Data = new File(ds1Folder, DS1_DATA_FILE);
        FileUtilities.writeToFile(ds1Data, DATA1);

        ds2 = createDataSetDescription(DS2_CODE, DS2_LOCATION, true);
        ds2Location = new File(share, DS2_LOCATION);
        File ds2Folder = new File(ds2Location, ORIGINAL);
        ds2Folder.mkdirs();
        ds2Data = new File(ds2Folder, DS2_DATA_FILE);
        FileUtilities.writeToFile(ds2Data, DATA2);

        destination = new File(workingDirectory, "destination");
        destination.mkdirs();

        ds1ArchivedLocationFile = new File(destination, ds1.getDataSetLocation());
        ds1ArchivedDataFile =
                new File(ds1ArchivedLocationFile, ORIGINAL + File.separator + ds1Data.getName());
        ds2ArchivedLocationFile = new File(destination, ds2.getDataSetLocation());
        ds2ArchivedDataFile =
                new File(ds2ArchivedLocationFile, ORIGINAL + File.separator + ds2Data.getName());

        rsyncExec = new File(workingDirectory, "my-rsync");
        rsyncExec.createNewFile();
        sshExec = new File(workingDirectory, "my-rssh");
        sshExec.createNewFile();
    }

    private DatasetDescription createDataSetDescription(String dataSetCode, String location,
            boolean withSample)
    {
        DatasetDescription description = new DatasetDescription();
        description.setDatasetCode(dataSetCode);
        description.setDatasetTypeCode("MY_DATA");
        description.setDataSetLocation(location);
        description.setDatabaseInstanceCode("i");
        description.setSpaceCode("g");
        description.setProjectCode("p");
        description.setExperimentCode("e");
        description.setExperimentIdentifier("/g/p/e");
        description.setExperimentTypeCode("MY_EXPERIMENT");
        if (withSample)
        {
            description.setSampleCode("s");
            description.setSampleIdentifier("/g/s");
            description.setSampleTypeCode("MY_SAMPLE");
        }
        return description;
    }

    @AfterMethod
    public void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    /*
     * --< LOCAL >----------------------------------------------------------------------------------
     */

    @Test
    public void testLocalCopyToDestination()
    {
        Properties properties = createLocalDestinationProperties();
        RsyncDataSetCopier dataSetCopier =
                new RsyncDataSetCopier(properties, copierFactory, sshFactory);
        prepareForCheckingLastModifiedDate();

        // check that data set is not yet in archive
        assertDs1NotInArchive();

        /*
         * archive 1st time
         */
        Status status = dataSetCopier.copyToDestination(ds1Location, ds1);
        assertSuccessful(status);
        // check that data set is now in archive
        assertDs1InArchive();
        // check that data set is still in store
        assertDs1InStore();

        /*
         * archive 2nd time (could happen on crash of DSS, but shouldn't hurt)
         */
        status = dataSetCopier.copyToDestination(ds1Location, ds1);
        assertSuccessful(status);
        // check that data set is now in archive
        assertDs1InArchive();
        // check that data set is still in store
        assertDs1InStore();

        context.assertIsSatisfied();
    }

    @Test
    public void testLocalCopyToNonExistentDestination()
    {
        Properties properties = createLocalDestinationProperties();
        RsyncDataSetCopier dataSetCopier =
                new RsyncDataSetCopier(properties, copierFactory, sshFactory);
        prepareForCheckingLastModifiedDate();

        destination.delete(); // if destination folder doesn't exist it will be created

        // check that data set is not yet in archive
        assertDs1NotInArchive();

        /*
         * archive
         */
        Status status = dataSetCopier.copyToDestination(ds1Location, ds1);
        assertSuccessful(status);
        // check that data set is now in archive
        assertDs1InArchive();
        // check that data set is still in store
        assertDs1InStore();

        context.assertIsSatisfied();
    }

    @Test(dependsOnMethods = "testLocalCopyToDestination")
    public void testLocalCopyTwoDataSetsToDestination()
    {
        Properties properties = createLocalDestinationProperties();
        RsyncDataSetCopier dataSetCopier =
                new RsyncDataSetCopier(properties, copierFactory, sshFactory);
        prepareForCheckingLastModifiedDate();

        // check that both data sets are not yet in archive
        assertDs1NotInArchive();
        assertDs2NotInArchive();

        /*
         * copy 1st data set
         */
        Status status1 = dataSetCopier.copyToDestination(ds1Location, ds1);
        assertSuccessful(status1);
        assertDs1InArchive();
        // check that 2nd data set is not yet in archive
        assertDs2NotInArchive();

        /*
         * copy 2nd data set
         */
        Status status2 = dataSetCopier.copyToDestination(ds2Location, ds2);
        assertSuccessful(status2);
        assertDs2InArchive();
        // check that 1st data set is still in archive
        assertDs1InArchive();

        // both data sets should be in the store
        assertDs1InStore();
        assertDs2InStore();

        context.assertIsSatisfied();
    }

    @Test(dependsOnMethods = "testLocalCopyToDestination")
    public void testLocalRetrieveFromDestination()
    {
        /*
         * copy to archive
         */
        Properties properties = createLocalDestinationProperties();
        RsyncDataSetCopier dataSetCopier =
                new RsyncDataSetCopier(properties, copierFactory, sshFactory);
        prepareForCheckingLastModifiedDate();

        // check that data set is not yet in archive
        assertDs1NotInArchive();

        Status status = dataSetCopier.copyToDestination(ds1Location, ds1);

        assertSuccessful(status);
        assertDs1InArchive();

        /*
         * delete from store
         */
        try
        {
            FileUtils.deleteDirectory(ds1Location);
        } catch (IOException e)
        {
            fail(e.getMessage());
        }
        assertFalse(ds1Data.exists());

        /*
         * retrieve from archive - 1st time
         */
        Status statusRetrieve = dataSetCopier.retrieveFromDestination(ds1Location, ds1);
        assertSuccessful(statusRetrieve);
        assertDs1InStore();
        assertDs1InArchive();
        assertDs2InStore(); // ds2 shouldn't be affected at all

        /*
         * retrieve from archive - 2nd time (possible e.g. after crash)
         */
        statusRetrieve = dataSetCopier.retrieveFromDestination(ds1Location, ds1);
        assertSuccessful(statusRetrieve);
        assertDs1InStore();
        assertDs1InArchive();
        assertDs2InStore(); // ds2 shouldn't be affected at all

        context.assertIsSatisfied();
    }

    @Test(dependsOnMethods = "testLocalCopyToDestination")
    public void testLocalPresentInDestination()
    {
        Properties properties = createLocalDestinationProperties();
        RsyncDataSetCopier dataSetCopier =
                new RsyncDataSetCopier(properties, copierFactory, sshFactory);
        prepareForCheckingLastModifiedDate();

        // check that data set is not yet in archive
        assertDs1NotInArchive();

        /*
         * before copying - doesn't exist
         */
        BooleanStatus boolStatus = dataSetCopier.isPresentInDestination(ds1Location, ds1);
        assertFalse(boolStatus);

        /*
         * copy to archive
         */
        Status status = dataSetCopier.copyToDestination(ds1Location, ds1);
        assertSuccessful(status);
        assertDs1InArchive();

        /*
         * after copying - exists
         */
        boolStatus = dataSetCopier.isPresentInDestination(ds1Location, ds1);
        assertTrue(boolStatus);

        context.assertIsSatisfied();
    }

    @Test(dependsOnMethods = "testLocalCopyToDestination")
    public void testLocalDeleteFromDestination()
    {
        /*
         * copy to archive
         */
        Properties properties = createLocalDestinationProperties();
        RsyncDataSetCopier dataSetCopier =
                new RsyncDataSetCopier(properties, copierFactory, sshFactory);
        prepareForCheckingLastModifiedDate();

        // check that data set is not yet in archive
        assertDs1NotInArchive();

        Status status = dataSetCopier.copyToDestination(ds1Location, ds1);

        assertSuccessful(status);
        assertDs1InArchive();

        /*
         * delete from archive
         */
        Status statusDelete = dataSetCopier.deleteFromDestination(ds1);
        assertSuccessful(statusDelete);
        assertDs1NotInArchive();
        assertDs1InStore(); // we didn't delete it from store

        context.assertIsSatisfied();
    }

    private Properties createLocalDestinationProperties()
    {
        final Properties properties = new Properties();
        properties.setProperty(RsyncDataSetCopier.DESTINATION_KEY, destination.getPath());
        return properties;
    }

    /*
     * --< REMOTE >---------------------------------------------------------------------------------
     */

    @Test
    public void testRemoteViaSshCopyToDestination()
    {
        Properties properties = createRemoteViaSshDestinationProperties();
        prepareRemoteCreateAndCheckCopier(HOST, null, true);
        RsyncDataSetCopier dataSetCopier =
                new RsyncDataSetCopier(properties, copierFactory, sshFactory);
        context.checking(new Expectations()
            {
                {
                    /*
                     * ds1: directory exists in archive -> first delete from directory
                     */
                    one(sshExecutor).exists(ds1ArchivedLocationFile.getPath(), SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createTrue()));

                    one(sshExecutor).executeCommandRemotely(
                            "rm -rf " + ds1ArchivedLocationFile.getPath(), SSH_TIMEOUT_MILLIS);
                    will(returnValue(OK_RESULT));

                    one(copier).copyToRemote(ds1Location, ds1ArchivedLocationFile.getParentFile(),
                            HOST, null, null);
                    will(returnValue(Status.OK));

                    /*
                     * ds2: directory doesn't exist in archive -> only copy
                     */
                    one(sshExecutor).exists(ds2ArchivedLocationFile.getPath(), SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createFalse()));

                    one(copier).copyToRemote(ds2Location, ds2ArchivedLocationFile.getParentFile(),
                            HOST, null, null);
                    will(returnValue(Status.OK));
                }
            });
        Status status1 = dataSetCopier.copyToDestination(ds1Location, ds1);
        Status status2 = dataSetCopier.copyToDestination(ds2Location, ds2);
        assertSuccessful(status1);
        assertSuccessful(status2);

        context.assertIsSatisfied();
    }

    @Test
    public void testRemoteViaSshCopyToDestinationWithErrors()
    {
        Properties properties = createRemoteViaSshDestinationProperties();
        prepareRemoteCreateAndCheckCopier(HOST, null, true);
        RsyncDataSetCopier dataSetCopier =
                new RsyncDataSetCopier(properties, copierFactory, sshFactory);
        context.checking(new Expectations()
            {
                {
                    one(sshExecutor).exists(ds1ArchivedLocationFile.getPath(), SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createFalse()));
                    one(copier).copyToRemote(ds1Location, ds1ArchivedLocationFile.getParentFile(),
                            HOST, null, null);
                    will(returnValue(Status.createError(DUMMY_ERROR_MESSAGE)));
                }
            });
        Status status = dataSetCopier.copyToDestination(ds1Location, ds1);
        assertError(status, DUMMY_ERROR_MESSAGE);

        context.assertIsSatisfied();
    }

    @Test
    public void testRemoteViaSshRetrieveFromDestination()
    {
        Properties properties = createRemoteViaSshDestinationProperties();
        prepareRemoteCreateAndCheckCopier(HOST, null, true);
        RsyncDataSetCopier dataSetCopier =
                new RsyncDataSetCopier(properties, copierFactory, sshFactory);
        context.checking(new Expectations()
            {
                {
                    one(sshExecutor).exists(ds1ArchivedLocationFile.getPath(), SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createTrue()));
                    one(copier).copyFromRemote(ds1ArchivedLocationFile, HOST,
                            ds1Location.getParentFile(), null, null);
                    will(returnValue(Status.OK));
                }
            });
        Status status = dataSetCopier.retrieveFromDestination(ds1Location, ds1);
        assertSuccessful(status);

        context.assertIsSatisfied();
    }

    public void testRemoteViaSshRetrieveFromDestinationWithErrors()
    {
        Properties properties = createRemoteViaSshDestinationProperties();
        prepareRemoteCreateAndCheckCopier(HOST, null, true);
        RsyncDataSetCopier dataSetCopier =
                new RsyncDataSetCopier(properties, copierFactory, sshFactory);
        context.checking(new Expectations()
            {
                {
                    /*
                     * ds1: destination doesn't exist
                     */
                    one(sshExecutor).exists(ds1ArchivedLocationFile.getPath(), SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createFalse()));

                    /*
                     * ds2: copy failed
                     */
                    one(sshExecutor).exists(ds2ArchivedLocationFile.getPath(), SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createTrue()));
                    one(copier).copyFromRemote(ds2ArchivedLocationFile, HOST,
                            ds2Location.getParentFile(), null, null);
                    will(returnValue(Status.createError(DUMMY_ERROR_MESSAGE)));
                }
            });

        Status status1 = dataSetCopier.retrieveFromDestination(ds1Location, ds1);
        assertError(status1, RsyncDataSetCopier.DESTINATION_DOES_NOT_EXIST);

        Status status2 = dataSetCopier.retrieveFromDestination(ds2Location, ds2);
        assertError(status2, DUMMY_ERROR_MESSAGE);

        context.assertIsSatisfied();
    }

    @Test
    public void testRemoteViaSshIsPresentInDestination()
    {
        Properties properties = createRemoteViaSshDestinationProperties();
        prepareRemoteCreateAndCheckCopier(HOST, null, true);
        RsyncDataSetCopier dataSetCopier =
                new RsyncDataSetCopier(properties, copierFactory, sshFactory);
        context.checking(new Expectations()
            {
                {
                    /*
                     * ds1: present
                     */
                    one(sshExecutor).exists(ds1ArchivedLocationFile.getPath(), SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createTrue()));

                    /*
                     * ds2: not present
                     */
                    one(sshExecutor).exists(ds2ArchivedLocationFile.getPath(), SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createFalse()));
                }
            });
        BooleanStatus status1 = dataSetCopier.isPresentInDestination(ds1Location, ds1);
        BooleanStatus status2 = dataSetCopier.isPresentInDestination(ds2Location, ds2);
        assertTrue(status1);
        assertFalse(status2);

        context.assertIsSatisfied();
    }

    @Test
    public void testRemoteViaSshIsPresentInDestinationWithError()
    {
        Properties properties = createRemoteViaSshDestinationProperties();
        prepareRemoteCreateAndCheckCopier(HOST, null, true);
        RsyncDataSetCopier dataSetCopier =
                new RsyncDataSetCopier(properties, copierFactory, sshFactory);
        context.checking(new Expectations()
            {
                {
                    one(sshExecutor).exists(ds1ArchivedLocationFile.getPath(), SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createError(DUMMY_ERROR_MESSAGE)));
                }
            });
        BooleanStatus status = dataSetCopier.isPresentInDestination(ds1Location, ds1);
        assertError(status, DUMMY_ERROR_MESSAGE);

        context.assertIsSatisfied();
    }

    @Test
    public void testRemoteViaSshDeleteFromDestination()
    {
        Properties properties = createRemoteViaSshDestinationProperties();
        prepareRemoteCreateAndCheckCopier(HOST, null, true);
        RsyncDataSetCopier dataSetCopier =
                new RsyncDataSetCopier(properties, copierFactory, sshFactory);
        context.checking(new Expectations()
            {
                {
                    /*
                     * ds1: directory exists in archive -> delete from directory
                     */
                    one(sshExecutor).exists(ds1ArchivedLocationFile.getPath(), SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createTrue()));
                    one(sshExecutor).executeCommandRemotely(
                            "rm -rf " + ds1ArchivedLocationFile.getPath(), SSH_TIMEOUT_MILLIS);
                    will(returnValue(OK_RESULT));

                    /*
                     * ds2: directory doesn't exist in archive -> nothing to do
                     */
                    one(sshExecutor).exists(ds2ArchivedLocationFile.getPath(), SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createFalse()));
                }
            });
        Status status1 = dataSetCopier.deleteFromDestination(ds1);
        Status status2 = dataSetCopier.deleteFromDestination(ds2);
        assertSuccessful(status1);
        assertSuccessful(status2);

        context.assertIsSatisfied();
    }

    @Test
    public void testRemoteViaSshDeleteFromDestinationWithErrors()
    {
        Properties properties = createRemoteViaSshDestinationProperties();
        prepareRemoteCreateAndCheckCopier(HOST, null, true);
        RsyncDataSetCopier dataSetCopier =
                new RsyncDataSetCopier(properties, copierFactory, sshFactory);
        context.checking(new Expectations()
            {
                {
                    /*
                     * ds1: fail to delete
                     */
                    one(sshExecutor).exists(ds1ArchivedLocationFile.getPath(), SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createTrue()));
                    one(sshExecutor).executeCommandRemotely(
                            "rm -rf " + ds1ArchivedLocationFile.getPath(), SSH_TIMEOUT_MILLIS);
                    will(returnValue(ERROR_RESULT));

                    /*
                     * ds2: fail to check existence
                     */
                    one(sshExecutor).exists(ds2ArchivedLocationFile.getPath(), SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createError(DUMMY_ERROR_MESSAGE)));
                }
            });
        Status status1 = dataSetCopier.deleteFromDestination(ds1);
        Status status2 = dataSetCopier.deleteFromDestination(ds2);
        assertError(status1, "couldn't delete");
        assertError(status2, "couldn't check existence");

        context.assertIsSatisfied();
    }

    // TODO 2011-03-14, Piotr Buczek: test rsync operations

    // @Test
    // public void testRemoteViaRsyncCopyToDestination()
    // {
    // Properties properties = createRemoteViaRsyncDestinationProperties();
    // RsyncDataSetCopier dataSetCopier =
    // new RsyncDataSetCopier(properties, copierFactory, sshFactory);
    // prepareForCheckingLastModifiedDate();
    //
    // File copiedDataSet = ds1ArchivedLocationFile();
    // File copiedData = ds1ArchivedDataFile();
    //
    // // check that data set is not yet in archive
    // assertEquals(false, copiedDataSet.exists());
    //
    // /*
    // * archive 1st time
    // */
    // Status status = dataSetCopier.copyToDestination(ds1Location, ds1);
    // assertEquals(Status.OK, status);
    // // check that data set is now in archive
    // assertDs1InArchive(copiedDataSet, copiedData);
    // // check that data set is still in store
    // assertDs1InStore();
    //
    // /*
    // * archive 2nd time (could happen on crash of DSS, but shouldn't hurt)
    // */
    // status = dataSetCopier.copyToDestination(ds1Location, ds1);
    // assertEquals(Status.OK, status);
    // // check that data set is now in archive
    // assertDs1InArchive(copiedDataSet, copiedData);
    // // check that data set is still in store
    // assertDs1InStore();
    //
    // context.assertIsSatisfied();
    // }

    private void prepareRemoteCreateAndCheckCopier(final String hostOrNull,
            final String rsyncModuleOrNull, final boolean checkingResult)
    {
        context.checking(new Expectations()
            {
                {
                    one(copierFactory).create(rsyncExec, sshExec);
                    will(returnValue(copier));

                    one(sshFactory).create(sshExec, hostOrNull);
                    will(returnValue(sshExecutor));

                    one(copier).check();
                    if (hostOrNull != null)
                    {
                        if (rsyncModuleOrNull != null)
                        {
                            one(copier).checkRsyncConnectionViaRsyncServer(hostOrNull,
                                    rsyncModuleOrNull, rsyncModuleOrNull + "-password",
                                    SSH_TIMEOUT_MILLIS);
                        } else
                        {
                            one(copier).checkRsyncConnectionViaSsh(hostOrNull, null,
                                    SSH_TIMEOUT_MILLIS);
                        }
                        will(returnValue(checkingResult));
                    }
                }
            });
    }

    private static String HOST = "localhost";

    private static String RSYNC_MODULE = "abc";

    private Properties createRemoteViaSshDestinationProperties()
    {
        final Properties properties = new Properties();
        properties.setProperty(RsyncDataSetCopier.DESTINATION_KEY,
                HOST + ":" + destination.getPath());
        properties.setProperty(RsyncDataSetCopier.RSYNC_EXEC + "-executable", rsyncExec.getPath());
        properties.setProperty(RsyncDataSetCopier.SSH_EXEC + "-executable", sshExec.getPath());
        return properties;
    }

    @SuppressWarnings("unused")
    private Properties createRemoteViaRsyncDestinationProperties()
    {
        final Properties properties = new Properties();
        properties.setProperty(RsyncDataSetCopier.DESTINATION_KEY, HOST + ":" + RSYNC_MODULE + ":"
                + destination.getPath());
        properties.setProperty(RsyncDataSetCopier.RSYNC_PASSWORD_FILE_KEY, "abc-password");
        properties.setProperty(RsyncDataSetCopier.RSYNC_EXEC + "-executable", rsyncExec.getPath());
        properties.setProperty(RsyncDataSetCopier.SSH_EXEC + "-executable", sshExec.getPath());
        return properties;
    }

    /*
     * --< COMMON >---------------------------------------------------------------------------------
     */

    private void assertSuccessful(Status status)
    {
        assertEquals(Status.OK, status);
    }

    private void assertError(Status status, String expectedErrorMessage)
    {
        assertTrue(status.isError());
        assertEquals(expectedErrorMessage, status.tryGetErrorMessage());
    }

    private void assertTrue(BooleanStatus boolStatus)
    {
        assertEquals(true, boolStatus.isSuccess());
        assertEquals(false, boolStatus.isError());
    }

    private void assertFalse(BooleanStatus boolStatus)
    {
        assertEquals(false, boolStatus.isSuccess());
        assertEquals(false, boolStatus.isError());
    }

    private void assertError(BooleanStatus boolStatus, String expectedErrorMessage)
    {
        assertEquals(false, boolStatus.isSuccess());
        assertEquals(true, boolStatus.isError());
        assertEquals(expectedErrorMessage, boolStatus.tryGetMessage());
    }

    private void assertDs1InStore()
    {
        assertEquals(true, ds1Data.exists());
        assertEquals(DATA1, FileUtilities.loadToString(ds1Data).trim());
    }

    private void assertDs2InStore()
    {
        assertEquals(true, ds2Data.exists());
        assertEquals(DATA2, FileUtilities.loadToString(ds2Data).trim());
    }

    private void assertDs1InArchive()
    {
        assertEquals(true, ds1ArchivedLocationFile.isDirectory());
        assertEquals(ds1Data.lastModified(), ds1ArchivedDataFile.lastModified());
        assertEquals(DATA1, FileUtilities.loadToString(ds1ArchivedDataFile).trim());
    }

    private void assertDs2InArchive()
    {
        assertEquals(true, ds2ArchivedLocationFile.isDirectory());
        assertEquals(ds2Data.lastModified(), ds2ArchivedDataFile.lastModified());
        assertEquals(DATA2, FileUtilities.loadToString(ds2ArchivedDataFile).trim());
    }

    private void assertDs1NotInArchive()
    {
        assertFalse(ds1ArchivedLocationFile.exists());
    }

    private void assertDs2NotInArchive()
    {
        assertFalse(ds2ArchivedLocationFile.exists());
    }

    private void prepareForCheckingLastModifiedDate()
    {
        // Sleep long enough to test last modified date of target will be same as of source.
        try
        {
            Thread.sleep(2000);
        } catch (InterruptedException ex)
        {
            // ignored
        }
    }

}
