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

package ch.systemsx.cisd.common.serviceconversation;

import java.io.Serializable;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;


import ch.systemsx.cisd.common.serviceconversation.server.IService;
import ch.systemsx.cisd.common.serviceconversation.server.IServiceFactory;
import ch.systemsx.cisd.common.serviceconversation.server.ServiceConversationServer;

public final class RpcServiceFactory<T extends ConversationalServer> implements IServiceFactory 
{

    private ServiceConversationServer server;
    private T service;
    private SessionFactory factory;
    private Class<?> providedInterface;
    private int timeout;
    
    public RpcServiceFactory(ServiceConversationServer server, T service, Class<?> providedInterface, int clientTimeoutInMillis, SessionFactory factory) {
        this.server = server;
        this.service = service;
        this.providedInterface = providedInterface;
        this.timeout = clientTimeoutInMillis;
        this.factory = factory;
    }

    public final String getServiceTypeId() {
        return this.providedInterface.getName();
    }
    
    public IService create()
    {
        return new IService()
        {

            public void run(IServiceMessenger messenger)
            {
                MethodCall call =
                        (MethodCall) messenger.receive(Serializable.class);

                Session s = SessionFactoryUtils.doGetSession(factory, true);
                TransactionSynchronizationManager.bindResource(factory, new SessionHolder(s));
                Transaction tx = s.beginTransaction();
                
                Serializable result = null;
                boolean success = false;
                try {
                    result = call.executeOn(service, server, messenger.getId(), timeout);
                    success = true;
                } finally {
                    if (success) {
                        tx.commit();
                    } else {
                        tx.rollback();
                    }
                    s.close();
                    TransactionSynchronizationManager.unbindResource(factory);
                }
                messenger.send(result);
            }
        };
    }

    public int getClientTimeoutMillis()
    {
        return this.timeout;
    }
    
    public boolean interruptServiceOnClientException()
    {
        return false;
    }

}
