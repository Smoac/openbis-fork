/*
 * Copyright 2009 ETH Zuerich, CISD
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

import static ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants.DATA_STORE_SERVER_SERVICE_NAME;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;

/**
 * Factory of cached {@link IDataStoreService} instances.
 *
 * @author Franz-Josef Elmer
 */
public class DataStoreServiceFactory implements IDataStoreServiceFactory
{
    private final Map<String, IDataStoreService> services = new HashMap<String, IDataStoreService>();
    
    public IDataStoreService create(String serverURL)
    {
        IDataStoreService service = services.get(serverURL);
        if (service == null)
        {
            service = HttpInvokerUtils.createServiceStub(IDataStoreService.class, serverURL + "/"
                    + DATA_STORE_SERVER_SERVICE_NAME, 5);
            services.put(serverURL, service);
        }
        return service;
    }

}
