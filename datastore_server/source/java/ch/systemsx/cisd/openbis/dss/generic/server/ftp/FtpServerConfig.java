/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.ConfigParameters;

/**
 * @author Kaloyan Enimanev
 */
public class FtpServerConfig
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FtpServerConfig.class);

    private final static String PREFIX = "ftp.server.";

    final static String ENABLE_KEY = PREFIX + "enable";

    final static String PORT_KEY = PREFIX + "port";

    final static String USE_SSL_KEY = PREFIX + "use-ssl";

    final static String MAX_THREADS_KEY = PREFIX + "maxThreads";

    final static String DATASET_DISPLAY_TEMPLATE_KEY = PREFIX + "dataset.display.template";

    final static String DATASET_FILELIST_SUBPATH_KEY = PREFIX + "dataset.filelist.subpath.";

    final static String DATASET_FILELIST_FILTER_KEY = PREFIX + "dataset.filelist.filter.";

    final static String ACTIVE_MODE_ENABLE_KEY = PREFIX + "activemode.enable";

    final static String ACTIVE_PORT_KEY = PREFIX + "activemode.port";

    private static final int DEFAULT_PORT = 2121;

    private static final int DEFAULT_ACTIVE_PORT = 2122;

    private static final boolean DEFAULT_USE_SSL = true;

    private static final int DEFAULT_MAX_THREADS = 25;

    private static final String DEFAULT_DATASET_TEMPLATE = "${dataSetCode}";

    private boolean startServer;

    private int port;

    private boolean activeModeEnabled;

    private int activePort;

    private boolean useSSL;

    private File keyStore;

    private String keyPassword;

    private String keyStorePassword;

    private String dataSetDisplayTemplate;

    private int maxThreads;

    private Map<String /* dataset type */, String /* path */> fileListSubPaths =
            new HashMap<String, String>();

    private Map<String /* dataset type */, String /* filter pattern */> fileListFilters =
            new HashMap<String, String>();

    public FtpServerConfig(Properties props) {
        this.startServer = PropertyUtils.getBoolean(props, ENABLE_KEY, false);
        if (startServer)
        {
            initializeProperties(props);
        }
    }

    private void initializeProperties(Properties props)
    {
        port = PropertyUtils.getPosInt(props, PORT_KEY, DEFAULT_PORT);
        useSSL = PropertyUtils.getBoolean(props, USE_SSL_KEY, DEFAULT_USE_SSL);
        if (useSSL)
        {
            initializeSSLProperties(props);
        }
        activeModeEnabled = PropertyUtils.getBoolean(props, ACTIVE_MODE_ENABLE_KEY, false);
        activePort = PropertyUtils.getPosInt(props, ACTIVE_PORT_KEY, DEFAULT_ACTIVE_PORT);
        maxThreads = PropertyUtils.getPosInt(props, MAX_THREADS_KEY, DEFAULT_MAX_THREADS);
        dataSetDisplayTemplate =
                PropertyUtils.getProperty(props, DATASET_DISPLAY_TEMPLATE_KEY, DEFAULT_DATASET_TEMPLATE);

        ExtendedProperties fileListSubPathProps =
                ExtendedProperties.getSubset(props, DATASET_FILELIST_SUBPATH_KEY, true);
        for (Object key : fileListSubPathProps.keySet())
        {
            String dataSetType = key.toString();
            String subPath = fileListSubPathProps.getProperty(dataSetType);
            fileListSubPaths.put(dataSetType, subPath);

        }

        ExtendedProperties fileListFilterProps =
                ExtendedProperties.getSubset(props, DATASET_FILELIST_FILTER_KEY, true);
        for (Object key : fileListFilterProps.keySet())
        {
            String dataSetType = key.toString();
            String filter = fileListFilterProps.getProperty(dataSetType);
            fileListFilters.put(dataSetType, filter);
        }

    }

    private void initializeSSLProperties(Properties props)
    {
        String keyStoreFileName =
                PropertyUtils.getMandatoryProperty(props, ConfigParameters.KEYSTORE_PATH_KEY);
        keyStore = new File(keyStoreFileName);
        keyStorePassword =
                PropertyUtils.getMandatoryProperty(props, ConfigParameters.KEYSTORE_PASSWORD_KEY);
        keyPassword =
                PropertyUtils.getMandatoryProperty(props,
                        ConfigParameters.KEYSTORE_KEY_PASSWORD_KEY);
    }

    public boolean isStartServer()
    {
        return startServer;
    }

    public int getPort()
    {
        return port;
    }

    public boolean isUseSSL()
    {
        return useSSL;
    }

    public File getKeyStore()
    {
        return keyStore;
    }

    public String getKeyPassword()
    {
        return keyPassword;
    }

    public String getKeyStorePassword()
    {
        return keyStorePassword;
    }

    public Integer getMaxThreads()
    {
        return maxThreads;
    }

    public String getDataSetDisplayTemplate()
    {
        return dataSetDisplayTemplate;
    }

    public Map<String, String> getFileListSubPaths()
    {
        return Collections.unmodifiableMap(fileListSubPaths);
    }

    public Map<String, String> getFileListFilters()
    {
        return Collections.unmodifiableMap(fileListFilters);
    }

    /**
     * information being logged on FTP server startup.
     */
    public void logStartupInfo()
    {
        operationLog.info("Ftp Server port: " + port);
        operationLog.info("Ftp Server using SSL: " + useSSL);
        operationLog.info("Ftp Server data set display template : " + dataSetDisplayTemplate);
        operationLog.info("Ftp Server enable active mode: " + activeModeEnabled);
        if (activeModeEnabled)
        {
            operationLog.info("Ftp Server active mode port: " + activePort);
        }

        for (Entry<String, String> subpathEntry : fileListSubPaths.entrySet())
        {
            String message =
                    String.format("Ftp Server subpath configuration for data "
                            + "set type '%s' : '%s'", subpathEntry.getKey(),
                            subpathEntry.getValue());
            operationLog.info(message);
        }
        for (Entry<String, String> filterEntry : fileListFilters.entrySet())
        {
            String message =
                    String.format("Ftp Server file filter configuration for data "
                            + "set type '%s' : '%s'", filterEntry.getKey(), filterEntry.getValue());
            operationLog.info(message);
        }
    }

    public boolean isActiveModeEnabled()
    {
        return activeModeEnabled;
    }

    public int getActiveLocalPort()
    {
        return activePort;
    }

}
