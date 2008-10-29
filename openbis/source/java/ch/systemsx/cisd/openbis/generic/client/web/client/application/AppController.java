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

import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;

/**
 * @author Izabela Adamczyk
 */
public class AppController extends Controller
{

    private AppView appView;

    private LoginView loginView;

    private final GenericViewContext viewContext;

    public AppController(GenericViewContext viewContext)
    {
        this.viewContext = viewContext;
        registerEventTypes(AppEvents.Init);
        registerEventTypes(AppEvents.UserNotLoggedIn);
        registerEventTypes(AppEvents.MenuEvent);
    }

    @Override
    public void handleEvent(AppEvent<?> event)
    {
        switch (event.type)
        {
            case AppEvents.UserNotLoggedIn:
                onLogin(event);
                break;
            case AppEvents.Init:
                onInit(event);
                break;
            case AppEvents.MenuEvent:
                onLeftMenuSelectionChanged(event);
                break;
        }
    }

    private void onLeftMenuSelectionChanged(AppEvent<?> event)
    {
        forwardToView(appView, event);
    }

    @Override
    public void initialize()
    {
        appView = new AppView(this, viewContext);
        loginView = new LoginView(this, viewContext);
    }

    private void onLogin(AppEvent<?> event)
    {
        forwardToView(loginView, event);
    }

    private void onInit(AppEvent<?> event)
    {
        forwardToView(appView, event);
    }

}
