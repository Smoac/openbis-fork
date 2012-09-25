/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.TimeoutExceptionUnchecked;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.conversation.annotation.Conversational;
import ch.systemsx.cisd.common.conversation.annotation.Progress;
import ch.systemsx.cisd.common.conversation.client.ServiceConversationClientDetails;
import ch.systemsx.cisd.common.conversation.manager.BaseServiceConversationClientManager;
import ch.systemsx.cisd.common.conversation.manager.BaseServiceConversationServerManager;
import ch.systemsx.cisd.common.conversation.manager.IServiceConversationClientManagerRemote;
import ch.systemsx.cisd.common.conversation.manager.IServiceConversationServerManagerRemote;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.serviceconversation.client.ServiceExecutionException;
import ch.systemsx.cisd.common.spring.WaitAction;

@Test
public class ServiceConversationTest
{

    private static final int SERVER_PORT = 9000;

    private static final String SERVER_PATH = "/conversationServer";

    private static final String SERVER_URL = "http://localhost:" + SERVER_PORT + SERVER_PATH;

    private static final int CLIENT_PORT_1 = 9001;

    private static final int CLIENT_PORT_2 = 9002;

    private static final String CLIENT_PATH = "/conversationClient";

    private static final String CLIENT_URL_1 = "http://localhost:" + CLIENT_PORT_1 + CLIENT_PATH;

    private static final String CLIENT_URL_2 = "http://localhost:" + CLIENT_PORT_2 + CLIENT_PATH;

    private static final String SESSION_TOKEN_1 = "test-session-token-1";

    private static final String SESSION_TOKEN_2 = "test-session-token-2";

    private static final int TIMEOUT = 100;

    private static final Integer CLIENT_ID_1 = Integer.valueOf(1);

    private static final Integer CLIENT_ID_2 = Integer.valueOf(2);

    private static final Integer UNKNOWN_CLIENT_ID = Integer.valueOf(3);

    private Mockery context;

    private TestServiceWrapper1 serviceOnServerSideWrapper1;

    private TestServiceWrapper2 serviceOnServerSideWrapper2;

    private BaseServiceConversationClientManager clientManager1;

    private BaseServiceConversationClientManager clientManager2;

    private BaseServiceConversationServerManager serverManager;

    private ServiceExporter<?> clientExporter1;

    private ServiceExporter<?> clientExporter2;

    private ServiceExporter<?> serverExporter;

    @BeforeClass(alwaysRun = true)
    public void beforeClass() throws Exception
    {
        LogInitializer.init();

        serviceOnServerSideWrapper1 = new TestServiceWrapper1();
        serviceOnServerSideWrapper2 = new TestServiceWrapper2();

        clientManager1 = createClientManager();
        clientManager2 = createClientManager();
        serverManager = createServerManager();

        clientExporter1 = createClientExporter(CLIENT_PORT_1, clientManager1);
        clientExporter2 = createClientExporter(CLIENT_PORT_2, clientManager2);
        serverExporter = createServerExporter(serverManager);

        clientExporter1.getServer().start();
        clientExporter2.getServer().start();
        serverExporter.getServer().start();
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() throws Exception
    {
        clientExporter1.getServer().stop();
        clientExporter2.getServer().stop();
        serverExporter.getServer().stop();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() throws Exception
    {
        context = new Mockery();

        serviceOnServerSideWrapper1.setService(context.mock(TestService1.class));
        serviceOnServerSideWrapper2.setService(context.mock(TestService2.class));
    }

    private <S> S getServiceOnClientSide1(Class<S> serviceInterface)
    {
        return clientManager1.getService(SERVER_URL, serviceInterface, SESSION_TOKEN_1,
                CLIENT_ID_1, TIMEOUT);
    }

    private <S> S getServiceOnClientSide2(Class<S> serviceInterface)
    {
        return clientManager2.getService(SERVER_URL, serviceInterface, SESSION_TOKEN_2,
                CLIENT_ID_2, TIMEOUT);
    }

    @Test(expectedExceptions = ServiceExecutionException.class)
    public void testNonConversationalMethod() throws Exception
    {
        try
        {
            getServiceOnClientSide1(TestService1.class).nonConversationalMethod();
        } finally
        {
            assertNoMoreConversations();
        }
    }

    @Test
    public void testMethodWithoutReturnValue()
    {
        context.checking(new Expectations()
            {
                {
                    one(serviceOnServerSideWrapper1.getService()).methodWithoutReturnValue();
                }
            });
        getServiceOnClientSide1(TestService1.class).methodWithoutReturnValue();
        assertNoMoreConversations();
    }

    @Test
    public void testMethodWithPrimitiveReturnValue()
    {
        context.checking(new Expectations()
            {
                {
                    one(serviceOnServerSideWrapper1.getService()).methodWithPrimitiveReturnValue();
                    will(returnValue(1));
                }
            });

        Assert.assertEquals(getServiceOnClientSide1(TestService1.class)
                .methodWithPrimitiveReturnValue(), 1);
        assertNoMoreConversations();
    }

    @Test
    public void testMethodWithSerializableReturnValue()
    {
        context.checking(new Expectations()
            {
                {
                    one(serviceOnServerSideWrapper1.getService())
                            .methodWithSerializableReturnValue();
                    will(returnValue("abc"));

                }
            });
        Assert.assertEquals(getServiceOnClientSide1(TestService1.class)
                .methodWithSerializableReturnValue(), "abc");
        assertNoMoreConversations();
    }

    @Test(expectedExceptions = ServiceExecutionException.class)
    public void testMethodWithNonSerializableReturnValue()
    {
        try
        {
            context.checking(new Expectations()
                {
                    {
                        one(serviceOnServerSideWrapper1.getService()).methodWithObjectReturnValue();
                        will(returnValue(new Object()));
                    }
                });
            getServiceOnClientSide1(TestService1.class).methodWithObjectReturnValue();
        } finally
        {
            assertNoMoreConversations();
        }
    }

    @Test
    public void testMethodWithPrimitiveParameter()
    {
        context.checking(new Expectations()
            {
                {
                    one(serviceOnServerSideWrapper2.getService()).methodWithPrimitiveParameter(1);
                }
            });
        getServiceOnClientSide1(TestService2.class).methodWithPrimitiveParameter(1);
        assertNoMoreConversations();
    }

    @Test
    public void testMethodWithSerializableParameter()
    {
        context.checking(new Expectations()
            {
                {
                    one(serviceOnServerSideWrapper2.getService()).methodWithSerializableParameter(
                            "abc");
                }
            });
        getServiceOnClientSide1(TestService2.class).methodWithSerializableParameter("abc");
        assertNoMoreConversations();
    }

    @Test(expectedExceptions = RemoteAccessException.class)
    public void testMethodWithNonSerializableParameter()
    {
        try
        {
            getServiceOnClientSide1(TestService2.class).methodWithObjectParameter(new Object());
        } finally
        {
            // wait for the server to timeout and clean up the conversation
            assertNoMoreConversations(TIMEOUT);
        }
    }

    @Test(expectedExceptions = TimeoutExceptionUnchecked.class)
    public void testTimeout()
    {
        try
        {
            context.checking(new Expectations()
                {
                    {
                        one(serviceOnServerSideWrapper1.getService()).methodWithoutReturnValue();
                        will(new WaitAction(2 * TIMEOUT));
                    }
                });
            getServiceOnClientSide1(TestService1.class).methodWithoutReturnValue();
        } finally
        {
            // wait for the server to wake up and clean up the conversation
            assertNoMoreConversations(TIMEOUT);
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUnknownClient()
    {
        try
        {
            context.checking(new Expectations()
                {
                    {
                        one(serviceOnServerSideWrapper1.getService()).methodWithoutReturnValue();
                    }
                });

            TestService1 service =
                    clientManager1.getService(SERVER_URL, TestService1.class, SESSION_TOKEN_1,
                            UNKNOWN_CLIENT_ID, TIMEOUT);
            service.methodWithoutReturnValue();
        } finally
        {
            assertNoMoreConversations();
        }
    }

    @Test
    public void testMultipleClientsWithSameService()
    {
        context.checking(new Expectations()
            {
                {
                    one(serviceOnServerSideWrapper2.getService()).echo(1);
                    will(returnValue(1));

                    one(serviceOnServerSideWrapper2.getService()).echo(2);
                    will(returnValue(2));

                    one(serviceOnServerSideWrapper2.getService()).echo(3);
                    will(returnValue(3));

                    one(serviceOnServerSideWrapper2.getService()).echo(4);
                    will(returnValue(4));
                }
            });
        Assert.assertEquals(getServiceOnClientSide1(TestService2.class).echo(1), 1);
        Assert.assertEquals(getServiceOnClientSide2(TestService2.class).echo(2), 2);
        Assert.assertEquals(getServiceOnClientSide2(TestService2.class).echo(3), 3);
        Assert.assertEquals(getServiceOnClientSide1(TestService2.class).echo(4), 4);
        assertNoMoreConversations();
    }

    @Test
    public void testMultipleClientsWithDifferentService()
    {
        context.checking(new Expectations()
            {
                {
                    one(serviceOnServerSideWrapper1.getService()).echo(1);
                    will(returnValue(1));

                    one(serviceOnServerSideWrapper2.getService()).echo(2);
                    will(returnValue(2));
                }
            });
        Assert.assertEquals(getServiceOnClientSide1(TestService1.class).echo(1), 1);
        Assert.assertEquals(getServiceOnClientSide2(TestService2.class).echo(2), 2);
        assertNoMoreConversations();
    }

    private void assertNoMoreConversations()
    {
        assertNoMoreConversations(0);
    }

    private void assertNoMoreConversations(int delayBeforeChecking)
    {
        // wait for the server thread to run and clean up
        ConcurrencyUtilities.sleep(delayBeforeChecking + 100);
        Assert.assertEquals(clientManager1.getConversationCount(), 0);
        Assert.assertEquals(clientManager2.getConversationCount(), 0);
        Assert.assertEquals(serverManager.getConversationCount(), 0);
    }

    private BaseServiceConversationClientManager createClientManager()
    {
        return new BaseServiceConversationClientManager();
    }

    private BaseServiceConversationServerManager createServerManager()
    {
        return new BaseServiceConversationServerManager()
            {
                {
                    addService(TestService1.class, serviceOnServerSideWrapper1);
                    addService(TestService2.class, serviceOnServerSideWrapper2);
                }

                @Override
                protected ServiceConversationClientDetails getClientDetailsForClientId(
                        Object clientId)
                {
                    if (clientId.equals(CLIENT_ID_1))
                    {
                        return new ServiceConversationClientDetails(CLIENT_URL_1, TIMEOUT);
                    } else if (clientId.equals(CLIENT_ID_2))
                    {
                        return new ServiceConversationClientDetails(CLIENT_URL_2, TIMEOUT);
                    } else
                    {
                        throw new IllegalArgumentException("Unknown client");
                    }
                }
            };
    }

    private ServiceExporter<IServiceConversationClientManagerRemote> createClientExporter(int port,
            BaseServiceConversationClientManager manager)
    {
        try
        {
            ServiceExporter<IServiceConversationClientManagerRemote> exporter =
                    new ServiceExporter<IServiceConversationClientManagerRemote>(port, CLIENT_PATH,
                            IServiceConversationClientManagerRemote.class, manager);
            return exporter;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private ServiceExporter<IServiceConversationServerManagerRemote> createServerExporter(
            BaseServiceConversationServerManager manager)
    {
        try
        {
            ServiceExporter<IServiceConversationServerManagerRemote> exporter =
                    new ServiceExporter<IServiceConversationServerManagerRemote>(SERVER_PORT,
                            SERVER_PATH, IServiceConversationServerManagerRemote.class, manager);
            return exporter;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private class ServiceExporter<S>
    {

        private Server server;

        public ServiceExporter(final int port, final String path, final Class<?> serviceInterface,
                final S service)
        {
            SelectChannelConnector connector = new SelectChannelConnector();
            connector.setPort(port);

            this.server = new Server();
            this.server.addConnector(connector);

            DispatcherServlet dispatcher = new DispatcherServlet()
                {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected WebApplicationContext findWebApplicationContext()
                    {
                        GenericWebApplicationContext ctx = new GenericWebApplicationContext();

                        GenericBeanDefinition exporterBean = new GenericBeanDefinition();
                        exporterBean.setBeanClass(HttpInvokerServiceExporter.class);
                        MutablePropertyValues exporterProperties = new MutablePropertyValues();
                        exporterProperties.addPropertyValue("service", service);
                        exporterProperties.addPropertyValue("serviceInterface", serviceInterface);
                        exporterBean.setPropertyValues(exporterProperties);
                        ctx.registerBeanDefinition("serviceExporter", exporterBean);

                        GenericBeanDefinition mappingBean = new GenericBeanDefinition();
                        mappingBean.setBeanClass(SimpleUrlHandlerMapping.class);
                        Map<String, String> urlMap = new HashMap<String, String>();
                        urlMap.put(path, "serviceExporter");
                        MutablePropertyValues mappingProperties = new MutablePropertyValues();
                        mappingProperties.addPropertyValue("urlMap", urlMap);
                        mappingBean.setPropertyValues(mappingProperties);
                        ctx.registerBeanDefinition("mapping", mappingBean);

                        ctx.refresh();
                        return ctx;
                    }
                };

            ServletContextHandler servletCtx =
                    new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
            servletCtx.addServlet(new ServletHolder(dispatcher), "/*");
        }

        public Server getServer()
        {
            return server;
        }

    }

    public static interface TestService1
    {

        public void nonConversationalMethod();

        @Conversational(progress = Progress.MANUAL)
        public void methodWithoutReturnValue();

        @Conversational(progress = Progress.MANUAL)
        public int methodWithPrimitiveReturnValue();

        @Conversational(progress = Progress.MANUAL)
        public Serializable methodWithSerializableReturnValue();

        @Conversational(progress = Progress.MANUAL)
        public Object methodWithObjectReturnValue();

        @Conversational(progress = Progress.MANUAL)
        public Object echo(Object parameter);

    }

    public static interface TestService2
    {
        @Conversational(progress = Progress.MANUAL)
        public void methodWithPrimitiveParameter(int parameter);

        @Conversational(progress = Progress.MANUAL)
        public void methodWithSerializableParameter(Serializable parameter);

        @Conversational(progress = Progress.MANUAL)
        public void methodWithObjectParameter(Object parameter);

        @Conversational(progress = Progress.MANUAL)
        public Object echo(Object parameter);

    }

    public static interface TestServiceLocal
    {
        public void someLocalMethod();
    }

    public static class TestServiceWrapper1 implements TestServiceLocal, TestService1
    {

        private TestService1 service;

        @Override
        public void nonConversationalMethod()
        {
            service.nonConversationalMethod();
        }

        @Override
        public void methodWithoutReturnValue()
        {
            service.methodWithoutReturnValue();
        }

        @Override
        public int methodWithPrimitiveReturnValue()
        {
            return service.methodWithPrimitiveReturnValue();
        }

        @Override
        public Serializable methodWithSerializableReturnValue()
        {
            return service.methodWithSerializableReturnValue();
        }

        @Override
        public Object methodWithObjectReturnValue()
        {
            return service.methodWithObjectReturnValue();
        }

        @Override
        public Object echo(Object parameter)
        {
            return service.echo(parameter);
        }

        @Override
        public void someLocalMethod()
        {
        }

        public TestService1 getService()
        {
            return service;
        }

        public void setService(TestService1 service)
        {
            this.service = service;
        }

    }

    public static class TestServiceWrapper2 implements TestServiceLocal, TestService2
    {

        private TestService2 service;

        @Override
        public void methodWithPrimitiveParameter(int parameter)
        {
            service.methodWithPrimitiveParameter(parameter);
        }

        @Override
        public void methodWithSerializableParameter(Serializable parameter)
        {
            service.methodWithSerializableParameter(parameter);
        }

        @Override
        public void methodWithObjectParameter(Object parameter)
        {
            service.methodWithObjectParameter(parameter);
        }

        @Override
        public Object echo(Object parameter)
        {
            return service.echo(parameter);
        }

        @Override
        public void someLocalMethod()
        {
        }

        public TestService2 getService()
        {
            return service;
        }

        public void setService(TestService2 service)
        {
            this.service = service;
        }

    }

}
