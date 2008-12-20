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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.PersonsView;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.MainTabPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * A {@link AbstractDefaultTestCommand} extension for creating a person.
 * 
 * @author Christian Ribeaud
 */
public final class CreatePerson extends AbstractDefaultTestCommand
{
    private final String personCode;

    public CreatePerson(final String personCode)
    {
        super(PersonsView.ListPersonsCallback.class);
        this.personCode = personCode;
    }

    //
    // AbstractDefaultTestCommand
    //

    public final void execute()
    {
        GWTTestUtil.selectTabItemWithId(MainTabPanel.ID, PersonsView.ID + MainTabPanel.TAB_SUFFIX);
        GWTTestUtil.clickButtonWithID(PersonsView.ADD_BUTTON_ID);
        GWTTestUtil.getTextFieldWithID(AddPersonDialog.CODE_FIELD_ID).setValue(personCode);
        GWTTestUtil.clickButtonWithID(AddPersonDialog.SAVE_BUTTON_ID);
    }

}
