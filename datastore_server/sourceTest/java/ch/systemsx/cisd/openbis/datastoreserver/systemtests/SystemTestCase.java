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
import static ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil.SERVICE_PROPERTIES_FILE;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
import org.testng.annotations.BeforeSuite;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.DefaultStorageProcessor;
import ch.systemsx.cisd.etlserver.ETLDaemon;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional;
import ch.systemsx.cisd.etlserver.registrator.JythonTopLevelDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.server.DataStoreServer;
import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;

/**
 * @author Franz-Josef Elmer
 */
public abstract class SystemTestCase extends AssertJUnit
{
    private static final String UNIT_TEST_WORKING_DIRECTORY = "unit-test-wd";

    private static final String TARGETS_DIRECTORY = "targets";

    private static final File UNIT_TEST_ROOT_DIRECTORY = new File(TARGETS_DIRECTORY
            + File.separator + UNIT_TEST_WORKING_DIRECTORY);

    private static final String ROOT_DIR_KEY = "root-dir";

    private static final String ROOT_DIR_PREFIX = "${" + ROOT_DIR_KEY + "}/";

    protected File workingDirectory;

    protected File rootDir;

    protected GenericWebApplicationContext applicationContext;

    protected SystemTestCase()
    {
        createWorkingDirectory();
        rootDir = new File(workingDirectory, "dss-root");
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

    @BeforeSuite
    public void beforeSuite() throws Exception
    {
        setUpDatabaseProperties();
        Server server = new Server();
        Connector connector = new SelectChannelConnector();
        connector.setPort(8888);
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

        List<String> serviceProperties =
                FileUtilities.loadToStringList(new File(SERVICE_PROPERTIES_FILE));
        for (String property : serviceProperties)
        {
            int index = property.indexOf(ROOT_DIR_PREFIX);
            if (index >= 0)
            {
                File folder =
                        new File(rootDir, property.substring(index + ROOT_DIR_PREFIX.length()));
                if (folder.exists())
                {
                    FileUtilities.deleteRecursively(folder);
                }
                assertEquals("Couldn't create folder " + folder.getAbsolutePath(), true,
                        folder.mkdirs());
            }
        }
        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX + ROOT_DIR_KEY,
                rootDir.getAbsolutePath());

        setUpTestThread();

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

    protected void setUpTestThread()
    {
        setUpTestThread(JythonTopLevelDataSetHandler.class, DefaultStorageProcessor.class, 
                "sourceTest/java/ch/systemsx/cisd/openbis/datastoreserver/systemtests/data-set-handler.py");
    }

    /**
     * Set up a DSS dropbox to be used by the test.
     */
    @SuppressWarnings("rawtypes")
    protected void setUpTestThread(
            Class<? extends JythonTopLevelDataSetHandler> jythonTopLevelDataSetHandlerClass,
            Class<? extends IStorageProcessorTransactional> storageProcessorClass, String scriptPath)
    {
        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX + "dss-rpc.put-default",
                "dss-system-test-thread");

        // Create a standard dss-system-test-thread configuration
        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX + "inputs",
                "dss-system-test-thread");
        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX
                + "dss-system-test-thread.incoming-dir", getIncomingDirectory().getAbsolutePath());
        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX
                + "dss-system-test-thread.incoming-data-completeness-condition", "auto-detection");
        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX
                + "dss-system-test-thread.top-level-data-set-handler",
                jythonTopLevelDataSetHandlerClass.getName());
        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX
                + "dss-system-test-thread.storage-processor", storageProcessorClass.getName());
        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX
                + "dss-system-test-thread.script-path", scriptPath);
    }

    /**
     * the path to the default incoming directory
     */
    protected File getIncomingDirectory()
    {
        return new File(rootDir, "incoming");
    }
}
