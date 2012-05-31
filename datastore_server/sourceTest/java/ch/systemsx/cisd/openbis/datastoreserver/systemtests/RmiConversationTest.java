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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.base.exceptions.TimeoutExceptionUnchecked;
import ch.systemsx.cisd.common.conversation.ConversationalRmiClient;
import ch.systemsx.cisd.common.conversation.IProgressListener;
import ch.systemsx.cisd.common.conversation.RmiConversationController;
import ch.systemsx.cisd.common.conversation.ConversationalRmiServer;
import ch.systemsx.cisd.common.conversation.RmiServiceFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.serviceconversation.ServiceConversationDTO;
import ch.systemsx.cisd.common.serviceconversation.ServiceMessage;
import ch.systemsx.cisd.common.serviceconversation.server.ServiceConversationServer;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RmiConversationTest extends SystemTestCase
{
    private static RmiConversationController cont;
    private EchoService echo;
    private Server conversationClient;
    
    @BeforeClass
    public void beforeClass() throws Exception {
        LogInitializer.init();

        conversationClient = new Server();
        Connector clientConnector = new SelectChannelConnector();
        clientConnector.setPort(8882);
        conversationClient.addConnector(clientConnector);
        DispatcherServlet clientDispatcherServlet = new DispatcherServlet()
            {
                private static final long serialVersionUID = 1L;

                @Override
                protected WebApplicationContext findWebApplicationContext()
                {
                    GenericWebApplicationContext ctx = new GenericWebApplicationContext();

                    GenericBeanDefinition definition = new GenericBeanDefinition();
                    definition.setBeanClass(ClientBean.class);
                    ctx.registerBeanDefinition("client", definition);

                    GenericBeanDefinition exporter = new GenericBeanDefinition();
                    exporter.setBeanClass(ClientExporter.class);
                    ctx.registerBeanDefinition("clientExporter", exporter);
                    
                    ctx.refresh();
                    
                    return ctx;
                }
            };
        ServletContextHandler clientSch =
                new ServletContextHandler(conversationClient, "/", ServletContextHandler.SESSIONS);
        clientSch.addServlet(new ServletHolder(clientDispatcherServlet), "/*");
        conversationClient.start();
        
    }

    @BeforeMethod
    public void beforeMethod() throws Exception {
        EchoService httpEcho = HttpInvokerUtils.createServiceStub(EchoService.class, "http://localhost:8888/openbis/rmi-echoservice", 5000);
        cont = new RmiConversationController("http://localhost:8882");
        echo = cont.getConversationalReference("", httpEcho, EchoService.class);
    }
    
    @AfterClass
    public void afterClass() throws Exception {
        conversationClient.getGracefulShutdown();
    }

    
    @Test
    public void callThroughRpcServiceConversationWorks() throws Exception {
        assertThat(echo.echo("echo", 0), is("echo"));
    }
    
    @Test(expectedExceptions = TimeoutExceptionUnchecked.class)
    public void clientTimeoutsIfNoProgressMade() {
        echo.echoWithoutProgress("echo", 5000);
    }
    
    @Test
    public void clientDoesNotTimeOutIfProgressIsReported() {
        assertThat(echo.echo("echo", 5000), is("echo"));
    }

    @Test
    public void transactionIsRolledBackIfThereIsAnExceptionDuringRequestProcessing() throws Exception {
        try {   
            echo.echoWithStoreAndProcessingException("echo");
            assertThat(true, is(false));
        } catch (Exception e) {
        }
        
        assertThat(echo.exists("echo"), is(false));
    }
    
    @Test
    public void transactionIsCommitedAfterSuccessfulRequestProcessing() throws Exception {
        assertThat(echo.echoWithStore("stored"), is("stored"));
        assertThat(echo.exists("stored"), is(true));
    }
    
    
    public interface EchoService extends ConversationalRmiServer {
        public String echo(String input, Integer delayInMillis);
        public String echoWithoutProgress(String input, Integer delayInMillis);
        public String echoWithStore(String input);
        public String echoWithStoreAndProcessingException(String input);
        public boolean exists(String code);
    }

    public static class EchoServiceBean implements EchoService {

        private ServiceConversationServer server;
        
        private SessionFactory sessionFactory;
        
        public EchoServiceBean() {
            this.server = new ServiceConversationServer();
        }
        
        public ServiceConversationDTO startConversation(String sessionToken, String clientUrl,
                String typeId)
        {
            ConversationalRmiClient client = HttpInvokerUtils.createServiceStub(ConversationalRmiClient.class, "http://localhost:8882/client", 5000);
            server.addClientResponseTransport("test-client-id", client);
            return this.server.startConversation(typeId, "test-client-id");
        }

        public void send(ServiceMessage message)
        {
            server.getIncomingMessageTransport().send(message);
        }

        public String echo(String input, Integer delayInMillis)
        {
            return echo(input, delayInMillis);
        }
        
        public String echo(String input, Integer delayInMillis,IProgressListener progress) {
                        
            long startTime = System.currentTimeMillis();
            
            while (System.currentTimeMillis() - startTime < delayInMillis) {
                try
                {
                    Thread.sleep(delayInMillis / 50);
                } catch (InterruptedException ex)
                {
                    ex.printStackTrace();
                }
                progress.update("progress", 1, 1);
                
            }
            return input;  
        }
        
        public String echoWithoutProgress(String input, Integer delayInMillis)
        {
            return echoWithoutProgress(input, delayInMillis, null);
        }
        
        public String echoWithoutProgress(String input, Integer delayInMillis, IProgressListener progress) {
            try
            {
                Thread.sleep(delayInMillis);
            } catch (InterruptedException ex)
            {
                ex.printStackTrace();
            }
            return input;
        }

        public String echoWithStore(String input)
        {
            return echoWithStore(input, null);
        }
        
        public String echoWithStore(String input, IProgressListener progress) {
            
            DatabaseInstancePE db = new DatabaseInstancePE();
            db.setCode(input);
            db.setOriginalSource(false);
            db.setRegistrationDate(new Date());
            db.setUuid(UUID.randomUUID().toString());
            sessionFactory.getCurrentSession().persist(db);
            return input;
        }
        
        public String echoWithStoreAndProcessingException(String input)
        {
            return echoWithStoreAndProcessingException(input, null);
        }
        
        public String echoWithStoreAndProcessingException(String input, IProgressListener progress) {
            
            DatabaseInstancePE db = new DatabaseInstancePE();
            db.setCode(input);
            db.setOriginalSource(false);
            db.setRegistrationDate(new Date());
            db.setUuid(UUID.randomUUID().toString());
            sessionFactory.getCurrentSession().persist(db);

            throw new NullPointerException("Exception");
        }
        
        public boolean exists(String code) {
            return exists(code, null);
        }
        
        public boolean exists(String code, IProgressListener progress) {
            
           Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DatabaseInstancePE.class);
           criteria.add(Restrictions.eq("code", code));

           @SuppressWarnings({ "unchecked", "cast" })
           List<DatabaseInstancePE> list = (List<DatabaseInstancePE>)criteria.list();
           boolean result =  list.size() > 0;
           if (result) {
               sessionFactory.getCurrentSession().delete(list.get(0));
           }
           return result;
        }

        public void setSessionFactory(SessionFactory sessionFactory) {
            this.sessionFactory = sessionFactory;
            this.server.addServiceType(new RmiServiceFactory<EchoService>(this.server, this, EchoService.class, 1000, this.sessionFactory));
        }
    }    
    
    
    public static class ClientBean implements ConversationalRmiClient {

        public void send(ServiceMessage message)
        {
            cont.process(message);
        }        
    }
    
    @RequestMapping({ "/client" })
    public static class ClientExporter extends HttpInvokerServiceExporter {
        @Override
        public void afterPropertiesSet()
        {
            setServiceInterface(ConversationalRmiClient.class);
            setService(new ClientBean());
            super.afterPropertiesSet();
        }
    }
    
    @RequestMapping({ "/openbis/rmi-echoservice" })
    public static class EchoServiceExporter extends HttpInvokerServiceExporter {

        @Resource(name = "echoService")
        private EchoService echoService;
        
        @Override
        public void afterPropertiesSet()
        {
            setServiceInterface(EchoService.class);
            setService(echoService);
            super.afterPropertiesSet();
        }
    }
}
