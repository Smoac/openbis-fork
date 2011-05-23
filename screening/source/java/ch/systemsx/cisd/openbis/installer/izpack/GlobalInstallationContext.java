/*
 * Copyright 2011 ETH Zuerich, CISD
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
import java.net.InetAddress;

import com.izforge.izpack.installer.AutomatedInstallData;

import ch.systemsx.cisd.common.utilities.PasswordGenerator;

/**
 * @author Kaloyan Enimanev
 */
public class GlobalInstallationContext
{
    public static final String POSTGRES_BIN_VARNAME = "POSTGRES_BIN";

    public static final String ADMIN_PASSWORD_VARNAME = "ADMIN_PASSWORD";

    public static final String ETL_SERVER_PASSWORD_VARNAME = "ETLSERVER_PASSWORD";

    public static final String HOSTNAME_VARNAME = "HOSTNAME";
    /**
     * set to true if the installation process is trying to update an existing openBIS installation.
     */
    public static boolean isUpdateInstallation = false;

    /**
     * set to true if this is the first openBIS installation on the machine.
     */
    public static boolean isFirstTimeInstallation = true;

    /**
     * this variable is set to true if 'psql' or 'pg_dump' are not on path.
     * <p>
     * used by the installation xml config, not meant to be used by Java code.
     */
    public static boolean noPsqlToolsOnPath = (false == PostgresInstallationDetectorUtils
            .areCommandLineToolsOnPath());

    public static File installDir;

    public static void initialize(AutomatedInstallData data)
    {
        String installPath = data.getInstallPath();
        installDir = new File(installPath);
        isFirstTimeInstallation = (installDir.exists() == false);
        isUpdateInstallation = installDir.exists();

        data.setVariable(POSTGRES_BIN_VARNAME, "");

        if (isFirstTimeInstallation)
        {
            populateFirstTimeInstallVariables(data);
        }
    }

    /**
     * populates variables need for fist time installations.
     */
    private static void populateFirstTimeInstallVariables(AutomatedInstallData data)
    {
        data.setVariable(ETL_SERVER_PASSWORD_VARNAME,
                new PasswordGenerator(true).generatePassword());
        data.setVariable(HOSTNAME_VARNAME, getHostName());
    }

    /**
     * Return the cannonical host name for the localhost machine.
     */
    private static String getHostName()
    {
        try
        {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (Exception ex)
        {
            return "localhost";
        }
    }
}
