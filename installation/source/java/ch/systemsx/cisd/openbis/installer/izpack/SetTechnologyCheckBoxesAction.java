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

import static ch.systemsx.cisd.openbis.installer.izpack.GlobalInstallationContext.TECHNOLOGY_PROTEOMICS;
import static ch.systemsx.cisd.openbis.installer.izpack.GlobalInstallationContext.TECHNOLOGY_SCREENING;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;

/**
 * Action which sets the variables which are the values of check boxes on the technology page. If
 * the variable is already set nothing is done. Otherwise the behavior depends on whether this is
 * installation or upgrading. In case of installation the flag will be <code>true</code>. In case of
 * upgrading <code>service.properties</code> files are scanned in order to check whether a certain
 * technology is enabled or not.
 * 
 * @author Franz-Josef Elmer
 */
public class SetTechnologyCheckBoxesAction implements PanelAction
{
    static final String DISABLED_TECHNOLOGIES_KEY = "disabled-technologies";
    
    private static interface ITechnologyChecker
    {
        String getTechnologyName();
        boolean isTechnologyEnabled(File installDir);
    }
    
    private static final class SimpleTechnologyChecker implements ITechnologyChecker
    {

        private final String technologyName;

        private final String dssPropertiesSignature;

        SimpleTechnologyChecker(String technologyName, String dssPropertiesSignature)
        {
            this.technologyName = technologyName;
            this.dssPropertiesSignature = dssPropertiesSignature;
        }

        public String getTechnologyName()
        {
            return technologyName;
        }

        public boolean isTechnologyEnabled(File installDir)
        {
            String technologies =
                    Utils.tryToGetServicePropertyOfAS(installDir, DISABLED_TECHNOLOGIES_KEY);
            if (technologies != null)
            {
                return technologies.contains(technologyName.toLowerCase()) == false;
            }
            return Utils.dssPropertiesContains(installDir, dssPropertiesSignature);
        }
    }
    
    private final Map<String, ITechnologyChecker> technologyCheckers =
            new HashMap<String, ITechnologyChecker>();
    
    public SetTechnologyCheckBoxesAction()
    {
        registerTechnologyChecker(new SimpleTechnologyChecker(TECHNOLOGY_PROTEOMICS, "proteomics"));
        registerTechnologyChecker(new SimpleTechnologyChecker(TECHNOLOGY_SCREENING, "screen"));
    }
    
    private void registerTechnologyChecker(ITechnologyChecker checker)
    {
        technologyCheckers.put(checker.getTechnologyName(), checker);
    }

    public void initialize(PanelActionConfiguration configuration)
    {
    }
    
    public void executeAction(AutomatedInstallData data, AbstractUIHandler handler)
    {
        for (String technology : GlobalInstallationContext.TECHNOLOGIES)
        {
            String variable = data.getVariable(technology);
            if (variable == null)
            {
                boolean technologyFlag = true;
                if (GlobalInstallationContext.isUpdateInstallation)
                {
                    technologyFlag =
                            isTechnologyEnabled(GlobalInstallationContext.installDir, technology);
                }
                data.setVariable(technology, Boolean.toString(technologyFlag));
            }
        }
    }

    boolean isTechnologyEnabled(File installDir, String technology)
    {
        ITechnologyChecker technologyChecker = technologyCheckers.get(technology);
        if (technologyChecker == null)
        {
            return true;
        }
        return technologyChecker.isTechnologyEnabled(installDir);
    }
}
