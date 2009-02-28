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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.util.GridTestUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * A {@link AbstractDefaultTestCommand} extension for creating sample list.
 * 
 * @author Christian Ribeaud
 */
public class ListSamples extends AbstractDefaultTestCommand
{
    private final String sampleTypeNameOrNull;

    private final String groupNameOrNull;

    public ListSamples(final String groupNameOrNull, final String sampleTypeNameOrNull)
    {
        this.groupNameOrNull = groupNameOrNull;
        this.sampleTypeNameOrNull = sampleTypeNameOrNull;
        addCallbackClass(GroupSelectionWidget.ListGroupsCallback.class);
        addCallbackClass(SampleTypeSelectionWidget.ListItemsCallback.class);
    }

    //
    // AbstractDefaultTestCommand
    //

    public void execute()
    {
        final GroupSelectionWidget groupSelector =
                (GroupSelectionWidget) GWTTestUtil.getWidgetWithID(GroupSelectionWidget.ID
                        + GroupSelectionWidget.SUFFIX + SampleBrowserToolbar.ID);

        final ComboBox<SampleTypeModel> sampleTypeSelector =
                (SampleTypeSelectionWidget) GWTTestUtil
                        .getWidgetWithID(SampleTypeSelectionWidget.ID
                                + SampleTypeSelectionWidget.SUFFIX + SampleBrowserToolbar.ID);

        GWTUtils.setSelectedItem(sampleTypeSelector, ModelDataPropertyNames.CODE,
                sampleTypeNameOrNull);
        GWTUtils.setSelectedItem(groupSelector, ModelDataPropertyNames.CODE, groupNameOrNull);

        GridTestUtils.pressRefreshGridButton();
    }
}
