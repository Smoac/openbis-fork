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

    private static BeanFactory cachedApplicationContext;

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
    }

}
