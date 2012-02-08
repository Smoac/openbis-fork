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

import static ch.systemsx.cisd.openbis.installer.izpack.SetTechnologyCheckBoxesAction.DISABLED_TECHNOLOGIES_KEY;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;

import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * Action which sets the variable <code>DISABLED_TECHNOLOGIES_VARNAME</code> or updates
 * service.properties of AS.
 * 
 * @author Franz-Josef Elmer
 */
public class SetDisableTechnologiesVariableAction implements PanelAction
{
    static final String DISABLED_TECHNOLOGIES_VARNAME = "DISABLED_TECHNOLOGIES";

    public void initialize(PanelActionConfiguration configuration)
    {
    }

    public void executeAction(AutomatedInstallData data, AbstractUIHandler handler)
    {
        boolean isFirstTimeInstallation = GlobalInstallationContext.isFirstTimeInstallation;
        File installDir = GlobalInstallationContext.installDir;
        updateDisabledTechnologyProperty(data, isFirstTimeInstallation, installDir);
    }

    void updateDisabledTechnologyProperty(AutomatedInstallData data,
            boolean isFirstTimeInstallation, File installDir)
    {
        String newTechnologyList = createListOfDisabledTechnologies(data);
        if (isFirstTimeInstallation)
        {
            data.setVariable(DISABLED_TECHNOLOGIES_VARNAME, newTechnologyList);
        } else
        {
            File configFile = new File(installDir, Utils.AS_PATH + Utils.SERVICE_PROPERTIES_PATH);
            List<String> list = FileUtilities.loadToStringList(configFile);
            boolean defined = false;
            boolean unchanged = false;
            String propertiesEntry = DISABLED_TECHNOLOGIES_KEY + " = " + newTechnologyList;
            for (int i = 0; i < list.size(); i++)
            {
                String line = list.get(i);
                if (line.startsWith(DISABLED_TECHNOLOGIES_KEY))
                {
                    defined = true;
                    String currentTechnologyList =
                            line.substring(DISABLED_TECHNOLOGIES_KEY.length()).trim();
                    if (currentTechnologyList.startsWith("="))
                    {
                        currentTechnologyList = currentTechnologyList.substring(1).trim();
                    }
                    unchanged = currentTechnologyList.equals(newTechnologyList);
                    if (unchanged == false)
                    {
                        list.set(i, propertiesEntry);
                    }
                    break;
                }
            }
            if (defined)
            {
                if (unchanged == false)
                {
                    updateConfigFile(configFile, list);
                }
            } else
            {
                appendEntryToConfigFile(configFile, propertiesEntry);
            }
        }
    }

    private String createListOfDisabledTechnologies(AutomatedInstallData data)
    {
        StringBuilder builder = new StringBuilder();
        for (String technology : GlobalInstallationContext.TECHNOLOGIES)
        {
            String technologyFlag = data.getVariable(technology);
            if (Boolean.FALSE.toString().equalsIgnoreCase(technologyFlag))
            {
                if (builder.length() > 0)
                {
                    builder.append(", ");
                }
                builder.append(technology.toLowerCase());
            }
        }
        return builder.toString();
    }

    private void updateConfigFile(File configFile, List<String> list)
    {
        PrintWriter printWriter = null;
        try
        {
            printWriter = new PrintWriter(configFile);
            for (String line : list)
            {
                printWriter.println(line);
            }
        } catch (IOException ex)
        {
            throw new RuntimeException("Couldn't update " + configFile, ex);
        } finally
        {
            IOUtils.closeQuietly(printWriter);
        }
    }

    private void appendEntryToConfigFile(File configFile, String propertiesEntry)
    {
        FileWriter fileWriter = null;
        try
        {
            fileWriter = new FileWriter(configFile, true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println();
            printWriter.println(propertiesEntry);
        } catch (IOException ex)
        {
            throw new RuntimeException("Couldn't append property " + DISABLED_TECHNOLOGIES_KEY
                    + " to " + configFile, ex);
        } finally
        {
            IOUtils.closeQuietly(fileWriter);
        }
    }

}
