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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.CallbackClassCondition;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ITestCommandWithCondition;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SessionContextCallback;

/**
 * Command for login after {@link SessionContextCallback} has finished.
 *
 * @author Franz-Josef Elmer
 */
public class LoginCommand extends CallbackClassCondition implements ITestCommandWithCondition<Object>
{
    private final String user;
    private final String password;

    public LoginCommand(String user, String password)
    {
        super(SessionContextCallback.class);
        this.user = user;
        this.password = password;
    }

    @SuppressWarnings("unchecked")
    public void execute()
    {
        GWTTestUtil.<String>getTextFieldWithID(LoginWidget.USER_FIELD_ID).setValue(user);
        GWTTestUtil.<String>getTextFieldWithID(LoginWidget.PASSWORD_FIELD_ID).setValue(password);
        GWTTestUtil.getButtonWithID(LoginWidget.BUTTON_ID).fireEvent(Events.Select);
    }

}
