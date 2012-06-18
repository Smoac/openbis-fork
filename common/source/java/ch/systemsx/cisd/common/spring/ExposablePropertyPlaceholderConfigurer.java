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

package ch.systemsx.cisd.common.spring;

import java.util.HashSet;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.util.StringUtils;

/**
 * Bean that should be used instead of the {@link PropertyPlaceholderConfigurer} if you want to have
 * access to the resolved properties not obligatory from the Spring context. e.g. from JSP or so.
 * 
 * @author Christian Ribeaud
 */
public class ExposablePropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer
{
    /** Standard bean name in an application context file. */
    public static final String PROPERTY_CONFIGURER_BEAN_NAME = "propertyConfigurer";
    
    private Properties resolvedProps;

    /** Returns the resolved properties. */
    public final Properties getResolvedProps()
    {
        return resolvedProps;
    }

    //
    // PropertyPlaceholderConfigurer
    //

    @Override
    protected final String convertPropertyValue(final String originalValue)
    {
        // Can handle null value
        return StringUtils.trimWhitespace(originalValue);
    }

    @Override
    protected final void processProperties(
            final ConfigurableListableBeanFactory beanFactoryToProcess, final Properties props)
            throws BeansException
    {
        super.processProperties(beanFactoryToProcess, props);
        resolvedProps = new Properties();
        for (final Object key : props.keySet())
        {
            final String keyStr = key.toString();
            resolvedProps.setProperty(keyStr, parseStringValue(props.getProperty(keyStr), props,
                    new HashSet<Object>()));
        }
    }
}