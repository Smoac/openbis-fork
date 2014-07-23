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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ch.ethz.sis.openbis.generic.shared.api.v3.IApplicationServerApi;
import ch.systemsx.cisd.openbis.common.api.server.AbstractApiServiceExporter;

/**
 * @author Franz-Josef Elmer
 */
@Controller
@RequestMapping(
    { IApplicationServerApi.SERVICE_URL, "/openbis" + IApplicationServerApi.SERVICE_URL })
public class ApplicationServerApiServer extends AbstractApiServiceExporter
{
    @Resource(name = IApplicationServerApi.INTERNAL_SERVICE_NAME)
    private IApplicationServerApi service;

    @Override
    public void afterPropertiesSet()
    {
        establishService(IApplicationServerApi.class, service, IApplicationServerApi.SERVICE_NAME,
                IApplicationServerApi.SERVICE_URL);
        super.afterPropertiesSet();
    }

}
