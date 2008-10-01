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

import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.CallbackClassCondition;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.ITestCommandWithCondition;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class CreateGroupCommand extends CallbackClassCondition implements ITestCommandWithCondition<Object>
{

    private final String groupCode;

    public CreateGroupCommand(String groupCode)
    {
        super(GroupsView.ListGroupsCallback.class);
        this.groupCode = groupCode;
    }

    public void execute()
    {
        GWTTestUtil.clickButtonWithID(GroupsView.ADD_BUTTON_ID);
        GWTTestUtil.getTextFieldWithID(AddGroupDialog.CODE_FIELD_ID).setValue(groupCode);
        GWTTestUtil.clickButtonWithID(AddGroupDialog.SAVE_BUTTON_ID);
    }

}
