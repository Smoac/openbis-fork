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

package ch.systemsx.cisd.cifex.server.util;

import org.testng.annotations.Test;

import ch.systemsx.cisd.cifex.shared.basic.dto.FileUploadFeedback;
import ch.systemsx.cisd.cifex.shared.basic.dto.Message;

import static org.testng.AssertJUnit.*;

/**
 * Test cases for the {@link FileUploadFeedbackProvider}.
 * 
 * @author Bernd Rinn
 */
public class FileUploadFeedbackProviderTest
{

    @Test
    public void testInitialState() throws InterruptedException
    {
        final FileUploadFeedbackProvider provider = new FileUploadFeedbackProvider();
        assertTrue(provider.take().isFinished());
    }

    @Test
    public void testProvideFeedback() throws InterruptedException
    {
        final FileUploadFeedbackProvider provider = new FileUploadFeedbackProvider();
        final FileUploadFeedback feedback = new FileUploadFeedback();
        provider.set(feedback);
        assertSame(feedback, provider.take());
    }

    @Test
    public void testProvideFeedbackMultipleSetsOneGet() throws InterruptedException
    {
        final FileUploadFeedbackProvider provider = new FileUploadFeedbackProvider();
        final FileUploadFeedback feedback1 = new FileUploadFeedback();
        final FileUploadFeedback feedback2 = new FileUploadFeedback();
        final FileUploadFeedback feedback3 = new FileUploadFeedback();
        provider.set(feedback1);
        provider.set(feedback2);
        provider.set(feedback3);
        assertSame(feedback3, provider.take());
    }

    @Test
    public void testProvideFeedbackFinishUp() throws InterruptedException
    {
        final FileUploadFeedbackProvider provider = new FileUploadFeedbackProvider();
        final FileUploadFeedback feedback1 = new FileUploadFeedback();
        final FileUploadFeedback feedback2 = new FileUploadFeedback();
        final FileUploadFeedback feedback3 = new FileUploadFeedback();
        provider.set(feedback1);
        provider.set(feedback2);
        provider.set(feedback3);
        provider.setFileUploadFinished();
        final FileUploadFeedback fakeFeedback = provider.take();
        assertNotSame(feedback3, fakeFeedback);
        assertTrue(fakeFeedback.isFinished());
    }

    @Test
    public void testProvideFeedbackProvideMessage() throws InterruptedException
    {
        final FileUploadFeedbackProvider provider = new FileUploadFeedbackProvider();
        final FileUploadFeedback feedback1 = new FileUploadFeedback();
        final FileUploadFeedback feedback2 = new FileUploadFeedback();
        final FileUploadFeedback feedback3 = new FileUploadFeedback();
        provider.set(feedback1);
        provider.set(feedback2);
        provider.set(feedback3);
        final Message message = new Message(Message.WARNING, "Some Message");
        provider.setMessage(message);
        final FileUploadFeedback fakeFeedback = provider.take();
        assertNotSame(feedback3, fakeFeedback);
        assertSame(message, fakeFeedback.getMessage());
        assertTrue(fakeFeedback.isFinished());
        final FileUploadFeedback fakeFeedback2 = provider.take();
        assertSame(message, fakeFeedback.getMessage());
        assertTrue(fakeFeedback2.isFinished());
    }

    @Test
    public void testWaitForFeedback() throws InterruptedException
    {
        final FileUploadFeedbackProvider provider = new FileUploadFeedbackProvider();
        final FileUploadFeedback initialFeedback = new FileUploadFeedback();
        provider.set(initialFeedback);
        provider.take(); // empty the queue
        final FileUploadFeedback feedbackSet = new FileUploadFeedback();
        new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        Thread.sleep(100);
                    } catch (InterruptedException ex)
                    {
                        // ignore
                    }
                    provider.set(feedbackSet);
                }
            }).start();
        final FileUploadFeedback feedbackObtained = provider.take();
        assertSame(feedbackSet, feedbackObtained);
    }

    @Test
    public void testWaitForFeedbackSendMessage() throws InterruptedException
    {
        final FileUploadFeedbackProvider provider = new FileUploadFeedbackProvider();
        final FileUploadFeedback initialFeedback = new FileUploadFeedback();
        provider.set(initialFeedback);
        provider.take(); // empty the queue
        final Message message = new Message(Message.INFO, "Message from other thread");
        new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        Thread.sleep(100);
                    } catch (InterruptedException ex)
                    {
                        // ignore
                    }
                    provider.setMessage(message);
                }
            }).start();
        final FileUploadFeedback feedbackObtained = provider.take();
        assertSame(message, feedbackObtained.getMessage());
    }

    @Test
    public void testWaitForFeedbackFinishUp() throws InterruptedException
    {
        final FileUploadFeedbackProvider provider = new FileUploadFeedbackProvider();
        final FileUploadFeedback initialFeedback = new FileUploadFeedback();
        provider.set(initialFeedback);
        provider.take(); // empty the queue
        new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        Thread.sleep(100);
                    } catch (InterruptedException ex)
                    {
                        // ignore
                    }
                    provider.setFileUploadFinished();
                }
            }).start();
        final FileUploadFeedback feedbackObtained = provider.take();
        assertTrue(feedbackObtained.isFinished());
    }
}
