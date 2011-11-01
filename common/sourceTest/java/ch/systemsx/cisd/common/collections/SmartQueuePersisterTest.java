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
import java.io.RandomAccessFile;
import java.util.concurrent.ArrayBlockingQueue;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * @author Pawel Glyzewski
 */
public class SmartQueuePersisterTest extends AssertJUnit
{
    private static final File TMP = new File("targets/unit-test-wd");

    private static final File QUEUE_FILE = new File(TMP, "SmartQueuePersisterTestQueue");

    private static final File QUEUE_FILE_NEW = new File(TMP, "SmartQueuePersisterTestQueue.new");

    private ArrayBlockingQueue<String> queue;

    private SmartQueuePersister<String> persister;

    @BeforeMethod
    public void setUp()
    {
        assertEquals("Couldn't delete " + TMP, true, FileUtilities.deleteRecursively(TMP));
        TMP.mkdirs();
        queue = new ArrayBlockingQueue<String>(10);
        persister = new SmartQueuePersister<String>(queue, QUEUE_FILE);
    }

    @AfterMethod
    public void tearDown()
    {
        queue.clear();
        persister.persist();
        persister.close();
        QUEUE_FILE.delete();
        QUEUE_FILE_NEW.delete();
    }

    @Test
    public void testPersist()
    {
        String element =
                "a string with more characters than "
                        + "RecordBasedQueuePersister.DEFAULT_INITIAL_RECORD_SIZE";
        queue.add(element);

        persister.persist();

        persister.close();
        queue.clear();
        persister = new SmartQueuePersister<String>(queue, QUEUE_FILE);

        assertEquals(1, queue.size());
        assertEquals(element, queue.peek());
    }

    @Test
    public void testAddToTail()
    {
        persister.addToTail("hello world");
    }

    @Test
    public void testZeroRecordSize()
    {
        // First delete the queue file created by setUp so we can create one where the initial
        // record size is 0.
        assertEquals("Couldn't delete " + TMP, true, FileUtilities.deleteRecursively(TMP));
        TMP.mkdirs();
        queue = new ArrayBlockingQueue<String>(10);
        persister = new SmartQueuePersister<String>(queue, QUEUE_FILE, false);

        queue.add("");
        queue.add("foo");
        persister.persist();
        persister.close();
        queue.clear();
        persister = new SmartQueuePersister<String>(queue, QUEUE_FILE);

        assertEquals(2, queue.size());
        assertEquals("", queue.poll());
        assertEquals("foo", queue.poll());
    }

    @Test
    public void testQueueFileSizeLessThanHeaderSize() throws Exception
    {
        queue = new ArrayBlockingQueue<String>(10);

        // write a data shorter than header size
        RandomAccessFile tooShortRaf = new RandomAccessFile(QUEUE_FILE, "rw");
        tooShortRaf.writeInt(1);
        tooShortRaf.close();

        persister = new SmartQueuePersister<String>(queue, QUEUE_FILE);

        queue.add("test");
        persister.persist();
        persister.close();
        queue.clear();
        persister = new SmartQueuePersister<String>(queue, QUEUE_FILE);

        assertEquals(1, queue.size());
        assertEquals("test", queue.poll());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testMigrateRecordBasedToSmartQueue() throws Exception
    {
        assertEquals("Couldn't delete " + TMP, true, FileUtilities.deleteRecursively(TMP));
        TMP.mkdirs();
        queue = new ArrayBlockingQueue<String>(10);
        RecordBasedQueuePersister<String> recordBasedPersister =
                new RecordBasedQueuePersister<String>(queue, QUEUE_FILE, 0, false);

        queue.add("");
        queue.add("foobar");
        queue.add("test");
        queue.add("tralalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalala"
                + "lalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalala");
        recordBasedPersister.persist();
        recordBasedPersister.close();
        queue.clear();
        long fileSize = QUEUE_FILE.length();
        persister = new SmartQueuePersister<String>(queue, QUEUE_FILE);

        assertTrue(fileSize > 3 * QUEUE_FILE.length());
        assertEquals(4, queue.size());
        assertEquals("", queue.poll());
        assertEquals("foobar", queue.poll());
        assertEquals("test", queue.poll());
        assertEquals(
                "tralalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalala"
                        + "lalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalalala",
                queue.poll());
    }

    @Test
    public void testAddAndRemove() throws Exception
    {
        queue = new ArrayBlockingQueue<String>(10);
        persister = new SmartQueuePersister<String>(queue, QUEUE_FILE);
        queue.add("one");
        queue.add("two");
        queue.add("three");
        persister.persist();
        persister.removeFromHead(queue.remove());
        queue.add("four");
        persister.addToTail("four");
        persister.removeFromHead(queue.remove());
        persister.close();
        queue.clear();
        persister = new SmartQueuePersister<String>(queue, QUEUE_FILE);
        assertEquals(2, queue.size());
    }
}
