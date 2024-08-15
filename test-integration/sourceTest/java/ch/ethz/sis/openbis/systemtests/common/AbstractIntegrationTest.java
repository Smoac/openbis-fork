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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
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
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.remoting.rmi.CodebaseAwareObjectInputStream;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import ch.ethz.sis.afs.manager.TransactionConnection;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameterUtil;
import ch.ethz.sis.openbis.generic.OpenBIS;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.PhysicalDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.FileFormatTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.ProprietaryStorageFormatPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.RelativeLocationLocatorTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.PersonCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.TransactionConfiguration;
import ch.ethz.sis.shared.startup.Configuration;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;

/**
 * @author pkupczyk
 */
public abstract class AbstractIntegrationTest
{
    public static final String TEST_INTERACTIVE_SESSION_KEY = "integration-test-interactive-session-key";

    public static final String TEST_DATA_STORE_CODE = "TEST";

    public static final String DEFAULT_SPACE = "DEFAULT";

    public static final String TEST_SPACE = "TEST";

    public static final String INSTANCE_ADMIN = "admin";

    public static final String DEFAULT_SPACE_ADMIN = "default_space_admin";

    public static final String TEST_SPACE_ADMIN = "test_space_admin";

    public static final String TEST_SPACE_OBSERVER = "test_space_observer";

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
        createApplicationServerData();
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

        String storageIncomingShareId = configuration.getStringProperty(AtomicFileSystemServerParameter.storageIncomingShareId);

        new File(storageRoot, storageIncomingShareId).mkdirs();
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

    private void createApplicationServerData() throws Exception
    {
        Configuration configuration = getAfsServerConfiguration();

        OpenBIS openBIS = createOpenBIS();
        openBIS.login(INSTANCE_ADMIN, PASSWORD);

        createDataStore(TEST_DATA_STORE_CODE);

        String afsServerUser = configuration.getStringProperty(AtomicFileSystemServerParameter.openBISUser);
        createUser(openBIS, afsServerUser, null, Role.ETL_SERVER);

        createSpace(openBIS, TEST_SPACE);
        createUser(openBIS, TEST_SPACE_ADMIN, TEST_SPACE, Role.ADMIN);
        createUser(openBIS, TEST_SPACE_OBSERVER, TEST_SPACE, Role.OBSERVER);

        createUser(openBIS, DEFAULT_SPACE_ADMIN, DEFAULT_SPACE, Role.ADMIN);
    }

    private void startAfsServer() throws Exception
    {
        log("Starting afs server.");
        Configuration configuration = getAfsServerConfiguration();
        AbstractIntegrationTest.afsServer = new ch.ethz.sis.afsserver.server.Server<>(configuration);
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
        applicationServerSpringContext.close();
        ((ClassPathXmlApplicationContext) applicationServerSpringContext.getParent()).close();
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

    public static Properties getApplicationServerConfiguration(boolean createDatabase) throws Exception
    {
        Properties configuration = new Properties();
        configuration.load(new FileInputStream("etc/as/service.properties"));
        configuration.setProperty("database.create-from-scratch", String.valueOf(createDatabase));
        configuration.setProperty("enabled-modules", "events-search");
        configuration.setProperty(TransactionConfiguration.APPLICATION_SERVER_URL_PROPERTY_NAME, TestInstanceHostUtils.getOpenBISProxyUrl());
        configuration.setProperty(TransactionConfiguration.AFS_SERVER_URL_PROPERTY_NAME,
                TestInstanceHostUtils.getAFSProxyUrl() + TestInstanceHostUtils.getAFSPath());
        return configuration;
    }

    public static Configuration getAfsServerConfiguration()
    {
        Configuration configuration = new Configuration(List.of(AtomicFileSystemServerParameter.class),
                "etc/afs/service.properties");
        configuration.setProperty(AtomicFileSystemServerParameter.httpServerPort, String.valueOf(TestInstanceHostUtils.getAFSPort()));
        configuration.setProperty(AtomicFileSystemServerParameter.httpServerUri, TestInstanceHostUtils.getAFSPath());
        configuration.setProperty(AtomicFileSystemServerParameter.openBISUrl, TestInstanceHostUtils.getOpenBISProxyUrl());
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

    public static void createDataStore(String dataStoreCode)
    {
        DataStorePE testDataStore = new DataStorePE();
        testDataStore.setCode(dataStoreCode);
        testDataStore.setDownloadUrl("");
        testDataStore.setRemoteUrl("");
        testDataStore.setDatabaseInstanceUUID("");
        testDataStore.setSessionToken("");
        testDataStore.setArchiverConfigured(false);

        executeInApplicationServerTransaction((status) ->
        {
            IDAOFactory daoFactory = applicationServerSpringContext.getBean(IDAOFactory.class);
            daoFactory.getDataStoreDAO().createOrUpdateDataStore(testDataStore);
            return null;
        });
    }

    public static Space createSpace(OpenBIS openBIS, String spaceCode)
    {
        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode(spaceCode);
        List<SpacePermId> spaceIds = openBIS.createSpaces(List.of(spaceCreation));
        Space space = getSpace(openBIS, spaceIds.get(0));
        log("Created " + space.getCode() + " space.");
        return space;
    }

    public static Space getSpace(OpenBIS openBIS, ISpaceId spaceId)
    {
        return openBIS.getSpaces(List.of(spaceId), new SpaceFetchOptions()).get(spaceId);
    }

    public static Project createProject(OpenBIS openBIS, ISpaceId spaceId, String projectCode)
    {
        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setSpaceId(spaceId);
        projectCreation.setCode(projectCode);
        List<ProjectPermId> projectIds = openBIS.createProjects(List.of(projectCreation));
        Project project = openBIS.getProjects(projectIds, new ProjectFetchOptions()).get(projectIds.get(0));
        log("Created " + project.getIdentifier() + " project.");
        return project;
    }

    public static Experiment createExperiment(OpenBIS openBIS, IProjectId projectId, String experimentCode)
    {
        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setTypeId(new EntityTypePermId("UNKNOWN"));
        experimentCreation.setProjectId(projectId);
        experimentCreation.setCode(experimentCode);
        List<ExperimentPermId> experimentIds = openBIS.createExperiments(List.of(experimentCreation));
        Experiment experiment = openBIS.getExperiments(experimentIds, new ExperimentFetchOptions()).get(experimentIds.get(0));
        log("Created " + experiment.getIdentifier() + " experiment.");
        return experiment;
    }

    public static Sample createSample(OpenBIS openBIS, ISpaceId spaceId, String sampleCode)
    {
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setTypeId(new EntityTypePermId("UNKNOWN"));
        sampleCreation.setSpaceId(spaceId);
        sampleCreation.setCode(sampleCode);
        List<SamplePermId> sampleIds = openBIS.createSamples(List.of(sampleCreation));
        Sample sample = getSample(openBIS, sampleIds.get(0));
        log("Created " + sample.getIdentifier() + " sample.");
        return sample;
    }

    public static Sample getSample(OpenBIS openBIS, ISampleId sampleId)
    {
        return openBIS.getSamples(List.of(sampleId), new SampleFetchOptions()).get(sampleId);
    }

    public static DataSet createDataSet(OpenBIS openBIS, IExperimentId experimentId, String dataSetCode, String testFile, byte[] testData)
            throws IOException
    {
        Configuration afsServerConfiguration = getAfsServerConfiguration();
        String storageRoot = AtomicFileSystemServerParameterUtil.getStorageRoot(afsServerConfiguration);
        String storageUuid = AtomicFileSystemServerParameterUtil.getStorageUuid(afsServerConfiguration);
        Integer shareId = AtomicFileSystemServerParameterUtil.getStorageIncomingShareId(afsServerConfiguration);

        PhysicalDataCreation physicalCreation = new PhysicalDataCreation();
        physicalCreation.setShareId(shareId.toString());
        physicalCreation.setFileFormatTypeId(new FileFormatTypePermId("PROPRIETARY"));
        physicalCreation.setLocatorTypeId(new RelativeLocationLocatorTypePermId());
        physicalCreation.setLocation("test-location-" + UUID.randomUUID());
        physicalCreation.setStorageFormatId(new ProprietaryStorageFormatPermId());
        physicalCreation.setH5arFolders(false);
        physicalCreation.setH5Folders(false);

        DataSetCreation dataSetCreation = new DataSetCreation();
        dataSetCreation.setDataStoreId(new DataStorePermId(TEST_DATA_STORE_CODE));
        dataSetCreation.setDataSetKind(DataSetKind.PHYSICAL);
        dataSetCreation.setTypeId(new EntityTypePermId("UNKNOWN"));
        dataSetCreation.setExperimentId(experimentId);
        dataSetCreation.setCode(dataSetCode);
        dataSetCreation.setPhysicalData(physicalCreation);

        List<DataSetPermId> dataSetIds = openBIS.createDataSetsAS(List.of(dataSetCreation));
        DataSet dataSet = openBIS.getDataSets(dataSetIds, new DataSetFetchOptions()).get(dataSetIds.get(0));

        if (testFile != null && testData != null)
        {
            List<String> dataSetFolderParts = new ArrayList<>();
            dataSetFolderParts.add(storageRoot);
            dataSetFolderParts.add(shareId.toString());
            dataSetFolderParts.add(storageUuid);
            dataSetFolderParts.addAll(Arrays.asList(ch.ethz.sis.shared.io.IOUtils.getShards(dataSet.getCode())));
            dataSetFolderParts.add(dataSet.getCode());
            File dataSetFolder = new File(String.join(File.separator, dataSetFolderParts));

            Files.createDirectories(dataSetFolder.toPath());
            Path testFilePath = Files.createFile(Path.of(dataSetFolder.getPath(), testFile));
            ch.ethz.sis.shared.io.IOUtils.write(testFilePath.toFile().getAbsolutePath(), 0L, testData);
        }

        log("Created " + dataSet.getPermId() + " dataSet.");
        return dataSet;
    }

    public static Person createUser(OpenBIS openBIS, String userId, String spaceCode, Role spaceRole)
    {
        PersonCreation personCreation = new PersonCreation();
        personCreation.setUserId(userId);
        PersonPermId personId = openBIS.createPersons(List.of(personCreation)).get(0);

        RoleAssignmentCreation roleCreation = new RoleAssignmentCreation();
        roleCreation.setUserId(personId);
        if (spaceCode != null)
        {
            roleCreation.setSpaceId(new SpacePermId(spaceCode));
        }
        roleCreation.setRole(spaceRole);
        openBIS.createRoleAssignments(List.of(roleCreation));

        Person person = openBIS.getPersons(List.of(personId), new PersonFetchOptions()).get(personId);
        log("Created " + person.getUserId() + " user.");
        return person;
    }

    public static void executeInApplicationServerTransaction(TransactionCallback<?> callback)
    {
        PlatformTransactionManager manager = applicationServerSpringContext.getBean(PlatformTransactionManager.class);
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        definition.setReadOnly(false);
        TransactionTemplate template = new TransactionTemplate(manager, definition);
        template.execute(callback);
    }

    public static void log(String message)
    {
        System.out.println("[TEST] " + message);
    }

}
