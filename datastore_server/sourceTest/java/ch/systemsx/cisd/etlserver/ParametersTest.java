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

package ch.systemsx.cisd.etlserver;

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
import ch.systemsx.cisd.etlserver.plugin_tasks.demo.DemoProcessingPlugin;
import ch.systemsx.cisd.etlserver.plugin_tasks.demo.DemoReportingPlugin;
import ch.systemsx.cisd.etlserver.plugin_tasks.framework.AbstractPluginTaskFactory;
import ch.systemsx.cisd.etlserver.plugin_tasks.framework.DatasetDescription;
import ch.systemsx.cisd.etlserver.plugin_tasks.framework.IProcessingPluginTask;
import ch.systemsx.cisd.etlserver.plugin_tasks.framework.IReportingPluginTask;
import ch.systemsx.cisd.etlserver.plugin_tasks.framework.PluginTaskFactories;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginTaskDescription;

/**
 * Tests for {@link Parameters} class.
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses =
    { Parameters.class, AbstractPluginTaskFactory.class })
public class ParametersTest extends AbstractFileSystemTestCase
{
    @Test
    public void testCreateReportingPluginsFactories() throws Exception
    {
        Properties props = new Properties();
        String plugin1 = "plugin1";
        String plugin2 = "plugin2";

        props.put(Parameters.REPORTING_PLUGIN_NAMES, plugin1 + ", " + plugin2);
        String pluginLabel1 = "Demo Reporting 1";
        String datasetCodes1 = "MZXML, EICML";
        putPluginProperties(props, plugin1, pluginLabel1, datasetCodes1, DemoReportingPlugin.class);

        String pluginLabel2 = "Demo Reporting 2";
        String datasetCodes2 = "EICML";
        putPluginProperties(props, plugin2, pluginLabel2, datasetCodes2, DemoReportingPlugin.class);

        PluginTaskFactories<IReportingPluginTask> factories =
                Parameters.createReportingPluginsFactories(props);
        factories.check();
        factories.logConfigurations();
        IReportingPluginTask pluginInstance1 = factories.createPluginInstance(plugin1);
        pluginInstance1.createReport(createDatasetDescriptions());
        factories.createPluginInstance(plugin2);

        List<PluginTaskDescription> descriptions = factories.getPluginDescriptions();
        assertEquals(2, descriptions.size());
        for (PluginTaskDescription desc : descriptions)
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
        props.put(Parameters.REPORTING_PLUGIN_NAMES, "plugin1");
        Parameters.createReportingPluginsFactories(props);
    }

    @Test
    public void testUnspecifiedPluginsParameters() throws Exception
    {
        Properties props = new Properties();
        PluginTaskFactories<IProcessingPluginTask> processing =
                Parameters.createProcessingPluginsFactories(props);
        assertEquals(0, processing.getPluginDescriptions().size());

        PluginTaskFactories<IProcessingPluginTask> reporting =
                Parameters.createProcessingPluginsFactories(props);
        assertEquals(0, reporting.getPluginDescriptions().size());

    }

    @Test
    public void testCreateProcessingPluginsFactories() throws Exception
    {
        Properties props = new Properties();
        String plugin1 = "plugin1";

        props.put(Parameters.PROCESSING_PLUGIN_NAMES, plugin1);
        putPluginProperties(props, plugin1, "pluginLabel1", "datasetCodes1",
                DemoProcessingPlugin.class);

        PluginTaskFactories<IProcessingPluginTask> factories =
                Parameters.createProcessingPluginsFactories(props);
        factories.check();
        factories.logConfigurations();
        IProcessingPluginTask pluginInstance1 = factories.createPluginInstance(plugin1);
        pluginInstance1.process(createDatasetDescriptions());
    }

    private static List<DatasetDescription> createDatasetDescriptions()
    {
        return Arrays.asList(new DatasetDescription(new File("."), "3123123123-123"));
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
