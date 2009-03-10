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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.IWebService;

/**
 * Main class of the service. Starts up jetty with {@link DatasetDownloadServlet}.
 * 
 * @author Franz-Josef Elmer
 */
public class DatasetDownloadService
{
    private static final class DataStoreServlet extends HttpServlet
    {
        private static final long serialVersionUID = 1L;

        private HttpRequestHandler target;

        @Override
        public void init() throws ServletException
        {
            target = ServiceProvider.getDataStoreServer();
        }

        // Code copied from org.springframework.web.context.support.HttpRequestHandlerServlet
        @Override
        protected void service(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException
        {

            LocaleContextHolder.setLocale(request.getLocale());
            try
            {
                this.target.handleRequest(request, response);
            } catch (HttpRequestMethodNotSupportedException ex)
            {
                String[] supportedMethods = ex.getSupportedMethods();
                if (supportedMethods != null)
                {
                    response.setHeader("Allow", StringUtils.arrayToDelimitedString(
                            supportedMethods, ", "));
                }
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex
                        .getMessage());
            } finally
            {
                LocaleContextHolder.resetLocaleContext();
            }
        }
    }

    static final String APPLICATION_CONTEXT_KEY = "application-context";

    private static final String PREFIX = "data-set-download.";

    private static final int PREFIX_LENGTH = PREFIX.length();

    private static final String SERVICE_PROPERTIES_FILE = "etc/service.properties";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DatasetDownloadService.class);

    private static Server server;

    public static final void start()
    {
        assert server == null : "Server already started";
        final ApplicationContext applicationContext = createApplicationContext();
        server = createServer(applicationContext);
        try
        {
            server.start();
            selfTest(applicationContext);
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Data set download server ready on port "
                        + applicationContext.getConfigParameters().getPort());
            }
        } catch (final Exception ex)
        {
            operationLog.error("Failed to start server.", ex);
        }
    }

    public static final void stop()
    {
        assert server != null : "Server has not been started.";
        if (server.isRunning())
        {
            try
            {
                server.stop();
            } catch (final Exception ex)
            {
                operationLog.error("Failed to stop server.", ex);
            }
        }
        server = null;
    }

    public static void main(final String[] args)
    {
        LogInitializer.init();
        start();
    }

    private final static Server createServer(final ApplicationContext applicationContext)
    {
        final ConfigParameters configParameters = applicationContext.getConfigParameters();
        final int port = configParameters.getPort();
        final Server thisServer = new Server();
        final SslSocketConnector socketConnector = new SslSocketConnector();
        socketConnector.setPort(port);
        socketConnector.setMaxIdleTime(30000);
        socketConnector.setKeystore(configParameters.getKeystorePath());
        socketConnector.setPassword(configParameters.getKeystorePassword());
        socketConnector.setKeyPassword(configParameters.getKeystoreKeyPassword());
        thisServer.addConnector(socketConnector);
        final Context context = new Context(thisServer, "/", Context.SESSIONS);
        context.setAttribute(APPLICATION_CONTEXT_KEY, applicationContext);
        String applicationName = "/" + applicationContext.getApplicationName();
        context.addServlet(DatasetDownloadServlet.class, applicationName + "/*");
        context.addServlet(new ServletHolder(new DataStoreServlet()), applicationName + "/dss/*");
        return thisServer;
    }

    private final static void selfTest(final ApplicationContext applicationContext)
    {
        IEncapsulatedOpenBISService dataSetService = applicationContext.getDataSetService();
        final int version = dataSetService.getVersion();
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

    private final static ApplicationContext createApplicationContext()
    {
        final ConfigParameters configParameters = getConfigParameters();
        IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();
        final ApplicationContext applicationContext =
                new ApplicationContext(openBISService, configParameters,
                        "dataset-download");
        return applicationContext;
    }

    private final static ConfigParameters getConfigParameters()
    {
        final Properties properties;
        if (new File(SERVICE_PROPERTIES_FILE).exists() == false)
        {
            properties = new Properties();
        } else
        {
            properties = PropertyUtils.loadProperties(SERVICE_PROPERTIES_FILE);
        }
        final Properties systemProperties = System.getProperties();
        final Enumeration<?> propertyNames = systemProperties.propertyNames();
        while (propertyNames.hasMoreElements())
        {
            final String name = (String) propertyNames.nextElement();
            if (name.startsWith(PREFIX))
            {
                final String value = systemProperties.getProperty(name);
                properties.setProperty(name.substring(PREFIX_LENGTH), value);
            }
        }
        final ConfigParameters configParameters = new ConfigParameters(properties);
        configParameters.log();
        return configParameters;
    }
}
