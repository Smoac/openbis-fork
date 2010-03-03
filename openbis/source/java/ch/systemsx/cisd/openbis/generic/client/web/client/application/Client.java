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
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AppController;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AppEvents;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.LoginController;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.IViewLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.OpenViewAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.PermlinkLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.SearchLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocatorResolverRegistry;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.DefaultClientPluginFactoryProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactoryProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DictonaryBasedMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;

/**
 * The {@link EntryPoint} implementation.
 * 
 * @author Franz-Josef Elmer
 * @author Izabela Adamczyk
 */
public class Client implements EntryPoint
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
        CommonViewContext commonContext =
                new CommonViewContext(service, messageProvider, imageBundle, pageController);
        commonContext.setClientPluginFactoryProvider(createPluginFactoryProvider(commonContext));
        initializeLocatorHandlerRegistry(commonContext.getLocatorResolverRegistry(), commonContext);
        return commonContext;
    }

    /**
     * Creates the provider for client plugin factories. Can be overridden in subclasses.
     */
    protected IClientPluginFactoryProvider createPluginFactoryProvider(
            IViewContext<ICommonClientServiceAsync> commonContext)
    {
        return new DefaultClientPluginFactoryProvider(commonContext);
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

        final ViewLocator locator = createViewLocator(GWTUtils.getParamString());

        final IClientServiceAsync service = getServiceForRetrievingApplicationInfo(viewContext);
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
                                    new OpenViewAction(viewContext.getLocatorResolverRegistry(),
                                            locator));
                    service.tryToGetCurrentSessionContext(sessionContextCallback);
                }
            });
    }

    public static ViewLocator createViewLocator(String urlParams)
    {
        return new ViewLocator(URL.decodeComponent(urlParams));
    }

    protected IClientServiceAsync getServiceForRetrievingApplicationInfo(
            IViewContext<ICommonClientServiceAsync> context)
    {
        return context.getService();
    }

    /**
     * A version of onModuleLoad specifically for tests. Ignore the state in the session and go
     * directly to the login page.
     */
    public final void onModuleLoadTest()
    {
        Controller openUrlController = WindowUtils.createOpenUrlController();
        if (viewContext == null)
        {
            viewContext = createViewContext(openUrlController);
            initializeControllers(openUrlController);
        }

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
                    final Dispatcher dispatcher = Dispatcher.get();
                    dispatcher.dispatch(AppEvents.LOGIN);
                }
            });
    }

    /**
     * Callback class which handles return value
     * {@link ICommonClientService#tryToGetCurrentSessionContext()}.
     * 
     * @author Franz-Josef Elmer
     */
    private static final class SessionContextCallback extends AbstractAsyncCallback<SessionContext>
    {
        private final IDelegatedAction afterInitAction;

        /**
         * @param afterInitAction action executed after application init (if user is logged in)
         */
        SessionContextCallback(final CommonViewContext viewContext,
                final IDelegatedAction afterInitAction)
        {
            super(viewContext);
            this.afterInitAction = afterInitAction;

        }

        //
        // AbstractAsyncCallback
        //

        @Override
        public final void process(final SessionContext sessionContext)
        {
            final Dispatcher dispatcher = Dispatcher.get();
            if (sessionContext == null)
            {
                dispatcher.dispatch(AppEvents.LOGIN);
            } else
            {
                viewContext.getModel().setSessionContext(sessionContext);
                // NOTE: Display settings manager needs to be reinitialized after login.
                // Otherwise if two users used the same browser one after another without server
                // restart than display settings of the user that logged in first would be used
                // also for the second user.
                viewContext.initDisplaySettingsManager();
                dispatcher.dispatch(AppEvents.INIT);
                afterInitAction.execute();
                GWTUtils.setAllowConfirmOnExit(true);
            }
        }
    }

    /**
     * Register any handlers for locators specified in the openBIS URL.
     * 
     * @param handlerRegistry The handler registry to initialize
     * @param context The ViewContext which may be needed by resolvers. Can't use the viewContext
     *            instance variable since it may not have been initialized yet.
     */
    protected void initializeLocatorHandlerRegistry(ViewLocatorResolverRegistry handlerRegistry,
            IViewContext<ICommonClientServiceAsync> context)
    {
        IViewLocatorResolver handler;
        handler = new PermlinkLocatorResolver(context);
        handlerRegistry.registerHandler(handler);

        handler = new SearchLocatorResolver(context);
        handlerRegistry.registerHandler(handler);
    }
}
