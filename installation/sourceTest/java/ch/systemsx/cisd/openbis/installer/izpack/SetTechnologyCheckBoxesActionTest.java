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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SetTechnologyCheckBoxesActionTest extends AssertJUnit
{
    private static final File TEST_FOLDER = new File("targets/test-folder");
    
    @BeforeMethod
    public void setUp() throws Exception
    {
        FileUtilities.deleteRecursively(TEST_FOLDER);
        TEST_FOLDER.mkdirs();
    }
    
    @Test
    public void testEmptyInstallDir()
    {
        SetTechnologyCheckBoxesAction action = new SetTechnologyCheckBoxesAction();
        
        assertEquals(false, action.isTechnologyEnabled(TEST_FOLDER, "proteomics"));
        assertEquals(false, action.isTechnologyEnabled(TEST_FOLDER, "screening"));
        assertEquals(true, action.isTechnologyEnabled(TEST_FOLDER, "blabla"));
    }
    
    @Test
    public void testDisabledTechnologiesPropertyPresent() throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty(SetTechnologyCheckBoxesAction.DISABLED_TECHNOLOGIES_KEY, "proteomics");
        saveProperties(properties, Utils.AS_PATH + Utils.SERVICE_PROPERTIES_PATH);
        SetTechnologyCheckBoxesAction action = new SetTechnologyCheckBoxesAction();
        
        assertEquals(false, action.isTechnologyEnabled(TEST_FOLDER, "proteomics"));
        assertEquals(true, action.isTechnologyEnabled(TEST_FOLDER, "screening"));
        assertEquals(true, action.isTechnologyEnabled(TEST_FOLDER, "blabla"));
    }
    
    @Test
    public void testDisabledTechnologiesPropertyNotPresentForGeneric() throws Exception
    {
        Properties properties = new Properties();
        saveProperties(properties, Utils.DSS_PATH + Utils.SERVICE_PROPERTIES_PATH);
        SetTechnologyCheckBoxesAction action = new SetTechnologyCheckBoxesAction();
        
        assertEquals(false, action.isTechnologyEnabled(TEST_FOLDER, "proteomics"));
        assertEquals(false, action.isTechnologyEnabled(TEST_FOLDER, "screening"));
    }
    
    @Test
    public void testDisabledTechnologiesPropertyNotPresentForProteomics() throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty("blabla.Proteomics", "blub");
        saveProperties(properties, Utils.DSS_PATH + Utils.SERVICE_PROPERTIES_PATH);
        SetTechnologyCheckBoxesAction action = new SetTechnologyCheckBoxesAction();
        
        assertEquals(true, action.isTechnologyEnabled(TEST_FOLDER, "proteomics"));
        assertEquals(false, action.isTechnologyEnabled(TEST_FOLDER, "screening"));
    }
    
    @Test
    public void testDisabledTechnologiesPropertyNotPresentForScreening() throws Exception
    {
        Properties properties = new Properties();
        properties.setProperty("blabla.class", "blub.ScreeningProcessor");
        saveProperties(properties, Utils.DSS_PATH + Utils.SERVICE_PROPERTIES_PATH);
        SetTechnologyCheckBoxesAction action = new SetTechnologyCheckBoxesAction();
        
        assertEquals(false, action.isTechnologyEnabled(TEST_FOLDER, "proteomics"));
        assertEquals(true, action.isTechnologyEnabled(TEST_FOLDER, "screening"));
    }
    
    private void saveProperties(Properties properties, String path) throws IOException
    {
        FileWriter fileWriter = null;
        try
        {
            File file = new File(TEST_FOLDER, path);
            file.getParentFile().mkdirs();
            fileWriter = new FileWriter(file);
            properties.store(fileWriter, "");
        } finally
        {
            if (fileWriter != null)
            {
                fileWriter.close();
            }
        }
    }
}
