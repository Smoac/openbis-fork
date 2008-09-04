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

import java.util.Date;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.LoginWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DictonaryBasedMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class Client implements EntryPoint
{
    private GenericViewContext viewContext;
    
    public void onModuleLoad()
    {
        if (viewContext == null)
        {
            viewContext = createViewContext();
        }
        final IGenericClientServiceAsync service = viewContext.getService();
        service.getApplicationInfo(new AbstractAsyncCallback<ApplicationInfo>(viewContext)
                {
                    public void onSuccess(ApplicationInfo info)
                    {
                        viewContext.getModel().setApplicationInfo(info);
                        service.tryToGetCurrentSessionContext(new AbstractAsyncCallback<SessionContext>(viewContext)
                            {
                                public void onSuccess(SessionContext sessionContext)
                                {
                                    RootPanel rootPanel = RootPanel.get();
                                    rootPanel.clear();
                                    Component widget;
                                    if (sessionContext == null)
                                    {
                                        widget = createLoginPage();
                                    } else
                                    {
                                        viewContext.getModel().setSessionContext(sessionContext);
                                        widget = new Application(viewContext);
                                    }
                                    rootPanel.add(decorateWithFooter(widget));
                                }
                            });

                    }
                });
    }

    private GenericViewContext createViewContext()
    {
        IGenericClientServiceAsync service = GWT.create(IGenericClientService.class);
        ServiceDefTarget endpoint = (ServiceDefTarget) service;
        endpoint.setServiceEntryPoint(GenericConstants.SERVER_NAME);
        IGenericImageBundle imageBundle = GWT.<IGenericImageBundle> create(IGenericImageBundle.class);
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
    
    private LayoutContainer createLoginPage()
    {
        LayoutContainer container = new LayoutContainer();
        container.setLayout(new CenterLayout());
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setSpacing(30);
        verticalPanel.setWidth(600);
        verticalPanel.setHorizontalAlign(HorizontalAlignment.CENTER);
        container.add(verticalPanel);
        
        HorizontalPanel headerPanel = new HorizontalPanel();
        headerPanel.setSpacing(10);
        headerPanel.add(viewContext.getImageBundle().getLogo().createImage());
        Text welcomeLabel = new Text(viewContext.getMessage("welcome", new Date()));
        welcomeLabel.setStyleName("header-title");
        headerPanel.add(welcomeLabel);
        verticalPanel.add(headerPanel);
        HorizontalPanel horizontalPanel = new HorizontalPanel();
        verticalPanel.add(horizontalPanel);
        
        LoginWidget loginWidget = new LoginWidget(viewContext);
        verticalPanel.add(loginWidget);
        return container;
    }
    
    private Component decorateWithFooter(Component widget)
    {
        LayoutContainer container = new LayoutContainer();
        container.setLayout(new BorderLayout());
        
        container.add(widget, new BorderLayoutData(LayoutRegion.CENTER));
        
        String version = viewContext.getModel().getApplicationInfo().getVersion();
        Text footerText = new Text(viewContext.getMessage("footer", version));
        footerText.setStyleName("footer-text");
        container.add(footerText, new BorderLayoutData(LayoutRegion.SOUTH));
        
        Viewport viewport = new Viewport();
        viewport.setLayout(new FitLayout());
        viewport.add(container);
        return viewport;
    }
    
}
