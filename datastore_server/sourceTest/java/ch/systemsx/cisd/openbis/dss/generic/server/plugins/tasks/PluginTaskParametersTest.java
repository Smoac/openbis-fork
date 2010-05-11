/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.demo.DemoProcessingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.demo.DemoReportingPlugin;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Tests for {@link PluginTaskProviders} class.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses =
    { PluginTaskProviders.class, AbstractPluginTaskFactory.class })
public class PluginTaskParametersTest extends AbstractFileSystemTestCase
{
    private static final File STORE_ROOT = new File(".");

    @Test
    public void testCreateReportingPluginsFactories() throws Exception
    {
        Properties props = new Properties();
        String plugin1 = "plugin1";
        String plugin2 = "plugin2";

        props.put(PluginTaskProviders.REPORTING_PLUGIN_NAMES, plugin1 + ", " + plugin2);
        String pluginLabel1 = "Demo Reporting 1";
        String datasetCodes1 = "MZXML, EICML";
        putPluginProperties(props, plugin1, pluginLabel1, datasetCodes1, DemoReportingPlugin.class);

        String pluginLabel2 = "Demo Reporting 2";
        String datasetCodes2 = "EICML";
        putPluginProperties(props, plugin2, pluginLabel2, datasetCodes2, DemoReportingPlugin.class);

        PluginTaskProvider<IReportingPluginTask> factories = createReportingPluginsFactories(props);
        factories.check(false);
        factories.logConfigurations();
        IReportingPluginTask pluginInstance1 = factories.getPluginInstance(plugin1);
        pluginInstance1.createReport(createDatasetDescriptions());

        List<DatastoreServiceDescription> descriptions = factories.getPluginDescriptions();
        assertEquals(2, descriptions.size());
        for (DatastoreServiceDescription desc : descriptions)
        {
            String key = desc.getKey();
            if (key.equals(plugin2))
            {
                assertEquals(pluginLabel2, desc.getLabel());
                assertEquals(1, desc.getDatasetTypeCodes().length);
                assertEquals(datasetCodes2, desc.getDatasetTypeCodes()[0]);
            } else
            {
                assertEquals(plugin1, key);
            }
        }
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testMissingPluginSpecFails() throws Exception
    {
        Properties props = new Properties();
        props.put(PluginTaskProviders.REPORTING_PLUGIN_NAMES, "plugin1");
        createReportingPluginsFactories(props);
    }

    private static PluginTaskProvider<IReportingPluginTask> createReportingPluginsFactories(
            Properties props)
    {
        return PluginTaskProviders.createReportingPluginsFactories(props, "dss", STORE_ROOT);
    }

    private static PluginTaskProvider<IProcessingPluginTask> createProcessingPluginsFactories(
            Properties props)
    {
        return PluginTaskProviders.createProcessingPluginsFactories(props, "dss", STORE_ROOT);
    }

    @Test
    public void testUnspecifiedPluginsParameters() throws Exception
    {
        Properties props = new Properties();
        PluginTaskProvider<IProcessingPluginTask> processing =
                createProcessingPluginsFactories(props);
        assertEquals(0, processing.getPluginDescriptions().size());

        PluginTaskProvider<IProcessingPluginTask> reporting =
                createProcessingPluginsFactories(props);
        assertEquals(0, reporting.getPluginDescriptions().size());

    }

    @Test
    public void testCreateProcessingPluginsFactories() throws Exception
    {
        Properties props = new Properties();
        String plugin1 = "plugin1";

        props.put(PluginTaskProviders.PROCESSING_PLUGIN_NAMES, plugin1);
        putPluginProperties(props, plugin1, "pluginLabel1", "datasetCodes1",
                DemoProcessingPlugin.class);

        PluginTaskProvider<IProcessingPluginTask> factories =
                createProcessingPluginsFactories(props);
        factories.check(true);
        factories.logConfigurations();
        IProcessingPluginTask pluginInstance1 = factories.getPluginInstance(plugin1);
        pluginInstance1.process(createDatasetDescriptions(), null);
    }

    private static List<DatasetDescription> createDatasetDescriptions()
    {
        DatasetDescription description = new DatasetDescription();
        description.setDatasetCode(".");
        description.setDataSetLocation("3123123123-123");
        description.setSampleCode("sampleCode");
        description.setGroupCode("groupCode");
        description.setProjectCode("projCode");
        description.setExperimentCode("expCode");
        description.setDatabaseInstanceCode("instance");
        return Arrays.asList(description);
    }

    private void putPluginProperties(Properties props, String pluginId, String pluginLabel,
            String datasetCodes, Class<?> pluginClass) throws IOException, FileNotFoundException
    {
        putPluginProperty(props, pluginId, AbstractPluginTaskFactory.LABEL_PROPERTY_NAME,
                pluginLabel);
        putPluginProperty(props, pluginId, AbstractPluginTaskFactory.DATASET_CODES_PROPERTY_NAME,
                datasetCodes);
        putPluginProperty(props, pluginId, AbstractPluginTaskFactory.CLASS_PROPERTY_NAME,
                pluginClass.getName());
        String[] pluginParams = new String[]
            { "param1 = X", "param2 = Y" };
        File paramsFile = createPluginPropertiesFile(pluginParams);
        putPluginProperty(props, pluginId,
                AbstractPluginTaskFactory.PARAMS_FILE_PATH_PROPERTY_NAME, paramsFile.getPath());
    }

    private File createPluginPropertiesFile(String... lines) throws IOException,
            FileNotFoundException
    {
        File paramsFile = new File(workingDirectory, "pluginParams.properties");
        IOUtils.writeLines(Arrays.asList(lines), "\n", new OutputStreamWriter(new FileOutputStream(
                paramsFile)));
        return paramsFile;
    }

    private static void putPluginProperty(Properties props, String pluginKey, String propertyName,
            String value)
    {
        props.put(pluginKey + "." + propertyName, value);

    }
}
