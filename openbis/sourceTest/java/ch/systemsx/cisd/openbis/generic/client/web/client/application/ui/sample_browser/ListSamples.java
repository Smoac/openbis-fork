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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser;

import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * A {@link AbstractDefaultTestCommand} extension for creating sample list.
 * 
 * @author Christian Ribeaud
 */
public final class ListSamples extends AbstractDefaultTestCommand
{
    private final String sampleTypeNameOrNull;

    private final boolean includeShared;

    private final boolean includeGroup;

    private final String groupNameOrNull;

    public ListSamples(final boolean includeShared, final boolean includeGroup,
            final String groupNameOrNull, final String sampleTypeNameOrNull)
    {
        this.includeShared = includeShared;
        this.includeGroup = includeGroup;
        this.groupNameOrNull = groupNameOrNull;
        this.sampleTypeNameOrNull = sampleTypeNameOrNull;
        addCallbackClass(GroupSelectionWidget.ListGroupsCallback.class);
        addCallbackClass(SampleTypeSelectionWidget.ListSampleTypesCallback.class);
    }

    //
    // AbstractDefaultTestCommand
    //

    public final void execute()
    {
        final GroupSelectionWidget groupSelector =
                (GroupSelectionWidget) GWTTestUtil.getWidgetWithID(GroupSelectionWidget.ID);

        final ComboBox<SampleTypeModel> sampleTypeSelector =
                (SampleTypeSelectionWidget) GWTTestUtil
                        .getWidgetWithID(SampleTypeSelectionWidget.ID);

        final CheckBox includeGroupCheckbox =
                (CheckBox) GWTTestUtil
                        .getWidgetWithID(SampleBrowserToolbar.INCLUDE_GROUP_CHECKBOX_ID);
        final CheckBox includeSharedCheckbox =
                (CheckBox) GWTTestUtil
                        .getWidgetWithID(SampleBrowserToolbar.INCLUDE_SHARED_CHECKBOX_ID);

        GWTUtils.setSelectedItem(sampleTypeSelector, ModelDataPropertyNames.CODE,
                sampleTypeNameOrNull);
        includeSharedCheckbox.setValue(includeShared);
        includeGroupCheckbox.setValue(includeGroup);
        if (includeGroup)
        {
            GWTUtils.setSelectedItem(groupSelector, ModelDataPropertyNames.CODE, groupNameOrNull);
        }

        final Button refresh =
                (Button) GWTTestUtil.getWidgetWithID(SampleBrowserToolbar.REFRESH_BUTTON_ID);
        assertTrue(refresh.isEnabled());
        GWTTestUtil.clickButtonWithID(SampleBrowserToolbar.REFRESH_BUTTON_ID);
    }
}
