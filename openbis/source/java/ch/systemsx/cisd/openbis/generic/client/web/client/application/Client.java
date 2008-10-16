/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DictonaryBasedMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;

/**
 * @author Franz-Josef Elmer
 * @author Izabela Adamczyk
 */
public class Client implements EntryPoint
{
    private GenericViewContext viewContext;

    public final GenericViewContext tryToGetViewContext()
    {
        return viewContext;
    }

    public void onModuleLoad()
    {
        if (viewContext == null)
        {
            viewContext = createViewContext();
        }
        final IGenericClientServiceAsync service = viewContext.getService();
        service.getApplicationInfo(new AbstractAsyncCallback<ApplicationInfo>(viewContext)
            {
                @Override
                public void process(ApplicationInfo info)
                {
                    viewContext.getModel().setApplicationInfo(info);
                    service.tryToGetCurrentSessionContext(new SessionContextCallback(viewContext));
                }
            });
    }

    private GenericViewContext createViewContext()
    {
        IGenericClientServiceAsync service = GWT.create(IGenericClientService.class);
        ServiceDefTarget endpoint = (ServiceDefTarget) service;
        endpoint.setServiceEntryPoint(GenericConstants.SERVER_NAME);
        IGenericImageBundle imageBundle =
                GWT.<IGenericImageBundle> create(IGenericImageBundle.class);
        IMessageProvider messageProvider = new DictonaryBasedMessageProvider("generic");
        IPageController pageController = new IPageController()
            {
                public void reload()
                {
                    onModuleLoad();
                }
            };
        return new GenericViewContext(service, messageProvider, imageBundle, pageController);
    }

}
