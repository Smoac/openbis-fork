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
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CorePlugin;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginScanner;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginScanner.ScannerType;

/**
 * A class that injects web apps into jetty.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class JettyWebAppPluginInjector
{
    private static final String WEBAPP_FOLDER = "webapp";

    /**
     * A utility class that generates a configuration file for a Jetty context for a webapp.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static class ContextConfiguration
    {
        private final String webapp;

        private final Properties properties;

        public ContextConfiguration(String webapp, Properties properties)
        {
            this.webapp = webapp;
            this.properties = properties;
        }

        public String getConfigurationOrNull()
        {

            String resourceBase =
                    properties.getProperty(JettyWebAppPluginInjector.WEB_APP_FOLDER_PROPERTY);
            if (null == resourceBase)
            {
                JettyWebAppPluginInjector.operationLog.error("No configuration property for "
                        + JettyWebAppPluginInjector.WEB_APP_FOLDER_PROPERTY
                        + " was found in webapp properties :\n" + properties.toString());
                return null;
            }
            String configuration =
                    "<Configure class=\"org.eclipse.jetty.server.handler.ContextHandler\">\n"
                            + "  <Call class=\"org.eclipse.jetty.util.log.Log\" name=\"debug\"><Arg>Configure ["
                            + webapp
                            + "] webapp</Arg></Call>\n"
                            + "  <Set name=\"contextPath\">/"
                            + webapp
                            + "</Set>\n"
                            + "  <Set name=\"resourceBase\">"
                            + resourceBase
                            + "</Set>\n"
                            + "  <Set name=\"handler\">\n"
                            + "    <New class=\"org.eclipse.jetty.server.handler.ResourceHandler\">\n"
                            + "      <Set name=\"welcomeFiles\">\n"
                            + "        <Array type=\"String\">\n"
                            + "          <Item>index.html</Item>\n" + "        </Array>\n"
                            + "      </Set>\n"
                            + "      <Set name=\"cacheControl\">max-age=3600,public</Set>\n"
                            + "    </New>\n" + "  </Set>\n" + "</Configure>";

            return configuration;
        }
    }

    // This is the folder referenced in jetty.xml. It must have the same name as in the
    // jetty.xml configuration file.
    private static final String CONTEXT_FOLDER = "contexts";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            JettyWebAppPluginInjector.class);

    private final List<String> webapps;

    private final Map<String, Properties> webappProperties;

    // This is initialized in ensureContextFolderExists and is invalid before that
    // method runs
    private File contextsFolder = null;

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
        String corePluginsFolder = props.getProperty("core-plugins-folder", "../../core-plugins");
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
                        String f =
                                webappProperties.get(webappName).getProperty(
                                        WEB_APP_FOLDER_PROPERTY);
                        webappToFoldersMap.put(webappName, new File(folder, f));
                    }
                }
            }
        }
    }

    public void injectWebApps()
    {
        logWebappsToInject();

        // Leave if there is nothing to do
        if (webapps.isEmpty())
        {
            return;
        }
        if (false == isRunningUnderJetty())
        {
            List<File> targets = findInjectionTargets();
            for (String webapp : webapps)
            {
                File folder = webappToFoldersMap.get(webapp);
                String path = folder.getAbsolutePath();
                for (File target : targets)
                {
                    File link = new File(target, webapp);
                    if (link.exists() == false)
                    {
                        String linkPath = link.getAbsolutePath();
                        Unix.createSymbolicLink(path, linkPath);
                        operationLog.info("WebApp '" + webapp + "': Symbolic link " + linkPath
                                + " -> " + path);
                    }
                }
            }
            return;
        }
        if (false == ensureContextFolderExists())
        {
            operationLog.error("Could not create folder " + contextsFolder.getAbsolutePath()
                    + ". Cannot inject webapps.");
            return;
        }
        for (String webapp : webapps)
        {
            injectWebapp(webapp, webappProperties.get(webapp));
        }
    }

    private List<File> findInjectionTargets()
    {
        List<File> list = new ArrayList<File>();
        String jettyHome = System.getProperty("jetty.home");
        if (jettyHome != null)
        {
            list.add(new File(jettyHome + "/webapps/openbis/" + WEBAPP_FOLDER));
        } else
        {
            File[] files = new File("targets/www").listFiles();
            for (File file : files)
            {
                File webappFolder = new File(file, WEBAPP_FOLDER);
                if (webappFolder.isDirectory())
                {
                    list.add(webappFolder);
                }
            }
        }
        return list;
    }

    private void logWebappsToInject()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Found " + webapps.size());
        if (1 == webapps.size())
        {
            sb.append(" webapp");
        } else
        {
            sb.append(" webapps");
        }
        sb.append(" to inject.");

        operationLog.info(sb.toString());
    }

    private boolean ensureContextFolderExists()
    {
        // This must be non-null because isRunningUnderJetty is true
        String jettyHomePath = System.getProperty("jetty.home");
        contextsFolder = new File(jettyHomePath, CONTEXT_FOLDER);
        if (false == contextsFolder.exists())
        {
            return contextsFolder.mkdir();
        }
        return true;
    }

    private boolean isRunningUnderJetty()
    {
        return null != System.getProperty("jetty.home");
    }

    private void injectWebapp(String webapp, Properties props)
    {
        String webappDisplayName = webapp;
        operationLog.info("Injecting webapp [" + webappDisplayName + "]");
        ContextConfiguration config = new ContextConfiguration(webappDisplayName, props);
        File contextConfig = new File(contextsFolder, webappDisplayName + ".xml");
        String configContent = config.getConfigurationOrNull();
        if (null != configContent)
        {
            FileUtilities.writeToFile(contextConfig, configContent);
        }
    }
}
