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

import com.extjs.gxt.ui.client.widget.form.TextField;

import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * A {@link AbstractDefaultTestCommand} extension which triggers a search just after a login has
 * been performed with specified parameters.
 * 
 * @author Christian Ribeaud
 */
final class SearchCommand extends AbstractDefaultTestCommand
{
    private final String searchString;

    SearchCommand(final String searchString)
    {
        super(SessionContextCallback.class);
        this.searchString = searchString;
    }

    //
    // AbstractDefaultTestCommand
    //

    public final void execute()
    {
        final TextField<String> textField =
                GWTTestUtil.getTextFieldWithID(SearchWidget.TEXT_FIELD_ID);
        textField.setValue(searchString);
        assertEquals(searchString, textField.getValue());
        GWTTestUtil.clickButtonWithID(SearchWidget.SUBMIT_BUTTON_ID);
    }

}
