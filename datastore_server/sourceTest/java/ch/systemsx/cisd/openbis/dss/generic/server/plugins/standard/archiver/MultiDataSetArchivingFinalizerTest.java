/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.BeanFactory;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.time.TimingParameters;
import ch.systemsx.cisd.common.utilities.MockTimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverDBTransaction;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDeleter;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProviderTestWrapper;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetCodesWithStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.DatasetDescriptionBuilder;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
public class MultiDataSetArchivingFinalizerTest extends AbstractFileSystemTestCase
{
    private static final String USER_ID = "test-user";

    private static final String USER_EMAIL = "a@bc.de";

    private BufferedAppender logRecorder;

    private Mockery context;

    private IDataStoreServiceInternal dssService;

    private MultiDataSetArchivingFinalizer finalizer;

    private Map<String, String> parameterBindings;

    private DataSetProcessingContext processingContext;

    private IDataSetDeleter dataSetDeleter;

    private List<DataSetCodesWithStatus> updatedStatus;

    private File dataFileInArchive;

    private File dataFileReplicated;

    private File dataFilePartiallyReplicated;

    private IEncapsulatedOpenBISService openBISService;

    private IMultiDataSetArchiverDBTransaction transaction;

    private MockCleaner cleaner;

    @BeforeMethod
    public void setUpTestEnvironment()
    {
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO, "OPERATION.*");
        context = new Mockery();
        BeanFactory beanFactory = context.mock(BeanFactory.class);
        ServiceProviderTestWrapper.setApplicationContext(beanFactory);
        dssService = ServiceProviderTestWrapper.mock(context, IDataStoreServiceInternal.class);
        openBISService = ServiceProviderTestWrapper.mock(context, IEncapsulatedOpenBISService.class);
        dataSetDeleter = context.mock(IDataSetDeleter.class);
        transaction = context.mock(IMultiDataSetArchiverDBTransaction.class);
        File archive = new File(workingDirectory, "archive");
        archive.mkdirs();
        dataFileInArchive = new File(archive, "data.txt");
        FileUtilities.writeToFile(dataFileInArchive, "hello archive");
        File replicate = new File(workingDirectory, "replicate");
        replicate.mkdirs();
        dataFileReplicated = new File(replicate, "data.txt");
        FileUtilities.writeToFile(dataFileReplicated, "hello archive");
        dataFilePartiallyReplicated = new File(replicate, "data2.txt");
        FileUtilities.writeToFile(dataFilePartiallyReplicated, "hello");
        parameterBindings = new LinkedHashMap<String, String>();
        parameterBindings.put(MultiDataSetArchivingFinalizer.ORIGINAL_FILE_PATH_KEY, dataFileInArchive.getPath());
        parameterBindings.put(MultiDataSetArchivingFinalizer.REPLICATED_FILE_PATH_KEY, dataFileReplicated.getPath());
        parameterBindings.put(MultiDataSetArchivingFinalizer.FINALIZER_POLLING_TIME_KEY, "20000");
        parameterBindings.put(MultiDataSetArchivingFinalizer.FINALIZER_MAX_WAITING_TIME_KEY, "300000");
        parameterBindings.put(MultiDataSetArchivingFinalizer.STATUS_KEY, DataSetArchivingStatus.ARCHIVED.toString());
        processingContext = new DataSetProcessingContext(null, null,
                parameterBindings, null, USER_ID, USER_EMAIL);
        updatedStatus = new ArrayList<DataSetCodesWithStatus>();
        cleaner = new MockCleaner();
        finalizer = new MultiDataSetArchivingFinalizer(null, new MockTimeProvider())
            {
                private static final long serialVersionUID = 1L;

                @Override
                protected void updateStatus(DataSetCodesWithStatus codesWithStatus)
                {
                    updatedStatus.add(codesWithStatus);
                }

                @Override
                IMultiDataSetArchiverDBTransaction getTransaction()
                {
                    return transaction;
                }

                @Override
                protected IMultiDataSetArchiveCleaner getCleaner()
                {
                    return cleaner;
                }
            };
        context.checking(new Expectations()
            {
                {
                    allowing(dssService).getDataSetDeleter();
                    will(returnValue(dataSetDeleter));
                }
            });
    }

    @AfterMethod
    public void checkMockExpectations(ITestResult result)
    {
        if (result.getStatus() == ITestResult.FAILURE)
        {
            String logContent = logRecorder.getLogContent();
            fail(result.getName() + " failed. Log content:\n" + logContent);
        }
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();

        ServiceProviderTestWrapper.restoreApplicationContext();
    }

    @Test
    public void testReplicationForArchiving()
    {
        DatasetDescription ds1 = new DatasetDescriptionBuilder("ds1").getDatasetDescription();
        prepareScheduleDeletion(ds1);

        ProcessingStatus status = finalizer.process(Arrays.asList(ds1), processingContext);

        assertEquals("INFO  OPERATION.MultiDataSetArchivingFinalizer - "
                + "Parameters: {original-file-path=" + dataFileInArchive.getPath()
                + ", replicated-file-path=" + dataFileReplicated.getPath() + ", "
                + "finalizer-polling-time=20000, finalizer-max-waiting-time=300000, status=ARCHIVED}\n"
                + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Waiting for replication of archive "
                + "'" + dataFileInArchive.getPath() + "' containing the following data sets: [ds1]\n"
                + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Condition fulfilled after < 1sec, condition: "
                + "13 bytes of 13 bytes are replicated for " + dataFileInArchive,
                logRecorder.getLogContent());
        assertEquals("OK", status.tryGetStatusByDataset(ds1.getDataSetCode()).toString());
        assertEquals("[[ds1] - ARCHIVED]", updatedStatus.toString());
        assertEquals(true, updatedStatus.get(0).isPresentInArchive());
        assertEquals("[]", cleaner.toString());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testReplicationForAddToArchiv()
    {
        DatasetDescription ds1 = new DatasetDescriptionBuilder("ds1").getDatasetDescription();
        prepareScheduleDeletion(ds1);
        parameterBindings.put(MultiDataSetArchivingFinalizer.STATUS_KEY, DataSetArchivingStatus.AVAILABLE.toString());
        
        ProcessingStatus status = finalizer.process(Arrays.asList(ds1), processingContext);
        
        assertEquals("INFO  OPERATION.MultiDataSetArchivingFinalizer - "
                + "Parameters: {original-file-path=" + dataFileInArchive.getPath()
                + ", replicated-file-path=" + dataFileReplicated.getPath() + ", "
                + "finalizer-polling-time=20000, finalizer-max-waiting-time=300000, status=AVAILABLE}\n"
                + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Waiting for replication of archive "
                + "'" + dataFileInArchive.getPath() + "' containing the following data sets: [ds1]\n"
                + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Condition fulfilled after < 1sec, condition: "
                + "13 bytes of 13 bytes are replicated for " + dataFileInArchive,
                logRecorder.getLogContent());
        assertEquals("OK", status.tryGetStatusByDataset(ds1.getDataSetCode()).toString());
        assertEquals("[[ds1] - AVAILABLE]", updatedStatus.toString());
        assertEquals(true, updatedStatus.get(0).isPresentInArchive());
        assertEquals("[]", cleaner.toString());
        context.assertIsSatisfied();
    }

    @Test
    public void testReplicationFailedForArchiving()
    {
        final DatasetDescription ds1 = new DatasetDescriptionBuilder("ds1").getDatasetDescription();
        final DatasetDescription ds2 = new DatasetDescriptionBuilder("ds2").getDatasetDescription();
        parameterBindings.put(MultiDataSetArchivingFinalizer.REPLICATED_FILE_PATH_KEY, dataFilePartiallyReplicated.getPath());
        context.checking(new Expectations()
            {
                {
                    one(transaction).deleteContainer(dataFileInArchive.getName());
                    one(transaction).commit();
                    one(transaction).close();
                    one(openBISService).archiveDataSets(Arrays.asList(ds1.getDataSetCode(), ds2.getDataSetCode()), true);
                }
            });

        ProcessingStatus status = finalizer.process(Arrays.asList(ds1, ds2), processingContext);

        assertEquals("INFO  OPERATION.MultiDataSetArchivingFinalizer - "
                + "Parameters: {original-file-path=" + dataFileInArchive.getPath()
                + ", replicated-file-path=" + dataFilePartiallyReplicated.getPath() + ", "
                + "finalizer-polling-time=20000, finalizer-max-waiting-time=300000, status=ARCHIVED}\n"
                + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Waiting for replication of archive "
                + "'" + dataFileInArchive.getPath() + "' containing the following data sets: [ds1, ds2]\n"
                + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Condition still not fulfilled after < 1sec, "
                + "condition: 5 bytes of 13 bytes are replicated for " + dataFileInArchive + "\n"
                + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Condition still not fulfilled after 2min, "
                + "condition: 5 bytes of 13 bytes are replicated for " + dataFileInArchive + "\n"
                + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Condition still not fulfilled after 5min, "
                + "condition: 5 bytes of 13 bytes are replicated for " + dataFileInArchive + "\n"
                + "ERROR OPERATION.MultiDataSetArchivingFinalizer - Replication of '"
                + dataFileInArchive.getPath() + "' failed.", logRecorder.getLogContent());
        assertEquals("ERROR: \"Replication of '" + dataFileInArchive.getPath() + "' failed.\"",
                status.tryGetStatusByDataset(ds1.getDataSetCode()).toString());
        assertEquals("ERROR: \"Replication of '" + dataFileInArchive.getPath() + "' failed.\"",
                status.tryGetStatusByDataset(ds2.getDataSetCode()).toString());
        assertEquals("[[ds1, ds2] - AVAILABLE]", updatedStatus.toString());
        assertEquals(false, updatedStatus.get(0).isPresentInArchive());
        assertEquals(Arrays.asList(dataFileInArchive, dataFilePartiallyReplicated).toString(), cleaner.toString());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testReplicationFailedForAddToArchive()
    {
        final DatasetDescription ds1 = new DatasetDescriptionBuilder("ds1").getDatasetDescription();
        final DatasetDescription ds2 = new DatasetDescriptionBuilder("ds2").getDatasetDescription();
        parameterBindings.put(MultiDataSetArchivingFinalizer.REPLICATED_FILE_PATH_KEY, dataFilePartiallyReplicated.getPath());
        parameterBindings.put(MultiDataSetArchivingFinalizer.STATUS_KEY, DataSetArchivingStatus.AVAILABLE.toString());
        context.checking(new Expectations()
            {
                {
                    one(transaction).deleteContainer(dataFileInArchive.getName());
                    one(transaction).commit();
                    one(transaction).close();
                    one(openBISService).archiveDataSets(Arrays.asList(ds1.getDataSetCode(), ds2.getDataSetCode()), false);
                }
            });

        ProcessingStatus status = finalizer.process(Arrays.asList(ds1, ds2), processingContext);
        
        assertEquals("INFO  OPERATION.MultiDataSetArchivingFinalizer - "
                + "Parameters: {original-file-path=" + dataFileInArchive.getPath()
                + ", replicated-file-path=" + dataFilePartiallyReplicated.getPath() + ", "
                + "finalizer-polling-time=20000, finalizer-max-waiting-time=300000, status=AVAILABLE}\n"
                + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Waiting for replication of archive "
                + "'" + dataFileInArchive.getPath() + "' containing the following data sets: [ds1, ds2]\n"
                + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Condition still not fulfilled after < 1sec, "
                + "condition: 5 bytes of 13 bytes are replicated for " + dataFileInArchive + "\n"
                + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Condition still not fulfilled after 2min, "
                + "condition: 5 bytes of 13 bytes are replicated for " + dataFileInArchive + "\n"
                + "INFO  OPERATION.MultiDataSetArchivingFinalizer - Condition still not fulfilled after 5min, "
                + "condition: 5 bytes of 13 bytes are replicated for " + dataFileInArchive + "\n"
                + "ERROR OPERATION.MultiDataSetArchivingFinalizer - Replication of '"
                + dataFileInArchive.getPath() + "' failed.", logRecorder.getLogContent());
        assertEquals("ERROR: \"Replication of '" + dataFileInArchive.getPath() + "' failed.\"",
                status.tryGetStatusByDataset(ds1.getDataSetCode()).toString());
        assertEquals("ERROR: \"Replication of '" + dataFileInArchive.getPath() + "' failed.\"",
                status.tryGetStatusByDataset(ds2.getDataSetCode()).toString());
        assertEquals("[[ds1, ds2] - AVAILABLE]", updatedStatus.toString());
        assertEquals(false, updatedStatus.get(0).isPresentInArchive());
        assertEquals(Arrays.asList(dataFileInArchive, dataFilePartiallyReplicated).toString(), cleaner.toString());
        context.assertIsSatisfied();
    }

    private void prepareScheduleDeletion(final DatasetDescription... descriptions)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataSetDeleter).scheduleDeletionOfDataSets(Arrays.asList(descriptions), TimingParameters.DEFAULT_MAXIMUM_RETRY_COUNT,
                            TimingParameters.DEFAULT_INTERVAL_TO_WAIT_AFTER_FAILURE_SECONDS);
                }
            });
    }
}
