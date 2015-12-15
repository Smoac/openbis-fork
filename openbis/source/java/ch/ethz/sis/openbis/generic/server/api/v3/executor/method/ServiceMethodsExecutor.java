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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.service.Service;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.service.fetchoptions.ServiceFetchOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.service.id.IServiceId;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class ServiceMethodsExecutor extends AbstractMethodExecutor implements IServiceMethodsExecutor, InitializingBean
{
    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    protected ExposablePropertyPlaceholderConfigurer configurer;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        Properties props = configurer == null ? new Properties() : configurer.getResolvedProps();
    }
    
    @Override
    public List<Service> listServices(String sessionToken, ServiceFetchOptions fetchOptions)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Serializable executeService(String sessionToken, IServiceId serviceId, Map<String, String> parameters)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
