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

import org.apache.commons.lang.SystemUtils;

/**
 * A store for client-side configuration files.
 * 
 * @author Bernd Rinn
 */
public class ClientConfigurationFiles
{
    /** The directory for CIFEX configuration data (per host). */
    public final static File USER_CONFIG_DIRECTORY = getCIFEXUserConfigDir();

    /** The directory for CIFEX configuration data (per user). */
    public final static File HOST_CONFIG_DIRECTORY = getCIFEXHostConfigDir();

    private final static String USER_PROPERTIES_FILENAME = "properties";

    /** The file containing the user properties. */
    public final static File USER_PROPERTIES_FILE =
            new File(USER_CONFIG_DIRECTORY, USER_PROPERTIES_FILENAME);

    private static final String SESSION_TOKEN_FILE_NAME = "session-token";

    /** The file containing the current session token. */
    public static final File SESSION_TOKEN_FILE =
            new File(USER_CONFIG_DIRECTORY, SESSION_TOKEN_FILE_NAME);
    
    private static final String BASE_URL_FILE_NAME = "server";

    /** The file containing the base URL of the server. */
    public static final File BASE_URL_FILE = new File(HOST_CONFIG_DIRECTORY, BASE_URL_FILE_NAME);
    
    private static final String KEYSTORE_FILE_NAME = "keystore";

    /** The file of the (private) java keystore. */
    public static final File KEYSTORE_FILE = new File(HOST_CONFIG_DIRECTORY, KEYSTORE_FILE_NAME);

    private static File getCIFEXHostConfigDir()
    {
        final File cifexInstallationConfigDir = tryGetCIFEXInstallationConfigDirPath();
        if (cifexInstallationConfigDir != null)
        {
            return cifexInstallationConfigDir;
        } else
        {
            return getCIFEXUserConfigDir();
        }
    }

    private static File getCIFEXUserConfigDir()
    {
        final File cifexUserConfigDir = new File(SystemUtils.getUserHome(), ".cifex");
        cifexUserConfigDir.mkdirs();
        return cifexUserConfigDir;
    }

    private static File tryGetCIFEXInstallationConfigDirPath()
    {
        final String cifexInstallationConfigDirPath = System.getProperty("cifex.config");
        if (cifexInstallationConfigDirPath != null)
        {
            final File cifexInstallationConfigDir = new File(cifexInstallationConfigDirPath);
            cifexInstallationConfigDir.mkdirs();
            return cifexInstallationConfigDir;
        } else
        {
            return null;
        }
    }

    private ClientConfigurationFiles()
    {
        // Not to be instantiated.
    }

}
