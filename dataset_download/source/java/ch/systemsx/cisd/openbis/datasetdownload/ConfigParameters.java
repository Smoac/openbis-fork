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

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class ConfigParameters
{
    static final String PASSWORD_KEY = "password";
    static final String USERNAME_KEY = "username";
    static final String SERVER_URL_KEY = "server-url";
    static final String PORT_KEY = "port";
    static final String STOREROOT_DIR_KEY = "storeroot-dir";
    private static final String KEYSTORE = "keystore.";
    static final String KEYSTORE_PATH_KEY = KEYSTORE + "path";
    static final String KEYSTORE_PASSWORD_KEY = KEYSTORE + "password";
    static final String KEYSTORE_KEY_PASSWORD_KEY = KEYSTORE + "key-password";

    private final String storePath;
    
    private final int port;
    
    private final String serverURL;
    
    private final String userName;
    
    private final String password;
    
    private final String keystorePath;
    
    private final String keystorePassword;
    
    private final String keystoreKeyPassword;
    
    public ConfigParameters(Properties properties)
    {
        storePath = properties.getProperty(STOREROOT_DIR_KEY);
        port = Integer.parseInt(properties.getProperty(PORT_KEY));
        serverURL = properties.getProperty(SERVER_URL_KEY);
        userName = properties.getProperty(USERNAME_KEY);
        password = properties.getProperty(PASSWORD_KEY);
        keystorePath = properties.getProperty(KEYSTORE_PATH_KEY);
        keystorePassword = properties.getProperty(KEYSTORE_PASSWORD_KEY);
        keystoreKeyPassword = properties.getProperty(KEYSTORE_KEY_PASSWORD_KEY);
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

    public final String getUserName()
    {
        return userName;
    }

    public final String getPassword()
    {
        return password;
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
