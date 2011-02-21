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

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.WebApplicationContext;

import com.marathon.util.spring.StreamSupportingHttpInvokerServiceExporter;

import ch.systemsx.cisd.common.api.IRpcServiceNameServer;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceVersionDTO;
import ch.systemsx.cisd.common.api.server.RpcServiceNameServer;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.openbis.dss.generic.server.ConfigParameters.PluginServlet;
import ch.systemsx.cisd.openbis.dss.generic.server.api.v1.DssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.authorization.internal.DssSessionAuthorizationHolder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.authorization.internal.IDssServiceRpcGenericInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.DataStoreApiUrlUtilities;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.DatasetImageOverviewUtilities;

/**
 * Main class of the service. Starts up jetty with {@link DatasetDownloadServlet}.
 * 
 * @author Franz-Josef Elmer
 */
public class DataStoreServer
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
                    response.setHeader("Allow",
                            StringUtils.arrayToDelimitedString(supportedMethods, ", "));
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

    static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataStoreServer.class);

    private static Server server;

    public static final void start()
    {
        assert server == null : "Server already started";
        final ConfigParameters configParameters = getConfigParameters();
        final IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();
        final ApplicationContext applicationContext =
                new ApplicationContext(openBISService, ServiceProvider.getShareIdManager(),
                        configParameters);
        DssSessionAuthorizationHolder.setAuthorizer(new DatasetSessionAuthorizer(configParameters
                .getAuthCacheExpirationTimeMins(), configParameters
                .getAuthCacheCleanupTimerPeriodMins()));
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
        final Connector socketConnector = createSocketConnector(configParameters);
        socketConnector.setPort(port);
        socketConnector.setMaxIdleTime(30000);
        thisServer.addConnector(socketConnector);
    }

    private static void initializeContext(final ApplicationContext applicationContext,
            final ConfigParameters configParameters, final Server thisServer)
    {
        final ServletContextHandler context =
                new ServletContextHandler(thisServer, "/", ServletContextHandler.SESSIONS);
        context.setAttribute(APPLICATION_CONTEXT_KEY, applicationContext);
        context.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                ServiceProvider.getApplicationContext());
        String applicationName = "/" + DATA_STORE_SERVER_WEB_APPLICATION_NAME;
        context.addServlet(new ServletHolder(new DataStoreServlet()), "/"
                + DATA_STORE_SERVER_SERVICE_NAME + "/*");
        DatasetDownloadServlet.setDownloadUrl(configParameters.getDownloadURL());
        context.addServlet(DatasetDownloadServlet.class, applicationName + "/*");

        initializeRpcServices(context, applicationContext, configParameters);
        registerPluginServlets(context, configParameters.getPluginServlets());
        registerImageOverviewServlet(context, configParameters);
    }

    /**
     * Initialize RPC service interfaces
     */
    // TODO 2010-06-01, CR : The registration process here needs to be made cleaner.
    // Perhaps by using Spring and the dssApplicationContext.xml more effectively, or perhaps by
    // using annotations and reflection.
    private static void initializeRpcServices(final ServletContextHandler context,
            final ApplicationContext applicationContext, final ConfigParameters configParameters)
    {
        // Get the spring bean and do some additional configuration
        StreamSupportingHttpInvokerServiceExporter v1ServiceExporter =
                ServiceProvider.getDssServiceRpcGeneric();
        IDssServiceRpcGenericInternal service =
                (IDssServiceRpcGenericInternal) v1ServiceExporter.getService();
        service.setStoreDirectory(applicationContext.getConfigParameters().getStorePath());
        service.setIncomingDirectory(applicationContext.getConfigParameters()
                .getRpcIncomingDirectory());

        // Export the spring bean to the world by wrapping it in an HttpInvokerServlet
        String rpcV1Suffix = "/rmi-dss-api-v1";
        String rpcV1Path = DataStoreApiUrlUtilities.getUrlForRpcService(rpcV1Suffix);
        context.addServlet(new ServletHolder(new HttpInvokerServlet(v1ServiceExporter, rpcV1Path)),
                rpcV1Path);

        HttpInvokerServiceExporter nameServiceExporter =
                ServiceProvider.getRpcNameServiceExporter();
        String nameServerPath =
                DataStoreApiUrlUtilities
                        .getUrlForRpcService(IRpcServiceNameServer.PREFFERED_URL_SUFFIX);
        context.addServlet(new ServletHolder(new HttpInvokerServlet(nameServiceExporter,
                nameServerPath)), nameServerPath);

        RpcServiceInterfaceVersionDTO nameServerVersion =
                new RpcServiceInterfaceVersionDTO(IRpcServiceNameServer.PREFFERED_SERVICE_NAME,
                        IRpcServiceNameServer.PREFFERED_URL_SUFFIX, 1, 0);

        // Inform the name server about the services I export
        // N.b. In the future, this could be done using spring instead of programmatically
        RpcServiceNameServer rpcNameServer =
                (RpcServiceNameServer) nameServiceExporter.getService();

        RpcServiceInterfaceVersionDTO v1Interface =
                new RpcServiceInterfaceVersionDTO(DssServiceRpcGeneric.DSS_SERVICE_NAME,
                        rpcV1Suffix, 1, 0);
        rpcNameServer.addSupportedInterfaceVersion(v1Interface);
        rpcNameServer.addSupportedInterfaceVersion(nameServerVersion);
    }

    @SuppressWarnings("unchecked")
    private static void registerPluginServlets(ServletContextHandler context,
            List<PluginServlet> pluginServlets)
    {
        for (PluginServlet pluginServlet : pluginServlets)
        {
            Class<? extends Servlet> classInstance;
            try
            {
                classInstance =
                        (Class<? extends Servlet>) Class.forName(pluginServlet.getServletClass());
            } catch (ClassNotFoundException ex)
            {
                throw EnvironmentFailureException.fromTemplate(
                        "Error while loading servlet plugin class '%s': %s",
                        pluginServlet.getClass(), ex.getMessage());
            } catch (ClassCastException ex)
            {
                throw EnvironmentFailureException.fromTemplate(
                        "Error while loading servlet plugin class '%s': %s. "
                                + "Servlet implementation expected.", pluginServlet.getClass(),
                        ex.getMessage());
            }
            ServletHolder holder =
                    context.addServlet(classInstance, pluginServlet.getServletPath());
            // Add any additional parameters to the init parameters
            holder.setInitParameters(pluginServlet.getServletProperties());
        }
    }

    private static void registerImageOverviewServlet(ServletContextHandler context,
            ConfigParameters configParameters)
    {
        DatasetImageOverviewServlet.initConfiguration(configParameters.getProperties());
        context.addServlet(DatasetImageOverviewServlet.class, "/"
                + DatasetImageOverviewUtilities.SERVLET_NAME + "/*");
    }

    private static Connector createSocketConnector(ConfigParameters configParameters)
    {
        if (configParameters.isUseSSL())
        {
            final SslConnector socketConnector =
                    configParameters.isUseNIO() ? new SslSelectChannelConnector()
                            : new SslSocketConnector();
            socketConnector.setKeystore(configParameters.getKeystorePath());
            socketConnector.setPassword(configParameters.getKeystorePassword());
            socketConnector.setKeyPassword(configParameters.getKeystoreKeyPassword());
            return socketConnector;
        } else
        {
            operationLog.warn("creating connector to openBIS without SSL");
            return configParameters.isUseNIO() ? new SelectChannelConnector()
                    : new SocketConnector();
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
            properties = DssPropertyParametersUtil.loadServiceProperties();
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
