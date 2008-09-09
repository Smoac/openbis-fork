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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.User;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class Header extends HorizontalPanel
{
    private static final String PREFIX = "header_";
    
    public Header(final GenericViewContext viewContext)
    {
        setSpacing(10);
        add(new Text(createUserInfo(viewContext)));
        SelectionListener<ComponentEvent> listener = new SelectionListener<ComponentEvent>()
            {
                @Override
                public void componentSelected(ComponentEvent ce)
                {
                    viewContext.getService().logout(new AbstractAsyncCallback<Void>(viewContext)
                        {
                            public void onSuccess(Void result)
                            {
                                viewContext.getPageController().reload();
                            }
                        });
                }

            };
        add(new Button(viewContext.getMessage(PREFIX + "logoutButtonLabel"), listener));
    }

    private String createUserInfo(GenericViewContext viewContext)
    {
        SessionContext sessionContext = viewContext.getModel().getSessionContext();
        User user = sessionContext.getUser();
        String userName = user.getUserName();
        String homeGroup = user.getHomeGroupCode();
        if (homeGroup == null)
        {
            return viewContext.getMessage(PREFIX + "userWithoutHomegroup", userName);
        }
        return viewContext.getMessage(PREFIX + "userWithHomegroup", userName, homeGroup);
    }
}
