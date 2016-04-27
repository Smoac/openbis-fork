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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.service;

import java.util.Properties;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.plugin.service.ICustomASServiceExecutor;
import ch.ethz.sis.openbis.generic.asapi.v3.plugin.service.context.CustomASServiceContext;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;

/**
 * @author Franz-Josef Elmer
 */
public class JythonBasedCustomASServiceExecutor implements ICustomASServiceExecutor
{
    private static final String SCRIPT_PATH = "script-path";

    private final CustomASServiceScriptRunnerFactory factory;

    public JythonBasedCustomASServiceExecutor(Properties properties)
    {
        this(PropertyUtils.getMandatoryProperty(properties, SCRIPT_PATH), CommonServiceProvider.getApplicationServerApi());
    }

    JythonBasedCustomASServiceExecutor(String scriptPath, IApplicationServerApi applicationService)
    {
        factory = new CustomASServiceScriptRunnerFactory(scriptPath, applicationService);
    }

    @Override
    public Object executeService(CustomASServiceContext context, CustomASServiceExecutionOptions options)
    {
        return factory.createServiceRunner(context).process(options);
    }
}
