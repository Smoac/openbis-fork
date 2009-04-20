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
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AttachmentManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.GroupSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CodeField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;

/**
 * A {@link LayoutContainer} extension for registering and editing projects.
 * 
 * @author Izabela Adamczyk
 */
abstract class AbstractProjectEditRegisterForm extends AbstractRegistrationForm
{

    private static final int DEFAULT_NUMBER_OF_ATTACHMENTS = 3;

    final IViewContext<ICommonClientServiceAsync> viewContext;

    protected final CodeField projectCodeField;

    protected final VarcharField projectDescriptionField;

    protected final GroupSelectionWidget groupField;

    private final AttachmentManager attachmentManager;

    protected final String sessionKey;

    abstract protected void saveProject();

    abstract protected void setValues();

    protected AbstractProjectEditRegisterForm(
            final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this(viewContext, null, null);
    }

    protected AbstractProjectEditRegisterForm(
            final IViewContext<ICommonClientServiceAsync> viewContext, Long projectIdOrNull,
            String groupCodeOrNull)
    {
        super(viewContext, createId(projectIdOrNull), DEFAULT_LABEL_WIDTH + 20, DEFAULT_FIELD_WIDTH);
        sessionKey = createId(projectIdOrNull);
        attachmentManager =
                new AttachmentManager(sessionKey, DEFAULT_NUMBER_OF_ATTACHMENTS, "Attachment");
        this.viewContext = viewContext;
        projectCodeField = createProjectCodeField();
        groupField = createGroupField(groupCodeOrNull);
        projectDescriptionField = createProjectDescriptionField();
        addUploadFeatures(formPanel, sessionKey);
    }

    public static String createId(Long id)
    {
        String editOrRegister = (id == null) ? "register" : ("edit_" + id);
        return GenericConstants.ID_PREFIX + "project-" + editOrRegister + "_form";
    }

    GroupSelectionWidget getGroupField()
    {
        return groupField;
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

    private final GroupSelectionWidget createGroupField(String code)
    {
        GroupSelectionWidget field = new GroupSelectionWidget(viewContext, getId(), false, code);
        FieldUtil.markAsMandatory(field);
        field.setFieldLabel(viewContext.getMessage(Dict.GROUP));
        return field;
    }

    private final void addFormFields()
    {
        formPanel.add(projectCodeField);
        formPanel.add(groupField);
        formPanel.add(projectDescriptionField);
        for (FileUploadField attachmentField : attachmentManager.getFields())
        {
            formPanel.add(wrapUnaware((Field<?>) attachmentField).get());
        }
        formPanel.addListener(Events.Submit, new FormPanelListener(infoBox)
            {
                @Override
                protected void onSuccessfullUpload()
                {
                    saveProject();
                }

                @Override
                protected void setUploadEnabled()
                {
                    AbstractProjectEditRegisterForm.this.setUploadEnabled(true);
                }
            });
        redefineSaveListeners();
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
                            saveProject();
                        }
                    }
                }
            });
    }

    @Override
    protected final void submitValidForm()
    {
    }

    @Override
    protected final void onRender(final Element target, final int index)
    {
        super.onRender(target, index);
        addFormFields();
        setValues();
    }

}
