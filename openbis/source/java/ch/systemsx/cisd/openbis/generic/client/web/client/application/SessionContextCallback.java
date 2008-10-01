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

import com.extjs.gxt.ui.client.widget.Component;
import com.google.gwt.user.client.ui.RootPanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.LoginPage;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;

/**
 * Callback class which handles return value
 * {@link IGenericClientService#tryToGetCurrentSessionContext()}.
 * 
 * @author Franz-Josef Elmer
 */
public final class SessionContextCallback extends AbstractAsyncCallback<SessionContext>
{
    SessionContextCallback(GenericViewContext viewContext)
    {
        super(viewContext);
    }

    @Override
    public void process(SessionContext sessionContext)
    {
        System.out.println("SessionContextCallback.process() "+sessionContext);
        RootPanel rootPanel = RootPanel.get();
        rootPanel.clear();
        Component widget;
        if (sessionContext == null)
        {
            widget = new LoginPage(viewContext);
        } else
        {
            viewContext.getModel().setSessionContext(sessionContext);
            widget = new Application(viewContext);
        }
        rootPanel.add(widget);
    }
}