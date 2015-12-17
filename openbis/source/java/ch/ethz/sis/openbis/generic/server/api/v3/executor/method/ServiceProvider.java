/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.service.Service;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.service.id.ServiceCode;
import ch.ethz.sis.openbis.generic.as.api.v3.plugin.IServiceExecutor;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class ServiceProvider implements InitializingBean, IServiceProvider
{
    public static final String SERVICES_PROPERTY_KEY = "services";
    public static final String CLASS_KEY = "class";
    public static final String LABEL_KEY = "label";
    public static final String DESCRIPTION_KEY = "description";
    
    private List<Service> services;
    private Map<String, IServiceExecutor> executors = new HashMap<String, IServiceExecutor>();
    
    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    protected ExposablePropertyPlaceholderConfigurer configurer;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        Properties serviceProperties = configurer == null ? new Properties() : configurer.getResolvedProps();
        SectionProperties[] sectionsProperties =
                PropertyParametersUtil.extractSectionProperties(serviceProperties,
                        SERVICES_PROPERTY_KEY, false);
        ArrayList<Service> list = new ArrayList<Service>();
        for (SectionProperties sectionProperties : sectionsProperties)
        {
            String code = sectionProperties.getKey();
            Properties properties = sectionProperties.getProperties();
            String className = PropertyUtils.getMandatoryProperty(properties, CLASS_KEY);
            Service service = new Service();
            service.setCode(new ServiceCode(code));
            service.setLabel(properties.getProperty(LABEL_KEY, code));
            service.setDescription(properties.getProperty(DESCRIPTION_KEY, ""));
            IServiceExecutor serviceExecutor = ClassUtils.create(IServiceExecutor.class, className, properties);
            list.add(service);
            executors.put(code, serviceExecutor);
        }
        services = Collections.unmodifiableList(list);
    }
    
    @Override
    public List<Service> getServices()
    {
        return services;
    }
    
    @Override
    public IServiceExecutor tryGetExecutor(String code)
    {
        return executors.get(code);
    }
    
}
