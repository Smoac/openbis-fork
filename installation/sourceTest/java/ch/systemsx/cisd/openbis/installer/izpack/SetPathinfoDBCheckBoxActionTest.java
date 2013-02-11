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

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.izforge.izpack.installer.data.InstallData;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SetPathinfoDBCheckBoxActionTest  extends AbstractFileSystemTestCase
{
    private File dssServicePropertiesFile;
    private SetPathinfoDBCheckBoxAction action;

    @BeforeMethod
    public void setUpFiles() throws IOException
    {
        dssServicePropertiesFile =
                new File(workingDirectory, Utils.DSS_PATH + Utils.SERVICE_PROPERTIES_PATH);
        FileUtils.copyFile(new File("../openbis_standard_technologies/dist/etc/service.properties/"),
                dssServicePropertiesFile);
        GlobalInstallationContext.installDir = workingDirectory;
        action = new SetPathinfoDBCheckBoxAction();
    }

    @Test
    public void testDefaultServiceProperties()
    {
        InstallData data = new InstallData(new Properties(), null);
        action.executeAction(data, null);
        
        assertEquals("true", data.getVariable(GlobalInstallationContext.PATHINFO_DB_ENABLED));
    }
    
    @Test
    public void testRemovedPathinfoDatasource()
    {
        Utils.updateOrAppendProperty(dssServicePropertiesFile,
                ExecuteSetupScriptsAction.DATA_SOURCES_KEY, "halihalo");
        InstallData data = new InstallData(new Properties(), null);
        action.executeAction(data, null);

        assertEquals("false", data.getVariable(GlobalInstallationContext.PATHINFO_DB_ENABLED));
    }
    
    @Test
    public void testNonExistingServiceProperties()
    {
        FileUtilities.delete(dssServicePropertiesFile);
        InstallData data = new InstallData(new Properties(), null);
        action.executeAction(data, null);
        
        assertEquals("true", data.getVariable(GlobalInstallationContext.PATHINFO_DB_ENABLED));
    }
}
