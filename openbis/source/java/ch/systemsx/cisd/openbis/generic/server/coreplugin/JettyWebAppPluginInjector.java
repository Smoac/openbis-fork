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

import static ch.systemsx.cisd.openbis.generic.server.coreplugin.CorePluginsInjectingPropertyPlaceholderConfigurer.PLUGIN_TYPE_WEBAPPS;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.unix.Unix;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CorePlugin;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginScanner;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginScanner.ScannerType;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginsUtils;

/**
 * A class that injects web apps into jetty.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class JettyWebAppPluginInjector
{
    private static final String WEBAPP_FOLDER = "webapp";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            JettyWebAppPluginInjector.class);

    private final List<String> webapps;

    private final Map<String, Properties> webappProperties;

    public static final String WEB_APP_FOLDER_PROPERTY = "webapp-folder";

    private Map<String, File> webappToFoldersMap;

    private static Map<String, Properties> extractWebappProperties(Properties props,
            List<String> webapps)
    {
        HashMap<String, Properties> map = new HashMap<String, Properties>();

        for (String webapp : webapps)
        {
            Properties webappProperties =
                    PropertyParametersUtil.extractSingleSectionProperties(props, webapp, false)
                            .getProperties();
            map.put(webapp, webappProperties);
        }
        return map;
    }

    public JettyWebAppPluginInjector(Properties props)
    {
        List<String> appList =
                PropertyUtils.tryGetListInOriginalCase(props, BasicConstant.WEB_APPS_PROPERTY);
        webapps = (null == appList) ? Collections.<String> emptyList() : appList;
        webappProperties = extractWebappProperties(props, webapps);
        webappToFoldersMap = new HashMap<String, File>();
        String corePluginsFolder = CorePluginsUtils.getCorePluginsFolder(props, ScannerType.AS);
        CorePluginScanner scanner = new CorePluginScanner(corePluginsFolder, ScannerType.AS);
        List<CorePlugin> plugins = scanner.scanForPlugins();
        for (CorePlugin plugin : plugins)
        {
            File webappsFolder =
                    new File(corePluginsFolder, CorePluginScanner.constructPath(plugin,
                            ScannerType.AS, PLUGIN_TYPE_WEBAPPS));
            if (webappsFolder.isDirectory())
            {
                File[] pluginFolders = webappsFolder.listFiles();
                for (File folder : pluginFolders)
                {
                    String webappName = folder.getName();
                    if (webappName.startsWith(".") == false)
                    {
                        Properties webappProps = webappProperties.get(webappName);
                        if (webappProps == null)
                        {
                            break;
                        }
                        String f = webappProps.getProperty(WEB_APP_FOLDER_PROPERTY);
                        webappToFoldersMap.put(webappName, new File(f));
                    }
                }
            }
        }
    }

    public void injectWebApps()
    {
        operationLog.info("Inject the following web apps: " + webapps);
        // Leave if there is nothing to do
        if (webapps.isEmpty())
        {
            return;
        }
        List<File> targets = findInjectionTargets();
        for (String webapp : webapps)
        {
            File folder = webappToFoldersMap.get(webapp);
            if (folder == null)
            {
                continue;
            }
            String path = folder.getAbsolutePath();
            for (File target : targets)
            {
                File link = new File(target, webapp);
                if (link.exists() == false)
                {
                    String linkPath = link.getAbsolutePath();
                    Unix.createSymbolicLink(path, linkPath);
                    operationLog.info("WebApp '" + webapp + "': Symbolic link " + linkPath + " -> "
                            + path);
                }
            }
        }
        return;
    }

    private List<File> findInjectionTargets()
    {
        List<File> list = new ArrayList<File>();
        String jettyHome = System.getProperty("jetty.home");
        if (jettyHome != null)
        {
            File webappFolder = new File(jettyHome + "/webapps/openbis/" + WEBAPP_FOLDER);
            webappFolder.mkdirs();
            list.add(webappFolder);
        } else
        {
            File[] files = new File("targets/www").listFiles();
            for (File file : files)
            {
                if (file.getName().equals("WEB-INF") == false)
                {
                    File webappFolder = new File(file, WEBAPP_FOLDER);
                    webappFolder.mkdirs();
                    list.add(webappFolder);
                }
            }
        }
        return list;
    }

}
