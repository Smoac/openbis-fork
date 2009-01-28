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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment;

import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ExperimentTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.BrowserGridPagingToolBar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * A {@link AbstractDefaultTestCommand} extension for creating experiment list.
 * 
 * @author Izabela Adamczyk
 */
public class ListExperiments extends AbstractDefaultTestCommand
{
    private final String experimentTypeName;

    private final String projectCodeOrNull;

    public ListExperiments(final String projectNameOrNull, final String experimentTypeNameOrNull)
    {
        this.projectCodeOrNull = projectNameOrNull;
        this.experimentTypeName = experimentTypeNameOrNull;
        addCallbackClass(ProjectSelectionWidget.ListProjectsCallback.class);
        addCallbackClass(ExperimentTypeSelectionWidget.ListExperimentTypesCallback.class);
    }

    //
    // AbstractDefaultTestCommand
    //

    public void execute()
    {
        final ProjectSelectionWidget project =
                (ProjectSelectionWidget) GWTTestUtil.getWidgetWithID(ProjectSelectionWidget.ID
                        + ExperimentBrowserToolbar.ID);

        final ComboBox<ExperimentTypeModel> experimentTypeSelector =
                (ExperimentTypeSelectionWidget) GWTTestUtil
                        .getWidgetWithID(ExperimentTypeSelectionWidget.ID
                                + ExperimentBrowserToolbar.ID);

        GWTUtils.setSelectedItem(experimentTypeSelector, ModelDataPropertyNames.CODE,
                experimentTypeName);
        GWTUtils.setSelectedItem(project, ModelDataPropertyNames.CODE, projectCodeOrNull);

        final Button refresh =
                (Button) GWTTestUtil.getWidgetWithID(BrowserGridPagingToolBar.REFRESH_BUTTON_ID);
        assertTrue(refresh.isEnabled());
        GWTTestUtil.clickButtonWithID(BrowserGridPagingToolBar.REFRESH_BUTTON_ID);
    }
}
