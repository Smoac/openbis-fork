/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import static ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil.OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.etlserver.ETLDaemon;
import ch.systemsx.cisd.openbis.dss.generic.server.DataStoreServer;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;

/**
 * @author Franz-Josef Elmer
 */
public abstract class SystemTestCase extends AssertJUnit
{
    public static final int SYSTEM_TEST_CASE_SERVER_PORT = 8888;

    private static final String UNIT_TEST_WORKING_DIRECTORY = "unit-test-wd";

    private static final String TARGETS_DIRECTORY = "targets";

    private static final File UNIT_TEST_ROOT_DIRECTORY = new File(TARGETS_DIRECTORY
            + File.separator + UNIT_TEST_WORKING_DIRECTORY);

    private static final String ROOT_DIR_KEY = "root-dir";

    private static final String DATA_SET_IMPORTED_LOG_MARKER = "Successfully registered data set";

    protected static GenericWebApplicationContext applicationContext;
    
    protected File workingDirectory;

    protected File rootDir;

    protected File store;

    protected BufferedAppender logAppender;

    protected SystemTestCase()
    {
        createWorkingDirectory();
        rootDir = new File(workingDirectory, "dss-root");
        store = new File(rootDir, "store");
        store.mkdirs();
    }

    protected void createWorkingDirectory()
    {
        workingDirectory = new File(UNIT_TEST_ROOT_DIRECTORY, "SystemTests");
        if (workingDirectory.exists())
        {
            try
            {
                FileUtils.deleteDirectory(workingDirectory);
            } catch (IOException ioex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ioex);
            }
        }
        workingDirectory.mkdirs();
        workingDirectory.deleteOnExit();
    }
    
    @BeforeMethod
    public void beforeTest(Method method)
    {
        System.out.println("BEFORE " + render(method));
    }

    @AfterMethod
    public void afterTest(Method method)
    {
        System.out.println("AFTER  " + render(method));
    }
    
    private String render(Method method)
    {
        return method.getDeclaringClass().getName() + "." + method.getName();
    }
    
//    @BeforeTest
//    public void setUpLogAppender()
//    {
//        logAppender = new BufferedAppender();
//    }

    @BeforeSuite
    public void beforeSuite() throws Exception
    {
        setUpDatabaseProperties();
        Server server = new Server();
        Connector connector = new SelectChannelConnector();
        connector.setPort(SYSTEM_TEST_CASE_SERVER_PORT);
        server.addConnector(connector);
        DispatcherServlet dispatcherServlet = new DispatcherServlet()
            {
                private static final long serialVersionUID = 1L;

                @Override
                protected WebApplicationContext findWebApplicationContext()
                {
                    XmlBeanFactory f =
                            new XmlBeanFactory(new FileSystemResource(
                                    "../openbis/resource/server/spring-servlet.xml"));
                    applicationContext = new GenericWebApplicationContext(f);
                    applicationContext.setParent(new ClassPathXmlApplicationContext(
                            getApplicationContextLocation()));
                    applicationContext.refresh();
                    return applicationContext;
                }
            };
        ServletContextHandler sch =
                new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        sch.addServlet(new ServletHolder(dispatcherServlet), "/*");
        server.start();

        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX + "inputs", "");
        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX + "core-plugins-folder",
                "sourceTest/core-plugins");
        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX + ROOT_DIR_KEY,
                rootDir.getAbsolutePath());
        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX
                + DssPropertyParametersUtil.DSS_REGISTRATION_LOG_DIR_PATH, getRegistrationLogDir()
                .getAbsolutePath());
        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX + "dss-rpc.put-default",
                "test");


        DataStoreServer.main(new String[0]);
        ETLDaemon.runForTesting(new String[0]);
    }

    /**
     * sets up the openbis database to be used by the tests.
     */
    protected void setUpDatabaseProperties()
    {
        TestInitializer.initWithIndex();
    }

    /**
     * Return the location of the openBIS application context config.
     */
    protected String getApplicationContextLocation()
    {
        return "classpath:applicationContext.xml";
    }

    /**
     * the path to the default incoming directory
     */
    protected File getIncomingDirectory()
    {
        return new File(rootDir, "incoming");
    }

    protected File getRegistrationLogDir()
    {
        return new File(workingDirectory, "log-registrations");
    }

    protected void waitUntilDataSetImported() throws Exception
    {
        boolean dataSetImported = false;
        String logContent = "";
        final int maxLoops = dataSetImportWaitDurationInSeconds();
        if (logAppender == null)
        {
            logAppender = new BufferedAppender(); 
        }
        for (int loops = 0; loops < maxLoops && dataSetImported == false; loops++)
        {
            Thread.sleep(1000);
            logContent = logAppender.getLogContent();
            if (logContent.contains(DATA_SET_IMPORTED_LOG_MARKER))
            {
                dataSetImported = true;
            } else
            {
                assertFalse(logContent, logContent.contains("ERROR"));
            }
        }

        if (dataSetImported == false)
        {
            fail("Failed to determine whether data set import was successful:" + logContent);
        }

    }

    /**
     * Time to wait to determine if a data set has been registered or not. Subclasses may override.
     */
    protected int dataSetImportWaitDurationInSeconds()
    {
        return 20;
    }

    protected void moveFileToIncoming(File exampleDataSet) throws IOException
    {
        FileUtils.moveDirectoryToDirectory(exampleDataSet, getIncomingDirectory(), false);
    }
}
