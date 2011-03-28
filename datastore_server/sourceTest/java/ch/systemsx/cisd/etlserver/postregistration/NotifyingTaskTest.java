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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class NotifyingTaskTest extends AbstractFileSystemTestCase
{
    private static final String DATA_SET_CODE = "ds-1";

    private Mockery context;

    private IEncapsulatedOpenBISService service;
    
    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
    }
    
    @AfterMethod(alwaysRun = true)
    public void afterMethod()
    {
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void test() throws IOException
    {
        Properties properties = new Properties();
        FileInputStream inStream =
                new FileInputStream(new File("sourceTest/java/" + getClass().getName().replace('.', '/')
                        + "-Example.properties"));
        try
        {
            properties.load(inStream);
        } finally
        {
            IOUtils.closeQuietly(inStream);
        }
        final DataSetBuilder dataSet = new DataSetBuilder().code(DATA_SET_CODE);
        dataSet.property("MY-ID", "hello");
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet(DATA_SET_CODE);
                    will(returnValue(dataSet.getDataSet()));
                }
            });
        NotifyingTask notifyingTask = new NotifyingTask(properties, service);
        IPostRegistrationTaskExecutor executor = notifyingTask.createExecutor(DATA_SET_CODE);
        ICleanupTask cleanupTask = executor.createCleanupTask();
        executor.execute();
        
        assertEquals(NoCleanupTask.class, cleanupTask.getClass());
        assertEquals("data-set = ds-1\n" + "identifier = hello",
                FileUtilities.loadExactToString(new File("targets/notifyingTask.txt")));
        context.assertIsSatisfied();
    }
}
