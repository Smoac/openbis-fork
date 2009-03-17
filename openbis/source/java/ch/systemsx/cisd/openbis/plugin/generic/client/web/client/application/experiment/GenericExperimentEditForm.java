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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.InfoBoxCallbackListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ProjectSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EditableExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityEditForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.FormPanelListener;

/**
 * The <i>generic</i> experiment edit form.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericExperimentEditForm
        extends
        AbstractGenericEntityEditForm<ExperimentType, ExperimentTypePropertyType, ExperimentProperty, EditableExperiment>
{

    public static final String ID_PREFIX = createId(EntityKind.EXPERIMENT, "");

    private static final int DEFAULT_NUMBER_OF_ATTACHMENTS = 3;

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    private final AttachmentManager attachmentManager;

    private String sessionKey;

    private Html attachmentsInfo;

    private ProjectSelectionWidget projectChooser;

    private String originalProjectIdentifier;

    public GenericExperimentEditForm(IViewContext<IGenericClientServiceAsync> viewContext,
            EditableExperiment entity, boolean editMode)
    {
        super(viewContext, entity, editMode);
        this.viewContext = viewContext;
        sessionKey = createSimpleId(EntityKind.EXPERIMENT, entity.getId() + "");
        originalProjectIdentifier = entity.getProjectIdentifier();
        projectChooser =
                new ProjectSelectionWidget(viewContext, sessionKey, originalProjectIdentifier);
        FieldUtil.markAsMandatory(projectChooser);
        attachmentManager =
                new AttachmentManager(sessionKey, DEFAULT_NUMBER_OF_ATTACHMENTS, "New Attachment");
        addUploadFeatures(formPanel, sessionKey);
        formPanel.addListener(Events.Submit, new FormPanelListener(infoBox)
            {
                @Override
                protected void onSuccessfullUpload()
                {
                    save();
                }

                @Override
                protected void setUploadEnabled()
                {
                    // GenericExperimentRegistrationForm.this.setUploadEnabled(true);
                }
            });
        redefineSaveListeners();
    }

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
                            // setUploadEnabled(false);
                            formPanel.submit();
                        } else
                        {
                            save();
                        }
                    }
                }
            });
    }

    private void save()
    {
        final List<ExperimentProperty> properties = extractProperties();
        final String newProjectIdentifierOrNull = extractIdentifier();
        viewContext.getCommonService()
                .updateExperiment(sessionKey, entity.getIdentifier(), properties,
                        newProjectIdentifierOrNull, new RegisterExperimentCallback(viewContext));
    }

    private String extractIdentifier()
    {
        final String newIdentifier = projectChooser.tryGetSelectedProject().getIdentifier();
        if (originalProjectIdentifier.equals(newIdentifier))
        {
            return null;
        } else
        {
            return newIdentifier;
        }
    }

    @Override
    public final void submitValidForm()
    {
    }

    public final class RegisterExperimentCallback extends AbstractAsyncCallback<Void>
    {

        RegisterExperimentCallback(final IViewContext<?> viewContext)
        {
            super(viewContext, new InfoBoxCallbackListener<Void>(infoBox));
        }

        private final String createSuccessfullRegistrationInfo()
        {
            return "Experiment successfully updated";
        }

        @Override
        protected final void process(final Void result)
        {
            infoBox.displayInfo(createSuccessfullRegistrationInfo());
            showCheckPage();
        }
    }

    @Override
    protected PropertiesEditor<ExperimentType, ExperimentTypePropertyType, ExperimentProperty> createPropertiesEditor(
            List<ExperimentTypePropertyType> entityTypesPropertyTypes,
            List<ExperimentProperty> properties, String id,
            IViewContext<ICommonClientServiceAsync> context)
    {
        return new ExperimentPropertyEditor(entityTypesPropertyTypes, properties, id, context);
    }

    @Override
    protected List<Field<?>> getEntitySpecificFormFields()
    {
        List<Field<?>> fields = new ArrayList<Field<?>>();
        fields.add(projectChooser);
        for (FileUploadField f : attachmentManager.getFields())
        {
            fields.add(f);
        }
        return fields;
    }

    @Override
    protected List<Widget> getEntitySpecificCheckPageWidgets()
    {
        final ArrayList<Widget> widgets = new ArrayList<Widget>();
        widgets.add(attachmentsInfo = new Html());
        return widgets;
    }

    @Override
    protected void updateCheckPageWidgets()
    {
        projectChooser.updateOriginalValue();
        attachmentsInfo.setHtml(getAttachmentInfoText(attachmentManager.attachmentsDefined()));
    }

    public String getAttachmentInfoText(int attachmentDefined)
    {
        if (attachmentDefined > 0)
        {
            return Format.substitute("Added {0} new attachment{1}.", attachmentDefined,
                    attachmentDefined == 1 ? "" : "s");

        } else
        {
            return "No new attachments added.";
        }
    }
}
