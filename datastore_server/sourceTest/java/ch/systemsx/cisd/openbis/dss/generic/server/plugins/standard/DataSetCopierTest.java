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

import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.DataSetCopier.DESTINATION_KEY;
import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.DataSetCopier.RSYNC_PASSWORD_FILE_KEY;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.filesystem.ssh.ISshCommandExecutor;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.DataSetCopier.IPathCopierFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.DataSetCopier.ISshCommandExecutorFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ProcessingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = DataSetCopier.class)
public class DataSetCopierTest extends AbstractFileSystemTestCase
{
    private static final String DS1_LOCATION = "ds1";

    private static final String DS2_LOCATION = "ds2";

    private Mockery context;

    private IPathCopierFactory pathFactory;

    private ISshCommandExecutorFactory sshFactory;

    private IPathCopier copier;

    private ISshCommandExecutor sshCommandExecutor;

    private File storeRoot;

    private File sshExecutableDummy;

    private File rsyncExecutableDummy;

    private Properties properties;

    private DatasetDescription ds1;

    private File ds1Data;

    private DatasetDescription ds2;

    private File ds2Data;

    @BeforeMethod
    public void beforeMethod() throws IOException
    {
        context = new Mockery();
        pathFactory = context.mock(DataSetCopier.IPathCopierFactory.class);
        sshFactory = context.mock(DataSetCopier.ISshCommandExecutorFactory.class);
        copier = context.mock(IPathCopier.class);
        sshCommandExecutor = context.mock(ISshCommandExecutor.class);
        storeRoot = new File(workingDirectory, "store");
        storeRoot.mkdirs();
        sshExecutableDummy = new File(workingDirectory, "my-ssh");
        sshExecutableDummy.createNewFile();
        rsyncExecutableDummy = new File(workingDirectory, "my-rsync");
        rsyncExecutableDummy.createNewFile();
        properties = new Properties();
        properties.setProperty("ssh-executable", sshExecutableDummy.getPath());
        properties.setProperty("rsync-executable", rsyncExecutableDummy.getPath());
        ds1 = new DatasetDescription("ds1", DS1_LOCATION, "s", "g", "p", "e", null, null);
        File ds1Folder = new File(storeRoot, DS1_LOCATION + "/original");
        ds1Folder.mkdirs();
        ds1Data = new File(ds1Folder, "data.txt");
        ds1Data.createNewFile();
        ds2 = new DatasetDescription("ds2", DS2_LOCATION, "s", "g", "p", "e", null, null);
        File ds2Folder = new File(storeRoot, DS2_LOCATION + "/original");
        ds2Folder.mkdirs();
        ds2Data = new File(ds2Folder, "images");
        ds2Data.mkdirs();
    }

    @AfterMethod
    public void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testMissingDestinationProperty()
    {
        try
        {
            new DataSetCopier(new Properties(), storeRoot, pathFactory, sshFactory);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Given key '" + DESTINATION_KEY + "' not found in properties '[]'", ex
                    .getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testMissingRsyncExecutableFile()
    {
        rsyncExecutableDummy.delete();
        try
        {
            new DataSetCopier(properties, storeRoot, pathFactory, sshFactory);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Path to executable 'rsync' is not a file: "
                    + rsyncExecutableDummy.getAbsolutePath(), ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testMissingSshExecutableFile()
    {
        sshExecutableDummy.delete();
        try
        {
            new DataSetCopier(properties, storeRoot, pathFactory, sshFactory);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Path to executable 'ssh' is not a file: "
                    + sshExecutableDummy.getAbsolutePath(), ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testFailingSshConnection()
    {
        properties.setProperty(DESTINATION_KEY, "host:tmp/test");
        prepareCreateAndCheckCopier("host", null, false);
        try
        {
            new DataSetCopier(properties, storeRoot, pathFactory, sshFactory);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("No good rsync executable found on host 'host'", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testFailingRsyncConnection()
    {
        properties.setProperty(DESTINATION_KEY, "host:abc:tmp/test");
        properties.setProperty(RSYNC_PASSWORD_FILE_KEY, "abc-password");
        prepareCreateAndCheckCopier("host", "abc", false);
        try
        {
            new DataSetCopier(properties, storeRoot, pathFactory, sshFactory);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Connection to rsync module host::abc failed", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testCopyTwoDataSetsLocally()
    {
        properties.setProperty(DESTINATION_KEY, "tmp/test");
        prepareCreateAndCheckCopier(null, null, true);
        context.checking(new Expectations()
            {
                {
                    one(copier).copyToRemote(ds1Data, new File("tmp/test"), null, null, null);
                    will(returnValue(Status.OK));
                    one(copier).copyToRemote(ds2Data, new File("tmp/test"), null, null, null);
                    will(returnValue(Status.OK));
                }
            });
        DataSetCopier dataSetCopier =
                new DataSetCopier(properties, storeRoot, pathFactory, sshFactory);

        dataSetCopier.process(Arrays.asList(ds1, ds2));

        context.assertIsSatisfied();
    }

    @Test
    public void testCopyLocallyFails()
    {
        properties.setProperty(DESTINATION_KEY, "tmp/test");
        prepareCreateAndCheckCopier(null, null, true);
        context.checking(new Expectations()
            {
                {
                    one(copier).copyToRemote(ds1Data, new File("tmp/test"), null, null, null);
                    will(returnValue(Status.createError("error message")));

                    one(copier).copyToRemote(ds2Data, new File("tmp/test"), null, null, null);
                    will(returnValue(Status.OK));
                }
            });
        DataSetCopier dataSetCopier =
                new DataSetCopier(properties, storeRoot, pathFactory, sshFactory);

        ProcessingStatus processingStatus = dataSetCopier.process(Arrays.asList(ds1, ds2));

        // processing first data set fails but second one is processed successfully
        Status errorStatus = Status.createError(DataSetCopier.COPYING_FAILED_MSG);
        assertEquals(1, processingStatus.getErrorStatuses().size());
        assertEquals(errorStatus, processingStatus.getErrorStatuses().get(0));
        assertEquals(1, processingStatus.getDatasetsByStatus(errorStatus).size());
        assertEquals(ds1, processingStatus.getDatasetsByStatus(errorStatus).get(0));
        assertEquals(1, processingStatus.getDatasetsByStatus(Status.OK).size());
        assertEquals(ds2, processingStatus.getDatasetsByStatus(Status.OK).get(0));

        context.assertIsSatisfied();
    }

    @Test
    public void testCopyRemotelyViaSsh()
    {
        properties.setProperty(DESTINATION_KEY, "host:tmp/test");
        prepareCreateAndCheckCopier("host", null, true);
        context.checking(new Expectations()
            {
                {
                    one(sshCommandExecutor).exists(new File("tmp/test/data.txt"),
                            DataSetCopier.SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createFalse()));
                    one(copier).copyToRemote(ds1Data, new File("tmp/test"), "host", null, null);
                    will(returnValue(Status.OK));
                }
            });
        DataSetCopier dataSetCopier =
                new DataSetCopier(properties, storeRoot, pathFactory, sshFactory);

        dataSetCopier.process(Arrays.asList(ds1));

        context.assertIsSatisfied();
    }

    @Test
    public void testCopyRemotelyViaSshWithErrors()
    {
        properties.setProperty(DESTINATION_KEY, "host:tmp/test");
        prepareCreateAndCheckCopier("host", null, true);
        context.checking(new Expectations()
            {
                {
                    one(sshCommandExecutor).exists(new File("tmp/test/data.txt"),
                            DataSetCopier.SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createFalse()));
                    one(copier).copyToRemote(ds1Data, new File("tmp/test"), "host", null, null);
                    will(returnValue(Status.createError("error message")));

                    one(sshCommandExecutor).exists(new File("tmp/test/images"),
                            DataSetCopier.SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createFalse()));
                    one(copier).copyToRemote(ds2Data, new File("tmp/test"), "host", null, null);
                    will(returnValue(Status.OK));
                }
            });
        DataSetCopier dataSetCopier =
                new DataSetCopier(properties, storeRoot, pathFactory, sshFactory);

        ProcessingStatus processingStatus = dataSetCopier.process(Arrays.asList(ds1, ds2));

        // processing first data set fails but second one is processed successfully
        Status errorStatus = Status.createError(DataSetCopier.COPYING_FAILED_MSG);
        assertEquals(1, processingStatus.getErrorStatuses().size());
        assertEquals(errorStatus, processingStatus.getErrorStatuses().get(0));
        assertEquals(1, processingStatus.getDatasetsByStatus(errorStatus).size());
        assertEquals(ds1, processingStatus.getDatasetsByStatus(errorStatus).get(0));
        assertEquals(1, processingStatus.getDatasetsByStatus(Status.OK).size());
        assertEquals(ds2, processingStatus.getDatasetsByStatus(Status.OK).get(0));

        context.assertIsSatisfied();
    }

    @Test
    public void testCopyRemotelyViaRsyncServer()
    {
        properties.setProperty(DESTINATION_KEY, "host:abc:tmp/test");
        properties.setProperty(RSYNC_PASSWORD_FILE_KEY, "abc-password");
        prepareCreateAndCheckCopier("host", "abc", true);
        context.checking(new Expectations()
            {
                {
                    one(sshCommandExecutor).exists(new File("tmp/test/data.txt"),
                            DataSetCopier.SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createFalse()));
                    one(copier).copyToRemote(ds1Data, new File("tmp/test"), "host", "abc",
                            "abc-password");
                    will(returnValue(Status.OK));
                }
            });
        DataSetCopier dataSetCopier =
                new DataSetCopier(properties, storeRoot, pathFactory, sshFactory);

        dataSetCopier.process(Arrays.asList(ds1));

        context.assertIsSatisfied();
    }

    @Test
    public void testCopyRemotelyViaRsyncServerWithErrors()
    {
        properties.setProperty(DESTINATION_KEY, "host:abc:tmp/test");
        properties.setProperty(RSYNC_PASSWORD_FILE_KEY, "abc-password");
        prepareCreateAndCheckCopier("host", "abc", true);
        context.checking(new Expectations()
            {
                {
                    one(sshCommandExecutor).exists(new File("tmp/test/data.txt"),
                            DataSetCopier.SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createFalse()));
                    one(copier).copyToRemote(ds1Data, new File("tmp/test"), "host", "abc",
                            "abc-password");
                    will(returnValue(Status.createError("error message")));

                    one(sshCommandExecutor).exists(new File("tmp/test/images"),
                            DataSetCopier.SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createFalse()));
                    one(copier).copyToRemote(ds2Data, new File("tmp/test"), "host", "abc",
                            "abc-password");
                    will(returnValue(Status.OK));
                }
            });
        DataSetCopier dataSetCopier =
                new DataSetCopier(properties, storeRoot, pathFactory, sshFactory);

        ProcessingStatus processingStatus = dataSetCopier.process(Arrays.asList(ds1, ds2));

        // processing first data set fails but second one is processed successfully
        Status errorStatus = Status.createError(DataSetCopier.COPYING_FAILED_MSG);
        assertEquals(1, processingStatus.getErrorStatuses().size());
        assertEquals(errorStatus, processingStatus.getErrorStatuses().get(0));
        assertEquals(1, processingStatus.getDatasetsByStatus(errorStatus).size());
        assertEquals(ds1, processingStatus.getDatasetsByStatus(errorStatus).get(0));
        assertEquals(1, processingStatus.getDatasetsByStatus(Status.OK).size());
        assertEquals(ds2, processingStatus.getDatasetsByStatus(Status.OK).get(0));

        context.assertIsSatisfied();
    }

    private void prepareCreateAndCheckCopier(final String hostOrNull,
            final String rsyncModuleOrNull, final boolean checkingResult)
    {
        context.checking(new Expectations()
            {
                {
                    one(pathFactory).create(rsyncExecutableDummy, sshExecutableDummy);
                    will(returnValue(copier));

                    one(sshFactory).create(sshExecutableDummy, hostOrNull);
                    will(returnValue(sshCommandExecutor));

                    one(copier).check();
                    if (hostOrNull != null)
                    {
                        if (rsyncModuleOrNull != null)
                        {
                            one(copier).checkRsyncConnectionViaRsyncServer(hostOrNull,
                                    rsyncModuleOrNull, rsyncModuleOrNull + "-password");
                        } else
                        {
                            one(copier).checkRsyncConnectionViaSsh(hostOrNull, null);
                        }
                        will(returnValue(checkingResult));
                    }
                }
            });
    }
}
