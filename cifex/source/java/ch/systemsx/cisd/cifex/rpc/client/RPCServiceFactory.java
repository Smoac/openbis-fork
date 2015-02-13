/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.rpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

import org.apache.commons.lang.time.DateUtils;
import org.apache.http.HttpHost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.remoting.httpinvoker.HttpComponentsHttpInvokerRequestExecutor;

import com.marathon.util.spring.StreamSupportingHttpInvokerProxyFactoryBean;

import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.common.ssl.SslCertificateHelper;

/**
 * The factory for the CIFEX RPC service.
 * 
 * @author Bernd Rinn
 */
public final class RPCServiceFactory
{

    private static final String SPRING_BEAN_URL_PROTOCOL = "spring-bean://";

    private static final int SERVER_TIMEOUT_MIN = 5;

    private static final class ServiceInvocationHandler implements InvocationHandler
    {
        private final ICIFEXRPCService service;

        private ServiceInvocationHandler(ICIFEXRPCService service)
        {
            this.service = service;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            try
            {
                return method.invoke(service, args);
            } catch (InvocationTargetException ex)
            {
                throw ex.getCause();
            }
        }
    }

    private RPCServiceFactory()
    {
        // Can not be instantiated.
    }

    /**
     * Creates the CIFEX component class.
     */
    public static ICIFEXComponent createCIFEXComponent(String serviceURL,
            boolean getServerCertificateFromServer) throws IncompatibleAPIVersionsException
    {
        final ClassLoader classLoader = RPCServiceFactory.class.getClassLoader();
        final ICIFEXRPCService service = createService(serviceURL, getServerCertificateFromServer);
        final ServiceInvocationHandler invocationHandler = new ServiceInvocationHandler(service);
        final ICIFEXRPCService proxy =
                (ICIFEXRPCService) Proxy.newProxyInstance(classLoader, new Class[]
                    { ICIFEXRPCService.class }, invocationHandler);
        final int apiServerVersion = proxy.getVersion();
        final int apiMinClientVersion = proxy.getMinClientVersion();
        if (ICIFEXRPCService.VERSION < apiMinClientVersion
                || ICIFEXRPCService.VERSION > apiServerVersion)
        {
            throw new IncompatibleAPIVersionsException(ICIFEXRPCService.VERSION, apiServerVersion,
                    apiMinClientVersion);
        }
        return new CIFEXComponent(proxy);
    }

    // TODO 2010-04-21, Tomasz Pylak: refactor to use
    // HttpInvokerUtils.createStreamSupportingServiceStub()
    private static ICIFEXRPCService createService(String serviceURL,
            boolean getServerCertificateFromServer)
    {
        if (serviceURL.startsWith(SPRING_BEAN_URL_PROTOCOL))
        {
            AbstractApplicationContext applicationContext =
                    new ClassPathXmlApplicationContext(new String[]
                        { "applicationContext.xml" }, true);
            LogInitializer.init();
            return ((ICIFEXRPCService) applicationContext.getBean("rpc-service"));
        }
        if (getServerCertificateFromServer)
        {
            setUpKeyStore(serviceURL);
        }
        final StreamSupportingHttpInvokerProxyFactoryBean httpInvokerProxy =
                new StreamSupportingHttpInvokerProxyFactoryBean();
        httpInvokerProxy.setServiceUrl(serviceURL);
        httpInvokerProxy.setServiceInterface(ICIFEXRPCService.class);
        
        HttpComponentsHttpInvokerRequestExecutor httpInvokerRequestExecutor;        
        final InetSocketAddress proxyAddressOrNull = HttpInvokerUtils.tryFindProxy(serviceURL);
        if (proxyAddressOrNull != null)
        {
            HttpHost proxy = new HttpHost(proxyAddressOrNull.getHostName(), proxyAddressOrNull.getPort(), "http");
            CloseableHttpClient client = HttpClients.custom().setProxy(proxy).build();
            httpInvokerRequestExecutor = new HttpComponentsHttpInvokerRequestExecutor(client);            
        } else {
            httpInvokerRequestExecutor = new HttpComponentsHttpInvokerRequestExecutor();                        
        }
        
        httpInvokerRequestExecutor.setReadTimeout((int) DateUtils.MILLIS_PER_MINUTE * SERVER_TIMEOUT_MIN);
        httpInvokerProxy.setHttpInvokerRequestExecutor(httpInvokerRequestExecutor);
        httpInvokerProxy.afterPropertiesSet();
        return (ICIFEXRPCService) httpInvokerProxy.getObject();
    }

    private static void setUpKeyStore(String serviceURL)
    {
        new SslCertificateHelper(serviceURL, ClientConfigurationFiles.KEYSTORE_FILE, "cifex")
                .setUpKeyStore();
    }

}
