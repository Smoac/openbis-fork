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

package ch.systemsx.cisd.common.spring;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.remoting.httpinvoker.HttpInvokerRequestExecutor;

import com.marathon.util.spring.StreamSupportingHttpInvokerProxyFactoryBean;

/**
 * Utility methods for HTTP Invocations. <br>
 * This class is used by clients to create service stubs. If a dependency to external libraries is
 * added to this class then build scripts used to build the API jar have to be changed as well.
 * 
 * @author Franz-Josef Elmer
 */
public class HttpInvokerUtils
{
    private static class ClosableHttpInvokerProxyFactoryBean extends HttpInvokerProxyFactoryBean
    {
        @Override
        public Object invoke(MethodInvocation methodInvocation) throws Throwable
        {
            try
            {
                return super.invoke(methodInvocation);
            } finally
            {
                HttpInvokerRequestExecutor httpInvokerRequestExecutor = getHttpInvokerRequestExecutor();
                if (httpInvokerRequestExecutor instanceof CommonsHttpInvokerRequestExecutor)
                {
                    ((CommonsHttpInvokerRequestExecutor) httpInvokerRequestExecutor).getHttpClient().getHttpConnectionManager()
                            .closeIdleConnections(0);
                }
            }
        }
    }
    /**
     * Creates a service stub for the specified service interface.
     * 
     * @param serviceURL URL providing the service via HTTP tunneling.
     * @param serverTimeoutInMillis Service time out in milliseconds. A values of 0 means never
     *            timeout.
     */
    public static <T> T createServiceStub(final Class<T> serviceInterface, final String serviceURL,
            final long serverTimeoutInMillis)
    {
        final HttpInvokerProxyFactoryBean httpInvokerProxy = new ClosableHttpInvokerProxyFactoryBean();
        httpInvokerProxy.setBeanClassLoader(serviceInterface.getClassLoader());
        httpInvokerProxy.setServiceUrl(serviceURL);
        httpInvokerProxy.setServiceInterface(serviceInterface);
        final CommonsHttpInvokerRequestExecutor httpInvokerRequestExecutor =
                new CommonsHttpInvokerRequestExecutor();
        httpInvokerRequestExecutor.setReadTimeout((int) serverTimeoutInMillis);
        final InetSocketAddress proxyAddressOrNull = tryFindProxy(serviceURL);
        if (proxyAddressOrNull != null)
        {
            httpInvokerRequestExecutor.getHttpClient().getHostConfiguration().setProxy(
                    proxyAddressOrNull.getHostName(), proxyAddressOrNull.getPort());
        }
        httpInvokerProxy.setHttpInvokerRequestExecutor(httpInvokerRequestExecutor);
        httpInvokerProxy.afterPropertiesSet();
        return getCastedService(httpInvokerProxy);
    }

    public static <T> T createStreamSupportingServiceStub(final Class<T> serviceInterface,
            final String serviceURL, final long serverTimeoutInMillis)
    {
        final StreamSupportingHttpInvokerProxyFactoryBean httpInvokerProxy =
                new StreamSupportingHttpInvokerProxyFactoryBean();
        httpInvokerProxy.setBeanClassLoader(serviceInterface.getClassLoader());
        httpInvokerProxy.setServiceUrl(serviceURL);
        httpInvokerProxy.setServiceInterface(serviceInterface);
        ((CommonsHttpInvokerRequestExecutor) httpInvokerProxy.getHttpInvokerRequestExecutor())
                .setReadTimeout((int) serverTimeoutInMillis);
        final InetSocketAddress proxyAddressOrNull = HttpInvokerUtils.tryFindProxy(serviceURL);
        if (proxyAddressOrNull != null)
        {
            ((CommonsHttpInvokerRequestExecutor) httpInvokerProxy.getHttpInvokerRequestExecutor())
                    .getHttpClient().getHostConfiguration().setProxy(
                            proxyAddressOrNull.getHostName(), proxyAddressOrNull.getPort());
        }
        httpInvokerProxy.afterPropertiesSet();
        return getCastedService(httpInvokerProxy);
    }

    @SuppressWarnings("unchecked")
    private final static <T> T getCastedService(final HttpInvokerProxyFactoryBean httpInvokerProxy)
    {
        return (T) httpInvokerProxy.getObject();
    }

    /**
     * Returns the proxy's inet address for <var>serviceURL</var>, or <code>null</code>, if no proxy
     * is defined.
     */
    public static InetSocketAddress tryFindProxy(String serviceURL)
    {
        try
        {
            final ProxySelector selector = ProxySelector.getDefault();
            final List<java.net.Proxy> proxyList = selector.select(new URI(serviceURL));
            for (java.net.Proxy proxy : proxyList)
            {
                if (java.net.Proxy.Type.HTTP == proxy.type())
                {
                    return (InetSocketAddress) proxy.address();
                }
            }
        } catch (IllegalArgumentException e)
        {
        } catch (URISyntaxException ex)
        {
        }
        return null;
    }

    private HttpInvokerUtils()
    {
    }

}
