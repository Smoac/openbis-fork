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

package ch.systemsx.cisd.common.serviceconversation;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.base.exceptions.TimeoutExceptionUnchecked;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.logging.LogInitializer;

/**
 * Test cases for the {@Link ServiceConversationCollection} class.
 * 
 * @author Bernd Rinn
 */
public class ServiceConversationCollectionTest
{
    @BeforeTest
    public void init()
    {
        LogInitializer.init();
    }
    
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
        conversations.addServiceType(new IServiceFactory()
            {
                public IService create()
                {
                    return new SingleEchoService();
                }

                public int getClientTimeoutMillis()
                {
                    return 100;
                }

                public String getServiceTypeId()
                {
                    return "singleEcho";
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
        conversations.addServiceType(new IServiceFactory()
            {
                public IService create()
                {
                    return new EchoService();
                }

                public int getClientTimeoutMillis()
                {
                    return 100;
                }

                public String getServiceTypeId()
                {
                    return "echo";
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
            }).getServiceConversationId();
        assertTrue(conversations.hasConversation(id));
        int messageIdx = 0;

        conversations.send(new ServiceMessage(id, 0, false, "One"));
        ServiceMessage m = messageQueue.take();
        assertEquals(id, m.getConversationId());
        assertEquals(messageIdx++, m.getMessageIdx());
        assertEquals("One", m.getPayload());

        conversations.send(new ServiceMessage(id, 1, false, "Two"));
        // Try to resend and check that the second one is swallowed.
        conversations.send(new ServiceMessage(id, 1, false, "Two"));
        m = messageQueue.take();
        assertEquals(id, m.getConversationId());
        assertEquals(messageIdx++, m.getMessageIdx());
        assertEquals("Two", m.getPayload());

        conversations.send(new ServiceMessage(id, 2, false, "Three"));
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
        conversations.addServiceType(new IServiceFactory()
            {
                public IService create()
                {
                    return new EchoService();
                }

                public int getClientTimeoutMillis()
                {
                    return 100;
                }

                public String getServiceTypeId()
                {
                    return "echo";
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
            }).getServiceConversationId();
        assertTrue(conversations.hasConversation(id));
        int messageIdx = 0;
        conversations.send(new ServiceMessage(id, 0, false, "One"));
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
        conversations.addServiceType(new IServiceFactory()
            {
                public IService create()
                {
                    return new ExceptionThrowingService();
                }

                public int getClientTimeoutMillis()
                {
                    return 100;
                }

                public String getServiceTypeId()
                {
                    return "throwException";
                }
            });
        final ClientMessenger messenger = conversations.startConversation("throwException");
        try
        {
            messenger.receive(Serializable.class);
            fail();
        } catch (ServiceExecutionException ex)
        {
            assertEquals(messenger.getServiceConversationId(), ex.getServiceConversationId());
            assertTrue(ex.getDescription().contains("RuntimeException"));
            assertTrue(ex.getDescription().contains("Don't like you!"));
        }
    }

    private static class EventuallyExceptionThrowingService implements IService
    {
        public void run(IServiceMessenger messenger)
        {
            messenger.send("OK1");
            messenger.send("OK2");
            messenger.send("OK3");
            throw new RuntimeException("Don't like you!");
        }
    }

    @Test
    public void testServiceEventuallyThrowsException() throws Exception
    {
        final ServiceConversationCollection conversations = new ServiceConversationCollection(100);
        conversations.addServiceType(new IServiceFactory()
            {
                public IService create()
                {
                    return new EventuallyExceptionThrowingService();
                }

                public int getClientTimeoutMillis()
                {
                    return 100;
                }

                public String getServiceTypeId()
                {
                    return "throwException";
                }
            });
        final ClientMessenger messenger = conversations.startConversation("throwException");
        ConcurrencyUtilities.sleep(100L);
        try
        {
            // The regular messages should have been cleared by now so that we get to see the
            // exception immediately.
            messenger.receive(Serializable.class);
            fail();
        } catch (ServiceExecutionException ex)
        {
            assertEquals(messenger.getServiceConversationId(), ex.getServiceConversationId());
            assertTrue(ex.getDescription().contains("RuntimeException"));
            assertTrue(ex.getDescription().contains("Don't like you!"));
        }
    }

    private static class DelayedService implements IService
    {
        public void run(IServiceMessenger messenger)
        {
            try
            {
                ConcurrencyUtilities.sleep(100L);
            } catch (InterruptedExceptionUnchecked ex)
            {
                System.err.println("DelayedService got interrupted.");
            }
        }
    }

    @Test
    public void testClientTimesout() throws Exception
    {
        final ServiceConversationCollection conversations = new ServiceConversationCollection(100);
        conversations.addServiceType(new IServiceFactory()
            {
                public IService create()
                {
                    return new DelayedService();
                }

                public int getClientTimeoutMillis()
                {
                    return 10;
                }

                public String getServiceTypeId()
                {
                    return "delayed";
                }
            });
        final ClientMessenger messenger = conversations.startConversation("delayed");
        try
        {
            messenger.receive(Serializable.class);
            fail();
        } catch (TimeoutExceptionUnchecked ex)
        {
        }
        // Wait for service to find out that the client timed out.
        ConcurrencyUtilities.sleep(100L);
    }

}
