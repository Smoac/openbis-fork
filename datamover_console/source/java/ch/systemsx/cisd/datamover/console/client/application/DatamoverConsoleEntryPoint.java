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

package ch.systemsx.cisd.datamover.console.client.application;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.datamover.console.client.IDatamoverConsoleService;
import ch.systemsx.cisd.datamover.console.client.IDatamoverConsoleServiceAsync;
import ch.systemsx.cisd.datamover.console.client.application.ui.Console;
import ch.systemsx.cisd.datamover.console.client.application.ui.HeaderFooterDecorator;
import ch.systemsx.cisd.datamover.console.client.application.ui.LoginWidget;
import ch.systemsx.cisd.datamover.console.client.dto.ApplicationInfo;
import ch.systemsx.cisd.datamover.console.client.dto.User;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DatamoverConsoleEntryPoint implements EntryPoint
{

    private final static IDatamoverConsoleServiceAsync createService()
    {
        final IDatamoverConsoleServiceAsync service =
                GWT.<IDatamoverConsoleServiceAsync>create(IDatamoverConsoleService.class);
        final ServiceDefTarget endpoint = (ServiceDefTarget) service;
        endpoint.setServiceEntryPoint(Constants.SERVER_NAME);
        return service;
    }
    
    private IPageController pageController = new IPageController()
        {
            private Timer timer;

            public void setTimer(Timer timer)
            {
                this.timer = timer;
            }

            public void reload()
            {
                if (timer != null)
                {
                    timer.cancel();
                }
                onModuleLoad();
            }
        };
    private ViewContext viewContext;
    
    public void onModuleLoad()
    {
        setupViewContext();
        final IDatamoverConsoleServiceAsync service = viewContext.getService();
        service.getApplicationInfo(new AbstractAsyncCallback<ApplicationInfo>(viewContext)
            {
                public void onSuccess(ApplicationInfo info)
                {
                    viewContext.getModel().setApplicationInfo(info);
                    service.tryToGetCurrentUser(new AbstractAsyncCallback<User>(viewContext)
                        {
                            public void onSuccess(User user)
                            {
                                RootPanel rootPanel = RootPanel.get();
                                rootPanel.clear();
                                Widget widget;
                                if (user == null)
                                {
                                    widget = new LoginWidget(viewContext);
                                } else
                                {
                                    viewContext.getModel().setUser(user);
                                    widget = new Console(viewContext);
                                }
                                rootPanel.add(new HeaderFooterDecorator(widget, viewContext));
                            }
                        });

                }
            });
    }
    
    private void setupViewContext()
    {
        if (viewContext == null)
        {
            final IMessageResources messageResources =
                    GWT.<IMessageResources> create(IMessageResources.class);
            IImageBundle imageBundle = GWT.<IImageBundle> create(IImageBundle.class);
            viewContext =
                    new ViewContext(createService(), pageController, messageResources, imageBundle);
        }
    }

}
