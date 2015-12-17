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
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.service.ExecutionOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.service.id.IServiceId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.service.id.ServiceCode;
import ch.ethz.sis.openbis.generic.as.api.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.as.api.v3.exceptions.UnsupportedObjectIdException;
import ch.ethz.sis.openbis.generic.as.api.v3.plugin.service.IServiceExecutor;
import ch.ethz.sis.openbis.generic.as.api.v3.plugin.service.context.ServiceContext;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class ExecuteServiceMethodExecutor implements IExecuteServiceMethodExecutor
{
    @Autowired
    private IServiceProvider serviceProvider;

    @Override
    public Serializable executeService(String sessionToken, IServiceId serviceId, ExecutionOptions options)
    {
        if (serviceId instanceof ServiceCode == false)
        {
            throw new UnsupportedObjectIdException(serviceId);
        }
        ServiceCode serviceCode = (ServiceCode) serviceId;
        IServiceExecutor serviceExecutor = serviceProvider.tryGetExecutor(serviceCode.getPermId());
        if (serviceExecutor == null)
        {
            throw new ObjectNotFoundException(serviceId);
        }
        ServiceContext serviceContext = new ServiceContext();
        serviceContext.setSessionToken(sessionToken);
        return serviceExecutor.executeService(serviceContext, options);
    }

}
