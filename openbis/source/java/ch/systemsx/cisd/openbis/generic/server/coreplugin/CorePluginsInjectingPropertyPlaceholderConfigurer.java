/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.coreplugin;

import java.io.IOException;
import java.util.Properties;

import ch.systemsx.cisd.common.maintenance.MaintenanceTaskUtils;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CustomImport;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginScanner.ScannerType;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginsInjector;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.IPluginType;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.PluginType;

/**
 * Property placeholder configurer which merges service.properties with core plugins properties.
 * 
 * @author Franz-Josef Elmer
 */
public class CorePluginsInjectingPropertyPlaceholderConfigurer extends
        ExposablePropertyPlaceholderConfigurer
{

    @Override
    protected void loadProperties(Properties properties) throws IOException
    {
        super.loadProperties(properties);
        PluginType maintenanceTasks =
                new PluginType("maintenance-tasks",
                        MaintenanceTaskUtils.DEFAULT_MAINTENANCE_PLUGINS_PROPERTY_NAME);
        PluginType customImports =
                new PluginType("custom-imports",
                        CustomImport.PropertyNames.CUSTOM_IMPORTS.getName());
        PluginType queryDatabases = new PluginType("query-databases", "query-databases");
        PluginType miscellaneous = new PluginType("miscellaneous", null);

        new CorePluginsInjector(ScannerType.AS, new IPluginType[]
            { maintenanceTasks, customImports, queryDatabases, miscellaneous })
                .injectCorePlugins(properties);
    }

}
