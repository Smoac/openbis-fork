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

package ch.systemsx.cisd.openbis.generic.server.business;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.apache.log4j.Level;
import org.springframework.beans.factory.config.BeanPostProcessor;

import ch.systemsx.cisd.common.logging.LogInvocationHandler;
import ch.systemsx.cisd.openbis.generic.server.business.bo.GenericBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGenericBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;

/**
 * @author Tomasz Pylak
 */
public class GenericManagers
{
    private IPersonManager personManager;

    public GenericManagers(final IAuthorizationDAOFactory daoFactory,
            final BeanPostProcessor processor)
    {
        assert daoFactory != null : "Undefined DAO Factory";

        final IGenericBusinessObjectFactory boFactory =
                new GenericBusinessObjectFactory(daoFactory);
        personManager = createProxy(processor, new PersonManager(daoFactory, boFactory));
    }

    protected final <T> T createProxy(final BeanPostProcessor processor, final T manager)
    {
        final Object proxy =
                processor.postProcessAfterInitialization(manager, "proxy of "
                        + manager.getClass().getName());
        final Class<?> clazz = getClass();
        final InvocationHandler invocationHandler =
                new LogInvocationHandler(proxy, manager.getClass().getSimpleName(), Level.DEBUG,
                        clazz, true);
        final Class<?>[] interfaces = manager.getClass().getInterfaces();
        return cast(Proxy.newProxyInstance(clazz.getClassLoader(), interfaces, invocationHandler));
    }

    @SuppressWarnings("unchecked")
    protected final static <T> T cast(final Object proxy)
    {
        return (T) proxy;
    }

    /**
     * Returns the person manager.
     */
    public final IPersonManager getPersonManager()
    {
        return personManager;
    }

}
