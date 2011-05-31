/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared;

import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.BeanFactory;

/**
 * When running a test suite the Spring application context is initialized only once. Thus when a
 * test replaces the {@link ServiceProvider} application context it needs to restore it back after
 * the its execution.
 * 
 * @author Kaloyan Enimanev
 */
public class ServiceProviderTestWrapper
{

    private static BeanFactory mockApplicationContext;
    private static BeanFactory cachedApplicationContext;

    private final static Map<Class<?>, String /* bean name */> classNameToBeanName;
    static
    {
        classNameToBeanName = new HashMap<Class<?>, String>();
        classNameToBeanName.put(IEncapsulatedOpenBISService.class, "openBIS-service");
        classNameToBeanName.put(IShareIdManager.class, "share-id-manager");
        classNameToBeanName.put(IConfigProvider.class, "config-provider");
        classNameToBeanName
                .put(IHierarchicalContentProvider.class, "hierarchical-content-provider");
    }

    /**
     * caches the existing application context and replaces it temporarily with another one for test
     * purposes.
     */
    @SuppressWarnings("deprecation")
    public static void setApplicationContext(BeanFactory applicationContext)
    {
        if (cachedApplicationContext == null)
        {
            cachedApplicationContext = ServiceProvider.tryGetApplicationContext(false);
        }
        mockApplicationContext = applicationContext;
        ServiceProvider.setBeanFactory(applicationContext);
    }

    /**
     * restore the replaced application context back, so that it will be available for other tests
     * in the same suite.
     */
    @SuppressWarnings("deprecation")
    public static void restoreApplicationContext()
    {
        ServiceProvider.setBeanFactory(cachedApplicationContext);
        cachedApplicationContext = null;
        mockApplicationContext = null;
    }

    /**
     * A helper method for test cases that creates a mock instance and sets it up within the mocked
     * application context of ServiceProvider.
     */
    public static <T> T mock(Mockery mockery, final Class<T> clazz)
    {
        final T mock = mockery.mock(clazz);
        mockery.checking(new Expectations()
            {
                {
                    String beanName = classNameToBeanName.get(clazz);
                    allowing(mockApplicationContext).getBean(beanName);
                    will(returnValue(mock));
                }
            });

        return mock;
    }

}
