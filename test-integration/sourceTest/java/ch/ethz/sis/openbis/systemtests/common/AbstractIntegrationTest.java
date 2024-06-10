/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.systemtests.common;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.remoting.rmi.CodebaseAwareObjectInputStream;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import ch.ethz.sis.afs.manager.TransactionConnection;
import ch.ethz.sis.afsserver.server.observer.impl.DummyServerObserver;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.openbis.generic.OpenBIS;
import ch.ethz.sis.openbis.generic.server.asapi.v3.TransactionConfiguration;
import ch.ethz.sis.shared.startup.Configuration;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;

/**
 * @author pkupczyk
 */
public abstract class AbstractIntegrationTest
{
    public static final String TEST_TRANSACTION_COORDINATOR_KEY = "integration-test-transaction-coordinator-key";

    public static final String TEST_INTERACTIVE_SESSION_KEY = "integration-test-interactive-session-key";

    public static final String USER = "test";

    public static final String PASSWORD = "password";

    private static Server applicationServerProxy;

    private static ProxyInterceptor applicationServerProxyInterceptor;

    private static Server applicationServer;

    protected static GenericWebApplicationContext applicationServerSpringContext;

    private static Server afsServerProxy;

    private static ProxyInterceptor afsServerProxyInterceptor;

    private static ch.ethz.sis.afsserver.server.Server<TransactionConnection, Object> afsServer;

    @BeforeSuite
    public void beforeSuite() throws Exception
    {
        initLogging();

        cleanupApplicationServerFolders();
        cleanupAfsServerFolders();

        startApplicationServer(true);
        startApplicationServerProxy();
        startAfsServer();
        startAfsServerProxy();
    }

    @AfterSuite
    public void afterSuite() throws Exception
    {
        shutdownApplicationServer();
        shutdownApplicationServerProxy();
        shutdownAfsServer();
        shutdownAfsServerProxy();
    }

    @BeforeMethod
    public void beforeMethod(Method method)
    {
        log("BEFORE " + method.getDeclaringClass().getName() + "." + method.getName());
        setApplicationServerProxyInterceptor(null);
        setAfsServerProxyInterceptor(null);
    }

    @AfterMethod
    public void afterMethod(Method method) throws Exception
    {
        log("AFTER  " + method.getDeclaringClass().getName() + "." + method.getName());
    }

    private void initLogging()
    {
        System.setProperty("log4j.configuration", "etc/as/log4j1.xml");
        System.setProperty("log4j.configurationFile", "etc/as/log4j1.xml");
    }

    private void cleanupApplicationServerFolders() throws Exception
    {
        Properties configuration = getApplicationServerConfiguration(true);

        String transactionLogFolder = configuration.getProperty(TransactionConfiguration.TRANSACTION_LOG_FOLDER_PATH_PROPERTY_NAME);
        cleanupFolderSafely(transactionLogFolder);
    }

    private void cleanupAfsServerFolders() throws Exception
    {
        Configuration configuration = getAfsServerConfiguration();

        String writeAheadLogFolder = configuration.getStringProperty(AtomicFileSystemServerParameter.writeAheadLogRoot);
        cleanupFolderSafely(writeAheadLogFolder);

        String storageRoot = configuration.getStringProperty(AtomicFileSystemServerParameter.storageRoot);
        cleanupFolderSafely(storageRoot);
    }

    private void cleanupFolderSafely(String folderPath) throws Exception
    {
        if (!new File(folderPath).exists())
        {
            return;
        }

        File safetyRoot = new File("../").getCanonicalFile();
        File folderParent = new File(folderPath).getCanonicalFile();

        while (folderParent != null && !Files.isSameFile(safetyRoot.toPath(), folderParent.toPath()))
        {
            folderParent = folderParent.getParentFile();
        }

        if (folderParent == null)
        {
            throw new RuntimeException(
                    "Folder " + new File(folderPath).getAbsolutePath() + " is outside of " + safetyRoot.getAbsolutePath()
                            + " therefore cannot be safely deleted.");
        } else
        {
            FileUtilities.deleteRecursively(new File(folderPath));
            log("Deleted folder: " + new File(folderPath).getAbsolutePath());
        }
    }

    private void startApplicationServer(boolean createDatabase) throws Exception
    {
        log("Starting application server.");
        Properties configuration = getApplicationServerConfiguration(createDatabase);

        for (Object key : configuration.keySet())
        {
            Object value = configuration.get(key);
            System.setProperty(String.valueOf(key), String.valueOf(value));
        }

        Server server = new Server();
        HttpConfiguration httpConfig = new HttpConfiguration();
        ServerConnector connector =
                new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        connector.setPort(TestInstanceHostUtils.getOpenBISPort());
        server.addConnector(connector);
        DispatcherServlet dispatcherServlet = new DispatcherServlet()
        {
            private static final long serialVersionUID = 1L;

            @Override
            protected WebApplicationContext findWebApplicationContext()
            {
                XmlBeanFactory beanFactory =
                        new XmlBeanFactory(new FileSystemResource("../server-application-server/resource/server/spring-servlet.xml"));
                applicationServerSpringContext = new GenericWebApplicationContext(beanFactory);
                applicationServerSpringContext.setParent(new ClassPathXmlApplicationContext("classpath:applicationContext.xml"));
                applicationServerSpringContext.refresh();
                return applicationServerSpringContext;
            }
        };
        ServletContextHandler servletContext =
                new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        servletContext.addServlet(new ServletHolder(dispatcherServlet), "/*");
        server.start();

        AbstractIntegrationTest.applicationServer = server;
        log("Started application server.");
    }

    private void startApplicationServerProxy() throws Exception
    {
        log("Starting application server proxy.");
        Server server = new Server();
        HttpConfiguration httpConfig = new HttpConfiguration();
        ServerConnector connector =
                new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        connector.setPort(TestInstanceHostUtils.getOpenBISProxyPort());
        server.addConnector(connector);
        ProxyServlet proxyServlet = new ProxyServlet.Transparent()
        {
            @Override protected void service(final HttpServletRequest request, final HttpServletResponse response)
            {
                try
                {
                    ProxyRequest proxyRequest = new ProxyRequest(request);

                    CodebaseAwareObjectInputStream objectInputStream =
                            new CodebaseAwareObjectInputStream(proxyRequest.getInputStream(), getClass().getClassLoader(), true);
                    RemoteInvocation remoteInvocation = (RemoteInvocation) objectInputStream.readObject();

                    System.out.println(
                            "[AS PROXY] url: " + proxyRequest.getRequestURL() + ", method: " + remoteInvocation.getMethodName() + ", parameters: "
                                    + Arrays.toString(
                                    remoteInvocation.getArguments()));

                    if (applicationServerProxyInterceptor != null)
                    {
                        applicationServerProxyInterceptor.invoke(remoteInvocation.getMethodName(), () ->
                        {
                            super.service(proxyRequest, response);
                            return null;
                        });
                    } else
                    {
                        super.service(proxyRequest, response);
                    }
                } catch (Exception e)
                {
                    System.out.println("[AS PROXY] failed");
                    throw new RuntimeException(e);
                }
            }
        };
        ServletHolder proxyServletHolder = new ServletHolder(proxyServlet);
        proxyServletHolder.setInitParameter("proxyTo", TestInstanceHostUtils.getOpenBISUrl() + "/");
        ServletContextHandler servletContext =
                new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        servletContext.addServlet(proxyServletHolder, "/*");
        server.start();

        AbstractIntegrationTest.applicationServerProxy = server;
        log("Started application server proxy.");
    }

    private void startAfsServer() throws Exception
    {
        log("Starting afs server.");
        Configuration configuration = getAfsServerConfiguration();
        DummyServerObserver dummyServerObserver = new DummyServerObserver();

        AbstractIntegrationTest.afsServer = new ch.ethz.sis.afsserver.server.Server<>(configuration, dummyServerObserver, dummyServerObserver);
        log("Started afs server.");
    }

    private void startAfsServerProxy() throws Exception
    {
        log("Starting afs server proxy.");
        Server server = new Server();
        HttpConfiguration httpConfig = new HttpConfiguration();
        ServerConnector connector =
                new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        connector.setPort(TestInstanceHostUtils.getAFSProxyPort());
        server.addConnector(connector);
        ProxyServlet proxyServlet = new ProxyServlet.Transparent()
        {
            @Override protected void service(final HttpServletRequest request, final HttpServletResponse response)
            {
                try
                {
                    ProxyRequest proxyRequest = new ProxyRequest(request);

                    Map<String, String> parameters = new HashMap<>();

                    if (HttpMethod.GET.is(proxyRequest.getMethod()))
                    {
                        Iterator<String> iterator = proxyRequest.getParameterNames().asIterator();
                        while (iterator.hasNext())
                        {
                            String name = iterator.next();
                            parameters.put(name, proxyRequest.getParameter(name));
                        }
                    } else if (HttpMethod.POST.is(proxyRequest.getMethod()))
                    {
                        String parametersString = IOUtils.toString(proxyRequest.getInputStream(), StandardCharsets.UTF_8);
                        List<NameValuePair> parametersList = URLEncodedUtils.parse(parametersString, StandardCharsets.UTF_8);
                        for (NameValuePair parameterItem : parametersList)
                        {
                            parameters.put(parameterItem.getName(), parameterItem.getValue());
                        }
                    }

                    System.out.println(
                            "[AFS PROXY] url: " + proxyRequest.getRequestURL() + ", method: " + parameters.get("method") + ", parameters: "
                                    + parameters);

                    if (afsServerProxyInterceptor != null)
                    {
                        afsServerProxyInterceptor.invoke(parameters.get("method"), () ->
                        {
                            super.service(proxyRequest, response);
                            return null;
                        });
                    } else
                    {
                        super.service(proxyRequest, response);
                    }
                } catch (Exception e)
                {
                    System.out.println("[AFS PROXY] failed");
                    throw new RuntimeException(e);
                }
            }
        };
        ServletHolder proxyServletHolder = new ServletHolder(proxyServlet);
        proxyServletHolder.setInitParameter("proxyTo", TestInstanceHostUtils.getAFSUrl());
        ServletContextHandler servletContext =
                new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        servletContext.addServlet(proxyServletHolder, "/*");
        server.start();

        AbstractIntegrationTest.afsServerProxy = server;
        log("Started afs server proxy.");
    }

    private void shutdownApplicationServer() throws Exception
    {
        // manually destroy EHCache (without it the new AS won't start in the same VM)
        applicationServerSpringContext.getBean(EhCacheManagerFactoryBean.class).destroy();
        applicationServer.stop();
        log("Shut down application server.");
    }

    private void shutdownApplicationServerProxy()
    {
        applicationServerProxy.setStopAtShutdown(true);
        log("Shut down application server proxy.");
    }

    private void shutdownAfsServer() throws Exception
    {
        afsServer.shutdown(false);
        log("Shut down afs server.");
    }

    private void shutdownAfsServerProxy()
    {
        afsServerProxy.setStopAtShutdown(true);
        log("Shut down afs server proxy.");
    }

    public void restartApplicationServer() throws Exception
    {
        log("Restarting application server.");
        shutdownApplicationServer();
        startApplicationServer(false);
    }

    public void restartAfsServer() throws Exception
    {
        log("Restarting afs server.");
        shutdownAfsServer();
        startAfsServer();
    }

    private Properties getApplicationServerConfiguration(boolean createDatabase) throws Exception
    {
        Properties configuration = new Properties();
        configuration.load(new FileInputStream("etc/as/service.properties"));
        configuration.setProperty("database.create-from-scratch", String.valueOf(createDatabase));
        configuration.setProperty("database.kind", "integration");
        configuration.setProperty("script-folder", "../server-application-server/source");
        configuration.setProperty(TransactionConfiguration.COORDINATOR_KEY_PROPERTY_NAME, TEST_TRANSACTION_COORDINATOR_KEY);
        configuration.setProperty(TransactionConfiguration.INTERACTIVE_SESSION_KEY_PROPERTY_NAME, TEST_INTERACTIVE_SESSION_KEY);
        configuration.setProperty(TransactionConfiguration.TRANSACTION_LOG_FOLDER_PATH_PROPERTY_NAME, "./targets/transaction-logs");
        configuration.setProperty(TransactionConfiguration.TRANSACTION_TIMEOUT_PROPERTY_NAME, "5");
        configuration.setProperty(TransactionConfiguration.FINISH_TRANSACTIONS_INTERVAL_PROPERTY_NAME, "1");
        configuration.setProperty(TransactionConfiguration.APPLICATION_SERVER_URL_PROPERTY_NAME, TestInstanceHostUtils.getOpenBISProxyUrl());
        configuration.setProperty(TransactionConfiguration.AFS_SERVER_URL_PROPERTY_NAME,
                TestInstanceHostUtils.getAFSProxyUrl() + TestInstanceHostUtils.getAFSPath());
        return configuration;
    }

    private Configuration getAfsServerConfiguration()
    {
        Configuration configuration = new Configuration(List.of(AtomicFileSystemServerParameter.class),
                "etc/afs/service.properties");
        configuration.setProperty(AtomicFileSystemServerParameter.logConfigFile, "etc/afs/log4j2.xml");
        configuration.setProperty(AtomicFileSystemServerParameter.writeAheadLogRoot, "./targets/afs/transaction-logs");
        configuration.setProperty(AtomicFileSystemServerParameter.storageRoot, "./targets/afs/storage");
        configuration.setProperty(AtomicFileSystemServerParameter.apiServerTransactionManagerKey, TEST_TRANSACTION_COORDINATOR_KEY);
        configuration.setProperty(AtomicFileSystemServerParameter.apiServerInteractiveSessionKey, TEST_INTERACTIVE_SESSION_KEY);
        configuration.setProperty(AtomicFileSystemServerParameter.httpServerPort, String.valueOf(TestInstanceHostUtils.getAFSPort()));
        configuration.setProperty(AtomicFileSystemServerParameter.httpServerUri, TestInstanceHostUtils.getAFSPath());
        return configuration;
    }

    public static void setApplicationServerProxyInterceptor(
            final ProxyInterceptor applicationServerProxyInterceptor)
    {
        AbstractIntegrationTest.applicationServerProxyInterceptor = applicationServerProxyInterceptor;
    }

    public static void setAfsServerProxyInterceptor(final ProxyInterceptor afsServerProxyInterceptor)
    {
        AbstractIntegrationTest.afsServerProxyInterceptor = afsServerProxyInterceptor;
    }

    public interface ProxyInterceptor
    {
        void invoke(String method, Callable<Void> defaultAction) throws Exception;
    }

    private static class ProxyRequest extends HttpServletRequestWrapper
    {
        private boolean read;

        private byte[] bytes;

        public ProxyRequest(final HttpServletRequest request)
        {
            super(request);
        }

        @Override public ServletInputStream getInputStream() throws IOException
        {
            if (!read)
            {
                bytes = IOUtils.toByteArray(super.getInputStream());
                read = true;
            }
            return new ServletInputStream()
            {
                private final ByteArrayInputStream bytesStream = new ByteArrayInputStream(bytes);

                @Override public int read()
                {
                    return bytesStream.read();
                }

                @Override public boolean isReady()
                {
                    return true;
                }

                @Override public boolean isFinished()
                {
                    return bytesStream.available() == 0;
                }

                @Override public void setReadListener(final ReadListener readListener)
                {
                }

            };
        }
    }

    public static OpenBIS createOpenBIS()
    {
        return new OpenBIS(TestInstanceHostUtils.getOpenBISUrl() + TestInstanceHostUtils.getOpenBISPath(),
                TestInstanceHostUtils.getDSSUrl() + TestInstanceHostUtils.getDSSPath(),
                TestInstanceHostUtils.getAFSUrl() + TestInstanceHostUtils.getAFSPath());
    }

    public void log(String message)
    {
        System.out.println("[TEST] " + message);
    }

}
