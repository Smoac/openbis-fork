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

import com.extjs.gxt.ui.client.widget.form.ComboBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ExperimentTypeModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * A {@link AbstractDefaultTestCommand} extension for creating experiment list.
 * 
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
public class ListExperiments extends AbstractDefaultTestCommand
{
    private final String experimentTypeName;

    private final String projectCodeOrNull;

    public ListExperiments(final String projectNameOrNull, final String experimentTypeNameOrNull)
    {
        this.projectCodeOrNull = projectNameOrNull;
        this.experimentTypeName = experimentTypeNameOrNull;
        addCallbackClass(ProjectSelectionTreeWidget.ListProjectsCallback.class);
        addCallbackClass(ExperimentTypeSelectionWidget.ListItemsCallback.class);
    }

    //
    // AbstractDefaultTestCommand
    //

    public void execute()
    {
        final ProjectSelectionTreeWidget projectSelector =
                (ProjectSelectionTreeWidget) GWTTestUtil
                        .getWidgetWithID(ProjectSelectionTreeWidget.ID);

        final ComboBox<ExperimentTypeModel> experimentTypeSelector =
                (ExperimentTypeSelectionWidget) GWTTestUtil
                        .getWidgetWithID(ExperimentTypeSelectionWidget.ID
                                + ExperimentTypeSelectionWidget.SUFFIX
                                + ExperimentBrowserToolbar.ID);

        GWTUtils.setSelectedItem(projectSelector,
                ProjectSelectionTreeWidget.PROJECT_WITH_GROUP_CODE, projectCodeOrNull);
        GWTUtils.setSelectedItem(experimentTypeSelector, ModelDataPropertyNames.CODE,
                experimentTypeName);
    }
}
