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
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.user.client.ui.RootPanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.LoginPage;

/**
 * Responsible for creating login page and displaying it.
 * 
 * @author Izabela Adamczyk
 */
public class LoginView extends View
{
    private LoginPage loginPage;

    private final GenericViewContext viewContext;

    public LoginView(final Controller controller, final GenericViewContext viewContext2)
    {
        super(controller);
        viewContext = viewContext2;
    }

    @Override
    protected void initialize()
    {
        loginPage = new LoginPage(viewContext);
    }

    @Override
    protected void handleEvent(final AppEvent<?> event)
    {
        switch (event.type)
        {
            case AppEvents.USER_NOT_LOGGED_IN:
                initUI();
                break;
        }
    }

    private void initUI()
    {
        RootPanel.get().clear();
        RootPanel.get().add(loginPage);
    }

}
