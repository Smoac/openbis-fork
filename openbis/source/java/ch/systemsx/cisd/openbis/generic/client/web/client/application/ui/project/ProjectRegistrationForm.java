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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.project;

import com.extjs.gxt.ui.client.widget.LayoutContainer;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.InfoBoxCallbackListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;

/**
 * A {@link LayoutContainer} extension for registering a new project.
 * 
 * @author Izabela Adamczyk
 */
public final class ProjectRegistrationForm extends AbstractRegistrationForm
{
    public static final String ID = GenericConstants.ID_PREFIX + "project-registration_form";

    final IViewContext<ICommonClientServiceAsync> viewContext;

    private CodeField projectCodeField;

    private VarcharField projectDescriptionField;

    private GroupSelectionWidget groupField;

    public ProjectRegistrationForm(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, ID, DEFAULT_LABEL_WIDTH + 20, DEFAULT_FIELD_WIDTH);
        this.viewContext = viewContext;
        addFields();
    }

    private final CodeField createProjectCodeField()
    {
        final CodeField codeField =
                new CodeField(viewContext, viewContext.getMessage(Dict.CODE),
                        CodeField.CODE_PATTERN_WITH_DOT);
        codeField.setId(getId() + "_code");
        return codeField;
    }

    private final VarcharField createProjectDescriptionField()
    {
        final VarcharField varcharField =
                new VarcharField(viewContext.getMessage(Dict.DESCRIPTION), false);
        varcharField.setId(getId() + "_description");
        varcharField.setMaxLength(80);
        return varcharField;
    }

    private final GroupSelectionWidget createGroupField()
    {
        GroupSelectionWidget field = new GroupSelectionWidget(viewContext, getId(), false);
        field.setLabelSeparator(GenericConstants.MANDATORY_LABEL_SEPARATOR);
        field.setFieldLabel(viewContext.getMessage(Dict.GROUP));
        field.setAllowBlank(false);
        return field;
    }

    private final void addFields()
    {
        formPanel.add(projectCodeField = createProjectCodeField());
        formPanel.add(groupField = createGroupField());
        formPanel.add(projectDescriptionField = createProjectDescriptionField());
    }

    private final Project createProject()
    {
        final Project project = new Project();
        project.setDescription(projectDescriptionField.getValue());
        project.setIdentifier(groupField.tryGetSelectedGroup().getIdentifier() + "/"
                + projectCodeField.getValue());
        return project;
    }

    //
    // AbstractRegistrationForm
    //

    @Override
    protected final void submitValidForm()
    {
        final Project project = createProject();
        viewContext.getService().registerProject(project,
                new ProjectRegistrationCallback(viewContext, project));
    }

    public final class ProjectRegistrationCallback extends AbstractAsyncCallback<Void>
    {
        private final Project project;

        ProjectRegistrationCallback(final IViewContext<?> viewContext, final Project project)
        {
            super(viewContext, new InfoBoxCallbackListener<Void>(infoBox));
            this.project = project;
        }

        private final String createMessage()
        {
            return "Project <b>" + project.getIdentifier().toUpperCase()
                    + "</b> successfully registered.";
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        protected final void process(final Void result)
        {
            infoBox.displayInfo(createMessage());
            formPanel.reset();
        }
    }

}
