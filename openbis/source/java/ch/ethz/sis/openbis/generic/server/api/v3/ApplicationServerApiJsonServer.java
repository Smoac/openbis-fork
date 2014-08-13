/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.ethz.sis.openbis.generic.shared.api.v3.IApplicationServerApi;
import ch.systemsx.cisd.openbis.common.api.server.AbstractApiJsonServiceExporter;
import ch.systemsx.cisd.openbis.generic.shared.api.v3.json.ObjectMapperResource;

/**
 * @author Franz-Josef Elmer
 */
// Do not expose the new api before we make sure it does not introduce any security threats or dangerous bugs
// @Controller
@RequestMapping(
{ IApplicationServerApi.JSON_SERVICE_URL, "/openbis" + IApplicationServerApi.JSON_SERVICE_URL })
public class ApplicationServerApiJsonServer extends AbstractApiJsonServiceExporter
{
    @Resource(name = ObjectMapperResource.NAME)
    private ObjectMapper objectMapper;

    @Resource(name = IApplicationServerApi.INTERNAL_SERVICE_NAME)
    private IApplicationServerApi service;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        setObjectMapper(objectMapper);
        establishService(IApplicationServerApi.class, service, IApplicationServerApi.SERVICE_NAME,
                IApplicationServerApi.JSON_SERVICE_URL);
        super.afterPropertiesSet();
    }

}
