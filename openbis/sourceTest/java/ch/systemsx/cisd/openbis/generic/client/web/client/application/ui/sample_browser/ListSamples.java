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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupModel;
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

    private final String groupNameOrNull;

    private final boolean includeGroup;

    private final boolean includeShared;

    public ListSamples(final String sampleTypeNameOrNull, final String groupNameOrNull,
            final boolean includeGroup, final boolean includeShared)
    {
        addCallbackClass(GroupSelectionWidget.ListGroupsCallback.class);
        addCallbackClass(SampleTypeSelectionWidget.ListSampleTypesCallback.class);
        assert sampleTypeNameOrNull != null && groupNameOrNull != null;
        this.sampleTypeNameOrNull = sampleTypeNameOrNull;
        this.groupNameOrNull = groupNameOrNull;
        this.includeGroup = includeGroup;
        this.includeShared = includeShared;
    }

    //
    // AbstractDefaultTestCommand
    //

    public final void execute()
    {

        GroupSelectionWidget groupSelector =
                (GroupSelectionWidget) GWTTestUtil.getWidgetWithID(GroupSelectionWidget.ID);

        SampleTypeSelectionWidget sampleTypeSelector =
                (SampleTypeSelectionWidget) GWTTestUtil
                        .getWidgetWithID(SampleTypeSelectionWidget.ID);

        CheckBox includeGroupCheckbox =
                (CheckBox) GWTTestUtil
                        .getWidgetWithID(SampleBrowserToolbar.INCLUDE_GROUP_CHECKBOX_ID);
        CheckBox includeSharedCheckbox =
                (CheckBox) GWTTestUtil
                        .getWidgetWithID(SampleBrowserToolbar.INCLUDE_GROUP_CHECKBOX_ID);

        sampleTypeSelector.setSelectedItem(SampleTypeModel.CODE, sampleTypeNameOrNull);
        includeGroupCheckbox.setValue(includeShared);
        if (includeGroup)
        {
            groupSelector.setSelectedItem(GroupModel.CODE, groupNameOrNull);
        }
        includeSharedCheckbox.setValue(includeShared);

        Button refresh =
                (Button) GWTTestUtil.getWidgetWithID(SampleBrowserToolbar.REFRESH_BUTTON_ID);
        assert refresh.isEnabled();
        GWTTestUtil.clickButtonWithID(SampleBrowserToolbar.REFRESH_BUTTON_ID);
    }
}
