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
import java.util.Properties;

import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.ConfigParameters;

/**
 * @author Kaloyan Enimanev
 */
public class FtpServerConfig
{
    private final static String PREFIX = "ftp.server.";

    private final static String ENABLE_KEY = PREFIX + "enable";

    private final static String PORT_KEY = PREFIX + "port";

    private final static String USE_SSL_KEY = PREFIX + "use-ssl";

    private final static String MAX_THREADS_KEY = PREFIX + "maxThreads";

    private final static String DATASET_DISPLAY_TEMPLATE_KEY = PREFIX + "dataset.display.template";

    private static final int DEFAULT_PORT = 2121;

    private static final boolean DEFAULT_USE_SSL = false;

    private static final int DEFAULT_MAX_THREADS = 25;

    private static final String DEFAULT_DATASET_TEMPLATE = "${dataSetCode}";

    private boolean startServer;

    private int port;

    private boolean useSSL;

    private File keyStore;

    private String keyPassword;

    private String keyStorePassword;

    private String dataSetDisplayTemplate;

    private int maxThreads;

    public FtpServerConfig(Properties props) {
        this.startServer = PropertyUtils.getBoolean(props, ENABLE_KEY, false);
        if (startServer)
        {
            initializeProperties(props);
        }
    }

    private void initializeProperties(Properties props)
    {
        this.port = PropertyUtils.getPosInt(props, PORT_KEY, DEFAULT_PORT);
        this.useSSL = PropertyUtils.getBoolean(props, USE_SSL_KEY, DEFAULT_USE_SSL);
        if (useSSL)
        {
            initializeSSLProperties(props);
        }
        maxThreads = PropertyUtils.getPosInt(props, MAX_THREADS_KEY, DEFAULT_MAX_THREADS);
        dataSetDisplayTemplate =
                PropertyUtils.getProperty(props, DATASET_DISPLAY_TEMPLATE_KEY, DEFAULT_DATASET_TEMPLATE);
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

}
