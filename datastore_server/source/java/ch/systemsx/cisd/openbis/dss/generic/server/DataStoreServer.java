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

import static ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants.DATA_STORE_SERVER_SERVICE_NAME;
import static ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.WebApplicationContext;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.openbis.dss.generic.server.ConfigParameters.PluginServlet;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.PropertyParametersUtil;
import ch.systemsx.cisd.openbis.generic.shared.IServer;

/**
 * Main class of the service. Starts up jetty with {@link DatasetDownloadServlet}.
 * 
 * @author Franz-Josef Elmer
 */
public class DataStoreServer
{

    /** Part of the URL of the DSS RPC service. */
    public static final String DATA_STORE_SERVER_RPC_SERVICE_NAME =
            DATA_STORE_SERVER_WEB_APPLICATION_NAME + "/rpc";

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
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getMessage());
            } finally
            {
                LocaleContextHolder.resetLocaleContext();
            }
        }
    }

    /**
     * A more generic version of the DataStoreServlet above.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    // TODO Refactor to make the reference to DataStoreServlet use the HttpInvokerServlet.
    private static final class HttpInvokerServlet extends HttpServlet
    {
        private static final long serialVersionUID = 1L;

        private final HttpRequestHandler target;

        private final String canonicalPath;

        HttpInvokerServlet(HttpRequestHandler target, String canonicalPath)
        {
            this.target = target;
            this.canonicalPath = canonicalPath;
        }

        @Override
        public void init() throws ServletException
        {
            operationLog.info("HTTP invoker-based RPC service available at " + canonicalPath);
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
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, ex.getMessage());
            } finally
            {
                LocaleContextHolder.resetLocaleContext();
            }
        }
    }

    static final String APPLICATION_CONTEXT_KEY = "application-context";

    private static final String PREFIX = "data-set-download.";

    private static final int PREFIX_LENGTH = PREFIX.length();

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DataStoreServer.class);

    private static Server server;

    public static final void start()
    {
        assert server == null : "Server already started";
        final ConfigParameters configParameters = getConfigParameters();
        IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();
        final ApplicationContext applicationContext =
                new ApplicationContext(openBISService, configParameters);
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
        initializeServer(configParameters, port, thisServer);
        initializeContext(applicationContext, configParameters, thisServer);
        return thisServer;
    }

    private static void initializeServer(final ConfigParameters configParameters, final int port,
            final Server thisServer)
    {
        final SocketConnector socketConnector = createSocketConnector(configParameters);
        socketConnector.setPort(port);
        socketConnector.setMaxIdleTime(30000);
        thisServer.addConnector(socketConnector);
    }

    private static void initializeContext(final ApplicationContext applicationContext,
            final ConfigParameters configParameters, final Server thisServer)
    {
        final Context context = new Context(thisServer, "/", Context.SESSIONS);
        context.setAttribute(APPLICATION_CONTEXT_KEY, applicationContext);
        context.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                ServiceProvider.APPLICATION_CONTEXT);
        String applicationName = "/" + DATA_STORE_SERVER_WEB_APPLICATION_NAME;
        context.addServlet(new ServletHolder(new DataStoreServlet()), "/"
                + DATA_STORE_SERVER_SERVICE_NAME + "/*");
        context.addServlet(DatasetDownloadServlet.class, applicationName + "/*");

        String rpcPath = "/" + DATA_STORE_SERVER_RPC_SERVICE_NAME + "/v1";
        context.addServlet(new ServletHolder(new HttpInvokerServlet(ServiceProvider
                .getDssServiceRpcV1(), rpcPath)), rpcPath);

        registerPluginServlets(context, configParameters.getPluginServlets());
    }

    private static void registerPluginServlets(Context context, List<PluginServlet> pluginServlets)
    {
        for (PluginServlet pluginServlet : pluginServlets)
        {
            Class<?> classInstance;
            try
            {
                classInstance = Class.forName(pluginServlet.getServletClass());
            } catch (ClassNotFoundException ex)
            {
                throw EnvironmentFailureException.fromTemplate(
                        "Error while loading servlet plugin class '%s': %s", pluginServlet
                                .getClass(), ex.getMessage());
            }
            ServletHolder holder =
                    context.addServlet(classInstance, pluginServlet.getServletPath());
            // Add any additional parameters to the init parameters
            holder.setInitParameters(pluginServlet.getServletProperties());
        }
    }

    private static SocketConnector createSocketConnector(ConfigParameters configParameters)
    {
        if (configParameters.isUseSSL())
        {
            SslSocketConnector socketConnector = new SslSocketConnector();
            socketConnector.setKeystore(configParameters.getKeystorePath());
            socketConnector.setPassword(configParameters.getKeystorePassword());
            socketConnector.setKeyPassword(configParameters.getKeystoreKeyPassword());
            return socketConnector;
        } else
        {
            operationLog.warn("creating connector to openBIS without SSL");
            return new SocketConnector();
        }
    }

    private final static void selfTest(final ApplicationContext applicationContext)
    {
        IEncapsulatedOpenBISService dataSetService = applicationContext.getDataSetService();
        final int version = dataSetService.getVersion();
        if (IServer.VERSION != version)
        {
            throw new ConfigurationFailureException(
                    "This client has the wrong service version for the server (client: "
                            + IServer.VERSION + ", server: " + version + ").");
        }
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("openBIS service (interface version " + version + ") is reachable");
        }
    }

    final static ConfigParameters getConfigParameters()
    {
        Properties properties;
        try
        {
            properties = PropertyParametersUtil.loadServiceProperties();
        } catch (ConfigurationFailureException ex)
        {
            properties = new Properties();
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
        final ConfigParameters configParameters =
                new ConfigParameters(ExtendedProperties.createWith(properties));
        configParameters.log();
        return configParameters;
    }
}
