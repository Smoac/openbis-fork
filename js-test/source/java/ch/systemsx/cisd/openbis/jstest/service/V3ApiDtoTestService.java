/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.jstest.service;

import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.plugin.service.ICustomASServiceExecutor;
import ch.ethz.sis.openbis.generic.asapi.v3.plugin.service.context.CustomASServiceContext;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class V3ApiDtoTestService implements ICustomASServiceExecutor
{
    public V3ApiDtoTestService(Properties properties)
    {
    }

    @Override
    public Object executeService(CustomASServiceContext context, CustomASServiceExecutionOptions options)
    {
        Map<String, Object> parameters = options.getParameters();
        String sessionToken = context.getSessionToken();
        System.out.println("SESSION TOKEN: " + sessionToken);
        Set<Entry<String, Object>> entrySet = parameters.entrySet();
        System.out.println("PARAMETERS:");
        for (Entry<String, Object> entry : entrySet)
        {
            String key = entry.getKey();
            Object value = entry.getValue();
            System.out.println(">>>>> " + key + " = " + value + (value == null ? "" : "[" + value.getClass().getName() + "]"));
        }
        Space space = new Space();
        space.setCode("SPACE1");
        space.setDescription("a space");
        space.setRegistrationDate(new Date(1234567890));
        return space;
    }

}
