/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.utilities;

import static org.testng.AssertJUnit.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.utilities.IPathHandler;
import ch.systemsx.cisd.common.utilities.QueuingPathHandler;

/**
 * Test cases for the {@link QueuingPathHandler}.
 * 
 * @author Bernd Rinn
 */
public class QueuingPathHandlerTest
{

    final static long MILLIS_TO_WAIT_FOR_PROCESSING_TO_FINISH = 100;

    private static class RecordingIPathHandler implements IPathHandler
    {
        private final List<File> handled = new ArrayList<File>();

        private final int blockBeforeFile;

        private final long blockMillis;
        
        private boolean interrupted;

        RecordingIPathHandler(int blockBeforeFile, long blockMillis)
        {
            this.blockBeforeFile = blockBeforeFile;
            this.blockMillis = blockMillis;
        }

        RecordingIPathHandler()
        {
            this(0, 0L);
        }

        public void handle(File path)
        {
            if (handled.size() + 1 == blockBeforeFile)
            {
                try
                {
                    Thread.sleep(blockMillis);
                } catch (InterruptedException ex)
                {
                    interrupted = true;
                    return;
                }
            }
            handled.add(path);
        }

        List<File> getHandledFiles()
        {
            return handled;
        }
        
        boolean isInterrupted()
        {
            return interrupted;
        }

    }

    @Test
    public void testSingleProcessing() throws InterruptedException
    {
        final File testFile = new File("test_file_to_handle");
        final RecordingIPathHandler recorder = new RecordingIPathHandler();
        final IPathHandler qPathHandler = QueuingPathHandler.create(recorder, "test-thread");
        qPathHandler.handle(testFile);
        Thread.sleep(MILLIS_TO_WAIT_FOR_PROCESSING_TO_FINISH);
        assertEquals(Collections.singletonList(testFile), recorder.getHandledFiles());
    }

    @Test
    public void testMultipleProcessing() throws InterruptedException
    {
        final List<File> fileList = new ArrayList<File>(10);
        for (int i = 0; i < 10; ++i)
        {
            fileList.add(new File("File " + i));
        }
        final RecordingIPathHandler recorder = new RecordingIPathHandler();
        final IPathHandler qPathHandler = QueuingPathHandler.create(recorder, "test-thread");
        for (File f : fileList)
        {
            qPathHandler.handle(f);
        }
        Thread.sleep(MILLIS_TO_WAIT_FOR_PROCESSING_TO_FINISH);
        assertEquals(fileList, recorder.getHandledFiles());
    }

    @Test
    public void testTermination() throws InterruptedException
    {
        final List<File> processedFileList = new ArrayList<File>(4);
        final List<File> fileList = new ArrayList<File>(10);
        final int FILES_TO_PROCESS = 4;
        for (int i = 0; i < 10; ++i)
        {
            final File f = new File("File " + i); 
            if (i < FILES_TO_PROCESS)
            {
                processedFileList.add(f);
            }
            fileList.add(f);
        }
        final RecordingIPathHandler blocker =
                new RecordingIPathHandler(FILES_TO_PROCESS + 1, MILLIS_TO_WAIT_FOR_PROCESSING_TO_FINISH * 10L);
        final QueuingPathHandler qPathHandler = QueuingPathHandler.create(blocker, "test-thread");
        for (File f : fileList)
        {
            qPathHandler.handle(f);
        }
        Thread.sleep(MILLIS_TO_WAIT_FOR_PROCESSING_TO_FINISH);
        assertEquals(processedFileList, blocker.getHandledFiles());
        assertTrue(qPathHandler.terminate());
        Thread.sleep(MILLIS_TO_WAIT_FOR_PROCESSING_TO_FINISH);
        assertTrue(blocker.isInterrupted());
        assertEquals(processedFileList, blocker.getHandledFiles());
    }

}
