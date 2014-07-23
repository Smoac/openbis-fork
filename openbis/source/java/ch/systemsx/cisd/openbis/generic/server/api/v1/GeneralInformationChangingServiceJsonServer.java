/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.api.v1;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.systemsx.cisd.openbis.common.api.server.AbstractApiJsonServiceExporter;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.json.ObjectMapperResource;

/**
 * @author Kaloyan Enimanev
 */
@Controller
@RequestMapping(
{ IGeneralInformationChangingService.JSON_SERVICE_URL,
        "/openbis" + IGeneralInformationChangingService.JSON_SERVICE_URL })
public class GeneralInformationChangingServiceJsonServer extends AbstractApiJsonServiceExporter
{

    @Resource(name = ObjectMapperResource.NAME)
    private ObjectMapper objectMapper;

    @Resource(name = ResourceNames.GENERAL_INFORMATION_CHANGING_SERVICE_SERVER)
    private IGeneralInformationChangingService service;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        setObjectMapper(objectMapper);
        establishService(IGeneralInformationChangingService.class, service,
                IGeneralInformationService.SERVICE_NAME,
                IGeneralInformationService.JSON_SERVICE_URL);
        super.afterPropertiesSet();
    }

}
