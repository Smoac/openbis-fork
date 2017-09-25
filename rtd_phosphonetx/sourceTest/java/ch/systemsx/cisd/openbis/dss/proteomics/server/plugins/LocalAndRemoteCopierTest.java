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

package ch.systemsx.cisd.openbis.dss.proteomics.server.plugins;

import static ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.DataSetCopier.SSH_TIMEOUT_MILLIS;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.concurrent.ExecutionResult;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.filesystem.ssh.ISshCommandExecutor;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.common.utilities.ITextHandler;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.DataSetCopier;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.IPathCopierFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.ISshCommandExecutorFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.RSyncConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = LocalAndRemoteCopier.class)
public class LocalAndRemoteCopierTest extends AbstractFileSystemTestCase
{
    private static final ProcessResult OK_RESULT = new ProcessResult(Arrays.asList(""), 0, null,
            ExecutionResult.create(null), null, 0, (List<String>) null, null, null, null);

    private static final String SAMPLE_CODE = "my-sample";

    private static final DataSetType DATA_SET_TYPE = new DataSetType("MY");

    private static final DataSetKind DATA_SET_KIND = DataSetKind.PHYSICAL;

    private static final String DATA_SET_CODE = "my-dataset-123";

    private static final String FOLDER_NAME = DATA_SET_CODE;

    private static final String DATA = "hello test";

    private Mockery context;

    private File dataSet;

    private IPathCopierFactory copierFactory;

    private IPathCopier copier;

    private ISshCommandExecutorFactory sshExecutorFactory;

    private ISshCommandExecutor sshExecutor;

    private File destination;

    private File dataFile;

    private File rsyncExec;

    private File sshExec;

    @BeforeMethod
    public void beforeMethod() throws Exception
    {
        context = new Mockery();
        copierFactory = context.mock(IPathCopierFactory.class);
        copier = context.mock(IPathCopier.class);
        sshExecutorFactory = context.mock(ISshCommandExecutorFactory.class);
        sshExecutor = context.mock(ISshCommandExecutor.class);
        dataSet = new File(workingDirectory, "data-set");
        dataSet.mkdirs();
        dataFile = new File(dataSet, "data.txt");
        FileUtilities.writeToFile(dataFile, DATA);
        destination = new File(workingDirectory, "destination");
        destination.mkdirs();
        rsyncExec = new File(workingDirectory, "my-rsync");
        rsyncExec.createNewFile();
        sshExec = new File(workingDirectory, "my-rssh");
        sshExec.createNewFile();
    }

    @AfterMethod
    public void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testLocalWithKnownSample()
    {
        Properties properties = new Properties();
        prepareCopier(properties);
        properties.setProperty(DataSetCopier.DESTINATION_KEY, destination.getPath());
        LocalAndRemoteCopier msInjectionCopier =
                new LocalAndRemoteCopier(properties, copierFactory, sshExecutorFactory);
        prepareForCheckingLastModifiedDate();

        DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setDataSetCode(DATA_SET_CODE);
        dataSetInformation.setDataSetType(DATA_SET_TYPE);
        dataSetInformation.setDataSetKind(DATA_SET_KIND);
        HashMap<String, String> parameterBindings = new HashMap<String, String>();
        parameterBindings.put(DATA_SET_CODE, SAMPLE_CODE);
        Status status = msInjectionCopier.handle(dataSet, dataSetInformation, parameterBindings);

        assertEquals(Status.OK, status);
        File copiedDataSet = new File(destination, FOLDER_NAME);
        assertEquals(true, copiedDataSet.isDirectory());
        assertEquals(dataSet.lastModified(), copiedDataSet.lastModified());
        assertEquals(DATA, FileUtilities.loadToString(new File(copiedDataSet, dataFile.getName()))
                .trim());

        context.assertIsSatisfied();
    }

    @Test
    public void testLocalWithUnknownSample()
    {
        Properties properties = new Properties();
        prepareCopier(properties);
        properties.setProperty(DataSetCopier.DESTINATION_KEY, destination.getPath());
        LocalAndRemoteCopier msInjectionCopier =
                new LocalAndRemoteCopier(properties, copierFactory, sshExecutorFactory);

        DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setDataSetCode(DATA_SET_CODE);
        dataSetInformation.setDataSetType(DATA_SET_TYPE);
        dataSetInformation.setDataSetKind(DATA_SET_KIND);
        HashMap<String, String> parameterBindings = new HashMap<String, String>();
        Status status = msInjectionCopier.handle(dataSet, dataSetInformation, parameterBindings);

        assertEquals(Status.OK, status);
        File copiedDataSet = new File(destination, DATA_SET_CODE);
        assertEquals(true, copiedDataSet.isDirectory());

        context.assertIsSatisfied();
    }

    @Test
    public void testLocalWithAlreadyExistingDestination()
    {
        Properties properties = new Properties();
        prepareCopier(properties);
        properties.setProperty(DataSetCopier.DESTINATION_KEY, destination.getPath());
        LocalAndRemoteCopier msInjectionCopier =
                new LocalAndRemoteCopier(properties, copierFactory, sshExecutorFactory);
        File copiedDataSet = new File(destination, FOLDER_NAME);
        copiedDataSet.mkdirs();
        File dummy = new File(copiedDataSet, "dummy");
        FileUtilities.writeToFile(dummy, "hello");

        DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setDataSetCode(DATA_SET_CODE);
        dataSetInformation.setDataSetType(DATA_SET_TYPE);
        dataSetInformation.setDataSetKind(DATA_SET_KIND);
        HashMap<String, String> parameterBindings = new HashMap<String, String>();
        parameterBindings.put(DATA_SET_CODE, SAMPLE_CODE);
        Status status = msInjectionCopier.handle(dataSet, dataSetInformation, parameterBindings);

        assertEquals(Status.OK, status);
        assertEquals(true, copiedDataSet.isDirectory());
        assertEquals(DATA, FileUtilities.loadToString(new File(copiedDataSet, dataFile.getName()))
                .trim());
        assertEquals(false, dummy.exists());

        context.assertIsSatisfied();
    }

    @Test
    public void testRemoteWithMarkerFile()
    {
        Properties properties = new Properties();
        properties.setProperty(LocalAndRemoteCopier.MARKER_FILE_PREFIX, "MARKER-");
        properties.setProperty(DataSetCopier.DESTINATION_KEY, "localhost:" + destination.getPath());
        properties.setProperty(DataSetCopier.RSYNC_EXEC + "-executable", rsyncExec.getPath());
        properties.setProperty(DataSetCopier.SSH_EXEC + "-executable", sshExec.getPath());
        context.checking(new Expectations()
            {
                {
                    one(copierFactory).create(rsyncExec, sshExec, DataSetCopier.SSH_TIMEOUT_MILLIS,
                            RSyncConfig.getInstance().getAdditionalCommandLineOptions());
                    will(returnValue(copier));

                    one(copier).check();
                    one(copier).checkRsyncConnectionViaSsh("localhost", null,
                            DataSetCopier.SSH_TIMEOUT_MILLIS);
                    will(returnValue(true));

                    one(sshExecutorFactory).create(sshExec, "localhost");
                    will(returnValue(sshExecutor));

                    final String copiedDataSet = new File(destination, FOLDER_NAME).getPath();
                    one(sshExecutor).exists(copiedDataSet, SSH_TIMEOUT_MILLIS);
                    will(returnValue(BooleanStatus.createTrue()));

                    one(sshExecutor).executeCommandRemotely("rm -rf " + copiedDataSet,
                            SSH_TIMEOUT_MILLIS);
                    will(returnValue(OK_RESULT));

                    one(copier).copyToRemote(dataSet, destination.getPath(), "localhost", null, null, null, null);
                    will(returnValue(Status.OK));

                    one(sshExecutor).executeCommandRemotely(
                            "mv " + new File(destination, dataSet.getName()) + " " + copiedDataSet,
                            SSH_TIMEOUT_MILLIS);
                    will(returnValue(OK_RESULT));

                    one(sshExecutor).executeCommandRemotely(
                            "touch " + new File(destination, "MARKER-" + FOLDER_NAME),
                            SSH_TIMEOUT_MILLIS);
                    will(returnValue(OK_RESULT));
                }
            });
        LocalAndRemoteCopier msInjectionCopier =
                new LocalAndRemoteCopier(properties, copierFactory, sshExecutorFactory);

        DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setDataSetCode(DATA_SET_CODE);
        dataSetInformation.setDataSetType(DATA_SET_TYPE);
        dataSetInformation.setDataSetKind(DATA_SET_KIND);
        HashMap<String, String> parameterBindings = new HashMap<String, String>();
        parameterBindings.put(DATA_SET_CODE, SAMPLE_CODE);
        Status status = msInjectionCopier.handle(dataSet, dataSetInformation, parameterBindings);

        assertEquals(Status.OK, status);
        context.assertIsSatisfied();
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

    private void prepareCopier(Properties properties)
    {
        properties.setProperty(DataSetCopier.RSYNC_EXEC + "-executable", rsyncExec.getPath());
        context.checking(new Expectations()
            {
                {
                    one(copierFactory).create(rsyncExec, null, DataSetCopier.SSH_TIMEOUT_MILLIS,
                            RSyncConfig.getInstance().getAdditionalCommandLineOptions());
                    will(returnValue(new MockCopier()));
                }
            });
    }

    private final class MockCopier implements IPathCopier
    {

        @Override
        public boolean isProgressEnabled()
        {
            return false;
        }

        @Override
        public boolean terminate()
        {
            return false;
        }

        @Override
        public boolean isRemote()
        {
            return false;
        }

        @Override
        public void check() throws EnvironmentFailureException, ConfigurationFailureException
        {
        }

        @Override
        public Status copy(File sourcePath, File destinationDirectory,
                ITextHandler stdoutHandlerOrNull, ITextHandler stderrHandlerOrNull)
        {
            return null;
        }

        @Override
        public Status copyContent(File sourcePath, File destinationDirectory,
                ITextHandler stdoutHandlerOrNull, ITextHandler stderrHandlerOrNull)
        {
            return null;
        }

        @Override
        public Status copyToRemote(File sourcePath, String destinationDirectory,
                String destinationHostOrNull, String rsyncModuleNameOrNull,
                String rsyncPasswordFileOrNull,
                ITextHandler stdoutHandlerOrNull, ITextHandler stderrHandlerOrNull)
        {
            try
            {
                FileUtils.copyDirectoryToDirectory(dataSet, new File(destinationDirectory));
            } catch (IOException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
            File file = new File(destinationDirectory, dataSet.getName());
            file.setLastModified(dataSet.lastModified());
            return Status.OK;
        }

        @Override
        public Status copyContentToRemote(File sourcePath, String destinationDirectory,
                String destinationHostOrNull, String rsyncModuleNameOrNull,
                String rsyncPasswordFileOrNull,
                ITextHandler stdoutHandlerOrNull, ITextHandler stderrHandlerOrNull)
        {
            return null;
        }

        @Override
        public Status copyFromRemote(String sourcePath, String sourceHost,
                File destinationDirectory, String rsyncModuleNameOrNull,
                String rsyncPasswordFileOrNull,
                ITextHandler stdoutHandlerOrNull, ITextHandler stderrHandlerOrNull)
        {
            return null;
        }

        @Override
        public Status copyContentFromRemote(String sourcePath, String sourceHost,
                File destinationDirectory, String rsyncModuleNameOrNull,
                String rsyncPasswordFileOrNull,
                ITextHandler stdoutHandlerOrNull, ITextHandler stderrHandlerOrNull)
        {
            return null;
        }

        @Override
        public boolean checkRsyncConnectionViaSsh(String host, String rsyncExecutableOnHostOrNull,
                long millisToWaitForCompletion)
        {
            return false;
        }

        @Override
        public boolean checkRsyncConnectionViaRsyncServer(String host, String rsyncModule,
                String rsyncPassworFileOrNull, long millisToWaitForCompletion)
        {
            return false;
        }

    }

}
