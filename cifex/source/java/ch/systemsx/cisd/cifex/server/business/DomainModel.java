/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.server.business;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.common.logging.LogInvocationHandler;
import ch.systemsx.cisd.common.mail.IMailClient;

/**
 * Provider of all manager objects.
 *
 * @author Franz-Josef Elmer
 */
public final class DomainModel implements IDomainModel
{
    private final IUserManager userManager;

    /**
     * Constructor only used for unit tests.
     */
    public DomainModel(IDAOFactory daoFactory, IMailClient mailClient)
    {
        this(daoFactory, mailClient, new BeanPostProcessor()
            {
                public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException
                {
                    return bean;
                }

                public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException
                {
                    return bean;
                }
            });
    }
    
    /**
     * Creates an instance based on the specified DAO Factory and mail client. The specified bean post processor is
     * needed to create proxies for the various manager objects which handle transactions. Corresponding manager methods
     * are annotated with <code>@Transactional</code>. In the Spring <code>applicationContext.xml</code> it is assumed 
     * that a the bean post processor is correctly configured with the right TransactionInterceptor.
     */
    public DomainModel(IDAOFactory daoFactory, IMailClient mailClient, BeanPostProcessor processor)
    {
        assert daoFactory != null : "Undefined DAO Factory";
        assert mailClient != null : "Undefined mail client";
        
        userManager = createLoggingProxy(processor, new UserManager(daoFactory));
    }

    private <T> T createLoggingProxy(BeanPostProcessor processor, final T manager)
    {
        Object proxy = processor.postProcessAfterInitialization(manager, "proxy of " + manager.getClass().getName());
        Class<? extends DomainModel> clazz = getClass();
        InvocationHandler invocationHandler =
                new LogInvocationHandler(proxy, manager.getClass().getSimpleName(), clazz);
        final Class<?>[] interfaces = manager.getClass().getInterfaces();
        return cast(Proxy.newProxyInstance(clazz.getClassLoader(), interfaces, invocationHandler));
    }

    @SuppressWarnings("unchecked")
    private <T> T cast(Object proxy)
    {
        return (T) proxy;
    }

    /**
     * Returns the one and only one instance of {@link IUserManager}.
     */
    public final IUserManager getUserManager()
    {
        return userManager;
    }

}
