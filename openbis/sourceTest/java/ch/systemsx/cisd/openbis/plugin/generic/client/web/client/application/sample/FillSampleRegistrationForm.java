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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import com.extjs.gxt.ui.client.widget.form.CheckBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * A {@link AbstractDefaultTestCommand} extension for creating sample list.
 * 
 * @author Christian Ribeaud
 */
public final class FillSampleRegistrationForm extends AbstractDefaultTestCommand
{

    private final String code;

    private final String groupNameOrNull;

    private final boolean includeShared;

    private String parent;
    
    private String container;

    public FillSampleRegistrationForm(final boolean includeShared, final String groupNameOrNull,
            final String code)
    {
        this.includeShared = includeShared;
        this.groupNameOrNull = groupNameOrNull;
        this.code = code;
        addCallbackClass(GroupSelectionWidget.ListGroupsCallback.class);
    }
    
    public FillSampleRegistrationForm parent(String parentFieldValue)
    {
        this.parent = parentFieldValue;
        return this;
    }

    public FillSampleRegistrationForm container(String containerFieldValue)
    {
        this.container = containerFieldValue;
        return this;
    }
    
    //
    // AbstractDefaultTestCommand
    //

    @SuppressWarnings("unchecked")
    public final void execute()
    {
        GWTTestUtil.setTextFieldValue(GenericSampleRegistrationForm.CODE_FIELD_ID, code);

        final CheckBox includeSharedCheckbox =
                (CheckBox) GWTTestUtil
                        .getWidgetWithID(GenericSampleRegistrationForm.SHARED_CHECKBOX_ID);
        includeSharedCheckbox.setValue(includeShared);

        if (includeShared == false)
        {
            final GroupSelectionWidget groupSelector =
                    (GroupSelectionWidget) GWTTestUtil.getWidgetWithID(GroupSelectionWidget.ID);
            GWTUtils.setSelectedItem(groupSelector, ModelDataPropertyNames.CODE, groupNameOrNull);
        }
        
        GWTTestUtil.setTextFieldValue(GenericSampleRegistrationForm.PARENT_GENERATOR_FIELD_ID, parent);
        GWTTestUtil.setTextFieldValue(GenericSampleRegistrationForm.PARENT_CONTAINER_FIELD_ID, container);

        GWTTestUtil.clickButtonWithID(GenericSampleRegistrationForm.SAVE_BUTTON_ID);
    }
}
