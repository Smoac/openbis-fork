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
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.openbis.dss.generic.server.DataStoreServer;
import ch.systemsx.cisd.openbis.dss.generic.server.IServletPropertiesManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.Constants;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SessionWorkspaceUtil;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatastoreServiceDescriptions;

/**
 * Info provider for plugin tasks.
 * 
 * @author Tomasz Pylak
 */
public class PluginTaskInfoProvider implements IPluginTaskInfoProvider
{
    public static final String STOREROOT_DIR_KEY = "storeroot-dir";

    /** name of archiver properties section */
    @Private
    static final String ARCHIVER_SECTION_NAME = "archiver";

    private final PluginTaskProvider<IReportingPluginTask> reportingPlugins;

    private final PluginTaskProvider<IProcessingPluginTask> processingPlugins;

    private final ArchiverPluginFactory archiverTaskFactory;

    private final File storeRoot;

    private final File sessionWorkspaceRootDir;

    /** for external injections */
    public static IPluginTaskInfoProvider create()
    {
        IServletPropertiesManager servletPropertiesManager = DataStoreServer.getConfigParameters();
        Properties properties = DssPropertyParametersUtil.loadServiceProperties();
        final String storeRootDir = properties.getProperty(STOREROOT_DIR_KEY);
        final File storeRoot = new File(storeRootDir);
        final File workspaceRoot = SessionWorkspaceUtil.getSessionWorkspace(properties);
        PluginTaskInfoProvider providers =
                new PluginTaskInfoProvider(properties, servletPropertiesManager, storeRoot,
                        workspaceRoot);
        providers.check();
        providers.logConfigurations();
        return providers;
    }

    @Private
    // public only for tests
    public PluginTaskInfoProvider(Properties serviceProperties,
            IServletPropertiesManager servletPropertiesManager, File storeRoot,
            File sessionWorkspaceRoot)
    {
        this.storeRoot = storeRoot;
        this.sessionWorkspaceRootDir = sessionWorkspaceRoot;
        String datastoreCode = DssPropertyParametersUtil.getDataStoreCode(serviceProperties);
        this.reportingPlugins =
                createReportingPluginsFactories(serviceProperties, servletPropertiesManager,
                        datastoreCode, storeRoot);
        this.processingPlugins =
                createProcessingPluginsFactories(serviceProperties, servletPropertiesManager,
                        datastoreCode, storeRoot);
        this.archiverTaskFactory = createArchiverTaskFactory(serviceProperties, datastoreCode);
    }

    /**
     * Returns the root directory of the data store.
     */
    @Override
    public final File getStoreRoot()
    {
        return storeRoot;
    }

    /**
     * Returns the root directory of session workspaces.
     */
    @Override
    public File getSessionWorkspaceRootDir()
    {
        return sessionWorkspaceRootDir;
    }

    @Override
    public PluginTaskProvider<IReportingPluginTask> getReportingPluginsProvider()
    {
        return reportingPlugins;
    }

    @Override
    public PluginTaskProvider<IProcessingPluginTask> getProcessingPluginsProvider()
    {
        return processingPlugins;
    }

    @Override
    public ArchiverPluginFactory getArchiverPluginFactory()
    {
        return archiverTaskFactory;
    }

    private void check()
    {
        processingPlugins.check(true);
        reportingPlugins.check(false);
    }

    @Override
    public void logConfigurations()
    {
        processingPlugins.logConfigurations();
        reportingPlugins.logConfigurations();
        archiverTaskFactory.logConfiguration();
    }

    @Private
    static PluginTaskProvider<IReportingPluginTask> createReportingPluginsFactories(
            Properties serviceProperties, IServletPropertiesManager configParameters,
            String datastoreCode, File storeRoot)
    {
        SectionProperties[] sectionsProperties =
                extractSectionProperties(serviceProperties, Constants.REPORTING_PLUGIN_NAMES);
        ReportingPluginTaskFactory[] factories =
                new ReportingPluginTaskFactory[sectionsProperties.length];
        for (int i = 0; i < factories.length; i++)
        {
            factories[i] =
                    new ReportingPluginTaskFactory(configParameters, sectionsProperties[i],
                            datastoreCode, storeRoot);
        }
        return new PluginTaskProvider<IReportingPluginTask>(factories);
    }

    @Private
    static PluginTaskProvider<IProcessingPluginTask> createProcessingPluginsFactories(
            Properties serviceProperties, IServletPropertiesManager configParameters,
            String datastoreCode, File storeRoot)
    {
        SectionProperties[] sectionsProperties =
                extractSectionProperties(serviceProperties, Constants.PROCESSING_PLUGIN_NAMES);
        ProcessingPluginTaskFactory[] factories =
                new ProcessingPluginTaskFactory[sectionsProperties.length];
        for (int i = 0; i < factories.length; i++)
        {
            factories[i] =
                    new ProcessingPluginTaskFactory(configParameters, sectionsProperties[i],
                            datastoreCode, storeRoot);
        }
        return new PluginTaskProvider<IProcessingPluginTask>(factories);
    }

    private ArchiverPluginFactory createArchiverTaskFactory(Properties serviceProperties,
            String datastoreCode)
    {
        SectionProperties sectionsProperties =
                extractSingleSectionProperties(serviceProperties, ARCHIVER_SECTION_NAME);
        return new ArchiverPluginFactory(sectionsProperties);
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

    @Override
    public DatastoreServiceDescriptions getPluginTaskDescriptions()
    {
        return new DatastoreServiceDescriptions(reportingPlugins.getPluginDescriptions(),
                processingPlugins.getPluginDescriptions());
    }

}
