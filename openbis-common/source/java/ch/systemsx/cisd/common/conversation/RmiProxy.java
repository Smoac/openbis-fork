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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;

import ch.systemsx.cisd.common.serviceconversation.client.IServiceConversation;
import ch.systemsx.cisd.common.serviceconversation.client.ServiceConversationClient;

/**
 * RpcProxy is a dynamic proxy that passes RPC calls to a ConversationalServer through 
 * service conversation framework.
 *
 * @author anttil
 */
public class RmiProxy implements InvocationHandler 
{
    private ServiceConversationClient client;
    private String typeId;
    
    @SuppressWarnings("unchecked")
    static <T extends ConversationalRmiServer> T newInstance(Class<T> clazz, ServiceConversationClient client) {

        Collection<Class<?>> interfaces = new HashSet<Class<?>>();
        interfaces.add(clazz);
        return (T)java.lang.reflect.Proxy.newProxyInstance(
            clazz.getClassLoader(),
            interfaces.toArray(new Class<?>[0]),
            new RmiProxy(client, clazz.getName()));
    }

    private RmiProxy(ServiceConversationClient client, String typeId) {
        this.client = client;
        this.typeId = typeId;
    }

    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable
    {
        IServiceConversation conversation = this.client.startConversation(this.typeId);
        try {
            conversation.send(new MethodInvocation(m.getName(), args));
            return conversation.receive(Serializable.class);
        } finally {
            conversation.close();
        }
    }
}
