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

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.base.namedthread.NamingThreadPoolExecutor;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.concurrent.ITerminableFuture;
import ch.systemsx.cisd.common.concurrent.TerminableCallable.ICallable;
import ch.systemsx.cisd.common.concurrent.TerminableCallable.IStoppableExecutor;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A collection of service conversations.
 * 
 * @author Bernd Rinn
 */
public class ServiceConversationServer
{
    private final static int NUMBER_OF_CORE_THREADS = 10;

    private final static int SHUTDOWN_TIMEOUT_MILLIS = 10000;

    final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ServiceConversationServer.class);

    private final int messageReceivingTimeoutMillis;

    private final ExecutorService executor = new NamingThreadPoolExecutor("Service Conversations")
            .corePoolSize(NUMBER_OF_CORE_THREADS).daemonize();

    private final Map<String, IServiceFactory> serviceFactoryMap =
            new ConcurrentHashMap<String, IServiceFactory>();

    private final Map<String, IServiceMessageTransport> responseMessageMap =
            new ConcurrentHashMap<String, IServiceMessageTransport>();

    private final Map<String, ServiceConversationRecord> conversations =
            new ConcurrentHashMap<String, ServiceConversationRecord>();

    private final Random rng = new Random();

    private final IServiceMessageTransport incomingTransport = new IServiceMessageTransport()
        {
            public void send(ServiceMessage message)
            {
                final String conversationId = message.getConversationId();
                final ServiceConversationRecord record = conversations.get(conversationId);
                if (record == null)
                {
                    operationLog.error(String.format(
                            "Message for unknown service conversation '%s'", conversationId));
                    return;
                }
                if (message.hasPayload())
                {
                    record.getMessenger().sendToService(message);
                } else
                {
                    if (message.isException())
                    {
                        operationLog.error(String.format(
                                "[id: %s] Client execution exception.\n%s", conversationId,
                                message.tryGetExceptionDescription()));
                    } else
                    {
                        operationLog.error(String.format(
                                "[id: %s] Client requests termination of service conversation.",
                                conversationId));
                    }
                    record.getMessenger().markAsInterrupted();
                    record.getController().cancel(true);
                }
            }
        };

    public ServiceConversationServer(int messageReceivingTimeoutMillis)
    {
        this.messageReceivingTimeoutMillis = messageReceivingTimeoutMillis;
    }

    //
    // Initial setup
    //

    /**
     * Adds a new service type to this conversation object.
     */
    public void addServiceType(IServiceFactory factory)
    {
        final String id = factory.getServiceTypeId();
        if (serviceFactoryMap.containsKey(id))
        {
            throw new IllegalArgumentException("Service type '" + id + "' is already registered.");
        }
        serviceFactoryMap.put(id, factory);
    }

    //
    // Client setup
    //

    /**
     * Adds the client transport (to be called when client connects).
     */
    public void addClientResponseTransport(String clientId,
            IServiceMessageTransport responseTransport)
    {
        responseMessageMap.put(clientId, responseTransport);
    }

    /**
     * Removes the client transport (to be called when client disconnects).
     * 
     * @return <code>true</code> if the client transport was removed.
     */
    public boolean removeClientResponseTransport(String clientId)
    {
        return responseMessageMap.remove(clientId) != null;
    }

    /**
     * Returns the transport for incoming messages from clients.
     */
    public IServiceMessageTransport getIncomingMessageTransport()
    {
        return incomingTransport;
    }

    /**
     * Starts a service conversation of type <var>typeId</var>.
     * 
     * @param typeId The service type of the conversation.
     * @param clientId The id of the client, used to find a suitable transport to communicate back
     *            the messages from the service to the client.
     * @return The information about the service conversation started.
     */
    public ServiceConversationDTO startConversation(final String typeId, final String clientId)
    {
        final IServiceFactory serviceFactory = serviceFactoryMap.get(typeId);
        if (serviceFactory == null)
        {
            throw new UnknownServiceTypeException(typeId);
        }
        final IServiceMessageTransport responseMessenger = responseMessageMap.get(clientId);
        if (responseMessenger == null)
        {
            throw new UnknownClientException(clientId);
        }
        final IService serviceInstance = serviceFactory.create();
        final String serviceConversationId =
                Long.toString(System.currentTimeMillis()) + "-" + rng.nextInt(Integer.MAX_VALUE);
        final BidirectionalServiceMessenger messenger =
                new BidirectionalServiceMessenger(serviceConversationId,
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
                                if (ex instanceof InterruptedExceptionUnchecked == false)
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
                                        operationLog.error(
                                                String.format(
                                                        "[id: %s] Cannot send message about exception to client.",
                                                        serviceConversationId), ex2);
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

}
