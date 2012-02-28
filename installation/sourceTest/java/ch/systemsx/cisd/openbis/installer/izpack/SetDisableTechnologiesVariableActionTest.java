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
import static ch.systemsx.cisd.openbis.installer.izpack.SetDisableTechnologiesVariableAction.DISABLED_CORE_PLUGINS_KEY;
import static ch.systemsx.cisd.openbis.installer.izpack.SetDisableTechnologiesVariableAction.DISABLED_TECHNOLOGIES_VARNAME;
import static ch.systemsx.cisd.openbis.installer.izpack.SetTechnologyCheckBoxesAction.DISABLED_TECHNOLOGIES_KEY;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.core.substitutor.VariableSubstitutorImpl;
import com.izforge.izpack.installer.data.InstallData;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SetDisableTechnologiesVariableActionTest extends AbstractFileSystemTestCase
{
    private File configFile;
    private File dssConfigFile;

    @Override
    @BeforeMethod
    public void setUp()
    {
        configFile = new File(workingDirectory, Utils.AS_PATH + Utils.SERVICE_PROPERTIES_PATH);
        configFile.getParentFile().mkdirs();
        dssConfigFile = new File(workingDirectory, Utils.DSS_PATH + Utils.SERVICE_PROPERTIES_PATH);
        dssConfigFile.getParentFile().mkdirs();
        try
        {
            dssConfigFile.createNewFile();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }
    
    @AfterMethod
    public void tearDown()
    {
        configFile.delete();
        dssConfigFile.delete();
    }
    
    @Test
    public void testFirstInstallation()
    {
        Properties variables = new Properties();
        variables.setProperty(TECHNOLOGY_PROTEOMICS, "true");
        variables.setProperty(TECHNOLOGY_SCREENING, "false");

        AutomatedInstallData data = updateDisabledTechnologyProperties(variables, true);

        assertEquals("screening", data.getVariable(DISABLED_TECHNOLOGIES_VARNAME));
    }

    @Test
    public void testFirstInstallationScreeningOnly()
    {
        Properties variables = new Properties();
        variables.setProperty(TECHNOLOGY_PROTEOMICS, "false");
        variables.setProperty(TECHNOLOGY_SCREENING, "false");

        AutomatedInstallData data = updateDisabledTechnologyProperties(variables, true);

        assertEquals("proteomics, screening", data.getVariable(DISABLED_TECHNOLOGIES_VARNAME));
    }
    
    @Test
    public void testUpdateMissingConfigFile()
    {
        Properties variables = new Properties();
        variables.setProperty(TECHNOLOGY_PROTEOMICS, "false");
        variables.setProperty(TECHNOLOGY_SCREENING, "false");
        
        try
        {
            updateDisabledTechnologyProperties(variables, false);
            fail("Exception expected.");
        } catch (RuntimeException ex)
        {
            assertEquals("targets/unit-test-wd/ch.systemsx.cisd.openbis.installer.izpack."
                    + "SetDisableTechnologiesVariableActionTest/servers/openBIS-server/jetty/"
                    + "etc/service.properties (No such file or directory)", ex.getMessage());
        }
    }

    @Test
    public void testUpdateUnchangedProperty()
    {
        FileUtilities.writeToFile(configFile, "abc = 123\n" + DISABLED_TECHNOLOGIES_KEY
                + "=screening");
        Properties variables = new Properties();
        variables.setProperty(TECHNOLOGY_PROTEOMICS, "true");
        variables.setProperty(TECHNOLOGY_SCREENING, "false");

        updateDisabledTechnologyProperties(variables, false);

        assertEquals("[abc = 123, " + DISABLED_TECHNOLOGIES_KEY + "=screening]", FileUtilities
                .loadToStringList(configFile).toString());
    }
    
    @Test
    public void testUpdateChangeProperty()
    {
        FileUtilities.writeToFile(configFile, "abc = 123\n" + DISABLED_TECHNOLOGIES_KEY
                + "=screening\nanswer = 42\n");
        Properties variables = new Properties();
        variables.setProperty(TECHNOLOGY_PROTEOMICS, "true");
        variables.setProperty(TECHNOLOGY_SCREENING, "true");
        
        updateDisabledTechnologyProperties(variables, false);
        
        assertEquals("[abc = 123, " + DISABLED_TECHNOLOGIES_KEY + " = , answer = 42]", FileUtilities
                .loadToStringList(configFile).toString());
        assertEquals("[" + DISABLED_CORE_PLUGINS_KEY + " = ]", FileUtilities
                .loadToStringList(dssConfigFile).toString());
    }
    
    @Test
    public void testUpdateAppendProperty()
    {
        FileUtilities.writeToFile(configFile, "abc = 123");
        Properties variables = new Properties();
        variables.setProperty(TECHNOLOGY_PROTEOMICS, "false");
        variables.setProperty(TECHNOLOGY_SCREENING, "true");
        
        updateDisabledTechnologyProperties(variables, false);
        
        assertEquals("[abc = 123, " + DISABLED_TECHNOLOGIES_KEY + " = proteomics]", FileUtilities
                .loadToStringList(configFile).toString());
        assertEquals("[" + DISABLED_CORE_PLUGINS_KEY + " = proteomics]", FileUtilities
                .loadToStringList(dssConfigFile).toString());
    }

    @Test
    public void testUpdateDisabledPluginsForSwitchedTechnologies()
    {
        FileUtilities.writeToFile(configFile, "abc = 123");
        FileUtilities.writeToFile(dssConfigFile, "a = b\n" + DISABLED_CORE_PLUGINS_KEY
                + "= screening, proteomics:a:b\n" + "gamma = alpha");
        Properties variables = new Properties();
        variables.setProperty(TECHNOLOGY_PROTEOMICS, "false");
        variables.setProperty(TECHNOLOGY_SCREENING, "true");

        updateDisabledTechnologyProperties(variables, false);

        assertEquals("[a = b, " + DISABLED_CORE_PLUGINS_KEY + " = proteomics:a:b, proteomics, "
                + "gamma = alpha]", FileUtilities.loadToStringList(dssConfigFile).toString());
    }
    
    @Test
    public void testUpdateDisabledPluginsForSameTechnology()
    {
        FileUtilities.writeToFile(configFile, "abc = 123");
        FileUtilities.writeToFile(dssConfigFile, "a = b\n" + DISABLED_CORE_PLUGINS_KEY
                + "= proteomics, proteomics:a:b\n" + "gamma = alpha");
        Properties variables = new Properties();
        variables.setProperty(TECHNOLOGY_PROTEOMICS, "false");
        variables.setProperty(TECHNOLOGY_SCREENING, "true");
        
        updateDisabledTechnologyProperties(variables, false);
    }

    private AutomatedInstallData updateDisabledTechnologyProperties(Properties variables,
            boolean isFirstTimeInstallation)
    {
        InstallData data = new InstallData(variables, new VariableSubstitutorImpl(new Properties()));
        SetDisableTechnologiesVariableAction action = new SetDisableTechnologiesVariableAction();
        action.updateDisabledTechnologyProperty(data, isFirstTimeInstallation, workingDirectory);
        return data;
    }
}
