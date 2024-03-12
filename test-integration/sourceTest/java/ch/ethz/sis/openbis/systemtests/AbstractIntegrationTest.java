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

import org.apache.log4j.Level;
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
import org.testng.annotations.BeforeTest;

import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author pkupczyk
 */
public abstract class AbstractIntegrationTest
{
    protected static GenericWebApplicationContext applicationContext;

    protected BufferedAppender logAppender;

    @BeforeMethod
    public void beforeTest(Method method)
    {
        System.out.println("BEFORE " + render(method));
        getLogAppender().resetLogContent();
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

    @BeforeSuite
    public void beforeSuite() throws Exception
    {
        TestInitializer.initWithIndex();

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
                XmlBeanFactory f =
                        new XmlBeanFactory(new FileSystemResource("../server-application-server/resource/server/spring-servlet.xml"));
                applicationContext = new GenericWebApplicationContext(f);
                applicationContext.setParent(new ClassPathXmlApplicationContext("classpath:applicationContext.xml"));
                applicationContext.refresh();
                return applicationContext;
            }
        };
        ServletContextHandler sch =
                new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        sch.addServlet(new ServletHolder(dispatcherServlet), "/*");
        server.start();
    }

    @BeforeTest
    public void setUpLogAppender()
    {
        logAppender = LogRecordingUtils.createRecorder();
    }

    private BufferedAppender getLogAppender()
    {
        if (logAppender == null)
        {
            logAppender = LogRecordingUtils.createRecorder("%d %p [%t] %c - %m%n", Level.INFO);
        }
        return logAppender;
    }

}
