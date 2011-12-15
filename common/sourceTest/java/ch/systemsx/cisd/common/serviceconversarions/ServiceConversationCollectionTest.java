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

package ch.systemsx.cisd.common.serviceconversarions;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.serviceconversation.ClientMessenger;
import ch.systemsx.cisd.common.serviceconversation.IClientMessenger;
import ch.systemsx.cisd.common.serviceconversation.ISendingMessenger;
import ch.systemsx.cisd.common.serviceconversation.IService;
import ch.systemsx.cisd.common.serviceconversation.IServiceFactory;
import ch.systemsx.cisd.common.serviceconversation.IServiceMessenger;
import ch.systemsx.cisd.common.serviceconversation.ServiceConversationCollection;
import ch.systemsx.cisd.common.serviceconversation.ServiceExecutionException;
import ch.systemsx.cisd.common.serviceconversation.ServiceMessage;
import ch.systemsx.cisd.common.serviceconversation.UnknownServiceTypeException;

/**
 * Test cases for the {@Link ServiceConversationCollection} class.
 * 
 * @author Bernd Rinn
 */
public class ServiceConversationCollectionTest
{
    private static class SingleEchoService implements IService
    {
        public void run(IServiceMessenger messenger)
        {
            messenger.send(messenger.receive(String.class));
        }
    }

    @Test
    public void testSingleEchoServiceHappyCase() throws Exception
    {
        final ServiceConversationCollection conversations = new ServiceConversationCollection(100);
        conversations.addServiceType("singleEcho", new IServiceFactory()
            {
                public IService create()
                {
                    return new SingleEchoService();
                }
            });
        final IClientMessenger messenger = conversations.startConversation("singleEcho");
        messenger.send("Hallo Echo");
        assertEquals("Hallo Echo", messenger.receive(String.class));
    }

    private static class EchoService implements IService
    {
        public void run(IServiceMessenger messenger)
        {
            try
            {
                while (true)
                {
                    System.err.println(Thread.currentThread().getName());
                    messenger.send(messenger.receive(String.class));
                }
            } catch (RuntimeException ex)
            {
                // Show exception
                ex.printStackTrace();
                // This doesn't matter: the exception goes into the void.
                throw ex;
            }
        }
    }

    @Test
    public void testMultipleEchoServiceTerminateHappyCase() throws Exception
    {
        final ServiceConversationCollection conversations = new ServiceConversationCollection(100);
        conversations.addServiceType("echo", new IServiceFactory()
            {
                public IService create()
                {
                    return new EchoService();
                }
            });
        final BlockingQueue<ServiceMessage> messageQueue =
                new LinkedBlockingQueue<ServiceMessage>();
        final String id = conversations.startConversation("echo", new ISendingMessenger()
            {
                public void send(ServiceMessage message)
                {
                    messageQueue.add(message);
                }
            });
        assertTrue(conversations.hasConversation(id));
        int messageIdx = 0;

        conversations.send(new ServiceMessage(id, 0, "One"));
        ServiceMessage m = messageQueue.take();
        assertEquals(id, m.getConversationId());
        assertEquals(messageIdx++, m.getMessageIdx());
        assertEquals("One", m.getPayload());

        conversations.send(new ServiceMessage(id, 1, "Two"));
        // Try to resend and check that the second one is swallowed.
        conversations.send(new ServiceMessage(id, 1, "Two"));
        m = messageQueue.take();
        assertEquals(id, m.getConversationId());
        assertEquals(messageIdx++, m.getMessageIdx());
        assertEquals("Two", m.getPayload());

        conversations.send(new ServiceMessage(id, 2, "Three"));
        m = messageQueue.take();
        assertEquals(id, m.getConversationId());
        assertEquals(messageIdx++, m.getMessageIdx());
        assertEquals("Three", m.getPayload());

        conversations.send(ServiceMessage.terminate(id));
        for (int i = 0; i < 100 && conversations.hasConversation(id); ++i)
        {
            ConcurrencyUtilities.sleep(10L);
        }
        assertFalse(conversations.hasConversation(id));
    }

    @Test
    public void testEchoServiceTimeout() throws Exception
    {
        final ServiceConversationCollection conversations = new ServiceConversationCollection(100);
        conversations.addServiceType("echo", new IServiceFactory()
            {
                public IService create()
                {
                    return new EchoService();
                }
            });
        final BlockingQueue<ServiceMessage> messageQueue =
                new LinkedBlockingQueue<ServiceMessage>();
        final String id = conversations.startConversation("echo", new ISendingMessenger()
            {
                public void send(ServiceMessage message)
                {
                    messageQueue.add(message);
                }
            });
        assertTrue(conversations.hasConversation(id));
        int messageIdx = 0;
        conversations.send(new ServiceMessage(id, 0, "One"));
        ServiceMessage m = messageQueue.take();
        assertEquals(id, m.getConversationId());
        assertEquals(messageIdx++, m.getMessageIdx());
        assertEquals("One", m.getPayload());

        // Wait for timeout to happen.
        for (int i = 0; i < 100 && conversations.hasConversation(id); ++i)
        {
            ConcurrencyUtilities.sleep(10L);
        }
        assertFalse(conversations.hasConversation(id));
        m = messageQueue.take();
        assertTrue(m.isException());
        assertTrue(m.tryGetExceptionDescription().startsWith(
                "ch.systemsx.cisd.base.exceptions.TimeoutExceptionUnchecked:"));
    }

    @Test(expectedExceptions = UnknownServiceTypeException.class)
    public void testNonExistentService() throws Exception
    {
        final ServiceConversationCollection conversations = new ServiceConversationCollection(100);
        conversations.startConversation("echo", new ISendingMessenger()
            {
                public void send(ServiceMessage message)
                {
                }
            });
    }

    private static class ExceptionThrowingService implements IService
    {
        public void run(IServiceMessenger messenger)
        {
            throw new RuntimeException("Don't like you!");
        }
    }

    @Test
    public void testServiceThrowsException() throws Exception
    {
        final ServiceConversationCollection conversations = new ServiceConversationCollection(100);
        conversations.addServiceType("throwException", new IServiceFactory()
            {
                public IService create()
                {
                    return new ExceptionThrowingService();
                }
            });
        final ClientMessenger messenger = conversations.startConversation("throwException");
        try
        {
            messenger.receive(Object.class);
            fail();
        } catch (ServiceExecutionException ex)
        {
            assertEquals(messenger.getServiceConversationId(),
                    ex.getServiceConversationId());
            assertTrue(ex.getDescription().contains("RuntimeException"));
            assertTrue(ex.getDescription().contains("Don't like you!"));
        }
    }
}
