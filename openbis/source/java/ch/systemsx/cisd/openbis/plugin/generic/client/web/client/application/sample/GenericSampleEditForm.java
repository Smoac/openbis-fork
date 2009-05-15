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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample;

import static ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField.wrapUnaware;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FileFieldManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ExperimentChooserField.ExperimentChooserFieldAdaptor;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.PropertiesEditor;

/**
 * The <i>generic</i> sample edit form.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericSampleEditForm extends
        AbstractGenericEntityRegistrationForm<SampleType, SampleTypePropertyType, SampleProperty>
{
    private static final int DEFAULT_NUMBER_OF_ATTACHMENTS = 3;

    private FileFieldManager attachmentManager;

    private String sessionKey;

    private Sample originalSample;

    // null if sample cannot be attached to an experiment
    private ExperimentChooserFieldAdaptor experimentFieldOrNull;

    public static DatabaseModificationAwareComponent create(
            IViewContext<IGenericClientServiceAsync> viewContext,
            IIdentifiable identifiable)
    {
        GenericSampleEditForm form = new GenericSampleEditForm(viewContext, identifiable);
        return new DatabaseModificationAwareComponent(form, form);
    }

    private GenericSampleEditForm(IViewContext<IGenericClientServiceAsync> viewContext,
            IIdentifiable identifiable)
    {
        super(viewContext, identifiable, EntityKind.SAMPLE);
        sessionKey = createSimpleId(identifiable, EntityKind.SAMPLE);
        addUploadFeatures(sessionKey);
    }

    private ExperimentChooserFieldAdaptor createExperimentField()
    {
        String label = viewContext.getMessage(Dict.EXPERIMENT);
        Experiment experiment = originalSample.getExperiment();
        ExperimentIdentifier originalExperiment =
                experiment == null ? null : ExperimentIdentifier.createIdentifier(experiment);
        return ExperimentChooserField.create(label, false, originalExperiment, viewContext
                .getCommonViewContext());
    }

    @Override
    public final void submitValidForm()
    {
    }

    private void save()
    {
        final List<SampleProperty> properties = extractProperties();
        ExperimentIdentifier experimentIdent =
                experimentFieldOrNull != null ? experimentFieldOrNull.getValue() : null;
        viewContext.getService().updateSample(sessionKey, originalSample.getIdentifier(),
                properties, experimentIdent, originalSample.getModificationDate(),
                new UpdateSampleCallback(viewContext));
    }

    public final class UpdateSampleCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<Date>
    {

        UpdateSampleCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final Date result)
        {
            originalSample.setModificationDate(result);
            updateOriginalValues();
            super.process(result);
        }

        @Override
        protected String createSuccessfullRegistrationInfo(Date result)
        {
            return "Sample successfully updated";
        }
    }

    public void updateOriginalValues()
    {
        updatePropertyFieldsOriginalValues();
        if (experimentFieldOrNull != null)
            experimentFieldOrNull.updateOriginalValue();
    }

    @Override
    protected PropertiesEditor<SampleType, SampleTypePropertyType, SampleProperty> createPropertiesEditor(

    String id, IViewContext<ICommonClientServiceAsync> context)
    {
        SamplePropertyEditor editor = new SamplePropertyEditor(id, context);
        return editor;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<DatabaseModificationAwareField<?>> getEntitySpecificFormFields()
    {
        ArrayList<DatabaseModificationAwareField<?>> fields =
                new ArrayList<DatabaseModificationAwareField<?>>();
        if (experimentFieldOrNull != null)
        {
            fields.add(wrapUnaware(experimentFieldOrNull.getField()));
        }
        for (FileUploadField f : attachmentManager.getFields())
        {
            fields.add(DatabaseModificationAwareField.wrapUnaware(f));
        }
        return fields;
    }

    private static boolean canAttachToExperiment(Sample sample)
    {
        return sample.getGroup() != null;
    }

    @Override
    protected void createEntitySpecificFormFields()
    {
        experimentFieldOrNull =
                canAttachToExperiment(originalSample) ? createExperimentField() : null;
        attachmentManager =
                new FileFieldManager(sessionKey, DEFAULT_NUMBER_OF_ATTACHMENTS, "New Attachment");
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
                    GenericSampleEditForm.this.setUploadEnabled(true);
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
                        if (attachmentManager.filesDefined() > 0)
                        {
                            setUploadEnabled(false);
                            formPanel.submit();
                        } else
                        {
                            save();
                        }
                    }
                }
            });
    }

    private void setOriginalSample(Sample sample)
    {
        this.originalSample = sample;
    }

    @Override
    protected void initializeFormFields()
    {
        propertiesEditor.initWithProperties(originalSample.getSampleType()
                .getAssignedPropertyTypes(), originalSample.getProperties());
        codeField.setValue(originalSample.getCode());

    }

    @Override
    protected void loadForm()
    {
        String sampleIdentifier = identifiableOrNull.getIdentifier();
        viewContext.getService().getSampleInfo(sampleIdentifier,
                new SampleInfoCallback(viewContext));
    }

    public final class SampleInfoCallback extends AbstractAsyncCallback<Sample>
    {

        private SampleInfoCallback(final IViewContext<IGenericClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected final void process(final Sample result)
        {
            setOriginalSample(result);
            initGUI();
        }
    }

}
