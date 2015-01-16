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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import ch.ethz.cisd.hotdeploy.PluginContainer;
import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.io.PropertyIOUtils;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.string.Template;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginScanner.ScannerType;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginsInjector;
import ch.systemsx.cisd.openbis.generic.shared.coreplugin.CorePluginsUtils;

/**
 * Utility class to load properties.
 * 
 * @author Tomasz Pylak
 */
public class DssPropertyParametersUtil
{

    /** Prefix of system properties which may override service.properties. */
    public static final String OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX = "openbis.dss.";

    public static final String DSS_CODE_KEY = "data-store-server-code";

    public static final String STOREROOT_DIR_KEY = "storeroot-dir";

    public static final String DOWNLOAD_URL_KEY = "download-url";

    public static final String SERVER_URL_KEY = "server-url";

    public static final String DATA_STREAM_TIMEOUT = "data-stream-timeout";

    public static final String DATA_STREAM_MAX_TIMEOUT = "data-stream-max-timeout";

    public static final int MINIMUM_TIME_TO_KEEP_STREAMS_DEFAULT = 5;

    public static final int MAXIMUM_TIME_TO_KEEP_STREAMS_DEFAULT = 60 * 60 * 4; // 4 hours

    /**
     * Temp directory for dss usage.
     */
    static final String DSS_TEMP_DIR_PATH = "dss-temp-dir";

    @Private
    static final String EMPTY_TEST_FILE_NAME = "an-empty-test-file";

    /**
     * Directory for registration log files.
     */
    public static final String DSS_REGISTRATION_LOG_DIR_PATH = "dss-registration-log-dir";

    /**
     * Directory for recovery state files.
     */
    public static final String DSS_RECOVERY_STATE_DIR_PATH = "dss-recovery-state-dir";

    /** Location of service properties file. */
    public static final String SERVICE_PROPERTIES_FILE = "etc/service.properties";

    private static final String EXPLANATION =
            "Please make sure this directory exists on the local file system and is writable "
                    + "by the data store server or provide such a directory "
                    + "by the configuration parameter '${path-key}'.";

    private static final Template NON_EXISTING_DIR_TEMPLATE = new Template(
            "Could not create ${dir-description} at path: ${path}. " + EXPLANATION);

    private static final Template NON_LOCAL_DIR_TEMPLATE = new Template(
            "Directory at path '${path}' is not on the local file system. " + EXPLANATION);

    /** loads server configuration */
    public static ExtendedProperties loadServiceProperties()
    {
        ExtendedProperties properties = loadProperties(SERVICE_PROPERTIES_FILE);
        CorePluginsUtils.addCorePluginsProperties(properties, ScannerType.DSS);
        ExtendedProperties serviceProperties = extendProperties(properties);
        CorePluginsInjector injector =
                new CorePluginsInjector(ScannerType.DSS, DssPluginType.values());
        Map<String, File> pluginFolders = injector.injectCorePlugins(serviceProperties);

        if (PluginContainer.tryGetInstance() == null)
        {
            PluginContainer.initHotDeployment();
        }

        for (String name : pluginFolders.keySet())
        {
            File mainFolder = pluginFolders.get(name);
            File hotDeployFolder = new File(mainFolder, "plugin");
            if (hotDeployFolder.exists() && hotDeployFolder.isDirectory()
                    && PluginContainer.tryGetInstance(name) == null)
            {
                PluginContainer pluginContainer = PluginContainer.initHotDeployment(name);
                pluginContainer.addPluginDirectory(hotDeployFolder);
                pluginContainer.refresh(true);
            }
        }

        return serviceProperties;
    }

    public static ExtendedProperties loadProperties(String filePath)
    {
        return extendProperties(PropertyIOUtils.loadProperties(filePath));
    }

    static ExtendedProperties extendProperties(Properties properties)
    {
        Properties systemProperties = System.getProperties();
        ExtendedProperties dssSystemProperties =
                ExtendedProperties.getSubset(systemProperties,
                        OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX, true);
        Set<Entry<Object, Object>> entrySet = dssSystemProperties.entrySet();
        for (Entry<Object, Object> entry : entrySet)
        {
            properties.put(entry.getKey(), entry.getValue());
        }
        return ExtendedProperties.createWith(properties);
    }

    public static String getDataStoreCode(Properties serviceProperties)
    {
        return PropertyUtils.getMandatoryProperty(serviceProperties, DSS_CODE_KEY).toUpperCase();
    }

    public final static File getStoreRootDir(final Properties properties)
    {
        return FileUtilities.normalizeFile(new File(PropertyUtils.getMandatoryProperty(properties,
                STOREROOT_DIR_KEY)));
    }

    public static String getOpenBisServerUrl(Properties serviceProperties)
    {
        return PropertyUtils.getMandatoryProperty(serviceProperties, SERVER_URL_KEY);
    }

    public static String getDownloadUrl(Properties serviceProperties)
    {
        return PropertyUtils.getMandatoryProperty(serviceProperties, DOWNLOAD_URL_KEY);
    }

    public static int getDataStreamTimeout(Properties serviceProperties)
    {
        return PropertyUtils.getPosInt(serviceProperties, DATA_STREAM_TIMEOUT,
                MINIMUM_TIME_TO_KEEP_STREAMS_DEFAULT);
    }

    public static int getDataStreamMaxTimeout(Properties serviceProperties)
    {
        return PropertyUtils.getPosInt(serviceProperties, DATA_STREAM_MAX_TIMEOUT,
                MAXIMUM_TIME_TO_KEEP_STREAMS_DEFAULT);
    }

    public static File getDssInternalTempDir(final Properties properties)
    {
        return getDssInternalTempDir(FileOperations.getInstance(), properties);
    }

    @Private
    static File getDssInternalTempDir(IFileOperations fileOperations, final Properties properties)
    {
        return getDir(fileOperations, properties, "dss-tmp",
                "an internal temp directory for the data store server", DSS_TEMP_DIR_PATH);
    }

    public static File getDssRegistrationLogDir(final Properties properties)
    {
        return getDssRegistrationLogDir(FileOperations.getInstance(), properties);
    }

    @Private
    static File getDssRegistrationLogDir(IFileOperations fileOperations, final Properties properties)
    {
        return getDir(fileOperations, properties, "log-registrations",
                "a directory for storing registration logs", DSS_REGISTRATION_LOG_DIR_PATH);
    }

    public static File getDssRecoveryStateDir(final Properties properties)
    {
        return getDssRecoveryStateDir(FileOperations.getInstance(), properties);
    }

    @Private
    static File getDssRecoveryStateDir(IFileOperations fileOperations, final Properties properties)
    {
        return getDir(fileOperations, properties, "recovery-state",
                "a directory for storing recovery state for the dss", DSS_RECOVERY_STATE_DIR_PATH);
    }

    private static File getDir(IFileOperations fileOperations, final Properties properties,
            String defaultDirName, String dirDescription, String pathKey)
    {
        String defaultRegistrationLogDirPath =
                new File(System.getProperty("user.dir"), defaultDirName).getAbsolutePath();
        String registrationLogDirPath =
                PropertyUtils.getProperty(properties, pathKey, defaultRegistrationLogDirPath);
        File registrationLogDir = new File(registrationLogDirPath);
        fileOperations.mkdirs(registrationLogDir);
        assertDirExistsAndIsLocal(fileOperations, registrationLogDir, dirDescription, pathKey);
        return registrationLogDir;
    }

    private static AtomicInteger atomicEmptyFileIndex = new AtomicInteger();

    private static void assertDirExistsAndIsLocal(IFileOperations fileOperations, File dir,
            String dirDescription, String pathKey)
    {
        String threadSafeEmptyTestFileName = EMPTY_TEST_FILE_NAME + atomicEmptyFileIndex.incrementAndGet();
        File threadSafeEmptyTestFile = null;
        File emptyTestFileInDir = null;

        try
        {
            assertDirExists(fileOperations, dir, dirDescription, pathKey);
            threadSafeEmptyTestFile = new File(threadSafeEmptyTestFileName);
            fileOperations.createNewFile(threadSafeEmptyTestFile);
            emptyTestFileInDir = new File(dir, threadSafeEmptyTestFileName);
            if (fileOperations.rename(threadSafeEmptyTestFile, emptyTestFileInDir) == false)
            {
                throw createException(NON_LOCAL_DIR_TEMPLATE.createFreshCopy(), dir,
                        dirDescription, pathKey);
            }
        } finally
        {
            if (threadSafeEmptyTestFile != null)
            {
                fileOperations.delete(threadSafeEmptyTestFile);
            }
            if (emptyTestFileInDir != null)
            {
                fileOperations.delete(emptyTestFileInDir);
            }
        }
    }

    private static void assertDirExists(IFileOperations fileOperations, File dir,
            String dirDescription, String pathKey)
    {
        if (fileOperations.exists(dir) == false)
        {
            throw createException(NON_EXISTING_DIR_TEMPLATE.createFreshCopy(), dir, dirDescription,
                    pathKey);
        }
    }

    private static ConfigurationFailureException createException(Template template, File dir,
            String dirName, String pathKey)
    {
        template.attemptToBind("dir-description", dirName);
        template.bind("path", dir.getPath());
        template.bind("path-key", pathKey);
        ConfigurationFailureException e = new ConfigurationFailureException(template.createText());
        return e;
    }
}
