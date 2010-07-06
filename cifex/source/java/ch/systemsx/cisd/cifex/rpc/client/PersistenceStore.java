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

package ch.systemsx.cisd.cifex.rpc.client;

import java.io.File;
import java.util.Properties;

import org.apache.commons.lang.SystemUtils;

import ch.systemsx.cisd.common.utilities.PropertyUtils;

/**
 * Methods for persistent entries on the client side.
 * 
 * @author Bernd Rinn
 */
public class PersistenceStore
{
    private final static String USER_HOME_PATH = SystemUtils.getUserHome().getPath();

    private final static String WORKING_DIRECTORY_KEY = "working-directory";

    private final static String DELETE_ENCRYPTED_FILES_KEY = "delete-encrypted-files";

    private final static Properties userProperties = getProperties();

    private PersistenceStore()
    {
        // Not to be initialized
    }

    public static Properties getProperties()
    {
        if (ClientConfigurationFiles.USER_PROPERTIES_FILE.exists())
        {
            try
            {
                return PropertyUtils.loadProperties(ClientConfigurationFiles.USER_PROPERTIES_FILE);
            } catch (Exception ex)
            {
                ex.printStackTrace();
                return createDefaultProperties();
            }
        } else
        {
            return createDefaultProperties();
        }
    }

    private static Properties createDefaultProperties()
    {
        final Properties result = new Properties();
        enrichPropertiesWithDefaults(result);
        return result;
    }

    private static void enrichPropertiesWithDefaults(final Properties properties)
    {
        if (properties.getProperty(WORKING_DIRECTORY_KEY) == null)
        {
            properties.setProperty(WORKING_DIRECTORY_KEY, USER_HOME_PATH);
        }
        if (properties.getProperty(DELETE_ENCRYPTED_FILES_KEY) == null)
        {
            properties.setProperty(DELETE_ENCRYPTED_FILES_KEY, Boolean.TRUE.toString());
        }
    }

    /**
     * Returns the working directory for file upload / download operations.
     */
    public static File getWorkingDirectory()
    {
        return new File(userProperties.getProperty(WORKING_DIRECTORY_KEY));
    }

    /**
     * Sets the working directory for file upload / download operations.
     */
    public static void setWorkingDirectory(File workingDirectory)
    {
        userProperties.setProperty(WORKING_DIRECTORY_KEY, workingDirectory.getAbsolutePath());
    }

    /**
     * Returns whether encrypted files should be deleted after successful upload / decryption.
     */
    public static boolean isDeleteEncryptedFiles()
    {
        return "true".equalsIgnoreCase(userProperties.getProperty(DELETE_ENCRYPTED_FILES_KEY));
    }

    /**
     * Sets whether encrypted files should be deleted after successful upload / decryption.
     */
    public static void setDeleteEncryptedFiles(boolean deleteEncryptedFiles)
    {
        userProperties.setProperty(DELETE_ENCRYPTED_FILES_KEY, Boolean
                .toString(deleteEncryptedFiles));
    }

    /**
     * Saves the current state of the properties into the properties file.
     */
    public static void saveProperties()
    {
        try
        {
            PropertyUtils.saveProperties(ClientConfigurationFiles.USER_PROPERTIES_FILE,
                    userProperties);
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
