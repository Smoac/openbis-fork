/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.rpc.client.gui;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.common.utilities.PropertyUtils;

/**
 * Methods for persistent entries on the client side.
 * 
 * @author Bernd Rinn
 */
public class PersistenceStore
{

    private final static File HOME_DIRECTORY = new File(System.getProperty("user.home"));

    private final static String CIFEX_PROPERTIES_FILENAME = ".cifex.properties";

    private final static File CIFEX_PROPERTIES_FILE =
            new File(HOME_DIRECTORY, CIFEX_PROPERTIES_FILENAME);

    private final static String WORKING_DIRECTORY_KEY = "working-directory";

    private PersistenceStore()
    {
        // Not to be initialized
    }

    public static Properties getProperties()
    {
        if (CIFEX_PROPERTIES_FILE.exists())
        {
            try
            {
                return PropertyUtils.loadProperties(CIFEX_PROPERTIES_FILE);
            } catch (Exception ex)
            {
                ex.printStackTrace();
                return new Properties();
            }
        } else
        {
            return new Properties();
        }
    }

    public static File getWorkingDirectory()
    {
        return new File(getProperties()
                .getProperty(WORKING_DIRECTORY_KEY, HOME_DIRECTORY.getPath()));
    }
    
    public static void setWorkingDirectory(File workingDirectory)
    {
        final Properties props = getProperties();
        props.setProperty(WORKING_DIRECTORY_KEY, workingDirectory.getAbsolutePath());
        try
        {
            PropertyUtils.saveProperties(CIFEX_PROPERTIES_FILE, props);
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
