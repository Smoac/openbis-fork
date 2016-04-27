/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import ch.systemsx.cisd.base.unix.Unix;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginScanner.ScannerType;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginsInjector;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginsUtils;

/**
 * Helper application which creates symbolic links for JAR files in DSS core plugins in folder lib.
 *
 * @author Franz-Josef Elmer
 */
public class AutoSymlink
{

    public static void main(String[] args)
    {
        ExtendedProperties properties =
                DssPropertyParametersUtil
                        .loadProperties(DssPropertyParametersUtil.SERVICE_PROPERTIES_FILE);
        CorePluginsUtils.addCorePluginsProperties(properties, ScannerType.DSS);
        ExtendedProperties serviceProperties =
                DssPropertyParametersUtil.extendProperties(properties);
        CorePluginsInjector injector =
                new CorePluginsInjector(ScannerType.DSS, DssPluginType.values());
        Map<String, File> pluginFolders =
                injector.injectCorePlugins(serviceProperties);

        try
        {
            File libDir = new File("lib");
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
