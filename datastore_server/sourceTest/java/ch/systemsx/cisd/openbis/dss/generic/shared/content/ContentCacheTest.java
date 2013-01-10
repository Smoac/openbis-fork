/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.content;

import java.io.File;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.IFileOperations;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ContentCacheTest extends AssertJUnit
{
    private static final String SESSION_TOKEN = "session";
    
    private Mockery context;

    private IFileOperations fileOperations;
    
    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        fileOperations = context.mock(IFileOperations.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testDataSetLocking()
    {
        ContentCache cache = createCache(false);
        
        cache.lockDataSet(SESSION_TOKEN, "DS-1");
        
        assertEquals(true, cache.isDataSetLocked(SESSION_TOKEN, "DS-1"));
        assertEquals(false, cache.isDataSetLocked(SESSION_TOKEN, "DS-2"));
        
        cache.lockDataSet(SESSION_TOKEN, "DS-1");
        
        assertEquals(true, cache.isDataSetLocked(SESSION_TOKEN, "DS-1"));
        
        cache.unlockDataSet(SESSION_TOKEN, "DS-1");
        
        assertEquals(true, cache.isDataSetLocked(SESSION_TOKEN, "DS-1"));
        
        cache.unlockDataSet(SESSION_TOKEN, "DS-1");
        
        assertEquals(false, cache.isDataSetLocked(SESSION_TOKEN, "DS-1"));
        
        context.assertIsSatisfied();
    }

    private ContentCache createCache(boolean sessionCache)
    {
        final File workSpace = new File(".");
        if (sessionCache == false)
        {
            context.checking(new Expectations()
                {
                    {
                        one(fileOperations).removeRecursivelyQueueing(
                                new File(workSpace, ContentCache.DOWNLOADING_FOLDER));
                    }
                });
        }
        return new ContentCache(null, workSpace, sessionCache, fileOperations);
    }
}
