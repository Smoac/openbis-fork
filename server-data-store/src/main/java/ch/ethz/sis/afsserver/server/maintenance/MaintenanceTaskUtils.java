/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.afsserver.server.maintenance;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.log.Logger;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;

/**
 * A static helper class that knows e.g. how to read configuration of maintenance tasks from {@link Properties} and start all the maintenance plugins.
 *
 * @author Piotr Buczek
 */
public class MaintenanceTaskUtils
{
    /**
     * default name of a property with maintenance plugin names separated by delimiter
     */
    public static final String DEFAULT_MAINTENANCE_PLUGINS_PROPERTY_NAME = "maintenance-plugins";

    private static final Logger operationLog =
            LogManager.getLogger(MaintenanceTaskUtils.class);

    public static List<MaintenancePlugin> startupMaintenancePlugins(
            MaintenanceTaskParameters[] maintenancePlugins)
    {
        final List<MaintenancePlugin> plugins = new ArrayList<MaintenancePlugin>();
        for (MaintenanceTaskParameters parameters : maintenancePlugins)
        {
            MaintenancePlugin plugin = new MaintenancePlugin(parameters);
            plugins.add(plugin);
        }

        for (MaintenancePlugin plugin : plugins)
        {
            plugin.start();
        }
        return plugins;
    }

    public static void shutdownMaintenancePlugins(List<MaintenancePlugin> maintenancePlugins)
    {
        if (maintenancePlugins == null)
        {
            return;
        }
        for (MaintenancePlugin plugin : maintenancePlugins)
        {
            try
            {
                plugin.shutdown();
            } catch (Exception ex)
            {
                operationLog.catching(new RuntimeException(
                        "Error shutting down maintenance task '" + plugin.getPluginName()
                                + "' failed.", ex));
            }
        }
    }

    /**
     * Inject a maintenance plugin into the running plugin environment.
     */
    public static void injectMaintenancePlugin(MaintenancePlugin plugin)
    {
        plugin.start();
    }

    public static MaintenanceTaskParameters[] createMaintenancePlugins(
            final Properties serviceProperties)
    {
        return createMaintenancePlugins(serviceProperties,
                DEFAULT_MAINTENANCE_PLUGINS_PROPERTY_NAME);
    }

    public static MaintenanceTaskParameters[] createMaintenancePlugins(
            final Properties serviceProperties, final String maintenancePluginsPropertyName)
    {
        SectionProperties[] sectionsProperties =
                PropertyParametersUtil.extractSectionProperties(serviceProperties,
                        DEFAULT_MAINTENANCE_PLUGINS_PROPERTY_NAME, true);
        return asMaintenanceParameters(sectionsProperties);
    }

    private static MaintenanceTaskParameters[] asMaintenanceParameters(
            SectionProperties[] sectionProperties)
    {
        final MaintenanceTaskParameters[] maintenanceParameters =
                new MaintenanceTaskParameters[sectionProperties.length];
        for (int i = 0; i < maintenanceParameters.length; i++)
        {
            SectionProperties section = sectionProperties[i];
            operationLog.info("Create parameters for maintenance plugin '" + section.getKey()
                    + "'.");
            maintenanceParameters[i] =
                    new MaintenanceTaskParameters(section.getProperties(), section.getKey());
        }
        return maintenanceParameters;
    }

}
