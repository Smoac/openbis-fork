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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.PanelActionConfiguration;
import com.izforge.izpack.api.handler.AbstractUIHandler;
import com.izforge.izpack.data.PanelAction;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.utilities.CommaSeparatedListBuilder;

/**
 * Action which sets the variable <code>DISABLED_TECHNOLOGIES_VARNAME</code> or updates
 * service.properties of AS.
 * 
 * @author Franz-Josef Elmer
 */
public class SetDisableTechnologiesVariableAction implements PanelAction
{
    static final String DISABLED_TECHNOLOGIES_VARNAME = "DISABLED_TECHNOLOGIES";
    static final String DISABLED_CORE_PLUGINS_KEY = "disabled-core-plugins";

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
        data.setVariable(DISABLED_TECHNOLOGIES_VARNAME, newTechnologyList);
        if (isFirstTimeInstallation == false)
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
        updateDisabledDssPluginsProperty(data, installDir);
    }
    
    private void updateDisabledDssPluginsProperty(AutomatedInstallData data, File installDir)
    {
        Set<String> disabledTechnologies = new LinkedHashSet<String>();
        Set<String> technologies = new HashSet<String>();
        for (String technology : GlobalInstallationContext.TECHNOLOGIES)
        {
            String technologyFlag = data.getVariable(technology);
            if (Boolean.FALSE.toString().equalsIgnoreCase(technologyFlag))
            {
                disabledTechnologies.add(technology.toLowerCase());
            }
            technologies.add(technology.toLowerCase());
        }
        File configFile = new File(installDir, Utils.DSS_PATH + Utils.SERVICE_PROPERTIES_PATH);
        List<String> list = FileUtilities.loadToStringList(configFile);
        updateDisabledDssPluginsProperty(list, technologies, disabledTechnologies);
        updateConfigFile(configFile, list);
    }

    private void updateDisabledDssPluginsProperty(List<String> list, Set<String> technologies,
            Set<String> disabledTechnologies)
    {
        for (int i = 0; i < list.size(); i++)
        {
            String line = list.get(i);
            if (line.startsWith(DISABLED_CORE_PLUGINS_KEY))
            {
                String[] property = line.split("=");
                String oldPluginsList = property.length < 2 ? "" : property[1];
                String pluginsList =
                        mergeWithDisabledPluginsList(oldPluginsList, technologies,
                                disabledTechnologies);
                list.set(i, DISABLED_CORE_PLUGINS_KEY + " = " + pluginsList);
                return;
            }
        }
        String pluginsList = mergeWithDisabledPluginsList("", technologies, disabledTechnologies);
        list.add(DISABLED_CORE_PLUGINS_KEY + " = " + pluginsList);
    }
    
    private String mergeWithDisabledPluginsList(String disabledPlugins, Set<String> technologies,
            Set<String> disabledTechnologies)
    {
        CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
        Set<String> plugins = new HashSet<String>();
        String[] terms = disabledPlugins.split(",");
        for (String term : terms)
        {
            String plugin = term.trim();
            if (plugin.length() > 0)
            {
                if (technologies.contains(plugin) == false || disabledTechnologies.contains(plugin))
                {
                    builder.append(plugin);
                    plugins.add(plugin);
                }
            }
        }
        for (String disabledTechnology : disabledTechnologies)
        {
            if (plugins.contains(disabledTechnology) == false)
            {
                builder.append(disabledTechnology);
            }
        }
        return builder.toString();
    }

    private String createListOfDisabledTechnologies(AutomatedInstallData data)
    {
        CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
        for (String technology : GlobalInstallationContext.TECHNOLOGIES)
        {
            String technologyFlag = data.getVariable(technology);
            if (Boolean.FALSE.toString().equalsIgnoreCase(technologyFlag))
            {
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
