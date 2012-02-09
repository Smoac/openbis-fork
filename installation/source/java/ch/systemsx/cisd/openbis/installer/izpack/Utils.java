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

package ch.systemsx.cisd.openbis.installer.izpack;

import java.io.File;
import java.io.FileReader;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

/**
 * Utility functions for exploring <code>service.properties</code> files of an installation.
 *
 * @author Franz-Josef Elmer
 */
class Utils
{
    private static final String SERVERS_PATH = "servers/";
    static final String AS_PATH = SERVERS_PATH + "openBIS-server/jetty/";
    static final String DSS_PATH = SERVERS_PATH + "datastore_server/";
    static final String SERVICE_PROPERTIES_PATH = "etc/service.properties";

    static String tryToGetServicePropertyOfAS(File installDir, String propertyKey)
    {
        Properties serviceProperties = tryToGetServicePropertiesOfAS(installDir);
        return serviceProperties == null ? null : serviceProperties.getProperty(propertyKey);
    }
    
    static boolean dssPropertiesContains(File installDir, String string)
    {
        Properties properties =
                tryToGetServiceProperties(installDir, DSS_PATH + SERVICE_PROPERTIES_PATH);
        if (properties == null)
        {
            return false;
        }
        for (Entry<Object, Object> entry : properties.entrySet())
        {
            String key = entry.getKey().toString();
            if (key.toLowerCase().contains(string))
            {
                return true;
            }
            String value = entry.getValue().toString();
            if (value.toLowerCase().contains(string))
            {
                return true;
            }
        }
        return false;
    }
    
    private static Properties tryToGetServicePropertiesOfAS(File installDir)
    {
        return tryToGetServiceProperties(installDir, AS_PATH + SERVICE_PROPERTIES_PATH);
    }
    
    private static Properties tryToGetServiceProperties(File installDir, String relativePath)
    {
        Properties properties = new Properties();
        FileReader fileReader = null;
        try
        {
            fileReader = new FileReader(new File(installDir, relativePath));
            properties.load(fileReader);
            return properties;
        } catch (Exception ex)
        {
            return null;
        } finally
        {
            IOUtils.closeQuietly(fileReader);
        }
    }
}
