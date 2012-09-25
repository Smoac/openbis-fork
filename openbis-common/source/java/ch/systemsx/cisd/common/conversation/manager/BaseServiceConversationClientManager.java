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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import ch.systemsx.cisd.common.conversation.client.ServiceConversationClientWithConversationTracking;
import ch.systemsx.cisd.common.serviceconversation.ServiceMessage;
import ch.systemsx.cisd.common.serviceconversation.client.IServiceConversation;

/**
 * @author pkupczyk
 */
public class BaseServiceConversationClientManager implements
        IServiceConversationClientManagerRemote
{

    private Map<ClientConfig, ServiceConversationClientWithConversationTracking> clientConfigToClientMap =
            new HashMap<ClientConfig, ServiceConversationClientWithConversationTracking>();

    private Map<String, ServiceConversationClientWithConversationTracking> conversationIdToClientMap =
            new HashMap<String, ServiceConversationClientWithConversationTracking>();

    @Override
    public void send(ServiceMessage message)
    {
        ServiceConversationClientWithConversationTracking client =
                getClientForConversationId(message.getConversationId());

        if (client != null)
        {
            client.receiveMessage(message);
        }
    }

    @Override
    public void ping()
    {
    }

    public <T> T getService(String serverUrl, Class<T> serviceInterface, String sessionToken,
            Object clientId, int clientTimeout)
    {
        return ServiceConversationServiceProxy.newInstance(
                getClientForServerUrl(serverUrl, clientId, clientTimeout), serviceInterface,
                sessionToken);
    }

    public int getConversationCount()
    {
        return conversationIdToClientMap.size();
    }

    public int getClientCount()
    {
        return clientConfigToClientMap.size();
    }

    private synchronized ServiceConversationClientWithConversationTracking getClientForServerUrl(
            String serverUrl, Object clientId, int clientTimeout)
    {
        ClientConfig clientConfig = new ClientConfig(serverUrl, clientId, clientTimeout);

        ServiceConversationClientWithConversationTracking client =
                clientConfigToClientMap.get(clientConfig);

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
            clientConfigToClientMap.put(clientConfig, client);
        }

        return client;
    }

    private ServiceConversationClientWithConversationTracking getClientForConversationId(
            String conversationId)
    {
        return conversationIdToClientMap.get(conversationId);
    }

    private static class ClientConfig
    {
        private String serverUrl;

        private Object clientId;

        private int clientTimeout;

        public ClientConfig(String serverUrl, Object clientId, int clientTimeout)
        {
            this.serverUrl = serverUrl;
            this.clientId = clientId;
            this.clientTimeout = clientTimeout;
        }

        @Override
        public int hashCode()
        {
            HashCodeBuilder builder = new HashCodeBuilder();
            builder.append(serverUrl);
            builder.append(clientId);
            builder.append(clientTimeout);
            return builder.toHashCode();
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj == null)
                return false;
            if (obj == this)
                return true;
            if (obj.getClass() != getClass())
                return false;

            ClientConfig that = (ClientConfig) obj;
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(serverUrl, that.serverUrl);
            builder.append(clientId, that.clientId);
            builder.append(clientTimeout, that.clientTimeout);
            return builder.isEquals();
        }

    }

}
