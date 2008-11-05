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

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.SessionContextCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TopMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * Command for logout after {@link SessionContextCallback} has finished.
 * 
 * @author Franz-Josef Elmer
 */
public class Logout extends AbstractDefaultTestCommand
{
    public Logout()
    {
        super(SessionContextCallback.class);
    }

    public void execute()
    {
        final Widget w = GWTTestUtil.getWidgetWithID(TopMenu.LOGOUT_BUTTON_ID);
        assertTrue("Widget '" + TopMenu.LOGOUT_BUTTON_ID + "' is not a TextToolItem",
                w instanceof TextToolItem);
        final TextToolItem textToolItem = (TextToolItem) w;
        textToolItem.fireEvent(Events.Select);
    }

}
