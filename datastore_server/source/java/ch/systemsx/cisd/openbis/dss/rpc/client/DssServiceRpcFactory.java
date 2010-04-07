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

package ch.systemsx.cisd.openbis.dss.rpc.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
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
import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;

import com.marathon.util.spring.StreamSupportingHttpInvokerProxyFactoryBean;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.dss.rpc.shared.DssServiceRpcInterface;
import ch.systemsx.cisd.openbis.dss.rpc.shared.IDssServiceRpc;
import ch.systemsx.cisd.openbis.dss.rpc.shared.IDssServiceRpcNameServer;

/**
 * Client-side factory for DssServiceRpc objects.
 * <p>
 * Create client-side proxies to server RPC interface objects. The server does not require a
 * factory, since it is configured by Spring.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssServiceRpcFactory implements IDssServiceRpcFactory
{
    private static final int SERVER_TIMEOUT_MIN = 5;

    private static final String NAME_SERVER_SUFFIX = "/rpc";

    public DssServiceRpcInterface[] getSupportedInterfaces(String serverURL,
            boolean getServerCertificateFromServer) throws IncompatibleAPIVersionsException
    {
        // We assume the location of the name server follows the convention
        String nameServerURL = serverURL + NAME_SERVER_SUFFIX;
        Class<IDssServiceRpcNameServer> clazz = IDssServiceRpcNameServer.class;
        if (getServerCertificateFromServer)
        {
            new SslCertificateHelper(nameServerURL, getConfigDirectory()).setUpKeyStore();
        }

        IDssServiceRpcNameServer nameServer =
                new ServiceProxyBuilder<IDssServiceRpcNameServer>(nameServerURL, clazz,
                        SERVER_TIMEOUT_MIN, 1).getServiceInterface();
        return nameServer.getSupportedInterfaces();
    }

    public <T extends IDssServiceRpc> T getService(DssServiceRpcInterface iface,
            Class<T> ifaceClazz, String serverURL, boolean getServerCertificateFromServer)
            throws IncompatibleAPIVersionsException
    {
        String serviceURL = serverURL + iface.getInterfaceUrlSuffix();
        if (getServerCertificateFromServer)
        {
            new SslCertificateHelper(serviceURL, getConfigDirectory()).setUpKeyStore();
        }

        return new ServiceProxyBuilder<T>(serviceURL, ifaceClazz, SERVER_TIMEOUT_MIN, 1)
                .getServiceInterface();
    }

    private File getConfigDirectory()
    {
        String homeDir = System.getProperty("dss.root");
        File configDir;
        if (homeDir != null)
        {
            configDir = new File(homeDir, "etc");
        } else
        {
            homeDir = System.getProperty("user.home");
            configDir = new File(homeDir, ".dss");
        }
        configDir.mkdirs();
        return configDir;
    }
}

/**
 * Internal helper class for constructing service proxies;
 * 
 * @author Chandrasekhar Ramakrishnan
 */
@SuppressWarnings("unchecked")
class ServiceProxyBuilder<T extends IDssServiceRpc>
{
    private final String serviceURL;

    private final Class<?> clazz;

    private final int serverTimeoutMin;

    private final int apiClientVersion;

    ServiceProxyBuilder(String serviceURL, Class<?> clazz, int serverTimeoutMin,
            int apiClientVersion)
    {
        this.serviceURL = serviceURL;
        this.clazz = clazz;
        this.serverTimeoutMin = serverTimeoutMin;
        this.apiClientVersion = apiClientVersion;
    }

    T getServiceInterface() throws IncompatibleAPIVersionsException
    {
        T service = getRawServiceProxy();
        service = wrapProxyInServiceInvocationHandler(service);
        final int apiServerVersion = service.getVersion();
        final int apiMinClientVersion = service.getMinClientVersion();
        if (apiClientVersion < apiMinClientVersion || apiClientVersion > apiServerVersion)
        {
            throw new IncompatibleAPIVersionsException(apiClientVersion, apiServerVersion,
                    apiMinClientVersion);
        }

        return service;
    }

    private T getRawServiceProxy()
    {
        final StreamSupportingHttpInvokerProxyFactoryBean httpInvokerProxy =
                new StreamSupportingHttpInvokerProxyFactoryBean();
        httpInvokerProxy.setServiceUrl(serviceURL);
        httpInvokerProxy.setServiceInterface(clazz);
        ((CommonsHttpInvokerRequestExecutor) httpInvokerProxy.getHttpInvokerRequestExecutor())
                .setReadTimeout((int) DateUtils.MILLIS_PER_MINUTE * serverTimeoutMin);
        final InetSocketAddress proxyAddressOrNull = HttpInvokerUtils.tryFindProxy(serviceURL);
        if (proxyAddressOrNull != null)
        {
            ((CommonsHttpInvokerRequestExecutor) httpInvokerProxy.getHttpInvokerRequestExecutor())
                    .getHttpClient().getHostConfiguration().setProxy(
                            proxyAddressOrNull.getHostName(), proxyAddressOrNull.getPort());
        }
        httpInvokerProxy.afterPropertiesSet();
        return (T) httpInvokerProxy.getObject();
    }

    private T wrapProxyInServiceInvocationHandler(T service)
    {
        final ClassLoader classLoader = DssServiceRpcFactory.class.getClassLoader();
        final ServiceInvocationHandler invocationHandler = new ServiceInvocationHandler(service);
        final T proxy = (T) Proxy.newProxyInstance(classLoader, new Class[]
            { clazz }, invocationHandler);
        return proxy;
    }

    /**
     * An invocation handler that unwraps exceptions that occur in methods called via reflection.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static final class ServiceInvocationHandler implements InvocationHandler
    {
        private final IDssServiceRpc service;

        private ServiceInvocationHandler(IDssServiceRpc service)
        {
            this.service = service;
        }

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
}

/**
 * Internal helper class for handling SSL overhead.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class SslCertificateHelper
{
    private final String serviceURL;

    private final File configDirectory;

    SslCertificateHelper(String serviceURL, File configDirectory)
    {
        this.serviceURL = serviceURL;
        this.configDirectory = configDirectory;
    }

    void setUpKeyStore()
    {
        if (serviceURL.startsWith("https"))
        {
            Certificate[] certificates = getServerCertificate();
            KeyStore keyStore;
            try
            {
                keyStore = KeyStore.getInstance("JKS");
                keyStore.load(null, null);
                for (int i = 0; i < certificates.length; i++)
                {
                    keyStore.setCertificateEntry("dss" + i, certificates[i]);
                }
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
            FileOutputStream fileOutputStream = null;
            try
            {
                File keyStoreFile = new File(configDirectory, "keystore");
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

    private Certificate[] getServerCertificate()
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

    private void setUpAllAcceptingTrustManager()
    {
        TrustManager[] trustAllCerts = new TrustManager[]
            { new X509TrustManager()
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

    // WORKAROUND: see comment submitted on 31-JAN-2008 for
    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6514454
    private void workAroundABugInJava6()
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
