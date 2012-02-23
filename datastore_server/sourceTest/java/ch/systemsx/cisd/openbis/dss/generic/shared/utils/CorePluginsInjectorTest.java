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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import static ch.systemsx.cisd.common.maintenance.MaintenanceTaskUtils.DEFAULT_MAINTENANCE_PLUGINS_PROPERTY_NAME;
import static ch.systemsx.cisd.openbis.dss.generic.shared.Constants.INPUT_THREAD_NAMES;
import static ch.systemsx.cisd.openbis.dss.generic.shared.Constants.REPORTING_PLUGIN_NAMES;
import static ch.systemsx.cisd.openbis.dss.generic.shared.utils.CorePluginsInjector.PLUGIN_PROPERTIES_FILE_NAME;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class CorePluginsInjectorTest extends AbstractFileSystemTestCase
{

    private Mockery context;
    private ISimpleLogger logger;
    private CorePluginsInjector injector;
    private File corePluginsFolder;
    private String corePluginsFolderProperty;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        logger = context.mock(ISimpleLogger.class);
        injector = new CorePluginsInjector(logger);
        corePluginsFolder = new File(workingDirectory, "core-plugins");
        corePluginsFolder.mkdirs();
        corePluginsFolderProperty = CorePluginsInjector.CORE_PLUGINS_FOLDER_KEY + " = "
                + corePluginsFolder + "\n";
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testNoCorePluginsDefined()
    {
        injector.injectCorePlugins(new Properties());
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testMissingPluginProperties()
    {
        File alpha = new File(corePluginsFolder, "screening/1/dss/drop-boxes/alpha");
        alpha.mkdirs();
        Properties properties = createProperties();
        properties.setProperty(INPUT_THREAD_NAMES, "a, b");
        
        try
        {
            injector.injectCorePlugins(properties);
            fail("EnvironmentFailureException expected.");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Missing plugin properties: " + alpha.getPath() + "/"
                    + PLUGIN_PROPERTIES_FILE_NAME, ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testPluginIsNotAFolder() throws IOException
    {
        File alpha = new File(corePluginsFolder, "screening/1/dss/drop-boxes/alpha");
        alpha.getParentFile().mkdirs();
        alpha.createNewFile();
        Properties properties = createProperties();
        
        try
        {
            injector.injectCorePlugins(properties);
            fail("EnvironmentFailureException expected.");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Is not a directory: " + alpha.getPath(), ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testDropBoxAndReportingPluginWithSameKey()
    {
        Properties properties = createProperties();
        properties.setProperty(INPUT_THREAD_NAMES, "key1, key2");
        properties.setProperty(REPORTING_PLUGIN_NAMES, "key2, key3");
        
        try
        {
            injector.injectCorePlugins(properties);
            fail("ConfigurationFailureException expected.");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Property key 'key2' for key list 'reporting-plugins' " +
                    "is already defined in some other key list.", ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testADropBoxWithScriptFile()
    {
        File myDropBox = new File(corePluginsFolder, "screening/1/dss/drop-boxes/my-drop-box");
        myDropBox.mkdirs();
        FileUtilities.writeToFile(new File(myDropBox, PLUGIN_PROPERTIES_FILE_NAME),
                "class = blabla\n" + "incoming = ${data}\n" + "script = handler.py");
        FileUtilities.writeToFile(new File(myDropBox, "handler.py"), "print 'hello world'");
        Properties properties = createProperties();
        properties.setProperty(INPUT_THREAD_NAMES, "a, b");
        properties.setProperty("data", "../my-data");
        preparePluginNameLog("screening:drop-boxes:my-drop-box [" + myDropBox + "]");

        injector.injectCorePlugins(properties);

        assertProperties(corePluginsFolderProperty + "data = ../my-data\n"
                + "inputs = a, b, my-drop-box\n" + "my-drop-box.class = blabla\n"
                + "my-drop-box.incoming = ${data}\n" + "my-drop-box.script = " + myDropBox
                + "/handler.py\n", properties);
        assertEquals("../my-data", properties.getProperty("my-drop-box.incoming"));

        context.assertIsSatisfied();
    }
    
    @Test
    public void testDropBoxAndProcessingPluginsFromDifferentTechnologiesAndVersions()
    {
        File myDropBox = new File(corePluginsFolder, "screening/1/dss/drop-boxes/my-drop-box");
        myDropBox.mkdirs();
        FileUtilities.writeToFile(new File(myDropBox, PLUGIN_PROPERTIES_FILE_NAME),
                "script1 = script1.py\n" + "script2 = script2.py");
        FileUtilities.writeToFile(new File(myDropBox, "script1.py"), "print 'hello world'");
        FileUtilities.writeToFile(new File(myDropBox, "script2.py"), "print 'hello universe'");
        File myProcessingPlugin = new File(corePluginsFolder, "proteomics/2/dss/processing-plugins/my-processing");
        myProcessingPlugin.mkdirs();
        FileUtilities.writeToFile(new File(myProcessingPlugin, PLUGIN_PROPERTIES_FILE_NAME),
                "script = script.py");
        FileUtilities.writeToFile(new File(myProcessingPlugin, "script.py"), "print 'hello world'");
        Properties properties = createProperties();
        properties.setProperty(REPORTING_PLUGIN_NAMES, "a, b");
        preparePluginNameLog("screening:drop-boxes:my-drop-box [" + myDropBox + "]",
                "proteomics:processing-plugins:my-processing [" + myProcessingPlugin + "]");
        
        injector.injectCorePlugins(properties);
        
        assertProperties(corePluginsFolderProperty + "inputs = my-drop-box\n"
                + "my-drop-box.script1 = " + myDropBox + "/script1.py\n" + "my-drop-box.script2 = "
                + myDropBox + "/script2.py\n" + "my-processing.script = " + myProcessingPlugin
                + "/script.py\n" + "processing-plugins = my-processing\n"
                + "reporting-plugins = a, b\n", properties);

        context.assertIsSatisfied();
    }
    
    @Test
    public void testADuplicatedReportingPluginVersion2()
    {
        new File(corePluginsFolder, "screening/1/dss/drop-boxes/my-drop-box").mkdirs();
        File dropBox2 = new File(corePluginsFolder, "screening/2/dss/drop-boxes/k2");
        dropBox2.mkdirs();
        FileUtilities.writeToFile(new File(dropBox2, PLUGIN_PROPERTIES_FILE_NAME),
                "class = blabla\n");
        Properties properties = createProperties();
        properties.setProperty(REPORTING_PLUGIN_NAMES, "k1, k2");

        
        try
        {
            injector.injectCorePlugins(properties);
            fail("ConfigurationFailureException expected.");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("There is already a plugin named 'k2'.", ex.getMessage());
        }
                
        context.assertIsSatisfied();
    }
    
    @Test
    public void testAMiscellaneousPlugin()
    {
        File misc = new File(corePluginsFolder, "screening/1/dss/miscellaneous/a");
        misc.mkdirs();
        FileUtilities.writeToFile(new File(misc, PLUGIN_PROPERTIES_FILE_NAME),
                REPORTING_PLUGIN_NAMES + " = my-report\n" + "my-report.script = r.py\n"
                        + DEFAULT_MAINTENANCE_PLUGINS_PROPERTY_NAME
                        + " = t1, t2\n" + "t1.class = blabla\n" + "t2.script = task.py");
        FileUtilities.writeToFile(new File(misc, "r.py"), "print 'hello world'");
        FileUtilities.writeToFile(new File(misc, "task.py"), "print 'hello task'");
        Properties properties = createProperties();
        properties.setProperty(REPORTING_PLUGIN_NAMES, "k1, k2");
        preparePluginNameLog("screening:miscellaneous:a [" + misc + "]");

        injector.injectCorePlugins(properties);

        assertProperties(corePluginsFolderProperty + "maintenance-plugins = t1, t2\n"
                + "my-report.script = " + misc + "/r.py\n"
                + "reporting-plugins = k1, k2, my-report\n" + "t1.class = blabla\n"
                + "t2.script = " + misc + "/task.py\n", properties);

        context.assertIsSatisfied();
    }
    
    private void preparePluginNameLog(final String... fullPluginNames)
    {
        context.checking(new Expectations()
            {
                {
                    for (String fullPluginName : fullPluginNames)
                    {
                        one(logger).log(LogLevel.INFO, "Plugin " + fullPluginName + " added.");
                    }
                }
            });
    }

    private void assertProperties(String expectedProperties, Properties properties)
    {
        Set<Entry<Object, Object>> entrySet = properties.entrySet();
        List<String> actualProperties = new ArrayList<String>();
        for (Entry<Object, Object> entry : entrySet)
        {
            actualProperties.add(entry.getKey() + " = " + entry.getValue());
        }
        Collections.sort(actualProperties);
        StringBuilder builder = new StringBuilder();
        for (String actualProperty : actualProperties)
        {
            builder.append(actualProperty).append("\n");
        }
        assertEquals(expectedProperties, builder.toString());
    }
    
    private Properties createProperties()
    {
        Properties properties = new ExtendedProperties();
        properties.setProperty(CorePluginsInjector.CORE_PLUGINS_FOLDER_KEY, corePluginsFolder.getPath());
        return properties;
    }

}
