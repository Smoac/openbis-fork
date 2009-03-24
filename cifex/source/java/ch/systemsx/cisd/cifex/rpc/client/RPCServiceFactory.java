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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.common.logging.LogInitializer;

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

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            synchronized (service)
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
    }

    private RPCServiceFactory()
    {
        // Can not be instantiated.
    }

    /**
     * Returns the directory of CIFEX configuration data.
     */
    public static File getCIFEXConfigDir()
    {
        String homeDir = System.getProperty("cifex.root");
        File cifexDir;
        if (homeDir != null)
        {
            cifexDir = new File(homeDir, "etc");
        } else
        {
            homeDir = System.getProperty("user.home");
            cifexDir = new File(homeDir, ".cifex");
        }
        cifexDir.mkdirs();
        return cifexDir;
    }

    /**
     * Creates the CIFEX RPC service.
     */
    public static ICIFEXRPCService createServiceProxy(String serviceURL,
            boolean getServerCertificateFromServer)
    {
        ClassLoader classLoader = RPCServiceFactory.class.getClassLoader();
        ICIFEXRPCService service = createService(serviceURL, getServerCertificateFromServer);
        ServiceInvocationHandler invocationHandler = new ServiceInvocationHandler(service);
        return (ICIFEXRPCService) Proxy.newProxyInstance(classLoader, new Class[]
            { ICIFEXRPCService.class }, invocationHandler);
    }

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
        final HttpInvokerProxyFactoryBean httpInvokerProxy = new HttpInvokerProxyFactoryBean();
        httpInvokerProxy.setServiceUrl(serviceURL);
        httpInvokerProxy.setServiceInterface(ICIFEXRPCService.class);
        final CommonsHttpInvokerRequestExecutor httpInvokerRequestExecutor =
                new CommonsHttpInvokerRequestExecutor();
        httpInvokerRequestExecutor.setReadTimeout((int) DateUtils.MILLIS_PER_MINUTE
                * SERVER_TIMEOUT_MIN);
        httpInvokerProxy.setHttpInvokerRequestExecutor(httpInvokerRequestExecutor);
        httpInvokerProxy.afterPropertiesSet();
        return (ICIFEXRPCService) httpInvokerProxy.getObject();
    }

    private static void setUpKeyStore(String serviceURL)
    {
        if (serviceURL.startsWith("https"))
        {
            Certificate[] certificates = getServerCertificate(serviceURL);
            KeyStore keyStore;
            try
            {
                keyStore = KeyStore.getInstance("JKS");
                keyStore.load(null, null);
                for (int i = 0; i < certificates.length; i++)
                {
                    keyStore.setCertificateEntry("cifex" + i, certificates[i]);
                }
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
            FileOutputStream fileOutputStream = null;
            try
            {
                File cifexDir = getCIFEXConfigDir();
                File keyStoreFile = new File(cifexDir, "keystore");
                fileOutputStream = new FileOutputStream(keyStoreFile);
                keyStore.store(fileOutputStream, "changeit".toCharArray());
                fileOutputStream.close();
                System.setProperty("javax.net.ssl.trustStore", keyStoreFile.getAbsolutePath());
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            } finally
            {
                IOUtils.closeQuietly(fileOutputStream);
            }
        }
    }

    private static Certificate[] getServerCertificate(String serviceURL)
    {
        workAroundABugInJava6();

        // Create a trust manager that does not validate certificate chains
        setUpAllAcceptingTrustManager();
        SSLSocket socket = null;
        try
        {
            URL url = new URL(serviceURL);
            int port = url.getPort();
            String hostname = url.getHost();
            SSLSocketFactory factory = HttpsURLConnection.getDefaultSSLSocketFactory();
            socket = (SSLSocket) factory.createSocket(hostname, port);
            socket.startHandshake();
            return socket.getSession().getPeerCertificates();
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        } finally
        {
            if (socket != null)
            {
                try
                {
                    socket.close();
                } catch (IOException ex)
                {
                    // ignored
                }
            }
        }
    }

    private static void setUpAllAcceptingTrustManager()
    {
        TrustManager[] trustAllCerts = new TrustManager[]
            { 
                new X509TrustManager()
                {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers()
                    {
                        return null;
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
                            String authType)
                    {
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
                            String authType)
                    {
                    }
                } };
        // Install the all-trusting trust manager
        try
        {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e)
        {
        }
    }

    // see comment submitted on 31-JAN-2008 for
    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6514454
    private static void workAroundABugInJava6()
    {
        try
        {
            SSLContext.getInstance("SSL").createSSLEngine();
        } catch (Exception ex)
        {
            // Ignore this one.
        }
    }

}
