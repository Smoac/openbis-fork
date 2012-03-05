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

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.maintenance.MaintenanceTaskUtils;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CorePlugin;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginScanner;

/**
 * Injector of DSS plugin.properties from core plugins folder into service.properties.
 *
 * @author Franz-Josef Elmer
 */
class CorePluginsInjector
{
     static final String DELETE_KEY_WORD = "__DELETE__";

    private static final String UNALLOWED_PLUGIN_NAME_CHARACTERS = " ,=";

    static final String CORE_PLUGINS_FOLDER_KEY = "core-plugins-folder";
    
    static final String DISABLED_CORE_PLUGINS_KEY = "disabled-core-plugins";
    
    static final String DISABLED_MARKER_FILE_NAME = "disabled";
    
    static final String PLUGIN_PROPERTIES_FILE_NAME = "plugin.properties";
    
    enum PluginType
    {
        DROP_BOXES("drop-boxes", Constants.INPUT_THREAD_NAMES), 
        DATA_SOURCES("data-sources", Constants.DATA_SOURCES_KEY), 
        SERVICES("services", Constants.PLUGIN_SERVICES_LIST_KEY), 
        REPORTING_PLUGINS("reporting-plugins", Constants.REPORTING_PLUGIN_NAMES), 
        PROCESSING_PLUGINS("processing-plugins", Constants.PROCESSING_PLUGIN_NAMES),
        MAINTENANCE_TASKS("maintenance-tasks", MaintenanceTaskUtils.DEFAULT_MAINTENANCE_PLUGINS_PROPERTY_NAME), 
        MISCELLANEOUS("miscellaneous", null);

        private final String subFolderName;
        private final String keyOfKeyListPropertyOrNull;

        PluginType(String subFolderName, String keyOfKeyListPropertyOrNull)
        {
            this.subFolderName = subFolderName;
            this.keyOfKeyListPropertyOrNull = keyOfKeyListPropertyOrNull;
        }

        public String getSubFolderName()
        {
            return subFolderName;
        }

        public String getKeyOfKeyListPropertyOrNull()
        {
            return keyOfKeyListPropertyOrNull;
        }

        public boolean isUniquePluginNameRequired()
        {
            return keyOfKeyListPropertyOrNull != null;
        }
        
    }
    
    private static final ISimpleLogger DEFAULT_LOGGER = new Log4jSimpleLogger(LogFactory.getLogger(
            LogCategory.OPERATION, CorePluginsInjector.class));
    
    private final ISimpleLogger logger;

    private final Set<String> keysOfKeyLists;
    
    CorePluginsInjector()
    {
        this(DEFAULT_LOGGER);
    }
    
    CorePluginsInjector(ISimpleLogger logger)
    {
        this.logger = logger;
        PluginType[] values = PluginType.values();
        keysOfKeyLists = new HashSet<String>();
        for (PluginType type : values)
        {
            String keyOfKeyListPropertyOrNull = type.getKeyOfKeyListPropertyOrNull();
            if (keyOfKeyListPropertyOrNull != null)
            {
                keysOfKeyLists.add(keyOfKeyListPropertyOrNull);
            }
        }
    }
    
    void injectCorePlugins(Properties properties)
    {
        String corePluginsFolderPath = properties.getProperty(CORE_PLUGINS_FOLDER_KEY);
        if (corePluginsFolderPath == null)
        {
            return;
        }
        Set<String> disabledPlugins = getDisabledPlugins(properties);
        PluginKeyBundles pluginKeyBundles = new PluginKeyBundles(properties);
        Set<String> pluginNames = new HashSet<String>();
        pluginKeyBundles.addAndCheckUniquePluginNames(pluginNames);
        Map<PluginType, Map<String, DssCorePlugin>> plugins =
                scanForCorePlugins(corePluginsFolderPath, disabledPlugins, pluginNames);
        for (Entry<PluginType, Map<String, DssCorePlugin>> entry : plugins.entrySet())
        {
            PluginType pluginType = entry.getKey();
            Map<String, DssCorePlugin> map = entry.getValue();
            for (Entry<String, DssCorePlugin> entry2 : map.entrySet())
            {
                String pluginName = entry2.getKey();
                DssCorePlugin plugin = entry2.getValue();
                File definingFolder = plugin.getDefiningFolder();
                if (new File(definingFolder, DISABLED_MARKER_FILE_NAME).exists())
                {
                    continue;
                }
                Properties pluginProperties = getPluginProperties(definingFolder);
                if (pluginType.isUniquePluginNameRequired())
                {
                    pluginKeyBundles.addPluginNameFor(pluginType, pluginName);
                    for (Entry<Object, Object> keyValuePair : pluginProperties.entrySet())
                    {
                        String value = keyValuePair.getValue().toString();
                        injectProperty(properties, pluginName + "." + keyValuePair.getKey(), value);
                    }
                } else
                {
                    PluginKeyBundles miscPluginKeyBundles = new PluginKeyBundles(pluginProperties);
                    for (Entry<Object, Object> keyValuePair : pluginProperties.entrySet())
                    {
                        String value = keyValuePair.getValue().toString();
                        String key = keyValuePair.getKey().toString();
                        if (keysOfKeyLists.contains(key) == false)
                        {
                            injectProperty(properties, key, value);
                        }
                    }
                    pluginKeyBundles.add(miscPluginKeyBundles);
                }
                logger.log(LogLevel.INFO, "Plugin " + plugin + " added.");
            }
        }
        pluginKeyBundles.addOrReplaceKeyBundleIn(properties);
    }

    private void injectProperty(Properties properties, String key, String value)
    {
        String property = properties.getProperty(key);
        if (property != null && property.trim().equals(DELETE_KEY_WORD))
        {
            properties.remove(key);
        } else
        {
            properties.setProperty(key, value);
        }
    }
    
    private Set<String> getDisabledPlugins(Properties properties)
    {
        Set<String> set = new HashSet<String>();
        String property = properties.getProperty(DISABLED_CORE_PLUGINS_KEY);
        if (StringUtils.isNotBlank(property))
        {
            String[] splittedProperty = property.split(",");
            for (String term : splittedProperty)
            {
                set.add(term.trim());
            }
        }
        return set;
    }

    private Map<PluginType, Map<String, DssCorePlugin>> scanForCorePlugins(
            String corePluginsFolderPath, Set<String> disabledPlugins, Set<String> pluginNames)
    {
        Map<PluginType, Map<String, DssCorePlugin>> typeToPluginsMap =
                new LinkedHashMap<CorePluginsInjector.PluginType, Map<String, DssCorePlugin>>();
        CorePluginScanner scanner =
                new CorePluginScanner(corePluginsFolderPath, CorePluginScanner.ScannerType.DSS,
                        logger);
        List<CorePlugin> plugins = scanner.scanForPlugins();
        for (CorePlugin corePlugin : plugins)
        {
            String technology = corePlugin.getName();
            File dssFolder =
                    new File(corePluginsFolderPath, technology + "/" + corePlugin.getVersion()
                            + "/" + CorePluginScanner.ScannerType.DSS.getSubFolderName());
            PluginType[] values = PluginType.values();
            for (PluginType pluginType : values)
            {
                File file = new File(dssFolder, pluginType.getSubFolderName());
                if (file.isDirectory())
                {
                    File[] pluginFolders = file.listFiles(new FilenameFilter()
                        {
                            public boolean accept(File dir, String name)
                            {
                                return name.startsWith(".") == false;
                            }
                        });
                    for (File pluginFolder : pluginFolders)
                    {
                        String pluginName = pluginFolder.getName();
                        assertAndAddPluginName(pluginName, pluginNames, pluginType);
                        DssCorePlugin plugin =
                                new DssCorePlugin(technology, pluginType, pluginFolder);
                        String fullPluginName = plugin.getName();
                        if (isDisabled(disabledPlugins, fullPluginName) == false)
                        {
                            Map<String, DssCorePlugin> map = typeToPluginsMap.get(pluginType);
                            if (map == null)
                            {
                                map = new LinkedHashMap<String, CorePluginsInjector.DssCorePlugin>();
                                typeToPluginsMap.put(pluginType, map);
                            }
                            map.put(pluginName, plugin);
                        }
                    }
                }
            }
        }
        return typeToPluginsMap;
    }
    
    private boolean isDisabled(Set<String> disabledPlugins, String fullPluginName)
    {
        for (String disabledPlugin : disabledPlugins)
        {
            if (fullPluginName.startsWith(disabledPlugin))
            {
                return true;
            }
        }
        return false;
    }

    private void assertAndAddPluginName(String pluginName, Set<String> pluginNames,
            PluginType pluginType)
    {
        for (int i = 0; i < UNALLOWED_PLUGIN_NAME_CHARACTERS.length(); i++)
        {
            char c = UNALLOWED_PLUGIN_NAME_CHARACTERS.charAt(i);
            if (pluginName.contains(Character.toString(c)))
            {
                throw new EnvironmentFailureException("Plugin name contains '" + c + "': "
                        + pluginName);
            }
        }
        if (pluginType.isUniquePluginNameRequired())
        {
            if (pluginNames.contains(pluginName))
            {
                throw new ConfigurationFailureException(
                        "There is already a plugin named '" + pluginName + "'.");
            }
            pluginNames.add(pluginName);
        }
    }

    /**
     * Load plugin properties file where all references to script names are replaced by script paths.
     */
    private Properties getPluginProperties(File definingFolder)
    {
        File pluginPropertiesFile = new File(definingFolder, PLUGIN_PROPERTIES_FILE_NAME);
        if (pluginPropertiesFile.exists() == false)
        {
            throw new EnvironmentFailureException("Missing plugin properties: "
                    + pluginPropertiesFile);
        }
        File[] scripts = definingFolder.listFiles(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return name.equals(PLUGIN_PROPERTIES_FILE_NAME) == false;
                }
            });
        Properties pluginProperties = loadProperties(pluginPropertiesFile);
        for (Entry<Object, Object> keyValuePair : pluginProperties.entrySet())
        {
            String value = keyValuePair.getValue().toString();
            for (File script : scripts)
            {
                value = value.replace(script.getName(), script.getPath());
            }
            keyValuePair.setValue(value);
        }
        return pluginProperties;
    }

    private Properties loadProperties(File pluginPropertiesFile)
    {
        Properties pluginProperties = new Properties();
        FileInputStream inStream = null;
        try
        {
            inStream = new FileInputStream(pluginPropertiesFile);
            pluginProperties.load(inStream);
        } catch (IOException ex)
        {
            throw new EnvironmentFailureException("Couldn't load plugin properties '"
                    + pluginPropertiesFile + "'.", ex);
        } finally
        {
            IOUtils.closeQuietly(inStream);
        }
        return pluginProperties;
    }

    private static final class PluginKeyBundles
    {
        private Map<PluginType, KeyBundle> keyBundles = new LinkedHashMap<PluginType, KeyBundle>();

        PluginKeyBundles(Properties properties)
        {
            PluginType[] values = PluginType.values();
            for (PluginType pluginType : values)
            {
                String key = pluginType.getKeyOfKeyListPropertyOrNull();
                if (key != null)
                {
                    keyBundles.put(pluginType, new KeyBundle(properties, key));
                }
            }
        }
        
        public void add(PluginKeyBundles bundles)
        {
            Set<Entry<PluginType, KeyBundle>> entrySet = keyBundles.entrySet();
            for (Entry<PluginType, KeyBundle> entry : entrySet)
            {
                PluginType pluginType = entry.getKey();
                KeyBundle keyBundle = bundles.keyBundles.get(pluginType);
                if (keyBundle != null)
                {
                    entry.getValue().keys.addAll(keyBundle.keys);
                }
            }
        }

        void addAndCheckUniquePluginNames(Set<String> pluginNames)
        {
            for (KeyBundle keyBundle : keyBundles.values())
            {
                keyBundle.addAndCheckUniquePluginNames(pluginNames);
            }
        }
        
        void addPluginNameFor(PluginType pluginType, String pluginName)
        {
            KeyBundle keyBundle = keyBundles.get(pluginType);
            if (keyBundle != null)
            {
                keyBundle.addKey(pluginName);
            }
        }
        
        void addOrReplaceKeyBundleIn(Properties properties)
        {
            for (KeyBundle bundle : keyBundles.values())
            {
                bundle.addOrReplaceKeyBundleIn(properties);
            }
        }
    }
    
    private static final class KeyBundle
    {
        private final String key;
        private final Set<String> keys;

        KeyBundle(Properties properties, String key)
        {
            this.key = key;
            keys = new TreeSet<String>();
            String keysAsString = properties.getProperty(key);
            if (keysAsString != null)
            {
                String[] keyArray = PropertyParametersUtil.parseItemisedProperty(keysAsString, key);
                keys.addAll(Arrays.asList(keyArray));
            }
        }
        
        public void addKey(String newKey)
        {
            keys.add(newKey);
        }

        void addAndCheckUniquePluginNames(Set<String> pluginNames)
        {
            for (String keyPrefix : keys)
            {
                if (pluginNames.contains(keyPrefix))
                {
                    throw new ConfigurationFailureException("Property key '" + keyPrefix
                            + "' for key list '" + key
                            + "' is already defined in some other key list.");
                }
                pluginNames.add(keyPrefix);
            }
        }
        
        void addOrReplaceKeyBundleIn(Properties properties)
        {
            if (keys.isEmpty() == false)
            {
                StringBuilder builder = new StringBuilder();
                for (String k : keys)
                {
                    if (builder.length() > 0)
                    {
                        builder.append(", ");
                    }
                    builder.append(k);
                }
                properties.setProperty(key, builder.toString());
            }
        }
    }

    private static final class DssCorePlugin
    {
        private final String name;
        private final File definingFolder;

        DssCorePlugin(String technology, PluginType pluginType, File definingFolder)
        {
            name = technology + ":" + pluginType.getSubFolderName() + ":" + definingFolder.getName();
            this.definingFolder = definingFolder;
            if (definingFolder.isDirectory() == false)
            {
                throw new EnvironmentFailureException("Is not a directory: " + definingFolder);
            }
        }

        String getName()
        {
            return name;
        }

        File getDefiningFolder()
        {
            return definingFolder;
        }

        @Override
        public String toString()
        {
            return name + " [" + definingFolder + "]";
        }
        
    }
}
