/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IStorage;
import ch.systemsx.cisd.bds.v1_0.DataStructureV1_0;
import ch.systemsx.cisd.bds.v1_0.DataStructureV1_0Proxy;
import ch.systemsx.cisd.bds.v1_1.DataStructureV1_1;
import ch.systemsx.cisd.bds.v1_1.DataStructureV1_1Proxy;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.ClassUtils;

/**
 * Factory of data structures. Currently only structures compatible with Version 1.0 can be created.
 * 
 * @author Franz-Josef Elmer
 */
public final class DataStructureFactory
{
    /**
     * The {@link IDataStructure} implementation per version.
     */
    private static final Factory<IDataStructure> factory = new Factory<IDataStructure>();

    /**
     * Proxy classes for {@link IDataStructure} extended interfaces.
     */
    private static final Map<Class<?>, Class<?>> proxyClasses = new HashMap<Class<?>, Class<?>>();

    static
    {
        factory.register(new Version(1, 0), DataStructureV1_0.class);
        factory.register(new Version(1, 1), DataStructureV1_1.class);
        proxyClasses.put(DataStructureV1_0.class, DataStructureV1_0Proxy.class);
        proxyClasses.put(DataStructureV1_1.class, DataStructureV1_1Proxy.class);
    }

    private DataStructureFactory()
    {
        // Can not be instantiated
    }

    /**
     * Returns the class of the object returned after invoking
     * {@link #createDataStructure(IStorage, Version)}.
     * 
     * @param version Version of the data structure.
     * @throws DataStructureException if no data structure can be created for the specified version.
     */
    public static Class<? extends IDataStructure> getDataStructureClassFor(final Version version)
    {
        return factory.getClassFor(version);
    }

    /**
     * Creates a data structure for the specified version.
     * 
     * @param storage Storage behind the data structure.
     * @param version Version of the data structure to be created.
     * @throws EnvironmentFailureException found data structure class has not an appropriated
     *             constructor.
     * @throws DataStructureException if no data structure can be created for the specified version.
     */
    public final static IDataStructure createDataStructure(final IStorage storage,
            final Version version)
    {
        IDataStructure dataStructure = factory.create(IStorage.class, storage, version);
        final Class<?> dataStructureClazz = dataStructure.getClass();
        final Class<?>[] interfaces = dataStructure.getClass().getInterfaces();
        dataStructure =
                (IDataStructure) Proxy.newProxyInstance(
                        DataStructureFactory.class.getClassLoader(), interfaces,
                        new DataStructureProxy<IDataStructure>(dataStructure));
        return proxy(dataStructureClazz, dataStructure);
    }

    @SuppressWarnings("unchecked")
    private final static <T extends IDataStructure> T proxy(final Class<?> dataStructureClass,
            final T dataStructure)
    {
        final Class<?> proxy = proxyClasses.get(dataStructureClass);
        if (proxy != null)
        {
            return (T) ClassUtils.create(IDataStructure.class, proxy, dataStructure);
        }
        return dataStructure;
    }

    //
    // Helper classes
    //

    /**
     * This {@link InvocationHandler} allows calls for methods that have not been specified in
     * <code>NO_PROXIED_METHODS</code>, only if
     * {@link IDataStructure#open(ch.systemsx.cisd.bds.IDataStructure.Mode)} or
     * {@link IDataStructure#create()} has been called before.
     * 
     * @author Christian Ribeaud
     */
    private static class DataStructureProxy<T extends IDataStructure> implements InvocationHandler
    {
        private final T dataStructure;

        private final static String[] NO_PROXIED_METHODS =
            { "isOpenOrCreated", "create", "open", "getVersion", "hashCode", "equals", "toString" };

        DataStructureProxy(final T dataStructure)
        {
            this.dataStructure = dataStructure;
        }

        /**
         * Asserts that this {@link IDataStructure} is already opened or created otherwise a
         * {@link IllegalStateException} is thrown.
         */
        private final void assertOpenOrCreated()
        {
            if (dataStructure.isOpenOrCreated() == false)
            {
                throw new IllegalStateException("Data structure should first be opened or created.");
            }
        }

        //
        // InvocationHandler
        //

        public final Object invoke(final Object proxy, final Method method, final Object[] args)
                throws Throwable

        {
            final String methodName = method.getName();
            if (ArrayUtils.indexOf(NO_PROXIED_METHODS, methodName) < 0)
            {
                assertOpenOrCreated();
            }
            try
            {
                return method.invoke(dataStructure, args);
            } catch (final InvocationTargetException ex)
            {
                final Throwable cause = ex.getCause();
                if (cause instanceof DataStructureException)
                {
                    throw cause;
                }
                throw ex;
            }
        }
    }
}
