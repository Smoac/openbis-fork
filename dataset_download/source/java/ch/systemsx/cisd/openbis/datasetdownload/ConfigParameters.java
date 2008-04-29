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
    private final String storePath;
    
    private final int port;
    
    private final String serverURL;
    
    private final String userName;
    
    private final String password;
    
    public ConfigParameters(Properties properties)
    {
        storePath = properties.getProperty("storeroot-dir");
        port = Integer.parseInt(properties.getProperty("port"));
        serverURL = properties.getProperty("server-url");
        userName = properties.getProperty("username");
        password = properties.getProperty("password");
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
    
    
}
