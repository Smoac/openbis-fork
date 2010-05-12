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

package ch.systemsx.cisd.openbis.generic.shared;

/**
 * A factory for creating proxies to the openBIS application server.
 * <p>
 * The OpenBisServiceFactory will create a proxy by trying several possible locations for the
 * service.
 * <p>
 * After calling {@link #createService}, you can get the url createService used by calling the
 * {@link #getUsedServerUrl} method.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class OpenBisServiceFactory
{
    /**
     * An interface that can create a {@link IETLLIMSService} proxy to a service located at a given
     * a URL.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static interface ILimsServiceStubFactory
    {
        /**
         * Create a proxy to the service located at the serverURL. Implementations should not alter
         * the url, e.g., by appending something to it.
         * 
         * @param serverUrl The URL of of the IETLLIMSService service
         * @return IETLLIMSService The service located at the serverUrl
         */
        public IETLLIMSService createServiceStub(String serverUrl);
    }

    private final String initialServerUrl;

    private final ILimsServiceStubFactory stubFactory;

    private String usedServerUrl;

    /**
     * Constructor for the OpenBisServiceFactory. The service factory works best when the serverUrl
     * is simply the protocol://machine:port of the openBIS application server. It will
     * automatically append likely locations of the openBIS service to the url.
     * <p>
     * Examples:
     * <ul>
     * <li>OpenBisServiceFactory("http://localhost:8888/", stubFactory)</li>
     * <li>OpenBisServiceFactory("https://openbis.ethz.ch:8443/", stubFactory)</li>
     * </ul>
     * 
     * @param serverUrl The Url where the openBIS server is assumed to be.
     * @param stubFactory A factory that, given a url, returns an IETLLIMSService proxy to the url
     */
    public OpenBisServiceFactory(String serverUrl, ILimsServiceStubFactory stubFactory)
    {
        this.initialServerUrl = serverUrl;
        this.stubFactory = stubFactory;
        this.usedServerUrl = "";
    }

    /**
     * Create a IETLLIMSService by trying several possible locations for the service until one that
     * works is found. If the service cannot be found, a proxy to the constructor-provided serverUrl
     * will be returned.
     */
    public IETLLIMSService createService()
    {
        IETLLIMSService service;
        usedServerUrl = computeOpenbisOpenbisServerUrl(initialServerUrl);
        // Try the url that ends in openbis/openbis
        service = stubFactory.createServiceStub(usedServerUrl + "/rmi-etl");
        if (canConnectToService(service))
        {
            return service;
        }

        // Try the url that ends in just one openbis
        usedServerUrl = computeOpenbisServerUrl(initialServerUrl);
        service = stubFactory.createServiceStub(usedServerUrl + "/rmi-etl");
        if (canConnectToService(service))
        {
            return service;
        }

        // Try the url as provided
        usedServerUrl = initialServerUrl;
        service = stubFactory.createServiceStub(usedServerUrl + "/rmi-etl");
        return service;
    }

    /**
     * Return the serverUrl used by the createService method. The result of this method only makes
     * sense after calling createService.
     */
    public String getUsedServerUrl()
    {
        return usedServerUrl;
    }

    private boolean canConnectToService(IETLLIMSService service)
    {
        try
        {
            service.getVersion();
        } catch (Exception e)
        {
            return false;
        }
        return true;
    }

    private String computeOpenbisOpenbisServerUrl(String serverUrl)
    {
        if (serverUrl.endsWith("/openbis/openbis"))
        {
            return serverUrl;
        }

        if (serverUrl.endsWith("/openbis"))
        {
            return serverUrl + "/openbis";
        }

        String myServerUrl = serverUrl;
        if (false == serverUrl.endsWith("/"))
        {
            myServerUrl = myServerUrl + "/";
        }
        return myServerUrl + "openbis/openbis";
    }

    private String computeOpenbisServerUrl(String serverUrl)
    {
        if (serverUrl.endsWith("/openbis/openbis"))
        {
            return serverUrl.substring(0, serverUrl.length() - "/openbis".length());
        }

        if (serverUrl.endsWith("/openbis"))
        {
            return serverUrl;
        }

        String myServerUrl = serverUrl;
        if (false == serverUrl.endsWith("/"))
        {
            myServerUrl = myServerUrl + "/";
        }
        return myServerUrl + "openbis";
    }
}
