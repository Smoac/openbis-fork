/*
 * Copyright 2013 ETH Zuerich, CISD
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
 * Action which sets the boolean variable PATHINFO_DB_ENABLED
 * 
 * @author Franz-Josef Elmer
 */
public class SetPathinfoDBCheckBoxAction implements PanelAction
{
    @Override
    public void initialize(PanelActionConfiguration configuration)
    {
    }

    @Override
    public void executeAction(AutomatedInstallData data, AbstractUIHandler handler)
    {
        boolean pathinfoDBEnabled = true;
        String dataSources =
                Utils.tryToGetServicePropertyOfDSS(GlobalInstallationContext.installDir,
                        ExecuteSetupScriptsAction.DATA_SOURCES_KEY);
        if (dataSources != null)
        {
            pathinfoDBEnabled =
                    dataSources.contains(ExecuteSetupScriptsAction.PATHINFO_DB_DATA_SOURCE);
        }
        data.setVariable(GlobalInstallationContext.PATHINFO_DB_ENABLED,
                Boolean.toString(pathinfoDBEnabled));
    }

}
