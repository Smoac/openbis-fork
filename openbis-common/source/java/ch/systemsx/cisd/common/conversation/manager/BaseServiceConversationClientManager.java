/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.conversation.manager;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.common.conversation.client.ServiceConversationClientWithConversationTracking;
import ch.systemsx.cisd.common.serviceconversation.ServiceMessage;
import ch.systemsx.cisd.common.serviceconversation.UnknownServiceConversationException;
import ch.systemsx.cisd.common.serviceconversation.client.IServiceConversation;

/**
 * @author pkupczyk
 */
public class BaseServiceConversationClientManager implements
        IServiceConversationClientManagerRemote
{

    private Map<String, ServiceConversationClientWithConversationTracking> serverUrlToClientMap =
            new HashMap<String, ServiceConversationClientWithConversationTracking>();

    private Map<String, ServiceConversationClientWithConversationTracking> conversationIdToClientMap =
            new HashMap<String, ServiceConversationClientWithConversationTracking>();

    @Override
    public void send(ServiceMessage message)
    {
        ServiceConversationClientWithConversationTracking client =
                getClientForConversationId(message.getConversationId());

        if (client == null)
        {
            throw new UnknownServiceConversationException(String.format(
                    "Message for unknown service conversation '%s'", message.getConversationId()));
        } else
        {
            client.receiveMessage(message);
        }
    }

    public <T> T getService(String serverUrl, Class<T> serviceInterface, String sessionToken,
            Object clientId, int clientTimeout)
    {
        return ServiceConversationServiceProxy.newInstance(
                getClientForServerUrl(serverUrl, clientId, clientTimeout), serviceInterface,
                sessionToken);
    }

    private synchronized ServiceConversationClientWithConversationTracking getClientForServerUrl(
            String serverUrl, Object clientId, int clientTimeout)
    {
        ServiceConversationClientWithConversationTracking client =
                serverUrlToClientMap.get(serverUrl);

        if (client == null)
        {
            client =
                    new ServiceConversationClientWithConversationTracking(serverUrl, clientId,
                            clientTimeout)
                        {
                            @Override
                            public void onConversationStart(IServiceConversation conversation)
                            {
                                conversationIdToClientMap.put(conversation.getId(), this);
                            }

                            @Override
                            public void onConversationClose(IServiceConversation conversation)
                            {
                                conversationIdToClientMap.remove(conversation.getId());
                            }
                        };
            serverUrlToClientMap.put(serverUrl, client);
        }

        return client;
    }

    private ServiceConversationClientWithConversationTracking getClientForConversationId(
            String conversationId)
    {
        return conversationIdToClientMap.get(conversationId);
    }

}
