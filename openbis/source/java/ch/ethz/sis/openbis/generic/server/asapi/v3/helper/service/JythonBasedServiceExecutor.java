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

import java.io.Serializable;
import java.util.Properties;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.ExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.plugin.service.IServiceExecutor;
import ch.ethz.sis.openbis.generic.asapi.v3.plugin.service.context.ServiceContext;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class JythonBasedServiceExecutor implements IServiceExecutor
{
    private static final String SCRIPT_PATH = "script-path";
    
    private final ScriptRunnerFactory factory;

    public JythonBasedServiceExecutor(Properties properties)
    {
        this(PropertyUtils.getMandatoryProperty(properties, SCRIPT_PATH), CommonServiceProvider.getApplicationServerApi());
    }
    
    JythonBasedServiceExecutor(String scriptPath, IApplicationServerApi applicationService)
    {
        factory = new ScriptRunnerFactory(scriptPath, applicationService);
    }

    @Override
    public Serializable executeService(ServiceContext context, ExecutionOptions options)
    {
        return factory.createServiceRunner(context).process(options);
    }

}
