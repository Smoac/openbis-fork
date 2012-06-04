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

package ch.systemsx.cisd.common.conversation;

import ch.systemsx.cisd.common.serviceconversation.ServiceConversationDTO;
import ch.systemsx.cisd.common.serviceconversation.ServiceMessage;
import ch.systemsx.cisd.common.serviceconversation.client.IRemoteServiceConversationServer;
import ch.systemsx.cisd.common.serviceconversation.client.ServiceConversationClient;

/**
 * RmiConversationController controls the client side communication with the service conversation 
 * framework. Clients can use RmiConversationController to create conversational references to remote
 * services and to process incoming service conversation related messages.
 * 
 * @author anttil
 */
public class RmiConversationController {
    private ServiceConversationClient client = null;
    private final String callbackUrl;
    
    public RmiConversationController(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
    
    public synchronized <T extends ConversationalRmiServer, U extends T> 
        T getConversationalReference(final String sessionToken, final U service, Class<T> type) {

        if (this.client == null) {
            this.client = new ServiceConversationClient(
                    new IRemoteServiceConversationServer() {
                        @Override
                        public ServiceConversationDTO startConversation(String typeId)
                        {
                            return service.startConversation(sessionToken, callbackUrl, typeId);
                        }
                    },
                    service);
        }
        return RmiProxy.newInstance(type, client);
    }
    
    public void process(ServiceMessage message) {
        this.client.getIncomingResponseMessageTransport().send(message);
    }
}
