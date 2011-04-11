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

import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.etlserver.ETLDaemon;
import ch.systemsx.cisd.etlserver.IETLServerPlugin;
import ch.systemsx.cisd.etlserver.ITopLevelDataSetRegistrator;
import ch.systemsx.cisd.etlserver.Parameters;
import ch.systemsx.cisd.etlserver.ThreadParameters;
import ch.systemsx.cisd.etlserver.validation.IDataSetValidator;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;

/**
 * Utility class the maps between data set types (strings) and IETLServerPlugin instances. Made
 * public to aid tests, but is really package internal.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class DataSetTypeToRegistratorMapper
{
    // The default plugin is either the one explicitly specified in the properties file, or,
    // otherwise, the first-defined thread
    private final ITopLevelDataSetRegistrator defaultHandler;

    private final HashMap<String, ITopLevelDataSetRegistrator> handlerMap;

    private static final String DSS_RPC_SECTION_KEY = "dss-rpc";

    private static final String DEFAULT_THREAD_KEY = "put-default";

    private static final String PUT_SECTION_KEY = "put";

    /**
     * Constructor for testing purposes. Should not be used otherwise.
     * 
     * @param plugin
     */
    protected DataSetTypeToRegistratorMapper(ITopLevelDataSetRegistrator plugin)
    {
        defaultHandler = plugin;
        handlerMap = new HashMap<String, ITopLevelDataSetRegistrator>();
    }

    DataSetTypeToRegistratorMapper(Parameters params, String shareId,
            IEncapsulatedOpenBISService openBISService, IMailClient mailClient,
            IDataSetValidator dataSetValidator)
    {
        DataSetTypeToTopLevelHandlerMapperInitializer initializer =
                new DataSetTypeToTopLevelHandlerMapperInitializer(params, shareId, openBISService,
                        mailClient, dataSetValidator);
        initializer.initialize();
        defaultHandler = initializer.getDefaultHandler();
        handlerMap = initializer.getHandlerMap();
    }

    public ITopLevelDataSetRegistrator getRegistratorForType(String dataSetTypeOrNull)
    {
        if (null == dataSetTypeOrNull)
        {
            return defaultHandler;
        }
        ITopLevelDataSetRegistrator plugin = handlerMap.get(dataSetTypeOrNull);
        return (null == plugin) ? defaultHandler : plugin;
    }

    public void initializeStoreRootDirectory(File storeDirectory)
    {
        initializeStoreRootDirectory(storeDirectory, defaultHandler);
        for (ITopLevelDataSetRegistrator handler : handlerMap.values())
        {
            initializeStoreRootDirectory(storeDirectory, handler);
        }
    }

    private void initializeStoreRootDirectory(File storeDirectory,
            ITopLevelDataSetRegistrator registrator)
    {
        if (registrator instanceof PutDataSetServerPluginHolder)
        {
            IETLServerPlugin plugin = ((PutDataSetServerPluginHolder) registrator).getPlugin();
            plugin.getStorageProcessor().setStoreRootDirectory(storeDirectory);
        }
    }

    private class DataSetTypeToTopLevelHandlerMapperInitializer
    {
        private final Parameters params;

        private final String shareId;

        private final IEncapsulatedOpenBISService openBISService;

        private final IMailClient mailClient;

        private final IDataSetValidator dataSetValidator;

        private final HashMap<String, ThreadParameters> threadParamMap;

        private ExtendedProperties section;

        /**
         * @param params
         * @param shareId
         * @param openBISService
         * @param mailClient
         * @param dataSetValidator
         */
        public DataSetTypeToTopLevelHandlerMapperInitializer(Parameters params, String shareId,
                IEncapsulatedOpenBISService openBISService, IMailClient mailClient,
                IDataSetValidator dataSetValidator)
        {
            super();
            this.params = params;
            this.shareId = shareId;
            this.openBISService = openBISService;
            this.mailClient = mailClient;
            this.dataSetValidator = dataSetValidator;
            threadParamMap = new HashMap<String, ThreadParameters>();
        }

        public void initialize()
        {
            initializeThreadMap();
            initializeSectionProperties();
        }

        public ITopLevelDataSetRegistrator getDefaultHandler()
        {
            ThreadParameters[] threadParams = params.getThreads();
            ThreadParameters firstThread = threadParams[0];

            String defaultThreadName = section.getProperty(DEFAULT_THREAD_KEY);
            ThreadParameters defaultThread =
                    (null != defaultThreadName) ? threadParamMap.get(defaultThreadName) : null;
            if (null == defaultThread)
            {
                return ETLDaemon.createTopLevelDataSetRegistrator(params.getProperties(),
                        firstThread, shareId, openBISService, mailClient, dataSetValidator, false,
                        false, false, firstThread.tryGetPreRegistrationScript(),
                        firstThread.tryGetPostRegistrationScript(),
                        firstThread.tryValidationScript(), PutDataSetServerPluginHolder.class);
            }

            return ETLDaemon.createTopLevelDataSetRegistrator(params.getProperties(),
                    defaultThread, shareId, openBISService, mailClient, dataSetValidator, false,
                    false, false, firstThread.tryGetPreRegistrationScript(),
                    firstThread.tryGetPostRegistrationScript(), firstThread.tryValidationScript(),
                    PutDataSetServerPluginHolder.class);
        }

        public HashMap<String, ITopLevelDataSetRegistrator> getHandlerMap()
        {
            HashMap<String, ITopLevelDataSetRegistrator> map =
                    new HashMap<String, ITopLevelDataSetRegistrator>();

            Properties putSection = section.getSubset(PUT_SECTION_KEY + ".", true);

            for (Object keyObject : putSection.keySet())
            {
                String key = (String) keyObject;
                String threadName = putSection.getProperty(key);
                ThreadParameters threadParams = threadParamMap.get(threadName);
                if (null != threadParams)
                {
                    map.put(key.toUpperCase(), ETLDaemon.createTopLevelDataSetRegistrator(
                            params.getProperties(), threadParams, shareId, openBISService,
                            mailClient, dataSetValidator, false));
                }
            }
            return map;
        }

        private void initializeThreadMap()
        {
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
