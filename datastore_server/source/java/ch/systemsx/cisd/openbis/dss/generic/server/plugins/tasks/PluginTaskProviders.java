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
import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PropertyParametersUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatastoreServiceDescriptions;

/**
 * @author Tomasz Pylak
 */
public class PluginTaskProviders
{
    /** property with repotring plugins names separated by delimiter */
    @Private
    static final String REPORTING_PLUGIN_NAMES = "reporting-plugins";

    /** property with processing plugins names separated by delimiter */
    @Private
    static final String PROCESSING_PLUGIN_NAMES = "processing-plugins";

    /** name of archiver properties section */
    @Private
    static final String ARCHIVER_SECTION_NAME = "archiver";

    private final PluginTaskProvider<IReportingPluginTask> reportingPlugins;

    private final PluginTaskProvider<IProcessingPluginTask> processingPlugins;

    private final ArchiverTaskFactory archiverTaskFactory;

    /** for external injections */
    public static PluginTaskProviders create(File storeRoot)
    {
        Properties properties = PropertyParametersUtil.loadServiceProperties();
        PluginTaskProviders providers = new PluginTaskProviders(properties, storeRoot);
        providers.check();
        providers.logConfigurations();
        return providers;
    }

    @Private
    // public only for tests
    public PluginTaskProviders(Properties serviceProperties, File storeRoot)
    {
        String datastoreCode = PropertyParametersUtil.getDataStoreCode(serviceProperties);
        this.reportingPlugins =
                createReportingPluginsFactories(serviceProperties, datastoreCode, storeRoot);
        this.processingPlugins =
                createProcessingPluginsFactories(serviceProperties, datastoreCode, storeRoot);
        this.archiverTaskFactory = createArchiverTaskFactory(serviceProperties, datastoreCode);
    }

    public PluginTaskProvider<IReportingPluginTask> getReportingPluginsProvider()
    {
        return reportingPlugins;
    }

    public PluginTaskProvider<IProcessingPluginTask> getProcessingPluginsProvider()
    {
        return processingPlugins;
    }

    public ArchiverTaskFactory getArchiverTaskFactory()
    {
        return archiverTaskFactory;
    }

    private void check()
    {
        processingPlugins.check(true);
        reportingPlugins.check(false);
    }

    public void logConfigurations()
    {
        processingPlugins.logConfigurations();
        reportingPlugins.logConfigurations();
        archiverTaskFactory.logConfiguration();
    }

    @Private
    static PluginTaskProvider<IReportingPluginTask> createReportingPluginsFactories(
            Properties serviceProperties, String datastoreCode, File storeRoot)
    {
        SectionProperties[] sectionsProperties =
                extractSectionProperties(serviceProperties, REPORTING_PLUGIN_NAMES);
        ReportingPluginTaskFactory[] factories =
                new ReportingPluginTaskFactory[sectionsProperties.length];
        for (int i = 0; i < factories.length; i++)
        {
            factories[i] =
                    new ReportingPluginTaskFactory(sectionsProperties[i], datastoreCode, storeRoot);
        }
        return new PluginTaskProvider<IReportingPluginTask>(factories);
    }

    @Private
    static PluginTaskProvider<IProcessingPluginTask> createProcessingPluginsFactories(
            Properties serviceProperties, String datastoreCode, File storeRoot)
    {
        SectionProperties[] sectionsProperties =
                extractSectionProperties(serviceProperties, PROCESSING_PLUGIN_NAMES);
        ProcessingPluginTaskFactory[] factories =
                new ProcessingPluginTaskFactory[sectionsProperties.length];
        for (int i = 0; i < factories.length; i++)
        {
            factories[i] =
                    new ProcessingPluginTaskFactory(sectionsProperties[i], datastoreCode, storeRoot);
        }
        return new PluginTaskProvider<IProcessingPluginTask>(factories);
    }

    private ArchiverTaskFactory createArchiverTaskFactory(Properties serviceProperties,
            String datastoreCode)
    {
        SectionProperties sectionsProperties =
                extractSingleSectionProperties(serviceProperties, ARCHIVER_SECTION_NAME);
        return new ArchiverTaskFactory(sectionsProperties);
    }

    private static SectionProperties[] extractSectionProperties(Properties serviceProperties,
            String namesListPropertyKey)
    {
        return PropertyParametersUtil.extractSectionProperties(serviceProperties,
                namesListPropertyKey, false);
    }

    private static SectionProperties extractSingleSectionProperties(Properties serviceProperties,
            String sectionName)
    {
        return PropertyParametersUtil.extractSingleSectionProperties(serviceProperties,
                sectionName, false);
    }

    public DatastoreServiceDescriptions getPluginTaskDescriptions()
    {
        return new DatastoreServiceDescriptions(reportingPlugins.getPluginDescriptions(),
                processingPlugins.getPluginDescriptions());
    }

}
