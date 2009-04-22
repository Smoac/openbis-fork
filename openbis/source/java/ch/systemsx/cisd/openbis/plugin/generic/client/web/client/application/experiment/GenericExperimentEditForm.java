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

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField.wrapUnaware;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FileFieldManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.InfoBoxCallbackListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.EditableExperiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ProjectSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdates;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityEditForm;

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

    private final FileFieldManager attachmentManager;

    private String sessionKey;

    private Html attachmentsInfo;

    private Html samplesInfo;

    private ProjectSelectionWidget projectChooser;

    private ExperimentSamplesArea samplesArea;

    private String originalProjectIdentifier;

    public static DatabaseModificationAwareComponent create(
            IViewContext<IGenericClientServiceAsync> viewContext, EditableExperiment entity,
            boolean editMode)
    {
        GenericExperimentEditForm form =
                new GenericExperimentEditForm(viewContext, entity, editMode);
        return new DatabaseModificationAwareComponent(form, form);
    }

    private GenericExperimentEditForm(IViewContext<IGenericClientServiceAsync> viewContext,
            EditableExperiment entity, boolean editMode)
    {
        super(viewContext, entity, editMode);
        this.viewContext = viewContext;
        super.initializeComponents(viewContext);

        sessionKey = createSimpleId(EntityKind.EXPERIMENT, entity.getId() + "");
        originalProjectIdentifier = entity.getProjectIdentifier();
        setHeaderVisible(true);
        updateHeader();
        projectChooser =
                new ProjectSelectionWidget(viewContext, sessionKey, originalProjectIdentifier);
        FieldUtil.markAsMandatory(projectChooser);
        samplesArea = createSamplesArea();
        attachmentManager =
                new FileFieldManager(sessionKey, DEFAULT_NUMBER_OF_ATTACHMENTS, "New Attachment");
        addUploadFeatures(sessionKey);

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

    private ExperimentSamplesArea createSamplesArea()
    {
        ExperimentSamplesArea area =
                new ExperimentSamplesArea(viewContext, ID_PREFIX + entity.getIdentifier());
        area.setEnabled(false);
        area.setValue(viewContext.getMessage(Dict.LOAD_IN_PROGRESS));
        loadSamplesInBackground();
        return area;
    }

    private void loadSamplesInBackground()
    {
        final ListSampleCriteria sampleCriteria = new ListSampleCriteria();
        sampleCriteria.setExperimentIdentifier(entity.getIdentifier());
        viewContext.getCommonService().listSamples(sampleCriteria, true,
                new ListSamplesCallback(viewContext));
    }

    private class ListSamplesCallback extends AbstractAsyncCallback<ResultSet<Sample>>
    {

        public ListSamplesCallback(IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(ResultSet<Sample> result)
        {
            samplesArea.setSamples(result.getList());
            samplesArea.setEnabled(true);
        }
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
                        if (attachmentManager.filesDefined() > 0)
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
        ExperimentUpdates updates = new ExperimentUpdates();
        updates.setExperimentIdentifier(entity.getIdentifier());
        updates.setVersion(entity.getModificationDate());
        updates.setProperties(extractProperties());
        updates.setProjectIdentifier(extractProjectIdentifier());
        updates.setAttachmentSessionKey(sessionKey);
        updates.setSampleCodes(samplesArea.tryGetSampleCodes());
        viewContext.getCommonService().updateExperiment(updates,
                new RegisterExperimentCallback(viewContext));
    }

    private String extractProjectIdentifier()
    {
        return projectChooser.tryGetSelectedProject().getIdentifier();
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

    @SuppressWarnings("unchecked")
    @Override
    protected List<DatabaseModificationAwareField<?>> getEntitySpecificFormFields()
    {
        List<DatabaseModificationAwareField<?>> fields =
                new ArrayList<DatabaseModificationAwareField<?>>();
        fields.add(projectChooser.asDatabaseModificationAware());
        fields.add(wrapUnaware(samplesArea));
        for (FileUploadField f : attachmentManager.getFields())
        {
            fields.add(wrapUnaware(f));
        }
        return fields;
    }

    @Override
    protected List<Widget> getEntitySpecificCheckPageWidgets()
    {
        final ArrayList<Widget> widgets = new ArrayList<Widget>();
        widgets.add(attachmentsInfo = new Html());
        widgets.add(samplesInfo = new Html());
        return widgets;
    }

    @Override
    protected void updateCheckPageWidgets()
    {
        projectChooser.updateOriginalValue();
        samplesArea.updateOriginalValue(samplesArea.getValue());
        originalProjectIdentifier = projectChooser.tryGetSelectedProject().getIdentifier();
        entity.setIdentifier(originalProjectIdentifier + "/" + entity.getCode());
        attachmentsInfo.setHtml(getAttachmentInfoText(attachmentManager.filesDefined()));
        if (samplesArea.tryGetSampleCodes() != null)
        {
            samplesInfo.setHtml(viewContext.getMessage(Dict.SAMPLES) + ": "
                    + samplesArea.getValue());
        }
        updateHeader();
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

    private void updateHeader()
    {
        setHeading("Experiment " + entity.getIdentifier());
    }
}
