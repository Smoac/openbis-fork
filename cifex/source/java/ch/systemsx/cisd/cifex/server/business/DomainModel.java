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

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import ch.systemsx.cisd.cifex.server.business.dataaccess.IDAOFactory;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogInvocationHandler;
import ch.systemsx.cisd.common.mail.IMailClient;

/**
 * Provider of all manager objects.
 * 
 * @author Franz-Josef Elmer
 */
public final class DomainModel implements IDomainModel
{
    private IUserManager userManager;

    private IFileManager fileManager;

    private final IDAOFactory daoFactory;

    private final BeanPostProcessor processor;

    private File fileStore;

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
     * are annotated with <code>@Transactional</code>. In the Spring <code>applicationContext.xml</code> it is assumed that a the bean post
     *                processor is correctly configured with the right TransactionInterceptor.
     */
    public DomainModel(IDAOFactory daoFactory, IMailClient mailClient, BeanPostProcessor processor)
    {
        this.daoFactory = daoFactory;
        this.processor = processor;
        assert daoFactory != null : "Undefined DAO Factory";
        assert mailClient != null : "Undefined mail client";

    }
    
    public final void setFileStorePath(String fileStorePath)
    {
        fileStore = new File(fileStorePath);
        if (fileStore.exists())
        {
            if (fileStore.isDirectory() == false)
            {
                throw new EnvironmentFailureException("File store '" + fileStore.getAbsolutePath()
                        + "' already exists but is not a directory.");
            }
        } else
        {
            boolean successful = fileStore.mkdirs();
            if (successful == false)
            {
                throw new EnvironmentFailureException("Couldn't create file store '" + fileStore.getAbsolutePath()
                        + "' for some unknown reason.");
            }
            
        }
    }

    private <T> T createLoggingProxy(final T manager)
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
        if (userManager == null)
        {
            userManager = createLoggingProxy(new UserManager(daoFactory));
        }
        return userManager;
    }

    /**
     * Returns the one and only one instance of {@link IFileManager}.
     */
    public final IFileManager getFileManager()
    {
        if (fileManager == null)
        {
            fileManager = createLoggingProxy(new FileManager(daoFactory, fileStore));
        }
        return fileManager;
    }

}
