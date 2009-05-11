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
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FileFieldManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.FormPanelListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ProjectSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdateResult;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.AbstractGenericEntityRegistrationForm;

/**
 * The <i>generic</i> experiment edit form.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericExperimentEditForm
        extends
        AbstractGenericEntityRegistrationForm<ExperimentType, ExperimentTypePropertyType, ExperimentProperty>
{

    private static final int DEFAULT_NUMBER_OF_ATTACHMENTS = 3;

    private FileFieldManager attachmentManager;

    private String attachmentsSessionKey;

    private ProjectSelectionWidget projectChooser;

    private ExperimentSamplesArea samplesArea;

    private final String samplesSessionKey;

    private FileFieldManager importSamplesFileManager;

    private SampleTypeSelectionWidget importSampleTypeSelection;

    private Radio existingSamplesRadio;

    private Radio importSamplesRadio;

    private CheckBox autoGenerateCodes;

    private Experiment originalExperiment;

    private String simpleId;

    public static DatabaseModificationAwareComponent create(
            IViewContext<IGenericClientServiceAsync> viewContext, IIdentifierHolder identifierHolder)
    {
        GenericExperimentEditForm form =
                new GenericExperimentEditForm(viewContext, identifierHolder);
        return new DatabaseModificationAwareComponent(form, form);
    }

    private GenericExperimentEditForm(IViewContext<IGenericClientServiceAsync> viewContext,
            IIdentifierHolder identifierHolder)
    {
        super(viewContext, identifierHolder, EntityKind.EXPERIMENT);

        simpleId = createSimpleId(identifierHolderOrNull, EntityKind.EXPERIMENT);
        attachmentsSessionKey = simpleId + "_attachments";
        samplesSessionKey = simpleId + "_samples";
        List<String> sesionKeys = new ArrayList<String>();
        sesionKeys.add(attachmentsSessionKey);
        sesionKeys.add(samplesSessionKey);
        addUploadFeatures(sesionKeys);
    }

    // TODO 2009-04-22, IA: merge this class with GenericExperimentRegistrationForm
    private void updateSamples()
    {
        Boolean useExistingSamples = existingSamplesRadio.getValue();
        samplesArea.setVisible(useExistingSamples);
        samplesArea.setEnabled(useExistingSamples);
        importSampleTypeSelection.setVisible(useExistingSamples == false);
        importSampleTypeSelection.setEnabled(useExistingSamples == false);
        autoGenerateCodes.setVisible(useExistingSamples == false);
        autoGenerateCodes.setEnabled(useExistingSamples == false);
        for (FileUploadField samplesFileField : importSamplesFileManager.getFields())
        {
            samplesFileField.setVisible(useExistingSamples == false);
            samplesFileField.setEnabled(useExistingSamples == false);
            samplesFileField.validate();
        }
        samplesArea.validate();
        importSampleTypeSelection.validate();
    }

    private ExperimentSamplesArea createSamplesArea()
    {
        ExperimentSamplesArea area = new ExperimentSamplesArea(viewContext, simpleId);
        area.setEnabled(false);
        area.setValue(viewContext.getMessage(Dict.LOAD_IN_PROGRESS));
        loadSamplesInBackground();
        return area;
    }

    private void loadSamplesInBackground()
    {
        final ListSampleCriteria sampleCriteria =
                ListSampleCriteria.createForExperiment(identifierHolderOrNull.getIdentifier());
        viewContext.getCommonService().listSamples(sampleCriteria,
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

    private void redefineSaveListeners()
    {
        saveButton.removeAllListeners();
        saveButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    if (formPanel.isValid())
                    {
                        if (attachmentManager.filesDefined() > 0
                                || importSamplesFileManager.filesDefined() > 0)
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

    private void save()
    {
        ExperimentUpdates updates = new ExperimentUpdates();
        updates.setExperimentIdentifier(originalExperiment.getIdentifier());
        updates.setVersion(originalExperiment.getModificationDate());
        updates.setProperties(extractProperties());
        updates.setProjectIdentifier(extractProjectIdentifier());
        updates.setAttachmentSessionKey(attachmentsSessionKey);
        updates.setSampleCodes(getSamples());
        updates.setSampleType(getSampleType());
        updates.setGenerateCodes(autoGenerateCodes.getValue().booleanValue());
        updates.setRegisterSamples(existingSamplesRadio.getValue() == false);
        updates.setSamplesSessionKey(samplesSessionKey);
        viewContext.getService().updateExperiment(updates,
                new UpdateExperimentCallback(viewContext));
    }

    private String extractProjectIdentifier()
    {
        return projectChooser.tryGetSelectedProject().getIdentifier();
    }

    @Override
    public final void submitValidForm()
    {
    }

    public final class UpdateExperimentCallback extends
            AbstractRegistrationForm.AbstractRegistrationCallback<ExperimentUpdateResult>
    {

        UpdateExperimentCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected void process(final ExperimentUpdateResult result)
        {
            originalExperiment.setModificationDate(result.getModificationDate());
            updateOriginalValues(result.getSamples());
            super.process(result);
        }

        @Override
        protected String createSuccessfullRegistrationInfo(ExperimentUpdateResult result)
        {
            return "Experiment successfully updated";
        }

    }

    @Override
    protected PropertiesEditor<ExperimentType, ExperimentTypePropertyType, ExperimentProperty> createPropertiesEditor(
            String id, IViewContext<ICommonClientServiceAsync> context)
    {
        ExperimentPropertyEditor editor = new ExperimentPropertyEditor(id, context);
        return editor;
    }

    public void updateOriginalValues(String[] samples)
    {
        updatePropertyFieldsOriginalValues();
        updateFieldOriginalValue(projectChooser);
        samplesArea.setSampleCodes(samples);
    }

    RadioGroup createSamplesSourceRadio(Radio existing, Radio importFromFile)
    {
        RadioGroup result = new RadioGroup();
        result.setSelectionRequired(true);
        result.setFieldLabel(viewContext.getMessage(Dict.SAMPLES));
        result.setOrientation(Orientation.HORIZONTAL);
        result.add(existing);
        result.add(importFromFile);
        result.addListener(Events.Change, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    updateSamples();
                }
            });
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<DatabaseModificationAwareField<?>> getEntitySpecificFormFields()
    {
        List<DatabaseModificationAwareField<?>> fields =
                new ArrayList<DatabaseModificationAwareField<?>>();
        fields.add(projectChooser.asDatabaseModificationAware());
        fields.add(wrapUnaware(createSamplesSourceRadio(existingSamplesRadio, importSamplesRadio)));
        fields.add(wrapUnaware(samplesArea));
        fields.add(importSampleTypeSelection.asDatabaseModificationAware());
        for (FileUploadField samplesFileField : importSamplesFileManager.getFields())
        {
            fields.add(wrapUnaware((Field<?>) samplesFileField));
        }
        fields.add(wrapUnaware(autoGenerateCodes));

        for (FileUploadField f : attachmentManager.getFields())
        {
            fields.add(wrapUnaware(f));
        }
        return fields;
    }

    private String[] getSamples()
    {
        if (existingSamplesRadio.getValue())
            return samplesArea.tryGetSampleCodes();
        else
            return null;
    }

    private SampleType getSampleType()
    {
        if (existingSamplesRadio.getValue() == false)
            return importSampleTypeSelection.tryGetSelectedSampleType();
        else
            return null;
    }

    private void setOriginalExperiment(Experiment experiment)
    {
        this.originalExperiment = experiment;
    }

    @Override
    protected void createEntitySpecificFormFields()
    {
        projectChooser = new ProjectSelectionWidget(viewContext, simpleId);
        FieldUtil.markAsMandatory(projectChooser);

        samplesArea = createSamplesArea();

        importSamplesFileManager = new FileFieldManager(samplesSessionKey, 1, "File");
        importSamplesFileManager.setMandatory();
        importSampleTypeSelection = new SampleTypeSelectionWidget(viewContext, simpleId, false);
        FieldUtil.markAsMandatory(importSampleTypeSelection);

        existingSamplesRadio = GenericExperimentRegistrationForm.cerateExistingSamplesRadio();
        importSamplesRadio = GenericExperimentRegistrationForm.createImportRadio();
        autoGenerateCodes = GenericExperimentRegistrationForm.createAutoGenerateCheckbox();

        attachmentManager =
                new FileFieldManager(attachmentsSessionKey, DEFAULT_NUMBER_OF_ATTACHMENTS,
                        "New Attachment");
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
                    GenericExperimentEditForm.this.setUploadEnabled(true);
                }
            });
        redefineSaveListeners();

    }

    @Override
    protected void initializeFormFields()
    {
        propertiesEditor.initWithProperties(originalExperiment.getExperimentType()
                .getAssignedPropertyTypes(), originalExperiment.getProperties());
        updateSamples();
        codeField.setValue(originalExperiment.getCode());
        projectChooser.selectProjectAndUpdateOriginal(originalExperiment.getProject()
                .getIdentifier());
    }

    @Override
    protected void loadForm()
    {
        String experimentIdentifier = identifierHolderOrNull.getIdentifier();
        viewContext.getService().getExperimentInfo(experimentIdentifier,
                new ExperimentInfoCallback(viewContext));
    }

    public final class ExperimentInfoCallback extends AbstractAsyncCallback<Experiment>
    {

        private ExperimentInfoCallback(final IViewContext<IGenericClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected final void process(final Experiment result)
        {
            setOriginalExperiment(result);
            initGUI();
        }
    }
}
