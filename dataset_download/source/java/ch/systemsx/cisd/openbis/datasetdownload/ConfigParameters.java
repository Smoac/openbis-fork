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

package ch.systemsx.cisd.openbis.datasetdownload;

import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;

/**
 * Configuration parameters for the Data Set Download Server.
 *
 * @author Franz-Josef Elmer
 */
class ConfigParameters
{
    static final String SERVER_URL_KEY = "server-url";
    static final String PORT_KEY = "port";
    static final String STOREROOT_DIR_KEY = "storeroot-dir";
    static final String SESSION_TIMEOUT_KEY = "session-timeout";
    private static final String KEYSTORE = "keystore.";
    static final String KEYSTORE_PATH_KEY = KEYSTORE + "path";
    static final String KEYSTORE_PASSWORD_KEY = KEYSTORE + "password";
    static final String KEYSTORE_KEY_PASSWORD_KEY = KEYSTORE + "key-password";

    private final String storePath;
    
    private final int port;
    
    private final String serverURL;
    
    private final int sessionTimeout;
    
    private final String keystorePath;
    
    private final String keystorePassword;
    
    private final String keystoreKeyPassword;
    
    /**
     * Creates an instance based on the specified properties.
     * 
     * @throws ConfigurationFailureException if a property is missed or has an invalid value.
     */
    public ConfigParameters(Properties properties)
    {
        storePath = getProperty(properties, STOREROOT_DIR_KEY);
        port = getIntegerProperty(properties, PORT_KEY);
        serverURL = getProperty(properties, SERVER_URL_KEY);
        sessionTimeout = getIntegerProperty(properties, SESSION_TIMEOUT_KEY) * 60;
        keystorePath = getProperty(properties, KEYSTORE_PATH_KEY);
        keystorePassword = getProperty(properties, KEYSTORE_PASSWORD_KEY);
        keystoreKeyPassword = getProperty(properties, KEYSTORE_KEY_PASSWORD_KEY);
    }
    
    private int getIntegerProperty(Properties properties, String key)
    {
        String property = getProperty(properties, key);
        try
        {
            return Integer.parseInt(property);
        } catch (NumberFormatException ex)
        {
            throw new ConfigurationFailureException("Configuration parameter '" + key
                    + "' is not an integer number: " + property);
        }
    }
    
    private String getProperty(Properties properties, String key)
    {
        String property = properties.getProperty(key);
        if (property == null)
        {
            throw new ConfigurationFailureException("Configuration parameter '" + key
                    + "' not specified.");
        }
        return property;
    }

    public final String getStorePath()
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
    
    
}
