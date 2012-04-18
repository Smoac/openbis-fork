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

import static ch.systemsx.cisd.openbis.installer.izpack.SetTechnologyCheckBoxesAction.DISABLED_TECHNOLOGIES_KEY;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.common.filesystem.FileUtilities;

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
    static final String JETTY_XML_PATH = "etc/jetty.xml";
    static final String KEYSTORE_PATH = "etc/openBIS.keystore";

    static String tryToGetServicePropertyOfAS(File installDir, String propertyKey)
    {
        Properties serviceProperties = tryToGetServicePropertiesOfAS(installDir);
        return serviceProperties == null ? null : serviceProperties.getProperty(propertyKey);
    }
    
    static String tryToGetServicePropertyOfDSS(File installDir, String propertyKey)
    {
        Properties serviceProperties = tryToGetServicePropertiesOfDSS(installDir);
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
    
    private static Properties tryToGetServicePropertiesOfDSS(File installDir)
    {
        return tryToGetServiceProperties(installDir, DSS_PATH + SERVICE_PROPERTIES_PATH);
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

    static void updateConfigFile(File configFile, List<String> list)
    {
        PrintWriter printWriter = null;
        try
        {
            printWriter = new PrintWriter(configFile);
            for (String line : list)
            {
                printWriter.println(line);
            }
        } catch (IOException ex)
        {
            throw new RuntimeException("Couldn't update " + configFile, ex);
        } finally
        {
            IOUtils.closeQuietly(printWriter);
        }
    }

    static void appendEntryToConfigFile(File configFile, String propertiesEntry)
    {
        FileWriter fileWriter = null;
        try
        {
            fileWriter = new FileWriter(configFile, true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println();
            printWriter.println(propertiesEntry);
        } catch (IOException ex)
        {
            throw new RuntimeException("Couldn't append property " + DISABLED_TECHNOLOGIES_KEY
                    + " to " + configFile, ex);
        } finally
        {
            IOUtils.closeQuietly(fileWriter);
        }
    }

    static void updateOrAppendProperty(File configFile, String propertyKey, String propertyValue)
    {
        List<String> list = FileUtilities.loadToStringList(configFile);
        boolean defined = false;
        boolean unchanged = false;
        String propertiesEntry = propertyKey + " = " + propertyValue;
        for (int i = 0; i < list.size(); i++)
        {
            String line = list.get(i);
            if (line.startsWith(propertyKey))
            {
                defined = true;
                String currentPropertyValue =
                        line.substring(propertyKey.length()).trim();
                if (currentPropertyValue.startsWith("="))
                {
                    currentPropertyValue = currentPropertyValue.substring(1).trim();
                }
                unchanged = currentPropertyValue.equals(propertyValue);
                if (unchanged == false)
                {
                    list.set(i, propertiesEntry);
                }
                break;
            }
        }
        if (defined)
        {
            if (unchanged == false)
            {
                updateConfigFile(configFile, list);
            }
        } else
        {
            appendEntryToConfigFile(configFile, propertiesEntry);
        }
    }

    static final String DSS_KEYSTORE_KEY_PASSWORD_KEY = "keystore.key-password";
    static final String DSS_KEYSTORE_PASSWORD_KEY = "keystore.password";
}
