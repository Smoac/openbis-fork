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

import static ch.systemsx.cisd.openbis.installer.izpack.SetTechnologyCheckBoxesAction.ENABLED_TECHNOLOGIES_KEY;

import java.io.File;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;

import ch.systemsx.cisd.common.shared.basic.utils.CommaSeparatedListBuilder;

/**
 * Action which sets the variable <code>ENABLED_TECHNOLOGIES_VARNAME</code> or updates
 * service.properties of AS.
 * 
 * @author Franz-Josef Elmer
 */
public class SetEnableTechnologiesVariableAction implements PanelAction
{
    static final String ENABLED_TECHNOLOGIES_VARNAME = "ENABLED_TECHNOLOGIES";

    private final SetTechnologyCheckBoxesAction technologyCheckBoxesAction = new SetTechnologyCheckBoxesAction();
    
    @Override
    public void initialize(PanelActionConfiguration configuration)
    {
    }

    @Override
    public void executeAction(AutomatedInstallData data, AbstractUIHandler handler)
    {
        // The technologyCheckBoxesAction is a 'preactivate' action to populate the technology check
        // boxes.
        // But in case of console installation the 'preactivate' action isn't executed. We can
        // execute it here without any check whether this is a console-based or GUI-based
        // installation
        // because the GUI sets the technology flags and nothing will be populated from existing
        // service.properties.
        technologyCheckBoxesAction.executeAction(data, handler);
        boolean isFirstTimeInstallation = GlobalInstallationContext.isFirstTimeInstallation;
        File installDir = GlobalInstallationContext.installDir;
        updateEnabledTechnologyProperty(data, isFirstTimeInstallation, installDir);
    }

    void updateEnabledTechnologyProperty(AutomatedInstallData data,
            boolean isFirstTimeInstallation, File installDir)
    {
        String newTechnologyList = createListOfEnabledTechnologies(data);
        data.setVariable(ENABLED_TECHNOLOGIES_VARNAME, newTechnologyList);
        if (isFirstTimeInstallation == false)
        {
            File asConfigFile = new File(installDir, Utils.AS_PATH + Utils.SERVICE_PROPERTIES_PATH);
            Utils.updateOrAppendProperty(asConfigFile, ENABLED_TECHNOLOGIES_KEY, newTechnologyList);
            File dssConfigFile =
                    new File(installDir, Utils.DSS_PATH + Utils.SERVICE_PROPERTIES_PATH);
            Utils.updateOrAppendProperty(dssConfigFile, ENABLED_TECHNOLOGIES_KEY, newTechnologyList);
        }
    }

    private String createListOfEnabledTechnologies(AutomatedInstallData data)
    {
        CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
        for (String technology : GlobalInstallationContext.TECHNOLOGIES)
        {
            String technologyFlag = data.getVariable(technology);
            if (Boolean.TRUE.toString().equalsIgnoreCase(technologyFlag))
            {
                builder.append(technology.toLowerCase());
            }
        }
        return builder.toString();
    }
    
}
