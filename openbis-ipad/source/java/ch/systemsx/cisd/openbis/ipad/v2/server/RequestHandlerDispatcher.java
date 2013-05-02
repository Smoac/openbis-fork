/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.ipad.v2.server;

import java.util.Map;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchService;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.ISimpleTableModelBuilderAdaptor;

/**
 * A class that takes an aggregation service request and dispatches it to the appropriate handler.
 * 
 * @author cramakri
 */
public class RequestHandlerDispatcher
{

    public static enum RequestType
    {
        CLIENT_PREFS, NAVIGATION, ROOT, DRILL, DETAIL
    }

    private IRequestHandlerFactory clientPreferencesRequestHandlerFactory;

    private IRequestHandlerFactory navigationRequestHandlerFactory;

    private IRequestHandlerFactory rootRequestHandlerFactory;

    private IRequestHandlerFactory drillRequestHandlerFactory;

    private IRequestHandlerFactory detailRequestHandlerFactory;

    private IRequestHandlerFactory emptyDataRequestHandlerFactory;

    /**
     * The constructor initialized the clientPreferencesRequestHandlerFactory and
     * emptyDataRequestHandlerFactory variables. All others must be initialized by the client of
     * this object.
     */
    public RequestHandlerDispatcher()
    {
        clientPreferencesRequestHandlerFactory = new IRequestHandlerFactory()
            {
                @Override
                public IRequestHandler createRequestHandler(Map<String, Object> parameters,
                        ISimpleTableModelBuilderAdaptor builder, ISearchService searchService)
                {
                    return new ClientPreferencesRequestHandler(parameters, builder, searchService);
                }

            };
        emptyDataRequestHandlerFactory = new IRequestHandlerFactory()
            {
                @Override
                public IRequestHandler createRequestHandler(Map<String, Object> parameters,
                        ISimpleTableModelBuilderAdaptor builder, ISearchService searchService)
                {
                    return new EmptyDataRequestHandler(parameters, builder, searchService);
                }

            };
    }

    public IRequestHandlerFactory getClientPreferencesRequestHandlerFactory()
    {
        return clientPreferencesRequestHandlerFactory;
    }

    public void setClientPreferencesRequestHandlerFactory(
            IRequestHandlerFactory clientPreferencesRequestHandlerFactory)
    {
        this.clientPreferencesRequestHandlerFactory = clientPreferencesRequestHandlerFactory;
    }

    public IRequestHandlerFactory getNavigationRequestHandlerFactory()
    {
        return navigationRequestHandlerFactory;
    }

    public void setNavigationRequestHandlerFactory(
            IRequestHandlerFactory navigationRequestHandlerFactory)
    {
        this.navigationRequestHandlerFactory = navigationRequestHandlerFactory;
    }

    public IRequestHandlerFactory getRootRequestHandlerFactory()
    {
        return rootRequestHandlerFactory;
    }

    public void setRootRequestHandlerFactory(IRequestHandlerFactory rootRequestHandlerFactory)
    {
        this.rootRequestHandlerFactory = rootRequestHandlerFactory;
    }

    public IRequestHandlerFactory getDrillRequestHandlerFactory()
    {
        return drillRequestHandlerFactory;
    }

    public void setDrillRequestHandlerFactory(IRequestHandlerFactory drillRequestHandlerFactory)
    {
        this.drillRequestHandlerFactory = drillRequestHandlerFactory;
    }

    public IRequestHandlerFactory getDetailRequestHandlerFactory()
    {
        return detailRequestHandlerFactory;
    }

    public void setDetailRequestHandlerFactory(IRequestHandlerFactory detailRequestHandlerFactory)
    {
        this.detailRequestHandlerFactory = detailRequestHandlerFactory;
    }

    public IRequestHandlerFactory getEmptyDataRequestHandlerFactory()
    {
        return emptyDataRequestHandlerFactory;
    }

    public void setEmptyDataRequestHandlerFactory(
            IRequestHandlerFactory emptyDataRequestHandlerFactory)
    {
        this.emptyDataRequestHandlerFactory = emptyDataRequestHandlerFactory;
    }

    public void dispatch(Map<String, Object> parameters, ISimpleTableModelBuilderAdaptor builder,
            ISearchService searchService)
    {
        RequestType requestType = tryRequestType(parameters);
        IRequestHandlerFactory handlerFactory = getHandlerFactory(requestType);
        handlerFactory.createRequestHandler(parameters, builder, searchService).processRequest();
    }

    protected RequestType tryRequestType(Map<String, Object> parameters)
    {
        String requestKey = (String) parameters.get("requestKey");
        if (null == requestKey)
        {
            return null;
        }
        RequestType requestType;
        try
        {
            requestType = RequestType.valueOf(requestKey);
        } catch (IllegalArgumentException e)
        {
            requestType = null;
        }
        return requestType;
    }

    protected IRequestHandlerFactory getHandlerFactory(RequestType requestType)
    {
        if (null == requestType)
        {
            return emptyDataRequestHandlerFactory;
        }
        IRequestHandlerFactory handlerFactory;
        switch (requestType)
        {
            case CLIENT_PREFS:
                handlerFactory = clientPreferencesRequestHandlerFactory;
                break;
            case DETAIL:
                handlerFactory = detailRequestHandlerFactory;
                break;
            case DRILL:
                handlerFactory = drillRequestHandlerFactory;
                break;
            case NAVIGATION:
                handlerFactory = navigationRequestHandlerFactory;
                break;
            case ROOT:
                handlerFactory = rootRequestHandlerFactory;
                break;
            default:
                handlerFactory = emptyDataRequestHandlerFactory;
                break;
        }
        return handlerFactory;
    }
}
