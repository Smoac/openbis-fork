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

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;

/**
 * Action which sets the check boxes on the technology page.
 *
 * @author Franz-Josef Elmer
 */
public class SetTechnologyCheckBoxesAction implements PanelAction
{

    public void executeAction(AutomatedInstallData data, AbstractUIHandler handler)
    {
        for (String technology : GlobalInstallationContext.TECHNOLOGIES)
        {
            String variable = data.getVariable(technology);
            if (variable == null)
            {
                data.setVariable(technology, "true");
            }
        }
    }

    public void initialize(PanelActionConfiguration configuration)
    {
    }

}
