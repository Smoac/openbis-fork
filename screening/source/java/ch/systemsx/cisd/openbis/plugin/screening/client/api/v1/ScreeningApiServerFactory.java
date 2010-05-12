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

package ch.systemsx.cisd.openbis.plugin.screening.client.api.v1;

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;

// TODO: Refactor this and the OpenBisServiceFactory into a common implementation. The refactoring is easy, but in which project should the code go?
/**
 * A factory for creating proxies to the openBIS screening application server.
 * <p>
 * The ScreeningApiServerFactory will create a proxy by trying several possible locations for the
 * service.
 * <p>
 * After calling {@link #createService}, you can get the url createService used by calling the
 * {@link #getUsedServerUrl} method.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ScreeningApiServerFactory
{
    /**
     * An interface that can create a {@link IScreeningApiServer} proxy to a service located at a
     * given a URL.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static interface IScreeningApiServerStubFactory
    {
        /**
         * Create a proxy to the service located at the serverURL. Implementations should not alter
         * the url, e.g., by appending something to it.
         * 
         * @param serverUrl The URL of of the IScreeningApiServer service
         * @return IScreeningApiServer The service located at the serverUrl
         */
        public IScreeningApiServer createServiceStub(String serverUrl);
    }

    private final String initialServerUrl;

    private final String urlServiceSuffix;

    private final IScreeningApiServerStubFactory stubFactory;

    private String usedServerUrl;

    /**
     * Constructor for the ScreeningApiServerFactory. The service factory works best when the
     * serverUrl is simply the protocol://machine:port of the openBIS application server. It will
     * automatically append likely locations of the openBIS service to the url.
     * <p>
     * Examples:
     * <ul>
     * <li>ScreeningApiServerFactory("http://localhost:8888/", "/rmi-screening-api-v1", stubFactory)
     * </li>
     * <li>ScreeningApiServerFactory("https://openbis.ethz.ch:8443/", "/rmi-screening-api-v1",
     * stubFactory)</li>
     * </ul>
     * 
     * @param serverUrl The Url where the openBIS server is assumed to be.
     * @param urlServiceSuffix The suffix added to the url to reference the service.
     * @param stubFactory A factory that, given a url, returns an IETLLIMSService proxy to the url
     */
    public ScreeningApiServerFactory(String serverUrl, String urlServiceSuffix,
            IScreeningApiServerStubFactory stubFactory)
    {
        this.initialServerUrl = serverUrl;
        this.urlServiceSuffix = urlServiceSuffix;
        this.stubFactory = stubFactory;
        this.usedServerUrl = "";
    }

    /**
     * Create a IScreeningApiServer by trying several possible locations for the service until one
     * that works is found. If the service cannot be found, a proxy to the constructor-provided
     * serverUrl will be returned.
     */
    public IScreeningApiServer createService()
    {
        IScreeningApiServer service;
        usedServerUrl = computeOpenbisOpenbisServerUrl(initialServerUrl);
        // Try the url that ends in openbis/openbis
        service = stubFactory.createServiceStub(usedServerUrl + urlServiceSuffix);
        if (canConnectToService(service))
        {
            return service;
        }

        // Try the url that ends in just one openbis
        usedServerUrl = computeOpenbisServerUrl(initialServerUrl);
        service = stubFactory.createServiceStub(usedServerUrl + urlServiceSuffix);
        if (canConnectToService(service))
        {
            return service;
        }

        // Try the url as provided
        usedServerUrl = initialServerUrl;
        service = stubFactory.createServiceStub(usedServerUrl + urlServiceSuffix);
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

    private boolean canConnectToService(IScreeningApiServer service)
    {
        try
        {
            service.getMajorVersion();
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
