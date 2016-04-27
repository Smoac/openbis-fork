/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.collections;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class RecordBasedQueuePersisterTest extends AssertJUnit
{
    private static final File TMP = new File("targets/unit-test-wd");
    private static final File QUEUE_FILE = new File(TMP, "RecordBasedQueuePersisterTestQueue");
    
    private ArrayBlockingQueue<String> queue;
    private RecordBasedQueuePersister<String> persister;
    
    @BeforeTest
    public void setUp()
    {
        assertEquals("Couldn't delete " + TMP, true, FileUtilities.deleteRecursively(TMP));
        TMP.mkdirs();
        queue = new ArrayBlockingQueue<String>(10);
        persister = new RecordBasedQueuePersister<String>(queue, QUEUE_FILE);
    }
    
    @AfterTest
    public void tearDown()
    {
        persister.close();
    }

    @Test
    public void testPersist()
    {
        String element =
                "a string with more characters then "
                        + "RecordBasedQueuePersister.DEFAULT_INITIAL_RECORD_SIZE";
        assertEquals(true, element.length() > RecordBasedQueuePersister.DEFAULT_INITIAL_RECORD_SIZE);
        queue.add(element);

        persister.persist();

        persister.close();
        queue.clear();
        persister = new RecordBasedQueuePersister<String>(queue, QUEUE_FILE);

        assertEquals(1, queue.size());
        assertEquals(element, queue.peek());
    }
    
    @Test
    public void testAddToTail()
    {
        persister.addToTail("hello world");
    }
}
