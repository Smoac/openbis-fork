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

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.base.namedthread.NamingThreadPoolExecutor;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.concurrent.ITerminableFuture;
import ch.systemsx.cisd.common.concurrent.TerminableCallable.ICallable;
import ch.systemsx.cisd.common.concurrent.TerminableCallable.IStoppableExecutor;

/**
 * The service conversation collection.
 * 
 * @author Bernd Rinn
 */
public class ServiceConversationCollection implements ISendingMessenger
{
    private final static int NUMBER_OF_CORE_THREADS = 10;

    private final static int SHUTDOWN_TIMEOUT_MILLIS = 10000;

    private final int messageReceivingTimeoutMillis;

    private final ExecutorService executor = new NamingThreadPoolExecutor("Service Conversations")
            .corePoolSize(NUMBER_OF_CORE_THREADS).daemonize();

    private Random rng = new Random();

    private final Map<String, IServiceFactory> serviceFactoryMap =
            new ConcurrentHashMap<String, IServiceFactory>();

    private Map<String, ServiceConversationRecord> conversations =
            new ConcurrentHashMap<String, ServiceConversationRecord>();

    public ServiceConversationCollection(int messageReceivingTimeoutMillis)
    {
        this.messageReceivingTimeoutMillis = messageReceivingTimeoutMillis;
    }

    /**
     * Adds a new service type to this conversation object.
     */
    public void addServiceType(String id, IServiceFactory factory)
    {
        serviceFactoryMap.put(id, factory);
    }

    /**
     * Starts a service conversation of type <var>typeId</var>.
     * 
     * @param typeId The service type of the conversation.
     * @return a {@link ClientMessenger} to communicate with the service.
     */
    public ClientMessenger startConversation(final String typeId)
    {
        final ClientMessenger clientMessenger = new ClientMessenger(this);
        final ServiceConversationDTO serviceConversationRecord =
                startConversation(typeId, clientMessenger.getResponseMessenger());
        clientMessenger.setServiceConversationDTO(serviceConversationRecord);
        return clientMessenger;
    }

    /**
     * Starts a service conversation of type <var>typeId</var>.
     * 
     * @param typeId The service type of the conversation.
     * @param responseMessenger The messenger to communicate back the messages from the service to
     *            the client.
     * @return The information about the service conversation started.
     */
    public ServiceConversationDTO startConversation(final String typeId,
            final ISendingMessenger responseMessenger)
    {
        final IServiceFactory serviceFactory = serviceFactoryMap.get(typeId);
        if (serviceFactory == null)
        {
            throw new UnknownServiceTypeException(typeId);
        }
        final IService serviceInstance = serviceFactory.create();
        final String serviceConversationId =
                Long.toString(System.currentTimeMillis()) + "-" + rng.nextInt(Integer.MAX_VALUE);
        final BidirectinoalServiceMessenger messenger =
                new BidirectinoalServiceMessenger(serviceConversationId,
                        messageReceivingTimeoutMillis, responseMessenger);
        final ServiceConversationRecord record = new ServiceConversationRecord(messenger);
        conversations.put(serviceConversationId, record);
        final ITerminableFuture<Void> controller =
                ConcurrencyUtilities.submit(executor, new ICallable<Void>()
                    {
                        public Void call(IStoppableExecutor<Void> stoppableExecutor)
                                throws Exception
                        {
                            try
                            {
                                serviceInstance.run(messenger.getServiceMessenger());
                            } catch (Exception ex)
                            {
                                if (ex instanceof InterruptedExceptionUnchecked == false
                                        && ex instanceof ClientExecutionException == false)
                                {
                                    final String errorMessage =
                                            ServiceExecutionException
                                                    .getDescriptionFromException(ex);
                                    try
                                    {
                                        responseMessenger.send(new ServiceMessage(
                                                serviceConversationId, messenger
                                                        .nextOutgoingMessageIndex(), true,
                                                errorMessage));
                                    } catch (Exception ex2)
                                    {
                                        // TODO: improve logging
                                        ex2.printStackTrace();
                                    }
                                }
                            } finally
                            {
                                conversations.remove(serviceConversationId);
                            }
                            return null;
                        }

                        // TODO: uncomment once we can name an ICallable.
                        // public String getCallableName()
                        // {
                        // return conversationId + " (" + typeId + ")";
                        // }

                    });
        record.setController(controller);
        return new ServiceConversationDTO(serviceConversationId,
                serviceFactory.getClientTimeoutMillis());
    }

    public void shutdown()
    {
        try
        {
            for (ServiceConversationRecord record : conversations.values())
            {
                record.getController().cancel(true);
            }
            executor.awaitTermination(SHUTDOWN_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (Exception ex)
        {
            throw new CheckedExceptionTunnel(ex);
        }
    }

    public void shutdownNow()
    {
        try
        {
            for (ServiceConversationRecord record : conversations.values())
            {
                record.getController().cancel(true);
            }
            executor.awaitTermination(0, TimeUnit.MILLISECONDS);
        } catch (Exception ex)
        {
            throw new CheckedExceptionTunnel(ex);
        }
    }

    public boolean hasConversation(String conversationId)
    {
        return conversations.containsKey(conversationId);
    }

    //
    // IIncomingMessenger
    //

    public void send(ServiceMessage message)
    {
        final String conversationId = message.getConversationId();
        final ServiceConversationRecord record = conversations.get(conversationId);
        if (record == null)
        {
            if (message.isException() == false)
            {
                throw new UnknownServiceConversationException(conversationId);
            } else
            {
                // If it was an exception on the client side, be silent.
                return;
            }
        }
        if (message.isTerminate())
        {
            record.getController().cancel(true);
        } else
        {
            record.getMessenger().sendToService(message);
        }
    }

}
