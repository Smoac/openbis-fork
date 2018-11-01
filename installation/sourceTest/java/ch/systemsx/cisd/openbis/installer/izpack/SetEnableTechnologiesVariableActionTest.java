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
import static ch.systemsx.cisd.openbis.installer.izpack.SetEnableTechnologiesVariableAction.ENABLED_TECHNOLOGIES_VARNAME;
import static ch.systemsx.cisd.openbis.installer.izpack.SetTechnologyCheckBoxesAction.ENABLED_TECHNOLOGIES_KEY;

import java.io.File;
import java.util.Properties;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.installer.data.InstallData;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * @author Franz-Josef Elmer
 */
public class SetEnableTechnologiesVariableActionTest extends AbstractFileSystemTestCase
{
    private File corePluginsFolder;

    private File corePluginsProperties;

    @Override
    @BeforeMethod
    public void setUp()
    {
        corePluginsFolder = new File(workingDirectory, Utils.CORE_PLUGINS_PATH);
        corePluginsFolder.mkdirs();
        corePluginsProperties = new File(workingDirectory, Utils.CORE_PLUGINS_PROPERTIES_PATH);
        corePluginsProperties.getParentFile().mkdirs();
    }

    @AfterMethod
    public void tearDown()
    {
        FileUtilities.deleteRecursively(corePluginsFolder);
        corePluginsProperties.delete();
    }

    @Test
    public void testFirstInstallation()
    {
        Properties variables = new Properties();
        variables.setProperty(TECHNOLOGY_PROTEOMICS, "true");
        variables.setProperty(TECHNOLOGY_SCREENING, "false");

        AutomatedInstallData data = updateEnabledTechnologyProperties(variables, true);

        assertEquals("proteomics", data.getVariable(ENABLED_TECHNOLOGIES_VARNAME));
    }

    @Test
    public void testFirstInstallationScreeningOnly()
    {
        Properties variables = new Properties();
        variables.setProperty(TECHNOLOGY_PROTEOMICS, "false");
        variables.setProperty(TECHNOLOGY_SCREENING, "false");

        AutomatedInstallData data = updateEnabledTechnologyProperties(variables, true);

        assertEquals("", data.getVariable(ENABLED_TECHNOLOGIES_VARNAME));
    }

    @Test
    public void testUpdateMissingConfigFile()
    {
        Properties variables = new Properties();
        variables.setProperty(TECHNOLOGY_PROTEOMICS, "false");
        variables.setProperty(TECHNOLOGY_SCREENING, "true");

        updateEnabledTechnologyProperties(variables, false);
        assertEquals("[, enabled-modules = monitoring-support, dropbox-monitor, dataset-uploader, dataset-file-search, screening]",
                FileUtilities.loadToStringList(corePluginsProperties).toString());
    }

    @Test
    public void testUpdateInstallationWithOtherEnabledTechnologiesInAs()
    {
        FileUtilities.writeToFile(corePluginsProperties, "abc = 123\n" + ENABLED_TECHNOLOGIES_KEY
                + "=monitoring-support, dropbox-monitor, dataset-uploader, dataset-file-search, proteomics, my-tech");
        Properties variables = new Properties();
        variables.setProperty(TECHNOLOGY_PROTEOMICS, "true");
        variables.setProperty(TECHNOLOGY_SCREENING, "false");

        updateEnabledTechnologyProperties(variables, false);

        assertEquals("[abc = 123, " + ENABLED_TECHNOLOGIES_KEY + "=monitoring-support, dropbox-monitor, dataset-uploader, "
                + "dataset-file-search, proteomics, my-tech]",
                FileUtilities.loadToStringList(corePluginsProperties).toString());
    }

    @Test
    public void testUpdateUnchangedProperty()
    {
        FileUtilities.writeToFile(corePluginsProperties, "abc = 123\n" + ENABLED_TECHNOLOGIES_KEY
                + "=monitoring-support, dropbox-monitor, dataset-uploader, dataset-file-search, proteomics");
        Properties variables = new Properties();
        variables.setProperty(TECHNOLOGY_PROTEOMICS, "true");
        variables.setProperty(TECHNOLOGY_SCREENING, "false");

        updateEnabledTechnologyProperties(variables, false);

        assertEquals("[abc = 123, " + ENABLED_TECHNOLOGIES_KEY + "=monitoring-support, dropbox-monitor, dataset-uploader, "
                + "dataset-file-search, proteomics]", FileUtilities
                .loadToStringList(corePluginsProperties).toString());
    }

    @Test
    public void testUpdateChangeProperty()
    {
        FileUtilities.writeToFile(corePluginsProperties, "abc = 123\n" + ENABLED_TECHNOLOGIES_KEY
                + "=screening\nanswer = 42\n");
        Properties variables = new Properties();
        variables.setProperty(TECHNOLOGY_PROTEOMICS, "true");
        variables.setProperty(TECHNOLOGY_SCREENING, "true");

        updateEnabledTechnologyProperties(variables, false);

        assertEquals("[abc = 123, " + ENABLED_TECHNOLOGIES_KEY + " = monitoring-support, dropbox-monitor, dataset-uploader, "
                + "dataset-file-search, proteomics, screening, answer = 42]",
                FileUtilities
                        .loadToStringList(corePluginsProperties).toString());
    }

    @Test
    public void testUpdateAppendProperty()
    {
        FileUtilities.writeToFile(corePluginsProperties, "abc = 123");
        Properties variables = new Properties();
        variables.setProperty(TECHNOLOGY_PROTEOMICS, "false");
        variables.setProperty(TECHNOLOGY_SCREENING, "true");

        updateEnabledTechnologyProperties(variables, false);

        assertEquals("[abc = 123, " + ENABLED_TECHNOLOGIES_KEY + " = monitoring-support, dropbox-monitor, dataset-uploader, "
                + "dataset-file-search, screening]", FileUtilities
                .loadToStringList(corePluginsProperties).toString());
    }

    @Test
    public void testUpdateEnabledTechnologiesForSwitchedTechnologies()
    {
        FileUtilities.writeToFile(corePluginsProperties, "a = b\n" + ENABLED_TECHNOLOGIES_KEY
                + "= proteomics\n" + "gamma = alpha");
        Properties variables = new Properties();
        variables.setProperty(TECHNOLOGY_PROTEOMICS, "false");
        variables.setProperty(TECHNOLOGY_SCREENING, "true");

        updateEnabledTechnologyProperties(variables, false);

        assertEquals("[a = b, " + ENABLED_TECHNOLOGIES_KEY + " = monitoring-support, dropbox-monitor, dataset-uploader, "
                + "dataset-file-search, screening, gamma = alpha]", 
                FileUtilities.loadToStringList(corePluginsProperties).toString());
    }

    private AutomatedInstallData updateEnabledTechnologyProperties(Properties variables,
            boolean isFirstTimeInstallation)
    {
        InstallData data = new InstallData(variables, new VariableSubstitutorImpl(new Properties()));
        SetEnableTechnologiesVariableAction action = new SetEnableTechnologiesVariableAction();
        action.updateEnabledTechnologyProperty(data, isFirstTimeInstallation, workingDirectory);
        return data;
    }
}
