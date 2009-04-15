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

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField.wrapUnaware;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AttachmentManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;

/**
 * A {@link LayoutContainer} extension for registering a new project.
 * 
 * @author Izabela Adamczyk
 */
public final class ProjectRegistrationForm extends AbstractRegistrationForm
{
    public static final String ID = GenericConstants.ID_PREFIX + "project-registration_form";

    public static final String SESSION_KEY = ID;

    private static final int DEFAULT_NUMBER_OF_ATTACHMENTS = 3;

    final IViewContext<ICommonClientServiceAsync> viewContext;

    private CodeField projectCodeField;

    private VarcharField projectDescriptionField;

    private GroupSelectionWidget groupField;

    private AttachmentManager attachmentManager =
            new AttachmentManager(SESSION_KEY, DEFAULT_NUMBER_OF_ATTACHMENTS, "Attachment");

    public static DatabaseModificationAwareComponent create(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        ProjectRegistrationForm form = new ProjectRegistrationForm(viewContext);
        return new DatabaseModificationAwareComponent(form, form.groupField);
    }

    private ProjectRegistrationForm(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, ID, DEFAULT_LABEL_WIDTH + 20, DEFAULT_FIELD_WIDTH);
        this.viewContext = viewContext;
        addFields();
        addUploadFeatures(formPanel, SESSION_KEY);
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
        FieldUtil.markAsMandatory(field);
        field.setFieldLabel(viewContext.getMessage(Dict.GROUP));
        return field;
    }

    private final void addFields()
    {
        formPanel.add(projectCodeField = createProjectCodeField());
        formPanel.add(groupField = createGroupField());
        formPanel.add(projectDescriptionField = createProjectDescriptionField());
        for (FileUploadField attachmentField : attachmentManager.getFields())
        {
            formPanel.add(wrapUnaware((Field<?>) attachmentField).get());
        }
        formPanel.addListener(Events.Submit, new FormPanelListener(infoBox)
            {
                @Override
                protected void onSuccessfullUpload()
                {
                    registerProject();
                }

                @Override
                protected void setUploadEnabled()
                {
                    ProjectRegistrationForm.this.setUploadEnabled(true);
                }
            });
        redefineSaveListeners();
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
    void redefineSaveListeners()
    {
        saveButton.removeAllListeners();
        saveButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    if (formPanel.isValid())
                    {
                        if (attachmentManager.attachmentsDefined() > 0)
                        {
                            setUploadEnabled(false);
                            formPanel.submit();
                        } else
                        {
                            registerProject();
                        }
                    }
                }
            });
    }

    @Override
    protected final void submitValidForm()
    {
    }

    private void registerProject()
    {
        final Project project = createProject();
        viewContext.getService().registerProject(SESSION_KEY, project,
                new ProjectRegistrationCallback(viewContext, project));
    }

    public final class ProjectRegistrationCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback
    {
        private final Project project;

        ProjectRegistrationCallback(final IViewContext<?> viewContext, final Project project)
        {
            super(viewContext);
            this.project = project;
        }

        @Override
        protected String createSuccessfullRegistrationInfo()
        {
            return "Project <b>" + project.getIdentifier().toUpperCase()
                    + "</b> successfully registered.";
        }

    }
}
