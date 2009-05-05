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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AppController;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.LoginController;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DictonaryBasedMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;

/**
 * The {@link EntryPoint} implementation.
 * 
 * @author Franz-Josef Elmer
 * @author Izabela Adamczyk
 */
public final class Client implements EntryPoint
{
    private IViewContext<ICommonClientServiceAsync> viewContext;

    private List<Controller> controllers = new ArrayList<Controller>();

    public final IViewContext<ICommonClientServiceAsync> tryToGetViewContext()
    {
        return viewContext;
    }

    private final IViewContext<ICommonClientServiceAsync> createViewContext(
            final Controller openUrlController)
    {
        final ICommonClientServiceAsync service = GWT.create(ICommonClientService.class);
        final ServiceDefTarget endpoint = (ServiceDefTarget) service;
        endpoint.setServiceEntryPoint(GenericConstants.COMMON_SERVER_NAME);
        final IGenericImageBundle imageBundle =
                GWT.<IGenericImageBundle> create(IGenericImageBundle.class);
        final IMessageProvider messageProvider = new DictonaryBasedMessageProvider("common");
        final IPageController pageController = new IPageController()
            {
                //
                // IPageController
                //

                public final void reload(final boolean logout)
                {
                    if (logout)
                    {
                        initializeControllers(openUrlController);
                    }
                    onModuleLoad();
                }
            };
        return new CommonViewContext(service, messageProvider, imageBundle, pageController);
    }

    private final void initializeControllers(Controller openUrlController)
    {
        removeControllers();
        addController(new LoginController(viewContext));
        addController(new AppController((CommonViewContext) viewContext));
        addController(openUrlController);
    }

    public void removeControllers()
    {
        final Dispatcher dispatcher = Dispatcher.get();
        for (Controller controller : controllers)
        {
            dispatcher.removeController(controller);
        }
        controllers.clear();
    }

    private void addController(Controller controller)
    {
        final Dispatcher dispatcher = Dispatcher.get();
        dispatcher.addController(controller);
        controllers.add(controller);
    }

    public final void onModuleLoad()
    {
        onModuleLoad(WindowUtils.createOpenUrlController());
    }

    // @Private - exposed for tests
    public final void onModuleLoad(Controller openUrlController)
    {
        if (viewContext == null)
        {
            viewContext = createViewContext(openUrlController);
            initializeControllers(openUrlController);
        }

        final UrlParamsHelper urlParamsHelper = new UrlParamsHelper(viewContext);
        urlParamsHelper.initUrlParams();

        final IClientServiceAsync service = viewContext.getService();
        service.getApplicationInfo(new AbstractAsyncCallback<ApplicationInfo>(viewContext)
            {

                //
                // AbstractAsyncCallback
                //

                @Override
                public final void process(final ApplicationInfo info)
                {
                    viewContext.getModel().setApplicationInfo(info);
                    // the callback sets the SessionContext and redirects to the login page or the
                    // initial page and may additionaly open an initial tab
                    SessionContextCallback sessionContextCallback =
                            new SessionContextCallback((CommonViewContext) viewContext,
                                    urlParamsHelper.getOpenInitialTabAction());
                    service.tryToGetCurrentSessionContext(sessionContextCallback);
                }
            });
    }

}
