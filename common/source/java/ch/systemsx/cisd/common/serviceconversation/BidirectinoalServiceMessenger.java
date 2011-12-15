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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.TimeoutExceptionUnchecked;

/**
 * A bidirectional messenger on the server side.
 * 
 * @author Bernd Rinn
 */
class BidirectinoalServiceMessenger
{
    private final BlockingQueue<ServiceMessage> incoming =
            new LinkedBlockingQueue<ServiceMessage>();

    private final String conversationId;

    private final ISendingMessenger responseMessenger;

    private final int messageReceivingTimeoutMillis;

    private int outgoingMessageIdx;

    private int messageIdxLastSeen = -1;

    BidirectinoalServiceMessenger(String conversationId, int messageReceivingTimeoutMillis,
            ISendingMessenger responseMessenger)
    {
        this.conversationId = conversationId;
        this.messageReceivingTimeoutMillis = messageReceivingTimeoutMillis;
        this.responseMessenger = responseMessenger;
    }

    IServiceMessenger getServiceMessenger()
    {
        return new IServiceMessenger()
            {
                @SuppressWarnings("unchecked")
                public <T> T receive(Class<T> messageClass)
                {
                    final Object payload;
                    try
                    {
                        final ServiceMessage message =
                                incoming.poll(messageReceivingTimeoutMillis, TimeUnit.MILLISECONDS);
                        if (message == null)
                        {
                            throw new TimeoutExceptionUnchecked(
                                    "Timeout while waiting for message to return.");
                        }
                        payload = message.getPayload();
                    } catch (InterruptedException ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }
                    if (messageClass != null
                            && messageClass.isAssignableFrom(payload.getClass()) == false)
                    {
                        throw new UnexpectedMessagePayloadException(payload.getClass(),
                                messageClass);
                    }
                    return (T) payload;
                }

                public void send(Object message)
                {
                    responseMessenger.send(new ServiceMessage(conversationId,
                            nextOutgoingMessageIndex(), message));
                }
            };
    }

    int nextOutgoingMessageIndex()
    {
        return outgoingMessageIdx++;
    }

    public void sendToService(ServiceMessage message)
    {
        if (message.getMessageIdx() <= messageIdxLastSeen)
        {
            // Drop duplicate message.
            return;
        } else
        {
            messageIdxLastSeen = message.getMessageIdx();
        }
        incoming.add(message);
    }
}