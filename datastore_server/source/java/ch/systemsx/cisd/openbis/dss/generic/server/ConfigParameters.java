/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil.SectionProperties;

/**
 * Configuration parameters for the Data Set Download Server.
 * 
 * @author Franz-Josef Elmer
 */
final class ConfigParameters
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ConfigParameters.class);

    static final String SERVER_URL_KEY = "server-url";

    static final String PORT_KEY = "port";

    static final String STOREROOT_DIR_KEY = "storeroot-dir";

    static final String SESSION_TIMEOUT_KEY = "session-timeout";

    private static final String KEYSTORE = "keystore.";

    static final String USE_SSL = "use-ssl";

    static final String KEYSTORE_PATH_KEY = KEYSTORE + "path";

    static final String KEYSTORE_PASSWORD_KEY = KEYSTORE + "password";

    static final String KEYSTORE_KEY_PASSWORD_KEY = KEYSTORE + "key-password";

    static final String PLUGIN_SERVICES_LIST_KEY = "plugin-services";

    static final String PLUGIN_SERVICE_CLASS_KEY = "class";

    static final String PLUGIN_SERVICE_PATH_KEY = "path";

    // PropertyParametersUtil

    private final File storePath;

    private final int port;

    private final String serverURL;

    private final int sessionTimeout;

    private final boolean useSSL;

    private final String keystorePath;

    private final String keystorePassword;

    private final String keystoreKeyPassword;

    private final List<PluginServlet> pluginServlets;

    public static final class PluginServlet
    {
        private final String servletClass;

        private final String servletPath;

        private final Properties servletProperties;

        public PluginServlet(String servletClass, String servletPath, Properties servletProperties)
        {
            this.servletClass = servletClass;
            this.servletPath = servletPath;
            this.servletProperties = servletProperties;
        }

        public String getServletClass()
        {
            return servletClass;
        }

        /** URL path at which the servlet will be deployed */
        public String getServletPath()
        {
            return servletPath;
        }

        /** Any additional properties specified in the properties file */
        public Properties getServletProperties()
        {
            return servletProperties;
        }

        @Override
        public String toString()
        {
            return "class = " + servletClass + ", path = " + servletPath;
        }

    }

    /**
     * Creates an instance based on the specified properties.
     * 
     * @throws ConfigurationFailureException if a property is missed or has an invalid value.
     */
    public ConfigParameters(final Properties properties)
    {
        storePath = new File(PropertyUtils.getMandatoryProperty(properties, STOREROOT_DIR_KEY));
        port = getMandatoryIntegerProperty(properties, PORT_KEY);
        serverURL = PropertyUtils.getMandatoryProperty(properties, SERVER_URL_KEY);
        sessionTimeout = getMandatoryIntegerProperty(properties, SESSION_TIMEOUT_KEY) * 60;
        useSSL = PropertyUtils.getBoolean(properties, USE_SSL, true);
        if (useSSL == true)
        {
            keystorePath = PropertyUtils.getMandatoryProperty(properties, KEYSTORE_PATH_KEY);
            keystorePassword =
                    PropertyUtils.getMandatoryProperty(properties, KEYSTORE_PASSWORD_KEY);
            keystoreKeyPassword =
                    PropertyUtils.getMandatoryProperty(properties, KEYSTORE_KEY_PASSWORD_KEY);
        } else
        {
            keystorePath = keystorePassword = keystoreKeyPassword = null;
        }
        pluginServlets = extractPluginServletsProperties(properties);
    }

    private static List<PluginServlet> extractPluginServletsProperties(Properties properties)
    {
        List<PluginServlet> servlets = new ArrayList<PluginServlet>();
        SectionProperties[] pluginServicesProperties =
                PropertyParametersUtil.extractSectionProperties(properties,
                        PLUGIN_SERVICES_LIST_KEY, false);
        for (SectionProperties sectionProperties : pluginServicesProperties)
        {
            Properties servletProps = sectionProperties.getProperties();
            String servletClass =
                    PropertyUtils.getMandatoryProperty(servletProps, PLUGIN_SERVICE_CLASS_KEY);
            String servletPath =
                    PropertyUtils.getMandatoryProperty(servletProps, PLUGIN_SERVICE_PATH_KEY);
            servlets.add(new PluginServlet(servletClass, servletPath, servletProps));
        }
        return servlets;
    }

    private final static int getMandatoryIntegerProperty(final Properties properties,
            final String key)
    {
        final String property = PropertyUtils.getMandatoryProperty(properties, key);
        try
        {
            return Integer.parseInt(property);
        } catch (final NumberFormatException ex)
        {
            throw new ConfigurationFailureException("Configuration parameter '" + key
                    + "' is not an integer number: " + property);
        }
    }

    public final File getStorePath()
    {
        return storePath;
    }

    public final int getPort()
    {
        return port;
    }

    public final String getServerURL()
    {
        return serverURL;
    }

    public final int getSessionTimeout()
    {
        return sessionTimeout;
    }

    public final String getKeystorePath()
    {
        return keystorePath;
    }

    public final String getKeystorePassword()
    {
        return keystorePassword;
    }

    public final String getKeystoreKeyPassword()
    {
        return keystoreKeyPassword;
    }

    public final List<PluginServlet> getPluginServlets()
    {
        return pluginServlets;
    }

    public boolean isUseSSL()
    {
        return useSSL;
    }

    public final void log()
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Store root directory: '%s'.", storePath));
            operationLog.info(String.format("Port number: %d.", port));
            operationLog.info(String.format("URL of openBIS server: '%s'.", serverURL));
            operationLog.info(String.format("Session timeout (seconds): %d.", sessionTimeout));
            operationLog.info(String.format("Use SSL: '%s'.", useSSL));
            operationLog.info(String.format("Keystore path: '%s'.", keystorePath));
            for (PluginServlet pluginServlet : pluginServlets)
            {
                operationLog.info("Plugin servlet: " + pluginServlet);
            }
        }
    }
}
