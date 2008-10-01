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

import junit.framework.Assert;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CallbackClassCondition;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.ITestCommandWithCondition;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class LogoutCommand extends CallbackClassCondition implements ITestCommandWithCondition<Object>
{
    public LogoutCommand()
    {
        super(LoginWidget.LoginCallback.class);
    }

    public void execute()
    {
        System.out.println(RootPanel.get());
        Widget w = GWTTestUtil.getWidgetWithID(TopMenu.LOGOUT_BUTTON_ID);
        Assert.assertTrue("Widget '" + TopMenu.LOGOUT_BUTTON_ID + "' is not a TextToolItem",
                w instanceof TextToolItem);
        TextToolItem textToolItem = (TextToolItem) w;
        textToolItem.fireEvent(Events.Select);
    }

}
