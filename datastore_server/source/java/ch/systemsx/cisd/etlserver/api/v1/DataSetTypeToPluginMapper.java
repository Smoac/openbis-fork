/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.api.v1;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;

import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.etlserver.IETLServerPlugin;
import ch.systemsx.cisd.etlserver.Parameters;
import ch.systemsx.cisd.etlserver.ThreadParameters;

/**
 * Utility class the maps between data set types (strings) and IETLServerPlugin instances. Made
 * public to aid tests, but is really package internal.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class DataSetTypeToPluginMapper
{
    // The default plugin is either the one explicitly specified in the properties file, or,
    // otherwise, the first-defined thread
    private final IETLServerPlugin defaultPlugin;

    private final HashMap<String, IETLServerPlugin> pluginMap;

    private static final String DSS_RPC_SECTION_KEY = "dss-rpc";

    private static final String DEFAULT_THREAD_KEY = "put-default";

    private static final String PUT_SECTION_KEY = "put";

    /**
     * Constructor for testing purposes. Should not be used otherwise.
     * 
     * @param plugin
     */
    protected DataSetTypeToPluginMapper(IETLServerPlugin plugin)
    {
        defaultPlugin = plugin;
        pluginMap = new HashMap<String, IETLServerPlugin>();
    }

    DataSetTypeToPluginMapper(Parameters params)
    {
        DataSetTypeToPluginMapperInitializer initializer =
                new DataSetTypeToPluginMapperInitializer(params);
        initializer.initialize();
        defaultPlugin = initializer.getDefaultPlugin();
        pluginMap = initializer.getPluginMap();
    }

    public IETLServerPlugin getPluginForType(String dataSetTypeOrNull)
    {
        if (null == dataSetTypeOrNull)
        {
            return defaultPlugin;
        }
        IETLServerPlugin plugin = pluginMap.get(dataSetTypeOrNull);
        return (null == plugin) ? defaultPlugin : plugin;
    }

    public void initializeStoreRootDirectory(File storeDirectory)
    {
        defaultPlugin.getStorageProcessor().setStoreRootDirectory(storeDirectory);
        for (IETLServerPlugin plugin : pluginMap.values())
        {
            plugin.getStorageProcessor().setStoreRootDirectory(storeDirectory);
        }
    }

    private class DataSetTypeToPluginMapperInitializer
    {
        private final Parameters params;

        private HashMap<String, ThreadParameters> threadParamMap;

        private ExtendedProperties section;

        DataSetTypeToPluginMapperInitializer(Parameters params)
        {
            this.params = params;
        }

        public void initialize()
        {
            initializeThreadMap();
            initializeSectionProperties();
        }

        public IETLServerPlugin getDefaultPlugin()
        {
            ThreadParameters[] threadParams = params.getThreads();
            ThreadParameters firstThread = threadParams[0];

            String defaultThreadName = section.getProperty(DEFAULT_THREAD_KEY);
            if (null == defaultThreadName)
            {
                return firstThread.getPlugin();
            }

            ThreadParameters defaultThread = threadParamMap.get(defaultThreadName);
            if (null == defaultThread)
            {
                return firstThread.getPlugin();
            }

            return defaultThread.getPlugin();
        }

        public HashMap<String, IETLServerPlugin> getPluginMap()
        {
            HashMap<String, IETLServerPlugin> map = new HashMap<String, IETLServerPlugin>();

            Properties putSection = section.getSubset(PUT_SECTION_KEY + ".", true);

            for (Object keyObject : putSection.keySet())
            {
                String key = (String) keyObject;
                String threadName = putSection.getProperty(key);
                ThreadParameters threadParams = threadParamMap.get(threadName);
                if (null != threadParams)
                {
                    map.put(key.toUpperCase(), threadParams.getPlugin());
                }
            }
            return map;
        }

        private void initializeThreadMap()
        {
            threadParamMap = new HashMap<String, ThreadParameters>();
            ThreadParameters[] threadParams = params.getThreads();
            for (ThreadParameters threadParam : threadParams)
            {
                threadParamMap.put(threadParam.getThreadName(), threadParam);
            }
        }

        private void initializeSectionProperties()
        {
            section =
                    ExtendedProperties.createWith(params.getProperties()).getSubset(
                            DSS_RPC_SECTION_KEY + ".", true);
        }
    }
}
