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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.service.Service;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.service.fetchoptions.ServiceFetchOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.service.search.ServiceSearchCriteria;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class SearchServiceMethodExecutor implements ISearchServiceMethodExecutor
{
    @Autowired
    private IServiceProvider serviceProvider;
    
    @Override
    public SearchResult<Service> search(String sessionToken, ServiceSearchCriteria searchCriteria, ServiceFetchOptions fetchOptions)
    {
        List<Service> services = serviceProvider.getServices();
        return new SearchResult<>(services, services.size());
    }

}
