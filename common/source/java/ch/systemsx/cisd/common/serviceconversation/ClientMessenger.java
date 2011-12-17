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

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.TimeoutExceptionUnchecked;

/**
 * A class that a client can use to receive messages from a service.
 * 
 * @author Bernd Rinn
 */
public class ClientMessenger implements IClientMessenger
{
    private final BlockingQueue<ServiceMessage> messageQueue =
            new LinkedBlockingQueue<ServiceMessage>();

    private final ISendingMessenger senderToService;

    private String serviceConversationId;

    private int timeoutMillis;

    private int messageIdxLastSeen = -1;

    private int outgoingMessageIdx;

    public ClientMessenger(ServiceConversationDTO serviceConversationDTO,
            ISendingMessenger senderToService)
    {
        assert senderToService != null;
        this.serviceConversationId = serviceConversationDTO.getServiceConversationId();
        assert serviceConversationId != null;
        this.timeoutMillis = serviceConversationDTO.getClientTimeoutInMillis();
        this.senderToService = senderToService;
    }

    ClientMessenger(ISendingMessenger senderToService)
    {
        this.senderToService = senderToService;
    }

    public ISendingMessenger getResponseMessenger()
    {
        return new ISendingMessenger()
            {
                public void send(ServiceMessage message)
                {
                    if (serviceConversationId != null
                            && serviceConversationId.equals(message.getConversationId()) == false)
                    {
                        throw new IllegalArgumentException(
                                "Attempt to put in a message for conversation "
                                        + message.getConversationId()
                                        + " into queue for conversation " + serviceConversationId);
                    }
                    if (message.getMessageIdx() <= messageIdxLastSeen)
                    {
                        return;
                    } else
                    {
                        messageIdxLastSeen = message.getMessageIdx();
                    }
                    if (message.hasPayload() == false)
                    {
                        messageQueue.clear();
                    }
                    messageQueue.add(message);
                }
            };
    }

    public void send(Serializable message)
    {
        senderToService.send(new ServiceMessage(serviceConversationId, nextOutgoingMessageIndex(),
                false, message));
    }

    private int nextOutgoingMessageIndex()
    {
        return outgoingMessageIdx++;
    }

    public <T extends Serializable> T receive(Class<T> messageClass)
    {
        try
        {
            return handleMessage(messageQueue.poll(timeoutMillis, TimeUnit.MILLISECONDS),
                    messageClass);
        } catch (InterruptedException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T handleMessage(ServiceMessage message, Class<T> messageClass)
    {
        if (message == null)
        {
            final TimeoutExceptionUnchecked exception =
                    new TimeoutExceptionUnchecked("Timeout while waiting on message from service.");
            final String exceptionDescription =
                    ServiceExecutionException.getDescriptionFromException(exception);
            senderToService.send(new ServiceMessage(serviceConversationId,
                    nextOutgoingMessageIndex(), true, exceptionDescription));
            throw exception;
        }
        if (message.isException())
        {
            throw new ServiceExecutionException(message.getConversationId(),
                    message.tryGetExceptionDescription());
        }
        final Object payload = message.getPayload();
        if (messageClass != null && messageClass.isAssignableFrom(payload.getClass()) == false)
        {
            throw new UnexpectedMessagePayloadException(payload.getClass(), messageClass);
        }
        return (T) payload;
    }

    public String getServiceConversationId()
    {
        return serviceConversationId;
    }

    void setServiceConversationDTO(ServiceConversationDTO serviceConversation)
    {
        this.serviceConversationId = serviceConversation.getServiceConversationId();
        this.timeoutMillis = serviceConversation.getClientTimeoutInMillis();
    }

}
