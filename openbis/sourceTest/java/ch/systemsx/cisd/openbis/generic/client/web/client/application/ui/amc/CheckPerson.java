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

import junit.framework.Assert;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * A {@link AbstractDefaultTestCommand} extension to check whether a given group is present.
 * 
 * @author Franz-Josef Elmer
 */
public final class CheckPerson extends AbstractDefaultTestCommand
{
    private final String personCode;

    public CheckPerson(final String personCode)
    {
        super(PersonsView.ListPersonsCallback.class);
        this.personCode = personCode;
    }

    //
    // AbstractDefaultTestCommand
    //

    @SuppressWarnings("unchecked")
    public final void execute()
    {
        final Widget widget = GWTTestUtil.getWidgetWithID(PersonsView.TABLE_ID);
        Assert.assertTrue(widget instanceof Grid);
        final Grid<PersonModel> table = (Grid<PersonModel>) widget;
        final ListStore<PersonModel> store = table.getStore();
        for (int i = 0, n = store.getCount(); i < n; i++)
        {
            final PersonModel personModel = store.getAt(i);
            final Object value = personModel.get(PersonModel.USER_ID);
            if (personCode.equals(value))
            {
                return;
            }
        }
        Assert.fail("No person with code '" + personCode + "' found.");
    }

}
