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
public class ExecuteSetupScriptsAction extends AbstractScriptExecutor implements PanelAction
{
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
        String backupConfigFolder =
                data.getVariable(GlobalInstallationContext.BACKUP_FOLDER_VARNAME)
                        + "/config-backup";
        executeAdminScript(null, script, backupConfigFolder);
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

    public void initialize(PanelActionConfiguration arg0)
    {
    }
    



}
