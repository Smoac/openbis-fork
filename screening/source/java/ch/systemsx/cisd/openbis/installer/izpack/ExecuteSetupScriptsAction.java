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

import static ch.systemsx.cisd.openbis.installer.izpack.GlobalInstallationContext.ADMIN_PASSWORD_VARNAME;
import static ch.systemsx.cisd.openbis.installer.izpack.GlobalInstallationContext.ETL_SERVER_PASSWORD_VARNAME;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;

/**
 * Executes a script that configures the installation.
 * 
 * @author Kaloyan Enimanev
 */
public class ExecuteSetupScriptsAction implements PanelAction
{
    private static final String INSTALL_BIN_PATH_VARNAME = "INSTALL_BIN_PATH";

    /**
     * executed for first time installations.
     */
    private static final String POST_INSTALLATION_SCRIPT = "post-installation.sh";

    /**
     * executed for upgrade installations to restore backed up the configuration files.
     */
    private static final String RESTORE_CONFIG_FROM_BACKUP_SCRIPT = "restore-config-from-backup.sh";

    public synchronized void executeAction(AutomatedInstallData data, AbstractUIHandler arg1)
    {
        if (GlobalInstallationContext.isFirstTimeInstallation)
        {
            executePostInstallationScript(data);
        } else
        {
            executRestoreConfigScript(data);
        }
    }

    private void executRestoreConfigScript(AutomatedInstallData data)
    {
        String script = getAdminScript(data, RESTORE_CONFIG_FROM_BACKUP_SCRIPT);
        executeAdminScript(null, script, data.getVariable("BACKUP_FOLDER"));
    }

    private void executePostInstallationScript(AutomatedInstallData data)
    {
        String script = getAdminScript(data, POST_INSTALLATION_SCRIPT);
        Map<String, String> customEnvironment = new HashMap<String, String>();
        customEnvironment.put(ADMIN_PASSWORD_VARNAME, data.getVariable(ADMIN_PASSWORD_VARNAME));
        customEnvironment.put(ETL_SERVER_PASSWORD_VARNAME,
                data.getVariable(ETL_SERVER_PASSWORD_VARNAME));
        executeAdminScript(customEnvironment, script);
    }

    private String getAdminScript(AutomatedInstallData data, String scriptFileName)
    {
        File adminScriptFile = new File(data.getVariable(INSTALL_BIN_PATH_VARNAME), scriptFileName);
        return adminScriptFile.getAbsolutePath();
    }

    private void executeAdminScript(Map<String, String> customEnv, String... command)
    {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.environment().putAll(System.getenv());
        if (customEnv != null)
        {
            pb.environment().putAll(customEnv);
        }
        try
        {
            Process process = pb.start();
            pipe(process.getErrorStream(), System.err);
            pipe(process.getInputStream(), System.out);
            process.waitFor();
        } catch (Exception e)
        {
            System.out.println("Error executing " + command[0] + ": " + e.getMessage());
        }
    }

    public void initialize(PanelActionConfiguration arg0)
    {
    }
    

    private static void pipe(final InputStream src, final PrintStream dest) {
        new Thread(new Runnable() 
        {
            public void run() 
            {
                try 
                {
                    byte[] buffer = new byte[1024];
                    for (int n = 0; n != -1; n = src.read(buffer)) 
                    {
                        dest.write(buffer, 0, n);
                    }
                } catch (IOException e) 
                { 
                }
            }
            }).start();
    }


}
