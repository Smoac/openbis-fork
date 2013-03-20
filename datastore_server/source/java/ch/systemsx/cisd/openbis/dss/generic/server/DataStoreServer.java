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

import static ch.systemsx.cisd.openbis.generic.shared.basic.GenericSharedConstants.DATA_STORE_SERVER_SERVICE_NAME;
import static ch.systemsx.cisd.openbis.generic.shared.basic.GenericSharedConstants.DATA_STORE_SERVER_WEB_APPLICATION_NAME;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.servlet.DispatcherType;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
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

import com.googlecode.jsonrpc4j.spring.JsonServiceExporter;
import com.marathon.util.spring.StreamSupportingHttpInvokerServiceExporter;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.api.IRpcServiceNameServer;
import ch.systemsx.cisd.common.api.RpcServiceInterfaceVersionDTO;
import ch.systemsx.cisd.common.api.retry.RetryCaller;
import ch.systemsx.cisd.common.api.retry.config.RetryConfiguration;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.resource.IInitializable;
import ch.systemsx.cisd.openbis.common.api.server.RpcServiceNameServer;
import ch.systemsx.cisd.openbis.common.conversation.manager.IServiceConversationClientManagerRemote;
import ch.systemsx.cisd.openbis.common.conversation.manager.IServiceConversationServerManagerRemote;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.DssSessionAuthorizationHolder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.IDssServiceRpcGenericInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.DataStoreApiUrlUtilities;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.PluginServletConfig;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssCrossOriginFilter;
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

    private static final String UPLOAD_GUI_SERVING_SERVLET_PATH = "/dss_upload_gui";

    private static ConfigParameters configParameters;

    public static final void start()
    {
        assert server == null : "Server already started";
        ConfigParameters configParams = getConfigParameters();
        final IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();
        OpenbisSessionTokenCache sessionTokenCache =
                (OpenbisSessionTokenCache) ServiceProvider.getApplicationContext().getBean(
                        "as-session-token-cache");
        final ApplicationContext applicationContext =
                new ApplicationContext(openBISService, sessionTokenCache,
                        ServiceProvider.getShareIdManager(),
                        ServiceProvider.getHierarchicalContentProvider(), configParams);
        DssSessionAuthorizationHolder.setAuthorizer(new DatasetSessionAuthorizer(configParams
                .getAuthCacheExpirationTimeMins(), configParams
                .getAuthCacheCleanupTimerPeriodMins()));
        configParams.log();
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
            ((IInitializable) ServiceProvider.getApplicationContext().getBean("data-store-service"))
                    .initialize();
        } catch (final Exception ex)
        {
            operationLog.error("Failed to start server.", ex);
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
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
        final ConfigParameters configParams = applicationContext.getConfigParameters();
        final int port = configParams.getPort();
        final Server thisServer = new Server();
        initializeServer(configParams, port, thisServer);
        initializeContext(applicationContext, configParams, thisServer);
        return thisServer;
    }

    private static void initializeServer(final ConfigParameters configParams, final int port,
            final Server thisServer)
    {
        final Connector socketConnector = createSocketConnector(configParams);
        socketConnector.setPort(port);
        socketConnector.setMaxIdleTime(300000);
        thisServer.addConnector(socketConnector);
    }

    private static void initializeContext(final ApplicationContext applicationContext,
            final ConfigParameters configParams, final Server thisServer)
    {
        // Create a handler collection for grouping together the handlers
        ContextHandlerCollection contextHandlers = new ContextHandlerCollection();
        thisServer.setHandler(contextHandlers);

        // Register the handler that returns the webstart jars
        registerDssUploadClientHandler(thisServer, contextHandlers, configParams);

        ServletContextHandler servletContextHandler =
                new ServletContextHandler(contextHandlers, "/", ServletContextHandler.SESSIONS);
        servletContextHandler.setAttribute(APPLICATION_CONTEXT_KEY, applicationContext);
        servletContextHandler.setAttribute(
                WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                ServiceProvider.getApplicationContext());
        // Disable URL rewriting (forces container to stop appending ";jsessionid=xxx" to urls)
        // to avoid mistakes in URL parsing by download servlets
        servletContextHandler.getSessionHandler().getSessionManager()
                .setSessionIdPathParameterName(null);
        String applicationName = "/" + DATA_STORE_SERVER_WEB_APPLICATION_NAME;
        servletContextHandler.addServlet(new ServletHolder(new DataStoreServlet()), "/"
                + DATA_STORE_SERVER_SERVICE_NAME + "/*");
        DatasetDownloadServlet.setDownloadUrl(configParams.getDownloadURL());
        servletContextHandler.addServlet(DatasetDownloadServlet.class, applicationName + "/*");

        servletContextHandler.addServlet(SessionWorkspaceFileUploadServlet.class, applicationName
                + "/session_workspace_file_upload");
        servletContextHandler.addServlet(SessionWorkspaceFileDownloadServlet.class, applicationName
                + "/session_workspace_file_download");

        initializeRpcServices(servletContextHandler, applicationContext, configParams);
        registerPluginServlets(servletContextHandler, configParams.getPluginServlets());
        registerImageOverviewServlet(servletContextHandler, configParams);
        registerStreamHandlingServlet(servletContextHandler);
    }

    /**
     * Initialize RPC service interfaces
     */
    // TODO 2010-06-01, CR : The registration process here needs to be made cleaner.
    // Perhaps by using Spring and the dssApplicationContext.xml more effectively, or perhaps by
    // using annotations and reflection.
    private static void initializeRpcServices(final ServletContextHandler context,
            final ApplicationContext applicationContext, ConfigParameters configParams)
    {
        // Get the spring bean and do some additional configuration
        StreamSupportingHttpInvokerServiceExporter v1ServiceExporter =
                ServiceProvider.getDssServiceRpcGeneric();
        IDssServiceRpcGenericInternal service =
                (IDssServiceRpcGenericInternal) v1ServiceExporter.getService();
        service.setStoreDirectory(applicationContext.getConfigParameters().getStorePath());

        // Export the spring bean to the world by wrapping it in an HttpInvokerServlet
        String rpcV1Suffix = "/rmi-dss-api-v1";
        String rpcV1Path = DataStoreApiUrlUtilities.getUrlForRpcService(rpcV1Suffix);
        context.addServlet(new ServletHolder(new HttpInvokerServlet(v1ServiceExporter, rpcV1Path)),
                rpcV1Path);

        // Export service conversation client manager
        String clientPath =
                DataStoreApiUrlUtilities
                        .getUrlForRpcService(IServiceConversationClientManagerRemote.PATH);
        context.addServlet(
                new ServletHolder(new HttpInvokerServlet(ServiceProvider
                        .getServiceConversationClientManagerServer(), clientPath)), clientPath);

        // Export service conversation server manager
        String serverPath =
                DataStoreApiUrlUtilities
                        .getUrlForRpcService(IServiceConversationServerManagerRemote.PATH);
        context.addServlet(
                new ServletHolder(new HttpInvokerServlet(ServiceProvider
                        .getServiceConversationServerManagerServer(), serverPath)), serverPath);

        //
        // export the API via JSON
        //
        String jsonRpcV1Suffix = rpcV1Suffix + ".json";
        String jsonRpcV1Path = DataStoreApiUrlUtilities.getUrlForRpcService(jsonRpcV1Suffix);
        JsonServiceExporter jsonV1ServiceExporter = new JsonServiceExporter();
        jsonV1ServiceExporter.setService(service);
        jsonV1ServiceExporter.setServiceInterface(IDssServiceRpcGeneric.class);
        jsonV1ServiceExporter
                .setApplicationContext((org.springframework.context.ApplicationContext) ServiceProvider
                        .getApplicationContext());
        try
        {
            jsonV1ServiceExporter.afterPropertiesSet();
        } catch (Exception ex)
        {
            throw new RuntimeException("Cannot initialize json-rpc service exporter:"
                    + ex.getMessage(), ex);
        }

        context.addServlet(new ServletHolder(new HttpInvokerServlet(jsonV1ServiceExporter,
                jsonRpcV1Path)), jsonRpcV1Path);
        context.addFilter(DssCrossOriginFilter.class, "/*", EnumSet.allOf(DispatcherType.class));

        HttpInvokerServiceExporter nameServiceExporter =
                ServiceProvider.getRpcNameServiceExporter();
        String nameServerPath =
                DataStoreApiUrlUtilities
                        .getUrlForRpcService(IRpcServiceNameServer.PREFFERED_URL_SUFFIX);
        context.addServlet(new ServletHolder(new HttpInvokerServlet(nameServiceExporter,
                nameServerPath)), nameServerPath);

        // Inform the name server about the services I export
        // N.b. In the future, this could be done using spring instead of programmatically
        RpcServiceNameServer rpcNameServer =
                (RpcServiceNameServer) nameServiceExporter.getService();

        RpcServiceInterfaceVersionDTO nameServerVersion =
                new RpcServiceInterfaceVersionDTO(IRpcServiceNameServer.PREFFERED_SERVICE_NAME,
                        IRpcServiceNameServer.PREFFERED_URL_SUFFIX,
                        rpcNameServer.getMajorVersion(), rpcNameServer.getMinorVersion());
        RpcServiceInterfaceVersionDTO v1Interface =
                new RpcServiceInterfaceVersionDTO(IDssServiceRpcGeneric.DSS_SERVICE_NAME,
                        rpcV1Suffix, service.getMajorVersion(), service.getMinorVersion());
        RpcServiceInterfaceVersionDTO jsonV1Interface =
                new RpcServiceInterfaceVersionDTO(IDssServiceRpcGeneric.DSS_SERVICE_NAME,
                        jsonRpcV1Suffix, service.getMajorVersion(), service.getMinorVersion());

        rpcNameServer.addSupportedInterfaceVersion(nameServerVersion);
        rpcNameServer.addSupportedInterfaceVersion(v1Interface);
        rpcNameServer.addSupportedInterfaceVersion(jsonV1Interface);
    }

    @SuppressWarnings("unchecked")
    private static void registerPluginServlets(ServletContextHandler context,
            List<PluginServletConfig> pluginServlets)
    {
        for (PluginServletConfig pluginServlet : pluginServlets)
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
            ConfigParameters configParams)
    {
        DatasetImageOverviewServlet.initConfiguration(configParams.getProperties());
        context.addServlet(DatasetImageOverviewServlet.class, "/"
                + DatasetImageOverviewUtilities.SERVLET_NAME + "/*");
    }

    private static void registerStreamHandlingServlet(ServletContextHandler context)
    {
        context.addServlet(IdentifiedStreamHandlingServlet.class, "/"
                + DATA_STORE_SERVER_WEB_APPLICATION_NAME + "/"
                + IdentifiedStreamHandlingServlet.SERVLET_NAME + "/*");
    }

    private static void registerDssUploadClientHandler(Server thisServer,
            ContextHandlerCollection context, ConfigParameters configParams)
    {
        String servletPathSuffix = UPLOAD_GUI_SERVING_SERVLET_PATH;
        // Map this resource to a name that is accessible from outside
        String servletPath = DataStoreApiUrlUtilities.getUrlForRpcService(servletPathSuffix);

        ContextHandler webstartContextHandler = new ContextHandler(context, servletPath);

        // The resource base should refer to the folder that contains the jars for the web start
        // client.
        // This is the value assigned to the ${dss_upload_gui} variable in dss/build.xml .
        // We have set this up to be the same as the servletPathSuffix.
        webstartContextHandler.setResourceBase(configParams.getWebstartJarPath()
                + servletPathSuffix);
        // Add a resource handler to the webstart jar path to serve files from the file system.
        ResourceHandler webstartJarHandler = new ResourceHandler();
        webstartContextHandler.setHandler(webstartJarHandler);
    }

    private static Connector createSocketConnector(ConfigParameters configParams)
    {
        if (configParams.isUseSSL())
        {
            final SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePath(configParams.getKeystorePath());
            sslContextFactory.setKeyStorePassword(configParams.getKeystorePassword());
            sslContextFactory.setKeyManagerPassword(configParams.getKeystoreKeyPassword());
            final SslConnector socketConnector =
                    configParams.isUseNIO() ? new SslSelectChannelConnector(sslContextFactory)
                            : new SslSocketConnector(sslContextFactory);
            return socketConnector;
        } else
        {
            operationLog.warn("creating connector to openBIS without SSL");
            return configParams.isUseNIO() ? new SelectChannelConnector() : new SocketConnector();
        }
    }
    
    private static class RetryingSelfTest extends RetryCaller<Void, RuntimeException>
    {
        private final ApplicationContext applicationContext;
        
        RetryingSelfTest(ApplicationContext applicationContext)
        {
            super(new RetryConfiguration()
                {
                    @Override
                    public float getWaitingTimeBetweenRetriesIncreasingFactor()
                    {
                        return 2;
                    }
                    
                    @Override
                    public int getWaitingTimeBetweenRetries()
                    {
                        return 5000;
                    }
                    
                    @Override
                    public int getMaximumNumberOfRetries()
                    {
                        return 5;
                    }
                }, new Log4jSimpleLogger(operationLog));
            this.applicationContext = applicationContext;
        }

        @Override
        protected Void call() throws RuntimeException
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
            return null;
        }
    }

    private final static void selfTest(final ApplicationContext applicationContext)
    {
        new RetryingSelfTest(applicationContext).callWithRetry();
    }

    public static ConfigParameters getConfigParameters()
    {
        if (configParameters == null)
        {
            Properties properties = DssPropertyParametersUtil.loadServiceProperties();
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
            configParameters = new ConfigParameters(ExtendedProperties.createWith(properties));
        }
        return configParameters;
    }
}
