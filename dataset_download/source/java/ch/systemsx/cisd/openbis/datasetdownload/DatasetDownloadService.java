/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.datasetdownload;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.Context;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.lims.base.IDataSetService;
import ch.systemsx.cisd.lims.base.IWebService;
import ch.systemsx.cisd.lims.base.RMIBasedLIMSServiceFactory;
import ch.systemsx.cisd.lims.base.ServiceRegistry;

/**
 * Main class of the service. Starts up jetty with {@link DatasetDownloadServlet}.
 *
 * @author Franz-Josef Elmer
 */
public class DatasetDownloadService
{
    static final String APPLICATION_CONTEXT_KEY = "application-context";
    private static final String PREFIX = "data-set-download.";
    private static final int PREFIX_LENGTH = PREFIX.length();

    private static final String SERVICE_PROPERTIES_FILE = "etc/service.properties";
    
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DatasetDownloadService.class);

    public static void main(String[] args) throws Exception 
    {
        LogInitializer.init();
        ServiceRegistry.setLIMSServiceFactory(RMIBasedLIMSServiceFactory.INSTANCE);
        
        ApplicationContext applicationContext = createApplicationContext();
        ConfigParameters configParameters = applicationContext.getConfigParameters();
        int port = configParameters.getPort();
        Server server = new Server();
        SslSocketConnector socketConnector = new SslSocketConnector();
        socketConnector.setPort(port);
        socketConnector.setMaxIdleTime(30000);
        socketConnector.setKeystore(configParameters.getKeystorePath());
        socketConnector.setPassword(configParameters.getKeystorePassword());
        socketConnector.setKeyPassword(configParameters.getKeystoreKeyPassword());
        server.addConnector(socketConnector);
        Context context = new Context(server, "/", Context.SESSIONS);
        context.setAttribute(APPLICATION_CONTEXT_KEY, applicationContext);
        context.addServlet(DatasetDownloadServlet.class, "/dataset-download/*");
        server.start();
        
        selfTest(applicationContext);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Data set download server ready on port " + port);
        }
    }
    
    private static void selfTest(ApplicationContext applicationContext)
    {
        int version = applicationContext.getDataSetService().getVersion();
        if (IWebService.VERSION != version)
        {
            throw new ConfigurationFailureException(
                    "This client has the wrong service version for the server (client: "
                            + IWebService.VERSION + ", server: " + version + ").");
        } 
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("openBIS service (interface version " + version + ") is reachable");
        }
    }

    private static ApplicationContext createApplicationContext()
    {
        ConfigParameters configParameters = getConfigParameters();
        IDataSetService dataSetService = new DataSetService(configParameters);
        ApplicationContext applicationContext = new ApplicationContext(dataSetService, configParameters);
        return applicationContext;
    }
    
    private static ConfigParameters getConfigParameters()
    {
        Properties properties = loadProperties();
        Properties systemProperties = System.getProperties();
        Enumeration<?> propertyNames = systemProperties.propertyNames();
        while (propertyNames.hasMoreElements())
        {
            String name = (String) propertyNames.nextElement();
            if (name.startsWith(PREFIX))
            {
                String value = systemProperties.getProperty(name);
                properties.setProperty(name.substring(PREFIX_LENGTH), value);
            }
        }
        return new ConfigParameters(properties);
    }
    
    private static Properties loadProperties()
    {
        
        final Properties properties = new Properties();
        try
        {
            final InputStream is = new FileInputStream(SERVICE_PROPERTIES_FILE);
            try
            {
                properties.load(is);
                return properties;
            } finally
            {
                IOUtils.closeQuietly(is);
            }
        } catch (Exception ex)
        {
            final String msg =
                    "Could not load the service properties from resource '"
                            + SERVICE_PROPERTIES_FILE + "'.";
            operationLog.warn(msg, ex);
            throw new ConfigurationFailureException(msg, ex);
        }

    }
}
