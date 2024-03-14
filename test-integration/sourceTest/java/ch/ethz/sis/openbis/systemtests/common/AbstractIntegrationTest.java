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

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
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
import ch.ethz.sis.openbis.generic.server.asapi.v3.TransactionConfiguration;
import ch.ethz.sis.shared.startup.Configuration;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;

/**
 * @author pkupczyk
 */
public abstract class AbstractIntegrationTest
{
    public static final String TEST_TRANSACTION_COORDINATOR_KEY = "integration-test-transaction-coordinator-key";

    public static final String TEST_INTERACTIVE_SESSION_KEY = "integration-test-interactive-session-key";

    private static Server applicationServer;

    private static ch.ethz.sis.afsserver.server.Server<TransactionConnection, Object> afsServer;

    protected static GenericWebApplicationContext applicationServerSpringContext;

    protected static IntegrationTestLogger logger = new IntegrationTestLogger();

    @BeforeSuite
    public void beforeSuite() throws Exception
    {
        LogInitializer.init();

        cleanupApplicationServerFolders();
        cleanupAfsServerFolders();

        startApplicationServer();
        startAfsServer();
    }

    @AfterSuite
    public void afterSuite() throws Exception
    {
        shutdownApplicationServer();
        shutdownAfsServer();
    }

    private void shutdownApplicationServer()
    {
        applicationServer.setStopAtShutdown(true);
        logger.log("Shut down application server.");
    }

    private void shutdownAfsServer() throws Exception
    {
        afsServer.shutdown(false);
        logger.log("Shut down afs server.");
    }

    @BeforeMethod
    public void beforeTest(Method method)
    {
        logger.log("BEFORE " + method.getDeclaringClass().getName() + "." + method.getName());
    }

    @AfterMethod
    public void afterTest(Method method)
    {
        logger.log("AFTER  " + method.getDeclaringClass().getName() + "." + method.getName());
    }

    private void cleanupApplicationServerFolders() throws Exception
    {
        Properties configuration = getApplicationServerConfiguration();

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
            logger.log("Deleted folder: " + new File(folderPath).getAbsolutePath());
        }
    }

    private void startApplicationServer() throws Exception
    {
        Properties configuration = getApplicationServerConfiguration();

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
    }

    private void startAfsServer() throws Exception
    {
        Configuration configuration = getAfsServerConfiguration();
        DummyServerObserver dummyServerObserver = new DummyServerObserver();

        AbstractIntegrationTest.afsServer = new ch.ethz.sis.afsserver.server.Server<>(configuration, dummyServerObserver, dummyServerObserver);
    }

    private Properties getApplicationServerConfiguration() throws Exception
    {
        Properties configuration = new Properties();
        configuration.load(new FileInputStream("../server-application-server/source/java/service.properties"));
        configuration.setProperty("database.create-from-scratch", "true");
        configuration.setProperty("database.kind", "integration");
        configuration.setProperty("script-folder", "../server-application-server/source");
        configuration.setProperty(TransactionConfiguration.COORDINATOR_KEY_PROPERTY_NAME, TEST_TRANSACTION_COORDINATOR_KEY);
        configuration.setProperty(TransactionConfiguration.INTERACTIVE_SESSION_KEY_PROPERTY_NAME, TEST_INTERACTIVE_SESSION_KEY);
        configuration.setProperty(TransactionConfiguration.TRANSACTION_LOG_FOLDER_PATH_PROPERTY_NAME, "./targets/transaction-logs");
        configuration.setProperty(TransactionConfiguration.TRANSACTION_TIMEOUT_PROPERTY_NAME, "15");
        configuration.setProperty(TransactionConfiguration.APPLICATION_SERVER_URL_PROPERTY_NAME, TestInstanceHostUtils.getOpenBISUrl());
        configuration.setProperty(TransactionConfiguration.AFS_SERVER_URL_PROPERTY_NAME,
                TestInstanceHostUtils.getAFSUrl() + TestInstanceHostUtils.getAFSPath());
        return configuration;
    }

    private Configuration getAfsServerConfiguration()
    {
        Configuration configuration = new Configuration(List.of(AtomicFileSystemServerParameter.class),
                "../server-data-store/src/main/resources/server-data-store-config.properties");
        configuration.setProperty(AtomicFileSystemServerParameter.logConfigFile, "etc/log4j2.xml");
        configuration.setProperty(AtomicFileSystemServerParameter.writeAheadLogRoot, "./targets/afs/transaction-logs");
        configuration.setProperty(AtomicFileSystemServerParameter.storageRoot, "./targets/afs/storage");
        configuration.setProperty(AtomicFileSystemServerParameter.apiServerTransactionManagerKey, TEST_TRANSACTION_COORDINATOR_KEY);
        configuration.setProperty(AtomicFileSystemServerParameter.apiServerInteractiveSessionKey, TEST_INTERACTIVE_SESSION_KEY);
        configuration.setProperty(AtomicFileSystemServerParameter.httpServerPort, String.valueOf(TestInstanceHostUtils.getAFSPort()));
        configuration.setProperty(AtomicFileSystemServerParameter.httpServerUri, TestInstanceHostUtils.getAFSPath());
        return configuration;
    }

}
