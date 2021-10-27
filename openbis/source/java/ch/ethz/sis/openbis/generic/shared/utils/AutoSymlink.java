/*
 * Copyright 2021 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.shared.utils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.service.CustomASServiceProvider;
import ch.systemsx.cisd.base.unix.Unix;
import ch.systemsx.cisd.common.io.PropertyIOUtils;
import ch.systemsx.cisd.common.maintenance.MaintenanceTaskUtils;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginScanner.ScannerType;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginsInjector;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginsUtils;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.IPluginType;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.PluginType;

/**
 * @author Franz-Josef Elmer
 */
public class AutoSymlink
{

    public static void main(String[] args) throws Exception
    {
        Properties properties 
                = ExtendedProperties.createWith(PropertyIOUtils.loadProperties(getPathToServiceProperties()));
        CorePluginsUtils.addCorePluginsProperties(properties, ScannerType.AS);
        PluginType maintenanceTasks =
                new PluginType("maintenance-tasks",
                        MaintenanceTaskUtils.DEFAULT_MAINTENANCE_PLUGINS_PROPERTY_NAME);
        PluginType services =
                new PluginType("services",
                        CustomASServiceProvider.SERVICES_PROPERTY_KEY);
        PluginType miscellaneous = new PluginType("miscellaneous", null);
        CorePluginsInjector injector =
                new CorePluginsInjector(ScannerType.AS, new IPluginType[] { maintenanceTasks, services, miscellaneous });
        Map<String, File> pluginFolders = injector.injectCorePlugins(properties);
        createSymlinks(getLibFolder(), pluginFolders);
    }

    private static String getPathToServiceProperties()
    {
        String jettyHome = System.getProperty("jetty.base");
        if (jettyHome != null)
        {
            return jettyHome + "etc/service.properties";
        }
        return "targets/www/WEB-INF/classes/service.properties";
    }

    public static File getLibFolder()
    {
        String jettyHome = System.getProperty("jetty.base");
        if (jettyHome != null)
        {
            return new File(jettyHome + "webapps/openbis/WEB-INF/lib/");
        }
        return new File("targets/www/lib");
    }

    public static void createSymlinks(File libDir, Map<String, File> pluginFolders)
    {
        try
        {
            for (File link : libDir.listFiles())
            {
                if (link.getName().startsWith("autolink-"))
                {
                    link.delete();
                }
            }

            for (String key : pluginFolders.keySet())
            {
                File pluginLibFolder = new File(pluginFolders.get(key).getCanonicalPath() + "/lib");
                if (pluginLibFolder.exists())
                {
                    for (File jar : pluginLibFolder.listFiles())
                    {
                        if (jar.isFile() && jar.getName().endsWith(".jar"))
                        {
                            String link =
                                    libDir.getAbsolutePath() + "/autolink-" + key + "-"
                                            + jar.getName();
                            Unix.createSymbolicLink(jar.getAbsolutePath(), link);
                        }
                    }
                }
            }
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

}
