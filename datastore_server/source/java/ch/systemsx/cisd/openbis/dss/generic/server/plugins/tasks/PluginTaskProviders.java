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

import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PropertyParametersUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.openbis.generic.shared.dto.PluginTaskDescriptions;

/**
 * @author Tomasz Pylak
 */
public class PluginTaskProviders
{
    @Private
    /* * property with repotring plugins names separated by delimiter */
    static final String REPORTING_PLUGIN_NAMES = "reporting-plugins";

    @Private
    /* * property with processing plugins names separated by delimiter */
    static final String PROCESSING_PLUGIN_NAMES = "processing-plugins";

    private final PluginTaskProvider<IReportingPluginTask> reportingPlugins;

    private final PluginTaskProvider<IProcessingPluginTask> processingPlugins;

    public static PluginTaskProviders create()
    {
        Properties properties = PropertyParametersUtil.loadServiceProperties();
        PluginTaskProviders providers = new PluginTaskProviders(properties);
        providers.check();
        providers.logConfigurations();
        return providers;
    }

    @Private
    // only for tests
    public PluginTaskProviders(Properties serviceProperties)
    {
        this.reportingPlugins = createReportingPluginsFactories(serviceProperties);
        this.processingPlugins = createProcessingPluginsFactories(serviceProperties);
    }

    public PluginTaskProvider<IReportingPluginTask> getReportingPluginsProvider()
    {
        return reportingPlugins;
    }

    public PluginTaskProvider<IProcessingPluginTask> getProcessingPluginsProvider()
    {
        return processingPlugins;
    }

    private void check()
    {
        processingPlugins.check();
        reportingPlugins.check();
    }

    public void logConfigurations()
    {
        processingPlugins.logConfigurations();
        reportingPlugins.logConfigurations();
    }

    @Private
    static PluginTaskProvider<IReportingPluginTask> createReportingPluginsFactories(
            Properties serviceProperties)
    {
        SectionProperties[] sectionsProperties =
                extractSectionProperties(serviceProperties, REPORTING_PLUGIN_NAMES);
        ReportingPluginTaskFactory[] factories =
                new ReportingPluginTaskFactory[sectionsProperties.length];
        for (int i = 0; i < factories.length; i++)
        {
            factories[i] = new ReportingPluginTaskFactory(sectionsProperties[i]);
        }
        return new PluginTaskProvider<IReportingPluginTask>(factories);
    }

    @Private
    static PluginTaskProvider<IProcessingPluginTask> createProcessingPluginsFactories(
            Properties serviceProperties)
    {
        SectionProperties[] sectionsProperties =
                extractSectionProperties(serviceProperties, PROCESSING_PLUGIN_NAMES);
        ProcessingPluginTaskFactory[] factories =
                new ProcessingPluginTaskFactory[sectionsProperties.length];
        for (int i = 0; i < factories.length; i++)
        {
            factories[i] = new ProcessingPluginTaskFactory(sectionsProperties[i]);
        }
        return new PluginTaskProvider<IProcessingPluginTask>(factories);
    }

    private static SectionProperties[] extractSectionProperties(Properties serviceProperties,
            String namesListPropertyKey)
    {
        return PropertyParametersUtil.extractSectionProperties(serviceProperties,
                namesListPropertyKey, false);
    }

    public PluginTaskDescriptions getPluginTaskDescriptions()
    {
        return new PluginTaskDescriptions(reportingPlugins.getPluginDescriptions(),
                processingPlugins.getPluginDescriptions());
    }

}
