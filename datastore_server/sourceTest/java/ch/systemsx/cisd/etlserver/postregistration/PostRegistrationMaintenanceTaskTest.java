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

import static ch.systemsx.cisd.etlserver.postregistration.PostRegistrationMaintenanceTask.CLEANUP_TASKS_FOLDER_PROPERTY;
import static ch.systemsx.cisd.etlserver.postregistration.PostRegistrationMaintenanceTask.LAST_SEEN_DATA_SET_FILE_PROPERTY;
import static ch.systemsx.cisd.etlserver.postregistration.PostRegistrationMaintenanceTask.POST_REGISTRATION_TASKS_PROPERTY;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.springframework.beans.factory.BeanFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses=PostRegistrationMaintenanceTask.class)
public class PostRegistrationMaintenanceTaskTest extends AbstractFileSystemTestCase
{
    private static final String TASK_1_NAME = "task 1";
    private static final String TASK_2_NAME = "task 2";

    private static final String TASK_NAME_PROPERTY = "task-name";
    
    private static final class MockCleanupTask implements ICleanupTask
    {
        private static final long serialVersionUID = 1L;
        private final String name;
        
        public MockCleanupTask(String name)
        {
            this.name = name;
        }

        public void cleanup()
        {
            cleanupInvocations.add(name);
        }
        
    }
    
    public static final class MockPostRegistrationTask extends AbstractPostRegistrationTask
    {
        private final IPostRegistrationTask task;
        
        public MockPostRegistrationTask(Properties properties, IEncapsulatedOpenBISService service)
        {
            super(properties, service);
            String taskName = properties.getProperty(TASK_NAME_PROPERTY);
            task = mockTasks.get(taskName);
            assertNotNull("No task found for '" + taskName + "'.", task);
        }
        
        public boolean requiresDataStoreLock()
        {
            return task.requiresDataStoreLock();
        }

        public IPostRegistrationTaskExecutor createExecutor(String dataSetCode)
        {
            return task.createExecutor(dataSetCode);
        }

    }
    
    private static Map<String, IPostRegistrationTask> mockTasks = new HashMap<String, IPostRegistrationTask>();
    private static List<String> cleanupInvocations = new ArrayList<String>();
    
    private BufferedAppender logRecorder;
    private Mockery context;

    private IEncapsulatedOpenBISService service;
    
    private ICleanupTask cleanupTask;
    private IPostRegistrationTask task1;
    private IPostRegistrationTaskExecutor executor1;
    private IPostRegistrationTask task2;
    private IPostRegistrationTaskExecutor executor2;
    private File cleanupTasksFolder;
    private File lastSeenDataSetFile;

    @BeforeMethod
    public void beforeMethod()
    {
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.INFO);
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        cleanupTask = new MockCleanupTask("1");
        cleanupInvocations.clear();
        mockTasks.clear();
        task1 = context.mock(IPostRegistrationTask.class, TASK_1_NAME);
        executor1 = context.mock(IPostRegistrationTaskExecutor.class, "executor 1");
        mockTasks.put(TASK_1_NAME, task1);
        task2 = context.mock(IPostRegistrationTask.class, TASK_2_NAME);
        executor2 = context.mock(IPostRegistrationTaskExecutor.class, "executor 2");
        mockTasks.put(TASK_2_NAME, task2);
        
        final BeanFactory beanFactory = context.mock(BeanFactory.class);
        context.checking(new Expectations()
            {
                {
                    allowing(beanFactory).getBean("openBIS-service");
                    will(returnValue(service));
                }
            });
        ServiceProvider.setBeanFactory(beanFactory);
        cleanupTasksFolder = new File(workingDirectory, "cleanup-tasks");
        lastSeenDataSetFile = new File(workingDirectory, "last-seen-data-set.txt");
    }
    
    @AfterMethod
    public void afterMethod()
    {
        logRecorder.reset();
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testRequireLockLogic()
    {
        Properties properties = createDefaultProperties();
        context.checking(new Expectations()
            {
                {
                    one(task1).requiresDataStoreLock();
                    will(returnValue(false));
                    one(task2).requiresDataStoreLock();
                    will(returnValue(true));
                }
            });

        PostRegistrationMaintenanceTask maintenanceTask = new PostRegistrationMaintenanceTask();
        maintenanceTask.setUp("post-registration", properties);

        assertEquals("", logRecorder.getLogContent());
        assertEquals(true, maintenanceTask.requiresDataStoreLock());
        assertEmptyCleanupTaskFolder();
        assertNoUexpectedInvocations();
        context.assertIsSatisfied();
    }
    
    @Test
    public void testLastSeenDataSetFile()
    {
        Properties properties = createDefaultProperties();
        final RecordingMatcher<TrackingDataSetCriteria> criteriaMatcher =
                new RecordingMatcher<TrackingDataSetCriteria>();
        context.checking(new Expectations()
            {
                {
                    one(task1).requiresDataStoreLock();
                    will(returnValue(true));
                    one(task2).requiresDataStoreLock();
                    will(returnValue(false));

                    one(service).listNewerDataSets(with(criteriaMatcher));
                    will(returnValue(Arrays.asList()));
                }
            });
        FileUtilities.writeToFile(lastSeenDataSetFile, "42");
        
        PostRegistrationMaintenanceTask maintenanceTask = new PostRegistrationMaintenanceTask();
        maintenanceTask.setUp("post-registration", properties);
        maintenanceTask.execute();
        
        assertEquals("", logRecorder.getLogContent());
        assertEquals(42, criteriaMatcher.recordedObject().getLastSeenDataSetId());
        assertEquals("42", FileUtilities.loadExactToString(lastSeenDataSetFile).trim());
        assertEquals(0, cleanupInvocations.size());
        assertEquals(true, maintenanceTask.requiresDataStoreLock());
        assertEmptyCleanupTaskFolder();
        assertNoUexpectedInvocations();
        context.assertIsSatisfied();
    }

    @Test
    public void testCleanup() throws IOException
    {
        Properties properties = createDefaultProperties();
        final RecordingMatcher<TrackingDataSetCriteria> criteriaMatcher =
                new RecordingMatcher<TrackingDataSetCriteria>();
        context.checking(new Expectations()
            {
                {
                    one(task1).requiresDataStoreLock();
                    will(returnValue(true));
                    one(task2).requiresDataStoreLock();
                    will(returnValue(true));

                    one(service).listNewerDataSets(with(criteriaMatcher));
                    will(returnValue(Arrays.asList()));
                }
            });
        File cleanupFile1 = new File(cleanupTasksFolder, "ds-1_1.ser");
        FileUtils.writeByteArrayToFile(cleanupFile1, SerializationUtils.serialize(cleanupTask));
        File file = new File(cleanupTasksFolder, "blabla.txt");
        FileUtilities.writeToFile(file, "hello world");
        File cleanupFile2 = new File(cleanupTasksFolder, "blabla.ser");
        FileUtilities.writeToFile(cleanupFile2, "hello world");
        
        PostRegistrationMaintenanceTask maintenanceTask = new PostRegistrationMaintenanceTask();
        maintenanceTask.setUp("post-registration", properties);
        maintenanceTask.execute();
        
        AssertionUtil.assertContains("ERROR OPERATION.PostRegistrationMaintenanceTask - "
                + "Couldn't performed clean up task " + cleanupFile2, logRecorder.getLogContent());
        assertEquals(0, criteriaMatcher.recordedObject().getLastSeenDataSetId());
        assertEquals(true, maintenanceTask.requiresDataStoreLock());
        assertEquals(1, cleanupInvocations.size());
        assertEquals(false, cleanupFile1.exists());
        assertEquals(true, file.exists());
        assertEquals(false, cleanupFile2.exists());
        assertNoUexpectedInvocations();
        context.assertIsSatisfied();
    }
    
    @Test
    public void testExecute()
    {
        Properties properties = createDefaultProperties();
        final RecordingMatcher<TrackingDataSetCriteria> criteriaMatcher =
                new RecordingMatcher<TrackingDataSetCriteria>();
        final Sequence sequence = context.sequence("tasks");
        context.checking(new Expectations()
            {
                {
                    one(task1).requiresDataStoreLock();
                    will(returnValue(false));
                    one(task2).requiresDataStoreLock();
                    will(returnValue(false));

                    one(service).listNewerDataSets(with(criteriaMatcher));
                    DataSetBuilder ds1 = new DataSetBuilder(1).code("ds-1");
                    DataSetBuilder ds2 = new DataSetBuilder(2).code("ds-2");
                    will(returnValue(Arrays.asList(ds2.getDataSet(), ds1.getDataSet())));

                    one(task1).createExecutor("ds-1");
                    will(returnValue(executor1));
                    inSequence(sequence);
                    one(executor1).createCleanupTask();
                    will(returnValue(cleanupTask));
                    inSequence(sequence);
                    one(executor1).execute();
                    inSequence(sequence);
                    one(task2).createExecutor("ds-1");
                    will(returnValue(executor2));
                    inSequence(sequence);
                    one(executor2).createCleanupTask();
                    will(returnValue(cleanupTask));
                    inSequence(sequence);
                    one(executor2).execute();
                    inSequence(sequence);

                    one(task1).createExecutor("ds-2");
                    will(returnValue(executor1));
                    inSequence(sequence);
                    one(executor1).createCleanupTask();
                    will(returnValue(cleanupTask));
                    inSequence(sequence);
                    one(executor1).execute();
                    inSequence(sequence);
                    one(task2).createExecutor("ds-2");
                    will(returnValue(executor2));
                    inSequence(sequence);
                    one(executor2).createCleanupTask();
                    will(returnValue(cleanupTask));
                    inSequence(sequence);
                    one(executor2).execute();
                    inSequence(sequence);
                }
            });

        PostRegistrationMaintenanceTask maintenanceTask = new PostRegistrationMaintenanceTask();
        maintenanceTask.setUp("post-registration", properties);
        assertEquals(false, maintenanceTask.requiresDataStoreLock());
        maintenanceTask.execute();

        assertEquals("", logRecorder.getLogContent());
        assertEquals(0, criteriaMatcher.recordedObject().getLastSeenDataSetId());
        assertEquals("2", FileUtilities.loadExactToString(lastSeenDataSetFile).trim());
        assertEquals(0, cleanupInvocations.size());
        assertEmptyCleanupTaskFolder();
        assertNoUexpectedInvocations();
        context.assertIsSatisfied();
    }

    
    @Test
    public void testExecuteWithExceptionThrown()
    {
        Properties properties = createDefaultProperties();
        final RecordingMatcher<TrackingDataSetCriteria> criteriaMatcher =
                new RecordingMatcher<TrackingDataSetCriteria>();
        final Sequence sequence = context.sequence("tasks");
        context.checking(new Expectations()
            {
                {
                    one(task1).requiresDataStoreLock();
                    will(returnValue(false));
                    one(task2).requiresDataStoreLock();
                    will(returnValue(false));

                    one(service).listNewerDataSets(with(criteriaMatcher));
                    DataSetBuilder ds1 = new DataSetBuilder(1).code("ds-1");
                    DataSetBuilder ds2 = new DataSetBuilder(2).code("ds-2");
                    DataSetBuilder ds3 = new DataSetBuilder(3).code("ds-3");
                    will(returnValue(Arrays.asList(ds2.getDataSet(), ds3.getDataSet(), ds1.getDataSet())));

                    one(task1).createExecutor("ds-1");
                    will(returnValue(executor1));
                    inSequence(sequence);
                    one(executor1).createCleanupTask();
                    will(returnValue(new MockCleanupTask("A")));
                    inSequence(sequence);
                    one(executor1).execute();
                    inSequence(sequence);
                    one(task2).createExecutor("ds-1");
                    will(returnValue(executor2));
                    inSequence(sequence);
                    one(executor2).createCleanupTask();
                    will(returnValue(new MockCleanupTask("B")));
                    inSequence(sequence);
                    one(executor2).execute();
                    inSequence(sequence);

                    one(task1).createExecutor("ds-2");
                    will(returnValue(executor1));
                    inSequence(sequence);
                    one(executor1).createCleanupTask();
                    will(returnValue(new MockCleanupTask("C")));
                    inSequence(sequence);
                    one(executor1).execute();
                    inSequence(sequence);
                    one(task2).createExecutor("ds-2");
                    will(returnValue(executor2));
                    inSequence(sequence);
                    one(executor2).createCleanupTask();
                    will(returnValue(new MockCleanupTask("D")));
                    inSequence(sequence);
                    one(executor2).execute();
                    inSequence(sequence);
                    will(throwException(new Throwable("error")));
                }
            });
        
        PostRegistrationMaintenanceTask maintenanceTask = new PostRegistrationMaintenanceTask();
        maintenanceTask.setUp("post-registration", properties);
        assertEquals(false, maintenanceTask.requiresDataStoreLock());
        maintenanceTask.execute();
        
        AssertionUtil.assertContains("ERROR OPERATION.PostRegistrationMaintenanceTask - "
                + "Post registration task '2' for data set ds-2 failed.",
                logRecorder.getLogContent());
        AssertionUtil.assertContains("ERROR OPERATION.PostRegistrationMaintenanceTask - "
                + "Because post registration task failed for data set ds-2 "
                + "post registration tasks are postponed for the following data sets: ds-3",
                logRecorder.getLogContent());
        assertEquals(0, criteriaMatcher.recordedObject().getLastSeenDataSetId());
        assertEquals("1", FileUtilities.loadExactToString(lastSeenDataSetFile).trim());
        assertEquals("[D]", cleanupInvocations.toString());
        assertEmptyCleanupTaskFolder();
        assertNoUexpectedInvocations();
        context.assertIsSatisfied();
    }
    
    private void assertNoUexpectedInvocations()
    {
        String logContent = logRecorder.getLogContent();
        int index = logContent.indexOf("unexpected invocation:");
        if (index >= 0)
        {
            fail(logContent.substring(index));
        }
    }
    
    private void assertEmptyCleanupTaskFolder()
    {
        assertEquals("[]", Arrays.asList(cleanupTasksFolder.list()).toString());
    }
    
    private Properties createDefaultProperties()
    {
        Properties properties = new Properties();
        properties.setProperty(CLEANUP_TASKS_FOLDER_PROPERTY, cleanupTasksFolder.toString());
        properties.setProperty(LAST_SEEN_DATA_SET_FILE_PROPERTY, lastSeenDataSetFile.toString());
        properties.setProperty(POST_REGISTRATION_TASKS_PROPERTY, "1, 2");
        properties.setProperty("1.class", MockPostRegistrationTask.class.getName());
        properties.setProperty("1." + TASK_NAME_PROPERTY, TASK_1_NAME);
        properties.setProperty("2.class", MockPostRegistrationTask.class.getName());
        properties.setProperty("2." + TASK_NAME_PROPERTY, TASK_2_NAME);
        return properties;
    }
}
