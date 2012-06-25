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

import java.io.Serializable;

import ch.systemsx.cisd.common.serviceconversation.IServiceMessenger;
import ch.systemsx.cisd.common.serviceconversation.server.IService;
import ch.systemsx.cisd.common.serviceconversation.server.IServiceFactory;
import ch.systemsx.cisd.common.serviceconversation.server.ServiceConversationServer;

/**
 * A factory for RMI services. Runs incoming MethodInvocations within transactions.
 * 
 * @author anttil
 */
public final class RmiServiceFactory<T extends ConversationalRmiServer> implements IServiceFactory
{

    private ServiceConversationServer server;

    private T service;

    private Class<?> providedInterface;

    private int timeout;

    public RmiServiceFactory(ServiceConversationServer server, T service,
            Class<?> providedInterface,
            int clientTimeoutInMillis)
    {
        this.server = server;
        this.service = service;
        this.providedInterface = providedInterface;
        this.timeout = clientTimeoutInMillis;
    }

    @Override
    public final String getServiceTypeId()
    {
        return this.providedInterface.getName();
    }

    @Override
    public IService create()
    {
        return new IService()
            {

                @Override
                public void run(IServiceMessenger messenger)
                {
                    MethodInvocation call =
                            (MethodInvocation) messenger.receive(Serializable.class);

                    Serializable result =
                            call.executeOn(service, server, messenger.getId(), timeout);
                    messenger.send(result);
                }
            };
    }

    @Override
    public int getClientTimeoutMillis()
    {
        return this.timeout;
    }

    @Override
    public boolean interruptServiceOnClientException()
    {
        return false;
    }

}
