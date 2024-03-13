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
package ch.ethz.sis.openbis.systemtests;

import java.lang.reflect.Method;
import java.util.List;

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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import ch.ethz.sis.afsserver.server.observer.impl.DummyServerObserver;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.shared.startup.Configuration;
import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;

/**
 * @author pkupczyk
 */
public abstract class AbstractIntegrationTest
{
    protected static GenericWebApplicationContext applicationServerSpringContext;

    protected static TestLogger logger = new TestLogger();

    @BeforeSuite
    public void beforeSuite() throws Exception
    {
        TestInitializer.initWithIndex();
        startApplicationServer();
        startAfsServer();
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

    private void startApplicationServer() throws Exception
    {
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
    }

    private void startAfsServer() throws Exception
    {
        Configuration configuration = new Configuration(List.of(AtomicFileSystemServerParameter.class),
                "../server-data-store/src/main/resources/server-data-store-config.properties");
        configuration.setProperty(AtomicFileSystemServerParameter.logConfigFile, "etc/log4j2.xml");
        DummyServerObserver dummyServerObserver = new DummyServerObserver();
        new ch.ethz.sis.afsserver.server.Server<>(configuration, dummyServerObserver, dummyServerObserver);
    }

}
