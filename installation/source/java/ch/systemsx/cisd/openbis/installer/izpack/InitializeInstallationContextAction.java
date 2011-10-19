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

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;

/**
 * An action that initializes the installation process.
 *
 * @author Kaloyan Enimanev
 */
public class InitializeInstallationContextAction implements PanelAction
{
    private static final String ROOT_USERNAME = "root";

    public void executeAction(AutomatedInstallData data, AbstractUIHandler arg1)
    {

        abortIfRunningAsRoot();

        GlobalInstallationContext.initialize(data);

        if (GlobalInstallationContext.isUpdateInstallation)
        {
            new PrepareInstallationBackupAction().executeAction(data, arg1);
        }
    }

    private void abortIfRunningAsRoot()
    {
        String userName = System.getProperty("user.name");

        if (ROOT_USERNAME.equalsIgnoreCase(userName))
        {
            System.err.println("The openBIS installation cannot be executed as 'root'. "
                    + "Please switch to a user with lower privilidges.");
            System.exit(1);
        }
    }

    public void initialize(PanelActionConfiguration arg0)
    {
    }

}
